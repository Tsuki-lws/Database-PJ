<template>
  <div class="raw-question-detail">
    <div class="page-header">
      <h2>原始问题详情</h2>
      <div class="actions">
        <el-button @click="$router.back()">返回</el-button>
        <el-button type="warning" @click="handleEdit">编辑问题</el-button>
        <el-button type="primary" @click="handleConvert">转为标准问题</el-button>
      </div>
    </div>

    <el-card shadow="never" class="question-card" v-loading="loading">
      <template #header>
        <div class="card-header">
          <h3>{{ question.questionTitle || '无标题' }}</h3>
          <div class="meta">
            <span>来源: {{ question.source || '未知' }}</span>
            <span>创建时间: {{ formatDate(question.createdAt) }}</span>
            <span v-if="question.sourceUrl">
              <el-link type="primary" :href="question.sourceUrl" target="_blank">原始链接</el-link>
            </span>
          </div>
        </div>
      </template>
      <div class="question-content">
        <div class="content-label">问题内容:</div>
        <div class="content-body">{{ question.questionBody }}</div>
      </div>
    </el-card>

    <el-card shadow="never" class="answers-card" v-loading="answersLoading">
      <template #header>
        <div class="card-header">
          <h3>原始回答列表 ({{ answerList.length }})</h3>
          <div class="actions">
            <el-button type="success" @click="handleAddAnswer">添加回答</el-button>
          </div>
        </div>
      </template>

      <div v-if="answerList.length === 0" class="no-data">
        暂无回答数据
      </div>

      <div v-for="answer in answerList" :key="answer.answerId" class="answer-item">
        <div class="answer-meta">
          <span>作者: {{ answer.authorInfo || '未知' }}</span>
          <span>赞同数: {{ answer.upvotes || 0 }}</span>
          <span>{{ answer.isAccepted ? '✓ 已采纳' : '' }}</span>
          <span>创建时间: {{ formatDate(answer.createdAt) }}</span>
        </div>
        <div class="answer-content">{{ answer.answerBody }}</div>
        <div class="answer-actions">
          <el-button size="small" type="warning" @click="handleEditAnswer(answer)">
            编辑
          </el-button>
          <el-button size="small" type="primary" @click="handleConvertAnswer(answer)">
            转为标准答案
          </el-button>
          <el-popconfirm
            title="确认删除该回答吗？"
            @confirm="handleDeleteAnswer(answer.answerId)"
          >
            <template #reference>
              <el-button size="small" type="danger">删除</el-button>
            </template>
          </el-popconfirm>
        </div>
      </div>
    </el-card>

    <!-- 编辑问题对话框 -->
    <el-dialog
      v-model="editDialogVisible"
      title="编辑原始问题"
      width="650px">
      <el-form
        ref="editFormRef"
        :model="editForm"
        :rules="editRules"
        label-width="100px">
        <el-form-item label="问题标题" prop="questionTitle">
          <el-input
            v-model="editForm.questionTitle"
            placeholder="请输入问题标题" />
        </el-form-item>
        <el-form-item label="问题内容" prop="questionBody">
          <el-input
            v-model="editForm.questionBody"
            type="textarea"
            :rows="6"
            placeholder="请输入问题内容" />
        </el-form-item>
        <el-form-item label="来源" prop="source">
          <el-select v-model="editForm.source" placeholder="请选择来源">
            <el-option label="Stack Overflow" value="stackoverflow" />
            <el-option label="GitHub Issues" value="github" />
            <el-option label="Quora" value="quora" />
            <el-option label="手动添加" value="manual" />
          </el-select>
        </el-form-item>
        <el-form-item label="来源URL">
          <el-input v-model="editForm.sourceUrl" placeholder="可选，原始问题链接" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="editDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitEdit" :loading="submitLoading">
            确认
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 添加/编辑回答对话框 -->
    <el-dialog
      v-model="answerDialogVisible"
      :title="isEditAnswer ? '编辑原始回答' : '添加原始回答'"
      width="650px">
      <el-form
        ref="answerFormRef"
        :model="answerForm"
        :rules="answerRules"
        label-width="100px">
        <el-form-item label="回答内容" prop="answerBody">
          <el-input
            v-model="answerForm.answerBody"
            type="textarea"
            :rows="8"
            placeholder="请输入回答内容" />
        </el-form-item>
        <el-form-item label="作者信息">
          <el-input v-model="answerForm.authorInfo" placeholder="可选，作者信息" />
        </el-form-item>
        <el-form-item label="赞同数">
          <el-input-number v-model="answerForm.upvotes" :min="0" />
        </el-form-item>
        <el-form-item label="是否采纳">
          <el-switch v-model="answerForm.isAccepted" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="answerDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitAnswer" :loading="submitLoading">
            确认
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 转换为标准问题对话框 -->
    <el-dialog
      v-model="convertDialogVisible"
      title="转换为标准问题"
      width="650px">
      <el-form
        ref="convertFormRef"
        :model="convertForm"
        :rules="convertRules"
        label-width="100px">
        <el-form-item label="问题内容" prop="question">
          <el-input
            v-model="convertForm.question"
            type="textarea"
            :rows="4"
            placeholder="请输入问题内容" />
        </el-form-item>

        <el-form-item label="问题类型" prop="questionType">
          <el-select v-model="convertForm.questionType" placeholder="请选择问题类型">
            <el-option label="单选题" value="single_choice" />
            <el-option label="多选题" value="multiple_choice" />
            <!-- <el-option label="简单事实题" value="simple_fact" /> -->
            <el-option label="主观题" value="subjective" />
          </el-select>
        </el-form-item>

        <el-form-item label="问题分类" prop="categoryId">
          <el-select v-model="convertForm.categoryId" placeholder="请选择问题分类">
            <el-option
              v-for="category in categories"
              :key="category.categoryId"
              :label="category.name"
              :value="category.categoryId" />
          </el-select>
        </el-form-item>

        <el-form-item label="难度" prop="difficulty">
          <el-select v-model="convertForm.difficulty" placeholder="请选择难度">
            <el-option label="简单" value="easy" />
            <el-option label="中等" value="medium" />
            <el-option label="困难" value="hard" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="convertDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitConvert" :loading="convertLoading">
            确认
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 转换为标准答案对话框 -->
    <el-dialog
      v-model="convertAnswerDialogVisible"
      title="转换为标准答案"
      width="650px">
      <el-form
        ref="convertAnswerFormRef"
        :model="convertAnswerForm"
        :rules="convertAnswerRules"
        label-width="100px">
        <el-form-item label="答案内容" prop="answer">
          <el-input
            v-model="convertAnswerForm.answer"
            type="textarea"
            :rows="6"
            placeholder="请输入答案内容" />
        </el-form-item>

        <el-form-item label="关联问题" prop="standardQuestionId">
          <el-select
            v-model="convertAnswerForm.standardQuestionId"
            placeholder="请选择关联的标准问题"
            filterable
            remote
            :remote-method="searchStandardQuestions"
            :loading="questionsLoading">
            <el-option
              v-for="item in standardQuestions"
              :key="item.standardQuestionId"
              :label="item.question"
              :value="item.standardQuestionId" />
          </el-select>
        </el-form-item>

        <el-form-item label="是否为最终答案">
          <el-switch v-model="convertAnswerForm.isFinal" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="convertAnswerDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitConvertAnswer" :loading="convertAnswerLoading">
            确认
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { 
  getRawQuestionById, 
  getRawAnswersByQuestionId, 
  convertToStandardQuestion,
  updateRawQuestion,
  deleteRawAnswer,
  type RawQuestion,
  type RawAnswer
} from '@/api/raw'
// 重新导入修改后的函数
import { createRawAnswer, updateRawAnswer } from '@/api/rawAnswers'
import { getAllCategories } from '@/api/category'
import { convertToStandardAnswer } from '@/api/raw'
import { getQuestionList } from '@/api/question'

const route = useRoute()
const router = useRouter()
const questionId = ref(parseInt(route.params.id as string) || 0)

// 数据
const question = ref<RawQuestion>({} as RawQuestion)
const answerList = ref<RawAnswer[]>([])
const loading = ref(false)
const answersLoading = ref(false)
const categories = ref<any[]>([])
const standardQuestions = ref<any[]>([])
const questionsLoading = ref(false)
const submitLoading = ref(false)

// 编辑问题
const editDialogVisible = ref(false)
const editFormRef = ref()
const editForm = reactive({
  questionId: 0,
  questionTitle: '',
  questionBody: '',
  source: '',
  sourceUrl: ''
})
const editRules = {
  questionTitle: [{ required: true, message: '请输入问题标题', trigger: 'blur' }],
  questionBody: [{ required: true, message: '请输入问题内容', trigger: 'blur' }],
  source: [{ required: true, message: '请选择来源', trigger: 'change' }]
}

// 添加/编辑回答
const answerDialogVisible = ref(false)
const isEditAnswer = ref(false)
const answerFormRef = ref()
const answerForm = reactive({
  answerId: undefined as number | undefined,
  questionId: 0,
  answerBody: '',
  authorInfo: '',
  upvotes: 0,
  isAccepted: false
})
const answerRules = {
  answerBody: [{ required: true, message: '请输入回答内容', trigger: 'blur' }]
}

// 转换问题对话框
const convertDialogVisible = ref(false)
const convertLoading = ref(false)
const convertForm = reactive({
  question: '',
  questionType: '',
  categoryId: undefined as number | undefined,
  difficulty: '',
})
const convertRules = {
  question: [{ required: true, message: '请输入问题内容', trigger: 'blur' }],
  questionType: [{ required: true, message: '请选择问题类型', trigger: 'change' }],
  categoryId: [{ required: true, message: '请选择问题分类', trigger: 'change' }]
}
const convertFormRef = ref()

// 转换答案对话框
const convertAnswerDialogVisible = ref(false)
const convertAnswerLoading = ref(false)
const convertAnswerForm = reactive({
  answer: '',
  standardQuestionId: undefined as number | undefined,
  isFinal: false,
  sourceAnswerId: 0
})
const convertAnswerRules = {
  answer: [{ required: true, message: '请输入答案内容', trigger: 'blur' }],
  standardQuestionId: [{ required: true, message: '请选择关联的标准问题', trigger: 'change' }]
}
const convertAnswerFormRef = ref()

// 初始化
onMounted(() => {
  getQuestionDetail()
  getAnswers()
  getCategories()
})

// 获取问题详情
const getQuestionDetail = async () => {
  if (!questionId.value) return
  
  loading.value = true
  try {
    const response = await getRawQuestionById(questionId.value)
    // 适应后端返回的数据结构
    question.value = response.data || response
  } catch (error: any) {
    ElMessage.error('获取问题详情失败: ' + error.message)
  } finally {
    loading.value = false
  }
}

// 获取回答列表
const getAnswers = async () => {
  if (!questionId.value) return
  
  answersLoading.value = true
  try {
    const response = await getRawAnswersByQuestionId(questionId.value)
    // 适应后端返回的数据结构
    answerList.value = (response.data || response) || []
  } catch (error: any) {
    ElMessage.error('获取回答列表失败: ' + error.message)
  } finally {
    answersLoading.value = false
  }
}

// 获取分类数据
const getCategories = async () => {
  try {
    const response = await getAllCategories()
    categories.value = (response.data || response) || []
  } catch (error: any) {
    ElMessage.error('获取分类列表失败: ' + error.message)
  }
}

// 搜索标准问题
const searchStandardQuestions = async (query: string) => {
  if (query.trim() === '') {
    standardQuestions.value = []
    return
  }
  
  questionsLoading.value = true
  try {
    const response = await getQuestionList({
      keyword: query,
      page: 1,
      size: 10
    })
    
    // 适应后端返回的数据结构
    const res = response.data || response
    standardQuestions.value = res.content || []
  } catch (error: any) {
    ElMessage.error('搜索标准问题失败: ' + error.message)
  } finally {
    questionsLoading.value = false
  }
}

// 转换为标准问题
const handleConvert = () => {
  convertForm.question = question.value.questionBody || ''
  convertForm.questionType = 'subjective' // 默认为主观题
  convertForm.difficulty = 'medium' // 默认为中等难度
  convertDialogVisible.value = true
}

// 提交转换
const submitConvert = async () => {
  if (!convertFormRef.value) return
  
  await convertFormRef.value.validate(async (valid: boolean) => {
    if (!valid) return
    
    convertLoading.value = true
    try {
      const response = await convertToStandardQuestion(questionId.value, {
        question: convertForm.question,
        questionType: convertForm.questionType,
        categoryId: convertForm.categoryId,
        difficulty: convertForm.difficulty
      })
      
      // 适应后端返回的数据结构
      const result = response.data || response
      
      ElMessage.success('转换成功')
      convertDialogVisible.value = false
    } catch (error: any) {
      ElMessage.error('转换失败: ' + error.message)
    } finally {
      convertLoading.value = false
    }
  })
}

// 转换为标准答案
const handleConvertAnswer = (answer: RawAnswer) => {
  convertAnswerForm.answer = answer.answerBody || ''
  convertAnswerForm.isFinal = answer.isAccepted || false
  convertAnswerForm.sourceAnswerId = answer.answerId || 0
  convertAnswerDialogVisible.value = true
}

// 提交转换答案
const submitConvertAnswer = async () => {
  if (!convertAnswerFormRef.value) return
  
  await convertAnswerFormRef.value.validate(async (valid: boolean) => {
    if (!valid) return
    
    convertAnswerLoading.value = true
    try {
      const response = await convertToStandardAnswer(convertAnswerForm.sourceAnswerId, {
        answer: convertAnswerForm.answer,
        standardQuestionId: convertAnswerForm.standardQuestionId,
        isFinal: convertAnswerForm.isFinal
      })
      
      // 适应后端返回的数据结构
      const result = response.data || response
      
      ElMessage.success('转换成功')
      convertAnswerDialogVisible.value = false
    } catch (error: any) {
      ElMessage.error('转换失败: ' + error.message)
    } finally {
      convertAnswerLoading.value = false
    }
  })
}

// 格式化日期
const formatDate = (dateString: string | undefined) => {
  if (!dateString) return '未知'
  return new Date(dateString).toLocaleString()
}

// 编辑问题
const handleEdit = () => {
  editForm.questionId = question.value.questionId || 0
  editForm.questionTitle = question.value.questionTitle || ''
  editForm.questionBody = question.value.questionBody || ''
  editForm.source = question.value.source || 'manual'
  editForm.sourceUrl = question.value.sourceUrl || ''
  editDialogVisible.value = true
}

// 提交编辑
const submitEdit = async () => {
  if (!editFormRef.value) return
  
  await editFormRef.value.validate(async (valid: boolean) => {
    if (!valid) return
    
    submitLoading.value = true
    try {
      await updateRawQuestion(editForm.questionId, editForm)
      ElMessage.success('更新问题成功')
      editDialogVisible.value = false
      getQuestionDetail()
    } catch (error: any) {
      ElMessage.error('更新问题失败: ' + error.message)
    } finally {
      submitLoading.value = false
    }
  })
}

// 添加回答
const handleAddAnswer = () => {
  if (!questionId.value) {
    ElMessage.error('问题ID无效')
    return
  }
  
  isEditAnswer.value = false
  answerForm.answerId = undefined
  answerForm.questionId = questionId.value
  answerForm.answerBody = ''
  answerForm.authorInfo = ''
  answerForm.upvotes = 0
  answerForm.isAccepted = false
  answerDialogVisible.value = true
}

// 编辑回答
const handleEditAnswer = (answer: RawAnswer) => {
  isEditAnswer.value = true
  answerForm.answerId = answer.answerId
  answerForm.questionId = answer.questionId
  answerForm.answerBody = answer.answerBody
  answerForm.authorInfo = answer.authorInfo || ''
  answerForm.upvotes = answer.upvotes || 0
  answerForm.isAccepted = answer.isAccepted || false
  answerDialogVisible.value = true
}

// 提交回答
const submitAnswer = async () => {
  if (!answerFormRef.value) return
  if (!questionId.value) {
    ElMessage.error('问题ID无效')
    return
  }
  
  await answerFormRef.value.validate(async (valid: boolean) => {
    if (!valid) return
    
    submitLoading.value = true
    try {
      if (isEditAnswer.value && answerForm.answerId) {
        await updateRawAnswer(answerForm.answerId, answerForm)
        ElMessage.success('更新回答成功')
      } else {
        await createRawAnswer(questionId.value, answerForm)
        ElMessage.success('添加回答成功')
      }
      answerDialogVisible.value = false
      getAnswers()
    } catch (error: any) {
      ElMessage.error(isEditAnswer.value ? '更新回答失败: ' + error.message : '添加回答失败: ' + error.message)
    } finally {
      submitLoading.value = false
    }
  })
}

// 删除回答
const handleDeleteAnswer = async (answerId: number) => {
  try {
    await deleteRawAnswer(answerId)
    ElMessage.success('删除回答成功')
    getAnswers()
  } catch (error: any) {
    ElMessage.error('删除回答失败: ' + error.message)
  }
}
</script>

<style scoped>
.raw-question-detail {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.question-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h3 {
  margin: 0 0 10px 0;
}

.meta {
  display: flex;
  gap: 15px;
  font-size: 14px;
  color: #666;
}

.question-content {
  margin-top: 10px;
}

.content-label {
  font-weight: bold;
  margin-bottom: 10px;
}

.content-body {
  white-space: pre-wrap;
  line-height: 1.6;
}

.answers-card {
  margin-bottom: 20px;
}

.no-data {
  text-align: center;
  padding: 20px;
  color: #999;
}

.answer-item {
  padding: 15px;
  border-bottom: 1px solid #eee;
}

.answer-item:last-child {
  border-bottom: none;
}

.answer-meta {
  display: flex;
  gap: 15px;
  font-size: 14px;
  color: #666;
  margin-bottom: 10px;
}

.answer-content {
  white-space: pre-wrap;
  line-height: 1.6;
  margin-bottom: 10px;
}

.answer-actions {
  margin-top: 10px;
  display: flex;
  gap: 10px;
}
</style>