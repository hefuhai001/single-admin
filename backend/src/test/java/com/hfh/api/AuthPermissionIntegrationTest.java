package com.hfh.api;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 集成测试：覆盖「登录 → 鉴权 → 无权限拒绝」核心链路。
 * <p>
 * 使用真实 MySQL / Redis 容器启动完整上下文，通过 MockMvc 走完整的 Sa-Token 拦截器与
 * 全局异常处理，验证：
 * <ul>
 *     <li>未携带 token 访问受保护接口返回 401；</li>
 *     <li>管理员账号登录后可访问需要 user:query 权限的接口；</li>
 *     <li>无角色账号登录后访问同一接口返回 403（权限注解生效）；</li>
 *     <li>密码错误返回业务错误码 400，而不是 500。</li>
 * </ul>
 * 本机没有 Docker 时整类自动跳过，不影响本地编译。
 * </p>
 */
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true)
class AuthPermissionIntegrationTest {

    /** 测试账号统一密码明文 */
    private static final String PASSWORD = "E2ePass123";

    /** 拥有超级管理员角色（role_id = 1）的测试账号 */
    private static final String ADMIN_USER = "e2e_admin";

    /** 未分配任何角色的测试账号 */
    private static final String LIMITED_USER = "e2e_limited";

    @Container
    static final MySQLContainer<?> MYSQL = new MySQLContainer<>(DockerImageName.parse("mysql:8.0.45"))
            .withDatabaseName("db_single_admin")
            .withUsername("test")
            .withPassword("test");

    @Container
    static final GenericContainer<?> REDIS = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MYSQL::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL::getUsername);
        registry.add("spring.datasource.password", MYSQL::getPassword);
        registry.add("spring.data.redis.host", REDIS::getHost);
        registry.add("spring.data.redis.port", () -> REDIS.getMappedPort(6379));
        registry.add("spring.data.redis.password", () -> "");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void prepareAccounts() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        // 清理上次运行留下的测试账号，保证用例可重复执行
        jdbcTemplate.update("delete from sys_user_role where user_id in "
                + "(select id from sys_user where username in (?, ?))", ADMIN_USER, LIMITED_USER);
        jdbcTemplate.update("delete from sys_user where username in (?, ?)", ADMIN_USER, LIMITED_USER);

        // 管理员账号：绑定 role_id = 1（超级管理员，具备全部权限）
        insertUser(ADMIN_USER, encoder.encode(PASSWORD));
        Long adminId = jdbcTemplate.queryForObject(
                "select id from sys_user where username = ?", Long.class, ADMIN_USER);
        jdbcTemplate.update("insert into sys_user_role (user_id, role_id) values (?, 1)", adminId);

        // 受限账号：不分配任何角色，权限列表为空
        insertUser(LIMITED_USER, encoder.encode(PASSWORD));
    }

    private void insertUser(String username, String passwordHash) {
        jdbcTemplate.update(
                "insert into sys_user (institution_id, username, password, nickname, email, status) "
                        + "values (null, ?, ?, ?, ?, 1)",
                username, passwordHash, username, username + "@example.com");
    }

    private String login(String username, String password) throws Exception {
        String body = mockMvc.perform(post("/auth/login")
                        .param("username", username)
                        .param("password", password)
                        .param("deviceId", "e2e-device"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn()
                .getResponse()
                .getContentAsString();
        return JsonPath.read(body, "$.data.accessToken");
    }

    @Test
    void unauthenticatedRequestIsRejected() throws Exception {
        mockMvc.perform(get("/admin/user/page"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    void adminCanAccessPermissionProtectedEndpoint() throws Exception {
        String token = login(ADMIN_USER, PASSWORD);

        mockMvc.perform(get("/admin/user/page")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void userWithoutPermissionIsForbidden() throws Exception {
        String token = login(LIMITED_USER, PASSWORD);

        mockMvc.perform(get("/admin/user/page")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403))
                .andExpect(jsonPath("$.msg").value(containsString("user:query")));
    }

    @Test
    void wrongPasswordReturnsBusinessError() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .param("username", LIMITED_USER)
                        .param("password", "wrong-password")
                        .param("deviceId", "e2e-device"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }
}
