<template>
  <div class="all-results-container">
    <div class="page-header">
      <h2>所有评测结果</h2>
      <el-button v-if="fromPrevious" icon="el-icon-back" link @click="goBack">返回上一页</el-button>
    </div>

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
      <el-table v-loading="loading" :data="resultsList" style="width: 100%">
        <el-table-column prop="batchId" label="批次ID" width="80" />
        <el-table-column prop="batchName" label="评测批次" />
        <el-table-column prop="modelName" label="模型" width="150" />
        <el-table-column prop="datasetName" label="数据集" width="150" />
        <el-table-column prop="method" label="评测方法" width="120">
          <template #default="scope">
            <el-tag :type="getMethodType(scope.row.method)">
              {{ getMethodText(scope.row.method) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="score" label="平均得分" width="100">
          <template #default="scope">
            {{ scope.row.score.toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column prop="questionCount" label="问题数量" width="100" />
        <el-table-column prop="completedAt" label="完成时间" width="180" />
        <el-table-column fixed="right" label="操作" width="220">
          <template #default="scope">
            <el-button link type="primary" @click="viewDetail(scope.row)">详情</el-button>
            <!-- <el-button link type="primary" @click="compareResults(scope.row)">对比</el-button> -->
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

    <!-- 模型对比卡片
    <el-card shadow="never" class="comparison-card">
      <template #header>
        <div class="card-header">
          <span>模型对比</span>
          <el-button type="primary" @click="startComparison" :disabled="isComparisonDisabled">开始对比</el-button>
        </div>
      </template>
      
      <p>选择要对比的模型评测结果：</p>
      <el-transfer
        v-model="selectedModels"
        :data="allModelResults"
        :titles="['可选模型', '已选模型']"
      >
        <template #default="{ option }">
          {{ option.label }}
        </template>
      </el-transfer>
    </el-card>-->
  </div> 
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getEvaluationResults, getAllEvaluations, deleteEvaluation, getModelList } from '@/api/evaluations'
import { getDatasetList } from '@/api/datasets'

const router = useRouter()
const route = useRoute()
const loading = ref(false)
const resultsList = ref<any[]>([])
const total = ref(0)

// 判断是否从其他页面跳转而来
const fromPrevious = computed(() => {
  return route.query.from !== undefined
})

// 返回上一页
const goBack = () => {
  if (route.query.from === 'modelEvaluations') {
    // 如果是从ModelEvaluations页面跳转而来
    if (route.query.questionId) {
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
    // 默认返回历史记录的上一页
    router.back()
  }
}

// 查询参数
const queryParams = reactive({
  page: 1,
  size: 10,
  modelId: '',
  datasetId: '',
  method: ''
})

// 模型和数据集选项
const models = ref<any[]>([])
const datasets = ref<any[]>([])

// 对比相关
const allModelResults = ref<any[]>([])
const selectedModels = ref<number[]>([])

// 计算属性
const isComparisonDisabled = computed(() => selectedModels.value.length < 2)

// 初始化
onMounted(() => {
  fetchData()
  fetchModels()
  fetchDatasets()
})

// 获取评测结果列表
const fetchData = async () => {
  loading.value = true
  try {
    const params = {
      page: queryParams.page - 1, // 前端页码从1开始，后端从0开始
      size: queryParams.size,
      modelId: queryParams.modelId || undefined,
      datasetId: queryParams.datasetId || undefined,
      method: queryParams.method || undefined
    }
    
    const res = await getAllEvaluations(params)
    
    if (res.data) {
      if (Array.isArray(res.data)) {
        // 直接返回数组
        resultsList.value = res.data.map(formatEvaluationResult)
        total.value = res.data.length
      } else if (res.data.content) {
        // 分页格式返回
        resultsList.value = res.data.content.map(formatEvaluationResult)
        total.value = res.data.totalElements || 0
      } else {
        resultsList.value = []
        total.value = 0
      }
    } else {
      resultsList.value = []
      total.value = 0
    }
    
    // 更新对比数据
    updateComparisonData()
  } catch (error: any) {
    ElMessage.error('获取评测结果列表失败: ' + error.message)
    resultsList.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

// 格式化评测结果数据
const formatEvaluationResult = (item: any) => {
  return {
    batchId: item.evaluationId || 0,
    batchName: `${item.llmAnswer?.model?.name || '未知模型'} 评测`,
    modelName: item.llmAnswer?.model?.name + ' ' + (item.llmAnswer?.model?.version || ''),
    datasetName: item.llmAnswer?.standardQuestion?.dataset?.name || '未知数据集',
    method: item.method || 'unknown',
    score: item.score || 0,
    questionCount: 1, // 每个评测结果对应一个问题
    completedAt: item.createdAt || ''
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

// 获取数据集列表
const fetchDatasets = async () => {
  try {
    const res = await getDatasetList({})
    datasets.value = res.data || []
  } catch (error: any) {
    ElMessage.error('获取数据集列表失败: ' + error.message)
    datasets.value = []
  }
}

// 更新对比数据
const updateComparisonData = () => {
  allModelResults.value = resultsList.value.map(item => ({
    key: item.batchId,
    label: `${item.modelName} - ${item.batchName} (${item.score.toFixed(2)})`,
    modelName: item.modelName,
    batchId: item.batchId
  }))
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
  queryParams.datasetId = ''
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
    path: `/evaluations/detail/${row.batchId}`,
    query: {
      from: 'allResults'
    }
  })
}

// 查看对比
const compareResults = (row: any) => {
  router.push(`/evaluations/results/${row.batchId}`)
}

// 开始对比
const startComparison = () => {
  const selected = selectedModels.value
  if (selected.length < 2) {
    ElMessage.warning('请至少选择两个模型进行对比')
    return
  }
  
  // 获取第一个模型的批次ID作为基准
  const baseBatchId = selected[0]
  router.push({
    path: `/evaluations/results/${baseBatchId}`,
    query: {
      compareIds: selected.join(',')
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

// 删除评测结果
const handleDelete = async (row: any) => {
  try {
    await ElMessageBox.confirm('确定要删除该评测结果吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const res: any = await deleteEvaluation(row.batchId)
    
    // 204 No Content响应会被转换为 {success: true}
    if (res && (res.success || res.data)) {
      ElMessage.success('评测结果删除成功')
      queryParams.page = 1
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
.all-results-container {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.filter-container {
  margin-bottom: 20px;
}

.results-card {
  margin-bottom: 20px;
}

.comparison-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}
</style> 