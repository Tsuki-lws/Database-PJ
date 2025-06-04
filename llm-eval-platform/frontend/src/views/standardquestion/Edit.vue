<template>
  <div class="question-edit">
    <div class="header">
      <h2>{{ isEdit ? '编辑问题' : '创建问题' }}</h2>
    </div>

    <el-form
      ref="formRef"
      :model="formData"
      :rules="rules"
      label-width="100px"
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
          <el-option label="主观题" value="subjective" />
          <el-option label="客观题" value="objective" />
        </el-select>
      </el-form-item>

      <el-form-item label="难度等级" prop="difficulty">
        <el-select v-model="formData.difficulty" placeholder="请选择难度等级">
          <el-option label="简单" value="easy" />
          <el-option label="中等" value="medium" />
          <el-option label="困难" value="hard" />
        </el-select>
      </el-form-item>

      <el-form-item label="状态" prop="status">
        <el-select v-model="formData.status" placeholder="请选择状态">
          <el-option label="草稿" value="draft" />
          <el-option label="待审核" value="pending_review" />
          <el-option label="已通过" value="approved" />
          <el-option label="已拒绝" value="rejected" />
        </el-select>
      </el-form-item>

      <el-form-item>
        <el-button type="primary" @click="submitForm">保存</el-button>
        <el-button @click="$router.back()">取消</el-button>
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
const categoryOptions = ref([])

const formData = reactive({
  standardQuestionId: 0,
  question: '',
  categoryId: null,
  questionType: '',
  difficulty: '',
  status: '',
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
    const res = await getStandardQuestion(id)
    Object.assign(formData, res)
    // 如果有categoryId，从category对象中提取
    if (res.category) {
      formData.categoryId = res.category.categoryId
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
    categoryOptions.value = res.map((item: any) => ({
      value: item.categoryId,
      label: item.name
    }))
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
        await updateStandardQuestion(formData.standardQuestionId, formData)
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