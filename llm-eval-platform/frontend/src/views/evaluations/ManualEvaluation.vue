<template>
  <div class="manual-evaluation-container">
    <div class="page-header">
      <h2>人工评测</h2>
      <div>
        <el-button @click="goBack">返回</el-button>
      </div>
    </div>

    <!-- 评测界面 -->
    <div v-if="currentEvaluation" class="evaluation-interface">
      <el-card shadow="hover" class="question-card">
        <template #header>
          <div class="card-header">
            <span>问题详情</span>
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
            <el-button @click="goBack">取消</el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </div>
    
    <div v-else class="loading-container" v-loading="loading">
      <span v-if="!loading">未找到需要评测的回答</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getEvaluationDetail, submitManualEvaluation, getStandardAnswersByQuestionId, getKeyPointsByAnswerId } from '@/api/evaluations'
import { useRoute, useRouter } from 'vue-router'

const loading = ref(false)
const submitting = ref(false)
const currentEvaluation = ref<any>(null)
const route = useRoute()
const router = useRouter()

// 评测表单
const evaluationForm = reactive({
  score: 0,
  isCorrect: false,
  keyPointsStatus: [] as string[],
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

// 初始化
onMounted(() => {
  // 检查URL参数中是否有answerId
  const answerId = route.query.answerId as string
  
  if (answerId) {
    // 如果有answerId，直接加载该回答的评测详情
    startEvaluation({ answerId: parseInt(answerId) })
  } else {
    ElMessage.error('未提供有效的回答ID')
    loading.value = false
  }
})

// 开始评测
const startEvaluation = async (row: any) => {
  loading.value = true
  try {
    console.log('开始评测回答，ID:', row.answerId)
    
    // 获取评测详情
    const res = await getEvaluationDetail(row.answerId)
    console.log('获取到评测详情:', res.data)
    
    if (!res.data || res.data.error) {
      ElMessage.error(`获取评测详情失败: ${res.data?.error || '未知错误'}`)
      loading.value = false
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
  } finally {
    loading.value = false
  }
}

// 重置评测表单
const resetEvaluationForm = () => {
  evaluationForm.score = 0
  evaluationForm.isCorrect = false
  evaluationForm.keyPointsStatus = []
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
    
    const res = await submitManualEvaluation(params)
    ElMessage.success('评测提交成功')
    
    // 评测成功后跳转到评测结果页面
    if (res.data && res.data.evaluationId) {
      router.push({
        path: `/evaluations/result/${res.data.evaluationId}`
      })
    } else {
      goBack()
    }
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
  }
  
  return true
}

// 返回上一页
const goBack = () => {
  // 获取来源页面信息（如果有）
  const fromQuestion = route.query.fromQuestion
  const questionId = route.query.questionId
  
  if (fromQuestion && questionId) {
    // 如果是从问题回答列表页面过来的，则返回到该页面
    router.push(`/evaluations/model?view=answers&questionId=${questionId}`)
  } else {
    // 否则使用常规的返回上一页
    router.back()
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
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.loading-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 300px;
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

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style> 