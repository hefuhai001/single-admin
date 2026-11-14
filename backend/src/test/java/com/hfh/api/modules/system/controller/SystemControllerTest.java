package com.hfh.api.modules.system.controller;

import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hfh.api.modules.system.dto.ConfigSaveDTO;
import com.hfh.api.modules.system.dto.ConfigUpdateDTO;
import com.hfh.api.modules.system.dto.DictDataSaveDTO;
import com.hfh.api.modules.system.dto.DictDataUpdateDTO;
import com.hfh.api.modules.system.dto.DictTypeSaveDTO;
import com.hfh.api.modules.system.dto.DictTypeUpdateDTO;
import com.hfh.api.modules.system.dto.InstitutionsSaveDTO;
import com.hfh.api.modules.system.dto.InstitutionsUpdateDTO;
import com.hfh.api.modules.system.dto.PermissionSaveDTO;
import com.hfh.api.modules.system.dto.PermissionUpdateDTO;
import com.hfh.api.modules.system.dto.RoleSaveDTO;
import com.hfh.api.modules.system.dto.RoleUpdateDTO;
import com.hfh.api.modules.system.dto.UserQueryDTO;
import com.hfh.api.modules.system.dto.UserSaveDTO;
import com.hfh.api.modules.system.dto.UserUpdateDTO;
import com.hfh.api.modules.system.entity.ConfigEntity;
import com.hfh.api.modules.system.entity.DictDataEntity;
import com.hfh.api.modules.system.entity.DictTypeEntity;
import com.hfh.api.modules.system.entity.InstitutionsEntity;
import com.hfh.api.modules.system.entity.PermissionEntity;
import com.hfh.api.modules.system.entity.RoleEntity;
import com.hfh.api.modules.system.service.ConfigService;
import com.hfh.api.modules.system.service.DictDataService;
import com.hfh.api.modules.system.service.DictTypeService;
import com.hfh.api.modules.system.service.InstitutionsService;
import com.hfh.api.modules.system.service.PermissionService;
import com.hfh.api.modules.system.service.RolePermissionService;
import com.hfh.api.modules.system.service.RoleService;
import com.hfh.api.modules.system.service.UserRoleService;
import com.hfh.api.modules.system.service.UserService;
import com.hfh.api.modules.system.vo.RoleVO;
import com.hfh.api.modules.system.vo.RouteVO;
import com.hfh.api.modules.system.vo.UserVO;
import com.hfh.api.config.PermissionCache;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SystemControllerTest {

    private ConfigService configService;
    private DictDataService dictDataService;
    private DictTypeService dictTypeService;
    private InstitutionsService institutionsService;
    private PermissionService permissionService;
    private RoleService roleService;
    private RolePermissionService rolePermissionService;
    private UserRoleService userRoleService;
    private UserService userService;
    private PermissionCache permissionCache;
    private MockedStatic<StpUtil> stp;

    @BeforeEach
    void setUp() {
        configService = mock(ConfigService.class);
        dictDataService = mock(DictDataService.class);
        dictTypeService = mock(DictTypeService.class);
        institutionsService = mock(InstitutionsService.class);
        permissionService = mock(PermissionService.class);
        roleService = mock(RoleService.class);
        rolePermissionService = mock(RolePermissionService.class);
        userRoleService = mock(UserRoleService.class);
        userService = mock(UserService.class);
        permissionCache = mock(PermissionCache.class);
        stp = mockStatic(StpUtil.class);
    }

    @AfterEach
    void tearDown() {
        stp.close();
    }

    private static SaResult okData() {
        return SaResult.data("ok");
    }

    @Test
    void configControllerShouldHandleAllEndpoints() {
        ConfigController controller = new ConfigController(configService);
        when(configService.getById(1L)).thenReturn(null);
        when(configService.getById(2L)).thenReturn(new ConfigEntity());
        when(configService.list(any(com.baomidou.mybatisplus.core.conditions.Wrapper.class))).thenReturn(List.of(new ConfigEntity()));
        when(configService.page(any(), any())).thenReturn(new Page<>());
        when(configService.save(any())).thenReturn(true);
        when(configService.updateById(any())).thenReturn(true);
        when(configService.removeById(1L)).thenReturn(true);
        when(configService.deleteByIds(anyList())).thenReturn(true);

        assertThat(controller.getConfigById(1L).getCode()).isNotEqualTo(200);
        assertThat(controller.getConfigById(2L).getData()).isNotNull();
        assertThat((List<?>) controller.listConfig().getData()).hasSize(1);
        assertThat(controller.pageConfig(1, 10, 1L, "k", "v", "n", "g", "t", "r").getData()).isNotNull();
        assertThat(controller.saveConfig(new ConfigSaveDTO()).getMsg()).isEqualTo("新增成功");
        assertThat(controller.updateConfig(new ConfigUpdateDTO()).getMsg()).isEqualTo("更新成功");
        assertThat(controller.deleteConfig(1L).getMsg()).isEqualTo("删除成功");
        assertThat(controller.deleteConfigBatch(List.of()).getMsg()).isEqualTo("请选择要删除的数据");
        assertThat(controller.deleteConfigBatch(List.of(1L)).getMsg()).isEqualTo("批量删除成功");

        when(configService.save(any())).thenReturn(false);
        assertThat(controller.saveConfig(new ConfigSaveDTO()).getMsg()).isEqualTo("新增失败");
    }

    @Test
    void dictDataControllerShouldHandleAllEndpoints() {
        DictDataController controller = new DictDataController(dictDataService);
        when(dictDataService.getById(1L)).thenReturn(null);
        when(dictDataService.getById(2L)).thenReturn(new DictDataEntity());
        when(dictDataService.list(any(com.baomidou.mybatisplus.core.conditions.Wrapper.class))).thenReturn(List.of(new DictDataEntity()));
        when(dictDataService.listByDictType("sex")).thenReturn(List.of(new DictDataEntity()));
        when(dictDataService.page(any(), any())).thenReturn(new Page<>());
        when(dictDataService.save(any())).thenReturn(true);
        when(dictDataService.updateById(any())).thenReturn(true);
        when(dictDataService.removeById(1L)).thenReturn(true);
        when(dictDataService.removeByIds(anyList())).thenReturn(true);

        assertThat(controller.getDictDataById(1L).getCode()).isNotEqualTo(200);
        assertThat(controller.getDictDataById(2L).getData()).isNotNull();
        assertThat((List<?>) controller.listDictData().getData()).hasSize(1);
        assertThat((List<?>) controller.listDictDataByType("sex").getData()).hasSize(1);
        assertThat(controller.pageDictData(1, 10, 1L, "sex", "男", "1", 1, "c", "l").getData()).isNotNull();
        assertThat(controller.saveDictData(new DictDataSaveDTO()).getMsg()).isEqualTo("新增成功");
        assertThat(controller.updateDictData(new DictDataUpdateDTO()).getMsg()).isEqualTo("更新成功");
        assertThat(controller.deleteDictData(1L).getMsg()).isEqualTo("删除成功");
        assertThat(controller.deleteDictDataBatch(null).getMsg()).isEqualTo("请选择要删除的数据");
        assertThat(controller.deleteDictDataBatch(List.of(1L)).getMsg()).isEqualTo("批量删除成功");
    }

    @Test
    void dictTypeControllerShouldHandleAllEndpoints() {
        DictTypeController controller = new DictTypeController(dictTypeService);
        when(dictTypeService.getById(1L)).thenReturn(null);
        when(dictTypeService.getById(2L)).thenReturn(new DictTypeEntity());
        when(dictTypeService.list(any(com.baomidou.mybatisplus.core.conditions.Wrapper.class))).thenReturn(List.of(new DictTypeEntity()));
        when(dictTypeService.page(any(), any())).thenReturn(new Page<>());
        when(dictTypeService.save(any())).thenReturn(true);
        when(dictTypeService.updateById(any())).thenReturn(true);
        when(dictTypeService.removeById(1L)).thenReturn(false);
        when(dictTypeService.removeByIds(anyList())).thenReturn(true);

        assertThat(controller.getDictTypeById(1L).getCode()).isNotEqualTo(200);
        assertThat(controller.getDictTypeById(2L).getData()).isNotNull();
        assertThat((List<?>) controller.listDictType().getData()).hasSize(1);
        assertThat(controller.pageDictType(1, 10, 1L, "n", "t").getData()).isNotNull();
        assertThat(controller.saveDictType(new DictTypeSaveDTO()).getMsg()).isEqualTo("新增成功");
        assertThat(controller.updateDictType(new DictTypeUpdateDTO()).getMsg()).isEqualTo("更新成功");
        assertThat(controller.deleteDictType(1L).getMsg()).isEqualTo("删除失败");
        assertThat(controller.deleteDictTypeBatch(List.of()).getMsg()).isEqualTo("请选择要删除的数据");
        assertThat(controller.deleteDictTypeBatch(List.of(1L)).getMsg()).isEqualTo("批量删除成功");
    }

    @Test
    void institutionsControllerShouldHandleAllEndpoints() {
        InstitutionsController controller = new InstitutionsController(institutionsService);
        when(institutionsService.listTree()).thenReturn(List.of(new InstitutionsEntity()));
        when(institutionsService.getById(1L)).thenReturn(null);
        when(institutionsService.getById(2L)).thenReturn(new InstitutionsEntity());
        when(institutionsService.list(any(com.baomidou.mybatisplus.core.conditions.Wrapper.class))).thenReturn(List.of(new InstitutionsEntity()));
        when(institutionsService.page(any(), any())).thenReturn(new Page<>());
        when(institutionsService.save(any())).thenReturn(true);
        when(institutionsService.updateById(any())).thenReturn(false);
        when(institutionsService.removeById(1L)).thenReturn(true);
        when(institutionsService.removeByIds(anyList())).thenReturn(true);

        assertThat((List<?>) controller.treeInstitutions().getData()).hasSize(1);
        assertThat(controller.getInstitutionsById(1L).getCode()).isNotEqualTo(200);
        assertThat(controller.getInstitutionsById(2L).getData()).isNotNull();
        assertThat((List<?>) controller.listInstitutions().getData()).hasSize(1);
        assertThat(controller.pageInstitutions(1, 10, 1L, 2L, "c", "n").getData()).isNotNull();
        assertThat(controller.saveInstitutions(new InstitutionsSaveDTO()).getMsg()).isEqualTo("新增成功");
        assertThat(controller.updateInstitutions(new InstitutionsUpdateDTO()).getMsg()).isEqualTo("更新失败");
        assertThat(controller.deleteInstitutions(1L).getMsg()).isEqualTo("删除成功");
        assertThat(controller.deleteInstitutionsBatch(null).getMsg()).isEqualTo("请选择要删除的数据");
        assertThat(controller.deleteInstitutionsBatch(List.of(1L)).getMsg()).isEqualTo("批量删除成功");
    }

    @Test
    void permissionControllerShouldHandleAllEndpoints() {
        PermissionController controller = new PermissionController(permissionService);
        when(permissionService.listTree()).thenReturn(List.of(new PermissionEntity()));
        when(permissionService.getById(1L)).thenReturn(null);
        when(permissionService.getById(2L)).thenReturn(new PermissionEntity());
        when(permissionService.page(any(), any())).thenReturn(new Page<>());
        when(permissionService.save(any())).thenReturn(true);
        when(permissionService.updateById(any())).thenReturn(true);
        when(permissionService.removeById(1L)).thenReturn(true);
        when(permissionService.removeByIds(anyList())).thenReturn(true);

        assertThat((List<?>) controller.treePermission().getData()).hasSize(1);
        assertThat(controller.pagePermission(1, 10, 1L, "c", "n", 1, 2L, "r", "cp", "rd", "ic", 1, 1, 1)
                .getData()).isNotNull();
        assertThat(controller.pagePermission(1, 10, null, null, null, null, null, null, null, null, null,
                null, null, null).getData()).isNotNull();
        assertThat(controller.getPermissionById(1L).getCode()).isNotEqualTo(200);
        assertThat(controller.getPermissionById(2L).getData()).isNotNull();
        assertThat(controller.savePermission(new PermissionSaveDTO()).getMsg()).isEqualTo("新增成功");
        assertThat(controller.updatePermission(new PermissionUpdateDTO()).getMsg()).isEqualTo("更新成功");
        assertThat(controller.deletePermission(1L).getMsg()).isEqualTo("删除成功");
        assertThat(controller.deletePermissionBatch(List.of()).getMsg()).isEqualTo("请选择要删除的数据");
        assertThat(controller.deletePermissionBatch(List.of(1L)).getMsg()).isEqualTo("批量删除成功");
    }

    @Test
    void roleControllerShouldHandleCrudAndRelations() {
        RoleController controller = new RoleController(roleService, rolePermissionService);
        when(roleService.list(any(com.baomidou.mybatisplus.core.conditions.Wrapper.class))).thenReturn(List.of(new RoleEntity()));
        when(roleService.page(any(), any())).thenReturn(new Page<>());
        when(roleService.getRoleWithPermissions(1L)).thenReturn(null);
        when(roleService.getRoleWithPermissions(2L)).thenReturn(new RoleVO());
        when(roleService.save(any())).thenAnswer(inv -> {
            inv.<RoleEntity>getArgument(0).setId(9L);
            return true;
        });
        when(roleService.updateById(any())).thenReturn(true);
        when(roleService.deleteRole(1L)).thenReturn(true);
        when(roleService.deleteRoles(anyList())).thenReturn(false);
        when(rolePermissionService.remove(any())).thenReturn(true);

        RoleSaveDTO saveDto = new RoleSaveDTO();
        saveDto.setRoleName("r");
        saveDto.setRoleCode("R");
        saveDto.setStatus(1);
        saveDto.setPermissionIds(List.of(1L, 2L));
        RoleUpdateDTO updateDto = new RoleUpdateDTO();
        updateDto.setId(5L);
        updateDto.setPermissionIds(List.of(3L));

        assertThat((List<?>) controller.listRole().getData()).hasSize(1);
        assertThat(controller.pageRole(1, 10, 1L, "n", "c", "d", 1).getData()).isNotNull();
        assertThat(controller.getRoleById(1L).getCode()).isNotEqualTo(200);
        assertThat(controller.getRoleById(2L).getData()).isNotNull();
        assertThat(controller.saveRole(saveDto).getMsg()).isEqualTo("新增成功");
        verify(rolePermissionService).saveRolePermissions(9L, List.of(1L, 2L));
        assertThat(controller.updateRole(updateDto).getMsg()).isEqualTo("更新成功");
        verify(rolePermissionService).saveRolePermissions(5L, List.of(3L));
        assertThat(controller.deleteRole(1L).getMsg()).isEqualTo("删除成功");
        verify(roleService).deleteRole(1L);
        assertThat(controller.deleteRoleBatch(List.of()).getMsg()).isEqualTo("请选择要删除的数据");
        assertThat(controller.deleteRoleBatch(List.of(1L)).getMsg()).isEqualTo("批量删除失败");
    }

    @Test
    void roleControllerShouldSkipRelationsWhenSaveFails() {
        RoleController controller = new RoleController(roleService, rolePermissionService);
        when(roleService.save(any())).thenReturn(false);
        when(roleService.updateById(any())).thenReturn(false);
        RoleSaveDTO saveDto = new RoleSaveDTO();
        saveDto.setPermissionIds(List.of(1L));
        RoleUpdateDTO updateDto = new RoleUpdateDTO();
        updateDto.setPermissionIds(List.of(1L));

        assertThat(controller.saveRole(saveDto).getMsg()).isEqualTo("新增失败");
        assertThat(controller.updateRole(updateDto).getMsg()).isEqualTo("更新失败");
        verify(rolePermissionService, never()).saveRolePermissions(anyLong(), anyList());
    }

    @Test
    void roleControllerShouldFailDeleteWhenServiceReturnsFalse() {
        RoleController controller = new RoleController(roleService, rolePermissionService);
        when(roleService.deleteRole(1L)).thenReturn(false);

        assertThat(controller.deleteRole(1L).getMsg()).isEqualTo("删除失败");
    }

    @Test
    void userControllerShouldHandleAllEndpoints() {
        UserController controller = new UserController(userService);
        ReflectionTestUtils.setField(controller, "userService", userService);
        stp.when(StpUtil::isLogin).thenReturn(false);
        when(userService.getUserPermissions(1L)).thenReturn(List.of("a"));
        when(userService.getUserRoutes(1L)).thenReturn(List.of(new RouteVO()));
        when(userService.pageUsers(any())).thenReturn(new Page<>());
        when(userService.getUserDetail(1L)).thenReturn(null);
        when(userService.getUserDetail(2L)).thenReturn(new UserVO());

        assertThat(controller.permissionsUser().getMsg()).isEqualTo("用户未登录");
        assertThat(controller.routesUser().getMsg()).isEqualTo("用户未登录");

        stp.when(StpUtil::isLogin).thenReturn(true);
        stp.when(StpUtil::getLoginId).thenReturn("1");
        assertThat((List<?>) controller.permissionsUser().getData()).hasSize(1);
        assertThat((List<?>) controller.routesUser().getData()).hasSize(1);
        assertThat(controller.pageUser(new UserQueryDTO()).getData()).isNotNull();
        assertThat(controller.getUserById(1L).getCode()).isNotEqualTo(200);
        assertThat(controller.getUserById(2L).getData()).isNotNull();
        assertThat(controller.saveUser(new UserSaveDTO()).getMsg()).isEqualTo("新增成功");
        assertThat(controller.updateUser(new UserUpdateDTO()).getMsg()).isEqualTo("更新成功");
        assertThat(controller.deleteUser(1L).getMsg()).isEqualTo("删除成功");
        assertThat(controller.deleteUserBatch(List.of(1L)).getMsg()).isEqualTo("批量删除成功");
    }

    @Test
    void userControllerShouldParseOtherLoginIdTypes() {
        UserController controller = new UserController(userService);
        stp.when(StpUtil::isLogin).thenReturn(true);
        when(userService.getUserPermissions(2L)).thenReturn(List.of());
        when(userService.getUserPermissions(3L)).thenReturn(List.of());

        stp.when(StpUtil::getLoginId).thenReturn(2);
        assertThat(controller.permissionsUser().getData()).isNotNull();

        stp.when(StpUtil::getLoginId).thenReturn("bad-id");
        assertThat(controller.permissionsUser().getMsg()).isEqualTo("用户未登录");
    }
}
