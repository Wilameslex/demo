package com.example.demo.dto.admin;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;

/**
 * 基因信息后台新增/编辑DTO（仅包含可编辑字段，排除自动填充的时间/主键）
 */
@Data
public class GeneInfoAdminDTO {

    /**
     * 基因ID（必传，如LOC127000280）
     */
    @NotBlank(message = "基因ID不能为空")
    @Length(max = 50, message = "基因ID长度不能超过50字符")
    private String gene;

    /**
     * 基因名称（必传）
     */
    @NotBlank(message = "基因名称不能为空")
    @Length(max = 100, message = "基因名称长度不能超过100字符")
    private String name;

    /**
     * 染色体（必传，如char1）
     */
    @NotBlank(message = "染色体不能为空")
    @Length(max = 20, message = "染色体长度不能超过20字符")
    private String chromosome;

    /**
     * 起始位置（必传，正整数）
     */
    @NotNull(message = "起始位置不能为空")
    @PositiveOrZero(message = "起始位置必须为非负整数")
    private Integer start;

    /**
     * 结束位置（必传，大于起始位置）
     */
    @NotNull(message = "结束位置不能为空")
    @PositiveOrZero(message = "结束位置必须为非负整数")
    private Integer end;

    /**
     * 蛋白ID（可选）
     */
    @NotBlank(message = "蛋白质ID不能为空")
    @Length(max = 50, message = "蛋白ID长度不能超过50字符")
    private String protein;

    /**
     * 产物（可选）
     */
    @Length(max = 200, message = "产物长度不能超过200字符")
    private String product;

    /**
     * 描述（可选）
     */
    @Length(max = 500, message = "描述长度不能超过500字符")
    private String description;

    /**
     * 状态（必传：1启用/0禁用）
     */
    @NotNull(message = "状态不能为空")
    private Integer status;
}
