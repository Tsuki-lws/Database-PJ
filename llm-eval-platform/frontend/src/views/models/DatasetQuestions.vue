<template>
  <div class="dataset-questions-container">
    <div class="page-header">
      <h2>{{ datasetName || '数据集' }}问题列表</h2>
      <div>
        <el-button @click="goBack">返回数据集列表</el-button>
      </div>
    </div>

    <el-card shadow="never" class="filter-container">
      <el-form :model="queryParams" label-width="80px" :inline="true">
        <el-form-item label="问题">
          <el-input v-model="queryParams.keyword" placeholder="问题内容关键词" clearable />
        </el-form-item>
        <el-form-item label="回答状态">
          <el-select v-model="queryParams.hasAnswer" placeholder="回答状态" clearable>
            <el-option label="有回答" :value="true" />
            <el-option label="无回答" :value="false" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="list-card">
      <el-table v-loading="loading" :data="questionList" style="width: 100%">
        <el-table-column prop="questionId" label="ID" width="80" />
        <el-table-column prop="question" label="问题内容" show-overflow-tooltip />
        <el-table-column prop="questionType" label="类型" width="120">
          <template #default="scope">
            <el-tag :type="getQuestionTypeTag(scope.row.questionType)">
              {{ formatQuestionType(scope.row.questionType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="difficulty" label="难度" width="100">
          <template #default="scope">
            <el-tag :type="getDifficultyTag(scope.row.difficulty)">
              {{ formatDifficulty(scope.row.difficulty) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="answerCount" label="回答数量" width="100">
          <template #default="scope">
            <el-tag v-if="scope.row.answerCount === 0" type="danger">无回答</el-tag>
            <span v-else>{{ scope.row.answerCount }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="models" label="模型" width="200" show-overflow-tooltip>
          <template #default="scope">
            <el-tag 
              v-for="(model, index) in scope.row.models" 
              :key="index" 
              style="margin-right: 5px; margin-bottom: 5px;"
              size="small"
            >
              {{ model }}
            </el-tag>
            <span v-if="scope.row.models.length === 0">-</span>
          </template>
        </el-table-column>
        <el-table-column fixed="right" label="操作" width="150">
          <template #default="scope">
            <el-button link type="primary" @click="viewModelAnswers(scope.row)">查看回答</el-button>
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
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getQuestionsWithAnswersInDataset } from '@/api/import'

// 辅助类型定义
interface QuestionInfo {
  questionId: number
  question: string
  questionType: string
  difficulty: string
  answerCount: number
  models: string[]
  [key: string]: any
}

const router = useRouter()
const route = useRoute()
const loading = ref(false)
const questionList = ref<QuestionInfo[]>([])
const total = ref(0)
const datasetName = ref('')
const datasetId = ref<number | null>(null)

// 查询参数
const queryParams = reactive({
  page: 1,
  size: 10,
  keyword: '',
  hasAnswer: null as boolean | null // 添加回答状态过滤
})

// 初始化
onMounted(() => {
  // 从路由参数中获取数据集ID
  const id = route.params.id
  if (id && !isNaN(Number(id))) {
    datasetId.value = Number(id)
    
    // 从查询参数中获取数据集名称
    if (route.query.name) {
      datasetName.value = route.query.name as string
    }
    
    fetchData()
  } else {
    ElMessage.error('无效的数据集ID')
    goBack()
  }
})

// 获取数据集中的问题列表
const fetchData = async () => {
  if (!datasetId.value) return
  
  loading.value = true
  try {
    const res = await getQuestionsWithAnswersInDataset(datasetId.value, {
      page: queryParams.page - 1, // 后端页码从0开始
      size: queryParams.size,
      keyword: queryParams.keyword || undefined,
      hasAnswer: queryParams.hasAnswer
    })
    
    // 处理后端响应
    const responseData = res.data || res
    
    if (responseData && responseData.content && Array.isArray(responseData.content)) {
      questionList.value = responseData.content
      total.value = responseData.totalElements || responseData.content.length
      
      // 如果有数据集信息，更新数据集名称
      if (responseData.dataset && responseData.dataset.name) {
        datasetName.value = responseData.dataset.name
      }
    } else {
      questionList.value = []
      total.value = 0
      console.error('未能识别的响应格式:', res)
    }
  } catch (error: any) {
    ElMessage.error('获取问题列表失败: ' + error.message)
    questionList.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  queryParams.page = 1
  fetchData()
}

// 重置查询
const resetQuery = () => {
  queryParams.keyword = ''
  queryParams.hasAnswer = null
  queryParams.page = 1
  fetchData()
}

// 处理页码变化
const handleCurrentChange = (val: number) => {
  queryParams.page = val
  fetchData()
}

// 处理每页大小变化
const handleSizeChange = (val: number) => {
  queryParams.size = val
  queryParams.page = 1
  fetchData()
}

// 查看问题的模型回答
const viewModelAnswers = (question: QuestionInfo) => {
  if (!datasetId.value) return
  
  router.push({
    path: `/model-answers/dataset/${datasetId.value}/question/${question.questionId}`,
    query: { 
      datasetName: datasetName.value,
      questionContent: question.question
    }
  })
}

// 返回数据集列表
const goBack = () => {
  router.push('/model-answers')
}

// 格式化问题类型
const formatQuestionType = (type: string) => {
  switch (type) {
    case 'single_choice': return '单选题'
    case 'multiple_choice': return '多选题'
    case 'subjective': return '主观题'
    case 'simple_fact': return '事实题'
    default: return type
  }
}

// 获取问题类型标签样式
const getQuestionTypeTag = (type: string) => {
  switch (type) {
    case 'single_choice': return 'success'
    case 'multiple_choice': return 'warning'
    case 'subjective': return 'info'
    case 'simple_fact': return 'primary'
    default: return ''
  }
}

// 格式化难度
const formatDifficulty = (difficulty: string) => {
  switch (difficulty) {
    case 'easy': return '简单'
    case 'medium': return '中等'
    case 'hard': return '困难'
    default: return difficulty
  }
}

// 获取难度标签样式
const getDifficultyTag = (difficulty: string) => {
  switch (difficulty) {
    case 'easy': return 'success'
    case 'medium': return 'warning'
    case 'hard': return 'danger'
    default: return ''
  }
}
</script>

<style scoped>
.dataset-questions-container {
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
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style> 