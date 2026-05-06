import { searchExpression, getSamples } from '@/api/expression';

const state = {
    searchParams: {},
    searchResults: null,
    samples: [],
    targetIds: []
};

const mutations = {
    SET_SEARCH_PARAMS(state, params) {
        state.searchParams = { ...(state.searchParams || {}), ...(params || {}) };
    },
    SET_SEARCH_RESULTS(state, payload) {
        state.searchResults = payload;
    },
    SET_SAMPLES(state, samples) {
        state.samples = samples;
    },
    SET_TARGET_IDS(state, targetIds) {
        state.targetIds = targetIds;
    }
};

const actions = {
    async searchExpression({ commit }, requestParams) { // 使用正确的参数名
        try {
            const response = await searchExpression(requestParams); // 传递参数
            const resultData = response.data || {}; // Result 对象
            const businessData = resultData.data || {}; // 业务数据
            commit('SET_SEARCH_RESULTS', response.data);
            commit('SET_SEARCH_PARAMS', requestParams);
            return true;
        } catch (error) {
            console.error('表达量搜索失败:', error);
            return false;
        }
    },

    async fetchSamples({ commit }, { pipeline, transcriptome, searchType }) {
        try {
            const response = await getSamples(pipeline, transcriptome, searchType);
            commit('SET_SAMPLES', response.data);
            return true;
        } catch (error) {
            console.error('获取样本列表失败:', error);
            return false;
        }
    }
};

export default {
    namespaced: true,
    state,
    mutations,
    actions
};
