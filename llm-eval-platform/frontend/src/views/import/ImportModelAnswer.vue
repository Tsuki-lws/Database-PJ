<template>
  <div class="import-model-answer-container">
    <div class="page-header">
      <h2>导入模型回答</h2>
      <div>
        <el-button @click="navigateToModelAnswers">返回模型回答列表</el-button>
      </div>
    </div>

    <!-- 步骤条 -->
    <el-steps :active="currentStep" finish-status="success" class="steps">
      <el-step title="选择数据集" />
      <el-step title="选择问题" />
      <el-step title="导入回答" />
    </el-steps>

    <!-- 步骤1：选择数据集 -->
    <div v-if="currentStep === 1">
      <el-card shadow="never" class="filter-container">
        <el-form :model="datasetQueryParams" label-width="80px" :inline="true">
          <el-form-item label="数据集名称">
            <el-input v-model="datasetQueryParams.name" placeholder="数据集名称关键词" clearable />
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="datasetQueryParams.isPublished" placeholder="发布状态" clearable>
              <el-option label="已发布" :value="true" />
              <el-option label="未发布" :value="false" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleDatasetSearch">搜索</el-button>
            <el-button @click="resetDatasetQuery">重置</el-button>
          </el-form-item>
        </el-form>
      </el-card>

      <el-card shadow="never" class="list-card">
        <el-table v-loading="datasetLoading" :data="datasetList" style="width: 100%" @row-click="handleDatasetRowClick">
          <el-table-column type="selection" width="55" />
          <el-table-column prop="datasetId" label="ID" width="80" />
          <el-table-column prop="name" label="数据集名称" show-overflow-tooltip />
          <el-table-column prop="version" label="版本" width="80" />
          <el-table-column prop="isPublished" label="状态" width="100">
            <template #default="scope">
              <el-tag :type="scope.row.isPublished ? 'success' : 'info'">
                {{ scope.row.isPublished ? '已发布' : '未发布' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="questionCount" label="问题数量" width="100" />
          <el-table-column fixed="right" label="操作" width="150">
            <template #default="scope">
              <el-button link type="primary" @click.stop="selectDataset(scope.row)">选择</el-button>
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页 -->
        <div class="pagination-container">
          <el-pagination
            v-model:current-page="datasetQueryParams.page"
            v-model:page-size="datasetQueryParams.size"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next, jumper"
            :total="datasetTotal"
            @size-change="handleDatasetSizeChange"
            @current-change="handleDatasetCurrentChange"
          />
        </div>
      </el-card>
    </div>

    <!-- 步骤2：选择问题 -->
    <div v-if="currentStep === 2">
      <el-card shadow="never" class="dataset-info-card">
        <div class="dataset-info">
          <h3>已选数据集: {{ selectedDataset?.name || '未选择' }}</h3>
          <el-button size="small" @click="backToDatasetSelection">更换数据集</el-button>
        </div>
      </el-card>

      <el-card shadow="never" class="filter-container">
        <el-form :model="queryParams" label-width="80px" :inline="true">
          <el-form-item label="关键词">
            <el-input v-model="queryParams.keyword" placeholder="问题内容" clearable @keyup.enter="handleSearch" />
          </el-form-item>
          <el-form-item label="分类">
            <el-select v-model="queryParams.categoryId" placeholder="全部分类" clearable>
              <el-option v-for="item in categoryOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="类型">
            <el-select v-model="queryParams.questionType" placeholder="全部类型" clearable>
              <el-option v-for="item in typeOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">搜索</el-button>
            <el-button @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-form>
      </el-card>

      <el-card shadow="never" class="list-card">
        <el-table v-loading="loading" :data="questionList" style="width: 100%" @row-click="handleRowClick">
          <el-table-column type="selection" width="55" />
          <el-table-column prop="standardQuestionId" label="ID" width="80" />
          <el-table-column prop="question" label="问题内容" show-overflow-tooltip />
          <el-table-column prop="category" label="分类" width="120">
            <template #default="scope">
              {{ scope.row.category && scope.row.category.categoryName ? scope.row.category.categoryName : 
                 scope.row.category && scope.row.category.name ? scope.row.category.name : '未分类' }}
            </template>
          </el-table-column>
          <el-table-column prop="questionType" label="类型" width="120">
            <template #default="scope">
              <el-tag :type="getQuestionTypeTag(scope.row.questionType)">
                {{ formatQuestionType(scope.row.questionType) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column fixed="right" label="操作" width="150">
            <template #default="scope">
              <el-button link type="primary" @click.stop="importAnswer(scope.row)">导入回答</el-button>
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

    <!-- 步骤3：导入回答 -->
    <div v-if="currentStep === 3">
      <el-card shadow="never" class="dataset-info-card">
        <div class="dataset-info">
          <h3>已选数据集: {{ selectedDataset?.name || '未选择' }}</h3>
          <el-button size="small" @click="backToDatasetSelection">更换数据集</el-button>
        </div>
      </el-card>

      <el-card shadow="never" class="question-info-card">
        <div class="question-info">
          <h3>已选问题</h3>
          <el-button size="small" @click="backToQuestionSelection">更换问题</el-button>
        </div>
        <div class="question-content">{{ currentQuestion?.question }}</div>
        
        <template v-if="currentQuestion?.standardAnswer">
          <h3>标准答案</h3>
          <div class="standard-answer">{{ currentQuestion.standardAnswer.answer }}</div>
        </template>
      </el-card>

      <el-card shadow="never" class="import-form-card">
        <el-form
          ref="importFormRef"
          :model="importForm"
          :rules="importRules"
          label-width="100px"
        >
          <el-form-item label="选择模型" prop="modelId">
            <el-select v-model="importForm.modelId" placeholder="请选择模型" style="width: 100%;">
              <el-option
                v-for="model in models"
                :key="model.modelId"
                :label="model.name + ' ' + (model.version || '')"
                :value="model.modelId"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="模型回答" prop="answer">
            <el-input
              v-model="importForm.answer"
              type="textarea"
              :rows="10"
              placeholder="请输入模型回答内容"
            />
          </el-form-item>
          <el-form-item label="响应时间(ms)" prop="responseTime">
            <el-input-number v-model="importForm.responseTime" :min="0" :step="100" style="width: 100%;" />
          </el-form-item>
          <el-form-item label="状态" prop="status">
            <el-radio-group v-model="importForm.status">
              <el-radio label="success">成功</el-radio>
              <el-radio label="failed">失败</el-radio>
              <el-radio label="timeout">超时</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="submitImport" :loading="submitLoading">确定导入</el-button>
            <el-button @click="backToQuestionSelection">取消</el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getStandardQuestions } from '@/api/question'
import { getModelList } from '@/api/evaluation'
import { importModelAnswer, getDatasetVersionsWithAnswers } from '@/api/import'
import { getAllCategories } from '@/api/category'
import type { FormInstance, FormRules } from 'element-plus'

const router = useRouter()
const loading = ref(false)
const datasetLoading = ref(false)
const submitLoading = ref(false)
const questionList = ref<any[]>([])
const datasetList = ref<any[]>([])
const total = ref(0)
const datasetTotal = ref(0)
const currentStep = ref(1)
const importFormRef = ref<FormInstance>()
const currentQuestion = ref<any>(null)
const selectedDataset = ref<any>(null)
const models = ref<any[]>([])

// 分类选项
const categoryOptions = ref<{value: number, label: string}[]>([])

// 数据集查询参数
const datasetQueryParams = reactive({
  page: 1,
  size: 10,
  name: '',
  isPublished: undefined as boolean | undefined,
})

// 问题查询参数
const queryParams = reactive({
  page: 1,
  size: 10,
  keyword: '',
  categoryId: undefined as number | undefined,
  questionType: undefined as string | undefined,
})

// 问题类型选项
const typeOptions = [
  { value: 'single_choice', label: '单选题' },
  { value: 'multiple_choice', label: '多选题' },
  { value: 'subjective', label: '主观题' }
]

// 导入表单数据
const importForm = reactive({
  questionId: 0,
  datasetId: 0,
  modelId: undefined as number | undefined,
  answer: '',
  responseTime: 1000,
  status: 'success'
})

// 表单验证规则
const importRules = reactive<FormRules>({
  modelId: [
    { required: true, message: '请选择模型', trigger: 'change' }
  ],
  answer: [
    { required: true, message: '请输入模型回答', trigger: 'blur' }
  ]
})

// 初始化
onMounted(() => {
  fetchDatasets()
  fetchModels()
  fetchCategories()
})

// 获取数据集列表
const fetchDatasets = async () => {
  datasetLoading.value = true
  try {
    const res = await getDatasetVersionsWithAnswers()
    
    // 处理后端不同格式的响应
    const responseData = res.data || res
    
    if (Array.isArray(responseData)) {
      // 过滤数据
      let filteredData = [...responseData]
      
      if (datasetQueryParams.name) {
        const keyword = datasetQueryParams.name.toLowerCase()
        filteredData = filteredData.filter(item => 
          item.name && item.name.toLowerCase().includes(keyword))
      }
      
      if (datasetQueryParams.isPublished !== undefined) {
        filteredData = filteredData.filter(item => 
          item.isPublished === datasetQueryParams.isPublished)
      }
      
      // 手动分页
      const start = (datasetQueryParams.page - 1) * datasetQueryParams.size
      const end = start + datasetQueryParams.size
      
      datasetList.value = filteredData.slice(start, end)
      datasetTotal.value = filteredData.length
    } else {
      datasetList.value = []
      datasetTotal.value = 0
      console.error('未能识别的响应格式:', res)
    }
  } catch (error: any) {
    ElMessage.error('获取数据集列表失败: ' + error.message)
    datasetList.value = []
    datasetTotal.value = 0
  } finally {
    datasetLoading.value = false
  }
}

// 获取问题列表
const getQuestionList = async () => {
  if (!selectedDataset.value) {
    ElMessage.warning('请先选择数据集')
    return
  }
  
  loading.value = true
  try {
    // 从数据集中获取问题
    const params = {
      ...queryParams,
      datasetId: selectedDataset.value.datasetId
    }
    
    const res = await getStandardQuestions(params)
    
    if (res && res.data && res.data.content) {
      questionList.value = res.data.content
      total.value = res.data.total
    } else if (Array.isArray(res)) {
      questionList.value = res
      total.value = res.length
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

// 获取分类列表
const fetchCategories = async () => {
  try {
    const res = await getAllCategories()
    
    // 处理后端不同格式的响应
    const responseData = res.data || res
    
    if (Array.isArray(responseData)) {
      categoryOptions.value = responseData.map((item: any) => ({
        value: item.categoryId,
        label: item.name
      }))
    } else {
      categoryOptions.value = []
      console.error('未能识别的分类响应格式:', res)
    }
  } catch (error: any) {
    ElMessage.error('获取分类列表失败: ' + error.message)
    categoryOptions.value = []
  }
}

// 选择数据集
const selectDataset = (dataset: any) => {
  selectedDataset.value = dataset
  importForm.datasetId = dataset.datasetId
  currentStep.value = 2
  getQuestionList()
}

// 返回数据集选择
const backToDatasetSelection = () => {
  currentStep.value = 1
  selectedDataset.value = null
  importForm.datasetId = 0
}

// 返回问题选择
const backToQuestionSelection = () => {
  currentStep.value = 2
  currentQuestion.value = null
  importForm.questionId = 0
}

// 数据集搜索
const handleDatasetSearch = () => {
  datasetQueryParams.page = 1
  fetchDatasets()
}

// 重置数据集查询
const resetDatasetQuery = () => {
  datasetQueryParams.name = ''
  datasetQueryParams.isPublished = undefined
  datasetQueryParams.page = 1
  fetchDatasets()
}

// 处理数据集页码变化
const handleDatasetCurrentChange = (val: number) => {
  datasetQueryParams.page = val
  fetchDatasets()
}

// 处理数据集每页大小变化
const handleDatasetSizeChange = (val: number) => {
  datasetQueryParams.size = val
  datasetQueryParams.page = 1
  fetchDatasets()
}

// 处理数据集行点击
const handleDatasetRowClick = (row: any) => {
  selectDataset(row)
}

// 问题搜索
const handleSearch = () => {
  queryParams.page = 1
  getQuestionList()
}

// 重置问题查询
const resetQuery = () => {
  queryParams.page = 1
  queryParams.keyword = ''
  queryParams.categoryId = undefined
  queryParams.questionType = undefined
  getQuestionList()
}

// 处理问题页码变化
const handleCurrentChange = (val: number) => {
  queryParams.page = val
  getQuestionList()
}

// 处理问题每页大小变化
const handleSizeChange = (val: number) => {
  queryParams.size = val
  queryParams.page = 1
  getQuestionList()
}

// 处理问题行点击
const handleRowClick = (row: any) => {
  importAnswer(row)
}

// 导入回答
const importAnswer = (question: any) => {
  currentQuestion.value = question
  importForm.questionId = question.standardQuestionId
  currentStep.value = 3
}

// 提交导入
const submitImport = async () => {
  if (!importFormRef.value) return
  
  await importFormRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        // 构建提交数据，修改为与后端匹配的格式
        const submitData = {
          questionId: importForm.questionId,
          modelId: importForm.modelId,
          answer: importForm.answer,
          responseTime: importForm.responseTime,
          status: importForm.status
        }
        
        await importModelAnswer(submitData)
        ElMessage.success('导入成功')
        
        // 重置表单
        importForm.answer = ''
        importForm.responseTime = 1000
        importForm.status = 'success'
        
        // 返回问题选择页面
        currentStep.value = 2
        currentQuestion.value = null
        
      } catch (error: any) {
        ElMessage.error('导入失败: ' + error.message)
      } finally {
        submitLoading.value = false
      }
    } else {
      ElMessage.warning('请完善表单信息')
    }
  })
}

// 导航到模型回答列表
const navigateToModelAnswers = () => {
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
</script>

<style scoped>
.import-model-answer-container {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.steps {
  margin-bottom: 30px;
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

.dataset-info-card, .question-info-card {
  margin-bottom: 20px;
}

.dataset-info, .question-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.dataset-info h3, .question-info h3 {
  margin: 0;
  font-size: 16px;
}

.question-content {
  background-color: #f5f7fa;
  padding: 15px;
  border-radius: 4px;
  margin-bottom: 10px;
}

.standard-answer {
  background-color: #f5f7fa;
  padding: 15px;
  border-radius: 4px;
  margin-bottom: 20px;
}

.import-form-card {
  padding: 20px;
}
</style>