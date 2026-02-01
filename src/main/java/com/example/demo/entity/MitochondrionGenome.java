package com.example.demo.entity;

import lombok.Data;

/**
 * 适配分表的线粒体基因组实体类（字段对应数据库表的Name/Feature等）
 */
@Data
public class MitochondrionGenome {
    private Long id;                // 自增主键（表中id字段）
    private String name;     // 对应表中“Name”字段（特征名称）
    private String feature;     // 对应表中“Feature”字段（特征类型）
    private Integer start;  // 对应表中“Start”字段（起始位置）
    private Integer end;    // 对应表中“End”字段（结束位置）
    private Integer length;      // 对应表中“Length”字段（序列长度）
    private String strand;          // 对应表中“Strand”字段（链方向）
}