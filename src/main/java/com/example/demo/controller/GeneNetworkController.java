package com.example.demo.controller;

import com.example.demo.common.Result;
import com.example.demo.dto.GeneNetworkQueryDTO;
import com.example.demo.service.GeneNetworkService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/gene-network")
@RequiredArgsConstructor
public class GeneNetworkController {

    private final GeneNetworkService geneNetworkService;

    // 基因网络查询接口（POST请求，适配现有前端请求方式）
    @PostMapping("/search")
    public ResponseEntity<Result<Map<String, Object>>> searchGeneNetwork(
            @Valid @RequestBody GeneNetworkQueryDTO query) {
        try {
            Map<String, Object> networkData = geneNetworkService.queryGeneNetwork(query);
            return ResponseEntity.ok(Result.success(networkData));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Result.error("查询失败：" + e.getMessage()));
        }
    }
}
