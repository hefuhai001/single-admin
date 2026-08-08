package com.hfh.api.modules.monitor.online.service;

import com.hfh.api.modules.monitor.online.vo.OnlineUserVO;

import java.util.List;

/**
 * 在线用户服务接口
 */
public interface OnlineUserService {

    /**
     * 获取在线用户列表
     *
     * @return 在线用户列表
     */
    List<OnlineUserVO> listOnlineUsers();

    /**
     * 强制指定 token 下线
     *
     * @param token accessToken
     */
    void kickByToken(String token);
}
