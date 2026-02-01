package com.example.demo.mapper;

import com.example.demo.entity.GeneExpression;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

@Mapper
public interface GeneExpressionMapper {
    // 批量查询基因表达量（动态表名）
    List<Map<String, Object>> queryExprBatch(
            @Param("tableName") String tableName,
            @Param("geneIds") List<String> geneIds
    );
}