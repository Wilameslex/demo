#!/bin/bash

PROJECT_DIR="/mnt/d/workspace/demo"

echo "停止服务..."

# 停止文件服务器
if [ -f "$PROJECT_DIR/deploy/file-server.pid" ]; then
    FILE_SERVER_PID=$(cat "$PROJECT_DIR/deploy/file-server.pid")
    if ps -p $FILE_SERVER_PID > /dev/null 2>&1; then
        kill $FILE_SERVER_PID
        echo "文件服务器已停止 (PID: $FILE_SERVER_PID)"
    else
        echo "文件服务器进程不存在"
    fi
    rm "$PROJECT_DIR/deploy/file-server.pid"
else
    echo "未找到文件服务器PID文件"
fi

# 停止后端服务
if [ -f "$PROJECT_DIR/deploy/backend.pid" ]; then
    BACKEND_PID=$(cat "$PROJECT_DIR/deploy/backend.pid")
    if ps -p $BACKEND_PID > /dev/null 2>&1; then
        kill $BACKEND_PID
        echo "后端服务已停止 (PID: $BACKEND_PID)"
    else
        echo "后端服务进程不存在"
    fi
    rm "$PROJECT_DIR/deploy/backend.pid"
else
    echo "未找到后端PID文件"
fi

# 清理Maven进程
pkill -f "spring-boot:run"

echo "所有服务已停止"
