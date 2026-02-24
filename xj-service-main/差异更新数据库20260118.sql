use oa_service;
SET FOREIGN_KEY_CHECKS = 0;

-- 1. 删除原有约束
ALTER TABLE oa_consumable_in_out_record 
DROP CONSTRAINT chk_operation_type,
DROP CONSTRAINT chk_quantity_change;

-- 2. 修改 operation_type 字段类型为 VARCHAR
ALTER TABLE oa_consumable_in_out_record 
MODIFY COLUMN operation_type VARCHAR(20) NOT NULL COMMENT '操作类型：IN-入库，OUT-出库，SCRAP-报废';

-- 3. 重新添加检查约束（和原有规则完全一致）
ALTER TABLE oa_consumable_in_out_record 
ADD CONSTRAINT chk_operation_type CHECK (operation_type IN ('IN', 'OUT', 'SCRAP')),
ADD CONSTRAINT chk_quantity_change CHECK (
    (operation_type = 'IN' AND quantity_change > 0) OR
    (operation_type = 'OUT' AND quantity_change < 0) OR
    (operation_type = 'SCRAP' AND quantity_change < 0)
);

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

DROP TABLE IF EXISTS `oa_consumable_scrap`;
CREATE TABLE `oa_consumable_scrap`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `scrap_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '报废单号',
  `apply_user_id` int NOT NULL COMMENT '申请人ID',
  `apply_user_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '申请人姓名',
  `apply_dept` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '申请人部门',
  `assignee_id` int NULL DEFAULT NULL COMMENT '待审核人ID（后保部）',
  `assignee_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '待审核人名称（后保部）',
  `apply_time` datetime NOT NULL COMMENT '申请时间',
  `reason` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '报废原因',
  `process_method` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '处理方式：RECYCLE-回收 / DESTROY-销毁',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '状态：DRAFT / REVIEW / APPROVED / REJECTED',
  `review_time` datetime NULL DEFAULT NULL COMMENT '后保部审核时间',
  `review_remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '后保部审核意见',
  `next_approver_id` int NULL DEFAULT NULL COMMENT '下一级审批人ID',
  `next_approver_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '下一级审批人姓名',
  `approve_time` datetime NULL DEFAULT NULL COMMENT '分管领导审批时间',
  `approve_remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '分管领导审批意见',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_scrap_no`(`scrap_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 24 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '低值易耗品报废单主表' ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `oa_consumable_stock_in_approval`;
CREATE TABLE `oa_consumable_stock_in_approval` (
                                        `id` int NOT NULL AUTO_INCREMENT,
                                        `apply_no` varchar(50) NOT NULL COMMENT '入库单号',
                                        `applicant_id` int NOT NULL COMMENT '申请人ID',
                                        `applicant_name` varchar(100) DEFAULT NULL COMMENT '申请人姓名',
                                        `applicant_dept` varchar(100) DEFAULT NULL COMMENT '申请人部门',
                                        `remark` text COMMENT '入库说明',

                                        `status` varchar(20) NOT NULL DEFAULT 'PENDING' COMMENT '状态：PENDING, APPROVED, REJECTED',
                                        `approver_id` int NOT NULL COMMENT '审批人ID',
                                        `approver_name` varchar(100) DEFAULT NULL COMMENT '审批人姓名',
                                        `approve_remark` text COMMENT '审批意见',
                                        `approve_time` datetime DEFAULT NULL COMMENT '审批时间',
                                        `item_name` varchar(200) NOT NULL COMMENT '物品名称',
                                        `category_id` int DEFAULT NULL COMMENT '分类ID',
                                        `category_name` varchar(100) DEFAULT NULL COMMENT '分类名称',
                                        `spec` varchar(200) DEFAULT NULL COMMENT '型号规格',
                                        `quantity` int NOT NULL COMMENT '数量',
                                        `unit` varchar(20) DEFAULT '个' COMMENT '单位',
                                        `brand` varchar(100) DEFAULT NULL COMMENT '品牌',
                                        `supplier` varchar(200) DEFAULT NULL COMMENT '供货商',
                                        `unit_price` decimal(10,2) DEFAULT NULL COMMENT '单价',
                                        `purchase_date` date DEFAULT NULL COMMENT '入库日期',
                                        `expire_date` date DEFAULT NULL COMMENT '过期日期',
                                        `warning_value` int DEFAULT NULL COMMENT '预警值',

                                        `apply_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
                                        `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                        `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

                                        PRIMARY KEY (`id`),
                                        UNIQUE KEY `uk_apply_no` (`apply_no`),
                                        KEY `idx_applicant_id` (`applicant_id`),
                                        KEY `idx_approver_id` (`approver_id`),
                                        KEY `idx_status` (`status`),
                                        KEY `idx_apply_time` (`apply_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='入库审批表';
DROP TABLE IF EXISTS `oa_consumable_notice`;
CREATE TABLE `oa_consumable_notice` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '公示ID',
  `notice_type` varchar(20) NOT NULL COMMENT '公示类型：IN-入库公示，SCRAP-报废公示，STAT-领用统计公示',
  `title` varchar(100) NOT NULL COMMENT '公示标题',
  `content` text NOT NULL COMMENT '公示内容（JSON格式存储，如入库单号、耗材名称、数量等）',
  `dept_name` varchar(50) DEFAULT NULL COMMENT '所属部门（统计公示用）',
  `notice_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '公示时间',
  `expire_time` datetime NOT NULL COMMENT '过期时间（公示30天后过期）',
  `creator_id` int NOT NULL COMMENT '创建人ID',
  `creator_name` varchar(50) NOT NULL COMMENT '创建人姓名',
  `status` varchar(20) NOT NULL DEFAULT 'UNAPPROVED' COMMENT '公示状态：UNAPPROVED-待审批，APPROVED-已批准，EXPIRED-已过期',
  PRIMARY KEY (`id`),
  KEY `idx_notice_type` (`notice_type`),
  KEY `idx_notice_time` (`notice_time`),
  KEY `idx_expire_time` (`expire_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='耗材公示表（入库/报废/领用统计公示，保留30天）';
ALTER TABLE oa_consumable_goods DROP COLUMN storage_location;
SET FOREIGN_KEY_CHECKS = 1;