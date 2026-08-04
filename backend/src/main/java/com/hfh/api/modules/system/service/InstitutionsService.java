package com.hfh.api.modules.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hfh.api.modules.system.entity.InstitutionsEntity;

import java.util.List;

public interface InstitutionsService extends IService<InstitutionsEntity> {

    List<InstitutionsEntity> listTree();

    List<Long> getChildInstitutionIds(Long parentId);

    List<Long> getAccessibleInstitutionIds(Long institutionId);
}
