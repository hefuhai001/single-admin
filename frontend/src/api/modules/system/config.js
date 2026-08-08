import request from '@/utils/requestUtil.js'

/**
 * 根据ID查询系统配置表
 * @param {number} id
 * @returns {Promise}
 */
export function getConfigById(id) {
    return request.get(`/system/config/` + id)
}

/**
 * 查询所有系统配置表
 * @returns {Promise}
 */
export function listConfig() {
    return request.get(`/system/config/list`)
}

/**
 * 分页查询系统配置表
 * @param {object} params
 * @param {number} params.current 当前页
 * @param {number} params.size 每页大小
 * @param {Long} params.id 主键ID
 * @param {String} params.configKey 配置键（唯一标识，如：register.enable）
 * @param {String} params.configValue 配置值（存储具体配置内容）
 * @param {String} params.configName 配置名称（如：是否开启注册）
 * @param {String} params.configGroup 配置分组（system/email/sms/oss等）
 * @param {String} params.configType 配置类型（string/integer/boolean/array/json）
 * @param {Boolean} params.isSystem 是否系统内置（1-是，不可删除；0-否）
 * @param {Boolean} params.status 状态（0停用 1启用）
 * @param {String} params.remark 备注说明
 * @param {Date} params.createTime 创建时间
 * @param {Date} params.updateTime 更新时间
 * @returns {Promise}
 */
export function pageConfig(params) {
    return request.get(`/system/config/page`, { params })
}

/**
 * 新增系统配置表
 * @param {object} params 系统配置表
 * @param {Long} params.id 主键ID
 * @param {String} params.configKey 配置键（唯一标识，如：register.enable）
 * @param {String} params.configValue 配置值（存储具体配置内容）
 * @param {String} params.configName 配置名称（如：是否开启注册）
 * @param {String} params.configGroup 配置分组（system/email/sms/oss等）
 * @param {String} params.configType 配置类型（string/integer/boolean/array/json）
 * @param {Boolean} params.isSystem 是否系统内置（1-是，不可删除；0-否）
 * @param {Boolean} params.status 状态（0停用 1启用）
 * @param {String} params.remark 备注说明
 * @param {Date} params.createTime 创建时间
 * @param {Date} params.updateTime 更新时间
 * @returns {Promise}
 */
export function saveConfig(params) {
    return request.post(`/system/config`, params)
}

/**
 * 更新系统配置表
 * @param {object} params 系统配置表
 * @param {Long} params.id 主键ID
 * @param {String} params.configKey 配置键（唯一标识，如：register.enable）
 * @param {String} params.configValue 配置值（存储具体配置内容）
 * @param {String} params.configName 配置名称（如：是否开启注册）
 * @param {String} params.configGroup 配置分组（system/email/sms/oss等）
 * @param {String} params.configType 配置类型（string/integer/boolean/array/json）
 * @param {Boolean} params.isSystem 是否系统内置（1-是，不可删除；0-否）
 * @param {Boolean} params.status 状态（0停用 1启用）
 * @param {String} params.remark 备注说明
 * @param {Date} params.createTime 创建时间
 * @param {Date} params.updateTime 更新时间
 * @returns {Promise}
 */
export function updateConfig(params) {
    return request.put(`/system/config`, params)
}

/**
 * 删除系统配置表
 * @param {number} id
 * @returns {Promise}
 */
export function deleteConfig(id) {
    return request.delete(`/system/config/` + id)
}

/**
 * 批量删除系统配置表
 * @param {number[]} ids
 * @returns {Promise}
 */
export function batchDeleteConfig(ids) {
    return request.delete(`/system/config/batch`, { data: ids })
}
