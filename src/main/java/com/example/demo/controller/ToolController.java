package com.example.demo.controller;

import com.example.demo.entity.SequenceRequest;
import com.example.demo.entity.SequenceResponse;
import com.example.demo.service.SequenceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 工具控制器（对接 SequenceFetch 前端）
 */
@RestController
@RequestMapping("/tools") // 接口基础路径
public class ToolController {

    @Autowired
    private SequenceService sequenceService;

    /**
     * 序列获取接口（POST请求）
     * 前端请求示例：POST http://localhost:3000/api/tools/fetch-sequence
     * @param request 前端传递的序列请求参数（JSON格式）
     * @return 响应：200（成功，返回序列列表）/400（参数错误）
     */
    @PostMapping("/fetch-sequence")
    public ResponseEntity<?> fetchSequence(@RequestBody SequenceRequest request) {
        try {
            // 1. 调用 Service 获取序列
            List<SequenceResponse> sequences = sequenceService.getSequence(request);
            // 2. 成功：返回序列列表（前端直接用于 formattedFasta）
            return ResponseEntity.ok(sequences);
        } catch (IllegalArgumentException e) {
            // 3. 参数错误：返回400 + 错误信息
            return ResponseEntity
                    .badRequest()
                    .body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            // 4. 其他异常：返回500 + 通用错误
            return ResponseEntity
                    .internalServerError()
                    .body(new ErrorResponse("Failed to fetch sequences: " + e.getMessage()));
        }
    }

    // 内部类：错误响应（与 GeneController 格式统一）
    static class ErrorResponse {
        private String error;

        public ErrorResponse(String error) {
            this.error = error;
        }

        public String getError() { return error; }
    }
}
