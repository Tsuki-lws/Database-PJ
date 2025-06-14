<template>
  <div class="import-model-answer-container">
    <div class="page-header">
      <h2>导入模型回答</h2>
      <div>
        <el-button @click="navigateToModelAnswers">返回模型回答列表</el-button>
      </div>
    </div>

    <!-- 筛选和选择问题 -->
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

    <!-- 问题列表 -->
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

    <!-- 导入回答对话框 -->
    <el-dialog
      v-model="importDialogVisible"
      title="导入模型回答"
      width="600px"
      destroy-on-close
    >
      <div v-if="currentQuestion" class="question-detail">
        <h3>问题内容</h3>
        <div class="question-content">{{ currentQuestion.question }}</div>
        
        <template v-if="currentQuestion.standardAnswer">
          <h3>标准答案</h3>
          <div class="standard-answer">{{ currentQuestion.standardAnswer.answer }}</div>
        </template>
      </div>

      <el-form
        ref="importFormRef"
        :model="importForm"
        :rules="importRules"
        label-width="100px"
        style="margin-top: 20px;"
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
      </el-form>
      <template #footer>
        <el-button @click="importDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitImport" :loading="submitLoading">确定导入</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getStandardQuestions } from '@/api/question'
import { getModelList } from '@/api/evaluation'
import { importModelAnswer } from '@/api/import'
import { getAllCategories } from '@/api/category'
import type { FormInstance, FormRules } from 'element-plus'

const router = useRouter()
const loading = ref(false)
const submitLoading = ref(false)
const questionList = ref<any[]>([])
const total = ref(0)
const importDialogVisible = ref(false)
const currentQuestion = ref<any>(null)
const models = ref<any[]>([])
const importFormRef = ref<FormInstance>()

// 分类选项
const categoryOptions = ref<{value: number, label: string}[]>([])

// 查询参数
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

// 获取问题列表
const getQuestionList = async () => {
  loading.value = true
  try {
    const res = await getStandardQuestions(queryParams)
    
    if (res && res.data && res.data.content) {
      questionList.value = res.data.content
      total.value = res.data.total
      
      // 调试日志
      console.log('获取到的问题列表:', questionList.value)
      if(questionList.value.length > 0) {
        console.log('第一个问题的分类信息:', questionList.value[0].category)
      }
      
    } else if (Array.isArray(res)) {
      questionList.value = res
      total.value = res.length
      
      // 调试日志
      console.log('获取到的问题列表(数组):', questionList.value)
      if(questionList.value.length > 0) {
        console.log('第一个问题的分类信息:', questionList.value[0].category)
      }
      
    } else {
      // 模拟数据
      questionList.value = [
        { 
          standardQuestionId: 1, 
          question: '什么是大语言模型？',
          category: { categoryName: '人工智能', categoryId: 1 },
          questionType: 'subjective',
          standardAnswer: {
            answer: '大语言模型是一种基于深度学习的自然语言处理模型，通过在海量文本数据上进行预训练，能够理解和生成人类语言。'
          }
        },
        { 
          standardQuestionId: 2, 
          question: '解释一下量子计算的基本原理',
          category: { categoryName: '量子计算', categoryId: 2 },
          questionType: 'subjective',
          standardAnswer: {
            answer: '量子计算利用量子力学原理进行信息处理，核心是量子比特（qubit）。'
          }
        },
        { 
          standardQuestionId: 3, 
          question: '编写一个快速排序算法',
          category: { categoryName: '算法', categoryId: 3 },
          questionType: 'subjective',
          standardAnswer: {
            answer: '快速排序是一种分治算法，具体实现代码...'
          }
        }
      ]
      total.value = 3
    }
  } catch (error: any) {
    ElMessage.error('获取问题列表失败: ' + error.message)
    questionList.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

// 获取分类选项
const getCategoryOptions = async () => {
  try {
    const res = await getAllCategories()
    
    if (Array.isArray(res)) {
      categoryOptions.value = res.map(item => ({
        value: item.categoryId,
        label: item.name || item.categoryName
      }))
    } else if (res && res.data && Array.isArray(res.data)) {
      categoryOptions.value = res.data.map(item => ({
        value: item.categoryId,
        label: item.name || item.categoryName
      }))
    } else {
      // 模拟分类选项
      categoryOptions.value = [
        { value: 1, label: '人工智能' },
        { value: 2, label: '算法' },
        { value: 3, label: '量子计算' }
      ]
    }
    
    // 调试日志
    console.log('获取到的分类选项:', categoryOptions.value)
  } catch (error) {
    console.error('获取分类选项失败', error)
    categoryOptions.value = []
  }
}

// 获取模型列表
const getModels = async () => {
  try {
    const res = await getModelList()
    
    if (Array.isArray(res)) {
      models.value = res
    } else if (res && res.data && Array.isArray(res.data)) {
      models.value = res.data
    } else {
      // 模拟数据
      models.value = [
        { modelId: 1, name: 'GPT-4', version: '0613' },
        { modelId: 2, name: 'Claude 2', version: '' },
        { modelId: 3, name: 'LLaMA 2', version: '70B' },
        { modelId: 4, name: 'Mistral', version: '7B' },
        { modelId: 5, name: 'Baichuan 2', version: '13B' }
      ]
    }
  } catch (error: any) {
    ElMessage.error('获取模型列表失败: ' + error.message)
    models.value = []
  }
}

// 格式化问题类型
const formatQuestionType = (type: string) => {
  const map: Record<string, string> = {
    'single_choice': '单选题',
    'multiple_choice': '多选题',
    'subjective': '主观题'
  }
  return map[type] || type
}

// 获取问题类型标签类型
const getQuestionTypeTag = (type: string) => {
  const map: Record<string, string> = {
    'single_choice': 'info',
    'multiple_choice': 'warning',
    'subjective': 'primary'
  }
  return map[type] || ''
}

// 行点击
const handleRowClick = (row: any) => {
  importAnswer(row)
}

// 导入回答
const importAnswer = (row: any) => {
  currentQuestion.value = row
  importDialogVisible.value = true
  
  // 重置表单
  Object.assign(importForm, {
    questionId: row.standardQuestionId,
    modelId: undefined,
    answer: '',
    responseTime: 1000,
    status: 'success'
  })
}

// 提交导入
const submitImport = async () => {
  if (!importFormRef.value) return
  
  await importFormRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        const submitData = {
          questionId: importForm.questionId,
          modelId: importForm.modelId,
          answer: importForm.answer,
          responseTime: importForm.responseTime,
          status: importForm.status
        }
        
        await importModelAnswer(submitData)
        ElMessage.success('导入成功')
        importDialogVisible.value = false
        
      } catch (error: any) {
        ElMessage.error('导入失败: ' + error.message)
      } finally {
        submitLoading.value = false
      }
    }
  })
}

// 搜索
const handleSearch = () => {
  queryParams.page = 1
  getQuestionList()
}

// 重置查询条件
const resetQuery = () => {
  queryParams.page = 1
  queryParams.keyword = ''
  queryParams.categoryId = undefined
  queryParams.questionType = undefined
  getQuestionList()
}

// 每页数量变化
const handleSizeChange = (val: number) => {
  queryParams.size = val
  getQuestionList()
}

// 当前页变化
const handleCurrentChange = (val: number) => {
  queryParams.page = val
  getQuestionList()
}

// 导航到模型回答列表
const navigateToModelAnswers = () => {
  router.push('/answers/model')
}

onMounted(() => {
  getQuestionList()
  getCategoryOptions()
  getModels()
})
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

.question-detail {
  margin-bottom: 20px;
}

.question-detail h3 {
  margin-top: 15px;
  margin-bottom: 10px;
  color: #303133;
  font-size: 16px;
}

.question-content {
  background-color: #f5f7fa;
  padding: 12px;
  border-radius: 4px;
  margin-bottom: 15px;
}

.standard-answer {
  background-color: #f5f7fa;
  padding: 12px;
  border-radius: 4px;
  white-space: pre-wrap;
  margin-bottom: 15px;
}
</style>