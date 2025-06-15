<template>
  <div class="evaluation-result-container">
    <div class="page-header">
      <h2>评测结果详情</h2>
      <div>
        <el-button @click="goBack">返回</el-button>
      </div>
    </div>

    <div v-loading="loading" class="result-content">
      <el-card shadow="hover" class="info-card">
        <template #header>
          <div class="card-header">
            <span>基本信息</span>
          </div>
        </template>
        
        <el-descriptions :column="3" border>
          <el-descriptions-item label="问题ID">{{ resultData.questionId }}</el-descriptions-item>
          <el-descriptions-item label="模型">{{ resultData.modelName }}</el-descriptions-item>
          <el-descriptions-item label="评测时间">{{ resultData.evaluatedAt }}</el-descriptions-item>
          <el-descriptions-item label="问题类型">{{ getQuestionTypeText(resultData.questionType) }}</el-descriptions-item>
          <el-descriptions-item label="分类">{{ resultData.category }}</el-descriptions-item>
          <el-descriptions-item label="评测人员">{{ resultData.evaluator || '系统评测' }}</el-descriptions-item>
        </el-descriptions>
      </el-card>

      <el-row :gutter="20" class="score-row">
        <el-col :span="24">
          <el-card shadow="hover" class="score-card">
            <div class="score-value" :class="getScoreClass(resultData.score)">
              {{ resultData.score !== null ? resultData.score.toFixed(1) : '未评分' }}
            </div>
            <div class="score-label">总分</div>
          </el-card>
        </el-col>
      </el-row>

      <el-card shadow="hover" class="question-card">
        <template #header>
          <div class="card-header">
            <span>问题详情</span>
          </div>
        </template>
        <div class="question-content">
          <h3>{{ resultData.question }}</h3>
          <div v-if="resultData.questionType === 'single_choice' || resultData.questionType === 'multiple_choice'" class="options-container">
            <div v-for="(option, index) in resultData.options" :key="index" class="option-item">
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
            <div class="answer-content" v-html="resultData.standardAnswer"></div>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card shadow="hover" class="answer-card">
            <template #header>
              <div class="card-header">
                <span>模型回答</span>
                <span class="model-name">{{ resultData.modelName }}</span>
              </div>
            </template>
            <div class="answer-content" v-html="resultData.modelAnswer"></div>
          </el-card>
        </el-col>
      </el-row>

      <el-card v-if="resultData.keyPoints && resultData.keyPoints.length > 0" shadow="hover" class="keypoints-card">
        <template #header>
          <div class="card-header">
            <span>关键点评估</span>
          </div>
        </template>
        
        <el-table :data="resultData.keyPoints" style="width: 100%">
          <el-table-column prop="pointText" label="关键点" />
          <el-table-column prop="pointWeight" label="权重" width="80" />
          <el-table-column prop="matchStatus" label="匹配状态" width="120">
            <template #default="scope">
              <el-tag :type="getMatchStatusType(scope.row.matchStatus)">
                {{ getMatchStatusText(scope.row.matchStatus) }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
      </el-card>

      <el-card v-if="resultData.comments" shadow="hover" class="comments-card">
        <template #header>
          <div class="card-header">
            <span>评测意见</span>
          </div>
        </template>
        <div class="comments-content">{{ resultData.comments }}</div>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getEvaluationResultDetailList } from '@/api/evaluations'

const route = useRoute()
const router = useRouter()
const loading = ref(true)

// 评测结果数据
const resultData = reactive({
  id: 0,
  questionId: 0,
  question: '',
  modelName: '',
  standardAnswer: '',
  modelAnswer: '',
  score: null as number | null,
  evaluatedAt: '',
  evaluator: '',
  questionType: '',
  category: '',
  options: [] as Array<{optionCode: string, optionText: string}>,
  keyPoints: [] as Array<{pointText: string, pointWeight: number, matchStatus: string}>,
  comments: ''
})

onMounted(() => {
  const evaluationId = route.params.id as string
  
  if (evaluationId) {
    fetchEvaluationResult(parseInt(evaluationId))
  } else {
    ElMessage.error('评测ID无效')
    loading.value = false
  }
})

// 获取评测结果
const fetchEvaluationResult = async (evaluationId: number) => {
  try {
    console.log('获取评测结果详情，ID:', evaluationId);
    
    const res = await getEvaluationResultDetailList(evaluationId);
    console.log('API响应数据:', res);
    
    if (res.data) {
      // 处理返回的数据
      const data = res.data;
      console.log('处理评测结果数据:', data);
      
      // 创建一个临时对象，避免直接修改reactive对象的属性
      const tempData = {
        id: data.evaluationId || data.id || evaluationId,
        questionId: data.questionId || 0,
        question: data.question || '未知问题',
        modelName: data.modelName || '未知模型',
        standardAnswer: data.standardAnswer || '',
        modelAnswer: data.modelAnswer || data.content || '',
        score: data.score !== undefined ? data.score : null,
        evaluatedAt: data.evaluatedAt || data.createdAt || new Date().toISOString(),
        evaluator: data.evaluator || '系统评测',
        questionType: data.questionType || 'subjective',
        category: data.category || '未分类',
        options: data.options || [],
        keyPoints: (data.keyPoints || []).map((point: any) => ({
          ...point,
          matchStatus: point.matchStatus || 'missed'
        })),
        comments: data.comments || ''
      };
      
      // 将临时对象的属性赋值给reactive对象
      Object.assign(resultData, tempData);
      console.log('评测结果数据处理完成:', resultData);
    } else {
      console.error('API返回数据为空');
      ElMessage.error('获取评测结果失败: API返回数据为空');
      
      // 使用模拟数据，确保界面有内容显示
      Object.assign(resultData, {
        id: evaluationId,
        questionId: 1001,
        question: '请解释量子计算的基本原理',
        modelName: 'GPT-4',
        standardAnswer: '量子计算是利用量子力学现象如叠加和纠缠来处理信息的计算方法...',
        modelAnswer: '量子计算是一种利用量子力学原理进行信息处理的计算模型...',
        score: 9.2,
        evaluatedAt: '2023-05-20 15:30:45',
        evaluator: '张三',
        questionType: 'subjective',
        category: '计算机科学',
        options: [],
        keyPoints: [
          { pointText: '提及量子叠加', pointWeight: 2, matchStatus: 'matched' },
          { pointText: '解释量子纠缠', pointWeight: 2, matchStatus: 'partial' },
          { pointText: '讨论量子比特', pointWeight: 1, matchStatus: 'matched' }
        ],
        comments: '回答准确且全面，清晰地解释了量子计算的基本概念。'
      });
    }
  } catch (error: any) {
    console.error('获取评测结果失败:', error);
    ElMessage.error('获取评测结果失败: ' + error.message);
    
    // 使用模拟数据，确保界面有内容显示
    Object.assign(resultData, {
      id: evaluationId,
      questionId: 1001,
      question: '请解释量子计算的基本原理',
      modelName: 'GPT-4',
      standardAnswer: '量子计算是利用量子力学现象如叠加和纠缠来处理信息的计算方法...',
      modelAnswer: '量子计算是一种利用量子力学原理进行信息处理的计算模型...',
      score: 9.2,
      evaluatedAt: '2023-05-20 15:30:45',
      evaluator: '张三',
      questionType: 'subjective',
      category: '计算机科学',
      options: [],
      keyPoints: [
        { pointText: '提及量子叠加', pointWeight: 2, matchStatus: 'matched' },
        { pointText: '解释量子纠缠', pointWeight: 2, matchStatus: 'partial' },
        { pointText: '讨论量子比特', pointWeight: 1, matchStatus: 'matched' }
      ],
      comments: '回答准确且全面，清晰地解释了量子计算的基本概念。'
    });
  } finally {
    loading.value = false;
  }
}

// 返回上一页
const goBack = () => {
  // 尝试从sessionStorage获取保存的返回状态
  const savedStateStr = sessionStorage.getItem('evaluationReturnState')
  
  if (savedStateStr) {
    try {
      const savedState = JSON.parse(savedStateStr)
      
      // 如果有保存的状态，并且是从modelEvaluations页面来的
      if (savedState.from === 'modelEvaluations') {
        if (savedState.returnToQuestions === 'true' && savedState.datasetId) {
          // 返回到问题列表页面
          router.push({
            path: '/evaluations/model',
            query: {
              view: 'questions',
              datasetId: savedState.datasetId
            }
          })
          return
        }
      }
    } catch (e) {
      console.error('解析保存的状态信息失败:', e)
    }
  }
  
  // 如果没有保存的状态或解析失败，则使用URL参数
  if (route.query.from === 'modelEvaluations') {
    if (route.query.returnToQuestions === 'true' && route.query.datasetId) {
      // 返回到问题列表页面
      router.push({
        path: '/evaluations/model',
        query: {
          view: 'questions',
          datasetId: route.query.datasetId
        }
      })
    } else if (route.query.questionId) {
      // 返回到问题回答列表页面
      router.push({
        path: '/evaluations/model',
        query: {
          view: 'answers',
          questionId: route.query.questionId
        }
      })
    } else {
      router.push('/evaluations/model')
    }
  } else {
    router.back()
  }
}

// 获取问题类型文本
const getQuestionTypeText = (type: string) => {
  switch (type) {
    case 'subjective': return '主观题'
    case 'objective': return '客观题'
    case 'single_choice': return '单选题'
    case 'multiple_choice': return '多选题'
    default: return '未知类型'
  }
}

// 获取分数样式类
const getScoreClass = (score: number | null) => {
  if (score === null) return ''
  if (score >= 8) return 'score-high'
  if (score >= 6) return 'score-medium'
  return 'score-low'
}

// 获取匹配状态类型（用于标签颜色）
const getMatchStatusType = (status: string) => {
  switch (status) {
    case 'matched': return 'success'
    case 'partial': return 'warning'
    case 'missed': return 'danger'
    default: return 'info'
  }
}

// 获取匹配状态文本
const getMatchStatusText = (status: string) => {
  switch (status) {
    case 'matched': return '完全匹配'
    case 'partial': return '部分匹配'
    case 'missed': return '未匹配'
    default: return '未知'
  }
}
</script>

<style scoped>
.evaluation-result-container {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.result-content {
  min-height: 400px;
}

.info-card {
  margin-bottom: 20px;
}

.score-row {
  margin-bottom: 20px;
}

.score-card {
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  padding: 20px 0;
}

.score-value {
  font-size: 48px;
  font-weight: bold;
  margin-bottom: 10px;
}

.score-high {
  color: #67c23a;
}

.score-medium {
  color: #e6a23c;
}

.score-low {
  color: #f56c6c;
}

.score-label {
  font-size: 16px;
  color: #909399;
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
  min-height: 150px;
}

.model-name {
  font-size: 14px;
  color: #909399;
  margin-left: 10px;
}

.keypoints-card,
.comments-card {
  margin-bottom: 20px;
}

.comments-content {
  padding: 10px 0;
  white-space: pre-wrap;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style> 