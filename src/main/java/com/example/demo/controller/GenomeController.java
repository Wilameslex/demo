package com.example.demo.controller;

import com.example.demo.common.Result;
import com.example.demo.entity.MitochondrionGenome;
import com.example.demo.service.MitochondrionGenomeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.Map;

/**
 * 基因组信息接口（线粒体基因组模块）
 */
@RestController
@RequestMapping("/genome/mitochondrion")  // 接口路径：与CorsConfig放行路径匹配
@RequiredArgsConstructor
public class GenomeController {

    private final MitochondrionGenomeService mitochondrionGenomeService;

    /**
     * 接口1：获取支持的物种列表（供前端下拉框）
     */
    @GetMapping("/species-list")
    public ResponseEntity<Result<List<Map<String, String>>>> getSpeciesList() {
        List<Map<String, String>> speciesList = mitochondrionGenomeService.getSpeciesList();
        return ResponseEntity.ok(Result.success(speciesList));
    }

    /**
     * 接口2：根据物种标识，获取线粒体注释信息
     * @param species 物种标识（sinensis/japonica/hepuensis）
     */
    @GetMapping("/info")
    public ResponseEntity<Result<List<MitochondrionGenome>>> getMitoInfo(
            @RequestParam String species) {
        List<MitochondrionGenome> mitoInfo = mitochondrionGenomeService.getMitoInfoBySpecies(species);
        return ResponseEntity.ok(Result.success(mitoInfo));
    }
}