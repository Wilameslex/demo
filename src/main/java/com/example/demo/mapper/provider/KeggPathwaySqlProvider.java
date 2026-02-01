package com.example.demo.mapper.provider;

import com.example.demo.dto.PathwayQueryDTO;
import org.apache.ibatis.jdbc.SQL;
import java.util.Map;
import java.util.stream.Collectors;

public class KeggPathwaySqlProvider {
    public String searchPathway(Map<String, Object> params) {
        PathwayQueryDTO query = (PathwayQueryDTO) params.get("query");
        return new SQL() {{
            SELECT("*");
            FROM("gene_pathway");

            // 基因条件
            if ((query.getGenes() != null && !query.getGenes().isEmpty())
                    || (query.getNames() != null && !query.getNames().isEmpty())) {
                StringBuilder geneNameCondition = new StringBuilder("(");

                // 添加 Gene 字段条件（基因ID）
                if (query.getGenes() != null && !query.getGenes().isEmpty()) {
                    String geneCond = query.getGenes().stream()
                            .map(gene -> "Gene LIKE CONCAT('%', #{query.genes[" + query.getGenes().indexOf(gene) + "]}, '%')")
                            .collect(Collectors.joining(" OR "));
                    geneNameCondition.append(geneCond);
                }

                // 添加 Name 字段条件（基因名称）：与 Gene 用 OR 连接
                if (query.getNames() != null && !query.getNames().isEmpty()) {
                    if (query.getGenes() != null && !query.getGenes().isEmpty()) {
                        geneNameCondition.append(" OR ");
                    }
                    String nameCond = query.getNames().stream()
                            .map(name -> "Name LIKE CONCAT('%', #{query.names[" + query.getNames().indexOf(name) + "]}, '%')")
                            .collect(Collectors.joining(" OR "));
                    geneNameCondition.append(nameCond);
                }

                geneNameCondition.append(")");
                WHERE(geneNameCondition.toString());
            }

            // KO号条件
            if (query.getKos() != null && !query.getKos().isEmpty()) {
                WHERE("(" +
                        query.getKos().stream()
                                .map(ko -> "KO LIKE CONCAT('%', #{ko}, '%')")
                                .collect(Collectors.joining(" OR ")) +
                        ")");
            }

            // 通路名称条件
            if (query.getPathways() != null && !query.getPathways().isEmpty()) {
                WHERE("(" +
                        query.getPathways().stream()
                                .map(pathway -> "Name LIKE CONCAT('%', #{pathway}, '%')")
                                .collect(Collectors.joining(" OR ")) +
                        ")");
            }

            ORDER_BY("Score DESC");
        }}.toString();
    }
}
