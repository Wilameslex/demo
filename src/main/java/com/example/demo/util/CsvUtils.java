package com.example.demo.util; // 包路径需与Controller导入路径一致

import java.util.List;
import java.util.Map;
import java.util.Set;

public class CsvUtils {
    /**
     * 将List<Map>转为CSV字符串（适配下载功能）
     */
    public static String convertToCsv(List<Map<String, Object>> data) {
        if (data == null || data.isEmpty()) {
            return "";
        }
        // 1. 提取CSV表头（取第一条数据的所有key）
        Set<String> headers = data.get(0).keySet();
        StringBuilder csvBuilder = new StringBuilder();
        // 2. 写入表头
        csvBuilder.append(String.join(",", headers)).append("\n");
        // 3. 写入数据行（处理空值，避免CSV格式错乱）
        for (Map<String, Object> row : data) {
            List<String> values = headers.stream()
                    .map(header -> {
                        Object value = row.get(header);
                        // 空值处理为空白字符串，避免CSV出现"null"
                        return value != null ? value.toString().replace(",", "，") : "";
                    })
                    .toList();
            csvBuilder.append(String.join(",", values)).append("\n");
        }
        return csvBuilder.toString();
    }
}