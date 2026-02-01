package com.example.demo.service;

import com.example.demo.dto.EnrichmentRequestDTO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.annotation.EnableScheduling;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.annotation.PostConstruct;

@Service
@RequiredArgsConstructor
@Slf4j
@EnableScheduling
public class EnrichmentService {

    @Value("${r.scripts.path}")
    private String rScriptsPath;

    @Value("${r.temp.dir}")
    private String tempDir;

    @Value("${r.eggnog.emfile.path}")
    private String emfilePath;

    private final Map<String, Map<String, Object>> taskResults = new ConcurrentHashMap<>();

    @Value("${r.lib.path}")
    private String rLibPath;

    @Value("${r.bin.path}")
    private String rBinPath;

    // 添加初始化方法
    @PostConstruct
    public void init() {
        log.info("===== R 环境初始化 =====");
        testRInstallation();
        validatePaths();
    }

    private void testRInstallation() {
        try {
            Process process = Runtime.getRuntime().exec("Rscript --version");
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                StringBuilder output = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
                int exitCode = process.waitFor();
                if (exitCode == 0) {
                    log.info("✅ Rscript 版本:\n{}", output);
                } else {
                    log.error("❌ Rscript 测试失败，退出码: {}", exitCode);
                }
            }
        } catch (Exception e) {
            log.error("Rscript 命令测试失败", e);
        }
    }

    private void validatePaths() {
        log.info("验证路径...");
        validatePath("R脚本路径", rScriptsPath);
        validatePath("临时目录", tempDir);
        validatePath("Emfile路径", emfilePath);
        validatePath("R库路径", rLibPath);

        // 验证关键文件存在
        validateFileExists("GO脚本", Paths.get(rScriptsPath, "GO_enrichment.R"));
        validateFileExists("KEGG脚本", Paths.get(rScriptsPath, "KEGG_enrichment.R"));
    }

    private void validatePath(String name, String path) {
        Path pathObj = Paths.get(path);
        if (Files.exists(pathObj)) {
            log.info("✅ {} 存在: {}", name, pathObj.toAbsolutePath());
        } else {
            log.error("❌ {} 不存在: {}", name, pathObj.toAbsolutePath());
        }
    }

    private void validateFileExists(String name, Path filePath) {
        if (Files.exists(filePath)) {
            log.info("✅ {} 存在: {}", name, filePath.toAbsolutePath());
        } else {
            log.error("❌ {} 不存在: {}", name, filePath.toAbsolutePath());
        }
    }

    public void runAnalysis(String taskId, EnrichmentRequestDTO request) {
        log.info("===== 开始分析任务: {} =====", taskId);
        log.info("分析类型: {}", request.getAnalysisType());
        log.info("基因数量: {}", request.getGenes().size());

        try {
            // 创建临时目录
            Path taskDir = createTaskDirectory(taskId);

            // 创建基因文件
            Path geneFile = createGeneFile(taskDir, request.getGenes());

            // 准备结果文件路径
            Path resultFile = taskDir.resolve("results.json");
            Path imageFile = taskDir.resolve("chart.png");

            // 构建并执行命令
            List<String> command = buildCommand(request, geneFile, resultFile, imageFile);
            executeRCommand(command, taskDir);

            // 处理结果
            Map<String, Object> results = processResults(request, resultFile, imageFile);
            taskResults.put(taskId, results);

        } catch (Exception e) {
            handleAnalysisError(taskId, e);
        }
    }

    private Path createTaskDirectory(String taskId) throws IOException {
        Path taskDir = Paths.get(tempDir, "enrichment", taskId);
        if (!Files.exists(taskDir)) {
            Files.createDirectories(taskDir);
            log.info("创建临时目录: {}", taskDir);

            // Windows权限设置
            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                try {
                    Runtime.getRuntime().exec(
                            String.format("icacls \"%s\" /grant Everyone:(OI)(CI)F", taskDir)
                    );
                } catch (IOException e) {
                    log.warn("设置目录权限失败，但将继续执行", e);
                }
            }
        }
        return taskDir;
    }

    private Path createGeneFile(Path taskDir, List<String> genes) throws IOException {
        Path geneFile = taskDir.resolve("genes.txt");
        if (!genes.isEmpty()) {
            // 确保文件以换行符结束
            String geneContent = String.join("\n", genes) + "\n";
            Files.writeString(geneFile, geneContent, StandardCharsets.UTF_8);
            log.info("基因文件创建成功: {}", geneFile);
        } else {
            throw new IllegalArgumentException("基因列表不能为空");
        }
        return geneFile;
    }

    private List<String> buildCommand(EnrichmentRequestDTO request,
                                      Path geneFile, Path resultFile, Path imageFile) {
        List<String> command = new ArrayList<>();
        command.add("Rscript");

        // 确保使用绝对路径
        String rScript = request.getAnalysisType().equals("GO")
                ? "GO_enrichment.R"
                : "KEGG_enrichment.R";

        Path scriptPath = Paths.get(rScriptsPath, rScript);
        command.add(scriptPath.toAbsolutePath().toString());

        //添加显式参数
        command.add("--input");
        command.add(geneFile.toString());
        command.add("--output");
        command.add(resultFile.toString());
        command.add("--image");
        command.add(imageFile.toString());

        // 仅KEGG添加emfile参数
        if ("KEGG".equals(request.getAnalysisType())) {
            command.add("--emfile");
            command.add(emfilePath);
        }

        log.info("执行命令: {}", String.join(" ", command));
        return command;
    }

    private void executeRCommand(List<String> command, Path taskDir)
            throws IOException, InterruptedException {

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.directory(taskDir.toFile());
        pb.redirectErrorStream(false);

        // 获取环境变量并安全处理
        Map<String, String> env = pb.environment();
        env.put("R_LIBS_USER", rLibPath);
        env.put("R_LIBS", rLibPath); // 添加额外的环境变量


        String currentPath = System.getenv("PATH");
        if (currentPath == null) currentPath = "";
        if (!currentPath.contains(rBinPath)) {
            env.put("PATH", rBinPath + ":" + currentPath);
        }

        log.info("执行命令: {}", String.join(" ", command));
        log.info("工作目录: {}", taskDir.toAbsolutePath());
        log.info("环境变量 R_LIBS_USER = {}", env.get("R_LIBS_USER"));
        log.info("环境变量 PATH = {}", env.get("PATH"));


        Process process = pb.start();

        // 使用线程读取标准输出
        Thread outputThread = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    log.info("R [OUT] >> {}", line);
                }
            } catch (IOException e) {
                log.error("读取R标准输出失败", e);
            }
        });

        // 使用线程读取错误输出
        Thread errorThread = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getErrorStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    log.error("R [ERR] >> {}", line);
                }
            } catch (IOException e) {
                log.error("读取R错误输出失败", e);
            }
        });

        outputThread.start();
        errorThread.start();

        int exitCode = process.waitFor();

        // 等待线程完成
        outputThread.join();
        errorThread.join();

        log.info("R脚本退出码: {}", exitCode);

        if (exitCode != 0) {
            // 检查错误日志文件
            Path errorLog = taskDir.resolve("error.log");
            if (Files.exists(errorLog)) {
                String errorContent = Files.readString(errorLog);
                log.error("R错误日志内容:\n{}", errorContent);
            }
            throw new RuntimeException("R脚本执行失败，退出码: " + exitCode);
        }
    }

    private void ensureBasicEnvVars(Map<String, String> env) {
        // 确保PATH存在
        if (!env.containsKey("PATH") || env.get("PATH") == null) {
            String defaultPath = "C:/Windows/system32;C:/Windows;C:/Windows/System32/Wbem";
            env.put("PATH", defaultPath);
            log.warn("PATH环境变量不存在，设置默认值: {}", defaultPath);
        }

        // 确保系统根目录存在
        if (!env.containsKey("SystemRoot")) {
            env.put("SystemRoot", "C:/Windows");
        }

        // 确保临时目录存在
        if (!env.containsKey("TEMP")) {
            env.put("TEMP", System.getProperty("java.io.tmpdir"));
        }
    }

    private void logProcessOutput(Process process) {
        // 创建两个线程分别处理 stdout 和 stderr
        Thread outThread = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    // 添加前缀区分输出源
                    log.info("R [OUT] >> {}", line);
                }
            } catch (IOException e) {
                log.error("读取R输出失败", e);
            }
        });

        Thread errThread = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getErrorStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    // 记录为警告级别
                    log.warn("R [ERR] >> {}", line);
                }
            } catch (IOException e) {
                log.error("读取R错误流失败", e);
            }
        });

        outThread.start();
        errThread.start();

        try {
            outThread.join();
            errThread.join();
        } catch (InterruptedException e) {
            log.error("等待输出线程失败", e);
            Thread.currentThread().interrupt();
        }
    }

    private Map<String, Object> processResults(EnrichmentRequestDTO request,
                                               Path resultFile, Path imageFile)
            throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        final Map<String, Object> resultData;  // 声明为 final
        if (Files.exists(resultFile)) {
            resultData = mapper.readValue(resultFile.toFile(),
                    new TypeReference<Map<String, Object>>() {});
        } else {
            resultData = new HashMap<>();
        }

        final String imageBase64;  // 声明为 final
        if (Files.exists(imageFile)) {
            byte[] imageBytes = Files.readAllBytes(imageFile);
            imageBase64 = "data:image/png;base64," +
                    Base64.getEncoder().encodeToString(imageBytes);
        } else {
            imageBase64 = "";
        }

        // 新增日志：打印base64长度（确认非空）
        if (!imageBase64.isEmpty()) {
            log.info("图表base64生成成功，长度: {} 字符", imageBase64.length());
        } else {
            log.error("图表base64生成失败，imageFile是否存在: {}", Files.exists(imageFile));
        }

        return new HashMap<>() {{
            put("status", "completed");
            put("analysis_type", request.getAnalysisType());
            put("chart_image", imageBase64);  // 现在可正常访问 final 变量
            put("results", resultData);
        }};
    }

    private void handleAnalysisError(String taskId, Exception e) {
        log.error("分析失败: {}", e.getMessage(), e);

        Map<String, Object> errorResult = new HashMap<>();
        errorResult.put("status", "error");
        errorResult.put("error", e.toString());
        errorResult.put("stacktrace", Arrays.toString(e.getStackTrace()));

        taskResults.put(taskId, errorResult);
    }

    public Path exportToCsv(String taskId, List<Map<String, Object>> items) throws IOException {
        Path exportFile = Paths.get(tempDir, "enrichment", taskId, "export_result.csv");
        log.info("生成CSV文件路径: {}", exportFile.toAbsolutePath()); // 新增日志

        try (BufferedWriter writer = Files.newBufferedWriter(exportFile);
             CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT)) {

            // 添加表头
            if (!items.isEmpty()) {
                csvPrinter.printRecord(items.get(0).keySet());
            }

            // 添加数据行
            for (Map<String, Object> item : items) {
                csvPrinter.printRecord(item.values());
            }
        }
        return exportFile;
    }

    public Path exportToExcel(String taskId, List<Map<String, Object>> items) throws IOException {
        Path exportFile = Paths.get(tempDir, "enrichment", taskId, "export_result.xlsx");

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Enrichment Results");

            // 创建表头
            Row headerRow = sheet.createRow(0);
            if (!items.isEmpty()) {
                int colNum = 0;
                for (String key : items.get(0).keySet()) {
                    Cell cell = headerRow.createCell(colNum++);
                    cell.setCellValue(key);
                }
            }

            // 填充数据
            int rowNum = 1;
            for (Map<String, Object> item : items) {
                Row row = sheet.createRow(rowNum++);
                int colNum = 0;
                for (Object value : item.values()) {
                    Cell cell = row.createCell(colNum++);
                    if (value instanceof Number) {
                        cell.setCellValue(((Number) value).doubleValue());
                    } else {
                        cell.setCellValue(value.toString());
                    }
                }
            }

            // 自动调整列宽
            for (int i = 0; i < items.get(0).size(); i++) {
                sheet.autoSizeColumn(i);
            }

            // 写入文件
            try (FileOutputStream outputStream = new FileOutputStream(exportFile.toFile())) {
                workbook.write(outputStream);
            }
        }
        log.info("CSV文件生成成功，大小: {} KB", Files.size(exportFile) / 1024);
        return exportFile;
    }

    // 添加清理方法
    @Scheduled(fixedDelay = 3600000) // 每小时清理一次
    public void cleanupTempFiles() {
        Path tempBaseDir = Paths.get(this.tempDir, "enrichment");
        if (!Files.exists(tempBaseDir)) return;

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(tempBaseDir)) {
            for (Path taskDir : stream) {
                if (Files.isDirectory(taskDir)) {
                    // 检查目录创建时间（超过24小时）
                    long lastModified = Files.getLastModifiedTime(taskDir).toMillis();
                    if (System.currentTimeMillis() - lastModified > 86400000) {
                        // 递归删除目录
                        Files.walk(taskDir)
                                .sorted(Comparator.reverseOrder())
                                .forEach(path -> {
                                    try {
                                        Files.deleteIfExists(path);
                                    } catch (IOException e) {
                                        log.warn("Failed to delete temp file: {}", path, e);
                                    }
                                });
                        log.info("Deleted old temp directory: {}", taskDir);
                    }
                }
            }
        } catch (IOException e) {
            log.error("Error cleaning temp files", e);
        }
    }

    public Optional<Map<String, Object>> getResults(String taskId) {
        return Optional.ofNullable(taskResults.get(taskId));
    }
}