<template>
  <div class="evaluation-detail-container">
    <div class="page-header">
      <h2>评测详情</h2>
      <div>
        <el-button type="primary" @click="handleRunEvaluation" v-if="evaluation.status === 'created'">
          开始评测
        </el-button>
        <el-button @click="navigateBack">返回</el-button>
      </div>
    </div>

    <el-card shadow="never" class="detail-card" v-loading="loading">
      <div class="evaluation-header">
        <h3>{{ evaluation.name }}</h3>
        <el-tag :type="getStatusTag(evaluation.status)">{{ formatStatus(evaluation.status) }}</el-tag>
      </div>

      <el-descriptions :column="2" border>
        <el-descriptions-item label="评测ID">{{ evaluation.evaluationId }}</el-descriptions-item>
        <el-descriptions-item label="模型名称">{{ evaluation.modelName }}</el-descriptions-item>
        <el-descriptions-item label="模型提供商">{{ evaluation.modelProvider }}</el-descriptions-item>
        <el-descriptions-item label="模型版本">{{ evaluation.modelVersion }}</el-descriptions-item>
        <el-descriptions-item label="数据集">{{ evaluation.datasetName }}</el-descriptions-item>
        <el-descriptions-item label="问题数量">{{ evaluation.questionCount || 0 }}</el-descriptions-item>
        <el-descriptions-item label="评测方法">{{ formatEvaluationMethod(evaluation.evaluationMethod) }}</el-descriptions-item>
        <el-descriptions-item label="温度参数">{{ evaluation.temperature }}</el-descriptions-item>
        <el-descriptions-item label="最大输出长度">{{ evaluation.maxTokens }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ evaluation.createdAt }}</el-descriptions-item>
        <el-descriptions-item label="开始时间">{{ evaluation.startTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="完成时间">{{ evaluation.endTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="评测描述" :span="2">{{ evaluation.description || '无描述' }}</el-descriptions-item>
        <el-descriptions-item label="评分标准" :span="2">{{ evaluation.scoringCriteria || '无评分标准' }}</el-descriptions-item>
      </el-descriptions>

      <el-divider />

      <div class="evaluation-progress">
        <h3>评测进度</h3>
        <el-progress 
          :percentage="calculateProgress()" 
          :status="getProgressStatus(evaluation.status)"
          :format="progressFormat"
          :stroke-width="20">
        </el-progress>
      </div>

      <el-divider />

      <div class="evaluation-results" v-if="evaluation.status === 'completed'">
        <h3>评测结果</h3>
        
        <div class="result-summary">
          <el-row :gutter="20">
            <el-col :span="8">
              <el-card class="result-card">
                <template #header>
                  <div class="result-card-header">
                    <span>总体得分</span>
                  </div>
                </template>
                <div class="result-card-content">
                  <div class="result-score">{{ (evaluation.averageScore || 0).toFixed(2) }}</div>
                  <div class="result-score-max">/ 10</div>
                </div>
              </el-card>
            </el-col>
            <el-col :span="8">
              <el-card class="result-card">
                <template #header>
                  <div class="result-card-header">
                    <span>正确率</span>
                  </div>
                </template>
                <div class="result-card-content">
                  <div class="result-score">{{ (evaluation.accuracyRate || 0).toFixed(2) }}%</div>
                </div>
              </el-card>
            </el-col>
            <el-col :span="8">
              <el-card class="result-card">
                <template #header>
                  <div class="result-card-header">
                    <span>响应时间</span>
                  </div>
                </template>
                <div class="result-card-content">
                  <div class="result-score">{{ (evaluation.averageResponseTime || 0).toFixed(2) }}s</div>
                </div>
              </el-card>
            </el-col>
          </el-row>
        </div>

        <div class="result-charts">
          <el-row :gutter="20">
            <el-col :span="12">
              <div class="chart-container">
                <h4>得分分布</h4>
                <div class="chart-placeholder">
                  <!-- 这里放置得分分布图表 -->
                  <div class="chart-empty">
                    <el-empty description="暂无图表数据"></el-empty>
                  </div>
                </div>
              </div>
            </el-col>
            <el-col :span="12">
              <div class="chart-container">
                <h4>分类得分</h4>
                <div class="chart-placeholder">
                  <!-- 这里放置分类得分图表 -->
                  <div class="chart-empty">
                    <el-empty description="暂无图表数据"></el-empty>
                  </div>
                </div>
              </div>
            </el-col>
          </el-row>
        </div>
      </div>

      <el-divider />

      <div class="evaluation-answers">
        <div class="section-header">
          <h3>评测答案列表</h3>
        </div>
        
        <el-table :data="answerList" style="width: 100%">
          <el-table-column prop="questionId" label="问题ID" width="100" />
          <el-table-column prop="question" label="问题内容" show-overflow-tooltip />
          <el-table-column prop="score" label="得分" width="100">
            <template #default="scope">
              <span v-if="scope.row.score !== undefined">{{ scope.row.score.toFixed(1) }}</span>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="120">
            <template #default="scope">
              <el-tag :type="getAnswerStatusTag(scope.row.status)">
                {{ formatAnswerStatus(scope.row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="responseTime" label="响应时间(s)" width="120">
            <template #default="scope">
              <span>{{ scope.row.responseTime ? scope.row.responseTime.toFixed(2) : '-' }}</span>
            </template>
          </el-table-column>
          <el-table-column fixed="right" label="操作" width="150">
            <template #default="scope">
              <el-button link type="primary" @click="viewAnswer(scope.row)">查看</el-button>
              <el-button 
                v-if="evaluation.evaluationMethod === 'manual' && scope.row.status === 'completed'" 
                link 
                type="primary" 
                @click="rateAnswer(scope.row)">
                评分
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
      </div>
    </el-card>

    <!-- 查看答案对话框 -->
    <el-dialog v-model="answerDialogVisible" title="答案详情" width="70%">
      <div class="answer-detail" v-loading="answerLoading">
        <div class="answer-question">
          <h4>问题</h4>
          <div class="content-box">{{ currentAnswer.question }}</div>
        </div>

        <div class="answer-standard" v-if="currentAnswer.standardAnswer">
          <h4>标准答案</h4>
          <div class="content-box">{{ currentAnswer.standardAnswer }}</div>
        </div>

        <div class="answer-model">
          <h4>模型回答</h4>
          <div class="content-box">{{ currentAnswer.modelAnswer }}</div>
        </div>

        <div class="answer-score" v-if="currentAnswer.score !== undefined">
          <h4>评分</h4>
          <div class="score-display">
            <el-rate
              v-model="currentAnswer.score"
              :max="10"
              :colors="['#99A9BF', '#F7BA2A', '#FF9900']"
              disabled
              show-score>
            </el-rate>
          </div>
        </div>

        <div class="answer-feedback" v-if="currentAnswer.feedback">
          <h4>评价反馈</h4>
          <div class="content-box">{{ currentAnswer.feedback }}</div>
        </div>
      </div>
    </el-dialog>

    <!-- 评分对话框 -->
    <el-dialog v-model="rateDialogVisible" title="评分" width="500px">
      <el-form :model="rateForm" label-width="100px">
        <el-form-item label="问题">
          <div class="content-box small">{{ rateForm.question }}</div>
        </el-form-item>
        <el-form-item label="标准答案">
          <div class="content-box small">{{ rateForm.standardAnswer }}</div>
        </el-form-item>
        <el-form-item label="模型回答">
          <div class="content-box small">{{ rateForm.modelAnswer }}</div>
        </el-form-item>
        <el-form-item label="评分">
          <el-rate
            v-model="rateForm.score"
            :max="10"
            :colors="['#99A9BF', '#F7BA2A', '#FF9900']"
            show-score>
          </el-rate>
        </el-form-item>
        <el-form-item label="评价反馈">
          <el-input 
            v-model="rateForm.feedback" 
            type="textarea" 
            :rows="3" 
            placeholder="请输入评价反馈">
          </el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="rateDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitRate">确认</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import { 
  getEvaluationById, 
  getEvaluationAnswers, 
  startEvaluation, 
  rateEvaluationAnswer 
} from '@/api/evaluation'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const evaluation = ref<any>({})
const answerList = ref<any[]>([])
const total = ref(0)

// 查询参数
const queryParams = reactive({
  page: 1,
  size: 10
})

// 答案对话框
const answerDialogVisible = ref(false)
const answerLoading = ref(false)
const currentAnswer = ref<any>({})

// 评分对话框
const rateDialogVisible = ref(false)
const rateForm = reactive({
  answerId: 0,
  question: '',
  standardAnswer: '',
  modelAnswer: '',
  score: 0,
  feedback: ''
})

// 获取评测详情
const getEvaluationDetail = async () => {
  const id = Number(route.params.id)
  if (!id) return
  
  loading.value = true
  try {
    evaluation.value = await getEvaluationById(id)
    await getAnswers()
  } catch (error) {
    console.error('获取评测详情失败', error)
  } finally {
    loading.value = false
  }
}

// 获取评测答案
const getAnswers = async () => {
  const id = Number(route.params.id)
  if (!id) return
  
  try {
    const res = await getEvaluationAnswers(id, queryParams)
    answerList.value = res.list || []
    total.value = res.total || 0
  } catch (error) {
    console.error('获取评测答案失败', error)
  }
}

// 格式化状态
const formatStatus = (status: string) => {
  const map: Record<string, string> = {
    'created': '已创建',
    'running': '进行中',
    'completed': '已完成',
    'failed': '失败',
    'cancelled': '已取消'
  }
  return map[status] || status
}

// 获取状态标签类型
const getStatusTag = (status: string) => {
  const map: Record<string, string> = {
    'created': 'info',
    'running': 'primary',
    'completed': 'success',
    'failed': 'danger',
    'cancelled': 'warning'
  }
  return map[status] || ''
}

// 格式化评测方法
const formatEvaluationMethod = (method: string) => {
  const map: Record<string, string> = {
    'manual': '人工评测',
    'auto': '自动评测',
    'hybrid': '混合评测'
  }
  return map[method] || method
}

// 格式化答案状态
const formatAnswerStatus = (status: string) => {
  const map: Record<string, string> = {
    'pending': '待处理',
    'processing': '处理中',
    'completed': '已完成',
    'failed': '失败'
  }
  return map[status] || status
}

// 获取答案状态标签类型
const getAnswerStatusTag = (status: string) => {
  const map: Record<string, string> = {
    'pending': 'info',
    'processing': 'primary',
    'completed': 'success',
    'failed': 'danger'
  }
  return map[status] || ''
}

// 计算评测进度
const calculateProgress = () => {
  if (evaluation.value.status === 'completed') {
    return 100
  }
  
  if (!evaluation.value.questionCount || evaluation.value.questionCount === 0) {
    return 0
  }
  
  const completedAnswers = answerList.value.filter(a => a.status === 'completed').length
  return Math.round((completedAnswers / evaluation.value.questionCount) * 100)
}

// 进度格式化
const progressFormat = (percentage: number) => {
  if (!evaluation.value.questionCount) return `${percentage}%`
  const completedAnswers = answerList.value.filter(a => a.status === 'completed').length
  return `${percentage}% (${completedAnswers}/${evaluation.value.questionCount})`
}

// 获取进度条状态
const getProgressStatus = (status: string) => {
  const map: Record<string, string> = {
    'created': '',
    'running': '',
    'completed': 'success',
    'failed': 'exception',
    'cancelled': 'warning'
  }
  return map[status] || ''
}

// 每页数量变化
const handleSizeChange = (val: number) => {
  queryParams.size = val
  getAnswers()
}

// 当前页变化
const handleCurrentChange = (val: number) => {
  queryParams.page = val
  getAnswers()
}

// 查看答案
const viewAnswer = (row: any) => {
  currentAnswer.value = row
  answerDialogVisible.value = true
}

// 评分答案
const rateAnswer = (row: any) => {
  rateForm.answerId = row.answerId
  rateForm.question = row.question
  rateForm.standardAnswer = row.standardAnswer
  rateForm.modelAnswer = row.modelAnswer
  rateForm.score = row.score || 0
  rateForm.feedback = row.feedback || ''
  rateDialogVisible.value = true
}

// 提交评分
const submitRate = async () => {
  try {
    await rateEvaluationAnswer(evaluation.value.evaluationId, rateForm.answerId, {
      score: rateForm.score,
      feedback: rateForm.feedback
    })
    ElMessage.success('评分成功')
    rateDialogVisible.value = false
    getAnswers()
  } catch (error) {
    console.error('评分失败', error)
  }
}

// 开始评测
const handleRunEvaluation = () => {
  ElMessageBox.confirm(
    '确认开始此评测吗？',
    '开始确认',
    {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'info'
    }
  ).then(async () => {
    try {
      await startEvaluation(evaluation.value.evaluationId)
      ElMessage.success('评测已开始')
      getEvaluationDetail()
    } catch (error) {
      console.error('开始评测失败', error)
    }
  }).catch(() => {})
}

// 返回上一页
const navigateBack = () => {
  router.back()
}

onMounted(() => {
  getEvaluationDetail()
})
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

.evaluation-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.evaluation-header h3 {
  margin: 0;
  font-size: 20px;
}

.evaluation-progress {
  margin: 20px 0;
}

.evaluation-progress h3 {
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

.evaluation-answers {
  margin: 20px 0;
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

.content-box {
  padding: 10px;
  background-color: #f5f7fa;
  border-radius: 4px;
  white-space: pre-wrap;
  margin-bottom: 15px;
  max-height: 300px;
  overflow-y: auto;
}

.content-box.small {
  max-height: 100px;
}

.answer-detail h4 {
  margin-bottom: 5px;
  color: #606266;
}

.score-display {
  padding: 10px 0;
}

.result-summary {
  margin-bottom: 20px;
}

.result-card {
  text-align: center;
}

.result-card-header {
  font-weight: bold;
}

.result-card-content {
  display: flex;
  justify-content: center;
  align-items: baseline;
}

.result-score {
  font-size: 36px;
  font-weight: bold;
  color: #409EFF;
}

.result-score-max {
  font-size: 16px;
  color: #909399;
  margin-left: 5px;
}

.chart-container {
  margin-bottom: 20px;
}

.chart-container h4 {
  margin-bottom: 10px;
}

.chart-placeholder {
  height: 300px;
  background-color: #f5f7fa;
  border-radius: 4px;
  display: flex;
  justify-content: center;
  align-items: center;
}

.chart-empty {
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
}
</style> 