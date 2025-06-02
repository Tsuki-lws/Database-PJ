<template>
  <div class="answer-collection-container">
    <div class="page-header">
      <h2>众包答案收集</h2>
      <el-button @click="$router.push('/crowdsourcing')">返回任务列表</el-button>
    </div>

    <el-card shadow="never" class="task-info-card" v-loading="taskLoading">
      <div class="task-header">
        <h3>{{ task.title }}</h3>
        <el-tag :type="getStatusTag(task.status)">{{ formatStatus(task.status) }}</el-tag>
      </div>
      <p class="task-description">{{ task.description }}</p>
      <div class="task-meta">
        <span>问题数量: {{ task.questionCount || 0 }}</span>
        <span>每题答案数量: {{ task.minAnswersPerQuestion }}</span>
        <span>任务奖励: {{ task.rewardInfo || '无' }}</span>
      </div>
    </el-card>

    <el-card shadow="never" class="question-list-card">
      <div class="question-header">
        <h3>问题列表</h3>
      </div>

      <el-table v-loading="loading" :data="questionList" style="width: 100%">
        <el-table-column prop="standardQuestionId" label="问题ID" width="80" />
        <el-table-column prop="question" label="问题内容" show-overflow-tooltip>
          <template #default="scope">
            <el-popover
              placement="top-start"
              title="问题内容"
              :width="400"
              trigger="hover"
              :content="scope.row.question">
              <template #reference>
                <span>{{ truncateText(scope.row.question, 100) }}</span>
              </template>
            </el-popover>
          </template>
        </el-table-column>
        <el-table-column prop="categoryName" label="分类" width="120" />
        <el-table-column prop="difficulty" label="难度" width="100">
          <template #default="scope">
            <el-tag :type="getDifficultyTag(scope.row.difficulty)">
              {{ formatDifficulty(scope.row.difficulty) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="currentAnswerCount" label="已收集答案" width="120" />
        <el-table-column prop="requiredAnswers" label="所需答案数" width="120" />
        <el-table-column prop="status" label="状态" width="120">
          <template #default="scope">
            <el-tag :type="getQuestionStatusTag(scope.row)">
              {{ getQuestionStatusText(scope.row) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column fixed="right" label="操作" width="120">
          <template #default="scope">
            <el-button 
              link 
              type="primary" 
              @click="handleAnswer(scope.row)"
              :disabled="isQuestionCompleted(scope.row)">
              提交答案
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="queryParams.page"
          v-model:page-size="queryParams.size"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 提交答案对话框 -->
    <el-dialog v-model="answerDialogVisible" title="提交候选答案" width="650px">
      <el-form :model="answerForm" :rules="answerRules" ref="answerFormRef" label-width="100px">
        <el-form-item label="问题ID">
          <span>{{ answerForm.standardQuestionId }}</span>
        </el-form-item>
        <el-form-item label="问题内容">
          <div class="question-content">{{ answerForm.question }}</div>
        </el-form-item>
        <el-form-item label="答案内容" prop="answerText">
          <el-input 
            v-model="answerForm.answerText" 
            type="textarea" 
            :rows="6" 
            placeholder="请输入您的答案">
          </el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="answerDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitAnswer" :loading="submitting">提交</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElNotification } from 'element-plus'
import { 
  getCrowdsourcingTask, 
  getTaskQuestions,
  submitCrowdsourcedAnswer
} from '@/api/crowdsourcing'

const route = useRoute()
const router = useRouter()
const taskId = ref(parseInt(route.params.taskId as string))

// 数据
const task = ref({} as any)
const questionList = ref([] as any[])
const total = ref(0)
const taskLoading = ref(false)
const loading = ref(false)
const submitting = ref(false)

// 查询参数
const queryParams = reactive({
  page: 1,
  size: 10,
  taskId: taskId.value
})

// 答案对话框
const answerDialogVisible = ref(false)
const answerForm = reactive({
  taskId: taskId.value,
  standardQuestionId: 0,
  question: '',
  answerText: ''
})
const answerRules = {
  answerText: [{ required: true, message: '请输入答案内容', trigger: 'blur' }]
}
const answerFormRef = ref()

// 初始化
onMounted(() => {
  getTaskInfo()
  getQuestions()
})

// 获取任务信息
const getTaskInfo = async () => {
  taskLoading.value = true
  try {
    const res = await getCrowdsourcingTask(taskId.value)
    task.value = res.data
  } catch (error: any) {
    ElMessage.error('获取任务信息失败: ' + error.message)
  } finally {
    taskLoading.value = false
  }
}

// 获取问题列表
const getQuestions = async () => {
  loading.value = true
  try {
    const res = await getTaskQuestions(queryParams)
    questionList.value = res.data.content || []
    total.value = res.data.totalElements || 0
  } catch (error: any) {
    ElMessage.error('获取问题列表失败: ' + error.message)
  } finally {
    loading.value = false
  }
}

// 处理答案提交
const handleAnswer = (question: any) => {
  answerForm.standardQuestionId = question.standardQuestionId
  answerForm.question = question.question
  answerForm.answerText = ''
  answerDialogVisible.value = true
}

// 提交答案
const submitAnswer = async () => {
  answerFormRef.value.validate(async (valid: boolean) => {
    if (!valid) return
    
    submitting.value = true
    try {
      await submitCrowdsourcedAnswer(answerForm)
      ElMessage.success('答案提交成功')
      answerDialogVisible.value = false
      getQuestions() // 刷新问题列表
    } catch (error: any) {
      ElMessage.error('答案提交失败: ' + error.message)
    } finally {
      submitting.value = false
    }
  })
}

// 格式化状态
const formatStatus = (status: string) => {
  const map: Record<string, string> = {
    'draft': '草稿',
    'ongoing': '进行中',
    'completed': '已完成',
    'cancelled': '已取消'
  }
  return map[status] || status
}

// 获取状态标签类型
const getStatusTag = (status: string) => {
  const map: Record<string, string> = {
    'draft': 'info',
    'ongoing': 'primary',
    'completed': 'success',
    'cancelled': 'danger'
  }
  return map[status] || ''
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

// 判断问题是否已完成收集
const isQuestionCompleted = (question: any) => {
  return question.currentAnswerCount >= question.requiredAnswers
}

// 获取问题状态标签
const getQuestionStatusTag = (question: any) => {
  if (isQuestionCompleted(question)) {
    return 'success'
  }
  return 'primary'
}

// 获取问题状态文本
const getQuestionStatusText = (question: any) => {
  if (isQuestionCompleted(question)) {
    return '已完成'
  }
  return '收集中'
}

// 截断文本
const truncateText = (text: string, length: number) => {
  if (!text) return ''
  if (text.length <= length) return text
  return text.substring(0, length) + '...'
}

// 分页大小变化
const handleSizeChange = (size: number) => {
  queryParams.size = size
  getQuestions()
}

// 页码变化
const handleCurrentChange = (page: number) => {
  queryParams.page = page
  getQuestions()
}
</script>

<style scoped>
.answer-collection-container {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.task-info-card,
.question-list-card {
  margin-bottom: 20px;
}

.task-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.task-description {
  margin-bottom: 15px;
  color: #606266;
  line-height: 1.5;
}

.task-meta {
  display: flex;
  gap: 20px;
  color: #909399;
}

.question-header {
  margin-bottom: 15px;
}

.question-content {
  padding: 10px;
  background-color: #f5f7fa;
  border-radius: 4px;
  margin-bottom: 10px;
  max-height: 100px;
  overflow-y: auto;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
</style> 