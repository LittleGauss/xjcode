use oa_service;

DROP TABLE IF EXISTS `oa_consumable_in_out_record`;
CREATE TABLE `oa_consumable_in_out_record` (
                                               `id` int NOT NULL AUTO_INCREMENT COMMENT '记录ID',
                                               `goods_id` int NOT NULL COMMENT '物品ID，关联oa_consumable_goods.id',
                                               `item_name` varchar(100) NOT NULL COMMENT '物品名称（冗余存储，便于查询）',
                                               `category_id` int DEFAULT NULL COMMENT '分类ID',
                                               `category_name` varchar(50) DEFAULT NULL COMMENT '分类名称',
                                               `operation_type` ENUM('IN', 'OUT') NOT NULL COMMENT '操作类型：IN-入库，OUT-出库',
                                               `quantity_change` int NOT NULL COMMENT '数量变化（正数表示增加，负数表示减少）',
                                               `quantity_before` int NOT NULL COMMENT '操作前数量',
                                               `quantity_after` int NOT NULL COMMENT '操作后数量',
                                               `operator_id` int NOT NULL COMMENT '操作人ID',
                                               `operator_name` varchar(50) NOT NULL COMMENT '操作人姓名',
                                               `operator_dept` varchar(50) DEFAULT NULL COMMENT '操作人部门',
                                               `operation_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
                                               `supplier` varchar(100) DEFAULT NULL COMMENT '供货商（入库时记录）',
                                               `unit_price` decimal(10,2) DEFAULT NULL COMMENT '单价',
                                               `total_price` decimal(10,2) DEFAULT NULL COMMENT '总价',
                                               `remark` varchar(500) DEFAULT NULL COMMENT '备注',
                                               PRIMARY KEY (`id`),
                                               KEY `idx_goods_id` (`goods_id`),
                                               KEY `idx_operation_time` (`operation_time`),
                                               KEY `idx_operation_type` (`operation_type`),
                                               KEY `idx_operator_id` (`operator_id`),
                                               KEY `idx_supplier` (`supplier`),
                                               CONSTRAINT `fk_record_goods` FOREIGN KEY (`goods_id`) REFERENCES `oa_consumable_goods` (`id`) ON DELETE CASCADE,
                                               CONSTRAINT `chk_operation_type` CHECK (`operation_type` IN ('IN', 'OUT')),
                                               CONSTRAINT `chk_quantity_change` CHECK (
                                                   (`operation_type` = 'IN' AND `quantity_change` > 0) OR
                                                   (`operation_type` = 'OUT' AND `quantity_change` < 0)
                                                   )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='耗材出入库记录表';


LOCK TABLES `sys_permission` WRITE;


INSERT INTO `sys_permission` (
    `id`,
    `code`,
    `name`,
    `description`,
    `resource_type`,
    `resource_path`,
    `created_at`,
    `updated_at`
)
VALUES (
           50,
           'SUPPLY:IT_ITEM',
           '新增电子物品',
           '新增电子物品和危化品权限（仅IT部）',
           'BUTTON',
           NULL,
           '2026-01-03 16:57:30',
           '2026-01-03 16:57:30'
       );

UNLOCK TABLES;