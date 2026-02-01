// src/api/enrichment.js 修改后
import request from '@/utils/request';

// 1. 单独导出的函数（推荐）
export function runEnrichmentAnalysis(data) {
    return request({
        url: '/enrichment/analyze',
        method: 'post',
        data,
        headers: { 'Content-Type': 'application/json' },
        timeout: 60000
    });
}

export function getEnrichmentResults(params) {
    return request({
        url: '/enrichment/results',
        method: 'get',
        params
    });
}

// 2. 默认导出的函数（同步修改）
export default {
    runEnrichmentAnalysis(data) {
        return request({
            url: '/enrichment/analyze',
            method: 'post',
            data,
            headers: { 'Content-Type': 'application/json' },
            timeout: 60000
        });
    },
    getEnrichmentResults(params) {
        return request({
            url: '/enrichment/results',
            method: 'get',
            params
        });
    },

    exportResults: (params) => request({
        url: '/enrichment/export',
        method: 'get',
        params,
        responseType: 'blob'
    })

};