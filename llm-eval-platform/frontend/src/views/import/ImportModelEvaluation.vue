<template>
  <div class="import-container">
    <div class="page-header">
      <h2>模型评测结果导入</h2>
    </div>

    <el-card shadow="never" class="import-card">
      <el-form label-position="top" :model="formData">
        <el-form-item label="选择模型回答">
          <el-select
            v-model="formData.answerId"
            placeholder="请选择模型回答"
            filterable
            remote
            :remote-method="searchAnswers"
            :loading="loading.answers"
            clearable
          >
            <el-option
              v-for="item in answerOptions"
              :key="item.answerId"
              :label="`${item.modelName} - ${item.question}`"
              :value="item.answerId"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="评测方法">
          <el-radio-group v-model="formData.method">
            <el-radio label="human">人工评测</el-radio>
            <el-radio label="auto">自动评测</el-radio>
            <el-radio label="judge_model">评判模型</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="评分 (0-10分)" v-if="formData.method !== 'auto'">
          <el-slider
            v-model="formData.score"
            :min="0"
            :max="10"
            :step="0.1"
            show-input
            :marks="scoreMarks"
          />
        </el-form-item>

        <el-form-item label="是否正确" v-if="selectedAnswerType === 'objective'">
          <el-radio-group v-model="formData.isCorrect">
            <el-radio :label="true">正确</el-radio>
            <el-radio :label="false">错误</el-radio>
          </el-radio-group>
        </el-form-item>

        <template v-if="selectedAnswerType === 'subjective' && formData.method !== 'auto'">
          <el-form-item label="评测维度">
            <el-row :gutter="20">
              <el-col :span="8">
                <div class="dimension-item">
                  <span>准确性</span>
                  <el-rate v-model="formData.dimensions.accuracy" :max="5" />
                </div>
              </el-col>
              <el-col :span="8">
                <div class="dimension-item">
                  <span>完整性</span>
                  <el-rate v-model="formData.dimensions.completeness" :max="5" />
                </div>
              </el-col>
              <el-col :span="8">
                <div class="dimension-item">
                  <span>清晰度</span>
                  <el-rate v-model="formData.dimensions.clarity" :max="5" />
                </div>
              </el-col>
            </el-row>
          </el-form-item>

          <el-form-item label="关键点评估" v-if="keyPoints.length > 0">
            <div v-for="(point, index) in keyPoints" :key="index" class="key-point-evaluation">
              <span>{{ point.pointText }}</span>
              <el-radio-group v-model="formData.keyPointsStatus[index]">
                <el-radio label="matched">完全匹配</el-radio>
                <el-radio label="partial">部分匹配</el-radio>
                <el-radio label="missed">未匹配</el-radio>
              </el-radio-group>
            </div>
          </el-form-item>
        </template>

        <el-form-item label="评测意见">
          <el-input
            v-model="formData.comments"
            type="textarea"
            :rows="4"
            placeholder="请输入评测意见..."
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="loading.submit">提交</el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 历史记录 -->
    <el-card shadow="never" class="history-card">
      <template #header>
        <div class="card-header">
          <span>最近评测记录</span>
        </div>
      </template>
      
      <el-table :data="evaluationHistory" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="question" label="问题" show-overflow-tooltip />
        <el-table-column prop="modelName" label="模型" width="120" />
        <el-table-column prop="score" label="评分" width="80" />
        <el-table-column prop="method" label="评测方法" width="100">
          <template #default="scope">
            <el-tag :type="getMethodType(scope.row.method)">
              {{ getMethodText(scope.row.method) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="evaluationTime" label="评测时间" width="180" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="scope">
            <el-button link type="primary" @click="viewDetail(scope.row)">查看详情</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { submitManualEvaluation } from '@/api/evaluations'
import { useRoute } from 'vue-router'

// 表单数据
const formData = reactive({
  answerId: null as number | null,
  method: 'human',
  score: 0,
  isCorrect: false,
  dimensions: {
    accuracy: 0,
    completeness: 0,
    clarity: 0
  },
  keyPointsStatus: [] as string[],
  comments: ''
})

// 加载状态
const loading = reactive({
  answers: false,
  submit: false
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

// 回答选项
const answerOptions = ref<any[]>([])

// 选中的回答类型
const selectedAnswerType = ref<string>('subjective')

// 关键点
const keyPoints = ref<any[]>([])

// 评测历史
const evaluationHistory = ref([
  {
    id: 1,
    question: '什么是大语言模型？',
    modelName: 'GPT-4',
    score: 8.5,
    method: 'human',
    evaluationTime: '2023-05-15 14:30:22'
  },
  {
    id: 2,
    question: '解释一下量子计算的基本原理',
    modelName: 'Claude 2',
    score: 7.8,
    method: 'judge_model',
    evaluationTime: '2023-05-14 10:15:45'
  },
  {
    id: 3,
    question: '编写一个快速排序算法',
    modelName: 'LLaMA 2',
    score: 9.2,
    method: 'auto',
    evaluationTime: '2023-05-16 09:22:10'
  }
])

// 监听回答ID变化
watch(() => formData.answerId, async (newVal) => {
  if (newVal) {
    await fetchAnswerDetail(newVal)
  } else {
    resetKeyPoints()
  }
})

// 初始化
onMounted(() => {
  // 检查URL参数中是否有answerId
  const route = useRoute();
  const answerId = route.query.answerId;
  
  if (answerId) {
    formData.answerId = Number(answerId);
    fetchAnswerDetail(Number(answerId));
  }
})

// 搜索回答
const searchAnswers = async (query: string) => {
  if (query.length < 2) return
  
  loading.value.answers = true
  try {
    // 实际项目中应该调用API获取数据
    // const res = await searchModelAnswers(query)
    // answerOptions.value = res.data
    
    // 模拟数据
    setTimeout(() => {
      answerOptions.value = [
        { answerId: 1, modelName: 'GPT-4', question: '什么是大语言模型？', questionType: 'subjective' },
        { answerId: 2, modelName: 'Claude 2', question: '解释一下量子计算的基本原理', questionType: 'subjective' },
        { answerId: 3, modelName: 'LLaMA 2', question: '编写一个快速排序算法', questionType: 'subjective' },
        { answerId: 4, modelName: 'Mistral 7B', question: 'Python和Java的主要区别是什么？', questionType: 'subjective' },
        { answerId: 5, modelName: 'Baichuan 2', question: '以下哪个不是JavaScript框架？', questionType: 'objective' }
      ].filter(item => 
        item.question.includes(query) || 
        item.modelName.includes(query)
      )
      loading.value.answers = false
    }, 500)
  } catch (error) {
    console.error('搜索回答失败', error)
    loading.value.answers = false
  }
}

// 获取回答详情
const fetchAnswerDetail = async (answerId: number) => {
  try {
    // 实际项目中应该调用API获取数据
    // const res = await getAnswerDetail(answerId)
    // const detail = res.data
    
    // 模拟数据
    const answer = answerOptions.value.find(item => item.answerId === answerId)
    if (answer) {
      selectedAnswerType.value = answer.questionType
      
      // 如果是主观题，模拟加载关键点
      if (answer.questionType === 'subjective') {
        keyPoints.value = [
          { keyPointId: 1, pointText: '定义准确（基于深度学习的NLP模型）', pointWeight: 1.0, pointType: 'required' },
          { keyPointId: 2, pointText: '提到海量文本数据训练', pointWeight: 1.0, pointType: 'required' },
          { keyPointId: 3, pointText: '提到Transformer架构', pointWeight: 0.8, pointType: 'bonus' },
          { keyPointId: 4, pointText: '提到模型规模（参数量）', pointWeight: 0.8, pointType: 'bonus' },
          { keyPointId: 5, pointText: '列举应用场景', pointWeight: 1.0, pointType: 'required' }
        ]
        // 初始化关键点状态
        formData.keyPointsStatus = keyPoints.value.map(() => 'missed')
      } else {
        resetKeyPoints()
      }
    }
  } catch (error) {
    console.error('获取回答详情失败', error)
  }
}

// 重置关键点
const resetKeyPoints = () => {
  keyPoints.value = []
  formData.keyPointsStatus = []
}

// 提交表单
const handleSubmit = async () => {
  // 表单验证
  if (!formData.answerId) {
    ElMessage.warning('请选择模型回答')
    return
  }
  
  if (selectedAnswerType.value === 'subjective' && formData.method !== 'auto') {
    if (formData.score === 0) {
      ElMessage.warning('请给出评分')
      return
    }
    
    if (keyPoints.value.length > 0) {
      const allEvaluated = formData.keyPointsStatus.every(status => status !== '')
      if (!allEvaluated) {
        ElMessage.warning('请评估所有关键点')
        return
      }
    }
    
    if (formData.dimensions.accuracy === 0 || 
        formData.dimensions.completeness === 0 || 
        formData.dimensions.clarity === 0) {
      ElMessage.warning('请完成所有评测维度')
      return
    }
  }
  
  loading.value.submit = true
  try {
    // 实际项目中应该调用API提交数据
    // const res = await submitManualEvaluation(formData)
    // if (res.success) {
    //   ElMessage.success('评测结果提交成功')
    //   resetForm()
    // } else {
    //   ElMessage.error(res.message || '提交失败')
    // }
    
    // 模拟成功响应
    setTimeout(() => {
      ElMessage.success('评测结果提交成功')
      resetForm()
      loading.value.submit = false
    }, 1000)
  } catch (error) {
    console.error('提交评测结果失败', error)
    ElMessage.error('提交失败，请重试')
    loading.value.submit = false
  }
}

// 重置表单
const resetForm = () => {
  formData.answerId = null
  formData.method = 'human'
  formData.score = 0
  formData.isCorrect = false
  formData.dimensions.accuracy = 0
  formData.dimensions.completeness = 0
  formData.dimensions.clarity = 0
  formData.keyPointsStatus = []
  formData.comments = ''
  resetKeyPoints()
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

// 查看详情
const viewDetail = (row: any) => {
  ElMessage.info(`查看ID为 ${row.id} 的详情`)
  // 实际项目中应该跳转到详情页面或弹出详情对话框
}
</script>

<style scoped>
.import-container {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.import-card {
  margin-bottom: 20px;
}

.history-card {
  margin-top: 30px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
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

.key-point-evaluation {
  margin-bottom: 15px;
  padding: 10px;
  border-radius: 4px;
  background-color: #f5f7fa;
}
</style> 