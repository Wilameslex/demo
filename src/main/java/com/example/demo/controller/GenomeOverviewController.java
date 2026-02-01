package com.example.demo.controller;

import com.example.demo.common.Result;
import com.example.demo.entity.GenomeAssembly;
import com.example.demo.entity.GenomeChromosome;
import com.example.demo.service.GenomeAssemblyService;
import com.example.demo.service.GenomeChromosomeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

/**
 * Genome Overview模块接口（仅展示中华绒螯蟹参考基因组）
 */
@RestController
@RequestMapping("/genome/overview")
@RequiredArgsConstructor
public class GenomeOverviewController {

    private final GenomeAssemblyService assemblyService;
    private final GenomeChromosomeService chromosomeService;

    /**
     * 接口1：获取中华绒螯蟹参考基因组详情（固定ASM2467909v1）
     */
    @GetMapping("/assembly-detail")
    public ResponseEntity<Result<GenomeAssembly>> getAssemblyDetail() {
        GenomeAssembly assembly = assemblyService.getEriocheirSinensisAssembly();
        return ResponseEntity.ok(Result.success(assembly));
    }

    /**
     * 接口2：获取中华绒螯蟹所有染色体信息
     */
    @GetMapping("/chromosomes")
    public ResponseEntity<Result<List<GenomeChromosome>>> getChromosomes() {
        List<GenomeChromosome> chromosomes = chromosomeService.getAllChromosomes();
        return ResponseEntity.ok(Result.success(chromosomes));
    }
}