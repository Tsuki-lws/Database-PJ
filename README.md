# LLM评估平台数据库设置指南

## 问题描述
使用默认字符集创建MySQL数据库时，中文注释（如 `COMMENT '来源平台的问题ID'`）可能会显示为乱码。
此外，原始schema.sql文件中存在表创建顺序问题，导致出现 `Failed to open the referenced table 'users'` 错误。

## 解决方案
需要使用支持中文的字符集（utf8mb4）创建数据库，并按正确的表创建顺序执行脚本。

### 方法一：使用fixed-schema-order.sql脚本（推荐）
1. 在MySQL中执行以下命令：
```sql
source /path/to/llm-eval-platform/backend/src/main/resources/init-db.sql
source /path/to/llm-eval-platform/backend/src/main/resources/fixed-schema-order.sql
```

### 方法二：手动创建数据库并导入修复的schema
1. 创建数据库并设置字符集：
```sql
CREATE DATABASE llm_eval_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. 使用数据库：
```sql
USE llm_eval_db;
```

3. 设置连接字符集：
```sql
SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;
```

4. 导入修复后的schema.sql：
```sql
source /path/to/llm-eval-platform/backend/src/main/resources/fixed-schema-order.sql
```

### 方法三：命令行一次性执行
```bash
mysql -u username -p --default-character-set=utf8mb4 -e "CREATE DATABASE IF NOT EXISTS llm_eval_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
mysql -u username -p --default-character-set=utf8mb4 llm_eval_db < /path/to/llm-eval-platform/backend/src/main/resources/fixed-schema-order.sql
```

## 表创建顺序问题说明
原始schema.sql中的错误是由于表创建顺序不正确导致的，主要问题是：
1. `users` 表被多个表引用，但创建顺序靠后
2. `crowdsourcing_tasks` 和 `crowdsourced_answers` 之间存在循环引用

fixed-schema-order.sql文件解决了这些问题，通过合理安排表的创建顺序：
1. 首先创建基础表（如users表）
2. 然后创建依赖这些基础表的表
3. 最后创建有复杂依赖关系的表

## 验证字符集设置
使用以下命令确认数据库字符集设置：
```sql
SHOW VARIABLES LIKE 'character_set%';
SHOW CREATE DATABASE llm_eval_db;
```

正确设置后，数据库的字符集应为utf8mb4，中文注释将正常显示。 