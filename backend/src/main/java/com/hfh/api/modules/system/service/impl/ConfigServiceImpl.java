package com.hfh.api.modules.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hfh.api.modules.system.entity.ConfigEntity;
import com.hfh.api.modules.system.mapper.ConfigMapper;
import com.hfh.api.modules.system.service.ConfigService;
import org.springframework.stereotype.Service;

/**
 * 系统配置服务实现
 */
@Service
public class ConfigServiceImpl extends ServiceImpl<ConfigMapper, ConfigEntity> implements ConfigService {

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
        QueryWrapper<ConfigEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("config_key", configKey);
        wrapper.eq("status", 1);
        wrapper.last("limit 1");
        return getOne(wrapper);
    }
}
