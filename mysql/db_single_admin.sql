/*
 Navicat Premium Dump SQL

 Source Server         : 127.0.0.1
 Source Server Type    : MySQL
 Source Server Version : 80045 (8.0.45)
 Source Host           : 127.0.0.1:3306
 Source Schema         : db_single_admin

 Target Server Type    : MySQL
 Target Server Version : 80045 (8.0.45)
 File Encoding         : 65001

 Date: 08/08/2026 02:16:28
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for doc_feedbacks
-- ----------------------------
DROP TABLE IF EXISTS `doc_feedbacks`;
CREATE TABLE `doc_feedbacks`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '反馈ID',
  `institution_id` bigint NOT NULL COMMENT '机构ID',
  `feedback_type` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '反馈类型: question-疑问, suggestion-建议, complaint-投诉, issue-问题报告, other-其他',
  `feedback_title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '反馈标题',
  `feedback_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '反馈内容',
  `attachment_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '附件URL',
  `priority` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'medium' COMMENT '优先级: low-低, medium-中, high-高, urgent-紧急',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'pending' COMMENT '状态: pending-待处理, processing-处理中, resolved-已解决, closed-已关闭, rejected-已驳回',
  `reply_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '回复内容',
  `replied_by` bigint NULL DEFAULT NULL COMMENT '回复人ID',
  `replied_at` datetime NULL DEFAULT NULL COMMENT '回复时间',
  `feedback_by` bigint NULL DEFAULT NULL COMMENT '反馈人ID',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint(1) NULL DEFAULT 0 COMMENT '软删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_feedback_type`(`feedback_type` ASC) USING BTREE,
  INDEX `idx_institution_id`(`institution_id` ASC) USING BTREE,
  INDEX `idx_is_deleted`(`is_deleted` ASC) USING BTREE,
  INDEX `idx_priority`(`priority` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  CONSTRAINT `fk_if_institution` FOREIGN KEY (`institution_id`) REFERENCES `sys_institutions` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '机构反馈表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of doc_feedbacks
-- ----------------------------

-- ----------------------------
-- Table structure for doc_guides
-- ----------------------------
DROP TABLE IF EXISTS `doc_guides`;
CREATE TABLE `doc_guides`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '指南ID',
  `institution_id` bigint NOT NULL COMMENT '机构ID',
  `guide_title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '指南标题',
  `guide_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '指南内容(富文本)',
  `guide_file_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '指南附件URL(PDF/Word等)',
  `guide_type` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'general' COMMENT '指南类型: general-通用指南, operation-操作指南, quality-质控指南, other-其他',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'active' COMMENT '状态: active-有效, expired-已过期, archived-已归档',
  `published_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
  `created_by` bigint NULL DEFAULT NULL COMMENT '发布人ID',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint(1) NULL DEFAULT 0 COMMENT '软删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_guide_type`(`guide_type` ASC) USING BTREE,
  INDEX `idx_institution_id`(`institution_id` ASC) USING BTREE,
  INDEX `idx_is_deleted`(`is_deleted` ASC) USING BTREE,
  INDEX `idx_published_at`(`published_at` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  CONSTRAINT `fk_ig_institution` FOREIGN KEY (`institution_id`) REFERENCES `sys_institutions` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '机构指南' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of doc_guides
-- ----------------------------

-- ----------------------------
-- Table structure for sys_config
-- ----------------------------
DROP TABLE IF EXISTS `sys_config`;
CREATE TABLE `sys_config`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `config_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '配置键（唯一标识，如：register.enable）',
  `config_value` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '配置值（存储具体配置内容）',
  `config_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '配置名称（如：是否开启注册）',
  `config_group` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'system' COMMENT '配置分组（system/email/sms/oss等）',
  `config_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'string' COMMENT '配置类型（string/integer/boolean/array/json）',
  `is_system` tinyint(1) NULL DEFAULT 0 COMMENT '是否系统内置（1-是，不可删除；0-否）',
  `status` tinyint(1) NULL DEFAULT 1 COMMENT '状态（0停用 1启用）',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注说明',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_config_key`(`config_key` ASC) USING BTREE,
  INDEX `idx_config_group`(`config_group` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 23 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统配置表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_config
-- ----------------------------
INSERT INTO `sys_config` VALUES (1, 'register.enable', 'true', '是否开启注册', 'system', 'boolean', 1, 1, 'true-开启，false-关闭', '2026-08-05 23:31:49', '2026-08-05 23:31:49');
INSERT INTO `sys_config` VALUES (2, 'register.verify_code.enable', 'true', '注册是否需要验证码', 'system', 'boolean', 1, 1, 'true-需要，false-不需要', '2026-08-05 23:31:49', '2026-08-05 23:31:49');
INSERT INTO `sys_config` VALUES (3, 'register.default_role', 'user', '注册默认角色', 'system', 'string', 1, 1, 'user-普通用户，vip-会员用户', '2026-08-05 23:31:49', '2026-08-05 23:31:49');
INSERT INTO `sys_config` VALUES (4, 'register.default_institution', 'main', '注册默认机构', 'system', 'string', 1, 1, '', '2026-08-05 23:31:49', '2026-08-08 02:06:37');
INSERT INTO `sys_config` VALUES (5, 'register.whitelist.enable', 'false', '是否开启客户端', 'system', 'boolean', 0, 1, '开启后登录跳转到客户端', '2026-08-05 23:31:49', '2026-08-05 23:31:49');
INSERT INTO `sys_config` VALUES (6, 'email.verify.enable', 'true', '是否开启邮箱验证', 'email', 'boolean', 1, 1, 'true-开启，false-关闭', '2026-08-05 23:31:49', '2026-08-05 23:31:49');
INSERT INTO `sys_config` VALUES (7, 'email.smtp.host', 'smtp.qq.com', 'SMTP服务器地址', 'email', 'string', 1, 1, 'QQ邮箱SMTP服务器', '2026-08-05 23:31:49', '2026-08-05 23:31:49');
INSERT INTO `sys_config` VALUES (8, 'email.smtp.port', '465', 'SMTP端口', 'email', 'integer', 1, 1, 'SSL端口465，TLS端口587', '2026-08-05 23:31:49', '2026-08-05 23:31:49');
INSERT INTO `sys_config` VALUES (9, 'email.sender', 'your_email@qq.com', '发件人邮箱', 'email', 'string', 1, 1, '发送邮件的邮箱地址', '2026-08-05 23:31:49', '2026-08-05 23:31:49');
INSERT INTO `sys_config` VALUES (10, 'email.sender.password', 'your_auth_code', '发件人授权码', 'email', 'string', 1, 1, '邮箱授权码或密码', '2026-08-05 23:31:49', '2026-08-05 23:31:49');
INSERT INTO `sys_config` VALUES (11, 'email.verify.expire', '300', '验证码有效期（秒）', 'email', 'integer', 1, 1, '默认5分钟', '2026-08-05 23:31:49', '2026-08-05 23:31:49');
INSERT INTO `sys_config` VALUES (12, 'login.max_attempts', '5', '登录最大尝试次数', 'login', 'integer', 1, 1, '超过锁定账号', '2026-08-05 23:31:49', '2026-08-05 23:31:49');
INSERT INTO `sys_config` VALUES (13, 'login.lock_duration', '1800', '账号锁定时长（秒）', 'login', 'integer', 1, 1, '默认30分钟', '2026-08-05 23:31:49', '2026-08-05 23:31:49');
INSERT INTO `sys_config` VALUES (14, 'login.single_device', 'true', '是否单设备登录', 'login', 'boolean', 0, 1, 'true-挤掉旧设备，false-允许多设备', '2026-08-05 23:31:49', '2026-08-05 23:31:49');
INSERT INTO `sys_config` VALUES (15, 'sms.enable', 'false', '是否开启短信功能', 'sms', 'boolean', 0, 1, 'true-开启，false-关闭', '2026-08-05 23:31:49', '2026-08-05 23:31:49');
INSERT INTO `sys_config` VALUES (16, 'sms.provider', 'aliyun', '短信服务商', 'sms', 'string', 0, 1, 'aliyun-阿里云，tencent-腾讯云', '2026-08-05 23:31:49', '2026-08-05 23:31:49');
INSERT INTO `sys_config` VALUES (17, 'sms.sign_name', '通用后台', '短信签名', 'sms', 'string', 0, 1, '短信签名内容', '2026-08-05 23:31:49', '2026-08-05 23:31:49');
INSERT INTO `sys_config` VALUES (18, 'oss.enable', 'false', '是否开启OSS存储', 'oss', 'boolean', 0, 1, 'true-开启，false-关闭', '2026-08-05 23:31:49', '2026-08-05 23:31:49');
INSERT INTO `sys_config` VALUES (19, 'oss.provider', 'aliyun', 'OSS服务商', 'oss', 'string', 0, 1, 'aliyun-阿里云，qiniu-七牛云', '2026-08-05 23:31:49', '2026-08-05 23:31:49');
INSERT INTO `sys_config` VALUES (20, 'oss.bucket_name', 'my-bucket', '存储桶名称', 'oss', 'string', 0, 1, 'OSS Bucket名称', '2026-08-05 23:31:49', '2026-08-05 23:31:49');
INSERT INTO `sys_config` VALUES (21, 'oss.upload_path', '/uploads', '上传路径前缀', 'oss', 'string', 0, 1, '文件存储路径', '2026-08-05 23:31:49', '2026-08-05 23:31:49');
INSERT INTO `sys_config` VALUES (22, 'oss.max_file_size', '10485760', '最大文件大小（字节）', 'oss', 'integer', 0, 1, '默认10MB', '2026-08-05 23:31:49', '2026-08-05 23:31:49');

-- ----------------------------
-- Table structure for sys_dict_data
-- ----------------------------
DROP TABLE IF EXISTS `sys_dict_data`;
CREATE TABLE `sys_dict_data`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `dict_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '所属字典类型（关联dict_type）',
  `dict_label` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '显示标签（如：男、女）',
  `dict_value` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '存储值（如：1、0）',
  `dict_sort` int NULL DEFAULT 0 COMMENT '排序',
  `status` tinyint(1) NULL DEFAULT 1 COMMENT '状态',
  `is_default` tinyint(1) NULL DEFAULT 0 COMMENT '是否默认值',
  `css_class` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '样式属性（前端用）',
  `list_class` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '表格列表样式',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_dict_type`(`dict_type` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 15 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_dict_data
-- ----------------------------
INSERT INTO `sys_dict_data` VALUES (1, 'sex', '男', '1', 1, 1, 1, 'primary', 'tag-primary');
INSERT INTO `sys_dict_data` VALUES (2, 'sex', '女', '0', 2, 1, 0, 'danger', 'tag-danger');
INSERT INTO `sys_dict_data` VALUES (3, 'sex', '未知', '2', 3, 0, 0, 'info', 'tag-info');
INSERT INTO `sys_dict_data` VALUES (4, 'user_status', '正常', '1', 1, 1, 1, 'success', 'tag-success');
INSERT INTO `sys_dict_data` VALUES (5, 'user_status', '禁用', '0', 2, 1, 0, 'danger', 'tag-danger');
INSERT INTO `sys_dict_data` VALUES (6, 'user_status', '未激活', '2', 3, 1, 0, 'warning', 'tag-warning');
INSERT INTO `sys_dict_data` VALUES (7, 'order_status', '待支付', '0', 1, 1, 1, 'warning', 'tag-warning');
INSERT INTO `sys_dict_data` VALUES (8, 'order_status', '已支付', '1', 2, 1, 0, 'success', 'tag-success');
INSERT INTO `sys_dict_data` VALUES (9, 'order_status', '已发货', '2', 3, 1, 0, 'primary', 'tag-primary');
INSERT INTO `sys_dict_data` VALUES (10, 'order_status', '已完成', '3', 4, 1, 0, 'info', 'tag-info');
INSERT INTO `sys_dict_data` VALUES (11, 'order_status', '已取消', '4', 5, 1, 0, 'danger', 'tag-danger');
INSERT INTO `sys_dict_data` VALUES (12, 'pay_method', '微信支付', 'wechat', 1, 1, 1, NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (13, 'pay_method', '支付宝支付', 'alipay', 2, 1, 0, NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (14, 'pay_method', '银行卡支付', 'bank', 3, 1, 0, NULL, NULL);

-- ----------------------------
-- Table structure for sys_dict_type
-- ----------------------------
DROP TABLE IF EXISTS `sys_dict_type`;
CREATE TABLE `sys_dict_type`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `dict_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '字典名称（如：性别）',
  `dict_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '字典类型（唯一标识，如：sex）',
  `status` tinyint(1) NULL DEFAULT 1 COMMENT '状态（0停用 1启用）',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_dict_type`(`dict_type` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_dict_type
-- ----------------------------
INSERT INTO `sys_dict_type` VALUES (1, '性别', 'sex', 1);
INSERT INTO `sys_dict_type` VALUES (2, '用户状态', 'user_status', 0);
INSERT INTO `sys_dict_type` VALUES (3, '订单状态', 'order_status', 1);
INSERT INTO `sys_dict_type` VALUES (4, '支付方式', 'pay_method', 1);

-- ----------------------------
-- Table structure for sys_institutions
-- ----------------------------
DROP TABLE IF EXISTS `sys_institutions`;
CREATE TABLE `sys_institutions`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '机构ID',
  `parent_id` bigint NULL DEFAULT NULL COMMENT '父机构ID(上级机构)',
  `institution_code` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '机构编码',
  `institution_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '机构名称',
  `institution_level` tinyint NULL DEFAULT 1 COMMENT '层级: 1-顶级机构,2-子机构,3-孙机构',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态: 0-禁用,1-启用',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_parent_id`(`parent_id` ASC) USING BTREE,
  INDEX `idx_institution_code`(`institution_code` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  CONSTRAINT `fk_institution_parent` FOREIGN KEY (`parent_id`) REFERENCES `sys_institutions` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '机构(支持父子层级)' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_institutions
-- ----------------------------
INSERT INTO `sys_institutions` VALUES (1, NULL, 'main', '深圳总公司', 1, 1, '2026-04-18 18:58:26', '2026-08-08 02:14:18');
INSERT INTO `sys_institutions` VALUES (2, 1, 'nc', '南昌分公司', 1, 1, '2026-04-18 18:58:52', '2026-08-08 02:14:23');
INSERT INTO `sys_institutions` VALUES (3, 1, 'cs', '长沙分公司', 1, 1, '2026-04-18 18:58:52', '2026-08-08 02:14:28');

-- ----------------------------
-- Table structure for sys_permission
-- ----------------------------
DROP TABLE IF EXISTS `sys_permission`;
CREATE TABLE `sys_permission`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `permission_code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `permission_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `permission_type` int NULL DEFAULT NULL COMMENT '权限类型：1-接口权限 2-路由权限',
  `parent_id` bigint NULL DEFAULT NULL COMMENT '父权限ID，0表示顶级菜单',
  `route_path` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '路由路径',
  `component_path` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '组件路径',
  `redirect` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '重定向路径',
  `icon` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '图标',
  `sort` int NULL DEFAULT NULL COMMENT '排序',
  `visible` int NULL DEFAULT NULL COMMENT '是否可见：1-可见 0-隐藏',
  `status` int NULL DEFAULT NULL COMMENT '状态：1-启用 0-禁用',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `permission_code`(`permission_code` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 164 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '权限（1:接口权限 2:路由权限）' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_permission
-- ----------------------------
INSERT INTO `sys_permission` VALUES (1, 'system', '系统管理', 2, 0, '/admin/system', '', '/admin/system/user/index', 'Menu', 2, 1, 1, '2026-04-01 14:27:20', '2026-04-28 23:11:40');
INSERT INTO `sys_permission` VALUES (2, 'system:user', '用户管理', 2, 1, '/admin/system/user', '/admin/system/user/index', NULL, 'User', 4, 1, 1, '2026-04-01 14:27:20', '2026-04-01 14:27:20');
INSERT INTO `sys_permission` VALUES (3, 'system:role', '角色管理', 2, 1, '/admin/system/role', '/admin/system/role/index', NULL, 'Headset', 5, 1, 1, '2026-04-01 14:27:20', '2026-04-01 14:27:20');
INSERT INTO `sys_permission` VALUES (4, 'system:permission', '权限管理', 2, 1, '/admin/system/permission', '/admin/system/permission/index', NULL, 'Lock', 6, 1, 1, '2026-04-01 14:27:20', '2026-04-01 14:27:20');
INSERT INTO `sys_permission` VALUES (5, 'dashboard', '首页', 2, 0, '/admin/dashboard', '', '/admin/dashboard/index', 'Platform', 1, 1, 1, '2026-04-01 14:27:20', '2026-04-28 23:11:40');
INSERT INTO `sys_permission` VALUES (6, 'dashboard:home', '仪表盘', 2, 5, '/admin/dashboard/index', '/admin/dashboard/index', NULL, 'Position', 1, 1, 1, '2026-04-01 14:27:20', '2026-04-19 02:38:15');
INSERT INTO `sys_permission` VALUES (7, 'user:add', '用户新增', 1, 2, NULL, NULL, NULL, NULL, 1, 1, 1, '2026-04-01 14:27:20', '2026-04-01 14:27:20');
INSERT INTO `sys_permission` VALUES (8, 'user:delete', '用户删除', 1, 2, NULL, NULL, NULL, NULL, 2, 1, 1, '2026-04-01 14:27:20', '2026-04-01 14:27:20');
INSERT INTO `sys_permission` VALUES (9, 'user:update', '用户更新', 1, 2, NULL, NULL, NULL, NULL, 3, 1, 1, '2026-04-01 14:27:20', '2026-04-01 14:27:20');
INSERT INTO `sys_permission` VALUES (10, 'user:query', '用户查询', 1, 2, NULL, NULL, NULL, NULL, 4, 1, 1, '2026-04-01 14:27:20', '2026-04-01 14:27:20');
INSERT INTO `sys_permission` VALUES (11, 'role:add', '角色新增', 1, 3, NULL, NULL, NULL, NULL, 1, 1, 1, '2026-04-01 14:27:20', '2026-04-01 14:27:20');
INSERT INTO `sys_permission` VALUES (12, 'role:delete', '角色删除', 1, 3, NULL, NULL, NULL, NULL, 2, 1, 1, '2026-04-01 14:27:20', '2026-04-01 14:27:20');
INSERT INTO `sys_permission` VALUES (13, 'role:update', '角色更新', 1, 3, NULL, NULL, NULL, NULL, 3, 1, 1, '2026-04-01 14:27:20', '2026-04-01 14:27:20');
INSERT INTO `sys_permission` VALUES (14, 'role:query', '角色查询', 1, 3, NULL, NULL, NULL, NULL, 4, 1, 1, '2026-04-01 14:27:20', '2026-04-01 14:27:20');
INSERT INTO `sys_permission` VALUES (15, 'permission:add', '权限新增', 1, 4, NULL, NULL, NULL, NULL, 1, 1, 1, '2026-04-01 14:27:20', '2026-04-01 14:27:20');
INSERT INTO `sys_permission` VALUES (16, 'permission:delete', '权限删除', 1, 4, NULL, NULL, NULL, NULL, 2, 1, 1, '2026-04-01 14:27:20', '2026-04-01 14:27:20');
INSERT INTO `sys_permission` VALUES (17, 'permission:update', '权限更新', 1, 4, NULL, NULL, NULL, NULL, 3, 1, 1, '2026-04-01 14:27:20', '2026-04-01 14:27:20');
INSERT INTO `sys_permission` VALUES (18, 'permission:query', '权限查询', 1, 4, NULL, NULL, NULL, NULL, 4, 1, 1, '2026-04-01 14:27:20', '2026-04-01 14:27:20');
INSERT INTO `sys_permission` VALUES (19, 'guides:list', '指南管理', 2, 51, '/admin/document/guides', '/admin/document/guides/index', NULL, 'Reading', 5, 1, 1, '2026-04-15 01:43:53', '2026-04-18 19:06:05');
INSERT INTO `sys_permission` VALUES (20, 'guides:add', '指南新增', 1, 19, NULL, NULL, NULL, NULL, 1, 1, 1, '2026-04-15 01:43:53', '2026-04-15 01:43:53');
INSERT INTO `sys_permission` VALUES (21, 'guides:delete', '指南删除', 1, 19, NULL, NULL, NULL, NULL, 2, 1, 1, '2026-04-15 01:43:53', '2026-04-15 01:43:53');
INSERT INTO `sys_permission` VALUES (22, 'guides:update', '指南更新', 1, 19, NULL, NULL, NULL, NULL, 3, 1, 1, '2026-04-15 01:43:53', '2026-04-15 01:43:53');
INSERT INTO `sys_permission` VALUES (23, 'guides:query', '指南查询', 1, 19, NULL, NULL, NULL, NULL, 4, 1, 1, '2026-04-15 01:43:53', '2026-04-15 01:43:53');
INSERT INTO `sys_permission` VALUES (24, 'feedbacks:list', '反馈管理', 2, 51, '/admin/document/feedbacks', '/admin/document/feedbacks/index', NULL, 'Phone', 6, 1, 1, '2026-04-15 01:44:22', '2026-04-18 19:06:05');
INSERT INTO `sys_permission` VALUES (25, 'feedbacks:add', '反馈新增', 1, 24, NULL, NULL, NULL, NULL, 1, 1, 1, '2026-04-15 01:44:23', '2026-04-15 01:44:23');
INSERT INTO `sys_permission` VALUES (26, 'feedbacks:delete', '反馈删除', 1, 24, NULL, NULL, NULL, NULL, 2, 1, 1, '2026-04-15 01:44:23', '2026-04-15 01:44:23');
INSERT INTO `sys_permission` VALUES (27, 'feedbacks:update', '反馈更新', 1, 24, NULL, NULL, NULL, NULL, 3, 1, 1, '2026-04-15 01:44:23', '2026-04-15 01:44:23');
INSERT INTO `sys_permission` VALUES (28, 'feedbacks:query', '反馈查询', 1, 24, NULL, NULL, NULL, NULL, 4, 1, 1, '2026-04-15 01:44:23', '2026-04-15 01:44:23');
INSERT INTO `sys_permission` VALUES (29, 'institutions:list', '机构管理', 2, 1, '/admin/system/institutions', '/admin/system/institutions/index', NULL, 'OfficeBuilding', 1, 1, 1, '2026-04-15 01:50:42', '2026-04-18 19:06:05');
INSERT INTO `sys_permission` VALUES (30, 'institutions:add', '机构新增', 1, 29, NULL, NULL, NULL, NULL, 1, 1, 1, '2026-04-15 01:50:42', '2026-04-15 01:50:42');
INSERT INTO `sys_permission` VALUES (31, 'institutions:delete', '机构删除', 1, 29, NULL, NULL, NULL, NULL, 2, 1, 1, '2026-04-15 01:50:42', '2026-04-15 01:50:42');
INSERT INTO `sys_permission` VALUES (32, 'institutions:update', '机构更新', 1, 29, NULL, NULL, NULL, NULL, 3, 1, 1, '2026-04-15 01:50:42', '2026-04-15 01:50:42');
INSERT INTO `sys_permission` VALUES (33, 'institutions:query', '机构查询', 1, 29, NULL, NULL, NULL, NULL, 4, 1, 1, '2026-04-15 01:50:42', '2026-04-15 01:50:42');
INSERT INTO `sys_permission` VALUES (34, 'minio', '文件管理', 2, 0, '/admin/minio', '', '/admin/minio/index', 'UploadFilled', 4, 1, 1, '2026-06-17 22:43:07', '2026-06-17 22:43:07');
INSERT INTO `sys_permission` VALUES (35, 'minio:list', '文件列表', 2, 34, '/admin/minio/index', '/admin/minio/index', NULL, 'Folder', 1, 1, 1, '2026-06-17 22:43:07', '2026-06-17 22:43:07');
INSERT INTO `sys_permission` VALUES (36, 'minio:bucket:query', '查询存储桶', 1, 35, NULL, NULL, NULL, NULL, 1, 1, 1, '2026-06-17 22:43:07', '2026-06-17 22:43:07');
INSERT INTO `sys_permission` VALUES (37, 'minio:bucket:add', '创建存储桶', 1, 35, NULL, NULL, NULL, NULL, 2, 1, 1, '2026-06-17 22:43:07', '2026-06-17 22:43:07');
INSERT INTO `sys_permission` VALUES (38, 'minio:bucket:delete', '删除存储桶', 1, 35, NULL, NULL, NULL, NULL, 3, 1, 1, '2026-06-17 22:43:07', '2026-06-17 22:43:07');
INSERT INTO `sys_permission` VALUES (39, 'minio:object:query', '查询文件', 1, 35, NULL, NULL, NULL, NULL, 4, 1, 1, '2026-06-17 22:43:07', '2026-06-17 22:43:07');
INSERT INTO `sys_permission` VALUES (40, 'minio:object:upload', '上传文件', 1, 35, NULL, NULL, NULL, NULL, 5, 1, 1, '2026-06-17 22:43:07', '2026-06-17 22:43:07');
INSERT INTO `sys_permission` VALUES (41, 'minio:object:download', '下载文件', 1, 35, NULL, NULL, NULL, NULL, 6, 1, 1, '2026-06-17 22:43:07', '2026-06-17 22:43:07');
INSERT INTO `sys_permission` VALUES (42, 'minio:object:preview', '预览文件', 1, 35, NULL, NULL, NULL, NULL, 7, 1, 1, '2026-06-17 22:43:07', '2026-06-17 22:43:07');
INSERT INTO `sys_permission` VALUES (43, 'minio:object:delete', '删除文件', 1, 35, NULL, NULL, NULL, NULL, 8, 1, 1, '2026-06-17 22:43:07', '2026-06-17 22:43:07');
INSERT INTO `sys_permission` VALUES (44, 'job', '定时任务', 2, 0, '/admin/task/job', '', '/admin/task/job/index', 'List', 5, 1, 1, '2026-07-04 01:20:00', '2026-07-04 01:20:00');
INSERT INTO `sys_permission` VALUES (45, 'job:list', '定时任务管理', 2, 44, '/admin/task/job', '/admin/task/job/index', NULL, 'Timer', 7, 1, 1, '2026-07-04 01:20:00', '2026-07-04 01:20:00');
INSERT INTO `sys_permission` VALUES (46, 'job:query', '定时任务查询', 1, 45, NULL, NULL, NULL, NULL, 1, 1, 1, '2026-07-04 01:20:00', '2026-07-04 01:20:00');
INSERT INTO `sys_permission` VALUES (47, 'job:add', '定时任务新增', 1, 45, NULL, NULL, NULL, NULL, 2, 1, 1, '2026-07-04 01:20:00', '2026-07-04 01:20:00');
INSERT INTO `sys_permission` VALUES (48, 'job:delete', '定时任务删除', 1, 45, NULL, NULL, NULL, NULL, 3, 1, 1, '2026-07-04 01:20:00', '2026-07-04 01:20:00');
INSERT INTO `sys_permission` VALUES (49, 'job:update', '定时任务更新', 1, 45, NULL, NULL, NULL, NULL, 4, 1, 1, '2026-07-04 01:20:00', '2026-07-04 01:20:00');
INSERT INTO `sys_permission` VALUES (50, 'jobLog:list', '定时任务日志', 2, 44, '/admin/task/jobLog', '/admin/task/jobLog/index', NULL, 'Document', 8, 1, 1, '2026-07-04 01:20:00', '2026-07-04 01:20:00');
INSERT INTO `sys_permission` VALUES (51, 'guides', '文档管理', 2, 0, '/admin/document', '', '/admin/document/guide/index', 'Grid', 3, 1, 1, '2026-04-15 01:43:53', '2026-04-18 19:06:05');
INSERT INTO `sys_permission` VALUES (149, 'dictData:list', '字典数据', 2, 1, '/admin/system/dictData', '/admin/system/dictData/index', NULL, 'Document', 3, 0, 1, '2026-08-05 15:52:42', '2026-08-05 15:52:42');
INSERT INTO `sys_permission` VALUES (150, 'dictData:add', '数据新增', 1, 149, NULL, NULL, NULL, NULL, 1, 1, 1, '2026-08-05 15:52:42', '2026-08-05 15:52:42');
INSERT INTO `sys_permission` VALUES (151, 'dictData:delete', '数据删除', 1, 149, NULL, NULL, NULL, NULL, 2, 1, 1, '2026-08-05 15:52:42', '2026-08-05 15:52:42');
INSERT INTO `sys_permission` VALUES (152, 'dictData:update', '数据更新', 1, 149, NULL, NULL, NULL, NULL, 3, 1, 1, '2026-08-05 15:52:42', '2026-08-05 15:52:42');
INSERT INTO `sys_permission` VALUES (153, 'dictData:query', '数据查询', 1, 149, NULL, NULL, NULL, NULL, 4, 1, 1, '2026-08-05 15:52:42', '2026-08-05 15:52:42');
INSERT INTO `sys_permission` VALUES (154, 'dictType:list', '字典管理', 2, 1, '/admin/system/dictType', '/admin/system/dictType/index', NULL, 'Notebook', 2, 1, 1, '2026-08-05 15:52:42', '2026-08-05 15:52:42');
INSERT INTO `sys_permission` VALUES (155, 'dictType:add', '字典新增', 1, 154, NULL, NULL, NULL, NULL, 1, 1, 1, '2026-08-05 15:52:42', '2026-08-05 15:52:42');
INSERT INTO `sys_permission` VALUES (156, 'dictType:delete', '字典删除', 1, 154, NULL, NULL, NULL, NULL, 2, 1, 1, '2026-08-05 15:52:42', '2026-08-05 15:52:42');
INSERT INTO `sys_permission` VALUES (157, 'dictType:update', '字典更新', 1, 154, NULL, NULL, NULL, NULL, 3, 1, 1, '2026-08-05 15:52:42', '2026-08-05 15:52:42');
INSERT INTO `sys_permission` VALUES (158, 'dictType:query', '字典查询', 1, 154, NULL, NULL, NULL, NULL, 4, 1, 1, '2026-08-05 15:52:42', '2026-08-05 15:52:42');
INSERT INTO `sys_permission` VALUES (159, 'config:list', '系统配置', 2, 1, '/admin/system/config', '/admin/system/config/index', NULL, 'Document', 1, 1, 1, '2026-08-05 23:53:42', '2026-08-05 23:53:42');
INSERT INTO `sys_permission` VALUES (160, 'config:add', '配置新增', 1, 159, NULL, NULL, NULL, NULL, 1, 1, 1, '2026-08-05 23:53:42', '2026-08-05 23:53:42');
INSERT INTO `sys_permission` VALUES (161, 'config:delete', '配置删除', 1, 159, NULL, NULL, NULL, NULL, 2, 1, 1, '2026-08-05 23:53:42', '2026-08-05 23:53:42');
INSERT INTO `sys_permission` VALUES (162, 'config:update', '配置更新', 1, 159, NULL, NULL, NULL, NULL, 3, 1, 1, '2026-08-05 23:53:42', '2026-08-05 23:53:42');
INSERT INTO `sys_permission` VALUES (163, 'config:query', '配置查询', 1, 159, NULL, NULL, NULL, NULL, 4, 1, 1, '2026-08-05 23:53:42', '2026-08-05 23:53:42');

-- ----------------------------
-- Table structure for sys_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `role_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `role_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `description` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `status` int NULL DEFAULT 1,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `role_name`(`role_name` ASC) USING BTREE,
  UNIQUE INDEX `role_code`(`role_code` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '角色' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_role
-- ----------------------------
INSERT INTO `sys_role` VALUES (1, '超级管理员', 'admin', '拥有所有权限', 1, '2026-04-14 01:02:28', '2026-08-08 01:57:06');
INSERT INTO `sys_role` VALUES (2, '填报人员', 'user', '普通用户权限', 1, '2026-04-14 01:02:28', '2026-08-08 01:56:57');
INSERT INTO `sys_role` VALUES (3, '访客', 'guest', '访客权限', 1, '2026-04-14 01:02:28', '2026-04-14 01:02:28');

-- ----------------------------
-- Table structure for sys_role_permission
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_permission`;
CREATE TABLE `sys_role_permission`  (
  `role_id` bigint NOT NULL,
  `permission_id` bigint NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`role_id`, `permission_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '角色权限关联' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_role_permission
-- ----------------------------
INSERT INTO `sys_role_permission` VALUES (1, 1, '2026-08-06 00:20:11', '2026-08-06 00:20:11');
INSERT INTO `sys_role_permission` VALUES (1, 2, '2026-08-06 00:20:11', '2026-08-06 00:20:11');
INSERT INTO `sys_role_permission` VALUES (1, 3, '2026-08-06 00:20:11', '2026-08-06 00:20:11');
INSERT INTO `sys_role_permission` VALUES (1, 4, '2026-08-06 00:20:11', '2026-08-06 00:20:11');
INSERT INTO `sys_role_permission` VALUES (1, 5, '2026-08-06 00:20:11', '2026-08-06 00:20:11');
INSERT INTO `sys_role_permission` VALUES (1, 6, '2026-08-06 00:20:11', '2026-08-06 00:20:11');
INSERT INTO `sys_role_permission` VALUES (1, 7, '2026-08-06 00:20:11', '2026-08-06 00:20:11');
INSERT INTO `sys_role_permission` VALUES (1, 8, '2026-08-06 00:20:11', '2026-08-06 00:20:11');
INSERT INTO `sys_role_permission` VALUES (1, 9, '2026-08-06 00:20:11', '2026-08-06 00:20:11');
INSERT INTO `sys_role_permission` VALUES (1, 10, '2026-08-06 00:20:11', '2026-08-06 00:20:11');
INSERT INTO `sys_role_permission` VALUES (1, 11, '2026-08-06 00:20:11', '2026-08-06 00:20:11');
INSERT INTO `sys_role_permission` VALUES (1, 12, '2026-08-06 00:20:11', '2026-08-06 00:20:11');
INSERT INTO `sys_role_permission` VALUES (1, 13, '2026-08-06 00:20:11', '2026-08-06 00:20:11');
INSERT INTO `sys_role_permission` VALUES (1, 14, '2026-08-06 00:20:11', '2026-08-06 00:20:11');
INSERT INTO `sys_role_permission` VALUES (1, 15, '2026-08-06 00:20:11', '2026-08-06 00:20:11');
INSERT INTO `sys_role_permission` VALUES (1, 16, '2026-08-06 00:20:11', '2026-08-06 00:20:11');
INSERT INTO `sys_role_permission` VALUES (1, 17, '2026-08-06 00:20:11', '2026-08-06 00:20:11');
INSERT INTO `sys_role_permission` VALUES (1, 18, '2026-08-06 00:20:11', '2026-08-06 00:20:11');
INSERT INTO `sys_role_permission` VALUES (1, 19, '2026-08-06 00:20:11', '2026-08-06 00:20:11');
INSERT INTO `sys_role_permission` VALUES (1, 20, '2026-08-06 00:20:11', '2026-08-06 00:20:11');
INSERT INTO `sys_role_permission` VALUES (1, 21, '2026-08-06 00:20:11', '2026-08-06 00:20:11');
INSERT INTO `sys_role_permission` VALUES (1, 22, '2026-08-06 00:20:11', '2026-08-06 00:20:11');
INSERT INTO `sys_role_permission` VALUES (1, 23, '2026-08-06 00:20:11', '2026-08-06 00:20:11');
INSERT INTO `sys_role_permission` VALUES (1, 24, '2026-08-06 00:20:11', '2026-08-06 00:20:11');
INSERT INTO `sys_role_permission` VALUES (1, 25, '2026-08-06 00:20:11', '2026-08-06 00:20:11');
INSERT INTO `sys_role_permission` VALUES (1, 26, '2026-08-06 00:20:11', '2026-08-06 00:20:11');
INSERT INTO `sys_role_permission` VALUES (1, 27, '2026-08-06 00:20:11', '2026-08-06 00:20:11');
INSERT INTO `sys_role_permission` VALUES (1, 28, '2026-08-06 00:20:11', '2026-08-06 00:20:11');
INSERT INTO `sys_role_permission` VALUES (1, 29, '2026-08-06 00:20:11', '2026-08-06 00:20:11');
INSERT INTO `sys_role_permission` VALUES (1, 30, '2026-08-06 00:20:11', '2026-08-06 00:20:11');
INSERT INTO `sys_role_permission` VALUES (1, 31, '2026-08-06 00:20:11', '2026-08-06 00:20:11');
INSERT INTO `sys_role_permission` VALUES (1, 32, '2026-08-06 00:20:11', '2026-08-06 00:20:11');
INSERT INTO `sys_role_permission` VALUES (1, 33, '2026-08-06 00:20:11', '2026-08-06 00:20:11');
INSERT INTO `sys_role_permission` VALUES (1, 34, '2026-08-06 00:20:11', '2026-08-06 00:20:11');
INSERT INTO `sys_role_permission` VALUES (1, 35, '2026-08-06 00:20:11', '2026-08-06 00:20:11');
INSERT INTO `sys_role_permission` VALUES (1, 36, '2026-08-06 00:20:11', '2026-08-06 00:20:11');
INSERT INTO `sys_role_permission` VALUES (1, 37, '2026-08-06 00:20:11', '2026-08-06 00:20:11');
INSERT INTO `sys_role_permission` VALUES (1, 38, '2026-08-06 00:20:11', '2026-08-06 00:20:11');
INSERT INTO `sys_role_permission` VALUES (1, 39, '2026-08-06 00:20:11', '2026-08-06 00:20:11');
INSERT INTO `sys_role_permission` VALUES (1, 40, '2026-08-06 00:20:11', '2026-08-06 00:20:11');
INSERT INTO `sys_role_permission` VALUES (1, 41, '2026-08-06 00:20:11', '2026-08-06 00:20:11');
INSERT INTO `sys_role_permission` VALUES (1, 42, '2026-08-06 00:20:11', '2026-08-06 00:20:11');
INSERT INTO `sys_role_permission` VALUES (1, 43, '2026-08-06 00:20:11', '2026-08-06 00:20:11');
INSERT INTO `sys_role_permission` VALUES (1, 44, '2026-08-06 00:20:11', '2026-08-06 00:20:11');
INSERT INTO `sys_role_permission` VALUES (1, 45, '2026-08-06 00:20:11', '2026-08-06 00:20:11');
INSERT INTO `sys_role_permission` VALUES (1, 46, '2026-08-06 00:20:11', '2026-08-06 00:20:11');
INSERT INTO `sys_role_permission` VALUES (1, 47, '2026-08-06 00:20:11', '2026-08-06 00:20:11');
INSERT INTO `sys_role_permission` VALUES (1, 48, '2026-08-06 00:20:11', '2026-08-06 00:20:11');
INSERT INTO `sys_role_permission` VALUES (1, 49, '2026-08-06 00:20:11', '2026-08-06 00:20:11');
INSERT INTO `sys_role_permission` VALUES (1, 50, '2026-08-06 00:20:11', '2026-08-06 00:20:11');
INSERT INTO `sys_role_permission` VALUES (1, 51, '2026-08-06 00:20:11', '2026-08-06 00:20:11');
INSERT INTO `sys_role_permission` VALUES (1, 149, '2026-08-06 00:20:11', '2026-08-06 00:20:11');
INSERT INTO `sys_role_permission` VALUES (1, 150, '2026-08-06 00:20:11', '2026-08-06 00:20:11');
INSERT INTO `sys_role_permission` VALUES (1, 151, '2026-08-06 00:20:11', '2026-08-06 00:20:11');
INSERT INTO `sys_role_permission` VALUES (1, 152, '2026-08-06 00:20:11', '2026-08-06 00:20:11');
INSERT INTO `sys_role_permission` VALUES (1, 153, '2026-08-06 00:20:11', '2026-08-06 00:20:11');
INSERT INTO `sys_role_permission` VALUES (1, 154, '2026-08-06 00:20:11', '2026-08-06 00:20:11');
INSERT INTO `sys_role_permission` VALUES (1, 155, '2026-08-06 00:20:11', '2026-08-06 00:20:11');
INSERT INTO `sys_role_permission` VALUES (1, 156, '2026-08-06 00:20:11', '2026-08-06 00:20:11');
INSERT INTO `sys_role_permission` VALUES (1, 157, '2026-08-06 00:20:11', '2026-08-06 00:20:11');
INSERT INTO `sys_role_permission` VALUES (1, 158, '2026-08-06 00:20:11', '2026-08-06 00:20:11');
INSERT INTO `sys_role_permission` VALUES (1, 159, '2026-08-06 00:20:11', '2026-08-06 00:20:11');
INSERT INTO `sys_role_permission` VALUES (1, 160, '2026-08-06 00:20:11', '2026-08-06 00:20:11');
INSERT INTO `sys_role_permission` VALUES (1, 161, '2026-08-06 00:20:11', '2026-08-06 00:20:11');
INSERT INTO `sys_role_permission` VALUES (1, 162, '2026-08-06 00:20:11', '2026-08-06 00:20:11');
INSERT INTO `sys_role_permission` VALUES (1, 163, '2026-08-06 00:20:11', '2026-08-06 00:20:11');
INSERT INTO `sys_role_permission` VALUES (2, 1, '2026-06-19 00:49:48', '2026-06-19 00:49:48');
INSERT INTO `sys_role_permission` VALUES (2, 5, '2026-06-19 00:49:48', '2026-06-19 00:49:48');
INSERT INTO `sys_role_permission` VALUES (2, 6, '2026-06-19 00:49:48', '2026-06-19 00:49:48');
INSERT INTO `sys_role_permission` VALUES (2, 19, '2026-06-19 00:49:48', '2026-06-19 00:49:48');
INSERT INTO `sys_role_permission` VALUES (2, 20, '2026-06-19 00:49:48', '2026-06-19 00:49:48');
INSERT INTO `sys_role_permission` VALUES (2, 21, '2026-06-19 00:49:48', '2026-06-19 00:49:48');
INSERT INTO `sys_role_permission` VALUES (2, 22, '2026-06-19 00:49:48', '2026-06-19 00:49:48');
INSERT INTO `sys_role_permission` VALUES (2, 23, '2026-06-19 00:49:48', '2026-06-19 00:49:48');
INSERT INTO `sys_role_permission` VALUES (2, 24, '2026-06-19 00:49:48', '2026-06-19 00:49:48');
INSERT INTO `sys_role_permission` VALUES (2, 25, '2026-06-19 00:49:48', '2026-06-19 00:49:48');
INSERT INTO `sys_role_permission` VALUES (2, 26, '2026-06-19 00:49:48', '2026-06-19 00:49:48');
INSERT INTO `sys_role_permission` VALUES (2, 27, '2026-06-19 00:49:48', '2026-06-19 00:49:48');
INSERT INTO `sys_role_permission` VALUES (2, 28, '2026-06-19 00:49:48', '2026-06-19 00:49:48');
INSERT INTO `sys_role_permission` VALUES (2, 29, '2026-06-19 00:49:48', '2026-06-19 00:49:48');
INSERT INTO `sys_role_permission` VALUES (2, 30, '2026-06-19 00:49:48', '2026-06-19 00:49:48');
INSERT INTO `sys_role_permission` VALUES (2, 31, '2026-06-19 00:49:48', '2026-06-19 00:49:48');
INSERT INTO `sys_role_permission` VALUES (2, 32, '2026-06-19 00:49:48', '2026-06-19 00:49:48');
INSERT INTO `sys_role_permission` VALUES (2, 33, '2026-06-19 00:49:48', '2026-06-19 00:49:48');
INSERT INTO `sys_role_permission` VALUES (2, 34, '2026-06-19 00:49:48', '2026-06-19 00:49:48');
INSERT INTO `sys_role_permission` VALUES (2, 35, '2026-06-19 00:49:48', '2026-06-19 00:49:48');
INSERT INTO `sys_role_permission` VALUES (2, 36, '2026-06-19 00:49:48', '2026-06-19 00:49:48');
INSERT INTO `sys_role_permission` VALUES (2, 37, '2026-06-19 00:49:48', '2026-06-19 00:49:48');
INSERT INTO `sys_role_permission` VALUES (2, 38, '2026-06-19 00:49:48', '2026-06-19 00:49:48');
INSERT INTO `sys_role_permission` VALUES (2, 39, '2026-06-19 00:49:48', '2026-06-19 00:49:48');
INSERT INTO `sys_role_permission` VALUES (2, 40, '2026-06-19 00:49:48', '2026-06-19 00:49:48');
INSERT INTO `sys_role_permission` VALUES (2, 41, '2026-06-19 00:49:48', '2026-06-19 00:49:48');
INSERT INTO `sys_role_permission` VALUES (2, 42, '2026-06-19 00:49:48', '2026-06-19 00:49:48');
INSERT INTO `sys_role_permission` VALUES (2, 43, '2026-06-19 00:49:48', '2026-06-19 00:49:48');
INSERT INTO `sys_role_permission` VALUES (3, 5, '2026-04-19 04:20:15', '2026-04-19 04:20:15');
INSERT INTO `sys_role_permission` VALUES (3, 6, '2026-04-19 04:20:15', '2026-04-19 04:20:15');

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `institution_id` bigint NULL DEFAULT NULL,
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `nickname` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `status` int NULL DEFAULT 1,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `username`(`username` ASC) USING BTREE,
  INDEX `sys_user_sys_institutions_id_fk`(`institution_id` ASC) USING BTREE,
  CONSTRAINT `sys_user_sys_institutions_id_fk` FOREIGN KEY (`institution_id`) REFERENCES `sys_institutions` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_user
-- ----------------------------
INSERT INTO `sys_user` VALUES (1, 1, 'admin', '$2a$10$rhfpPO4hfp01DkWG1IKq7OErtdBUvG8v/FBcgzfCTPhFLj7RtPgX.', '管理员', 'admin@example.com', NULL, 1, '2026-04-14 01:02:28', '2026-08-08 00:54:47');
INSERT INTO `sys_user` VALUES (2, 2, 'usr', '$2a$10$DXoObZRO97Iyx8d/VXi3L.tLsMrwzGVlIsPEWPHZbn4KRSqZFs4Nu', '张三', 'zhang@example.com', NULL, 1, '2026-04-14 01:02:28', '2026-08-08 00:54:47');
INSERT INTO `sys_user` VALUES (3, 2, 'tut', '$2a$10$rxqD4vasYuljMe4H.eSEoeb/zBtZbslMdgznGc0WVtlq9F7qSPjRO', '李四', 'li@example.com', NULL, 1, '2026-04-14 01:02:28', '2026-08-08 00:54:47');
INSERT INTO `sys_user` VALUES (6, 1, 'hfh', '$2a$10$ownnOJpdIp11xCoSxxURsuF9nFJ1oL.Ccu11QdSY3BxoqP26m8P86', 'hfh', '2109664977@qq.com', '', 1, '2026-08-08 02:15:48', '2026-08-08 02:15:48');

-- ----------------------------
-- Table structure for sys_user_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role`  (
  `user_id` bigint NOT NULL,
  `role_id` bigint NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`, `role_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户角色关联' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_user_role
-- ----------------------------
INSERT INTO `sys_user_role` VALUES (1, 1, '2026-04-14 01:02:28', '2026-04-14 01:02:28');
INSERT INTO `sys_user_role` VALUES (2, 2, '2026-04-19 04:17:46', '2026-04-19 04:17:46');
INSERT INTO `sys_user_role` VALUES (3, 3, '2026-04-19 04:17:53', '2026-04-19 04:17:53');
INSERT INTO `sys_user_role` VALUES (6, 2, '2026-08-08 02:15:48', '2026-08-08 02:15:48');

-- ----------------------------
-- Table structure for task_job
-- ----------------------------
DROP TABLE IF EXISTS `task_job`;
CREATE TABLE `task_job`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `job_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '任务名称',
  `job_group` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'DEFAULT' COMMENT '任务分组',
  `invoke_target` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '调用目标字符串（类名#方法名）',
  `cron_expression` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'cron执行表达式',
  `misfire_policy` tinyint NULL DEFAULT 1 COMMENT '计划执行错误策略（1立即执行 2执行一次 3放弃执行）',
  `concurrent` tinyint NULL DEFAULT 1 COMMENT '是否并发执行（0允许 1禁止）',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态（0停用 1运行）',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '备注信息',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `job_name_group`(`job_name` ASC, `job_group` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '定时任务' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of task_job
-- ----------------------------
INSERT INTO `task_job` VALUES (1, '示例任务：打印HELLO WORLD', 'DEFAULT', 'com.hfh.api.modules.system.task.DemoJobTask#sayHello(Demo)', '0 0 0 * * ?', 1, 1, 0, 'admin', '2026-07-04 01:30:00', 'admin', '2026-07-04 01:30:00', '每晚12点打印HELLO {JobName}');

-- ----------------------------
-- Table structure for task_job_log
-- ----------------------------
DROP TABLE IF EXISTS `task_job_log`;
CREATE TABLE `task_job_log`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `job_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '任务名称',
  `job_group` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'DEFAULT' COMMENT '任务分组',
  `invoke_target` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '调用目标字符串',
  `job_message` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '执行信息',
  `status` tinyint NULL DEFAULT 0 COMMENT '执行状态（0失败 1成功）',
  `exception_info` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '异常信息',
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '执行时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '定时任务日志' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of task_job_log
-- ----------------------------
INSERT INTO `task_job_log` VALUES (1, '示例任务：打印HELLO WORLD', 'DEFAULT', 'com.hfh.api.modules.system.task.DemoJobTask#sayHello', '执行成功', 1, NULL, '2026-07-04 01:54:48');

SET FOREIGN_KEY_CHECKS = 1;
