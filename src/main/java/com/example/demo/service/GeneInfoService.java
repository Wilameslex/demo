package com.example.demo.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.demo.dto.GeneQueryDTO;
import com.example.demo.entity.GeneInfo;

import java.util.List;

public interface GeneInfoService {
    IPage<GeneInfo> searchGenes(GeneQueryDTO query);
    List<GeneInfo> batchSearchGenes(List<String> genes);
    List<GeneInfo> exportGenes(GeneQueryDTO query);
}
