package com.example.demo.entity;

import lombok.Data;

/**
 * 序列获取请求实体（对应前端 SequenceFetch 表单）
 */
@Data
public class SequenceRequest {
    private String type;          // 搜索类型：gene（基因ID）/ region（染色体区域）
    private String genes;         // 基因ID列表（多行文本，用换行分隔）
    private String region;        // 染色体区域（格式：chr:start-end，如 NC_066509.1:1000-5000）
    private String sequenceType;  // 序列类型：genomic/cds/exons/mrna
}
