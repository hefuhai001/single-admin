package com.hfh.api.modules.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hfh.api.modules.system.entity.ConfigEntity;

import java.io.Serializable;
import java.util.Collection;

/**
 * 系统配置服务接口
 * <p>
 * 使用方式：
 * <pre>
 *     // 注入服务
 *     @Autowired
 *     private ConfigService configService;
 *
 *     // 根据 configKey 获取配置值
 *     String value = configService.getConfigValue("site.name");
 *
 *     // 获取配置值，不存在时返回默认值
 *     String value = configService.getConfigValue("site.name", "默认值");
 *
 *     // 获取完整配置对象
 *     ConfigEntity config = configService.getConfigByKey("site.name");
 * </pre>
 * </p>
 */
public interface ConfigService extends IService<ConfigEntity> {

    /**
     * 根据 configKey 获取 configValue
     *
     * @param configKey 配置键
     * @return 配置值，不存在返回 null
     */
    String getConfigValue(String configKey);

    /**
     * 根据 configKey 获取 configValue，不存在返回默认值
     *
     * @param configKey    配置键
     * @param defaultValue 默认值
     * @return 配置值
     */
    String getConfigValue(String configKey, String defaultValue);

    /**
     * 根据 configKey 获取完整配置对象
     *
     * @param configKey 配置键
     * @return 配置对象
     */
    ConfigEntity getConfigByKey(String configKey);

    /**
     * 根据 ID 批量删除配置，删除成功后刷新缓存
     *
     * @param idList ID 集合
     * @return 是否成功
     */
    boolean deleteByIds(Collection<? extends Serializable> idList);
}
