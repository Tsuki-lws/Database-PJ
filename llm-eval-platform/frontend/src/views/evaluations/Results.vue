<template>
  <div class="results-container">
    <div class="page-header">
      <h2>评测结果详情</h2>
      <div>
        <el-button type="primary" @click="exportResults">导出结果</el-button>
        <el-button @click="goBack">返回</el-button>
      </div>
    </div>

    <el-card shadow="never" class="info-card">
      <template #header>
        <div class="card-header">
          <span>评测基本信息</span>
        </div>
      </template>
      
      <el-descriptions :column="3" border>
        <el-descriptions-item label="评测ID">{{ evaluationInfo.batchId }}</el-descriptions-item>
        <el-descriptions-item label="评测名称">{{ evaluationInfo.batchName }}</el-descriptions-item>
        <el-descriptions-item label="评测方法">
          <el-tag :type="getMethodType(evaluationInfo.method)">
            {{ getMethodText(evaluationInfo.method) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="模型">{{ evaluationInfo.modelName }}</el-descriptions-item>
        <el-descriptions-item label="数据集">{{ evaluationInfo.datasetName }}</el-descriptions-item>
        <el-descriptions-item label="问题数量">{{ evaluationInfo.questionCount }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ evaluationInfo.createdAt }}</el-descriptions-item>
        <el-descriptions-item label="完成时间">{{ evaluationInfo.completedAt }}</el-descriptions-item>
        <el-descriptions-item label="耗时">{{ evaluationInfo.timeElapsed }}</el-descriptions-item>
      </el-descriptions>
    </el-card>

    <el-row :gutter="20" class="metrics-row">
      <el-col :span="6">
        <el-card shadow="hover" class="metric-card">
          <div class="metric-value">{{ evaluationInfo.avgScore.toFixed(2) }}</div>
          <div class="metric-label">平均得分</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="metric-card">
          <div class="metric-value">{{ evaluationInfo.accuracyRate.toFixed(2) }}%</div>
          <div class="metric-label">正确率</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="metric-card">
          <div class="metric-value">{{ evaluationInfo.avgResponseTime.toFixed(2) }}s</div>
          <div class="metric-label">平均响应时间</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="metric-card">
          <div class="metric-value">{{ evaluationInfo.completionRate.toFixed(2) }}%</div>
          <div class="metric-label">完成率</div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" class="chart-card">
      <template #header>
        <div class="card-header">
          <span>评分分布</span>
          <el-radio-group v-model="chartView" size="small">
            <el-radio-button label="score">得分分布</el-radio-button>
            <el-radio-button label="category">分类得分</el-radio-button>
            <el-radio-button label="difficulty">难度得分</el-radio-button>
          </el-radio-group>
        </div>
      </template>
      
      <div class="chart-placeholder">
        <template v-if="chartView === 'score'">
          <div class="score-distribution">
            <div class="score-bar-container" v-for="(count, score) in scoreDistribution" :key="score">
              <div class="score-label">{{ score }}</div>
              <div class="score-bar-wrapper">
                <div class="score-bar" :style="{ width: getScoreBarWidth(count) + '%' }"></div>
              </div>
              <div class="score-count">{{ count }}</div>
            </div>
          </div>
        </template>
        
        <template v-else-if="chartView === 'category'">
          <el-table :data="categoryScores" style="width: 100%">
            <el-table-column prop="category" label="分类" />
            <el-table-column prop="score" label="平均得分">
              <template #default="scope">
                <div class="table-score-bar-container">
                  <div class="table-score-bar" :style="{ width: scope.row.score * 10 + '%' }">
                    {{ scope.row.score.toFixed(2) }}
                  </div>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="count" label="问题数量" width="120" />
          </el-table>
        </template>
        
        <template v-else>
          <el-table :data="difficultyScores" style="width: 100%">
            <el-table-column prop="difficulty" label="难度">
              <template #default="scope">
                <el-tag :type="getDifficultyType(scope.row.difficulty)">
                  {{ getDifficultyText(scope.row.difficulty) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="score" label="平均得分">
              <template #default="scope">
                <div class="table-score-bar-container">
                  <div class="table-score-bar" :style="{ width: scope.row.score * 10 + '%' }">
                    {{ scope.row.score.toFixed(2) }}
                  </div>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="count" label="问题数量" width="120" />
          </el-table>
        </template>
      </div>
    </el-card>

    <el-card shadow="never" class="answers-card">
      <template #header>
        <div class="card-header">
          <span>评测结果列表</span>
          <div class="filter-actions">
            <el-input
              v-model="searchQuery"
              placeholder="搜索问题..."
              clearable
              @input="handleSearch"
              style="width: 250px; margin-right: 10px;"
            />
            <el-select v-model="filterCategory" placeholder="分类" clearable @change="handleSearch">
              <el-option
                v-for="category in categories"
                :key="category"
                :label="category"
                :value="category"
              />
            </el-select>
          </div>
        </div>
      </template>
      
      <el-table v-loading="loading" :data="filteredResults" style="width: 100%">
        <el-table-column prop="questionId" label="ID" width="80" />
        <el-table-column prop="question" label="问题" show-overflow-tooltip />
        <el-table-column prop="category" label="分类" width="120" />
        <el-table-column prop="difficulty" label="难度" width="100">
          <template #default="scope">
            <el-tag :type="getDifficultyType(scope.row.difficulty)">
              {{ getDifficultyText(scope.row.difficulty) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="score" label="得分" width="80">
          <template #default="scope">
            {{ scope.row.score.toFixed(1) }}
          </template>
        </el-table-column>
        <el-table-column prop="responseTime" label="响应时间" width="100">
          <template #default="scope">
            {{ scope.row.responseTime.toFixed(2) }}s
          </template>
        </el-table-column>
        <el-table-column fixed="right" label="操作" width="120">
          <template #default="scope">
            <el-button link type="primary" @click="viewDetail(scope.row)">查看详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="totalResults"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="评测详情"
      width="70%"
      destroy-on-close
    >
      <div v-loading="detailLoading" class="detail-dialog-content">
        <h3>问题</h3>
        <div class="detail-question">{{ currentDetail.question }}</div>
        
        <div class="detail-row">
          <div class="detail-col">
            <h3>标准答案</h3>
            <div class="detail-answer">{{ currentDetail.standardAnswer }}</div>
          </div>
          <div class="detail-col">
            <h3>模型回答</h3>
            <div class="detail-answer">{{ currentDetail.modelAnswer }}</div>
          </div>
        </div>
        
        <h3>评测结果</h3>
        <el-descriptions :column="3" border>
          <el-descriptions-item label="得分">{{ currentDetail.score.toFixed(1) }}</el-descriptions-item>
          <el-descriptions-item label="响应时间">{{ currentDetail.responseTime.toFixed(2) }}s</el-descriptions-item>
          <el-descriptions-item label="评测方法">{{ getMethodText(currentDetail.evaluationMethod) }}</el-descriptions-item>
        </el-descriptions>
        
        <template v-if="currentDetail.dimensions">
          <h3>评测维度</h3>
          <el-row :gutter="20">
            <el-col :span="8" v-for="(score, dimension) in currentDetail.dimensions" :key="dimension">
              <div class="dimension-item">
                <div class="dimension-label">{{ getDimensionText(dimension) }}</div>
                <el-rate v-model="currentDetail.dimensions[dimension]" disabled />
              </div>
            </el-col>
          </el-row>
        </template>
        
        <template v-if="currentDetail.keyPoints && currentDetail.keyPoints.length > 0">
          <h3>关键点评估</h3>
          <div class="key-points-container">
            <div class="key-point-item" v-for="(point, index) in currentDetail.keyPoints" :key="index">
              <div class="key-point-text">{{ point.text }}</div>
              <el-tag :type="getKeyPointStatusType(point.status)">
                {{ getKeyPointStatusText(point.status) }}
              </el-tag>
            </div>
          </div>
        </template>
        
        <template v-if="currentDetail.feedback">
          <h3>评测反馈</h3>
          <div class="detail-feedback">{{ currentDetail.feedback }}</div>
        </template>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getEvaluationResults, getEvaluationResultDetail } from '@/api/evaluations'

const route = useRoute()
const router = useRouter()
const batchId = Number(route.params.batchId)
const loading = ref(false)
const detailLoading = ref(false)
const detailDialogVisible = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const totalResults = ref(0)
const searchQuery = ref('')
const filterCategory = ref('')
const chartView = ref('score')

// 评测基本信息
const evaluationInfo = reactive({
  batchId: batchId,
  batchName: 'GPT-4 基准测试',
  modelName: 'GPT-4',
  datasetName: '通用能力评测集 v1.0',
  method: 'auto',
  questionCount: 500,
  createdAt: '2023-05-15 09:30:22',
  completedAt: '2023-05-15 12:45:18',
  timeElapsed: '3h 15m',
  avgScore: 8.75,
  accuracyRate: 92.5,
  avgResponseTime: 2.34,
  completionRate: 100
})

// 评测结果列表
const results = ref<any[]>([])

// 当前详情
const currentDetail = reactive({
  question: '',
  standardAnswer: '',
  modelAnswer: '',
  score: 0,
  responseTime: 0,
  evaluationMethod: '',
  dimensions: null as Record<string, number> | null,
  keyPoints: [] as Array<{ text: string, status: string }>,
  feedback: '',
  routeParams: {}
})

// 得分分布
const scoreDistribution = reactive({
  '0-1': 0,
  '1-2': 2,
  '2-3': 5,
  '3-4': 8,
  '4-5': 15,
  '5-6': 25,
  '6-7': 42,
  '7-8': 95,
  '8-9': 198,
  '9-10': 110
})

// 分类得分
const categoryScores = ref([
  { category: '编程', score: 8.9, count: 80 },
  { category: '数学', score: 9.2, count: 70 },
  { category: '物理', score: 8.5, count: 60 },
  { category: '化学', score: 8.3, count: 50 },
  { category: '生物', score: 7.8, count: 40 },
  { category: '历史', score: 8.1, count: 30 },
  { category: '地理', score: 8.6, count: 25 },
  { category: '文学', score: 9.0, count: 45 },
  { category: '常识', score: 9.5, count: 100 }
])

// 难度得分
const difficultyScores = ref([
  { difficulty: 'easy', score: 9.3, count: 200 },
  { difficulty: 'medium', score: 8.5, count: 200 },
  { difficulty: 'hard', score: 7.2, count: 100 }
])

// 分类列表
const categories = computed(() => {
  return [...new Set(categoryScores.value.map(item => item.category))]
})

// 筛选后的结果
const filteredResults = computed(() => {
  return results.value.filter(item => {
    const matchesQuery = searchQuery.value ? 
      item.question.toLowerCase().includes(searchQuery.value.toLowerCase()) : 
      true
    
    const matchesCategory = filterCategory.value ? 
      item.category === filterCategory.value : 
      true
    
    return matchesQuery && matchesCategory
  })
})

// 初始化
onMounted(() => {
  fetchResults()
})

// 获取评测结果
const fetchResults = async () => {
  loading.value = true
  try {
    // 实际项目中应该调用API获取数据
    // const res = await getEvaluationResults(batchId, {
    //   page: currentPage.value,
    //   size: pageSize.value
    // })
    // results.value = res.data.content
    // totalResults.value = res.data.totalElements
    
    // // 模拟数据
    // setTimeout(() => {
    //   results.value = [
    //     { questionId: 1, question: '什么是大语言模型？', category: '人工智能', difficulty: 'easy', score: 9.5, responseTime: 1.2 },
    //     { questionId: 2, question: '解释一下量子计算的基本原理', category: '物理', difficulty: 'hard', score: 8.2, responseTime: 2.5 },
    //     { questionId: 3, question: '编写一个快速排序算法', category: '编程', difficulty: 'medium', score: 9.0, responseTime: 1.8 },
    //     { questionId: 4, question: '简述人工智能的发展历史', category: '人工智能', difficulty: 'medium', score: 8.7, responseTime: 3.2 },
    //     { questionId: 5, question: '如何实现神经网络的反向传播算法？', category: '机器学习', difficulty: 'hard', score: 7.8, responseTime: 2.9 },
    //     { questionId: 6, question: 'Python和Java的主要区别是什么？', category: '编程', difficulty: 'easy', score: 9.3, responseTime: 1.5 },
    //     { questionId: 7, question: '解释相对论的基本原理', category: '物理', difficulty: 'hard', score: 8.0, responseTime: 3.5 },
    //     { questionId: 8, question: '什么是区块链技术？', category: '技术', difficulty: 'medium', score: 8.8, responseTime: 2.1 },
    //     { questionId: 9, question: '解释一下梯度下降算法', category: '机器学习', difficulty: 'medium', score: 9.1, responseTime: 1.7 },
    //     { questionId: 10, question: '什么是函数式编程？', category: '编程', difficulty: 'medium', score: 8.5, responseTime: 2.3 }
    //   ]
    //   totalResults.value = 500
    //   loading.value = false
    // }, 500)
  } catch (error: any) {
    ElMessage.error('获取评测结果失败: ' + error.message)
    loading.value = false
  }
}

// 查看详情
const viewDetail = async (row: any) => {
  detailDialogVisible.value = true
  detailLoading.value = true
  
  // 保存当前查询参数和路由信息，以便在详情对话框中使用
  currentDetail.routeParams = {
    from: route.query.from,
    returnToQuestions: route.query.returnToQuestions,
    datasetId: route.query.datasetId,
    questionId: route.query.questionId,
    view: route.query.view
  }
  
  try {
    // 实际项目中应该调用API获取数据
    // const res = await getEvaluationResultDetail(batchId, row.questionId)
    // Object.assign(currentDetail, res.data)
    
    // 模拟数据
    setTimeout(() => {
      // Object.assign(currentDetail, {
      //   question: row.question,
      //   standardAnswer: '大语言模型（Large Language Model，LLM）是一种基于深度学习的自然语言处理模型，通过在海量文本数据上训练，能够理解、生成和转换人类语言。它通常基于Transformer架构，具有数十亿到数万亿参数，能够执行各种语言任务，如文本生成、翻译、问答、摘要等，无需针对特定任务进行专门训练。',
      //   modelAnswer: '大语言模型（LLM）是一种使用深度学习技术训练的大规模自然语言处理模型。这些模型通常基于Transformer架构，拥有数十亿到数万亿个参数，通过在互联网规模的文本数据上进行训练，能够理解和生成类似人类的文本。大语言模型可以执行各种任务，如文本生成、翻译、问答、摘要等，而无需针对每个特定任务进行专门训练。它们展现出了强大的上下文理解能力和知识储备，代表了人工智能在自然语言处理领域的重要进展。',
      //   score: row.score,
      //   responseTime: row.responseTime,
      //   evaluationMethod: evaluationInfo.method,
      //   dimensions: {
      //     accuracy: 5,
      //     completeness: 4,
      //     clarity: 5
      //   },
      //   keyPoints: [
      //     { text: '定义准确（基于深度学习的NLP模型）', status: 'matched' },
      //     { text: '提到海量文本数据训练', status: 'matched' },
      //     { text: '提到Transformer架构', status: 'matched' },
      //     { text: '提到模型规模（参数量）', status: 'matched' },
      //     { text: '列举应用场景', status: 'matched' }
      //   ],
      //   feedback: '模型回答全面准确，涵盖了大语言模型的定义、架构、规模和应用场景，表述清晰流畅。'
      // })
      // detailLoading.value = false
    }, 500)
  } catch (error: any) {
    ElMessage.error('获取详情失败: ' + error.message)
    detailLoading.value = false
  }
}

// 导出结果
const exportResults = () => {
  ElMessage.success('评测结果导出成功')
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
  if (route.query.from === 'modelEvaluations') {
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
  } else if (route.query.from === 'allResults') {
    router.push('/evaluations/all')
  } else {
    router.back()
  }
}

// 处理搜索
const handleSearch = () => {
  currentPage.value = 1
  // 在实际项目中，这里应该重新请求API
  // 由于我们使用的是本地筛选，不需要额外操作
}

// 每页数量变化
const handleSizeChange = (val: number) => {
  pageSize.value = val
  fetchResults()
}

// 当前页变化
const handleCurrentChange = (val: number) => {
  currentPage.value = val
  fetchResults()
}

// 获取得分条宽度
const getScoreBarWidth = (count: number) => {
  const max = Math.max(...Object.values(scoreDistribution))
  return (count / max) * 100
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

// 获取维度文本
const getDimensionText = (dimension: string) => {
  switch (dimension) {
    case 'accuracy': return '准确性'
    case 'completeness': return '完整性'
    case 'clarity': return '清晰度'
    default: return dimension
  }
}

// 获取关键点状态类型（用于标签颜色）
const getKeyPointStatusType = (status: string) => {
  switch (status) {
    case 'matched': return 'success'
    case 'partial': return 'warning'
    case 'missed': return 'danger'
    default: return 'info'
  }
}

// 获取关键点状态文本
const getKeyPointStatusText = (status: string) => {
  switch (status) {
    case 'matched': return '完全匹配'
    case 'partial': return '部分匹配'
    case 'missed': return '未匹配'
    default: return '未知'
  }
}
</script>

<style scoped>
.results-container {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.info-card,
.chart-card,
.answers-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.metrics-row {
  margin: 20px 0;
}

.metric-card {
  text-align: center;
  padding: 20px;
  height: 100%;
}

.metric-value {
  font-size: 28px;
  font-weight: bold;
  color: #409EFF;
  margin-bottom: 10px;
}

.metric-label {
  font-size: 14px;
  color: #606266;
}

.chart-placeholder {
  min-height: 300px;
  padding: 20px 0;
}

.score-distribution {
  padding: 0 20px;
}

.score-bar-container {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
}

.score-label {
  width: 50px;
  text-align: right;
  margin-right: 10px;
}

.score-bar-wrapper {
  flex: 1;
  background-color: #f5f7fa;
  height: 20px;
  border-radius: 4px;
  overflow: hidden;
}

.score-bar {
  height: 100%;
  background-color: #409EFF;
}

.score-count {
  width: 50px;
  text-align: left;
  margin-left: 10px;
}

.table-score-bar-container {
  background-color: #f5f7fa;
  height: 20px;
  border-radius: 4px;
  overflow: hidden;
  width: 100%;
}

.table-score-bar {
  height: 100%;
  background-color: #409EFF;
  color: white;
  text-align: right;
  padding-right: 5px;
  font-size: 12px;
  line-height: 20px;
}

.filter-actions {
  display: flex;
  align-items: center;
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

.detail-dialog-content {
  padding: 10px;
}

.detail-dialog-content h3 {
  margin-top: 20px;
  margin-bottom: 10px;
  font-size: 16px;
  color: #303133;
}

.detail-question {
  background-color: #f5f7fa;
  padding: 15px;
  border-radius: 4px;
  margin-bottom: 20px;
}

.detail-row {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
}

.detail-col {
  flex: 1;
}

.detail-answer {
  background-color: #f5f7fa;
  padding: 15px;
  border-radius: 4px;
  height: 200px;
  overflow-y: auto;
  white-space: pre-wrap;
}

.dimension-item {
  text-align: center;
  margin-bottom: 15px;
}

.dimension-label {
  margin-bottom: 5px;
}

.key-points-container {
  margin-top: 10px;
}

.key-point-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px;
  border-bottom: 1px solid #ebeef5;
}

.key-point-item:last-child {
  border-bottom: none;
}

.key-point-text {
  flex: 1;
  margin-right: 10px;
}

.detail-feedback {
  background-color: #f5f7fa;
  padding: 15px;
  border-radius: 4px;
  white-space: pre-wrap;
}
</style> 