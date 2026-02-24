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

 Date: 10/11/2025 16:29:50
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for oa_vehicle_management
-- ----------------------------
DROP TABLE IF EXISTS `oa_vehicle_management`;
CREATE TABLE `oa_vehicle_management`  (
  `id` int(0) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `plate_number` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '车牌号',
  `brand` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '品牌',
  `model` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '型号',
  `department` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '所属部门',
  `mileage` int(0) NULL DEFAULT NULL COMMENT '总里程（公里）',
  `fuel_cost` decimal(10, 2) NULL DEFAULT NULL COMMENT '油耗费用（元）',
  `repair_cost` decimal(10, 2) NULL DEFAULT NULL COMMENT '维修费用（元）',
  `insurance_status` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '保险状态',
  `created_at` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `displacement` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '排量',
  `fuel_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '燃油类型',
  `vehicle_status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '正常' COMMENT '车辆状态',
  `purchase_date` date NULL DEFAULT NULL COMMENT '购置日期',
  `remark` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '备注',
  `updated_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `deleted_flag` tinyint(1) NOT NULL DEFAULT 0 COMMENT '软删除标识',
  `version` int(0) NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uq_vehicle_plate_number`(`plate_number`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '公车管理表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of oa_vehicle_management
-- ----------------------------
INSERT INTO `oa_vehicle_management` VALUES (1, '晋C11111', '本田', NULL, NULL, NULL, NULL, NULL, NULL, '2025-11-09 19:22:49', NULL, NULL, '正常', NULL, NULL, '2025-11-09 19:34:37', 0, 0);

SET FOREIGN_KEY_CHECKS = 1;
