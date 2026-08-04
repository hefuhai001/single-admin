package com.hfh.api.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class RedisUtil {
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    // 存储数据
    public void set(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    // 存储数据并设置过期时间
    public void setWithExpire(String key, Object value, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    // 获取数据
    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    // 删除数据
    public void delete(String key) {
        redisTemplate.delete(key);
    }

    // 向列表中添加元素
    public void addToList(String key, Object value) {
        redisTemplate.opsForList().rightPush(key, value);
    }

    // 从列表中获取所有元素
    public List<Object> getAllFromList(String key) {
        return redisTemplate.opsForList().range(key, 0, -1);
    }


}
