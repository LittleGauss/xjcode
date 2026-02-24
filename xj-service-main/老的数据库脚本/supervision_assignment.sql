/*
 Navicat MySQL Data Transfer

 Source Server         : oa_service
 Source Server Type    : MySQL
 Source Server Version : 80034
 Source Host           : localhost:3306
 Source Schema         : oa_service

 Target Server Type    : MySQL
 Target Server Version : 80034
 File Encoding         : 65001

 Date: 24/11/2025 16:51:13
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for oa_supervision_assignment
-- ----------------------------
DROP TABLE IF EXISTS `oa_supervision_assignment`;
CREATE TABLE `oa_supervision_assignment`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT,
  `task_id` bigint(0) NOT NULL,
  `assignee_user_id` bigint(0) NULL DEFAULT NULL,
  `assignee_dept_id` bigint(0) NULL DEFAULT NULL,
  `assigned_by` bigint(0) NULL DEFAULT NULL,
  `assigned_at` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0),
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'pending',
  `completed_at` datetime(0) NULL DEFAULT NULL,
  `flowable_task_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'Flowable task id',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_assignment_task_id`(`task_id`) USING BTREE,
  INDEX `idx_assignment_user`(`assignee_user_id`) USING BTREE,
  INDEX `idx_assignment_flowable_task_id`(`flowable_task_id`) USING BTREE,
  CONSTRAINT `fk_assignment_task` FOREIGN KEY (`task_id`) REFERENCES `oa_supervision_task` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 24 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of oa_supervision_assignment
-- ----------------------------
INSERT INTO `oa_supervision_assignment` VALUES (40, 41, 19, NULL, NULL, '2025-11-24 13:54:41', 'completed', '2025-11-24 14:02:32', '11719161-c8fa-11f0-b01c-7208943ae4a9');
INSERT INTO `oa_supervision_assignment` VALUES (43, 43, 19, NULL, NULL, '2025-11-24 16:04:15', 'completed', '2025-11-24 16:04:33', '2b418540-c90c-11f0-bd70-7208943ae4a9');

SET FOREIGN_KEY_CHECKS = 1;
