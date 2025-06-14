<template>
  <div class="task-detail-container">
    <div class="page-header">
      <h2>众包任务详情</h2>
      <el-button @click="goBack">返回</el-button>
    </div>

    <el-card v-loading="loading" shadow="never" class="detail-card">
      <template #header>
        <div class="card-header">
          <span>{{ task.title }}</span>
          <div class="header-actions">
            <el-tag :type="getStatusTag(task.status)">{{ formatStatus(task.status) }}</el-tag>
            <el-button 
              v-if="task.status === 'DRAFT'" 
              type="success" 
              size="small" 
              @click="handlePublish"
            >
              发布任务
            </el-button>
            <el-button 
              v-if="task.status === 'PUBLISHED'" 
              type="warning" 
              size="small" 
              @click="handleComplete"
            >
              完成任务
            </el-button>
            <el-button 
              v-if="task.status === 'DRAFT'" 
              type="danger" 
              size="small" 
              @click="handleDelete"
            >
              删除任务
            </el-button>
          </div>
        </div>
      </template>

      <div class="detail-item">
        <div class="item-label">任务描述</div>
        <div class="item-value">{{ task.description || '无' }}</div>
      </div>

      <!-- <div class="detail-item">
        <div class="item-label">创建人</div>
        <div class="item-value">{{ task.createdBy }}</div>
      </div> -->

      <div class="detail-item">
        <div class="item-label">所需答案数量</div>
        <div class="item-value">{{ task.requiredAnswers }}</div>
      </div>

      <div class="detail-item">
        <div class="item-label">当前答案数量</div>
        <div class="item-value">{{ task.submittedAnswerCount || 0 }}</div>
      </div>
      
      <!-- <div class="detail-item">
        <div class="item-label">已提交答案总数</div>
        <div class="item-value">{{ task.submittedAnswerCount || 0 }}</div>
      </div> -->

      <!-- <div class="detail-item">
        <div class="item-label">创建时间</div>
        <div class="item-value">{{ formatDateTime(task.createdAt) }}</div>
      </div>

      <div class="detail-item">
        <div class="item-label">最后更新时间</div>
        <div class="item-value">{{ formatDateTime(task.updatedAt) }}</div>
      </div> -->

      <div class="detail-item">
        <div class="item-label">完成进度</div>
        <div class="item-value">
          <el-progress 
            :percentage="calculateProgress(task)" 
            :status="getProgressStatus(task.status)"
          >
            <span>
              {{ task.submittedAnswerCount || task.submittedAnswerCount || 0 }}/{{ task.requiredAnswers }}
            </span>
          </el-progress>
        </div>
      </div>

      <el-divider></el-divider>

      <h3>标准问题</h3>
      <el-card v-if="standardQuestion" shadow="never" class="question-card">
        <template #header>
          <div class="question-header">
            <span>问题ID: {{ standardQuestion.standardQuestionId }}</span>
          </div>
        </template>
        <div class="question-content">
          {{ standardQuestion.question }}
        </div>
      </el-card>
      <el-empty v-else description="暂无标准问题信息"></el-empty>

      <el-divider></el-divider>
      
      <h3>任务关联问题</h3>
      <el-table
        v-if="taskQuestions.length > 0"
        :data="taskQuestions"
        border
        style="width: 100%">
        <el-table-column prop="id" label="关联ID" width="100"></el-table-column>
        <el-table-column prop="standardQuestionId" label="问题ID" width="100"></el-table-column>
        <el-table-column prop="currentAnswerCount" label="当前答案数量" width="150"></el-table-column>
        <el-table-column prop="isCompleted" label="是否完成" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.isCompleted ? 'success' : 'info'">
              {{ scope.row.isCompleted ? '已完成' : '未完成' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间">
          <template #default="scope">
            {{ formatDateTime(scope.row.createdAt) }}
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-else description="暂无关联问题信息"></el-empty>

      <el-divider></el-divider>

      <div class="action-buttons">
        <el-button type="primary" @click="navigateToAnswers">查看答案列表</el-button>
        <el-button 
          v-if="task.status === 'PUBLISHED'" 
          type="success" 
          @click="navigateToCollection"
        >
          参与回答
        </el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getTaskDetail, publishTask, completeTask, deleteTask, getTaskQuestions } from '@/api/crowdsourcing'
import { getQuestionById } from '@/api/question'

const router = useRouter()
const route = useRoute()
const loading = ref(true)

// 使用computed属性确保任务ID为有效的数字
const taskId = computed(() => {
  const id = route.params.id
  if (typeof id === 'string') {
    const numId = parseInt(id, 10)
    if (!isNaN(numId) && numId > 0) {
      console.log('解析任务ID成功:', numId)
      return numId
    }
  }
  console.error('无效的任务ID参数:', id)
  // 返回0而不是null，避免类型错误
  return 0
})

const task = ref<any>({})
const standardQuestion = ref<any>(null)
const taskQuestions = ref<any[]>([])

// 获取任务详情
const getTaskDetails = async () => {
  loading.value = true
  try {
    const response = await getTaskDetail(taskId.value)
    if (response && response.data) {
      task.value = response.data
      
      // 获取任务关联的标准问题和问题列表
      await fetchTaskQuestions()
      
    } else {
      ElMessage.error('获取任务详情失败')
    }
  } catch (error) {
    console.error('获取任务详情失败:', error)
    ElMessage.error('获取任务详情失败')
  } finally {
    loading.value = false
  }
}

// 格式化状态
const formatStatus = (status: string) => {
  const map: Record<string, string> = {
    'DRAFT': '草稿',
    'PUBLISHED': '已发布',
    'COMPLETED': '已完成',
    'CLOSED': '已关闭'
  }
  return map[status] || status
}

// 获取状态标签类型
const getStatusTag = (status: string) => {
  const map: Record<string, string> = {
    'DRAFT': 'info',
    'PUBLISHED': 'primary',
    'COMPLETED': 'success',
    'CLOSED': 'danger'
  }
  return map[status] || ''
}

// 计算进度
const calculateProgress = (task: any) => {
  if (task.status === 'COMPLETED' || task.status === 'CLOSED') {
    return 100
  }
  if (task.requiredAnswers === 0) {
    return 0
  }
  
  // 优先使用 approvedAnswerCount，这是已批准的答案数量
  const currentAnswers = task.submittedAnswerCount || task.submittedAnswerCount || 0
  return Math.min(Math.floor((currentAnswers / task.requiredAnswers) * 100), 100)
}

// 获取进度条状态
const getProgressStatus = (status: string) => {
  const map: Record<string, string> = {
    'DRAFT': '',
    'PUBLISHED': 'primary',
    'COMPLETED': 'success',
    'CLOSED': 'exception'
  }
  return map[status] || ''
}

// 格式化日期时间
const formatDateTime = (dateTimeStr: string) => {
  if (!dateTimeStr) return '';
  const date = new Date(dateTimeStr);
  return date.toLocaleString();
}

// 发布任务
const handlePublish = () => {
  if (!taskId.value) {
    ElMessage.error('无效的任务ID，无法执行操作')
    return
  }
  
  ElMessageBox.confirm(
    `确认发布任务 "${task.value.title}" 吗？`,
    '发布确认',
    {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'info'
    }
  ).then(async () => {
    try {
      await publishTask(taskId.value)
      ElMessage.success('任务发布成功')
      getTaskDetails()
    } catch (error) {
      console.error('发布任务失败', error)
      ElMessage.error('发布失败，请重试')
    }
  }).catch(() => {})
}

// 完成任务
const handleComplete = () => {
  if (!taskId.value) {
    ElMessage.error('无效的任务ID，无法执行操作')
    return
  }
  
  ElMessageBox.confirm(
    `确认完成任务 "${task.value.title}" 吗？完成后将不再接受新的答案。`,
    '完成确认',
    {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await completeTask(taskId.value)
      ElMessage.success('任务已完成')
      getTaskDetails()
    } catch (error) {
      console.error('完成任务失败', error)
      ElMessage.error('操作失败，请重试')
    }
  }).catch(() => {})
}

// 删除任务
const handleDelete = () => {
  if (!taskId.value) {
    ElMessage.error('无效的任务ID，无法执行操作')
    return
  }
  
  ElMessageBox.confirm(
    `确认删除任务 "${task.value.title}" 吗？此操作不可逆。`,
    '删除确认',
    {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await deleteTask(taskId.value)
      ElMessage.success('任务删除成功')
      router.push('/crowdsourcing')
    } catch (error) {
      console.error('删除任务失败', error)
      ElMessage.error('删除失败，请重试')
    }
  }).catch(() => {})
}

// 返回上一页
const goBack = () => {
  router.back()
}

// 导航到答案列表
const navigateToAnswers = () => {
  if (!taskId.value) {
    ElMessage.error('无效的任务ID，无法导航到答案列表')
    return
  }
  console.log('导航到答案列表，任务ID:', taskId.value)
  router.push(`/crowdsourcing/answers/${taskId.value}`)
}

// 导航到答案收集页面
const navigateToCollection = () => {
  console.log('从任务详情页跳转到收集页面，任务ID:', taskId.value)
  if (!taskId.value || isNaN(Number(taskId.value))) {
    console.error('无效的任务ID:', taskId.value)
    ElMessage.error('无效的任务ID，无法跳转')
    return
  }
  router.push(`/crowdsourcing/collection/${taskId.value}`)
}

// 获取任务关联的问题
const fetchTaskQuestions = async () => {
  if (!taskId.value) {
    console.error('任务ID无效，无法获取关联问题')
    return
  }
  
  try {
    const res = await getTaskQuestions(taskId.value)
    if (res.data && Array.isArray(res.data) && res.data.length > 0) {
      // 处理关联问题数据
      taskQuestions.value = res.data
      
      // 使用第一个关联的标准问题
      const standardQuestionId = res.data[0].standardQuestionId
      console.log('找到关联的标准问题ID:', standardQuestionId)
      
      // 获取标准问题详情
      await getStandardQuestion(standardQuestionId)
    } else {
      console.error('任务没有关联的标准问题')
    }
  } catch (error) {
    console.error('获取任务关联问题失败', error)
    ElMessage.error('获取任务关联问题失败')
  }
}

// 获取标准问题
const getStandardQuestion = async (questionId: number) => {
  try {
    const res = await getQuestionById(questionId)
    if (res && res.data) {
      standardQuestion.value = res.data
      console.log('获取到标准问题:', standardQuestion.value)
    }
  } catch (error) {
    console.error('获取标准问题失败', error)
  }
}

onMounted(() => {
  // 打印路由参数信息，帮助调试
  console.log('路由参数:', route.params)
  console.log('任务ID参数类型:', typeof route.params.id)
  console.log('任务ID参数值:', route.params.id)
  console.log('解析后的任务ID:', taskId.value)
  
  // 只有在任务ID有效时才获取任务详情
  if (taskId.value) {
    getTaskDetails()
  } else {
    loading.value = false
    ElMessage.error('无效的任务ID参数，请检查URL')
  }
})
</script>

<style scoped>
.task-detail-container {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.detail-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  gap: 10px;
  align-items: center;
}

.detail-item {
  margin-bottom: 16px;
}

.item-label {
  font-weight: bold;
  margin-bottom: 4px;
  color: #606266;
}

.item-value {
  color: #303133;
}

.question-card {
  margin-top: 16px;
}

.question-header {
  display: flex;
  justify-content: space-between;
}

.question-content {
  white-space: pre-wrap;
}

.action-buttons {
  margin-top: 20px;
  display: flex;
  gap: 10px;
}
</style> 