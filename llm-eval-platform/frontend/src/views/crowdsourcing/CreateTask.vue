<template>
  <div class="task-create-container">
    <div class="page-header">
      <h2>创建众包任务</h2>
      <el-button @click="navigateBack">返回</el-button>
    </div>

    <el-card shadow="never" class="form-card" v-loading="loading">
      <el-form :model="taskForm" :rules="rules" ref="taskFormRef" label-width="120px" class="full-width-form">
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

        <el-form-item label="所需答案数量" prop="requiredAnswers">
          <el-input-number 
            v-model="taskForm.requiredAnswers" 
            :min="1" 
            :max="10">
          </el-input-number>
        </el-form-item>

        <el-divider content-position="left">选择标准问题</el-divider>

        <el-form-item label="标准问题" prop="standardQuestionId">
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
                <el-option label="简单" value="EASY"></el-option>
                <el-option label="中等" value="MEDIUM"></el-option>
                <el-option label="困难" value="HARD"></el-option>
              </el-select>
              
              <el-button type="primary" @click="searchQuestions">筛选</el-button>
              <el-button type="success" @click="searchQuestionsWithoutAnswer">
                <el-icon><Search /></el-icon>查找无标准答案问题
              </el-button>
            </div>
            
            <div class="question-table">
              <div class="query-mode-indicator" v-if="queryMode === 'withoutAnswer'">
                <el-tag type="success" effect="dark">当前显示: 无标准答案的问题</el-tag>
              </div>
              <el-table
                v-loading="questionLoading"
                :data="questionList"
                @selection-change="handleSelectionChange"
                height="450px"
                border
                stripe
                style="width: 100%">
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
              已选择 <span v-if="selectedQuestion">标准问题ID: {{ selectedQuestion.standardQuestionId }}</span>
              <span v-else>未选择</span>
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
import { createTask } from '@/api/crowdsourcing'
import { getQuestionList, getQuestionsWithoutAnswer } from '@/api/question'
import { getAllCategories } from '@/api/category'

const router = useRouter()
const loading = ref(false)
const questionLoading = ref(false)
const taskFormRef = ref()

// 表单数据
const taskForm = reactive({
  title: '',
  description: '',
  standardQuestionId: null,
  createdBy: 1, // 默认创建人ID
  requiredAnswers: 3,
  status: 'DRAFT'
})

// 表单验证规则
const rules = {
  title: [
    { required: true, message: '请输入任务标题', trigger: 'blur' }
  ],
  requiredAnswers: [
    { required: true, message: '请输入所需答案数量', trigger: 'blur' }
  ],
  standardQuestionId: [
    { required: true, message: '请选择标准问题', trigger: 'change' }
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

// 查询模式
const queryMode = ref('normal') // 'normal' 或 'withoutAnswer'

// 问题列表
const questionList = ref<any[]>([])
const questionTotal = ref(0)

// 已选择的问题
const selectedQuestion = ref<any>(null)

// 分类选项
const categoryOptions = ref<any[]>([])

// 获取分类列表
const getCategories = async () => {
  try {
    const res = await getAllCategories()
    if (res.data) {
      categoryOptions.value = res.data.map((item: any) => ({
        value: item.categoryId,
        label: item.name
      }))
    }
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
  queryMode.value = 'normal'
  fetchQuestions()
}

// 获取问题列表
const fetchQuestions = async () => {
  questionLoading.value = true
  try {
    const res = await getQuestionList(questionQuery)
    if (res.data) {
      questionList.value = res.data.content || []
      questionTotal.value = res.data.total || 0
    } else {
      questionList.value = []
      questionTotal.value = 0
    }
  } catch (error) {
    console.error('获取问题列表失败', error)
    questionList.value = []
    questionTotal.value = 0
  } finally {
    questionLoading.value = false
  }
}

// 选择问题变化
const handleSelectionChange = (selection: any[]) => {
  if (selection.length > 0) {
    // 只允许选择一个问题
    selectedQuestion.value = selection[0]
    taskForm.standardQuestionId = selection[0].standardQuestionId
    console.log('已选择标准问题:', selectedQuestion.value)
  } else {
    selectedQuestion.value = null
    taskForm.standardQuestionId = null
  }
}

// 处理每页显示数量变化
const handleSizeChange = (val: number) => {
  questionQuery.size = val
  if (queryMode.value === 'withoutAnswer') {
    searchQuestionsWithoutAnswer()
  } else {
    fetchQuestions()
  }
}

// 处理当前页变化
const handleCurrentChange = (val: number) => {
  questionQuery.page = val
  if (queryMode.value === 'withoutAnswer') {
    searchQuestionsWithoutAnswer()
  } else {
    fetchQuestions()
  }
}

// 提交表单
const submitForm = async () => {
  if (!taskFormRef.value) return
  
  // 确保已选择标准问题
  if (!taskForm.standardQuestionId && selectedQuestion.value) {
    taskForm.standardQuestionId = selectedQuestion.value.standardQuestionId
  }
  
  if (!taskForm.standardQuestionId) {
    ElMessage.error('请选择标准问题')
    return
  }
  
  await taskFormRef.value.validate(async (valid: boolean) => {
    if (!valid) {
      return false
    }
    
    loading.value = true
    try {
      // 确保提交的数据符合后端要求
      const submitData = {
        title: taskForm.title,
        description: taskForm.description,
        standardQuestionId: taskForm.standardQuestionId,
        createdBy: taskForm.createdBy,
        requiredAnswers: taskForm.requiredAnswers,
        status: taskForm.status
      }
      
      await createTask(submitData)
      ElMessage.success('任务创建成功')
      router.push('/crowdsourcing')
    } catch (error) {
      console.error('创建任务失败', error)
      ElMessage.error('创建任务失败，请重试')
    } finally {
      loading.value = false
    }
  })
}

// 重置表单
const resetForm = () => {
  if (taskFormRef.value) {
    taskFormRef.value.resetFields()
    selectedQuestion.value = null
  }
}

// 返回上一页
const navigateBack = () => {
  router.back()
}

// 格式化难度
const formatDifficulty = (difficulty: string) => {
  const map: Record<string, string> = {
    'EASY': '简单',
    'MEDIUM': '中等',
    'HARD': '困难'
  }
  return map[difficulty] || difficulty
}

// 获取难度标签类型
const getDifficultyTag = (difficulty: string) => {
  const map: Record<string, string> = {
    'EASY': 'success',
    'MEDIUM': 'warning',
    'HARD': 'danger'
  }
  return map[difficulty] || ''
}

// 搜索无标准答案问题
const searchQuestionsWithoutAnswer = async () => {
  questionLoading.value = true
  try {
    const params = {
      page: questionQuery.page,
      size: questionQuery.size,
      categoryId: questionFilter.categoryId,
      difficulty: questionFilter.difficulty,
      keyword: questionSearchKeyword.value
    }
    
    queryMode.value = 'withoutAnswer'
    
    const res = await getQuestionsWithoutAnswer(params)
    if (res.data) {
      questionList.value = res.data.content || []
      questionTotal.value = res.data.total || 0
      
      if (questionList.value.length === 0) {
        ElMessage.info('没有找到无标准答案的问题')
      } else {
        ElMessage.success(`找到 ${questionTotal.value} 个无标准答案的问题`)
      }
    } else {
      questionList.value = []
      questionTotal.value = 0
    }
  } catch (error) {
    console.error('获取无标准答案问题列表失败', error)
    ElMessage.error('获取无标准答案问题列表失败')
    questionList.value = []
    questionTotal.value = 0
  } finally {
    questionLoading.value = false
  }
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
  max-width: 1200px;
  margin: 0 auto 20px;
}

.full-width-form :deep(.el-form-item__content) {
  width: calc(100% - 120px);
}

.question-selection {
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 24px;
  margin-bottom: 24px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
  background-color: #fafafa;
  min-height: 600px;
}

.question-search {
  margin-bottom: 20px;
}

.question-filter {
  display: flex;
  margin-bottom: 20px;
  gap: 12px;
  flex-wrap: wrap;
}

.question-filter .el-select {
  min-width: 150px;
}

.question-table {
  margin-bottom: 20px;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.pagination-container {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

.selected-count {
  font-weight: bold;
  margin-top: 12px;
  padding: 10px;
  background-color: #f0f9eb;
  border-radius: 4px;
  border-left: 4px solid #67c23a;
}

.selected-count span {
  color: #409eff;
  font-weight: 600;
}

.query-mode-indicator {
  margin-bottom: 10px;
  display: flex;
  justify-content: flex-end;
}
</style> 