import { runEnrichmentAnalysis, getEnrichmentResults } from '@/api/enrichment';

const state = {
    analysisParams: null,
    results: null,
    loading: false,
    error: null,
    analysisType: 'GO'
};

const mutations = {
    SET_ANALYSIS_PARAMS(state, params) {
        state.analysisParams = params;
    },
    SET_TASK_ID(state, taskId) {
        state.taskId = taskId;
    },
    setResults(state, results) {
        state.results = results;
    },
    SET_LOADING(state, loading) {
        state.loading = loading;
    },
    setError(state, error) {
        state.error = error;
    },
    SET_ANALYSIS_TYPE(state, type) {
        state.analysisType = type;
    },
    RESET(state) {
        state.analysisParams = null;
        state.results = null;
        state.loading = false;
        state.error = null;
        state.analysisType = 'GO';
    }
};

const actions = {
    async runEnrichmentAnalysis({ commit }, params) {
        commit('RESET');
        commit('SET_LOADING', true);
        commit('SET_ANALYSIS_TYPE', params.analysisType);

        try {
            const response = await runEnrichmentAnalysis(params);

            // 更健壮的响应检查
            if (!response || !response.task_id) {
                let errorMsg = '无效的响应格式';
                if (response && response.message) {
                    errorMsg = response.message;
                } else if (response && response.error) {
                    errorMsg = response.error;
                }
                throw new Error(errorMsg);
            }

            commit('SET_ANALYSIS_PARAMS', {
                ...params,
                taskId: response.task_id,
            });

            return response.task_id;
        } catch (error) {
            // 更详细的错误日志
            console.error('分析请求失败:', error);

            let errorMsg = '分析请求失败';
            if (error.response) {
                errorMsg = `服务器错误: ${error.response.status}`;
                if (error.response.data) {
                    errorMsg += ` - ${JSON.stringify(error.response.data)}`;
                }
            } else if (error.message) {
                errorMsg = error.message;
            }

            commit('SET_ERROR', errorMsg);
            return null;
        } finally {
            commit('SET_LOADING', false);
        }
    },

    async fetchResults({ commit, state }) {
        // 1. 检查analysisParams是否存在（避免空指针）
        if (!state.analysisParams || !state.analysisParams.taskId) {
            commit('SET_ERROR', '分析任务ID丢失，请重新提交分析');
            return;
        }

        commit('SET_LOADING', true);
        try {
            const timeout = new Promise((_, reject) =>
                setTimeout(() => reject(new Error('请求超时')), 30000)
            );

            // 🔴 修复：使用state.analysisParams.taskId，而非state.taskId
            const response = await Promise.race([
                getEnrichmentResults({ taskId: state.analysisParams.taskId }),
                timeout
            ]);
            console.log("获取结果响应:", response);

            // 🔴 修复：正确处理响应结构（避免response.data不存在的问题）
            const resultData = response || {};
            const processedResults = {
                ...resultData,
                chartImage: resultData.chart_image || resultData.chartImage || ''
            };

            commit('setResults', processedResults); // 注意：mutation名是setResults（小写s），之前是SET_RESULTS（大写），需匹配
        } catch (error) {
            let errorMsg = '获取结果失败';
            if (error.response) {
                errorMsg += `: ${error.response.status} ${error.response.data?.message || ''}`;
            } else if (error.message) {
                errorMsg += `: ${error.message}`;
            }
            commit('setError', errorMsg);
        } finally {
            commit('SET_LOADING', false);
        }
    }
};

export default {
    namespaced: true,
    state,
    mutations,
    actions
};