package com.example.demo.mapper;

import com.example.demo.entity.GenomeChromosome;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface GenomeChromosomeMapper {
    /**
     * 查询所有染色体信息（按染色体编号升序）
     */
    List<GenomeChromosome> selectAllChromosomes();
}