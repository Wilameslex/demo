package com.example.demo.service.impl;

import com.example.demo.dto.GeneNetworkQueryDTO;
import com.example.demo.entity.GeneCombinedInfo;
import com.example.demo.mapper.GeneCombinedMapper;
import com.example.demo.entity.GeneExpression;
import com.example.demo.mapper.GeneExpressionMapper;
import com.example.demo.entity.GeneNetworkData;
import com.example.demo.mapper.GeneNetworkMapper;
import com.example.demo.service.GeneNetworkService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.math.BigDecimal;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GeneNetworkServiceImpl implements GeneNetworkService {

    private final GeneNetworkMapper geneNetworkMapper;
    private final GeneCombinedMapper geneCombinedMapper;
    private final GeneExpressionMapper geneExpressionMapper;

    @Override
    public Map<String, Object> queryGeneNetwork(GeneNetworkQueryDTO query) {
        // 1. 基础网络数据查询
        String tableName = query.getNetworkType() + "_network";

        List<GeneNetworkData> relatedGenes = geneNetworkMapper.queryTop20RelatedGenes(
                tableName,
                query.getTargetGeneId()
        );
        // 2. 构建基因ID列表（目标基因+20个相关基因）
        List<String> allGeneIds = new ArrayList<>();
        allGeneIds.add(query.getTargetGeneId()); // 目标基因
        allGeneIds.addAll(relatedGenes.stream().map(GeneNetworkData::getRelatedGeneId).collect(Collectors.toList())); // 相关基因
        // 3. 批量查询基因基础信息
        List<GeneCombinedInfo> geneInfoList = geneCombinedMapper.queryGeneInfoBatch(allGeneIds);
        Map<String, GeneCombinedInfo> geneInfoMap = geneInfoList.stream()
                .collect(Collectors.toMap(GeneCombinedInfo::getGeneId, info -> info, (a, b) -> a));

        // 4. 批量查询基因表达量
        String exprTableName = query.getNetworkType() + "_gene_rsem"; // 拼接表达量表名
        List<Map<String, Object>> exprList = geneExpressionMapper.queryExprBatch(exprTableName, allGeneIds);
        Map<String, GeneExpression> exprMap = new HashMap<>();
        for (Map<String, Object> exprMapItem : exprList) {
            String geneId = (String) exprMapItem.get("gene_id");
            GeneExpression geneExpression = new GeneExpression();
            geneExpression.setGeneId(geneId);
            // 提取样本表达量（排除gene_id字段，其余为样本列）
            Map<String, BigDecimal> exprData = new HashMap<>();
            for (Map.Entry<String, Object> entry : exprMapItem.entrySet()) {
                if (!"gene_id".equals(entry.getKey())) {
                    exprData.put(entry.getKey(), new BigDecimal(entry.getValue().toString()));
                }
            }
            geneExpression.setExprData(exprData);
            exprMap.put(geneId, geneExpression);
        }

        // 5. 关联相关基因的基础信息
        List<Map<String, Object>> relatedGenesWithInfo = new ArrayList<>();
        for (GeneNetworkData gene : relatedGenes) {
            Map<String, Object> geneMap = new HashMap<>();
            // 原有网络数据
            geneMap.put("relatedGeneId", gene.getRelatedGeneId());
            geneMap.put("correlation", gene.getCorrelation());
            geneMap.put("pvalue", gene.getPvalue());
            geneMap.put("qvalue", gene.getQvalue());
            // 关联基础信息
            GeneCombinedInfo info = geneInfoMap.get(gene.getRelatedGeneId());
            if (info != null) {
                geneMap.put("geneAbbrev", info.getGeneAbbrev());
                geneMap.put("description", info.getDescription());
                geneMap.put("chr", info.getChr());
                geneMap.put("start", info.getStart());
                geneMap.put("end", info.getEnd());
            }
            relatedGenesWithInfo.add(geneMap);
        }



        // 6. 构建返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("targetGeneId", query.getTargetGeneId());
        result.put("networkType", query.getNetworkType());
        result.put("targetGeneInfo", geneInfoMap.get(query.getTargetGeneId())); // 目标基因基础信息
        result.put("relatedGenesWithInfo", relatedGenesWithInfo); // 相关基因（含基础信息）
        result.put("geneExpressionMap", exprMap); // 所有基因的表达量
        result.put("total", relatedGenes.size());

        return result;
    }
}
