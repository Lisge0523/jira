-- ============================================================
-- Jira 后端重构 —— 种子数据（阶段 0 产出）
-- 依赖：先执行 schema.sql
-- 说明：由 Mock 的 db.json 清洗而来，仅保留必要的演示数据
--
-- 测试账号（密码统一为 123456）：
--   gaoxiuwen       高修文     —— 是全部 5 个项目的成员（能看到所有演示数据）
--   xiongtiancheng  熊天成     —— 项目 1、2 的成员
--   zhenghua        郑华       —— 项目 4 的成员
--   wangwenjing     王文静     —— 项目 5 的成员
--
-- 可用于验证「用户隔离」：用 wangwenjing 登录，只应看到「送餐路线规划系统」。
-- ============================================================

USE jira;
SET NAMES utf8mb4;

-- Workbench 默认开启 "Safe Updates"，会拦截不带 WHERE 的 DELETE/UPDATE。
-- 这里显式关闭，保证脚本在 Workbench 中能完整执行（只影响当前会话）。
SET SQL_SAFE_UPDATES = 0;

-- 清空旧数据（按依赖倒序）
DELETE FROM `task`;
DELETE FROM `kanban`;
DELETE FROM `epic`;
DELETE FROM `task_type`;
DELETE FROM `project_member`;
DELETE FROM `project`;
DELETE FROM `sys_user`;

-- ------------------------------------------------------------
-- 用户（password_hash 为 BCrypt 哈希，明文密码 = 123456）
-- ------------------------------------------------------------
INSERT INTO `sys_user` (`id`, `username`, `password_hash`, `name`, `email`, `title`, `organization`) VALUES
(1, 'gaoxiuwen',      '$2b$10$YnYN4XlDPpjK.JXXrSSlx.naXeaH37uLqddpYsFIxZ/Q/47owbCEy', '高修文', '', '', '外卖组'),
(2, 'xiongtiancheng', '$2b$10$YnYN4XlDPpjK.JXXrSSlx.naXeaH37uLqddpYsFIxZ/Q/47owbCEy', '熊天成', '', '', '团购组'),
(3, 'zhenghua',       '$2b$10$YnYN4XlDPpjK.JXXrSSlx.naXeaH37uLqddpYsFIxZ/Q/47owbCEy', '郑华',   '', '', '总部'),
(4, 'wangwenjing',    '$2b$10$YnYN4XlDPpjK.JXXrSSlx.naXeaH37uLqddpYsFIxZ/Q/47owbCEy', '王文静', '', '', '外卖组');

-- ------------------------------------------------------------
-- 项目
-- ------------------------------------------------------------
INSERT INTO `project` (`id`, `name`, `person_id`, `organization`, `created`, `pin`) VALUES
(1, '骑手管理',         1, '外卖组', 1604989757139, 1),
(2, '团购 APP',         2, '团购组', 1604989757139, 1),
(3, '物料管理系统',     2, '物料组', 1546300800000, 1),
(4, '总部管理系统',     3, '总部',   1604980000011, 1),
(5, '送餐路线规划系统', 4, '外卖组', 1546900800000, 1);

-- ------------------------------------------------------------
-- 项目成员（决定「谁能看到哪个项目」）
-- ------------------------------------------------------------
INSERT INTO `project_member` (`project_id`, `user_id`) VALUES
(1, 1), (2, 1), (3, 1), (4, 1), (5, 1),   -- 高修文：全部
(1, 2), (2, 2),                            -- 熊天成：项目 1、2
(4, 3),                                    -- 郑华：项目 4
(5, 4);                                    -- 王文静：项目 5

-- ------------------------------------------------------------
-- 看板列
-- ------------------------------------------------------------
INSERT INTO `kanban` (`id`, `name`, `project_id`, `sort_order`) VALUES
(1, '待办',   1, 1),
(2, '进行中', 1, 2),
(3, '已完成', 1, 3),
(4, '待办',   2, 1);

-- ------------------------------------------------------------
-- 任务组（补上了 Mock 缺失的 start/end，修复页面 Invalid Date）
-- ------------------------------------------------------------
INSERT INTO `epic` (`id`, `name`, `project_id`, `start_time`, `end_time`) VALUES
(1, '骑手管理-一期',    1, 1609459200000, 1614556800000),
(2, '团购 APP-首页改版', 2, 1609459200000, 1612137600000);

-- ------------------------------------------------------------
-- 任务类型（id 0/1/2 被前端图标逻辑依赖，不可改）
-- ------------------------------------------------------------
INSERT INTO `task_type` (`id`, `name`) VALUES
(0, '类型'),
(1, 'Bug'),
(2, 'Task');

-- ------------------------------------------------------------
-- 任务
-- ------------------------------------------------------------
INSERT INTO `task` (`id`, `name`, `project_id`, `kanban_id`, `epic_id`, `type_id`, `processor_id`, `note`, `sort_order`) VALUES
(1, '任务1',     1, 1, 1,    2, 1,    NULL, 1),
(2, '任务2',     1, 1, 1,    2, 2,    NULL, 2),
(3, 'Bug测试',   1, 2, NULL, 1, NULL, NULL, 1),
(4, '222',       1, 2, NULL, 2, NULL, NULL, 2),
(5, '可以可以',  1, 3, NULL, 1, NULL, NULL, 1),
(6, '首页原型',  2, 4, 2,    2, 2,    NULL, 1);
