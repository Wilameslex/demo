import axios from 'axios'
import { ElMessage } from 'element-plus'
import { v4 as uuidv4 } from 'uuid'

 const service = axios.create({
     baseURL: process.env.VUE_APP_BASE_API,
     timeout: 30000,
     headers: {
         'Content-Type': 'application/json'
     }
 })

// 请求拦截器：新增【管理员Token添加】逻辑
service.interceptors.request.use(
    config => {
        config.headers['X-Request-ID'] = uuidv4();

        // 修正：匹配所有/admin开头的请求，添加Token（无需/api前缀，因为config.url不含/api）
        if (config.url && config.url.includes('/admin/')) {
            const adminToken = localStorage.getItem('adminToken');
            if (adminToken && adminToken.trim()) {
                config.headers['Admin-Token'] = `Bearer ${adminToken}`;
                console.log('🔑 已添加Token:', config.headers['Admin-Token']); // 验证是否添加成功
            } else {
                console.warn('⚠️ adminToken不存在或为空，可能导致403');
            }
        }

        if (process.env.NODE_ENV === 'development') {
            console.log('📡 请求发出 =>', config.method?.toUpperCase(), config.url);
            console.log('📦 请求参数:', config.data);
            console.log('🔑 请求头:', config.headers['Admin-Token']); // 新增日志，确认Token是否存在
        }

        return config;
    },
    error => {
        console.error('🚨 请求错误:', error);
        return Promise.reject(error);
    }
)
// 响应拦截器：新增【Token失效处理】逻辑
service.interceptors.response.use(
    response => {
        if (response.config.responseType === 'blob') {
            return response;
        }

        const res = response.data

        // 1. 保留原有：开发环境日志（不删除）
        if (process.env.NODE_ENV === 'development') {
            console.log('✅ 响应接收 =>', response.config.url)
            console.log('📊 响应数据:', res)
        }

        if (['number', 'string', 'boolean'].includes(typeof res)) {
            return res;
        }
        // 🔴 核心兼容逻辑：接口白名单（仅富集分析接口返回完整响应）
        const enrichmentUrls = [
            '/enrichment/analyze',  // 富集分析提交接口
            '/enrichment/results'  // 富集分析结果接口
        ];
        // 判断当前接口是否在白名单中
        const isEnrichmentApi = enrichmentUrls.some(url =>
            response.config.url?.includes(url)
        );


        // 2. 保留原有：业务错误提示（不删除，若后端code≠200时提示）
        if (isEnrichmentApi) {
            // 富集分析接口：返回完整响应（含 code、task_id、status 等）
            return res;
        } else {
            // 其他接口：仍返回 res.data（保持原有逻辑，不影响其他模块）
            return res.data;
        }
    },
    error => {
        // 3. 新增：处理401 Token失效（管理员登录过期）
        if (error.response?.status === 401) {
            // 清除无效Token，避免死循环
            localStorage.removeItem('adminToken')
            localStorage.removeItem('adminUsername')
            // 跳转到管理员登录页
            window.location.href = '/admin/login'
            ElMessage.error('登录已失效，请重新登录')
            return Promise.reject(new Error('Token失效，请重新登录'))
        }

        // 4. 保留原有：错误提示逻辑（不删除）
        const errorMsg = error.response?.data?.message ||
            error.message ||
            '请求失败'
        console.error('🚨 响应错误:', {
            url: error.config?.url,
            status: error.response?.status,
            message: errorMsg
        })
        ElMessage.error(errorMsg)
        return Promise.reject(error)
    }
)

export default service