<template>
  <div class="question-edit">
    <div class="header">
      <h2>编辑问题</h2>
    </div>

    <el-form
      ref="formRef"
      :model="formData"
      :rules="rules"
      label-width="100px"
      v-loading="loading"
      class="question-form">
      <el-form-item label="问题内容" prop="question">
        <el-input
          v-model="formData.question"
          type="textarea"
          :rows="4"
          placeholder="请输入问题内容" />
      </el-form-item>

      <el-form-item label="问题类型" prop="questionType">
        <el-select v-model="formData.questionType" placeholder="请选择问题类型">
          <el-option 
            v-for="item in typeOptions" 
            :key="item.value" 
            :label="item.label" 
            :value="item.value" />
        </el-select>
      </el-form-item>

      <el-form-item label="难度等级" prop="difficulty">
        <el-select v-model="formData.difficulty" placeholder="请选择难度等级">
          <el-option 
            v-for="item in difficultyOptions" 
            :key="item.value" 
            :label="item.label" 
            :value="item.value" />
        </el-select>
      </el-form-item>

      <el-form-item label="分类" prop="categoryId">
        <el-select v-model="formData.categoryId" placeholder="请选择分类" clearable>
          <el-option 
            v-for="item in categoryOptions" 
            :key="item.value" 
            :label="item.label" 
            :value="item.value" />
        </el-select>
      </el-form-item>

      <el-form-item label="状态" prop="status">
        <el-select v-model="formData.status" placeholder="请选择状态">
          <el-option 
            v-for="item in statusOptions" 
            :key="item.value" 
            :label="item.label" 
            :value="item.value" />
        </el-select>
      </el-form-item>

      <el-form-item>
        <el-button type="primary" @click="submitForm(formRef)" :loading="submitting">保存</el-button>
        <el-button @click="handleCancel">取消</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { getStandardQuestion, updateStandardQuestion } from '@/api/standardQuestions'
import { getAllCategories } from '@/api/category'

const route = useRoute()
const router = useRouter()
const formRef = ref<FormInstance>()
const loading = ref(false)
const submitting = ref(false)
const categoryOptions = ref<any[]>([])
const isEdit = ref(true)

const formData = reactive({
  standardQuestionId: 0,
  question: '',
  categoryId: null as number | null,
  questionType: '',
  difficulty: '',
  status: 'draft',
  createdAt: '',
  updatedAt: '',
  version: 0
})

const rules = reactive<FormRules>({
  question: [{ required: true, message: '请输入问题内容', trigger: 'blur' }],
  questionType: [{ required: true, message: '请选择问题类型', trigger: 'change' }],
  difficulty: [{ required: true, message: '请选择难度级别', trigger: 'change' }]
})

// 问题类型选项
const typeOptions = [
  { value: 'single_choice', label: '单选题' },
  { value: 'multiple_choice', label: '多选题' },
  { value: 'simple_fact', label: '简单事实题' },
  { value: 'subjective', label: '主观题' }
]

// 难度选项
const difficultyOptions = [
  { value: 'easy', label: '简单' },
  { value: 'medium', label: '中等' },
  { value: 'hard', label: '困难' }
]

// 状态选项
const statusOptions = [
  { value: 'draft', label: '草稿' },
  { value: 'pending_review', label: '待审核' },
  { value: 'approved', label: '已通过' },
  { value: 'rejected', label: '已拒绝' }
]

// 获取问题详情
const getQuestionDetail = async () => {
  const id = Number(route.params.id)
  if (!id) return
  
  loading.value = true
  try {
    const response = await getStandardQuestion(id)
    console.log('获取问题详情原始响应:', response)
    
    // 处理响应数据
    let questionData
    if (response && typeof response === 'object') {
      // 处理标准返回格式：{ code: 200, data: {...}, message: "Success" }
      if (response.code === 200 && response.data) {
        questionData = response.data
      } else {
        // 如果响应本身就是问题对象
        questionData = response
      }
    }
    
    if (questionData) {
      console.log('问题详情数据:', questionData)
      
      // 更新表单数据
      formData.standardQuestionId = questionData.standardQuestionId || id
      formData.question = questionData.question || ''
      formData.questionType = questionData.questionType || ''
      formData.difficulty = questionData.difficulty || ''
      formData.status = questionData.status || 'draft'
      formData.version = questionData.version || 0
      formData.createdAt = questionData.createdAt || ''
      formData.updatedAt = questionData.updatedAt || ''
      
      // 如果有分类信息，设置categoryId
      if (questionData.category) {
        formData.categoryId = questionData.category.categoryId
      } else if (questionData.categoryId) {
        formData.categoryId = questionData.categoryId
      } else {
        formData.categoryId = null
      }
    } else {
      ElMessage.error('获取问题详情失败')
    }
  } catch (error) {
    console.error('获取问题详情失败', error)
    ElMessage.error('获取问题详情失败')
  } finally {
    loading.value = false
  }
}

// 获取分类选项
const getCategories = async () => {
  try {
    const res = await getAllCategories()
    console.log('获取分类列表响应:', res)
    
    // 处理响应数据
    let categoriesData
    if (res && typeof res === 'object') {
      if (res.code === 200 && res.data) {
        categoriesData = res.data
      } else if (Array.isArray(res)) {
        categoriesData = res
      }
    }
    
    if (categoriesData && Array.isArray(categoriesData)) {
      categoryOptions.value = categoriesData.map((item: any) => ({
        value: item.categoryId,
        label: item.name || item.categoryName
      }))
    } else {
      console.warn('获取分类列表返回格式不符合预期')
    }
  } catch (error) {
    console.error('获取分类列表失败', error)
  }
}

// 提交表单
const submitForm = async (formEl: FormInstance | undefined) => {
  if (!formEl) return
  
  await formEl.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        console.log('提交的表单数据:', formData)
        
        // 准备提交的数据
        const submitData = {
          standardQuestionId: formData.standardQuestionId,
          question: formData.question,
          questionType: formData.questionType,
          difficulty: formData.difficulty,
          status: formData.status,
          categoryId: formData.categoryId,
          version: formData.version
        }
        
        const response = await updateStandardQuestion(formData.standardQuestionId, submitData)
        console.log('更新问题响应:', response)
        
        ElMessage.success('更新成功')
        router.push(`/questions/detail/${formData.standardQuestionId}`)
      } catch (error) {
        console.error('更新问题失败', error)
        ElMessage.error('更新失败，请重试')
      } finally {
        submitting.value = false
      }
    }
  })
}

// 返回
const handleCancel = () => {
  router.back()
}

onMounted(() => {
  getQuestionDetail()
  getCategories()
})
</script>

<style scoped>
.question-edit {
  padding: 20px;
}

.header {
  margin-bottom: 20px;
}

.question-form {
  max-width: 800px;
}
</style> 