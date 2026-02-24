CREATE TABLE `oa_consumable_goods` (
                                       `id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                       `item_name` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '物品名称',
                                       `category_id` int DEFAULT NULL COMMENT '分类ID（关联 oa_consumable_category 表主键）',
                                       `spec` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '型号规格',
                                       `quantity` int DEFAULT NULL COMMENT '数量',
                                       `unit` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '单位（如件、个）',
                                       `purchase_date` date DEFAULT NULL COMMENT '采购日期',
                                       `status` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '物品状态',
                                       `storage_location` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '存放位置',
                                       `brand` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '品牌',
                                       `unit_price` decimal(10,2) DEFAULT NULL COMMENT '单价',
                                       `expire_date` date DEFAULT NULL COMMENT '过期日期',
                                       `warning_value` int DEFAULT NULL COMMENT '库存预警值',
                                       `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                       PRIMARY KEY (`id`),
                                       KEY `idx_category_id` (`category_id`) COMMENT '分类ID索引，提升查询效率',
    -- 移除外键约束后的 COMMENT，兼容低版本 MySQL
                                       CONSTRAINT `fk_goods_category` FOREIGN KEY (`category_id`) REFERENCES `oa_consumable_category` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='低值易耗品管理表';

-- 插入物品数据（关联分类ID关联分类表，确保category_id在分类表中存在）
INSERT INTO `oa_consumable_goods` (
    `item_name`, `category_id`, `spec`, `quantity`, `unit`,
    `purchase_date`, `status`, `storage_location`, `brand`,
    `unit_price`, `expire_date`, `warning_value`
) VALUES
-- 办公用品（假设分类表中“办公用品”的ID=1）
('A4打印纸', 1, '70g/包，500张', 20, '包', '2025-10-15', '1', '仓库A区货架1', '得力', 16.80, '2026-10-15', 5),
('中性笔', 1, '0.5mm黑色，按动式', 50, '支', '2025-11-02', '1', '仓库A区货架2', '晨光', 2.50, '2026-11-02', 10),
('文件夹', 1, 'A4，塑料材质', 15, '个', '2025-09-20', '1', '仓库A区货架3', '齐心', 8.50, NULL, 3),

-- 电子设备（假设分类表中“电子耗材”的ID=2）
('无线鼠标', 2, '蓝牙5.0，续航30天', 8, '个', '2025-10-05', '1', '仓库B区货架1', '罗技', 129.00, NULL, 2),
('U盘', 2, '128GB，USB3.0', 10, '个', '2025-11-10', '1', '仓库B区货架2', '金士顿', 59.90, NULL, 2),

-- 劳保用品（假设分类表中“实验耗材”的ID=3）
('一次性口罩', 3, '医用防护，独立包装', 100, '个', '2025-11-01', '1', '仓库C区货架1', '3M', 3.20, '2026-05-01', 20),
('橡胶手套', 3, 'XL号，耐酸碱', 30, '双', '2025-10-25', '1', '仓库C区货架2', '妙洁', 4.50, '2026-04-25', 5),

-- 清洁用品（假设分类表中“清洁用品”的ID=4）
('消毒湿巾', 4, '100抽/包，杀菌99%', 12, '包', '2025-10-30', '1', '仓库C区货架3', '滴露', 19.90, '2026-04-30', 3),
('拖把', 4, '平板式，可拆洗', 5, '把', '2025-09-10', '1', '仓库D区工具架', '美丽雅', 39.90, NULL, 1);