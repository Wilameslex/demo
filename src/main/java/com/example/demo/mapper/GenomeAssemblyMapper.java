package com.example.demo.mapper;

import com.example.demo.entity.GenomeAssembly;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface GenomeAssemblyMapper {
    /**
     * 固定查询中华绒螯蟹参考基因组（ASM2467909v1）
     */
    GenomeAssembly selectEriocheirSinensisAssembly();
}