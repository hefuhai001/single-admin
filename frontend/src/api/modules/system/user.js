import request from '@/utils/requestUtil.js'

/**
 * 分页查询用户表
 * @param {object} params
 * @param {number} params.current 当前页
 * @param {number} params.size 每页大小
 * @param {Long} params.id 用户ID
 * @param {String} params.username 用户名
 * @param {String} params.password 密码（加密存储）
 * @param {String} params.nickname 昵称
 * @param {String} params.email 邮箱
 * @param {String} params.phone 手机号
 * @param {Integer} params.status 状态 0-禁用 1-正常
 * @param {Date} params.createdAt 创建时间
 * @param {Date} params.updatedAt 更新时间
 * @returns {Promise}
 */
export function pageUser(params) {
    return request.get(`/admin/user/page`, { params })
}

/**
 * 根据ID查询用户表
 * @param {number} id
 * @returns {Promise}
 */
export function getUserById(id) {
    return request.get(`/admin/user/` + id)
}

/**
 * 新增用户表
 * @param {object} params 用户表
 * @param {Long} params.id 用户ID
 * @param {String} params.username 用户名
 * @param {String} params.password 密码（加密存储）
 * @param {String} params.nickname 昵称
 * @param {String} params.email 邮箱
 * @param {String} params.phone 手机号
 * @param {Integer} params.status 状态 0-禁用 1-正常
 * @param {Date} params.createdAt 创建时间
 * @param {Date} params.updatedAt 更新时间
 * @returns {Promise}
 */
export function saveUser(params) {
    return request.post(`/admin/user`, params)
}

/**
 * 更新用户表
 * @param {object} params 用户表
 * @param {Long} params.id 用户ID
 * @param {String} params.username 用户名
 * @param {String} params.password 密码（加密存储）
 * @param {String} params.nickname 昵称
 * @param {String} params.email 邮箱
 * @param {String} params.phone 手机号
 * @param {Integer} params.status 状态 0-禁用 1-正常
 * @param {Date} params.createdAt 创建时间
 * @param {Date} params.updatedAt 更新时间
 * @returns {Promise}
 */
export function updateUser(params) {
    return request.put(`/admin/user`, params)
}

/**
 * 删除用户表
 * @param {number} id
 * @returns {Promise}
 */
export function deleteUser(id) {
    return request.delete(`/admin/user/` + id)
}

/**
 * 批量删除用户表
 * @param {number[]} ids
 * @returns {Promise}
 */
export function batchDeleteUser(ids) {
    return request.delete(`/admin/user/batch`, { data: ids })
}

/**
 * 获取当前用户路由
 * @returns
 */
export function routesUser() {
    return request.get(`/admin/user/routes`)
}

/**
 * 获取当前用户全部权限编码数组
 * @returns
 */
export function permissionsUser() {
    return request.get(`/admin/user/permissions`)
}
