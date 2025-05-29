# LLM评测平台后端

基于Spring Boot的大语言模型评测平台后端，提供问题管理、标准答案管理、评测批次管理和数据统计等功能。

## 项目概述

本项目是一个用于评测大语言模型能力的平台后端，具有以下主要功能：

- 标准问题和答案管理
- 问题分类和标签管理
- 数据集版本管理
- LLM模型管理
- 评测批次和结果管理
- 众包任务和答案管理
- 统计分析

## 技术栈

- Java 17
- Spring Boot 3.2.3
- Spring Data JPA
- MySQL 8.0
- Swagger/OpenAPI 3.0
- Gradle 8.x

## 项目结构

```
src/main/java/com/llm/eval/
├── config/               # 配置类
├── controller/           # 控制器层
├── model/                # 模型层（实体类）
├── repository/           # 数据库访问层
├── service/              # 服务层
│   └── impl/             # 服务实现类
├── dto/                  # 数据传输对象
├── exception/            # 自定义异常
└── LlmEvalApplication.java  # 主程序入口
```

## API概览

### 1. 标签管理

- `GET /api/tags` - 获取所有标签
- `GET /api/tags/{id}` - 获取特定标签
- `POST /api/tags` - 创建新标签
- `PUT /api/tags/{id}` - 更新标签
- `DELETE /api/tags/{id}` - 删除标签
- `GET /api/tags/question/{questionId}` - 获取问题的标签
- `POST /api/tags/question/{questionId}` - 为问题添加标签
- `DELETE /api/tags/question/{questionId}/{tagId}` - 移除问题的标签

### 2. 分类管理

- `GET /api/categories` - 获取所有分类
- `GET /api/categories/{id}` - 获取特定分类
- `GET /api/categories/root` - 获取根分类
- `GET /api/categories/{id}/children` - 获取子分类
- `POST /api/categories` - 创建新分类
- `PUT /api/categories/{id}` - 更新分类
- `DELETE /api/categories/{id}` - 删除分类
- `GET /api/categories/with-count` - 获取带问题数量的分类列表

### 3. 标准问题管理

- `GET /api/questions` - 获取所有标准问题
- `GET /api/questions/{id}` - 获取特定标准问题
- `GET /api/questions/category/{categoryId}` - 获取特定分类下的问题
- `GET /api/questions/tag/{tagId}` - 获取特定标签下的问题
- `GET /api/questions/without-answer` - 获取无标准答案的问题
- `POST /api/questions` - 创建新标准问题
- `PUT /api/questions/{id}` - 更新标准问题
- `DELETE /api/questions/{id}` - 删除标准问题

### 4. 标准答案管理

- `GET /api/answers` - 获取所有标准答案
- `GET /api/answers/{id}` - 获取特定标准答案
- `GET /api/answers/question/{questionId}` - 获取问题的所有标准答案
- `GET /api/answers/question/{questionId}/final` - 获取问题的最终标准答案
- `POST /api/answers` - 创建新标准答案
- `PUT /api/answers/{id}` - 更新标准答案
- `DELETE /api/answers/{id}` - 删除标准答案
- `POST /api/answers/{id}/set-final` - 设置为最终标准答案
- `POST /api/answers/{id}/key-points` - 添加关键点
- `GET /api/answers/{id}/key-points` - 获取答案的所有关键点
- `PUT /api/answers/{id}/key-points/{keyPointId}` - 更新关键点
- `DELETE /api/answers/{id}/key-points/{keyPointId}` - 删除关键点

### 5. 数据集版本管理

- `GET /api/datasets` - 获取所有数据集版本
- `GET /api/datasets/{id}` - 获取特定数据集版本
- `GET /api/datasets/published` - 获取已发布的数据集版本
- `GET /api/datasets/{id}/questions` - 获取数据集版本中的问题
- `POST /api/datasets` - 创建新数据集版本
- `PUT /api/datasets/{id}` - 更新数据集版本
- `DELETE /api/datasets/{id}` - 删除数据集版本
- `POST /api/datasets/{id}/questions` - 添加问题到数据集版本
- `DELETE /api/datasets/{id}/questions` - 从数据集版本中移除问题
- `POST /api/datasets/{id}/publish` - 发布数据集版本

### 6. LLM模型管理

- `GET /api/models` - 获取所有模型
- `GET /api/models/{id}` - 获取特定模型
- `POST /api/models` - 创建新模型
- `PUT /api/models/{id}` - 更新模型
- `DELETE /api/models/{id}` - 删除模型

### 7. 评测批次管理

- `GET /api/batches` - 获取所有评测批次
- `GET /api/batches/{id}` - 获取特定评测批次
- `GET /api/batches/model/{modelId}` - 获取特定模型的评测批次
- `GET /api/batches/dataset/{versionId}` - 获取特定数据集版本的评测批次
- `POST /api/batches` - 创建新评测批次
- `PUT /api/batches/{id}` - 更新评测批次
- `DELETE /api/batches/{id}` - 删除评测批次
- `POST /api/batches/{id}/start` - 开始评测批次
- `POST /api/batches/{id}/complete` - 完成评测批次

### 8. 评测结果管理

- `GET /api/evaluations` - 获取所有评测结果
- `GET /api/evaluations/{id}` - 获取特定评测结果
- `GET /api/evaluations/answer/{answerId}` - 获取特定回答的评测结果
- `GET /api/evaluations/batch/{batchId}` - 获取特定批次的评测结果
- `POST /api/evaluations` - 创建评测结果
- `PUT /api/evaluations/{id}` - 更新评测结果
- `DELETE /api/evaluations/{id}` - 删除评测结果
- `GET /api/evaluations/batch/{batchId}/average` - 获取批次的平均评分
- `GET /api/evaluations/batch/{batchId}/passing` - 获取通过评测的回答数量
- `POST /api/evaluations/batch/{batchId}/run` - 执行批次评测

### 9. 众包任务管理

- `GET /api/crowdsourcing/tasks` - 获取所有众包任务
- `GET /api/crowdsourcing/tasks/{id}` - 获取特定众包任务
- `POST /api/crowdsourcing/tasks` - 创建新众包任务
- `PUT /api/crowdsourcing/tasks/{id}` - 更新众包任务
- `DELETE /api/crowdsourcing/tasks/{id}` - 删除众包任务
- `POST /api/crowdsourcing/tasks/{id}/publish` - 发布众包任务
- `POST /api/crowdsourcing/tasks/{id}/complete` - 完成众包任务
- `GET /api/crowdsourcing/answers` - 获取所有众包答案
- `GET /api/crowdsourcing/answers/{id}` - 获取特定众包答案
- `GET /api/crowdsourcing/tasks/{taskId}/answers` - 获取特定任务的所有答案
- `POST /api/crowdsourcing/tasks/{taskId}/answers` - 提交众包答案
- `PUT /api/crowdsourcing/answers/{id}` - 更新众包答案
- `DELETE /api/crowdsourcing/answers/{id}` - 删除众包答案
- `POST /api/crowdsourcing/answers/{id}/review` - 审核众包答案
- `POST /api/crowdsourcing/answers/{id}/promote` - 将众包答案提升为标准答案

### 10. 统计数据管理

- `GET /api/stats/raw-counts` - 获取原始问题和答案数量
- `GET /api/stats/standard-counts` - 获取标准问题和答案数量
- `GET /api/stats/category-counts` - 获取各分类的问题数量
- `GET /api/stats/tag-counts` - 获取各标签的问题数量
- `GET /api/stats/without-standard-answer` - 获取无标准答案的问题数量
- `GET /api/stats/dataset-versions` - 获取数据集版本数量
- `GET /api/stats/summary` - 获取统计数据摘要

## 如何运行

### 前提条件

- JDK 17+
- MySQL 8.0+
- Gradle 8.x

### 数据库配置

1. 创建MySQL数据库：

```sql
CREATE DATABASE llm_eval;
CREATE USER 'llm_user'@'localhost' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON llm_eval.* TO 'llm_user'@'localhost';
FLUSH PRIVILEGES;
```

2. 配置数据库连接：

编辑 `src/main/resources/application.properties` 文件：

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/llm_eval?useSSL=false&serverTimezone=UTC
spring.datasource.username=llm_user
spring.datasource.password=your_password
spring.jpa.hibernate.ddl-auto=update
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect
```

### 构建和运行

```bash
# 克隆仓库
git clone https://github.com/yourusername/llm-eval-platform.git
cd llm-eval-platform/backend

# 构建项目
./gradlew build -x test

# 运行项目
./gradlew bootRun
```

服务将在 `http://localhost:8080` 启动。

## API文档

项目集成了Swagger/OpenAPI，运行项目后可以通过以下URL访问API文档：

```
http://localhost:8080/swagger-ui/index.html
```

## 项目依赖

项目主要依赖如下：

- Spring Boot Starter Web
- Spring Boot Starter Data JPA
- Spring Boot Starter Validation
- SpringDoc OpenAPI UI
- MySQL Connector
- Lombok

## 许可证

本项目采用MIT许可证。 