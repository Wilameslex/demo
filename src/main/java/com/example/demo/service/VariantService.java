package com.example.demo.service;

import com.example.demo.entity.GeneCoord; // 修正：用你实际的GeneCoord类（非GeneCoordinate）
import com.example.demo.entity.VariantFreq;
import com.example.demo.mapper.VariantFreqMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class VariantService {
    // 仅注入你实际使用的VariantFreqMapper（无geneMapper，之前是伪代码笔误）
    @Autowired
    private VariantFreqMapper variantFreqMapper;

    // 变异类型→表名映射（不变）
    private static final Map<String, String> TYPE_TABLE_MAP = new HashMap<String, String>() {{
        put("snp", "variant_freq_snp");
        put("indel", "variant_freq_indel");
        put("sv", "variant_freq_sv");
    }};

    // ====================== 1. 按基因查询（修正类名、依赖、参数逻辑） ======================
    // 修正：upstream/downstream参数类型为Integer（与你Mapper方法一致）
    public Map<String, Object> getVariantsByGene(
            String geneId, Integer upstream, Integer downstream, Integer size) {
        Map<String, Object> result = new HashMap<>();

        // 1. 修正：查询基因坐标（用VariantFreqMapper+GeneCoord类，传递所有参数）
        // 注意：getGeneCoord方法是你之前定义的，已包含“start-upstream”“end+downstream”逻辑
        GeneCoord geneCoord = variantFreqMapper.getGeneCoord(geneId, upstream, downstream);
        if (geneCoord == null) {
            throw new RuntimeException("Gene ID not found: " + geneId);
        }

        // 2. 提取扩展后的区间（用GeneCoord的实际字段：startExtend/endExtend）
        String chr = geneCoord.getChr();
        Long start = geneCoord.getStartExtend(); // 已包含上游扩展
        Long end = geneCoord.getEndExtend();     // 已包含下游扩展

        // 3. 查询SNP/INDEL/SV（逻辑不变）
        List<VariantFreq> snps = getSingleTypeVariants("snp", chr, start, end, size);
        int snpTotal = countSingleTypeVariants("snp", chr, start, end);
        List<VariantFreq> indels = getSingleTypeVariants("indel", chr, start, end, size);
        int indelTotal = countSingleTypeVariants("indel", chr, start, end);
        List<VariantFreq> svs = getSingleTypeVariants("sv", chr, start, end, size);
        int svTotal = countSingleTypeVariants("sv", chr, start, end);

        // 4. 组装结果（逻辑不变，FST用分组方法）
        result.put("snps", snps);
        result.put("snpTotal", snpTotal);
        result.put("indels", indels);
        result.put("indelTotal", indelTotal);
        result.put("svs", svs);
        result.put("svTotal", svTotal);
        result.put("geneCoord", geneCoord);
        // 修正：FST用处理宽表的分组方法（与染色体查询逻辑统一）
        result.put("fstMap", getFstByChrPosGrouped(chr, start, end));
        result.put("piMap", getPiByChrPosGrouped(chr, start, end));

        return result;
    }

    // ====================== 2. 按染色体位置查询（逻辑不变，仅确保FST调用正确） ======================
    public Map<String, Object> getVariantsByChrPos(
            String chr, Long start, Long end, Integer size) {
        Map<String, Object> result = new HashMap<>();

        // 查询SNP/INDEL/SV（逻辑不变）
        List<VariantFreq> snps = getSingleTypeVariants("snp", chr, start, end, size);
        int snpTotal = countSingleTypeVariants("snp", chr, start, end);
        List<VariantFreq> indels = getSingleTypeVariants("indel", chr, start, end, size);
        int indelTotal = countSingleTypeVariants("indel", chr, start, end);
        List<VariantFreq> svs = getSingleTypeVariants("sv", chr, start, end, size);
        int svTotal = countSingleTypeVariants("sv", chr, start, end);

        // 组装结果（FST用分组方法）
        result.put("snps", snps);
        result.put("snpTotal", snpTotal);
        result.put("indels", indels);
        result.put("indelTotal", indelTotal);
        result.put("svs", svs);
        result.put("svTotal", svTotal);
        result.put("region", Map.of("chr", chr, "start", start, "end", end));
        result.put("fstMap", getFstByChrPosGrouped(chr, start, end)); // 与基因查询统一
        result.put("piMap", getPiByChrPosGrouped(chr, start, end));

        return result;
    }

    // ====================== 3. 下载查询结果（逻辑不变） ======================
    public List<Map<String, Object>> downloadVariants(
            String variantType, String chr, Long start, Long end) {
        if (!TYPE_TABLE_MAP.containsKey(variantType)) {
            throw new IllegalArgumentException("Invalid variant type: " + variantType);
        }

        List<VariantFreq> variants = variantFreqMapper.getVariantsByType(
                TYPE_TABLE_MAP.get(variantType),
                chr, start, end,
                0, Integer.MAX_VALUE
        );

        variants.forEach(v -> v.setVariantType(variantType.toUpperCase()));

        return variants.stream().map(variant -> {
            Map<String, Object> row = new HashMap<>();
            row.put("Pos", variant.getPos());
            row.put("Variant", variant.getVariant());
            row.put("LR", cleanFreqValue(variant.getLR()));
            row.put("YR", cleanFreqValue(variant.getYR()));
            row.put("JP", cleanFreqValue(variant.getJP()));
            row.put("MJ", cleanFreqValue(variant.getMJ()));
            row.put("RU", cleanFreqValue(variant.getRU()));
            row.put("HP", cleanFreqValue(variant.getHP()));
            row.put("Ya", cleanFreqValue(variant.getYa()));
            row.put("variant_type", variantType.toUpperCase());
            return row;
        }).collect(Collectors.toList());
    }

    // ====================== 4. 供Controller调用的Public方法（逻辑不变） ======================
    public List<VariantFreq> getVariantsByType(
            String variantType, String chr, Long start, Long end, Integer size) {
        return getSingleTypeVariants(variantType, chr, start, end, size);
    }

    public int countVariantsByType(
            String variantType, String chr, Long start, Long end) {
        return countSingleTypeVariants(variantType, chr, start, end);
    }

    // ====================== 5. 辅助方法（逻辑不变） ======================
    private List<VariantFreq> getSingleTypeVariants(
            String variantType, String chr, Long start, Long end, Integer size) {
        List<VariantFreq> variants = variantFreqMapper.getVariantsByType(
                TYPE_TABLE_MAP.get(variantType),
                chr, start, end,
                0, size
        );
        if (!variants.isEmpty()) {
            VariantFreq first = variants.get(0);
            System.out.println("第一条数据Ya：" + first.getYa()); // 应输出"1.000|0.000|0.000"
            System.out.println("第一条数据HP：" + first.getHP());
        }

        variants.forEach(variant -> {
            variant.setLR(cleanFreqValue(variant.getLR()));
            variant.setYR(cleanFreqValue(variant.getYR()));
            variant.setJP(cleanFreqValue(variant.getJP()));
            variant.setMJ(cleanFreqValue(variant.getMJ()));
            variant.setRU(cleanFreqValue(variant.getRU()));
            variant.setHP(cleanFreqValue(variant.getHP()));
            variant.setYa(cleanFreqValue(variant.getYa()));
            variant.setVariantType(variantType.toUpperCase());
        });

        return variants;
    }

    private String cleanFreqValue(String freqStr) {
        // 1. 处理空值、NaN、空字符串
        if (freqStr == null || freqStr.trim().isEmpty() || "-nan".equalsIgnoreCase(freqStr.trim())) {
            return "-";
        }
        // 2. 处理多值频率（如"1.000|0.000|0.000"）
        if (freqStr.contains("|")) {
            String[] freqParts = freqStr.split("\\|"); // 拆分多值
            StringBuilder formatted = new StringBuilder();
            for (String part : freqParts) {
                try {
                    // 逐个格式化数值，保留4位小数
                    double partFreq = Double.parseDouble(part.trim());
                    formatted.append(String.format("%.4f", partFreq)).append("|");
                } catch (NumberFormatException e) {
                    // 单个值解析失败时，用"-"替代该部分
                    formatted.append("-|");
                }
            }
            // 移除最后一个多余的"|"
            return !formatted.isEmpty() ? formatted.substring(0, formatted.length() - 1) : "-";
        }
        // 3. 处理单值频率（兼容未来可能的单值场景）
        try {
            double freq = Double.parseDouble(freqStr.trim());
            return String.format("%.4f", freq);
        } catch (NumberFormatException e) {
            return "-";
        }
    }

    private int countSingleTypeVariants(
            String variantType, String chr, Long start, Long end) {
        return variantFreqMapper.countVariantsByType(
                TYPE_TABLE_MAP.get(variantType),
                chr, start, end
        );
    }

    // ====================== 6. FST宽表转长表+分组方法（逻辑不变） ======================
    public List<Map<String, Object>> convertFstWideToLong(String chr, Long start, Long end) {
        List<Map<String, Object>> rawFstList = variantFreqMapper.getFstByChrPos(chr, start, end);
        List<Map<String, Object>> processedFstList = new ArrayList<>();

        if (rawFstList != null) {
            for (Map<String, Object> row : rawFstList) {
                String chrom = (String) row.get("CHROM");
                Long pos = (Long) row.get("POS");
                // 遍历所有群体对列（与你fst_result表的列一致）
                processPopulationPair(row, "YavsMJ", chrom, pos, processedFstList);
                processPopulationPair(row, "YavsHP", chrom, pos, processedFstList);
                processPopulationPair(row, "YavsJP", chrom, pos, processedFstList);
                processPopulationPair(row, "YavsRU", chrom, pos, processedFstList);
                processPopulationPair(row, "HPvsRU", chrom, pos, processedFstList);
                processPopulationPair(row, "HPvsJP", chrom, pos, processedFstList);
                processPopulationPair(row, "YavsLR", chrom, pos, processedFstList);
                processPopulationPair(row, "YavsYR", chrom, pos, processedFstList);
            }
        }

        return processedFstList;
    }

    private void processPopulationPair(Map<String, Object> row, String populationPair, String chrom, Long pos, List<Map<String, Object>> processedList) {
        Float fstValue = (Float) row.get(populationPair);
        if (fstValue != null) {
            Map<String, Object> fstItem = new HashMap<>();
            fstItem.put("chrom", chrom);
            fstItem.put("pos", pos);
            fstItem.put("populationPair", populationPair);
            fstItem.put("weirAndCockerhamFst", fstValue);
            processedList.add(fstItem);
        }
    }

    public Map<String, List<Map<String, Object>>> getFstByChrPosGrouped(String chr, Long start, Long end) {
        List<Map<String, Object>> fstList = convertFstWideToLong(chr, start, end);
        // 过滤null key，避免分组异常
        return fstList.stream()
                .filter(item -> item.get("populationPair") != null)
                .collect(Collectors.groupingBy(item -> (String) item.get("populationPair")));
    }

    // ====================== 7. 旧FST方法（可选保留，仅用于兼容，建议后续删除） ======================
    public Map<String, List<Map<String, Object>>> getFstByChrPos(String chr, Long start, Long end) {
        List<Map<String, Object>> fstList = variantFreqMapper.getFstByChrPos(chr, start, end);
        if (fstList == null) {
            return new HashMap<>();
        }
        // 注意：此方法直接用宽表数据分组，会因无populationPair字段抛null key异常，建议删除
        return fstList.stream()
                .collect(Collectors.groupingBy(
                        (Map<String, Object> fst) -> (String) fst.get("populationPair")
                ));
    }

    // ====================== Pi专属处理方法（模仿FST逻辑） ======================
    /**
     * Pi宽表转长表（7个群体→每条记录对应一个群体）
     */
    public List<Map<String, Object>> convertPiWideToLong(String chr, Long start, Long end) {
        List<Map<String, Object>> rawPiList = variantFreqMapper.getPiByChrPos(chr, start, end);
        List<Map<String, Object>> processedPiList = new ArrayList<>();
        if (rawPiList != null) {
            for (Map<String, Object> row : rawPiList) {
                String chrom = (String) row.get("CHROM");
                Long pos = (Long) row.get("POS");
                // 遍历7个群体，生成单独条目
                processPiPopulation(row, "YR", chrom, pos, processedPiList);
                processPiPopulation(row, "LR", chrom, pos, processedPiList);
                processPiPopulation(row, "Ya", chrom, pos, processedPiList);
                processPiPopulation(row, "MJ", chrom, pos, processedPiList);
                processPiPopulation(row, "HP", chrom, pos, processedPiList);
                processPiPopulation(row, "JP", chrom, pos, processedPiList);
                processPiPopulation(row, "RU", chrom, pos, processedPiList);
            }
        }
        return processedPiList;
    }

    /**
     * 辅助：处理单个群体的Pi值
     */
    private void processPiPopulation(Map<String, Object> row, String population, String chrom, Long pos, List<Map<String, Object>> processedList) {
        Float piValue = (Float) row.get(population);
        if (piValue != null) {
            Map<String, Object> piItem = new HashMap<>();
            piItem.put("chrom", chrom);
            piItem.put("pos", pos);
            piItem.put("population", population);  // 群体名（如YR/LR）
            piItem.put("piValue", piValue);        // Pi值
            processedList.add(piItem);
        }
    }

    // Pi方法（暂未使用，保留注释）
    public Map<String, List<Map<String, Object>>> getPiByChrPosGrouped(String chr, Long start, Long end) {
    List<Map<String, Object>> piList = convertPiWideToLong(chr, start, end);
    // 过滤null key，避免分组异常
    return piList.stream()
            .filter(item -> item.get("population") != null)
            .collect(Collectors.groupingBy(item -> (String) item.get("population")));
}
}