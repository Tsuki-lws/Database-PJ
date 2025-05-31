# LLM评测平台API文档（补充）

## 数据导入模块扩展接口

### 1. 仅导入原始问题（不包含回答）

```
POST /api/import/raw-question-only
```

**请求参数：**

```json
{
  "sourceQuestionId": 123,
  "questionTitle": "如何使用Spring Boot开发RESTful API?",
  "questionBody": "<p>我想了解如何使用Spring Boot开发RESTful API，有什么最佳实践？</p>",
  "source": "stackoverflow",
  "sourceId": 123,
  "sourceUrl": "https://stackoverflow.com/questions/123"
}
```

**响应：**

```json
{
  "success": true,
  "message": "原始问题导入成功",
  "importedCount": 1,
  "failedCount": 0,
  "totalCount": 1
}
```

### 2. 批量导入原始问题（不包含回答）

```
POST /api/import/raw-questions-only
```

**请求参数：**

```json
[
  {
    "sourceQuestionId": 123,
    "questionTitle": "如何使用Spring Boot开发RESTful API?",
    "questionBody": "<p>我想了解如何使用Spring Boot开发RESTful API，有什么最佳实践？</p>",
    "source": "stackoverflow",
    "sourceId": 123,
    "sourceUrl": "https://stackoverflow.com/questions/123"
  },
  {
    "sourceQuestionId": 124,
    "questionTitle": "Spring Data JPA的使用方法",
    "questionBody": "<p>如何正确使用Spring Data JPA？</p>",
    "source": "stackoverflow",
    "sourceId": 124,
    "sourceUrl": "https://stackoverflow.com/questions/124"
  }
]
```

**响应：**

```json
{
  "success": true,
  "message": "批量导入原始问题完成",
  "importedCount": 2,
  "failedCount": 0,
  "totalCount": 2
}
```

### 3. 为已存在的原始问题导入回答

```
POST /api/import/raw-answer
```

**请求参数：**

```json
{
  "questionId": 1,
  "sourceAnswerId": 456,
  "answerBody": "<p>要使用Spring Boot开发RESTful API，您可以：...</p>",
  "authorInfo": "John Doe",
  "upvotes": 10,
  "isAccepted": true
}
```

**响应：**

```json
{
  "success": true,
  "message": "原始回答导入成功",
  "importedCount": 1,
  "failedCount": 0,
  "totalCount": 1
}
```

### 4. 批量导入原始回答

```
POST /api/import/raw-answers
```

**请求参数：**

```json
[
  {
    "questionId": 1,
    "sourceAnswerId": 456,
    "answerBody": "<p>要使用Spring Boot开发RESTful API，您可以：...</p>",
    "authorInfo": "John Doe",
    "upvotes": 10,
    "isAccepted": true
  },
  {
    "questionId": 1,
    "sourceAnswerId": 457,
    "answerBody": "<p>另一种方式是使用Spring MVC...</p>",
    "authorInfo": "Jane Smith",
    "upvotes": 5,
    "isAccepted": false
  }
]
```

**响应：**

```json
{
  "success": true,
  "message": "批量导入原始回答完成",
  "importedCount": 2,
  "failedCount": 0,
  "totalCount": 2
}
```

### 5. 仅导入标准问题（不包含答案）

```
POST /api/import/standard-question
```

**请求参数：**

```json
{
  "question": "请详细介绍Spring Boot开发RESTful API的最佳实践",
  "categoryId": 1,
  "questionType": "subjective",
  "difficulty": "medium",
  "sourceQuestionId": 1,
  "tags": [1, 2, 3]
}
```

**响应：**

```json
{
  "success": true,
  "message": "标准问题导入成功",
  "importedCount": 1,
  "failedCount": 0,
  "totalCount": 1
}
```

### 6. 批量导入标准问题（不包含答案）

```
POST /api/import/standard-questions
```

**请求参数：**

```json
[
  {
    "question": "请详细介绍Spring Boot开发RESTful API的最佳实践",
    "categoryId": 1,
    "questionType": "subjective",
    "difficulty": "medium",
    "sourceQuestionId": 1,
    "tags": [1, 2, 3]
  },
  {
    "question": "如何使用Spring Data JPA实现复杂查询？",
    "categoryId": 1,
    "questionType": "subjective",
    "difficulty": "hard",
    "sourceQuestionId": 2,
    "tags": [1, 4]
  }
]
```

**响应：**

```json
{
  "success": true,
  "message": "批量导入标准问题完成",
  "importedCount": 2,
  "failedCount": 0,
  "totalCount": 2
}
```

### 7. 为标准问题导入标准答案

```
POST /api/import/standard-answer
```

**请求参数：**

```json
{
  "standardQuestionId": 1,
  "answer": "开发Spring Boot RESTful API的最佳实践包括...",
  "sourceAnswerId": 1,
  "sourceType": "raw",
  "keyPoints": [
    {
      "pointText": "使用合适的HTTP方法",
      "pointWeight": 1.0,
      "pointType": "required",
      "exampleText": "GET用于获取资源，POST用于创建资源..."
    },
    {
      "pointText": "适当的错误处理",
      "pointWeight": 1.0,
      "pointType": "required",
      "exampleText": "使用统一的错误响应格式..."
    }
  ]
}
```

**响应：**

```json
{
  "success": true,
  "message": "标准答案导入成功",
  "importedCount": 1,
  "failedCount": 0,
  "totalCount": 1
}
```

### 8. 批量导入标准答案

```
POST /api/import/standard-answers
```

**请求参数：**

```json
[
  {
    "standardQuestionId": 1,
    "answer": "开发Spring Boot RESTful API的最佳实践包括...",
    "sourceAnswerId": 1,
    "sourceType": "raw",
    "keyPoints": [
      {
        "pointText": "使用合适的HTTP方法",
        "pointWeight": 1.0,
        "pointType": "required",
        "exampleText": "GET用于获取资源，POST用于创建资源..."
      },
      {
        "pointText": "适当的错误处理",
        "pointWeight": 1.0,
        "pointType": "required",
        "exampleText": "使用统一的错误响应格式..."
      }
    ]
  },
  {
    "standardQuestionId": 2,
    "answer": "使用Spring Data JPA实现复杂查询的方法包括...",
    "sourceAnswerId": 2,
    "sourceType": "raw",
    "keyPoints": [
      {
        "pointText": "使用@Query注解",
        "pointWeight": 1.0,
        "pointType": "required",
        "exampleText": "可以使用JPQL或原生SQL..."
      }
    ]
  }
]
```

**响应：**

```json
{
  "success": true,
  "message": "批量导入标准答案完成",
  "importedCount": 2,
  "failedCount": 0,
  "totalCount": 2
}
```

### 9. 创建标准问答对关联

```
POST /api/import/standard-qa-link
```

**请求参数：**

```json
{
  "standardQuestionId": 1,
  "standardAnswerId": 1,
  "sourceQuestionId": 1,
  "sourceAnswerId": 1
}
```

**响应：**

```json
{
  "success": true,
  "message": "标准问答对关联创建成功",
  "importedCount": 1,
  "failedCount": 0,
  "totalCount": 1
}
```

### 10. 批量创建标准问答对关联

```
POST /api/import/standard-qa-links
```

**请求参数：**

```json
[
  {
    "standardQuestionId": 1,
    "standardAnswerId": 1,
    "sourceQuestionId": 1,
    "sourceAnswerId": 1
  },
  {
    "standardQuestionId": 2,
    "standardAnswerId": 2,
    "sourceQuestionId": 2,
    "sourceAnswerId": 2
  }
]
```

**响应：**

```json
{
  "success": true,
  "message": "批量创建标准问答对关联完成",
  "importedCount": 2,
  "failedCount": 0,
  "totalCount": 2
}
```

### 11. 文件上传导入原始问题（不包含回答）

```
POST /api/import/raw-questions-only-file
Content-Type: multipart/form-data
```

**请求参数：**
- file: JSON文件，包含原始问题列表

**响应：**

```json
{
  "success": true,
  "message": "批量导入原始问题完成",
  "importedCount": 10,
  "failedCount": 0,
  "totalCount": 10
}
```

### 12. 文件上传导入原始回答

```
POST /api/import/raw-answers-file
Content-Type: multipart/form-data
```

**请求参数：**
- file: JSON文件，包含原始回答列表

**响应：**

```json
{
  "success": true,
  "message": "批量导入原始回答完成",
  "importedCount": 20,
  "failedCount": 0,
  "totalCount": 20
}
```

## 标准问题查询模块扩展接口

### 1. 分页查询无标准答案的问题

```
GET /api/questions/without-answer/paged
```

**请求参数：**

- `page` (int, optional): 页码，从0开始，默认为0
- `size` (int, optional): 每页大小，默认为10
- `categoryId` (Long, optional): 分类ID过滤
- `questionType` (String, optional): 问题类型过滤
- `difficulty` (String, optional): 难度级别过滤

**示例请求：**
```
GET /api/questions/without-answer/paged?page=0&size=20&categoryId=1&difficulty=MEDIUM
```

**响应：**

```json
{
  "content": [
    {
      "id": 1,
      "questionTitle": "如何优化数据库查询性能？",
      "questionBody": "在处理大量数据时，如何有效优化数据库查询性能？",
      "categoryId": 1,
      "categoryName": "数据库",
      "questionType": "技术问题",
      "difficulty": "MEDIUM",
      "createTime": "2024-01-15T10:30:00",
      "updateTime": "2024-01-15T10:30:00",
      "tags": ["数据库", "性能优化", "SQL"],
      "sourceInfo": {
        "source": "stackoverflow",
        "sourceId": 123,
        "sourceUrl": "https://stackoverflow.com/questions/123"
      }
    }
  ],
  "page": {
    "number": 0,
    "size": 20,
    "totalElements": 150,
    "totalPages": 8,
    "first": true,
    "last": false,
    "numberOfElements": 20
  }
}
```

### 2. 获取无标准答案问题统计详情

```
GET /api/stats/without-standard-answer/details
```

**请求参数：**

- `page` (int, optional): 页码，从0开始，默认为0
- `size` (int, optional): 每页大小，默认为10
- `categoryId` (Long, optional): 分类ID过滤
- `questionType` (String, optional): 问题类型过滤
- `difficulty` (String, optional): 难度级别过滤

**示例请求：**
```
GET /api/stats/without-standard-answer/details?page=0&size=15&categoryId=2
```

**响应：**

```json
{
  "summary": {
    "totalCount": 150,
    "filteredCount": 75
  },
  "questions": {
    "content": [
      {
        "id": 5,
        "questionTitle": "Spring Boot自动配置原理",
        "questionBody": "请解释Spring Boot的自动配置是如何工作的？",
        "categoryId": 2,
        "categoryName": "Java框架",
        "questionType": "概念理解",
        "difficulty": "HARD",
        "createTime": "2024-01-20T14:15:00",
        "updateTime": "2024-01-20T14:15:00",
        "tags": ["Spring Boot", "自动配置", "框架"],
        "sourceInfo": {
          "source": "github",
          "sourceId": 456,
          "sourceUrl": "https://github.com/issues/456"
        }
      }
    ],
    "page": {
      "number": 0,
      "size": 15,
      "totalElements": 75,
      "totalPages": 5,
      "first": true,
      "last": false,
      "numberOfElements": 15
    }
  }
}
```

## 版本管理模块接口

### 1. 创建数据集版本

```
POST /api/dataset-versions
```

**请求参数：**

```json
{
  "datasetId": 1,
  "versionName": "v1.0.0",
  "description": "初始版本，包含基础问题集",
  "questionIds": [1, 2, 3, 4, 5],
  "baseVersionId": null
}
```

**响应：**

```json
{
  "success": true,
  "message": "数据集版本创建成功",
  "versionId": 10,
  "versionInfo": {
    "id": 10,
    "datasetId": 1,
    "versionName": "v1.0.0",
    "description": "初始版本，包含基础问题集",
    "questionCount": 5,
    "createTime": "2024-01-25T09:00:00",
    "status": "ACTIVE"
  }
}
```

### 2. 获取数据集版本列表

```
GET /api/dataset-versions/{datasetId}
```

**路径参数：**
- `datasetId` (Long): 数据集ID

**请求参数：**
- `page` (int, optional): 页码，默认为0
- `size` (int, optional): 每页大小，默认为10

**响应：**

```json
{
  "content": [
    {
      "id": 10,
      "versionName": "v1.0.0",
      "description": "初始版本，包含基础问题集",
      "questionCount": 5,
      "createTime": "2024-01-25T09:00:00",
      "status": "ACTIVE",
      "isLatest": true
    },
    {
      "id": 11,
      "versionName": "v1.1.0",
      "description": "添加了高难度问题",
      "questionCount": 8,
      "createTime": "2024-01-26T10:30:00",
      "status": "ACTIVE",
      "isLatest": false
    }
  ],
  "page": {
    "number": 0,
    "size": 10,
    "totalElements": 2,
    "totalPages": 1,
    "first": true,
    "last": true,
    "numberOfElements": 2
  }
}
```

### 3. 创建标准问题版本

```
POST /api/standard-questions/{questionId}/versions
```

**路径参数：**
- `questionId` (Long): 标准问题ID

**请求参数：**

```json
{
  "versionName": "v2.0",
  "changeReason": "更新了问题描述，增加了更多细节",
  "questionTitle": "如何优化数据库查询性能？（更新版）",
  "questionBody": "在处理大量数据时，如何有效优化数据库查询性能？请提供具体的优化策略和工具。",
  "standardAnswer": "数据库查询性能优化可以从以下几个方面入手：1. 索引优化 2. 查询语句优化 3. 数据库配置调优...",
  "referenceAnswers": ["参考答案1", "参考答案2"]
}
```

**响应：**

```json
{
  "success": true,
  "message": "标准问题版本创建成功",
  "versionId": 25,
  "versionInfo": {
    "id": 25,
    "questionId": 1,
    "versionName": "v2.0",
    "changeReason": "更新了问题描述，增加了更多细节",
    "createTime": "2024-01-25T11:15:00",
    "status": "ACTIVE",
    "isLatest": true
  }
}
```

### 4. 获取标准问题版本历史

```
GET /api/standard-questions/{questionId}/versions
```

**路径参数：**
- `questionId` (Long): 标准问题ID

**响应：**

```json
{
  "questionId": 1,
  "currentVersion": "v2.0",
  "versions": [
    {
      "id": 24,
      "versionName": "v1.0",
      "changeReason": "初始版本",
      "createTime": "2024-01-20T09:00:00",
      "status": "ARCHIVED",
      "isLatest": false
    },
    {
      "id": 25,
      "versionName": "v2.0",
      "changeReason": "更新了问题描述，增加了更多细节",
      "createTime": "2024-01-25T11:15:00",
      "status": "ACTIVE",
      "isLatest": true
    }
  ]
}
```

### 5. 比较标准问题版本差异

```
GET /api/standard-questions/versions/compare
```

**请求参数：**
- `fromVersionId` (Long): 源版本ID
- `toVersionId` (Long): 目标版本ID

**响应：**

```json
{
  "comparison": {
    "fromVersion": {
      "id": 24,
      "versionName": "v1.0",
      "createTime": "2024-01-20T09:00:00"
    },
    "toVersion": {
      "id": 25,
      "versionName": "v2.0",
      "createTime": "2024-01-25T11:15:00"
    },
    "differences": {
      "questionTitle": {
        "changed": true,
        "oldValue": "如何优化数据库查询性能？",
        "newValue": "如何优化数据库查询性能？（更新版）"
      },
      "questionBody": {
        "changed": true,
        "oldValue": "在处理大量数据时，如何有效优化数据库查询性能？",
        "newValue": "在处理大量数据时，如何有效优化数据库查询性能？请提供具体的优化策略和工具。"
      },
      "standardAnswer": {
        "changed": true,
        "oldValue": "数据库查询性能优化主要包括索引优化...",
        "newValue": "数据库查询性能优化可以从以下几个方面入手：1. 索引优化 2. 查询语句优化 3. 数据库配置调优..."
      }
    }
  }
}
```

## 错误响应格式

所有接口在出现错误时返回统一格式：

```json
{
  "success": false,
  "message": "错误描述",
  "errorCode": "ERROR_CODE",
  "timestamp": "2024-01-25T12:00:00",
  "path": "/api/questions/without-answer/paged"
}
```

**常见错误码：**

- `INVALID_PARAMETER`: 请求参数无效
- `RESOURCE_NOT_FOUND`: 资源不存在
- `VERSION_CONFLICT`: 版本冲突
- `PAGINATION_ERROR`: 分页参数错误
- `FILTER_ERROR`: 过滤条件错误