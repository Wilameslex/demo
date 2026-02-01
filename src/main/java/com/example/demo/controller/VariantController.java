package com.example.demo.controller;

import com.example.demo.common.Result;
import com.example.demo.entity.VariantFreq;
import com.example.demo.service.VariantService;
import com.example.demo.util.CsvUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@RestController
@RequestMapping("/variant")
public class VariantController {

    @Autowired
    private VariantService variantService;

    // 按基因查询接口：路径为 /api/variant/by-gene
    @GetMapping("/by-gene")
    public Result<Map<String, Object>> getByGene(
            @RequestParam String geneId,
            @RequestParam Integer upstream,
            @RequestParam Integer downstream,
            @RequestParam Integer size) {
        Map<String, Object> result = variantService.getVariantsByGene(geneId, upstream, downstream, size);
        return Result.success(result);
    }

    /**
     * 统一查询接口：根据染色体区间+变异类型获取结果
     * @param chr 染色体
     * @param start 起始位置
     * @param end 终止位置
     * @param variantType 变异类型（snp/indel/sv/all，all表示返回三种）
     * @param size 单类型返回条数（默认10）
     */

    @GetMapping("/by-chr-pos")
    public Result<Map<String, Object>> getByChrPos(
            @RequestParam String chr,
            @RequestParam Long start,
            @RequestParam Long end,
            @RequestParam(defaultValue = "all") String variantType,
            @RequestParam(defaultValue = "10") Integer size) {

        // 直接调用Service的统筹方法，一次性获取SNP/INDEL/SV+FST数据
        Map<String, Object> result = variantService.getVariantsByChrPos(chr, start, end, size);

        // 1. 如果查询所有类型，依次获取SNP/INDEL/SV
        if ("all".equals(variantType)) {
            // SNP
            List<VariantFreq> snps = variantService.getVariantsByType("snp", chr, start, end, size);
            int snpTotal = variantService.countVariantsByType("snp", chr, start, end);
            // INDEL
            List<VariantFreq> indels = variantService.getVariantsByType("indel", chr, start, end, size);
            int indelTotal = variantService.countVariantsByType("indel", chr, start, end);
            // SV
            List<VariantFreq> svs = variantService.getVariantsByType("sv", chr, start, end, size);
            int svTotal = variantService.countVariantsByType("sv", chr, start, end);

            result.put("snps", snps);
            result.put("snpTotal", snpTotal);
            result.put("indels", indels);
            result.put("indelTotal", indelTotal);
            result.put("svs", svs);
            result.put("svTotal", svTotal);
        } else {
            // 2. 仅查询单一类型
            List<VariantFreq> variants = variantService.getVariantsByType(variantType, chr, start, end, size);
            int total = variantService.countVariantsByType(variantType, chr, start, end);
            result.put(variantType + "s", variants); // 如"snps"
            result.put(variantType + "Total", total); // 如"snpTotal"
        }

        // 3. 补充Fst/Pi数据（复用之前的逻辑）
//        result.put("fstMap", variantService.getFstByChrPos(chr, start, end));
//        result.put("piMap", variantService.getPiByChrPos(chr, start, end));
        // 4. 补充区间信息
        result.put("region", Map.of("chr", chr, "start", start, "end", end));

        return Result.success(result);
    }

    /**
     * 统一下载接口：根据类型下载所有结果
     */
    @GetMapping("/download")
    public ResponseEntity<byte[]> download(
            @NotBlank(message = "chr参数不能为空") @RequestParam String chr, // 非空校验
            @NotNull(message = "start参数不能为空") @RequestParam Long start,
            @NotNull(message = "end参数不能为空") @RequestParam Long end,
            @NotBlank(message = "variantType参数不能为空") @RequestParam String variantType) {

        try {
            List<Map<String, Object>> data = variantService.downloadVariants(variantType, chr, start, end);
            if (data.isEmpty()) {
                return ResponseEntity.noContent().build(); // 无数据时返回204
            }
            String csvStr = CsvUtils.convertToCsv(data);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            // 文件名编码（避免中文乱码）
            String filename = URLEncoder.encode(variantType + "_results.csv", StandardCharsets.UTF_8.name());
            headers.setContentDispositionFormData("attachment", filename);
            return new ResponseEntity<>(csvStr.getBytes(StandardCharsets.UTF_8), headers, HttpStatus.OK);
        } catch (Exception e) {
            // 捕获异常并返回500错误信息
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("下载失败：" + e.getMessage()).getBytes());
        }
    }
}
