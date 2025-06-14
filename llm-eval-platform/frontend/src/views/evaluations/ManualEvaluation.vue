<template>
  <div class="manual-evaluation-container">
    <div class="page-header">
      <h2>人工评测</h2>
    </div>

    <!-- 搜索和筛选 -->
    <el-card shadow="never" class="filter-container">
      <el-form :model="queryParams" label-width="100px" :inline="true">
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
        <el-form-item label="问题类型">
          <el-select v-model="queryParams.questionType" placeholder="选择问题类型" clearable>
            <el-option label="主观题" value="subjective" />
            <el-option label="客观题" value="objective" />
            <el-option label="单选题" value="single_choice" />
            <el-option label="多选题" value="multiple_choice" />
          </el-select>
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="queryParams.categoryId" placeholder="选择问题分类" clearable>
            <el-option
              v-for="category in categories"
              :key="category.categoryId"
              :label="category.name"
              :value="category.categoryId"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 评测列表 -->
    <el-card v-if="!currentEvaluation" shadow="never" class="list-card">
      <el-table v-loading="loading" :data="evaluationList" style="width: 100%">
        <el-table-column prop="questionId" label="问题ID" width="80" />
        <el-table-column prop="question" label="问题" show-overflow-tooltip />
        <el-table-column prop="modelName" label="模型" width="120" />
        <el-table-column prop="status" label="评测状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column fixed="right" label="操作" width="150">
          <template #default="scope">
            <el-button link type="primary" @click="startEvaluation(scope.row)">评测</el-button>
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

    <!-- 评测界面 -->
    <div v-if="currentEvaluation" class="evaluation-interface">
      <el-card shadow="hover" class="question-card">
        <template #header>
          <div class="card-header">
            <span>问题详情</span>
            <el-button link @click="backToList">返回列表</el-button>
          </div>
        </template>
        <div class="question-content">
          <h3>{{ currentEvaluation.question }}</h3>
          <div v-if="currentEvaluation.questionType === 'single_choice' || currentEvaluation.questionType === 'multiple_choice'" class="options-container">
            <div v-for="(option, index) in currentEvaluation.options" :key="index" class="option-item">
              <span class="option-label">{{ option.optionCode }}.</span>
              <span class="option-text">{{ option.optionText }}</span>
            </div>
          </div>
        </div>
      </el-card>

      <el-row :gutter="20" class="answer-row">
        <el-col :span="12">
          <el-card shadow="hover" class="answer-card">
            <template #header>
              <div class="card-header">
                <span>标准答案</span>
              </div>
            </template>
            <div class="answer-content" v-html="currentEvaluation.standardAnswer"></div>
            <div v-if="currentEvaluation.keyPoints && currentEvaluation.keyPoints.length > 0" class="key-points">
              <h4>评分要点：</h4>
              <el-table :data="currentEvaluation.keyPoints" style="width: 100%">
                <el-table-column prop="pointText" label="要点描述" />
                <el-table-column prop="pointWeight" label="权重" width="80" />
                <el-table-column prop="pointType" label="类型" width="100">
                  <template #default="scope">
                    <el-tag :type="getPointType(scope.row.pointType)">
                      {{ getPointTypeText(scope.row.pointType) }}
                    </el-tag>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card shadow="hover" class="answer-card">
            <template #header>
              <div class="card-header">
                <span>模型回答</span>
                <span class="model-name">{{ currentEvaluation.modelName }}</span>
              </div>
            </template>
            <div class="answer-content" v-html="currentEvaluation.modelAnswer"></div>
          </el-card>
        </el-col>
      </el-row>

      <el-card shadow="hover" class="evaluation-card">
        <template #header>
          <div class="card-header">
            <span>评测打分</span>
          </div>
        </template>
        
        <el-form :model="evaluationForm" label-position="top">
          <!-- 客观题评测 -->
          <template v-if="currentEvaluation.questionType === 'single_choice' || currentEvaluation.questionType === 'multiple_choice'">
            <el-form-item label="是否正确">
              <el-radio-group v-model="evaluationForm.isCorrect">
                <el-radio :label="true">正确</el-radio>
                <el-radio :label="false">错误</el-radio>
              </el-radio-group>
            </el-form-item>
          </template>
          
          <!-- 主观题评测 -->
          <template v-else>
            <el-form-item label="综合评分 (0-10分)">
              <el-slider
                v-model="evaluationForm.score"
                :min="0"
                :max="10"
                :step="0.1"
                show-input
                :marks="scoreMarks"
              />
            </el-form-item>
            
            <template v-if="currentEvaluation.keyPoints && currentEvaluation.keyPoints.length > 0">
              <el-form-item label="关键点评估">
                <div v-for="(point, index) in currentEvaluation.keyPoints" :key="index" class="key-point-evaluation">
                  <span>{{ point.pointText }}</span>
                  <el-radio-group v-model="evaluationForm.keyPointsStatus[index]">
                    <el-radio label="matched">完全匹配</el-radio>
                    <el-radio label="partial">部分匹配</el-radio>
                    <el-radio label="missed">未匹配</el-radio>
                  </el-radio-group>
                </div>
              </el-form-item>
            </template>
            
            <el-form-item label="评测维度">
              <el-row :gutter="20">
                <el-col :span="8">
                  <div class="dimension-item">
                    <span>准确性</span>
                    <el-rate v-model="evaluationForm.dimensions.accuracy" :max="5" />
                  </div>
                </el-col>
                <el-col :span="8">
                  <div class="dimension-item">
                    <span>完整性</span>
                    <el-rate v-model="evaluationForm.dimensions.completeness" :max="5" />
                  </div>
                </el-col>
                <el-col :span="8">
                  <div class="dimension-item">
                    <span>清晰度</span>
                    <el-rate v-model="evaluationForm.dimensions.clarity" :max="5" />
                  </div>
                </el-col>
              </el-row>
            </el-form-item>
          </template>
          
          <el-form-item label="评测意见">
            <el-input
              v-model="evaluationForm.comments"
              type="textarea"
              :rows="4"
              placeholder="请输入评测意见..."
            />
          </el-form-item>
          
          <el-form-item>
            <el-button type="primary" @click="submitEvaluation" :loading="submitting">提交评测</el-button>
            <el-button @click="backToList">取消</el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUnevaluatedAnswers, getEvaluationDetail, submitManualEvaluation, getStandardAnswersByQuestionId, getKeyPointsByAnswerId } from '@/api/evaluations'
import { useRoute } from 'vue-router'

const loading = ref(false)
const submitting = ref(false)
const evaluationList = ref<any[]>([])
const total = ref(0)
const currentEvaluation = ref<any>(null)

// 查询参数
const queryParams = reactive({
  page: 1,
  size: 10,
  modelId: '',
  questionType: '',
  categoryId: ''
})

// 评测表单
const evaluationForm = reactive({
  score: 0,
  isCorrect: false,
  keyPointsStatus: [] as string[],
  dimensions: {
    accuracy: 0,
    completeness: 0,
    clarity: 0
  },
  comments: ''
})

// 分数标记
const scoreMarks = {
  0: '0',
  2: '2',
  4: '4',
  6: '6',
  8: '8',
  10: '10'
}

// 模型和分类数据
const models = ref<any[]>([])
const categories = ref<any[]>([])

// 初始化
onMounted(() => {
  // 检查URL参数中是否有answerId
  const route = useRoute()
  const answerId = route.query.answerId as string
  
  if (answerId) {
    // 如果有answerId，直接加载该回答的评测详情
    startEvaluation({ answerId: parseInt(answerId) })
  } else {
    // 否则加载待评测列表
    fetchData()
  }
  
  fetchModels()
  fetchCategories()
})

// 获取待评测列表
const fetchData = async () => {
  loading.value = true
  try {
    const res = await getUnevaluatedAnswers(queryParams)
    evaluationList.value = res.data.content || []
    total.value = res.data.totalElements || 0
  } catch (error: any) {
    ElMessage.error('获取待评测列表失败: ' + error.message)
  } finally {
    loading.value = false
  }
}

// 获取模型列表
const fetchModels = async () => {
  try {
    // 模拟API调用
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

// 获取分类列表
const fetchCategories = async () => {
  try {
    // 模拟API调用
    categories.value = [
      { categoryId: 1, name: '编程' },
      { categoryId: 2, name: '数学' },
      { categoryId: 3, name: '物理' },
      { categoryId: 4, name: '化学' },
      { categoryId: 5, name: '生物' }
    ]
  } catch (error: any) {
    ElMessage.error('获取分类列表失败: ' + error.message)
  }
}

// 开始评测
const startEvaluation = async (row: any) => {
  try {
    console.log('开始评测回答，ID:', row.answerId)
    
    // 获取评测详情
    const res = await getEvaluationDetail(row.answerId)
    console.log('获取到评测详情:', res.data)
    
    if (!res.data || res.data.error) {
      ElMessage.error(`获取评测详情失败: ${res.data?.error || '未知错误'}`)
      return
    }
    
    // 处理可能的null或undefined值
    const detailData = {
      ...res.data,
      standardAnswer: '',
      keyPoints: [],
      questionType: res.data.questionType || 'subjective'
    }
    
    // 先设置当前评测数据，但不包含标准答案和关键点
    currentEvaluation.value = detailData
    
    // 根据问题ID单独请求标准答案
    if (detailData.questionId) {
      console.log(`根据问题ID: ${detailData.questionId} 获取标准答案`)
      try {
        // 调用API获取标准答案
        const standardAnswerRes = await getStandardAnswersByQuestionId(detailData.questionId)
        const standardAnswerData = standardAnswerRes.data || []
        console.log('获取到标准答案:', standardAnswerData)
        
        if (standardAnswerData && standardAnswerData.length > 0) {
          // 找到最终版本的标准答案，如果没有则使用最新的
          let targetAnswer = standardAnswerData.find((answer: any) => answer.isFinal === true)
          if (!targetAnswer) {
            // 如果没有最终版本，按更新时间排序找最新的
            standardAnswerData.sort((a: any, b: any) => {
              return new Date(b.updatedAt || b.createdAt).getTime() - 
                     new Date(a.updatedAt || a.createdAt).getTime()
            })
            targetAnswer = standardAnswerData[0]
          }
          
          console.log('选择的标准答案:', targetAnswer)
          
          // 更新当前评测数据中的标准答案
          currentEvaluation.value.standardAnswer = targetAnswer.answer || ''
          currentEvaluation.value.standardAnswerId = targetAnswer.standardAnswerId
          
          // 单独请求关键点
          try {
            const keyPointsRes = await getKeyPointsByAnswerId(targetAnswer.standardAnswerId)
            const keyPointsData = keyPointsRes.data || []
            console.log('获取到关键点:', keyPointsData)
            
            if (keyPointsData && keyPointsData.length > 0) {
              // 按照pointOrder排序
              const sortedKeyPoints = [...keyPointsData].sort((a: any, b: any) => {
                if (a.pointOrder !== undefined && b.pointOrder !== undefined) {
                  return a.pointOrder - b.pointOrder
                }
                return (a.keyPointId || 0) - (b.keyPointId || 0)
              })
              
              currentEvaluation.value.keyPoints = sortedKeyPoints
              
              // 初始化关键点评估状态
              evaluationForm.keyPointsStatus = sortedKeyPoints.map(() => 'missed')
            } else {
              console.log('标准答案没有关键点')
              currentEvaluation.value.keyPoints = []
              evaluationForm.keyPointsStatus = []
            }
          } catch (error: any) {
            console.error('获取关键点失败:', error)
            currentEvaluation.value.keyPoints = []
            evaluationForm.keyPointsStatus = []
          }
        } else {
          console.log('未找到标准答案')
          currentEvaluation.value.standardAnswer = ''
          currentEvaluation.value.keyPoints = []
        }
      } catch (error: any) {
        console.error('获取标准答案失败:', error)
        ElMessage.warning('获取标准答案失败，将继续评测但没有标准答案参考')
      }
    }
    
    // 初始化评测表单
    resetEvaluationForm()
  } catch (error: any) {
    console.error('获取评测详情失败:', error)
    ElMessage.error('获取评测详情失败: ' + (error.response?.data?.error || error.message))
  }
}

// 重置评测表单
const resetEvaluationForm = () => {
  evaluationForm.score = 0
  evaluationForm.isCorrect = false
  evaluationForm.keyPointsStatus = []
  evaluationForm.dimensions.accuracy = 0
  evaluationForm.dimensions.completeness = 0
  evaluationForm.dimensions.clarity = 0
  evaluationForm.comments = ''
}

// 提交评测
const submitEvaluation = async () => {
  // 验证表单
  if (!validateForm()) {
    return
  }
  
  submitting.value = true
  try {
    // 构建提交参数
    const params = {
      answerId: currentEvaluation.value.answerId,
      score: evaluationForm.score,
      isCorrect: evaluationForm.isCorrect,
      dimensions: evaluationForm.dimensions,
      comments: evaluationForm.comments
    }
    
    // 只有在有关键点且已评估的情况下才添加关键点状态
    if (currentEvaluation.value.keyPoints && 
        currentEvaluation.value.keyPoints.length > 0 && 
        evaluationForm.keyPointsStatus.length > 0) {
      (params as any).keyPointsStatus = evaluationForm.keyPointsStatus
    }
    
    // 如果有标准答案ID，添加到参数中
    if (currentEvaluation.value.standardAnswerId) {
      (params as any).standardAnswerId = currentEvaluation.value.standardAnswerId
    }
    
    console.log('提交评测数据:', params)
    
    await submitManualEvaluation(params)
    ElMessage.success('评测提交成功')
    backToList()
    fetchData() // 刷新列表
  } catch (error: any) {
    console.error('评测提交失败:', error)
    ElMessage.error('评测提交失败: ' + (error.response?.data?.error || error.message))
  } finally {
    submitting.value = false
  }
}

// 验证表单
const validateForm = () => {
  if (currentEvaluation.value.questionType === 'single_choice' || currentEvaluation.value.questionType === 'multiple_choice') {
    // 客观题只需要验证是否已选择正确/错误
    return true
  } else {
    // 主观题需要验证评分
    if (evaluationForm.score === 0) {
      ElMessage.warning('请给出评分')
      return false
    }
    
    // 如果有关键点，检查是否全部评估
    if (currentEvaluation.value.keyPoints && 
        currentEvaluation.value.keyPoints.length > 0 && 
        evaluationForm.keyPointsStatus.length > 0) {
      console.log('验证关键点评估状态')
      const allEvaluated = evaluationForm.keyPointsStatus.every(status => status !== '')
      if (!allEvaluated) {
        ElMessage.warning('请评估所有关键点')
        return false
      }
    }
    
    // 检查评测维度
    if (evaluationForm.dimensions.accuracy === 0 || 
        evaluationForm.dimensions.completeness === 0 || 
        evaluationForm.dimensions.clarity === 0) {
      ElMessage.warning('请完成所有评测维度')
      return false
    }
  }
  
  return true
}

// 返回列表
const backToList = () => {
  currentEvaluation.value = null
  resetEvaluationForm()
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
  queryParams.questionType = ''
  queryParams.categoryId = ''
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

// 获取状态类型（用于标签颜色）
const getStatusType = (status: string) => {
  switch (status) {
    case 'pending': return 'info'
    case 'in_progress': return 'warning'
    case 'completed': return 'success'
    default: return 'info'
  }
}

// 获取状态文本
const getStatusText = (status: string) => {
  switch (status) {
    case 'pending': return '待评测'
    case 'in_progress': return '评测中'
    case 'completed': return '已完成'
    default: return '未知'
  }
}

// 获取关键点类型（用于标签颜色）
const getPointType = (type: string) => {
  switch (type) {
    case 'required': return 'primary'
    case 'bonus': return 'success'
    case 'penalty': return 'danger'
    default: return 'info'
  }
}

// 获取关键点类型文本
const getPointTypeText = (type: string) => {
  switch (type) {
    case 'required': return '必须'
    case 'bonus': return '加分'
    case 'penalty': return '减分'
    default: return '未知'
  }
}
</script>

<style scoped>
.manual-evaluation-container {
  padding: 20px;
}

.page-header {
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

.evaluation-interface {
  margin-top: 20px;
}

.question-card {
  margin-bottom: 20px;
}

.question-content {
  padding: 10px 0;
}

.options-container {
  margin-top: 15px;
}

.option-item {
  margin-bottom: 10px;
  display: flex;
}

.option-label {
  font-weight: bold;
  margin-right: 10px;
  min-width: 20px;
}

.answer-row {
  margin-bottom: 20px;
}

.answer-card {
  height: 100%;
}

.answer-content {
  padding: 10px 0;
  white-space: pre-wrap;
}

.model-name {
  font-size: 14px;
  color: #909399;
  margin-left: 10px;
}

.key-points {
  margin-top: 20px;
}

.key-point-evaluation {
  margin-bottom: 15px;
  padding: 10px;
  border-radius: 4px;
  background-color: #f5f7fa;
}

.dimension-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 15px;
}

.dimension-item span {
  margin-bottom: 5px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style> 