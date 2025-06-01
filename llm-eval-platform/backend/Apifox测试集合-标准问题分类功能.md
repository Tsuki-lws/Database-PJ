# Apifox 测试集合 - 标准问题分类功能

## 测试环境配置

### 环境变量设置
```
BASE_URL: http://localhost:8080
QUESTION_ID: 1
CATEGORY_ID: 2
INVALID_ID: 99999
```

## 测试用例集合

### 1. 前置条件检查

#### 1.1 检查问题是否存在
```
GET {{BASE_URL}}/api/questions/{{QUESTION_ID}}
```

**期望结果**: 状态码 200，返回问题详情

#### 1.2 检查分类是否存在
```
GET {{BASE_URL}}/api/categories
```

**期望结果**: 状态码 200，返回分类列表，确认 categoryId=2 存在

### 2. 正常功能测试

#### 2.1 为问题设置分类
```
PUT {{BASE_URL}}/api/questions/{{QUESTION_ID}}/category
Content-Type: application/json

{
  "categoryId": {{CATEGORY_ID}}
}
```

**期望结果**: 
- 状态码: 200
- 响应包含 category 字段且 categoryId = 2
- version 字段值增加
- updatedAt 字段更新

**测试脚本**:
```javascript
pm.test("设置分类成功", function () {
    pm.response.to.have.status(200);
    var jsonData = pm.response.json();
    pm.expect(jsonData.category).to.not.be.null;
    pm.expect(jsonData.category.categoryId).to.eql(2);
});

pm.test("版本号递增", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData.version).to.be.above(0);
});
```

#### 2.2 验证分类设置结果
```
GET {{BASE_URL}}/api/questions/{{QUESTION_ID}}
```

**期望结果**: 问题的 category 字段不为空，categoryId = 2

#### 2.3 更改问题分类
```
PUT {{BASE_URL}}/api/questions/{{QUESTION_ID}}/category
Content-Type: application/json

{
  "categoryId": 3
}
```

**期望结果**: 分类更新为 categoryId = 3

#### 2.4 移除问题分类
```
PUT {{BASE_URL}}/api/questions/{{QUESTION_ID}}/category
Content-Type: application/json

{
  "categoryId": null
}
```

**期望结果**: 
- 状态码: 200
- category 字段为 null
- version 字段再次增加

**测试脚本**:
```javascript
pm.test("移除分类成功", function () {
    pm.response.to.have.status(200);
    var jsonData = pm.response.json();
    pm.expect(jsonData.category).to.be.null;
});
```

#### 2.5 按分类查询问题
```
GET {{BASE_URL}}/api/questions/category/{{CATEGORY_ID}}
```

**期望结果**: 返回该分类下的所有问题列表

### 3. 异常情况测试

#### 3.1 不存在的问题ID
```
PUT {{BASE_URL}}/api/questions/{{INVALID_ID}}/category
Content-Type: application/json

{
  "categoryId": {{CATEGORY_ID}}
}
```

**期望结果**: 状态码 404

**测试脚本**:
```javascript
pm.test("问题不存在返回404", function () {
    pm.response.to.have.status(404);
});
```

#### 3.2 不存在的分类ID
```
PUT {{BASE_URL}}/api/questions/{{QUESTION_ID}}/category
Content-Type: application/json

{
  "categoryId": {{INVALID_ID}}
}
```

**期望结果**: 状态码 404

#### 3.3 无效的请求格式
```
PUT {{BASE_URL}}/api/questions/{{QUESTION_ID}}/category
Content-Type: application/json

{
  "invalidField": "invalidValue"
}
```

**期望结果**: 状态码 400

#### 3.4 空请求体
```
PUT {{BASE_URL}}/api/questions/{{QUESTION_ID}}/category
Content-Type: application/json

{}
```

**期望结果**: categoryId 为 null，相当于移除分类

### 4. 数据一致性验证

#### 4.1 并发更新测试
设置两个相同的请求同时执行，验证数据一致性

#### 4.2 版本控制验证
连续多次更新，验证版本号正确递增

## Apifox 导入步骤

### 方法一：手动创建

1. **创建新项目**
   - 项目名称: "LLM评估平台-分类功能"
   - 基础URL: http://localhost:8080

2. **创建接口分组**
   - 分组1: "标准问题管理"
   - 分组2: "分类管理"

3. **创建接口**
   按照上述API列表创建各个接口

4. **创建环境**
   - 环境名称: "本地开发"
   - 变量配置如上所示

5. **创建测试用例**
   按照测试用例集合创建自动化测试

### 方法二：导入OpenAPI文档

1. 将上面生成的 `标准问题分类API-OpenAPI.json` 文件导入Apifox
2. 创建环境变量
3. 添加测试脚本

## 测试数据准备

### 数据库准备脚本
```sql
-- 确保有测试分类
INSERT INTO question_categories (category_id, name, description) VALUES 
(1, '编程语言', '各种编程语言相关问题'),
(2, 'Java编程', 'Java相关的编程问题'),
(3, 'Python编程', 'Python相关的编程问题');

-- 确保有测试问题
INSERT INTO standard_questions (standard_question_id, question, question_type, difficulty, status, created_at, updated_at, version) VALUES 
(1, '什么是Java中的多态性？', 'MULTIPLE_CHOICE', 'MEDIUM', 'ACTIVE', NOW(), NOW(), 1);
```

## 执行顺序建议

1. **环境检查** → 检查服务是否启动
2. **数据准备** → 确认测试数据存在
3. **正常流程** → 执行正常功能测试
4. **异常处理** → 执行异常情况测试
5. **数据验证** → 验证数据一致性
6. **清理数据** → 恢复测试环境

## 自动化测试脚本

### 全局前置脚本
```javascript
// 设置通用变量
pm.globals.set("timestamp", new Date().toISOString());

// 通用响应时间检查
pm.test("Response time is less than 2000ms", function () {
    pm.expect(pm.response.responseTime).to.be.below(2000);
});
```

### 全局后置脚本
```javascript
// 记录测试结果
console.log("API:", pm.request.url);
console.log("Method:", pm.request.method);
console.log("Status:", pm.response.status());
console.log("Response Time:", pm.response.responseTime + "ms");
```

这些文档和配置可以帮助您在 Apifox 中完整地测试标准问题分类功能。
