<template>
  <div class="model-answers-container">
    <div class="page-header">
      <h2>模型回答管理</h2>
      <el-button type="primary" @click="navigateTo('/model-answers/import')">导入模型回答</el-button>
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

const router = useRouter()
const loading = ref(false)
const detailLoading = ref(false)
const detailDialogVisible = ref(false)
const answersList = ref<any[]>([])
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
const currentDetail = reactive({
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
    // 实际项目中应该调用API获取数据
    // const res = await searchModelAnswers(queryParams)
    // answersList.value = res.data.content
    // total.value = res.data.totalElements
    
    // 模拟数据
    setTimeout(() => {
      answersList.value = [
        { 
          id: 1, 
          question: '什么是大语言模型？', 
          modelName: 'GPT-4', 
          status: 'success', 
          responseTime: 1200, 
          createdAt: '2023-05-15 14:30:22' 
        },
        { 
          id: 2, 
          question: '解释一下量子计算的基本原理', 
          modelName: 'Claude 2', 
          status: 'success', 
          responseTime: 1500, 
          createdAt: '2023-05-14 10:15:45' 
        },
        { 
          id: 3, 
          question: '编写一个快速排序算法', 
          modelName: 'LLaMA 2', 
          status: 'failed', 
          responseTime: 3000, 
          createdAt: '2023-05-16 09:22:10' 
        },
        { 
          id: 4, 
          question: '简述人工智能的发展历史', 
          modelName: 'GPT-4', 
          status: 'success', 
          responseTime: 1800, 
          createdAt: '2023-05-13 16:40:33' 
        },
        { 
          id: 5, 
          question: '如何实现神经网络的反向传播算法？', 
          modelName: 'Mistral 7B', 
          status: 'timeout', 
          responseTime: 5000, 
          createdAt: '2023-05-12 11:25:18' 
        }
      ]
      total.value = 25
      loading.value = false
    }, 500)
  } catch (error: any) {
    ElMessage.error('获取模型回答列表失败: ' + error.message)
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

// 查看详情
const viewDetail = async (row: any) => {
  detailDialogVisible.value = true
  detailLoading.value = true
  
  try {
    // 实际项目中应该调用API获取数据
    // const res = await getAnswerDetail(row.id)
    // Object.assign(currentDetail, res.data)
    
    // 模拟数据
    setTimeout(() => {
      Object.assign(currentDetail, {
        id: row.id,
        question: row.question,
        answer: row.id === 1 ? 
          '大语言模型（LLM）是一种使用深度学习技术训练的大规模自然语言处理模型。这些模型通常基于Transformer架构，拥有数十亿到数万亿个参数，通过在互联网规模的文本数据上进行训练，能够理解和生成类似人类的文本。大语言模型可以执行各种任务，如文本生成、翻译、问答、摘要等，而无需针对每个特定任务进行专门训练。它们展现出了强大的上下文理解能力和知识储备，代表了人工智能在自然语言处理领域的重要进展。' : 
          '这是模型的回答内容...',
        modelName: row.modelName,
        status: row.status,
        responseTime: row.responseTime,
        createdAt: row.createdAt,
        questionType: '主观题',
        category: '人工智能'
      })
      detailLoading.value = false
    }, 500)
  } catch (error: any) {
    ElMessage.error('获取详情失败: ' + error.message)
    detailLoading.value = false
  }
}

// 评测回答
const evaluateAnswer = (row: any) => {
  router.push({
    path: '/model-answers/evaluation',
    query: { answerId: row.id }
  })
}

// 删除回答
const deleteAnswer = (row: any) => {
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
      // 实际项目中应该调用API删除数据
      // await deleteModelAnswer(row.id)
      
      // 模拟成功响应
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
const getStatusType = (status: string) => {
  switch (status) {
    case 'success': return 'success'
    case 'failed': return 'danger'
    case 'timeout': return 'warning'
    default: return 'info'
  }
}

// 获取状态文本
const getStatusText = (status: string) => {
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