package com.example.demo.mapper;

import com.example.demo.entity.ProcessExprCount;
import com.example.demo.entity.ProcessExprBoxplot;
import com.example.demo.entity.ProcessSpecGene;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface ProcessExprMapper {
    // 动态表名：process_${tableSuffix}_count
    List<ProcessExprCount> selectExprCount(
            @Param("tableSuffix") String tableSuffix,
            @Param("process") String process,
            @Param("pipeline") String pipeline
    );

    // 动态表名：process_${tableSuffix}_boxplot
    List<ProcessExprBoxplot> selectExprBoxplot(
            @Param("tableSuffix") String tableSuffix,
            @Param("process") String process,
            @Param("pipeline") String pipeline
    );

    // 动态表名：process_${tableSuffix}_spec
    List<ProcessSpecGene> selectSpecGene(
            @Param("tableSuffix") String tableSuffix,
            @Param("process") String process,
            @Param("pipeline") String pipeline
    );
}