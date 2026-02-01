package com.example.demo.service;

import com.example.demo.entity.Gene;
/**
 * 基因服务接口
 */
public interface GeneService {
    /**
     * 根据基因ID查询位置
     * @param geneId 基因ID
     * @return 基因位置信息（null 表示基因不存在）
     */
    Gene getGeneLocation(String geneId);
}
