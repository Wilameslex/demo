package com.example.demo.service.impl.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.dto.admin.GeneInfoAdminDTO;
import com.example.demo.dto.admin.GeneInfoAdminQueryDTO;
import com.example.demo.entity.admin.GeneInfoAdmin;
import com.example.demo.mapper.admin.GeneInfoAdminMapper;
import com.example.demo.service.admin.GeneInfoAdminService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Arrays;

/**
 * 基因信息后台管理Service实现类
 */
@Service
public class GeneInfoAdminServiceImpl extends ServiceImpl<GeneInfoAdminMapper, GeneInfoAdmin> implements GeneInfoAdminService {

    /**
     * 分页查询（支持基因ID/名称模糊搜索、状态筛选）
     */
    @Override
    public Page<GeneInfoAdmin> getGeneInfoPage(GeneInfoAdminQueryDTO queryDTO) {
        // 1. 构建分页参数
        Page<GeneInfoAdmin> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());

        // 2. 构建查询条件（LambdaQueryWrapper避免硬编码字段名）
        LambdaQueryWrapper<GeneInfoAdmin> wrapper = new LambdaQueryWrapper<>();
        // 模糊搜索：基因ID或基因名称包含关键词
        if (StringUtils.hasText(queryDTO.getKeyword())) {
            wrapper.like(GeneInfoAdmin::getGene, queryDTO.getKeyword())
                    .or()
                    .like(GeneInfoAdmin::getName, queryDTO.getKeyword());
        }
        // 状态筛选：1启用/0禁用
        if (queryDTO.getStatus() != null) {
            wrapper.eq(GeneInfoAdmin::getStatus, queryDTO.getStatus());
        }
        // 逻辑删除：只查询未删除的数据（deleted=0）
        wrapper.eq(GeneInfoAdmin::getDeleted, 0);
        // 排序：按更新时间倒序（最新修改的在前）
        wrapper.orderByDesc(GeneInfoAdmin::getUpdateTime);

        // 3. 执行分页查询
        return baseMapper.selectPage(page, wrapper);
    }

    /**
     * 新增基因信息
     */
    @Override
    public void addGeneInfo(GeneInfoAdminDTO dto) {
        // 1. DTO转实体
        GeneInfoAdmin geneInfoAdmin = new GeneInfoAdmin();
        BeanUtils.copyProperties(dto, geneInfoAdmin);

        // 2. 补充默认值（逻辑删除默认0，时间由自动填充处理）
        geneInfoAdmin.setDeleted(0);

        // 3. 插入数据库
        baseMapper.insert(geneInfoAdmin);
    }

    /**
     * 编辑基因信息
     */
    @Override
    public void updateGeneInfo(GeneInfoAdminDTO dto) {
        // 1. 构建查询条件：通过gene和protein（联合主键）查询原有数据
        LambdaQueryWrapper<GeneInfoAdmin> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GeneInfoAdmin::getGene, dto.getGene()) // 联合主键1
                .eq(GeneInfoAdmin::getProtein, dto.getProtein()) // 联合主键2
                .eq(GeneInfoAdmin::getDeleted, 0);

        GeneInfoAdmin existing = baseMapper.selectOne(wrapper);

        if (existing == null) {
            throw new RuntimeException("基因信息不存在或已删除");
        }

        // 2. 复制字段并更新
        BeanUtils.copyProperties(dto, existing);
        baseMapper.update(existing, wrapper); // 用相同条件更新
    }

    /**
     * 批量删除（逻辑删除，deleted=1）
     */
    @Override
    public void batchDeleteGeneInfo(Long[] ids) {
        // 抑制varargs警告
        @SuppressWarnings("unchecked")
        int rows = baseMapper.deleteBatchIds(Arrays.asList(ids));
        if (rows == 0) {
            throw new RuntimeException("删除失败：数据不存在或已删除");
        }
    }
}