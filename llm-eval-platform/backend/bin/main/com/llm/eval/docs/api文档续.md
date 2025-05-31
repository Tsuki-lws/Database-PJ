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



下面的不需要



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
