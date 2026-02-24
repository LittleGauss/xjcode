-- 精简版分类表：去掉时间字段，仅保留核心业务字段
CREATE TABLE `oa_consumable_category` (
                                          `id` int(0) NOT NULL AUTO_INCREMENT COMMENT '分类主键ID',
                                          `category_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '分类名称（唯一）',
                                          `sort` int(0) NULL DEFAULT 0 COMMENT '排序序号（前端下拉框显示顺序）',
                                          PRIMARY KEY (`id`) USING BTREE,
                                          UNIQUE KEY `uk_category_name` (`category_name`) COMMENT '避免重复创建分类'
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '低值易耗品分类表';

-- 插入初始分类数据（不变）
INSERT INTO `oa_consumable_category` (`category_name`, `sort`)
VALUES
    ('办公用品', 1),
    ('电子耗材', 2),
    ('实验耗材', 3),
    ('清洁用品', 4),
    ('其他物资', 5);