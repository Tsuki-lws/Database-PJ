<template>
  <div class="evaluation-list-container">
    <div class="page-header">
      <h2>模型评测批次</h2>
      <el-button type="primary" @click="navigateToCreate">
        <el-icon><Plus /></el-icon>创建评测批次
      </el-button>
    </div>

    <!-- 搜索和筛选 -->
    <el-card shadow="never" class="filter-container">
      <el-form :model="queryParams" label-width="80px" :inline="true">
        <el-form-item label="模型">
          <el-select v-model="queryParams.modelId" placeholder="全部模型" clearable>
            <el-option v-for="item in modelOptions" :key="item.value" :label="item.label" :value="item.value"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="数据集">
          <el-select v-model="queryParams.versionId" placeholder="全部数据集" clearable>
            <el-option v-for="item in datasetOptions" :key="item.value" :label="item.label" :value="item.value"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="全部状态" clearable>
            <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 评测批次列表 -->
    <el-card shadow="never" class="list-card">
      <el-table v-loading="loading" :data="batchList" style="width: 100%">
        <el-table-column prop="batchId" label="ID" width="80" />
        <el-table-column prop="name" label="批次名称" show-overflow-tooltip>
          <template #default="scope">
            <el-link type="primary" @click="navigateToDetail(scope.row.batchId)">
              {{ scope.row.name }}
            </el-link>
          </template>
        </el-table-column>
        <el-table-column prop="modelName" label="评测模型" width="150" />
        <el-table-column prop="datasetName" label="数据集版本" width="150" />
        <el-table-column prop="evaluationMethod" label="评测方法" width="120">
          <template #default="scope">
            <el-tag :type="getMethodTag(scope.row.evaluationMethod)">
              {{ formatMethod(scope.row.evaluationMethod) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusTag(scope.row.status)">
              {{ formatStatus(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="progress" label="进度" width="200">
          <template #default="scope">
            <el-progress 
              :percentage="calculateProgress(scope.row)" 
              :status="getProgressStatus(scope.row.status)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="avgScore" label="平均分" width="100">
          <template #default="scope">
            <span v-if="scope.row.status === 'completed'">{{ formatScore(scope.row.avgScore) }}</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column fixed="right" label="操作" width="250">
          <template #default="scope">
            <el-button link type="primary" @click="navigateToDetail(scope.row.batchId)">详情</el-button>
            <el-button 
              v-if="scope.row.status === 'completed'" 
              link 
              type="primary" 
              @click="navigateToResults(scope.row.batchId)"
            >
              查看结果
            </el-button>
            <el-button 
              v-if="scope.row.status === 'pending'" 
              link 
              type="success" 
              @click="handleStart(scope.row)"
            >
              开始评测
            </el-button>
            <el-button 
              v-if="scope.row.status === 'pending'" 
              link 
              type="danger" 
              @click="handleDelete(scope.row)"
            >
              删除
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
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getEvaluationBatches, startEvaluationBatch } from '@/api/evaluation'
import { getModelList } from '@/api/evaluation'
import { getDatasetVersions } from '@/api/dataset'

const router = useRouter()
const loading = ref(false)
const batchList = ref([])
const total = ref(0)

// 查询参数
const queryParams = reactive({
  page: 1,
  size: 10,
  modelId: undefined,
  versionId: undefined,
  status: undefined
})

// 模型选项
const modelOptions = ref([])

// 数据集选项
const datasetOptions = ref([])

// 状态选项
const statusOptions = [
  { value: 'pending', label: '待执行' },
  { value: 'in_progress', label: '执行中' },
  { value: 'completed', label: '已完成' },
  { value: 'failed', label: '失败' }
]

// 获取评测批次列表
const getList = async () => {
  loading.value = true
  try {
    const res = await getEvaluationBatches(queryParams)
    batchList.value = res.list || []
    total.value = res.total || 0
  } catch (error) {
    console.error('获取评测批次列表失败', error)
  } finally {
    loading.value = false
  }
}

// 获取模型选项
const getModels = async () => {
  try {
    const res = await getModelList()
    modelOptions.value = res.map((item: any) => ({
      value: item.modelId,
      label: `${item.name} (${item.version})`
    }))
  } catch (error) {
    console.error('获取模型列表失败', error)
  }
}

// 获取数据集选项
const getDatasets = async () => {
  try {
    const res = await getDatasetVersions({ isPublished: true })
    datasetOptions.value = res.list.map((item: any) => ({
      value: item.versionId,
      label: item.name
    }))
  } catch (error) {
    console.error('获取数据集列表失败', error)
  }
}

// 格式化评测方法
const formatMethod = (method: string) => {
  const map: Record<string, string> = {
    'human': '人工评测',
    'auto': '自动评测',
    'judge_model': '裁判模型'
  }
  return map[method] || method
}

// 获取评测方法标签类型
const getMethodTag = (method: string) => {
  const map: Record<string, string> = {
    'human': 'info',
    'auto': 'success',
    'judge_model': 'warning'
  }
  return map[method] || ''
}

// 格式化状态
const formatStatus = (status: string) => {
  const map: Record<string, string> = {
    'pending': '待执行',
    'in_progress': '执行中',
    'completed': '已完成',
    'failed': '失败'
  }
  return map[status] || status
}

// 获取状态标签类型
const getStatusTag = (status: string) => {
  const map: Record<string, string> = {
    'pending': 'info',
    'in_progress': 'primary',
    'completed': 'success',
    'failed': 'danger'
  }
  return map[status] || ''
}

// 计算进度
const calculateProgress = (batch: any) => {
  if (batch.status === 'completed') {
    return 100
  }
  if (batch.status === 'pending') {
    return 0
  }
  // 这里应该根据实际情况计算进度
  return 50
}

// 获取进度条状态
const getProgressStatus = (status: string) => {
  const map: Record<string, string> = {
    'pending': '',
    'in_progress': 'primary',
    'completed': 'success',
    'failed': 'exception'
  }
  return map[status] || ''
}

// 格式化分数
const formatScore = (score: number) => {
  return score ? score.toFixed(2) : '-'
}

// 搜索
const handleSearch = () => {
  queryParams.page = 1
  getList()
}

// 重置查询条件
const resetQuery = () => {
  queryParams.modelId = undefined
  queryParams.versionId = undefined
  queryParams.status = undefined
  handleSearch()
}

// 每页数量变化
const handleSizeChange = (val: number) => {
  queryParams.size = val
  getList()
}

// 当前页变化
const handleCurrentChange = (val: number) => {
  queryParams.page = val
  getList()
}

// 开始评测
const handleStart = (row: any) => {
  ElMessageBox.confirm(
    `确认开始执行评测批次 "${row.name}" 吗？`,
    '开始评测',
    {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'info'
    }
  ).then(async () => {
    try {
      await startEvaluationBatch(row.batchId)
      ElMessage.success('评测已开始')
      getList()
    } catch (error) {
      console.error('开始评测失败', error)
    }
  }).catch(() => {})
}

// 删除评测批次
const handleDelete = (row: any) => {
  ElMessageBox.confirm(
    `确认删除评测批次 "${row.name}" 吗？`,
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
      getList()
    } catch (error) {
      console.error('删除评测批次失败', error)
    }
  }).catch(() => {})
}

// 导航到创建页面
const navigateToCreate = () => {
  router.push('/evaluations/create')
}

// 导航到详情页面
const navigateToDetail = (id: number) => {
  router.push(`/evaluations/detail/${id}`)
}

// 导航到结果页面
const navigateToResults = (id: number) => {
  router.push(`/evaluations/results/${id}`)
}

onMounted(() => {
  getList()
  getModels()
  getDatasets()
})
</script>

<style scoped>
.evaluation-list-container {
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

.list-card {
  margin-bottom: 20px;
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}
</style> 