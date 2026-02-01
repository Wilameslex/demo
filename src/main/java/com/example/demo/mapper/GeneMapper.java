package com.example.demo.mapper;

import com.example.demo.entity.Gene;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * 基因 Mapper（操作 gene 表）
 */
@Repository // 标记为数据访问组件
public interface GeneMapper {
    /**
     * 根据基因ID查询位置（适配 GeneSearch 接口）
     * @param geneId 基因ID（如 Esi00100001）
     * @return 基因位置信息（chr/start/end）
     */
    Gene getGeneLocation(@Param("geneId") String geneId);
}
