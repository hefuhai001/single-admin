package com.hfh.api.config;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 用户角色 / 权限的本地缓存。
 * <p>
 * Sa-Token 每次鉴权都会调用 {@link StpInterfaceImpl} 查询数据库，
 * 缓存可避免高频接口反复 JOIN 三张表。TTL 兜底保证最终一致，
 * 角色、权限分配变更时调用 {@link #evictUser(Long)} / {@link #evictAll()} 主动失效。
 * </p>
 */
@Component
public class PermissionCache {

    /**
     * 缓存有效期（毫秒）：即使漏掉某个失效点，变更最迟 2 分钟后生效
     */
    private static final long TTL_MILLIS = 2 * 60 * 1000L;

    private record Entry(List<String> values, long expireAt) {
        boolean expired() {
            return System.currentTimeMillis() > expireAt;
        }
    }

    private final Map<Long, Entry> permissions = new ConcurrentHashMap<>();
    private final Map<Long, Entry> roles = new ConcurrentHashMap<>();

    public List<String> getPermissions(Long userId) {
        return get(permissions, userId);
    }

    public void putPermissions(Long userId, List<String> values) {
        permissions.put(userId, new Entry(copyOf(values), System.currentTimeMillis() + TTL_MILLIS));
    }

    public List<String> getRoles(Long userId) {
        return get(roles, userId);
    }

    public void putRoles(Long userId, List<String> values) {
        roles.put(userId, new Entry(copyOf(values), System.currentTimeMillis() + TTL_MILLIS));
    }

    /**
     * 失效单个用户的缓存（用户角色变更、用户删除时调用）
     */
    public void evictUser(Long userId) {
        if (userId == null) {
            return;
        }
        permissions.remove(userId);
        roles.remove(userId);
    }

    /**
     * 失效全部缓存（角色权限变更、角色删除时调用）
     */
    public void evictAll() {
        permissions.clear();
        roles.clear();
    }

    private List<String> get(Map<Long, Entry> cache, Long userId) {
        if (userId == null) {
            return null;
        }
        Entry entry = cache.get(userId);
        if (entry == null) {
            return null;
        }
        if (entry.expired()) {
            cache.remove(userId);
            return null;
        }
        return entry.values();
    }

    private List<String> copyOf(List<String> values) {
        if (values == null || values.isEmpty()) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(new ArrayList<>(values));
    }
}
