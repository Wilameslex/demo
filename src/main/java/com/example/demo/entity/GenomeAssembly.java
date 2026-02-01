package com.example.demo.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class GenomeAssembly {
    private Long id;                        // 自增主键
    private String assemblyName;            // 组装名称（ASM2467909v1）
    private String species;                 // 物种名称
    private Integer taxonomicId;            // 分类ID
    private String infraspecificBreed;      // 亚种品系
    private String annotationName;          // 注释名称
    private Long totalSequenceLength;       // 总序列长度（bp）
    private String assemblyLevel;           // 组装级别
    private Integer chromosomes;            // 染色体数量
    private Integer contigN50;             // Contig N50
    private Integer scaffoldN50;           // Scaffold N50
    private Integer scaffolds;              // Scaffold数量
    private Double gcPercent;              // GC百分比
    private String busco;                  // BUSCO评估结果
    private String sequencingTech;          // 测序技术
    private String bioprojectAccession;     // BioProject登录号
    private Integer genes;                 // 总基因数
    private Integer proteinCodingGenes;     // 蛋白编码基因数
    private LocalDateTime createTime;       // 创建时间
    private LocalDateTime updateTime;       // 更新时间
}