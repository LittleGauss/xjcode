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

 Date: 24/11/2025 16:50:50
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for oa_supervision_task
-- ----------------------------
DROP TABLE IF EXISTS `oa_supervision_task`;
CREATE TABLE `oa_supervision_task`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT,
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
  `created_by` bigint(0) NULL DEFAULT NULL,
  `created_at` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0),
  `due_date` date NULL DEFAULT NULL,
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'open',
  `progress` int(0) NULL DEFAULT 0,
  `last_update` datetime(0) NULL DEFAULT NULL,
  `notice_id` bigint(0) NULL DEFAULT NULL,
  `process_instance_id` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_supervision_notice_id`(`notice_id`) USING BTREE,
  INDEX `idx_supervision_status`(`status`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 25 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of oa_supervision_task
-- ----------------------------
INSERT INTO `oa_supervision_task` VALUES (41, '任务1', '任务一内容', 19, '2025-11-24 13:54:41', '2025-11-28', 'completed', 100, '2025-11-24 14:02:32', NULL, '1170ce06-c8fa-11f0-b01c-7208943ae4a9');
INSERT INTO `oa_supervision_task` VALUES (42, '任务2', '任务二2内容', 19, '2025-11-24 14:21:37', '2025-11-27', 'open', 0, '2025-11-24 16:03:51', NULL, 'd4b0e84d-c8fd-11f0-b01c-7208943ae4a9');
INSERT INTO `oa_supervision_task` VALUES (43, '任务3', '任务3内容', 19, '2025-11-24 16:04:14', '2025-11-27', 'feedback', 80, '2025-11-24 16:04:33', NULL, '2b1a0005-c90c-11f0-bd70-7208943ae4a9');

SET FOREIGN_KEY_CHECKS = 1;
