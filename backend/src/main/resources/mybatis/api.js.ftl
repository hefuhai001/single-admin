import request from '@/utils/requestUtil.js'

<#assign entityPath = controllerMappingHyphen!table.entityPath>
/**
 * 根据ID查询${table.comment!}
 * @param {number} id
 * @returns {Promise}
 */
export function get${table.entityPath?cap_first?replace('Entity','')}ById(id) {
  return request.get(`/${package.ModuleName!''}/${entityPath?replace('-entity','')}/` + id);
}

/**
 * 查询所有${table.comment!}
 * @returns {Promise}
 */
export function list${table.entityPath?cap_first?replace('Entity','')}() {
  return request.get(`/${package.ModuleName!''}/${entityPath?replace('-entity','')}/list`);
}

/**
 * 分页查询${table.comment!}
 * @param {object} params
 * @param {number} params.current 当前页
 * @param {number} params.size 每页大小
<#list table.fields as field>
 * @param {${field.propertyType}} params.${field.propertyName} ${field.comment!field.propertyName}
</#list>
 * @returns {Promise}
 */
export function page${table.entityPath?cap_first?replace('Entity','')}(params) {
  return request.get(`/${package.ModuleName!''}/${entityPath?replace('-entity','')}/page`, { params });
}

/**
 * 新增${table.comment!}
 * @param {object} params ${table.comment!}
<#list table.fields as field>
 * @param {${field.propertyType}} params.${field.propertyName} ${field.comment!field.propertyName}
</#list>
 * @returns {Promise}
 */
export function save${table.entityPath?cap_first?replace('Entity','')}(params) {
  return request.post(`/${package.ModuleName!''}/${entityPath?replace('-entity','')}`, params);
}

/**
 * 更新${table.comment!}
 * @param {object} params ${table.comment!}
<#list table.fields as field>
 * @param {${field.propertyType}} params.${field.propertyName} ${field.comment!field.propertyName}
</#list>
 * @returns {Promise}
 */
export function update${table.entityPath?cap_first?replace('Entity','')}(params) {
  return request.put(`/${package.ModuleName!''}/${entityPath?replace('-entity','')}`, params);
}

/**
 * 删除${table.comment!}
 * @param {number} id
 * @returns {Promise}
 */
export function delete${table.entityPath?cap_first?replace('Entity','')}(id) {
  return request.delete(`/${package.ModuleName!''}/${entityPath?replace('-entity','')}/` + id);
}

/**
 * 批量删除${table.comment!}
 * @param {number[]} ids
 * @returns {Promise}
 */
export function batchDelete${table.entityPath?cap_first?replace('Entity','')}(ids) {
  return request.delete(`/${package.ModuleName!''}/${entityPath?replace('-entity','')}/batch`, { data: ids });
}
