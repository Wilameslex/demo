package com.example.demo.controller;

import com.example.demo.entity.Gene;
import com.example.demo.service.GeneService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 基因控制器（对接 GeneSearch 前端）
 */
@RestController // 标记为REST接口控制器
@RequestMapping("/gene") // 接口基础路径
public class GeneController {

    @Autowired
    private GeneService geneService;

    /**
     * 基因位置查询接口（GET请求）
     * 前端请求示例：http://localhost:3000/api/genes/location?geneId=Esi00100001
     * @param geneId 前端传递的基因ID
     * @return 响应：200（成功）/400（参数错误）/404（基因不存在）
     */
    @GetMapping("/location")
    public ResponseEntity<?> getGeneLocation(@RequestParam("geneId") String geneId) {
        // 1. 参数验证
        if (geneId == null || geneId.trim().isEmpty()) {
            return ResponseEntity
                    .badRequest()
                    .body(new ErrorResponse("Missing geneId parameter"));
        }

        // 2. 调用 Service 查询
        Gene gene = geneService.getGeneLocation(geneId);
        if (gene == null) {
            // 3. 基因不存在：返回404
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("Gene ID not found"));
        }

        // 4. 成功：返回基因位置（仅包含前端需要的 chr/start/end）
        return ResponseEntity.ok()
                .body(new GeneLocationResponse(gene.getChr(), gene.getStart(), gene.getEnd()));
    }

    // 内部类：基因位置响应（仅返回前端需要的字段，避免冗余）
    static class GeneLocationResponse {
        private String chr;
        private Long start;
        private Long end;

        public GeneLocationResponse(String chr, Long start, Long end) {
            this.chr = chr;
            this.start = start;
            this.end = end;
        }

        // getter（Lombok 不支持内部类，需手动生成）
        public String getChr() { return chr; }
        public Long getStart() { return start; }
        public Long getEnd() { return end; }
    }

    // 内部类：错误响应（统一错误格式）
    static class ErrorResponse {
        private String error;

        public ErrorResponse(String error) {
            this.error = error;
        }

        public String getError() { return error; }
    }
}
