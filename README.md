# LLM评测平台 - 项目使用说明

## 项目概述

LLM评测平台是一个用于评估和比较大语言模型性能的综合性系统。该平台提供了完整的问题管理、标准答案管理、评测管理、众包任务管理以及数据导入等功能。

## 系统架构

- **前端**: Vue 3 + TypeScript + Element Plus
- **后端**: Spring Boot 3.2.3 + Spring Data JPA
- **数据库**: MySQL 8.0
- **文档**: Swagger/OpenAPI 3.0

## 项目获取

### Git仓库

```bash
# 克隆项目
git clone git@github.com:Tsuki-lws/Database-PJ.git

# 进入项目目录
cd Database-PJ
```

**仓库地址**: https://github.com/Tsuki-lws/Database-PJ

## 环境准备

### 必需软件

- **JDK 17** 或更高版本
- **MySQL 8.0** 或更高版本
- **Gradle 8.x**
- **IDE** IntelliJ IDEA 

### 数据库配置

1. **创建数据库**

```sql
CREATE DATABASE llm_eval_db;
CREATE USER 'llm_user'@'localhost' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON llm_eval_db.* TO 'llm_user'@'localhost';
FLUSH PRIVILEGES;
```

2. **配置数据库连接**

编辑 `backend/src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/llm_eval_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=llm_user
spring.datasource.password=your_password
spring.jpa.hibernate.ddl-auto=update
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect
```

## 项目启动

### 后端启动

1. **命令行方式**

```bash
# 进入后端目录
cd backend

# 构建项目
./gradlew clean build

# 运行项目
./gradlew bootRun
```

2. **IDE方式**

- 在IDE中打开项目
- 找到 `LlmEvalPlatformApplication.java`
- 右键运行主类


### 前端启动

1. **环境要求**

- **Node.js** 18.0 或更高版本
- **npm** 或 **yarn** 包管理器

2. **安装依赖**

```bash
# 进入前端目录
cd frontend

# 安装依赖包
npm install
# 或使用 yarn
yarn install
```

3. **开发模式启动**

```bash
# 启动开发服务器
npm run dev
# 或使用 yarn
yarn dev
```


4. **验证启动**

开发模式启动成功后访问：
- **前端应用**: http://localhost:5173
- **默认登录页面**: http://localhost:5173

### 完整启动流程

1. **启动后端服务**
   ```bash
   cd backend
   ./gradlew bootRun
   ```

2. **启动前端服务** (新终端窗口)
   ```bash
   cd frontend
   npm install
   npm run dev
   ```

3. **访问应用**
   - 前端界面: http://localhost:5173
