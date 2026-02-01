package com.example.demo.entity;

import lombok.Data;

@Data
public class GeneCombinedInfo {
    private String geneId;        // 基因ID（与target_gene_id/related_gene_id关联）
    private String geneAbbrev;      // 基因名称
    private String description;   // 基因功能注释
    private String chr;    // 染色体位置
    private Long start;
    private Long end;
    private String strand;        // 链方向（+/-）
    // 可根据gene_combined视图的实际字段扩展
}
