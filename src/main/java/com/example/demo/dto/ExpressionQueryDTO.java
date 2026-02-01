package com.example.demo.dto;

import lombok.Data;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class ExpressionQueryDTO {
    @NotNull(message = "分析方法不能为空")
    private String pipeline; // "stringtie" 或 "rsem"

    @NotNull(message = "转录组类型不能为空")
    private String transcriptome; // "maturity" 或 "metamorphosis"

    @NotNull(message = "搜索类型不能为空")
    private String searchType; // "gene" 或 "transcript"

    @NotNull(message = "目标ID列表不能为空")
    private List<String> targetIds;

    private List<String> selectedSamples; // 用户选择的样本列
}
