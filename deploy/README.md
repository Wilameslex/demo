# 部署脚本使用说明

## 目录结构
```
deploy/
├── start.sh      # 启动脚本
├── stop.sh       # 停止脚本
├── status.sh     # 状态检查脚本
├── logs/         # 日志目录
│   ├── file-server.log
│   └── backend.log
├── file-server.pid
└── backend.pid
```

## 使用方法

### 1. 启动所有服务
```bash
cd /mnt/d/workspace/proj-c/demo
chmod +x deploy/*.sh
./deploy/start.sh
```

### 2. 停止所有服务
```bash
./deploy/stop.sh
```

### 3. 查看服务状态
```bash
./deploy/status.sh
```

### 4. 查看日志
```bash
# 文件服务器日志
tail -f deploy/logs/file-server.log

# 后端服务日志
tail -f deploy/logs/backend.log
```

## 服务说明

- **Nginx (80端口)**: 前端静态文件 + 反向代理
- **文件服务器 (3003端口)**: Node.js文件下载服务
- **后端服务 (3002端口)**: Spring Boot业务API

## 注意事项

1. 首次运行前需要构建前端:
```bash
cd crab-frontend-v3
npm run build
```

2. Nginx需要手动启动:
```bash
sudo service nginx start
```

3. 修改配置后需要重启相应服务
