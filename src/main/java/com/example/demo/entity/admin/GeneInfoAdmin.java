package com.example.demo.entity.admin;

import com.baomidou.mybatisplus.annotation.*;
import com.example.demo.entity.GeneInfo;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 基因信息后台管理实体类（扩展原有GeneInfo，补充管理字段）
 * 继承原有GeneInfo，避免重复字段定义
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "gene_info", autoResultMap = true) // 复用原有gene_info表
public class GeneInfoAdmin extends GeneInfo {


    /**
     * 状态：1启用（前台可见）/0禁用（前台不可见）
     */
    private Integer status;

    /**
     * 创建时间（自动填充，无需手动设置）
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间（自动填充，无需手动设置）
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 逻辑删除标记（0未删除/1已删除，后台删除默认逻辑删除）
     */
    @TableLogic
    @TableField(select = false) // 查询时默认不返回该字段
    private Integer deleted;
}