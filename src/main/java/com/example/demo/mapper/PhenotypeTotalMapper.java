package com.example.demo.mapper;

import com.example.demo.entity.PhenotypeTotal;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface PhenotypeTotalMapper {
    /**
     * 获取所有总表型数据（用于下载）
     */
    List<PhenotypeTotal> selectAll();
    Long selectTotalCount();
}