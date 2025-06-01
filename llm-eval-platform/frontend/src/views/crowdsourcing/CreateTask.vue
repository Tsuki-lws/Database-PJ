<template>
  <div class="task-create-container">
    <div class="page-header">
      <h2>创建众包任务</h2>
      <el-button @click="navigateBack">返回</el-button>
    </div>

    <el-card shadow="never" class="form-card" v-loading="loading">
      <el-form :model="taskForm" :rules="rules" ref="taskFormRef" label-width="120px">
        <el-form-item label="任务标题" prop="title">
          <el-input v-model="taskForm.title" placeholder="请输入任务标题"></el-input>
        </el-form-item>

        <el-form-item label="任务描述" prop="description">
          <el-input 
            v-model="taskForm.description" 
            type="textarea" 
            :rows="4" 
            placeholder="请输入任务描述">
          </el-input>
        </el-form-item>

        <el-form-item label="任务类型" prop="taskType">
          <el-select v-model="taskForm.taskType" placeholder="请选择任务类型">
            <el-option label="答案收集" value="answer_collection"></el-option>
            <el-option label="答案审核" value="answer_review"></el-option>
            <el-option label="答案评分" value="answer_rating"></el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="每题答案数量" prop="minAnswersPerQuestion">
          <el-input-number 
            v-model="taskForm.minAnswersPerQuestion" 
            :min="1" 
            :max="10">
          </el-input-number>
        </el-form-item>

        <el-form-item label="奖励信息" prop="rewardInfo">
          <el-input 
            v-model="taskForm.rewardInfo" 
            type="textarea" 
            :rows="2" 
            placeholder="请输入奖励信息">
          </el-input>
        </el-form-item>

        <el-form-item label="开始时间">
          <el-date-picker
            v-model="taskForm.startTime"
            type="datetime"
            placeholder="选择开始时间">
          </el-date-picker>
        </el-form-item>

        <el-form-item label="结束时间">
          <el-date-picker
            v-model="taskForm.endTime"
            type="datetime"
            placeholder="选择结束时间">
          </el-date-picker>
        </el-form-item>

        <el-divider content-position="left">选择问题</el-divider>

        <el-form-item label="问题选择" prop="questionIds">
          <div class="question-selection">
            <div class="question-search">
              <el-input
                v-model="questionSearchKeyword"
                placeholder="搜索问题"
                clearable
                @keyup.enter="searchQuestions">
                <template #append>
                  <el-button @click="searchQuestions">
                    <el-icon><Search /></el-icon>
                  </el-button>
                </template>
              </el-input>
            </div>
            
            <div class="question-filter">
              <el-select v-model="questionFilter.categoryId" placeholder="分类" clearable>
                <el-option 
                  v-for="item in categoryOptions" 
                  :key="item.value" 
                  :label="item.label" 
                  :value="item.value">
                </el-option>
              </el-select>
              
              <el-select v-model="questionFilter.difficulty" placeholder="难度" clearable>
                <el-option label="简单" value="easy"></el-option>
                <el-option label="中等" value="medium"></el-option>
                <el-option label="困难" value="hard"></el-option>
              </el-select>
              
              <el-button type="primary" @click="searchQuestions">筛选</el-button>
            </div>
            
            <div class="question-table">
              <el-table
                v-loading="questionLoading"
                :data="questionList"
                @selection-change="handleSelectionChange">
                <el-table-column type="selection" width="55"></el-table-column>
                <el-table-column prop="standardQuestionId" label="ID" width="80"></el-table-column>
                <el-table-column prop="question" label="问题内容" show-overflow-tooltip></el-table-column>
                <el-table-column prop="difficulty" label="难度" width="100">
                  <template #default="scope">
                    <el-tag :type="getDifficultyTag(scope.row.difficulty)">
                      {{ formatDifficulty(scope.row.difficulty) }}
                    </el-tag>
                  </template>
                </el-table-column>
              </el-table>
              
              <div class="pagination-container">
                <el-pagination
                  v-model:current-page="questionQuery.page"
                  v-model:page-size="questionQuery.size"
                  :page-sizes="[10, 20, 50]"
                  layout="total, sizes, prev, pager, next"
                  :total="questionTotal"
                  @size-change="handleSizeChange"
                  @current-change="handleCurrentChange">
                </el-pagination>
              </div>
            </div>
            
            <div class="selected-count">
              已选择 {{ taskForm.questionIds.length }} 个问题
            </div>
          </div>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="submitForm">保存</el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { createTask, CrowdsourcingTask } from '@/api/crowdsourcing'
import { getQuestionList } from '@/api/question'
import { getAllCategories } from '@/api/category'

const router = useRouter()
const loading = ref(false)
const questionLoading = ref(false)
const taskFormRef = ref()

// 表单数据
const taskForm = reactive<CrowdsourcingTask>({
  title: '',
  description: '',
  taskType: 'answer_collection',
  creatorId: 1, // 假设当前用户ID为1
  minAnswersPerQuestion: 3,
  rewardInfo: '',
  startTime: '',
  endTime: '',
  questionIds: []
})

// 表单验证规则
const rules = {
  title: [
    { required: true, message: '请输入任务标题', trigger: 'blur' }
  ],
  taskType: [
    { required: true, message: '请选择任务类型', trigger: 'change' }
  ],
  minAnswersPerQuestion: [
    { required: true, message: '请输入每题答案数量', trigger: 'blur' }
  ],
  questionIds: [
    { type: 'array', required: true, message: '请选择至少一个问题', trigger: 'change' }
  ]
}

// 问题搜索关键词
const questionSearchKeyword = ref('')

// 问题筛选条件
const questionFilter = reactive({
  categoryId: undefined,
  difficulty: undefined
})

// 问题查询参数
const questionQuery = reactive({
  page: 1,
  size: 10,
  keyword: '',
  categoryId: undefined,
  difficulty: undefined
})

// 问题列表
const questionList = ref([])
const questionTotal = ref(0)

// 分类选项
const categoryOptions = ref([])

// 获取分类列表
const getCategories = async () => {
  try {
    const res = await getAllCategories()
    categoryOptions.value = res.map((item: any) => ({
      value: item.categoryId,
      label: item.name
    }))
  } catch (error) {
    console.error('获取分类列表失败', error)
  }
}

// 搜索问题
const searchQuestions = () => {
  questionQuery.keyword = questionSearchKeyword.value
  questionQuery.categoryId = questionFilter.categoryId
  questionQuery.difficulty = questionFilter.difficulty
  questionQuery.page = 1
  fetchQuestions()
}

// 获取问题列表
const fetchQuestions = async () => {
  questionLoading.value = true
  try {
    const res = await getQuestionList(questionQuery)
    questionList.value = res.list || []
    questionTotal.value = res.total || 0
  } catch (error) {
    console.error('获取问题列表失败', error)
  } finally {
    questionLoading.value = false
  }
}

// 处理表格选择变化
const handleSelectionChange = (selection: any[]) => {
  taskForm.questionIds = selection.map(item => item.standardQuestionId)
}

// 处理每页数量变化
const handleSizeChange = (val: number) => {
  questionQuery.size = val
  fetchQuestions()
}

// 处理页码变化
const handleCurrentChange = (val: number) => {
  questionQuery.page = val
  fetchQuestions()
}

// 格式化难度
const formatDifficulty = (difficulty: string) => {
  const map: Record<string, string> = {
    'easy': '简单',
    'medium': '中等',
    'hard': '困难'
  }
  return map[difficulty] || difficulty
}

// 获取难度标签类型
const getDifficultyTag = (difficulty: string) => {
  const map: Record<string, string> = {
    'easy': 'success',
    'medium': 'warning',
    'hard': 'danger'
  }
  return map[difficulty] || ''
}

// 提交表单
const submitForm = async () => {
  if (!taskFormRef.value) return
  
  await taskFormRef.value.validate(async (valid: boolean) => {
    if (!valid) return
    
    if (taskForm.questionIds.length === 0) {
      ElMessage.warning('请选择至少一个问题')
      return
    }
    
    loading.value = true
    try {
      await createTask(taskForm)
      ElMessage.success('创建成功')
      router.push('/crowdsourcing')
    } catch (error) {
      console.error('创建任务失败', error)
    } finally {
      loading.value = false
    }
  })
}

// 重置表单
const resetForm = () => {
  if (!taskFormRef.value) return
  taskFormRef.value.resetFields()
  taskForm.questionIds = []
}

// 返回上一页
const navigateBack = () => {
  router.back()
}

onMounted(() => {
  getCategories()
  fetchQuestions()
})
</script>

<style scoped>
.task-create-container {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.form-card {
  margin-bottom: 20px;
}

.question-selection {
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  padding: 15px;
}

.question-search {
  margin-bottom: 15px;
}

.question-filter {
  display: flex;
  gap: 10px;
  margin-bottom: 15px;
}

.question-table {
  margin-bottom: 15px;
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 15px;
}

.selected-count {
  margin-top: 10px;
  font-weight: bold;
  color: #409EFF;
}
</style> 