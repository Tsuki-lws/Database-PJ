<template>
  <div class="answer-collection-container">
    <div class="page-header">
      <h2>参与众包回答</h2>
      <el-button @click="goBack">返回</el-button>
    </div>

    <el-card v-loading="loading" shadow="never" class="collection-card">
      <template #header>
        <div class="card-header">
          <span>{{ task.title || '加载中...' }}</span>
          <el-tag :type="getStatusTag(task.status)">{{ formatStatus(task.status) }}</el-tag>
        </div>
      </template>

      <div v-if="!task.taskId" class="task-error">
        <el-empty description="无法加载任务信息，请检查任务ID是否正确"></el-empty>
        <div class="error-actions">
          <el-button type="primary" @click="getTaskDetails">重试</el-button>
          <el-button @click="goBack">返回</el-button>
        </div>
      </div>

      <div v-else-if="task.status !== 'PUBLISHED'" class="task-closed">
        <el-empty :description="`当前任务${formatStatus(task.status)}，不接受新的回答`"></el-empty>
      </div>

      <div v-else>
        <div class="task-description" v-if="task.description">
          <h3>任务描述</h3>
          <p>{{ task.description }}</p>
        </div>

        <el-divider></el-divider>

        <h3>标准问题</h3>
        <el-card v-if="standardQuestion" shadow="never" class="question-card">
          <div class="question-content">
            {{ standardQuestion.question }}
          </div>
        </el-card>
        <el-empty v-else description="无法加载标准问题"></el-empty>

        <el-divider></el-divider>

        <h3>提交回答</h3>
        <el-form :model="answerForm" :rules="rules" ref="answerFormRef" label-width="100px">
          <el-form-item label="回答者姓名" prop="contributorName">
            <el-input v-model="answerForm.contributorName" placeholder="请输入您的姓名"></el-input>
          </el-form-item>
          
          <el-form-item label="回答者邮箱" prop="contributorEmail">
            <el-input v-model="answerForm.contributorEmail" placeholder="请输入您的邮箱"></el-input>
          </el-form-item>
          
          <el-form-item label="职业/身份" prop="occupation">
            <el-select v-model="answerForm.occupation" placeholder="请选择您的职业/身份">
              <el-option label="学生" value="学生"></el-option>
              <el-option label="教师" value="教师"></el-option>
              <el-option label="研究人员" value="研究人员"></el-option>
              <el-option label="工程师" value="工程师"></el-option>
              <el-option label="其他" value="其他"></el-option>
            </el-select>
          </el-form-item>
          
          <el-form-item label="专业领域" prop="expertise">
            <el-select v-model="answerForm.expertise" placeholder="请选择您的专业领域" multiple>
              <el-option label="计算机科学" value="计算机科学"></el-option>
              <el-option label="人工智能" value="人工智能"></el-option>
              <el-option label="自然语言处理" value="自然语言处理"></el-option>
              <el-option label="机器学习" value="机器学习"></el-option>
              <el-option label="数据科学" value="数据科学"></el-option>
              <el-option label="其他" value="其他"></el-option>
            </el-select>
          </el-form-item>
          
          <el-form-item label="您的回答" prop="answerContent">
            <el-input 
              v-model="answerForm.answerContent" 
              type="textarea" 
              :rows="8" 
              placeholder="请在此输入您的回答">
            </el-input>
          </el-form-item>
          
          <el-form-item label="参考资料" prop="references">
            <el-input 
              v-model="answerForm.references" 
              type="textarea" 
              :rows="3" 
              placeholder="如有参考资料，请在此列出（可选）">
            </el-input>
          </el-form-item>
          
          <el-form-item>
            <el-button type="primary" @click="submitAnswer">提交回答</el-button>
            <el-button @click="resetForm">重置</el-button>
          </el-form-item>
        </el-form>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getTaskDetail, submitAnswer as submitCrowdAnswer, getTaskQuestions } from '@/api/crowdsourcing'
import { getQuestionById } from '@/api/question'

const router = useRouter()
const route = useRoute()
const loading = ref(true)

// 使用computed属性确保任务ID为有效的数字
const taskId = computed(() => {
  const id = route.params.id
  if (typeof id === 'string') {
    const numId = parseInt(id, 10)
    if (!isNaN(numId)) {
      console.log('解析任务ID成功:', numId)
      return numId
    }
  }
  console.error('无效的任务ID参数:', id)
  return null
})

const task = ref<any>({})
const standardQuestion = ref<any>(null)
const answerFormRef = ref()

// 回答表单
const answerForm = reactive({
  contributorName: '',
  contributorEmail: '',
  occupation: '',
  expertise: [],
  answerContent: '',
  references: ''
})

// 表单验证规则
const rules = {
  contributorName: [
    { required: true, message: '请输入回答者姓名', trigger: 'blur' }
  ],
  contributorEmail: [
    { required: true, message: '请输入回答者邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  occupation: [
    { required: true, message: '请选择您的职业/身份', trigger: 'change' }
  ],
  expertise: [
    { required: true, message: '请选择您的专业领域', trigger: 'change' }
  ],
  answerContent: [
    { required: true, message: '请输入回答内容', trigger: 'blur' }
  ]
}

// 获取任务详情
const getTaskDetails = async () => {
  loading.value = true
  try {
    // 检查任务ID是否有效
    if (!taskId.value) {
      console.error('任务ID无效，无法获取任务详情')
      ElMessage.error('无效的任务ID，请检查URL')
      return
    }
    
    console.log('开始获取任务详情，任务ID:', taskId.value)
    const res = await getTaskDetail(taskId.value)
    console.log('获取任务详情响应:', res)
    
    // 适应后端返回结构变化，直接获取任务对象
    if (res && res.data) {
      task.value = res.data
      console.log('任务详情数据:', task.value)
      
      // 获取任务关联的标准问题
      try {
        const questionsRes = await getTaskQuestions(taskId.value)
        console.log('获取任务关联问题响应:', questionsRes)
        
        // 适应后端返回结构变化，直接获取问题列表
        if (questionsRes && questionsRes.data && Array.isArray(questionsRes.data) && questionsRes.data.length > 0) {
          // 使用第一个关联的标准问题
          const standardQuestionId = questionsRes.data[0].standardQuestionId
          console.log('找到关联的标准问题ID:', standardQuestionId)
          await getStandardQuestion(standardQuestionId)
        } else {
          console.error('任务没有关联的标准问题')
          ElMessage.warning('该任务没有关联的标准问题')
        }
      } catch (error) {
        console.error('获取任务关联问题失败:', error)
        ElMessage.warning('获取任务关联问题失败')
      }
    } else {
      console.error('任务详情数据为空')
      ElMessage.error('获取任务详情失败：数据为空')
    }
  } catch (error) {
    console.error('获取任务详情失败:', error)
    ElMessage.error('获取任务详情失败')
  } finally {
    loading.value = false
  }
}

// 获取标准问题
const getStandardQuestion = async (questionId: number) => {
  try {
    const res = await getQuestionById(questionId)
    // 适应后端返回结构变化，直接获取问题对象
    if (res && res.data) {
      standardQuestion.value = res.data
      console.log('获取到标准问题:', standardQuestion.value)
    }
  } catch (error) {
    console.error('获取标准问题失败', error)
  }
}

// 格式化状态
const formatStatus = (status: string) => {
  if (!status) {
    console.warn('任务状态为空，使用默认值"未知状态"')
    return '未知状态'
  }
  
  const map: Record<string, string> = {
    'DRAFT': '草稿',
    'PUBLISHED': '已发布',
    'COMPLETED': '已完成',
    'CLOSED': '已关闭'
  }
  return map[status] || status
}

// 获取状态标签类型
const getStatusTag = (status: string) => {
  if (!status) {
    console.warn('任务状态为空，使用默认标签类型"info"')
    return 'info'
  }
  
  const map: Record<string, string> = {
    'DRAFT': 'info',
    'PUBLISHED': 'primary',
    'COMPLETED': 'success',
    'CLOSED': 'danger'
  }
  return map[status] || ''
}

// 提交回答
const submitAnswer = async () => {
  if (!answerFormRef.value) return
  
  await answerFormRef.value.validate(async (valid: boolean) => {
    if (!valid) {
      return false
    }
    
    // 检查任务ID是否有效
    if (!taskId.value) {
      ElMessage.error('无效的任务ID，无法提交回答')
      return false
    }
    
    loading.value = true
    try {
      // 检查任务状态
      if (task.value.status !== 'PUBLISHED') {
        ElMessage.error(`当前任务${formatStatus(task.value.status)}，不接受新的回答`)
        return false
      }
      
      // 检查标准问题ID
      if (!standardQuestion.value || !standardQuestion.value.standardQuestionId) {
        console.error('没有找到关联的标准问题ID')
        ElMessage.error('无法提交回答：没有找到关联的标准问题')
        return false
      }
      
      // 准备提交数据，包括标准问题ID
      const submitData = {
        taskId: taskId.value,
        standardQuestionId: standardQuestion.value.standardQuestionId,
        contributorName: answerForm.contributorName,
        contributorEmail: answerForm.contributorEmail,
        occupation: answerForm.occupation,
        expertise: answerForm.expertise.join(','),
        answerText: answerForm.answerContent,
        references: answerForm.references
      }
      
      console.log('准备提交回答数据:', submitData)
      
      // 先检查连接是否正常
      try {
        const checkResponse = await fetch('/api/health', { method: 'GET' })
          .catch(error => {
            console.error('健康检查请求失败:', error)
          })
        
        if (checkResponse && checkResponse.ok) {
          console.log('API服务健康状态正常')
        }
      } catch (error) {
        console.warn('健康检查失败，但仍继续尝试提交答案')
      }
      
      try {
        const response = await submitCrowdAnswer(taskId.value, submitData)
        console.log('提交回答响应:', response)
        
        // 根据后端返回结构调整成功处理逻辑
        if (response && response.data) {
          ElMessage.success('回答提交成功，感谢您的参与！')
          resetForm()
        } else {
          ElMessage.error('提交失败：服务器返回异常')
        }
      } catch (error: any) {
        console.error('提交回答失败:', error)
        
        // 显示详细错误信息
        if (error.response && error.response.data) {
          const errorData = error.response.data
          ElMessage.error(`提交失败: ${errorData.message || errorData.error || JSON.stringify(errorData)}`)
        } else if (error.message) {
          ElMessage.error(`提交失败: ${error.message}`)
        } else {
          ElMessage.error('提交失败，请重试')
        }
      } finally {
        loading.value = false
      }
    } catch (error: any) {
      console.error('提交回答失败:', error)
      
      // 显示详细错误信息
      if (error.response && error.response.data) {
        const errorData = error.response.data
        ElMessage.error(`提交失败: ${errorData.message || errorData.error || '未知错误'}`)
      } else if (error.message) {
        ElMessage.error(`提交失败: ${error.message}`)
      } else {
        ElMessage.error('提交失败，请重试')
      }
    }
  })
}

// 重置表单
const resetForm = () => {
  if (answerFormRef.value) {
    answerFormRef.value.resetFields()
  }
}

// 返回上一页
const goBack = () => {
  router.back()
}

onMounted(() => {
  // 打印路由参数信息，帮助调试
  console.log('路由参数:', route.params)
  console.log('任务ID参数类型:', typeof route.params.id)
  console.log('任务ID参数值:', route.params.id)
  console.log('解析后的任务ID:', taskId.value)
  
  // 只有在任务ID有效时才获取任务详情
  if (taskId.value) {
    getTaskDetails()
  } else {
    loading.value = false
    ElMessage.error('无效的任务ID参数，请检查URL')
  }
})
</script>

<style scoped>
.answer-collection-container {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.collection-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.task-description {
  margin-bottom: 20px;
}

.task-description p {
  white-space: pre-wrap;
  color: #606266;
}

.question-card {
  margin-bottom: 20px;
}

.question-content {
  white-space: pre-wrap;
  padding: 10px;
  background-color: #f8f9fa;
  border-radius: 4px;
  border-left: 4px solid #409EFF;
}

.task-closed {
  padding: 40px 0;
}

.task-error {
  padding: 40px 0;
  text-align: center;
}

.error-actions {
  margin-top: 20px;
  display: flex;
  justify-content: center;
  gap: 10px;
}
</style> 