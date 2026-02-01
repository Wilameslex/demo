package com.example.demo.service;

import com.example.demo.entity.GenomeChromosome;
import java.util.List;

public interface GenomeChromosomeService {
    /**
     * 获取所有染色体信息（按编号排序）
     */
    List<GenomeChromosome> getAllChromosomes();
}