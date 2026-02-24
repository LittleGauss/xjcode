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

 Date: 30/11/2025 17:42:37
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for oa_leave_attachment
-- ----------------------------
DROP TABLE IF EXISTS `oa_leave_attachment`;
CREATE TABLE `oa_leave_attachment`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `leave_id` bigint NULL DEFAULT NULL COMMENT '请假单ID',
  `file_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '文件名',
  `file_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '文件存储路径',
  `file_size` bigint NULL DEFAULT NULL COMMENT '文件大小(字节)',
  `file_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件类型',
  `upload_user_id` bigint NOT NULL COMMENT '上传人ID',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `leave_id`(`leave_id` ASC) USING BTREE,
  CONSTRAINT `oa_leave_attachment_ibfk_1` FOREIGN KEY (`leave_id`) REFERENCES `oa_leave_process` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of oa_leave_attachment
-- ----------------------------
INSERT INTO `oa_leave_attachment` VALUES (1, NULL, 'ITOM-2025-Aligning Cyber Space with Physical World a comprehensive survey on embodied AI(4)的全文翻译.pdf', 'leave-attachments/leave/20251130/1764493935023_ITOM-2025-Aligning_Cyber_Space_with_Physical_World_a_comprehensive_survey_on_embodied_AI_4______.pdf', 10066862, 'application/pdf', 19, '2025-11-30 17:12:16');
INSERT INTO `oa_leave_attachment` VALUES (2, 1982738976936640535, 'ITOM-2025-Aligning Cyber Space with Physical World a comprehensive survey on embodied AI(4)的全文翻译.pdf', 'leave-attachments/leave/20251130/1764495065589_ITOM-2025-Aligning_Cyber_Space_with_Physical_World_a_comprehensive_survey_on_embodied_AI_4______.pdf', 10066862, 'application/pdf', 19, '2025-11-30 17:31:06');

SET FOREIGN_KEY_CHECKS = 1;
