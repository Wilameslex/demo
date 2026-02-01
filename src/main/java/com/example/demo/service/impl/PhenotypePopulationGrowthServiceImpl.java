package com.example.demo.service.impl;

import com.example.demo.entity.PhenotypePopulationGrowth;
import com.example.demo.mapper.PhenotypePopulationGrowthMapper;
import com.example.demo.service.PhenotypePopulationGrowthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PhenotypePopulationGrowthServiceImpl implements PhenotypePopulationGrowthService {

    private final PhenotypePopulationGrowthMapper growthMapper;

    @Override
    public List<PhenotypePopulationGrowth> getAllGrowthData() {
        return growthMapper.selectAll();
    }
}