package com.example.demo.service;

import com.example.demo.entity.PhenotypePopulationGrowth;
import java.util.List;

public interface PhenotypePopulationGrowthService {
    /**
     * 获取所有群体生长数据
     */
    List<PhenotypePopulationGrowth> getAllGrowthData();
}