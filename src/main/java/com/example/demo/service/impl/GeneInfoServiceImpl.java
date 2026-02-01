package com.example.demo.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.dto.GeneQueryDTO;
import com.example.demo.entity.GeneInfo;
import com.example.demo.mapper.GeneInfoMapper;
import com.example.demo.service.GeneInfoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeneInfoServiceImpl implements GeneInfoService {
    private final GeneInfoMapper geneInfoMapper;

    @Override
    public IPage<GeneInfo> searchGenes(GeneQueryDTO query) {
        if (query.getPage() == null) query.setPage(1);
        if (query.getSize() == null) query.setSize(10);
        Page<GeneInfo> page = new Page<>(query.getPage(), query.getSize());
        return geneInfoMapper.searchGenes(page, query);
    }

    @Override
    public List<GeneInfo> batchSearchGenes(List<String> genes) {
        return Collections.emptyList();
    }

    @Override
    public List<GeneInfo> exportGenes(GeneQueryDTO query) {
        // 调用Mapper获取全部数据（不分页）
        return geneInfoMapper.searchAllGenes(query);
    }
}