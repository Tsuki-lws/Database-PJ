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
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { getQuestionById, updateQuestion } from '@/api/question'
import { getAllCategories } from '@/api/category'
import type { StandardQuestion, Category } from '@/api/question'

const route = useRoute()
const router = useRouter()
const formRef = ref<FormInstance>()
const loading = ref(false)
const submitting = ref(false)
const categoryOptions = ref<any[]>([])
const isEdit = ref(true)
const categoryInput = ref('')

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
    const response = await getQuestionById(id)
    console.log('获取问题详情原始响应:', response)
    
    // 处理响应数据
    let questionData
    if (response && typeof response === 'object') {
      // 处理标准返回格式：{ code: 200, data: {...}, message: "Success" }
      if ('code' in response && response.code === 200 && response.data) {
        questionData = response.data
      } else if ('data' in response) {
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
      
      // 如果有分类信息，设置categoryId和categoryInput
      if (questionData.category) {
        formData.categoryId = questionData.category.categoryId
        categoryInput.value = questionData.category.categoryName || questionData.category.name || ''
      } else if (questionData.categoryId) {
        formData.categoryId = questionData.categoryId
        // 根据categoryId查找对应的分类名称
        const category = categoryOptions.value.find(item => item.value === questionData.categoryId)
        if (category) {
          categoryInput.value = category.label
        }
      } else {
        formData.categoryId = null
        categoryInput.value = ''
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
      
      // 如果已经有categoryId，根据categoryId设置categoryInput的值
      if (formData.categoryId) {
        const category = categoryOptions.value.find(item => item.value === formData.categoryId)
        if (category) {
          categoryInput.value = category.label
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
          standardQuestionId: formData.standardQuestionId,
          question: formData.question,
          questionType: formData.questionType as 'single_choice' | 'multiple_choice' | 'simple_fact' | 'subjective',
          difficulty: formData.difficulty as 'easy' | 'medium' | 'hard',
          status: formData.status as 'draft' | 'pending_review' | 'approved' | 'rejected',
          version: formData.version
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
          } else {
            // 使用新分类名称
            submitData.category = {
              name: categoryInput.value.trim()
            }
          }
        }
        // 如果没有分类，不设置categoryId和category字段
        
        console.log('提交的表单数据:', submitData)
        
        const response = await updateQuestion(formData.standardQuestionId, submitData)
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

const queryCategories = (query: string, cb: (options: any[]) => void) => {
  const results = query ? categoryOptions.value.filter(item => item.label.toLowerCase().indexOf(query.toLowerCase()) > -1) : categoryOptions.value
  cb(results)
}

const handleCategorySelect = (item: any) => {
  formData.categoryId = item.value
  categoryInput.value = item.label
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