package com.example.demo.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PhenotypePopulationGrowth {
    private Long id;                // 自增主键
    private String population;      // 群体名称
    private String date;            // 采集日期
    private Integer number;         // 样本数量
    private String carapaceLength;  // 甲壳长度（mm）
    private String carapaceWidth;   // 甲壳宽度（mm）
    private String fatFactor;       // 肥满度（替代原conditionFactor）
    private String hepatopancreasIndex; // 肝胰腺指数（%）
    private String gonadIndex;      // 生殖腺指数（%）
    private LocalDateTime createTime; // 创建时间
    private LocalDateTime updateTime; // 更新时间
}