import request from '@/utils/requestUtil.js'

/**
 * 根据ID查询机构指南表
 * @param {number} id
 * @returns {Promise}
 */
export function getGuidesById(id) {
    return request.get(`/admin/guides/` + id)
}

/**
 * 查询所有机构指南表
 * @returns {Promise}
 */
export function listGuides() {
    return request.get(`/admin/guides/list`)
}

/**
 * 分页查询机构指南表
 * @param {object} params
 * @param {number} params.current 当前页
 * @param {number} params.size 每页大小
 * @param {Long} params.id 指南ID
 * @param {Long} params.institutionId 机构ID
 * @param {String} params.guideTitle 指南标题
 * @param {String} params.guideContent 指南内容(富文本)
 * @param {String} params.guideFileUrl 指南附件URL(PDF/Word等)
 * @param {String} params.guideType 指南类型: general-通用指南, operation-操作指南, quality-质控指南, other-其他
 * @param {String} params.status 状态: active-有效, expired-已过期, archived-已归档
 * @param {Date} params.publishedAt 发布时间
 * @param {Long} params.createdBy 发布人ID
 * @param {Date} params.createdAt
 * @param {Date} params.updatedAt
 * @param {Boolean} params.isDeleted 软删除
 * @returns {Promise}
 */
export function pageGuides(params) {
    return request.get(`/admin/guides/page`, { params })
}

/**
 * 新增机构指南表
 * @param {object} params 机构指南表
 * @param {Long} params.id 指南ID
 * @param {Long} params.institutionId 机构ID
 * @param {String} params.guideTitle 指南标题
 * @param {String} params.guideContent 指南内容(富文本)
 * @param {String} params.guideFileUrl 指南附件URL(PDF/Word等)
 * @param {String} params.guideType 指南类型: general-通用指南, operation-操作指南, quality-质控指南, other-其他
 * @param {String} params.status 状态: active-有效, expired-已过期, archived-已归档
 * @param {Date} params.publishedAt 发布时间
 * @param {Long} params.createdBy 发布人ID
 * @param {Date} params.createdAt
 * @param {Date} params.updatedAt
 * @param {Boolean} params.isDeleted 软删除
 * @returns {Promise}
 */
export function saveGuides(params) {
    return request.post(`/admin/guides`, params)
}

/**
 * 更新机构指南表
 * @param {object} params 机构指南表
 * @param {Long} params.id 指南ID
 * @param {Long} params.institutionId 机构ID
 * @param {String} params.guideTitle 指南标题
 * @param {String} params.guideContent 指南内容(富文本)
 * @param {String} params.guideFileUrl 指南附件URL(PDF/Word等)
 * @param {String} params.guideType 指南类型: general-通用指南, operation-操作指南, quality-质控指南, other-其他
 * @param {String} params.status 状态: active-有效, expired-已过期, archived-已归档
 * @param {Date} params.publishedAt 发布时间
 * @param {Long} params.createdBy 发布人ID
 * @param {Date} params.createdAt
 * @param {Date} params.updatedAt
 * @param {Boolean} params.isDeleted 软删除
 * @returns {Promise}
 */
export function updateGuides(params) {
    return request.put(`/admin/guides`, params)
}

/**
 * 删除机构指南表
 * @param {number} id
 * @returns {Promise}
 */
export function deleteGuides(id) {
    return request.delete(`/admin/guides/` + id)
}

/**
 * 批量删除机构指南表
 * @param {number[]} ids
 * @returns {Promise}
 */
export function batchDeleteGuides(ids) {
    return request.delete(`/admin/guides/batch`, { data: ids })
}
