-- Migration: add flowable_task_id to oa_supervision_assignment
-- Generated: 2025-11-21

SET FOREIGN_KEY_CHECKS=0;

-- 兼容性：有些 MySQL 版本不支持 ALTER TABLE ... ADD COLUMN IF NOT EXISTS
-- 我们使用 INFORMATION_SCHEMA 检查列是否存在，若不存在则动态执行 ALTER TABLE
SET @schema_name = DATABASE();
SELECT COUNT(*) INTO @col_exists FROM INFORMATION_SCHEMA.COLUMNS
 WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'oa_supervision_assignment' AND COLUMN_NAME = 'flowable_task_id';
SET @sql = IF(@col_exists = 0,
  'ALTER TABLE `oa_supervision_assignment` ADD COLUMN `flowable_task_id` VARCHAR(64) DEFAULT NULL COMMENT ''Flowable task id'';',
  'SELECT 1;'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 对索引也使用检查：若不存在则添加
SELECT COUNT(*) INTO @idx_exists FROM INFORMATION_SCHEMA.STATISTICS
 WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'oa_supervision_assignment' AND INDEX_NAME = 'idx_assignment_flowable_task_id';
SET @sql = IF(@idx_exists = 0,
  'ALTER TABLE `oa_supervision_assignment` ADD INDEX idx_assignment_flowable_task_id (flowable_task_id);',
  'SELECT 1;'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET FOREIGN_KEY_CHECKS=1;

-- End of migration
