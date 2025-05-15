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
      <el-form-item label="问题内容" prop="content">
        <el-input
          v-model="formData.content"
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

      <el-form-item label="难度等级" prop="difficultyLevel">
        <el-select v-model="formData.difficultyLevel" placeholder="请选择难度等级">
          <el-option label="简单" value="easy" />
          <el-option label="中等" value="medium" />
          <el-option label="困难" value="hard" />
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
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import type { FormInstance, FormRules } from 'element-plus'
import { getQuestionById, createQuestion, updateQuestion } from '@/api/question'
import type { StandardQuestion } from '@/api/question'

const route = useRoute()
const router = useRouter()
const formRef = ref<FormInstance>()

const isEdit = computed(() => route.params.id !== undefined)

const formData = reactive<StandardQuestion>({
  content: '',
  questionType: 'subjective',
  difficultyLevel: 'medium'
})

const rules = reactive<FormRules>({
  content: [
    { required: true, message: '请输入问题内容', trigger: 'blur' }
  ],
  questionType: [
    { required: true, message: '请选择问题类型', trigger: 'change' }
  ]
})

const loadQuestion = async (id: number) => {
  try {
    const data = await getQuestionById(id)
    Object.assign(formData, data)
  } catch (error) {
    console.error('加载问题失败:', error)
  }
}

const submitForm = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        if (isEdit.value) {
          await updateQuestion(Number(route.params.id), formData)
        } else {
          await createQuestion(formData)
        }
        router.push('/questions')
      } catch (error) {
        console.error('保存失败:', error)
      }
    }
  })
}

onMounted(() => {
  if (isEdit.value) {
    loadQuestion(Number(route.params.id))
  }
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