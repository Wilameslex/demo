import {exportAllGenes, searchGenes} from '@/api/gene' // 明确使用命名导入

const state = {
    searchParams: {
        genes: [],
        names:[],
        proteins: [],
        products: [],
        page: 1,
        size: 10
    },
    searchResults: [],
    totalItems: 0
}

const mutations = {
    SET_SEARCH_PARAMS(state, params) {
        state.searchParams = { ...state.searchParams, ...params }
    },
    // 修改SET_SEARCH_RESULTS mutation
    SET_SEARCH_RESULTS(state, payload) {
        // 标准化字段命名
        state.searchResults = (payload.records || []).map(item => ({
            Gene: item.gene || item.Gene,
            Name: item.name || item.Name,
            Chromosome: item.chromosome || item.Chromosome,
            Start: item.start || item.Start,
            End: item.end || item.End,
            Protein: item.protein || item.Protein,
            Product: item.product || item.Product,
            Description: item.description || item.Description,
        }))
        state.totalItems = payload.total || 0
    }
}

const actions = {
    async searchGenes({ commit }, params) {
        try {
            const response = await searchGenes(params)

            // 兼容两种响应结构：
            // 1. { data: { records, total } } （axios默认包装）
            // 2. { records, total } （自定义响应）
            const resultData = response.data || response

            commit('SET_SEARCH_RESULTS', {
                records: resultData.records,
                total: resultData.total
            })
            return true
        } catch (error) {
            console.error('API请求失败:', error)
            return false
        }
    },
    async exportAllGenes({ commit, state }, params) {
        try {
            const response = await exportAllGenes(params);
            return response;
        } catch (error) {
            console.error('导出全部数据失败:', error);
            throw new Error('导出失败，请稍后再试');
        }
    }
}

export default {
    namespaced: true,
    state,
    mutations,
    actions
}
