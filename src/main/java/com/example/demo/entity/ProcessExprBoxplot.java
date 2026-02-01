package com.example.demo.entity;

import lombok.Data;

@Data
public class ProcessExprBoxplot {
    private String process;
    private String pipeline;
    private String sampleClass;
    private Double exprMin;
    private Double exprQ1;
    private Double exprMedian;
    private Double exprQ3;
    private Double exprMax;
}
