<template>
  <div class="evaluation-list-container">
    <div class="page-header">
      <h2>评测批次列表</h2>
      <el-button type="primary" @click="createEvaluation">创建评测</el-button>
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
          <el-select v-model="queryParams.versionId" placeholder="选择数据集" clearable>
            <el-option
              v-for="version in versions"
              :key="version.versionId"
              :label="version.name"
              :value="version.versionId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="选择状态" clearable>
            <el-option label="待执行" value="pending" />
            <el-option label="执行中" value="in_progress" />
            <el-option label="已完成" value="completed" />
            <el-option label="失败" value="failed" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="list-card">
      <el-table v-loading="loading" :data="evaluationList" style="width: 100%">
        <el-table-column prop="batchId" label="ID" width="80" />
        <el-table-column prop="name" label="评测名称" />
        <el-table-column prop="modelName" label="模型" width="150" />
        <el-table-column prop="datasetName" label="数据集" width="150" />
        <el-table-column prop="evaluationMethod" label="评测方法" width="120">
          <template #default="scope">
            <el-tag :type="getMethodType(scope.row.evaluationMethod)">
              {{ getMethodText(scope.row.evaluationMethod) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="progress" label="进度" width="180">
          <template #default="scope">
            <el-progress 
              :percentage="scope.row.progress" 
              :status="getProgressStatus(scope.row.status)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column fixed="right" label="操作" width="200">
          <template #default="scope">
            <el-button link type="primary" @click="viewDetail(scope.row)">详情</el-button>
            <el-button link type="primary" @click="viewResults(scope.row)">结果</el-button>
            <el-button 
              link 
              type="primary" 
              @click="startEvaluation(scope.row)"
              v-if="scope.row.status === 'pending'"
            >
              开始评测
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
import { ElMessage, ElMessageBox } from 'element-plus'
import { getEvaluationBatches, startEvaluationBatch } from '@/api/evaluation'

const router = useRouter()
const loading = ref(false)
const evaluationList = ref<any[]>([])
const total = ref(0)

// 查询参数
const queryParams = reactive({
  page: 1,
  size: 10,
  modelId: '',
  versionId: '',
  status: ''
})

// 模型和数据集选项
const models = ref<any[]>([])
const versions = ref<any[]>([])

// 初始化
onMounted(() => {
  fetchData()
  fetchModels()
  fetchVersions()
})

// 获取评测列表
const fetchData = async () => {
  loading.value = true
  try {
    // 实际项目中应该调用API获取数据
    // const res = await getEvaluationBatches(queryParams)
    // evaluationList.value = res.data.content || []
    // total.value = res.data.totalElements || 0
    
    // 模拟数据
    setTimeout(() => {
      evaluationList.value = [
        {
          batchId: 1,
          name: 'GPT-4 基准测试',
          modelName: 'GPT-4',
          datasetName: '通用能力评测集 v1.0',
          evaluationMethod: 'auto',
          status: 'completed',
          progress: 100,
          createdAt: '2023-05-15 09:30:22'
        },
        {
          batchId: 2,
          name: 'Claude 2 评测',
          modelName: 'Claude 2',
          datasetName: '通用能力评测集 v1.0',
          evaluationMethod: 'auto',
          status: 'completed',
          progress: 100,
          createdAt: '2023-05-14 16:45:10'
        },
        {
          batchId: 3,
          name: 'LLaMA 2 测试',
          modelName: 'LLaMA 2 70B',
          datasetName: '通用能力评测集 v1.0',
          evaluationMethod: 'auto',
          status: 'in_progress',
          progress: 65,
          createdAt: '2023-05-13 11:22:10'
        },
        {
          batchId: 4,
          name: 'Mistral 7B 评测',
          modelName: 'Mistral 7B',
          datasetName: '通用能力评测集 v1.0',
          evaluationMethod: 'judge_model',
          status: 'pending',
          progress: 0,
          createdAt: '2023-05-12 14:15:30'
        },
        {
          batchId: 5,
          name: 'Baichuan 2 评测',
          modelName: 'Baichuan 2 13B',
          datasetName: '编程能力评测集 v1.0',
          evaluationMethod: 'human',
          status: 'failed',
          progress: 32,
          createdAt: '2023-05-11 10:05:15'
        }
      ]
      total.value = 5
      loading.value = false
    }, 500)
  } catch (error: any) {
    ElMessage.error('获取评测列表失败: ' + error.message)
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

// 获取数据集版本列表
const fetchVersions = async () => {
  try {
    // 实际项目中应该调用API获取数据
    // const res = await getDatasetVersions()
    // versions.value = res.data
    
    // 模拟数据
    versions.value = [
      { versionId: 1, name: '通用能力评测集 v1.0' },
      { versionId: 2, name: '编程能力评测集 v1.0' },
      { versionId: 3, name: '数学能力评测集 v1.0' },
      { versionId: 4, name: '推理能力评测集 v1.0' },
      { versionId: 5, name: '知识能力评测集 v1.0' }
    ]
  } catch (error: any) {
    ElMessage.error('获取数据集版本列表失败: ' + error.message)
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
  queryParams.versionId = ''
  queryParams.status = ''
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

// 创建评测
const createEvaluation = () => {
  router.push('/evaluations/create')
}

// 查看详情
const viewDetail = (row: any) => {
  router.push(`/evaluations/detail/${row.batchId}`)
}

// 查看结果
const viewResults = (row: any) => {
  router.push(`/evaluations/results/${row.batchId}`)
}

// 开始评测
const startEvaluation = async (row: any) => {
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
      // await startEvaluationBatch(row.batchId)
      
      // 模拟成功响应
      ElMessage.success('评测任务已开始执行')
      row.status = 'in_progress'
      row.progress = 5
    })
  } catch (error: any) {
    ElMessage.error('开始评测失败: ' + error.message)
  }
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