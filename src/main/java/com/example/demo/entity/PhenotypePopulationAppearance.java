package com.example.demo.entity;


import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PhenotypePopulationAppearance {
    private Long id;                // 自增主键
    private String Population;      // 群体名称
    private String A1;              // 指标A1
    private String A2;              // 指标A2
    private String A3;              // 指标A3
    private String A4;              // 指标A4
    private String A5;              // 指标A5
    private String A6;              // 指标A6
    private String B1;              // 指标B1
    private String B2;              // 指标B2
    private String B3;              // 指标B3
    private String B4;              // 指标B4
    private String C1;              // 指标C1
    private String C2;              // 指标C2
    private String C3;              // 指标C3
    private String C4;              // 指标C4
    private String S1;              // 指标S1
    private String S2;              // 指标S2
    private String S3;              // 指标S3
    private String S4;              // 指标S4
    private String S5;              // 指标S5
    private String S6;              // 指标S6
    private String S7;              // 指标S7
    private LocalDateTime createTime; // 创建时间
    private LocalDateTime updateTime; // 更新时间
}
