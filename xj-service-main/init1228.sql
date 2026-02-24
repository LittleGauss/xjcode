SET NAMES utf8mb4;
SET CHARACTER_SET_CLIENT=utf8mb4;
SET CHARACTER_SET_RESULTS=utf8mb4;

DROP DATABASE IF EXISTS `oa_service`;
CREATE DATABASE  IF NOT EXISTS `oa_service` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;
USE `oa_service`;

SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS `oa_administrative_notice`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `oa_administrative_notice` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '公示标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '公示内容',
  `issue_date` date DEFAULT NULL COMMENT '发布日期',
  `effective_date` date DEFAULT NULL COMMENT '生效日期',
  `expire_date` date DEFAULT NULL COMMENT '失效日期',
  `department` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '发布部门',
  `status` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '当前状态（如已发布、草稿等）',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='行政公示管理表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `oa_administrative_notice`
--

LOCK TABLES `oa_administrative_notice` WRITE;
/*!40000 ALTER TABLE `oa_administrative_notice` DISABLE KEYS */;
/*!40000 ALTER TABLE `oa_administrative_notice` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `oa_administrative_notice_read`
--

DROP TABLE IF EXISTS `oa_administrative_notice_read`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `oa_administrative_notice_read` (
  `id` int NOT NULL AUTO_INCREMENT,
  `notice_id` int NOT NULL,
  `user_id` int NOT NULL,
  `read_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uq_notice_user` (`notice_id`,`user_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=19 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `oa_administrative_notice_read`
--

LOCK TABLES `oa_administrative_notice_read` WRITE;
/*!40000 ALTER TABLE `oa_administrative_notice_read` DISABLE KEYS */;
/*!40000 ALTER TABLE `oa_administrative_notice_read` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `oa_approval_comment`
--

DROP TABLE IF EXISTS `oa_approval_comment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `oa_approval_comment` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `process_instance_id` varchar(64) NOT NULL COMMENT '流程实例ID（如请假流程、合同审核流程）',
  `business_id` int NOT NULL COMMENT '关联的业务ID（如请假单ID、合同ID）',
  `business_type` varchar(32) NOT NULL COMMENT '业务类型（leave/contract等）',
  `approver_id` int NOT NULL COMMENT '审批人ID',
  `approver_name` varchar(64) NOT NULL COMMENT '审批人名称',
  `approval_node` varchar(32) NOT NULL COMMENT '审批节点（如"一级审批"、"二级审批"）',
  `comment` text COMMENT '审批意见内容',
  `approval_result` varchar(16) NOT NULL COMMENT '审批结果（APPROVED/REJECTED）',
  `approved_time` datetime NOT NULL COMMENT '审批时间',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_business` (`business_id`,`business_type`),
  KEY `idx_process` (`process_instance_id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='审批意见表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `oa_approval_comment`
--

LOCK TABLES `oa_approval_comment` WRITE;
/*!40000 ALTER TABLE `oa_approval_comment` DISABLE KEYS */;
/*!40000 ALTER TABLE `oa_approval_comment` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `oa_consumable_application`
--

DROP TABLE IF EXISTS `oa_consumable_application`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `oa_consumable_application` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '申请ID',
  `item_id` int NOT NULL COMMENT '关联耗材ID',
  `item_name` varchar(255) NOT NULL COMMENT '耗材名称',
  `supplier` varchar(100) DEFAULT NULL COMMENT '供货商',
  `applicant_id` int NOT NULL COMMENT '申请人ID',
  `applicant_name` varchar(50) NOT NULL COMMENT '申请人姓名',
  `applicant_dept` varchar(50) NOT NULL COMMENT '申请人部门',
  `consumable_type` varchar(20) NOT NULL COMMENT '耗材类型：electronic/experimental',
  `quantity` int NOT NULL COMMENT '申请数量',
  `purpose` varchar(500) DEFAULT NULL COMMENT '申请用途',
  `apply_time` datetime NOT NULL COMMENT '申请时间',
  `process_instance_id` varchar(64) DEFAULT NULL COMMENT '流程实例ID',
  `final_status` varchar(50) DEFAULT 'pending',
  `assignee_id` int NOT NULL,
  `assignee_name` varchar(45) NOT NULL,
  `actual_distribute_quantity` int NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_item_id` (`item_id`),
  KEY `idx_applicant_id` (`applicant_id`),
  KEY `idx_process_instance_id` (`process_instance_id`),
  KEY `idx_final_status` (`final_status`),
  KEY `idx_supplier` (`supplier`),
  KEY `idx_apply_time` (`apply_time`)
) ENGINE=InnoDB AUTO_INCREMENT=24 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='耗材领用申请表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `oa_consumable_application`
--

LOCK TABLES `oa_consumable_application` WRITE;
/*!40000 ALTER TABLE `oa_consumable_application` DISABLE KEYS */;
/*!40000 ALTER TABLE `oa_consumable_application` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `oa_consumable_category`
--

DROP TABLE IF EXISTS `oa_consumable_category`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `oa_consumable_category` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '分类主键ID',
  `category_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '分类名称（唯一）',
  `sort` int DEFAULT '0' COMMENT '排序序号（前端下拉框显示顺序）',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_category_name` (`category_name`) COMMENT '避免重复创建分类'
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='低值易耗品分类表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `oa_consumable_category`
--

LOCK TABLES `oa_consumable_category` WRITE;
/*!40000 ALTER TABLE `oa_consumable_category` DISABLE KEYS */;
INSERT INTO `oa_consumable_category` VALUES (1,'办公用品',1),(2,'电子耗材',2),(3,'实验耗材',3),(4,'清洁用品',4),(5,'危化品',5),(6,'其他物资',6);
/*!40000 ALTER TABLE `oa_consumable_category` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `oa_consumable_goods`
--

DROP TABLE IF EXISTS `oa_consumable_goods`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `oa_consumable_goods` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `item_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '物品名称',
  `category_id` int DEFAULT NULL COMMENT '分类ID（关联 oa_consumable_category 表主键）',
  `spec` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '型号规格',
  `quantity` int DEFAULT NULL COMMENT '数量',
  `unit` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '单位（如件、个）',
  `purchase_date` date DEFAULT NULL COMMENT '采购日期',
  `status` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '物品状态',
  `storage_location` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '存放位置',
  `brand` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '品牌',
  `supplier` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '供货商',
  `unit_price` decimal(10,2) DEFAULT NULL COMMENT '单价',
  `expire_date` date DEFAULT NULL COMMENT '过期日期',
  `warning_value` int DEFAULT NULL COMMENT '库存预警值',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_category_id` (`category_id`) COMMENT '分类ID索引，提升查询效率',
  CONSTRAINT `fk_goods_category` FOREIGN KEY (`category_id`) REFERENCES `oa_consumable_category` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='低值易耗品管理表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `oa_consumable_goods`
--

LOCK TABLES `oa_consumable_goods` WRITE;
/*!40000 ALTER TABLE `oa_consumable_goods` DISABLE KEYS */;
/*!40000 ALTER TABLE `oa_consumable_goods` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `oa_contract_attachment`
--

DROP TABLE IF EXISTS `oa_contract_attachment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `oa_contract_attachment` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `contract_id` int NOT NULL COMMENT '关联合同ID(oa_contract_review.id)',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '文件名',
  `type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '文件类型：word/pdf/other',
  `size` bigint DEFAULT NULL COMMENT '文件大小（字节）',
  `upload_time` datetime DEFAULT NULL COMMENT '上传时间',
  `store_path` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '实际存储路径或对象存储key（可选）',
  PRIMARY KEY (`id`),
  KEY `idx_contract_id` (`contract_id`),
  CONSTRAINT `fk_contract_attachment_review` FOREIGN KEY (`contract_id`) REFERENCES `oa_contract_review` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='合同附件表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `oa_contract_attachment`
--

LOCK TABLES `oa_contract_attachment` WRITE;
/*!40000 ALTER TABLE `oa_contract_attachment` DISABLE KEYS */;
/*!40000 ALTER TABLE `oa_contract_attachment` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `oa_contract_review`
--

DROP TABLE IF EXISTS `oa_contract_review`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `oa_contract_review` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `contract_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '合同名称',
  `applicant_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '申请人姓名',
  `department` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '申请人所属部门',
  `contract_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '合同类型',
  `submission_date` date DEFAULT NULL COMMENT '提交日期',
  `status` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '审核状态',
  `review_comments` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '审核意见',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `process_instance_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '流程实例ID',
  `current_approver` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '当前审批人',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='合同审核管理表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `oa_contract_review`
--

LOCK TABLES `oa_contract_review` WRITE;
/*!40000 ALTER TABLE `oa_contract_review` DISABLE KEYS */;
/*!40000 ALTER TABLE `oa_contract_review` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `oa_dispatch_attachment`
--

DROP TABLE IF EXISTS `oa_dispatch_attachment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `oa_dispatch_attachment` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `dispatch_id` bigint NOT NULL,
  `upload_id` bigint NOT NULL,
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `sort_order` int DEFAULT '0',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_da_dispatch` (`dispatch_id`) USING BTREE,
  KEY `idx_da_upload` (`upload_id`) USING BTREE,
  CONSTRAINT `fk_da_dispatch` FOREIGN KEY (`dispatch_id`) REFERENCES `oa_vehicle_dispatch` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_da_upload` FOREIGN KEY (`upload_id`) REFERENCES `uploads` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `oa_dispatch_attachment`
--

LOCK TABLES `oa_dispatch_attachment` WRITE;
/*!40000 ALTER TABLE `oa_dispatch_attachment` DISABLE KEYS */;
/*!40000 ALTER TABLE `oa_dispatch_attachment` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `oa_inspection_task`
--

DROP TABLE IF EXISTS `oa_inspection_task`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `oa_inspection_task` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '业务任务ID（主键）',
  `process_instance_id` varchar(64) DEFAULT NULL COMMENT 'Flowable流程实例ID',
  `process_definition_id` varchar(64) DEFAULT NULL COMMENT 'Flowable流程定义ID',
  `task_title` varchar(255) DEFAULT NULL COMMENT '检查任务标题',
  `task_desc` text COMMENT '检查任务描述',
  `initiator_id` bigint DEFAULT NULL COMMENT '发起人工号（部门负责人）',
  `initiator_name` varchar(50) DEFAULT NULL COMMENT '发起人姓名',
  `inspection_form_url` varchar(500) DEFAULT NULL COMMENT '原始检查单上传地址',
  `status` varchar(20) DEFAULT NULL COMMENT '任务整体状态：DRAFT/IN_PROGRESS/COMPLETED/CANCELLED',
  `total_inspectors` int DEFAULT NULL COMMENT '检查员总数',
  `completed_inspectors` int DEFAULT NULL COMMENT '已完成的检查员数',
  `deadline` datetime DEFAULT NULL COMMENT '任务截止日期',
  `overdue_flag` varchar(10) DEFAULT NULL COMMENT '是否逾期：YES/NO',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_initiator_id` (`initiator_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=26 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='检查任务主表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `oa_inspection_task`
--

LOCK TABLES `oa_inspection_task` WRITE;
/*!40000 ALTER TABLE `oa_inspection_task` DISABLE KEYS */;
/*!40000 ALTER TABLE `oa_inspection_task` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `oa_inspection_task_assignment`
--

DROP TABLE IF EXISTS `oa_inspection_task_assignment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `oa_inspection_task_assignment` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '业务分配记录ID（主键）',
  `inspection_task_id` bigint DEFAULT NULL COMMENT '关联检查任务主表ID',
  `inspector_id` bigint DEFAULT NULL COMMENT '检查员工号',
  `inspector_name` varchar(50) DEFAULT NULL COMMENT '检查员姓名',
  `flow_task_id` varchar(64) DEFAULT NULL COMMENT 'Flowable执行任务ID',
  `audit_flow_task_id` varchar(64) DEFAULT NULL COMMENT 'Flowable审核任务ID',
  `task_status` varchar(20) DEFAULT NULL COMMENT '个人任务状态：RECEIVED/COMPLETED/REJECTED/REDOING/OVERDUE',
  `reject_reason` text COMMENT '驳回原因',
  `redo_count` int DEFAULT '0' COMMENT '重新检查次数',
  `filled_form_url` varchar(500) DEFAULT NULL COMMENT '填写后的检查单上传地址',
  `receive_time` datetime DEFAULT NULL COMMENT '接收任务时间',
  `complete_time` datetime DEFAULT NULL COMMENT '完成任务时间',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_task_id` (`inspection_task_id`),
  KEY `idx_flow_task_id` (`flow_task_id`),
  KEY `idx_inspector_id` (`inspector_id`),
  KEY `idx_task_status` (`task_status`)
) ENGINE=InnoDB AUTO_INCREMENT=40 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='检查任务分配表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `oa_inspection_task_assignment`
--

LOCK TABLES `oa_inspection_task_assignment` WRITE;
/*!40000 ALTER TABLE `oa_inspection_task_assignment` DISABLE KEYS */;
INSERT INTO `oa_inspection_task_assignment` VALUES (34,23,20,'employee001','35e30a73-d8d9-11f0-85ca-005056c00008','474ae998-d8d9-11f0-85ca-005056c00008','COMPLETED','测试驳回',1,'oa-inspection/git操作.docx/20251214/1765708816376_git操作.docx','2025-12-14 18:31:45','2025-12-14 18:32:03','2025-12-14 14:31:34','2025-12-14 18:40:57'),(35,23,28,'quality_center','8556658b-d8d9-11f0-85ca-005056c00008','9397fe70-d8d9-11f0-85ca-005056c00008','COMPLETED','是是是',1,'oa-inspection/git操作.docx/20251214/1765708944390_git操作.docx','2025-12-14 18:41:22','2025-12-14 18:41:35','2025-12-14 14:31:34','2025-12-14 18:43:33'),(36,24,20,'employee001','8f502a1a-d8de-11f0-9752-005056c00008','3e180165-d9af-11f0-987f-005056c00008','OVERDUE',NULL,0,'oa-inspection/git操作.docx/20251215/1765800713095_git操作.docx','2025-12-15 20:11:40','2025-12-15 20:11:53','2025-12-14 19:18:05','2025-12-28 14:03:00'),(37,24,28,'quality_center','8f53faae-d8de-11f0-9752-005056c00008',NULL,'OVERDUE',NULL,0,NULL,NULL,NULL,'2025-12-14 19:18:05','2025-12-28 14:03:00'),(38,25,20,'employee001','ed2d651b-d9ad-11f0-987f-005056c00008',NULL,'OVERDUE',NULL,0,NULL,'2025-12-15 20:11:41',NULL,'2025-12-15 20:02:28','2025-12-28 14:03:00'),(39,25,28,'quality_center','ed326e2f-d9ad-11f0-987f-005056c00008',NULL,'OVERDUE',NULL,0,NULL,NULL,NULL,'2025-12-15 20:02:28','2025-12-28 14:03:00');
/*!40000 ALTER TABLE `oa_inspection_task_assignment` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `oa_inspection_template`
--

DROP TABLE IF EXISTS `oa_inspection_template`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `oa_inspection_template` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `template_name` varchar(255) NOT NULL COMMENT '模板名称',
  `file_type` varchar(255) NOT NULL COMMENT '文件类型（如docx、pdf、xlsx）',
  `file_size` bigint NOT NULL COMMENT '文件大小（字节）',
  `file_path` varchar(500) NOT NULL COMMENT 'MinIO存储路径（bucket/objectName）',
  `uploader_id` bigint NOT NULL COMMENT '上传人ID',
  `uploader_name` varchar(100) NOT NULL COMMENT '上传人名称',
  `upload_time` datetime NOT NULL COMMENT '上传时间',
  `biz_type` varchar(50) NOT NULL DEFAULT 'inspection-template' COMMENT '业务类型',
  `bucket_name` varchar(100) NOT NULL COMMENT '存储桶名称',
  PRIMARY KEY (`id`),
  KEY `idx_uploader_id` (`uploader_id`),
  KEY `idx_upload_time` (`upload_time`),
  KEY `idx_biz_type` (`biz_type`),
  KEY `idx_template_name` (`template_name`),
  KEY `idx_file_type` (`file_type`)
) ENGINE=InnoDB AUTO_INCREMENT=72 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='检查任务模板表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `oa_inspection_template`
--

LOCK TABLES `oa_inspection_template` WRITE;
/*!40000 ALTER TABLE `oa_inspection_template` DISABLE KEYS */;
/*!40000 ALTER TABLE `oa_inspection_template` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `oa_leave_attachment`
--

DROP TABLE IF EXISTS `oa_leave_attachment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `oa_leave_attachment` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `leave_id` bigint DEFAULT NULL COMMENT '请假单ID',
  `file_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '文件名',
  `file_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '文件存储路径',
  `file_size` bigint DEFAULT NULL COMMENT '文件大小(字节)',
  `file_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '文件类型',
  `upload_user_id` bigint NOT NULL COMMENT '上传人ID',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `leave_id` (`leave_id`) USING BTREE,
  CONSTRAINT `oa_leave_attachment_ibfk_1` FOREIGN KEY (`leave_id`) REFERENCES `oa_leave_process` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `oa_leave_attachment`
--

LOCK TABLES `oa_leave_attachment` WRITE;
/*!40000 ALTER TABLE `oa_leave_attachment` DISABLE KEYS */;
/*!40000 ALTER TABLE `oa_leave_attachment` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `oa_leave_process`
--

DROP TABLE IF EXISTS `oa_leave_process`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `oa_leave_process` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` int DEFAULT NULL COMMENT '用户ID',
  `user_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '用户姓名',
  `process_instance_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '流程实例ID',
  `leave_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '请假类型',
  `start_date` datetime DEFAULT NULL COMMENT '开始时间',
  `end_date` datetime DEFAULT NULL COMMENT '结束时间',
  `reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '请假原因',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT 'PENDING' COMMENT '状态(PENDING/APPROVED/REJECTED)',
  `current_approver` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '当前审批人',
  `created_at` datetime DEFAULT NULL COMMENT '创建时间',
  `updated_at` datetime DEFAULT NULL COMMENT '更新时间',
  `duration` double DEFAULT NULL COMMENT '请假时长(天)',
  `department_id` int DEFAULT NULL COMMENT '申请人所属部门ID',
  `department_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '申请人所属部门',
  `current_approver_name` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1982738976936640543 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='请假流程表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `oa_leave_process`
--

LOCK TABLES `oa_leave_process` WRITE;
/*!40000 ALTER TABLE `oa_leave_process` DISABLE KEYS */;
/*!40000 ALTER TABLE `oa_leave_process` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `oa_leave_report_back`
--

DROP TABLE IF EXISTS `oa_leave_report_back`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `oa_leave_report_back` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `leave_id` bigint NOT NULL COMMENT '关联请假单ID',
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='请假销假归档表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `oa_leave_report_back`
--

LOCK TABLES `oa_leave_report_back` WRITE;
/*!40000 ALTER TABLE `oa_leave_report_back` DISABLE KEYS */;
/*!40000 ALTER TABLE `oa_leave_report_back` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `oa_news`
--

DROP TABLE IF EXISTS `oa_news`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `oa_news` (
  `id` tinyint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `title` varchar(255) NOT NULL COMMENT '新闻标题',
  `content` text NOT NULL COMMENT '新闻内容',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '软删除标识（0未删除，1已删除）',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='新闻表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `oa_news`
--

LOCK TABLES `oa_news` WRITE;
/*!40000 ALTER TABLE `oa_news` DISABLE KEYS */;
INSERT INTO `oa_news` VALUES (2,'纤监科普 | 生活小妙招之如何判断织物甲醛含量超标','在我们的日常生活中，衣物、窗帘、床上用品等各类织物无处不在。然而，部分织物在生产过程中为了达到防皱、定型、染色固色等效果，可能会存在甲醛超标的现象。甲醛是一种对人体健康有害的物质，长期接触可能引发皮肤过敏、呼吸道不适等问题，严重时还会危害神经系统和免疫系统。掌握一些简单实用的判断方法，能帮助我们在日常生活中避开甲醛超标的织物。下面就为大家介绍几个常见的生活小妙招。\n\n \n\n一、感官判断法：直观感受初筛查\n\n感官判断是最直接、最便捷的初步筛查方式，通过闻气味、摸触感、看外观等简单操作，就能对织物的甲醛含量情况做出初步判断。\n\n \n\n1.闻气味，刺激性异味是重要信号：甲醛具有强烈的刺激性气味，这是其最明显的特征之一。拿到新织物后，先将其密封在塑料袋中放置1-2小时，让气味充分聚集。随后打开塑料袋，凑近闻织物的气味。如果能闻到明显的刺鼻味、类似福尔马林的味道，或者其他难以描述的刺激性异味，那么这款织物甲醛含量超标的可能性非常大。需要注意的是，有些织物可能会添加香精来掩盖甲醛的气味，所以如果闻到浓郁的香精味但仍能隐约察觉到刺激性气息，也需要提高警惕。\n\n \n\n2.摸触感，发硬发黏需谨慎：甲醛在织物上会形成一定的胶状物质，导致织物的触感发生变化。正常的织物手感柔软、顺滑，富有弹性。如果摸到的织物手感发硬、发脆，或者表面有黏腻感，尤其是在折叠后展开时能感觉到明显的“僵硬感”，说明织物可能经过了甲醛处理且残留量较高。这种情况在牛仔服、免烫衬衫等需要定型的衣物中较为常见，购买时需格外留意。\n\n \n\n3.看外观，异常色泽或褶皱要留意：虽然外观不能直接作为判断甲醛超标的依据，但某些异常表现也能提供参考。例如，部分甲醛超标的织物颜色会异常鲜艳，染色过于均匀，尤其是深色织物，为了固色可能会使用更多含甲醛的助剂。此外，一些宣称“永久免烫”的织物，如果褶皱异常平整且不易变形，也需要警惕甲醛残留问题，因为免烫效果的实现往往与甲醛类整理剂的使用有关。\n\n \n\n二、简易测试法：借助常见物品深检测\n\n如果通过感官判断无法确定织物甲醛含量是否超标，或者对织物的安全性要求较高（如婴儿衣物、贴身内衣），可以借助一些常见的物品进行简易测试，进一步验证织物的甲醛情况。\n\n \n\n1.水浸测试法，利用甲醛水溶性特性：甲醛具有较强的水溶性，将织物浸泡在水中，甲醛会溶解到水中，通过观察浸泡后的水和织物变化来判断。具体操作步骤如下：首先取一小块织物样本（若为衣物可剪取衣角内侧不显眼处），放入干净的玻璃杯中，加入适量清水，确保织物完全浸泡在水中，浸泡时间为1-2小时。浸泡结束后，观察水的颜色是否发生变化，若水质变得浑浊、出现异味，或者织物的颜色有明显脱落，说明织物可能存在甲醛超标或其他化学助剂残留问题。同时，将浸泡后的织物拧干后闻气味，若刺激性气味明显减弱，也从侧面印证了织物中可能含有可溶于水的甲醛。\n\n \n\n2.试纸测试法，快速检测更精准：甲醛检测试纸是一种专门用于检测甲醛含量的简易工具，在药店、网上商城等渠道都能购买到，操作简单且性价比高。使用时，先将织物密封一段时间，然后将试纸放入密封环境中，按照试纸说明书的要求等待一定时间，观察试纸颜色的变化。对照试纸的颜色对比卡，就能大致判断织物释放的甲醛含量是否超标。需要注意的是，试纸测试结果会受到环境温度、湿度等因素的影响，建议在常温常湿的环境下进行测试，以提高结果的准确性。\n\n \n\n三、专业参考法：关注标识与渠道\n\n除了自行判断，关注织物的产品标识和购买渠道，也是避免购买到甲醛超标织物的重要方法。\n\n \n\n1.查看产品标识，认准安全标准：正规厂家生产的织物都会标注完整的产品标识，包括产品名称、型号、执行标准、安全类别、生产厂家等信息。其中，安全类别是判断织物安全性的关键依据。我国将纺织品分为A、B、C三个安全类别，A类为婴幼儿用品，甲醛含量要求≤20mg/kg；B类为直接接触皮肤的用品，甲醛含量要求≤75mg/kg；C类为非直接接触皮肤的用品，甲醛含量要求≤300mg/kg。购买时一定要查看安全类别，婴幼儿衣物必须选择A类，贴身衣物优先选择B类，避免购买无标识或标识不完整的产品。\n\n \n\n2.选择正规渠道，降低购买风险：尽量选择正规的商场、超市或品牌官方旗舰店购买织物，这些渠道的产品通常经过严格的质量检测，甲醛超标的风险较低。而一些路边摊、无资质的小作坊生产织物时，为了降低成本，可能会使用劣质原料和含甲醛的助剂，导致产品质量难以保证。购买时还可以要求商家提供产品的质量检测报告，进一步确认织物的甲醛含量是否符合标准。\n\n \n\n温馨提示：即使是经过检测确认甲醛含量合格的织物，购买后也建议先进行清洗和晾晒。甲醛易挥发且溶于水，通过清水洗涤可以去除大部分残留的甲醛，晾晒时保持通风良好，让织物充分接触空气，能进一步加速甲醛的挥发，确保穿着和使用安全。\n\n \n\n通过以上这些生活小妙招，我们就能在日常生活中较为准确地判断织物甲醛含量是否超标，为自己和家人的健康保驾护航。在购买和使用织物时，多一份细心和留意，就能多一份安全与放心。','2025-12-01 14:06:07','2025-12-01 14:06:07',0),(3,'关于征集整治形式主义为基层减负问题线索及意见建议的公告','   为深入贯彻落实党中央、自治区党委及区局党组关于整治形式主义为基层减负的有关要求，持续推动解决基层干部群众身边的形式主义问题，现面向社会征集关于整治形式主义为基层减负方面的问题线索及意见建议。中心纪委对社会反映纤维监测方面的相关问题线索，将及时跟进，并督促相关部门整改；对反映的突出问题一经查实，将严肃查处问责、公开通报曝光；对提出的合理化意见建议，将及时采纳、推广。\n\n         征集电话：0991-3191709\n\n         征集邮箱：xjxjdjb@163.com。\n\n \n\n自治区纤维质量监测中心\n\n                            2024年4月7日        ','2025-12-01 14:06:36','2025-12-01 14:06:36',0),(4,'自治区纤维质量监测中心开展领导干部公开接访日和热线日活动安排','     为加强领导干部公开接访工作，更好解决中心行风问题，倾听群众合理诉求，维护群众合法权益，根据《全区市场监管系统行风建设三年攻坚专项行动实施方案》《全齐市场监管系统行风建设三年攻坚专项行动2023年工作要点》《关于开展领导干部公开接访日和热线日活动的通知》，自治区纤维质量监测中心决定开展领导干部公开接访日和热线日活动，现将有关事宜公告如下。\n\n         一、接待人员\n\n         自治区纤维质量监测中心领导班子成员、纪检干部。\n\n         二、接访流程\n\n         （一）公告。通过自治区纤维质量监测中心门户网站，向社会公开接访时间。\n\n         （二）登记。由行政办公室统筹安排专人负责做好来访人员的登记、疏导以及联络工作；对来访群众提出的来访事项应及时、全面、客观、准确登记，包括来访人反映问题的主要情况、诉求及理由，以往来访过程及有关办理情况，本次来访处理情况等；对进一步接谈的来访事项，要及时向接谈领导报告，需要疏导的来访事项，登记人员要认真倾听群众反映的诉求，深入细致地做好思想疏导、政策解释和路径指引工作。\n\n         （三）谈话。接访人员要核对来访人的身份证或其他有效证件，阅看相关材料，听取来访人的陈述，询问有关情况，引导来访人详细叙述建议、意见或者投诉请求，讲清事实和理由，核实来访登记信息。对疑难、复杂、敏感及群体性、政策性等来访事项，可协调相关地方、部门进行联合接待。\n\n         （四）处理。严格按照《信访工作条例》，对来访人提出的信访事项，按照信访事项的性质和管辖层级，引导来访人到有权处理的本级或者上一级单位设立或指定的接待场所提出；对来访人提出的投诉举报事项，按照投诉举报受理流程交由相关部门受理。对不予受理的，向来访人宣传有关法律法规，做好疏导引导工作。\n\n         三、时间地点\n\n         2023年11月10日下午（16:00-18:00）、13日下午（16:00- 18:00）,中心 1名领导进行接访工作，若有来访人员，接访人员应及时通知领导到接访室，因故不能到场的，需提前商请其他领导代替接访。\n\n \n\n \n\n自治区纤维质量监测中心\n\n2023年11月10日','2025-12-01 14:06:50','2025-12-01 14:06:50',0);
/*!40000 ALTER TABLE `oa_news` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `oa_routine_inspection`
--

DROP TABLE IF EXISTS `oa_routine_inspection`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `oa_routine_inspection` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `inspector_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '检查人员姓名',
  `inspect_date` date DEFAULT NULL COMMENT '检查日期',
  `target_department` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '被检查部门',
  `result` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '检查结果',
  `remarks` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '备注',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='日常监督检查表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `oa_routine_inspection`
--

LOCK TABLES `oa_routine_inspection` WRITE;
/*!40000 ALTER TABLE `oa_routine_inspection` DISABLE KEYS */;
/*!40000 ALTER TABLE `oa_routine_inspection` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `oa_scanner_record`
--

DROP TABLE IF EXISTS `oa_scanner_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `oa_scanner_record` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `scanner_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '高拍仪编号',
  `operator_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '操作人员姓名',
  `use_date` datetime DEFAULT NULL COMMENT '使用时间',
  `function_selected` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '所选功能',
  `camera_selected` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '所选摄像头',
  `remarks` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '备注',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='高拍仪使用记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `oa_scanner_record`
--

LOCK TABLES `oa_scanner_record` WRITE;
/*!40000 ALTER TABLE `oa_scanner_record` DISABLE KEYS */;
/*!40000 ALTER TABLE `oa_scanner_record` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `oa_sign_record`
--

DROP TABLE IF EXISTS `oa_sign_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `oa_sign_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '签名 ID（主键）自增',
  `user_id` bigint NOT NULL COMMENT '签名人 ID 关联sys_user.id',
  `sign_url` varchar(500) NOT NULL COMMENT '签名图片存储路径 MinIO路径',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '签名时间',
  `ext1` varchar(255) DEFAULT NULL COMMENT '扩展字段 1 可存储签名备注',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=25 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='电子签名记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `oa_sign_record`
--

LOCK TABLES `oa_sign_record` WRITE;
/*!40000 ALTER TABLE `oa_sign_record` DISABLE KEYS */;
/*!40000 ALTER TABLE `oa_sign_record` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `oa_supervision_assignment`
--

DROP TABLE IF EXISTS `oa_supervision_assignment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `oa_supervision_assignment` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `task_id` bigint NOT NULL,
  `assignee_user_id` bigint DEFAULT NULL,
  `assignee_dept_id` bigint DEFAULT NULL,
  `assigned_by` bigint DEFAULT NULL,
  `assigned_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT 'pending',
  `completed_at` datetime DEFAULT NULL,
  `flowable_task_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Flowable task id',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_assignment_task_id` (`task_id`) USING BTREE,
  KEY `idx_assignment_user` (`assignee_user_id`) USING BTREE,
  KEY `idx_assignment_flowable_task_id` (`flowable_task_id`) USING BTREE,
  CONSTRAINT `fk_assignment_task` FOREIGN KEY (`task_id`) REFERENCES `oa_supervision_task` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=50 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `oa_supervision_assignment`
--

LOCK TABLES `oa_supervision_assignment` WRITE;
/*!40000 ALTER TABLE `oa_supervision_assignment` DISABLE KEYS */;
/*!40000 ALTER TABLE `oa_supervision_assignment` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `oa_supervision_feedback`
--

DROP TABLE IF EXISTS `oa_supervision_feedback`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `oa_supervision_feedback` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `task_id` bigint NOT NULL,
  `assignment_id` bigint DEFAULT NULL,
  `feedback_by` bigint DEFAULT NULL,
  `feedback_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `finish_date` date DEFAULT NULL,
  `remarks` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_feedback_task_id` (`task_id`) USING BTREE,
  KEY `idx_feedback_assignment_id` (`assignment_id`) USING BTREE,
  CONSTRAINT `fk_feedback_assignment` FOREIGN KEY (`assignment_id`) REFERENCES `oa_supervision_assignment` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_feedback_task` FOREIGN KEY (`task_id`) REFERENCES `oa_supervision_task` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=37 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `oa_supervision_feedback`
--

LOCK TABLES `oa_supervision_feedback` WRITE;
/*!40000 ALTER TABLE `oa_supervision_feedback` DISABLE KEYS */;
/*!40000 ALTER TABLE `oa_supervision_feedback` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `oa_supervision_task`
--

DROP TABLE IF EXISTS `oa_supervision_task`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `oa_supervision_task` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
  `created_by` bigint DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `due_date` date DEFAULT NULL,
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'open',
  `progress` int DEFAULT '0',
  `last_update` datetime DEFAULT NULL,
  `notice_id` bigint DEFAULT NULL,
  `process_instance_id` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_supervision_notice_id` (`notice_id`) USING BTREE,
  KEY `idx_supervision_status` (`status`) USING BTREE,
  CONSTRAINT `fk_oa_supervision_task_notice` FOREIGN KEY (`notice_id`) REFERENCES `administrative_notice` (`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=46 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `oa_supervision_task`
--

LOCK TABLES `oa_supervision_task` WRITE;
/*!40000 ALTER TABLE `oa_supervision_task` DISABLE KEYS */;
/*!40000 ALTER TABLE `oa_supervision_task` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `oa_vehicle_dispatch`
--

DROP TABLE IF EXISTS `oa_vehicle_dispatch`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `oa_vehicle_dispatch` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `dispatch_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `vehicle_id` int NOT NULL,
  `driver_id` bigint DEFAULT NULL,
  `requester_id` bigint DEFAULT NULL,
  `department` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `purpose` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `start_time` datetime DEFAULT NULL,
  `end_time` datetime DEFAULT NULL,
  `start_location` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `end_location` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `start_mileage` bigint DEFAULT NULL,
  `end_mileage` bigint DEFAULT NULL,
  `estimated_mileage` bigint DEFAULT NULL,
  `actual_mileage` bigint DEFAULT NULL,
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'draft',
  `approve_by` bigint DEFAULT NULL,
  `approve_time` datetime DEFAULT NULL,
  `remarks` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
  `created_by` bigint DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `fuel_cost` decimal(10,2) DEFAULT '0.00' COMMENT '本次燃油费',
  `toll_fee` decimal(10,2) DEFAULT '0.00' COMMENT '过路费',
  `parking_fee` decimal(10,2) DEFAULT '0.00' COMMENT '停车费',
  `insurance_fee` decimal(10,2) DEFAULT '0.00' COMMENT '保险费',
  `annual_inspection_fee` decimal(10,2) DEFAULT '0.00' COMMENT '年审费',
  `trip_mileage` bigint DEFAULT NULL COMMENT '本次行驶里程',
  `requester_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '申请人名称(冗余)',
  `plate_number` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '冗余车牌号用于展示/查询',
  `deleted_flag` tinyint(1) NOT NULL DEFAULT '0' COMMENT '软删除标识',
  `version` int NOT NULL DEFAULT '0' COMMENT '乐观锁版本',
  `dispatch_number` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '派单编号',
  `brand` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '车辆品牌(冗余)',
  `model` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '车型(冗余)',
  `use_date` date DEFAULT NULL COMMENT '使用日期',
  `departure_time` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '出发时间',
  `return_time` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '返回时间',
  `repair_cost` decimal(10,2) DEFAULT '0.00' COMMENT '维修费',
  `remark` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '备注',
  `attachments` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '附件元数据(JSON)',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `dispatch_no` (`dispatch_no`) USING BTREE,
  KEY `idx_dispatch_vehicle` (`vehicle_id`) USING BTREE,
  KEY `idx_dispatch_driver` (`driver_id`) USING BTREE,
  KEY `idx_dispatch_status` (`status`) USING BTREE,
  KEY `idx_dispatch_start_time` (`start_time`) USING BTREE,
  KEY `idx_dispatch_end_time` (`end_time`) USING BTREE,
  KEY `idx_dispatch_plate_number` (`plate_number`) USING BTREE,
  KEY `idx_oa_vehicle_dispatch_dispatch_number` (`dispatch_number`) USING BTREE,
  CONSTRAINT `fk_dispatch_vehicle` FOREIGN KEY (`vehicle_id`) REFERENCES `oa_vehicle_management` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=27 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `oa_vehicle_dispatch`
--

LOCK TABLES `oa_vehicle_dispatch` WRITE;
/*!40000 ALTER TABLE `oa_vehicle_dispatch` DISABLE KEYS */;
/*!40000 ALTER TABLE `oa_vehicle_dispatch` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `oa_vehicle_management`
--

DROP TABLE IF EXISTS `oa_vehicle_management`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `oa_vehicle_management` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `plate_number` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '车牌号',
  `brand` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '品牌',
  `model` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '型号',
  `department` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '所属部门',
  `mileage` int DEFAULT NULL COMMENT '总里程（公里）',
  `fuel_cost` decimal(10,2) DEFAULT NULL COMMENT '油耗费用（元）',
  `repair_cost` decimal(10,2) DEFAULT NULL COMMENT '维修费用（元）',
  `insurance_status` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '保险状态',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `displacement` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '排量',
  `fuel_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '燃油类型',
  `vehicle_status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '正常' COMMENT '车辆状态',
  `purchase_date` date DEFAULT NULL COMMENT '购置日期',
  `remark` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '备注',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted_flag` tinyint(1) NOT NULL DEFAULT '0' COMMENT '软删除标识',
  `version` int NOT NULL DEFAULT '0' COMMENT '乐观锁版本',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uq_vehicle_plate_number` (`plate_number`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='公车管理表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `oa_vehicle_management`
--

LOCK TABLES `oa_vehicle_management` WRITE;
/*!40000 ALTER TABLE `oa_vehicle_management` DISABLE KEYS */;
/*!40000 ALTER TABLE `oa_vehicle_management` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `supervision_feedback_upload`
--

DROP TABLE IF EXISTS `supervision_feedback_upload`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `supervision_feedback_upload` (
  `feedback_id` bigint NOT NULL,
  `upload_id` bigint NOT NULL,
  PRIMARY KEY (`feedback_id`,`upload_id`),
  KEY `idx_sfu_feedback` (`feedback_id`),
  KEY `idx_sfu_upload` (`upload_id`),
  CONSTRAINT `fk_oa_sfu_feedback` FOREIGN KEY (`feedback_id`) REFERENCES `oa_supervision_feedback` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_oa_sfu_upload` FOREIGN KEY (`upload_id`) REFERENCES `uploads` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `supervision_feedback_upload`
--

LOCK TABLES `supervision_feedback_upload` WRITE;
/*!40000 ALTER TABLE `supervision_feedback_upload` DISABLE KEYS */;
/*!40000 ALTER TABLE `supervision_feedback_upload` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_department`
--

DROP TABLE IF EXISTS `sys_department`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_department` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '部门编码',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '部门名称',
  `parent_id` int DEFAULT NULL COMMENT '父部门ID',
  `leader_id` int DEFAULT NULL COMMENT '部门负责人ID',
  `sort_order` int DEFAULT '0' COMMENT '排序',
  `status` tinyint DEFAULT '1' COMMENT '状态',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `code` (`code`),
  KEY `parent_id` (`parent_id`),
  CONSTRAINT `sys_department_ibfk_1` FOREIGN KEY (`parent_id`) REFERENCES `sys_department` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='部门表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_department`
--

LOCK TABLES `sys_department` WRITE;
/*!40000 ALTER TABLE `sys_department` DISABLE KEYS */;
INSERT INTO `sys_department` VALUES (1,'DEPT001','技术研发部',NULL,19,1,1,'2025-10-28 01:24:59'),(2,'DEPT002','行政办公室',NULL,19,2,1,'2025-10-28 01:24:59'),(3,'DEPT003','后勤保障部',NULL,19,3,1,'2025-10-28 01:24:59'),(4,'DEPT004','信息技术中心',NULL,19,4,1,'2025-10-28 01:24:59'),(5,'DEPT005','质量检测中心',NULL,19,5,1,'2025-10-28 01:24:59'),(6,'DEPT006','综合管理部',NULL,19,6,1,'2025-10-28 01:24:59');
/*!40000 ALTER TABLE `sys_department` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_oper_log`
--

DROP TABLE IF EXISTS `sys_oper_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_oper_log` (
  `oper_id` bigint NOT NULL AUTO_INCREMENT COMMENT '日志主键',
  `title` varchar(50) DEFAULT '' COMMENT '模块标题',
  `business_type` int DEFAULT '0' COMMENT '业务类型（0其它 1新增 2修改 3删除 4授权 5导出 6导入 7强退 8生成代码 9清空数据 10上传）',
  `method` varchar(100) DEFAULT '' COMMENT '方法名称',
  `request_method` varchar(10) DEFAULT '' COMMENT '请求方式',
  `oper_name` varchar(50) DEFAULT '' COMMENT '操作人员',
  `oper_url` varchar(255) DEFAULT '' COMMENT '请求URL',
  `oper_ip` varchar(128) DEFAULT '' COMMENT '主机地址',
  `oper_param` longtext COMMENT '请求参数',
  `json_result` longtext COMMENT '返回参数',
  `status` int DEFAULT '0' COMMENT '操作状态（0正常 1异常）',
  `error_msg` varchar(2000) DEFAULT '' COMMENT '错误消息',
  `oper_time` datetime DEFAULT NULL COMMENT '操作时间',
  `cost_time` bigint DEFAULT '0' COMMENT '消耗时间(毫秒)',
  PRIMARY KEY (`oper_id`),
  KEY `idx_sys_oper_log_bt` (`business_type`),
  KEY `idx_sys_oper_log_s` (`status`),
  KEY `idx_sys_oper_log_ot` (`oper_time`)
) ENGINE=InnoDB AUTO_INCREMENT=2005157787181473795 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='操作日志记录';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_oper_log`
--

LOCK TABLES `sys_oper_log` WRITE;
/*!40000 ALTER TABLE `sys_oper_log` DISABLE KEYS */;
/*!40000 ALTER TABLE `sys_oper_log` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_permission`
--

DROP TABLE IF EXISTS `sys_permission`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_permission` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '权限编码',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '权限名称',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '权限描述',
  `resource_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '资源类型(菜单/按钮/API等)',
  `resource_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '资源路径',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `code` (`code`)
) ENGINE=InnoDB AUTO_INCREMENT=50 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='权限表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_permission`
--

LOCK TABLES `sys_permission` WRITE;
/*!40000 ALTER TABLE `sys_permission` DISABLE KEYS */;
INSERT INTO `sys_permission` VALUES (6,'USER:NORMAL','普通用户权限','登录、改密码、待办提醒等基础权限','API',NULL,'2025-10-28 01:02:18','2025-10-28 01:02:18'),(7,'LEAVE:APPLY','请假申请','发起请假申请','MENU',NULL,'2025-10-28 01:02:18','2025-10-28 01:02:18'),(8,'LEAVE:CANCEL_APPLY','销假申请','发起销假申请','BUTTON',NULL,'2025-10-28 01:02:18','2025-10-28 01:02:18'),(9,'LEAVE:VIEW_SELF','查看个人请假','查看个人请假记录','MENU',NULL,'2025-10-28 01:02:18','2025-10-28 01:02:18'),(10,'LEAVE:VIEW_DEPT','查看部门请假','查看本部门请假记录','MENU',NULL,'2025-10-28 01:02:18','2025-10-28 01:02:18'),(11,'LEAVE:VIEW_ALL','查看所有请假','查看所有部门请假记录','MENU',NULL,'2025-10-28 01:02:18','2025-10-28 01:02:18'),(12,'LEAVE:APPROVE_HALF_DAY','审批半天假','审批半天内的请假','API',NULL,'2025-10-28 01:02:18','2025-10-28 01:02:18'),(13,'LEAVE:APPROVE_3DAY','审批三天内假','审批三天内的请假','API',NULL,'2025-10-28 01:02:18','2025-10-28 01:02:18'),(14,'LEAVE:APPROVE_LONG','审批长期假','审批长期请假','API',NULL,'2025-10-28 01:02:18','2025-10-28 01:02:18'),(15,'LEAVE:APPROVE_LEADER','审批领导假','审批领导请假','API',NULL,'2025-10-28 01:02:18','2025-10-28 01:02:18'),(16,'LEAVE:STAT_DEPT','部门统计','查看部门请假统计','MENU',NULL,'2025-10-28 01:02:18','2025-10-28 01:02:18'),(17,'LEAVE:STAT_ALL','全局统计','查看所有部门请假统计','MENU',NULL,'2025-10-28 01:02:18','2025-10-28 01:02:18'),(18,'LEAVE:EXPORT','导出请假数据','导出请假统计报表','BUTTON',NULL,'2025-10-28 01:02:18','2025-10-28 01:02:18'),(19,'VEHICLE:RECORD','用车记录','录入用车记录','MENU',NULL,'2025-10-28 01:02:18','2025-10-28 01:02:18'),(20,'VEHICLE:UPLOAD','附件上传','上传用车相关附件','BUTTON',NULL,'2025-10-28 01:02:18','2025-10-28 01:02:18'),(21,'VEHICLE:VIEW','查看车辆信息','查看公车基本信息','MENU',NULL,'2025-10-28 01:02:18','2025-10-28 01:02:18'),(22,'VEHICLE:STAT','统计分析','查看公车统计数据','MENU',NULL,'2025-10-28 01:02:18','2025-10-28 01:02:18'),(23,'VEHICLE:EXPORT','导出车辆数据','导出车辆统计报表','BUTTON',NULL,'2025-10-28 01:02:18','2025-10-28 01:02:18'),(24,'VEHICLE:MANAGE','车辆管理','管理车辆基础信息','MENU',NULL,'2025-10-28 01:02:18','2025-10-28 01:02:18'),(25,'NOTICE:VIEW','查看公示','查看行政公示文件','MENU',NULL,'2025-10-28 01:02:18','2025-10-28 01:02:18'),(26,'NOTICE:MANAGE','管理公示','发布/编辑/删除公示','MENU',NULL,'2025-10-28 01:02:18','2025-10-28 01:02:18'),(27,'NOTICE:STAT','阅读统计','查看公示阅读情况统计','MENU',NULL,'2025-10-28 01:02:18','2025-10-28 01:02:18'),(28,'NOTICE:EXPORT_UNREAD','导出未读名单','导出未阅读人员名单','BUTTON',NULL,'2025-10-28 01:02:18','2025-10-28 01:02:18'),(29,'NOTICE:SUPERVISE','督办管理','发布和跟踪督办任务','MENU',NULL,'2025-10-28 01:02:18','2025-10-28 01:02:18'),(30,'NOTICE:FEEDBACK','任务反馈','反馈督办任务办结状态','BUTTON',NULL,'2025-10-28 01:02:18','2025-10-28 01:02:18'),(31,'SUPPLY:APPLY','领用申请','发起物品领用申请','MENU',NULL,'2025-10-28 01:02:18','2025-10-28 01:02:18'),(32,'SUPPLY:APPROVE_DEPT','部门审批','审批本部门领用申请','API',NULL,'2025-10-28 01:02:18','2025-10-28 01:02:18'),(33,'SUPPLY:APPROVE_IT','电子耗材审批','审批电子耗材申请','API',NULL,'2025-10-28 01:02:18','2025-10-28 01:02:18'),(34,'SUPPLY:APPROVE_QUALITY','实验耗材审批','审批实验耗材申请','API',NULL,'2025-10-28 01:02:18','2025-10-28 01:02:18'),(35,'SUPPLY:APPROVE_FINAL','最终审核','最终审核发放物品','API',NULL,'2025-10-28 01:02:18','2025-10-28 01:02:18'),(36,'SUPPLY:MANAGE','库存管理','管理库存和预警设置','MENU',NULL,'2025-10-28 01:02:18','2025-10-28 01:02:18'),(37,'SUPPLY:STAT','统计分析','查看物品统计报表','MENU',NULL,'2025-10-28 01:02:18','2025-10-28 01:02:18'),(38,'SUPPLY:EXPORT','导出物品数据','导出物品统计报表','BUTTON',NULL,'2025-10-28 01:02:18','2025-10-28 01:02:18'),(39,'SUPPLY:IMPORT','批量导入','Excel批量导入库存数据','BUTTON',NULL,'2025-10-28 01:02:18','2025-10-28 01:02:18'),(40,'CONTRACT:UPLOAD','上传合同','上传合同文件','BUTTON',NULL,'2025-10-28 01:02:18','2025-10-28 01:02:18'),(41,'CONTRACT:MODIFY','修改合同','修改合同内容','BUTTON',NULL,'2025-10-28 01:02:18','2025-10-28 01:02:18'),(42,'CONTRACT:LEGAL_REVIEW','法务审核','进行法律审核','API',NULL,'2025-10-28 01:02:18','2025-10-28 01:02:18'),(43,'CONTRACT:COUNTERSIGN','综合会签','综合管理部门会签','API',NULL,'2025-10-28 01:02:18','2025-10-28 01:02:18'),(44,'CONTRACT:FINAL_CONFIRM','最终确认','行政办最终确认合同','API',NULL,'2025-10-28 01:02:18','2025-10-28 01:02:18'),(45,'CONTRACT:VIEW_DEPT','查看部门合同','查看本部门合同','MENU',NULL,'2025-10-28 01:02:18','2025-10-28 01:02:18'),(46,'CONTRACT:VIEW_ALL','查看所有合同','查看所有部门合同','MENU',NULL,'2025-10-28 01:02:18','2025-10-28 01:02:18'),(47,'CONTRACT:DOWNLOAD','下载合同','下载合同文件','BUTTON',NULL,'2025-10-28 01:02:18','2025-10-28 01:02:18'),(48,'SUPERVISION:MANAGE','日常监督管理','日常监督管理权限','menu','/daily-supervision/manage','2025-12-07 16:15:07','2025-12-07 16:15:07'),(49,'SUPERVISION:INSPECT','日常监督检查','日常监督检查权限','menu','/daily-supervision/inspect','2025-12-07 16:15:07','2025-12-07 16:15:07');
/*!40000 ALTER TABLE `sys_permission` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_role`
--

DROP TABLE IF EXISTS `sys_role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_role` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '角色编码',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '角色名称',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '角色描述',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `code` (`code`)
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_role`
--

LOCK TABLES `sys_role` WRITE;
/*!40000 ALTER TABLE `sys_role` DISABLE KEYS */;
INSERT INTO `sys_role` VALUES (4,'ROLE_EMPLOYEE','普通员工','系统基础用户，可进行个人业务操作','2025-10-28 01:09:06','2025-10-28 01:09:06'),(5,'ROLE_DEPT_MANAGER','部门主管','各部门负责人，负责审批本部门业务','2025-10-28 01:09:06','2025-10-28 01:09:06'),(6,'ROLE_VICE_LEADER','分管领导','分管多个部门的领导，负责重要业务审批','2025-10-28 01:09:06','2025-10-28 01:09:06'),(7,'ROLE_MAIN_LEADER','主要领导','单位最高领导，负责重大事项审批','2025-10-28 01:09:06','2025-10-28 01:09:06'),(8,'ROLE_ADMIN_OFFICE','行政办','行政管理部门，负责综合行政事务管理','2025-10-28 01:09:06','2025-10-28 01:09:06'),(9,'ROLE_LOGISTICS','后保部','后勤保障部门，负责物资管理和车辆管理','2025-10-28 01:09:06','2025-10-28 01:09:06'),(10,'ROLE_FLEET_MANAGER','车队负责人','车队管理人员，负责公车管理相关操作','2025-10-28 01:09:06','2025-10-28 01:09:06'),(11,'ROLE_IT_CENTER','信息中心','信息技术部门，负责电子耗材审批','2025-10-28 01:09:06','2025-10-28 01:09:06'),(12,'ROLE_QUALITY_CENTER','质量中心','质量管理部门，负责实验耗材审批','2025-10-28 01:09:06','2025-10-28 01:09:06'),(13,'ROLE_MANAGEMENT_DEPT','综合管理部','综合管理部门，负责合同会签','2025-10-28 01:09:06','2025-10-28 01:09:06'),(14,'ROLE_DEPT_SPECIAL','后保指定人员','指定的特殊权限人员，可进行耗材领用审批操作','2025-10-28 01:09:06','2025-10-28 01:09:06'),(15,'SUPER_ADMIN','超级管理员','超级管理员拥有所有权限','2025-10-28 01:14:14','2025-10-28 01:14:14'),(16,'SUPERVISION','日常监督员','执行日常监督流程','2025-12-07 16:17:17','2025-12-07 16:17:17'),(17,'SUPERVISION_MANAGER','日常监督管理员','管理日常监督情况','2025-12-07 16:17:17','2025-12-07 16:17:17');
/*!40000 ALTER TABLE `sys_role` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_role_permission`
--

DROP TABLE IF EXISTS `sys_role_permission`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_role_permission` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `role_id` int NOT NULL COMMENT '角色ID',
  `permission_id` int NOT NULL COMMENT '权限ID',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_permission` (`role_id`,`permission_id`)
) ENGINE=InnoDB AUTO_INCREMENT=188 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色权限关联表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_role_permission`
--

LOCK TABLES `sys_role_permission` WRITE;
/*!40000 ALTER TABLE `sys_role_permission` DISABLE KEYS */;
INSERT INTO `sys_role_permission` VALUES (11,5,6,'2025-10-28 01:10:17'),(12,14,6,'2025-10-28 01:10:17'),(14,10,6,'2025-10-28 01:10:17'),(15,11,6,'2025-10-28 01:10:17'),(16,9,6,'2025-10-28 01:10:17'),(17,7,6,'2025-10-28 01:10:17'),(18,13,6,'2025-10-28 01:10:17'),(19,12,6,'2025-10-28 01:10:17'),(20,6,6,'2025-10-28 01:10:17'),(32,5,45,'2025-10-28 01:11:12'),(33,5,12,'2025-10-28 01:11:12'),(34,5,16,'2025-10-28 01:11:12'),(35,5,10,'2025-10-28 01:11:12'),(36,5,32,'2025-10-28 01:11:12'),(39,6,45,'2025-10-28 01:11:12'),(40,6,13,'2025-10-28 01:11:12'),(41,6,16,'2025-10-28 01:11:12'),(42,6,10,'2025-10-28 01:11:12'),(43,6,22,'2025-10-28 01:11:12'),(44,6,21,'2025-10-28 01:11:12'),(46,7,46,'2025-10-28 01:11:12'),(47,7,15,'2025-10-28 01:11:12'),(48,7,14,'2025-10-28 01:11:12'),(49,7,17,'2025-10-28 01:11:12'),(50,7,11,'2025-10-28 01:11:12'),(51,7,22,'2025-10-28 01:11:12'),(52,7,21,'2025-10-28 01:11:12'),(68,9,35,'2025-10-28 01:11:12'),(69,9,38,'2025-10-28 01:11:12'),(70,9,39,'2025-10-28 01:11:12'),(71,9,36,'2025-10-28 01:11:12'),(72,9,37,'2025-10-28 01:11:12'),(73,9,23,'2025-10-28 01:11:12'),(74,9,22,'2025-10-28 01:11:12'),(75,9,21,'2025-10-28 01:11:12'),(83,10,19,'2025-10-28 01:11:12'),(84,10,22,'2025-10-28 01:11:12'),(85,10,20,'2025-10-28 01:11:12'),(86,10,21,'2025-10-28 01:11:12'),(90,11,33,'2025-10-28 01:11:12'),(91,12,34,'2025-10-28 01:11:12'),(92,13,43,'2025-10-28 01:11:12'),(93,13,45,'2025-10-28 01:11:12'),(95,14,31,'2025-10-28 01:11:12'),(96,15,43,'2025-10-28 01:15:54'),(97,15,47,'2025-10-28 01:15:54'),(98,15,44,'2025-10-28 01:15:54'),(99,15,42,'2025-10-28 01:15:54'),(100,15,41,'2025-10-28 01:15:54'),(101,15,40,'2025-10-28 01:15:54'),(102,15,46,'2025-10-28 01:15:54'),(103,15,45,'2025-10-28 01:15:54'),(104,15,7,'2025-10-28 01:15:54'),(105,15,13,'2025-10-28 01:15:54'),(106,15,12,'2025-10-28 01:15:54'),(107,15,15,'2025-10-28 01:15:54'),(108,15,14,'2025-10-28 01:15:54'),(109,15,8,'2025-10-28 01:15:54'),(110,15,18,'2025-10-28 01:15:54'),(111,15,17,'2025-10-28 01:15:54'),(112,15,16,'2025-10-28 01:15:54'),(113,15,11,'2025-10-28 01:15:54'),(114,15,10,'2025-10-28 01:15:54'),(115,15,9,'2025-10-28 01:15:54'),(116,15,28,'2025-10-28 01:15:54'),(117,15,30,'2025-10-28 01:15:54'),(118,15,26,'2025-10-28 01:15:54'),(119,15,27,'2025-10-28 01:15:54'),(120,15,29,'2025-10-28 01:15:54'),(121,15,25,'2025-10-28 01:15:54'),(122,15,31,'2025-10-28 01:15:54'),(123,15,32,'2025-10-28 01:15:54'),(124,15,35,'2025-10-28 01:15:54'),(125,15,33,'2025-10-28 01:15:54'),(126,15,34,'2025-10-28 01:15:54'),(127,15,38,'2025-10-28 01:15:54'),(128,15,39,'2025-10-28 01:15:54'),(129,15,36,'2025-10-28 01:15:54'),(130,15,37,'2025-10-28 01:15:54'),(131,15,6,'2025-10-28 01:15:54'),(132,15,23,'2025-10-28 01:15:54'),(133,15,24,'2025-10-28 01:15:54'),(134,15,19,'2025-10-28 01:15:54'),(135,15,22,'2025-10-28 01:15:54'),(136,15,20,'2025-10-28 01:15:54'),(137,15,21,'2025-10-28 01:15:54'),(138,4,6,'2025-11-29 19:40:38'),(139,4,7,'2025-11-29 19:40:38'),(140,4,8,'2025-11-29 19:40:38'),(141,4,9,'2025-11-29 19:40:38'),(142,4,30,'2025-11-29 19:40:38'),(143,4,25,'2025-11-29 19:40:38'),(144,4,31,'2025-11-29 19:40:38'),(163,8,6,'2025-12-02 00:20:51'),(164,8,47,'2025-12-02 00:20:51'),(165,8,44,'2025-12-02 00:20:51'),(166,8,42,'2025-12-02 00:20:51'),(167,8,46,'2025-12-02 00:20:51'),(168,8,13,'2025-12-02 00:20:51'),(169,8,12,'2025-12-02 00:20:51'),(170,8,18,'2025-12-02 00:20:51'),(171,8,16,'2025-12-02 00:20:51'),(172,8,11,'2025-12-02 00:20:51'),(173,8,28,'2025-12-02 00:20:51'),(174,8,26,'2025-12-02 00:20:51'),(175,8,27,'2025-12-02 00:20:51'),(176,8,29,'2025-12-02 00:20:51'),(177,8,40,'2025-12-02 00:20:51'),(178,8,41,'2025-12-02 00:20:51'),(179,8,43,'2025-12-02 00:20:51'),(180,8,45,'2025-12-02 00:20:51'),(181,8,17,'2025-12-02 00:20:51'),(182,8,25,'2025-12-02 00:20:51'),(183,8,32,'2025-12-02 00:20:51'),(184,8,31,'2025-12-02 00:20:51'),(185,16,48,'2025-12-07 16:18:09'),(186,17,49,'2025-12-07 16:18:09'),(187,17,48,'2025-12-07 16:18:09');
/*!40000 ALTER TABLE `sys_role_permission` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_user`
--

DROP TABLE IF EXISTS `sys_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_user` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户名',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密码',
  `nickname` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '昵称',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '邮箱',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '手机号',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '头像',
  `status` tinyint DEFAULT '1' COMMENT '状态(0:禁用,1:正常)',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `department_id` int DEFAULT NULL COMMENT '所属部门ID',
  `job_title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '职务',
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`),
  KEY `department_id` (`department_id`),
  CONSTRAINT `sys_user_ibfk_1` FOREIGN KEY (`department_id`) REFERENCES `sys_department` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=37 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_user`
--

LOCK TABLES `sys_user` WRITE;
/*!40000 ALTER TABLE `sys_user` DISABLE KEYS */;
INSERT INTO `sys_user` VALUES (19,'super_admin','$2a$10$Y8Gffp8uJ1oPScMlsVwE4OROwVP3xl.uMOxipMugrBZk2kQaMLIOq','系统超级管理员','super_admin@example.com','13800138000',NULL,1,'2025-10-28 01:25:23','2025-11-30 16:37:38',1,'系统管理员'),(20,'employee001','$2a$10$yTje9GQkwT3o8xjlXGgtm..xAyeB7cTorxflm6EsY8rHhhSMrzvKK','张员工','employee001@example.com','13800138001',NULL,1,'2025-10-28 01:25:23','2025-12-14 19:20:03',2,'职员'),(21,'dept_manager','$2a$10$0vhAcjktjOg1aOTX.HPyIOC5KciQOMXBxc4mRq8G.z1vet.Hbyf3S','李主管','dept_manager@example.com','13800138002',NULL,1,'2025-10-28 01:25:23','2025-10-28 17:45:10',1,'部门经理'),(22,'vice_leader','$2a$10$cpn/TFt.KfH2EqZpRuyjGu9qDX.taePNG6BI003aFZvMfplp5mSYe','王副总','vice_leader@example.com','13800138003',NULL,1,'2025-10-28 01:25:23','2025-10-28 17:45:10',NULL,'副总经理'),(23,'main_leader','$2a$10$eqfTLYwnzNWZvR4p35sNju/Yz0XV3kwfpeX.eCONY3F.mzmNbYQOq','赵总裁','main_leader@example.com','13800138004',NULL,1,'2025-10-28 01:25:23','2025-10-28 17:45:10',NULL,'总经理'),(24,'admin_office','$2a$10$4civ0kBqsoNkT9Zq12cMLO5G1F5LU9IJ.wfXMTUy7DcZRZk8HhNYS','行政办张主任','admin_office@example.com','13800138005',NULL,1,'2025-10-28 01:25:23','2025-10-28 17:45:10',2,'行政主任'),(25,'logistics','$2a$10$./KJVUV54BCVHhvZmMDFsOIvLcFWp8V3f/08f7KQxmNEqFYpm6Ia2','后勤部李部长','logistics@example.com','13800138006',NULL,1,'2025-10-28 01:25:23','2025-10-28 17:45:10',3,'后勤部长'),(26,'fleet_manager','123','车队王队长','fleet_manager@example.com','13800138007',NULL,1,'2025-10-28 01:25:23','2025-10-28 17:45:10',3,'车队队长'),(27,'it_center','$2a$10$0XIu0NYSRzBsojSLAfZOHOs.e8wABopjmfu/BLxIoygug24j8fNjy','信息中心刘工','it_center@example.com','13800138008',NULL,1,'2025-10-28 01:25:23','2025-10-28 17:45:10',4,'技术主管'),(28,'quality_center','$2a$10$/04jlFa7JqDKCevpANpG6.SO5sRkgfjqviTH5ndIdLwrYkYrOVFau','质检部陈工','quality_center@example.com','13800138009',NULL,1,'2025-10-28 01:25:23','2025-10-28 17:45:10',5,'质量主管'),(29,'management_dept','123','综合部周主任','management_dept@example.com','13800138010',NULL,1,'2025-10-28 01:25:23','2025-10-28 17:45:10',6,'综合主任'),(30,'dept_special','123','部门专员钱工','dept_special@example.com','13800138011',NULL,1,'2025-10-28 01:25:23','2025-10-28 17:45:10',1,'行政专员');
/*!40000 ALTER TABLE `sys_user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_user_role`
--

DROP TABLE IF EXISTS `sys_user_role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_user_role` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` int NOT NULL COMMENT '用户ID',
  `role_id` int NOT NULL COMMENT '角色ID',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_role` (`user_id`,`role_id`)
) ENGINE=InnoDB AUTO_INCREMENT=62 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户角色关联表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_user_role`
--

LOCK TABLES `sys_user_role` WRITE;
/*!40000 ALTER TABLE `sys_user_role` DISABLE KEYS */;
INSERT INTO `sys_user_role` VALUES (5,20,4,'2025-10-28 01:30:10'),(6,21,5,'2025-10-28 01:30:10'),(7,22,6,'2025-10-28 01:30:10'),(8,23,7,'2025-10-28 01:30:10'),(9,24,8,'2025-10-28 01:30:10'),(15,30,14,'2025-10-28 01:30:10'),(16,21,4,'2025-10-28 01:30:30'),(17,22,4,'2025-10-28 01:30:30'),(18,23,4,'2025-10-28 01:30:30'),(19,24,4,'2025-10-28 01:30:30'),(20,30,4,'2025-10-28 01:30:30'),(23,25,9,'2025-11-30 16:36:19'),(24,25,4,'2025-11-30 16:36:19'),(25,19,15,'2025-11-30 16:37:38'),(26,19,4,'2025-11-30 16:37:38'),(27,19,5,'2025-11-30 16:37:38'),(28,19,6,'2025-11-30 16:37:38'),(29,19,7,'2025-11-30 16:37:38'),(30,19,8,'2025-11-30 16:37:38'),(31,19,9,'2025-11-30 16:37:38'),(32,19,10,'2025-11-30 16:37:38'),(33,19,11,'2025-11-30 16:37:38'),(34,19,12,'2025-11-30 16:37:38'),(35,19,13,'2025-11-30 16:37:38'),(36,19,14,'2025-11-30 16:37:38'),(37,26,10,'2025-11-30 16:40:12'),(38,26,4,'2025-11-30 16:40:12'),(39,27,11,'2025-11-30 16:40:17'),(40,27,4,'2025-11-30 16:40:17'),(43,29,13,'2025-11-30 16:40:30'),(44,29,4,'2025-11-30 16:40:30'),(49,20,16,'2025-12-07 16:19:45'),(50,31,16,'2025-12-07 16:19:45'),(51,28,17,'2025-12-10 00:06:09'),(52,28,12,'2025-12-10 00:06:09'),(53,28,4,'2025-12-10 00:06:09'),(54,28,8,'2025-12-10 00:06:09'),(55,28,9,'2025-12-10 00:06:09'),(56,28,10,'2025-12-10 00:06:09'),(57,28,11,'2025-12-10 00:06:09'),(58,28,13,'2025-12-10 00:06:09'),(59,28,14,'2025-12-10 00:06:09'),(60,28,15,'2025-12-10 00:06:09'),(61,28,16,'2025-12-10 00:06:09');
/*!40000 ALTER TABLE `sys_user_role` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `uploads`
--

DROP TABLE IF EXISTS `uploads`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `uploads` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `mime_type` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `size` bigint DEFAULT NULL,
  `storage_path` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `origin` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `origin_id` bigint DEFAULT NULL,
  `meta` json DEFAULT NULL,
  `created_by` bigint DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_origin` (`origin`,`origin_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `uploads`
--

LOCK TABLES `uploads` WRITE;
/*!40000 ALTER TABLE `uploads` DISABLE KEYS */;
/*!40000 ALTER TABLE `uploads` ENABLE KEYS */;
UNLOCK TABLES;

SET FOREIGN_KEY_CHECKS = 1;
