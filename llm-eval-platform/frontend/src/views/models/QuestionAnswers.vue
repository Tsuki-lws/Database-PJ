<template>
  <div class="question-answers-container">
    <div class="page-header">
      <h2>{{ questionContent ? '问题回答详情' : '模型回答列表' }}</h2>
      <div>
        <el-button @click="goBack">返回问题列表</el-button>
      </div>
    </div>

    <el-card shadow="never" class="question-card">
      <div class="question-header">
        <h3>问题内容</h3>
        <div class="dataset-info">
          数据集: <el-tag>{{ datasetName || '未知数据集' }}</el-tag>
        </div>
      </div>
      <div class="question-content">{{ questionContent || '加载中...' }}</div>
    </el-card>

    <el-card shadow="never" class="list-card">
      <div v-if="loading" class="loading-container">
        <el-skeleton :rows="5" animated />
      </div>
      <div v-else-if="answersList.length === 0" class="empty-container">
        <el-empty description="暂无模型回答" />
      </div>
      <div v-else>
        <div v-for="answer in answersList" :key="answer.llmAnswerId" class="answer-item">
          <div class="answer-header">
            <div class="model-info">
              <el-tag type="primary">{{ answer.model?.name }} {{ answer.model?.version }}</el-tag>
              <span class="latency">响应时间: {{ answer.latency || 0 }}ms</span>
            </div>
            <div class="answer-actions">
              <el-button type="primary" size="small" @click="evaluateAnswer(answer)">评测</el-button>
              <el-button type="danger" size="small" @click="deleteAnswer(answer)">删除</el-button>
            </div>
          </div>
          <div class="answer-content">
            <pre>{{ answer.content }}</pre>
          </div>
          <div class="answer-footer">
            <span class="created-at">创建时间: {{ formatDate(answer.createdAt) }}</span>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getModelAnswersForQuestion, deleteModelAnswer } from '@/api/import'

// 辅助类型定义
interface ModelAnswer {
  llmAnswerId: number
  content: string
  latency: number
  createdAt: string
  model?: {
    modelId: number
    name: string
    version: string
  }
  [key: string]: any
}

const router = useRouter()
const route = useRoute()
const loading = ref(false)
const answersList = ref<ModelAnswer[]>([])
const questionContent = ref('')
const datasetName = ref('')
const datasetId = ref<number | null>(null)
const questionId = ref<number | null>(null)

// 初始化
onMounted(() => {
  // 从路由参数中获取数据集ID和问题ID
  const dsId = route.params.datasetId
  const qId = route.params.questionId
  
  if (dsId && !isNaN(Number(dsId)) && qId && !isNaN(Number(qId))) {
    datasetId.value = Number(dsId)
    questionId.value = Number(qId)
    
    // 从查询参数中获取数据集名称和问题内容
    if (route.query.datasetName) {
      datasetName.value = route.query.datasetName as string
    }
    
    if (route.query.questionContent) {
      questionContent.value = route.query.questionContent as string
    }
    
    fetchData()
  } else {
    ElMessage.error('无效的数据集ID或问题ID')
    goBack()
  }
})

// 获取问题的模型回答列表
const fetchData = async () => {
  if (!datasetId.value || !questionId.value) return
  
  loading.value = true
  try {
    const res = await getModelAnswersForQuestion(datasetId.value, questionId.value)
    
    // 处理后端响应
    const responseData = res.data || res
    
    if (Array.isArray(responseData)) {
      answersList.value = responseData
    } else {
      answersList.value = []
      console.error('未能识别的响应格式:', res)
    }
  } catch (error: any) {
    ElMessage.error('获取模型回答列表失败: ' + error.message)
    answersList.value = []
  } finally {
    loading.value = false
  }
}

// 评测回答
const evaluateAnswer = (answer: ModelAnswer) => {
  if (!answer.llmAnswerId) {
    ElMessage.error('回答ID无效，无法评测')
    return
  }
  
  router.push({
    path: '/evaluations/manual',
    query: { answerId: answer.llmAnswerId.toString() }
  })
}

// 删除回答
const deleteAnswer = (answer: ModelAnswer) => {
  if (!answer.llmAnswerId) {
    ElMessage.error('回答ID无效，无法删除')
    return
  }
  
  ElMessageBox.confirm(
    `确定要删除该模型回答吗？`,
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await deleteModelAnswer(answer.llmAnswerId)
      ElMessage.success('删除成功')
      fetchData() // 重新加载数据
    } catch (error: any) {
      ElMessage.error('删除失败: ' + error.message)
    }
  }).catch(() => {})
}

// 返回问题列表
const goBack = () => {
  if (datasetId.value) {
    router.push({
      path: `/model-answers/dataset/${datasetId.value}`,
      query: { name: datasetName.value }
    })
  } else {
    router.push('/model-answers')
  }
}

// 格式化日期
const formatDate = (dateStr: string) => {
  if (!dateStr) return ''
  
  try {
    const date = new Date(dateStr)
    return date.toLocaleString()
  } catch (e) {
    return dateStr
  }
}
</script>

<style scoped>
.question-answers-container {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.question-card {
  margin-bottom: 20px;
}

.question-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.question-header h3 {
  margin: 0;
  font-size: 16px;
}

.question-content {
  background-color: #f5f7fa;
  padding: 15px;
  border-radius: 4px;
  margin-bottom: 10px;
}

.list-card {
  margin-bottom: 20px;
}

.loading-container, .empty-container {
  padding: 20px;
  text-align: center;
}

.answer-item {
  border-bottom: 1px solid #ebeef5;
  padding: 20px 0;
}

.answer-item:last-child {
  border-bottom: none;
}

.answer-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.model-info {
  display: flex;
  align-items: center;
}

.latency {
  margin-left: 10px;
  color: #909399;
  font-size: 14px;
}

.answer-content {
  background-color: #f5f7fa;
  padding: 15px;
  border-radius: 4px;
  margin-bottom: 10px;
  max-height: 300px;
  overflow-y: auto;
}

.answer-content pre {
  white-space: pre-wrap;
  margin: 0;
  font-family: inherit;
}

.answer-footer {
  display: flex;
  justify-content: flex-end;
}

.created-at {
  color: #909399;
  font-size: 14px;
}
</style> 