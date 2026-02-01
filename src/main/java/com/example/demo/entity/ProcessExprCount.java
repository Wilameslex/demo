package com.example.demo.entity;

import lombok.Data;

@Data
public class ProcessExprCount {
    private String process; // 生物学过程（如metamorphosis）
    private String pipeline; // 分析方法（stringtie/rsem）
    private String sampleClass; // 样本类（egg/zoea）
    private Integer expressedGeneCount; // 表达基因数
}
