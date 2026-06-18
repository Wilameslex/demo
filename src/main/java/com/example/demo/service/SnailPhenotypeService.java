package com.example.demo.service;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

public interface SnailPhenotypeService {
    List<Map<String, Object>> getPreviewData(Integer limit);

    void exportAllDataToExcel(HttpServletResponse response);
}
