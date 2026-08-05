package com.hfh.api.modules.system.service;

import com.hfh.api.modules.system.entity.DictDataEntity;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author xiaoHe
 * @since 2026-08-05
 */
public interface DictDataService extends IService<DictDataEntity> {

    /**
     * 根据字典类型获取字典数据（优先读取 Redis 缓存）
     */
    List<DictDataEntity> listByDictType(String dictType);
}
