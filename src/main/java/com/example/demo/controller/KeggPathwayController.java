package com.example.demo.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.demo.common.Result;
import com.example.demo.dto.PathwayQueryDTO;
import com.example.demo.entity.KeggPathway;
import com.example.demo.service.KeggPathwayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.core.io.InputStreamResource;

import javax.validation.Valid;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/pathway")
@RequiredArgsConstructor
@Slf4j
public class KeggPathwayController {
    private final KeggPathwayService keggPathwayService;

    @PostMapping("/search")
    public ResponseEntity<Result<IPage<KeggPathway>>> searchPathway(
            @Valid @RequestBody PathwayQueryDTO query) {

        log.info("KEGG通路搜索请求 - 基因: {}, KO: {}, 通路: {}",
                query.getGenes(), query.getKos(), query.getPathways());

        try {
            IPage<KeggPathway> result = keggPathwayService.searchPathway(query);
            return ResponseEntity.ok(Result.success(result));
        } catch (Exception e) {
            log.error("KEGG通路搜索异常", e);
            return ResponseEntity.internalServerError()
                    .body(Result.error(e.getMessage()));
        }
    }

    @PostMapping("/export")
    public ResponseEntity<InputStreamResource> exportPathway(
            @Valid @RequestBody PathwayQueryDTO query) {

        log.info("KEGG通路导出请求 - 基因: {}, KO: {}, 通路: {}",
                query.getGenes(), query.getKos(), query.getPathways());

        try {
            List<KeggPathway> data = keggPathwayService.searchAll(query);
            String csvContent = buildCsvContent(data);

            ByteArrayInputStream inputStream = new ByteArrayInputStream(
                    csvContent.getBytes(StandardCharsets.UTF_8));

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=kegg_pathway_export.csv")
                    .contentType(MediaType.parseMediaType("text/csv"))
                    .body(new InputStreamResource(inputStream));
        } catch (Exception e) {
            log.error("导出失败: ", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    /**
     * 构建CSV内容
     * @param data KEGG通路数据列表
     * @return CSV格式的字符串
     */
    private String buildCsvContent(List<KeggPathway> data) {
        StringBuilder csv = new StringBuilder();
        // CSV头
        csv.append("\"Gene\",\"Description\",\"Name\",\"EC\",\"KO\",\"KEGG_gene\",\"Score\"\n");

        // 数据行
        for (KeggPathway item : data) {
            csv.append(String.format("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",%d\n",
                    escapeCsv(item.getGene()),
                    escapeCsv(item.getDescription()),
                    escapeCsv(item.getName()),
                    escapeCsv(item.getEc()),
                    escapeCsv(item.getKo()),
                    escapeCsv(item.getKeggGene()),
                    item.getScore()));
        }

        return csv.toString();
    }

    /**
     * 转义CSV中的特殊字符
     * @param value 原始字符串
     * @return 转义后的字符串
     */
    private String escapeCsv(String value) {
        if (value == null) return "";
        return value.replace("\"", "\"\"");
    }

}