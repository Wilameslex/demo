package com.example.demo.service;

import com.example.demo.dto.ExpressionQueryDTO;
import com.example.demo.entity.ExpressionData;
import java.util.List;
import java.util.Map;

public interface ExpressionSearchService {
    Map<String, Object> searchExpression(ExpressionQueryDTO query);
    List<String> getSampleColumns(String pipeline, String transcriptome, String searchType);
}
