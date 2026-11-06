package com.hfh.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 集成测试：使用真实的 MySQL / Redis 容器启动完整上下文，
 * 同时验证 Flyway 迁移脚本可以在空库上建表并写入初始化数据。
 * <p>
 * 本机没有 Docker 时整类自动跳过，不影响本地编译。
 * </p>
 */
@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
class BackendApplicationTests {

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
    private JdbcTemplate jdbcTemplate;

    @Test
    void contextLoads() {
    }

    @Test
    void flywayMigrationCreatesTablesAndSeedData() {
        Integer userCount = jdbcTemplate.queryForObject("select count(*) from sys_user", Integer.class);
        assertThat(userCount).isPositive();

        Integer permissionCount = jdbcTemplate.queryForObject("select count(*) from sys_permission", Integer.class);
        assertThat(permissionCount).isPositive();

        Integer adminCount = jdbcTemplate.queryForObject(
                "select count(*) from sys_user where username = 'admin'", Integer.class);
        assertThat(adminCount).isEqualTo(1);
    }

}
