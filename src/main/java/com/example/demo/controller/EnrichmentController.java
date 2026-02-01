package com.example.demo.controller;

import com.example.demo.dto.EnrichmentRequestDTO;
import com.example.demo.service.EnrichmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/enrichment")
@RequiredArgsConstructor
@Slf4j
public class EnrichmentController {
    @Value("${r.temp.dir}")
    private String tempDir;

    private final EnrichmentService enrichmentService;
    // 添加线程池配置
    private final ExecutorService executorService = Executors.newFixedThreadPool(5);

    @PostMapping("/analyze")
    public ResponseEntity<?> analyzeGenes(@RequestBody EnrichmentRequestDTO request) {
        log.info("Received enrichment analysis request: {}", request);
        try {
            String taskId = UUID.randomUUID().toString();

            // 使用带超时的异步执行
            CompletableFuture.runAsync(() ->
                    enrichmentService.runAnalysis(taskId, request),
                    executorService
            ).orTimeout(10, TimeUnit.MINUTES); // 10分钟超时

            // 确保返回格式包含task_id
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200); // ✅ 新增：让前端拦截器识别为成功响应
            response.put("task_id", taskId);
            response.put("status", "processing");
            response.put("message", "分析任务已提交");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            // 异常响应也添加 code（保持格式统一）
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("code", 500); // ✅ 异常时返回 code: 500
            errorResponse.put("error", "Analysis failed to start");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("timestamp", Instant.now().toString());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse);
        }
    }

    @GetMapping("/results")
    public ResponseEntity<?> getResults(@RequestParam String taskId) {
        log.info("Getting results for task: {}", taskId);
        try {
            return enrichmentService.getResults(taskId)
                    .map(results -> {
                        // 核心修改：添加 code 字段
                        Map<String, Object> response = new HashMap<>();
                        if ("error".equals(results.get("status"))) {
                            response.put("code", 500); // 异常时 code: 500
                            response.putAll(results);
                            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                    .body(response);
                        }
                        response.put("code", 200); // 成功时 code: 200
                        response.putAll(results);
                        return ResponseEntity.ok().body(response);
                    })
                    .orElseGet(() -> {
                        // 分析中也添加 code: 200
                        Map<String, Object> processingResponse = new HashMap<>();
                        processingResponse.put("code", 200);
                        processingResponse.put("status", "processing");
                        processingResponse.put("message", "Analysis is still in progress");
                        return ResponseEntity.status(HttpStatus.ACCEPTED).body(processingResponse);
                    });
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("code", 500);
            errorResponse.put("error", "Error retrieving results: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @GetMapping("/export")
    public ResponseEntity<Resource> exportResults(
            @RequestParam String taskId,
            @RequestParam String format) {

        try {
            Map<String, Object> results = enrichmentService.getResults(taskId)
                    .orElseThrow(() -> new RuntimeException("Task not found"));

            if (!"completed".equals(results.get("status"))) {
                throw new RuntimeException("Analysis not completed");
            }

            // 获取结果数据
            Map<String, Object> resultData = (Map<String, Object>) results.get("results");
            List<Map<String, Object>> items = (List<Map<String, Object>>) resultData.get("results");
            if (items == null || items.isEmpty()) {
                throw new RuntimeException("无分析结果可导出（可能无显著富集条目）");
            }

            // 根据格式生成文件
            Path exportFile;
            if ("csv".equalsIgnoreCase(format)) {
                exportFile = enrichmentService.exportToCsv(taskId, items);
            } else if ("excel".equalsIgnoreCase(format)) {
                exportFile = enrichmentService.exportToExcel(taskId, items);
            } else {
                throw new IllegalArgumentException("Unsupported export format: " + format);
            }

            // 准备资源返回
            Resource resource = new UrlResource(exportFile.toUri());
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);

        } catch (Exception e) {
            log.error("Export failed for task: {}", taskId, e);
            return ResponseEntity.internalServerError()
                    .body(null);
        }
    }

    @GetMapping("/images/{taskId}")
    public ResponseEntity<Resource> getChart(@PathVariable String taskId)
        throws MalformedURLException {
        Path imagePath = Paths.get(tempDir, "enrichment", taskId, "chart.png");
        if (!Files.exists(imagePath)) {
            return ResponseEntity.notFound().build();
        }
        Resource resource = new UrlResource(imagePath.toUri());
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(resource);
    }
}