package com.example.demo.service.impl;

import com.example.demo.entity.MitochondrionGenome;
import com.example.demo.mapper.MitochondrionGenomeMapper;
import com.example.demo.service.MitochondrionGenomeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class MitochondrionGenomeServiceImpl implements MitochondrionGenomeService {

    private final MitochondrionGenomeMapper mitochondrionGenomeMapper;

    // 允许的物种：key=物种标识（对应表名前缀），value=物种显示名
    private static final Map<String, String> ALLOWED_SPECIES = new HashMap<>() {{
        put("sinensis", "中华绒螯蟹 (Eriocheir sinensis)");
        put("japonica", "日本绒螯蟹 (Eriocheir japonica)");
        put("hepuensis", "合浦绒螯蟹 (Eriocheir hepuensis)");
    }};

    @Override
    public List<Map<String, String>> getSpeciesList() {
        // 转换为前端下拉框需要的格式
        List<Map<String, String>> speciesList = new ArrayList<>();
        for (Map.Entry<String, String> entry : ALLOWED_SPECIES.entrySet()) {
            Map<String, String> item = new HashMap<>();
            item.put("label", entry.getValue());  // 显示名（如中华绒螯蟹）
            item.put("value", entry.getKey());    // 标识（如sinensis）
            speciesList.add(item);
        }
        return speciesList;
    }

    @Override
    public List<MitochondrionGenome> getMitoInfoBySpecies(String species) {
        // 1. 校验物种合法性（和之前一致）
        if (!ALLOWED_SPECIES.containsKey(species)) {
            throw new IllegalArgumentException("不支持的物种：" + species);
        }
        // 2. 拼接分表名（对应你的表名：mitogenome_sinensis/japonica/hepuensis）
        String tableName = "mitogenome_" + species;
        // 3. 调用Mapper查询对应分表
        return mitochondrionGenomeMapper.selectByTableName(tableName);
    }
}