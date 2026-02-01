package com.example.demo.service;

import com.example.demo.entity.PhenotypePopulationAppearance;
import java.util.List;

public interface PhenotypePopulationAppearanceService {
    /**
     * 获取所有群体外观数据
     */
    List<PhenotypePopulationAppearance> getAllAppearanceData();
}