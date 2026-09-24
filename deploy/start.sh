#!/bin/bash

# 项目根目录由脚本自身位置推算，部署到任何路径都无需修改
PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
LOG_DIR="$PROJECT_DIR/deploy/logs"

# Java 服务需要较多文件描述符，服务器默认 soft limit 可能只有 1024
ulimit -n 4096 2>/dev/null

# R 相关路径显式注入，避免依赖工作目录
export R_SCRIPTS_PATH="$PROJECT_DIR/src/main/resources/r_scripts"
export R_TEMP_DIR="$PROJECT_DIR/tmp"
export R_EGGNOG_EMFILE="$PROJECT_DIR/src/main/resources/data/eggnog/query_seqs.fa.emmaper.annotations"
export R_BIN_PATH="${R_BIN_PATH:-/usr/bin}"
export R_LIB_PATH="${R_LIB_PATH:-/usr/local/lib/R/site-library}"

mkdir -p "$LOG_DIR"
mkdir -p "$R_TEMP_DIR"

echo "项目目录: $PROJECT_DIR"
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
nohup java \
  -Xms512m -Xmx1024m \
  -XX:+UseParallelGC \
  -XX:MaxMetaspaceSize=256m \
  --add-opens java.base/java.lang=ALL-UNNAMED \
  --add-opens java.base/java.util=ALL-UNNAMED \
  -jar target/crdb-0.0.1-SNAPSHOT.jar > "$LOG_DIR/backend.log" 2>&1 &
BACKEND_PID=$!
echo "后端服务已启动, PID: $BACKEND_PID"
echo $BACKEND_PID > "$PROJECT_DIR/deploy/backend.pid"

echo "所有服务已启动"
echo "查看日志: tail -f $LOG_DIR/file-server.log"
echo "查看日志: tail -f $LOG_DIR/backend.log"
echo "停止服务: ./deploy/stop.sh"
