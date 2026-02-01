import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import store from './store'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import ErrorHandler from '@/utils/errorHandler'
import api from '@/api/enrichment'

// 全局配置
Vue.use(ElementPlus, {
  size: 'small',
  zIndex: 3000,
})

new Vue({
  router,
  store, // 注入统一管理的store
  render: (h) => h(App),
}).$mount('#app')

// 创建 Vue 实例
const app = createApp(App)
// 注册图标组件
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}
app.use(store)
app.use(router)
app.use(ElementPlus)
//挂载应用
app.mount('#app')

// 全局注册API
app.config.globalProperties.$api = {
  enrichment: api,
}

// 注册错误处理器
app.use(ErrorHandler)
