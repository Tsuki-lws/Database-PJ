<template>
  <div class="evaluation-create-container">
    <div class="page-header">
      <h2>创建模型评测</h2>
      <el-button @click="navigateBack">返回</el-button>
    </div>

    <el-card shadow="never" class="form-card" v-loading="loading">
      <el-form :model="evaluationForm" :rules="rules" ref="evaluationFormRef" label-width="120px">
        <el-form-item label="评测名称" prop="name">
          <el-input v-model="evaluationForm.name" placeholder="请输入评测名称"></el-input>
        </el-form-item>

        <el-form-item label="评测描述" prop="description">
          <el-input 
            v-model="evaluationForm.description" 
            type="textarea" 
            :rows="4" 
            placeholder="请输入评测描述">
          </el-input>
        </el-form-item>

        <el-form-item label="模型名称" prop="modelName">
          <el-input v-model="evaluationForm.modelName" placeholder="请输入模型名称"></el-input>
        </el-form-item>

        <el-form-item label="模型提供商" prop="modelProvider">
          <el-select v-model="evaluationForm.modelProvider" placeholder="请选择模型提供商">
            <el-option label="OpenAI" value="openai"></el-option>
            <el-option label="Anthropic" value="anthropic"></el-option>
            <el-option label="Google" value="google"></el-option>
            <el-option label="百度" value="baidu"></el-option>
            <el-option label="讯飞" value="xunfei"></el-option>
            <el-option label="智谱" value="zhipu"></el-option>
            <el-option label="其他" value="other"></el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="模型版本" prop="modelVersion">
          <el-input v-model="evaluationForm.modelVersion" placeholder="请输入模型版本"></el-input>
        </el-form-item>

        <el-form-item label="评测数据集" prop="datasetVersionId">
          <el-select 
            v-model="evaluationForm.datasetVersionId" 
            placeholder="请选择评测数据集"
            @change="handleDatasetChange">
            <el-option 
              v-for="item in datasetOptions" 
              :key="item.value" 
              :label="item.label" 
              :value="item.value">
            </el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="问题数量">
          <span>{{ selectedDatasetInfo.questionCount || 0 }} 个问题</span>
        </el-form-item>

        <el-divider content-position="left">评测设置</el-divider>

        <el-form-item label="评测方法" prop="evaluationMethod">
          <el-select v-model="evaluationForm.evaluationMethod" placeholder="请选择评测方法">
            <el-option label="人工评测" value="manual"></el-option>
            <el-option label="自动评测" value="auto"></el-option>
            <el-option label="混合评测" value="hybrid"></el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="评分标准" prop="scoringCriteria">
          <el-input 
            v-model="evaluationForm.scoringCriteria" 
            type="textarea" 
            :rows="3" 
            placeholder="请输入评分标准">
          </el-input>
        </el-form-item>

        <el-form-item label="温度参数" prop="temperature">
          <el-slider 
            v-model="evaluationForm.temperature" 
            :min="0" 
            :max="1" 
            :step="0.1" 
            show-input>
          </el-slider>
        </el-form-item>

        <el-form-item label="最大输出长度" prop="maxTokens">
          <el-input-number 
            v-model="evaluationForm.maxTokens" 
            :min="100" 
            :max="4096" 
            :step="100">
          </el-input-number>
        </el-form-item>

        <el-form-item label="API密钥" prop="apiKey">
          <el-input 
            v-model="evaluationForm.apiKey" 
            placeholder="请输入API密钥"
            show-password>
          </el-input>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="submitForm">创建评测</el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { createEvaluation } from '@/api/evaluation'
import { getDatasetVersions } from '@/api/dataset'

const router = useRouter()
const loading = ref(false)
const evaluationFormRef = ref()

// 表单数据
const evaluationForm = reactive({
  name: '',
  description: '',
  modelName: '',
  modelProvider: '',
  modelVersion: '',
  datasetVersionId: undefined,
  evaluationMethod: 'auto',
  scoringCriteria: '',
  temperature: 0.7,
  maxTokens: 1000,
  apiKey: ''
})

// 表单验证规则
const rules = {
  name: [
    { required: true, message: '请输入评测名称', trigger: 'blur' }
  ],
  modelName: [
    { required: true, message: '请输入模型名称', trigger: 'blur' }
  ],
  modelProvider: [
    { required: true, message: '请选择模型提供商', trigger: 'change' }
  ],
  datasetVersionId: [
    { required: true, message: '请选择评测数据集', trigger: 'change' }
  ],
  evaluationMethod: [
    { required: true, message: '请选择评测方法', trigger: 'change' }
  ],
  apiKey: [
    { required: true, message: '请输入API密钥', trigger: 'blur' }
  ]
}

// 数据集选项
const datasetOptions = ref<Array<{value: number, label: string}>>([])
const selectedDatasetInfo = ref<any>({})

// 获取数据集列表
const getDatasets = async () => {
  loading.value = true
  try {
    const res = await getDatasetVersions({ isPublished: true })
    datasetOptions.value = (res.list || []).map((item: any) => ({
      value: item.versionId,
      label: item.name,
      questionCount: item.questionCount
    }))
  } catch (error) {
    console.error('获取数据集列表失败', error)
  } finally {
    loading.value = false
  }
}

// 处理数据集变化
const handleDatasetChange = (value: number) => {
  const selected = datasetOptions.value.find(item => item.value === value)
  if (selected) {
    selectedDatasetInfo.value = selected
  } else {
    selectedDatasetInfo.value = {}
  }
}

// 提交表单
const submitForm = async () => {
  if (!evaluationFormRef.value) return
  
  await evaluationFormRef.value.validate(async (valid: boolean) => {
    if (!valid) return
    
    loading.value = true
    try {
      await createEvaluation(evaluationForm)
      ElMessage.success('创建成功')
      router.push('/evaluations')
    } catch (error) {
      console.error('创建评测失败', error)
    } finally {
      loading.value = false
    }
  })
}

// 重置表单
const resetForm = () => {
  if (!evaluationFormRef.value) return
  evaluationFormRef.value.resetFields()
  selectedDatasetInfo.value = {}
}

// 返回上一页
const navigateBack = () => {
  router.back()
}

onMounted(() => {
  getDatasets()
})
</script>

<style scoped>
.evaluation-create-container {
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
</style> 