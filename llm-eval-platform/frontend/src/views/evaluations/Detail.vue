<template>
  <div class="evaluation-detail-container">
    <div class="page-header">
      <h2>评测批次详情</h2>
      <el-button @click="goBack">返回</el-button>
    </div>

    <el-card shadow="never" class="detail-card" v-loading="loading">
      <template #header>
        <div class="card-header">
          <span>基本信息</span>
          <div>
            <el-button 
              type="primary" 
              @click="viewResults" 
              v-if="evaluationDetail.status === 'completed'"
            >
              查看结果
            </el-button>
            <el-button 
              type="primary" 
              @click="startEvaluation" 
              v-if="evaluationDetail.status === 'pending'"
            >
              开始评测
            </el-button>
          </div>
        </div>
      </template>
      
      <el-descriptions :column="2" border>
        <el-descriptions-item label="评测ID">{{ evaluationDetail.batchId }}</el-descriptions-item>
        <el-descriptions-item label="评测名称">{{ evaluationDetail.name }}</el-descriptions-item>
        <el-descriptions-item label="模型">{{ evaluationDetail.modelName }}</el-descriptions-item>
        <el-descriptions-item label="数据集">{{ evaluationDetail.datasetName }}</el-descriptions-item>
        <el-descriptions-item label="评测方法">
          <el-tag :type="getMethodType(evaluationDetail.evaluationMethod)">
            {{ getMethodText(evaluationDetail.evaluationMethod) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(evaluationDetail.status)">
            {{ getStatusText(evaluationDetail.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ evaluationDetail.createdAt }}</el-descriptions-item>
        <el-descriptions-item label="完成时间">{{ evaluationDetail.endTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="问题数量">{{ evaluationDetail.questionCount || 0 }}</el-descriptions-item>
        <el-descriptions-item label="已评测数量">{{ evaluationDetail.evaluatedCount || 0 }}</el-descriptions-item>
      </el-descriptions>

      <div class="progress-section">
        <h3>评测进度</h3>
        <el-progress 
          :percentage="evaluationDetail.progress || 0" 
          :status="getProgressStatus(evaluationDetail.status)"
        />
      </div>

      <div class="description-section" v-if="evaluationDetail.description">
        <h3>评测描述</h3>
        <p>{{ evaluationDetail.description }}</p>
      </div>
    </el-card>

    <el-card shadow="never" class="metrics-card" v-if="evaluationDetail.status === 'completed'">
      <template #header>
        <div class="card-header">
          <span>评测指标</span>
        </div>
      </template>
      
      <el-row :gutter="20">
        <el-col :span="6">
          <div class="metric-item">
            <div class="metric-label">平均得分</div>
            <div class="metric-value">{{ evaluationDetail.avgScore ? evaluationDetail.avgScore.toFixed(2) : '-' }}</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="metric-item">
            <div class="metric-label">正确率</div>
            <div class="metric-value">{{ evaluationDetail.accuracyRate ? evaluationDetail.accuracyRate.toFixed(2) + '%' : '-' }}</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="metric-item">
            <div class="metric-label">平均响应时间</div>
            <div class="metric-value">{{ evaluationDetail.avgResponseTime ? evaluationDetail.avgResponseTime.toFixed(2) + 's' : '-' }}</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="metric-item">
            <div class="metric-label">完成率</div>
            <div class="metric-value">{{ evaluationDetail.completionRate ? evaluationDetail.completionRate.toFixed(2) + '%' : '-' }}</div>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <el-card shadow="never" class="questions-card">
      <template #header>
        <div class="card-header">
          <span>评测问题列表</span>
        </div>
      </template>
      
      <el-table :data="questions" style="width: 100%">
        <el-table-column prop="questionId" label="问题ID" width="80" />
        <el-table-column prop="question" label="问题内容" show-overflow-tooltip />
        <el-table-column prop="category" label="分类" width="120" />
        <el-table-column prop="difficulty" label="难度" width="100">
          <template #default="scope">
            <el-tag :type="getDifficultyType(scope.row.difficulty)">
              {{ getDifficultyText(scope.row.difficulty) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getEvaluationStatusType(scope.row.status)">
              {{ getEvaluationStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="score" label="得分" width="80">
          <template #default="scope">
            {{ scope.row.score !== undefined ? scope.row.score.toFixed(1) : '-' }}
          </template>
        </el-table-column>
        <el-table-column fixed="right" label="操作" width="150">
          <template #default="scope">
            <el-button link type="primary" @click="viewQuestionDetail(scope.row)">查看详情</el-button>
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
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getEvaluationBatchById, startEvaluationBatch } from '@/api/evaluation'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const batchId = Number(route.params.id)
const total = ref(0)

// 评测详情
const evaluationDetail = ref({
  batchId: 0,
  name: '',
  modelName: '',
  datasetName: '',
  evaluationMethod: '',
  status: '',
  progress: 0,
  createdAt: '',
  endTime: '',
  description: '',
  questionCount: 0,
  evaluatedCount: 0,
  avgScore: 0,
  accuracyRate: 0,
  avgResponseTime: 0,
  completionRate: 0
})

// 查询参数
const queryParams = reactive({
  page: 1,
  size: 10,
  status: ''
})

// 问题列表
const questions = ref<any[]>([])

// 初始化
onMounted(() => {
  fetchDetail()
  fetchQuestions()
})

// 获取评测详情
const fetchDetail = async () => {
  loading.value = true
  try {
    // 实际项目中应该调用API获取数据
    // const res = await getEvaluationBatchById(batchId)
    // evaluationDetail.value = res.data
    
    // 模拟数据
    setTimeout(() => {
      evaluationDetail.value = {
        batchId: batchId,
        name: 'GPT-4 基准测试',
        modelName: 'GPT-4',
        datasetName: '通用能力评测集 v1.0',
        evaluationMethod: 'auto',
        status: 'completed',
        progress: 100,
        createdAt: '2023-05-15 09:30:22',
        endTime: '2023-05-15 12:45:18',
        description: '这是一个针对GPT-4模型的基准测试，使用通用能力评测集进行自动评测。',
        questionCount: 500,
        evaluatedCount: 500,
        avgScore: 8.75,
        accuracyRate: 92.5,
        avgResponseTime: 2.34,
        completionRate: 100
      }
      loading.value = false
    }, 500)
  } catch (error: any) {
    ElMessage.error('获取评测详情失败: ' + error.message)
    loading.value = false
  }
}

// 获取问题列表
const fetchQuestions = async () => {
  try {
    // 实际项目中应该调用API获取数据
    // const res = await getEvaluationQuestions(batchId, queryParams)
    // questions.value = res.data.content
    // total.value = res.data.totalElements
    
    // 模拟数据
    questions.value = [
      { questionId: 1, question: '什么是大语言模型？', category: '人工智能', difficulty: 'easy', status: 'completed', score: 9.5 },
      { questionId: 2, question: '解释一下量子计算的基本原理', category: '物理', difficulty: 'hard', status: 'completed', score: 8.2 },
      { questionId: 3, question: '编写一个快速排序算法', category: '编程', difficulty: 'medium', status: 'completed', score: 9.0 },
      { questionId: 4, question: '简述人工智能的发展历史', category: '人工智能', difficulty: 'medium', status: 'completed', score: 8.7 },
      { questionId: 5, question: '如何实现神经网络的反向传播算法？', category: '机器学习', difficulty: 'hard', status: 'completed', score: 7.8 }
    ]
    total.value = 500
  } catch (error: any) {
    ElMessage.error('获取问题列表失败: ' + error.message)
  }
}

// 每页数量变化
const handleSizeChange = (val: number) => {
  queryParams.size = val
  fetchQuestions()
}

// 当前页变化
const handleCurrentChange = (val: number) => {
  queryParams.page = val
  fetchQuestions()
}

// 返回上一页
const goBack = () => {
  // 尝试从sessionStorage获取保存的返回状态
  const savedStateStr = sessionStorage.getItem('evaluationReturnState')
  
  if (savedStateStr) {
    try {
      const savedState = JSON.parse(savedStateStr)
      
      // 如果有保存的状态，并且是从modelEvaluations页面来的
      if (savedState.from === 'modelEvaluations') {
        if (savedState.returnToQuestions === 'true' && savedState.datasetId) {
          // 返回到问题列表页面
          router.push({
            path: '/evaluations/model',
            query: {
              view: 'questions',
              datasetId: savedState.datasetId
            }
          })
          return
        }
      }
    } catch (e) {
      console.error('解析保存的状态信息失败:', e)
    }
  }
  
  // 如果没有保存的状态或解析失败，则使用URL参数
  if (route.query.from === 'allResults') {
    router.push('/evaluations/all')
  } else if (route.query.from === 'modelEvaluations') {
    if (route.query.returnToQuestions === 'true' && route.query.datasetId) {
      // 返回到问题列表页面
      router.push({
        path: '/evaluations/model',
        query: {
          view: 'questions',
          datasetId: route.query.datasetId
        }
      })
    } else if (route.query.questionId) {
      // 返回到问题回答列表页面
      router.push({
        path: '/evaluations/model',
        query: {
          view: 'answers',
          questionId: route.query.questionId
        }
      })
    } else {
      router.push('/evaluations/model')
    }
  } else {
    router.back()
  }
}

// 查看结果
const viewResults = () => {
  router.push(`/evaluations/results/${batchId}`)
}

// 开始评测
const startEvaluation = async () => {
  try {
    ElMessageBox.confirm(
      '确定要开始执行此评测任务吗？',
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    ).then(async () => {
      // 实际项目中应该调用API开始评测
      // await startEvaluationBatch(batchId)
      
      // 模拟成功响应
      ElMessage.success('评测任务已开始执行')
      evaluationDetail.value.status = 'in_progress'
      evaluationDetail.value.progress = 5
    })
  } catch (error: any) {
    ElMessage.error('开始评测失败: ' + error.message)
  }
}

// 查看问题详情
const viewQuestionDetail = (row: any) => {
  ElMessage.info(`查看问题ID为 ${row.questionId} 的详情`)
  // 实际项目中应该跳转到问题详情页面
}

// 获取状态类型（用于标签颜色）
const getStatusType = (status: string) => {
  switch (status) {
    case 'pending': return 'info'
    case 'in_progress': return 'warning'
    case 'completed': return 'success'
    case 'failed': return 'danger'
    default: return 'info'
  }
}

// 获取状态文本
const getStatusText = (status: string) => {
  switch (status) {
    case 'pending': return '待执行'
    case 'in_progress': return '执行中'
    case 'completed': return '已完成'
    case 'failed': return '失败'
    default: return '未知'
  }
}

// 获取评测方法类型（用于标签颜色）
const getMethodType = (method: string) => {
  switch (method) {
    case 'human': return 'primary'
    case 'auto': return 'success'
    case 'judge_model': return 'warning'
    default: return 'info'
  }
}

// 获取评测方法文本
const getMethodText = (method: string) => {
  switch (method) {
    case 'human': return '人工'
    case 'auto': return '自动'
    case 'judge_model': return '评判模型'
    default: return '未知'
  }
}

// 获取进度条状态
const getProgressStatus = (status: string) => {
  switch (status) {
    case 'in_progress': return ''
    case 'completed': return 'success'
    case 'failed': return 'exception'
    default: return ''
  }
}

// 获取难度类型（用于标签颜色）
const getDifficultyType = (difficulty: string) => {
  switch (difficulty) {
    case 'easy': return 'success'
    case 'medium': return 'warning'
    case 'hard': return 'danger'
    default: return 'info'
  }
}

// 获取难度文本
const getDifficultyText = (difficulty: string) => {
  switch (difficulty) {
    case 'easy': return '简单'
    case 'medium': return '中等'
    case 'hard': return '困难'
    default: return difficulty
  }
}

// 获取评测状态类型（用于标签颜色）
const getEvaluationStatusType = (status: string) => {
  switch (status) {
    case 'pending': return 'info'
    case 'in_progress': return 'warning'
    case 'completed': return 'success'
    case 'failed': return 'danger'
    default: return 'info'
  }
}

// 获取评测状态文本
const getEvaluationStatusText = (status: string) => {
  switch (status) {
    case 'pending': return '待评测'
    case 'in_progress': return '评测中'
    case 'completed': return '已完成'
    case 'failed': return '失败'
    default: return '未知'
  }
}
</script>

<style scoped>
.evaluation-detail-container {
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

.metrics-card {
  margin-bottom: 20px;
}

.questions-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.progress-section {
  margin-top: 20px;
}

.description-section {
  margin-top: 20px;
}

.metric-item {
  text-align: center;
  padding: 20px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.metric-label {
  font-size: 14px;
  color: #606266;
  margin-bottom: 10px;
}

.metric-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}
</style> 