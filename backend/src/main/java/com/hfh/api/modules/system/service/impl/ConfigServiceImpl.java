package com.hfh.api.modules.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hfh.api.modules.system.annotation.RefreshConfigCache;
import com.hfh.api.modules.system.cache.ConfigCacheService;
import com.hfh.api.modules.system.entity.ConfigEntity;
import com.hfh.api.modules.system.mapper.ConfigMapper;
import com.hfh.api.modules.system.service.ConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.Collection;

/**
 * 系统配置服务实现
 */
@Service
@RequiredArgsConstructor
public class ConfigServiceImpl extends ServiceImpl<ConfigMapper, ConfigEntity> implements ConfigService {

    private final ConfigCacheService configCacheService;

    @Override
    public String getConfigValue(String configKey) {
        ConfigEntity config = getConfigByKey(configKey);
        return config != null ? config.getConfigValue() : null;
    }

    @Override
    public String getConfigValue(String configKey, String defaultValue) {
        String value = getConfigValue(configKey);
        return value != null ? value : defaultValue;
    }

    @Override
    public ConfigEntity getConfigByKey(String configKey) {
        ConfigEntity config = configCacheService.getConfigByKey(configKey);
        if (config != null && Boolean.TRUE.equals(config.getStatus())) {
            return config;
        }
        return null;
    }

    @Override
    @RefreshConfigCache
    public boolean save(ConfigEntity entity) {
        return super.save(entity);
    }

    @Override
    @RefreshConfigCache
    public boolean updateById(ConfigEntity entity) {
        return super.updateById(entity);
    }

    @Override
    @RefreshConfigCache
    public boolean removeById(Serializable id) {
        return super.removeById(id);
    }

    @Override
    @RefreshConfigCache
    public boolean deleteByIds(Collection<? extends Serializable> idList) {
        return super.removeByIds(idList);
    }
}
