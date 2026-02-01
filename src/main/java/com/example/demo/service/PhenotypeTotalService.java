package com.example.demo.service;

import com.example.demo.entity.PhenotypeTotal;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

public interface PhenotypeTotalService {
    /**
     * 获取所有总表型数据（用于下载）
     */
    List<PhenotypeTotal> getAllTotalData();

    /**
     * 导出总表型数据为Excel（响应文件流）
     */
    void exportTotalDataToExcel(HttpServletResponse response);
    Long getTotalDataCount();
}