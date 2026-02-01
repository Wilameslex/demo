#!/bin/bash

PROJECT_DIR="/mnt/d/workspace/demo"
LOG_DIR="$PROJECT_DIR/deploy/logs"

mkdir -p "$LOG_DIR"

echo "开始启动服务..."

# 启动Node.js文件服务器(3003端口)
echo "启动文件服务器..."
cd "$PROJECT_DIR/server"
nohup node file-server.js > "$LOG_DIR/file-server.log" 2>&1 &
FILE_SERVER_PID=$!
echo "文件服务器已启动, PID: $FILE_SERVER_PID"
echo $FILE_SERVER_PID > "$PROJECT_DIR/deploy/file-server.pid"

# 等待文件服务器启动
sleep 2

# 启动Spring Boot后端(3002端口)
echo "启动后端服务..."
cd "$PROJECT_DIR"
nohup java -jar target/crdb-0.0.1-SNAPSHOT.jar > "$LOG_DIR/backend.log" 2>&1 &
BACKEND_PID=$!
echo "后端服务已启动, PID: $BACKEND_PID"
echo $BACKEND_PID > "$PROJECT_DIR/deploy/backend.pid"

echo "所有服务已启动"
echo "查看日志: tail -f $LOG_DIR/file-server.log"
echo "查看日志: tail -f $LOG_DIR/backend.log"
echo "停止服务: ./deploy/stop.sh"
