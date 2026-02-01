package com.example.demo.entity;

import lombok.Data;
import java.util.Date;

/**
 * 基因基础信息实体（对应 gene 表）
 */
@Data // Lombok 自动生成 getter/setter/toString
public class Gene {
    private Long id;             // 主键
    private String geneId;       // 基因ID（如 Esi00100001）
    private String chr;          // 染色体ID（如 NC_066509.1）
    private Long start;          // 基因起始位置
    private Long end;            // 基因结束位置
    private Date createTime;     // 创建时间
}