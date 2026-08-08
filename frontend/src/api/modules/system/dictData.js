import request from '@/utils/requestUtil.js'

/**
 * 根据ID查询
 * @param {number} id
 * @returns {Promise}
 */
export function getDictDataById(id) {
    return request.get(`/dict/dict-data/` + id)
}

/**
 * 查询所有
 * @returns {Promise}
 */
export function listDictData() {
    return request.get(`/dict/dict-data/list`)
}

/**
 * 分页查询
 * @param {object} params
 * @param {number} params.current 当前页
 * @param {number} params.size 每页大小
 * @param {Long} params.id
 * @param {String} params.dictType 所属字典类型（关联dict_type）
 * @param {String} params.dictLabel 显示标签（如：男、女）
 * @param {String} params.dictValue 存储值（如：1、0）
 * @param {Integer} params.dictSort 排序
 * @param {Boolean} params.status 状态
 * @param {Boolean} params.isDefault 是否默认值
 * @param {String} params.cssClass 样式属性（前端用）
 * @param {String} params.listClass 表格列表样式
 * @returns {Promise}
 */
export function pageDictData(params) {
    return request.get(`/dict/dict-data/page`, { params })
}

/**
 * 新增
 * @param {object} params
 * @param {Long} params.id
 * @param {String} params.dictType 所属字典类型（关联dict_type）
 * @param {String} params.dictLabel 显示标签（如：男、女）
 * @param {String} params.dictValue 存储值（如：1、0）
 * @param {Integer} params.dictSort 排序
 * @param {Boolean} params.status 状态
 * @param {Boolean} params.isDefault 是否默认值
 * @param {String} params.cssClass 样式属性（前端用）
 * @param {String} params.listClass 表格列表样式
 * @returns {Promise}
 */
export function saveDictData(params) {
    return request.post(`/dict/dict-data`, params)
}

/**
 * 更新
 * @param {object} params
 * @param {Long} params.id
 * @param {String} params.dictType 所属字典类型（关联dict_type）
 * @param {String} params.dictLabel 显示标签（如：男、女）
 * @param {String} params.dictValue 存储值（如：1、0）
 * @param {Integer} params.dictSort 排序
 * @param {Boolean} params.status 状态
 * @param {Boolean} params.isDefault 是否默认值
 * @param {String} params.cssClass 样式属性（前端用）
 * @param {String} params.listClass 表格列表样式
 * @returns {Promise}
 */
export function updateDictData(params) {
    return request.put(`/dict/dict-data`, params)
}

/**
 * 删除
 * @param {number} id
 * @returns {Promise}
 */
export function deleteDictData(id) {
    return request.delete(`/dict/dict-data/` + id)
}

/**
 * 批量删除
 * @param {number[]} ids
 * @returns {Promise}
 */
export function batchDeleteDictData(ids) {
    return request.delete(`/dict/dict-data/batch`, { data: ids })
}
