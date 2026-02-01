// import { defineConfig } from 'vite'
// import vue from '@vitejs/plugin-vue'
// import { resolve } from 'path'

// export default defineConfig({
//     plugins: [vue()],
//     resolve: {
//         alias: {
//             '@': resolve(__dirname, 'src')
//         }
//     },
//     server: {
//         host: '0.0.0.0', // 允许外部访问
//         port: 8080,
//         allowedHosts: 'all', // 允许所有域名访问，包括内网穿透域名
//         hmr: {
//             clientPort: 21546, // 内网穿透的外部端口
//             host: 'quick9.shenzhuo.vip' // 内网穿透的域名
//         },
//         proxy: {
//             '/api': {
//                 target: 'http://localhost:8000',
//                 changeOrigin: true
//             }
//         }
//     }
// })