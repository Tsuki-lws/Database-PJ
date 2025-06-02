# LLM评测平台

这是一个大语言模型（LLM）评测平台，用于评估和比较不同大语言模型的性能。

## 功能特点

### 评测功能
- 支持自动评测、人工评测和评判模型评测
- 支持对单个问题进行人工评测
- 支持批量评测和结果分析
- 支持多维度评测（准确性、完整性、清晰度等）
- 支持关键点评估

### 数据管理
- 原始问题和回答管理
- 标准问题和标准答案管理
- 数据集版本管理
- 众包任务管理

### 导入导出
- 支持导入模型回答（单条）
- 支持导入模型评测结果（单条）
- 支持批量导入评测结果（JSON、CSV、Excel格式）
- 支持导出评测结果

### 结果分析
- 模型评测结果对比
- 评测结果可视化
- 按分类、难度等维度分析

## 项目结构

### 前端
- 基于Vue 3 + TypeScript + Element Plus
- 采用组件化开发
- 使用Vue Router进行路由管理

### 后端
- 基于Spring Boot
- RESTful API设计
- 使用JPA进行数据访问
- 支持多种数据库

## 主要页面

1. 首页：展示评测概览和快速操作
2. 评测列表：管理评测批次
3. 评测详情：查看评测基本信息和进度
4. 评测结果：查看评测结果详情和分析
5. 人工评测：对单个问题进行人工评测
6. 模型回答导入：导入单条模型回答
7. 模型评测导入：导入单条模型评测结果
8. 评测结果导入：批量导入评测结果

## 开发指南

### 环境要求
- Node.js 16+
- JDK 11+
- Maven 3.6+

### 前端开发
```bash
# 进入前端目录
cd frontend

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

### 后端开发
```bash
# 进入后端目录
cd backend

# 编译项目
mvn clean package

# 运行项目
java -jar target/llm-eval-platform.jar
```

## API文档

### 评测相关API
- `GET /api/evaluations/batches`: 获取评测批次列表
- `GET /api/evaluations/batches/{batchId}`: 获取评测批次详情
- `POST /api/evaluations/batches`: 创建评测批次
- `POST /api/evaluations/batches/{batchId}/start`: 开始执行评测批次
- `GET /api/evaluations/batches/{batchId}/results`: 获取评测结果
- `GET /api/evaluations/batches/{batchId}/results/{questionId}`: 获取评测结果详情
- `GET /api/evaluations/unevaluated`: 获取待评测的回答
- `GET /api/evaluations/detail/{answerId}`: 获取评测详情
- `POST /api/evaluations/manual`: 提交人工评测结果
- `GET /api/evaluations/batches/{batchId}/export`: 导出评测结果

### 问题相关API
- `GET /api/questions/search`: 搜索标准问题
- `GET /api/questions/{questionId}`: 获取问题详情
- `GET /api/questions`: 获取问题列表
- `POST /api/questions`: 创建问题
- `PUT /api/questions/{questionId}`: 更新问题
- `DELETE /api/questions/{questionId}`: 删除问题

### 导入相关API
- `POST /api/import/model-answer`: 导入单条模型回答
- `GET /api/answers/search`: 搜索模型回答
- `GET /api/answers/{answerId}`: 获取模型回答详情 