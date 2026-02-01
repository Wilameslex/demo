package com.example.demo.dto;


import lombok.Data;
import javax.validation.constraints.NotNull;

@Data
public class ProcessExprQueryDTO {
    @NotNull(message = "生物学过程不能为空")
    private String process; // metamorphosis/maturity/molting

    @NotNull(message = "分析方法不能为空")
    private String pipeline; // stringtie/rsem

    @NotNull(message = "查询类型不能为空")
    private String searchType; // gene/transcript
}
