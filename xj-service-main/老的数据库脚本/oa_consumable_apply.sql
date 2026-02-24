CREATE TABLE `oa_consumable_application` (
                                          `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '申请ID',
                                          `item_id` int(11) NOT NULL COMMENT '关联耗材ID',
                                          `item_name` varchar(255) NOT NULL COMMENT '耗材名称',
                                          `applicant_id` int(11) NOT NULL COMMENT '申请人ID',
                                          `applicant_name` varchar(50) NOT NULL COMMENT '申请人姓名',
                                          `applicant_dept` varchar(50) NOT NULL COMMENT '申请人部门',
                                          `consumable_type` varchar(20) NOT NULL COMMENT '耗材类型：electronic/experimental',
                                          `quantity` int(11) NOT NULL COMMENT '申请数量',
                                          `purpose` varchar(500) DEFAULT NULL COMMENT '申请用途',
                                          `apply_time` datetime NOT NULL COMMENT '申请时间',
                                          `process_instance_id` varchar(64) DEFAULT NULL COMMENT '流程实例ID',
                                          `final_status` varchar(20) NOT NULL DEFAULT 'pending' COMMENT '最终状态',
                                          PRIMARY KEY (`id`),
                                          KEY `idx_item_id` (`item_id`),
                                          KEY `idx_applicant_id` (`applicant_id`),
                                          KEY `idx_process_instance_id` (`process_instance_id`),
                                          KEY `idx_final_status` (`final_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '耗材领用申请表';
INSERT INTO `oa_consumable_application` (
    `item_id`, `item_name`, `applicant_id`, `applicant_name`,
    `applicant_dept`, `consumable_type`, `quantity`, `purpose`,
    `apply_time`, `process_instance_id`, `final_status`
) VALUES
      -- 电子类耗材，待审批状态
      (101, 'USB数据线', 1001, '张三', '研发部', 'electronic', 5, '日常办公设备连接', '2024-11-10 09:30:00', 'PROC-20241110-001', 'pending'),

      -- 实验类耗材，已通过状态
      (202, '烧杯(500ml)', 1002, '李四', '实验室', 'experimental', 10, '化学实验专用', '2024-11-09 14:15:00', 'PROC-20241109-002', 'approved'),

      -- 电子类耗材，已拒绝状态
      (103, '无线鼠标', 1003, '王五', '行政部', 'electronic', 2, '备用办公设备', '2024-11-08 16:40:00', 'PROC-20241108-003', 'rejected'),

      -- 实验类耗材，待审批状态（无流程实例ID）
      (204, '试管架', 1004, '赵六', '质检部', 'experimental', 3, '样品检测摆放', '2024-11-10 11:20:00', NULL, 'pending');