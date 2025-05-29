# LLM评测平台ERD图设计指南

## ERD图绘制建议

您可以使用以下工具之一绘制ERD图：
- MySQL Workbench (可以直接导入schema.sql)
- Draw.io
- Lucidchart
- ERDPlus
- Visual Paradigm

## 实体关系概览

下面是系统主要实体及其关系的概览，可以作为绘制ERD图的基础：

### 核心实体

1. **raw_questions** (原始问题)
2. **raw_answers** (原始回答)
3. **standard_questions** (标准问题)
4. **standard_answers** (标准答案)
5. **objective_question_options** (客观题选项)
6. **answer_key_points** (答案关键点)
7. **users** (用户)
8. **llm_models** (LLM模型)
9. **llm_answers** (模型回答)
10. **evaluations** (评测结果)

### 主要实体关系

#### 原始数据关系
- `raw_questions` 1:N `raw_answers` (一个原始问题有多个原始回答)

#### 标准化数据关系
- `raw_questions` 1:N `standard_questions` (一个原始问题可以转化为多个标准问题)
- `standard_questions` 1:N `standard_answers` (一个标准问题有多个标准答案)
- `standard_questions` 1:N `objective_question_options` (一个客观题有多个选项)
- `standard_answers` 1:N `answer_key_points` (一个标准答案有多个评分关键点)

#### 分类和标签关系
- `question_categories` 1:N `question_categories` (分类自引用，形成层次结构)
- `question_categories` 1:N `standard_questions` (一个分类下有多个标准问题)
- `tags` N:M `standard_questions` (通过`standard_question_tags`关联)

#### 评测关系
- `standard_questions` 1:N `llm_answers` (一个标准问题有多个模型回答)
- `llm_models` 1:N `llm_answers` (一个模型生成多个回答)
- `llm_answers` 1:N `evaluations` (一个模型回答有多个评测记录)
- `standard_answers` 1:N `evaluations` (一个标准答案用于多个评测)
- `evaluations` 1:N `evaluation_key_points` (一个评测包含多个关键点评估)
- `answer_key_points` 1:N `evaluation_key_points` (一个关键点在多个评测中被评估)

#### 数据集关系
- `dataset_versions` N:M `standard_questions` (通过`dataset_question_mapping`关联)

#### 版本历史关系
- `standard_questions` 1:N `standard_question_versions` (一个标准问题有多个历史版本)
- `standard_answers` 1:N `standard_answer_versions` (一个标准答案有多个历史版本)
- `answer_key_points` 1:N `key_point_versions` (一个关键点有多个历史版本)
- `objective_question_options` 1:N `option_versions` (一个选项有多个历史版本)

#### 众包关系
- `users` 1:N `crowdsourcing_tasks` (一个用户创建多个众包任务)
- `crowdsourcing_tasks` N:M `standard_questions` (通过`crowdsourcing_task_questions`关联)
- `standard_questions` 1:N `crowdsourced_answers` (一个标准问题有多个众包答案)
- `users` 1:N `crowdsourced_answers` (一个用户提交多个众包答案)
- `users` 1:N `expert_answers` (一个专家提供多个专家答案)
- `standard_questions` 1:N `expert_answers` (一个标准问题有多个专家答案)

## ERD图绘制要点

1. **表示实体**：
   - 使用矩形表示实体（表）
   - 在矩形内部列出主要属性
   - 用下划线标识主键，用斜体标识外键

2. **表示关系**：
   - 使用菱形或线段连接相关实体
   - 在关系上标注基数（1:1, 1:N, N:M）
   - 对于多对多关系，显示关联表

3. **分组相关实体**：
   - 原始数据实体组：raw_questions, raw_answers
   - 标准化数据实体组：standard_questions, standard_answers, answer_key_points, objective_question_options
   - 版本历史实体组：各种_versions表
   - 评测实体组：llm_models, llm_answers, evaluations, evaluation_key_points
   - 众包实体组：crowdsourcing_tasks, crowdsourced_answers, expert_answers
   - 数据集实体组：dataset_versions, dataset_question_mapping

4. **色彩编码**（可选）：
   - 为不同组的实体使用不同颜色
   - 使用不同线型区分关系类型（如外键、多对多）

## 实体主要属性概览

为了保持ERD图的可读性，建议每个实体只显示以下关键属性：

1. **原始问题 (raw_questions)**
   - question_id (PK)
   - question_title
   - question_body
   - source
   - created_at

2. **标准问题 (standard_questions)**
   - standard_question_id (PK)
   - question
   - question_type
   - difficulty
   - version
   - source_question_id (FK)
   - category_id (FK)

3. **标准答案 (standard_answers)**
   - standard_answer_id (PK)
   - standard_question_id (FK)
   - answer
   - is_final
   - version

4. **答案关键点 (answer_key_points)**
   - key_point_id (PK)
   - standard_answer_id (FK)
   - point_text
   - point_weight
   - point_type
   - version

5. **客观题选项 (objective_question_options)**
   - option_id (PK)
   - standard_question_id (FK)
   - option_text
   - option_code
   - is_correct

6. **LLM模型 (llm_models)**
   - model_id (PK)
   - name
   - version
   - provider

7. **LLM回答 (llm_answers)**
   - llm_answer_id (PK)
   - model_id (FK)
   - standard_question_id (FK)
   - content
   - batch_id (FK)

8. **评测结果 (evaluations)**
   - evaluation_id (PK)
   - llm_answer_id (FK)
   - standard_answer_id (FK)
   - score
   - method

## 使用schema.sql生成ERD

如果您使用MySQL Workbench，可以直接导入schema.sql文件来自动生成ERD图：

1. 打开MySQL Workbench
2. 选择"File" > "New Model"
3. 右键点击左侧导航窗格的"Physical Schemas"，选择"Create Schema..."
4. 创建名为"llm_eval"的schema
5. 选择"File" > "Import" > "Reverse Engineer MySQL Create Script..."
6. 选择您的schema.sql文件
7. 选择刚刚创建的schema
8. 点击"Next"直到完成
9. 在左侧导航窗格中找到并展开您的schema
10. 双击"EER Diagram"（如果没有，右键点击schema，选择"Create EER Diagram From Catalog Objects..."）

这样您就可以获得一个基于实际数据库结构的ERD图，然后根据需要进行调整和美化。
