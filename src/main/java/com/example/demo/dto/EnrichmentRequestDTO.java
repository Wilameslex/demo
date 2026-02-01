package com.example.demo.dto;

import lombok.Data;

import java.util.List;

@Data
public class EnrichmentRequestDTO {
    private List<String> genes;
    private String analysisType; // "GO" or "KEGG"
}