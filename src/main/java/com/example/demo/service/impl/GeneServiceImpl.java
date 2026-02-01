package com.example.demo.service.impl;

import com.example.demo.entity.Gene;
import com.example.demo.entity.GeneNetworkData;
import com.example.demo.mapper.GeneMapper;
import com.example.demo.service.GeneService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 基因服务实现类
 */
@Service // 标记为业务逻辑组件
public class GeneServiceImpl implements GeneService {

    @Autowired // 注入 Mapper
    private GeneMapper geneMapper;

    @Override
    public Gene getGeneLocation(String geneId) {
        // 参数非空验证（空值直接返回null，由Controller处理错误）
        if (geneId == null || geneId.trim().isEmpty()) {
            return null;
        }
        // 调用 Mapper 查询
        return geneMapper.getGeneLocation(geneId.trim());
    }
}
