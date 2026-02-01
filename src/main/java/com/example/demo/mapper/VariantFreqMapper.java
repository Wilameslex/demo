package com.example.demo.mapper;

import com.example.demo.entity.GeneCoord;
import com.example.demo.entity.VariantFreq;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface VariantFreqMapper {

    // 1. 原有保留：查询基因坐标（按基因ID+上下游扩展）
    GeneCoord getGeneCoord(
            @Param("geneId") String geneId,
            @Param("upstream") Integer upstream,
            @Param("downstream") Integer downstream
    );
    /**
     * 通用查询：根据染色体区间+变异类型查询前N条数据
     * @param tableName 表名（如"variant_freq_snp"）
     * @param chr 染色体（chr01）
     * @param start 起始位置
     * @param end 终止位置
     * @param offset 分页偏移量
     * @param size 查询条数（前10行用size=10）
     */
    List<VariantFreq> getVariantsByType(
            @Param("tableName") String tableName,
            @Param("chr") String chr,
            @Param("start") Long start,
            @Param("end") Long end,
            @Param("offset") Integer offset,
            @Param("size") Integer size
    );

    /**
     * 通用统计：查询区间内该类型变异的总数（用于下载和分页）
     */
    int countVariantsByType(
            @Param("tableName") String tableName,
            @Param("chr") String chr,
            @Param("start") Long start,
            @Param("end") Long end
    );

    // 4. 新增：查询Fst数据（6组群体对）
    List<Map<String, Object>> getFstByChrPos(
            @Param("chr") String chr,
            @Param("start") Long start,
            @Param("end") Long end
    );

    // 5. 新增：查询Pi数据（7个群体）
   List<Map<String, Object>> getPiByChrPos(
           @Param("chr") String chr,
           @Param("start") Long start,
           @Param("end") Long end
   );
}
