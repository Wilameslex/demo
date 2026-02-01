package com.example.demo.controller;

import com.example.demo.common.Result;
import com.example.demo.entity.PhenotypePopulationAppearance;
import com.example.demo.entity.PhenotypePopulationGrowth;
import com.example.demo.service.PhenotypePopulationAppearanceService;
import com.example.demo.service.PhenotypePopulationGrowthService;
import com.example.demo.service.PhenotypeTotalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

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
        // 手动捕获异常，避免被GlobalExceptionHandler拦截返回JSON
        try {
            totalService.exportTotalDataToExcel(response);
        } catch (Exception e) {
            // 手动设置响应为流格式，返回错误信息（前端可捕获）
            response.setContentType("application/json;charset=utf-8");
            response.setCharacterEncoding("UTF-8");
            try (PrintWriter writer = response.getWriter()) {
                // 返回JSON格式错误，前端可解析
                writer.write("{\"code\":500,\"message\":\"Excel生成失败：" + e.getMessage() + "\"}");
                writer.flush();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            // 打印详细异常，便于后端排查
            System.err.println("下载接口异常：");
            e.printStackTrace();
        }
    }
}