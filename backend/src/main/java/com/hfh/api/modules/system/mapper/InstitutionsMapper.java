package com.hfh.api.modules.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hfh.api.modules.system.entity.InstitutionsEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 机构表(支持父子层级) Mapper 接口
 * </p>
 *
 * @author xiaoHe
 * @since 2026-04-15
 */
@Mapper
public interface InstitutionsMapper extends BaseMapper<InstitutionsEntity> {

}
