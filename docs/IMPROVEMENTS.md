# single-admin 改进评估清单

- 评估时间：2026-11-14
- 评估范围：后端 `backend/`、前端 `frontend/`、CI `.github/workflows/ci.yml`、部署 `docker-compose.yml`、文档 `README.md`
- 评估方式：静态代码审阅 + 配置核对，未做运行时渗透测试
- 结论：项目在同类后台基座中质量中上（双 token、缓存切面、Flyway 迁移、Trivy 扫描、前后端单元测试均已落地），但存在若干安全与一致性短板，建议按本文优先级推进。

> 说明：本文档由代码审阅结论整理而来，仅记录待改进项，不包含已实现能力的说明。
> 勾选框用于跟踪进度，完成一项即可勾选。

---

## 一、优先级总览

| 优先级 | 主题 | 数量 | 说明 |
| --- | --- | --- | --- |
| P0 | 安全 | 8 | 可被利用或直接暴露面，建议尽快处理 |
| P1 | 正确性与健壮性 | 7 | 会导致 500、数据不一致、部署后行为异常 |
| P2 | 测试与 CI | 4 | 已有基建但未形成强制门禁 |
| P3 | 文档一致性 | 4 | README 与实际能力不符，误导使用者 |
| P4 | 工程化与可维护性 | 5 | 长期演进成本 |

建议落地顺序：**P0-1 定时任务白名单 → P0-2 中间件端口收敛与 nginx 安全头 → P1-1 注册校验与邮箱唯一索引 → P2-1 CI 补前端测试与覆盖率门禁 → P3 文档修正**。前四项改动量都较小，收益最高。

---

## 二、P0 安全问题

### P0-1 定时任务可反射调用任意类（等效内置 RCE）

- [x] 已修复
- 位置：[JobTaskExecutor.java](../backend/src/main/java/com/hfh/api/modules/monitor/task/task/JobTaskExecutor.java)
- 现状：`Class.forName(className)` + `applicationContext.getBean(clazz)` + `Method.invoke` 执行前端填写的 `invokeTarget`，仅靠 `job:add` / `job:update` 权限保护。
- 影响：任何拥有定时任务管理权限的账号（或该账号被盗）都能调用容器内任意 Bean 的公开方法，等同于应用内任意代码执行。
- 修复：新增包白名单校验（`job.invoke.allowed-packages`，默认仅 `com.hfh.api.modules.monitor.task.task`），白名单外的类直接拒绝。
- 建议：
  - 维护允许调用的包名前缀或 Bean 白名单，不在白名单内直接拒绝；
  - 或改为只允许提交实现了统一任务接口（如 `JobTask`）的 Bean，禁止自由填写类名；
  - 同时限制可调用的方法参数类型（当前仅支持 `String`，可保留）。

### P0-2 中间件端口直接发布到宿主机

- [x] 已修复
- 位置：[docker-compose.yml](../docker-compose.yml)
- 现状：MySQL `3306`、Redis `6379`、MinIO `9000/9001` 均以 `${PORT}:PORT` 形式发布到宿主机。
- 影响：生产环境若宿主机有公网 IP 或防火墙配置不严，数据库、缓存、对象存储控制台可直接被访问。
- 修复：MySQL / Redis / MinIO 端口统一绑定 `127.0.0.1`，仅允许宿主机本地访问，容器间仍通过 `webnet` 通信。
- 建议：
  - 生产环境只绑 `127.0.0.1`（如 `127.0.0.1:${MYSQL_PORT}:3306`）或完全移除 `ports`，仅通过 `webnet` 内网访问；
  - MinIO 控制台端口尤其不应对外；
  - 如需本地调试，可用 `docker-compose.override.yml` 单独开放。

### P0-3 Redis 密码以命令行参数传入

- [x] 已修复
- 位置：[docker-compose.yml](../docker-compose.yml)
- 现状：`redis-server --requirepass ${REDIS_PASSWORD}`，healthcheck 也把密码写在命令行。
- 影响：`docker inspect`、`ps -ef`、容器日志中可能看到明文密码。
- 修复：新增 [redis-entrypoint.sh](../docker/redis/redis-entrypoint.sh)，从环境变量生成配置文件后启动；healthcheck 改用 `REDISCLI_AUTH` 环境变量认证。
- 建议：改用配置文件挂载（`redis.conf`）或 Docker secret；healthcheck 改用 `redis-cli -a` 从环境变量读取，或使用 `REDISCLI_AUTH` 环境变量。

### P0-4 登录锁定可被用于锁死任意账号

- [x] 已修复
- 位置：[AuthService.java](../backend/src/main/java/com/hfh/api/modules/auth/service/AuthService.java)
- 现状：登录失败计数与锁定 key 只按账号维度（`login:fail:{account}` / `login:lock:{account}`），未叠加 IP。
- 影响：攻击者对任意已知用户名连续输错 5 次，即可让该账号锁定 30 分钟，形成定向拒绝服务。
- 修复：失败计数与锁定改为按「账号 + IP」维度（`login:fail:{account}:{ip}` / `login:lock:{account}:{ip}`）；新增 IP 跨账号限流（`login:fail:ip:{ip}`）与指数退避（连续锁定 30m → 1h → …，上限 24h）。登录验证码作为可选的后续增强，未在本次实现。
- 建议：
  - 失败计数同时按 IP 维度累计，IP 维度超限直接拒绝；
  - 登录入口增加滑块/图形验证码（现有滑块能力可复用）；
  - 锁定时间采用指数退避。

### P0-5 邮箱验证码可被暴力枚举

- [x] 已修复
- 位置：[CaptchaService.java](../backend/src/main/java/com/hfh/api/modules/auth/service/CaptchaService.java)
- 现状：`verifyEmailCode` 校验失败不计数，验证码 5 分钟有效；`sendEmailCode` 只有发送频率限制。
- 影响：6 位数字验证码在有效期内可被脚本穷举，进而完成注册或邮箱验证。
- 修复：`verifyEmailCode` 增加失败计数（`captcha:email:fail:{email}`），连续失败 5 次后直接作废验证码；调用方保持统一错误提示。
- 建议：增加失败次数计数（如 5 次失败即删除验证码），并在响应中保持统一错误提示。

### P0-6 nginx 无安全响应头、无 HTTPS 配置

- [x] 已修复
- 位置：[default.conf](../frontend/default.conf)
- 现状：仅配置 gzip、缓存与反向代理，未设置任何安全响应头，也没有 HTTPS/TLS 配置。
- 影响：前端 token 存放于 localStorage，一旦出现 XSS，双 token 可被直接读取；缺少 `X-Frame-Options` 还存在点击劫持风险。
- 修复：已补充 `Content-Security-Policy`、`X-Content-Type-Options`、`X-Frame-Options`、`Referrer-Policy`、`Permissions-Policy`、`Strict-Transport-Security` 与 `server_tokens off`；TLS 仍需由外层负载均衡或挂载证书终结。
- 建议：
  - 增加 `Content-Security-Policy`（至少限制 `script-src 'self'`）、`X-Content-Type-Options: nosniff`、`X-Frame-Options: DENY`、`Referrer-Policy: strict-origin-when-cross-origin`；
  - 生产环境补充 HTTPS 与 HSTS（可在外层负载均衡终结 TLS，但需同步配置）；
  - 长期可评估将 token 迁移到 HttpOnly Cookie。

### P0-7 早期的接口文档暴露面

- [x] 已修复
- 位置：[application.yaml](../backend/src/main/resources/application.yaml)
- 现状：Knife4j 在默认配置中 `enable: true` 且 `knife4j.basic.enable: false`。
- 影响：测试环境若可被外网访问，等于公开全部接口清单与参数结构；生产环境已在 `application-prod.yaml` 关闭。
- 修复：基础配置默认关闭 Knife4j 与 springdoc，仅在 `application-dev.yaml` 中开启；test/prod 环境不再暴露接口文档。
- 建议：按 profile 关闭文档或开启 basic 鉴权；确认测试环境是否允许匿名访问 `/doc.html`。

### P0-8 生产数据库使用 root 账号

- [x] 已修复
- 位置：[application-prod.yaml](../backend/src/main/resources/application-prod.yaml)、[docker-compose.yml](../docker-compose.yml)
- 现状：应用连接与 compose 初始化均使用 `root`。
- 影响：一旦应用被注入或凭据泄露，攻击者拥有数据库全部权限（含建库、删库）。
- 修复：compose 通过 `MYSQL_USER` / `MYSQL_PASSWORD` 创建专用账号（默认 `single_admin`，仅拥有业务库权限），后端改用该账号连接，生产配置不再回退 root。
- 建议：创建专用账号，仅授予业务库的 `SELECT/INSERT/UPDATE/DELETE` 权限；Flyway 迁移可单独使用高权限账号。

---

## 三、P1 正确性与健壮性

### P1-1 注册接口缺少参数校验

- [x] 已修复
- 位置：[AuthController.java](../backend/src/main/java/com/hfh/api/modules/auth/controller/AuthController.java)、[RegisterDTO.java](../backend/src/main/java/com/hfh/api/modules/auth/dto/RegisterDTO.java)
- 现状：`register(@RequestBody RegisterDTO dto)` 未加 `@Valid`，DTO 上无任何约束注解。
- 影响：用户名/密码为空时会在 `BCryptPasswordEncoder.encode(null)` 处抛异常，返回 500 而非友好提示；超长用户名/密码会触发数据库异常。
- 修复：DTO 增加 `@NotBlank`、`@Size`、`@Email`、`@Pattern`（密码需含字母和数字）等约束，控制器加 `@Valid`，由全局异常处理返回友好提示；前端注册表单同步密码复杂度规则。
- 建议：补充 `@NotBlank`、`@Size`、`@Email` 等约束，控制器加 `@Valid`；密码增加复杂度校验。

### P1-2 `sys_user.email` 缺少唯一索引

- [x] 已修复
- 位置：[V1__init.sql](../backend/src/main/resources/db/migration/V1__init.sql)
- 现状：`sys_user` 仅有 `username` 唯一索引，邮箱唯一性只在 `AuthService.register` 中应用层查重。
- 影响：并发注册可写入重复邮箱；此后按邮箱登录使用 `selectOne` 会抛 `TooManyResultsException`。
- 修复：新增 [V2__add_user_email_unique.sql](../backend/src/main/resources/db/migration/V2__add_user_email_unique.sql)，先将重复邮箱中除最早记录外置空，再添加唯一索引 `uk_email`。
- 建议：新增 Flyway 迁移脚本 `V2__add_user_email_unique.sql`，先清理重复数据再加唯一索引（MySQL 允许多个 `NULL`）。

### P1-3 部署后客户端 IP 失真

- [x] 已修复
- 位置：[AuthController.java](../backend/src/main/java/com/hfh/api/modules/auth/controller/AuthController.java)、[application.yaml](../backend/src/main/resources/application.yaml)
- 现状：通过 `request.getRemoteAddr()` 获取登录 IP，但未配置 `server.forward-headers-strategy`；nginx 会设置 `X-Real-IP` / `X-Forwarded-For`。
- 影响：Docker 部署下记录的登录 IP、在线用户 IP 均为 nginx 容器地址，审计与排障价值丧失。
- 修复：配置 `server.forward-headers-strategy: framework`，由容器从转发头解析真实客户端 IP。
- 建议：配置 `server.forward-headers-strategy: framework`（或 native），并确认 nginx 透传头正确；对不可信来源仍需做 IP 解析校验。

### P1-4 分页参数无上限，OSS 列表全量加载

- [x] 已修复
- 位置：[OssController.java](../backend/src/main/java/com/hfh/api/modules/monitor/oss/controller/OssController.java) 及各 `page` 接口
- 现状：`size` 未限制最大值；OSS 对象列表先 `listObjects` 拉取全部，再内存 `subList` 分页。
- 影响：桶内文件较多时，单次请求可能拉取数十万条对象，造成内存占用过高、接口超时甚至 OOM。
- 修复：OSS 列表单页 `size` 限制为 1~100，底层列表最多扫描 1 万个对象，避免一次性拉取全量数据；数据库分页已由 MyBatis-Plus 的 `maxLimit` 统一限制。
- 建议：统一限制 `size <= 100`（可在 DTO 或全局参数校验中实现）；OSS 列表改用服务端分页（MinIO/S3 的 `listObjectsV2` 支持 continuation token）。

### P1-5 事务边界写在 Controller 层

- [x] 已修复
- 位置：[RoleController.java](../backend/src/main/java/com/hfh/api/modules/system/controller/RoleController.java)
- 现状：部分多表写操作在 Controller 方法上直接标注 `@Transactional`。
- 影响：事务边界与业务逻辑分离，后续 Controller 复用或加入非事务逻辑时容易产生隐式事务问题。
- 修复：删除角色/批量删除的事务逻辑下沉到 `RoleServiceImpl.deleteRole` / `deleteRoles`，Controller 仅做参数适配与响应包装。
- 建议：将事务下沉到 Service 层，Controller 只负责参数适配与响应包装。

### P1-6 删除语义不统一

- [x] 已修复
- 位置：[BaseEntity.java](../backend/src/main/java/com/hfh/api/base/BaseEntity.java)、`doc_*` 实体
- 现状：只有 `doc_guides` / `doc_feedbacks` 有 `is_deleted` 字段，且未使用 `@TableLogic`；系统模块实体均为物理删除。
- 影响：各模块删除策略不一致，后续新增表容易各自实现；误删无法恢复。
- 修复：统一约定为物理删除，移除 `doc_guides` / `doc_feedbacks` 实体中未生效的 `isDeleted` 字段，避免出现“看似软删除、实则物理删除”的误导；新增模块统一按物理删除实现。
- 建议：统一约定（全物理删除或全逻辑删除），若采用逻辑删除则在 `BaseEntity` 中抽象 `isDeleted` 并配置 `@TableLogic`，同时同步查询条件。

### P1-7 系统配置接口明文返回敏感项

- [x] 已修复
- 位置：[ConfigController.java](../backend/src/main/java/com/hfh/api/modules/system/controller/ConfigController.java)
- 现状：分页查询接口会返回 `oss.secretKey`、`email.sender.password` 等配置的明文值。
- 影响：拥有 `config:query` 权限的账号可直接读取第三方存储/邮箱凭据。
- 修复：分页与详情接口对名称含 `password` / `secret` / `accessKey` / `token` 的配置项统一脱敏为 `******`；更新时提交该占位值则保留数据库原值，避免误覆盖。
- 建议：接口层按配置 key 脱敏（如仅返回 `******`），编辑时通过独立接口校验后更新；或对敏感项单独加密存储。

---

## 四、P2 测试与 CI

### P2-1 CI 未执行前端测试

- [x] 已修复
- 位置：[ci.yml](../.github/workflows/ci.yml)
- 现状：前端 job 只执行 `lint:ci` 与 `build`，未执行 `pnpm test`。
- 影响：本次新增的 294 个前端用例没有流水线保障，回归不会被发现。
- 修复：前端 job 在 build 前执行 `pnpm test:coverage`，并通过 `actions/upload-artifact` 归档 `frontend/coverage`，测试失败即中断流水线。
- 建议：在 build 前增加 `pnpm test:coverage`，并将覆盖率产物归档。

### P2-2 覆盖率只生成不校验

- [x] 已修复
- 位置：[pom.xml](../backend/pom.xml)、[vitest.config.js](../frontend/vitest.config.js)
- 现状：后端 JaCoCo 仅 `report`，未配置 `check`；前端未配置 `coverage.thresholds`。
- 影响：覆盖率会随时间回退且无人察觉。
- 修复：JaCoCo 在 `verify` 阶段增加 BUNDLE 行覆盖率校验（不低于 70%）；前端 `coverage.thresholds` 设置行/语句/分支均不低于 75%，低于门槛时测试失败。
- 建议：基于当前水平设置门槛（后端行覆盖 70%、前端行覆盖 75%），后续逐步提高。

### P2-3 缺少端到端与权限链路集成测试

- [x] 已修复
- 位置：[AuthPermissionIntegrationTest.java](../backend/src/test/java/com/hfh/api/AuthPermissionIntegrationTest.java)、[playwright.config.js](../frontend/playwright.config.js)、[ci.yml](../.github/workflows/ci.yml)
- 现状：Testcontainers 仅覆盖一个应用启动用例；登录 → 鉴权 → 无权限拒绝这条核心链路没有集成或 E2E 测试。
- 影响：权限注解漏加、路由与权限不一致等问题只能靠人工发现。
- 修复：后端新增 Testcontainers 集成测试，覆盖未登录 401、管理员放行、无权限 403、密码错误 400；前端引入 Playwright，覆盖登录、菜单渲染、按钮级权限三条主流程（接口用路由桩 mock，不依赖真实后端），CI 新增独立 e2e job 并归档报告。同时修复两处既存 lint 错误，保证门禁可用。
- 建议：
  - 后端补充基于 Testcontainers 的登录与接口鉴权集成测试；
  - 前端引入 Playwright，覆盖登录、菜单渲染、按钮级权限三条主流程。

### P2-4 依赖与镜像安全扫描不完整

- [x] 已修复
- 位置：[ci.yml](../.github/workflows/ci.yml)、[dependabot.yml](../.github/dependabot.yml)
- 现状：Trivy 仅扫描文件系统（`scan-type: fs`），未扫描构建产物镜像；无依赖自动升级。
- 影响：基础镜像与运行时依赖的漏洞不会被发现。
- 修复：CI 新增 `image-scan` job，构建前后端镜像后以 `scan-type: image` 扫描 CRITICAL/HIGH 漏洞与密钥，发现即失败；新增 Dependabot 配置，每周检查 Maven、pnpm、GitHub Actions 及两个 Dockerfile 基础镜像。
- 建议：增加镜像扫描（`scan-type: image`）；接入 Dependabot 或 Renovate 定期升级依赖。

---

## 五、P3 文档一致性

### P3-1 README 声称具备操作日志能力，实际不存在

- [ ] 已修复
- 位置：[README.md](../README.md)
- 现状：README 写明"操作日志：登录日志与操作日志记录，支持审计与查询"，但代码与建表 SQL 中没有 `sys_log` / `login_log` 表，也没有日志切面。
- 影响：使用者按文档评估能力时会产生误判。
- 建议：二选一——删除该能力描述，或补齐登录日志/操作日志模块（表、切面、查询接口、前端页面）。

### P3-2 README 声称具备部门与岗位管理

- [ ] 已修复
- 位置：[README.md](../README.md)
- 现状：实际只有 `sys_institutions`（机构），没有岗位表与接口。
- 建议：修正为"机构管理"，或补齐岗位模块。

### P3-3 README 项目结构包含不存在的目录

- [ ] 已修复
- 位置：[README.md](../README.md)
- 现状：结构图中列出 `mysql/`、`redis/`、`minio/`，但这些目录并不存在（`docs/` 已作为项目文档目录落地，问题已关闭）。
- 建议：删除结构图中的 `mysql/`、`redis/`、`minio/`，或按实际部署方式补充说明。

### P3-4 代码生成器能力与文档描述不符

- [ ] 已修复
- 位置：[GenCustomTemplate.java](../backend/src/main/java/com/hfh/api/generator/GenCustomTemplate.java)
- 现状：README 将其列为平台能力，但只能通过命令行手动执行，无 Web 入口、无权限控制，且无测试覆盖。
- 建议：明确文档说明为"命令行工具"；如需平台化，再补充接口与页面。

---

## 六、P4 工程化与可维护性

### P4-1 默认激活 dev profile

- [ ] 已修复
- 位置：[application.yaml](../backend/src/main/resources/application.yaml)
- 现状：`spring.profiles.active: dev` 写在基础配置中。
- 影响：直接 `java -jar` 且未指定 profile 时，会以开发配置启动（包含 SQL stdout 日志）。
- 建议：移除默认值，由启动参数或环境变量显式指定；Docker 已在 compose 中注入，不受影响。

### P4-2 缺少统一 DTO/VO 转换策略

- [ ] 已修复
- 位置：各 `modules/**/controller`
- 现状：大量使用 `BeanUtils.copyProperties` 手工映射，且存在字段漏拷风险。
- 建议：引入 MapStruct 或统一转换器；或让实体直接作为查询结果时明确 `@JsonIgnore` 敏感字段（当前 `UserEntity.password` 已处理）。

### P4-3 响应统一为 HTTP 200 + 业务 code

- [ ] 已修复
- 位置：[ExceptionHandle.java](../backend/src/main/java/com/hfh/api/exception/ExceptionHandle.java)
- 现状：所有异常均返回 HTTP 200，401/403/500 只体现在响应体 code。
- 影响：网关、APM、负载均衡无法按状态码做告警、熔断与重试。
- 建议：至少 401/403/500 返回真实 HTTP 状态码，前端拦截器同时兼容。

### P4-4 前端未使用 TypeScript

- [ ] 已修复
- 位置：`frontend/`
- 现状：全部为 JS，仅有 `jsconfig.json`。
- 影响：接口字段变更缺乏编译期保护，重构成本较高。
- 建议：新模块优先使用 TS，或按模块渐进迁移（先 `stores`、`api`、`utils`）。

### P4-5 缺少发布与变更管理流程

- [ ] 已修复
- 位置：`.github/workflows/`
- 现状：只有 CI，没有镜像构建推送、版本 Tag、CHANGELOG 或回滚流程。
- 建议：增加 release workflow（构建并推送镜像、生成变更记录）；明确数据库迁移的回滚策略（Flyway 社区版不支持自动回滚，需提供逆向脚本）。

---

## 七、建议推进计划

| 阶段 | 内容 | 验收标准 |
| --- | --- | --- |
| 第一阶段 | P0-1、P0-2、P0-3、P0-7 | 定时任务无法调用白名单外的类；生产 compose 不对外暴露中间件端口；文档接口不再匿名可访问 |
| 第二阶段 | P0-4、P0-5、P0-6、P0-8 | 登录具备 IP 维度限流与验证码；邮箱验证码失败计数生效；nginx 返回安全头；应用使用专用数据库账号 |
| 第三阶段 | P1-1 ~ P1-7 | 注册参数校验生效；邮箱唯一索引上线；IP 记录正确；分页有上限；事务下沉；删除策略统一；敏感配置脱敏 |
| 第四阶段 | P2-1 ~ P2-4 | CI 前端测试与覆盖率门禁生效；核心链路有集成/E2E 测试；镜像纳入漏洞扫描 |
| 第五阶段 | P3、P4 | README 与实际一致；默认 profile、响应状态码、TS 迁移等按计划推进 |

---

## 八、评估局限

1. 本次为静态审阅，未执行依赖漏洞扫描、渗透测试或性能压测。
2. 未逐一验证所有接口的权限注解覆盖情况，建议结合自动化测试补齐。
3. 前端 `functions` 覆盖率（46.44%）受模板编译产物影响，不代表业务逻辑覆盖不足；后续以行/分支覆盖率为准。
4. 后端 Testcontainers 用例在无 Docker 环境会自动跳过，本地验证覆盖率时需注意。
