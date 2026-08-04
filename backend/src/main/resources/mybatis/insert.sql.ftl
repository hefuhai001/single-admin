<#-- 生成权限 INSERT SQL 模板 -->
<#-- 变量说明:
  - table.name: 表名
  - table.comment: 表注释
  - table.fields: 字段列表
  - entity: 实体类名
-->

-- ============================================
-- 表: ${table.name}
-- 说明: ${table.comment!}
-- 生成时间: ${date}
-- ============================================

<#-- 提取实体名称（去掉Entity后缀） -->
<#assign entityName = entity?replace("Entity", "")>
<#assign entityNameLower = entityName?uncap_first>
<#assign entityNameCapital = entityName?cap_first>

<#-- 生成路由权限 -->
INSERT INTO sys_permission (permission_code, permission_name, permission_type, parent_id, route_path, component_path, redirect, icon, sort, visible, status, created_at, updated_at)
VALUES ('${entityNameLower}:list', '${table.comment!}管理', 2, 1, '/system/${entityNameLower}', '/system/${entityNameLower}/index', null, 'Document', 1, 1, 1, NOW(), NOW());

SET @parent_id = LAST_INSERT_ID();

<#-- 生成接口权限 -->
INSERT INTO sys_permission (permission_code, permission_name, permission_type, parent_id, route_path, component_path, redirect, icon, sort, visible, status, created_at, updated_at)
VALUES ('${entityNameLower}:add', '${table.comment!}新增', 1, @parent_id, null, null, null, null, 1, 1, 1, NOW(), NOW());

INSERT INTO sys_permission (permission_code, permission_name, permission_type, parent_id, route_path, component_path, redirect, icon, sort, visible, status, created_at, updated_at)
VALUES ('${entityNameLower}:delete', '${table.comment!}删除', 1, @parent_id, null, null, null, null, 2, 1, 1, NOW(), NOW());

INSERT INTO sys_permission (permission_code, permission_name, permission_type, parent_id, route_path, component_path, redirect, icon, sort, visible, status, created_at, updated_at)
VALUES ('${entityNameLower}:update', '${table.comment!}更新', 1, @parent_id, null, null, null, null, 3, 1, 1, NOW(), NOW());

INSERT INTO sys_permission (permission_code, permission_name, permission_type, parent_id, route_path, component_path, redirect, icon, sort, visible, status, created_at, updated_at)
VALUES ('${entityNameLower}:query', '${table.comment!}查询', 1, @parent_id, null, null, null, null, 4, 1, 1, NOW(), NOW());
