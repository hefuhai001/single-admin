import request from '@/utils/requestUtil.js'

/**
 * 查询机构树
 * @returns
 */
export function treeInstitutions() {
    return request.get(`/admin/institutions/list-tree`)
}

/**
 * 根据ID查询机构表(支持父子层级)
 * @param {number} id
 * @returns {Promise}
 */
export function getInstitutionsById(id) {
    return request.get(`/admin/institutions/` + id)
}

/**
 * 查询所有机构表(支持父子层级)
 * @returns {Promise}
 */
export function listInstitutions() {
    return request.get(`/admin/institutions/list`)
}

/**
 * 分页查询机构表(支持父子层级)
 * @param {object} params
 * @param {number} params.current 当前页
 * @param {number} params.size 每页大小
 * @param {Long} params.id 机构ID
 * @param {Long} params.parentId 父机构ID(上级机构)
 * @param {String} params.institutionCode 机构编码
 * @param {String} params.institutionName 机构名称
 * @param {Byte} params.status 状态: 0-禁用,1-启用
 * @param {Date} params.createdAt
 * @param {Date} params.updatedAt
 * @returns {Promise}
 */
export function pageInstitutions(params) {
    return request.get(`/admin/institutions/page`, { params })
}

/**
 * 新增机构表(支持父子层级)
 * @param {object} params 机构表(支持父子层级)
 * @param {Long} params.id 机构ID
 * @param {Long} params.parentId 父机构ID(上级机构)
 * @param {String} params.institutionCode 机构编码
 * @param {String} params.institutionName 机构名称
 * @param {Byte} params.status 状态: 0-禁用,1-启用
 * @param {Date} params.createdAt
 * @param {Date} params.updatedAt
 * @returns {Promise}
 */
export function saveInstitutions(params) {
    return request.post(`/admin/institutions`, params)
}

/**
 * 更新机构表(支持父子层级)
 * @param {object} params 机构表(支持父子层级)
 * @param {Long} params.id 机构ID
 * @param {Long} params.parentId 父机构ID(上级机构)
 * @param {String} params.institutionCode 机构编码
 * @param {String} params.institutionName 机构名称
 * @param {Byte} params.status 状态: 0-禁用,1-启用
 * @param {Date} params.createdAt
 * @param {Date} params.updatedAt
 * @returns {Promise}
 */
export function updateInstitutions(params) {
    return request.put(`/admin/institutions`, params)
}

/**
 * 删除机构表(支持父子层级)
 * @param {number} id
 * @returns {Promise}
 */
export function deleteInstitutions(id) {
    return request.delete(`/admin/institutions/` + id)
}

/**
 * 批量删除机构表(支持父子层级)
 * @param {number[]} ids
 * @returns {Promise}
 */
export function batchDeleteInstitutions(ids) {
    return request.delete(`/admin/institutions/batch`, { data: ids })
}
