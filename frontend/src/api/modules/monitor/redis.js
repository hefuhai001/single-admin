import request from '@/utils/requestUtil.js'

/**
 * 获取 Redis 服务器信息总览
 * @returns {Promise}
 */
export function getRedisInfo() {
    return request.get('/admin/monitor/redis/info')
}

/**
 * 获取 Redis 命令统计
 * @returns {Promise}
 */
export function getCommandStats() {
    return request.get('/admin/monitor/redis/command-stats')
}

/**
 * 获取 Key 树（Key 按冒号分层）
 * @returns {Promise}
 */
export function getRedisKeyTree() {
    return request.get('/admin/monitor/redis/keys/tree')
}

/**
 * 分页查询 Key 列表
 * @param {object} params
 * @param {String} params.pattern Key 匹配模式，支持通配符，默认 *；纯前缀时按前缀及其子级匹配
 * @param {number} params.current 当前页
 * @param {number} params.size 每页大小
 * @returns {Promise}
 */
export function pageRedisKeys(params) {
    return request.get('/admin/monitor/redis/keys', { params })
}

/**
 * 获取 Key 详情
 * @param {String} key Key 名称
 * @returns {Promise}
 */
export function getRedisKeyDetail(key) {
    return request.get(`/admin/monitor/redis/keys/${encodeURIComponent(key)}`)
}

/**
 * 批量删除 Key
 * @param {Array<string>} keys Key 名称数组
 * @returns {Promise}
 */
export function deleteRedisKeys(keys) {
    return request.delete('/admin/monitor/redis/keys', { data: keys })
}
