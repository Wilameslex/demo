package com.example.demo.dto;

import lombok.Data;

import java.util.List;

@Data
public class GeneBatchQueryDTO {
    private List<String> genes;
    private List<String> proteins;
    private List<String> products;
}