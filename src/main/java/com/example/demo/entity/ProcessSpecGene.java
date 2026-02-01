package com.example.demo.entity;

import lombok.Data;

@Data
public class ProcessSpecGene {
    private String process;
    private String pipeline;
    private String sampleClass;
    private String geneId;
    private Double intraMean;
    private Double otherMean;
}
