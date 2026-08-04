package com.hfh.api.modules.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hfh.api.modules.system.entity.InstitutionsEntity;
import com.hfh.api.modules.system.mapper.InstitutionsMapper;
import com.hfh.api.modules.system.service.InstitutionsService;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class InstitutionsServiceImpl extends ServiceImpl<InstitutionsMapper, InstitutionsEntity> implements InstitutionsService {

    @Override
    public List<InstitutionsEntity> listTree() {
        List<InstitutionsEntity> allInstitutions = baseMapper.selectList(null);
        return buildInstitutionsTree(allInstitutions);
    }

    @Override
    public List<Long> getChildInstitutionIds(Long parentId) {
        List<InstitutionsEntity> allInstitutions = baseMapper.selectList(null);
        List<Long> result = new ArrayList<>();
        collectChildIds(allInstitutions, parentId, result);
        return result;
    }

    @Override
    public List<Long> getAccessibleInstitutionIds(Long institutionId) {
        if (institutionId == null) {
            return Collections.emptyList();
        }
        List<Long> ids = new ArrayList<>();
        ids.add(institutionId);
        ids.addAll(getChildInstitutionIds(institutionId));
        return ids;
    }

    private void collectChildIds(List<InstitutionsEntity> allInstitutions, Long parentId, List<Long> result) {
        for (InstitutionsEntity institution : allInstitutions) {
            if (parentId.equals(institution.getParentId())) {
                result.add(institution.getId());
                collectChildIds(allInstitutions, institution.getId(), result);
            }
        }
    }

    private List<InstitutionsEntity> buildInstitutionsTree(List<InstitutionsEntity> allInstitutions) {
        if (allInstitutions == null || allInstitutions.isEmpty()) {
            return allInstitutions;
        }

        Map<Long, InstitutionsEntity> institutionMap = allInstitutions.stream()
                .collect(Collectors.toMap(InstitutionsEntity::getId, institution -> institution));

        List<InstitutionsEntity> rootInstitutions = new ArrayList<>();

        for (InstitutionsEntity institution : allInstitutions) {
            Long parentId = institution.getParentId();

            if (parentId == null || parentId == 0) {
                rootInstitutions.add(institution);
            } else {
                InstitutionsEntity parent = institutionMap.get(parentId);
                if (parent != null) {
                    if (parent.getChildren() == null) {
                        parent.setChildren(new ArrayList<>());
                    }
                    parent.getChildren().add(institution);
                }
            }
        }

        return rootInstitutions;
    }

}
