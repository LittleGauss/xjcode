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

 Date: 18/11/2025 20:32:44
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for oa_administrative_notice_read
-- ----------------------------
DROP TABLE IF EXISTS `oa_administrative_notice_read`;
CREATE TABLE `oa_administrative_notice_read`  (
  `id` int(0) NOT NULL AUTO_INCREMENT,
  `notice_id` int(0) NOT NULL,
  `user_id` int(0) NOT NULL,
  `read_at` datetime(0) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uq_notice_user`(`notice_id`, `user_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of oa_administrative_notice_read
-- ----------------------------
INSERT INTO `oa_administrative_notice_read` VALUES (1, 6, 19, '2025-11-17 10:14:07');
INSERT INTO `oa_administrative_notice_read` VALUES (2, 6, 26, '2025-11-17 10:14:34');
INSERT INTO `oa_administrative_notice_read` VALUES (3, 6, 25, '2025-11-17 10:26:22');
INSERT INTO `oa_administrative_notice_read` VALUES (4, 6, 24, '2025-11-17 10:26:48');
INSERT INTO `oa_administrative_notice_read` VALUES (5, 6, 30, '2025-11-17 11:31:11');
INSERT INTO `oa_administrative_notice_read` VALUES (6, 7, 30, '2025-11-17 11:32:05');
INSERT INTO `oa_administrative_notice_read` VALUES (7, 12, 19, '2025-11-17 14:51:52');
INSERT INTO `oa_administrative_notice_read` VALUES (8, 13, 19, '2025-11-17 15:01:24');
INSERT INTO `oa_administrative_notice_read` VALUES (9, 14, 19, '2025-11-17 15:03:32');
INSERT INTO `oa_administrative_notice_read` VALUES (10, 8, 19, '2025-11-17 16:24:02');

SET FOREIGN_KEY_CHECKS = 1;
