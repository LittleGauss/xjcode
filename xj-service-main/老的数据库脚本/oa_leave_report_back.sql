CREATE TABLE IF NOT EXISTS `oa_leave_report_back` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `leave_id` bigint(20) NOT NULL COMMENT '关联请假单ID',
  `process_instance_id` varchar(64) NOT NULL COMMENT '流程实例ID',
  `actual_start_time` datetime DEFAULT NULL COMMENT '实际开始时间',
  `actual_end_time` datetime DEFAULT NULL COMMENT '实际结束时间',
  `report_back_time` datetime DEFAULT NULL COMMENT '销假申请时间',
  `status` varchar(32) DEFAULT 'PENDING' COMMENT '状态: PENDING-待确认, CONFIRMED-已确认, ARCHIVED-已归档',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_leave_id` (`leave_id`),
  KEY `idx_process_instance_id` (`process_instance_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='请假销假归档表';
