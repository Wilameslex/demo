package com.example.demo.entity;

import lombok.Data;

@Data
public class GenomeChromosome {
    private Long id;                // 自增主键
    private Integer chromosomes;    // 染色体编号（原chromosomeNum → 改为chromosomes）
    private String refseqAccession; // RefSeq登录号（如NC_066509.1）
    private Long gcCount;           // GC碱基总数
    private Double gcPercent;       // GC百分比
    private Long length;            // 染色体长度（bp）
}