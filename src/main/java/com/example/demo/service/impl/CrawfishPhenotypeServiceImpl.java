package com.example.demo.service.impl;

import com.example.demo.mapper.CrawfishPhenotypeMapper;
import com.example.demo.service.CrawfishPhenotypeService;
import com.example.demo.util.ExportUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CrawfishPhenotypeServiceImpl implements CrawfishPhenotypeService {

    private final CrawfishPhenotypeMapper crawfishPhenotypeMapper;

    @Override
    public List<Map<String, Object>> getPreviewData(Integer limit) {
        return crawfishPhenotypeMapper.selectPreview(limit == null || limit <= 0 ? 20 : limit);
    }

    @Override
    public void exportAllDataToExcel(HttpServletResponse response) {
        List<Map<String, Object>> rows = crawfishPhenotypeMapper.selectAllForExport();
        if (rows == null || rows.isEmpty()) {
            throw new RuntimeException("暂无小龙虾表型数据可导出");
        }

        List<String> columns = List.of(
                "sampleId", "sourceId", "a", "b", "c", "d", "e", "f",
                "CW", "h", "i", "j", "k", "L", "CL", "TL", "BL",
                "sampleTime", "createdAt", "updatedAt"
        );
        List<String> headers = List.of(
                "sample_id", "source_id", "a", "b", "c", "d", "e", "f",
                "CW", "h", "i", "j", "k", "L", "CL", "TL", "BL",
                "sample_time", "created_at", "updated_at"
        );

        ExportUtils.exportMapToExcel(
                rows,
                columns,
                headers,
                "Procambarus_clarkii_phenotype",
                "Procambarus clarkii",
                response
        );
    }
}
