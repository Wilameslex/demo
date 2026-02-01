package com.example.demo.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/gene-data")
public class GeneDataController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 按基因ID查询整合信息（对应“搜索基因”场景）
     * 入参：geneId（即gene_combined.gene_id）
     */
    @GetMapping("/by-id")
    public List<Map<String, Object>> getByGeneId(@RequestParam String geneId) {
        String sql = "SELECT " +
                "gene_id, " +
                "gene_abbrev, " +
                "description, " +
                "chr, " +  // 直接使用原始chr值（已确认是char32）
                "start, " +
                "end, " +
                "strand " +
                "FROM gene_combined " +
                "WHERE gene_id = ?";
        return jdbcTemplate.queryForList(sql, geneId);
    }

    /**
     * 按基因名查询（含外显子数据）
     * 入参：geneName（即gene_structure.gene_name，如LOC127006214）
     */
    @GetMapping("/by-name-with-exons")
    public List<Map<String, Object>> getByGeneNameWithExons(@RequestParam String geneName) {
        String sql = "SELECT " +
                "gs.gene_name AS gene_id, " +
                "gc.gene_abbrev, " + // 从gene_combined获取基因缩写
                "gs.description, " +
                "gs.chr, " +
                "gs.start, " +
                "gs.end, " +
                "gs.strand, " +
                "gs.transcripts " +
                "FROM gene_structure gs " +
                // 关联条件：gene_structure.gene_name = gene_combined.gene_id
                "LEFT JOIN gene_combined gc ON gs.gene_name = gc.gene_id " +
                "WHERE gs.gene_name = ?";

        List<Map<String, Object>> result = jdbcTemplate.queryForList(sql, geneName);
        for (Map<String, Object> gene : result) {
            String transcriptsJson = (String) gene.get("transcripts");
            if (transcriptsJson != null && !transcriptsJson.isEmpty()) {
                try {
                    JSONArray transcriptsArray = JSON.parseArray(transcriptsJson);
                    List<Map<String, Object>> transcripts = new ArrayList<>();
                    // 遍历每个转录本，保留transcript_id和对应的exons
                    for (Object transcriptObj : transcriptsArray) {
                        JSONObject transcriptJson = (JSONObject) transcriptObj;
                        Map<String, Object> transcript = new HashMap<>();
                        transcript.put("transcript_id", transcriptJson.getString("transcript_id"));
                        JSONArray exonsArray = transcriptJson.getJSONArray("exons");
                        List<Map<String, Object>> exons = new ArrayList<>();
                        if (exonsArray != null) {
                            @SuppressWarnings("unchecked")
                            List<Map<String, Object>> exonList = (List<Map<String, Object>>) (List<?>) exonsArray.toJavaList(Map.class);
                            // 后端预处理：将外显子start/end转为数字（避免前端重复处理）
                            exonList.forEach(exon -> {
                                exon.put("start", Long.valueOf(exon.get("start").toString()));  // 转为长整数
                                exon.put("end", Long.valueOf(exon.get("end").toString()));
                            });
                            exons.addAll(exonList);
                        }
                        transcript.put("exons", exons);
                        transcripts.add(transcript);
                    }
                    gene.put("transcripts", transcripts); // 每个基因的转录本数组
                } catch (Exception e) {
                    gene.put("transcripts", new ArrayList<>());
                    e.printStackTrace();
                }
            } else {
                gene.put("transcripts", new ArrayList<>());
            }
        }
        return result;
    }

    /**
     * 按染色体区间查询整合信息（对应"搜索染色体范围"场景，含外显子数据）
     * 入参：chr（染色体）、start（区间起始）、end（区间结束）
     * 查询逻辑：返回与查询区间有重叠的所有基因及其完整的转录本和外显子数据
     */
    @GetMapping("/by-region")
    public List<Map<String, Object>> getByRegion(
            @RequestParam String chr,
            @RequestParam Long start,
            @RequestParam Long end) {
        // 查询条件：基因区间与查询区间有重叠
        // 重叠条件：基因的start <= 查询的end AND 基因的end >= 查询的start
        String sql = "SELECT " +
                "gs.gene_name AS gene_id, " +
                "gc.gene_abbrev, " +
                "gs.description, " +
                "gs.chr, " +
                "gs.start, " +
                "gs.end, " +
                "gs.strand, " +
                "gs.transcripts " +
                "FROM gene_structure gs " +
                "LEFT JOIN gene_combined gc ON gs.gene_name = gc.gene_id " +
                "WHERE gs.chr = ? AND gs.start <= ? AND gs.end >= ?";

        List<Map<String, Object>> result = jdbcTemplate.queryForList(sql, chr, end, start);
        
        // 解析每个基因的transcripts JSON，提取外显子数据
        for (Map<String, Object> gene : result) {
            String transcriptsJson = (String) gene.get("transcripts");
            if (transcriptsJson != null && !transcriptsJson.isEmpty()) {
                try {
                    JSONArray transcriptsArray = JSON.parseArray(transcriptsJson);
                    List<Map<String, Object>> transcripts = new ArrayList<>();
                    
                    // 遍历每个转录本
                    for (Object transcriptObj : transcriptsArray) {
                        JSONObject transcriptJson = (JSONObject) transcriptObj;
                        Map<String, Object> transcript = new HashMap<>();
                        transcript.put("transcript_id", transcriptJson.getString("transcript_id"));
                        
                        // 提取外显子数据
                        JSONArray exonsArray = transcriptJson.getJSONArray("exons");
                        List<Map<String, Object>> exons = new ArrayList<>();
                        if (exonsArray != null) {
                            @SuppressWarnings("unchecked")
                            List<Map<String, Object>> exonList = (List<Map<String, Object>>) (List<?>) exonsArray.toJavaList(Map.class);
                            // 将外显子start/end转为数字
                            exonList.forEach(exon -> {
                                exon.put("start", Long.valueOf(exon.get("start").toString()));
                                exon.put("end", Long.valueOf(exon.get("end").toString()));
                            });
                            exons.addAll(exonList);
                        }
                        transcript.put("exons", exons);
                        transcripts.add(transcript);
                    }
                    gene.put("transcripts", transcripts);
                } catch (Exception e) {
                    gene.put("transcripts", new ArrayList<>());
                    e.printStackTrace();
                }
            } else {
                gene.put("transcripts", new ArrayList<>());
            }
        }
        return result;
    }
}