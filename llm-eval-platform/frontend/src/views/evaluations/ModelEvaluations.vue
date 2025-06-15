<template>
  <div class="model-evaluations-container">
    <div class="page-header">
      <h2>模型评测结果</h2>
    </div>

    <el-card shadow="never" class="filter-container">
      <el-form :model="queryParams" label-width="80px" :inline="true">
        <el-form-item label="模型">
          <el-select v-model="queryParams.modelId" placeholder="选择模型" clearable>
            <el-option
              v-for="model in models"
              :key="model.modelId"
              :label="model.name + ' ' + (model.version || '')"
              :value="model.modelId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="数据集">
          <el-select v-model="queryParams.datasetId" placeholder="选择数据集" clearable>
            <el-option
              v-for="dataset in datasets"
              :key="dataset.datasetId"
              :label="dataset.name"
              :value="dataset.datasetId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="评测类型">
          <el-select v-model="queryParams.evaluationType" placeholder="选择评测类型" clearable>
            <el-option label="人工评测" value="manual" />
            <el-option label="自动评测" value="auto" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="results-card">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="数据集评测" name="datasets">
          <!-- 数据集列表 -->
          <div v-if="currentView === 'datasets'">
            <el-table v-loading="loading" :data="datasetsList" style="width: 100%">
              <el-table-column prop="datasetId" label="ID" width="80" />
              <el-table-column prop="name" label="数据集名称" />
              <el-table-column prop="description" label="描述" show-overflow-tooltip />
              <el-table-column prop="questionCount" label="问题数量" width="100" />
              <el-table-column prop="evaluationProgress" label="评测进度" width="180">
                <template #default="scope">
                  <el-progress 
                    :percentage="calculateProgress(scope.row.evaluatedCount, scope.row.questionCount)" 
                    :format="(percentage: number) => `${scope.row.evaluatedCount}/${scope.row.questionCount} (${percentage}%)`"
                    :status="scope.row.isFullyEvaluated ? 'success' : ''"
                  />
                </template>
              </el-table-column>
              <el-table-column prop="avgScore" label="平均分数" width="100">
                <template #default="scope">
                  <span v-if="scope.row.isFullyEvaluated" :class="getScoreClass(scope.row.avgScore)">
                    {{ scope.row.avgScore !== null ? scope.row.avgScore.toFixed(1) : '0.0' }}
                  </span>
                  <span v-else class="evaluation-incomplete">
                    未评测完
                  </span>
                </template>
              </el-table-column>
              <el-table-column fixed="right" label="操作" width="150">
                <template #default="scope">
                  <el-button link type="primary" @click="viewDatasetQuestions(scope.row)">查看问题</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>

          <!-- 数据集问题列表 -->
          <div v-else-if="currentView === 'questions'">
            <div class="back-navigation">
              <el-button icon="el-icon-back" link @click="backToDatasets">返回数据集列表</el-button>
              <h3>{{ currentDataset.name }} - 问题列表</h3>
            </div>
            
            <el-table v-loading="loading" :data="questionsList" style="width: 100%">
              <el-table-column prop="questionId" label="ID" width="80" />
              <el-table-column prop="question" label="问题" show-overflow-tooltip />
              <el-table-column prop="category" label="分类" width="120" />
              <el-table-column prop="answerCount" label="回答数量" width="100" />
              <el-table-column prop="evaluationCount" label="评测数量" width="100" />
              <el-table-column prop="avgScore" label="平均分数" width="100">
                <template #default="scope">
                  <span :class="getScoreClass(scope.row.avgScore)">
                    {{ scope.row.avgScore !== null ? Number(scope.row.avgScore).toFixed(1) : '未评分' }}
                  </span>
                </template>
              </el-table-column>
              <el-table-column fixed="right" label="操作" width="180">
                <template #default="scope">
                  <el-button link type="primary" @click="viewQuestionAnswers(scope.row)">查看回答</el-button>
                  <el-button link type="primary" @click="viewQuestionEvaluations(scope.row)" v-if="scope.row.evaluationCount > 0">查看评测</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>

          <!-- 问题回答列表 -->
          <div v-else-if="currentView === 'answers'">
            <div class="back-navigation">
              <el-button icon="el-icon-back" link @click="backToQuestions">返回问题列表</el-button>
              <h3>{{ currentQuestion.question }} - 模型回答</h3>
            </div>
            
            <el-table v-loading="loading" :data="answersList" style="width: 100%">
              <el-table-column prop="answerId" label="ID" width="80" />
              <el-table-column prop="modelName" label="模型" width="120" />
              <el-table-column prop="content" label="回答内容" show-overflow-tooltip />
              <el-table-column prop="score" label="评分" width="80">
                <template #default="scope">
                  <span :class="getScoreClass(scope.row.score)">
                    {{ scope.row.score !== null ? scope.row.score.toFixed(1) : '未评分' }}
                  </span>
                </template>
              </el-table-column>
              <el-table-column prop="evaluationType" label="评测类型" width="100">
                <template #default="scope">
                  <el-tag v-if="scope.row.evaluationType" :type="getEvaluationTypeTag(scope.row.evaluationType)">
                    {{ getEvaluationTypeText(scope.row.evaluationType) }}
                  </el-tag>
                  <span v-else>未评测</span>
                </template>
              </el-table-column>
              <el-table-column prop="evaluatedAt" label="评测时间" width="180" />
              <el-table-column fixed="right" label="操作" width="280">
                <template #default="scope">
                  <el-button v-if="scope.row.evaluationId" link type="primary" @click="viewEvaluationDetail(scope.row)">查看评测</el-button>
                  <el-button v-if="scope.row.evaluationId" link type="danger" @click="handleDelete(scope.row)">删除评测</el-button>
                  <el-button v-if="!scope.row.evaluationId" link type="success" @click="handleEvaluate(scope.row, 'auto')">自动评测</el-button>
                  <el-button v-if="!scope.row.evaluationId" link type="warning" @click="handleEvaluate(scope.row, 'manual')">人工评测</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>

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
        </el-tab-pane>
        
        <!-- <el-tab-pane label="模型评测统计" name="statistics">
          <div class="statistics-container">
            <el-row :gutter="20">
              <el-col :span="8" v-for="stat in modelStatistics" :key="stat.modelId">
                <el-card shadow="hover" class="stat-card">
                  <div class="stat-header">
                    <h3>{{ stat.modelName }}</h3>
                    <el-tag>{{ stat.evaluationCount }}个评测</el-tag>
                  </div>
                  <div class="stat-score">
                    <span class="score-value" :class="getScoreClass(stat.avgScore)">{{ stat.avgScore.toFixed(1) }}</span>
                    <span class="score-label">平均分</span>
                  </div>
                  <div class="stat-actions">
                    <el-button type="primary" link @click="viewModelEvaluations(stat.modelId)">查看详情</el-button>
                  </div>
                </el-card>
              </el-col>
            </el-row>
          </div>
        </el-tab-pane> -->
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getModelList, getModelEvaluations, getModelEvaluationStatistics, deleteEvaluation, evaluateAnswer } from '@/api/evaluations'
import { getDatasetList, getDatasetQuestions, getQuestionAnswers, getDatasetEvaluationStats } from '@/api/datasets'

const router = useRouter()
const loading = ref(false)
const total = ref(0)
const activeTab = ref('datasets')

// 当前视图状态
const currentView = ref('datasets') // 'datasets', 'questions', 'answers'
const currentDataset = ref<any>({})
const currentQuestion = ref<any>({})

// 列表数据
const datasetsList = ref<any[]>([])
const questionsList = ref<any[]>([])
const answersList = ref<any[]>([])

// 查询参数
const queryParams = reactive({
  page: 1,
  size: 10,
  modelId: '',
  datasetId: '',
  evaluationType: ''
})

// 模型选项
const models = ref<any[]>([])
// 数据集选项
const datasets = ref<any[]>([])

// 模型统计数据
const modelStatistics = ref<any[]>([])

// 初始化
onMounted(() => {
  fetchModels()
  fetchDatasets()
  fetchModelStatistics()
  
  // 检查是否有保存的状态信息
  const savedStateStr = sessionStorage.getItem('evaluationReturnState')
  if (savedStateStr) {
    try {
      const savedState = JSON.parse(savedStateStr)
      
      // 如果有保存的状态，并且是从问题列表查看评测后返回
      if (savedState.view === 'questions' && savedState.datasetId) {
        // 恢复到问题列表视图
        currentView.value = 'questions'
        currentDataset.value = {
          datasetId: savedState.datasetId,
          name: savedState.datasetName || `数据集 #${savedState.datasetId}`
        }
        // 加载问题列表
        fetchDatasetQuestions(savedState.datasetId)
        
        // 使用后清除保存的状态
        sessionStorage.removeItem('evaluationReturnState')
        return
      }
    } catch (e) {
      console.error('解析保存的状态信息失败:', e)
    }
  }
  
  // 处理URL参数
  const route = useRouter().currentRoute.value
  if (route.query.view === 'answers' && route.query.questionId) {
    // 如果URL中包含view=answers和questionId参数，则直接打开问题回答列表
    const questionId = Number(route.query.questionId)
    // 先获取问题信息
    fetchQuestionById(questionId).then(questionData => {
      if (questionData) {
        currentQuestion.value = questionData
        currentView.value = 'answers'
        // 获取问题所属的数据集
        fetchDatasetById(questionData.datasetId).then(datasetData => {
          if (datasetData) {
            currentDataset.value = datasetData
          }
          // 加载回答列表
          fetchQuestionAnswers(questionId)
        })
      }
    })
  }
})

// 获取模型列表
const fetchModels = async () => {
  try {
    const res = await getModelList()
    models.value = res.data || []
  } catch (error: any) {
    ElMessage.error('获取模型列表失败: ' + error.message)
  }
}

// 获取数据集列表
const fetchDatasets = async () => {
  loading.value = true
  try {
    const params = {
      page: queryParams.page - 1,
      size: queryParams.size,
      modelId: queryParams.modelId || undefined,
      evaluationType: queryParams.evaluationType || undefined
    }
    
    console.log('发送GET请求: /api/datasets', params)
    const res = await getDatasetList(params)
    
    if (res.data) {
      // 处理返回的数据集列表
      let datasets = [];
      if (Array.isArray(res.data)) {
        // 直接返回数组
        datasets = res.data.map((item: any) => ({
          datasetId: item.versionId,
          name: item.name,
          description: item.description || '无描述',
          questionCount: item.questionCount || 0,
          evaluationCount: item.evaluationCount || 0,
          evaluatedCount: 0, // 默认为0，后续会更新
          avgScore: 0, // 默认为0，后续会更新
          isPublished: item.isPublished,
          isFullyEvaluated: false // 默认为未完全评测
        }))
        total.value = res.data.length
      } else if (res.data.content) {
        // 分页格式返回
        datasets = res.data.content.map((item: any) => ({
          datasetId: item.versionId,
          name: item.name,
          description: item.description || '无描述',
          questionCount: item.questionCount || 0,
          evaluationCount: item.evaluationCount || 0,
          evaluatedCount: 0, // 默认为0，后续会更新
          avgScore: 0, // 默认为0，后续会更新
          isPublished: item.isPublished,
          isFullyEvaluated: false // 默认为未完全评测
        }))
        total.value = res.data.totalElements || 0
      }
      
      // 获取每个数据集的评测统计信息
      const statsPromises = datasets.map(async (dataset: any) => {
        try {
          // 调用API获取数据集评测统计信息
          const statsRes = await getDatasetEvaluationStats(dataset.datasetId);
          if (statsRes.data) {
            // 更新数据集的评测信息
            dataset.evaluatedCount = statsRes.data.evaluatedCount || 0;
            dataset.avgScore = statsRes.data.avgScore || 0;
            dataset.isFullyEvaluated = statsRes.data.isFullyEvaluated || false;
          }
        } catch (error) {
          console.error(`获取数据集${dataset.datasetId}评测统计信息失败:`, error);
        }
        return dataset;
      });
      
      // 等待所有统计信息获取完成
      datasetsList.value = await Promise.all(statsPromises);
    } else {
      datasetsList.value = []
      total.value = 0
      ElMessage.warning('未获取到数据集数据')
    }
  } catch (error: any) {
    console.error('获取数据集列表失败:', error)
    ElMessage.error('获取数据集列表失败: ' + error.message)
    datasetsList.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

// 获取数据集问题列表
const fetchDatasetQuestions = async (datasetId: number) => {
  loading.value = true
  try {
    const params = {
      page: queryParams.page - 1,
      size: queryParams.size,
      datasetId: datasetId,
      modelId: queryParams.modelId || undefined
    }
    
    console.log('发送GET请求: /api/datasets/' + datasetId + '/questions', params)
    const res = await getDatasetQuestions(params)
    
    if (res.data) {
      // 处理返回的问题列表
      if (Array.isArray(res.data)) {
        // 直接返回数组
        questionsList.value = res.data.map((item: any) => ({
          questionId: item.standardQuestionId,
          question: item.question,
          category: item.category ? item.category.categoryName : '未分类',
          answerCount: item.answerCount || 0,
          avgScore: item.avgScore || 0,
          hasStandardAnswer: item.hasStandardAnswer,
          evaluationCount: item.evaluationCount || 0
        }))
        total.value = res.data.length
      } else if (res.data.content) {
        // 分页格式返回
        questionsList.value = res.data.content.map((item: any) => ({
          questionId: item.standardQuestionId,
          question: item.question,
          category: item.category ? item.category.categoryName : '未分类',
          answerCount: item.answerCount || 0,
          avgScore: item.avgScore || 0,
          hasStandardAnswer: item.hasStandardAnswer,
          evaluationCount: item.evaluationCount || 0
        }))
        total.value = res.data.totalElements || 0
      } else {
        questionsList.value = []
        total.value = 0
        ElMessage.warning('未获取到问题数据')
      }
    } else {
      questionsList.value = []
      total.value = 0
      ElMessage.warning('未获取到问题数据')
    }
  } catch (error: any) {
    console.error('获取数据集问题列表失败:', error)
    ElMessage.error('获取数据集问题列表失败: ' + error.message)
    questionsList.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

// 获取问题回答列表
const fetchQuestionAnswers = async (questionId: number) => {
  loading.value = true
  try {
    const params = {
      page: queryParams.page - 1,
      size: queryParams.size,
      questionId: questionId,
      modelId: queryParams.modelId || undefined,
      evaluationType: queryParams.evaluationType || undefined
    }
    
    console.log('发送GET请求: /api/questions/answers', params)
    const res = await getQuestionAnswers(params)
    
    if (res.data) {
      // 处理返回的回答列表
      if (Array.isArray(res.data)) {
        // 直接返回数组
        answersList.value = res.data
        total.value = res.data.length
      } else if (res.data.content) {
        // 分页格式返回
        answersList.value = res.data.content
        total.value = res.data.totalElements || 0
      } else {
        answersList.value = []
        total.value = 0
        ElMessage.warning('未获取到回答数据')
      }
    } else {
      answersList.value = []
      total.value = 0
      ElMessage.warning('未获取到回答数据')
    }
  } catch (error: any) {
    console.error('获取问题回答列表失败:', error)
    ElMessage.error('获取问题回答列表失败: ' + error.message)
    answersList.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

// 获取模型统计数据
const fetchModelStatistics = async () => {
  try {
    const res = await getModelEvaluationStatistics()
    
    if (res.data) {
      modelStatistics.value = res.data.map((item: any) => ({
        modelId: item.modelId,
        modelName: item.modelName || '未知模型',
        evaluationCount: item.evaluationCount || 0,
        avgScore: item.avgScore || 0
      }))
    } else {
      modelStatistics.value = []
      ElMessage.warning('未获取到模型统计数据')
    }
  } catch (error: any) {
    console.error('获取模型统计数据失败:', error)
    ElMessage.error('获取模型统计数据失败: ' + error.message)
    modelStatistics.value = []
  }
}

// 查看数据集问题
const viewDatasetQuestions = (dataset: any) => {
  currentDataset.value = dataset
  currentView.value = 'questions'
  queryParams.page = 1
  fetchDatasetQuestions(dataset.datasetId)
}

// 查看问题回答
const viewQuestionAnswers = (question: any) => {
  currentQuestion.value = question
  currentView.value = 'answers'
  queryParams.page = 1
  fetchQuestionAnswers(question.questionId)
}

// 查看评测详情
const viewEvaluationDetail = (answer: any) => {
  const query: any = {
    from: 'modelEvaluations',
    questionId: currentQuestion.value.questionId
  }
  
  // 如果当前视图是从问题列表进入的回答列表，则添加返回到问题列表的标记
  if (currentView.value === 'answers' && currentDataset.value.datasetId) {
    query.datasetId = currentDataset.value.datasetId
    query.returnToQuestions = 'true'
  }
  
  router.push({
    path: `/evaluations/result/${answer.answerId}`,
    query
  })
}

// 返回数据集列表
const backToDatasets = () => {
  currentView.value = 'datasets'
  queryParams.page = 1
  fetchDatasets()
}

// 返回问题列表
const backToQuestions = () => {
  currentView.value = 'questions'
  queryParams.page = 1
  fetchDatasetQuestions(currentDataset.value.datasetId)
}

// 搜索
const handleSearch = () => {
  queryParams.page = 1
  if (currentView.value === 'datasets') {
    fetchDatasets()
  } else if (currentView.value === 'questions') {
    fetchDatasetQuestions(currentDataset.value.datasetId)
  } else if (currentView.value === 'answers') {
    fetchQuestionAnswers(currentQuestion.value.questionId)
  }
}

// 重置查询条件
const resetQuery = () => {
  queryParams.page = 1
  queryParams.modelId = ''
  queryParams.datasetId = ''
  queryParams.evaluationType = ''
  handleSearch()
}

// 每页数量变化
const handleSizeChange = (val: number) => {
  queryParams.size = val
  handleSearch()
}

// 当前页变化
const handleCurrentChange = (val: number) => {
  queryParams.page = val
  handleSearch()
}

// 查看模型评测详情
const viewModelEvaluations = (modelId: number) => {
  queryParams.modelId = modelId.toString()
  activeTab.value = 'datasets'
  currentView.value = 'datasets'
  handleSearch()
}

// 获取评分样式类
const getScoreClass = (score: number) => {
  if (score >= 8) return 'score-high'
  if (score >= 6) return 'score-medium'
  return 'score-low'
}

// 获取评测类型标签类型
const getEvaluationTypeTag = (type: string) => {
  switch (type) {
    case 'manual': return 'primary'
    case 'auto': return 'success'
    default: return 'info'
  }
}

// 获取评测类型文本
const getEvaluationTypeText = (type: string) => {
  switch (type) {
    case 'manual': return '人工评测'
    case 'auto': return '自动评测'
    default: return '未知'
  }
}

// 查看问题评测
const viewQuestionEvaluations = (question: any) => {
  // 如果问题有评测，则跳转到评测结果页面
  if (question.evaluationCount > 0) {
    // 保存当前状态信息，以便返回时能够恢复
    const currentState = {
      view: 'questions',
      datasetId: currentDataset.value.datasetId,
      datasetName: currentDataset.value.name,
      questionId: question.questionId,
      questionText: question.question,
      from: 'modelEvaluations',
      returnToQuestions: 'true'
    }
    
    // 将状态信息存储到sessionStorage，以便在返回时恢复
    sessionStorage.setItem('evaluationReturnState', JSON.stringify(currentState))
    
    // 跳转到问题评测结果页面
    router.push({
      path: `/evaluations/question/${question.questionId}`,
      query: {
        questionText: question.question,
        from: 'modelEvaluations',
        datasetId: currentDataset.value.datasetId,
        datasetName: currentDataset.value.name
      }
    })
  } else {
    ElMessage.warning('该问题暂无评测结果')
  }
}

// 删除评测
const handleDelete = async (answer: any) => {
  try {
    await ElMessageBox.confirm('确定要删除该评测结果吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    // 如果有评测ID，则调用删除API
    if (answer.evaluationId) {
      const res: any = await deleteEvaluation(answer.evaluationId)
      // 204 No Content响应会被转换为 {success: true}
      if (res && (res.success || res.data)) {
        ElMessage.success('评测结果删除成功')
        // 重新加载数据
        fetchQuestionAnswers(currentQuestion.value.questionId)
      } else {
        ElMessage.error('评测结果删除失败')
      }
    } else {
      ElMessage.error('无法删除，未找到评测ID')
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('删除评测结果失败: ' + (error.message || '未知错误'))
    }
  }
}

// 处理评测
const handleEvaluate = async (answer: any, method: string) => {
  try {
    loading.value = true
    
    if (method === 'manual') {
      // 对于人工评测，跳转到人工评测页面
      router.push({
        path: `/evaluations/manual`,
        query: {
          answerId: answer.answerId,
          fromQuestion: 'true',
          questionId: currentQuestion.value.questionId
        }
      })
      return
    }
    
    // 对于自动评测，调用API
    const res: any = await evaluateAnswer(answer.answerId, method)
    
    if (res && res.evaluationId) {
      ElMessage.success('评测完成')
      // 重新加载数据
      fetchQuestionAnswers(currentQuestion.value.questionId)
    } else {
      ElMessage.warning('评测未完成，请稍后再试')
    }
  } catch (error: any) {
    ElMessage.error('评测失败: ' + (error.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

// 根据ID获取问题信息
const fetchQuestionById = async (questionId: number) => {
  try {
    // 这里可以调用API获取问题详情
    // 简化处理：返回一个包含必要信息的对象
    return {
      questionId,
      question: `问题 #${questionId}`,
      datasetId: 1 // 假设属于数据集1
    }
  } catch (error) {
    console.error('获取问题信息失败:', error)
    return null
  }
}

// 根据ID获取数据集信息
const fetchDatasetById = async (datasetId: number) => {
  try {
    // 这里可以调用API获取数据集详情
    // 简化处理：返回一个包含必要信息的对象
    return {
      datasetId,
      name: `数据集 #${datasetId}`
    }
  } catch (error) {
    console.error('获取数据集信息失败:', error)
    return null
  }
}

// 计算评测进度百分比
const calculateProgress = (evaluatedCount: number, totalCount: number) => {
  if (totalCount === 0) return 0;
  return Math.round((evaluatedCount / totalCount) * 100);
}
</script>

<style scoped>
.model-evaluations-container {
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

.back-navigation {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
}

.back-navigation h3 {
  margin: 0 0 0 10px;
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

.score-high {
  color: #67c23a;
  font-weight: bold;
}

.score-medium {
  color: #e6a23c;
  font-weight: bold;
}

.score-low {
  color: #f56c6c;
  font-weight: bold;
}

.evaluation-incomplete {
  color: #909399;
  font-weight: bold;
}

.statistics-container {
  margin-top: 20px;
}

.stat-card {
  margin-bottom: 20px;
}

.stat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.stat-header h3 {
  margin: 0;
}

.stat-score {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 15px;
}

.score-value {
  font-size: 36px;
  font-weight: bold;
}

.score-label {
  font-size: 14px;
  color: #909399;
}

.stat-actions {
  margin-top: 15px;
  text-align: center;
}
</style> 