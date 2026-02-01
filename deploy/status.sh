#!/bin/bash

PROJECT_DIR="/mnt/d/workspace/proj-c/demo"

echo "服务状态检查:"
echo "===================="

# 检查Nginx
if sudo service nginx status > /dev/null 2>&1; then
    echo "✓ Nginx: 运行中"
else
    echo "✗ Nginx: 未运行"
fi

# 检查文件服务器
if [ -f "$PROJECT_DIR/deploy/file-server.pid" ]; then
    FILE_SERVER_PID=$(cat "$PROJECT_DIR/deploy/file-server.pid")
    if ps -p $FILE_SERVER_PID > /dev/null 2>&1; then
        echo "✓ 文件服务器(3003): 运行中 (PID: $FILE_SERVER_PID)"
    else
        echo "✗ 文件服务器(3003): PID文件存在但进程不存在"
    fi
else
    echo "✗ 文件服务器(3003): 未运行"
fi

# 检查后端服务
if [ -f "$PROJECT_DIR/deploy/backend.pid" ]; then
    BACKEND_PID=$(cat "$PROJECT_DIR/deploy/backend.pid")
    if ps -p $BACKEND_PID > /dev/null 2>&1; then
        echo "✓ 后端服务(3002): 运行中 (PID: $BACKEND_PID)"
    else
        echo "✗ 后端服务(3002): PID文件存在但进程不存在"
    fi
else
    echo "✗ 后端服务(3002): 未运行"
fi

echo "===================="
echo "端口占用情况:"
sudo netstat -tlnp | grep -E ':(80|3002|3003) '
