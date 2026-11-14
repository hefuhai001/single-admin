# 单元测试完善与覆盖率报告

- 生成时间：2026-11-14
- 项目：single-admin（Spring Boot 3.5.14 / Java 25 + Vue 3.5 / Vite 7）
- 结论：**前后端行覆盖率均超过 60% 目标**（后端 82.7%，前端 85.33%），全部测试通过。

## 一、总体结果

| 端 | 测试框架 | 测试文件 | 用例数 | 通过 | 失败 | 跳过 | 行覆盖率 | 分支覆盖率 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| 后端 | JUnit 5 + Mockito + AssertJ + JaCoCo 0.8.14 | 40 | 295 | 295 | 0 | 2 | **82.7%**（2237/2705） | 74.98%（974/1299） |
| 前端 | Vitest 3 + @vue/test-utils + v8 coverage | 15 | 294 | 294 | 0 | 0 | **85.33%**（8788/10298） | 84.32%（834/989） |

> 后端跳过的 2 个用例为 `BackendApplicationTests` 的 Testcontainers 集成用例：本机未安装 Docker，`@Testcontainers(disabledWithoutDocker = true)` 自动跳过，属预期行为。
>
> 前端覆盖率以 statements/lines 为准（Vue SFC 两者统计口径一致）；functions 为 46.44%，主要因为模板编译产生大量行内渲染函数，不影响业务逻辑覆盖判断。

## 二、后端

### 2.1 新增测试基建

- `backend/pom.xml`：接入 `jacoco-maven-plugin:0.8.14`（`prepare-agent` + `test` 阶段生成报告），未添加任何排除规则。
- `backend/lombok.config`：`lombok.addLombokGeneratedAnnotation = true`，使 Lombok 生成的 getter/setter/构造器不计入覆盖率，避免虚高。
- 报告位置：`backend/target/site/jacoco/index.html`（明细）、`backend/target/site/jacoco/jacoco.csv`（汇总）。

### 2.2 测试分布（新增 36 个测试类，累计 40 个）

| 模块 | 测试类 | 主要覆盖点 |
| --- | --- | --- |
| auth | AuthServiceTest、CaptchaServiceTest、AuthControllerTest、CaptchaControllerTest | 登录锁定/失败计数/邮箱登录/双 token 刷新/注册校验、滑块与邮箱验证码 |
| system | ConfigServiceImplTest、PermissionServiceImplTest、InstitutionsServiceImplTest、RelationAndDictServiceTest、ConfigCacheServiceTest、DictCacheServiceTest、SystemControllerTest、DictMappingTest、CacheAspectTest | CRUD、树形权限、角色/用户关联、Redis 缓存命中与刷新、字典映射、缓存切面 |
| monitor | RedisMonitorServiceImplTest、EmailServiceTest、OnlineUserServiceImplTest、OssServiceTest、AbstractOssStrategyTest、Aliyun/Minio/Qiniu OssStrategyImplTest、DynamicOssConfigTest、JobServiceImplTest、JobSchedulingTest、JobTaskExecutorTest、各 Controller 测试、MailValidatorTest | Redis 信息解析/Key 管理、邮件发送与收件人校验、在线用户、三套 OSS 策略、任务调度与执行 |
| document | DocumentControllerTest | 指南/反馈分页、增删改查转发 |
| config / exception / utils | TraceIdFilterTest、StpInterfaceImplTest、ExceptionHandleTest、RedisUtilTest | 链路 ID、权限加载、全局异常、Redis 封装 |

### 2.3 各包行覆盖率（节选）

| 包 | 覆盖率 |
| --- | --- |
| auth.service / auth.controller | 97.1% / 100% |
| system.controller / system.cache / system.mapping | 99.2% / 95.3% / 92.3% |
| system.service.impl | 74.3% |
| monitor.redis.service.impl | 90.8% |
| monitor.oss.service(.impl) | 100% / 97.6% |
| monitor.task.task / task.controller | 95.9% / 100% |
| monitor.mail.service / validation | 100% / 100% |
| exception / utils | 100% / 100% |
| config | 45.3% |

未覆盖的主要部分：代码生成器（`GenCustomTemplate`、`CustomFreemarkerTemplateEngine`，共 136 行）、依赖 Spring 上下文初始化的配置类（`MailConfig`、`SaTokenConfigure`、`*Config`、`PasswordEncryptInitRunner` 等）、文档枚举常量类、`UserServiceImpl` 中导入导出等重分支。

## 三、前端

### 3.1 新增测试基建

- 新增 devDependencies：`vitest@3.2.7`、`@vue/test-utils@2.5.1`、`jsdom@26.1.0`、`@vitest/coverage-v8@3.2.7`。
- `frontend/vitest.config.js`：jsdom 环境、`@` 别名、`tests/**/*.spec.js`、v8 覆盖率，统计范围为 `src/**/*.{js,vue}`（仅排除 `main.js` 与自动生成的 `viewRegistry.generated.js`）。
- `frontend/tests/setup.js`：补齐 ResizeObserver / IntersectionObserver / matchMedia / scrollTo / crypto.randomUUID 等 jsdom 缺失能力。
- `package.json` 新增脚本：`test`、`test:watch`、`test:coverage`。
- 报告位置：`frontend/coverage/index.html`、`frontend/coverage/coverage-summary.json`。

### 3.2 测试分层

| 分层 | 文件 | 覆盖内容 |
| --- | --- | --- |
| API 层 | tests/api/*.spec.js | 19 个接口模块的 method/url/params/data 全量断言，聚合入口导出校验 |
| 状态层 | tests/stores/*.spec.js | auth 登录/注册/登出/资料、router 权限与动态路由、tabs 增删、theme/config 持久化 |
| 工具层 | tests/utils/*.spec.js | requestUtil 请求头、blob 透传、业务码异常、401 重试、404 提示；tokenRefresh 刷新成功/失败/并发排队 |
| 指令与组合式 | tests/directives、tests/composables | copy / debounce / throttle、useRegister 注册流程 |
| 路由 | tests/router/*.spec.js | 静态路由、动态路由生成与权限过滤、重置 |
| 组件与页面 | tests/components、tests/views/*.spec.js | 全部后台视图挂载渲染，用户 CRUD/批量删除、在线强退等关键交互；ECharts 组件 mock |

### 3.3 覆盖率较低的页面（其余均高于 67%）

| 页面 | 行覆盖率 | 原因 |
| --- | --- | --- |
| TabsCom.vue | 40.81% | 关闭/滚动逻辑依赖真实路由切换与鼠标滚轮事件 |
| monitor/oss/index.vue | 67.27% | 上传、预览、分片依赖 File 对象与预览窗口 |
| HeaderCom.vue | 71.92% | 全屏、搜索、下拉命令依赖浏览器能力 |
| system/role/index.vue | 72.06% | 权限树勾选/回填分支复杂 |
| system/permission/index.vue | 72.47% | 树形新增/编辑弹窗层级较深 |

## 四、复现命令

后端（**必须先切到 JDK 25**，本机默认 JAVA_HOME 为 17）：

```powershell
$env:JAVA_HOME = 'C:\Program Files\BellSoft\LibericaJDK-25'
cd backend
mvn clean test -B
# 覆盖率报告：target/site/jacoco/index.html
```

前端：

```powershell
cd frontend
pnpm exec vitest run --coverage
# 覆盖率报告：coverage/index.html
```

## 五、说明与后续建议

1. 本次仅新增测试与测试基建，**未修改任何 `src/main` 或 `frontend/src` 生产代码**。
2. 后端 `mvn test` 中的 Testcontainers 集成测试需要本地 Docker；无 Docker 时自动跳过，不影响单测与构建。
3. 后续可优先补：后端代码生成器与配置类（需 Spring 上下文）、前端 TabsCom 与 OSS 上传交互；如需强制门禁，可在 JaCoCo `check` 与 Vitest `coverage.thresholds` 中设置 60% 下限。
