// com/example/demo/entity/GeneNetworkData.java
package com.example.demo.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class GeneNetworkData {
    private Long id;
    private String targetGeneId; // 目标基因ID
    private String relatedGeneId; // 相关基因ID
    private Double correlation; // 相关性系数
    private Double pvalue; // p值
    private Double qvalue; // q值（FDR校正）
}
