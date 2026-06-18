package com.example.demo.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface CrawfishPhenotypeMapper {
    List<Map<String, Object>> selectPreview(@Param("limit") Integer limit);

    List<Map<String, Object>> selectAllForExport();
}
