# 贡献指南

感谢你愿意为 single-admin 做出贡献。本文档说明本地开发、提交规范与 Pull Request 流程。

## 行为准则

参与本项目即表示你同意尊重其他贡献者，保持友善、就事论事的沟通方式。人身攻击、歧视性言论与骚扰行为不被接受。

## 环境要求

| 工具 | 版本 |
| --- | --- |
| Java | 25（后端编译与运行均需要） |
| Maven | 使用仓库自带的 `mvnw`，无需单独安装 |
| Node.js | `^20.19.0` 或 `>=22.12.0` |
| pnpm | 10（`corepack enable pnpm`） |
| MySQL | 8.0+ |
| Redis | 7+ |
| MinIO | 可选，仅对象存储相关功能需要 |
| Docker | 可选，Testcontainers 集成测试需要 |

> 注意：本机默认 `JAVA_HOME` 可能是 17，运行后端前请显式切换到 JDK 25。

## 本地开发

```powershell
# 后端
$env:JAVA_HOME = 'C:\Program Files\BellSoft\LibericaJDK-25'
cd backend
./mvnw spring-boot:run

# 前端
cd frontend
corepack enable pnpm
pnpm install
pnpm dev
```

首次启动前请参考 [README](../README.md) 创建数据库并配置 `.env`。表结构与初始化数据由 Flyway 在启动时自动执行。

## 分支与提交

1. 从 `master` 切出功能分支，命名建议：
   - `feat/模块名-简述`
   - `fix/问题简述`
   - `docs/文档主题`
2. 提交信息遵循 Conventional Commits：

```text
<type>(<scope>): <subject>

[optional body]
```

常用 `type`：

| type | 用途 |
| --- | --- |
| `feat` | 新功能 |
| `fix` | 缺陷修复 |
| `refactor` | 不改变行为的重构 |
| `test` | 测试相关 |
| `docs` | 文档相关 |
| `build` | 构建、依赖、CI 配置 |
| `chore` | 其他杂项 |

示例：

```text
feat(system): 用户列表支持按角色筛选
fix(auth): 修复 refreshToken 并发刷新导致的重复登录
docs: 补充生产环境部署说明
```

3. 一个 PR 尽量只做一件事，避免混入无关格式化改动。

## 代码约定

### 后端

- 使用构造器注入（Lombok `@RequiredArgsConstructor`），不要使用字段注入；
- Controller 只负责参数适配与响应包装，业务逻辑放在 Service，事务边界放在 Service；
- 新增接口必须补充 Sa-Token 权限注解（`@SaCheckPermission` / `@SaCheckLogin`）；
- 敏感字段（密码、密钥）不得出现在接口响应中；
- 数据库变更通过 `backend/src/main/resources/db/migration/` 下的 Flyway 脚本完成，不要直接修改已执行的脚本；
- 对外可感知的校验失败使用 `BusinessException`，不要抛出裸 `RuntimeException`。

### 前端

- 统一使用 Composition API 与 `<script setup>`；
- 接口调用放在 `src/api/modules/`，不要在页面中直接使用 axios；
- 页面组件放 `src/views/`，公共组件放 `src/components/`；
- 新增页面后执行 `pnpm views:sync`（`dev` / `build` 会自动执行）以更新视图注册表。

## 测试与检查

提交前请在本地执行：

```powershell
# 后端（需要 JDK 25）
cd backend
./mvnw clean test -B

# 前端
cd frontend
pnpm lint:ci
pnpm test
```

- 后端使用 JUnit 5 + Mockito + AssertJ，覆盖率报告位于 `backend/target/site/jacoco/index.html`；
- 前端使用 Vitest，覆盖率报告位于 `frontend/coverage/index.html`；
- 无 Docker 环境时，依赖 Testcontainers 的集成测试会自动跳过，属预期行为；
- 修复缺陷时请补充能复现问题的测试用例。

## 提交 Pull Request

1. 确认已与最新 `master` 同步并解决冲突；
2. 在 PR 描述中说明**为什么**要做这个改动，而不只是改了什么；
3. 关联相关 Issue（如 `Closes #123`）；
4. 勾选 PR 模板中的自查项；
5. 确保 CI（后端测试、前端 lint/build、Trivy 扫描）通过。

维护者可能会请求调整实现方式或补充测试，请耐心配合。小步快跑、聚焦单一目标的 PR 更容易被合并。

## 文档

- 用户文档统一放在 `docs/`，索引见 [文档索引](README.md)；
- 新增功能或修改配置项时，同步更新对应文档；
- 不要在文档中写入真实的账号、密码、密钥或内网地址。

## 不要提交的内容

- `.env`、`backend/.env` 等包含凭据的文件；
- 数据库导出、日志、构建产物（`target/`、`dist/`、`coverage/`）；
- IDE 个人配置（`.idea/`、`.vscode/` 中的个人设置）。

## 问题反馈

- **Bug**：使用 Bug 报告模板，附上环境信息与复现步骤；
- **功能建议**：使用功能请求模板，说明使用场景与预期收益；
- **安全漏洞**：请勿公开提交，按 [SECURITY.md](SECURITY.md) 私密上报。
