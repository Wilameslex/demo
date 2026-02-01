package com.example.demo.service;

import com.example.demo.dto.ProcessExprQueryDTO;
import com.example.demo.entity.ProcessExprCount;
import com.example.demo.entity.ProcessExprBoxplot;
import com.example.demo.entity.ProcessSpecGene;
import java.util.List;
import java.util.Map;

public interface ProcessExprService {
    // 1. 获取表达基因数（柱状图）
    List<ProcessExprCount> getExprCount(ProcessExprQueryDTO query);

    // 2. 获取箱线图数据
    List<ProcessExprBoxplot> getExprBoxplot(ProcessExprQueryDTO query);

    // 3. 获取特异基因数据
    List<ProcessSpecGene> getSpecGene(ProcessExprQueryDTO query);
}
