const { defineConfig } = require('@vue/cli-service')

module.exports = defineConfig({
    transpileDependencies: true,
    devServer: {
        host: '0.0.0.0', // 允许外部访问
        port: 3001, // 前端开发端口
        allowedHosts: 'all', // 允许所有域名访问，解决 Invalid Host header
        client: {
            webSocketURL: 'ws://localhost:8080/ws',
        },
        proxy: {
            // 主后端服务代理
            '/api': {
                target: 'http://localhost:3002',
                changeOrigin: true,
                pathRewrite: { '^/api': '' },
                ws: false, // 关闭WebSocket代理
                proxyTimeout: 600000
            },
            '/enrichment-proxy': {
                target: 'http://localhost:3002', // 后端服务地址
                changeOrigin: true,
                // 转发时将 /enrichment-proxy 替换为 /api（主动补全后端需要的 /api 前缀）
                pathRewrite: { '^/enrichment-proxy': '' },
                ws: false,
                proxyTimeout: 600000, // 富集分析可能耗时，延长超时
                onProxyReq(proxyReq) {
                    // 处理路径中的空格（可选，视后端需求）
                    if (proxyReq.path.includes(' ')) {
                        proxyReq.path = proxyReq.path.replace(/ /g, '%20');
                    }
                }
            },

            // 文件服务代理 - 独立配置
            '/file-api': {
                target: 'http://localhost:3003',
                changeOrigin: true,
                pathRewrite: { '^/file-api': '/file-api' }, // 保持路径不变
                logLevel: 'debug',
                ws: false, // 这里也需要关闭WebSocket
                proxyTimeout: 60000, // 添加超时设置
                onProxyReq(proxyReq) { // 这里也需要空格处理逻辑
                    const reqPath = proxyReq.path;
                    if (reqPath.includes('%20')) {
                        proxyReq.path = reqPath.replace(/ /g, '%20');
                    }
                }
            }
        },

    },
    chainWebpack: config => {
        config.plugin("html")
            .tap(args => {
                args[0].title = "基因组数据库系统";
                args[0].keywords = "基因搜索,生物信息学";
                args[0].description = "中华绒螯蟹基因组育种数据库";
                return args;
            })
    }
})