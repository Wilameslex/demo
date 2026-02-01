package com.example.demo.service.impl;

import com.example.demo.dto.ExpressionQueryDTO;
import com.example.demo.mapper.ExpressionSearchMapper;
import com.example.demo.service.ExpressionSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExpressionSearchServiceImpl implements ExpressionSearchService {
    private final ExpressionSearchMapper expressionSearchMapper; // 正确注入的Mapper

    @Override
    public Map<String, Object> searchExpression(ExpressionQueryDTO query) {
        // 1. 根据参数构建表名
        String tableName = buildTableName(
                query.getTranscriptome(),
                query.getSearchType(),
                query.getPipeline());

        // 2. 获取该表的所有样本列（排除gene_id）
        List<String> allSamples = expressionSearchMapper.getSampleColumns(tableName); // 使用正确变量名
        if (allSamples == null || allSamples.isEmpty()) {
            throw new RuntimeException("未找到表或表没有样本列: " + tableName);
        }



        // 3. 处理用户选择的样本
        List<String> selectedSamples = query.getSelectedSamples();
        if (selectedSamples == null || selectedSamples.isEmpty()) {
            selectedSamples = allSamples;
        } else {
            selectedSamples = selectedSamples.stream()
                    .filter(allSamples::contains)
                    .collect(Collectors.toList());
            if (selectedSamples.isEmpty()) {
                selectedSamples = allSamples;
            }
        }

        List<String> safeColumns = selectedSamples.stream()
                .map(column -> "`" + column.replace("`", "``") + "`")
                .collect(Collectors.toList());
        if (safeColumns.isEmpty()) {
            throw new RuntimeException("无有效样本列，无法查询");
        }
        String columns = String.join(",", safeColumns);

        // 4. 查询数据
        List<Map<String, Object>> resultList;
        if (query.getTargetIds() == null || query.getTargetIds().isEmpty()) {
            // 空targetIds：查询所有基因/转录本（无WHERE条件）
            resultList = expressionSearchMapper.searchAllExpression(tableName, columns);
        } else {
            // 非空targetIds：按原有逻辑查询
            resultList = expressionSearchMapper.searchExpression(tableName, columns, query.getTargetIds());
        }

        // 5. 转换数据格式：将BigDecimal值转换为Double
        List<Map<String, Object>> expressionData = resultList.stream()
                .map(row -> {
                    Map<String, Object> convertedRow = new HashMap<>();
                    row.forEach((key, value) -> {
                        if (value instanceof BigDecimal) {
                            convertedRow.put(key, ((BigDecimal) value).doubleValue());
                        } else {
                            convertedRow.put(key, value);
                        }
                    });
                    return convertedRow;
                })
                .collect(Collectors.toList());

        // 6. 构建返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("expressionData", expressionData);
        result.put("samples", allSamples); // 返回所有样本供选择
        result.put("targetIds", query.getTargetIds());

        return result;
    }

    @Override
    public List<String> getSampleColumns(String pipeline, String transcriptome, String searchType) {
        String tableName = buildTableName(transcriptome, searchType, pipeline);
        return expressionSearchMapper.getSampleColumns(tableName); // 使用正确变量名
    }

    /**
     * 构建表名
     * @param transcriptome 转录组类型（maturity/metamorphosis）
     * @param searchType 搜索类型（gene/transcript）
     * @param pipeline 分析方法（stringtie/rsem）
     * @return 表名
     */
    private String buildTableName(String transcriptome, String searchType, String pipeline) {
        // 将搜索类型转换为表后缀：gene -> gene, transcript -> rna
        String typeSuffix = "gene".equals(searchType) ? "gene" : "rna";
        return String.format("%s_%s_%s", transcriptome, typeSuffix, pipeline);
    }
}