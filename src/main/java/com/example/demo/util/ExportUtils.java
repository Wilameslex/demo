package com.example.demo.util;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpHeaders;

import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 通用Excel导出工具类（基于POI，修复反射字段获取错误）
 */
public class ExportUtils {

    /**
     * 导出Excel（支持phenotype_total实体类，修复字段值获取逻辑）
     * @param dataList 导出数据列表（phenotype_total实体集合）
     * @param fileName 导出文件名（不含后缀）
     * @param sheetName Excel工作表名称
     * @param response 响应对象（用于输出文件流）
     * @param <T> 泛型（限定为phenotype_total实体类）
     */
    public static <T> void exportToExcel(List<T> dataList, String fileName, String sheetName, HttpServletResponse response) {
        try {
            // 1. 创建工作簿和工作表
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet(sheetName);
            sheet.setDefaultColumnWidth(15); // 默认列宽

            // 校验数据非空
            if (dataList.isEmpty()) {
                throw new RuntimeException("无数据可导出");
            }

            // 2. 定义表头（与phenotype_total实体字段顺序严格对应）
            List<String> headerNames = List.of(
                    "id", "sampleId", "group", "date", "wildSampleId", "gender", "weight",
                    "A1", "A2", "A3", "A4", "A5", "A6", "A7",
                    "B1", "B2", "B3", "B4", "C1", "C2", "C3", "C4",
                    "L1", "L2", "L3", "L4", "L5", "H",
                    "S1", "S2", "S3", "S4", "S5", "S6", "S7",
                    "F1", "F2", "F3", "F4", "hepatopancreas", "gonad",
                    "remarks", "remarks2", "createTime", "updateTime"
            );

            // 3. 创建表头行（第一行）
            Row headerRow = sheet.createRow(0);
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex()); // 表头背景色
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER); // 文字居中

            Font headerFont = workbook.createFont();
            headerFont.setBold(true); // 表头字体加粗
            headerStyle.setFont(headerFont);

            // 填充表头文字
            for (int i = 0; i < headerNames.size(); i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headerNames.get(i));
                cell.setCellStyle(headerStyle);
            }

            // 4. 反射获取实体类字段（phenotype_total的所有字段）
            Class<?> entityClass = dataList.get(0).getClass();
            Field[] fields = entityClass.getDeclaredFields(); // 获取所有声明的字段（包括private）

            // 5. 填充数据行（从第二行开始）
            for (int rowIndex = 0; rowIndex < dataList.size(); rowIndex++) {
                Row dataRow = sheet.createRow(rowIndex + 1); // 数据行从1开始（0是表头）
                T data = dataList.get(rowIndex);

                // 循环填充每个字段（与表头顺序严格对应）
                for (int colIndex = 0; colIndex < fields.length; colIndex++) {
                    Field field = fields[colIndex];
                    field.setAccessible(true); // 突破private访问限制（关键）

                    // 获取字段值（核心修复：用get()替代getString()）
                    Object fieldValue = field.get(data);

                    // 创建当前单元格
                    Cell cell = dataRow.createCell(colIndex);

                    // 根据字段类型处理值（避免空指针+格式统一）
                    if (fieldValue == null) {
                        cell.setCellValue(""); // 空值显示空字符串
                        continue;
                    }

                    // 按字段类型赋值（匹配phenotype_total实体字段类型）
                    if (field.getType() == Long.class || field.getType() == long.class) {
                        // 处理ID、数值类型（如ID、Weight、A1-A7等Double字段）
                        cell.setCellValue(((Number) fieldValue).doubleValue());
                    } else if (field.getType() == Double.class || field.getType() == double.class) {
                        cell.setCellValue(((Double) fieldValue));
                    } else if (field.getType() == String.class) {
                        // 处理字符串类型（如Sample ID、Group/Place等）
                        cell.setCellValue((String) fieldValue);
                    } else if (field.getType() == LocalDateTime.class) {
                        // 处理时间类型（Create Time、Update Time）
                        cell.setCellValue(fieldValue.toString());
                    } else {
                        // 其他类型默认转字符串
                        cell.setCellValue(fieldValue.toString());
                    }
                }
            }

            // 6. 设置响应头（解决浏览器下载乱码）
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("UTF-8");
            String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replace("+", "%20");
            response.setHeader(
                    HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename*=UTF-8''" + encodedFileName + ".xlsx"
            );

            // 7. 输出到浏览器（关闭流避免内存泄漏）
            try (OutputStream outputStream = response.getOutputStream()) {
                workbook.write(outputStream);
                outputStream.flush(); // 强制刷新，确保所有数据写入响应
                System.out.println("Excel流已写入响应");
            } finally {
                workbook.close(); // 强制关闭工作簿，释放内存
                System.out.println("Workbook已关闭");
            }

        } catch (Exception e) {
            // 抛出具体错误信息，便于排查
            throw new RuntimeException("Excel导出失败：" + e.getMessage(), e);
        }
    }
}