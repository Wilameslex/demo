import { searchPathway } from '@/api/pathway'

const state = {
  searchParams: {
    keyword: '',
    page: 1,
    size: 10,
  },
  searchResults: [],
  totalItems: 0,
}

const mutations = {
  SET_SEARCH_PARAMS(state, params) {
    state.searchParams = { ...state.searchParams, ...params }
  },
  SET_SEARCH_RESULTS(state, payload) {
    state.searchResults = payload.records || []
    state.totalItems = payload.total || 0
  },
}

const actions = {
  async searchPathway({ commit }, params) {
    try {
      const response = await searchPathway(params)
      const resultData = response.data || response
      commit('SET_SEARCH_RESULTS', {
        records: resultData.records,
        total: resultData.total,
      })
      return true
    } catch (error) {
      console.error('KEGG通路搜索失败:', error)
      return false
    }
  },
}

export default {
  namespaced: true,
  state,
  mutations,
  actions,
}
