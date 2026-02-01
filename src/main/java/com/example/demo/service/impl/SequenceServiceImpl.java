package com.example.demo.service.impl;

import com.example.demo.entity.SequenceRequest;
import com.example.demo.entity.SequenceResponse;
import com.example.demo.mapper.ChromosomeMapper;
import com.example.demo.mapper.SequenceMapper;
import com.example.demo.service.SequenceService;
import htsjdk.samtools.reference.IndexedFastaSequenceFile;
import htsjdk.samtools.reference.ReferenceSequence;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 序列服务实现类
 */
@Service
public class SequenceServiceImpl implements SequenceService {

    @Autowired
    private SequenceMapper sequenceMapper;
    @Autowired
    private ChromosomeMapper chromosomeMapper;

    // 关键：FASTA文件路径（resources目录下）
    private static final String FASTA_FILE_PATH = "genome.fa";
    // 区域查询默认序列类型（固定为genomic）
    private static final String DEFAULT_REGION_SEQ_TYPE = "genomic";


    // ---------------------- 核心方法：getSequence（与getSequenceFromFasta同级） ----------------------
    @Override
    public List<SequenceResponse> getSequence(SequenceRequest request) throws IllegalArgumentException {
        // 1. 基础参数验证
        String type = request.getType();
        String sequenceType = request.getSequenceType();
        List<SequenceResponse> sequences = new ArrayList<>();

        if (!"gene".equals(type) && !"region".equals(type)) {
            throw new IllegalArgumentException("Search type must be 'gene' or 'region'");
        }
        // 仅基因查询需要校验sequenceType，区域查询跳过（但需避免null）
        if ("gene".equals(type) && (sequenceType == null || !Arrays.asList("genomic", "cds", "exons", "mrna").contains(sequenceType))) {
            throw new IllegalArgumentException("Sequence type must be 'genomic', 'cds', 'exons' or 'mrna'");
        }


        // 2. 基因查询（原逻辑不变）
        if ("gene".equals(type)) {
            String genes = request.getGenes();
            if (!StringUtils.hasText(genes)) {
                throw new IllegalArgumentException("Please enter gene names/IDs");
            }
            List<String> geneIds = Arrays.stream(genes.split("\n"))
                    .map(String::trim)
                    .filter(StringUtils::hasText)
                    .distinct()
                    .collect(Collectors.toList());

            switch (sequenceType) {
                case "genomic":
                    sequences = sequenceMapper.getSequenceByGeneIds(geneIds, sequenceType);
                    break;
                case "mrna":
                    sequences = sequenceMapper.getmRNAByGeneIds(geneIds);
                    break;
                case "exons":
                    sequences = sequenceMapper.getExonsByGeneIds(geneIds);
                    break;
                case "cds":
                    sequences = sequenceMapper.getCDSByGeneIds(geneIds);
                    break;
            }
        }


        // 3. 区域查询（批量处理，修复大括号闭合）
        else {
            String regionInput = request.getRegion();
            if (!StringUtils.hasText(regionInput)) {
                throw new IllegalArgumentException("Please enter chromosome regions (one per line)");
            }

            // 拆分多个区域
            List<String> regionList = Arrays.stream(regionInput.split("\n"))
                    .map(String::trim)
                    .filter(line -> StringUtils.hasText(line) && line.matches("^[^:]+:\\d+-\\d+$"))
                    .distinct()
                    .collect(Collectors.toList());

            if (regionList.isEmpty()) {
                throw new IllegalArgumentException("No valid regions! Example: char1:1000-5000");
            }

            // 循环处理每个区域
            for (String singleRegion : regionList) {
                try {
                    // 解析染色体和位置
                    String[] regionParts = singleRegion.split(":");
                    String inputChr = regionParts[0].trim();
                    String fastaChr = inputChr;
                    String charFormat = null;

                    // char→NC 或 NC→char
                    if (inputChr.startsWith("char")) {
                        fastaChr = chromosomeMapper.getNcByChr(inputChr);
                        charFormat = inputChr;
                        if (fastaChr == null) {
                            throw new IllegalArgumentException("No NC mapping for: " + inputChr);
                        }
                    } else {
                        charFormat = chromosomeMapper.getChrByNc(fastaChr);
                    }

                    // 解析位置
                    String[] posParts = regionParts[1].split("-");
                    Long start = Long.parseLong(posParts[0].trim());
                    Long end = Long.parseLong(posParts[1].trim());
                    if (start >= end) {
                        throw new IllegalArgumentException("Start > End in region: " + singleRegion);
                    }
                    if (start < 1) {
                        throw new IllegalArgumentException("Start ≥1 required in region: " + singleRegion);
                    }

                    // 截取序列
                    String regionSequence = getSequenceFromFasta(fastaChr, start, end);

                    // 组装结果（含char序号）
                    SequenceResponse response = new SequenceResponse();
                    String chrName = charFormat != null ? charFormat + "(" + fastaChr + ")" : fastaChr;
                    response.setId(chrName + ":" + start + "-" + end + "_" + DEFAULT_REGION_SEQ_TYPE);
                    response.setSequence(regionSequence);
                    sequences.add(response);

                } catch (Exception e) {
                    throw new IllegalArgumentException("Failed to process region '" + singleRegion + "': " + e.getMessage());
                }
            }
        }

        return sequences; // getSequence方法的返回值
    } // 关键：此处必须闭合getSequence方法的大括号


    // ---------------------- 正确位置：getSequenceFromFasta与getSequence同级 ----------------------
    private String getSequenceFromFasta(String chr, Long start, Long end) throws Exception {
        // 读取resources目录下的FASTA文件
        ClassPathResource resource = new ClassPathResource(FASTA_FILE_PATH);
        File fastaFile = resource.getFile();

        // 校验索引文件
        File faiFile = new File(fastaFile.getParent(), fastaFile.getName() + ".fai");
        if (!faiFile.exists()) {
            throw new Exception("FASTA index missing! Generate with 'samtools faidx " + fastaFile.getName() + "'");
        }

        // 读取并截取序列
        try (IndexedFastaSequenceFile fastaReader = new IndexedFastaSequenceFile(fastaFile)) {
            ReferenceSequence refSeq = fastaReader.getSequence(chr);
            if (refSeq == null) {
                throw new Exception("Chromosome '" + chr + "' not found in FASTA");
            }
            if (end > refSeq.length()) {
                throw new Exception("End (" + end + ") > Chromosome length (" + refSeq.length() + ")");
            }

            // 截取碱基（FASTA从0开始，start需减1）
            byte[] seqBytes = refSeq.getBases();
            return new String(seqBytes, (int) (start - 1), (int) (end - start + 1));
        }
    }
}