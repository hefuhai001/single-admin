package com.hfh.api.modules.monitor.online.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.util.SaResult;
import com.hfh.api.modules.monitor.online.service.OnlineUserService;
import com.hfh.api.modules.monitor.online.vo.OnlineUserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 在线用户监控控制器
 */
@RestController("onlineUserController")
@RequestMapping("/admin/monitor")
@RequiredArgsConstructor
@Tag(name = "在线用户监控", description = "在线用户查询与踢人下线")
public class OnlineUserController {

    private final OnlineUserService onlineUserService;

    /**
     * 获取在线用户列表
     */
    @SaCheckLogin
    @SaCheckPermission("monitor:online:query")
    @Operation(summary = "获取在线用户列表")
    @GetMapping("/online")
    public SaResult list() {
        List<OnlineUserVO> list = onlineUserService.listOnlineUsers();
        return SaResult.data(list);
    }

    /**
     * 强制指定 token 下线
     */
    @SaCheckLogin
    @SaCheckPermission("monitor:online:kick")
    @Operation(summary = "强制指定 token 下线")
    @Parameter(name = "token", description = "要强制下线的 accessToken", required = true)
    @DeleteMapping("/online/{token}")
    public SaResult kick(@PathVariable("token") String token) {
        if (token == null || token.isEmpty()) {
            return SaResult.error("token 不能为空");
        }
        onlineUserService.kickByToken(token);
        return SaResult.ok("踢人下线成功");
    }

}
