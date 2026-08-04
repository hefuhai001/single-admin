import request from '@/utils/requestUtil.js'

/**
 * 分页查询角色表
 * @param {object} params
 * @param {number} params.current 当前页
 * @param {number} params.size 每页大小
 * @param {Long} params.id 角色ID
 * @param {String} params.roleName 角色名称
 * @param {String} params.roleCode 角色编码
 * @param {String} params.description 角色描述
 * @param {Integer} params.status 状态 0-禁用 1-正常
 * @param {Date} params.createdAt 创建时间
 * @param {Date} params.updatedAt 更新时间
 * @returns {Promise}
 */
export function pageRole(params) {
    return request.get(`/admin/role/page`, { params })
}

/**
 * 根据ID查询角色表
 * @param {number} id
 * @returns {Promise}
 */
export function getRoleById(id) {
    return request.get(`/admin/role/` + id)
}

/**
 * 新增角色表
 * @param {object} params 角色表
 * @param {Long} params.id 角色ID
 * @param {String} params.roleName 角色名称
 * @param {String} params.roleCode 角色编码
 * @param {String} params.description 角色描述
 * @param {Integer} params.status 状态 0-禁用 1-正常
 * @param {Date} params.createdAt 创建时间
 * @param {Date} params.updatedAt 更新时间
 * @returns {Promise}
 */
export function saveRole(params) {
    return request.post(`/admin/role`, params)
}

/**
 * 更新角色表
 * @param {object} params 角色表
 * @param {Long} params.id 角色ID
 * @param {String} params.roleName 角色名称
 * @param {String} params.roleCode 角色编码
 * @param {String} params.description 角色描述
 * @param {Integer} params.status 状态 0-禁用 1-正常
 * @param {Date} params.createdAt 创建时间
 * @param {Date} params.updatedAt 更新时间
 * @returns {Promise}
 */
export function updateRole(params) {
    return request.put(`/admin/role`, params)
}

/**
 * 删除角色表
 * @param {number} id
 * @returns {Promise}
 */
export function deleteRole(id) {
    return request.delete(`/admin/role/` + id)
}

/**
 * 批量删除角色表
 * @param {number[]} ids
 * @returns {Promise}
 */
export function batchDeleteRole(ids) {
    return request.delete(`/admin/role/batch`, { data: ids })
}

/**
 * 查询所有角色表
 * @returns
 */
export function list() {
    return request.get(`/admin/role/list`)
}
