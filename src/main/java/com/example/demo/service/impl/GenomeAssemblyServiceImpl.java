package com.example.demo.service.impl;

import com.example.demo.entity.GenomeAssembly;
import com.example.demo.mapper.GenomeAssemblyMapper;
import com.example.demo.service.GenomeAssemblyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GenomeAssemblyServiceImpl implements GenomeAssemblyService {

    private final GenomeAssemblyMapper assemblyMapper;

    @Override
    public GenomeAssembly getEriocheirSinensisAssembly() {
        GenomeAssembly assembly = assemblyMapper.selectEriocheirSinensisAssembly();
        if (assembly == null) {
            throw new IllegalArgumentException("未找到中华绒螯蟹参考基因组（ASM2467909v1）");
        }
        return assembly;
    }
}