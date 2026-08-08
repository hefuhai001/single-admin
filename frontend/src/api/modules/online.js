import request from '@/utils/requestUtil.js'

/**
 * 获取在线用户列表
 */
export function getOnlineUsers() {
    return request.get('/admin/monitor/online')
}

/**
 * 强制指定 token 下线
 * @param {string} token
 */
export function kickOnlineUser(token) {
    return request.delete(`/admin/monitor/online/${encodeURIComponent(token)}`)
}
