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

 Date: 18/11/2025 20:32:28
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for uploads
-- ----------------------------
DROP TABLE IF EXISTS `uploads`;
CREATE TABLE `uploads`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `mime_type` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `size` bigint(0) NULL DEFAULT NULL,
  `storage_path` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `origin` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `origin_id` bigint(0) NULL DEFAULT NULL,
  `meta` json NULL,
  `created_by` bigint(0) NULL DEFAULT NULL,
  `created_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0),
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_origin`(`origin`, `origin_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of uploads
-- ----------------------------
INSERT INTO `uploads` VALUES (1, '0226.png_860.png', 'image/png', 237267, 'dispatch-attachments/dispatch/20251113/1763045436383_0226.png_860.png', 'dispatch', NULL, NULL, NULL, '2025-11-13 22:50:37');
INSERT INTO `uploads` VALUES (2, '2.数据库高级设计技术2025试卷.pdf', 'application/pdf', 682438, 'notice-attachments/notice/20251117/1763362436107_2._________2025__.pdf', 'notice', NULL, NULL, NULL, '2025-11-17 14:53:56');
INSERT INTO `uploads` VALUES (3, '2.数据库高级设计技术2025试卷.pdf', 'application/pdf', 682438, 'notice-attachments/notice/20251117/1763362879515_2._________2025__.pdf', 'notice', NULL, NULL, NULL, '2025-11-17 15:01:20');
INSERT INTO `uploads` VALUES (4, '2.数据库高级设计技术2025试卷.pdf', 'application/pdf', 682438, 'notice-attachments/notice/20251117/1763363008037_2._________2025__.pdf', 'notice', 14, NULL, NULL, '2025-11-17 15:03:28');
INSERT INTO `uploads` VALUES (5, '2.数据库高级设计技术2025试卷.pdf', 'application/pdf', 682438, 'notice-attachments/notice/20251117/1763363311372_2._________2025__.pdf', 'notice', 12, NULL, NULL, '2025-11-17 15:08:31');
INSERT INTO `uploads` VALUES (6, '2.数据库高级设计技术2025试卷.pdf', 'application/pdf', 682438, 'notice-attachments/notice/20251117/1763365412118_2._________2025__.pdf', 'notice', 6, NULL, NULL, '2025-11-17 15:43:32');
INSERT INTO `uploads` VALUES (7, '2.数据库高级设计技术2025试卷.pdf', 'application/pdf', 682438, 'notice-attachments/notice/20251117/1763365915975_2._________2025__.pdf', 'notice', 6, NULL, NULL, '2025-11-17 15:51:56');

SET FOREIGN_KEY_CHECKS = 1;
