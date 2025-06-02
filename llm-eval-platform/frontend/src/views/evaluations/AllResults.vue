<template>
  <div class="all-results-container">
    <div class="page-header">
      <h2>所有评测结果</h2>
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
        <el-table-column fixed="right" label="操作" width="150">
          <template #default="scope">
            <el-button link type="primary" @click="viewDetail(scope.row)">详情</el-button>
            <el-button link type="primary" @click="compareResults(scope.row)">对比</el-button>
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

    <!-- 模型对比卡片 -->
    <el-card shadow="never" class="comparison-card">
      <template #header>
        <div class="card-header">
          <span>模型对比</span>
          <el-button type="primary" @click="startComparison" :disabled="selectedModels.length < 2">开始对比</el-button>
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
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getEvaluationResults } from '@/api/evaluations'

const router = useRouter()
const loading = ref(false)
const resultsList = ref<any[]>([])
const total = ref(0)

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
    // 实际项目中应该调用API获取数据
    // const res = await getEvaluationResults(queryParams)
    // resultsList.value = res.data.content || []
    // total.value = res.data.totalElements || 0
    
    // 模拟数据
    setTimeout(() => {
      resultsList.value = [
        {
          batchId: 1,
          batchName: 'GPT-4 基准测试',
          modelName: 'GPT-4',
          datasetName: '通用能力评测集 v1.0',
          method: 'auto',
          score: 8.75,
          questionCount: 500,
          completedAt: '2023-05-15 12:45:18'
        },
        {
          batchId: 2,
          batchName: 'Claude 2 评测',
          modelName: 'Claude 2',
          datasetName: '通用能力评测集 v1.0',
          method: 'auto',
          score: 8.42,
          questionCount: 500,
          completedAt: '2023-05-14 18:30:45'
        },
        {
          batchId: 3,
          batchName: 'LLaMA 2 测试',
          modelName: 'LLaMA 2 70B',
          datasetName: '通用能力评测集 v1.0',
          method: 'auto',
          score: 7.89,
          questionCount: 500,
          completedAt: '2023-05-13 15:20:33'
        },
        {
          batchId: 4,
          batchName: 'Mistral 7B 评测',
          modelName: 'Mistral 7B',
          datasetName: '通用能力评测集 v1.0',
          method: 'judge_model',
          score: 7.65,
          questionCount: 500,
          completedAt: '2023-05-12 20:10:15'
        },
        {
          batchId: 5,
          batchName: 'Baichuan 2 评测',
          modelName: 'Baichuan 2 13B',
          datasetName: '编程能力评测集 v1.0',
          method: 'human',
          score: 7.32,
          questionCount: 300,
          completedAt: '2023-05-11 14:55:40'
        }
      ]
      total.value = 5
      loading.value = false
      
      // 更新对比数据
      updateComparisonData()
    }, 500)
  } catch (error: any) {
    ElMessage.error('获取评测结果列表失败: ' + error.message)
    loading.value = false
  }
}

// 获取模型列表
const fetchModels = async () => {
  try {
    // 实际项目中应该调用API获取数据
    // const res = await getModelList()
    // models.value = res.data
    
    // 模拟数据
    models.value = [
      { modelId: 1, name: 'GPT-4', version: '0613' },
      { modelId: 2, name: 'Claude 2', version: '' },
      { modelId: 3, name: 'LLaMA 2', version: '70B' },
      { modelId: 4, name: 'Mistral', version: '7B' },
      { modelId: 5, name: 'Baichuan 2', version: '13B' }
    ]
  } catch (error: any) {
    ElMessage.error('获取模型列表失败: ' + error.message)
  }
}

// 获取数据集列表
const fetchDatasets = async () => {
  try {
    // 实际项目中应该调用API获取数据
    // const res = await getDatasets()
    // datasets.value = res.data
    
    // 模拟数据
    datasets.value = [
      { datasetId: 1, name: '通用能力评测集 v1.0' },
      { datasetId: 2, name: '编程能力评测集 v1.0' },
      { datasetId: 3, name: '数学能力评测集 v1.0' },
      { datasetId: 4, name: '推理能力评测集 v1.0' },
      { datasetId: 5, name: '知识能力评测集 v1.0' }
    ]
  } catch (error: any) {
    ElMessage.error('获取数据集列表失败: ' + error.message)
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
  router.push(`/evaluations/detail/${row.batchId}`)
}

// 查看对比
const compareResults = (row: any) => {
  router.push(`/evaluations/results/${row.batchId}`)
}

// 开始对比
const startComparison = () => {
  if (selectedModels.length < 2) {
    ElMessage.warning('请至少选择两个模型进行对比')
    return
  }
  
  // 获取第一个模型的批次ID作为基准
  const baseBatchId = selectedModels[0]
  router.push({
    path: `/evaluations/results/${baseBatchId}`,
    query: {
      compareIds: selectedModels.join(',')
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
</script>

<style scoped>
.all-results-container {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
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