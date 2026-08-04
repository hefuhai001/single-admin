import request from '@/utils/requestUtil.js'

/**
 * 根据ID查询机构反馈表
 * @param {number} id
 * @returns {Promise}
 */
export function getFeedbacksById(id) {
    return request.get(`/admin/feedbacks/` + id)
}

/**
 * 查询所有机构反馈表
 * @returns {Promise}
 */
export function listFeedbacks() {
    return request.get(`/admin/feedbacks/list`)
}

/**
 * 分页查询机构反馈表
 * @param {object} params
 * @param {number} params.current 当前页
 * @param {number} params.size 每页大小
 * @param {Long} params.id 反馈ID
 * @param {Long} params.institutionId 机构ID
 * @param {String} params.feedbackType 反馈类型: question-疑问, suggestion-建议, complaint-投诉, issue-问题报告, other-其他
 * @param {String} params.feedbackTitle 反馈标题
 * @param {String} params.feedbackContent 反馈内容
 * @param {String} params.attachmentUrl 附件URL
 * @param {String} params.priority 优先级: low-低, medium-中, high-高, urgent-紧急
 * @param {String} params.status 状态: pending-待处理, processing-处理中, resolved-已解决, closed-已关闭, rejected-已驳回
 * @param {String} params.replyContent 回复内容
 * @param {Long} params.repliedBy 回复人ID
 * @param {Date} params.repliedAt 回复时间
 * @param {Long} params.feedbackBy 反馈人ID
 * @param {Date} params.createdAt
 * @param {Date} params.updatedAt
 * @param {Boolean} params.isDeleted 软删除
 * @returns {Promise}
 */
export function pageFeedbacks(params) {
    return request.get(`/admin/feedbacks/page`, { params })
}

/**
 * 新增机构反馈表
 * @param {object} params 机构反馈表
 * @param {Long} params.id 反馈ID
 * @param {Long} params.institutionId 机构ID
 * @param {String} params.feedbackType 反馈类型: question-疑问, suggestion-建议, complaint-投诉, issue-问题报告, other-其他
 * @param {String} params.feedbackTitle 反馈标题
 * @param {String} params.feedbackContent 反馈内容
 * @param {String} params.attachmentUrl 附件URL
 * @param {String} params.priority 优先级: low-低, medium-中, high-高, urgent-紧急
 * @param {String} params.status 状态: pending-待处理, processing-处理中, resolved-已解决, closed-已关闭, rejected-已驳回
 * @param {String} params.replyContent 回复内容
 * @param {Long} params.repliedBy 回复人ID
 * @param {Date} params.repliedAt 回复时间
 * @param {Long} params.feedbackBy 反馈人ID
 * @param {Date} params.createdAt
 * @param {Date} params.updatedAt
 * @param {Boolean} params.isDeleted 软删除
 * @returns {Promise}
 */
export function saveFeedbacks(params) {
    return request.post(`/admin/feedbacks`, params)
}

/**
 * 更新机构反馈表
 * @param {object} params 机构反馈表
 * @param {Long} params.id 反馈ID
 * @param {Long} params.institutionId 机构ID
 * @param {String} params.feedbackType 反馈类型: question-疑问, suggestion-建议, complaint-投诉, issue-问题报告, other-其他
 * @param {String} params.feedbackTitle 反馈标题
 * @param {String} params.feedbackContent 反馈内容
 * @param {String} params.attachmentUrl 附件URL
 * @param {String} params.priority 优先级: low-低, medium-中, high-高, urgent-紧急
 * @param {String} params.status 状态: pending-待处理, processing-处理中, resolved-已解决, closed-已关闭, rejected-已驳回
 * @param {String} params.replyContent 回复内容
 * @param {Long} params.repliedBy 回复人ID
 * @param {Date} params.repliedAt 回复时间
 * @param {Long} params.feedbackBy 反馈人ID
 * @param {Date} params.createdAt
 * @param {Date} params.updatedAt
 * @param {Boolean} params.isDeleted 软删除
 * @returns {Promise}
 */
export function updateFeedbacks(params) {
    return request.put(`/admin/feedbacks`, params)
}

/**
 * 删除机构反馈表
 * @param {number} id
 * @returns {Promise}
 */
export function deleteFeedbacks(id) {
    return request.delete(`/admin/feedbacks/` + id)
}

/**
 * 批量删除机构反馈表
 * @param {number[]} ids
 * @returns {Promise}
 */
export function batchDeleteFeedbacks(ids) {
    return request.delete(`/admin/feedbacks/batch`, { data: ids })
}
