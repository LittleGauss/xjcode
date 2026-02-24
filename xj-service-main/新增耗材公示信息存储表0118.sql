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