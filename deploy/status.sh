#!/bin/bash

# 项目根目录由脚本自身位置推算，部署到任何路径都无需修改
PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

echo "服务状态检查:"
echo "===================="

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
echo "端口占用情况 (3002/3003):"

# ss 通常位于 /usr/sbin，可能不在普通用户 PATH 中，逐级回退
PORT_CMD=""
command -v ss > /dev/null 2>&1 && PORT_CMD="ss -tln"
[ -z "$PORT_CMD" ] && [ -x /usr/sbin/ss ] && PORT_CMD="/usr/sbin/ss -tln"
[ -z "$PORT_CMD" ] && command -v netstat > /dev/null 2>&1 && PORT_CMD="netstat -tln"

if [ -n "$PORT_CMD" ]; then
    $PORT_CMD 2>/dev/null | grep -E ':(3002|3003)[[:space:]]' || echo "  3002 / 3003 均未监听"
else
    echo "  系统无 ss / netstat，跳过端口检查"
fi
