-- 创建数据库并设置字符集为utf8mb4以支持中文
CREATE DATABASE IF NOT EXISTS llm_eval_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 使用创建的数据库
USE llm_eval_db;

-- 确保连接也使用utf8mb4字符集
SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

-- 注意：请按照以下顺序执行schema文件
-- 1. 首先创建没有外键依赖的基础表：raw_questions, question_categories, tags, users等
-- 2. 然后创建依赖这些基础表的表：standard_questions, crowdsourcing_tasks等
-- 3. 最后创建有复杂依赖关系的表和索引

-- 为解决循环引用问题，请使用fixed-schema-order.sql文件来创建表
-- source /path/to/llm-eval-platform/backend/src/main/resources/fixed-schema-order.sql 