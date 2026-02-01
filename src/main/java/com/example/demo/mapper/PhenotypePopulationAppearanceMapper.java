package com.example.demo.mapper;

import com.example.demo.entity.PhenotypePopulationAppearance;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface PhenotypePopulationAppearanceMapper {
    /**
     * 获取所有群体外观数据（按群体名称排序）
     */
    List<PhenotypePopulationAppearance> selectAll();
}