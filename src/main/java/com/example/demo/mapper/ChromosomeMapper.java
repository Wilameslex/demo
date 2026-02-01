package com.example.demo.mapper;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * 染色体格式转换Mapper（chr ↔ NC）
 */
@Repository
public interface ChromosomeMapper {

    /**
     * 根据chr格式（如chr1）查询对应的NC格式（如NC_066509.1）
     * @param chrFormat chr格式染色体（如chr1）
     * @return NC格式染色体（如NC_066509.1；无匹配返回null）
     */
    String getNcByChr(@Param("chrFormat") String chrFormat);

    // 新增方法：通过NC查char
    String getChrByNc(@Param("ncFormat") String ncFormat);
}