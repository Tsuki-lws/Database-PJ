# 数据导入补充API文档

## 原始问题和回答分离导入

### 单独导入原始问题

- **URL**: `/api/import/raw-question-only`
- **方法**: POST
- **描述**: 仅导入原始问题，不包含回答
- **内容类型**: `application/json`
- **请求体示例**:

```json
{
  "sourceQuestionId": 61,
  "questionTitle": "What is the difference between kernel Makefile terms?",
  "questionBody": "<p>While browsing through the Kernel Makefiles...</p>",
  "source": "StackExchange",
  "sourceId": 5518,
  "sourceUrl": "https://unix.stackexchange.com/questions/5518"
}
```

- **响应示例**:

```json
{
  "code": 200,
  "message": "原始问题导入成功",
  "data": {
    "rawQuestionId": 61,
    "sourceQuestionId": 61,
    "questionTitle": "What is the difference between kernel Makefile terms?",
    "source": "StackExchange",
    "createdAt": "2023-05-20T10:15:30"
  }
}
```

### 批量导入原始问题

- **URL**: `/api/import/raw-questions-only`
- **方法**: POST
- **描述**: 批量导入多个原始问题，不包含回答
- **内容类型**: `application/json`
- **请求体示例**:

```json
[
  {
    "sourceQuestionId": 61,
    "questionTitle": "What is the difference between kernel Makefile terms?",
    "questionBody": "<p>While browsing through the Kernel Makefiles...</p>",
    "source": "StackExchange",
    "sourceId": 5518,
    "sourceUrl": "https://unix.stackexchange.com/questions/5518"
  },
  {
    "sourceQuestionId": 62,
    "questionTitle": "How to configure network interfaces in Linux?",
    "questionBody": "<p>I'm trying to set up multiple network interfaces...</p>",
    "source": "StackExchange",
    "sourceId": 5519,
    "sourceUrl": "https://unix.stackexchange.com/questions/5519"
  }
]
```

- **响应示例**:

```json
{
  "code": 200,
  "message": "批量导入原始问题成功",
  "data": {
    "importedCount": 2,
    "updatedCount": 0,
    "failedCount": 0,
    "details": "成功导入2个原始问题"
  }
}
```

### 为原始问题导入回答

- **URL**: `/api/import/raw-answer`
- **方法**: POST
- **描述**: 为已存在的原始问题导入一个回答
- **内容类型**: `application/json`
- **请求体示例**:

```json
{
  "questionId": 61,
  "sourceAnswerId": 1513,
  "answerBody": "<p><strong>vmlinux</strong> is a statically linked executable file...</p>",
  "authorInfo": "user123",
  "upvotes": 42,
  "isAccepted": true
}
```

- **响应示例**:

```json
{
  "code": 200,
  "message": "回答导入成功",
  "data": {
    "rawAnswerId": 1513,
    "questionId": 61,
    "sourceAnswerId": 1513,
    "authorInfo": "user123",
    "isAccepted": true,
    "createdAt": "2023-05-20T10:20:30"
  }
}
```

### 批量导入原始问题回答

- **URL**: `/api/import/raw-answers`
- **方法**: POST
- **描述**: 批量导入多个原始问题的回答
- **内容类型**: `application/json`
- **请求体示例**:

```json
[
  {
    "questionId": 61,
    "sourceAnswerId": 1513,
    "answerBody": "<p><strong>vmlinux</strong> is a statically linked executable file...</p>",
    "authorInfo": "user123",
    "upvotes": 42,
    "isAccepted": true
  },
  {
    "questionId": 61,
    "sourceAnswerId": 1514,
    "answerBody": "<p>To add to the previous answer...</p>",
    "authorInfo": "user456",
    "upvotes": 28,
    "isAccepted": false
  },
  {
    "questionId": 62,
    "sourceAnswerId": 1515,
    "answerBody": "<p>You can configure network interfaces by...</p>",
    "authorInfo": "user789",
    "upvotes": 35,
    "isAccepted": true
  }
]
```

- **响应示例**:

```json
{
  "code": 200,
  "message": "批量导入回答成功",
  "data": {
    "importedCount": 3,
    "updatedCount": 0,
    "failedCount": 0,
    "details": "成功导入3个原始回答"
  }
}
```

## 标准问题和回答分离导入

### 单独导入标准问题

- **URL**: `/api/import/standard-question`
- **方法**: POST
- **描述**: 仅导入标准问题，不包含标准答案
- **内容类型**: `application/json`
- **请求体示例**:

```json
{
  "question": "What is the purpose of the 'vmlinux' file in Linux kernel?",
  "categoryId": 3,
  "questionType": "subjective",
  "difficulty": "medium",
  "sourceQuestionId": 61,
  "tags": [1, 5, 8]
}
```

- **响应示例**:

```json
{
  "code": 200,
  "message": "标准问题导入成功",
  "data": {
    "standardQuestionId": 1,
    "question": "What is the purpose of the 'vmlinux' file in Linux kernel?",
    "categoryId": 3,
    "questionType": "subjective",
    "difficulty": "medium",
    "sourceQuestionId": 61,
    "tagsCount": 3,
    "createdAt": "2023-05-20T11:15:30"
  }
}
```

### 批量导入标准问题

- **URL**: `/api/import/standard-questions`
- **方法**: POST
- **描述**: 批量导入多个标准问题，不包含标准答案
- **内容类型**: `application/json`
- **请求体示例**:

```json
[
  {
    "question": "What is the purpose of the 'vmlinux' file in Linux kernel?",
    "categoryId": 3,
    "questionType": "subjective",
    "difficulty": "medium",
    "sourceQuestionId": 61,
    "tags": [1, 5, 8]
  },
  {
    "question": "How to configure network interfaces in Linux?",
    "categoryId": 3,
    "questionType": "subjective",
    "difficulty": "easy",
    "sourceQuestionId": 62,
    "tags": [3, 7]
  }
]
```

- **响应示例**:

```json
{
  "code": 200,
  "message": "批量导入标准问题成功",
  "data": {
    "importedCount": 2,
    "updatedCount": 0,
    "failedCount": 0,
    "details": "成功导入2个标准问题"
  }
}
```

### 为标准问题导入标准答案

- **URL**: `/api/import/standard-answer`
- **方法**: POST
- **描述**: 为已存在的标准问题导入标准答案
- **内容类型**: `application/json`
- **请求体示例**:

```json
{
  "standardQuestionId": 1,
  "answer": "The 'vmlinux' file is the Linux kernel in a statically linked executable file format...",
  "sourceAnswerId": 1513,
  "sourceType": "raw",
  "keyPoints": [
    {
      "pointText": "vmlinux is a statically linked executable file format of the Linux kernel",
      "pointWeight": 1.0,
      "pointType": "required",
      "exampleText": "The vmlinux file contains the full kernel in ELF format"
    },
    {
      "pointText": "It serves as an intermediate step in the boot procedure",
      "pointWeight": 0.8,
      "pointType": "required",
      "exampleText": "The vmlinux is not directly bootable but is processed further"
    },
    {
      "pointText": "The raw 'vmlinux' file can be useful for debugging",
      "pointWeight": 0.5,
      "pointType": "bonus",
      "exampleText": "Kernel developers use vmlinux for debugging with tools like gdb"
    }
  ]
}
```

- **响应示例**:

```json
{
  "code": 200,
  "message": "标准答案导入成功",
  "data": {
    "standardAnswerId": 1,
    "standardQuestionId": 1,
    "keyPointsCount": 3,
    "isFinal": true,
    "createdAt": "2023-05-20T11:30:45"
  }
}
```

### 批量导入标准答案

- **URL**: `/api/import/standard-answers`
- **方法**: POST
- **描述**: 批量导入多个标准问题的标准答案
- **内容类型**: `application/json`
- **请求体示例**:

```json
[
  {
    "standardQuestionId": 1,
    "answer": "The 'vmlinux' file is the Linux kernel in a statically linked executable file format...",
    "sourceAnswerId": 1513,
    "sourceType": "raw",
    "keyPoints": [
      {
        "pointText": "vmlinux is a statically linked executable file format of the Linux kernel",
        "pointWeight": 1.0,
        "pointType": "required",
        "exampleText": "The vmlinux file contains the full kernel in ELF format"
      },
      {
        "pointText": "It serves as an intermediate step in the boot procedure",
        "pointWeight": 0.8,
        "pointType": "required",
        "exampleText": "The vmlinux is not directly bootable but is processed further"
      }
    ]
  },
  {
    "standardQuestionId": 2,
    "answer": "To configure network interfaces in Linux, you can use several methods...",
    "sourceAnswerId": 1515,
    "sourceType": "raw",
    "keyPoints": [
      {
        "pointText": "Network interfaces can be configured using command-line tools",
        "pointWeight": 1.0,
        "pointType": "required",
        "exampleText": "Tools like ifconfig, ip, and nmcli can be used"
      },
      {
        "pointText": "Configuration files for network interfaces are typically in /etc/network/",
        "pointWeight": 0.8,
        "pointType": "required",
        "exampleText": "The interfaces file contains network configuration details"
      }
    ]
  }
]
```

- **响应示例**:

```json
{
  "code": 200,
  "message": "批量导入标准答案成功",
  "data": {
    "importedCount": 2,
    "updatedCount": 0,
    "failedCount": 0,
    "details": "成功导入2个标准答案"
  }
}
```

## 标准问答关联API

### 创建标准问答对关联

- **URL**: `/api/import/standard-qa-link`
- **方法**: POST
- **描述**: 创建标准问题和标准答案的关联关系
- **内容类型**: `application/json`
- **请求体示例**:

```json
{
  "standardQuestionId": 1,
  "standardAnswerId": 1,
  "sourceQuestionId": 61,
  "sourceAnswerId": 1513
}
```

- **响应示例**:

```json
{
  "code": 200,
  "message": "标准问答对关联创建成功",
  "data": {
    "qaId": 1,
    "standardQuestionId": 1,
    "standardAnswerId": 1,
    "createdAt": "2023-05-20T12:15:30"
  }
}
```

### 批量创建标准问答对关联

- **URL**: `/api/import/standard-qa-links`
- **方法**: POST
- **描述**: 批量创建标准问题和标准答案的关联关系
- **内容类型**: `application/json`
- **请求体示例**:

```json
[
  {
    "standardQuestionId": 1,
    "standardAnswerId": 1,
    "sourceQuestionId": 61,
    "sourceAnswerId": 1513
  },
  {
    "standardQuestionId": 2,
    "standardAnswerId": 2,
    "sourceQuestionId": 62,
    "sourceAnswerId": 1515
  }
]
```

- **响应示例**:

```json
{
  "code": 200,
  "message": "批量创建标准问答对关联成功",
  "data": {
    "importedCount": 2,
    "updatedCount": 0,
    "failedCount": 0,
    "details": "成功创建2个标准问答对关联"
  }
}
```

# 众包式构建标准问题答案API

## 众包任务管理

### 创建众包任务

- **URL**: `/api/crowdsourcing/tasks`
- **方法**: POST
- **描述**: 创建一个新的众包任务，用于收集标准问题的候选答案
- **内容类型**: `application/json`
- **请求体示例**:

```json
{
  "title": "Linux内核基础知识收集",
  "description": "收集关于Linux内核基础知识问题的高质量回答",
  "taskType": "answer_collection",
  "creatorId": 1,
  "minAnswersPerQuestion": 3,
  "rewardInfo": "每个被采纳的回答奖励10积分",
  "startTime": "2023-06-01T00:00:00",
  "endTime": "2023-06-30T23:59:59",
  "questionIds": [1, 2, 3, 4, 5]
}
```

- **响应示例**:

```json
{
  "code": 200,
  "message": "众包任务创建成功",
  "data": {
    "taskId": 1,
    "title": "Linux内核基础知识收集",
    "taskType": "answer_collection",
    "questionCount": 5,
    "status": "draft",
    "createdAt": "2023-05-21T10:15:30"
  }
}
```

### 获取众包任务列表

- **URL**: `/api/crowdsourcing/tasks`
- **方法**: GET
- **描述**: 获取众包任务列表
- **参数**:
  - `status` (可选): 任务状态筛选 (draft, ongoing, completed, cancelled)
  - `page` (可选): 页码，默认为1
  - `size` (可选): 每页数量，默认为10
- **响应示例**:

```json
{
  "code": 200,
  "message": "获取众包任务列表成功",
  "data": {
    "total": 5,
    "pages": 1,
    "currentPage": 1,
    "tasks": [
      {
        "taskId": 1,
        "title": "Linux内核基础知识收集",
        "taskType": "answer_collection",
        "questionCount": 5,
        "status": "ongoing",
        "startTime": "2023-06-01T00:00:00",
        "endTime": "2023-06-30T23:59:59",
        "createdAt": "2023-05-21T10:15:30"
      },
      {
        "taskId": 2,
        "title": "网络协议问题回答收集",
        "taskType": "answer_collection",
        "questionCount": 8,
        "status": "draft",
        "startTime": null,
        "endTime": null,
        "createdAt": "2023-05-22T14:20:10"
      }
    ]
  }
}
```

### 获取众包任务详情

- **URL**: `/api/crowdsourcing/tasks/{taskId}`
- **方法**: GET
- **描述**: 获取指定众包任务的详细信息
- **响应示例**:

```json
{
  "code": 200,
  "message": "获取众包任务详情成功",
  "data": {
    "taskId": 1,
    "title": "Linux内核基础知识收集",
    "description": "收集关于Linux内核基础知识问题的高质量回答",
    "taskType": "answer_collection",
    "creatorId": 1,
    "creatorName": "admin",
    "questionCount": 5,
    "minAnswersPerQuestion": 3,
    "rewardInfo": "每个被采纳的回答奖励10积分",
    "startTime": "2023-06-01T00:00:00",
    "endTime": "2023-06-30T23:59:59",
    "status": "ongoing",
    "createdAt": "2023-05-21T10:15:30",
    "questions": [
      {
        "questionId": 1,
        "question": "What is the purpose of the 'vmlinux' file in Linux kernel?",
        "currentAnswerCount": 2,
        "isCompleted": false
      },
      {
        "questionId": 2,
        "question": "How to configure network interfaces in Linux?",
        "currentAnswerCount": 3,
        "isCompleted": true
      }
    ]
  }
}
```

### 更新众包任务状态

- **URL**: `/api/crowdsourcing/tasks/{taskId}/status`
- **方法**: PUT
- **描述**: 更新众包任务的状态
- **内容类型**: `application/json`
- **请求体示例**:

```json
{
  "status": "ongoing"
}
```

- **响应示例**:

```json
{
  "code": 200,
  "message": "任务状态更新成功",
  "data": {
    "taskId": 1,
    "status": "ongoing",
    "updatedAt": "2023-05-21T11:30:45"
  }
}
```

## 众包答案管理

### 提交众包答案

- **URL**: `/api/crowdsourcing/answers`
- **方法**: POST
- **描述**: 为标准问题提交一个众包答案
- **内容类型**: `application/json`
- **请求体示例**:

```json
{
  "taskId": 1,
  "standardQuestionId": 1,
  "userId": 5,
  "answerText": "The 'vmlinux' file is the statically linked executable version of the Linux kernel. It is an ELF (Executable and Linkable Format) file that contains the full kernel code, including all its functions and data structures. This file is not directly bootable but serves as an intermediate step in the kernel build process. It is typically compressed and converted into other formats (like bzImage) for actual booting. The vmlinux file is particularly useful for debugging kernel issues as it contains all the symbol information needed by debuggers like GDB."
}
```

- **响应示例**:

```json
{
  "code": 200,
  "message": "众包答案提交成功",
  "data": {
    "answerId": 1,
    "taskId": 1,
    "standardQuestionId": 1,
    "userId": 5,
    "submissionTime": "2023-05-21T14:25:30",
    "reviewStatus": "pending"
  }
}
```

### 批量提交众包答案

- **URL**: `/api/crowdsourcing/answers/batch`
- **方法**: POST
- **描述**: 批量提交多个标准问题的众包答案
- **内容类型**: `application/json`
- **请求体示例**:

```json
[
  {
    "taskId": 1,
    "standardQuestionId": 1,
    "userId": 5,
    "answerText": "The 'vmlinux' file is the statically linked executable version of the Linux kernel..."
  },
  {
    "taskId": 1,
    "standardQuestionId": 2,
    "userId": 5,
    "answerText": "To configure network interfaces in Linux, you can use several methods..."
  }
]
```

- **响应示例**:

```json
{
  "code": 200,
  "message": "批量提交众包答案成功",
  "data": {
    "submittedCount": 2,
    "details": [
      {
        "answerId": 1,
        "standardQuestionId": 1,
        "submissionTime": "2023-05-21T14:25:30"
      },
      {
        "answerId": 2,
        "standardQuestionId": 2,
        "submissionTime": "2023-05-21T14:25:30"
      }
    ]
  }
}
```

### 获取问题的众包答案列表

- **URL**: `/api/crowdsourcing/questions/{questionId}/answers`
- **方法**: GET
- **描述**: 获取指定标准问题的所有众包答案
- **参数**:
  - `reviewStatus` (可选): 筛选审核状态 (pending, approved, rejected)
  - `page` (可选): 页码，默认为1
  - `size` (可选): 每页数量，默认为10
- **响应示例**:

```json
{
  "code": 200,
  "message": "获取众包答案列表成功",
  "data": {
    "total": 3,
    "pages": 1,
    "currentPage": 1,
    "answers": [
      {
        "answerId": 1,
        "userId": 5,
        "userName": "expert_user",
        "answerText": "The 'vmlinux' file is the statically linked executable version of the Linux kernel...",
        "submissionTime": "2023-05-21T14:25:30",
        "reviewStatus": "approved",
        "qualityScore": 8.5,
        "isSelected": false
      },
      {
        "answerId": 3,
        "userId": 8,
        "userName": "linux_expert",
        "answerText": "vmlinux is an ELF executable file that contains the Linux kernel in a non-compressed form...",
        "submissionTime": "2023-05-22T09:15:20",
        "reviewStatus": "approved",
        "qualityScore": 9.2,
        "isSelected": true
      },
      {
        "answerId": 5,
        "userId": 12,
        "userName": "kernel_dev",
        "answerText": "The vmlinux file represents the Linux kernel in its raw, uncompressed form...",
        "submissionTime": "2023-05-23T11:40:15",
        "reviewStatus": "pending",
        "qualityScore": null,
        "isSelected": false
      }
    ]
  }
}
```

### 审核众包答案

- **URL**: `/api/crowdsourcing/answers/{answerId}/review`
- **方法**: PUT
- **描述**: 审核众包答案
- **内容类型**: `application/json`
- **请求体示例**:

```json
{
  "reviewerId": 2,
  "reviewStatus": "approved",
  "qualityScore": 8.5,
  "reviewComment": "回答准确全面，但缺少一些关于调试用途的具体例子"
}
```

- **响应示例**:

```json
{
  "code": 200,
  "message": "众包答案审核成功",
  "data": {
    "answerId": 1,
    "reviewStatus": "approved",
    "qualityScore": 8.5,
    "reviewerId": 2,
    "reviewTime": "2023-05-22T10:30:45"
  }
}
```

### 批量审核众包答案

- **URL**: `/api/crowdsourcing/answers/batch-review`
- **方法**: PUT
- **描述**: 批量审核多个众包答案
- **内容类型**: `application/json`
- **请求体示例**:

```json
{
  "reviewerId": 2,
  "reviews": [
    {
      "answerId": 1,
      "reviewStatus": "approved",
      "qualityScore": 8.5,
      "reviewComment": "回答准确全面，但缺少一些关于调试用途的具体例子"
    },
    {
      "answerId": 2,
      "reviewStatus": "rejected",
      "qualityScore": 4.0,
      "reviewComment": "回答内容不准确，有多处技术错误"
    }
  ]
}
```

- **响应示例**:

```json
{
  "code": 200,
  "message": "批量审核众包答案成功",
  "data": {
    "reviewedCount": 2,
    "approvedCount": 1,
    "rejectedCount": 1,
    "reviewTime": "2023-05-22T10:35:20"
  }
}
```

### 选择众包答案作为标准答案

- **URL**: `/api/crowdsourcing/answers/{answerId}/select`
- **方法**: POST
- **描述**: 将众包答案选为标准答案
- **内容类型**: `application/json`
- **请求体示例**:

```json
{
  "selectedBy": 2,
  "selectionReason": "该回答内容全面准确，解释清晰，是最佳答案",
  "keyPoints": [
    {
      "pointText": "vmlinux是Linux内核的静态链接可执行文件",
      "pointWeight": 1.0,
      "pointType": "required",
      "exampleText": "vmlinux是ELF格式的完整内核文件"
    },
    {
      "pointText": "vmlinux文件在引导过程中是一个中间步骤",
      "pointWeight": 0.8,
      "pointType": "required",
      "exampleText": "vmlinux不能直接引导，需要进一步处理"
    },
    {
      "pointText": "vmlinux文件对内核调试很有用",
      "pointWeight": 0.5,
      "pointType": "bonus",
      "exampleText": "内核开发者使用vmlinux和gdb等工具进行调试"
    }
  ]
}
```

- **响应示例**:

```json
{
  "code": 200,
  "message": "众包答案已选为标准答案",
  "data": {
    "standardAnswerId": 1,
    "standardQuestionId": 1,
    "sourceType": "crowdsourced",
    "sourceId": 1,
    "keyPointsCount": 3,
    "isFinal": true,
    "createdAt": "2023-05-23T15:20:30"
  }
}
```

## 众包答案评分与比较

### 评分众包答案

- **URL**: `/api/crowdsourcing/answers/{answerId}/rate`
- **方法**: POST
- **描述**: 对众包答案进行评分
- **内容类型**: `application/json`
- **请求体示例**:

```json
{
  "raterId": 3,
  "score": 8.5,
  "ratingCriteria": {
    "accuracy": 9.0,
    "completeness": 8.0,
    "clarity": 8.5
  },
  "comment": "回答内容准确，覆盖了问题的主要方面，表述清晰"
}
```

- **响应示例**:

```json
{
  "code": 200,
  "message": "评分成功",
  "data": {
    "ratingId": 1,
    "answerId": 1,
    "raterId": 3,
    "score": 8.5,
    "createdAt": "2023-05-24T09:30:15"
  }
}
```

### 比较两个众包答案

- **URL**: `/api/crowdsourcing/answers/compare`
- **方法**: POST
- **描述**: 比较两个众包答案的质量
- **内容类型**: `application/json`
- **请求体示例**:

```json
{
  "standardQuestionId": 1,
  "answer1Id": 1,
  "answer2Id": 3,
  "comparerId": 2,
  "winner": "answer2",
  "comparisonReason": "答案2更加全面，不仅解释了vmlinux的用途，还详细说明了它在内核开发和调试中的重要性"
}
```

- **响应示例**:

```json
{
  "code": 200,
  "message": "答案比较成功",
  "data": {
    "comparisonId": 1,
    "standardQuestionId": 1,
    "winner": "answer2",
    "createdAt": "2023-05-24T10:45:30"
  }
}
```

### 获取问题的众包答案统计

- **URL**: `/api/crowdsourcing/questions/{questionId}/answers/stats`
- **方法**: GET
- **描述**: 获取指定标准问题的众包答案统计信息
- **响应示例**:

```json
{
  "code": 200,
  "message": "获取众包答案统计成功",
  "data": {
    "standardQuestionId": 1,
    "totalAnswers": 5,
    "approvedAnswers": 3,
    "rejectedAnswers": 1,
    "pendingAnswers": 1,
    "averageScore": 8.2,
    "highestScoredAnswer": {
      "answerId": 3,
      "score": 9.2,
      "userName": "linux_expert"
    },
    "selectedAsStandard": {
      "answerId": 3,
      "selectedBy": "admin",
      "selectionTime": "2023-05-23T15:20:30"
    }
  }
}
```
