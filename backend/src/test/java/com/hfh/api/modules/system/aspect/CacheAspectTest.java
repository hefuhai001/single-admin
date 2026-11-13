package com.hfh.api.modules.system.aspect;

import com.hfh.api.modules.system.annotation.RefreshConfigCache;
import com.hfh.api.modules.system.annotation.RefreshDictCache;
import com.hfh.api.modules.system.cache.ConfigCacheService;
import com.hfh.api.modules.system.cache.DictCacheService;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class CacheAspectTest {

    @Test
    void configAspectShouldRefresh() {
        ConfigCacheService cacheService = mock(ConfigCacheService.class);
        ConfigCacheAspect aspect = new ConfigCacheAspect(cacheService);

        aspect.afterReturning(mock(RefreshConfigCache.class));

        verify(cacheService).refreshAll();
    }

    @Test
    void dictAspectShouldRefresh() {
        DictCacheService cacheService = mock(DictCacheService.class);
        DictCacheAspect aspect = new DictCacheAspect(cacheService);

        aspect.afterReturning(mock(RefreshDictCache.class));

        verify(cacheService).refreshAll();
    }
}
