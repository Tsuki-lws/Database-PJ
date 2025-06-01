# 简化分类管理功能 - API文档

## 功能说明

基于您的需求，您的系统已经具备完整的分类管理功能，但我们现在关注**简化的单层分类**，不使用层次化（嵌套）分类结构。

## 现有分类管理API

### 1. 创建分类

**基本信息**
- **接口路径**: `POST /api/categories`
- **请求方法**: POST
- **功能**: 创建新的问题分类

**请求示例**
```bash
POST http://localhost:8080/api/categories
Content-Type: application/json

{
  "name": "Java编程",
  "description": "Java相关的编程问题"
}
```

**响应示例**
```json
{
  "categoryId": 1,
  "name": "Java编程",
  "description": "Java相关的编程问题",
  "parent": null,
  "children": [],
  "questions": []
}
```

### 2. 获取所有分类

**基本信息**
- **接口路径**: `GET /api/categories`
- **请求方法**: GET
- **功能**: 获取系统中所有分类

**请求示例**
```bash
GET http://localhost:8080/api/categories
```

**响应示例**
```json
[
  {
    "categoryId": 1,
    "name": "Java编程",
    "description": "Java相关的编程问题",
    "parent": null,
    "children": [],
    "questions": []
  },
  {
    "categoryId": 2,
    "name": "Python编程",
    "description": "Python相关的编程问题",
    "parent": null,
    "children": [],
    "questions": []
  },
  {
    "categoryId": 3,
    "name": "数据结构",
    "description": "数据结构与算法问题",
    "parent": null,
    "children": [],
    "questions": []
  }
]
```

### 3. 根据ID获取分类

**基本信息**
- **接口路径**: `GET /api/categories/{id}`
- **请求方法**: GET
- **功能**: 获取指定ID的分类详情

**请求示例**
```bash
GET http://localhost:8080/api/categories/1
```

### 4. 更新分类

**基本信息**
- **接口路径**: `PUT /api/categories/{id}`
- **请求方法**: PUT
- **功能**: 更新指定分类的信息

**请求示例**
```bash
PUT http://localhost:8080/api/categories/1
Content-Type: application/json

{
  "name": "Java高级编程",
  "description": "Java高级特性和框架相关问题"
}
```

### 5. 删除分类

**基本信息**
- **接口路径**: `DELETE /api/categories/{id}`
- **请求方法**: DELETE
- **功能**: 删除指定分类

**请求示例**
```bash
DELETE http://localhost:8080/api/categories/1
```

### 6. 获取带问题数量的分类列表

**基本信息**
- **接口路径**: `GET /api/categories/with-count`
- **请求方法**: GET
- **功能**: 获取所有分类及其包含的问题数量

**请求示例**
```bash
GET http://localhost:8080/api/categories/with-count
```

## 完整的使用流程

### 步骤1：创建分类

首先创建几个基础分类供后续使用：

```bash
# 创建 Java 编程分类
POST /api/categories
{
  "name": "Java编程",
  "description": "Java相关的编程问题"
}

# 创建 Python 编程分类
POST /api/categories
{
  "name": "Python编程", 
  "description": "Python相关的编程问题"
}

# 创建数据结构分类
POST /api/categories
{
  "name": "数据结构",
  "description": "数据结构与算法问题"
}
```

### 步骤2：查看创建的分类

```bash
GET /api/categories
```

### 步骤3：为问题分配分类

使用之前实现的问题分类更新功能：

```bash
PUT /api/questions/1/category
{
  "categoryId": 1
}
```

### 步骤4：按分类查询问题

```bash
GET /api/questions/category/1
```

## 简化版本说明

由于您只需要**单层分类**，在创建分类时请注意：

1. **不要设置 parent 字段**：保持所有分类都是根分类
2. **创建时的JSON格式**：
   ```json
   {
     "name": "分类名称",
     "description": "分类描述"
     // 不包含 parent 字段
   }
   ```

## 数据库初始化脚本

为了方便测试，可以使用以下SQL脚本初始化一些基础分类：

```sql
-- 插入基础分类数据
INSERT INTO question_categories (name, description, parent_id) VALUES 
('Java编程', 'Java相关的编程问题', NULL),
('Python编程', 'Python相关的编程问题', NULL),
('数据结构', '数据结构与算法问题', NULL),
('数据库', '数据库设计与查询问题', NULL),
('前端开发', 'HTML/CSS/JavaScript相关问题', NULL);
```

## Apifox 测试配置

### 环境变量
```
BASE_URL: http://localhost:8080
```

### 测试用例顺序

1. **创建分类** → 创建几个基础分类
2. **查看所有分类** → 确认创建成功
3. **为问题设置分类** → 使用之前的分类更新API
4. **按分类查询问题** → 验证分类功能正常工作
5. **查看分类统计** → 使用with-count接口查看每个分类的问题数量

### 测试数据示例

```json
// 创建分类的测试数据
[
  {
    "name": "Java编程",
    "description": "Java相关的编程问题"
  },
  {
    "name": "Python编程", 
    "description": "Python相关的编程问题"
  },
  {
    "name": "数据结构",
    "description": "数据结构与算法问题"
  }
]
```

## 总结

您的系统已经具备完整的分类管理功能，包括：

✅ **创建分类** - `POST /api/categories`  
✅ **查询分类** - `GET /api/categories`  
✅ **更新分类** - `PUT /api/categories/{id}`  
✅ **删除分类** - `DELETE /api/categories/{id}`  
✅ **分类统计** - `GET /api/categories/with-count`  
✅ **问题分类管理** - `PUT /api/questions/{id}/category`  

对于简化的单层分类需求，只需要在创建分类时不设置父级分类即可。现有的API完全支持您的使用场景。
