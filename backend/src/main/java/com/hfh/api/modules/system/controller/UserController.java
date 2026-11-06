package com.hfh.api.modules.system.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.util.SaResult;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hfh.api.base.BaseController;
import com.hfh.api.modules.system.dto.UserSaveDTO;
import com.hfh.api.modules.system.dto.UserUpdateDTO;
import com.hfh.api.modules.system.entity.UserEntity;
import com.hfh.api.modules.system.entity.UserRoleEntity;
import com.hfh.api.modules.system.service.UserRoleService;
import com.hfh.api.modules.system.service.UserService;
import com.hfh.api.modules.system.vo.RouteVO;
import com.hfh.api.modules.system.vo.UserVO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController(value = "userEntityController")
@RequestMapping("/admin/user")
@RequiredArgsConstructor
@Tag(name = "用户表管理", description = "用户表增删改查接口")
public class UserController extends BaseController {

    private final UserService userService;
    private final UserRoleService userRoleService;

    /**
     * 获取当前用户全部权限编码数组
     *
     * @return 权限编码数组
     */
    @SaCheckLogin
    @Transactional(readOnly = true)
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
    public SaResult pageUser(
            @Parameter(description = "当前页") @RequestParam(defaultValue = "1") Integer current,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "用户ID") @RequestParam(required = false) Long id,
            @Parameter(description = "用户名") @RequestParam(required = false) String username,
            @Parameter(description = "昵称") @RequestParam(required = false) String nickname,
            @Parameter(description = "邮箱") @RequestParam(required = false) String email,
            @Parameter(description = "手机号") @RequestParam(required = false) String phone,
            @Parameter(description = "状态 0-禁用 1-正常") @RequestParam(required = false) Integer status,
            @Parameter(description = "角色ID") @RequestParam(required = false) Long roleId
    ) {
        Page<UserEntity> page = new Page<>(current, size);
        QueryWrapper<UserEntity> wrapper = new QueryWrapper<>();
        wrapper.eq(id != null, "id", id);
        wrapper.like(username != null && !username.isEmpty(), "username", username);
        wrapper.like(nickname != null && !nickname.isEmpty(), "nickname", nickname);
        wrapper.like(email != null && !email.isEmpty(), "email", email);
        wrapper.like(phone != null && !phone.isEmpty(), "phone", phone);
        wrapper.eq(status != null, "status", status);
        if (roleId != null) {
            wrapper.inSql("id", "SELECT user_id FROM sys_user_role WHERE role_id = " + roleId);
        }
        wrapper.orderByDesc("id");
        return SaResult.data(userService.page(page, wrapper));
    }

    @SaCheckPermission("user:query")
    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询用户表")
    public SaResult getUserById(@PathVariable("id") Long id) {
        UserEntity entity = userService.getById(id);
        if (entity == null) {
            return SaResult.error("用户表不存在");
        }

        QueryWrapper<UserRoleEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", id);
        List<UserRoleEntity> userRoles = userRoleService.list(wrapper);
        List<Long> roleIds = userRoles.stream().map(UserRoleEntity::getRoleId).collect(Collectors.toList());

        UserVO vo = new UserVO();
        vo.setId(entity.getId());
        vo.setUsername(entity.getUsername());
        vo.setNickname(entity.getNickname());
        vo.setEmail(entity.getEmail());
        vo.setPhone(entity.getPhone());
        vo.setStatus(entity.getStatus());
        vo.setCreatedAt(entity.getCreatedAt());
        vo.setUpdatedAt(entity.getUpdatedAt());
        vo.setRoleIds(roleIds);

        return SaResult.data(vo);
    }

    @SaCheckPermission("user:add")
    @PostMapping
    @Operation(summary = "新增用户表")
    public SaResult saveUser(@RequestBody UserSaveDTO dto) {
        UserEntity entity = new UserEntity();
        entity.setUsername(dto.getUsername());
        entity.setPassword(dto.getPassword());
        entity.setNickname(dto.getNickname());
        entity.setEmail(dto.getEmail());
        entity.setPhone(dto.getPhone());
        entity.setStatus(dto.getStatus());

        boolean success = userService.save(entity);
        if (success && dto.getRoleIds() != null && !dto.getRoleIds().isEmpty()) {
            userRoleService.saveUserRoles(entity.getId(), dto.getRoleIds());
        }
        return success ? SaResult.ok("新增成功") : SaResult.error("新增失败");
    }

    @SaCheckPermission("user:update")
    @PutMapping
    @Operation(summary = "更新用户表")
    public SaResult updateUser(@RequestBody UserUpdateDTO dto) {
        UserEntity entity = new UserEntity();
        entity.setId(dto.getId());
        entity.setUsername(dto.getUsername());
        entity.setPassword(dto.getPassword());
        entity.setNickname(dto.getNickname());
        entity.setEmail(dto.getEmail());
        entity.setPhone(dto.getPhone());
        entity.setStatus(dto.getStatus());

        boolean success = userService.updateById(entity);
        if (success && dto.getRoleIds() != null) {
            userRoleService.saveUserRoles(entity.getId(), dto.getRoleIds());
        }
        return success ? SaResult.ok("更新成功") : SaResult.error("更新失败");
    }

    @SaCheckPermission("user:delete")
    @DeleteMapping("/{id}")
    @Operation(summary = "删除用户表")
    @Transactional(rollbackFor = Exception.class)
    public SaResult deleteUser(@PathVariable("id") Long id) {
        // 先删除用户角色关联数据
        QueryWrapper<UserRoleEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", id);
        userRoleService.remove(wrapper);

        // 再删除用户数据
        boolean success = userService.removeById(id);
        return success ? SaResult.ok("删除成功") : SaResult.error("删除失败");
    }

    @SaCheckPermission("user:delete")
    @DeleteMapping("/batch")
    @Operation(summary = "批量删除用户表")
    @Transactional(rollbackFor = Exception.class)
    public SaResult deleteUserBatch(@RequestBody List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return SaResult.error("请选择要删除的数据");
        }

        // 先批量删除用户角色关联数据
        QueryWrapper<UserRoleEntity> wrapper = new QueryWrapper<>();
        wrapper.in("user_id", ids);
        userRoleService.remove(wrapper);

        // 再批量删除用户数据
        boolean success = userService.removeByIds(ids);
        return success ? SaResult.ok("批量删除成功") : SaResult.error("批量删除失败");
    }
}
