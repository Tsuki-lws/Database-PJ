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