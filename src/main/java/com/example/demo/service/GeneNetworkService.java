package com.example.demo.service;

import com.example.demo.dto.GeneNetworkQueryDTO;
import com.example.demo.entity.GeneNetworkData;
import java.util.Map;

public interface GeneNetworkService {
    Map<String, Object> queryGeneNetwork(GeneNetworkQueryDTO query);
}


