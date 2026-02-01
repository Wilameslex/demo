import { createApp } from 'vue';
import App from './App.vue'
import router from './router'
import axios from 'axios'
import store from './store'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import ErrorHandler from '@/utils/errorHandler';
import api from '@/api/enrichment';
// 创建 Vue 实例
const app = createApp(App);


// 禁用Vue Devtools警告
app.config.devtools = false;

// 关键：拦截ResizeObserver错误，隐藏webpack overlay
const ignoreErrorMessages = [
    "ResizeObserver loop completed with undelivered notifications",
    "ResizeObserver loop limit exceeded"
];

// 拦截全局错误，隐藏webpack overlay
window.addEventListener('error', (e) => {
    const errorMsg = e.message || '';
    const isIgnoreError = ignoreErrorMessages.some(msg => errorMsg.startsWith(msg));
    if (isIgnoreError) {
        e.stopImmediatePropagation();
        e.preventDefault();
        // 强制隐藏所有webpack overlay相关元素
        document.querySelectorAll('[id^="webpack-dev-server-client-overlay"]').forEach(el => {
            el.style.display = 'none';
            el.style.zIndex = '-1';
        });
        console.warn('[忽略良性错误]', errorMsg);
        return false;
    }
});
// 拦截未处理的Promise拒绝
window.addEventListener('unhandledrejection', (event) => {
    const errorMsg = event.reason?.message || '';
    if (ignoreErrorMessages.some(msg => errorMsg.startsWith(msg))) {
        event.preventDefault();
        console.warn('[忽略Promise错误]', errorMsg);
    }
});

//挂载全局axios
app.config.globalProperties.$axios = axios;
// 注册图标组件
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
    app.component(key, component)
}

// 使用插件
app.use(store)
app.use(router)
app.use(ElementPlus, {
    size: 'small',
    zIndex: 3000
})

// 全局注册API
app.config.globalProperties.$api = {
    enrichment: api
};

// 注册错误处理器
app.use(ErrorHandler);

// 挂载应用
app.mount('#app');





