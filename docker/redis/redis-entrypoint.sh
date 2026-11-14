#!/bin/sh
# Redis 启动脚本：从环境变量生成配置文件，避免密码出现在进程命令行中
set -eu

: "${REDIS_PASSWORD:?REDIS_PASSWORD is required}"

umask 077
CONF=/tmp/redis.conf

cat > "$CONF" <<EOF
bind 0.0.0.0
protected-mode yes
port 6379
dir /data
requirepass ${REDIS_PASSWORD}
save ${REDIS_SAVE_900:-900} ${REDIS_SAVE_900_VALUE:-1}
save ${REDIS_SAVE_300:-300} ${REDIS_SAVE_300_VALUE:-10}
save ${REDIS_SAVE_60:-60} ${REDIS_SAVE_60_VALUE:-10000}
dbfilename ${REDIS_DBFILENAME:-dump.rdb}
appendonly ${REDIS_APPENDONLY:-yes}
appendfilename ${REDIS_APPENDFILENAME:-appendonly.aof}
appendfsync ${REDIS_APPENDFSYNC:-everysec}
maxmemory ${REDIS_MAXMEMORY:-256mb}
maxmemory-policy ${REDIS_MAXMEMORY_POLICY:-allkeys-lru}
EOF

exec redis-server "$CONF"
