<template>
  <div class="task-detail-container">
    <div class="page-header">
      <h2>众包任务详情</h2>
      <div>
        <el-button @click="navigateToAnswers">查看答案</el-button>
        <el-button @click="navigateBack">返回</el-button>
      </div>
    </div>

    <el-card shadow="never" class="detail-card" v-loading="loading">
      <div class="task-header">
        <h3>{{ task.title }}</h3>
        <el-tag :type="getStatusTag(task.status)">{{ formatStatus(task.status) }}</el-tag>
      </div>

      <el-descriptions :column="2" border>
        <el-descriptions-item label="任务ID">{{ task.taskId }}</el-descriptions-item>
        <el-descriptions-item label="任务类型">
          <el-tag :type="getTaskTypeTag(task.taskType)">{{ formatTaskType(task.taskType) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="问题数量">{{ task.questionCount || 0 }}</el-descriptions-item>
        <el-descriptions-item label="每题答案数量">{{ task.minAnswersPerQuestion }}</el-descriptions-item>
        <el-descriptions-item label="开始时间">{{ task.startTime || '未设置' }}</el-descriptions-item>
        <el-descriptions-item label="结束时间">{{ task.endTime || '未设置' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ task.createdAt }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ task.updatedAt }}</el-descriptions-item>
        <el-descriptions-item label="任务描述" :span="2">{{ task.description || '无描述' }}</el-descriptions-item>
        <el-descriptions-item label="奖励信息" :span="2">{{ task.rewardInfo || '无奖励信息' }}</el-descriptions-item>
      </el-descriptions>

      <el-divider />

      <div class="task-progress">
        <h3>任务进度</h3>
        <el-progress 
          :percentage="calculateProgress()" 
          :status="getProgressStatus(task.status)"
          :format="progressFormat"
          :stroke-width="20">
        </el-progress>
      </div>

      <el-divider />

      <div class="task-questions">
        <div class="section-header">
          <h3>任务问题列表</h3>
          <span>共 {{ taskQuestions.length }} 个问题</span>
        </div>
        
        <el-table :data="taskQuestions" style="width: 100%">
          <el-table-column prop="standardQuestionId" label="问题ID" width="100" />
          <el-table-column prop="question" label="问题内容" show-overflow-tooltip />
          <el-table-column prop="currentAnswerCount" label="已收集答案数" width="120" />
          <el-table-column prop="status" label="状态" width="120">
            <template #default="scope">
              <el-tag v-if="scope.row.isCompleted" type="success">已完成</el-tag>
              <el-tag v-else type="warning">收集中</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="进度" width="200">
            <template #default="scope">
              <el-progress 
                :percentage="calculateQuestionProgress(scope.row)" 
                :status="scope.row.isCompleted ? 'success' : ''">
              </el-progress>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <el-divider />

      <div class="task-actions">
        <el-button 
          v-if="task.status === 'draft'" 
          type="primary" 
          @click="handlePublish">
          发布任务
        </el-button>
        <el-button 
          v-if="task.status === 'ongoing'" 
          type="warning" 
          @click="handleComplete">
          完成任务
        </el-button>
        <el-button 
          v-if="task.status === 'draft'" 
          type="danger" 
          @click="handleDelete">
          删除任务
        </el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import { getTaskById, updateTaskStatus } from '@/api/crowdsourcing'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const task = ref<any>({})
const taskQuestions = ref<any[]>([])

// 获取任务详情
const getTaskDetail = async () => {
  const id = Number(route.params.id)
  if (!id) return
  
  loading.value = true
  try {
    const data = await getTaskById(id)
    task.value = data
    taskQuestions.value = data.questions || []
  } catch (error) {
    console.error('获取任务详情失败', error)
  } finally {
    loading.value = false
  }
}

// 格式化任务类型
const formatTaskType = (type: string) => {
  const map: Record<string, string> = {
    'answer_collection': '答案收集',
    'answer_review': '答案审核',
    'answer_rating': '答案评分'
  }
  return map[type] || type
}

// 获取任务类型标签类型
const getTaskTypeTag = (type: string) => {
  const map: Record<string, string> = {
    'answer_collection': 'primary',
    'answer_review': 'success',
    'answer_rating': 'warning'
  }
  return map[type] || ''
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

// 计算任务进度
const calculateProgress = () => {
  if (task.value.status === 'completed') {
    return 100
  }
  
  if (!taskQuestions.value.length) {
    return 0
  }
  
  const completedQuestions = taskQuestions.value.filter(q => q.isCompleted).length
  return Math.round((completedQuestions / taskQuestions.value.length) * 100)
}

// 进度格式化
const progressFormat = (percentage: number) => {
  return `${percentage}% (${taskQuestions.value.filter(q => q.isCompleted).length}/${taskQuestions.value.length})`
}

// 获取进度条状态
const getProgressStatus = (status: string) => {
  const map: Record<string, string> = {
    'draft': '',
    'ongoing': '',
    'completed': 'success',
    'cancelled': 'exception'
  }
  return map[status] || ''
}

// 计算问题进度
const calculateQuestionProgress = (question: any) => {
  if (question.isCompleted) {
    return 100
  }
  
  if (!task.value.minAnswersPerQuestion) {
    return 0
  }
  
  return Math.min(Math.round((question.currentAnswerCount / task.value.minAnswersPerQuestion) * 100), 100)
}

// 发布任务
const handlePublish = () => {
  ElMessageBox.confirm(
    '确认发布此任务吗？发布后将开始收集答案。',
    '发布确认',
    {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'info'
    }
  ).then(async () => {
    try {
      await updateTaskStatus(task.value.taskId, 'ongoing')
      ElMessage.success('任务已发布')
      getTaskDetail()
    } catch (error) {
      console.error('发布任务失败', error)
    }
  }).catch(() => {})
}

// 完成任务
const handleComplete = () => {
  ElMessageBox.confirm(
    '确认完成此任务吗？完成后将不再收集新答案。',
    '完成确认',
    {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await updateTaskStatus(task.value.taskId, 'completed')
      ElMessage.success('任务已完成')
      getTaskDetail()
    } catch (error) {
      console.error('完成任务失败', error)
    }
  }).catch(() => {})
}

// 删除任务
const handleDelete = () => {
  ElMessageBox.confirm(
    '确认删除此任务吗？此操作不可恢复。',
    '删除确认',
    {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      // 这里应该调用删除API
      ElMessage.success('删除成功')
      router.push('/crowdsourcing')
    } catch (error) {
      console.error('删除任务失败', error)
    }
  }).catch(() => {})
}

// 导航到答案列表
const navigateToAnswers = () => {
  router.push(`/crowdsourcing/answers/${task.value.taskId}`)
}

// 返回上一页
const navigateBack = () => {
  router.back()
}

onMounted(() => {
  getTaskDetail()
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

.task-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.task-header h3 {
  margin: 0;
  font-size: 20px;
}

.task-progress {
  margin: 20px 0;
}

.task-progress h3 {
  margin-bottom: 15px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.section-header h3 {
  margin: 0;
}

.task-questions {
  margin: 20px 0;
}

.task-actions {
  display: flex;
  gap: 10px;
  margin-top: 20px;
}
</style> 