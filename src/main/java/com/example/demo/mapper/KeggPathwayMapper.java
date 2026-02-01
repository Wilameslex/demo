package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.demo.dto.PathwayQueryDTO;
import com.example.demo.entity.KeggPathway;
import com.example.demo.mapper.provider.KeggPathwaySqlProvider;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.SelectProvider;

import java.util.List;

@Mapper
public interface KeggPathwayMapper extends BaseMapper<KeggPathway> {
    @SelectProvider(type = KeggPathwaySqlProvider.class, method = "searchPathway")
    IPage<KeggPathway> searchPathway(IPage<KeggPathway> page, @Param("query") PathwayQueryDTO query);

    @SelectProvider(type = KeggPathwaySqlProvider.class, method = "searchPathway")
    List<KeggPathway> searchAll(@Param("query") PathwayQueryDTO query);
}