<template>
  <div class="question-edit">
    <div class="header">
      <h2>创建问题</h2>
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
        <el-autocomplete
          v-model="categoryInput"
          :fetch-suggestions="queryCategories"
          placeholder="请输入或选择分类"
          clearable
          @select="handleCategorySelect"
          style="width: 100%">
          <template #default="{ item }">
            <div>{{ item.label }}</div>
          </template>
        </el-autocomplete>
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
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { createQuestion } from '@/api/question'
import { getAllCategories } from '@/api/category'
import type { StandardQuestion, Category } from '@/api/question'

const router = useRouter()
const formRef = ref<FormInstance>()
const loading = ref(false)
const submitting = ref(false)
const categoryOptions = ref<any[]>([])
const categoryInput = ref('')

const formData = reactive({
  question: '',
  categoryId: null as number | null,
  questionType: 'subjective',
  difficulty: 'medium',
  status: 'draft'
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
  // { value: 'simple_fact', label: '简单事实题' },
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

// 获取分类选项
const getCategories = async () => {
  try {
    const res = await getAllCategories()
    console.log('获取分类列表响应:', res)
    
    // 处理响应数据
    let categoriesData
    if (res && typeof res === 'object') {
      if ('code' in res && res.code === 200 && res.data) {
        categoriesData = res.data
      } else if ('data' in res) {
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
      
      // 如果已有选择的分类ID，设置对应的分类名称
      if (formData.categoryId) {
        const selectedCategory = categoryOptions.value.find(item => item.value === formData.categoryId)
        if (selectedCategory) {
          categoryInput.value = selectedCategory.label
        }
      }
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
        // 准备提交的数据 - 包含分类信息
        const submitData: StandardQuestion = {
          question: formData.question,
          questionType: formData.questionType as 'single_choice' | 'multiple_choice' | 'simple_fact' | 'subjective',
          difficulty: formData.difficulty as 'easy' | 'medium' | 'hard',
          status: formData.status as 'draft' | 'pending_review' | 'approved' | 'rejected'
        }
        
        // 处理分类信息
        if (categoryInput.value.trim() !== '') {
          // 检查是否已存在同名分类
          const existingCategory = categoryOptions.value.find(item => 
            item.label.toLowerCase() === categoryInput.value.toLowerCase()
          )
          
          if (existingCategory) {
            // 使用已存在的分类ID
            submitData.categoryId = existingCategory.value
            // 清除可能存在的category对象，避免冲突
            submitData.category = undefined
          } else {
            // 使用新分类名称
            submitData.category = {
              name: categoryInput.value.trim(),
              description: "自动创建的分类"
            }
            // 清除categoryId，避免冲突
            submitData.categoryId = undefined
          }
        }
        // 如果没有分类，不设置categoryId和category字段
        
        console.log('提交的表单数据:', submitData)
        
        // 创建问题
        const response = await createQuestion(submitData)
        console.log('创建问题响应:', response)
        
        // 获取创建的问题ID
        let questionId = null
        if (response && typeof response === 'object') {
          if ('standardQuestionId' in response) {
            questionId = response.standardQuestionId
          } else if ('data' in response && response.data && 'standardQuestionId' in response.data) {
            questionId = response.data.standardQuestionId
          }
        }
        
        if (questionId) {
          ElMessage.success('创建成功')
          router.push(`/questions/detail/${questionId}`)
        } else {
          ElMessage.success('创建成功，但无法获取问题ID')
          router.push('/questions')
        }
      } catch (error: any) {
        console.error('创建失败:', error)
        // 增强错误显示
        if (error.response && error.response.data) {
          console.error('服务器返回错误:', error.response.data)
          if (error.response.data.message) {
            ElMessage.error(`创建失败: ${error.response.data.message}`)
          } else {
            ElMessage.error('创建失败，请重试')
          }
        } else {
          ElMessage.error('创建失败，请重试')
        }
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

const queryCategories = (query: string, cb: (options: any[]) => void) => {
  const options = categoryOptions.value
    .filter((item: any) => item.label.toLowerCase().includes(query.toLowerCase()))
    .map((item: any) => ({
      value: item.value,
      label: item.label
    }))
  cb(options)
}

const handleCategorySelect = (item: any) => {
  // 设置分类ID
  formData.categoryId = item.value
  // 确保categoryInput显示的是分类名称而不是ID
  categoryInput.value = item.label
}

onMounted(() => {
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