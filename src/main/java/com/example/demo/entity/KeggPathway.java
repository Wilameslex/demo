package com.example.demo.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName(value = "gene_pathway", autoResultMap = true)
public class KeggPathway {
    @TableField("Gene")
    private String gene;

    @TableField("Description")
    private String description;

    @TableField("Name")
    private String name;

    @TableField("EC")
    private String ec;

    @TableField("KO")
    private String ko;

    @TableField("KEGG_Gene_ID")
    private String keggGene;

    @TableField("Score")
    private Integer score;
}
