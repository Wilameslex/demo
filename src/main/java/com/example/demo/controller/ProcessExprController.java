package com.example.demo.controller;

import com.example.demo.common.Result;
import com.example.demo.dto.ProcessExprQueryDTO;
import com.example.demo.entity.ProcessExprCount;
import com.example.demo.entity.ProcessExprBoxplot;
import com.example.demo.entity.ProcessSpecGene;
import com.example.demo.service.ProcessExprService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/expression/process")
@RequiredArgsConstructor
public class ProcessExprController {
    private final ProcessExprService processExprService;

    @PostMapping("/data")
    public ResponseEntity<Result<Map<String, Object>>> getProcessExprData(@Valid @RequestBody ProcessExprQueryDTO query) {
        try {
            List<ProcessExprCount> exprCount = processExprService.getExprCount(query);
            List<ProcessExprBoxplot> exprBoxplot = processExprService.getExprBoxplot(query);
            List<ProcessSpecGene> specGene = processExprService.getSpecGene(query);

            Map<String, Object> data = new HashMap<>();
            data.put("exprCount", exprCount);
            data.put("exprBoxplot", exprBoxplot);
            data.put("specGene", specGene);

            return ResponseEntity.ok(Result.success(data));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Result.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Result.error("数据查询失败：" + e.getMessage()));
        }
    }
}