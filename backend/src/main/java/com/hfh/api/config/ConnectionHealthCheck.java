package com.hfh.api.config;

import com.zaxxer.hikari.HikariDataSource;
import io.minio.MinioClient;
import io.minio.messages.Bucket;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class ConnectionHealthCheck implements CommandLineRunner {

    private final DataSource dataSource;
    private final RedisTemplate<String, Object> redisTemplate;
    private final MinioClient minioClient;

    @Override
    public void run(String... args) {
        StringBuilder report = new StringBuilder();
        report.append("\n");
        report.append("+------------------------------------------------------------+\n");
        report.append("                    Connectivity Check Report                 \n");
        report.append("+------------------------------------------------------------+\n");

        boolean mysqlOk = checkMySQL(report);
        boolean redisOk = checkRedis(report);
        boolean minioOk = checkMinIO(report);

        report.append("+------------------------------------------------------------+\n");
        if (mysqlOk && redisOk && minioOk) {
            report.append("  Status: All services connected successfully\n");
        } else {
            report.append("  Status: Some services failed, please check the report above\n");
        }
        report.append("+------------------------------------------------------------+\n");

        log.info(report.toString());
    }

    private boolean checkMySQL(StringBuilder report) {
        long start = System.currentTimeMillis();
        try (Connection conn = dataSource.getConnection()) {
            boolean valid = conn.isValid(3);
            long cost = System.currentTimeMillis() - start;
            String url = ((HikariDataSource) dataSource).getJdbcUrl().replaceAll("password=[^&]*", "password=****");
            if (valid) {
                report.append(String.format("  [OK]  MySQL   | %d ms | %s%n", cost, url));
            } else {
                report.append(String.format("  [FAIL] MySQL   | %d ms | Connection invalid%n", cost));
            }
            return valid;
        } catch (Exception e) {
            long cost = System.currentTimeMillis() - start;
            report.append(String.format("  [FAIL] MySQL   | %d ms | %s%n", cost, e.getMessage()));
            return false;
        }
    }

    private boolean checkRedis(StringBuilder report) {
        long start = System.currentTimeMillis();
        try {
            String key = "health:check:" + System.currentTimeMillis();
            redisTemplate.opsForValue().set(key, "pong", 5, TimeUnit.SECONDS);
            String result = (String) redisTemplate.opsForValue().get(key);
            redisTemplate.delete(key);
            long cost = System.currentTimeMillis() - start;
            if ("pong".equals(result)) {
                report.append(String.format("  [OK]  Redis   | %d ms | Read/Write OK%n", cost));
            } else {
                report.append(String.format("  [FAIL] Redis   | %d ms | Data mismatch: %s%n", cost, result));
            }
            return "pong".equals(result);
        } catch (Exception e) {
            long cost = System.currentTimeMillis() - start;
            report.append(String.format("  [FAIL] Redis   | %d ms | %s%n", cost, e.getMessage()));
            return false;
        }
    }

    private boolean checkMinIO(StringBuilder report) {
        long start = System.currentTimeMillis();
        try {
            List<Bucket> buckets = minioClient.listBuckets();
            long cost = System.currentTimeMillis() - start;
            report.append(String.format("  [OK]  MinIO   | %d ms | Service OK, %d bucket(s)%n", cost, buckets.size()));
            return true;
        } catch (Exception e) {
            long cost = System.currentTimeMillis() - start;
            report.append(String.format("  [FAIL] MinIO   | %d ms | %s%n", cost, e.getMessage()));
            return false;
        }
    }
}
