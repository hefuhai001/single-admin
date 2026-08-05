package com.hfh.api.modules.system.service.impl;

import com.hfh.api.modules.system.cache.DictCacheService;
import com.hfh.api.modules.system.entity.DictDataEntity;
import com.hfh.api.modules.system.mapper.DictDataMapper;
import com.hfh.api.modules.system.service.DictDataService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author xiaoHe
 * @since 2026-08-05
 */
@Service
@RequiredArgsConstructor
public class DictDataServiceImpl extends ServiceImpl<DictDataMapper, DictDataEntity> implements DictDataService {

    private final DictCacheService dictCacheService;

    @Override
    public List<DictDataEntity> listByDictType(String dictType) {
        return dictCacheService.getDictDataList(dictType);
    }
}
