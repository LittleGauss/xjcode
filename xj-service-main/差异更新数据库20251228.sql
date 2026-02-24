use oa_service;

DROP TABLE IF EXISTS `oa_sign_record`;
CREATE TABLE `oa_sign_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '签名 ID（主键）自增',
  `user_id` bigint NOT NULL COMMENT '签名人 ID 关联sys_user.id',
  `sign_url` varchar(500) NOT NULL COMMENT '签名图片存储路径 MinIO路径',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '签名时间',
  `ext1` varchar(255) DEFAULT NULL COMMENT '扩展字段 1 可存储签名备注',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=24 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='电子签名记录表';
-- 添加新字段 supplier
ALTER TABLE `oa_consumable_application` 
ADD COLUMN `supplier` varchar(100) DEFAULT NULL COMMENT '供货商' AFTER `item_name`;

-- 添加 supplier 字段的索引
ALTER TABLE `oa_consumable_application` 
ADD INDEX `idx_supplier` (`supplier`);

-- 添加 apply_time 字段的索引
ALTER TABLE `oa_consumable_application` 
ADD INDEX `idx_apply_time` (`apply_time`);

-- 添加 supplier 字段
ALTER TABLE `oa_consumable_goods` 
ADD COLUMN `supplier` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '供货商' AFTER `brand`;


-- 添加派车记录的保险费与年审费字段（幂等）
ALTER TABLE `oa_service`.`oa_vehicle_dispatch`
ADD COLUMN `insurance_fee` DECIMAL(10,2) NULL DEFAULT 0.00 COMMENT '保险费' AFTER `parking_fee`;

ALTER TABLE `oa_service`.`oa_vehicle_dispatch`
ADD COLUMN `annual_inspection_fee` DECIMAL(10,2) NULL DEFAULT 0.00 COMMENT '年审费' AFTER `insurance_fee`;

LOCK TABLES `oa_consumable_category` WRITE;
-- 更新id=5（存在则更新，无则不操作）
UPDATE `oa_consumable_category` 
SET `category_name` = '危化品' 
WHERE `id` = 5;

-- 插入id=6（不存在则插入，存在则忽略）
INSERT IGNORE INTO `oa_consumable_category` (`id`, `category_name`, `sort`)
VALUES (6, '其他物资', 6);
UNLOCK TABLES;
