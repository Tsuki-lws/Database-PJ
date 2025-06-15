# LLM评估平台 - SQL功能说明文档

## 数据库架构概述

本评估平台采用了一个完善的关系型数据库设计，用于支持对大语言模型(LLM)的评估工作。数据库包含了多个相互关联的表，以支持从原始问题收集到标准化、众包评估以及最终的模型评测等完整流程。

## 核心表结构与功能说明

### 问题与答案管理

#### raw_questions 和 raw_answers
- 存储从问答社区抓取的原始问题和回答
- 记录来源网站和URL，便于溯源
- 支持软删除功能，不会永久删除有价值的数据

#### standard_questions 和 standard_answers
- 存储经过标准化处理的高质量问题和标准答案
- 支持版本控制系统，记录每次修改的历史
- 关联原始数据源，保持数据可追溯性

#### 版本控制表（standard_question_versions、standard_answer_versions等）
- 记录标准问题和答案的修改历史
- 存储每个版本的完整内容和修改原因
- 跟踪修改人信息和修改时间

### 评测数据组织

#### question_categories 和 tags
- 提供层级化的问题分类体系
- 支持通过标签灵活管理问题特性
- 多维度组织和检索评测数据

#### dataset_versions 和 dataset_question_mapping
- 管理评测数据集的不同版本
- 灵活配置每个数据集版本包含的问题
- 追踪数据集的发布状态和问题数量

### 众包系统

#### crowdsourcing_tasks 和 crowdsourced_answers
- 支持创建和管理众包任务
- 收集高质量的多样化答案
- 提供答案质量评估和审核流程

#### answer_ratings 和 answer_comparisons
- 支持对答案进行评分和比较
- 基于多维度标准评估答案质量
- 通过两两比较选出最佳答案

### 模型评测

#### llm_models 和 llm_answers
- 管理不同LLM模型的信息
- 存储模型生成的答案
- 记录模型响应时间和资源消耗

#### evaluation_batches 和 evaluations
- 组织批量评测任务
- 存储评测结果和关键点评估
- 支持多种评测方法（人工/自动/裁判模型）

#### judge_tasks
- 管理裁判模型评测任务
- 跟踪任务执行状态和完成时间
- 存储评分结果和评分理由

## 存储过程

数据库包含三个核心存储过程，用于维护版本控制系统：

### update_standard_question
```sql
CREATE PROCEDURE update_standard_question(
    IN p_question_id INT,
    IN p_new_question TEXT,
    IN p_category_id INT,
    IN p_question_type ENUM('single_choice', 'multiple_choice', 'simple_fact', 'subjective'),
    IN p_difficulty ENUM('easy', 'medium', 'hard'),
    IN p_user_id INT,
    IN p_change_reason TEXT
)
```
- **功能**：更新标准问题并自动保存历史版本
- **处理逻辑**：
  1. 获取问题当前版本号
  2. 将当前问题内容保存到历史版本表
  3. 更新问题内容并递增版本号
- **应用场景**：标准问题的修改与迭代

### update_standard_answer
```sql
CREATE PROCEDURE update_standard_answer(
    IN p_answer_id INT,
    IN p_new_answer TEXT,
    IN p_user_id INT,
    IN p_change_reason TEXT
)
```
- **功能**：更新标准答案并自动保存历史版本
- **处理逻辑**：
  1. 获取答案当前版本号
  2. 将当前答案内容保存到历史版本表
  3. 更新答案内容并递增版本号
- **应用场景**：标准答案的修改与优化

### update_key_point
```sql
CREATE PROCEDURE update_key_point(
    IN p_key_point_id INT,
    IN p_new_point_text TEXT,
    IN p_point_weight DECIMAL(5,2),
    IN p_point_type ENUM('required', 'bonus', 'penalty'),
    IN p_example_text TEXT,
    IN p_user_id INT,
    IN p_change_reason TEXT
)
```
- **功能**：更新答案关键点并自动保存历史版本
- **处理逻辑**：
  1. 获取关键点当前版本号
  2. 将当前关键点内容保存到历史版本表
  3. 更新关键点内容并递增版本号
- **应用场景**：评分关键点的调整与改进

## 索引设计

数据库包含多个精心设计的索引，以优化不同查询场景下的性能：

1. **检索索引**：对外键、状态字段等常用查询条件创建索引，如`idx_standard_questions_category`
2. **软删除优化**：对软删除标记字段添加索引，如`idx_raw_questions_deleted`
3. **关联查询索引**：对多表关联查询的外键创建索引，如`idx_standard_qa_pairs`
4. **版本控制索引**：对版本相关字段创建索引，如`idx_standard_question_versions`
5. **状态查询索引**：对任务状态等字段创建索引，如`idx_crowdsourcing_tasks_status`

## 触发器说明

当前数据库设计中没有显式定义触发器，主要通过存储过程来处理版本控制等功能。存储过程提供了更加可控的事务处理能力，而且可以被应用程序明确调用，有助于调试和错误跟踪。

## 数据完整性保障

数据库通过以下机制保障数据完整性：

1. **外键约束**：确保关联数据的一致性
2. **软删除机制**：通过`deleted_at`字段实现安全删除
3. **版本控制**：保留所有历史修改记录
4. **默认值设置**：为重要字段设置合理的默认值
5. **枚举类型**：使用枚举类型限制值域范围

## 扩展性考虑

数据库设计具有良好的扩展性：

1. 分类系统支持多层级嵌套，可适应未来更复杂的分类需求
2. 评测方法和标准可以灵活扩展
3. 版本控制系统可应用于更多实体类型
4. JSON类型字段可存储灵活的结构化数据，如API配置和评测结果

## 测试数据

数据库预置了10个测试用户，包括：
- 1个管理员用户
- 5个普通用户
- 2个专家用户
- 2个审核员用户

这些测试用户可用于系统功能测试和权限验证。 