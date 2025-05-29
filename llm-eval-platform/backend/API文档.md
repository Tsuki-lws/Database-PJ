# LLM评测平台 RESTful API文档

## 基础信息

- 基础URL: `http://localhost:8080`
- 请求格式: JSON
- 响应格式: JSON
- 认证方式: 待定

## 通用响应格式

### 成功响应

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    // 具体的数据对象
  }
}
```

### 错误响应

```json
{
  "code": 400,
  "message": "错误信息描述",
  "errors": [
    "具体错误信息1",
    "具体错误信息2"
  ]
}
```

## 模型管理API

### 获取所有模型

- **URL**: `/api/models`
- **方法**: GET
- **描述**: 获取系统中所有LLM模型
- **参数**: 无
- **响应示例**:

```json
[
  {
    "modelId": 1,
    "name": "GPT-4",
    "version": "v1.0",
    "provider": "OpenAI",
    "description": "OpenAI的GPT-4大语言模型",
    "apiConfig": "{\"api_key\": \"*****\", \"endpoint\": \"https://api.openai.com/v1\"}",
    "createdAt": "2023-05-20T10:15:30"
  },
  {
    "modelId": 2,
    "name": "Claude",
    "version": "v2.0",
    "provider": "Anthropic",
    "description": "Anthropic的Claude大语言模型",
    "apiConfig": "{\"api_key\": \"*****\", \"endpoint\": \"https://api.anthropic.com\"}",
    "createdAt": "2023-05-21T14:22:10"
  }
]
```

### 获取特定模型

- **URL**: `/api/models/{id}`
- **方法**: GET
- **描述**: 根据ID获取特定LLM模型
- **参数**:
  - `id`: 模型ID (路径参数)
- **响应示例**:

```json
{
  "modelId": 1,
  "name": "GPT-4",
  "version": "v1.0",
  "provider": "OpenAI",
  "description": "OpenAI的GPT-4大语言模型",
  "apiConfig": "{\"api_key\": \"*****\", \"endpoint\": \"https://api.openai.com/v1\"}",
  "createdAt": "2023-05-20T10:15:30"
}
```

### 创建模型

- **URL**: `/api/models`
- **方法**: POST
- **描述**: 创建新的LLM模型
- **请求体**:

```json
{
  "name": "LLaMA",
  "version": "v2.0",
  "provider": "Meta",
  "description": "Meta的开源大语言模型LLaMA",
  "apiConfig": "{\"endpoint\": \"http://localhost:8000/v1\"}"
}
```

- **响应示例**:

```json
{
  "modelId": 3,
  "name": "LLaMA",
  "version": "v2.0",
  "provider": "Meta",
  "description": "Meta的开源大语言模型LLaMA",
  "apiConfig": "{\"endpoint\": \"http://localhost:8000/v1\"}",
  "createdAt": "2023-06-01T09:30:15"
}
```

### 更新模型

- **URL**: `/api/models/{id}`
- **方法**: PUT
- **描述**: 更新现有LLM模型
- **参数**:
  - `id`: 模型ID (路径参数)
- **请求体**:

```json
{
  "name": "LLaMA",
  "version": "v2.0",
  "provider": "Meta",
  "description": "Meta的开源大语言模型LLaMA (更新后)",
  "apiConfig": "{\"endpoint\": \"http://localhost:8000/v1\", \"parameters\": {\"temperature\": 0.7}}"
}
```

- **响应示例**:

```json
{
  "modelId": 3,
  "name": "LLaMA",
  "version": "v2.0",
  "provider": "Meta",
  "description": "Meta的开源大语言模型LLaMA (更新后)",
  "apiConfig": "{\"endpoint\": \"http://localhost:8000/v1\", \"parameters\": {\"temperature\": 0.7}}",
  "createdAt": "2023-06-01T09:30:15"
}
```

### 删除模型

- **URL**: `/api/models/{id}`
- **方法**: DELETE
- **描述**: 删除LLM模型
- **参数**:
  - `id`: 模型ID (路径参数)
- **响应**: 204 No Content

## 评测批次API

### 获取所有评测批次

- **URL**: `/api/batches`
- **方法**: GET
- **描述**: 获取所有评测批次
- **参数**: 无
- **响应示例**:

```json
[
  {
    "batchId": 1,
    "name": "GPT-4在通用知识评测",
    "description": "评测GPT-4在通用知识方面的表现",
    "model": {
      "modelId": 1,
      "name": "GPT-4",
      "version": "v1.0"
    },
    "datasetVersion": {
      "versionId": 1,
      "name": "通用知识数据集v1.0"
    },
    "evaluationMethod": "AUTO",
    "status": "COMPLETED",
    "startTime": "2023-06-10T09:00:00",
    "endTime": "2023-06-10T10:15:30",
    "metricsSummary": "{\"average_score\": 8.7, \"passing_rate\": 0.92}",
    "createdAt": "2023-06-09T15:30:00",
    "updatedAt": "2023-06-10T10:15:30",
    "totalQuestions": 100,
    "completedQuestions": 100,
    "averageScore": 8.7
  },
  {
    "batchId": 2,
    "name": "Claude在医学知识评测",
    "description": "评测Claude在医学领域的专业知识",
    "model": {
      "modelId": 2,
      "name": "Claude",
      "version": "v2.0"
    },
    "datasetVersion": {
      "versionId": 2,
      "name": "医学知识数据集v1.0"
    },
    "evaluationMethod": "JUDGE_MODEL",
    "judgeModel": {
      "modelId": 1,
      "name": "GPT-4",
      "version": "v1.0"
    },
    "status": "IN_PROGRESS",
    "startTime": "2023-06-15T13:00:00",
    "metricsSummary": null,
    "createdAt": "2023-06-14T16:45:00",
    "updatedAt": "2023-06-15T13:00:00",
    "totalQuestions": 80,
    "completedQuestions": 35,
    "averageScore": 7.9
  }
]
```

### 获取特定评测批次

- **URL**: `/api/batches/{id}`
- **方法**: GET
- **描述**: 根据ID获取特定评测批次
- **参数**:
  - `id`: 批次ID (路径参数)
- **响应示例**:

```json
{
  "batchId": 1,
  "name": "GPT-4在通用知识评测",
  "description": "评测GPT-4在通用知识方面的表现",
  "model": {
    "modelId": 1,
    "name": "GPT-4",
    "version": "v1.0"
  },
  "datasetVersion": {
    "versionId": 1,
    "name": "通用知识数据集v1.0"
  },
  "evaluationMethod": "AUTO",
  "status": "COMPLETED",
  "startTime": "2023-06-10T09:00:00",
  "endTime": "2023-06-10T10:15:30",
  "metricsSummary": "{\"average_score\": 8.7, \"passing_rate\": 0.92}",
  "createdAt": "2023-06-09T15:30:00",
  "updatedAt": "2023-06-10T10:15:30",
  "totalQuestions": 100,
  "completedQuestions": 100,
  "averageScore": 8.7
}
```

### 获取特定模型的评测批次

- **URL**: `/api/batches/model/{modelId}`
- **方法**: GET
- **描述**: 获取特定模型的所有评测批次
- **参数**:
  - `modelId`: 模型ID (路径参数)
- **响应示例**: 与获取所有评测批次类似，但仅包含指定模型的批次

### 获取特定数据集版本的评测批次

- **URL**: `/api/batches/dataset/{versionId}`
- **方法**: GET
- **描述**: 获取特定数据集版本的所有评测批次
- **参数**:
  - `versionId`: 数据集版本ID (路径参数)
- **响应示例**: 与获取所有评测批次类似，但仅包含指定数据集版本的批次

### 创建评测批次

- **URL**: `/api/batches`
- **方法**: POST
- **描述**: 创建新的评测批次
- **请求体**:

```json
{
  "name": "LLaMA在代码生成评测",
  "description": "评测LLaMA在代码生成任务上的表现",
  "model": {
    "modelId": 3
  },
  "judgeModel": {
    "modelId": 1
  },
  "datasetVersion": {
    "versionId": 3
  },
  "evaluationMethod": "JUDGE_MODEL"
}
```

- **响应示例**:

```json
{
  "batchId": 3,
  "name": "LLaMA在代码生成评测",
  "description": "评测LLaMA在代码生成任务上的表现",
  "model": {
    "modelId": 3,
    "name": "LLaMA",
    "version": "v2.0"
  },
  "judgeModel": {
    "modelId": 1,
    "name": "GPT-4",
    "version": "v1.0"
  },
  "datasetVersion": {
    "versionId": 3,
    "name": "代码生成数据集v1.0"
  },
  "evaluationMethod": "JUDGE_MODEL",
  "status": "PENDING",
  "createdAt": "2023-06-20T11:30:00",
  "updatedAt": "2023-06-20T11:30:00",
  "totalQuestions": 50,
  "completedQuestions": 0,
  "averageScore": 0.0
}
```

### 更新评测批次

- **URL**: `/api/batches/{id}`
- **方法**: PUT
- **描述**: 更新现有评测批次
- **参数**:
  - `id`: 批次ID (路径参数)
- **请求体**:

```json
{
  "name": "LLaMA在代码生成评测(更新)",
  "description": "评测LLaMA在代码生成和调试任务上的表现",
  "evaluationMethod": "JUDGE_MODEL"
}
```

- **响应示例**:

```json
{
  "batchId": 3,
  "name": "LLaMA在代码生成评测(更新)",
  "description": "评测LLaMA在代码生成和调试任务上的表现",
  "model": {
    "modelId": 3,
    "name": "LLaMA",
    "version": "v2.0"
  },
  "judgeModel": {
    "modelId": 1,
    "name": "GPT-4",
    "version": "v1.0"
  },
  "datasetVersion": {
    "versionId": 3,
    "name": "代码生成数据集v1.0"
  },
  "evaluationMethod": "JUDGE_MODEL",
  "status": "PENDING",
  "createdAt": "2023-06-20T11:30:00",
  "updatedAt": "2023-06-20T11:45:00",
  "totalQuestions": 50,
  "completedQuestions": 0,
  "averageScore": 0.0
}
```

### 删除评测批次

- **URL**: `/api/batches/{id}`
- **方法**: DELETE
- **描述**: 删除评测批次
- **参数**:
  - `id`: 批次ID (路径参数)
- **响应**: 204 No Content

### 开始评测批次

- **URL**: `/api/batches/{id}/start`
- **方法**: POST
- **描述**: 开始执行评测批次
- **参数**:
  - `id`: 批次ID (路径参数)
- **响应示例**:

```json
{
  "batchId": 3,
  "status": "IN_PROGRESS",
  "startTime": "2023-06-20T14:00:00",
  "updatedAt": "2023-06-20T14:00:00"
}
```

### 完成评测批次

- **URL**: `/api/batches/{id}/complete`
- **方法**: POST
- **描述**: 手动标记评测批次为已完成
- **参数**:
  - `id`: 批次ID (路径参数)
- **响应示例**:

```json
{
  "batchId": 3,
  "status": "COMPLETED",
  "endTime": "2023-06-20T16:30:00",
  "updatedAt": "2023-06-20T16:30:00",
  "metricsSummary": "{\"average_score\": 7.8, \"passing_rate\": 0.84}"
}
```

## 评测结果API

### 获取所有评测结果

- **URL**: `/api/evaluations`
- **方法**: GET
- **描述**: 获取所有评测结果
- **参数**: 无
- **响应示例**:

```json
[
  {
    "evaluationId": 1,
    "llmAnswerId": 1,
    "standardAnswerId": 1,
    "score": 9.2,
    "method": "AUTO",
    "keyPointsEvaluation": "{\"matched\": [1, 2, 3], \"partial\": [4], \"missed\": [5]}",
    "comments": "模型回答准确度高，但缺少一些细节",
    "createdAt": "2023-06-10T09:15:30",
    "updatedAt": "2023-06-10T09:15:30"
  },
  {
    "evaluationId": 2,
    "llmAnswerId": 2,
    "standardAnswerId": 2,
    "score": 7.5,
    "method": "JUDGE_MODEL",
    "keyPointsEvaluation": "{\"matched\": [1, 3], \"partial\": [2], \"missed\": [4, 5]}",
    "judgeModelId": 1,
    "comments": "回答基本正确，但有一些医学术语使用不准确",
    "createdAt": "2023-06-15T13:30:45",
    "updatedAt": "2023-06-15T13:30:45"
  }
]
```

### 获取特定评测结果

- **URL**: `/api/evaluations/{id}`
- **方法**: GET
- **描述**: 根据ID获取特定评测结果
- **参数**:
  - `id`: 评测ID (路径参数)
- **响应示例**:

```json
{
  "evaluationId": 1,
  "llmAnswerId": 1,
  "standardAnswerId": 1,
  "score": 9.2,
  "method": "AUTO",
  "keyPointsEvaluation": "{\"matched\": [1, 2, 3], \"partial\": [4], \"missed\": [5]}",
  "comments": "模型回答准确度高，但缺少一些细节",
  "createdAt": "2023-06-10T09:15:30",
  "updatedAt": "2023-06-10T09:15:30",
  "keyPoints": [
    {
      "id": 1,
      "evaluationId": 1,
      "keyPointId": 1,
      "status": "MATCHED",
      "score": 1.0,
      "keyPointContent": "提到了主要概念"
    },
    {
      "id": 2,
      "evaluationId": 1,
      "keyPointId": 2,
      "status": "MATCHED",
      "score": 1.0,
      "keyPointContent": "解释了原理"
    }
  ],
  "standardAnswer": {
    "answerId": 1,
    "standardQuestionId": 1,
    "answer": "标准答案内容...",
    "isFinal": true
  }
}
```

### 获取特定回答的评测结果

- **URL**: `/api/evaluations/answer/{answerId}`
- **方法**: GET
- **描述**: 获取特定LLM回答的评测结果
- **参数**:
  - `answerId`: LLM回答ID (路径参数)
- **响应示例**: 与获取所有评测结果类似，但仅包含指定回答的评测

### 获取特定批次的评测结果

- **URL**: `/api/evaluations/batch/{batchId}`
- **方法**: GET
- **描述**: 获取特定评测批次的所有评测结果
- **参数**:
  - `batchId`: 批次ID (路径参数)
- **响应示例**: 与获取所有评测结果类似，但仅包含指定批次的评测

### 获取批次的平均评分

- **URL**: `/api/evaluations/batch/{batchId}/average`
- **方法**: GET
- **描述**: 计算特定评测批次的平均得分
- **参数**:
  - `batchId`: 批次ID (路径参数)
- **响应示例**:

```json
8.7
```

### 获取通过评测的回答数量

- **URL**: `/api/evaluations/batch/{batchId}/passing`
- **方法**: GET
- **描述**: 获取特定评测批次中通过评测的回答数量
- **参数**:
  - `batchId`: 批次ID (路径参数)
  - `threshold`: 通过阈值，默认为0.6 (查询参数)
- **响应示例**:

```json
92
```

### 执行批次评测

- **URL**: `/api/evaluations/batch/{batchId}/run`
- **方法**: POST
- **描述**: 对特定批次执行评测
- **参数**:
  - `batchId`: 批次ID (路径参数)
- **响应示例**:

```json
[
  {
    "evaluationId": 101,
    "llmAnswerId": 201,
    "standardAnswerId": 301,
    "score": 8.5,
    "method": "AUTO",
    "batchId": 3,
    "createdAt": "2023-06-20T14:15:30",
    "updatedAt": "2023-06-20T14:15:30"
  },
  {
    "evaluationId": 102,
    "llmAnswerId": 202,
    "standardAnswerId": 302,
    "score": 7.8,
    "method": "AUTO",
    "batchId": 3,
    "createdAt": "2023-06-20T14:15:45",
    "updatedAt": "2023-06-20T14:15:45"
  }
]
```

### 创建评测结果

- **URL**: `/api/evaluations`
- **方法**: POST
- **描述**: 创建新的评测结果
- **请求体**:

```json
{
  "llmAnswerId": 203,
  "standardAnswerId": 303,
  "score": 9.0,
  "method": "HUMAN",
  "keyPointsEvaluation": "{\"matched\": [1, 2, 3, 4], \"partial\": [], \"missed\": [5]}",
  "comments": "人工评测结果，回答非常准确",
  "batchId": 3
}
```

- **响应示例**:

```json
{
  "evaluationId": 103,
  "llmAnswerId": 203,
  "standardAnswerId": 303,
  "score": 9.0,
  "method": "HUMAN",
  "keyPointsEvaluation": "{\"matched\": [1, 2, 3, 4], \"partial\": [], \"missed\": [5]}",
  "comments": "人工评测结果，回答非常准确",
  "batchId": 3,
  "createdAt": "2023-06-20T16:00:00",
  "updatedAt": "2023-06-20T16:00:00"
}
```

### 更新评测结果

- **URL**: `/api/evaluations/{id}`
- **方法**: PUT
- **描述**: 更新现有评测结果
- **参数**:
  - `id`: 评测ID (路径参数)
- **请求体**:

```json
{
  "score": 8.8,
  "comments": "更新后的评测意见：回答非常准确，但有小瑕疵"
}
```

- **响应示例**:

```json
{
  "evaluationId": 103,
  "llmAnswerId": 203,
  "standardAnswerId": 303,
  "score": 8.8,
  "method": "HUMAN",
  "keyPointsEvaluation": "{\"matched\": [1, 2, 3, 4], \"partial\": [], \"missed\": [5]}",
  "comments": "更新后的评测意见：回答非常准确，但有小瑕疵",
  "batchId": 3,
  "createdAt": "2023-06-20T16:00:00",
  "updatedAt": "2023-06-20T16:15:00"
}
```

### 删除评测结果

- **URL**: `/api/evaluations/{id}`
- **方法**: DELETE
- **描述**: 删除评测结果
- **参数**:
  - `id`: 评测ID (路径参数)
- **响应**: 204 No Content

## 统计API

### 获取原始问题和答案数量

- **URL**: `/api/stats/raw-counts`
- **方法**: GET
- **描述**: 获取原始问题和答案的数量
- **参数**: 无
- **响应示例**:

```json
{
  "rawQuestionCount": 5000,
  "rawAnswerCount": 15000
}
```

### 获取标准问题和答案数量

- **URL**: `/api/stats/standard-counts`
- **方法**: GET
- **描述**: 获取标准问题和答案的数量
- **参数**: 无
- **响应示例**:

```json
{
  "standardQuestionCount": 1000,
  "standardAnswerCount": 950
}
```

### 获取各分类问题数量

- **URL**: `/api/stats/category-counts`
- **方法**: GET
- **描述**: 获取各个分类下的标准问题数量
- **参数**: 无
- **响应示例**:

```json
[
  {
    "categoryId": 1,
    "categoryName": "通用知识",
    "questionCount": 300
  },
  {
    "categoryId": 2,
    "categoryName": "医学知识",
    "questionCount": 200
  },
  {
    "categoryId": 3,
    "categoryName": "编程与计算机科学",
    "questionCount": 150
  }
]
```

### 获取各标签问题数量

- **URL**: `/api/stats/tag-counts`
- **方法**: GET
- **描述**: 获取各个标签下的标准问题数量
- **参数**: 无
- **响应示例**:

```json
[
  {
    "tagId": 1,
    "tagName": "数学",
    "questionCount": 120
  },
  {
    "tagId": 2,
    "tagName": "物理",
    "questionCount": 100
  },
  {
    "tagId": 3,
    "tagName": "历史",
    "questionCount": 80
  }
]
```

### 获取无标准答案的问题数量

- **URL**: `/api/stats/without-standard-answer`
- **方法**: GET
- **描述**: 获取尚未录入标准答案的标准问题数量
- **参数**: 无
- **响应示例**:

```json
{
  "count": 50,
  "questions": [
    {
      "standardQuestionId": 951,
      "question": "问题内容...",
      "categoryId": 1
    },
    {
      "standardQuestionId": 952,
      "question": "问题内容...",
      "categoryId": 2
    }
  ]
}
```

### 获取数据集版本数量

- **URL**: `/api/stats/dataset-versions`
- **方法**: GET
- **描述**: 获取数据集版本数量
- **参数**: 无
- **响应示例**:

```json
{
  "count": 5,
  "versions": [
    {
      "versionId": 1,
      "name": "通用知识数据集v1.0",
      "questionCount": 300
    },
    {
      "versionId": 2,
      "name": "医学知识数据集v1.0",
      "questionCount": 200
    }
  ]
}
```

### 获取统计摘要

- **URL**: `/api/stats/summary`
- **方法**: GET
- **描述**: 获取系统整体统计摘要
- **参数**: 无
- **响应示例**:

```json
{
  "rawData": {
    "rawQuestionCount": 5000,
    "rawAnswerCount": 15000
  },
  "standardData": {
    "standardQuestionCount": 1000,
    "standardAnswerCount": 950,
    "withoutAnswerCount": 50
  },
  "categoriesCount": 10,
  "tagsCount": 25,
  "datasetVersions": 5,
  "models": 3,
  "evaluationBatches": 5,
  "completedEvaluations": 450
}
```