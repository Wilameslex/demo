package com.example.demo.entity;

import lombok.Data;

@Data
public class VariantFreq {
    // 与数据库列名完全一致

    private String Ya;          // 数据库CJ列
    private String HP;          // 数据库HP列
    private String JP;          // 数据库JP列
    private String LR;          // 数据库LR列
    private String MJ;          // 数据库MJ列
    private String RU;          // 数据库RU列
    private String YR;          // 数据库YR列

    private String Pos;         // 染色体位置（如chr01:18329）
    private String Variant;     // 变异信息（如C/T、N/<DUP>）

    private String variantType; // 变异类型（"snp"/"indel"/"sv"）
}
