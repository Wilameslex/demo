package com.example.demo.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName(value = "gene_info", autoResultMap = true)
public class GeneInfo {
    @TableField("Gene")
    private String gene;

    @TableField("Name")
    private String name;

    @TableField("Chromosome")
    private String chromosome;

    @TableField("Start")
    private Integer start;

    @TableField("End")
    private Integer end;

    @TableField("Protein")
    private String protein;

    @TableField("Product")
    private String product;

    @TableField("Description")
    private String description;

    // 新增：时间戳字段，标记填充策略
    @TableField(value = "create_time", fill = FieldFill.INSERT) // 仅插入时填充
    private LocalDateTime createTime;
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE) // 插入+更新时填充
    private LocalDateTime updateTime;

    // 新增：状态字段，标记插入时填充（默认1）
    @TableField(value = "status", fill = FieldFill.INSERT) // 仅插入时填充
    private Integer status;
    @TableField(value = "deleted", fill = FieldFill.INSERT) // 仅插入时填充
    private Integer deleted;
}