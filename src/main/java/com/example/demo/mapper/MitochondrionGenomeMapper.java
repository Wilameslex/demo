package com.example.demo.mapper;

import com.example.demo.entity.MitochondrionGenome;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface MitochondrionGenomeMapper {
    /**
     * 动态表名查询：根据物种对应的表名，获取线粒体注释信息
     * @param tableName 表名（如sinensis_mitogenome）
     * @return 该物种的线粒体注释列表
     */
    List<MitochondrionGenome> selectByTableName(@Param("tableName") String tableName);
}