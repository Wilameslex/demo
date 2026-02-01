package com.example.demo.service;

import com.example.demo.entity.SequenceRequest;
import com.example.demo.entity.SequenceResponse;
import java.util.List;

/**
 * 序列服务接口
 */
public interface SequenceService {
    /**
     * 根据请求参数获取序列（支持基因ID或染色体区域）
     * @param request 序列请求参数
     * @return 序列列表（空列表表示无结果）
     * @throws IllegalArgumentException 参数格式错误时抛出
     */
    List<SequenceResponse> getSequence(SequenceRequest request) throws IllegalArgumentException;
}
