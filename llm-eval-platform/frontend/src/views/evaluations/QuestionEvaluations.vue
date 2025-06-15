<template>
  <div class="question-evaluations-container">
    <div class="page-header">
      <h2>问题评测结果</h2>
      <el-button v-if="fromPrevious" icon="el-icon-back" link @click="goBack">返回上一页</el-button>
    </div>

    <el-card shadow="never" class="question-info-card">
      <h3>问题信息</h3>
      <p><strong>问题：</strong>{{ questionText }}</p>
      <p v-if="datasetName"><strong>数据集：</strong>{{ datasetName }}</p>
    </el-card>

    <el-card shadow="never" class="filter-container">
      <el-form :model="queryParams" label-width="80px" :inline="true">
        <el-form-item label="模型">
          <el-select v-model="queryParams.modelId" placeholder="选择模型" clearable>
            <el-option
              v-for="model in models"
              :key="model.modelId"
              :label="model.name + ' ' + model.version"
              :value="model.modelId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="评测方法">
          <el-select v-model="queryParams.method" placeholder="选择评测方法" clearable>
            <el-option label="人工评测" value="human" />
            <el-option label="自动评测" value="auto" />
            <el-option label="评判模型" value="judge_model" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="results-card">
      <el-table v-loading="loading" :data="evaluationsList" style="width: 100%">
        <el-table-column prop="evaluationId" label="评测ID" width="80" />
        <el-table-column prop="modelName" label="模型" width="150">
          <template #default="scope">
            {{ scope.row.modelName }} {{ scope.row.modelVersion }}
          </template>
        </el-table-column>
        <el-table-column prop="answerContent" label="回答内容" show-overflow-tooltip>
          <template #default="scope">
            <el-tooltip
              class="box-item"
              effect="dark"
              :content="scope.row.answerContent || '无回答内容'"
              placement="top-start"
              :hide-after="0"
            >
              <div class="answer-content-preview">
                {{ (scope.row.answerContent && scope.row.answerContent.length > 50) 
                   ? scope.row.answerContent.substring(0, 50) + '...' 
                   : (scope.row.answerContent || '无回答内容') }}
              </div>
            </el-tooltip>
          </template>
        </el-table-column>
        <el-table-column prop="method" label="评测方法" width="120">
          <template #default="scope">
            <el-tag type="primary">
              人工评测
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="score" label="得分" width="100">
          <template #default="scope">
            <span :class="getScoreClass(scope.row.score)">
              {{ scope.row.score.toFixed(2) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="评测时间" width="180">
          <template #default="scope">
            {{ formatDate(scope.row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column fixed="right" label="操作" width="150">
          <template #default="scope">
            <el-button link type="primary" @click="viewDetail(scope.row)">详情</el-button>
            <el-button link type="danger" @click="handleDelete(scope.row)">删除</el-button>
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
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getQuestionEvaluations, deleteEvaluation, getModelList } from '@/api/evaluations'

const router = useRouter()
const route = useRoute()
const loading = ref(false)
const evaluationsList = ref<any[]>([])
const total = ref(0)
const questionId = computed(() => Number(route.params.questionId))
const questionText = ref(route.query.questionText as string || '未知问题')
const datasetName = ref(route.query.datasetName as string || '')

// 判断是否从其他页面跳转而来
const fromPrevious = computed(() => {
  return route.query.from !== undefined
})

// 返回上一页
const goBack = () => {
  if (route.query.from === 'modelEvaluations') {
    // 如果是从ModelEvaluations页面跳转而来
    const datasetId = route.query.datasetId
    if (datasetId) {
      // 从sessionStorage中获取保存的状态
      const savedStateStr = sessionStorage.getItem('evaluationReturnState')
      if (savedStateStr) {
        try {
          const savedState = JSON.parse(savedStateStr)
          router.push({
            path: '/evaluations/model',
            query: {
              view: 'questions',
              datasetId: savedState.datasetId
            }
          })
          return
        } catch (e) {
          console.error('解析保存的状态信息失败:', e)
        }
      }
      
      // 如果没有保存的状态或解析失败，则直接返回模型评测页面
      router.push('/evaluations/model')
    } else {
      router.push('/evaluations/model')
    }
  } else {
    // 默认返回历史记录的上一页
    router.back()
  }
}

// 查询参数
const queryParams = reactive({
  page: 1,
  size: 10,
  modelId: '',
  method: ''
})

// 模型选项
const models = ref<any[]>([])

// 初始化
onMounted(() => {
  fetchData()
  fetchModels()
})

// 获取评测结果列表
const fetchData = async () => {
  loading.value = true
  try {
    const params = {
      page: queryParams.page - 1, // 前端页码从1开始，后端从0开始
      size: queryParams.size,
      modelId: queryParams.modelId || undefined,
      method: queryParams.method || undefined
    }
    
    const res = await getQuestionEvaluations(questionId.value, params)
    
    if (res.data) {
      if (Array.isArray(res.data)) {
        // 直接返回数组
        evaluationsList.value = res.data.map(formatEvaluationData)
        total.value = res.data.length
      } else if (res.data.content) {
        // 分页格式返回
        evaluationsList.value = res.data.content.map(formatEvaluationData)
        total.value = res.data.totalElements || 0
      } else {
        evaluationsList.value = []
        total.value = 0
      }
    } else {
      evaluationsList.value = []
      total.value = 0
    }
  } catch (error: any) {
    ElMessage.error('获取评测结果列表失败: ' + error.message)
    evaluationsList.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

// 格式化评测数据，确保所有必要字段都存在
const formatEvaluationData = (item: any) => {
  return {
    evaluationId: item.evaluationId || 0,
    answerId: item.answerId || 0,
    modelId: item.modelId || 0,
    modelName: item.modelName || '未知模型',
    modelVersion: item.modelVersion || '',
    answerContent: item.answerContent || '无回答内容',
    method: item.method || 'unknown',
    score: item.score || 0,
    comments: item.comments || '',
    createdAt: item.createdAt || '',
    questionId: item.questionId || questionId.value,
    question: item.question || questionText.value
  }
}

// 获取模型列表
const fetchModels = async () => {
  try {
    const res = await getModelList()
    models.value = res.data || []
  } catch (error: any) {
    ElMessage.error('获取模型列表失败: ' + error.message)
    models.value = []
  }
}

// 搜索
const handleSearch = () => {
  queryParams.page = 1
  fetchData()
}

// 重置查询条件
const resetQuery = () => {
  queryParams.page = 1
  queryParams.modelId = ''
  queryParams.method = ''
  fetchData()
}

// 每页数量变化
const handleSizeChange = (val: number) => {
  queryParams.size = val
  fetchData()
}

// 当前页变化
const handleCurrentChange = (val: number) => {
  queryParams.page = val
  fetchData()
}

// 查看详情
const viewDetail = (row: any) => {
  router.push({
    path: `/evaluations/result/${row.evaluationId}`,
    query: {
      from: 'questionEvaluations',
      questionId: questionId.value
    }
  })
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

// 获取评分样式类
const getScoreClass = (score: number) => {
  if (score >= 8) return 'score-high'
  if (score >= 6) return 'score-medium'
  return 'score-low'
}

// 格式化日期
const formatDate = (dateString: string) => {
  if (!dateString) return '';
  
  try {
    const date = new Date(dateString);
    return date.toLocaleString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit'
    });
  } catch (error) {
    return dateString;
  }
}

// 删除评测结果
const handleDelete = async (row: any) => {
  try {
    await ElMessageBox.confirm('确定要删除该评测结果吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const res: any = await deleteEvaluation(row.evaluationId)
    
    // 204 No Content响应会被转换为 {success: true}
    if (res && (res.success || res.data)) {
      ElMessage.success('评测结果删除成功')
      fetchData()
    } else {
      ElMessage.error('评测结果删除失败')
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('删除评测结果失败: ' + (error.message || '未知错误'))
    }
  }
}
</script>

<style scoped>
.question-evaluations-container {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.question-info-card {
  margin-bottom: 20px;
}

.question-info-card h3 {
  margin-top: 0;
  margin-bottom: 15px;
}

.filter-container {
  margin-bottom: 20px;
}

.results-card {
  margin-bottom: 20px;
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

.answer-content-preview {
  max-width: 100%;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  cursor: pointer;
}
</style> 