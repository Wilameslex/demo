package com.example.demo.controller;

import com.example.demo.common.Result;
import com.example.demo.dto.ExpressionQueryDTO;
import com.example.demo.service.ExpressionSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/expression")
@RequiredArgsConstructor
public class ExpressionSearchController {
    private final ExpressionSearchService expressionSearchService;

    @PostMapping("/search")
    public ResponseEntity<Result<Map<String, Object>>> searchExpression(
            @Valid @RequestBody ExpressionQueryDTO query) {
        try {
            Map<String, Object> results = expressionSearchService.searchExpression(query);
            return ResponseEntity.ok(Result.success(results));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Result.error(e.getMessage()));
        }
    }

    @GetMapping("/samples")
    public ResponseEntity<Result<List<String>>> getSamples(
            @RequestParam String pipeline,
            @RequestParam String transcriptome,
            @RequestParam String searchType) {
        try {
            List<String> samples = expressionSearchService.getSampleColumns(
                    pipeline, transcriptome, searchType);
            return ResponseEntity.ok(Result.success(samples));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Result.error(e.getMessage()));
        }
    }

}
