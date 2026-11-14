# 单体通用开发基座 (Single Admin Base)

基于 Spring Boot 3 + Vue 3 的基础单体通用开发基座，采用前后端分离架构，内置 RBAC 权限控制、动态菜单路由、机构管理、字典参数、文件管理等通用系统能力，并附带命令行代码生成工具，可作为各类业务系统的起始模板。

---

## 功能特性

- **RBAC 权限系统**：基于“用户-角色-权限”多对多模型的细粒度权限控制，支持接口权限与菜单/按钮权限
- **动态菜单与路由**：根据用户权限自动生成前端菜单与路由，实现页面级权限控制
- **用户与角色管理**：用户增删改查、状态启用/禁用、角色分配、多角色支持
- **机构管理**：树形机构结构、用户关联
- **字典与参数管理**：系统字典、业务参数、运行配置维护
- **系统配置管理**：站点标题、主题色、备案号、对象存储等配置集中维护，前后端实时生效
- **在线用户管理**：实时监控在线用户，支持会话查看与强制下线
- **文件管理**：集成 MinIO / 阿里云 OSS / 七牛云对象存储，支持存储服务商配置切换、文件上传与统一管理
- **代码生成器（命令行工具）**：通过命令行手动执行，根据数据库表生成前后端基础代码；无 Web 入口与权限控制
- **动态主题**：支持自定义主题色、字体大小、深色模式，可切换后端统一下发或前端自主配置
- **多环境配置**：支持 dev / test / prod 等多环境配置切换
- **Docker 部署**：提供完整的 Docker Compose 编排方案，一键部署全部服务

---

## 技术栈

### 后端

| 技术 | 版本 | 说明 |
|------|------|------|
| Java | 25 | |
| Spring Boot | 3.5.14 | 基础框架 |
| Sa-Token | 1.40.0 | 认证鉴权（整合 Redis） |
| MyBatis-Plus | 3.5.14 | ORM 框架 |
| MySQL | 8.0+ | 数据库 |
| Redis | - | 缓存 / 会话存储 |
| MinIO | 8.5.7 | 对象存储 |
| Knife4j | 4.5.0 | API 文档 |
| Undertow | - | Web 容器（替代 Tomcat） |

### 前端

| 技术 | 版本 | 说明 |
|------|------|------|
| Vue | 3.5.x | 渐进式框架 |
| Vite | 7.3.x | 构建工具 |
| Vue Router | 5.0.x | 路由管理（含动态路由） |
| Pinia | 3.0.x | 状态管理 |
| Element Plus | 2.13.x | UI 组件库 |
| ECharts | 6.x | 图表库 |
| TailwindCSS | 3.4.x | 原子化 CSS |
| Axios | 1.13.x | HTTP 请求 |

---

## 系统架构

### 整体架构

```mermaid
graph TB
    subgraph "前端 Frontend"
        A1[Vue 3 + Vite]
        A2[Element Plus + TailwindCSS]
        A3[Pinia + Vue Router]
    end

    subgraph "后端 Backend"
        B1[Spring Boot 3]
        B2[Sa-Token 认证]
        B3[MyBatis-Plus ORM]
    end

    subgraph "数据层 Data"
        C1[(MySQL)]
        C2[(Redis)]
        C3[(MinIO)]
    end

    A1 -->|HTTP/RESTful API| B1
    B2 -->|会话存储| C2
    B3 -->|持久化| C1
    B1 -->|文件存储| C3
```

### RBAC 权限模型

```mermaid
graph TB
    U[sys_user 用户] -->|多对多| UR[sys_user_role 关联表]
    UR -->|roleId| R[sys_role 角色]
    R -->|多对多| RP[sys_role_permission 关联表]
    RP -->|permissionId| P[sys_permission 权限]

    P --> PT{permissionType}
    PT -->|1-接口权限| API[API 接口鉴权]
    PT -->|2-菜单/路由权限| ROUTE[前端动态路由]
```

### 权限查询流程

```mermaid
sequenceDiagram
    participant Client as 客户端
    participant Auth as StpInterfaceImpl
    participant DB as 数据库

    Client->>Auth: 登录认证
    Auth->>DB: 查询用户角色列表<br/>JOIN sys_user_role + sys_role
    DB-->>Auth: 返回角色列表
    Auth->>DB: 查询用户权限码列表<br/>JOIN sys_user_role + sys_role_permission + sys_permission
    DB-->>Auth: 返回权限码列表
    Auth-->>Client: 认证成功<br/>携带角色 & 权限信息
```

---

## 项目结构

```
single-admin/
├── backend/                          # 后端项目 (Spring Boot)
│   ├── src/main/java/com/hfh/api/
│   │   ├── config/                   # 配置类（Sa-Token、Redis、MyBatis-Plus）
│   │   ├── modules/
│   │   │   └── system/               # system 模块（用户、角色、权限、机构、字典、配置）
│   │   ├── base/                     # 基础类（Controller、Entity）
│   │   ├── utils/                    # 工具类
│   │   ├── exception/                # 异常处理
│   │   └── generator/                # 命令行代码生成工具（无 Web 入口）
│   └── src/main/resources/
│       ├── mapper/                   # MyBatis XML 映射
│       │   └── system/               # system 模块 Mapper XML
│       ├── db/migration/V1__init.sql # Flyway 迁移脚本（建表 + 初始化数据）
│       └── application*.yaml         # 配置文件
│
├── frontend/                         # 前端项目 (Vue 3 + Vite)
│   ├── src/
│   │   ├── api/                      # API 接口层
│   │   ├── views/                    # 页面视图
│   │   │   ├── admin/
│   │   │   │   ├── dashboard/        # 数据看板
│   │   │   │   ├── system/           # 系统管理（用户/角色/权限/机构/字典/配置）
│   │   │   │   └── layout/           # 布局组件
│   │   │   └── login/                # 登录页
│   │   ├── router/                   # 路由配置（静态 + 动态）
│   │   ├── stores/                   # Pinia 状态管理
│   │   ├── components/               # 公共组件
│   │   └── utils/                    # 工具函数
│   └── package.json
│
├── docker/                           # 容器辅助脚本
│   └── redis/redis-entrypoint.sh     # Redis 启动脚本（从环境变量生成配置）
├── docker-compose.yml                # Docker 编排配置
├── .env / .env.example               # 全项目唯一环境变量（前后端 / 本地 / Docker 共用）
└── docs/                             # 项目文档
    ├── IMPROVEMENTS.md               # 改进评估清单与推进计划
    └── test-report.md                # 单元测试与覆盖率报告
```

---

## 数据库设计

### 表结构概览

| 表名前缀 | 模块 | 说明 |
|----------|------|------|
| `sys_` | 系统模块 | 用户、角色、权限、机构、字典、配置等 |

### 核心权限表

- **sys_user** — 用户表
- **sys_role** — 角色表
- **sys_permission** — 权限表（支持接口权限 / 菜单路由权限两种类型）
- **sys_user_role** — 用户角色关联表（多对多）
- **sys_role_permission** — 角色权限关联表（多对多）

> 详细建表 SQL 请参见 `backend/src/main/resources/db/migration/V1__init.sql`（由 Flyway 在启动时执行）

---

## 快速开始

### 环境要求

- Node.js >= 20.19.0
- pnpm（通过 `corepack enable pnpm` 启用）
- Java >= 25
- Maven 3.x
- MySQL 8.0+
- Redis
- MinIO

> **环境变量**：全项目只使用项目根目录的 `.env`（从 `.env.example` 复制，已被 git 忽略，禁止提交）。docker compose、后端本地开发（Spring 导入）、前端 Vite 与前端 Docker 构建都读取它；不要再创建 `backend/.env` 或 `frontend/.env.*`。各变量含义见 `.env.example` 注释。

### 后端启动

```bash
# 1. 创建空数据库（表结构与初始化数据由后端启动时的 Flyway 自动执行）
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS db_single_admin CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci"

# 2. 配置本地凭据
# 在项目根目录复制 .env.example 为 .env，填写数据库与 Redis 密码

[Environment]::SetEnvironmentVariable('JAVA_HOME','C:\Program Files\BellSoft\LibericaJDK-25','User')

# 3. 启动后端服务（基础配置不默认激活 profile，需显式指定 dev）
$env:JAVA_HOME = 'C:\Program Files\BellSoft\LibericaJDK-25'
$env:Path = "$env:JAVA_HOME\bin;" + $env:Path
$env:SPRING_PROFILES_ACTIVE = 'dev'
java --version
javac --version

cd backend
./mvnw spring-boot:run
```

> PowerShell 5.1 会把 `-Dspring-boot.run.profiles=dev` 拆成两段，导致 `Unknown lifecycle phase ".run.profiles=dev"`；请改用上面的环境变量，或把参数整体加引号：`mvn spring-boot:run "-Dspring-boot.run.profiles=dev"`。bash 下可用 `SPRING_PROFILES_ACTIVE=dev ./mvnw spring-boot:run`。
> 后端基于 Java 25 编译，启动前请确认 `java -version` 与 `mvn -version` 的 `Java version` 均为 25，否则会报「不支持发行版本 25」。

后端服务默认运行在 `http://localhost:9999`（端口可通过 `SERVER_PORT` 环境变量覆盖），API 文档（仅 dev profile 开启）：`http://localhost:9999/doc.html`，健康检查：`http://localhost:9999/actuator/health`，Prometheus 指标：`http://localhost:9999/actuator/prometheus`

### 前端启动

```bash
# 1. 安装依赖（项目使用 pnpm，锁文件已纳入版本控制）
cd frontend
corepack enable pnpm
pnpm install

# 2. 启动开发服务器
pnpm dev
```

前端开发服务器默认运行在 `http://localhost:9998`

### Docker 部署（推荐）

```bash
# 1. 首次部署：复制统一模板并填写凭据（PowerShell 用 Copy-Item .env.example .env）
cp .env.example .env

# 2. 一键启动全部服务（在项目根目录执行）
docker compose up -d
```

Docker Compose 将自动启动以下服务：
- MySQL 8.0
- Redis
- MinIO
- 后端 API 服务
- 前端 Nginx 服务

compose 从根目录 `.env` 读取端口与凭据并注入后端容器；前端构建通过 `build args` 注入 `VITE_API_URL`，`.env` 本身不会进入镜像。

发布与回滚：
- 推送 `v*` tag 会触发发布流水线，构建前后端镜像并推送到 GHCR，同时生成 GitHub Release 变更记录。
- 回滚镜像：在 `.env` 中把 `VERSION` 改为上一个版本 tag（镜像保留历史 tag），执行 `docker compose up -d`。
- 回滚数据库：Flyway 社区版不支持自动回滚；含迁移脚本的发布需提前备份 MySQL 并准备逆向脚本，回滚时先在备份上验证再人工执行。

---

## 可用命令

### 前端

| 命令 | 说明 |
|------|------|
| `pnpm dev` | 开发环境启动 |
| `pnpm dev:test` | 测试环境启动 |
| `pnpm build` | 生产环境构建 |
| `pnpm build:test` | 测试环境构建 |
| `pnpm lint` | 代码检查 |
| `pnpm lint:ci` | 代码检查（CI 模式，不自动修复） |
| `pnpm test` | 单元测试 |
| `pnpm test:coverage` | 单元测试 + 覆盖率门槛校验 |
| `pnpm test:e2e` | Playwright 端到端测试（登录、菜单、按钮级权限） |
| `pnpm format` | 代码格式化 |

### 后端

| 命令 | 说明 |
|------|------|
| `$env:SPRING_PROFILES_ACTIVE='dev'; ./mvnw spring-boot:run` | 启动应用（PowerShell，显式指定 dev profile） |
| `SPRING_PROFILES_ACTIVE=dev ./mvnw spring-boot:run` | 启动应用（bash） |
| `./mvnw spring-boot:run "-Dspring-boot.run.profiles=dev"` | 启动应用（-D 参数必须加引号） |
| `./mvnw package -DskipTests` | 打包 JAR |
| `./mvnw verify` | 全量测试 + JaCoCo 覆盖率门槛校验 |
| `java -jar backend.jar --spring.profiles.active=prod` | 生产环境运行 |

---

## 文档

- [文档索引](docs/README.md)
- [改进评估清单](docs/IMPROVEMENTS.md)
- [测试与覆盖率报告](docs/test-report.md)
- [贡献指南](docs/CONTRIBUTING.md)
- [安全策略](docs/SECURITY.md)
- [变更日志](docs/CHANGELOG.md)

---

## 许可证

本项目基于 [MIT License](LICENSE) 开源。
