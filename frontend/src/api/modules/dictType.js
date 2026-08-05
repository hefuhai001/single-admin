import request from '@/utils/requestUtil.js'

/**
 * 根据ID查询
 * @param {number} id
 * @returns {Promise}
 */
export function getDictTypeById(id) {
  return request.get(`/dict/dict-type/` + id);
}

/**
 * 查询所有
 * @returns {Promise}
 */
export function listDictType() {
  return request.get(`/dict/dict-type/list`);
}

/**
 * 分页查询
 * @param {object} params
 * @param {number} params.current 当前页
 * @param {number} params.size 每页大小
 * @param {Long} params.id 
 * @param {String} params.dictName 字典名称（如：性别）
 * @param {String} params.dictType 字典类型（唯一标识，如：sex）
 * @param {Boolean} params.status 状态（0停用 1启用）
 * @returns {Promise}
 */
export function pageDictType(params) {
  return request.get(`/dict/dict-type/page`, { params });
}

/**
 * 新增
 * @param {object} params 
 * @param {Long} params.id 
 * @param {String} params.dictName 字典名称（如：性别）
 * @param {String} params.dictType 字典类型（唯一标识，如：sex）
 * @param {Boolean} params.status 状态（0停用 1启用）
 * @returns {Promise}
 */
export function saveDictType(params) {
  return request.post(`/dict/dict-type`, params);
}

/**
 * 更新
 * @param {object} params 
 * @param {Long} params.id 
 * @param {String} params.dictName 字典名称（如：性别）
 * @param {String} params.dictType 字典类型（唯一标识，如：sex）
 * @param {Boolean} params.status 状态（0停用 1启用）
 * @returns {Promise}
 */
export function updateDictType(params) {
  return request.put(`/dict/dict-type`, params);
}

/**
 * 删除
 * @param {number} id
 * @returns {Promise}
 */
export function deleteDictType(id) {
  return request.delete(`/dict/dict-type/` + id);
}

/**
 * 批量删除
 * @param {number[]} ids
 * @returns {Promise}
 */
export function batchDeleteDictType(ids) {
  return request.delete(`/dict/dict-type/batch`, { data: ids });
}
