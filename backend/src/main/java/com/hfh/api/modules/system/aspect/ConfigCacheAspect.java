package com.hfh.api.modules.system.aspect;

import com.hfh.api.modules.system.annotation.RefreshConfigCache;
import com.hfh.api.modules.system.cache.ConfigCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * 系统配置缓存刷新切面
 * 拦截标注了 @RefreshConfigCache 的方法，执行成功后刷新配置缓存
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class ConfigCacheAspect {

    private final ConfigCacheService configCacheService;

    @AfterReturning("@annotation(refreshConfigCache)")
    public void afterReturning(RefreshConfigCache refreshConfigCache) {
        configCacheService.refreshAll();
    }
}
