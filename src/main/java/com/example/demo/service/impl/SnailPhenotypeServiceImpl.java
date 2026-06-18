package com.example.demo.service.impl;

import com.example.demo.mapper.SnailPhenotypeMapper;
import com.example.demo.service.SnailPhenotypeService;
import com.example.demo.util.ExportUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SnailPhenotypeServiceImpl implements SnailPhenotypeService {

    private final SnailPhenotypeMapper snailPhenotypeMapper;

    @Override
    public List<Map<String, Object>> getPreviewData(Integer limit) {
        return snailPhenotypeMapper.selectPreview(limit == null || limit <= 0 ? 20 : limit);
    }

    @Override
    public void exportAllDataToExcel(HttpServletResponse response) {
        List<Map<String, Object>> rows = snailPhenotypeMapper.selectAllForExport();
        if (rows == null || rows.isEmpty()) {
            throw new RuntimeException("暂无螺丝表型数据可导出");
        }

        List<String> columns = List.of(
                "sampleId", "sourceId", "SW", "W2W", "AW", "SWd", "SH",
                "BWH", "W2H", "W1H", "AH", "SHt",
                "sampleTime", "createdAt", "updatedAt"
        );
        List<String> headers = List.of(
                "sample_id", "source_id", "SW", "W2W", "AW", "SWd", "SH",
                "BWH", "W2H", "W1H", "AH", "SHt",
                "sample_time", "created_at", "updated_at"
        );

        ExportUtils.exportMapToExcel(
                rows,
                columns,
                headers,
                "Snail_phenotype",
                "Snail",
                response
        );
    }
}
