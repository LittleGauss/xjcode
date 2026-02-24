/*
 Navicat Premium Dump SQL

 Source Server         : xinjiang
 Source Server Type    : MySQL
 Source Server Version : 80044 (8.0.44)
 Source Host           : localhost:3306
 Source Schema         : oa_service

 Target Server Type    : MySQL
 Target Server Version : 80044 (8.0.44)
 File Encoding         : 65001

 Date: 07/01/2026 21:09:56
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for oa_consumable_scrap_item
-- ----------------------------
DROP TABLE IF EXISTS `oa_consumable_scrap_item`;
CREATE TABLE `oa_consumable_scrap_item`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `scrap_id` int NOT NULL COMMENT '报废单ID',
  `goods_id` int NOT NULL COMMENT '物品ID',
  `goods_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '物品名称',
  `category_id` int NULL DEFAULT NULL COMMENT '分类ID',
  `category_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '分类名称',
  `quantity` int NOT NULL COMMENT '报废数量',
  `unit_price` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '单价',
  `total_price` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '总价',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_scrap_id`(`scrap_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 15 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '低值易耗品报废单明细表' ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
