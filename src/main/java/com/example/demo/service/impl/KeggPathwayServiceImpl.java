package com.example.demo.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.dto.PathwayQueryDTO;
import com.example.demo.entity.KeggPathway;
import com.example.demo.mapper.KeggPathwayMapper;
import com.example.demo.service.KeggPathwayService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class KeggPathwayServiceImpl implements KeggPathwayService {
    private final KeggPathwayMapper keggPathwayMapper;

    @Override
    public IPage<KeggPathway> searchPathway(PathwayQueryDTO query) {
        // 设置默认值
        if (query.getPage() == null) query.setPage(1);
        if (query.getSize() == null) query.setSize(10);

        return keggPathwayMapper.searchPathway(
                new Page<>(query.getPage(), query.getSize()),
                query
        );
    }

    @Override
    public List<KeggPathway> searchAll(PathwayQueryDTO query) {
        return keggPathwayMapper.searchAll(query);
    }
}