import axios from 'axios'
import { ElMessage } from 'element-plus'
import { v4 as uuidv4 } from 'uuid'

const service = axios.create({
  baseURL: process.env.VUE_APP_BASE_API,
  timeout: 30000,
  headers: {
    Authorization: 'Basic ' + btoa('admin:password'),
    'Content-Type': 'application/json',
  },
})

// 请求拦截器
service.interceptors.request.use(
  (config) => {
    config.headers['X-Request-ID'] = uuidv4()

    if (process.env.NODE_ENV === 'development') {
      console.log('📡 请求发出 =>', config.method?.toUpperCase(), config.url)
      console.log('📦 请求参数:', config.data)
    }
    return config
  },
  (error) => {
    console.error('🚨 请求错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  (response) => {
    const res = response.data

    if (process.env.NODE_ENV === 'development') {
      console.log('✅ 响应接收 =>', response.config.url)
      console.log('📊 响应数据:', res)
    }

    if (res.code !== 200) {
      ElMessage.error(res.message || '接口业务错误') // 使用Element Plus
      return Promise.reject(new Error(res.message || 'Error'))
    }
    return res
  },
  (error) => {
    const errorMsg =
      error.response?.data?.message || error.message || '请求失败'

    console.error('🚨 响应错误:', {
      url: error.config?.url,
      status: error.response?.status,
      message: errorMsg,
    })

    ElMessage.error(errorMsg) // 使用Element Plus
    return Promise.reject(error)
  }
)

export default service
