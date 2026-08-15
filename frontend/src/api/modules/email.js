import request from '@/utils/requestUtil.js'

/**
 * 发送简单文本邮件
 * @param {object} params 邮件请求
 * @param {String} params.to 单收件人邮箱地址
 * @param {Array<string>} params.toList 多收件人邮箱地址列表（群发），与 to 至少填一项
 * @param {Array<string>} params.cc 抄送邮箱地址列表
 * @param {String} params.subject 邮件主题
 * @param {String} params.content 邮件正文内容
 * @returns {Promise}
 */
export function sendSimpleEmail(params) {
    return request.post(`/admin/email/simple`, params)
}

/**
 * 发送HTML邮件（支持群发 + 附件）
 * @param {object} params 邮件请求
 * @param {String} params.to 单收件人邮箱地址
 * @param {Array<string>} params.toList 多收件人邮箱地址列表（群发），与 to 至少填一项
 * @param {Array<string>} params.cc 抄送邮箱地址列表
 * @param {Array<string>} params.bcc 密送邮箱地址列表
 * @param {String} params.subject 邮件主题
 * @param {String} params.content 邮件正文内容（HTML）
 * @param {Array<string>} params.attachments 附件文件路径列表（服务器本地绝对路径）
 * @returns {Promise}
 */
export function sendHtmlEmail(params) {
    return request.post(`/admin/email/html`, params)
}

/**
 * 发送Thymeleaf模板邮件
 * @param {object} params 模板邮件请求
 * @param {String} params.to 单收件人邮箱地址
 * @param {Array<string>} params.toList 多收件人邮箱地址列表（群发），与 to 至少填一项
 * @param {String} params.subject 邮件主题
 * @param {String} params.templateName Thymeleaf模板名称，如 mail/welcome
 * @param {object} params.variables 模板变量数据（welcome: username/code/link/expireMinutes）
 * @returns {Promise}
 */
export function sendTemplateEmail(params) {
    return request.post(`/admin/email/template`, params)
}

/**
 * 批量群发邮件
 * @param {Array<object>} params 邮件请求数组
 * @param {String} params[].to 单收件人邮箱地址
 * @param {Array<string>} params[].toList 多收件人邮箱地址列表（群发）
 * @param {String} params[].subject 邮件主题
 * @param {String} params[].content 邮件正文内容
 * @param {boolean} params[].html 是否为HTML格式邮件
 * @returns {Promise}
 */
export function sendBatchEmails(params) {
    return request.post(`/admin/email/batch`, params)
}
