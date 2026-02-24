-- Migration: Create supervision tables (tasks, assignments, feedbacks and feedback_upload join)
-- Run this script on your MySQL database. Backup DB before running in production.
-- Generated: 2025-11-19

SET FOREIGN_KEY_CHECKS=0;

-- oa_supervision_task: 主任务表（重命名为带 oa_ 前缀）
CREATE TABLE IF NOT EXISTS oa_supervision_task (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  title VARCHAR(255) NOT NULL,
  description TEXT,
  created_by BIGINT NULL,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  due_date DATE NULL,
  status VARCHAR(32) NOT NULL DEFAULT 'open', -- open/in_progress/completed/overdue/cancelled
  progress INT DEFAULT 0,
  last_update DATETIME NULL,
  -- 可选：关联某条行政公示（非必须）
  notice_id BIGINT NULL,
  INDEX idx_supervision_notice_id (notice_id),
  INDEX idx_supervision_status (status)
);

-- oa_supervision_assignment: 分配给部门或用户的记录（重命名为带 oa_ 前缀）
CREATE TABLE IF NOT EXISTS oa_supervision_assignment (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  task_id BIGINT NOT NULL,
  assignee_user_id BIGINT NULL,
  assignee_dept_id BIGINT NULL,
  assigned_by BIGINT NULL,
  assigned_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  status VARCHAR(32) DEFAULT 'pending', -- pending/accepted/declined/completed
  completed_at DATETIME NULL,
  INDEX idx_assignment_task_id (task_id),
  INDEX idx_assignment_user (assignee_user_id)
);

-- oa_supervision_feedback: 办结反馈记录（重命名为带 oa_ 前缀）
CREATE TABLE IF NOT EXISTS oa_supervision_feedback (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  task_id BIGINT NOT NULL,
  assignment_id BIGINT NULL,
  feedback_by BIGINT NULL,
  feedback_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  finish_date DATE NULL,
  remarks TEXT,
  INDEX idx_feedback_task_id (task_id),
  INDEX idx_feedback_assignment_id (assignment_id)
);

-- 可选中间表：把 feedback 与 uploads 关联（如果不使用 origin/origin_id 更新策略）
CREATE TABLE IF NOT EXISTS supervision_feedback_upload (
  feedback_id BIGINT NOT NULL,
  upload_id BIGINT NOT NULL,
  PRIMARY KEY (feedback_id, upload_id),
  INDEX idx_sfu_feedback (feedback_id),
  INDEX idx_sfu_upload (upload_id)
);

-- 外键约束（尽量在有外键支持的环境启用）
-- 注意：在某些生产环境中外键可能被禁用或表顺序导致约束失败，请根据环境调整或在部署步骤中单独添加外键
ALTER TABLE oa_supervision_task
  ADD CONSTRAINT fk_oa_supervision_task_notice
    FOREIGN KEY (notice_id) REFERENCES administrative_notice(id) ON DELETE SET NULL ON UPDATE CASCADE;

ALTER TABLE oa_supervision_assignment
  ADD CONSTRAINT fk_oa_assignment_task
    FOREIGN KEY (task_id) REFERENCES oa_supervision_task(id) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE oa_supervision_feedback
  ADD CONSTRAINT fk_oa_feedback_task
    FOREIGN KEY (task_id) REFERENCES oa_supervision_task(id) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE oa_supervision_feedback
  ADD CONSTRAINT fk_oa_feedback_assignment
    FOREIGN KEY (assignment_id) REFERENCES oa_supervision_assignment(id) ON DELETE SET NULL ON UPDATE CASCADE;

ALTER TABLE supervision_feedback_upload
  ADD CONSTRAINT fk_oa_sfu_feedback
    FOREIGN KEY (feedback_id) REFERENCES oa_supervision_feedback(id) ON DELETE CASCADE ON UPDATE CASCADE;

-- 假设 uploads 表已存在
ALTER TABLE supervision_feedback_upload
  ADD CONSTRAINT fk_oa_sfu_upload
    FOREIGN KEY (upload_id) REFERENCES uploads(id) ON DELETE CASCADE ON UPDATE CASCADE;

SET FOREIGN_KEY_CHECKS=1;

-- End of migration
