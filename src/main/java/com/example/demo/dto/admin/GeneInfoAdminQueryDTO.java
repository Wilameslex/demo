package com.example.demo.dto.admin;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

/**
 * 基因信息后台分页查询DTO（支持基因名称模糊搜索、状态筛选）
 */
@Data
public class GeneInfoAdminQueryDTO {
    /**
     * 基因ID/名称模糊搜索（可选）
     */
    private String keyword;

    /**
     * 状态筛选（1启用/0禁用，可选）
     */
    private Integer status;

    /**
     * 页码（默认1）
     */
    @Min(value = 1, message = "页码最小为1")
    private Integer pageNum = 1;

    /**
     * 每页条数（默认10，最大50）
     */
    @Min(value = 1, message = "每页条数最小为1")
    @Max(value = 50, message = "每页条数最大为50")
    private Integer pageSize = 10;
}