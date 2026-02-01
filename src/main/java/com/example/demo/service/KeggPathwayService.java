package com.example.demo.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.demo.dto.PathwayQueryDTO;
import com.example.demo.entity.KeggPathway;

import java.util.List;

public interface KeggPathwayService {
    IPage<KeggPathway> searchPathway(PathwayQueryDTO query);
    // 新增方法
    List<KeggPathway> searchAll(PathwayQueryDTO query);
}
