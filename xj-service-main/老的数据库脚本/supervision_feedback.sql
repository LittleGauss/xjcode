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

 Date: 24/11/2025 16:51:02
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for oa_supervision_feedback
-- ----------------------------
DROP TABLE IF EXISTS `oa_supervision_feedback`;
CREATE TABLE `oa_supervision_feedback`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT,
  `task_id` bigint(0) NOT NULL,
  `assignment_id` bigint(0) NULL DEFAULT NULL,
  `feedback_by` bigint(0) NULL DEFAULT NULL,
  `feedback_at` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0),
  `finish_date` date NULL DEFAULT NULL,
  `remarks` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_feedback_task_id`(`task_id`) USING BTREE,
  INDEX `idx_feedback_assignment_id`(`assignment_id`) USING BTREE,
  CONSTRAINT `fk_feedback_assignment` FOREIGN KEY (`assignment_id`) REFERENCES `oa_supervision_assignment` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_feedback_task` FOREIGN KEY (`task_id`) REFERENCES `oa_supervision_task` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of oa_supervision_feedback
-- ----------------------------
INSERT INTO `oa_supervision_feedback` VALUES (30, 41, NULL, 19, '2025-11-24 13:55:17', '2025-11-23', '完成');
INSERT INTO `oa_supervision_feedback` VALUES (31, 41, NULL, 19, '2025-11-24 13:55:41', NULL, '不可行');
INSERT INTO `oa_supervision_feedback` VALUES (32, 41, NULL, 19, '2025-11-24 14:02:11', '2025-11-23', '修复好了');
INSERT INTO `oa_supervision_feedback` VALUES (33, 41, NULL, 19, '2025-11-24 14:02:32', NULL, 'ok');
INSERT INTO `oa_supervision_feedback` VALUES (35, 43, NULL, 19, '2025-11-24 16:04:33', '2025-11-29', '完成');

SET FOREIGN_KEY_CHECKS = 1;
