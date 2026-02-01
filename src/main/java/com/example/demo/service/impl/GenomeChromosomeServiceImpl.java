package com.example.demo.service.impl;

import com.example.demo.entity.GenomeChromosome;
import com.example.demo.mapper.GenomeChromosomeMapper;
import com.example.demo.service.GenomeChromosomeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GenomeChromosomeServiceImpl implements GenomeChromosomeService {

    private final GenomeChromosomeMapper chromosomeMapper;

    @Override
    public List<GenomeChromosome> getAllChromosomes() {
        return chromosomeMapper.selectAllChromosomes();
    }
}