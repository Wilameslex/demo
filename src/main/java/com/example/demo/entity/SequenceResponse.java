package com.example.demo.entity;

import lombok.Data;

/**
 * 序列获取响应实体（对应前端 sequences 数组）
 */
@Data
public class SequenceResponse {
    private String id;         // 序列ID（如 Esi00100001_genomic）
    private String sequence;   // 序列内容（FASTA格式）
}
