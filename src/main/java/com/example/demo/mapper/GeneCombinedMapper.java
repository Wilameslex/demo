package com.example.demo.mapper;


import com.example.demo.entity.GeneCombinedInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface GeneCombinedMapper {
    // 批量查询基因基础信息
    List<GeneCombinedInfo> queryGeneInfoBatch(@Param("geneIds") List<String> geneIds);
}
