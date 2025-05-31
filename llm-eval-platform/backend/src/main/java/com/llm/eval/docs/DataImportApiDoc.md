# 数据导入 API 文档

本文档介绍了LLM评估平台的数据导入API接口，用于将原始问答数据和标准问答数据导入到系统中。

## 基本信息

- 基础路径: `/api/import`
- 认证方式: JWT Token (Authorization Header)

## 1. 导入原始问答数据

通过上传JSON文件导入原始问答数据。

### 请求

- 方法: **POST**
- URL: `/api/import/raw-qa`
- Content-Type: `multipart/form-data`

#### 参数

| 名称 | 类型 | 必填 | 描述 |
| --- | --- | --- | --- |
| file | File | 是 | 包含原始问答数据的JSON文件 |

### 响应

- Content-Type: `application/json`

```json
{
  "success": true,
  "message": "导入完成",
  "imported": 18,
  "failed": 0,
  "total": 18
}
```

### 示例

```bash
curl -X POST "http://localhost:8080/api/import/raw-qa" \
  -H "Authorization: Bearer <your_token>" \
  -F "file=@18个原始问答帖.json"
```

## 2. 导入标准问答数据

通过上传JSON文件导入标准问答数据。

### 请求

- 方法: **POST**
- URL: `/api/import/standard-qa`
- Content-Type: `multipart/form-data`

#### 参数

| 名称 | 类型 | 必填 | 描述 |
| --- | --- | --- | --- |
| file | File | 是 | 包含标准问答数据的JSON文件 |

### 响应

- Content-Type: `application/json`

```json
{
  "success": true,
  "message": "导入完成",
  "imported": 45,
  "failed": 0,
  "total": 45
}
```

### 示例

```bash
curl -X POST "http://localhost:8080/api/import/standard-qa" \
  -H "Authorization: Bearer <your_token>" \
  -F "file=@45个标准问答对.json"
```

## 3. 导入单个原始问题

导入单个原始问题及其答案。

### 请求

- 方法: **POST**
- URL: `/api/import/raw-question`
- Content-Type: `application/json`

#### 请求体

```json
{
  "sourceQuestionId": 61,
  "questionTitle": "What is the difference between the following kernel Makefile terms: vmLinux, vmlinuz, vmlinux.bin, zimage & bzimage?",
  "questionBody": "<p>While browsing through the Kernel Makefiles, I found these terms. So I would like to know what is the difference between <code>vmlinux</code>, <code>vmlinuz</code>, <code>vmlinux.bin</code>, <code>zimage</code> &amp; <code>bzimage</code>?</p>",
  "source": "StackExchange",
  "sourceId": 5518,
  "sourceUrl": "https://unix.stackexchange.com/questions/5518",
  "answer": {
    "sourceAnswerId": 1513,
    "answerBody": "<p><strong>vmlinux</strong></p>\n\n<p>This is the Linux kernel in an statically linked executable file format. Generally, you don't have to worry about this file, it's just a intermediate step in the boot procedure.</p>\n\n<p>The raw vmlinux file may be useful for debugging purposes.</p>",
    "authorInfo": "user123",
    "upvotes": 42,
    "isAccepted": true
  }
}
```

### 响应

- Content-Type: `application/json`

```json
{
  "success": true,
  "message": "导入成功",
  "imported": 1,
  "failed": 0,
  "total": 1
}
```

## 4. 批量导入原始问题

批量导入原始问题及其答案。

### 请求

- 方法: **POST**
- URL: `/api/import/raw-questions`
- Content-Type: `application/json`

#### 请求体

```json
[
  {
    "sourceQuestionId": 61,
    "questionTitle": "What is the difference between kernel Makefile terms?",
    "questionBody": "...",
    "source": "StackExchange",
    "sourceId": 5518,
    "sourceUrl": "https://unix.stackexchange.com/questions/5518",
    "answer": {
      "sourceAnswerId": 1513,
      "answerBody": "...",
      "authorInfo": "user123",
      "upvotes": 42,
      "isAccepted": true
    }
  },
  {
    "sourceQuestionId": 82,
    "questionTitle": "How is a message queue implemented in the Linux kernel?",
    "questionBody": "...",
    "source": "StackExchange",
    "sourceId": 6930,
    "sourceUrl": "https://unix.stackexchange.com/questions/6930",
    "answer": {
      "sourceAnswerId": 444,
      "answerBody": "...",
      "authorInfo": "user456",
      "upvotes": 30,
      "isAccepted": true
    }
  }
]
```

### 响应

- Content-Type: `application/json`

```json
{
  "success": true,
  "message": "导入完成",
  "imported": 2,
  "failed": 0,
  "total": 2
}
```

## 5. 导入单个标准问答对

导入单个标准问题、答案及关键点。

### 请求

- 方法: **POST**
- URL: `/api/import/standard-qa-single`
- Content-Type: `application/json`

#### 请求体

```json
{
  "qaId": 1,
  "sourceQuestionId": 61,
  "sourceAnswerId": 1513,
  "question": "What is the purpose of the 'vmlinux' file in the Linux kernel build process?",
  "answer": "The 'vmlinux' file is the Linux kernel in a statically linked executable file format. It is an intermediate step in the boot procedure and is generally not directly used for booting. The raw 'vmlinux' file may be useful for debugging purposes.",
  "keyPoints": [
    "vmlinux is a statically linked executable file format of the Linux kernel.",
    "It serves as an intermediate step in the boot procedure.",
    "The raw 'vmlinux' file can be useful for debugging."
  ]
}
```

### 响应

- Content-Type: `application/json`

```json
{
  "success": true,
  "message": "导入成功",
  "imported": 1,
  "failed": 0,
  "total": 1
}
```

## 6. 批量导入标准问答对

批量导入标准问题、答案及关键点。

### 请求

- 方法: **POST**
- URL: `/api/import/standard-qas`
- Content-Type: `application/json`

#### 请求体

```json
[
  {
    "qaId": 1,
    "sourceQuestionId": 61,
    "sourceAnswerId": 1513,
    "question": "What is the purpose of the 'vmlinux' file in the Linux kernel build process?",
    "answer": "The 'vmlinux' file is the Linux kernel in a statically linked executable file format. It is an intermediate step in the boot procedure and is generally not directly used for booting. The raw 'vmlinux' file may be useful for debugging purposes.",
    "keyPoints": [
      "vmlinux is a statically linked executable file format of the Linux kernel.",
      "It serves as an intermediate step in the boot procedure.",
      "The raw 'vmlinux' file can be useful for debugging."
    ]
  },
  {
    "qaId": 2,
    "sourceQuestionId": 61,
    "sourceAnswerId": 1513,
    "question": "What is the difference between 'vmlinux' and 'vmlinux.bin'?",
    "answer": "The 'vmlinux.bin' file is the same as 'vmlinux', but in a bootable raw binary file format where all symbols and relocation information is discarded.",
    "keyPoints": [
      "vmlinux.bin is a bootable raw binary file format of the kernel.",
      "It is generated from 'vmlinux' by discarding symbols and relocation information."
    ]
  }
]
```

### 响应

- Content-Type: `application/json`

```json
{
  "success": true,
  "message": "导入完成",
  "imported": 2,
  "failed": 0,
  "total": 2
}
```

## 命令行导入工具

除了API接口外，系统还提供了命令行工具用于数据导入，可以在服务器端直接使用。

### 使用方法

```bash
# 导入原始问答数据
java -jar app.jar --import.enabled=true --import.type=raw-qa --import.file=/path/to/18个原始问答帖.json

# 导入标准问答数据
java -jar app.jar --import.enabled=true --import.type=standard-qa --import.file=/path/to/45个标准问答对.json
``` 