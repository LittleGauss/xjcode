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

 Date: 10/11/2025 19:49:49
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for oa_vehicle_dispatch
-- ----------------------------
DROP TABLE IF EXISTS `oa_vehicle_dispatch`;
CREATE TABLE `oa_vehicle_dispatch`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT,
  `dispatch_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `vehicle_id` int(0) NOT NULL,
  `driver_id` bigint(0) NULL DEFAULT NULL,
  `requester_id` bigint(0) NULL DEFAULT NULL,
  `department` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `purpose` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `start_time` datetime(0) NULL DEFAULT NULL,
  `end_time` datetime(0) NULL DEFAULT NULL,
  `start_location` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `end_location` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `start_mileage` bigint(0) NULL DEFAULT NULL,
  `end_mileage` bigint(0) NULL DEFAULT NULL,
  `estimated_mileage` bigint(0) NULL DEFAULT NULL,
  `actual_mileage` bigint(0) NULL DEFAULT NULL,
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'draft',
  `approve_by` bigint(0) NULL DEFAULT NULL,
  `approve_time` datetime(0) NULL DEFAULT NULL,
  `remarks` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
  `created_by` bigint(0) NULL DEFAULT NULL,
  `created_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0),
  `updated_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0),
  `fuel_cost` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '本次燃油费',
  `toll_fee` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '过路费',
  `parking_fee` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '停车费',
  `trip_mileage` bigint(0) NULL DEFAULT NULL COMMENT '本次行驶里程',
  `requester_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '申请人名称(冗余)',
  `plate_number` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '冗余车牌号用于展示/查询',
  `deleted_flag` tinyint(1) NOT NULL DEFAULT 0 COMMENT '软删除标识',
  `version` int(0) NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
  `dispatch_number` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '派单编号',
  `brand` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '车辆品牌(冗余)',
  `model` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '车型(冗余)',
  `use_date` date NULL DEFAULT NULL COMMENT '使用日期',
  `departure_time` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '出发时间',
  `return_time` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '返回时间',
  `repair_cost` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '维修费',
  `remark` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '备注',
  `attachments` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '附件元数据(JSON)',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `dispatch_no`(`dispatch_no`) USING BTREE,
  INDEX `idx_dispatch_vehicle`(`vehicle_id`) USING BTREE,
  INDEX `idx_dispatch_driver`(`driver_id`) USING BTREE,
  INDEX `idx_dispatch_status`(`status`) USING BTREE,
  INDEX `idx_dispatch_start_time`(`start_time`) USING BTREE,
  INDEX `idx_dispatch_end_time`(`end_time`) USING BTREE,
  INDEX `idx_dispatch_plate_number`(`plate_number`) USING BTREE,
  INDEX `idx_oa_vehicle_dispatch_dispatch_number`(`dispatch_number`) USING BTREE,
  CONSTRAINT `fk_dispatch_vehicle` FOREIGN KEY (`vehicle_id`) REFERENCES `oa_vehicle_management` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 30 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of oa_vehicle_dispatch
-- ----------------------------
INSERT INTO `oa_vehicle_dispatch` VALUES (20, 'PD1762742790333', 1, NULL, NULL, '', NULL, NULL, NULL, NULL, NULL, 0, 0, NULL, NULL, 'draft', NULL, NULL, NULL, NULL, '2025-11-10 10:46:30', '2025-11-10 10:46:30', 0.00, 0.00, 0.00, 0, NULL, '晋C11111', 0, 0, NULL, '本田', '', '2025-11-10', '10:51', '', 0.00, '', '[]');
INSERT INTO `oa_vehicle_dispatch` VALUES (22, 'PD1762746026872', 1, NULL, NULL, '', NULL, NULL, NULL, NULL, NULL, 0, 0, NULL, NULL, 'draft', NULL, NULL, NULL, NULL, '2025-11-10 11:40:26', '2025-11-10 11:40:26', 0.00, 0.00, 0.00, 0, NULL, '晋C11111', 0, 0, NULL, '本田', '', '2025-11-10', '', '', 0.00, '', '[]');
INSERT INTO `oa_vehicle_dispatch` VALUES (23, 'PD1762746078869', 1, NULL, NULL, '', NULL, NULL, NULL, NULL, NULL, 0, 0, NULL, NULL, 'draft', NULL, NULL, NULL, NULL, '2025-11-10 11:41:18', '2025-11-10 11:41:18', 0.00, 0.00, 0.00, 0, NULL, '晋C11111', 0, 0, NULL, '本田', '', '2025-11-10', '', '', 0.00, '', '[]');
INSERT INTO `oa_vehicle_dispatch` VALUES (24, 'PD1762746133733', 1, NULL, NULL, '', NULL, NULL, NULL, NULL, NULL, 0, 0, NULL, NULL, 'draft', NULL, NULL, NULL, NULL, '2025-11-10 11:42:13', '2025-11-10 11:42:13', 0.00, 0.00, 0.00, 0, NULL, '晋C11111', 0, 0, NULL, '本田', '', '2025-11-10', '', '', 0.00, '', '[]');
INSERT INTO `oa_vehicle_dispatch` VALUES (25, 'PD1762747859412', 1, NULL, NULL, '', NULL, NULL, NULL, NULL, NULL, 0, 0, NULL, NULL, 'draft', NULL, NULL, NULL, NULL, '2025-11-10 12:10:59', '2025-11-10 12:10:59', 0.00, 0.00, 0.00, 0, NULL, '晋C11111', 0, 0, NULL, '本田', '', '2025-11-10', '', '', 0.00, '', '[]');
INSERT INTO `oa_vehicle_dispatch` VALUES (26, 'PD1762758606977', 1, NULL, NULL, '', NULL, NULL, NULL, NULL, NULL, 0, 0, NULL, NULL, 'draft', NULL, NULL, NULL, NULL, '2025-11-10 15:10:07', '2025-11-10 15:10:07', 0.00, 0.00, 0.00, 0, NULL, '晋C11111', 0, 0, NULL, '本田', '', '2025-11-30', '', '', 0.00, '', '[]');
INSERT INTO `oa_vehicle_dispatch` VALUES (27, 'PD1762762110078', 1, NULL, NULL, '', NULL, NULL, NULL, NULL, NULL, 0, 0, NULL, NULL, 'draft', NULL, NULL, NULL, NULL, '2025-11-10 16:08:30', '2025-11-10 16:08:30', 0.00, 0.00, 0.00, 0, NULL, '晋C11111', 0, 0, NULL, '本田', '', '2025-11-10', '', '', 0.00, '', '[]');
INSERT INTO `oa_vehicle_dispatch` VALUES (28, 'PD1762768437530', 1, NULL, NULL, '', NULL, NULL, NULL, NULL, NULL, 0, 0, NULL, NULL, 'draft', NULL, NULL, NULL, NULL, '2025-11-10 17:53:57', '2025-11-10 17:53:57', 0.00, 0.00, 0.00, 0, NULL, '晋C11111', 0, 0, NULL, '本田', '', '2025-11-10', '', '', 0.00, '', '[]');
INSERT INTO `oa_vehicle_dispatch` VALUES (29, 'PD1762768698890', 1, NULL, NULL, '', NULL, NULL, NULL, NULL, NULL, 0, 0, NULL, NULL, 'draft', NULL, NULL, NULL, NULL, '2025-11-10 17:58:18', '2025-11-10 17:58:18', 0.00, 0.00, 0.00, 0, NULL, '晋C11111', 0, 0, NULL, '本田', '', '2025-11-10', '', '', 0.00, '', '[]');

SET FOREIGN_KEY_CHECKS = 1;
