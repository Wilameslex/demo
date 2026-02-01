package com.example.demo.mapper.admin;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.entity.admin.GeneInfoAdmin;
import org.apache.ibatis.annotations.Mapper;

/**
 * 基因信息后台管理Mapper（继承BaseMapper，自带CRUD方法）
 */
@Mapper
public interface GeneInfoAdminMapper extends BaseMapper<GeneInfoAdmin> {
    // 无需额外方法：BaseMapper已包含insert/selectById/updateById/deleteById/selectList等
}