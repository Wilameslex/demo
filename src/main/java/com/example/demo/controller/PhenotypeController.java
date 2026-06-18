package com.example.demo.controller;

import com.example.demo.common.Result;
import com.example.demo.entity.PhenotypePopulationAppearance;
import com.example.demo.entity.PhenotypePopulationGrowth;
import com.example.demo.service.CrawfishPhenotypeService;
import com.example.demo.service.PhenotypePopulationAppearanceService;
import com.example.demo.service.PhenotypePopulationGrowthService;
import com.example.demo.service.PhenotypeTotalService;
import com.example.demo.service.SnailPhenotypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

/**
 * Phenotype Data模块接口（河蟹表型数据）
 */
@RestController
@RequestMapping("/phenotype")
@RequiredArgsConstructor
public class PhenotypeController {

    private final PhenotypePopulationAppearanceService appearanceService;
    private final PhenotypePopulationGrowthService growthService;
    private final PhenotypeTotalService totalService;
    private final CrawfishPhenotypeService crawfishPhenotypeService;
    private final SnailPhenotypeService snailPhenotypeService;

    /**
     * 接口1：获取所有群体外观数据
     */
    @GetMapping("/population/appearance")
    public ResponseEntity<Result<List<PhenotypePopulationAppearance>>> getPopulationAppearance() {
        List<PhenotypePopulationAppearance> appearanceData = appearanceService.getAllAppearanceData();
        return ResponseEntity.ok(Result.success(appearanceData));
    }

    /**
     * 接口2：获取所有群体生长数据
     */
    @GetMapping("/population/growth")
    public ResponseEntity<Result<List<PhenotypePopulationGrowth>>> getPopulationGrowth() {
        List<PhenotypePopulationGrowth> growthData = growthService.getAllGrowthData();
        return ResponseEntity.ok(Result.success(growthData));
    }

    @GetMapping("/total/count")
    public Long getTotalDataCount() {
        return totalService.getTotalDataCount();
    }

    /**
     * 接口3：下载总表型数据（Excel）
     */
    @GetMapping("/total/download")
    public void downloadTotalData(HttpServletResponse response) {
        try {
            totalService.exportTotalDataToExcel(response);
        } catch (Exception e) {
            writeDownloadError(response, e);
        }
    }

    @GetMapping("/crawfish/preview")
    public ResponseEntity<Result<List<Map<String, Object>>>> getCrawfishPreview(
            @RequestParam(defaultValue = "20") Integer limit
    ) {
        return ResponseEntity.ok(Result.success(crawfishPhenotypeService.getPreviewData(limit)));
    }

    @GetMapping("/crawfish/download")
    public void downloadCrawfishData(HttpServletResponse response) {
        try {
            crawfishPhenotypeService.exportAllDataToExcel(response);
        } catch (Exception e) {
            writeDownloadError(response, e);
        }
    }

    @GetMapping("/snail/preview")
    public ResponseEntity<Result<List<Map<String, Object>>>> getSnailPreview(
            @RequestParam(defaultValue = "20") Integer limit
    ) {
        return ResponseEntity.ok(Result.success(snailPhenotypeService.getPreviewData(limit)));
    }

    @GetMapping("/snail/download")
    public void downloadSnailData(HttpServletResponse response) {
        try {
            snailPhenotypeService.exportAllDataToExcel(response);
        } catch (Exception e) {
            writeDownloadError(response, e);
        }
    }

    private void writeDownloadError(HttpServletResponse response, Exception e) {
        response.setContentType("application/json;charset=utf-8");
        response.setCharacterEncoding("UTF-8");
        try (PrintWriter writer = response.getWriter()) {
            writer.write("{\"code\":500,\"message\":\"Excel生成失败：" + e.getMessage() + "\"}");
            writer.flush();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        System.err.println("下载接口异常：");
        e.printStackTrace();
    }
}
