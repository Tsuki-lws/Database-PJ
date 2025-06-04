<!-- <template>
  <div class="import-container">
    <div class="page-header">
      <h2>模型回答导入</h2>
    </div>

    <el-card shadow="never" class="import-card">
      <el-form label-position="top" :model="formData">
        <el-form-item label="选择问题">
          <el-select
            v-model="formData.questionId"
            placeholder="请选择问题"
            filterable
            remote
            :remote-method="searchQuestions"
            :loading="loading.questions"
            clearable
          >
            <el-option
              v-for="item in questionOptions"
              :key="item.questionId"
              :label="item.question"
              :value="item.questionId"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="选择模型">
          <el-select v-model="formData.modelId" placeholder="请选择模型" filterable clearable>
            <el-option
              v-for="item in modelOptions"
              :key="item.modelId"
              :label="item.name + ' ' + (item.version || '')"
              :value="item.modelId"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="模型回答内容">
          <el-input
            v-model="formData.answer"
            type="textarea"
            :rows="10"
            placeholder="请输入模型回答内容"
          />
        </el-form-item>

        <el-form-item label="回答时间 (ms)">
          <el-input-number v-model="formData.responseTime" :min="0" />
        </el-form-item>

        <el-form-item label="回答状态">
          <el-select v-model="formData.status" placeholder="请选择回答状态">
            <el-option label="成功" value="success" />
            <el-option label="失败" value="failed" />
            <el-option label="超时" value="timeout" />
          </el-select>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="loading.submit">提交</el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    历史记录
    <el-card shadow="never" class="history-card">
      <template #header>
        <div class="card-header">
          <span>最近导入记录</span>
        </div>
      </template>
      
      <el-table :data="importHistory" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="question" label="问题" show-overflow-tooltip />
        <el-table-column prop="modelName" label="模型" width="120" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="importTime" label="导入时间" width="180" />
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
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { importModelAnswer } from '@/api/import'
import { searchStandardQuestions } from '@/api/questions'
import { getModelList } from '@/api/evaluation'

// 表单数据
const formData = reactive({
  questionId: null as number | null,
  modelId: null as number | null,
  answer: '',
  responseTime: 1000,
  status: 'success'
})

// 加载状态
const loading = reactive({
  questions: false,
  submit: false
})

// 问题选项
const questionOptions = ref<any[]>([])

// 模型选项
const modelOptions = ref<any[]>([])

// 导入历史
const importHistory = ref([
  {
    id: 1,
    question: '什么是大语言模型？',
    modelName: 'GPT-4',
    status: 'success',
    importTime: '2023-05-15 14:30:22'
  },
  {
    id: 2,
    question: '解释一下量子计算的基本原理',
    modelName: 'Claude 2',
    status: 'success',
    importTime: '2023-05-14 10:15:45'
  },
  {
    id: 3,
    question: '编写一个快速排序算法',
    modelName: 'LLaMA 2',
    status: 'failed',
    importTime: '2023-05-16 09:22:10'
  }
])

// 初始化
onMounted(() => {
  fetchModels()
})

// 获取模型列表
const fetchModels = async () => {
  try {
    // 实际项目中应该调用API获取数据
    // const res = await getModelList()
    // modelOptions.value = res.data
    
    // 模拟数据
    modelOptions.value = [
      { modelId: 1, name: 'GPT-4', version: '0613' },
      { modelId: 2, name: 'Claude 2', version: '' },
      { modelId: 3, name: 'LLaMA 2', version: '70B' },
      { modelId: 4, name: 'Mistral', version: '7B' },
      { modelId: 5, name: 'Baichuan 2', version: '13B' }
    ]
  } catch (error) {
    console.error('获取模型列表失败', error)
  }
}

// 搜索问题
const searchQuestions = async (query: string) => {
  if (query.length < 2) return
  
  loading.value.questions = true
  try {
    // 实际项目中应该调用API获取数据
    // const res = await searchStandardQuestions(query)
    // questionOptions.value = res.data
    
    // 模拟数据
    setTimeout(() => {
      questionOptions.value = [
        { questionId: 1, question: '什么是大语言模型？' },
        { questionId: 2, question: '解释一下量子计算的基本原理' },
        { questionId: 3, question: '编写一个快速排序算法' },
        { questionId: 4, question: '简述人工智能的发展历史' },
        { questionId: 5, question: '如何实现神经网络的反向传播算法？' }
      ].filter(item => item.question.includes(query))
      loading.value.questions = false
    }, 500)
  } catch (error) {
    console.error('搜索问题失败', error)
    loading.value.questions = false
  }
}

// 提交表单
const handleSubmit = async () => {
  // 表单验证
  if (!formData.questionId) {
    ElMessage.warning('请选择问题')
    return
  }
  if (!formData.modelId) {
    ElMessage.warning('请选择模型')
    return
  }
  if (!formData.answer.trim()) {
    ElMessage.warning('请输入模型回答内容')
    return
  }
  
  loading.value.submit = true
  try {
    // 实际项目中应该调用API提交数据
    // const res = await importModelAnswer(formData)
    // if (res.success) {
    //   ElMessage.success('模型回答导入成功')
    //   resetForm()
    // } else {
    //   ElMessage.error(res.message || '导入失败')
    // }
    
    // 模拟成功响应
    setTimeout(() => {
      ElMessage.success('模型回答导入成功')
      resetForm()
      loading.value.submit = false
    }, 1000)
  } catch (error) {
    console.error('导入模型回答失败', error)
    ElMessage.error('导入失败，请重试')
    loading.value.submit = false
  }
}

// 重置表单
const resetForm = () => {
  formData.questionId = null
  formData.modelId = null
  formData.answer = ''
  formData.responseTime = 1000
  formData.status = 'success'
}

// 获取状态类型（用于标签颜色）
const getStatusType = (status: string) => {
  switch (status) {
    case 'success': return 'success'
    case 'failed': return 'danger'
    case 'timeout': return 'warning'
    default: return 'info'
  }
}

// 获取状态文本
const getStatusText = (status: string) => {
  switch (status) {
    case 'success': return '成功'
    case 'failed': return '失败'
    case 'timeout': return '超时'
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
</style>  -->