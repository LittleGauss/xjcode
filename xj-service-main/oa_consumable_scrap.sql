/*
 Navicat Premium Dump SQL

 Source Server         : xinjiang
 Source Server Type    : MySQL
 Source Server Version : 80044 (8.0.44)
 Source Host           : localhost:3306
 Source Schema         : oa_service

 Target Server Type    : MySQL
 Target Server Version : 80044 (8.0.44)
 File Encoding         : 65001

 Date: 11/01/2026 19:25:46
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for oa_consumable_scrap
-- ----------------------------
DROP TABLE IF EXISTS `oa_consumable_scrap`;
CREATE TABLE `oa_consumable_scrap`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `scrap_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '报废单号',
  `apply_user_id` int NOT NULL COMMENT '申请人ID',
  `apply_user_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '申请人姓名',
  `apply_user_nickname` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '申请人昵称',
  `apply_dept` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '申请人部门',
  `assignee_id` int NULL DEFAULT NULL COMMENT '待审核人ID（后保部）',
  `assignee_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '待审核人名称（后保部）',
  `apply_time` datetime NOT NULL COMMENT '申请时间',
  `reason` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '报废原因',
  `process_method` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '处理方式：RECYCLE-回收 / DESTROY-销毁',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '状态：DRAFT / REVIEW / APPROVED / REJECTED',
  `review_time` datetime NULL DEFAULT NULL COMMENT '后保部审核时间',
  `review_remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '后保部审核意见',
  `next_approver_id` int NULL DEFAULT NULL COMMENT '下一级审批人ID',
  `next_approver_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '下一级审批人姓名',
  `approve_time` datetime NULL DEFAULT NULL COMMENT '分管领导审批时间',
  `approve_remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '分管领导审批意见',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_scrap_no`(`scrap_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 24 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '低值易耗品报废单主表' ROW_FORMAT = DYNAMIC;

SET FOREIGN_KEY_CHECKS = 1;
