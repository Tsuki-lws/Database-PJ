<template>
  <div class="evaluation-results-container">
    <div class="page-header">
      <h2>评测结果对比</h2>
      <el-button @click="navigateBack">返回</el-button>
    </div>

    <el-card shadow="never" class="filter-container">
      <el-form :model="queryParams" label-width="80px" :inline="true">
        <el-form-item label="模型">
          <el-select 
            v-model="queryParams.modelIds" 
            multiple 
            collapse-tags 
            placeholder="选择模型"
            @change="handleModelChange">
            <el-option 
              v-for="item in modelOptions" 
              :key="item.value" 
              :label="item.label" 
              :value="item.value">
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="queryParams.categoryId" placeholder="选择分类" clearable>
            <el-option 
              v-for="item in categoryOptions" 
              :key="item.value" 
              :label="item.label" 
              :value="item.value">
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">筛选</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="results-card" v-loading="loading">
      <div class="results-header">
        <h3>模型评测结果对比</h3>
      </div>

      <div class="results-summary">
        <el-table :data="comparisonData" style="width: 100%">
          <el-table-column prop="modelName" label="模型名称" width="180" />
          <el-table-column prop="provider" label="提供商" width="120" />
          <el-table-column prop="version" label="版本" width="120" />
          <el-table-column prop="averageScore" label="平均得分" width="120">
            <template #default="scope">
              <span>{{ scope.row.averageScore.toFixed(2) }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="accuracyRate" label="正确率" width="120">
            <template #default="scope">
              <span>{{ scope.row.accuracyRate.toFixed(2) }}%</span>
            </template>
          </el-table-column>
          <el-table-column prop="averageResponseTime" label="平均响应时间(s)" width="150">
            <template #default="scope">
              <span>{{ scope.row.averageResponseTime.toFixed(2) }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="completionRate" label="完成率" width="120">
            <template #default="scope">
              <span>{{ scope.row.completionRate.toFixed(2) }}%</span>
            </template>
          </el-table-column>
          <el-table-column fixed="right" label="操作" width="120">
            <template #default="scope">
              <el-button link type="primary" @click="viewDetails(scope.row)">详情</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <el-divider />

      <div class="results-charts">
        <h3>评测指标对比</h3>
        
        <el-tabs v-model="activeTab" class="demo-tabs">
          <el-tab-pane label="得分对比" name="score">
            <div class="chart-container">
              <div class="chart-placeholder">
                <!-- 这里放置得分对比图表 -->
                <div class="chart-empty">
                  <el-empty description="暂无图表数据"></el-empty>
                </div>
              </div>
            </div>
          </el-tab-pane>
          <el-tab-pane label="响应时间对比" name="time">
            <div class="chart-container">
              <div class="chart-placeholder">
                <!-- 这里放置响应时间对比图表 -->
                <div class="chart-empty">
                  <el-empty description="暂无图表数据"></el-empty>
                </div>
              </div>
            </div>
          </el-tab-pane>
          <el-tab-pane label="分类得分对比" name="category">
            <div class="chart-container">
              <div class="chart-placeholder">
                <!-- 这里放置分类得分对比图表 -->
                <div class="chart-empty">
                  <el-empty description="暂无图表数据"></el-empty>
                </div>
              </div>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>

      <el-divider />

      <div class="results-questions">
        <h3>问题对比分析</h3>
        
        <el-table :data="questionComparisonData" style="width: 100%">
          <el-table-column prop="questionId" label="问题ID" width="100" />
          <el-table-column prop="question" label="问题内容" show-overflow-tooltip />
          <el-table-column prop="difficulty" label="难度" width="100">
            <template #default="scope">
              <el-tag :type="getDifficultyTag(scope.row.difficulty)">
                {{ formatDifficulty(scope.row.difficulty) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column 
            v-for="model in selectedModels" 
            :key="model.evaluationId" 
            :label="model.modelName" 
            width="120">
            <template #default="scope">
              <span>{{ getModelScore(scope.row, model.evaluationId) }}</span>
            </template>
          </el-table-column>
          <el-table-column fixed="right" label="操作" width="120">
            <template #default="scope">
              <el-button link type="primary" @click="compareAnswers(scope.row)">对比答案</el-button>
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

    <!-- 答案对比对话框 -->
    <el-dialog v-model="answerDialogVisible" title="答案对比" width="80%">
      <div class="answer-comparison" v-loading="answerLoading">
        <div class="answer-question">
          <h4>问题</h4>
          <div class="content-box">{{ currentQuestion.question }}</div>
        </div>

        <div class="answer-standard" v-if="currentQuestion.standardAnswer">
          <h4>标准答案</h4>
          <div class="content-box">{{ currentQuestion.standardAnswer }}</div>
        </div>

        <el-divider content-position="center">模型回答对比</el-divider>

        <div 
          v-for="answer in modelAnswers" 
          :key="answer.evaluationId" 
          class="model-answer">
          <h4>{{ answer.modelName }} (得分: {{ answer.score ? answer.score.toFixed(1) : '-' }})</h4>
          <div class="content-box">{{ answer.modelAnswer }}</div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { 
  getEvaluationBatchById, 
  getEvaluationComparison, 
  getQuestionComparison, 
  getModelAnswerComparison 
} from '@/api/evaluation'
import { getAllCategories } from '@/api/category'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const answerLoading = ref(false)
const batchId = Number(route.params.batchId)
const activeTab = ref('score')
const total = ref(0)

// 查询参数
const queryParams = reactive({
  page: 1,
  size: 10,
  modelIds: [] as number[],
  categoryId: undefined
})

// 模型选项
const modelOptions = ref<Array<{value: number, label: string}>>([])
const selectedModels = ref<any[]>([])

// 分类选项
const categoryOptions = ref<Array<{value: number, label: string}>>([])

// 比较数据
const comparisonData = ref<any[]>([])
const questionComparisonData = ref<any[]>([])

// 答案对比对话框
const answerDialogVisible = ref(false)
const currentQuestion = ref<any>({})
const modelAnswers = ref<any[]>([])

// 获取评测批次详情
const getBatchDetail = async () => {
  if (!batchId) return
  
  loading.value = true
  try {
    const data = await getEvaluationBatchById(batchId)
    
    // 设置模型选项
    modelOptions.value = (data.evaluations || []).map((item: any) => ({
      value: item.evaluationId,
      label: item.modelName,
      ...item
    }))
    
    // 默认选择所有模型
    queryParams.modelIds = modelOptions.value.map(item => item.value)
    selectedModels.value = modelOptions.value.map(item => item)
    
    await getCategories()
    await getComparisonData()
    await getQuestionComparison()
  } catch (error) {
    console.error('获取评测批次详情失败', error)
  } finally {
    loading.value = false
  }
}

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

// 获取比较数据
const getComparisonData = async () => {
  if (!batchId || queryParams.modelIds.length === 0) return
  
  try {
    const data = await getEvaluationComparison(batchId, {
      evaluationIds: queryParams.modelIds,
      categoryId: queryParams.categoryId
    })
    comparisonData.value = data || []
  } catch (error) {
    console.error('获取比较数据失败', error)
  }
}

// 获取问题比较数据
const getQuestionComparison = async () => {
  if (!batchId || queryParams.modelIds.length === 0) return
  
  try {
    const data = await getQuestionComparison(batchId, {
      evaluationIds: queryParams.modelIds,
      categoryId: queryParams.categoryId,
      page: queryParams.page,
      size: queryParams.size
    })
    questionComparisonData.value = data.list || []
    total.value = data.total || 0
  } catch (error) {
    console.error('获取问题比较数据失败', error)
  }
}

// 处理模型变化
const handleModelChange = (values: number[]) => {
  selectedModels.value = modelOptions.value.filter(item => values.includes(item.value))
}

// 搜索
const handleSearch = () => {
  queryParams.page = 1
  getComparisonData()
  getQuestionComparison()
}

// 重置查询条件
const resetQuery = () => {
  queryParams.categoryId = undefined
  queryParams.modelIds = modelOptions.value.map(item => item.value)
  selectedModels.value = modelOptions.value.map(item => item)
  handleSearch()
}

// 每页数量变化
const handleSizeChange = (val: number) => {
  queryParams.size = val
  getQuestionComparison()
}

// 当前页变化
const handleCurrentChange = (val: number) => {
  queryParams.page = val
  getQuestionComparison()
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

// 获取模型得分
const getModelScore = (question: any, evaluationId: number) => {
  if (!question.modelScores) return '-'
  const score = question.modelScores.find((s: any) => s.evaluationId === evaluationId)
  return score ? score.score.toFixed(1) : '-'
}

// 查看详情
const viewDetails = (row: any) => {
  router.push(`/evaluations/detail/${row.evaluationId}`)
}

// 对比答案
const compareAnswers = async (row: any) => {
  if (queryParams.modelIds.length === 0) {
    ElMessage.warning('请至少选择一个模型')
    return
  }
  
  answerLoading.value = true
  answerDialogVisible.value = true
  currentQuestion.value = row
  
  try {
    const data = await getModelAnswerComparison(batchId, row.questionId, {
      evaluationIds: queryParams.modelIds
    })
    modelAnswers.value = data || []
  } catch (error) {
    console.error('获取答案对比数据失败', error)
  } finally {
    answerLoading.value = false
  }
}

// 返回上一页
const navigateBack = () => {
  router.back()
}

onMounted(() => {
  getBatchDetail()
})
</script>

<style scoped>
.evaluation-results-container {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.filter-container {
  margin-bottom: 20px;
}

.results-card {
  margin-bottom: 20px;
}

.results-header {
  margin-bottom: 20px;
}

.results-header h3 {
  margin: 0;
  font-size: 18px;
}

.results-summary {
  margin-bottom: 20px;
}

.results-charts {
  margin: 20px 0;
}

.results-questions {
  margin: 20px 0;
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

.chart-container {
  margin-top: 20px;
}

.chart-placeholder {
  height: 400px;
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

.content-box {
  padding: 10px;
  background-color: #f5f7fa;
  border-radius: 4px;
  white-space: pre-wrap;
  margin-bottom: 15px;
  max-height: 200px;
  overflow-y: auto;
}

.answer-comparison h4 {
  margin-bottom: 5px;
  color: #606266;
}

.model-answer {
  margin-bottom: 20px;
}
</style> 