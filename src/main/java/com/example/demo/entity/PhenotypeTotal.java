package com.example.demo.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PhenotypeTotal {
    private Long id;                // 新增：自增主键（数据库id）
    private String sampleId;        // 样本ID（允许空）
    private String group;      // 群体/产地
    private String date;            // 采集日期
    private String wildSampleId;    // 野生样本ID
    private String gender;          // 性别
    private Double weight;          // 体重（g）
    // 指标字段改为大写（对应数据库A1、A2...）
    private Double A1;              // 表型指标A1
    private Double A2;              // 表型指标A2
    private Double A3;              // 表型指标A3
    private Double A4;              // 表型指标A4
    private Double A5;              // 表型指标A5
    private Double A6;              // 表型指标A6
    private Double A7;              // 表型指标A7
    private Double B1;              // 表型指标B1
    private Double B2;              // 表型指标B2
    private Double B3;              // 表型指标B3
    private Double B4;              // 表型指标B4
    private Double C1;              // 表型指标C1
    private Double C2;              // 表型指标C2
    private Double C3;              // 表型指标C3
    private Double C4;              // 表型指标C4
    private Double L1;              // 表型指标L1
    private Double L2;              // 表型指标L2
    private Double L3;              // 表型指标L3
    private Double L4;              // 表型指标L4
    private Double L5;              // 表型指标L5
    private Double H;               // 表型指标H
    private Double S1;              // 表型指标S1
    private Double S2;              // 表型指标S2
    private Double S3;              // 表型指标S3
    private Double S4;              // 表型指标S4
    private Double S5;              // 表型指标S5
    private Double S6;              // 表型指标S6
    private Double S7;              // 表型指标S7
    private Double F1;              // 表型指标F1
    private Double F2;              // 表型指标F2
    private Double F3;              // 表型指标F3
    private Double F4;              // 表型指标F4
    private Double hepatopancreas;  // 肝胰腺
    private Double gonad;           // 生殖腺
    private String remarks;         // 备注1
    private String remarks2;        // 备注2
    private LocalDateTime createTime; // 创建时间
    private LocalDateTime updateTime; // 更新时间
}