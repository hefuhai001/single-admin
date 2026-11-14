# Changelog

本文件记录项目的重要变更。

格式参考 [Keep a Changelog](https://keepachangelog.com/zh-CN/1.1.0/)，版本号遵循 [语义化版本](https://semver.org/lang/zh-CN/)。

> 项目目前尚未发布正式版本，以下 `Unreleased` 内容按当前 `master` 分支整理。

## [Unreleased]

### Added

- RBAC 权限体系：用户、角色、权限、用户角色与角色权限关联，支持接口权限与菜单路由权限。
- 动态菜单与路由：根据用户权限下发路由，前端动态注册并支持退出登录后重置。
- 双 Token 认证：accessToken + refreshToken，支持按设备登录、刷新与退出，限制同一账号在线设备数。
- 登录安全：登录失败计数、账号锁定、滑块验证码、邮箱验证码注册。
- 系统管理：用户、角色、权限、机构、字典、参数字典、系统配置。
- 监控能力：在线用户、Redis 缓存监控、对象存储管理、邮件发送、定时任务与任务日志。
- 对象存储：MinIO、阿里云 OSS、七牛云三套策略，可在系统配置中动态切换。
- 缓存能力：配置与字典缓存、权限缓存，支持注解触发的缓存刷新。
- 数据库版本管理：Flyway 迁移脚本，启动时自动建表与初始化数据。
- 可观测性：TraceId 过滤器、Actuator 健康检查、Prometheus 指标导出。
- 部署：Docker Compose 一键编排 MySQL、Redis、MinIO、后端与前端。
- 测试基建：后端 JUnit 5 + Mockito + JaCoCo，前端 Vitest + v8 coverage。
- CI：后端构建与测试、前端 lint 与构建、Trivy 文件系统漏洞与密钥扫描。

### Changed

- 敏感凭据从代码与配置文件迁移到 `.env` / 环境变量，仓库不再保留明文密码。
- 前端视图改为构建前生成显式注册表，避免全量扫描 `src/views`。
- 生产环境关闭 Knife4j 与 Swagger UI，日志改为结构化 JSON 输出。
- 统一全局异常响应结构，收敛参数校验与文件大小超限提示。

### Fixed

- 修复同一账号多设备登录时 token 相互覆盖的问题。
- 统一文件上传超限与参数校验失败的响应提示，避免直接返回堆栈信息。

### Security

- 移除仓库中的明文数据库与 Redis 凭据。
- 增加文件上传大小限制与统一异常处理，避免堆栈信息返回客户端。
- 增加登录失败锁定与验证码机制，降低暴力破解风险。

### Docs

- 新增单元测试与覆盖率报告（`test-report.md`）。
- 新增项目改进评估与推进计划（`IMPROVEMENTS.md`）。
- 补充 Windows 环境下 `JAVA_HOME` 配置说明。

## 发布约定

- 版本号采用 `MAJOR.MINOR.PATCH`；
- 破坏性变更必须在 `Changed` 中明确说明迁移方式；
- 每次发布同步更新本文件与 Git Tag。
