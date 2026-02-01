package com.example.demo.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.util.Map;

@Data
public class GeneExpression {
    private String geneId;                // 基因ID
    private Map<String, BigDecimal> exprData; // 样本表达量：key=样本名，value=表达量值
    // 若需固定样本列，可替换为具体字段（如sample1、sample2...）
}