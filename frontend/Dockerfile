# ==================== 构建阶段 ====================
FROM node:22 AS builder

# 设置工作目录
WORKDIR /app

# 启用 pnpm 并设置镜像源
RUN corepack enable pnpm && \
    pnpm config set registry https://registry.npmmirror.com

# 复制 pnpm 相关文件
COPY package.json pnpm-lock.yaml ./

# 安装依赖（忽略生命周期脚本，构建需要 devDependencies）
RUN pnpm install --ignore-scripts && \
    pnpm store prune

# 复制源代码
COPY . .

# 构建生产版本
RUN pnpm run build:prod

# ==================== 运行阶段 ====================
FROM nginx:1.30-alpine

# 安装必要工具和创建非 root 用户
RUN apk add --no-cache curl tzdata && \
    addgroup -S nginxgroup && \
    adduser -S nginxuser -G nginxgroup && \
    rm -rf /var/cache/apk/*

# 设置时区
ENV TZ=Asia/Shanghai

# 创建必要的目录并设置权限
RUN mkdir -p /var/cache/nginx /var/log/nginx /usr/share/nginx/html /run /tmp/nginx && \
    chown -R nginxuser:nginxgroup /var/cache/nginx /var/log/nginx /usr/share/nginx/html /etc/nginx /run /tmp/nginx

# 从构建阶段复制构建产物
COPY --from=builder /app/dist /usr/share/nginx/html

# 复制 nginx 配置文件
COPY default.conf /etc/nginx/conf.d/default.conf

# 切换到非 root 用户
USER nginxuser

# 暴露端口
EXPOSE 80

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --start-period=10s --retries=3 \
    CMD curl -f http://localhost/ || exit 1

# 启动 nginx
CMD ["nginx", "-g", "daemon off;"]