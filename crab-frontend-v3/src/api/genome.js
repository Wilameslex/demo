import request from '@/utils/request';

/**
 * 线粒体基因组相关API
 */
export const mitochondrionApi = {
    /**
     * 获取支持的物种列表（下拉框用）
     */
    getSpeciesList: () => {
        return request({
            url: '/genome/mitochondrion/species-list',
            method: 'get'
        });
    },

    /**
     * 根据物种标识获取线粒体注释信息
     * @param {string} species 物种标识（sinensis/japonica/hepuensis）
     */
    getMitoInfo: (species) => {
        return request({
            url: '/genome/mitochondrion/info',
            method: 'get',
            params: { species }  // 传递物种参数
        });
    },

    /**
     * 生成PDF结构图的访问URL
     * @param {string} species 物种标识
     * @returns {string} PDF完整URL
     */
    getSvgUrl: (species) => {
        try {
            // 路径格式：@/assets/svg/文件名（与你的实际目录一致）
            const svgPath = require(`@/assets/svg/E.${species}.svg`);
            return svgPath; // Webpack会返回打包后的正确路径
        } catch (error) {
            console.error(`加载SVG失败：未找到src/assets/svg/E.${species}.svg`, error);
            return '';
        }
    }
};