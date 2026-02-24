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

 Date: 18/11/2025 20:32:54
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for dispatch_attachment
-- ----------------------------
DROP TABLE IF EXISTS `oa_dispatch_attachment`;
CREATE TABLE `oa_dispatch_attachment`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT,
  `dispatch_id` bigint(0) NOT NULL,
  `upload_id` bigint(0) NOT NULL,
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `sort_order` int(0) NULL DEFAULT 0,
  `created_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0),
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_da_dispatch`(`dispatch_id`) USING BTREE,
  INDEX `idx_da_upload`(`upload_id`) USING BTREE,
  CONSTRAINT `fk_da_dispatch` FOREIGN KEY (`dispatch_id`) REFERENCES `oa_vehicle_dispatch` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_da_upload` FOREIGN KEY (`upload_id`) REFERENCES `uploads` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of dispatch_attachment
-- ----------------------------
INSERT INTO `oa_dispatch_attachment` VALUES (1, 38, 1, '0226.png_860.png', 1, '2025-11-13 22:50:37');

SET FOREIGN_KEY_CHECKS = 1;
