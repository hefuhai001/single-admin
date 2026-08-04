/*
 Navicat Premium Dump SQL

 Source Server         : 127.0.0.1
 Source Server Type    : MySQL
 Source Server Version : 80045 (8.0.45)
 Source Host           : 127.0.0.1:3306
 Source Schema         : db_valvular_disease

 Target Server Type    : MySQL
 Target Server Version : 80045 (8.0.45)
 File Encoding         : 65001

 Date: 04/07/2026 02:06:19
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for ins_patient
-- ----------------------------
DROP TABLE IF EXISTS `ins_patient`;
CREATE TABLE `ins_patient`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '患者ID',
  `patient_code` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '患者编号/受试者编号',
  `institution_id` bigint NULL DEFAULT NULL COMMENT '所属机构ID(就诊医院/中心)',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '患者姓名',
  `gender` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '性别: M-男,F-女',
  `birth_date` date NULL DEFAULT NULL COMMENT '出生日期',
  `discharge_date` date NULL DEFAULT NULL COMMENT '出院时间',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '联系电话',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint(1) NULL DEFAULT 0 COMMENT '软删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_patient_code`(`patient_code` ASC) USING BTREE,
  INDEX `idx_name`(`name` ASC) USING BTREE,
  INDEX `idx_institution_id`(`institution_id` ASC) USING BTREE,
  INDEX `idx_is_deleted`(`is_deleted` ASC) USING BTREE,
  CONSTRAINT `fk_patient_institution` FOREIGN KEY (`institution_id`) REFERENCES `sys_institutions` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '患者' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of ins_patient
-- ----------------------------
INSERT INTO `ins_patient` VALUES (2, '1-1-1', 1, '夯大力', 'M', '2026-05-03', NULL, '17799990000', '2026-05-03 01:40:32', '2026-05-03 01:40:32', 0);
INSERT INTO `ins_patient` VALUES (3, '2-1-1', 2, '张三', 'M', '2026-05-04', NULL, '19011118888', '2026-05-14 01:56:04', '2026-05-14 01:56:04', 0);

-- ----------------------------
-- Table structure for ins_patient_answer
-- ----------------------------
DROP TABLE IF EXISTS `ins_patient_answer`;
CREATE TABLE `ins_patient_answer`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '答案ID',
  `patient_questionnaire_id` bigint NOT NULL COMMENT '问卷记录ID',
  `group_id` bigint NULL DEFAULT NULL COMMENT '分组记录ID',
  `question_id` bigint NOT NULL COMMENT '问题ID(关联qus_questions)',
  `answer_value` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '答案值',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_pq_question`(`patient_questionnaire_id` ASC, `question_id` ASC) USING BTREE,
  INDEX `idx_question_id`(`question_id` ASC) USING BTREE,
  INDEX `fk_pa_group`(`group_id` ASC) USING BTREE,
  CONSTRAINT `fk_pa_group` FOREIGN KEY (`group_id`) REFERENCES `qus_question_group` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_pa_pq` FOREIGN KEY (`patient_questionnaire_id`) REFERENCES `ins_patient_questionnaire` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_pa_question` FOREIGN KEY (`question_id`) REFERENCES `qus_question` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '患者问卷答案' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of ins_patient_answer
-- ----------------------------
INSERT INTO `ins_patient_answer` VALUES (1, 1, 34, 473, '是', '2026-05-14 00:14:35', '2026-05-14 00:14:35');
INSERT INTO `ins_patient_answer` VALUES (2, 1, 32, 335, '否', '2026-05-14 00:14:51', '2026-05-14 23:29:03');
INSERT INTO `ins_patient_answer` VALUES (3, 1, 32, 336, '是', '2026-05-14 00:16:04', '2026-05-14 00:16:04');
INSERT INTO `ins_patient_answer` VALUES (5, 1, 1, 1, '1', '2026-05-14 01:18:47', '2026-05-14 01:18:47');
INSERT INTO `ins_patient_answer` VALUES (6, 1, 1, 2, '男', '2026-05-14 01:18:47', '2026-05-14 01:18:47');

-- ----------------------------
-- Table structure for ins_patient_questionnaire
-- ----------------------------
DROP TABLE IF EXISTS `ins_patient_questionnaire`;
CREATE TABLE `ins_patient_questionnaire`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '问卷记录ID',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `name` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '名称',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '描述',
  `questionnaire_id` bigint NOT NULL COMMENT '问卷ID',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'pending' COMMENT '状态: pending-待填写,in_progress-填写中,completed-已完成,reviewing-审核中,rejected-已驳回',
  `visit_number` int NULL DEFAULT 1 COMMENT '随访次数/第几次填写',
  `visit_date` date NULL DEFAULT NULL COMMENT '就诊/随访日期',
  `completed_at` datetime NULL DEFAULT NULL COMMENT '完成时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '软删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_patient_id`(`patient_id` ASC) USING BTREE,
  INDEX `idx_group_id`(`questionnaire_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_patient_visit`(`patient_id` ASC, `visit_number` ASC) USING BTREE,
  CONSTRAINT `fk_pq_group` FOREIGN KEY (`questionnaire_id`) REFERENCES `qus_question_group` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_pq_patient` FOREIGN KEY (`patient_id`) REFERENCES `ins_patient` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '患者问卷' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of ins_patient_questionnaire
-- ----------------------------
INSERT INTO `ins_patient_questionnaire` VALUES (1, 2, '基本信息', '1', 1, 'in_progress', 1, '2026-05-03', NULL, '2026-05-03 01:43:28', '2026-05-03 01:43:28', 0);
INSERT INTO `ins_patient_questionnaire` VALUES (2, 2, '随访一月', '2', 2, 'pending', 2, '2026-05-10', NULL, '2026-05-03 01:53:03', '2026-05-03 01:53:03', 0);

-- ----------------------------
-- Table structure for qus_question
-- ----------------------------
DROP TABLE IF EXISTS `qus_question`;
CREATE TABLE `qus_question`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `group_id` bigint NULL DEFAULT NULL COMMENT '分组ID',
  `parent_id` bigint NULL DEFAULT NULL COMMENT '父级问题',
  `text` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '问题内容',
  `question_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '问题类型',
  `is_required` tinyint(1) NOT NULL COMMENT '是否必填',
  `order` int UNSIGNED NOT NULL COMMENT '排序',
  `unit` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '单位',
  `min` int NULL DEFAULT NULL COMMENT '最小值',
  `max` int NULL DEFAULT NULL COMMENT '最大值',
  `col` int NULL DEFAULT NULL COMMENT '栅格',
  `options` json NULL COMMENT '选项列表（JSON格式）',
  `trigger_condition` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '触发条件',
  `trigger_answer` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '触发答案',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `qus_questions_qus_question_groups_id_fk`(`group_id` ASC) USING BTREE,
  INDEX `qus_question_qus_question_id_fk`(`parent_id` ASC) USING BTREE,
  CONSTRAINT `qus_question_qus_question_id_fk` FOREIGN KEY (`parent_id`) REFERENCES `qus_question` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 871 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '问题' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of qus_question
-- ----------------------------
INSERT INTO `qus_question` VALUES (1, 1, NULL, '姓名', 'input', 1, 1, '', NULL, NULL, 11, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-14 01:45:15');
INSERT INTO `qus_question` VALUES (2, 1, NULL, '性别', 'radio', 1, 2, '', NULL, NULL, 11, '[\"男\", \"女\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-14 01:37:22');
INSERT INTO `qus_question` VALUES (3, 1, NULL, '住院号 (门急诊病人可填登记号)', 'input', 1, 3, '', NULL, NULL, 11, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-14 01:37:11');
INSERT INTO `qus_question` VALUES (4, 1, NULL, '患者来源 (中心名称)', 'input', 1, 4, '', NULL, NULL, 11, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-14 01:37:16');
INSERT INTO `qus_question` VALUES (5, 1, NULL, '受试者编号', 'input', 1, 5, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-04-29 10:22:56');
INSERT INTO `qus_question` VALUES (6, 1, NULL, '知情同意签署日期', 'date', 1, 6, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-04-29 10:22:56');
INSERT INTO `qus_question` VALUES (7, 1, NULL, '登记人员', 'input', 1, 7, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-04-29 10:22:56');
INSERT INTO `qus_question` VALUES (8, 2, NULL, '年龄', 'number', 0, 1, '', NULL, NULL, 11, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-14 01:50:29');
INSERT INTO `qus_question` VALUES (9, 2, NULL, '民族', 'input', 1, 2, '', NULL, NULL, 11, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-14 01:50:34');
INSERT INTO `qus_question` VALUES (10, 2, NULL, '籍贯 (eg: 江西南昌/九江/上饶/鹰潭/吉安/赣州/XX)', 'input', 1, 3, '', NULL, NULL, 11, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-14 01:50:39');
INSERT INTO `qus_question` VALUES (11, 2, NULL, '联系电话 (若为老年人, 优先填写配偶/子女等亲人信息)', 'input', 1, 4, '', NULL, NULL, 11, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-14 01:50:58');
INSERT INTO `qus_question` VALUES (12, 2, NULL, '受教育程度', 'radio', 1, 5, NULL, NULL, NULL, NULL, '[\"文盲\", \"小学及以下\", \"初中\", \"中专/高中\", \"大专/本科\", \"硕士及以上\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 00:53:40');
INSERT INTO `qus_question` VALUES (13, 2, NULL, '婚姻状态', 'radio', 1, 6, '', NULL, NULL, NULL, '[\"未婚\", \"已婚\", \"离异\", \"丧偶\", \"未说明\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 02:18:16');
INSERT INTO `qus_question` VALUES (14, 2, NULL, '入选科室(为首次登记入选部门):', 'radio', 1, 7, NULL, NULL, NULL, NULL, '[\"心内科病房\", \"心外科病房\", \"其它\"]', '==', '其它', '2026-04-28 21:17:39', '2026-05-02 03:12:33');
INSERT INTO `qus_question` VALUES (15, 2, 14, '其它科室', 'input', 1, 8, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (16, 2, NULL, '入选来源', 'radio', 1, 9, NULL, NULL, NULL, NULL, '[\"门诊\", \"急诊\", \"住院\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (17, 3, NULL, '症状', 'radio', 1, 1, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', '==', '是', '2026-04-28 21:17:39', '2026-05-02 03:12:33');
INSERT INTO `qus_question` VALUES (18, 3, 17, '症状具体表现', 'checkbox', 1, 2, NULL, NULL, NULL, NULL, '[\"胸闷\", \"胸痛\", \"心绞痛\", \"水肿\", \"头晕\", \"晕厥\", \"心悸\", \"呼吸困难\", \"活动耐力下降\", \"活动后气短\", \"其它\"]', '==', '其它', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (19, 3, 18, 'CCS分级', 'radio', 1, 3, NULL, NULL, NULL, NULL, '[\"I级\", \"II级\", \"III级\", \"IV级\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (20, 3, 18, '其它症状', 'input', 1, 4, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (21, 3, NULL, '纽约心功能分级', 'radio', 1, 5, NULL, NULL, NULL, NULL, '[\"I级\", \"II级\", \"III级\", \"IV级\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (22, 3, NULL, 'STS 评分 (评分网址: http://riskcalc.sts.org/stswebriskcalc/calculate )', 'float', 1, 6, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (23, 3, NULL, 'Euroscore Ⅱ 评分 (评分网址: https://www.euroscore.org/index.php?id=17&lang=en )', 'float', 1, 7, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (24, 3, NULL, '是否进行衰弱评估', 'radio', 0, 8, '', NULL, NULL, NULL, '[\"否\", \"是\"]', '==', '是', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (25, 3, 24, '平素运动习惯（散步）', 'radio', 0, 9, NULL, NULL, NULL, NULL, '[\"=<1h/w\", \"=<1-2h/w(不包括2h)\", \"=<2-3h/w(不包括3h)\", \"≥3h/w\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (26, 3, 24, '近一年内跌倒病史', 'radio', 0, 10, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (27, 3, 24, '是否需要生活辅助器具（如轮椅、拐杖）', 'radio', 0, 11, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (28, 3, 24, '是否在家吸氧', 'radio', 0, 12, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (29, 3, 24, '一周内感到疲乏天数', 'radio', 0, 13, NULL, NULL, NULL, NULL, '[\"1d\", \"2d\", \"3-4d\", \"4d以上\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (30, 3, 24, '近3个月有没有食欲下降或进食困难', 'radio', 0, 14, NULL, NULL, NULL, NULL, '[\"否\", \"轻度\", \"中度\", \"重度\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (31, 3, 24, 'MMSE量表测试结果（分）', 'number', 0, 15, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (32, 3, 24, 'Barthel指数量表(0-100)评定结果（分）', 'number', 0, 16, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (33, 3, NULL, '六分钟步行试验结果(6MWT) (m)', 'number', 1, 17, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (34, 4, NULL, '身高 (cm)', 'float', 1, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-04-28 21:17:39');
INSERT INTO `qus_question` VALUES (35, 4, NULL, '体重 (kg)', 'float', 1, 2, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-04-28 21:17:39');
INSERT INTO `qus_question` VALUES (36, 4, NULL, '收缩压(mmHg)', 'float', 1, 3, '', NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:09:00');
INSERT INTO `qus_question` VALUES (37, 4, NULL, '舒张压(mmHg)', 'float', 1, 4, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-04-28 21:17:39');
INSERT INTO `qus_question` VALUES (38, 4, NULL, '静息心率(次/分)', 'number', 1, 5, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-04-28 21:17:39');
INSERT INTO `qus_question` VALUES (39, 5, NULL, '吸烟史', 'radio', 1, 1, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', '==', '是', '2026-04-28 21:17:39', '2026-05-02 03:12:33');
INSERT INTO `qus_question` VALUES (40, 5, 39, '吸烟年限', 'number', 1, 3, '', NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (41, 5, 39, '目前是否吸烟', 'radio', 1, 2, '', NULL, NULL, NULL, '[\"否\", \"是\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (42, 5, NULL, '饮酒史', 'radio', 1, 4, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', '==', '是', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (43, 5, 42, '饮酒年限', 'number', 1, 5, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (44, 5, NULL, '高血压', 'radio', 1, 6, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', '==', '是', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (45, 5, 44, '高血压分级', 'radio', 1, 7, NULL, NULL, NULL, NULL, '[\"1级\", \"2级\", \"3级\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (46, 5, 44, '血压控制情况', 'radio', 1, 8, NULL, NULL, NULL, NULL, '[\"良好\", \"不佳\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (47, 5, 44, '高血压年限', 'number', 1, 9, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (48, 5, NULL, '糖尿病', 'radio', 1, 10, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', '==', '是', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (49, 5, 48, '糖尿病分型', 'radio', 1, 11, NULL, NULL, NULL, NULL, '[\"I型\", \"II型\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (50, 5, 48, '糖尿病治疗形式', 'checkbox', 1, 12, NULL, NULL, NULL, NULL, '[\"饮食控制\", \"口服药物治疗\", \"胰岛素治疗\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (51, 5, 48, '糖尿病年限', 'number', 1, 13, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (52, 5, NULL, '高脂血症', 'radio', 1, 14, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', '==', '是', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (53, 5, 52, '年限', 'number', 1, 15, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (54, 5, 52, '高胆固醇血症', 'radio', 1, 16, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (55, 5, NULL, '冠心病', 'radio', 1, 17, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', '==', '是', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (56, 5, 55, '年限', 'number', 1, 18, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (57, 5, 55, '心肌梗死', 'radio', 1, 19, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (58, 5, 55, 'PCI术', 'radio', 1, 20, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', '==', '是', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (59, 5, 58, 'PCI术日期', 'date', 1, 21, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (60, 5, 55, 'CABG术', 'radio', 1, 22, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', '==', '是', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (61, 5, 60, 'CABG术日期', 'date', 1, 23, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (62, 5, NULL, '心律失常', 'radio', 1, 24, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', '==', '是', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (63, 5, 62, '房颤/房扑', 'radio', 1, 25, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', '==', '是', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (64, 5, 63, '性质', 'radio', 1, 26, NULL, NULL, NULL, NULL, '[\"初发性\", \"阵发性\", \"持续性\", \"长期持续性\", \"永久性\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (65, 5, 62, '经导管射频消融术', 'radio', 1, 27, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', '==', '是', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (66, 5, 65, '消融术日期', 'date', 1, 28, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (67, 5, 62, '高度房室传导阻滞', 'radio', 1, 29, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (68, 5, 62, '永久起搏器植入', 'radio', 1, 30, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', '==', '是', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (69, 5, 68, '起搏器类型', 'input', 1, 31, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (70, 5, 68, '植入日期', 'date', 1, 32, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (71, 5, 62, '其他类型心律失常', 'input', 1, 33, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (72, 5, NULL, '心肌病', 'radio', 1, 34, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', '==', '是', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (73, 5, 72, '心肌病类型', 'input', 1, 35, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (74, 5, 72, '心肌病年限', 'number', 1, 36, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (75, 5, NULL, '先天性心脏病', 'radio', 1, 37, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', '==', '是', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (76, 5, 75, '先心病类型', 'input', 1, 38, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (77, 5, 75, '先天性年限', 'number', 1, 39, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (78, 5, NULL, '心力衰竭', 'radio', 1, 40, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', '==', '是', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (79, 5, 78, '最近一次日期', 'date', 1, 41, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (80, 5, NULL, '外周血管疾病', 'radio', 1, 42, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', '==', '是', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (81, 5, 80, '外周血管疾病类型', 'input', 1, 43, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (82, 5, 80, '是否干预治疗', 'radio', 1, 44, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (83, 5, NULL, '主动脉疾病', 'radio', 1, 45, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', '==', '是', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (84, 5, 83, '主动脉疾病类型', 'input', 1, 46, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (85, 5, 83, '是否干预治疗', 'radio', 1, 47, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (86, 5, NULL, '既往瓣膜干预治疗手术', 'date', 1, 48, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', '==', '是', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (87, 5, 86, '瓣膜干预治疗手术类型', 'input', 1, 49, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (88, 5, 86, '瓣膜干预治疗手术日期', 'date', 1, 50, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (89, 5, NULL, '既往其他手术史', 'radio', 1, 51, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', '==', '是', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (90, 5, 89, '手术名称', 'input', 1, 52, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (91, 5, 89, '手术日期', 'date', 1, 53, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (92, 5, NULL, '脑血管疾病', 'radio', 1, 54, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', '==', '是', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (93, 5, 92, '类型', 'radio', 1, 55, NULL, NULL, NULL, NULL, '[\"脑出血\", \"脑血栓或脑栓塞\", \"TIA\", \"其它\"]', '==', '其它', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (94, 5, 93, '其它类型', 'input', 1, 56, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (95, 5, 92, '发病日期', 'date', 1, 57, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (96, 5, NULL, '慢性阻塞性肺疾病', 'radio', 1, 58, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', '==', '是', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (97, 5, 96, '慢性阻塞性肺疾病年限', 'input', 1, 59, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (98, 5, 96, '是否干预治疗', 'radio', 1, 60, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (99, 5, NULL, '既往消化道出血史', 'radio', 1, 61, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', '==', '是', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (100, 5, 99, '最近一次消化道出血日期', 'date', 1, 62, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (101, 5, NULL, '慢性肝炎/肝硬化', 'radio', 1, 63, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', '==', '是', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (102, 5, 101, '慢性肝炎/肝硬化 类型', 'input', 1, 64, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (103, 5, 101, '慢性肝炎/肝硬化 年限', 'number', 1, 65, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (104, 5, 101, '是否干预治疗', 'radio', 1, 66, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (105, 5, NULL, '慢性肾脏疾病', 'radio', 1, 67, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', '==', '是', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (106, 5, 105, '慢性肾脏疾病类型', 'input', 1, 68, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (107, 5, 105, '慢性肾脏疾病年限', 'number', 1, 69, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (108, 5, 105, '是否透析治疗', 'radio', 1, 70, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (109, 5, NULL, '血液病', 'radio', 1, 71, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', '==', '是', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (110, 5, 109, '血液病类型', 'input', 1, 72, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (111, 5, 109, '血液病年限', 'number', 1, 73, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (112, 5, NULL, '风湿免疫病', 'radio', 1, 74, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', '==', '是', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (113, 5, 112, '风湿免疫病类型', 'input', 1, 75, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (114, 5, 112, '风湿免疫病年限', 'number', 1, 76, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (115, 5, NULL, '恶性肿瘤', 'radio', 1, 77, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', '==', '是', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (116, 5, 115, '恶性肿瘤类型', 'input', 1, 78, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (117, 5, 115, '是否干预治疗', 'radio', 1, 79, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (118, 5, NULL, '是否曾经有颈动脉彩超检查', 'radio', 1, 80, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', '==', '是', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (119, 5, 118, '颈动脉彩超检查日期', 'date', 1, 81, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (120, 5, 118, '是否超过50%狭窄', 'radio', 1, 82, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (121, 5, 118, '是否存在颈动脉斑块', 'radio', 1, 83, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (122, 5, NULL, '严重精神性障碍', 'radio', 1, 84, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', '==', '是', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (123, 5, 122, '严重精神性障碍类型', 'input', 1, 85, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (124, 5, NULL, '其它', 'input', 0, 86, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (125, 23, NULL, '白细胞计数 WBC（10^12/L）', 'input', 1, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (126, 23, NULL, '红细胞计数 RBC（10^12/L）', 'input', 1, 2, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (127, 23, NULL, '血小板计数 Plt（10^9/L）', 'input', 1, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (128, 23, NULL, '中性粒细胞绝对值（10^9/L）', 'input', 1, 4, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (129, 23, NULL, '淋巴细胞绝对值（10^9/L）', 'input', 1, 5, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (130, 23, NULL, '单核细胞绝对值（10^9/L）', 'input', 1, 6, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (131, 23, NULL, '肌钙蛋白 cTNI ( ng/ml)', 'input', 1, 7, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (132, 23, NULL, '红细胞压积 HCT（%）', 'input', 1, 8, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (133, 23, NULL, '降钙素原 PCT（pg/mL）', 'input', 1, 9, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (134, 23, NULL, '总胆固醇 TC (mmol/L)', 'input', 1, 10, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (135, 23, NULL, '甘油三酯 TG（mmol/L）', 'input', 1, 11, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (136, 23, NULL, '高密度脂蛋白 HDL（mmol/L）*', 'input', 1, 12, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (137, 23, NULL, '同型半胱氨酸 HCY（µmol/L）*', 'input', 1, 13, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (138, 23, NULL, '丙氨酸氨基转移酶 ALT（U/L）', 'input', 1, 14, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (139, 23, NULL, '门冬氨酸转氨酶 AST（U/L）', 'input', 1, 15, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (140, 23, NULL, '估算肾小球滤过率 eGFR（ml/min）', 'input', 1, 16, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (141, 23, NULL, '国际标准化比值 INR', 'input', 1, 17, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (142, 23, NULL, '凝血酶原时间 PT（sec）', 'input', 1, 18, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (143, 23, NULL, 'D-二聚体（mg/L FEU）', 'input', 1, 19, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (144, 23, NULL, '血氧饱和度 SaO2（血气分析）（%）', 'input', 1, 20, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (145, 23, NULL, '血钾 K+（mmol/L）', 'input', 1, 21, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (146, 23, NULL, '血钙 Ca+（mmol/L）', 'input', 1, 22, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (147, 23, NULL, '全程C反应蛋白 CRP', 'input', 1, 23, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (148, 23, NULL, '全程C反应蛋白 CRP单位', 'radio', 1, 24, NULL, NULL, NULL, NULL, '[\"mg/l\", \"mg/dl\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (149, 23, NULL, '血红蛋白Hb', 'input', 1, 25, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (150, 23, NULL, '血红蛋白Hb单位', 'radio', 1, 26, NULL, NULL, NULL, NULL, '[\"g/l\", \"g/dl\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (151, 23, NULL, '白蛋白', 'input', 1, 27, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (152, 23, NULL, '白蛋白单位', 'radio', 1, 28, NULL, NULL, NULL, NULL, '[\"g/l\", \"g/dl\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (153, 23, NULL, '低密度脂蛋白', 'input', 1, 29, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (154, 23, NULL, '低密度脂蛋白单位', 'radio', 1, 30, NULL, NULL, NULL, NULL, '[\"mmol/l\", \"mg/dl\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (155, 23, NULL, '脂蛋白a  Lp', 'input', 1, 31, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (156, 23, NULL, '脂蛋白a  Lp单位', 'radio', 1, 32, NULL, NULL, NULL, NULL, '[\"mg/dl\", \"mmol/l\", \"nmol/l\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (157, 23, NULL, '肌酐 Cre', 'input', 1, 33, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (158, 23, NULL, '肌酐 Cre单位', 'radio', 1, 34, NULL, NULL, NULL, NULL, '[\"µmol/l\", \"mg/dl\", \"μmol/l\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (159, 23, NULL, 'NT-proBNP', 'input', 1, 35, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (160, 23, NULL, 'NT-proBNP单位', 'radio', 1, 36, NULL, NULL, NULL, NULL, '[\"pg/ml\", \"pmol/l\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (161, 23, NULL, 'B型脑钠肽 BNP', 'input', 1, 37, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (162, 23, NULL, 'B型脑钠肽 BNP单位', 'radio', 1, 38, NULL, NULL, NULL, NULL, '[\"pg/ml\", \"pmol/l\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (163, 7, NULL, 'P-R间期', 'float', 1, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (164, 7, NULL, 'QRS间期', 'float', 1, 2, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (165, 7, NULL, 'QT', 'float', 1, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (166, 7, NULL, 'QTc', 'float', 1, 4, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (167, 7, NULL, 'RV5', 'float', 1, 5, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (168, 7, NULL, 'SV1', 'float', 1, 6, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (169, 7, NULL, '心电图结果', 'radio', 1, 7, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', '==', '是', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (170, 7, 169, '心房颤动', 'radio', 1, 8, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (171, 7, 169, '束支传导阻滞', 'radio', 1, 9, NULL, NULL, NULL, NULL, '[\"否\", \"左束支传导阻滞\", \"右束支传导阻滞\", \"其他\"]', '==', '其他', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (172, 7, 171, '其他类型', 'input', 1, 10, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (173, 7, 169, '房室传导阻滞', 'radio', 1, 11, NULL, NULL, NULL, NULL, '[\"否\", \"室内传导阻滞\", \"高度房室传导阻滞\", \"其他\"]', '==', '其他', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (174, 7, 173, '其他类型', 'input', 1, 12, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (175, 7, 169, '起搏心率', 'radio', 1, 13, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (176, 7, 169, '上传心电图图片', 'upload', 1, 14, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (177, 8, NULL, '心脏彩超', 'radio', 1, 1, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', '==', '是', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (178, 8, 177, '经胸超声心动图（TTE）', 'radio', 1, 2, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', '==', '是', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (179, 8, 178, '经胸超声心动图（TTE）日期', 'date', 1, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (180, 8, 178, '主动脉窦部＜35mm', 'input', 1, 5, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (181, 8, 178, '主动脉瓣环 mm', 'input', 1, 6, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (182, 8, 178, '升主动脉＜35mm', 'input', 1, 7, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (183, 8, 178, '左房内径＜35mm', 'input', 1, 8, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (184, 8, 178, '右室前壁厚度＜6mm', 'input', 1, 9, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (185, 8, 178, '右室内径＜25mm', 'input', 1, 10, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (186, 8, 178, '室间隔厚度（8-11mm）', 'input', 1, 11, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (187, 8, 178, '左室后壁厚度（8-11mm）', 'input', 1, 12, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (188, 8, 178, '左室舒张末径（35-55mm）', 'input', 1, 13, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (189, 8, 178, '左室收缩末径（25-37mm）', 'input', 1, 14, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (190, 8, 178, '左室射血分数 %', 'input', 1, 15, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (191, 8, 178, '左室短轴缩短率≥25%', 'input', 1, 16, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (192, 8, 178, '肺动脉内径（16-25mm）', 'input', 1, 17, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (193, 8, 178, '右房左右径（30-40mm）', 'input', 1, 18, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (194, 8, 178, '肺动脉收缩压 mmHg（是否大于60mmhg）', 'input', 1, 19, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (195, 8, 178, '二尖瓣口舒张期血流速度cm/s e', 'input', 1, 20, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (196, 8, 178, '二尖瓣口舒张期血流速度cm/s a', 'input', 1, 21, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (197, 8, 178, '二尖瓣瓣环速度cm/s e', 'input', 1, 22, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (198, 8, 178, '二尖瓣瓣环速度cm/s a', 'input', 1, 23, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (199, 8, 178, '主动脉瓣狭窄/反流', 'radio', 1, 28, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', '==', '是', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (200, 8, 199, '主动脉瓣狭窄', 'radio', 1, 29, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', '==', '是', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (201, 8, 200, '类型', 'radio', 1, 30, NULL, NULL, NULL, NULL, '[\"自体瓣\", \"人工瓣\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (202, 8, 200, '主动脉瓣狭窄程度', 'radio', 1, 31, NULL, NULL, NULL, NULL, '[\"轻度\", \"轻中度\", \"中度\", \"中重度\", \"重度\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (203, 8, 200, '主动脉瓣狭窄病因', 'radio', 1, 32, NULL, NULL, NULL, NULL, '[\"风湿性\", \"退行性\", \"感染性\", \"功能性\", \"先天性\", \"炎性/免疫性\", \"其它\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (204, 8, 199, '主动脉瓣反流', 'radio', 1, 33, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', '==', '是', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (205, 8, 204, '主动脉瓣瓣体类型', 'radio', 1, 34, NULL, NULL, NULL, NULL, '[\"自体瓣\", \"人工瓣\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (206, 8, 204, '主动脉瓣反流程度', 'radio', 1, 35, NULL, NULL, NULL, NULL, '[\"轻度\", \"轻中度\", \"中度\", \"中重度\", \"重度\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (207, 8, 204, '主动脉瓣反流病因', 'radio', 1, 36, NULL, NULL, NULL, NULL, '[\"风湿性\", \"退行性\", \"感染性\", \"功能性\", \"先天性\", \"炎性/免疫性\", \"其它\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (208, 8, 199, '峰值流速 m/s', 'input', 1, 37, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (209, 8, 199, '平均跨瓣压差 mmHg', 'input', 1, 38, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (210, 8, 199, '最大跨瓣压差 mmHg', 'input', 1, 53, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (211, 8, 199, '主动脉瓣口面积 cm²', 'input', 1, 55, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (212, 8, 199, '三尖瓣环平面收缩期偏移（TAPSE）', 'input', 1, 73, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (213, 8, 199, '右心室应变（%）', 'input', 1, 84, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (214, 8, 199, '左心房应变（%）', 'input', 1, 90, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (215, 8, 199, '左室流出道内径 mm', 'input', 1, 93, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (216, 8, 199, '左室流出道面积 cm²', 'input', 1, 96, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (217, 8, 199, '左室流出道椭圆率', 'input', 1, 100, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (218, 8, 199, '主动脉瓣环直径mm', 'input', 1, 102, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (219, 8, 199, '主动脉瓣环面积cm²', 'input', 1, 104, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (220, 8, 199, '膜性隔膜长度 mm', 'input', 1, 106, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (221, 8, 178, '二尖瓣狭窄/反流', 'radio', 1, 39, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', '==', '是', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (222, 8, 221, '二尖瓣狭窄', 'radio', 1, 40, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', '==', '是', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (223, 8, 222, '类型', 'radio', 1, 41, NULL, NULL, NULL, NULL, '[\"自体瓣\", \"人工瓣\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (224, 8, 222, '二尖瓣狭窄程度', 'radio', 1, 42, NULL, NULL, NULL, NULL, '[\"轻度\", \"轻中度\", \"中度\", \"中重度\", \"重度\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (225, 8, 222, '二尖瓣狭窄病因', 'radio', 1, 43, NULL, NULL, NULL, NULL, '[\"风湿性\", \"退行性\", \"感染性\", \"功能性\", \"先天性\", \"炎性/免疫性\", \"其它\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (226, 8, 221, '二尖瓣反流', 'radio', 1, 44, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', '==', '是', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (227, 8, 226, '二尖瓣瓣体类型', 'radio', 1, 45, NULL, NULL, NULL, NULL, '[\"自体瓣\", \"人工瓣\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (228, 8, 226, '二尖瓣反流程度', 'radio', 1, 46, NULL, NULL, NULL, NULL, '[\"轻度\", \"轻中度\", \"中度\", \"中重度\", \"重度\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (229, 8, 226, '二尖瓣脱垂', 'radio', 1, 47, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', '==', '是', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (230, 8, 229, '二尖瓣脱垂类型', 'radio', 1, 48, NULL, NULL, NULL, NULL, '[\"前叶\", \"后叶\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (231, 8, 226, '二尖瓣脱垂病因', 'radio', 1, 49, NULL, NULL, NULL, NULL, '[\"原发性\", \"继发性\", \"混合性\"]', '==', '原发性', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (232, 8, 231, '原发性病因', 'radio', 1, 50, NULL, NULL, NULL, NULL, '[\"先天性\", \"退行性\", \"风湿性\", \"感染性\", \"粘液变性/炎性/免疫性\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (233, 8, 231, '继发性病因', 'radio', 1, 51, NULL, NULL, NULL, NULL, '[\"缺血性\", \"SAM\", \"房性功能性\", \"心室重构\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (234, 8, 226, '二尖瓣反流 Carpentier 分型', 'radio', 1, 52, NULL, NULL, NULL, NULL, '[\"Ia型（瓣叶穿孔、裂隙）\", \"Ib（瓣环扩张）\", \"II型（瓣膜脱垂、连枷）\", \"IIIa（瓣叶活动受限导致的狭窄）\", \"IIIb（瓣叶活动受限导致的反流）\", \"II型+IIIb\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (235, 8, 221, '平均跨瓣压差 mmHg', 'input', 0, 54, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (236, 8, 221, '二尖瓣口面积 cm²', 'input', 0, 56, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (237, 8, 221, '二尖瓣开放幅度 mm', 'input', 0, 74, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (238, 8, 221, '峰值流速 m/s', 'input', 0, 85, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (239, 8, 221, '缩流颈宽度 mm', 'input', 0, 91, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (240, 8, 221, '有效反流口面积 (EROA) cm²', 'input', 0, 94, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (241, 8, 221, '反流容积 ml', 'input', 0, 97, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (242, 8, 221, '反流束面积 cm²', 'input', 0, 101, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (243, 8, 221, '二尖瓣前叶(A2)长度 mm', 'input', 0, 103, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (244, 8, 221, '二尖瓣后叶(P2)长度 mm', 'input', 0, 105, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (245, 8, 221, '脱垂高度 mm', 'input', 0, 107, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (246, 8, 221, '脱垂宽度 mm', 'input', 0, 108, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (247, 8, 221, '压力减半时间 ms', 'input', 0, 109, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (248, 8, 221, '左心室收缩末期容积指数（LVESVI）（＜49ml/m 2）', 'input', 0, 110, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (249, 8, 221, '左心室舒张末期容积指数（LVEDVI）（＜97ml/m 2）', 'input', 0, 111, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (250, 8, 178, '三尖瓣狭窄/反流', 'radio', 1, 57, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', '==', '是', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (251, 8, 250, '三尖瓣狭窄', 'radio', 1, 58, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', '==', '是', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (252, 8, 251, '类型', 'radio', 1, 59, NULL, NULL, NULL, NULL, '[\"自体瓣\", \"人工瓣\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (253, 8, 251, '三尖瓣狭窄程度', 'radio', 1, 60, NULL, NULL, NULL, NULL, '[\"轻度\", \"轻中度\", \"中度\", \"中重度\", \"重度\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (254, 8, 251, '三尖瓣狭窄病因', 'radio', 1, 61, NULL, NULL, NULL, NULL, '[\"风湿性\", \"退行性\", \"感染性\", \"功能性\", \"先天性\", \"炎性/免疫性\", \"其它\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (255, 8, 250, '三尖瓣反流', 'radio', 1, 62, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', '==', '是', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (256, 8, 255, '三尖瓣反流程度', 'radio', 1, 63, NULL, NULL, NULL, NULL, '[\"轻度\", \"轻中度\", \"中度\", \"中重度\", \"重度\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (257, 8, 255, '三尖瓣反流病因', 'radio', 1, 64, NULL, NULL, NULL, NULL, '[\"风湿性\", \"退行性\", \"风湿性\", \"感染性\", \"功能性\", \"先天性\", \"炎性/免疫性\", \"其他\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (258, 8, 250, '峰值流速m/s', 'input', 1, 65, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (259, 8, 250, '缩流颈宽度 mm', 'input', 1, 66, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (260, 8, 250, '三尖瓣口面积（cm²）', 'input', 1, 67, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 02:53:54');
INSERT INTO `qus_question` VALUES (261, 8, 250, '平均跨瓣压差 mmHg', 'input', 1, 68, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (262, 8, 250, '近端等速表面积 (PISA) cm²', 'input', 1, 69, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (263, 8, 250, 'PISA反流体积 ml', 'input', 1, 70, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (264, 8, 250, '三尖瓣环平面收缩期偏移（TAPSE）', 'input', 1, 71, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (265, 8, 250, '三尖瓣反流喷射面积 cm²', 'input', 1, 72, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (266, 8, 178, '肺动脉瓣狭窄/反流', 'radio', 1, 75, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', '==', '是', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (267, 8, 266, '肺动脉瓣狭窄', 'radio', 1, 76, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', '==', '是', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (268, 8, 267, '类型', 'radio', 1, 77, NULL, NULL, NULL, NULL, '[\"自体瓣\", \"人工瓣\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (269, 8, 267, '肺动脉瓣狭窄程度', 'radio', 1, 78, NULL, NULL, NULL, NULL, '[\"轻度\", \"轻中度\", \"中度\", \"中重度\", \"重度\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (270, 8, 267, '肺动脉瓣狭窄病因', 'radio', 1, 79, NULL, NULL, NULL, NULL, '[\"风湿性\", \"退行性\", \"感染性\", \"功能性\", \"先天性\", \"炎性/免疫性\", \"其它\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (271, 8, 266, '肺动脉瓣反流', 'radio', 1, 80, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', '==', '是', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (272, 8, 271, '肺动脉瓣瓣体类型', 'radio', 1, 81, NULL, NULL, NULL, NULL, '[\"自体瓣\", \"人工瓣\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (273, 8, 271, '肺动脉瓣反流程度', 'radio', 1, 82, NULL, NULL, NULL, NULL, '[\"轻度\", \"轻中度\", \"中度\", \"中重度\", \"重度\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (274, 8, 271, '肺动脉瓣反流病因', 'radio', 1, 83, NULL, NULL, NULL, NULL, '[\"风湿性\", \"退行性\", \"感染性\", \"功能性\", \"先天性\", \"炎性/免疫性\", \"其它\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (275, 8, 266, '峰值流速 m/s', 'input', 1, 86, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (276, 8, 266, '峰值压差 mmHg', 'input', 1, 87, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (277, 8, 266, '右室流出道内径 mm', 'input', 1, 88, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (278, 8, 266, '缩流颈宽度 mm', 'input', 1, 89, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (279, 8, 178, '心包积液', 'radio', 1, 92, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (280, 8, 178, '室壁运动异常', 'radio', 1, 95, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (281, 8, 178, '上传经胸超声心动图（TTE）', 'upload', 1, 98, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (282, 8, 177, '经食道超声心动图(TEE)', 'radio', 1, 4, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', '==', '是', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (283, 8, 282, '跨瓣峰值流速m/s', 'input', 1, 24, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (284, 8, 282, '跨瓣最大压差mmHg', 'input', 1, 25, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (285, 8, 282, '跨瓣平均跨瓣压差mmHg', 'input', 1, 26, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (286, 8, 282, '估测瓣口面积 cm²', 'input', 1, 27, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (287, 8, 282, '上传经食道超声心动图(TEE)', 'upload', 1, 99, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (288, 9, NULL, '心脏CT', 'radio', 1, 1, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', '==', '是', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (289, 9, 288, '心脏CT日期', 'date', 1, 2, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (290, 9, 288, '冠脉病变部位', 'radio', 1, 3, NULL, NULL, NULL, NULL, '[\"前降支\", \"右冠\", \"回旋支\", \"左主干\", \"无病变\", \"其它\"]', '==', '其它', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (291, 9, 290, '其它病变部位', 'input', 1, 4, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (292, 9, 288, '冠脉病变描述', 'input', 1, 5, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (293, 9, 288, '主动脉瓣膜类型', 'checkbox', 1, 6, NULL, NULL, NULL, NULL, '[\"type0型二叶瓣\", \"type1型二叶瓣\", \"type2型二叶瓣\", \"三叶瓣(融合型即功能型二叶瓣)\", \"三叶瓣（无融合型）\", \"四叶瓣\", \"外科术后瓣膜\", \"TAVR术后瓣膜\", \"其它\"]', '==', '其它', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (294, 9, 293, '其它瓣膜类型', 'input', 1, 7, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (295, 9, 288, '瓣叶钙化', 'radio', 1, 8, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', '==', '是', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (296, 9, 295, '钙化积分类型', 'input', 1, 9, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (297, 9, 295, '钙化积分数值', 'input', 1, 10, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (298, 9, 288, '左冠瓣钙化体积（mm3）', 'input', 1, 11, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (299, 9, 288, '右冠瓣钙化体积（mm3）', 'input', 1, 12, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (300, 9, 288, '无冠瓣钙化体积（mm3）', 'input', 1, 13, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (301, 9, 288, '瓣叶增厚', 'radio', 1, 14, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (302, 9, 288, '瓣环直径', 'input', 1, 15, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (303, 9, 288, '瓣环面积/mm2', 'input', 1, 16, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (304, 9, 288, '瓣环椭圆率', 'input', 1, 17, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (305, 9, 288, '升主动脉平均直径（mm）', 'input', 1, 18, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (306, 9, 288, 'LVOT平均直径（mm）', 'input', 1, 19, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (307, 9, 288, 'LVOT钙化体积（mm3）', 'input', 1, 20, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (308, 9, 288, '膜性隔膜长度（mm）', 'input', 1, 21, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (309, 9, 288, '主动脉倾角', 'input', 1, 22, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (310, 9, 288, '左冠脉ostium高度（mm）', 'input', 1, 23, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (311, 9, 288, '右冠脉ostium高度（mm）', 'input', 1, 24, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (312, 9, 288, 'STJ高度（mm）', 'input', 1, 25, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (313, 9, 288, 'STJ直径（mm）', 'input', 1, 26, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (314, 9, 288, '有无大血管疾病', 'radio', 1, 27, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (315, 9, 288, '上传心脏CT图片', 'upload', 1, 28, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (316, 10, NULL, '动态心电图', 'radio', 1, 1, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', '==', '是', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (317, 10, 316, '动态心电图重要内容', 'input', 1, 2, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (318, 10, NULL, '肺功能检查', 'radio', 1, 3, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', '==', '是', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (319, 10, 318, '肺功能检查FVC占预计值百分比', 'input', 1, 4, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (320, 10, 318, '肺功能检查FEV1占预计值百分比', 'input', 1, 5, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (321, 10, 318, '肺功能检查FEV1/FVC比值', 'input', 1, 6, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (322, 10, 318, '支气管舒张实验', 'radio', 1, 7, NULL, NULL, NULL, NULL, '[\"阴性\", \"阳性\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (323, 10, NULL, '冠脉造影', 'radio', 1, 8, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', '==', '是', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (324, 10, 323, '冠脉造影日期', 'date', 1, 9, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (325, 10, 323, '冠脉造影结果', 'input', 1, 10, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (326, 10, 323, '血管病变累及狭窄大于50%（支）', 'number', 1, 11, NULL, NULL, NULL, NULL, '[\"0\", \"1\", \"2\", \"3\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (327, 10, NULL, '心脏磁共振', 'radio', 1, 12, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', '==', '是', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (328, 10, 327, '心脏磁共振日期', 'date', 1, 13, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (329, 10, 327, '心脏磁共振结果', 'input', 1, 14, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (330, 10, 327, '是否有心肌纤维化', 'radio', 1, 15, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (331, 10, NULL, '头CTA或MRA检查', 'radio', 1, 16, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', '==', '是', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (332, 10, 331, '头CTA或MRA检查日期', 'date', 1, 17, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (333, 10, 331, '头CTA或MRA检查结果', 'input', 1, 18, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (334, 10, 331, '是否超过50%狭窄', 'radio', 1, 19, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (335, 32, NULL, '外科手术治疗', 'radio', 1, 1, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', '==', '是', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (336, 32, 335, '是否行瓣膜手术', 'radio', 1, 2, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', '==', '是', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (337, 32, 336, '手术日期', 'date', 1, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (338, 32, 336, '手术方式', 'radio', 1, 4, NULL, NULL, NULL, NULL, '[\"全胸骨正中\", \"部分胸骨切口\", \"胸骨旁切口\", \"左胸切口\", \"右胸切口\", \"腔镜辅助\", \"全腔镜\", \"机器人辅助\", \"其他\"]', '==', '其他', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (339, 32, 338, '其他手术方式', 'input', 1, 5, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (340, 32, 336, '主动脉瓣手术', 'radio', 1, 6, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', '==', '是', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (341, 32, 340, '主动脉瓣手术手术方式(可多选)', 'checkbox', 1, 7, NULL, NULL, NULL, NULL, '[\"瓣膜置换\", \"瓣膜修复\", \"其他\"]', '==', '其他', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (342, 32, 340, '主动脉瓣手术植入物类型', 'radio', 1, 8, NULL, NULL, NULL, NULL, '[\"无植入物\", \"生物瓣\", \"机械瓣\", \"带瓣管道\", \"同种瓣\", \"成形环\", \"成形带\", \"自体瓣\", \"其他\"]', '==', '其他', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (343, 32, 342, '主动脉瓣手术其他植入物', 'input', 1, 9, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (344, 32, 340, '植入物品牌（完整名称）', 'input', 1, 10, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (345, 32, 340, '植入物尺寸', 'input', 1, 11, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (346, 32, 336, '二尖瓣手术', 'radio', 1, 12, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', '==', '是', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (347, 32, 346, '二尖瓣手术手术方式(可多选)', 'checkbox', 1, 13, NULL, NULL, NULL, NULL, '[\"瓣膜置换\", \"瓣膜修复\", \"其他\"]', '==', '其他', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (348, 32, 347, '其他手术方式', 'input', 1, 14, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (349, 32, 346, '二尖瓣手术手术入路', 'radio', 1, 15, NULL, NULL, NULL, NULL, '[\"微创\", \"常规\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (350, 32, 346, '二尖瓣手术植入物类型', 'radio', 1, 16, NULL, NULL, NULL, NULL, '[\"无植入物\", \"生物瓣\", \"机械瓣\", \"成形环\", \"其他\"]', '==', '其他', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (351, 32, 350, '二尖瓣手术其他植入物', 'input', 1, 17, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (352, 32, 346, '植入物品牌（完整名称）', 'input', 1, 18, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (353, 32, 346, '植入物尺寸', 'input', 1, 19, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (354, 32, 336, '三尖瓣手术', 'radio', 1, 22, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', '==', '是', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (355, 32, 354, '三尖瓣手术手术方式(可多选)', 'checkbox', 1, 23, NULL, NULL, NULL, NULL, '[\"瓣膜置换\", \"瓣膜修复\", \"其他\"]', '==', '其他', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (356, 32, 355, '其他手术方式', 'input', 1, 24, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (357, 32, 354, '手术入路', 'radio', 1, 25, NULL, NULL, NULL, NULL, '[\"微创\", \"常规\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (358, 32, 354, '三尖瓣手术植入物类型', 'radio', 1, 26, NULL, NULL, NULL, NULL, '[\"生物瓣\", \"机械瓣\", \"成形环\", \"其他\"]', '==', '其他', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (359, 32, 358, '三尖瓣手术其他植入物', 'input', 1, 27, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (360, 32, 354, '三尖瓣手术植入物品牌（完整名称）', 'input', 1, 28, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (361, 32, 354, '三尖瓣手术植入物尺寸', 'input', 1, 29, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (362, 32, 336, '肺动脉瓣手术', 'radio', 1, 34, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', '==', '是', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (363, 32, 362, '肺动脉瓣手术手术方式(可多选)', 'checkbox', 1, 35, NULL, NULL, NULL, NULL, '[\"瓣膜置换\", \"瓣膜修复\", \"其他\"]', '==', '其他', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (364, 32, 363, '其他手术方式', 'input', 1, 36, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (365, 32, 362, '手术入路', 'radio', 1, 37, NULL, NULL, NULL, NULL, '[\"微创\", \"常规\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (366, 32, 362, '肺动脉瓣手术植入物类型', 'radio', 1, 38, NULL, NULL, NULL, NULL, '[\"生物瓣\", \"机械瓣\", \"成形环\", \"其他\"]', '==', '其他', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (367, 32, 366, '肺动脉瓣手术其他植入物', 'input', 1, 39, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (368, 32, 362, '肺动脉瓣手术植入物品牌（完整名称）', 'input', 1, 40, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (369, 32, 362, '肺动脉瓣手术植入物尺寸', 'input', 1, 41, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (370, 32, 335, '是否同期行其他外科手术', 'radio', 1, 20, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', '==', '是', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (371, 32, 370, '同期行其他外科手术完整名称', 'input', 1, 21, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (372, 32, 335, '术中食道超声评估', 'radio', 1, 30, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', '==', '是', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (373, 32, 372, '主动脉瓣跨瓣峰值流速m/s', 'input', 1, 31, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (374, 32, 372, '主动脉瓣平均跨瓣压差mmHg', 'input', 1, 32, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (375, 32, 372, '瓣周漏', 'radio', 1, 33, NULL, NULL, NULL, NULL, '[\"无\", \"轻度\", \"中度\", \"重度\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (376, 32, 335, '循环辅助', 'radio', 1, 42, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', '==', '是', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (377, 32, 376, '术中体外循环时间（min）', 'input', 1, 43, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (378, 32, 376, '术中主动脉阻断时间（min）', 'input', 1, 44, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (379, 32, 376, '是否围术期循环辅助方式（多选）', 'checkbox', 1, 45, NULL, NULL, NULL, NULL, '[\"无\", \"IABP\", \"ECMO\", \"VAD\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (380, 32, 335, '累计机械辅助通气时间（小时）', 'input', 1, 46, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (381, 32, 335, '术后累计引流（ml）', 'input', 1, 47, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (382, 32, 335, '术后7天内尿量(1) （ml）', 'input', 1, 48, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (383, 32, 335, '术后7天内尿量(2) （ml）', 'input', 1, 49, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (384, 32, 335, '术后7天内尿量(3) （ml）', 'input', 1, 50, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (385, 32, 335, '术后7天内尿量(4) （ml）', 'input', 1, 51, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (386, 32, 335, '术后7天内尿量(5) （ml）', 'input', 1, 52, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (387, 32, 335, '术后7天内尿量(6) （ml）', 'input', 1, 53, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (388, 32, 335, '术后7天内尿量(7) （ml）', 'input', 1, 54, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (389, 32, 335, '术后最高血肌酐值（µmol/L）', 'input', 1, 55, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (390, 32, 335, '术后二次气管插管', 'radio', 1, 56, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (391, 33, NULL, '是否行瓣膜介入治疗', 'radio', 1, 1, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', '==', '是', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (392, 33, 391, '经皮球囊瓣膜成形术', 'radio', 1, 2, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', '==', '是', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (393, 33, 392, '手术日期', 'date', 1, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (394, 33, 392, '干预瓣膜', 'radio', 1, 4, NULL, NULL, NULL, NULL, '[\"主动脉瓣\", \"二尖瓣\", \"三尖瓣\", \"肺动脉瓣\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (395, 33, 391, '经导管主动脉瓣置换术（TAVR）', 'radio', 1, 5, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', '==', '是', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (396, 33, 395, '经导管主动脉瓣置换术（TAVR）手术日期', 'date', 1, 6, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (397, 33, 395, '毁损生物瓣的介入治疗（主动脉瓣位）', 'radio', 1, 7, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', '==', '是', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (398, 33, 397, '原生物瓣品牌+型号', 'input', 1, 8, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (399, 33, 395, '麻醉方式', 'radio', 1, 9, NULL, NULL, NULL, NULL, '[\"全麻\", \"局麻\", \"局麻转全麻\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (400, 33, 395, '手术入路', 'radio', 1, 10, NULL, NULL, NULL, NULL, '[\"股动脉\", \"锁骨下动脉\", \"升主动脉\", \"心尖\", \"颈动脉\", \"其他\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (401, 33, 395, '是否行循环辅助', 'radio', 1, 11, NULL, NULL, NULL, NULL, '[\"否\", \"体外循环\", \"IABP\", \"ECMO\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (402, 33, 395, '脑保护装置', 'radio', 1, 12, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', '==', '是', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (403, 33, 402, '脑保护装置', 'input', 1, 13, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (404, 33, 395, '冠脉保护', 'radio', 1, 14, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', '==', '是', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (405, 33, 404, '冠脉保护措施', 'input', 1, 15, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (406, 33, 395, '预扩张', 'radio', 1, 16, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', '==', '是', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (407, 33, 406, '尺寸', 'input', 1, 17, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (408, 33, 406, '次数', 'number', 1, 18, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (409, 33, 395, '人工瓣膜品牌', 'radio', 1, 19, NULL, NULL, NULL, NULL, '[\"VenusA\", \"VenusA Plus\", \"VenusA Pilot\", \"Lotus\", \"Lotus Edge\", \"Edwards XT\", \"Edwards 3\", \"Portico\", \"CoreValve\", \"Evolut R\", \"Evolut Pro\", \"ACURATE\", \"Vitaflow\", \"Vitaflow Ⅱ\", \"J-Valve\", \"Taurus One\", \"Taurus Elite\", \"其它\"]', '==', '是', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (410, 33, 409, '其它人工瓣膜品牌', 'input', 1, 20, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (411, 33, 395, '瓣膜尺寸（mm）', 'input', 1, 21, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (412, 33, 395, '植入深度（mm）', 'input', 1, 22, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (413, 33, 395, '后扩张', 'radio', 1, 23, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', '==', '是', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (414, 33, 413, '尺寸', 'input', 1, 24, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (415, 33, 413, '次数', 'number', 1, 25, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (416, 33, 395, '术前左室压力', 'input', 1, 26, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (417, 33, 395, '术前主动脉瓣上压力', 'input', 1, 27, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (418, 33, 395, '器械成功', 'radio', 1, 28, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (419, 33, 395, '术中不良事件', 'radio', 1, 29, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', '==', '是', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (420, 33, 419, '术中不良事件内容', 'radio', 1, 30, NULL, NULL, NULL, NULL, '[\"植入瓣中瓣\", \"瓣环破裂\", \"瓣膜移位\", \"瓣膜异位释放\", \"转外科手术\", \"瓣膜栓塞\", \"心肌穿孔/心包填塞\", \"非计划体外循环\", \"冠脉阻塞\", \"主动脉夹层\", \"瓣膜功能障碍\", \"室颤\", \"其它\"]', '==', '其他', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (421, 33, 420, '植入瓣中瓣（品牌+尺寸）', 'input', 1, 31, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (422, 33, 420, '其它术中不良事件', 'input', 1, 32, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (423, 33, 395, '术后左室压力', 'input', 1, 33, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (424, 33, 395, '术后主动脉瓣上压力', 'input', 1, 34, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (425, 33, 395, '术后即刻血压（mmHg)', 'input', 1, 35, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (426, 33, 395, '术后即刻心率（次/分）', 'input', 1, 36, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (427, 33, 395, '术后反流', 'radio', 1, 37, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (428, 33, 395, '血管闭合方式', 'radio', 1, 38, NULL, NULL, NULL, NULL, '[\"不适用\", \"外科缝合\", \"血管闭合器\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (429, 33, 395, '术中超声类型', 'radio', 1, 39, NULL, NULL, NULL, NULL, '[\"经食道\", \"经胸\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (430, 33, 391, '二尖瓣介入治疗', 'radio', 1, 40, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', '==', '是', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (431, 33, 430, '二尖瓣介入治疗手术日期', 'date', 1, 41, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (432, 33, 430, '损毁生物瓣的介入治疗(二尖瓣): ', 'radio', 1, 42, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', '==', '是', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (433, 33, 432, '损毁生物瓣的介入治疗 原生物瓣品牌+型号', 'input', 1, 43, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (434, 33, 430, '麻醉方式', 'radio', 1, 44, NULL, NULL, NULL, NULL, '[\"局麻\", \"全麻\", \"局麻转全麻\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (435, 33, 430, '手术入路', 'radio', 1, 45, NULL, NULL, NULL, NULL, '[\"股动脉\", \"锁骨下动脉\", \"升主动脉\", \"心尖\", \"颈动脉\", \"其他\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (436, 33, 430, '植入器械名称', 'radio', 1, 46, NULL, NULL, NULL, NULL, '[\"瓣叶\", \"瓣环\", \"原位置换\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (437, 33, 430, '是否行循环辅助', 'radio', 1, 47, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', '==', '是', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (438, 33, 437, '循环辅助方式', 'radio', 1, 48, NULL, NULL, NULL, NULL, '[\"体外循环\", \"IABP\", \"ECMO\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (439, 33, 430, '术中不良事件', 'radio', 1, 49, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', '==', '是', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (440, 33, 439, '术中不良事件内容', 'radio', 1, 50, NULL, NULL, NULL, NULL, '[\"心包积液\", \"出血\", \"紧急转外科手术\", \"栓塞\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (441, 33, 430, '术后即刻超声类型', 'radio', 1, 51, NULL, NULL, NULL, NULL, '[\"经食道\", \"经胸\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (442, 33, 430, '二尖瓣反流分级', 'radio', 1, 52, NULL, NULL, NULL, NULL, '[\"无\", \"轻度\", \"中度\", \"中重度\", \"重度\", \"极重度\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (443, 33, 430, '反流束最窄部位宽度（mm）', 'input', 1, 53, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (444, 33, 430, '反流量（ml/心搏）', 'input', 1, 54, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (445, 33, 430, '峰值跨瓣压差（mmHg）', 'input', 1, 55, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (446, 33, 430, '平均跨瓣压差（mmHg）', 'input', 1, 56, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (447, 33, 430, '肺静脉反流', 'radio', 1, 57, NULL, NULL, NULL, NULL, '[\"无\", \"有\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (448, 33, 430, '术前左心房压力', 'input', 1, 58, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (449, 33, 430, '术后左心房压力', 'input', 1, 59, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (450, 33, 430, '术前主动脉压力', 'input', 1, 60, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (451, 33, 430, '术后主动脉压力', 'input', 1, 61, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (452, 33, 430, '术前肺动脉压力', 'input', 1, 62, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (453, 33, 430, '术后肺动脉压力', 'input', 1, 63, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (454, 33, 391, '三尖瓣介入治疗', 'radio', 1, 64, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', '==', '是', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (455, 33, 454, '三尖瓣介入治疗手术日期', 'date', 1, 65, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (456, 33, 454, '三尖瓣手术方法', 'input', 1, 66, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (457, 33, 454, '三尖瓣麻醉方式', 'radio', 1, 67, NULL, NULL, NULL, NULL, '[\"局麻\", \"全麻\", \"局麻转全麻\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (458, 33, 454, '三尖瓣手术入路', 'radio', 1, 68, NULL, NULL, NULL, NULL, '[\"股动脉\", \"锁骨下动脉\", \"升主动脉\", \"心尖\", \"颈动脉\", \"其他\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (459, 33, 454, '三尖瓣手术是否行循环辅助', 'radio', 1, 69, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', '==', '是', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (460, 33, 459, '三尖瓣手术循环辅助方式', 'radio', 1, 70, NULL, NULL, NULL, NULL, '[\"体外循环\", \"IABP\", \"ECMO\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (461, 33, 454, '三尖瓣手术术中不良事件', 'radio', 1, 71, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', '==', '是', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (462, 33, 461, '三尖瓣手术术中不良事件内容', 'input', 1, 72, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (463, 33, 391, '肺动脉瓣介入治疗', 'radio', 1, 73, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', '==', '是', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (464, 33, 463, '肺动脉瓣介入治疗手术日期', 'date', 1, 74, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (465, 33, 463, '肺动脉瓣手术方法', 'input', 1, 75, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (466, 33, 463, '肺动脉瓣手术麻醉方式', 'radio', 1, 76, NULL, NULL, NULL, NULL, '[\"局麻\", \"全麻\", \"局麻转全麻\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (467, 33, 463, '肺动脉瓣手术入路', 'radio', 1, 77, NULL, NULL, NULL, NULL, '[\"股动脉\", \"锁骨下动脉\", \"升主动脉\", \"心尖\", \"颈动脉\", \"其他\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (468, 33, 463, '肺动脉瓣手术是否行循环辅助', 'radio', 1, 78, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', '==', '是', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (469, 33, 468, '肺动脉瓣手术循环辅助方式', 'radio', 1, 79, NULL, NULL, NULL, NULL, '[\"体外循环\", \"IABP\", \"ECMO\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (470, 33, 463, '肺动脉瓣手术术中不良事件', 'radio', 1, 80, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', '==', '是', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (471, 33, 470, '肺动脉瓣手术术中不良事件内容', 'input', 1, 81, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (472, 33, 391, '是否参加瓣膜介入治疗临床试验', 'radio', 1, 82, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (473, 34, NULL, '非瓣膜病介入治疗', 'radio', 1, 1, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', '==', '是', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (474, 34, 473, '冠状动脉介入治疗', 'radio', 1, 2, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', '==', '是', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (475, 34, 474, '是否冠状动脉介入治疗与瓣膜介入治疗同时', 'radio', 1, 3, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (476, 34, 474, '冠状动脉介入治疗支架', 'radio', 1, 4, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (477, 34, 473, '左心耳封堵术', 'radio', 1, 5, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (478, 34, 473, '起搏器植入术', 'radio', 1, 6, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', '==', '是', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (479, 34, 478, '起搏器植入术手术日期', 'date', 1, 7, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (480, 34, 478, '是否瓣膜介入治疗术后导致起搏器植入', 'radio', 1, 8, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (481, 34, 478, '起搏器类型', 'radio', 1, 9, NULL, NULL, NULL, NULL, '[\"单腔起搏器\", \"双腔起搏器\", \"三腔起搏器\", \"左束支起搏器\", \"其他\"]', '==', '其他', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (482, 34, 481, '其他起搏器类型', 'input', 1, 10, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (483, 34, 473, '房颤射频消融术', 'radio', 1, 11, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', '==', '是', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (484, 34, 483, '消融方式', 'radio', 1, 12, NULL, NULL, NULL, NULL, '[\"射频消融\", \"冷冻消融\", \"脉冲消融\", \"其他\"]', '==', '其他', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (485, 34, 484, '其他消融方式', 'input', 1, 13, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (486, 34, 483, '维持窦律药物', 'radio', 1, 14, NULL, NULL, NULL, NULL, '[\"普罗帕酮\", \"胺碘酮\", \"索他洛尔\", \"决奈达隆\", \"其他\"]', '==', '其他', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (487, 34, 486, '其他维持窦律药物', 'input', 1, 15, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (488, 35, NULL, '目前用药', 'radio', 1, 1, NULL, NULL, NULL, NULL, '[\"是\", \"否\"]', '==', '是', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (489, 35, 488, '用药类型', 'checkbox', 1, 2, NULL, NULL, NULL, NULL, '[\"抗血小板\", \"抗凝\", \"ACEI/ARB\", \"β阻滞剂\", \"他汀类\", \"利尿剂\", \"SGLT-2i\", \"PPI\", \"其他\"]', '==', '其他', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (490, 35, 489, '其他用药', 'input', 1, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (491, 35, 489, '抗血小板具体用药', 'checkbox', 1, 4, NULL, NULL, NULL, NULL, '[\"阿司匹林\", \"P2Y12受体抑制剂（氯吡格雷/替格瑞洛）\", \"其他\"]', '==', '其他', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (492, 35, 491, '其他抗血小板用药', 'input', 1, 5, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (493, 35, 489, '抗凝具体用药', 'checkbox', 1, 6, NULL, NULL, NULL, NULL, '[\"华法林\", \"利伐沙班\", \"依度沙班\", \"达比加群\", \"其他\"]', '==', '其他', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (494, 35, 493, '其他抗凝用药', 'input', 1, 7, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (495, 35, 489, 'ACEI/ARB具体用药', 'checkbox', 1, 8, NULL, NULL, NULL, NULL, '[\"贝那普利\", \"依那普利\", \"沙库巴曲缬沙坦\", \"厄贝沙坦\", \"其他\"]', '==', '其他', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (496, 35, 495, '其他ACEI/ARB用药', 'input', 1, 9, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (497, 35, 489, 'β阻滞剂具体用药', 'checkbox', 1, 10, NULL, NULL, NULL, NULL, '[\"比索洛尔\", \"美托洛尔\", \"索他洛尔\", \"其他\"]', '==', '其他', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (498, 35, 497, '其他β阻滞剂用药', 'input', 1, 11, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (499, 35, 489, '他汀类具体用药', 'checkbox', 1, 12, NULL, NULL, NULL, NULL, '[\"辛伐他汀\", \"瑞舒伐他汀\", \"阿托伐他汀\", \"其他\"]', '==', '其他', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (500, 35, 499, '其他他汀类用药', 'input', 1, 13, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (501, 11, 489, '利尿剂具体用药', 'checkbox', 1, 1, NULL, NULL, NULL, NULL, '[\"螺内酯\", \"呋塞米\", \"氢氯噻嗪\", \"其他\"]', '==', '其他', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (502, 11, 501, '其他利尿剂用药', 'input', 1, 2, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (503, 11, 489, 'SGLT-2i具体用药', 'checkbox', 1, 3, NULL, NULL, NULL, NULL, '[\"达格列净\", \"恩格列净\", \"其他\"]', '==', '其他', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (504, 11, 503, '其他SGLT-2i用药', 'input', 1, 4, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (505, 11, 489, 'PPI具体用药', 'checkbox', 1, 5, NULL, NULL, NULL, NULL, '[\"雷贝拉唑\", \"泮托拉唑\", \"其他\"]', '==', '其他', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (506, 11, 505, '其他PPI用药', 'input', 1, 6, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (507, 12, NULL, '白细胞计数 WBC（10^12/L）', 'input', 1, 10, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (508, 12, NULL, '红细胞计数 RBC（10^12/L）', 'input', 1, 11, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (509, 12, NULL, '血小板计数 Plt（10^9/L）', 'input', 1, 12, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (510, 12, NULL, '中性粒细胞绝对值（10^9/L）', 'input', 1, 13, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (511, 12, NULL, '淋巴细胞绝对值（10^9/L）', 'input', 1, 14, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (512, 12, NULL, '单核细胞绝对值（10^9/L）', 'input', 1, 15, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (513, 12, NULL, '肌钙蛋白 cTNI ( ng/ml)', 'input', 1, 16, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (514, 12, NULL, '红细胞压积 HCT（%）', 'input', 1, 17, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (515, 12, NULL, '降钙素原 PCT（pg/mL）', 'input', 1, 18, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (516, 12, NULL, '总胆固醇 TC (mmol/L)', 'input', 1, 19, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (517, 12, NULL, '甘油三酯 TG（mmol/L）', 'input', 1, 20, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (518, 12, NULL, '高密度脂蛋白 HDL（mmol/L）*', 'input', 1, 21, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (519, 12, NULL, '同型半胱氨酸 HCY（µmol/L）*', 'input', 1, 22, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (520, 12, NULL, '丙氨酸氨基转移酶 ALT（U/L）', 'input', 1, 23, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (521, 12, NULL, '门冬氨酸转氨酶 AST（U/L）', 'input', 1, 24, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (522, 12, NULL, '估算肾小球滤过率 eGFR（ml/min）', 'input', 1, 25, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (523, 12, NULL, '国际标准化比值 INR', 'input', 1, 26, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (524, 12, NULL, '凝血酶原时间 PT（sec）', 'input', 1, 27, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (525, 12, NULL, 'D-二聚体（mg/L FEU）', 'input', 1, 28, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (526, 12, NULL, '血氧饱和度 SaO2（血气分析）（%）', 'input', 1, 29, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (527, 12, NULL, '血钾 K+（mmol/L）', 'input', 1, 30, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (528, 12, NULL, '血钙 Ca+（mmol/L）', 'input', 1, 31, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (529, 12, NULL, '全程C反应蛋白 CRP', 'input', 1, 37, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (530, 12, NULL, '全程C反应蛋白 CRP单位', 'radio', 1, 38, NULL, NULL, NULL, NULL, '[\"mg/l\", \"mg/dl\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (531, 12, NULL, '血红蛋白Hb', 'input', 1, 40, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (532, 12, NULL, '血红蛋白Hb单位', 'radio', 1, 41, NULL, NULL, NULL, NULL, '[\"g/l\", \"g/dl\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (533, 12, NULL, '白蛋白', 'input', 1, 43, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (534, 12, NULL, '白蛋白单位', 'radio', 1, 44, NULL, NULL, NULL, NULL, '[\"g/l\", \"g/dl\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (535, 12, NULL, '低密度脂蛋白', 'input', 1, 46, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (536, 12, NULL, '低密度脂蛋白单位', 'radio', 1, 47, NULL, NULL, NULL, NULL, '[\"mmol/l\", \"mg/dl\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (537, 12, NULL, '脂蛋白a  Lp', 'input', 1, 49, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (538, 12, NULL, '脂蛋白a  Lp单位', 'radio', 1, 50, NULL, NULL, NULL, NULL, '[\"mg/dl\", \"mmol/l\", \"nmol/l\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (539, 12, NULL, '肌酐 Cre', 'input', 1, 52, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (540, 12, NULL, '肌酐 Cre单位', 'radio', 1, 53, NULL, NULL, NULL, NULL, '[\"µmol/l\", \"mg/dl\", \"μmol/l\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (541, 12, NULL, 'NT-proBNP', 'input', 1, 55, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (542, 12, NULL, 'NT-proBNP单位', 'radio', 1, 56, NULL, NULL, NULL, NULL, '[\"pg/ml\", \"pmol/l\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (543, 12, NULL, 'B型脑钠肽 BNP', 'input', 1, 58, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (544, 12, NULL, 'B型脑钠肽 BNP单位', 'radio', 1, 59, NULL, NULL, NULL, NULL, '[\"pg/ml\", \"pmol/l\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (545, 12, NULL, 'P-R间期', 'input', 1, 61, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (546, 12, NULL, 'QRS间期', 'input', 1, 62, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (547, 12, NULL, 'QT', 'input', 1, 63, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (548, 12, NULL, 'QTc', 'input', 1, 64, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (549, 12, NULL, 'RV5', 'input', 1, 65, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (550, 12, NULL, 'SV1', 'input', 1, 66, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (551, 12, NULL, '术后新发心电图改变', 'radio', 1, 32, NULL, NULL, NULL, NULL, '[\"完全性左束支传导阻滞\", \"高度房室传导阻滞\", \"心房颤动/扑动\", \"其它\"]', '==', '其他', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (552, 12, 551, '术后新发心电图改变', 'input', 1, 33, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (553, 12, NULL, '经胸超声心动图（TTE）', 'radio', 1, 34, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', '==', '是', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (554, 12, 553, '主动脉窦部＜35mm', 'input', 1, 35, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (555, 12, 553, '主动脉瓣环 mm', 'input', 1, 36, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (556, 12, 553, '升主动脉＜35mm', 'input', 1, 39, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (557, 12, 553, '左房内径＜35mm', 'input', 1, 42, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (558, 12, 553, '右室前壁厚度＜6mm', 'input', 1, 45, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (559, 12, 553, '右室内径＜25mm', 'input', 1, 48, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (560, 12, 553, '室间隔厚度（8-11mm）', 'input', 1, 51, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (561, 12, 553, '左室后壁厚度（8-11mm）', 'input', 1, 54, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (562, 12, 553, '左室舒张末径（35-55mm）', 'input', 1, 57, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (563, 12, 553, '左室收缩末径（25-37mm）', 'input', 1, 60, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (564, 12, 553, '左室射血分数 %', 'input', 1, 67, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (565, 12, 553, '左室短轴缩短率≥25%', 'input', 1, 68, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (566, 12, 553, '肺动脉内径（16-25mm）', 'input', 1, 69, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (567, 12, 553, '右房左右径（30-40mm）', 'input', 1, 70, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (568, 12, 553, '肺动脉收缩压 mmHg（是否大于60mmhg）', 'input', 1, 71, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (569, 12, 553, '二尖瓣口舒张期血流速度cm/s e', 'input', 1, 72, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (570, 12, 553, '二尖瓣口收缩期血流速度cm/s a', 'input', 1, 73, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (571, 12, 553, '二尖瓣瓣环速度cm/s e', 'input', 1, 74, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (572, 12, 553, '二尖瓣瓣环速度cm/s a', 'input', 1, 75, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (573, 12, 553, '左室流出道内径 mm', 'input', 1, 76, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (574, 12, 553, '左室流出道面积 cm²', 'input', 1, 77, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (575, 12, 553, '左室流出道椭圆率', 'input', 1, 78, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (576, 12, 553, '右室流出道内径 mm', 'input', 1, 79, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (577, 12, 553, '三尖瓣环平面收缩期偏移（TAPSE）', 'input', 1, 80, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (578, 12, 553, '右心室应变（%）', 'input', 1, 81, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (579, 12, 553, '左心房应变（%）', 'input', 1, 82, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (580, 12, 553, '左心室收缩末期容积指数（LVESVI）（＜49ml/m 2）', 'input', 1, 83, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (581, 12, 553, '左心室舒张末期容积指数（LVEDVI）（＜97ml/m 2）', 'input', 1, 84, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (582, 12, NULL, '术中/术后即刻经食道超声心动图(TEE)', 'radio', 1, 85, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', '==', '是', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (583, 12, 582, '最大跨瓣压差（mmHg）', 'input', 1, 86, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (584, 12, 582, '平均跨瓣压差（mmHg）', 'input', 1, 87, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (585, 12, 582, '峰值流速（cm/s））', 'input', 1, 88, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (586, 12, 582, '估测瓣口面积 cm²', 'input', 1, 89, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (587, 12, 582, '术后主动脉瓣瓣周漏', 'radio', 1, 90, NULL, NULL, NULL, NULL, '[\"无\", \"轻度\", \"中度\", \"重度\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (588, 12, 582, '术后主动脉瓣瓣中心性反流', 'radio', 1, 91, NULL, NULL, NULL, NULL, '[\"无\", \"轻度\", \"中度\", \"重度\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (589, 12, 582, '上传图像', 'upload', 1, 92, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (590, 12, NULL, '心脏CT', 'radio', 1, 1, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', '==', '是', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (591, 12, 590, '心脏CT检查时间', 'date', 1, 2, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (592, 12, 590, '上传心脏CT图片', 'upload', 1, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (593, 12, 590, '瓣叶增厚程度', 'radio', 1, 4, NULL, NULL, NULL, NULL, '[\"无\", \"≤25%\", \"25%-50%\", \"51%-75%\", \"＞75%\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (594, 12, 590, '瓣叶活动度（级）', 'input', 1, 5, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (595, 12, 590, '收缩期', 'radio', 1, 6, NULL, NULL, NULL, NULL, '[\"正常\", \"轻度受限\", \"完全无运动\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (596, 12, 590, '舒张期', 'radio', 1, 7, NULL, NULL, NULL, NULL, '[\"正常\", \"轻度受限\", \"完全无运动\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (597, 12, 590, '瓣膜微血栓', 'radio', 1, 8, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', '==', '是', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (598, 12, 597, '是否抗凝治疗', 'radio', 1, 9, NULL, NULL, NULL, NULL, '[\"否\", \"是\", \"原有继续\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (599, 12, NULL, '其它检查', 'input', 1, 93, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (600, 13, NULL, '住院治疗', 'radio', 1, 1, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', '==', '是', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (601, 13, 600, '入院日期', 'date', 1, 2, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (602, 13, 600, '出院日期', 'date', 1, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (603, 13, 600, '监护室天数（天）', 'number', 1, 4, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (604, 13, 600, '住院总费用（元）', 'number', 1, 5, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (605, 13, 600, '全因死亡', 'radio', 1, 6, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', '==', '是', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (606, 13, 605, '死亡日期', 'date', 1, 7, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (607, 13, 605, '直接死因', 'input', 1, 8, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (608, 13, 605, '出院时心功能分级（NHYA）', 'input', 1, 9, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (609, 13, 600, '转外科手术', 'radio', 1, 10, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (610, 13, 600, '冠脉阻塞', 'radio', 1, 11, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (611, 13, 600, '心肌穿孔/心包填塞', 'radio', 1, 12, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (612, 13, 600, '瓣膜异位释放', 'radio', 1, 13, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (613, 13, 600, '瓣环破裂', 'radio', 1, 14, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (614, 13, 600, '非计划体外循环', 'radio', 1, 15, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (615, 13, 600, '新发左束支传导阻滞', 'radio', 1, 16, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (616, 13, 600, '新发高度房室传导阻滞', 'radio', 1, 17, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (617, 13, 600, '新发其他严重心律失常', 'radio', 1, 18, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', '==', '是', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (618, 13, 617, '新发其他严重心律失常具体类型', 'input', 1, 19, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (619, 13, 600, '术后永久起搏器植入', 'radio', 1, 20, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', '==', '是', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (620, 13, 619, '植入起搏器类型', 'input', 1, 21, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (621, 13, 600, '脑卒中', 'radio', 1, 22, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', '==', '是', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (622, 13, 621, '脑卒中日期', 'date', 1, 23, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (623, 13, 621, '脑卒中类型', 'radio', 1, 24, NULL, NULL, NULL, NULL, '[\"缺血性\", \"出血性\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (624, 13, 600, '心肌梗死', 'radio', 1, 25, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', '==', '是', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (625, 13, 624, '心肌梗死日期', 'date', 1, 26, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (626, 13, 600, '急性肾衰竭', 'radio', 1, 27, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (627, 13, 600, '外周动脉血栓栓塞', 'radio', 1, 28, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (628, 13, 600, '主动脉夹层', 'radio', 1, 29, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (629, 13, 600, '出血事件', 'radio', 1, 30, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (630, 13, 600, '术后输血', 'radio', 1, 31, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (631, 13, 600, '感染性心内膜炎', 'radio', 1, 32, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (632, 13, 600, '肺栓塞', 'radio', 1, 33, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (633, 13, 600, '院内感染', 'radio', 1, 34, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (634, 13, 600, '其他并发症', 'input', 1, 35, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (635, 14, NULL, '随访时间', 'date', 1, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-04-28 21:17:39');
INSERT INTO `qus_question` VALUES (636, 14, NULL, '是否死亡', 'radio', 1, 2, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', '==', '是', '2026-04-28 21:17:39', '2026-05-02 03:12:33');
INSERT INTO `qus_question` VALUES (637, 14, 636, '死亡日期', 'date', 1, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (638, 14, 636, '直接死因', 'radio', 1, 4, NULL, NULL, NULL, NULL, '[\"心源性\", \"非心源性\", \"难以确定\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (639, 14, NULL, '是否再住院', 'radio', 1, 5, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', '==', '是', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (640, 14, 639, '再住院日期', 'date', 1, 6, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (641, 14, 639, '再住院原因', 'radio', 1, 7, NULL, NULL, NULL, NULL, '[\"心衰\", \"瓣膜病外科手术或介入\", \"其他心血管疾病\", \"非心源性原因\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (642, 14, NULL, '临床症状', 'radio', 1, 8, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', '==', '是', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (643, 14, 642, '具体临床症状', 'checkbox', 1, 9, NULL, NULL, NULL, NULL, '[\"胸闷\", \"胸痛\", \"心绞痛\", \"水肿\", \"头晕\", \"晕厥\", \"心悸\", \"呼吸困难\", \"活动耐力下降\", \"活动后气短\", \"其他\"]', '==', '其他', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (644, 14, 643, 'CCS分级', 'radio', 1, 10, NULL, NULL, NULL, NULL, '[\"Ⅰ级\", \"Ⅱ级\", \"Ⅲ级\", \"Ⅳ级\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (645, 14, 643, '其他症状', 'input', 1, 11, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (646, 14, NULL, '纽约心功能分级', 'radio', 1, 12, NULL, NULL, NULL, NULL, '[\"Ⅰ级\", \"Ⅱ级\", \"Ⅲ级\", \"Ⅳ级\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (647, 14, NULL, '是否进行衰弱评估', 'radio', 0, 13, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', '==', '是', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (648, 14, 647, '平素运动习惯（散步）', 'radio', 0, 14, NULL, NULL, NULL, NULL, '[\"=<1h/w\", \"=<1-2h/w(不包括2h)\", \"=<2-3h/w(不包括3h)\", \"≥3h/w\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (649, 14, 647, '近一年内跌倒病史', 'radio', 0, 15, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (650, 14, 647, '是否需要生活辅助器具（如轮椅、拐杖）', 'radio', 0, 16, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (651, 14, 647, '是否在家吸氧', 'radio', 0, 17, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (652, 14, 647, '一周内感到疲乏天数', 'radio', 0, 18, NULL, NULL, NULL, NULL, '[\"1d\", \"2d\", \"3-4d\", \"4d以上\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (653, 14, 647, '近3个月有没有食欲下降或进食困难', 'radio', 0, 19, NULL, NULL, NULL, NULL, '[\"否\", \"轻度\", \"中度\", \"重度\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (654, 14, 647, 'MMSE量表测试结果（分）', 'number', 0, 20, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (655, 14, 647, 'Barthel指数量表(0-100)评定结果（分）', 'number', 0, 21, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (656, 14, NULL, '六分钟步行试验结果（6MWT）（m）', 'number', 0, 22, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (657, 15, NULL, '收缩压（mmHg）', 'input', 1, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (658, 15, NULL, '舒张压（mmHg）', 'input', 1, 2, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (659, 15, NULL, '静息心率（次/分）', 'input', 1, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (660, 16, NULL, '心力衰竭', 'radio', 1, 1, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', '==', '是', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (661, 16, 660, '心力衰竭类型', 'input', 1, 2, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (662, 16, NULL, '脑卒中', 'radio', 1, 3, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', '==', '是', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (663, 16, 662, '脑卒中时间', 'date', 1, 4, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (664, 16, 662, '脑卒中症状', 'radio', 1, 5, NULL, NULL, NULL, NULL, '[\"头晕\", \"肢体活动障碍\", \"言语不利\", \"其他\"]', '==', '其他', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (665, 16, 664, '其他症状', 'input', 1, 6, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (666, 16, 662, '脑卒中类型', 'radio', 1, 7, NULL, NULL, NULL, NULL, '[\"缺血性\", \"出血性\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (667, 16, 662, '卒中致残', 'radio', 1, 8, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (668, 16, NULL, '严重心律失常', 'radio', 1, 9, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', '==', '是', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (669, 16, 668, '心律失常后果', 'checkbox', 1, 10, NULL, NULL, NULL, NULL, '[\"新发房颤/房扑\", \"室速/室颤\", \"高度/三度传导阻滞\", \"其他\"]', '==', '其他', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (670, 16, 669, '其他心律失常', 'input', 1, 11, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (671, 16, NULL, '急性心肌梗死', 'radio', 1, 12, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', '==', '是', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (672, 16, 671, '心肌梗死时间', 'date', 1, 13, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (673, 16, 671, '是否手术/介入干预', 'radio', 1, 14, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 02:53:54');
INSERT INTO `qus_question` VALUES (674, 16, NULL, '人工瓣膜功能障碍', 'radio', 1, 15, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 00:53:40');
INSERT INTO `qus_question` VALUES (675, 16, NULL, '大出血', 'radio', 1, 16, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', '==', '是', '2026-04-28 21:17:39', '2026-05-02 03:12:33');
INSERT INTO `qus_question` VALUES (676, 16, 675, '大出血时间', 'date', 1, 17, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (677, 16, NULL, '肾功能衰竭', 'radio', 1, 18, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (678, 16, NULL, '感染性心内膜炎', 'radio', 1, 19, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', '==', '是', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (679, 16, 678, '感染性心内膜炎时间', 'date', 1, 20, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (680, 16, NULL, '其他（时间+事件）', 'input', 0, 21, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (681, 17, NULL, '瓣膜外科或介入', 'radio', 1, 1, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', '==', '是', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (682, 17, 681, '外科瓣膜修复术', 'radio', 1, 2, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', '==', '是', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (683, 17, 682, '外科瓣膜修复术时间', 'date', 1, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (684, 17, 682, '外科瓣膜修复术瓣膜（多选）', 'checkbox', 1, 4, NULL, NULL, NULL, NULL, '[\"二尖瓣\", \"主动脉瓣\", \"三尖瓣\", \"肺动脉瓣\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (685, 17, 681, '外科瓣膜置换术', 'radio', 1, 5, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', '==', '是', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (686, 17, 685, '外科瓣膜置换术时间', 'date', 1, 6, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (687, 17, 685, '外科瓣膜置换术瓣膜（多选）', 'checkbox', 1, 7, NULL, NULL, NULL, NULL, '[\"二尖瓣\", \"主动脉瓣\", \"三尖瓣\", \"肺动脉瓣\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (688, 17, 681, '经皮球囊瓣膜成形术', 'radio', 1, 8, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', '==', '是', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (689, 17, 688, '经皮球囊瓣膜成形术时间', 'date', 1, 9, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (690, 17, 688, '经皮球囊瓣膜成形术瓣膜（多选）', 'checkbox', 1, 10, NULL, NULL, NULL, NULL, '[\"二尖瓣\", \"主动脉瓣\", \"三尖瓣\", \"肺动脉瓣\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (691, 17, 681, '经导管主动脉瓣置换术（TAVR）', 'radio', 1, 11, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', '==', '是', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (692, 17, 691, '经导管主动脉瓣置换术（TAVR）时间', 'date', 1, 12, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (693, 17, 681, '二尖瓣反流介入治疗', 'radio', 1, 13, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', '==', '是', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (694, 17, 693, '二尖瓣反流介入治疗时间', 'date', 1, 14, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (695, 17, 681, '三尖瓣反流介入治疗', 'radio', 1, 15, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', '==', '是', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (696, 17, 695, '三尖瓣反流介入治疗时间', 'date', 1, 16, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (697, 17, NULL, '主动脉外科手术', 'radio', 1, 17, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', '==', '是', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (698, 17, 697, '主动脉外科手术治疗方式', 'input', 1, 18, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (699, 17, 697, '主动脉外科手术时间', 'date', 1, 19, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (700, 17, NULL, 'PCI', 'radio', 1, 20, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', '==', '是', '2026-04-28 21:17:39', '2026-05-02 03:12:33');
INSERT INTO `qus_question` VALUES (701, 17, 700, 'PCI时间', 'date', 1, 21, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (702, 17, NULL, 'CABG', 'radio', 1, 22, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', '==', '是', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (703, 17, 702, 'CABG时间', 'date', 1, 23, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (704, 17, NULL, '永久起搏器植入', 'radio', 1, 24, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', '==', '是', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (705, 17, 704, '永久起搏器植入时间', 'date', 1, 25, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (706, 17, 704, '永久起搏器植入类型', 'input', 1, 26, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (707, 17, NULL, '外科/介入心脏射频消融手术', 'radio', 1, 27, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', '==', '是', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (708, 17, 707, '外科/介入心脏射频消融手术时间', 'date', 1, 28, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (709, 17, 707, '外科/介入心脏射频消融手术方式', 'radio', 1, 29, NULL, NULL, NULL, NULL, '[\"介入\", \"外科\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (710, 17, NULL, '其他心脏手术', 'radio', 1, 30, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', '==', '是', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (711, 17, 710, '其他心脏手术时间', 'date', 1, 31, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (712, 17, 710, '其他心脏手术治疗方式', 'input', 1, 32, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (713, 17, NULL, '其他', 'input', 1, 33, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (714, 18, NULL, '目前用药', 'radio', 1, 1, NULL, NULL, NULL, NULL, '[\"是\", \"否\"]', '==', '是', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (715, 18, 714, '用药类型', 'checkbox', 1, 2, NULL, NULL, NULL, NULL, '[\"抗血小板\", \"抗凝\", \"ACEI/ARB\", \"β阻滞剂\", \"他汀类\", \"利尿剂\", \"SGLT-2i\", \"PPI\", \"其他\"]', '==', '其他', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (716, 18, 715, '抗血小板具体用药', 'radio', 1, 3, NULL, NULL, NULL, NULL, '[\"阿司匹林\", \"P2Y12受体抑制剂（氯吡格雷/替格瑞洛）\", \"其他\"]', '==', '其他', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (717, 18, 715, '抗凝具体用药', 'radio', 1, 4, NULL, NULL, NULL, NULL, '[\"华法林\", \"利伐沙班\", \"依度沙班\", \"达比加群\", \"其他\"]', '==', '其他', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (718, 18, 715, 'ACEI/ARB具体用药', 'radio', 1, 5, NULL, NULL, NULL, NULL, '[\"贝那普利\", \"依那普利\", \"沙库巴曲缬沙坦\", \"厄贝沙坦\", \"其他\"]', '==', '其他', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (719, 18, 715, 'β阻滞剂具体用药', 'radio', 1, 6, NULL, NULL, NULL, NULL, '[\"比索洛尔\", \"美托洛尔\", \"索他洛尔\", \"其他\"]', '==', '其他', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (720, 18, 715, '他汀类具体用药', 'radio', 1, 7, NULL, NULL, NULL, NULL, '[\"辛伐他汀\", \"瑞舒伐他汀\", \"阿托伐他汀\", \"其他\"]', '==', '其他', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (721, 18, 715, '利尿剂具体用药', 'radio', 1, 8, NULL, NULL, NULL, NULL, '[\"螺内酯\", \"呋塞米\", \"氢氯噻嗪\", \"其他\"]', '==', '其他', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (722, 18, 715, 'SGLT-2i具体用药', 'radio', 1, 9, NULL, NULL, NULL, NULL, '[\"达格列净\", \"恩格列净\", \"其他\"]', '==', '其他', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (723, 18, 715, 'PPI具体用药', 'radio', 1, 10, NULL, NULL, NULL, NULL, '[\"雷贝拉唑\", \"泮托拉唑\", \"其他\"]', '==', '其他', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (724, 18, 716, '其他抗血小板用药', 'input', 1, 11, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (725, 18, 717, '其他抗凝用药', 'input', 1, 12, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (726, 18, 718, '其他ACEI/ARB用药', 'input', 1, 13, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (727, 18, 719, '其他β阻滞剂用药', 'input', 1, 14, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (728, 18, 720, '其他他汀类用药', 'input', 1, 15, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (729, 18, 721, '其他利尿剂用药', 'input', 1, 16, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (730, 18, 722, '其他SGLT-2i用药', 'input', 1, 17, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (731, 18, 723, '其他PPI用药', 'input', 1, 18, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (732, 19, NULL, '患者的饮食习惯如何?', 'radio', 0, 1, NULL, NULL, NULL, NULL, '[\"荤素均衡\", \"荤食为主\", \"素食为主\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (733, 19, NULL, '您吃东西的口味为以下哪种情况?', 'radio', 0, 2, NULL, NULL, NULL, NULL, '[\"正常(不咸不淡)\", \"轻\", \"中\", \"重\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (734, 19, NULL, '您吃东西的油腻程度如何？', 'radio', 0, 3, NULL, NULL, NULL, NULL, '[\"少油\", \"正常\", \"油\", \"非常油\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (735, 20, NULL, '睡眠质量', 'radio', 0, 1, NULL, NULL, NULL, NULL, '[\"很好\", \"一般\", \"较差\", \"极差\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (736, 20, NULL, '午睡习惯', 'radio', 0, 2, NULL, NULL, NULL, NULL, '[\"无\", \"有\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (737, 31, NULL, '白细胞计数 WBC（10^12/L）', 'input', 1, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (738, 31, NULL, '红细胞计数 RBC（10^12/L）', 'input', 1, 2, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (739, 31, NULL, '血小板计数 Plt（10^9/L）', 'input', 1, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (740, 31, NULL, '中性粒细胞绝对值（10^9/L）', 'input', 1, 4, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (741, 31, NULL, '淋巴细胞绝对值（10^9/L）', 'input', 1, 5, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (742, 31, NULL, '单核细胞绝对值（10^9/L）', 'input', 1, 6, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (743, 31, NULL, '肌钙蛋白 cTNI ( ng/ml)', 'input', 1, 7, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (744, 31, NULL, '红细胞压积 HCT（%）', 'input', 1, 8, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (745, 31, NULL, '降钙素原 PCT（pg/mL）', 'input', 1, 9, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (746, 31, NULL, '总胆固醇 TC (mmol/L)', 'input', 1, 10, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (747, 31, NULL, '甘油三酯 TG（mmol/L）', 'input', 1, 11, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (748, 31, NULL, '高密度脂蛋白 HDL（mmol/L）*', 'input', 1, 12, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (749, 31, NULL, '同型半胱氨酸 HCY（µmol/L）*', 'input', 1, 13, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (750, 31, NULL, '丙氨酸氨基转移酶 ALT（U/L）', 'input', 1, 14, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (751, 31, NULL, '门冬氨酸转氨酶 AST（U/L）', 'input', 1, 15, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (752, 31, NULL, '估算肾小球滤过率 eGFR（ml/min）', 'input', 1, 16, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (753, 31, NULL, '国际标准化比值 INR', 'input', 1, 17, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (754, 31, NULL, '凝血酶原时间 PT（sec）', 'input', 1, 18, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (755, 31, NULL, 'D-二聚体（mg/L FEU）', 'input', 1, 19, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (756, 31, NULL, '血氧饱和度 SaO2（血气分析）（%）', 'input', 1, 20, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (757, 31, NULL, '血钾 K+（mmol/L）', 'input', 1, 21, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (758, 31, NULL, '血钙 Ca+（mmol/L）', 'input', 1, 22, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (759, 31, NULL, '全程C反应蛋白 CRP', 'input', 1, 23, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (760, 31, NULL, '全程C反应蛋白 CRP单位', 'radio', 1, 24, NULL, NULL, NULL, NULL, '[\"mg/l\", \"mg/dl\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (761, 31, NULL, '血红蛋白Hb', 'input', 1, 25, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (762, 31, NULL, '血红蛋白Hb单位', 'radio', 1, 26, NULL, NULL, NULL, NULL, '[\"g/l\", \"g/dl\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (763, 31, NULL, '白蛋白', 'input', 1, 27, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (764, 31, NULL, '白蛋白单位', 'radio', 1, 28, NULL, NULL, NULL, NULL, '[\"g/l\", \"g/dl\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (765, 31, NULL, '低密度脂蛋白', 'input', 1, 29, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (766, 31, NULL, '低密度脂蛋白单位', 'radio', 1, 30, NULL, NULL, NULL, NULL, '[\"mmol/l\", \"mg/dl\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (767, 31, NULL, '脂蛋白a  Lp', 'input', 1, 31, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (768, 31, NULL, '脂蛋白a  Lp单位', 'radio', 1, 32, NULL, NULL, NULL, NULL, '[\"mg/dl\", \"mmol/l\", \"nmol/l\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (769, 31, NULL, '肌酐 Cre', 'input', 1, 33, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (770, 31, NULL, '肌酐 Cre单位', 'radio', 1, 34, NULL, NULL, NULL, NULL, '[\"µmol/l\", \"mg/dl\", \"μmol/l\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (771, 31, NULL, 'NT-proBNP', 'input', 1, 35, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (772, 31, NULL, 'NT-proBNP单位', 'radio', 1, 36, NULL, NULL, NULL, NULL, '[\"pg/ml\", \"pmol/l\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (773, 31, NULL, 'B型脑钠肽 BNP', 'input', 1, 37, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (774, 31, NULL, 'B型脑钠肽 BNP单位', 'radio', 1, 38, NULL, NULL, NULL, NULL, '[\"pg/ml\", \"pmol/l\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (775, 24, NULL, 'P-R间期', 'input', 1, 7, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (776, 24, NULL, 'QRS间期', 'input', 1, 8, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (777, 24, NULL, 'QT', 'input', 1, 9, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (778, 24, NULL, 'QTc', 'input', 1, 10, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (779, 24, NULL, 'RV5', 'input', 1, 11, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (780, 24, NULL, 'SV1', 'input', 1, 12, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (781, 24, NULL, '心电图结果', 'radio', 1, 1, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', '==', '是', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (782, 24, 781, '心房颤动', 'radio', 1, 2, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (783, 24, 781, '束支传导阻滞', 'radio', 1, 3, NULL, NULL, NULL, NULL, '[\"否\", \"左束支传导阻滞\", \"右束支传导阻滞\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (784, 24, 781, '房室传导阻滞', 'radio', 1, 4, NULL, NULL, NULL, NULL, '[\"否\", \"室内传导阻滞\", \"高度房室传导阻滞\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (785, 24, 781, '起搏心律', 'radio', 1, 5, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (786, 24, 781, '上传心电图图片', 'upload', 1, 6, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (787, 25, NULL, '是否进行心脏彩超检查', 'radio', 1, 1, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', '==', '是', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (788, 25, 787, '心脏彩超日期', 'date', 1, 2, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (789, 25, 787, '主动脉瓣狭窄', 'radio', 1, 3, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', '==', '是', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (790, 25, 789, '主动脉瓣狭窄程度', 'input', 1, 4, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (791, 25, 789, '主动脉瓣峰值流速 m/s', 'input', 1, 5, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (792, 25, 789, '主动脉瓣口面积 cm²', 'input', 1, 6, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (793, 25, 789, '主动脉瓣平均压差 mmHg', 'input', 1, 7, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (794, 25, 787, '主动脉瓣反流', 'radio', 1, 8, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', '==', '是', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (795, 25, 794, '主动脉瓣瓣体', 'radio', 1, 9, NULL, NULL, NULL, NULL, '[\"自体瓣\", \"人工瓣\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (796, 25, 794, '主动脉瓣反流程度', 'input', 1, 10, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (797, 25, 794, '缩流颈宽度 mm', 'input', 1, 11, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (798, 25, 794, '平均跨瓣压差 mmHg', 'input', 1, 12, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (799, 25, 787, '二尖瓣狭窄', 'radio', 1, 13, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', '==', '是', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (800, 25, 799, '二尖瓣狭窄程度', 'input', 1, 14, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (801, 25, 799, '峰值流速 m/s', 'input', 1, 15, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (802, 25, 799, '二尖瓣口面积（cm²）', 'input', 1, 16, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (803, 25, 799, '平均跨瓣压差 mmHg', 'input', 1, 17, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (804, 25, 787, '二尖瓣反流', 'radio', 1, 18, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', '==', '是', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (805, 25, 804, '二尖瓣反流程度', 'input', 1, 19, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (806, 25, 804, '缩流颈宽度 mm', 'input', 1, 20, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (807, 25, 804, '平均跨瓣压差 mmHg', 'input', 1, 21, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (808, 25, 787, '三尖瓣狭窄', 'radio', 1, 22, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', '==', '是', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (809, 25, 808, '三尖瓣狭窄程度', 'input', 1, 23, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (810, 25, 808, '峰值流速 m/s', 'input', 1, 24, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (811, 25, 808, '三尖瓣口面积（cm²）', 'input', 1, 25, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (812, 25, 808, '平均跨瓣压差 mmHg', 'input', 1, 26, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (813, 25, 787, '三尖瓣反流', 'radio', 1, 27, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', '==', '是', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (814, 25, 813, '三尖瓣反流程度', 'input', 1, 28, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (815, 25, 813, '缩流颈宽度 mm', 'input', 1, 29, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (816, 25, 813, '平均跨瓣压差 mmHg', 'input', 1, 30, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (817, 25, 787, '肺动脉瓣狭窄', 'radio', 1, 31, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', '==', '是', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (818, 25, 817, '肺动脉瓣狭窄程度', 'input', 1, 32, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (819, 25, 817, '峰值流速 m/s', 'input', 1, 33, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (820, 25, 817, '最大跨瓣压差 mmHg', 'input', 1, 34, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (821, 25, 787, '肺动脉瓣反流', 'radio', 1, 35, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', '==', '是', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (822, 25, 821, '肺动脉瓣瓣体', 'radio', 1, 36, NULL, NULL, NULL, NULL, '[\"自体瓣\", \"人工瓣\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (823, 25, 821, '肺动脉瓣反流程度', 'input', 1, 37, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (824, 25, 821, '缩流颈宽度 mm', 'input', 1, 38, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (825, 25, 821, '平均跨瓣压差 mmHg', 'input', 1, 39, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (826, 25, 787, '心包积液', 'radio', 1, 40, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', '==', '是', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (827, 25, 826, '心包积液程度', 'input', 1, 41, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (828, 25, 787, '室壁运动异常', 'radio', 1, 42, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (829, 25, 787, '换瓣术后复查', 'radio', 1, 43, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (830, 25, 787, '瓣膜微血栓', 'radio', 1, 44, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (831, 25, 787, '赘生物', 'radio', 1, 45, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (832, 25, 787, '主动脉窦部＜35mm', 'input', 1, 46, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (833, 25, 787, '主动脉瓣环 mm', 'input', 1, 47, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (834, 25, 787, '升主动脉＜35mm', 'input', 1, 48, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (835, 25, 787, '左房内径＜35mm', 'input', 1, 49, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (836, 25, 787, '右室前壁厚度＜6mm', 'input', 1, 50, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (837, 25, 787, '右室内径＜25mm', 'input', 1, 51, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (838, 25, 787, '室间隔厚度（8-11mm）', 'input', 1, 52, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (839, 25, 787, '左室后壁厚度（8-11mm）', 'input', 1, 53, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (840, 25, 787, '左室舒张末径（35-55mm）', 'input', 1, 54, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (841, 25, 787, '左室收缩末径（25-37mm）', 'input', 1, 55, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (842, 25, 787, '左室射血分数 %', 'input', 1, 56, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (843, 25, 787, '左室短轴缩短率≥25%', 'input', 1, 57, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (844, 25, 787, '肺动脉内径（16-25mm）', 'input', 1, 58, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (845, 25, 787, '右房左右径（30-40mm）', 'input', 1, 59, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (846, 25, 787, '肺动脉收缩压 mmHg（是否大于60mmhg）', 'input', 1, 60, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (847, 25, 787, '二尖瓣口舒张期血流速度 e cm/s', 'input', 1, 61, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (848, 25, 787, '二尖瓣口收缩期血流速度 a cm/s', 'input', 1, 62, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (849, 25, 787, '二尖瓣瓣环速度 e cm/s', 'input', 1, 63, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (850, 25, 787, '二尖瓣瓣环速度 a cm/s', 'input', 1, 64, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (851, 25, 787, '左室流出道内径 mm', 'input', 1, 65, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (852, 25, 787, '左室流出道面积 cm²', 'input', 1, 66, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (853, 25, 787, '左室流出道椭圆率', 'input', 1, 67, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (854, 25, 787, '右室流出道内径 mm', 'input', 1, 68, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (855, 25, 787, '三尖瓣环平面收缩期偏移（TAPSE）', 'input', 1, 69, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (856, 25, 787, '右心室应变（%）', 'input', 1, 70, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (857, 25, 787, '左心房应变（%）', 'input', 1, 71, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (858, 25, 787, '左心室收缩末期容积指数（LVESVI）（＜49ml/m 2）', 'input', 1, 72, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (859, 25, 787, '左心室舒张末期容积指数（LVEDVI）（＜97ml/m 2）', 'input', 1, 73, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (860, 25, 787, '上传心脏彩超图片', 'upload', 1, 74, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (861, 26, NULL, '心脏CT', 'radio', 1, 1, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', '==', '是', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (862, 26, 861, '心脏CT检查时间', 'date', 1, 2, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (863, 26, 861, '上传心脏CT图片', 'upload', 1, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (864, 30, 861, '瓣叶增厚程度', 'radio', 1, 1, NULL, NULL, NULL, NULL, '[\"无\", \"≤25%\", \"25%-50%\", \"51%-75%\", \"＞75%\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (865, 30, 861, '瓣叶活动度（级）', 'input', 1, 2, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (866, 30, 861, '收缩期', 'radio', 1, 3, NULL, NULL, NULL, NULL, '[\"正常\", \"轻度受限\", \"完全无运动\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (867, 30, 861, '舒张期', 'radio', 1, 4, NULL, NULL, NULL, NULL, '[\"正常\", \"轻度受限\", \"完全无运动\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (868, 30, 861, '瓣膜微血栓', 'radio', 1, 5, NULL, NULL, NULL, NULL, '[\"否\", \"是\"]', '==', '是', '2026-04-28 21:17:39', '2026-05-02 03:28:26');
INSERT INTO `qus_question` VALUES (869, 30, 868, '是否抗凝治疗', 'radio', 1, 6, NULL, NULL, NULL, NULL, '[\"否\", \"是\", \"原有继续\"]', NULL, NULL, '2026-04-28 21:17:39', '2026-05-02 03:28:26');

-- ----------------------------
-- Table structure for qus_question_group
-- ----------------------------
DROP TABLE IF EXISTS `qus_question_group`;
CREATE TABLE `qus_question_group`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `questionnaire_id` bigint NOT NULL COMMENT '问卷ID',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '分组名称',
  `order` int UNSIGNED NOT NULL COMMENT '排序',
  `parent_id` bigint NULL DEFAULT NULL COMMENT '父分组',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '软删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `qus_question_groups_qus_questionnaire_type_id_fk`(`questionnaire_id` ASC) USING BTREE,
  INDEX `qus_question_group_qus_question_group_id_fk`(`parent_id` ASC) USING BTREE,
  CONSTRAINT `qus_question_group_qus_question_group_id_fk` FOREIGN KEY (`parent_id`) REFERENCES `qus_question_group` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `qus_question_qus_group_questionnaire_id_fk` FOREIGN KEY (`questionnaire_id`) REFERENCES `qus_questionnaire` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 37 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '分组' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of qus_question_group
-- ----------------------------
INSERT INTO `qus_question_group` VALUES (1, 1, '患者登记', 1, NULL, '2026-04-28 21:16:51', '2026-04-28 21:16:51', 0);
INSERT INTO `qus_question_group` VALUES (2, 1, '人口学信息', 2, NULL, '2026-04-28 21:16:51', '2026-04-28 21:16:51', 0);
INSERT INTO `qus_question_group` VALUES (3, 1, '临床表现', 3, NULL, '2026-04-28 21:16:51', '2026-04-28 21:16:51', 0);
INSERT INTO `qus_question_group` VALUES (4, 1, '体格检查', 4, NULL, '2026-04-28 21:16:51', '2026-04-28 21:16:51', 0);
INSERT INTO `qus_question_group` VALUES (5, 1, '既往史及合并疾病', 5, NULL, '2026-04-28 21:16:51', '2026-04-28 21:16:51', 0);
INSERT INTO `qus_question_group` VALUES (6, 1, '辅助检查', 6, NULL, '2026-04-28 21:16:51', '2026-04-28 21:16:51', 0);
INSERT INTO `qus_question_group` VALUES (7, 1, '辅助检查(心电图)', 5, 6, '2026-04-28 21:16:51', '2026-04-28 21:16:51', 0);
INSERT INTO `qus_question_group` VALUES (8, 1, '辅助检查(心脏彩超)', 4, 6, '2026-04-28 21:16:51', '2026-04-28 21:16:51', 0);
INSERT INTO `qus_question_group` VALUES (9, 1, '辅助检查(心脏CT)', 3, 6, '2026-04-28 21:16:51', '2026-04-28 21:16:51', 0);
INSERT INTO `qus_question_group` VALUES (10, 1, '辅助检查(其他检查)', 2, 6, '2026-04-28 21:16:51', '2026-04-28 21:16:51', 0);
INSERT INTO `qus_question_group` VALUES (11, 1, '治疗', 7, NULL, '2026-04-28 21:16:51', '2026-04-29 00:12:08', 0);
INSERT INTO `qus_question_group` VALUES (12, 1, '术后辅助检查', 8, NULL, '2026-04-28 21:16:51', '2026-04-28 21:16:51', 0);
INSERT INTO `qus_question_group` VALUES (13, 1, '住院期间不良事件', 9, NULL, '2026-04-28 21:16:51', '2026-04-28 21:16:51', 0);
INSERT INTO `qus_question_group` VALUES (14, 2, '临床表现', 1, NULL, '2026-04-28 21:16:51', '2026-04-28 21:16:51', 0);
INSERT INTO `qus_question_group` VALUES (15, 2, '体格检查', 2, NULL, '2026-04-28 21:16:51', '2026-04-28 21:16:51', 0);
INSERT INTO `qus_question_group` VALUES (16, 2, '主要并发症', 3, NULL, '2026-04-28 21:16:51', '2026-04-28 21:16:51', 0);
INSERT INTO `qus_question_group` VALUES (17, 2, '有创干预治疗', 4, NULL, '2026-04-28 21:16:51', '2026-04-28 21:16:51', 0);
INSERT INTO `qus_question_group` VALUES (18, 2, '目前用药', 5, NULL, '2026-04-28 21:16:51', '2026-04-28 21:16:51', 0);
INSERT INTO `qus_question_group` VALUES (19, 2, '饮食情况', 6, NULL, '2026-04-28 21:16:51', '2026-04-28 21:16:51', 0);
INSERT INTO `qus_question_group` VALUES (20, 2, '睡眠情况', 7, NULL, '2026-04-28 21:16:51', '2026-04-28 21:16:51', 0);
INSERT INTO `qus_question_group` VALUES (21, 2, '辅助检查', 8, NULL, '2026-04-28 21:16:51', '2026-04-28 21:16:51', 0);
INSERT INTO `qus_question_group` VALUES (23, 1, '辅助检查(血液检查)', 1, 6, '2026-04-29 00:10:24', '2026-04-29 00:10:24', 0);
INSERT INTO `qus_question_group` VALUES (24, 2, '辅助检查(心电图)', 1, 21, '2026-04-29 00:17:09', '2026-04-29 00:17:53', 0);
INSERT INTO `qus_question_group` VALUES (25, 2, '辅助检查(心脏彩超)', 2, 21, '2026-04-29 00:21:56', '2026-04-29 00:21:56', 0);
INSERT INTO `qus_question_group` VALUES (26, 2, '辅助检查(心脏CT)', 3, 21, '2026-04-29 00:22:06', '2026-04-29 00:23:01', 0);
INSERT INTO `qus_question_group` VALUES (30, 2, '辅助检查(其他检查)', 4, 21, '2026-04-29 00:43:50', '2026-04-29 00:43:50', 0);
INSERT INTO `qus_question_group` VALUES (31, 2, '辅助检查(血液检查)', 5, 21, '2026-04-29 00:44:01', '2026-04-29 00:44:01', 0);
INSERT INTO `qus_question_group` VALUES (32, 1, '外科手术治疗', 1, 11, '2026-04-29 10:34:17', '2026-04-29 10:34:17', 0);
INSERT INTO `qus_question_group` VALUES (33, 1, '瓣膜介入治疗', 2, 11, '2026-04-29 10:34:54', '2026-04-29 10:34:54', 0);
INSERT INTO `qus_question_group` VALUES (34, 1, '非瓣膜介入治疗', 3, 11, '2026-04-29 10:35:14', '2026-04-29 10:35:14', 0);
INSERT INTO `qus_question_group` VALUES (35, 1, '药物治疗', 4, 11, '2026-04-29 10:35:27', '2026-04-29 10:35:27', 0);

-- ----------------------------
-- Table structure for qus_questionnaire
-- ----------------------------
DROP TABLE IF EXISTS `qus_questionnaire`;
CREATE TABLE `qus_questionnaire`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '问卷名称',
  `is_published` tinyint(1) NOT NULL COMMENT '是否发布',
  `order` int NULL DEFAULT NULL COMMENT '排序',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '软删除',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '问卷' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of qus_questionnaire
-- ----------------------------
INSERT INTO `qus_questionnaire` VALUES (1, '基本信息', 1, 1, '2026-04-28 21:17:29', '2026-04-28 21:17:29', 0);
INSERT INTO `qus_questionnaire` VALUES (2, '随访信息', 1, 2, '2026-04-28 21:17:29', '2026-04-28 21:17:29', 0);

-- ----------------------------
-- Table structure for sys_feedbacks
-- ----------------------------
DROP TABLE IF EXISTS `sys_feedbacks`;
CREATE TABLE `sys_feedbacks`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '反馈ID',
  `institution_id` bigint NOT NULL COMMENT '机构ID',
  `feedback_type` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '反馈类型: question-疑问, suggestion-建议, complaint-投诉, issue-问题报告, other-其他',
  `feedback_title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '反馈标题',
  `feedback_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '反馈内容',
  `attachment_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '附件URL',
  `priority` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'medium' COMMENT '优先级: low-低, medium-中, high-高, urgent-紧急',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'pending' COMMENT '状态: pending-待处理, processing-处理中, resolved-已解决, closed-已关闭, rejected-已驳回',
  `reply_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '回复内容',
  `replied_by` bigint NULL DEFAULT NULL COMMENT '回复人ID',
  `replied_at` datetime NULL DEFAULT NULL COMMENT '回复时间',
  `feedback_by` bigint NULL DEFAULT NULL COMMENT '反馈人ID',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint(1) NULL DEFAULT 0 COMMENT '软删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_institution_id`(`institution_id` ASC) USING BTREE,
  INDEX `idx_feedback_type`(`feedback_type` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_priority`(`priority` ASC) USING BTREE,
  INDEX `idx_is_deleted`(`is_deleted` ASC) USING BTREE,
  CONSTRAINT `fk_if_institution` FOREIGN KEY (`institution_id`) REFERENCES `sys_institutions` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '机构反馈表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_feedbacks
-- ----------------------------

-- ----------------------------
-- Table structure for sys_guides
-- ----------------------------
DROP TABLE IF EXISTS `sys_guides`;
CREATE TABLE `sys_guides`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '指南ID',
  `institution_id` bigint NOT NULL COMMENT '机构ID',
  `guide_title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '指南标题',
  `guide_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '指南内容(富文本)',
  `guide_file_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '指南附件URL(PDF/Word等)',
  `guide_type` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'general' COMMENT '指南类型: general-通用指南, operation-操作指南, quality-质控指南, other-其他',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'active' COMMENT '状态: active-有效, expired-已过期, archived-已归档',
  `published_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
  `created_by` bigint NULL DEFAULT NULL COMMENT '发布人ID',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint(1) NULL DEFAULT 0 COMMENT '软删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_institution_id`(`institution_id` ASC) USING BTREE,
  INDEX `idx_guide_type`(`guide_type` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_published_at`(`published_at` ASC) USING BTREE,
  INDEX `idx_is_deleted`(`is_deleted` ASC) USING BTREE,
  CONSTRAINT `fk_ig_institution` FOREIGN KEY (`institution_id`) REFERENCES `sys_institutions` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '机构指南' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_guides
-- ----------------------------

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
INSERT INTO `sys_institutions` VALUES (1, NULL, 'main', '南昌二附医院', 1, 1, '2026-04-18 18:58:26', '2026-04-18 19:02:52');
INSERT INTO `sys_institutions` VALUES (2, 1, 'ncrm', '南昌人民医院', 1, 1, '2026-04-18 18:58:52', '2026-04-18 18:58:56');
INSERT INTO `sys_institutions` VALUES (3, 1, 'ncfs', '南昌附属医院', 1, 1, '2026-04-18 18:58:52', '2026-04-18 18:58:56');

-- ----------------------------
-- Table structure for sys_job
-- ----------------------------
DROP TABLE IF EXISTS `sys_job`;
CREATE TABLE `sys_job`  (
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
-- Records of sys_job
-- ----------------------------
INSERT INTO `sys_job` VALUES (1, '示例任务：打印HELLO WORLD', 'DEFAULT', 'com.hfh.api.modules.system.task.DemoJobTask#sayHello(Demo)', '0 0 0 * * ?', 1, 1, 1, 'admin', '2026-07-04 01:30:00', 'admin', '2026-07-04 01:30:00', '每晚12点打印HELLO {JobName}');
INSERT INTO `sys_job` VALUES (2, '随访问卷生成', 'DEFAULT', 'com.hfh.api.modules.system.task.QuestionnaireJobTask#createFollowUpQuestionnaire(2)', '0 0 2 * * ?', 1, 1, 1, 'admin', '2026-07-04 02:00:00', 'admin', '2026-07-04 02:00:00', '每天凌晨2点检查患者是否出院，出院后30天内未填写随访问卷则创建随访问卷');

-- ----------------------------
-- Table structure for sys_job_log
-- ----------------------------
DROP TABLE IF EXISTS `sys_job_log`;
CREATE TABLE `sys_job_log`  (
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
-- Records of sys_job_log
-- ----------------------------
INSERT INTO `sys_job_log` VALUES (1, '示例任务：打印HELLO WORLD', 'DEFAULT', 'com.hfh.api.modules.system.task.DemoJobTask#sayHello', '执行成功', 1, NULL, '2026-07-04 01:54:48');
INSERT INTO `sys_job_log` VALUES (2, '示例任务：打印HELLO WORLD', 'DEFAULT', 'com.hfh.api.modules.system.task.DemoJobTask#sayHello', '执行成功', 1, NULL, '2026-07-04 01:54:58');
INSERT INTO `sys_job_log` VALUES (3, '示例任务：打印HELLO WORLD', 'DEFAULT', 'com.hfh.api.modules.system.task.DemoJobTask#sayHello', '执行成功', 1, NULL, '2026-07-04 01:55:33');
INSERT INTO `sys_job_log` VALUES (4, '示例任务：打印HELLO WORLD', 'DEFAULT', 'com.hfh.api.modules.system.task.DemoJobTask#sayHello', '执行成功', 1, NULL, '2026-07-04 01:55:43');
INSERT INTO `sys_job_log` VALUES (5, '示例任务：打印HELLO WORLD', 'DEFAULT', 'com.hfh.api.modules.system.task.DemoJobTask#sayHello(\"Demo\")', '执行成功', 1, NULL, '2026-07-04 02:03:12');
INSERT INTO `sys_job_log` VALUES (6, '示例任务：打印HELLO WORLD', 'DEFAULT', 'com.hfh.api.modules.system.task.DemoJobTask#sayHello(Demo)', '执行成功', 1, NULL, '2026-07-04 02:03:25');
INSERT INTO `sys_job_log` VALUES (7, '示例任务：打印HELLO WORLD', 'DEFAULT', 'com.hfh.api.modules.system.task.DemoJobTask#sayHello(Demo)', '执行成功', 1, NULL, '2026-07-04 02:03:50');

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
) ENGINE = InnoDB AUTO_INCREMENT = 149 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '权限（1:接口权限 2:路由权限）' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_permission
-- ----------------------------
INSERT INTO `sys_permission` VALUES (1, 'system', '系统管理', 2, 0, '/admin/system', '', '/admin/system/user', 'Menu', 2, 1, 1, '2026-04-01 14:27:20', '2026-04-28 23:11:40');
INSERT INTO `sys_permission` VALUES (2, 'system:user', '用户管理', 2, 1, '/admin/system/user', '/admin/system/user/index', NULL, 'User', 2, 1, 1, '2026-04-01 14:27:20', '2026-04-01 14:27:20');
INSERT INTO `sys_permission` VALUES (3, 'system:role', '角色管理', 2, 1, '/admin/system/role', '/admin/system/role/index', NULL, 'Headset', 3, 1, 1, '2026-04-01 14:27:20', '2026-04-01 14:27:20');
INSERT INTO `sys_permission` VALUES (4, 'system:permission', '权限管理', 2, 1, '/admin/system/permission', '/admin/system/permission/index', NULL, 'Lock', 4, 1, 1, '2026-04-01 14:27:20', '2026-04-01 14:27:20');
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
INSERT INTO `sys_permission` VALUES (27, 'guides:list', '指南管理', 2, 1, '/admin/system/guides', '/admin/system/guides/index', NULL, 'Reading', 5, 1, 1, '2026-04-15 01:43:53', '2026-04-18 19:06:05');
INSERT INTO `sys_permission` VALUES (28, 'guides:add', '指南新增', 1, 27, NULL, NULL, NULL, NULL, 1, 1, 1, '2026-04-15 01:43:53', '2026-04-15 01:43:53');
INSERT INTO `sys_permission` VALUES (29, 'guides:delete', '指南删除', 1, 27, NULL, NULL, NULL, NULL, 2, 1, 1, '2026-04-15 01:43:53', '2026-04-15 01:43:53');
INSERT INTO `sys_permission` VALUES (30, 'guides:update', '指南更新', 1, 27, NULL, NULL, NULL, NULL, 3, 1, 1, '2026-04-15 01:43:53', '2026-04-15 01:43:53');
INSERT INTO `sys_permission` VALUES (31, 'guides:query', '指南查询', 1, 27, NULL, NULL, NULL, NULL, 4, 1, 1, '2026-04-15 01:43:53', '2026-04-15 01:43:53');
INSERT INTO `sys_permission` VALUES (32, 'feedbacks:list', '反馈管理', 2, 1, '/admin/system/feedbacks', '/admin/system/feedbacks/index', NULL, 'Phone', 6, 1, 1, '2026-04-15 01:44:22', '2026-04-18 19:06:05');
INSERT INTO `sys_permission` VALUES (33, 'feedbacks:add', '反馈新增', 1, 32, NULL, NULL, NULL, NULL, 1, 1, 1, '2026-04-15 01:44:23', '2026-04-15 01:44:23');
INSERT INTO `sys_permission` VALUES (34, 'feedbacks:delete', '反馈删除', 1, 32, NULL, NULL, NULL, NULL, 2, 1, 1, '2026-04-15 01:44:23', '2026-04-15 01:44:23');
INSERT INTO `sys_permission` VALUES (35, 'feedbacks:update', '反馈更新', 1, 32, NULL, NULL, NULL, NULL, 3, 1, 1, '2026-04-15 01:44:23', '2026-04-15 01:44:23');
INSERT INTO `sys_permission` VALUES (36, 'feedbacks:query', '反馈查询', 1, 32, NULL, NULL, NULL, NULL, 4, 1, 1, '2026-04-15 01:44:23', '2026-04-15 01:44:23');
INSERT INTO `sys_permission` VALUES (37, 'institutions:list', '机构管理', 2, 1, '/admin/system/institutions', '/admin/system/institutions/index', NULL, 'OfficeBuilding', 1, 1, 1, '2026-04-15 01:50:42', '2026-04-18 19:06:05');
INSERT INTO `sys_permission` VALUES (38, 'institutions:add', '机构新增', 1, 37, NULL, NULL, NULL, NULL, 1, 1, 1, '2026-04-15 01:50:42', '2026-04-15 01:50:42');
INSERT INTO `sys_permission` VALUES (39, 'institutions:delete', '机构删除', 1, 37, NULL, NULL, NULL, NULL, 2, 1, 1, '2026-04-15 01:50:42', '2026-04-15 01:50:42');
INSERT INTO `sys_permission` VALUES (40, 'institutions:update', '机构更新', 1, 37, NULL, NULL, NULL, NULL, 3, 1, 1, '2026-04-15 01:50:42', '2026-04-15 01:50:42');
INSERT INTO `sys_permission` VALUES (41, 'institutions:query', '机构查询', 1, 37, NULL, NULL, NULL, NULL, 4, 1, 1, '2026-04-15 01:50:42', '2026-04-15 01:50:42');
INSERT INTO `sys_permission` VALUES (93, 'questionnaire:list', '问卷管理', 2, 113, '/admin/form/questionnaire', '/admin/form/questionnaire/index', NULL, 'Files', 1, 1, 1, '2026-04-28 23:03:37', '2026-04-28 23:13:53');
INSERT INTO `sys_permission` VALUES (94, 'questionnaire:add', '问卷新增', 1, 93, NULL, NULL, NULL, NULL, 1, 1, 1, '2026-04-28 23:03:37', '2026-04-28 23:03:37');
INSERT INTO `sys_permission` VALUES (95, 'questionnaire:delete', '问卷删除', 1, 93, NULL, NULL, NULL, NULL, 2, 1, 1, '2026-04-28 23:03:37', '2026-04-28 23:03:37');
INSERT INTO `sys_permission` VALUES (96, 'questionnaire:update', '问卷更新', 1, 93, NULL, NULL, NULL, NULL, 3, 1, 1, '2026-04-28 23:03:37', '2026-04-28 23:03:37');
INSERT INTO `sys_permission` VALUES (97, 'questionnaire:query', '问卷查询', 1, 93, NULL, NULL, NULL, NULL, 4, 1, 1, '2026-04-28 23:03:37', '2026-04-28 23:03:37');
INSERT INTO `sys_permission` VALUES (98, 'questionGroup:list', '分组管理', 2, 113, '/admin/form/questionGroup', '/admin/form/questionGroup/index', NULL, 'Coin', 2, 1, 1, '2026-04-28 23:03:49', '2026-04-28 23:13:53');
INSERT INTO `sys_permission` VALUES (99, 'questionGroup:add', '分组新增', 1, 98, NULL, NULL, NULL, NULL, 1, 1, 1, '2026-04-28 23:03:49', '2026-04-28 23:03:49');
INSERT INTO `sys_permission` VALUES (100, 'questionGroup:delete', '分组删除', 1, 98, NULL, NULL, NULL, NULL, 2, 1, 1, '2026-04-28 23:03:49', '2026-04-28 23:03:49');
INSERT INTO `sys_permission` VALUES (101, 'questionGroup:update', '分组更新', 1, 98, NULL, NULL, NULL, NULL, 3, 1, 1, '2026-04-28 23:03:49', '2026-04-28 23:03:49');
INSERT INTO `sys_permission` VALUES (102, 'questionGroup:query', '分组查询', 1, 98, NULL, NULL, NULL, NULL, 4, 1, 1, '2026-04-28 23:03:49', '2026-04-28 23:03:49');
INSERT INTO `sys_permission` VALUES (103, 'question:list', '问题管理', 2, 113, '/admin/form/question', '/admin/form/question/index', NULL, 'Document', 3, 1, 1, '2026-04-28 23:03:57', '2026-04-28 23:13:53');
INSERT INTO `sys_permission` VALUES (104, 'question:add', '问题新增', 1, 103, NULL, NULL, NULL, NULL, 1, 1, 1, '2026-04-28 23:03:57', '2026-04-28 23:03:57');
INSERT INTO `sys_permission` VALUES (105, 'question:delete', '问题删除', 1, 103, NULL, NULL, NULL, NULL, 2, 1, 1, '2026-04-28 23:03:57', '2026-04-28 23:03:57');
INSERT INTO `sys_permission` VALUES (106, 'question:update', '问题更新', 1, 103, NULL, NULL, NULL, NULL, 3, 1, 1, '2026-04-28 23:03:57', '2026-04-28 23:03:57');
INSERT INTO `sys_permission` VALUES (107, 'question:query', '问题查询', 1, 103, NULL, NULL, NULL, NULL, 4, 1, 1, '2026-04-28 23:03:57', '2026-04-28 23:03:57');
INSERT INTO `sys_permission` VALUES (113, 'form', '表单管理', 2, 0, '/admin/form', '', '/system/questionnaire', 'List', 4, 1, 1, '2026-04-01 14:27:20', '2026-04-28 23:11:40');
INSERT INTO `sys_permission` VALUES (114, 'combined:list', '综合管理', 2, 113, '/admin/form/combined', '/admin/form/combined/index', NULL, 'FullScreen', 5, 1, 1, '2026-04-28 23:04:02', '2026-04-29 11:12:03');
INSERT INTO `sys_permission` VALUES (115, 'fill', '数据填报', 2, 0, '/admin/fill', '', '/system/patient', 'Histogram', 5, 1, 1, '2026-04-01 14:27:20', '2026-04-28 23:11:40');
INSERT INTO `sys_permission` VALUES (116, 'patient:list', '患者管理', 2, 115, '/admin/fill/patient', '/admin/fill/patient/index', NULL, 'WindPower', 1, 1, 1, '2026-04-29 09:42:07', '2026-04-29 09:45:25');
INSERT INTO `sys_permission` VALUES (117, 'patient:add', '患者新增', 1, 116, NULL, NULL, NULL, NULL, 1, 1, 1, '2026-04-29 09:42:07', '2026-04-29 09:42:07');
INSERT INTO `sys_permission` VALUES (118, 'patient:delete', '患者删除', 1, 116, NULL, NULL, NULL, NULL, 2, 1, 1, '2026-04-29 09:42:07', '2026-04-29 09:42:07');
INSERT INTO `sys_permission` VALUES (119, 'patient:update', '患者更新', 1, 116, NULL, NULL, NULL, NULL, 3, 1, 1, '2026-04-29 09:42:07', '2026-04-29 09:42:07');
INSERT INTO `sys_permission` VALUES (120, 'patient:query', '患者查询', 1, 116, NULL, NULL, NULL, NULL, 4, 1, 1, '2026-04-29 09:42:07', '2026-04-29 09:42:07');
INSERT INTO `sys_permission` VALUES (121, 'patientQuestionnaire:list', '患者问卷管理', 2, 115, '/admin/fill/patientQuestionnaire', '/admin/fill/patientQuestionnaire/index', NULL, 'Money', 2, 1, 1, '2026-04-29 09:42:16', '2026-04-29 09:45:25');
INSERT INTO `sys_permission` VALUES (122, 'patientQuestionnaire:add', '患者问卷新增', 1, 121, NULL, NULL, NULL, NULL, 1, 1, 1, '2026-04-29 09:42:16', '2026-04-29 09:42:16');
INSERT INTO `sys_permission` VALUES (123, 'patientQuestionnaire:delete', '患者问卷删除', 1, 121, NULL, NULL, NULL, NULL, 2, 1, 1, '2026-04-29 09:42:16', '2026-04-29 09:42:16');
INSERT INTO `sys_permission` VALUES (124, 'patientQuestionnaire:update', '患者问卷更新', 1, 121, NULL, NULL, NULL, NULL, 3, 1, 1, '2026-04-29 09:42:16', '2026-04-29 09:42:16');
INSERT INTO `sys_permission` VALUES (125, 'patientQuestionnaire:query', '患者问卷查询', 1, 121, NULL, NULL, NULL, NULL, 4, 1, 1, '2026-04-29 09:42:16', '2026-04-29 09:42:16');
INSERT INTO `sys_permission` VALUES (126, 'patientAnswer:list', '问卷答案管理', 2, 115, '/admin/fill/patientAnswer', '/admin/fill/patientAnswer/index', NULL, 'Position', 3, 1, 1, '2026-04-29 09:42:23', '2026-04-29 09:45:25');
INSERT INTO `sys_permission` VALUES (127, 'patientAnswer:add', '问卷答案新增', 1, 126, NULL, NULL, NULL, NULL, 1, 1, 1, '2026-04-29 09:42:23', '2026-04-29 09:42:23');
INSERT INTO `sys_permission` VALUES (128, 'patientAnswer:delete', '问卷答案删除', 1, 126, NULL, NULL, NULL, NULL, 2, 1, 1, '2026-04-29 09:42:23', '2026-04-29 09:42:23');
INSERT INTO `sys_permission` VALUES (129, 'patientAnswer:update', '问卷答案更新', 1, 126, NULL, NULL, NULL, NULL, 3, 1, 1, '2026-04-29 09:42:23', '2026-04-29 09:42:23');
INSERT INTO `sys_permission` VALUES (130, 'patientAnswer:query', '问卷答案查询', 1, 126, NULL, NULL, NULL, NULL, 4, 1, 1, '2026-04-29 09:42:23', '2026-04-29 09:42:23');
INSERT INTO `sys_permission` VALUES (131, 'preview:list', '表单预览', 2, 113, '/admin/form/preview', '/admin/form/preview/index', NULL, 'Notebook', 5, 1, 1, '2026-04-28 23:04:02', '2026-05-14 01:29:52');
INSERT INTO `sys_permission` VALUES (132, 'patient:export', '患者导出', 1, 116, NULL, NULL, NULL, NULL, 0, 1, 1, '2026-05-15 00:32:46', '2026-05-15 00:32:46');
INSERT INTO `sys_permission` VALUES (133, 'minio', '文件管理', 2, 0, '/admin/minio', '', '/admin/minio/index', 'UploadFilled', 3, 1, 1, '2026-06-17 22:43:07', '2026-06-17 22:43:07');
INSERT INTO `sys_permission` VALUES (134, 'minio:list', '文件列表', 2, 133, '/admin/minio/index', '/admin/minio/index', NULL, 'Folder', 1, 1, 1, '2026-06-17 22:43:07', '2026-06-17 22:43:07');
INSERT INTO `sys_permission` VALUES (135, 'minio:bucket:query', '查询存储桶', 1, 134, NULL, NULL, NULL, NULL, 1, 1, 1, '2026-06-17 22:43:07', '2026-06-17 22:43:07');
INSERT INTO `sys_permission` VALUES (136, 'minio:bucket:add', '创建存储桶', 1, 134, NULL, NULL, NULL, NULL, 2, 1, 1, '2026-06-17 22:43:07', '2026-06-17 22:43:07');
INSERT INTO `sys_permission` VALUES (137, 'minio:bucket:delete', '删除存储桶', 1, 134, NULL, NULL, NULL, NULL, 3, 1, 1, '2026-06-17 22:43:07', '2026-06-17 22:43:07');
INSERT INTO `sys_permission` VALUES (138, 'minio:object:query', '查询文件', 1, 134, NULL, NULL, NULL, NULL, 4, 1, 1, '2026-06-17 22:43:07', '2026-06-17 22:43:07');
INSERT INTO `sys_permission` VALUES (139, 'minio:object:upload', '上传文件', 1, 134, NULL, NULL, NULL, NULL, 5, 1, 1, '2026-06-17 22:43:07', '2026-06-17 22:43:07');
INSERT INTO `sys_permission` VALUES (140, 'minio:object:download', '下载文件', 1, 134, NULL, NULL, NULL, NULL, 6, 1, 1, '2026-06-17 22:43:07', '2026-06-17 22:43:07');
INSERT INTO `sys_permission` VALUES (141, 'minio:object:preview', '预览文件', 1, 134, NULL, NULL, NULL, NULL, 7, 1, 1, '2026-06-17 22:43:07', '2026-06-17 22:43:07');
INSERT INTO `sys_permission` VALUES (142, 'minio:object:delete', '删除文件', 1, 134, NULL, NULL, NULL, NULL, 8, 1, 1, '2026-06-17 22:43:07', '2026-06-17 22:43:07');
INSERT INTO `sys_permission` VALUES (143, 'job:list', '定时任务管理', 2, 1, '/admin/system/job', '/admin/system/job/index', NULL, 'Timer', 7, 1, 1, '2026-07-04 01:20:00', '2026-07-04 01:20:00');
INSERT INTO `sys_permission` VALUES (144, 'job:query', '定时任务查询', 1, 143, NULL, NULL, NULL, NULL, 1, 1, 1, '2026-07-04 01:20:00', '2026-07-04 01:20:00');
INSERT INTO `sys_permission` VALUES (145, 'job:add', '定时任务新增', 1, 143, NULL, NULL, NULL, NULL, 2, 1, 1, '2026-07-04 01:20:00', '2026-07-04 01:20:00');
INSERT INTO `sys_permission` VALUES (146, 'job:delete', '定时任务删除', 1, 143, NULL, NULL, NULL, NULL, 3, 1, 1, '2026-07-04 01:20:00', '2026-07-04 01:20:00');
INSERT INTO `sys_permission` VALUES (147, 'job:update', '定时任务更新', 1, 143, NULL, NULL, NULL, NULL, 4, 1, 1, '2026-07-04 01:20:00', '2026-07-04 01:20:00');
INSERT INTO `sys_permission` VALUES (148, 'jobLog:list', '定时任务日志', 2, 1, '/admin/system/jobLog', '/admin/system/jobLog/index', NULL, 'Document', 8, 1, 1, '2026-07-04 01:20:00', '2026-07-04 01:20:00');

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
INSERT INTO `sys_role` VALUES (1, '超级管理员', 'super_admin', '拥有所有权限', 1, '2026-04-14 01:02:28', '2026-04-14 01:02:28');
INSERT INTO `sys_role` VALUES (2, '填报人员', 'common_user', '普通用户权限', 1, '2026-04-14 01:02:28', '2026-05-14 01:57:24');
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
INSERT INTO `sys_role_permission` VALUES (1, 1, '2026-07-04 01:41:44', '2026-07-04 01:41:44');
INSERT INTO `sys_role_permission` VALUES (1, 2, '2026-07-04 01:41:44', '2026-07-04 01:41:44');
INSERT INTO `sys_role_permission` VALUES (1, 3, '2026-07-04 01:41:44', '2026-07-04 01:41:44');
INSERT INTO `sys_role_permission` VALUES (1, 4, '2026-07-04 01:41:44', '2026-07-04 01:41:44');
INSERT INTO `sys_role_permission` VALUES (1, 5, '2026-07-04 01:41:44', '2026-07-04 01:41:44');
INSERT INTO `sys_role_permission` VALUES (1, 6, '2026-07-04 01:41:44', '2026-07-04 01:41:44');
INSERT INTO `sys_role_permission` VALUES (1, 7, '2026-07-04 01:41:44', '2026-07-04 01:41:44');
INSERT INTO `sys_role_permission` VALUES (1, 8, '2026-07-04 01:41:44', '2026-07-04 01:41:44');
INSERT INTO `sys_role_permission` VALUES (1, 9, '2026-07-04 01:41:44', '2026-07-04 01:41:44');
INSERT INTO `sys_role_permission` VALUES (1, 10, '2026-07-04 01:41:44', '2026-07-04 01:41:44');
INSERT INTO `sys_role_permission` VALUES (1, 11, '2026-07-04 01:41:44', '2026-07-04 01:41:44');
INSERT INTO `sys_role_permission` VALUES (1, 12, '2026-07-04 01:41:44', '2026-07-04 01:41:44');
INSERT INTO `sys_role_permission` VALUES (1, 13, '2026-07-04 01:41:44', '2026-07-04 01:41:44');
INSERT INTO `sys_role_permission` VALUES (1, 14, '2026-07-04 01:41:44', '2026-07-04 01:41:44');
INSERT INTO `sys_role_permission` VALUES (1, 15, '2026-07-04 01:41:44', '2026-07-04 01:41:44');
INSERT INTO `sys_role_permission` VALUES (1, 16, '2026-07-04 01:41:44', '2026-07-04 01:41:44');
INSERT INTO `sys_role_permission` VALUES (1, 17, '2026-07-04 01:41:44', '2026-07-04 01:41:44');
INSERT INTO `sys_role_permission` VALUES (1, 18, '2026-07-04 01:41:44', '2026-07-04 01:41:44');
INSERT INTO `sys_role_permission` VALUES (1, 27, '2026-07-04 01:41:44', '2026-07-04 01:41:44');
INSERT INTO `sys_role_permission` VALUES (1, 28, '2026-07-04 01:41:44', '2026-07-04 01:41:44');
INSERT INTO `sys_role_permission` VALUES (1, 29, '2026-07-04 01:41:44', '2026-07-04 01:41:44');
INSERT INTO `sys_role_permission` VALUES (1, 30, '2026-07-04 01:41:44', '2026-07-04 01:41:44');
INSERT INTO `sys_role_permission` VALUES (1, 31, '2026-07-04 01:41:44', '2026-07-04 01:41:44');
INSERT INTO `sys_role_permission` VALUES (1, 32, '2026-07-04 01:41:44', '2026-07-04 01:41:44');
INSERT INTO `sys_role_permission` VALUES (1, 33, '2026-07-04 01:41:44', '2026-07-04 01:41:44');
INSERT INTO `sys_role_permission` VALUES (1, 34, '2026-07-04 01:41:44', '2026-07-04 01:41:44');
INSERT INTO `sys_role_permission` VALUES (1, 35, '2026-07-04 01:41:44', '2026-07-04 01:41:44');
INSERT INTO `sys_role_permission` VALUES (1, 36, '2026-07-04 01:41:44', '2026-07-04 01:41:44');
INSERT INTO `sys_role_permission` VALUES (1, 37, '2026-07-04 01:41:44', '2026-07-04 01:41:44');
INSERT INTO `sys_role_permission` VALUES (1, 38, '2026-07-04 01:41:44', '2026-07-04 01:41:44');
INSERT INTO `sys_role_permission` VALUES (1, 39, '2026-07-04 01:41:44', '2026-07-04 01:41:44');
INSERT INTO `sys_role_permission` VALUES (1, 40, '2026-07-04 01:41:44', '2026-07-04 01:41:44');
INSERT INTO `sys_role_permission` VALUES (1, 41, '2026-07-04 01:41:44', '2026-07-04 01:41:44');
INSERT INTO `sys_role_permission` VALUES (1, 93, '2026-07-04 01:41:44', '2026-07-04 01:41:44');
INSERT INTO `sys_role_permission` VALUES (1, 94, '2026-07-04 01:41:44', '2026-07-04 01:41:44');
INSERT INTO `sys_role_permission` VALUES (1, 95, '2026-07-04 01:41:44', '2026-07-04 01:41:44');
INSERT INTO `sys_role_permission` VALUES (1, 96, '2026-07-04 01:41:44', '2026-07-04 01:41:44');
INSERT INTO `sys_role_permission` VALUES (1, 97, '2026-07-04 01:41:44', '2026-07-04 01:41:44');
INSERT INTO `sys_role_permission` VALUES (1, 98, '2026-07-04 01:41:44', '2026-07-04 01:41:44');
INSERT INTO `sys_role_permission` VALUES (1, 99, '2026-07-04 01:41:44', '2026-07-04 01:41:44');
INSERT INTO `sys_role_permission` VALUES (1, 100, '2026-07-04 01:41:44', '2026-07-04 01:41:44');
INSERT INTO `sys_role_permission` VALUES (1, 101, '2026-07-04 01:41:44', '2026-07-04 01:41:44');
INSERT INTO `sys_role_permission` VALUES (1, 102, '2026-07-04 01:41:44', '2026-07-04 01:41:44');
INSERT INTO `sys_role_permission` VALUES (1, 103, '2026-07-04 01:41:44', '2026-07-04 01:41:44');
INSERT INTO `sys_role_permission` VALUES (1, 104, '2026-07-04 01:41:44', '2026-07-04 01:41:44');
INSERT INTO `sys_role_permission` VALUES (1, 105, '2026-07-04 01:41:44', '2026-07-04 01:41:44');
INSERT INTO `sys_role_permission` VALUES (1, 106, '2026-07-04 01:41:44', '2026-07-04 01:41:44');
INSERT INTO `sys_role_permission` VALUES (1, 107, '2026-07-04 01:41:44', '2026-07-04 01:41:44');
INSERT INTO `sys_role_permission` VALUES (1, 113, '2026-07-04 01:41:44', '2026-07-04 01:41:44');
INSERT INTO `sys_role_permission` VALUES (1, 114, '2026-07-04 01:41:44', '2026-07-04 01:41:44');
INSERT INTO `sys_role_permission` VALUES (1, 115, '2026-07-04 01:41:44', '2026-07-04 01:41:44');
INSERT INTO `sys_role_permission` VALUES (1, 116, '2026-07-04 01:41:44', '2026-07-04 01:41:44');
INSERT INTO `sys_role_permission` VALUES (1, 117, '2026-07-04 01:41:44', '2026-07-04 01:41:44');
INSERT INTO `sys_role_permission` VALUES (1, 118, '2026-07-04 01:41:44', '2026-07-04 01:41:44');
INSERT INTO `sys_role_permission` VALUES (1, 119, '2026-07-04 01:41:44', '2026-07-04 01:41:44');
INSERT INTO `sys_role_permission` VALUES (1, 120, '2026-07-04 01:41:44', '2026-07-04 01:41:44');
INSERT INTO `sys_role_permission` VALUES (1, 121, '2026-07-04 01:41:44', '2026-07-04 01:41:44');
INSERT INTO `sys_role_permission` VALUES (1, 122, '2026-07-04 01:41:44', '2026-07-04 01:41:44');
INSERT INTO `sys_role_permission` VALUES (1, 123, '2026-07-04 01:41:44', '2026-07-04 01:41:44');
INSERT INTO `sys_role_permission` VALUES (1, 124, '2026-07-04 01:41:44', '2026-07-04 01:41:44');
INSERT INTO `sys_role_permission` VALUES (1, 125, '2026-07-04 01:41:44', '2026-07-04 01:41:44');
INSERT INTO `sys_role_permission` VALUES (1, 126, '2026-07-04 01:41:44', '2026-07-04 01:41:44');
INSERT INTO `sys_role_permission` VALUES (1, 127, '2026-07-04 01:41:44', '2026-07-04 01:41:44');
INSERT INTO `sys_role_permission` VALUES (1, 128, '2026-07-04 01:41:44', '2026-07-04 01:41:44');
INSERT INTO `sys_role_permission` VALUES (1, 129, '2026-07-04 01:41:44', '2026-07-04 01:41:44');
INSERT INTO `sys_role_permission` VALUES (1, 130, '2026-07-04 01:41:44', '2026-07-04 01:41:44');
INSERT INTO `sys_role_permission` VALUES (1, 131, '2026-07-04 01:41:44', '2026-07-04 01:41:44');
INSERT INTO `sys_role_permission` VALUES (1, 132, '2026-07-04 01:41:44', '2026-07-04 01:41:44');
INSERT INTO `sys_role_permission` VALUES (1, 133, '2026-07-04 01:41:44', '2026-07-04 01:41:44');
INSERT INTO `sys_role_permission` VALUES (1, 134, '2026-07-04 01:41:44', '2026-07-04 01:41:44');
INSERT INTO `sys_role_permission` VALUES (1, 135, '2026-07-04 01:41:44', '2026-07-04 01:41:44');
INSERT INTO `sys_role_permission` VALUES (1, 136, '2026-07-04 01:41:44', '2026-07-04 01:41:44');
INSERT INTO `sys_role_permission` VALUES (1, 137, '2026-07-04 01:41:44', '2026-07-04 01:41:44');
INSERT INTO `sys_role_permission` VALUES (1, 138, '2026-07-04 01:41:44', '2026-07-04 01:41:44');
INSERT INTO `sys_role_permission` VALUES (1, 139, '2026-07-04 01:41:44', '2026-07-04 01:41:44');
INSERT INTO `sys_role_permission` VALUES (1, 140, '2026-07-04 01:41:44', '2026-07-04 01:41:44');
INSERT INTO `sys_role_permission` VALUES (1, 141, '2026-07-04 01:41:44', '2026-07-04 01:41:44');
INSERT INTO `sys_role_permission` VALUES (1, 142, '2026-07-04 01:41:44', '2026-07-04 01:41:44');
INSERT INTO `sys_role_permission` VALUES (1, 143, '2026-07-04 01:41:44', '2026-07-04 01:41:44');
INSERT INTO `sys_role_permission` VALUES (1, 144, '2026-07-04 01:41:44', '2026-07-04 01:41:44');
INSERT INTO `sys_role_permission` VALUES (1, 145, '2026-07-04 01:41:44', '2026-07-04 01:41:44');
INSERT INTO `sys_role_permission` VALUES (1, 146, '2026-07-04 01:41:44', '2026-07-04 01:41:44');
INSERT INTO `sys_role_permission` VALUES (1, 147, '2026-07-04 01:41:44', '2026-07-04 01:41:44');
INSERT INTO `sys_role_permission` VALUES (1, 148, '2026-07-04 01:41:44', '2026-07-04 01:41:44');
INSERT INTO `sys_role_permission` VALUES (2, 1, '2026-06-19 00:49:48', '2026-06-19 00:49:48');
INSERT INTO `sys_role_permission` VALUES (2, 5, '2026-06-19 00:49:48', '2026-06-19 00:49:48');
INSERT INTO `sys_role_permission` VALUES (2, 6, '2026-06-19 00:49:48', '2026-06-19 00:49:48');
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
INSERT INTO `sys_role_permission` VALUES (2, 41, '2026-06-19 00:49:48', '2026-06-19 00:49:48');
INSERT INTO `sys_role_permission` VALUES (2, 97, '2026-06-19 00:49:48', '2026-06-19 00:49:48');
INSERT INTO `sys_role_permission` VALUES (2, 102, '2026-06-19 00:49:48', '2026-06-19 00:49:48');
INSERT INTO `sys_role_permission` VALUES (2, 107, '2026-06-19 00:49:48', '2026-06-19 00:49:48');
INSERT INTO `sys_role_permission` VALUES (2, 113, '2026-06-19 00:49:48', '2026-06-19 00:49:48');
INSERT INTO `sys_role_permission` VALUES (2, 115, '2026-06-19 00:49:48', '2026-06-19 00:49:48');
INSERT INTO `sys_role_permission` VALUES (2, 116, '2026-06-19 00:49:48', '2026-06-19 00:49:48');
INSERT INTO `sys_role_permission` VALUES (2, 117, '2026-06-19 00:49:48', '2026-06-19 00:49:48');
INSERT INTO `sys_role_permission` VALUES (2, 118, '2026-06-19 00:49:48', '2026-06-19 00:49:48');
INSERT INTO `sys_role_permission` VALUES (2, 119, '2026-06-19 00:49:48', '2026-06-19 00:49:48');
INSERT INTO `sys_role_permission` VALUES (2, 120, '2026-06-19 00:49:48', '2026-06-19 00:49:48');
INSERT INTO `sys_role_permission` VALUES (2, 121, '2026-06-19 00:49:48', '2026-06-19 00:49:48');
INSERT INTO `sys_role_permission` VALUES (2, 122, '2026-06-19 00:49:48', '2026-06-19 00:49:48');
INSERT INTO `sys_role_permission` VALUES (2, 123, '2026-06-19 00:49:48', '2026-06-19 00:49:48');
INSERT INTO `sys_role_permission` VALUES (2, 124, '2026-06-19 00:49:48', '2026-06-19 00:49:48');
INSERT INTO `sys_role_permission` VALUES (2, 125, '2026-06-19 00:49:48', '2026-06-19 00:49:48');
INSERT INTO `sys_role_permission` VALUES (2, 126, '2026-06-19 00:49:48', '2026-06-19 00:49:48');
INSERT INTO `sys_role_permission` VALUES (2, 127, '2026-06-19 00:49:48', '2026-06-19 00:49:48');
INSERT INTO `sys_role_permission` VALUES (2, 128, '2026-06-19 00:49:48', '2026-06-19 00:49:48');
INSERT INTO `sys_role_permission` VALUES (2, 129, '2026-06-19 00:49:48', '2026-06-19 00:49:48');
INSERT INTO `sys_role_permission` VALUES (2, 130, '2026-06-19 00:49:48', '2026-06-19 00:49:48');
INSERT INTO `sys_role_permission` VALUES (2, 131, '2026-06-19 00:49:48', '2026-06-19 00:49:48');
INSERT INTO `sys_role_permission` VALUES (2, 132, '2026-06-19 00:49:48', '2026-06-19 00:49:48');
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
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_user
-- ----------------------------
INSERT INTO `sys_user` VALUES (1, 1, 'admin', '123456', '管理员', 'admin@example.com', NULL, 1, '2026-04-14 01:02:28', '2026-04-18 19:00:25');
INSERT INTO `sys_user` VALUES (2, 2, 'usr', '123456', '张三', 'zhang@example.com', NULL, 1, '2026-04-14 01:02:28', '2026-04-19 04:17:46');
INSERT INTO `sys_user` VALUES (3, 2, 'tut', '123456', '李四', 'li@example.com', NULL, 1, '2026-04-14 01:02:28', '2026-04-19 04:17:53');

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

SET FOREIGN_KEY_CHECKS = 1;
