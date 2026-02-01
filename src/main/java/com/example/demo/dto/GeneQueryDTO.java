package com.example.demo.dto;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;
import java.util.List;

@Data
public class GeneQueryDTO {
    @Size(max = 100, message = "基因列表不能超过100个")
    private List<String> genes;

    @Size(max = 100, message = "基因名称列表不能超过100个") // 新增：Name 字段查询条件
    private List<String> names;

    @Size(max = 100, message = "蛋白列表不能超过100个")
    private List<String> proteins;

    @Size(max = 100, message = "产品列表不能超过100个")
    private List<String> products;

    @Min(value = 1, message = "页码最小为1")
    private Integer page = 1;

    @Min(value = 1, message = "每页数量最小为1")
    @Max(value = 100, message = "每页数量最大为100")
    private Integer size = 10;
    // 添加计算offset的getter方法
    public Long getOffset() {
        return (long) (page - 1) * size;
    }
}
