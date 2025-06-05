# 标签管理API

## 1. 获取所有标签

**请求**

```
GET /api/tags
```

**响应**

```json
{
  "code": 200,
  "data": [
    {
      "tagId": 1,
      "tagName": "数学",
      "description": "数学相关问题",
      "color": "#409EFF"
    },
    {
      "tagId": 2,
      "tagName": "物理",
      "description": "物理相关问题",
      "color": "#67C23A"
    }
  ],
  "message": "Success"
}
```

## 2. 获取标签详情

**请求**

```
GET /api/tags/{id}
```

**响应**

```json
{
  "code": 200,
  "data": {
    "tagId": 1,
    "tagName": "数学",
    "description": "数学相关问题",
    "color": "#409EFF"
  },
  "message": "Success"
}
```

## 3. 创建标签

**请求**

```
POST /api/tags
```

**请求体**

```json
{
  "tagName": "化学",
  "description": "化学相关问题",
  "color": "#F56C6C"
}
```

**响应**

```json
{
  "code": 200,
  "data": {
    "tagId": 3,
    "tagName": "化学",
    "description": "化学相关问题",
    "color": "#F56C6C"
  },
  "message": "Success"
}
```

## 4. 更新标签

**请求**

```
PUT /api/tags/{id}
```

**请求体**

```json
{
  "tagName": "高等数学",
  "description": "高等数学相关问题",
  "color": "#409EFF"
}
```

**响应**

```json
{
  "code": 200,
  "data": {
    "tagId": 1,
    "tagName": "高等数学",
    "description": "高等数学相关问题",
    "color": "#409EFF"
  },
  "message": "Success"
}
```

## 5. 删除标签

**请求**

```
DELETE /api/tags/{id}
```

**响应**

```json
{
  "code": 200,
  "message": "Success"
}
```

## 6. 获取问题的标签

**请求**

```
GET /api/tags/question/{questionId}
```

**响应**

```json
{
  "code": 200,
  "data": [
    {
      "tagId": 1,
      "tagName": "数学",
      "description": "数学相关问题",
      "color": "#409EFF"
    },
    {
      "tagId": 3,
      "tagName": "代数",
      "description": "代数相关问题",
      "color": "#E6A23C"
    }
  ],
  "message": "Success"
}
```

## 7. 给问题添加标签

**请求**

```
POST /api/tags/question/{questionId}
```

**请求体**

```json
[1, 2, 3]
```

**响应**

```json
{
  "code": 200,
  "message": "Success"
}
```

## 8. 从问题中移除标签

**请求**

```
DELETE /api/tags/question/{questionId}/{tagId}
```

**响应**

```json
{
  "code": 200,
  "message": "Success"
}
```

## 9. 获取带问题数量的标签列表

**请求**

```
GET /api/tags/with-count
```

**响应**

```json
{
  "code": 200,
  "data": {
    "数学": 15,
    "物理": 8,
    "化学": 12,
    "代数": 5
  },
  "message": "Success"
}
```

## 10. 根据标签查询问题

**请求**

```
GET /api/tags/search?tagIds=1,2,3
```

**响应**

```json
{
  "code": 200,
  "data": [101, 102, 105, 108],
  "message": "Success"
}
``` 