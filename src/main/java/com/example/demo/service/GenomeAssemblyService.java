package com.example.demo.service;

import com.example.demo.entity.GenomeAssembly;

public interface GenomeAssemblyService {
    /**
     * 获取中华绒螯蟹参考基因组信息（固定ASM2467909v1）
     */
    GenomeAssembly getEriocheirSinensisAssembly();
}