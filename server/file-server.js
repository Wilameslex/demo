const express = require('express');
const fs = require('fs');
const path = require('path');
const cors = require('cors');

const app = express();
const PORT = 3003;
const DOWNLOAD_BASE = path.resolve(__dirname, 'downloads');

// 初始化检查
console.log('服务启动信息:');
console.log('- 当前工作目录:', __dirname);
console.log('- 下载目录绝对路径:', DOWNLOAD_BASE);

// 确保下载目录存在
if (!fs.existsSync(DOWNLOAD_BASE)) {
    console.log('正在创建下载目录...');
    fs.mkdirSync(DOWNLOAD_BASE, { recursive: true });
}

// 安全路径验证
const validatePath = (inputPath) => {
    try {
        // 处理空路径和根路径
        if (!inputPath || inputPath === '/' || inputPath === '\\')
            return '/';

        // 标准化路径
        let normalized = inputPath
            .replace(/\\/g, '/')
            .replace(/\/+/g, '/');
        // 确保路径以斜杠开头
        if (!normalized.startsWith('/')) {
            normalized = '/' + normalized;
        }

        // 移除末尾斜杠（如果有）
        normalized = normalized.replace(/\/$/, '');

        // 禁止路径遍历
        if (normalized.includes('../'))
            throw new Error('包含非法路径遍历符');

        // 验证路径是否在允许范围内
        const resolved = path.resolve(DOWNLOAD_BASE, normalized.substring(1)); // 移除开头的斜杠

        if (!resolved.startsWith(DOWNLOAD_BASE))
            throw new Error('超出允许访问范围');

        return normalized;
    } catch (error) {
        console.error('路径验证失败:', error.message);
        throw new Error(`无效路径: ${inputPath}`);
    }
};

// 路径处理中间件
app.use((req, res, next) => {
    console.log(`收到请求: ${req.method} ${decodeURIComponent(req.url)}`);
    next();
});

// CORS配置
app.use(cors({
    origin: ['http://localhost:8080',
            'http://10.210.133.133',
            'http://localhost:8082'
            ],
    // 允许自定义头
    allowedHeaders: ['Content-Type', 'Authorization'],
    // 确保预检请求缓存
    maxAge: 86400
}));

// 文件列表API
app.get('/file-api/files', (req, res) => {
    try {
        let reqPath = req.query.path || '/';
        reqPath = decodeURIComponent(reqPath); // 解码前端传递的路径

        // 使用验证函数处理路径
        const validatedPath = validatePath(reqPath);
        console.log('验证后路径:', validatedPath);

        //构造完整文件系统路径
        const fullPath = path.join(DOWNLOAD_BASE, validatedPath);
        console.log('实际访问路径:', fullPath);

        if (!fs.existsSync(fullPath)) {
            console.log('路径不存在:', fullPath);
            return res.status(404).json({ error: '路径不存在' });
        }

        const stat = fs.statSync(fullPath);
        if (!stat.isDirectory()) {
            console.log('不是目录:', fullPath);
            return res.status(400).json({ error: '请求的不是目录' });
        }

        const items = fs.readdirSync(fullPath, { withFileTypes: true });

        const files = items.map(item => {
            const itemPath = path.join(validatedPath, item.name);
            const itemFullPath = path.join(fullPath, item.name);
            const itemStats = fs.statSync(itemFullPath);

            return {
                name: item.name,
                type: item.isDirectory() ? 'directory' : 'file',
                size: item.isDirectory() ? '-' : itemStats.size,
                path: encodeURIComponent(itemPath),
                lastModified: itemStats.mtime.toISOString()
            };
        });

        // 计算父路径
        let parentPath = path.dirname(validatedPath);
        if (parentPath === '/') parentPath = '/';
        else if (parentPath === '.') parentPath = '/';

        res.json({
            currentPath: validatedPath,
            parentPath: parentPath,
            files
        });

    } catch (error) {
        console.error('处理失败:', error.stack); // 打印完整错误堆栈
        res.status(500).json({
            error: '服务器错误',
            detail: error.message,
            stack: process.env.NODE_ENV === 'development' ? error.stack : undefined
        });
    }
});

// 文件下载API
app.get('/file-api/download', (req, res) => {
    try {
        if (!req.query.path) throw new Error('缺少path参数');

        // 解码并验证路径
        const filePath = decodeURIComponent(req.query.path)
            .replace(/\\/g, '/')  // 统一转为正斜杠
            .replace(/\/+/g, '/'); // 合并连续斜杠

        const fullPath = path.join(DOWNLOAD_BASE, filePath);
        console.log('下载请求:', fullPath);

        // 安全检查
        if (!fullPath.startsWith(DOWNLOAD_BASE)) {
            throw new Error('禁止访问该路径');
        }

        if (!fs.existsSync(fullPath)) {
            throw new Error('文件不存在');
        }

        if (fs.statSync(fullPath).isDirectory()) {
            throw new Error('不能下载目录');
        }

        // 设置下载头
        res.download(fullPath, path.basename(fullPath), {
            headers: {
                'Content-Disposition': `attachment; filename*=UTF-8''${encodeURIComponent(path.basename(fullPath))}`,
                'Content-Type': 'application/octet-stream',
                'Cache-Control': 'no-cache'
            }
        });

    } catch (error) {
        console.error('下载处理失败:', error.message);
        res.status(400).json({
            error: '下载失败',
            detail: error.message
        });
    }
});

// 默认路由
app.get('/', (req, res) => {
    res.send('基因组数据下载服务已运行');
});

// 启动服务
const server = app.listen(PORT, '0.0.0.0', () => {
        console.log(`服务已启动: http://localhost:${PORT}`);
        console.log('重要提示:');
        console.log('1. 请确保前端使用encodeURIComponent编码路径');
        console.log('2. 空格会被自动转为%20');
});

// 错误处理
process.on('uncaughtException', (err) => {
    console.error('未捕获异常:', err);
});

server.on('error', (err) => {  // 使用server变量
    console.error('服务器错误:', err);
});