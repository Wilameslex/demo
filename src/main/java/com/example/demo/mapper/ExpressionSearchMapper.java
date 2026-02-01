package com.example.demo.mapper;

import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface ExpressionSearchMapper {

    // 获取表的所有样本列名（排除gene_id）
    @Select("SELECT column_name FROM information_schema.columns " +
            "WHERE table_name = #{tableName} AND column_name != 'gene_id'")
    List<String> getSampleColumns(@Param("tableName") String tableName);

    // 修改参数类型：将columns改为String类型
    @Select({
            "<script>",
            "SELECT gene_id, ${columns} FROM ${tableName}",
            "WHERE gene_id IN ",
            "<foreach item='id' collection='targetIds' open='(' separator=',' close=')'>",
            "#{id}",
            "</foreach>",
            "</script>"
    })
    List<Map<String, Object>> searchExpression(
            @Param("tableName") String tableName,
            @Param("columns") String columns, // 改为String类型
            @Param("targetIds") List<String> targetIds
    );
    List<Map<String, Object>> searchAllExpression(
            @Param("tableName") String tableName,
            @Param("selectedSamples") String selectedSamples
    );
}
