<template>
  <div class="answer-edit-container">
    <div class="page-header">
      <h2>编辑标准答案</h2>
      <el-button @click="navigateBack">返回</el-button>
    </div>

    <el-card shadow="never" class="form-card" v-loading="loading">
      <el-form :model="answerForm" :rules="rules" ref="answerFormRef" label-width="100px">
        <el-form-item label="问题">
          <div class="question-info">{{ questionInfo }}</div>
        </el-form-item>

        <el-form-item label="答案内容" prop="answer">
          <el-input 
            v-model="answerForm.answer" 
            type="textarea" 
            :rows="8" 
            placeholder="请输入标准答案内容">
          </el-input>
        </el-form-item>

        <el-form-item label="答案来源" prop="sourceType">
          <el-select v-model="answerForm.sourceType" placeholder="请选择答案来源">
            <el-option label="手动创建" value="manual"></el-option>
            <el-option label="原始回答" value="raw"></el-option>
            <el-option label="众包答案" value="crowdsourced"></el-option>
            <el-option label="专家答案" value="expert"></el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="选择原因" prop="selectionReason">
          <el-input 
            v-model="answerForm.selectionReason" 
            type="textarea" 
            :rows="3" 
            placeholder="请输入选择该答案作为标准答案的原因">
          </el-input>
        </el-form-item>

        <el-form-item label="是否最终版本">
          <el-switch v-model="answerForm.isFinal"></el-switch>
        </el-form-item>

        <el-divider content-position="left">关键点设置</el-divider>

        <div v-for="(point, index) in answerForm.keyPoints" :key="index" class="key-point-item">
          <el-divider content-position="left">关键点 #{{ index + 1 }}</el-divider>
          
          <el-form-item :label="'关键点内容'" :prop="`keyPoints.${index}.pointText`">
            <el-input 
              v-model="point.pointText" 
              type="textarea" 
              :rows="2" 
              placeholder="请输入关键点描述">
            </el-input>
          </el-form-item>

          <el-row :gutter="20">
            <el-col :span="8">
              <el-form-item :label="'类型'" :prop="`keyPoints.${index}.pointType`">
                <el-select v-model="point.pointType" placeholder="请选择类型">
                  <el-option label="必须" value="required"></el-option>
                  <el-option label="加分" value="bonus"></el-option>
                  <el-option label="减分" value="penalty"></el-option>
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item :label="'权重'" :prop="`keyPoints.${index}.pointWeight`">
                <el-input-number 
                  v-model="point.pointWeight" 
                  :precision="1" 
                  :step="0.1" 
                  :min="0.1" 
                  :max="10">
                </el-input-number>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item :label="'顺序'" :prop="`keyPoints.${index}.pointOrder`">
                <el-input-number 
                  v-model="point.pointOrder" 
                  :precision="0" 
                  :step="1" 
                  :min="1">
                </el-input-number>
              </el-form-item>
            </el-col>
          </el-row>

          <el-form-item :label="'示例'" :prop="`keyPoints.${index}.exampleText`">
            <el-input 
              v-model="point.exampleText" 
              type="textarea" 
              :rows="2" 
              placeholder="请输入示例内容">
            </el-input>
          </el-form-item>

          <el-button 
            type="danger" 
            size="small" 
            @click="removeKeyPoint(index)" 
            class="remove-point-btn">
            删除此关键点
          </el-button>
        </div>

        <div class="add-key-point">
          <el-button type="primary" plain @click="addKeyPoint">
            <el-icon><Plus /></el-icon>添加关键点
          </el-button>
        </div>

        <el-form-item>
          <el-button type="primary" @click="submitForm">保存</el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getAnswerById, updateAnswer, getAnswerKeyPoints } from '@/api/answer'
import type { AnswerKeyPoint } from '@/api/answer'
import { getQuestionById } from '@/api/question'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const answerFormRef = ref()
const questionData = ref<any>({})

// 表单数据
const answerForm = reactive({
  standardAnswerId: Number(route.params.id) || 0,
  standardQuestionId: 0,
  answer: '',
  sourceType: 'manual' as 'manual' | 'raw' | 'crowdsourced' | 'expert',
  sourceAnswerId: undefined,
  sourceId: undefined,
  selectionReason: '',
  selectedBy: undefined,
  isFinal: false,
  keyPoints: [] as AnswerKeyPoint[]
})

// 问题信息
const questionInfo = computed(() => {
  if (!questionData.value) return '加载中...'
  if (!questionData.value.question) {
    if (answerForm.standardQuestionId) {
      return `问题ID: ${answerForm.standardQuestionId} (无法获取问题内容)`
    }
    return '未找到关联问题'
  }
  return `${questionData.value.standardQuestionId || answerForm.standardQuestionId}: ${questionData.value.question}`
})

// 表单验证规则
const rules = {
  answer: [
    { required: true, message: '请输入答案内容', trigger: 'blur' }
  ],
  sourceType: [
    { required: true, message: '请选择答案来源', trigger: 'change' }
  ]
}

// 获取答案详情
const getAnswerDetail = async () => {
  loading.value = true
  try {
    const response = await getAnswerById(answerForm.standardAnswerId)
    console.log('获取答案详情原始响应:', response)
    
    // 处理响应数据
    let answerData = null
    if (response && typeof response === 'object') {
      // 处理标准返回格式：{ code: 200, data: {...}, message: "Success" }
      if ('code' in response && response.code === 200 && response.data) {
        answerData = response.data
      } else if ('data' in response) {
        answerData = response.data
      } else {
        // 如果响应本身就是答案对象
        answerData = response
      }
    }
    
    console.log('处理后的答案数据:', answerData)
    
    if (answerData) {
      // 更新表单数据
      answerForm.standardAnswerId = answerData.standardAnswerId || answerForm.standardAnswerId
      answerForm.answer = answerData.answer || ''
      
      // 处理sourceType，确保类型正确
      if (answerData.sourceType && ['manual', 'raw', 'crowdsourced', 'expert'].includes(answerData.sourceType)) {
        answerForm.sourceType = answerData.sourceType as 'manual' | 'raw' | 'crowdsourced' | 'expert'
      } else {
        answerForm.sourceType = 'manual'
      }
      
      answerForm.sourceAnswerId = answerData.sourceAnswerId
      answerForm.sourceId = answerData.sourceId
      answerForm.selectionReason = answerData.selectionReason || ''
      answerForm.selectedBy = answerData.selectedBy
      answerForm.isFinal = answerData.isFinal || false
      
      // 获取问题ID - 检查所有可能的数据结构
      console.log('尝试获取问题ID...')
      if (answerData.standardQuestionId) {
        console.log('从standardQuestionId字段获取问题ID:', answerData.standardQuestionId)
        answerForm.standardQuestionId = answerData.standardQuestionId
      } else if (answerData.standardQuestion && answerData.standardQuestion.standardQuestionId) {
        console.log('从standardQuestion对象获取问题ID:', answerData.standardQuestion.standardQuestionId)
        answerForm.standardQuestionId = answerData.standardQuestion.standardQuestionId
      } else if (answerData.questionId) {
        console.log('从questionId字段获取问题ID:', answerData.questionId)
        answerForm.standardQuestionId = answerData.questionId
      }
      
      console.log('最终获取到的问题ID:', answerForm.standardQuestionId)
      
      // 获取关联问题
      if (answerForm.standardQuestionId) {
        await getQuestionDetail(answerForm.standardQuestionId)
      } else {
        console.error('无法获取问题ID，无法加载问题详情')
        ElMessage.warning('无法获取关联问题信息')
      }
      
      // 获取关键点
      await getKeyPoints()
    } else {
      ElMessage.error('获取答案详情失败')
    }
  } catch (error) {
    console.error('获取答案详情失败', error)
    ElMessage.error('获取答案详情失败')
  } finally {
    loading.value = false
  }
}

// 获取问题详情
const getQuestionDetail = async (questionId: number) => {
  try {
    console.log('开始获取问题详情，问题ID:', questionId)
    const response = await getQuestionById(questionId)
    console.log('获取问题详情原始响应:', response)
    
    // 处理响应数据
    if (response && typeof response === 'object') {
      // 处理标准返回格式：{ code: 200, data: {...}, message: "Success" }
      if ('code' in response && response.code === 200 && response.data) {
        questionData.value = response.data
      } else if ('data' in response) {
        questionData.value = response.data
      } else {
        // 如果响应本身就是问题对象
        questionData.value = response
      }
      
      console.log('处理后的问题数据:', questionData.value)
      
      // 验证是否有问题内容
      if (!questionData.value.question) {
        console.error('问题数据不包含问题内容')
        ElMessage.warning('获取问题内容失败')
      }
    } else {
      console.error('获取问题详情失败，响应数据无效')
      questionData.value = {}
    }
  } catch (error) {
    console.error('获取问题详情失败', error)
    questionData.value = {}
    ElMessage.error('获取问题详情失败')
  }
}

// 获取关键点
const getKeyPoints = async () => {
  try {
    const response = await getAnswerKeyPoints(answerForm.standardAnswerId)
    
    // 处理响应数据
    if (Array.isArray(response)) {
      answerForm.keyPoints = response
    } else if (response && typeof response === 'object') {
      if (Array.isArray(response.data)) {
        answerForm.keyPoints = response.data
      } else if (response.data && Array.isArray(response.data.content)) {
        answerForm.keyPoints = response.data.content
      } else {
        answerForm.keyPoints = []
      }
    } else {
      answerForm.keyPoints = []
    }
  } catch (error) {
    console.error('获取关键点失败', error)
    answerForm.keyPoints = []
  }
}

// 添加关键点
const addKeyPoint = () => {
  answerForm.keyPoints.push({
    pointText: '',
    pointOrder: answerForm.keyPoints.length + 1,
    pointWeight: 1.0,
    pointType: 'required',
    exampleText: ''
  })
}

// 移除关键点
const removeKeyPoint = (index: number) => {
  answerForm.keyPoints.splice(index, 1)
  // 更新顺序
  answerForm.keyPoints.forEach((point, idx) => {
    point.pointOrder = idx + 1
  })
}

// 提交表单
const submitForm = async () => {
  if (!answerFormRef.value) return
  
  await answerFormRef.value.validate(async (valid: boolean) => {
    if (!valid) return
    
    loading.value = true
    try {
      await updateAnswer(answerForm.standardAnswerId, answerForm)
      ElMessage.success('更新成功')
      // 返回到问题详情页面
      if (answerForm.standardQuestionId) {
        router.push(`/questions/detail/${answerForm.standardQuestionId}`)
      } else {
        router.push('/answers')
      }
    } catch (error) {
      console.error('更新答案失败', error)
    } finally {
      loading.value = false
    }
  })
}

// 重置表单
const resetForm = () => {
  getAnswerDetail()
}

// 返回上一页
const navigateBack = () => {
  router.back()
}

onMounted(() => {
  getAnswerDetail()
})
</script>

<style scoped>
.answer-edit-container {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.form-card {
  margin-bottom: 20px;
}

.question-info {
  font-size: 14px;
  line-height: 1.5;
  padding: 8px 12px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.key-point-item {
  position: relative;
  padding: 10px;
  margin-bottom: 20px;
  border: 1px dashed #dcdfe6;
  border-radius: 4px;
}

.remove-point-btn {
  position: absolute;
  top: 10px;
  right: 10px;
}

.add-key-point {
  margin: 20px 0;
  display: flex;
  justify-content: center;
}
</style> 