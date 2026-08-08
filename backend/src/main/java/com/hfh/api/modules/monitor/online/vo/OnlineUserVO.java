package com.hfh.api.modules.monitor.online.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 在线用户视图对象
 */
@Data
public class OnlineUserVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 设备标识
     */
    private String deviceId;

    /**
     * 登录IP地址
     */
    private String loginIp;

    /**
     * 登录时间
     */
    private Date loginTime;

    /**
     * accessToken
     */
    private String token;

}
