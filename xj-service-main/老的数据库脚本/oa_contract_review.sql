/*
 Navicat Premium Dump SQL

 Source Server         : oa_service
 Source Server Type    : MySQL
 Source Server Version : 80030 (8.0.30)
 Source Host           : localhost:3306
 Source Schema         : oa_service

 Target Server Type    : MySQL
 Target Server Version : 80030 (8.0.30)
 File Encoding         : 65001

 Date: 20/11/2025 23:00:57
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for oa_contract_review
-- ----------------------------
DROP TABLE IF EXISTS `oa_contract_review`;
CREATE TABLE `oa_contract_review`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `contract_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '合同名称',
  `applicant_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '申请人姓名',
  `department` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '申请人所属部门',
  `contract_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '合同类型',
  `submission_date` date NULL DEFAULT NULL COMMENT '提交日期',
  `status` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '审核状态',
  `review_comments` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '审核意见',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `process_instance_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '流程实例ID',
  `current_approver` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '当前审批人',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '合同审核管理表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of oa_contract_review
-- ----------------------------
INSERT INTO `oa_contract_review` (`id`, `contract_name`, `applicant_name`, `department`, `contract_type`, `submission_date`, `status`, `review_comments`, `created_at`, `current_approver`, `process_instance_id`) VALUES (1, '1', 'super_admin', '技术研发部', NULL, '2025-11-19', '待审批', '', '2025-11-19 11:00:44', NULL, NULL);
INSERT INTO `oa_contract_review` (`id`, `contract_name`, `applicant_name`, `department`, `contract_type`, `submission_date`, `status`, `review_comments`, `created_at`, `current_approver`, `process_instance_id`) VALUES (2, '2', 'super_admin', '行政办公室', NULL, '2025-11-19', '待审批', '', '2025-11-19 11:05:28', NULL, NULL);
INSERT INTO `oa_contract_review` (`id`, `contract_name`, `applicant_name`, `department`, `contract_type`, `submission_date`, `status`, `review_comments`, `created_at`, `current_approver`, `process_instance_id`) VALUES (3, '1', 'super_admin', '技术研发部', NULL, '2025-11-19', '待审批', '', '2025-11-19 11:51:44', NULL, NULL);
INSERT INTO `oa_contract_review` (`id`, `contract_name`, `applicant_name`, `department`, `contract_type`, `submission_date`, `status`, `review_comments`, `created_at`, `current_approver`, `process_instance_id`) VALUES (4, '3', 'super_admin', '技术研发部', NULL, '2025-11-19', '待审批', '', '2025-11-19 12:10:36', NULL, NULL);
INSERT INTO `oa_contract_review` (`id`, `contract_name`, `applicant_name`, `department`, `contract_type`, `submission_date`, `status`, `review_comments`, `created_at`, `current_approver`, `process_instance_id`) VALUES (5, '1', 'super_admin', '行政办公室', NULL, '2025-11-19', '待审批', '', '2025-11-19 12:20:48', NULL, NULL);
INSERT INTO `oa_contract_review` (`id`, `contract_name`, `applicant_name`, `department`, `contract_type`, `submission_date`, `status`, `review_comments`, `created_at`, `current_approver`, `process_instance_id`) VALUES (6, '3', 'super_admin', '技术研发部', NULL, '2025-11-19', '待审批', '', '2025-11-19 20:38:18', NULL, NULL);
INSERT INTO `oa_contract_review` (`id`, `contract_name`, `applicant_name`, `department`, `contract_type`, `submission_date`, `status`, `review_comments`, `created_at`, `current_approver`, `process_instance_id`) VALUES (7, '4', 'super_admin', '技术研发部', '', '2025-11-19', '待审批', '', '2025-11-19 20:45:40', NULL, NULL);

