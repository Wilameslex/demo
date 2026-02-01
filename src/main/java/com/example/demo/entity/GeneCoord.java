package com.example.demo.entity;

import lombok.Data;

@Data
public class GeneCoord {
    private String geneId;
    private String chr;
    private Long start;
    private Long end;
    private Long startExtend;   // 上游扩展后起始
    private Long endExtend;     // 下游扩展后结束
}
