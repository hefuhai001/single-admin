package com.hfh.api.base;

import cn.dev33.satoken.stp.StpUtil;
import com.hfh.api.modules.system.entity.UserEntity;
import com.hfh.api.modules.system.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;

public class BaseController {

    @Autowired
    private UserService userService;

    /**
     * 获取登录用户全部信息
     *
     * @return 登录用户
     */
    protected UserEntity getCurrentUser() {
        if (!isUserLoggedIn()) {
            return null;
        }
        Long userId = StpUtil.getLoginIdAsLong();
        return userService.getById(userId);
    }


    /**
     * 检查用户是否已登录
     *
     * @return 是否已登录
     */
    protected boolean isUserLoggedIn() {
        return StpUtil.isLogin();
    }


    protected String getCurrentUsername() {
        UserEntity user = getCurrentUser();
        return user != null ? user.getUsername() : null;
    }


    /**
     * 获取当前登录用户的ID
     *
     * @return 当前登录用户的ID，如果未登录则返回null
     */
    protected Long getCurrentUserId() {
        if (StpUtil.isLogin()) {
            Object loginId = StpUtil.getLoginId();
            if (loginId instanceof Long) {
                return (Long) loginId;
            } else if (loginId instanceof String) {
                try {
                    return Long.parseLong((String) loginId);
                } catch (NumberFormatException e) {
                    return null;
                }
            } else if (loginId instanceof Integer) {
                return ((Integer) loginId).longValue();
            }
        }
        return null;
    }

    /**
     * 获取当前登录用户的ID（带异常处理）
     *
     * @return 当前登录用户的ID
     * @throws cn.dev33.satoken.exception.NotLoginException 如果用户未登录
     */
    protected Long getCurrentUserIdOrFail() {
        Object loginId = StpUtil.getLoginId();
        if (loginId instanceof Long) {
            return (Long) loginId;
        } else if (loginId instanceof String) {
            try {
                return Long.parseLong((String) loginId);
            } catch (NumberFormatException e) {
                throw new RuntimeException("无法解析用户ID: " + loginId);
            }
        } else if (loginId instanceof Integer) {
            return ((Integer) loginId).longValue();
        }
        throw new RuntimeException("未知的用户ID类型: " + loginId.getClass());
    }


}
