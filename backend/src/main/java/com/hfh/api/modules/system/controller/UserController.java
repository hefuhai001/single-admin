package com.hfh.api.modules.system.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.util.SaResult;
import com.hfh.api.base.BaseController;
import com.hfh.api.modules.system.dto.UserQueryDTO;
import com.hfh.api.modules.system.dto.UserSaveDTO;
import com.hfh.api.modules.system.dto.UserUpdateDTO;
import com.hfh.api.modules.system.service.UserService;
import com.hfh.api.modules.system.vo.RouteVO;
import com.hfh.api.modules.system.vo.UserVO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController(value = "userEntityController")
@RequestMapping("/admin/user")
@RequiredArgsConstructor
@Tag(name = "用户表管理", description = "用户表增删改查接口")
public class UserController extends BaseController {

    private final UserService userService;

    /**
     * 获取当前用户全部权限编码数组
     *
     * @return 权限编码数组
     */
    @SaCheckLogin
    @Operation(summary = "获取当前用户全部权限编码数组")
    @GetMapping("/permissions")
    public SaResult permissionsUser() {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return SaResult.error("用户未登录");
        }
        List<String> permissions = userService.getUserPermissions(userId);
        return SaResult.data(permissions);
    }

    @SaCheckLogin
    @GetMapping("/routes")
    @Operation(summary = "获取当前用户路由")
    public SaResult routesUser() {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return SaResult.error("用户未登录");
        }
        List<RouteVO> routes = userService.getUserRoutes(userId);
        return SaResult.data(routes);
    }

    @SaCheckPermission("user:query")
    @GetMapping("/page")
    @Operation(summary = "分页查询用户表")
    public SaResult pageUser(@Valid UserQueryDTO query) {
        return SaResult.data(userService.pageUsers(query));
    }

    @SaCheckPermission("user:query")
    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询用户表")
    public SaResult getUserById(@PathVariable("id") Long id) {
        UserVO vo = userService.getUserDetail(id);
        if (vo == null) {
            return SaResult.error("用户表不存在");
        }
        return SaResult.data(vo);
    }

    @SaCheckPermission("user:add")
    @PostMapping
    @Operation(summary = "新增用户表")
    public SaResult saveUser(@Valid @RequestBody UserSaveDTO dto) {
        userService.createUser(dto);
        return SaResult.ok("新增成功");
    }

    @SaCheckPermission("user:update")
    @PutMapping
    @Operation(summary = "更新用户表")
    public SaResult updateUser(@Valid @RequestBody UserUpdateDTO dto) {
        userService.updateUser(dto);
        return SaResult.ok("更新成功");
    }

    @SaCheckPermission("user:delete")
    @DeleteMapping("/{id}")
    @Operation(summary = "删除用户表")
    public SaResult deleteUser(@PathVariable("id") Long id) {
        userService.deleteUser(id);
        return SaResult.ok("删除成功");
    }

    @SaCheckPermission("user:delete")
    @DeleteMapping("/batch")
    @Operation(summary = "批量删除用户表")
    public SaResult deleteUserBatch(@RequestBody List<Long> ids) {
        userService.deleteUsers(ids);
        return SaResult.ok("批量删除成功");
    }
}
