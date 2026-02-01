package com.example.demo.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.common.Result;
import com.example.demo.dto.admin.GeneInfoAdminDTO;
import com.example.demo.dto.admin.GeneInfoAdminQueryDTO;
import com.example.demo.entity.admin.GeneInfoAdmin;
import com.example.demo.service.admin.GeneInfoAdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 基因信息后台管理Controller（仅管理员可访问）
 * 接口路径：/admin/geneInfo，与前台接口区分
 */
@RestController
@RequestMapping("/admin/geneInfo")
@RequiredArgsConstructor
@Slf4j
@Validated // 开启参数校验
public class GeneInfoAdminController {

    private final GeneInfoAdminService geneInfoAdminService;

    /**
     * 1. 分页查询基因列表（支持筛选）
     */
    @GetMapping("/list")
    public Result<Page<GeneInfoAdmin>> getGeneInfoList(@Validated GeneInfoAdminQueryDTO queryDTO) {
        log.info("后台分页查询基因列表：{}", queryDTO);
        Page<GeneInfoAdmin> page = geneInfoAdminService.getGeneInfoPage(queryDTO);
        return Result.success(page);
    }

    /**
     * 2. 新增基因信息
     */
    @PostMapping("/add")
    public Result<?> addGeneInfo(@Validated @RequestBody GeneInfoAdminDTO dto) {
        log.info("后台新增基因信息：{}", dto);
        geneInfoAdminService.addGeneInfo(dto);
        return Result.success("新增基因信息成功");
    }

    /**
     * 3. 编辑基因信息
     */
    @PutMapping("/update")
    public Result<?> updateGeneInfo(@Valid @RequestBody GeneInfoAdminDTO dto) {
        // 原错误代码：geneInfoAdminService.updateGeneInfo(dto.getId(), dto);
        // 改为直接传递dto（服务层已改用gene+protein查询）
        geneInfoAdminService.updateGeneInfo(dto);
        return Result.success("编辑基因信息成功");
    }

    /**
     * 4. 批量删除基因信息
     */
    @DeleteMapping("/batchDelete")
    public Result<?> batchDeleteGeneInfo(@RequestBody Long[] ids) {
        geneInfoAdminService.batchDeleteGeneInfo(ids);
        return Result.success("批量删除基因信息成功");
    }

    /**
     * 5. 根据ID查询单个基因信息（用于编辑弹窗回显）
     */
    // 若有根据ID查询的接口（getById），需改为根据gene+protein查询
    @GetMapping("/getById")
    public Result<GeneInfoAdmin> getById(@RequestParam String gene, @RequestParam String protein) {
        LambdaQueryWrapper<GeneInfoAdmin> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GeneInfoAdmin::getGene, gene)
                .eq(GeneInfoAdmin::getProtein, protein)
                .eq(GeneInfoAdmin::getDeleted, 0);
        GeneInfoAdmin geneInfo = geneInfoAdminService.getOne(wrapper);

        // 关键修改：查询不到数据时返回错误，而非空data
        if (geneInfo == null) {
            return Result.error("未查询到基因信息（基因ID：" + gene + "，蛋白ID：" + protein + "）");
        }

        return Result.success(geneInfo);
    }
}