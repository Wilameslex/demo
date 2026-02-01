package com.example.demo.mapper;

import com.example.demo.entity.GeneNetworkData;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface GeneNetworkMapper {
    // 根据目标基因和网络类型查询Top20相关基因
    List<GeneNetworkData> queryTop20RelatedGenes(
            @Param("tableName") String tableName,
            @Param("targetGeneId") String targetGeneId
    );
}
