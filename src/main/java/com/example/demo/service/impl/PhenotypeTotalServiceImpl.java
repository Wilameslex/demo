package com.example.demo.service.impl;

import com.example.demo.entity.PhenotypeTotal;
import com.example.demo.mapper.PhenotypeTotalMapper;
import com.example.demo.service.PhenotypeTotalService;
import com.example.demo.util.ExportUtils; // 导入正确的工具类（util目录）
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PhenotypeTotalServiceImpl implements PhenotypeTotalService {

    private final PhenotypeTotalMapper totalMapper;

    @Override
    public List<PhenotypeTotal> getAllTotalData() {
        return totalMapper.selectAll();
    }

    @Override
    public void exportTotalDataToExcel(HttpServletResponse response) {
        try {
            // 日志1：确认进入方法
            System.out.println("=== 开始导出总表型数据 ===");
            // 1. 查询所有phenotype_total数据
            List<PhenotypeTotal> totalData = totalMapper.selectAll();
            // 日志2：确认数据条数
            System.out.println("查询到总表型数据条数：" + totalData.size());
            if (totalData.isEmpty()) {
                throw new RuntimeException("暂无表型数据可导出");
            }

            // 2. 调用ExportUtils生成Excel
            System.out.println("开始生成Excel文件...");
            ExportUtils.exportToExcel(
                    totalData,
                    "Eriocheir_sinensis_phenotype_total",
                    "河蟹总表型数据",
                    response
            );
            System.out.println("=== Excel导出完成 ===");
        } catch (Exception e) {
            // 日志3：捕获Service层异常
            System.err.println("Service层导出异常：" + e.getMessage());
            throw new RuntimeException("导出失败：" + e.getMessage(), e);
        }
    }
    @Override
    public Long getTotalDataCount() {
        return totalMapper.selectTotalCount();
    }
}