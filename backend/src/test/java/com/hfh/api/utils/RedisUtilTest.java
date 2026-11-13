package com.hfh.api.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RedisUtilTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;
    @Mock
    private ValueOperations<String, Object> valueOperations;
    @Mock
    private ListOperations<String, Object> listOperations;

    private RedisUtil redisUtil;

    @BeforeEach
    void setUp() {
        redisUtil = new RedisUtil();
        ReflectionTestUtils.setField(redisUtil, "redisTemplate", redisTemplate);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(redisTemplate.opsForList()).thenReturn(listOperations);
    }

    @Test
    void setAndGetShouldDelegate() {
        when(valueOperations.get("k")).thenReturn("v");

        redisUtil.set("k", "v");
        redisUtil.setWithExpire("k2", "v2", 10, TimeUnit.SECONDS);

        verify(valueOperations).set("k", "v");
        verify(valueOperations).set("k2", "v2", 10, TimeUnit.SECONDS);
        assertThat(redisUtil.get("k")).isEqualTo("v");
    }

    @Test
    void deleteShouldDelegate() {
        redisUtil.delete("k");

        verify(redisTemplate).delete("k");
    }

    @Test
    void listOperationsShouldDelegate() {
        when(listOperations.range("list", 0, -1)).thenReturn(List.of("a", "b"));

        redisUtil.addToList("list", "a");
        List<Object> all = redisUtil.getAllFromList("list");

        verify(listOperations).rightPush("list", "a");
        assertThat(all).containsExactly("a", "b");
    }
}
