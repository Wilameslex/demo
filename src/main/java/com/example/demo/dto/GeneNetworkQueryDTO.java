package com.example.demo.dto;


import lombok.Data;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
public class GeneNetworkQueryDTO {
    @NotNull(message = "网络类型不能为空")
    private String networkType; // 网络类型：metamorphosis/maturity
    @NotEmpty(message = "目标基因ID不能为空")
    private String targetGeneId; // 输入的目标基因ID
}
