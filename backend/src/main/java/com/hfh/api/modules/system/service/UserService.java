package com.hfh.api.modules.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hfh.api.modules.system.entity.UserEntity;
import com.hfh.api.modules.system.vo.RouteVO;

import java.util.List;

/**
 * <p>
 * 用户表 服务类
 * </p>
 *
 * @author xiaoHe
 * @since 2026-04-01
 */
public interface UserService extends IService<UserEntity> {

    List<RouteVO> getUserRoutes(Long userId);
    
    List<String> getUserPermissions(Long userId);
}
