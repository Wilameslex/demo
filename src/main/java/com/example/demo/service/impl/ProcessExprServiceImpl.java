package com.example.demo.service.impl;

import com.example.demo.dto.ProcessExprQueryDTO;
import com.example.demo.entity.ProcessExprCount;
import com.example.demo.entity.ProcessExprBoxplot;
import com.example.demo.entity.ProcessSpecGene;
import com.example.demo.mapper.ProcessExprMapper;
import com.example.demo.service.ProcessExprService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ProcessExprServiceImpl implements ProcessExprService {
    private final ProcessExprMapper processExprMapper;



    // 允许的参数值（防止SQL注入）
    private static final Set<String> ALLOWED_PROCESSES = new HashSet<>(Arrays.asList("metamorphosis", "maturity", "molting","carcinization","premature","outwater","photoperiodschange","relimb"));
    private static final Set<String> ALLOWED_PIPELINES = new HashSet<>(Arrays.asList("stringtie", "rsem"));
    private static final Set<String> ALLOWED_SEARCH_TYPES = new HashSet<>(Arrays.asList("gene", "transcript"));

    @Override
    public List<ProcessExprCount> getExprCount(ProcessExprQueryDTO query) {
        // 1. 参数校验
        validateQuery(query);
        // 2. 映射searchType到表名后缀：gene→gene，transcript→rna
        String tableSuffix = query.getSearchType().equals("gene") ? "gene" : "rna";
        // 3. 查询动态表
        return processExprMapper.selectExprCount(
                tableSuffix,
                query.getProcess(),
                query.getPipeline()
        );
    }

    @Override
    public List<ProcessExprBoxplot> getExprBoxplot(ProcessExprQueryDTO query) {
        validateQuery(query);
        String tableSuffix = query.getSearchType().equals("gene") ? "gene" : "rna";
        return processExprMapper.selectExprBoxplot(
                tableSuffix,
                query.getProcess(),
                query.getPipeline()
        );
    }

    @Override
    public List<ProcessSpecGene> getSpecGene(ProcessExprQueryDTO query) {
        validateQuery(query);
        String tableSuffix = query.getSearchType().equals("gene") ? "gene" : "rna";
        return processExprMapper.selectSpecGene(
                tableSuffix,
                query.getProcess(),
                query.getPipeline()
        );
    }

    // 参数合法性校验（防止非法输入导致SQL注入）
    private void validateQuery(ProcessExprQueryDTO query) {
        if (!ALLOWED_PROCESSES.contains(query.getProcess())) {
            throw new IllegalArgumentException("不支持的生物学过程：" + query.getProcess());
        }
        if (!ALLOWED_PIPELINES.contains(query.getPipeline())) {
            throw new IllegalArgumentException("不支持的分析方法：" + query.getPipeline());
        }
        if (!ALLOWED_SEARCH_TYPES.contains(query.getSearchType())) {
            throw new IllegalArgumentException("不支持的查询类型：" + query.getSearchType());
        }
    }

}
