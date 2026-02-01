package com.example.demo.mapper.provider;

import com.example.demo.dto.GeneQueryDTO;
import org.apache.ibatis.jdbc.SQL;

import java.util.Map;
import java.util.stream.Collectors;

public class GeneSqlProvider {
    public String searchAllGenes(Map<String, Object> params) {
        GeneQueryDTO query = (GeneQueryDTO) params.get("query");
        return new SQL() {{
            SELECT("*");
            FROM("gene_info");

            // 动态构建WHERE条件
            // 核心修改：同时处理 Gene 和 Name 字段
            if ((query.getGenes() != null && !query.getGenes().isEmpty())
                    || (query.getNames() != null && !query.getNames().isEmpty())) {
                StringBuilder geneNameCondition = new StringBuilder("(");

                // 添加 Gene 字段条件
                if (query.getGenes() != null && !query.getGenes().isEmpty()) {
                    String geneCond = query.getGenes().stream()
                            .map(gene -> "Gene LIKE CONCAT('%', #{query.genes[" + query.getGenes().indexOf(gene) + "], '%')")
                            .collect(Collectors.joining(" OR "));
                    geneNameCondition.append(geneCond);
                }

                // 添加 Name 字段条件（与 Gene 用 OR 连接）
                if (query.getNames() != null && !query.getNames().isEmpty()) {
                    if (query.getGenes() != null && !query.getGenes().isEmpty()) {
                        geneNameCondition.append(" OR ");
                    }
                    String nameCond = query.getNames().stream()
                            .map(name -> "Name LIKE CONCAT('%', #{query.names[" + query.getNames().indexOf(name) + "], '%')")
                            .collect(Collectors.joining(" OR "));
                    geneNameCondition.append(nameCond);
                }

                geneNameCondition.append(")");
                WHERE(geneNameCondition.toString());
            }


            if (query.getProteins() != null && !query.getProteins().isEmpty()) {
                String proteinCondition = query.getProteins().stream()
                        .map(protein -> "Protein LIKE CONCAT('%', #{query.proteins} , '%')")
                        .collect(Collectors.joining(" OR "));
                WHERE("(" + proteinCondition + ")");
            }

            if (query.getProducts() != null && !query.getProducts().isEmpty()) {
                String productCondition = query.getProducts().stream()
                        .map(product -> "Product LIKE CONCAT('%', #{query.products} , '%')")
                        .collect(Collectors.joining(" OR "));
                WHERE("(" + productCondition + ")");
            }
        }}.toString();
    }
}