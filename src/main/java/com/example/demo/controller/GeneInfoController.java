package com.example.demo.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.demo.common.Result;
import com.example.demo.dto.GeneQueryDTO;
import com.example.demo.entity.GeneInfo;
import com.example.demo.service.GeneInfoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.apache.commons.io.FileUtils;
import org.springframework.http.MediaType;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import javax.validation.Valid;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/genes")
@CrossOrigin(origins = "*") // 生产环境应指定具体域名
@RequiredArgsConstructor
@Slf4j // 使用Lombok的日志注解
public class GeneInfoController {

    private final GeneInfoService geneInfoService;

    /**
     * 统一基因搜索接口
     * @param query 查询参数DTO
     * @return 分页结果
     */
    @PostMapping("/search")
    public ResponseEntity<Result<IPage<GeneInfo>>> searchGenes(
            @Valid @RequestBody GeneQueryDTO query) {

        log.info("基因搜索请求: {}", query);

        // 参数验证
        if (isEmptyQuery(query)) {
            return ResponseEntity.badRequest()
                    .body(Result.error("至少需要提供一个搜索条件"));
        }

        try {
            IPage<GeneInfo> result = geneInfoService.searchGenes(query);
            return ResponseEntity.ok(Result.success(result));
        } catch (Exception e) {
            log.error("基因搜索异常: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(Result.error("搜索服务暂时不可用"));
        }
    }
    @PostMapping("/export")
    public ResponseEntity<Resource> exportGenes(@Valid @RequestBody GeneQueryDTO query) throws IOException {
        log.info("基因导出请求: {}", query);

        // 1. 获取数据
        List<GeneInfo> genes = geneInfoService.exportGenes(query);

        // 2. 生成CSV内容
        String csvContent = generateCsvContent(genes);

        // 3. 创建临时文件
        String filename = "genes_export_" + System.currentTimeMillis() + ".csv";
        File file = new File(filename);
        FileUtils.writeStringToFile(file, csvContent, StandardCharsets.UTF_8);

        // 4. 创建资源对象
        InputStreamResource resource;
        try {
            resource = new InputStreamResource(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            log.error("文件未找到", e);
            return ResponseEntity.internalServerError().build();
        }

        // 在返回前添加清理逻辑
        file.deleteOnExit();

        // 5. 设置响应头
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("text/csv"))
                .contentLength(file.length())
                .body(resource);
    }

    private String generateCsvContent(List<GeneInfo> genes) {
        StringBuilder csv = new StringBuilder();
        csv.append("\"Gene\",\"Name\",\"Chromosome\",\"Start\",\"End\",\"Protein\",\"Product\",\"Description\"\n");

        for (GeneInfo gene : genes) {
            csv.append(String.format("\"%s\",\"%s\",\"%s\",%d,%d,\"%s\",\"%s\",\"%s\"\n",
                    escapeCsv(gene.getGene()),
                    escapeCsv(gene.getName()),
                    escapeCsv(gene.getChromosome()),
                    gene.getStart(),
                    gene.getEnd(),
                    escapeCsv(gene.getProtein()),
                    escapeCsv(gene.getProduct()),
                    escapeCsv(gene.getDescription())
            ));
        }
        return csv.toString();
    }

    private String escapeCsv(String input) {
        if (input == null) return "";
        return input.replace("\"", "\"\"");
    }

    /**
     * 检查是否为空查询
     */
    private boolean isEmptyQuery(GeneQueryDTO query) {
        return (query.getGenes() == null || query.getGenes().isEmpty()) &&
                (query.getProteins() == null || query.getProteins().isEmpty()) &&
                (query.getProducts() == null || query.getProducts().isEmpty());
    }
}
