package com.example.demo.service.impl;

import com.example.demo.entity.PhenotypePopulationAppearance;
import com.example.demo.mapper.PhenotypePopulationAppearanceMapper;
import com.example.demo.service.PhenotypePopulationAppearanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PhenotypePopulationAppearanceServiceImpl implements PhenotypePopulationAppearanceService {

    private final PhenotypePopulationAppearanceMapper appearanceMapper;

    @Override
    public List<PhenotypePopulationAppearance> getAllAppearanceData() {
        return appearanceMapper.selectAll();
    }


}