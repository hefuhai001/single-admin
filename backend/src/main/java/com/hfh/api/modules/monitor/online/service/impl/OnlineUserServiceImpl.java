package com.hfh.api.modules.monitor.online.service.impl;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.session.TokenSign;
import cn.dev33.satoken.stp.StpUtil;
import com.hfh.api.modules.monitor.online.service.OnlineUserService;
import com.hfh.api.modules.monitor.online.vo.OnlineUserVO;
import com.hfh.api.modules.system.entity.UserEntity;
import com.hfh.api.modules.system.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 在线用户服务实现
 */
@Service
@RequiredArgsConstructor
public class OnlineUserServiceImpl implements OnlineUserService {

    private final UserService userService;

    @Override
    public List<OnlineUserVO> listOnlineUsers() {
        List<OnlineUserVO> list = new ArrayList<>();

        // 搜索所有登录会话，每次扫描 1000 条
        List<String> sessionIds = StpUtil.searchSessionId("", 0, 1000, false);
        if (sessionIds == null || sessionIds.isEmpty()) {
            return list;
        }

        for (String sessionId : sessionIds) {
            SaSession session = StpUtil.getSessionBySessionId(sessionId);
            if (session == null) {
                continue;
            }
            Object loginId = session.getLoginId();
            if (loginId == null) {
                continue;
            }

            // 遍历该账号下的 tokenSign，每个 accessToken 视为一个在线设备
            for (TokenSign tokenSign : session.getTokenSignList()) {
                String device = tokenSign.getDevice();
                // 跳过 refreshToken
                if (device != null && device.startsWith("refresh-")) {
                    continue;
                }

                OnlineUserVO vo = new OnlineUserVO();
                vo.setUserId(parseUserId(loginId));
                vo.setUsername(getUsername(loginId));
                vo.setNickname(getNickname(loginId));
                vo.setDeviceId(device);
                vo.setLoginIp("-");
                vo.setLoginTime(new Date(session.getCreateTime()));
                vo.setToken(tokenSign.getValue());
                list.add(vo);
            }
        }

        return list;
    }

    @Override
    public void kickByToken(String token) {
        if (token == null || token.isEmpty()) {
            throw new IllegalArgumentException("token 不能为空");
        }

        // 获取该 token 的 Token-Session，尝试读取对应的 refreshToken
        SaSession tokenSession = StpUtil.getTokenSessionByToken(token);
        if (tokenSession != null) {
            Object refreshToken = tokenSession.get("refreshToken");
            if (refreshToken != null && !refreshToken.toString().isEmpty()) {
                StpUtil.logoutByTokenValue(refreshToken.toString());
            }
        }

        // 同时从 Account-Session 中查找该设备对应的 refreshToken 并注销
        Object loginId = StpUtil.getLoginIdByToken(token);
        if (loginId != null) {
            SaSession accountSession = StpUtil.getSessionByLoginId(loginId, false);
            if (accountSession != null) {
                String device = null;
                for (TokenSign tokenSign : accountSession.getTokenSignList()) {
                    if (token.equals(tokenSign.getValue())) {
                        device = tokenSign.getDevice();
                        break;
                    }
                }
                if (device != null && !device.isEmpty()) {
                    String refreshDevice = "refresh-" + device;
                    for (TokenSign tokenSign : accountSession.getTokenSignList()) {
                        if (refreshDevice.equals(tokenSign.getDevice())) {
                            StpUtil.logoutByTokenValue(tokenSign.getValue());
                            break;
                        }
                    }
                }
            }
        }

        StpUtil.logoutByTokenValue(token);
    }

    private Long parseUserId(Object loginId) {
        if (loginId == null) {
            return null;
        }
        try {
            return Long.valueOf(String.valueOf(loginId));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String getUsername(Object loginId) {
        Long userId = parseUserId(loginId);
        if (userId == null) {
            return "-";
        }
        UserEntity user = userService.getById(userId);
        return user == null ? "-" : user.getUsername();
    }

    private String getNickname(Object loginId) {
        Long userId = parseUserId(loginId);
        if (userId == null) {
            return "-";
        }
        UserEntity user = userService.getById(userId);
        return user == null ? "-" : user.getNickname();
    }
}
