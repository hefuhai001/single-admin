# 项目文档

本目录集中存放 single-admin 的设计、部署与维护文档。根目录的 [README](../README.md) 保留项目简介与快速开始。

## 文档索引

### 已提供

| 文档 | 说明 |
| --- | --- |
| [IMPROVEMENTS.md](IMPROVEMENTS.md) | 代码审阅得出的改进清单，按 P0 ~ P4 分级，含推进计划与验收标准 |
| [test-report.md](test-report.md) | 前后端单元测试与覆盖率报告，含复现命令 |

### 计划中

以下文档尚未编写，欢迎按 [CONTRIBUTING.md](CONTRIBUTING.md) 的流程补充。

| 文档 | 计划内容 |
| --- | --- |
| `quickstart.md` | 5 分钟上手：Docker 与本地两套启动路径 |
| `deployment.md` | 生产部署：TLS、反向代理、端口收敛、备份恢复、升级与回滚 |
| `configuration.md` | 配置参考：环境变量、`application-*.yaml`、`sys_config` 配置项与默认值 |
| `architecture.md` | 架构说明：模块划分、RBAC 模型、双 Token、缓存与权限刷新机制 |
| `development.md` | 开发指南：新增业务模块的完整步骤、代码生成器用法 |
| `api.md` | 接口约定：认证流程、统一响应结构、错误码、权限码命名规则 |
| `security.md` | 安全基线：默认账号处理、密钥管理、上线检查清单 |
| `faq.md` | 常见问题：端口冲突、JDK 版本、Flyway 失败、Redis 连接异常 |
| `testing.md` | 测试指南：命令、覆盖率报告位置、Testcontainers 的前置条件 |

## 文档约定

- 使用简体中文编写，代码、命令、配置键保持英文；
- 命令以 Windows PowerShell 为主，跨平台命令单独标注；
- 不写入真实账号、密码、密钥或内网地址；
- 新增功能或修改配置项时同步更新对应文档；
- 文件名使用小写短横线或全小写单词，避免空格。

## 相关文件

- [贡献指南](CONTRIBUTING.md)
- [安全策略](SECURITY.md)
- [变更日志](CHANGELOG.md)
- [开源许可证](../LICENSE)
