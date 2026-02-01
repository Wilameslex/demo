package com.example.demo.service;

import com.example.demo.entity.MitochondrionGenome;
import java.util.List;
import java.util.Map;

public interface MitochondrionGenomeService {
    /**
     * 获取支持的物种列表（供前端下拉框）
     * @return 物种列表：[{label:"中华绒螯蟹", value:"sinensis"}, ...]
     */
    List<Map<String, String>> getSpeciesList();

    /**
     * 根据物种标识，获取线粒体注释信息
     * @param species 物种标识（sinensis/japonica/hepuensis）
     * @return 该物种的线粒体注释列表
     */
    List<MitochondrionGenome> getMitoInfoBySpecies(String species);
}