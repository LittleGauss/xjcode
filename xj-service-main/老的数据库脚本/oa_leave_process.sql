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

 Date: 01/12/2025 21:42:50
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for oa_leave_process
-- ----------------------------
DROP TABLE IF EXISTS `oa_leave_process`;
CREATE TABLE `oa_leave_process`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` int NULL DEFAULT NULL COMMENT '用户ID',
  `process_instance_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '流程实例ID',
  `leave_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '请假类型',
  `start_date` datetime NULL DEFAULT NULL COMMENT '开始时间',
  `end_date` datetime NULL DEFAULT NULL COMMENT '结束时间',
  `reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '请假原因',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'PENDING' COMMENT '状态(PENDING/APPROVED/REJECTED)',
  `current_approver` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '当前审批人',
  `created_at` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `updated_at` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `duration` double NULL DEFAULT NULL COMMENT '请假时长(天)',
  `department_id` int NULL DEFAULT NULL COMMENT '申请人所属部门ID',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1982738976936640540 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '请假流程表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of oa_leave_process
-- ----------------------------
INSERT INTO `oa_leave_process` VALUES (1982738976936640535, 19, '4c77cf56-cdcf-11f0-9b16-ca6e08f19d57', 'sick', '2025-11-28 00:00:00', '2025-11-30 00:00:00', '222', 'SUBMITTED', '21', '2025-11-30 17:31:07', '2025-11-30 17:31:08', 2, NULL);
INSERT INTO `oa_leave_process` VALUES (1982738976936640536, 19, 'ec6c8050-ce7e-11f0-9210-ca6e08f19d57', 'annual', '2025-12-01 00:00:00', '2025-12-03 00:00:00', '11', 'SUBMITTED', '22', '2025-12-01 14:28:17', '2025-12-01 14:28:18', 2, NULL);
INSERT INTO `oa_leave_process` VALUES (1982738976936640537, 19, 'c4b1cbf0-ce7f-11f0-a393-ca6e08f19d57', 'annual', '2025-12-09 00:00:00', '2025-12-15 00:00:00', '11', 'SUBMITTED', '22', '2025-12-01 14:34:20', '2025-12-01 14:34:21', 6, NULL);
INSERT INTO `oa_leave_process` VALUES (1982738976936640538, 19, 'a74339b7-ce88-11f0-a318-ca6e08f19d57', 'annual', '2025-12-01 00:00:00', '2025-12-02 00:00:00', '11', 'SUBMITTED', '22', '2025-12-01 15:37:56', '2025-12-01 15:37:57', 1, NULL);
INSERT INTO `oa_leave_process` VALUES (1982738976936640539, 19, 'da0c83fb-ceae-11f0-8e97-ca6e08f19d57', 'sick', '2025-12-01 00:00:00', '2025-12-03 00:00:00', '11', 'SUBMITTED', '22', '2025-12-01 20:11:22', '2025-12-01 20:11:23', 2, 1);

SET FOREIGN_KEY_CHECKS = 1;
