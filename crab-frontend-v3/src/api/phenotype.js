import request from '@/utils/request';

/**
 * Phenotype Data模块API
 */
export const phenotypeApi = {
    /**
     * 获取群体外观数据
     */
    getPopulationAppearance: () => {
        return request({
            url: '/phenotype/population/appearance',
            method: 'get'
        });
    },

    getPopulationGrowth: () => {
        return request({
            url: '/phenotype/population/growth',
            method: 'get'
        });
    },

    // 3. 新增：获取总表型数据条数（解决报错核心）
    getTotalDataCount: () => {
        return request({
            url: '/phenotype/total/count',
            method: 'get'
        })
    },
    // 4. 新增：下载总表型数据（兜底方案，可选）
    downloadTotalData: () => {
        return request({
            url: '/phenotype/total/download',
            method: 'get',
            responseType: 'blob' // 必须指定为blob，否则无法解析文件流
        });
    },

    getCrawfishPreview: (limit = 20) => {
        return request({
            url: '/phenotype/crawfish/preview',
            method: 'get',
            params: { limit }
        });
    },

    downloadCrawfishData: () => {
        return request({
            url: '/phenotype/crawfish/download',
            method: 'get',
            responseType: 'blob'
        });
    },

    getSnailPreview: (limit = 20) => {
        return request({
            url: '/phenotype/snail/preview',
            method: 'get',
            params: { limit }
        });
    },

    downloadSnailData: () => {
        return request({
            url: '/phenotype/snail/download',
            method: 'get',
            responseType: 'blob'
        });
    }
};
