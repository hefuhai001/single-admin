package com.hfh.api.modules.system.aspect;

import com.hfh.api.modules.system.annotation.RefreshDictCache;
import com.hfh.api.modules.system.cache.DictCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * 字典缓存刷新切面
 * 拦截标注了 @RefreshDictCache 的方法，执行成功后刷新字典缓存
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class DictCacheAspect {

    private final DictCacheService dictCacheService;

    @AfterReturning("@annotation(refreshDictCache)")
    public void afterReturning(RefreshDictCache refreshDictCache) {
        dictCacheService.refreshAll();
    }
}
