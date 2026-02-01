// src/api/geneNetwork.js
import request from '@/utils/request';

// 基因网络查询接口
export function searchGeneNetwork(data) {
    return request({
        url: '/gene-network/search',
        method: 'post',
        data,
        headers: {
            'Content-Type': 'application/json'
        }
    });
}