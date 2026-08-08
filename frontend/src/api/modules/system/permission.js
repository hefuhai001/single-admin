import request from '@/utils/requestUtil.js'

/**
 * 分页查询权限表（1:接口权限 2:路由权限）
 * @param {object} params
 * @param {number} params.current 当前页
 * @param {number} params.size 每页大小
 * @param {Long} params.id 权限ID
 * @param {String} params.permissionCode 权限编码
 * @param {String} params.permissionName 权限名称
 * @param {Integer} params.permissionType 权限类型 1-接口权限 2-路由权限
 * @param {Long} params.parentId 父权限ID
 * @param {String} params.routePath 路由路径
 * @param {String} params.componentPath 组件路径
 * @param {String} params.redirect 重定向地址
 * @param {String} params.icon 菜单图标
 * @param {Integer} params.sort 排序序号
 * @param {Integer} params.visible 是否显示 0-隐藏 1-显示
 * @param {Integer} params.status 状态 0-禁用 1-正常
 * @param {Date} params.createdAt 创建时间
 * @param {Date} params.updatedAt 更新时间
 * @returns {Promise}
 */
export function pagePermission(params) {
    return request.get(`/admin/permission/page`, { params })
}

/**
 * 根据ID查询权限表（1:接口权限 2:路由权限）
 * @param {number} id
 * @returns {Promise}
 */
export function getPermissionById(id) {
    return request.get(`/admin/permission/` + id)
}

/**
 * 新增权限表（1:接口权限 2:路由权限）
 * @param {object} params 权限表（1:接口权限 2:路由权限）
 * @param {Long} params.id 权限ID
 * @param {String} params.permissionCode 权限编码
 * @param {String} params.permissionName 权限名称
 * @param {Integer} params.permissionType 权限类型 1-接口权限 2-路由权限
 * @param {Long} params.parentId 父权限ID
 * @param {String} params.routePath 路由路径
 * @param {String} params.componentPath 组件路径
 * @param {String} params.redirect 重定向地址
 * @param {String} params.icon 菜单图标
 * @param {Integer} params.sort 排序序号
 * @param {Integer} params.visible 是否显示 0-隐藏 1-显示
 * @param {Integer} params.status 状态 0-禁用 1-正常
 * @param {Date} params.createdAt 创建时间
 * @param {Date} params.updatedAt 更新时间
 * @returns {Promise}
 */
export function savePermission(params) {
    return request.post(`/admin/permission`, params)
}

/**
 * 更新权限表（1:接口权限 2:路由权限）
 * @param {object} params 权限表（1:接口权限 2:路由权限）
 * @param {Long} params.id 权限ID
 * @param {String} params.permissionCode 权限编码
 * @param {String} params.permissionName 权限名称
 * @param {Integer} params.permissionType 权限类型 1-接口权限 2-路由权限
 * @param {Long} params.parentId 父权限ID
 * @param {String} params.routePath 路由路径
 * @param {String} params.componentPath 组件路径
 * @param {String} params.redirect 重定向地址
 * @param {String} params.icon 菜单图标
 * @param {Integer} params.sort 排序序号
 * @param {Integer} params.visible 是否显示 0-隐藏 1-显示
 * @param {Integer} params.status 状态 0-禁用 1-正常
 * @param {Date} params.createdAt 创建时间
 * @param {Date} params.updatedAt 更新时间
 * @returns {Promise}
 */
export function updatePermission(params) {
    return request.put(`/admin/permission`, params)
}

/**
 * 删除权限表（1:接口权限 2:路由权限）
 * @param {number} id
 * @returns {Promise}
 */
export function deletePermission(id) {
    return request.delete(`/admin/permission/` + id)
}

/**
 * 批量删除权限表（1:接口权限 2:路由权限）
 * @param {number[]} ids
 * @returns {Promise}
 */
export function batchDeletePermission(ids) {
    return request.delete(`/admin/permission/batch`, { data: ids })
}

/**
 * 查询权限树
 * @returns
 */
export function treePermission() {
    return request.get(`/admin/permission/list-tree`)
}
