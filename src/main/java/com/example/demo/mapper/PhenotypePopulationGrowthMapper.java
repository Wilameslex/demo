package com.example.demo.mapper;

import com.example.demo.entity.PhenotypePopulationGrowth;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface PhenotypePopulationGrowthMapper {
    /**
     * 获取所有群体生长数据（按群体名称排序）
     */
    List<PhenotypePopulationGrowth> selectAll();
}