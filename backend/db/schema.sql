-- ============================================================
-- Jira 后端重构 —— 建表脚本（阶段 0 产出）
-- 数据库：MySQL 8.0+      字符集：utf8mb4
-- 依据：《后端重构调研文档》第四章 4.3
--
-- 执行方式（任选其一）：
--   1) MySQL Workbench：打开本文件 → 执行（闪电图标）
--   2) 命令行：mysql -u root -p < schema.sql
--
-- 说明：本脚本可重复执行（会先删表再建表）。
--       生产环境请勿直接执行 DROP。
-- ============================================================

CREATE DATABASE IF NOT EXISTS jira
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_0900_ai_ci;

USE jira;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 按依赖倒序删除，保证脚本可重复执行
DROP TABLE IF EXISTS `task`;
DROP TABLE IF EXISTS `kanban`;
DROP TABLE IF EXISTS `epic`;
DROP TABLE IF EXISTS `task_type`;
DROP TABLE IF EXISTS `project_member`;
DROP TABLE IF EXISTS `project`;
DROP TABLE IF EXISTS `sys_user`;

SET FOREIGN_KEY_CHECKS = 1;

-- ------------------------------------------------------------
-- 1. 用户
-- ------------------------------------------------------------
CREATE TABLE `sys_user` (
  `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `username`      VARCHAR(50)  NOT NULL                COMMENT '登录名（唯一）',
  `password_hash` VARCHAR(100) NOT NULL                COMMENT '密码哈希（BCrypt），禁止存明文',
  `name`          VARCHAR(50)  NOT NULL                COMMENT '显示名',
  `email`         VARCHAR(100) DEFAULT NULL            COMMENT '邮箱',
  `title`         VARCHAR(50)  DEFAULT NULL            COMMENT '职位',
  `organization`  VARCHAR(50)  DEFAULT NULL            COMMENT '部门',
  `created_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sys_user_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户';

-- ------------------------------------------------------------
-- 2. 项目
-- ------------------------------------------------------------
CREATE TABLE `project` (
  `id`           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name`         VARCHAR(100) NOT NULL                COMMENT '项目名',
  `person_id`    BIGINT       DEFAULT NULL            COMMENT '负责人（sys_user.id）',
  `organization` VARCHAR(50)  DEFAULT NULL            COMMENT '所属部门',
  `created`      BIGINT       NOT NULL                COMMENT '创建时间（毫秒时间戳，与前端契约一致）',
  `pin`          TINYINT(1)   NOT NULL DEFAULT 0      COMMENT '是否收藏：0 否 / 1 是',
  PRIMARY KEY (`id`),
  KEY `idx_project_person` (`person_id`),
  CONSTRAINT `fk_project_person` FOREIGN KEY (`person_id`)
    REFERENCES `sys_user` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目';

-- ------------------------------------------------------------
-- 3. 项目成员（用户隔离的判定依据）
-- ------------------------------------------------------------
CREATE TABLE `project_member` (
  `id`         BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键',
  `project_id` BIGINT   NOT NULL                COMMENT '项目',
  `user_id`    BIGINT   NOT NULL                COMMENT '成员',
  `joined_at`  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_project_user` (`project_id`, `user_id`),
  KEY `idx_pm_user` (`user_id`),
  CONSTRAINT `fk_pm_project` FOREIGN KEY (`project_id`)
    REFERENCES `project` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_pm_user` FOREIGN KEY (`user_id`)
    REFERENCES `sys_user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目成员';

-- ------------------------------------------------------------
-- 4. 看板列
-- ------------------------------------------------------------
CREATE TABLE `kanban` (
  `id`         BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name`       VARCHAR(50) NOT NULL                COMMENT '列名',
  `project_id` BIGINT      NOT NULL                COMMENT '所属项目',
  `sort_order` INT         NOT NULL DEFAULT 0      COMMENT '列排序（拖拽用，从 1 开始）',
  PRIMARY KEY (`id`),
  KEY `idx_kanban_project` (`project_id`, `sort_order`),
  CONSTRAINT `fk_kanban_project` FOREIGN KEY (`project_id`)
    REFERENCES `project` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='看板列';

-- ------------------------------------------------------------
-- 5. 任务组（Epic）
-- ------------------------------------------------------------
CREATE TABLE `epic` (
  `id`         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name`       VARCHAR(100) NOT NULL                COMMENT '任务组名',
  `project_id` BIGINT       NOT NULL                COMMENT '所属项目',
  `start_time` BIGINT       DEFAULT NULL            COMMENT '开始时间（毫秒时间戳）',
  `end_time`   BIGINT       DEFAULT NULL            COMMENT '结束时间（毫秒时间戳）',
  PRIMARY KEY (`id`),
  KEY `idx_epic_project` (`project_id`),
  CONSTRAINT `fk_epic_project` FOREIGN KEY (`project_id`)
    REFERENCES `project` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务组';

-- ------------------------------------------------------------
-- 6. 任务类型（字典表：id 0/1/2 被前端逻辑依赖，故不设自增）
-- ------------------------------------------------------------
CREATE TABLE `task_type` (
  `id`   BIGINT      NOT NULL COMMENT '主键（0=类型 1=Bug 2=Task）',
  `name` VARCHAR(50) NOT NULL COMMENT '类型名',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务类型';

-- ------------------------------------------------------------
-- 7. 任务
-- ------------------------------------------------------------
CREATE TABLE `task` (
  `id`           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name`         VARCHAR(200) NOT NULL                COMMENT '任务名',
  `project_id`   BIGINT       NOT NULL                COMMENT '所属项目',
  `kanban_id`    BIGINT       NOT NULL                COMMENT '所属看板列',
  `epic_id`      BIGINT       DEFAULT NULL            COMMENT '所属任务组（可空）',
  `type_id`      BIGINT       DEFAULT NULL            COMMENT '任务类型',
  `processor_id` BIGINT       DEFAULT NULL            COMMENT '经办人（可空）',
  `note`         TEXT                                 COMMENT '备注',
  `sort_order`   INT          NOT NULL DEFAULT 0      COMMENT '列内排序（拖拽用）',
  PRIMARY KEY (`id`),
  KEY `idx_task_project`   (`project_id`),
  KEY `idx_task_kanban`    (`kanban_id`, `sort_order`),
  KEY `idx_task_epic`      (`epic_id`),
  KEY `idx_task_processor` (`processor_id`),
  CONSTRAINT `fk_task_project` FOREIGN KEY (`project_id`)
    REFERENCES `project` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_task_kanban` FOREIGN KEY (`kanban_id`)
    REFERENCES `kanban` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_task_epic` FOREIGN KEY (`epic_id`)
    REFERENCES `epic` (`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_task_type` FOREIGN KEY (`type_id`)
    REFERENCES `task_type` (`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_task_processor` FOREIGN KEY (`processor_id`)
    REFERENCES `sys_user` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务';

-- ============================================================
-- 级联删除策略（与调研文档一致）：
--   删项目  -> 级联删除 看板 / 任务组 / 任务 / 项目成员
--   删看板  -> 级联删除 列内任务
--   删任务组 -> 任务保留，仅将 task.epic_id 置空
--   删用户  -> 项目负责人 / 任务经办人 置空，项目成员关系删除
-- ============================================================
