package com.example.demo.mapper;

import com.example.demo.entity.SequenceResponse;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 序列 Mapper（操作 genome_sequence 表）
 */
@Repository
public interface SequenceMapper {
    /**
     * 按基因ID列表查询序列（适配 SequenceFetch 的 gene 类型）
     * @param geneIds 基因ID列表（如 ["Esi00100001", "Esi00100002"]）
     * @param sequenceType 序列类型（genomic/cds/exons/mrna）
     * @return 序列列表
     */
    List<SequenceResponse> getSequenceByGeneIds(
            @Param("geneIds") List<String> geneIds,
            @Param("sequenceType") String sequenceType
    );

    /**
     * 按染色体区域查询序列（适配 SequenceFetch 的 region 类型）
     * @param chr 染色体ID（如 NC_066509.1）
     * @param start 起始位置
     * @param end 结束位置
     * @param sequenceType 序列类型
     * @return 序列列表
     */
    List<SequenceResponse> getSequenceByRegion(
            @Param("chr") String chr,
            @Param("start") Long start,
            @Param("end") Long end,
            @Param("sequenceType") String sequenceType
    );

    /**
     * 按基因ID查转录本ID（原有方法，用于扩展）
     * @param geneId 基因ID
     * @return 转录本ID列表
     */
    List<String> getTranscriptIdsByGeneId(@Param("geneId") String geneId);


    // ---------------------- 新增方法（核心） ----------------------
    /**
     * 按基因ID列表查询mRNA序列（对应XML中 id="getmRNAByGeneIds"）
     * @param geneIds 基因ID列表（如 ["LOC127000532"]）
     * @return mRNA序列列表
     */
    List<SequenceResponse> getmRNAByGeneIds(@Param("geneIds") List<String> geneIds);

    /**
     * 按基因ID列表查询外显子序列（对应XML中 id="getExonsByGeneIds"）
     * @param geneIds 基因ID列表
     * @return 外显子序列列表
     */
    List<SequenceResponse> getExonsByGeneIds(@Param("geneIds") List<String> geneIds);

    /**
     * 按基因ID列表查询CDS序列（对应XML中 id="getCDSByGeneIds"）
     * @param geneIds 基因ID列表
     * @return CDS序列列表
     */
    List<SequenceResponse> getCDSByGeneIds(@Param("geneIds") List<String> geneIds);
}
