package com.example.demo.service.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.demo.dto.admin.GeneInfoAdminDTO;
import com.example.demo.dto.admin.GeneInfoAdminQueryDTO;
import com.example.demo.entity.admin.GeneInfoAdmin;

/**
 * 基因信息后台管理Service接口
 */
public interface GeneInfoAdminService extends IService<GeneInfoAdmin> {
    /**
     * 分页查询基因列表（支持模糊搜索、状态筛选）
     */
    Page<GeneInfoAdmin> getGeneInfoPage(GeneInfoAdminQueryDTO queryDTO);

    /**
     * 新增基因信息
     */
    void addGeneInfo(GeneInfoAdminDTO dto);

    /**
     * 编辑基因信息（根据ID更新）
     */
    void updateGeneInfo(GeneInfoAdminDTO dto);

    /**
     * 批量删除基因信息（逻辑删除）
     */
    void batchDeleteGeneInfo(Long[] ids);
}