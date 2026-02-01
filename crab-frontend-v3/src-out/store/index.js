import { createStore } from 'vuex'
import gene from './modules/gene'
import pathway from './modules/pathway'
import expression from './modules/expression'
import enrichment from './modules/enrichment'

export default createStore({
  modules: {
    gene, // 注册gene模块
    pathway,
    expression,
    enrichment,
  },
})
