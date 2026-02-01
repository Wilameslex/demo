import request from '@/utils/request';

/**
 * Genome Overview模块API（仅中华绒螯蟹）
 */
export const genomeOverviewApi = {
    /**
     * 获取中华绒螯蟹参考基因组详情（固定ASM2467909v1）
     */
    getAssemblyDetail: () => {
        return request({
            url: '/genome/overview/assembly-detail',
            method: 'get'
        });
    },

    /**
     * 获取中华绒螯蟹所有染色体信息
     */
    getChromosomes: () => {
        return request({
            url: '/genome/overview/chromosomes',
            method: 'get'
        });
    },

    /**
     * 格式化大数字（如1765827443 → 1,765,827,443）
     * @param {number} num 待格式化的数字
     */
    formatBigNumber: (num) => {
        if (!num) return '0';
        return num.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ',');
    }
};