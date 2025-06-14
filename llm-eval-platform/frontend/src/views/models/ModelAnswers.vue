<template>
  <div class="model-answers-container">
    <div class="page-header">
      <h2>模型回答管理</h2>
      <div>
        <el-button type="primary" @click="navigateTo('/model-answers/import')">导入模型回答</el-button>
      </div>
    </div>

    <el-card shadow="never" class="filter-container">
      <el-form :model="queryParams" label-width="80px" :inline="true">
        <el-form-item label="问题">
          <el-input v-model="queryParams.question" placeholder="问题关键词" clearable />
        </el-form-item>
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
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="回答状态" clearable>
            <el-option label="成功" value="success" />
            <el-option label="失败" value="failed" />
            <el-option label="超时" value="timeout" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="list-card">
      <el-table v-loading="loading" :data="answersList" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="question" label="问题" show-overflow-tooltip />
        <el-table-column prop="modelName" label="模型" width="120" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="responseTime" label="响应时间" width="120">
          <template #default="scope">
            {{ scope.row.responseTime }}ms
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column fixed="right" label="操作" width="250">
          <template #default="scope">
            <el-button link type="primary" @click="viewDetail(scope.row)">查看详情</el-button>
            <el-button link type="primary" @click="evaluateAnswer(scope.row)">评测</el-button>
            <el-button link type="danger" @click="deleteAnswer(scope.row)">删除</el-button>
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

    <!-- 详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="模型回答详情"
      width="70%"
      destroy-on-close
    >
      <div v-loading="detailLoading" class="detail-dialog-content">
        <h3>问题</h3>
        <div class="detail-question">{{ currentDetail.question }}</div>
        
        <h3>模型回答</h3>
        <div class="detail-answer">{{ currentDetail.answer }}</div>
        
        <el-descriptions :column="3" border class="detail-info">
          <el-descriptions-item label="模型">{{ currentDetail.modelName }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusType(currentDetail.status)">
              {{ getStatusText(currentDetail.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="响应时间">{{ currentDetail.responseTime }}ms</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ currentDetail.createdAt }}</el-descriptions-item>
          <el-descriptions-item label="问题类型">{{ currentDetail.questionType }}</el-descriptions-item>
          <el-descriptions-item label="问题分类">{{ currentDetail.category }}</el-descriptions-item>
        </el-descriptions>
        
        <div class="detail-actions">
          <el-button type="primary" @click="evaluateAnswer(currentDetail)">评测此回答</el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { searchModelAnswers, getAnswerDetail, deleteModelAnswer } from '@/api/import'
import { getModelList } from '@/api/evaluation'
import type { AxiosResponse } from 'axios'

// 辅助类型定义
interface ModelAnswer {
  id?: number
  question?: string
  answer?: string
  modelName?: string
  status?: string
  responseTime?: number
  createdAt?: string
  questionType?: string
  category?: string
  [key: string]: any
}

const router = useRouter()
const loading = ref(false)
const detailLoading = ref(false)
const detailDialogVisible = ref(false)
const answersList = ref<ModelAnswer[]>([])
const total = ref(0)

// 查询参数
const queryParams = reactive({
  page: 1,
  size: 10,
  question: '',
  modelId: '',
  status: ''
})

// 模型选项
const models = ref<any[]>([])

// 当前详情
const currentDetail = reactive<ModelAnswer>({
  id: 0,
  question: '',
  answer: '',
  modelName: '',
  status: '',
  responseTime: 0,
  createdAt: '',
  questionType: '',
  category: ''
})

// 初始化
onMounted(() => {
  fetchData()
  fetchModels()
})

  // 获取模型回答列表
const fetchData = async () => {
  loading.value = true
  try {
    const res = await searchModelAnswers({
      page: queryParams.page - 1, // 后端页码从0开始
      size: queryParams.size,
      modelId: queryParams.modelId || undefined,
      question: queryParams.question || undefined,
      status: queryParams.status || undefined
    })
    
    // 处理后端不同格式的响应
    const responseData = res.data || res
    
    if (responseData && responseData.content && Array.isArray(responseData.content)) {
      // 处理返回的数据，转换为前端需要的格式
      answersList.value = responseData.content.map((item: any) => {
        return {
          id: item.llmAnswerId,
          question: item.standardQuestion?.question || '未知问题',
          modelName: item.model?.name + ' ' + (item.model?.version || ''),
          answer: item.content,
          status: 'success', // 默认成功状态，可根据实际情况调整
          responseTime: item.latency || 0,
          createdAt: item.createdAt || new Date().toISOString()
        }
      })
      total.value = responseData.totalElements || 0
    } else if (Array.isArray(responseData)) {
      // 处理数组格式的响应
      answersList.value = responseData.map((item: any) => {
        return {
          id: item.llmAnswerId,
          question: item.standardQuestion?.question || '未知问题',
          modelName: item.model?.name + ' ' + (item.model?.version || ''),
          answer: item.content,
          status: 'success',
          responseTime: item.latency || 0,
          createdAt: item.createdAt || new Date().toISOString()
        }
      })
      total.value = responseData.length
    } else {
      answersList.value = []
      total.value = 0
      console.error('未能识别的响应格式:', res)
    }
  } catch (error: any) {
    ElMessage.error('获取模型回答列表失败: ' + error.message)
    answersList.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

// 获取模型列表
const fetchModels = async () => {
  try {
    const res = await getModelList()
    
    // 处理后端不同格式的响应
    const responseData = res.data || res
    
    if (Array.isArray(responseData)) {
      models.value = responseData
    } else if (responseData && responseData.content && Array.isArray(responseData.content)) {
      models.value = responseData.content
    } else {
      models.value = []
      console.error('未能识别的模型响应格式:', res)
    }
  } catch (error: any) {
    ElMessage.error('获取模型列表失败: ' + error.message)
    models.value = []
  }
}

// 查看详情
const viewDetail = async (row: ModelAnswer) => {
  detailDialogVisible.value = true
  detailLoading.value = true
  
  try {
    if (!row.id) {
      ElMessage.error('回答ID无效')
      detailLoading.value = false
      return
    }
    
    const res = await getAnswerDetail(row.id)
    
    // 处理后端不同格式的响应
    const responseData = res.data || res
    
    if (responseData) {
      // 转换数据格式
      const detailData = {
        id: responseData.llmAnswerId,
        question: responseData.standardQuestion?.question || '未知问题',
        answer: responseData.content || '',
        modelName: (responseData.model?.name || '') + ' ' + (responseData.model?.version || ''),
        status: 'success', // 默认成功状态
        responseTime: responseData.latency || 0,
        createdAt: responseData.createdAt || new Date().toISOString(),
        questionType: responseData.standardQuestion?.questionType || '未知类型',
        category: responseData.standardQuestion?.categoryName || '未分类'
      }
      
      Object.assign(currentDetail, detailData)
    } else {
      ElMessage.warning('获取详情数据格式异常')
    }
  } catch (error: any) {
    ElMessage.error('获取详情失败: ' + error.message)
  } finally {
    detailLoading.value = false
  }
}

// 评测回答
const evaluateAnswer = (row: ModelAnswer) => {
  if (!row.id) {
    ElMessage.error('回答ID无效，无法评测')
    return
  }
  
  router.push({
    path: '/evaluations/manual',
    query: { answerId: row.id }
  })
}

// 删除回答
const deleteAnswer = (row: ModelAnswer) => {
  if (!row.id) {
    ElMessage.error('回答ID无效，无法删除')
    return
  }
  
  ElMessageBox.confirm(
    `确定要删除ID为 ${row.id} 的模型回答吗？`,
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await deleteModelAnswer(row.id as number)
      ElMessage.success('删除成功')
      fetchData() // 重新加载数据
    } catch (error: any) {
      ElMessage.error('删除失败: ' + error.message)
    }
  }).catch(() => {})
}

// 搜索
const handleSearch = () => {
  queryParams.page = 1
  fetchData()
}

// 重置查询条件
const resetQuery = () => {
  queryParams.page = 1
  queryParams.question = ''
  queryParams.modelId = ''
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

// 导航到指定路由
const navigateTo = (path: string) => {
  router.push(path)
}

// 获取状态类型（用于标签颜色）
const getStatusType = (status: string | undefined) => {
  switch (status) {
    case 'success': return 'success'
    case 'failed': return 'danger'
    case 'timeout': return 'warning'
    default: return 'info'
  }
}

// 获取状态文本
const getStatusText = (status: string | undefined) => {
  switch (status) {
    case 'success': return '成功'
    case 'failed': return '失败'
    case 'timeout': return '超时'
    default: return '未知'
  }
}
</script>

<style scoped>
.model-answers-container {
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

.detail-answer {
  background-color: #f5f7fa;
  padding: 15px;
  border-radius: 4px;
  min-height: 150px;
  max-height: 300px;
  overflow-y: auto;
  white-space: pre-wrap;
  margin-bottom: 20px;
}

.detail-info {
  margin-bottom: 20px;
}

.detail-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}
</style> 