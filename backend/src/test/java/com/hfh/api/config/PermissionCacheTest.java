package com.hfh.api.config;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 权限缓存的本地读写与失效逻辑测试。
 */
class PermissionCacheTest {

    private final PermissionCache cache = new PermissionCache();

    @Test
    void shouldReturnNullOrEmptyForNullOrUnknownUser() {
        assertThat(cache.getPermissions(null)).isNull();
        assertThat(cache.getRoles(null)).isNull();
        assertThat(cache.getPermissions(1L)).isNull();
        assertThat(cache.getRoles(1L)).isNull();
    }

    @Test
    void shouldStorePermissionsAndRolesIndependently() {
        cache.putPermissions(1L, List.of("user:query"));
        cache.putRoles(1L, List.of("admin"));

        assertThat(cache.getPermissions(1L)).containsExactly("user:query");
        assertThat(cache.getRoles(1L)).containsExactly("admin");
    }

    @Test
    void shouldCopyValuesToAvoidExternalMutation() {
        List<String> source = new ArrayList<>(List.of("user:query"));
        cache.putPermissions(1L, source);

        source.add("user:delete");

        assertThat(cache.getPermissions(1L)).containsExactly("user:query");
        assertThatThrownBy(() -> cache.getPermissions(1L).add("x"))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void evictUserShouldOnlyClearThatUser() {
        cache.putPermissions(1L, List.of("a"));
        cache.putRoles(1L, List.of("r1"));
        cache.putPermissions(2L, List.of("b"));

        cache.evictUser(1L);
        cache.evictUser(null);

        assertThat(cache.getPermissions(1L)).isNull();
        assertThat(cache.getRoles(1L)).isNull();
        assertThat(cache.getPermissions(2L)).containsExactly("b");
    }

    @Test
    void evictAllShouldClearEverything() {
        cache.putPermissions(1L, List.of("a"));
        cache.putRoles(2L, List.of("r2"));

        cache.evictAll();

        assertThat(cache.getPermissions(1L)).isNull();
        assertThat(cache.getRoles(2L)).isNull();
    }
}
