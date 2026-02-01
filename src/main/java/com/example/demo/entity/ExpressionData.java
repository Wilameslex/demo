package com.example.demo.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

@Data
public class ExpressionData {
    private String geneId;
    private Map<String, BigDecimal> samples = new LinkedHashMap<>();

    // 动态添加样本数据
    public void addSampleValue(String sampleName, BigDecimal value) {
        samples.put(sampleName, value);
    }
}
