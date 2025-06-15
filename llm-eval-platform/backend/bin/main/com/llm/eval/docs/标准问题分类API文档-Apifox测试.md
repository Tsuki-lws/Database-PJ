# 标准问题分类功能 API 文档 - Apifox 测试

## API 接口列表

### 1. 更新问题分类

**基本信息**
- **接口名称**: 更新问题分类
- **请求方法**: PUT
- **接口路径**: `/api/questions/{id}/category`
- **接口描述**: 为指定的标准问题设置或移除分类

**路径参数**
| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| id | Integer | 是 | 标准问题的ID |

**请求体参数 (JSON)**
```json
{
  "categoryId": 2
}
```

| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| categoryId | Integer | 否 | 分类ID，可以为null表示移除分类 |

**请求示例**

1. **设置分类**
```
PUT http://localhost:8080/api/questions/1/category
Content-Type: application/json

{
  "categoryId": 2
}
```

2. **移除分类**
```
PUT http://localhost:8080/api/questions/1/category
Content-Type: application/json

{
  "categoryId": null
}
```

**响应示例**

**成功响应 (200 OK)**
```json
{
  "standardQuestionId": 1,
  "question": "什么是Java中的多态性？",
  "category": {
    "categoryId": 2,
    "name": "Java编程",
    "description": "Java相关的编程问题",
    "parent": null,
    "children": [],
    "questions": []
  },
  "questionType": "MULTIPLE_CHOICE",
  "difficulty": "MEDIUM",
  "sourceQuestion": null,
  "status": "ACTIVE",
  "createdAt": "2024-01-15T10:00:00",
  "updatedAt": "2024-01-15T14:30:00",
  "deletedAt": null,
  "version": 2,
  "tags": []
}
```

**错误响应**

**问题不存在 (404 Not Found)**
```json
{
  "timestamp": "2024-01-15T14:30:00.000+00:00",
  "status": 404,
  "error": "Not Found",
  "path": "/api/questions/999/category"
}
```

**分类不存在 (404 Not Found)**
```json
{
  "timestamp": "2024-01-15T14:30:00.000+00:00",
  "status": 404,
  "error": "Not Found",
  "path": "/api/questions/1/category"
}
```

**请求格式错误 (400 Bad Request)**
```json
{
  "timestamp": "2024-01-15T14:30:00.000+00:00",
  "status": 400,
  "error": "Bad Request",
  "path": "/api/questions/1/category"
}
```

---

### 2. 按分类查询问题

**基本信息**
- **接口名称**: 按分类查询问题
- **请求方法**: GET
- **接口路径**: `/api/questions/category/{categoryId}`
- **接口描述**: 获取指定分类下的所有标准问题

**路径参数**
| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| categoryId | Integer | 是 | 分类ID |

**请求示例**
```
GET http://localhost:8080/api/questions/category/2
```

**响应示例**

**成功响应 (200 OK)**
```json
[
  {
    "standardQuestionId": 1,
    "question": "什么是Java中的多态性？",
    "category": {
      "categoryId": 2,
      "name": "Java编程",
      "description": "Java相关的编程问题"
    },
    "questionType": "MULTIPLE_CHOICE",
    "difficulty": "MEDIUM",
    "status": "ACTIVE",
    "createdAt": "2024-01-15T10:00:00",
    "updatedAt": "2024-01-15T14:30:00",
    "version": 2
  },
  {
    "standardQuestionId": 3,
    "question": "解释Java中的接口和抽象类的区别",
    "category": {
      "categoryId": 2,
      "name": "Java编程",
      "description": "Java相关的编程问题"
    },
    "questionType": "SHORT_ANSWER",
    "difficulty": "HARD",
    "status": "ACTIVE",
    "createdAt": "2024-01-15T11:00:00",
    "updatedAt": "2024-01-15T11:00:00",
    "version": 1
  }
]
```

---

### 3. 获取所有分类

**基本信息**
- **接口名称**: 获取所有分类
- **请求方法**: GET
- **接口路径**: `/api/categories`
- **接口描述**: 获取系统中所有的问题分类

**请求示例**
```
GET http://localhost:8080/api/categories
```

**响应示例**

**成功响应 (200 OK)**
```json
[
  {
    "categoryId": 1,
    "name": "编程语言",
    "description": "各种编程语言相关问题",
    "parent": null,
    "children": [
      {
        "categoryId": 2,
        "name": "Java编程",
        "description": "Java相关的编程问题"
      },
      {
        "categoryId": 3,
        "name": "Python编程",
        "description": "Python相关的编程问题"
      }
    ]
  },
  {
    "categoryId": 4,
    "name": "数据结构与算法",
    "description": "数据结构和算法相关问题",
    "parent": null,
    "children": []
  }
]
```

---

### 4. 获取单个问题详情

**基本信息**
- **接口名称**: 获取单个问题详情
- **请求方法**: GET
- **接口路径**: `/api/questions/{id}`
- **接口描述**: 根据ID获取标准问题的详细信息

**路径参数**
| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| id | Integer | 是 | 标准问题的ID |

**请求示例**
```
GET http://localhost:8080/api/questions/1
```

**响应示例** (同更新分类的成功响应)

---

## Apifox 测试环境配置

### 环境变量
```
BASE_URL: http://localhost:8080
```

### 前置脚本 (如果需要认证)
```javascript
// 如果需要JWT token或其他认证
pm.environment.set("token", "your_jwt_token_here");
```

### 测试脚本示例

**分类更新接口测试脚本**
```javascript
// 验证状态码
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

// 验证响应结构
pm.test("Response has required fields", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData).to.have.property('standardQuestionId');
    pm.expect(jsonData).to.have.property('question');
    pm.expect(jsonData).to.have.property('category');
    pm.expect(jsonData).to.have.property('updatedAt');
    pm.expect(jsonData).to.have.property('version');
});

// 验证分类信息
pm.test("Category is correctly set", function () {
    var jsonData = pm.response.json();
    var requestData = JSON.parse(pm.request.body.raw);
    
    if (requestData.categoryId !== null) {
        pm.expect(jsonData.category).to.not.be.null;
        pm.expect(jsonData.category.categoryId).to.eql(requestData.categoryId);
    } else {
        pm.expect(jsonData.category).to.be.null;
    }
});

// 验证版本号递增
pm.test("Version number is incremented", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData.version).to.be.above(0);
});
```

## 测试用例设计

### 正常流程测试

1. **创建测试数据**
   - 先确保数据库中有测试问题和分类数据
   - 记录现有问题的版本号

2. **测试分类设置**
   - 为问题设置分类
   - 验证响应数据正确
   - 验证版本号递增

3. **测试分类更改**
   - 将问题改为另一个分类
   - 验证分类更新成功

4. **测试分类移除**
   - 将问题的分类设置为null
   - 验证分类被移除

### 异常情况测试

1. **不存在的问题ID**
   - 使用不存在的问题ID
   - 验证返回404错误

2. **不存在的分类ID**
   - 使用不存在的分类ID
   - 验证返回404错误

3. **无效的请求格式**
   - 发送格式错误的JSON
   - 验证返回400错误

### 数据验证测试

1. **查询验证**
   - 更新分类后查询问题详情
   - 通过分类查询问题列表
   - 验证数据一致性

2. **时间戳验证**
   - 验证updatedAt字段更新
   - 验证createdAt字段不变

## 测试顺序建议

1. 获取所有分类 → 获取可用的分类ID
2. 获取问题详情 → 获取问题当前状态
3. 更新问题分类 → 设置为某个分类
4. 验证更新结果 → 再次获取问题详情
5. 按分类查询 → 验证问题出现在分类列表中
6. 移除分类 → 设置分类为null
7. 验证移除结果 → 最终验证

这样的API文档可以直接导入到Apifox中进行系统的接口测试。
