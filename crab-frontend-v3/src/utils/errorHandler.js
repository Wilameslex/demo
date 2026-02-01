// 移除Element UI引用，使用Element Plus
import { ElMessage } from 'element-plus'

export default {
    install(app) {
        // 全局错误处理
        app.config.errorHandler = (err, vm, info) => {
            console.error('Vue错误:', err, info)
            ElMessage.error('发生了一个错误: ' + err.message)
        }

        // 全局Promise错误处理
        window.addEventListener('unhandledrejection', event => {
            console.error('Promise错误:', event.reason)
            ElMessage.error('发生了一个异步错误: ' + event.reason.message)
            event.preventDefault()
        })
    }
}