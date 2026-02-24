-- Migration: add process_instance_id to oa_supervision_task (safe/check)
-- Generated: 2025-11-23

SET @schema_name = (SELECT DATABASE());
SELECT COUNT(1) INTO @col_exists FROM information_schema.COLUMNS
 WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'oa_supervision_task' AND COLUMN_NAME = 'process_instance_id';

-- 使用表达式 IF(...) 生成要执行的 SQL，避免在非存储过程上下文使用 IF ... THEN
SET @sql = IF(@col_exists = 0,
  'ALTER TABLE `oa_supervision_task` ADD COLUMN `process_instance_id` VARCHAR(128) DEFAULT NULL COMMENT ''Flowable process instance id'';',
  'SELECT 1;'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(@col_exists = 0,
  'ALTER TABLE `oa_supervision_task` ADD INDEX idx_task_process_instance_id (process_instance_id);',
  'SELECT 1;'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- end
