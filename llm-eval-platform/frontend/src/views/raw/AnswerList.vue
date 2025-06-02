<template>
  <div class="raw-answer-list-container">
    <div class="page-header">
      <h2>原始回答管理</h2>
      <div class="header-actions">
        <el-button type="primary" @click="refreshData">刷新数据</el-button>
      </div>
    </div>

    <!-- 搜索和筛选 -->
    <el-card shadow="never" class="filter-container">
      <el-form :model="queryParams" label-width="80px" :inline="true">
        <el-form-item label="问题ID">
          <el-input v-model="queryParams.questionId" placeholder="输入问题ID" clearable />
        </el-form-item>
        <el-form-item label="关键词">
          <el-input v-model="queryParams.keyword" placeholder="搜索回答内容" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 回答列表 -->
    <el-card shadow="never" class="list-card">
      <el-table v-loading="loading" :data="answerList" style="width: 100%">
        <el-table-column prop="answerId" label="ID" width="80" />
        <el-table-column prop="questionId" label="问题ID" width="100" />
        <el-table-column prop="answerBody" label="回答内容" show-overflow-tooltip>
          <template #default="scope">
            <div v-html="truncateHtml(scope.row.answerBody, 100)"></div>
          </template>
        </el-table-column>
        <el-table-column prop="authorInfo" label="作者信息" width="120" />
        <el-table-column prop="upvotes" label="点赞数" width="100" />
        <el-table-column prop="isAccepted" label="是否采纳" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.isAccepted ? 'success' : 'info'">
              {{ scope.row.isAccepted ? '已采纳' : '未采纳' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column fixed="right" label="操作" width="220">
          <template #default="scope">
            <el-button link type="primary" @click="viewAnswer(scope.row)">查看</el-button>
            <el-button link type="primary" @click="editAnswer(scope.row)">编辑</el-button>
            <el-button link type="danger" @click="deleteAnswer(scope.row)">删除</el-button>
            <el-button link type="success" @click="convertToStandard(scope.row)">转为标准答案</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="queryParams.page"
          v-model:page-size="queryParams.size"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 查看回答详情对话框 -->
    <el-dialog v-model="viewDialogVisible" title="回答详情" width="700px">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="回答ID">{{ currentAnswer.answerId }}</el-descriptions-item>
        <el-descriptions-item label="问题ID">
          <el-link type="primary" @click="viewQuestion(currentAnswer.questionId)">
            {{ currentAnswer.questionId }}
          </el-link>
        </el-descriptions-item>
        <el-descriptions-item label="作者信息">{{ currentAnswer.authorInfo || '无' }}</el-descriptions-item>
        <el-descriptions-item label="点赞数">{{ currentAnswer.upvotes || 0 }}</el-descriptions-item>
        <el-descriptions-item label="是否采纳">
          <el-tag :type="currentAnswer.isAccepted ? 'success' : 'info'">
            {{ currentAnswer.isAccepted ? '已采纳' : '未采纳' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ currentAnswer.createdAt }}</el-descriptions-item>
        <el-descriptions-item label="回答内容">
          <div class="answer-content" v-html="currentAnswer.answerBody"></div>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <!-- 编辑回答对话框 -->
    <el-dialog v-model="editDialogVisible" title="编辑回答" width="700px">
      <el-form :model="editForm" label-width="100px">
        <el-form-item label="回答ID">
          <el-input v-model="editForm.answerId" disabled />
        </el-form-item>
        <el-form-item label="问题ID">
          <el-input v-model="editForm.questionId" disabled />
        </el-form-item>
        <el-form-item label="作者信息">
          <el-input v-model="editForm.authorInfo" />
        </el-form-item>
        <el-form-item label="点赞数">
          <el-input-number v-model="editForm.upvotes" :min="0" />
        </el-form-item>
        <el-form-item label="是否采纳">
          <el-switch v-model="editForm.isAccepted" />
        </el-form-item>
        <el-form-item label="回答内容">
          <el-input v-model="editForm.answerBody" type="textarea" :rows="10" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="editDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveAnswer" :loading="submitting">保存</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 转换为标准答案对话框 -->
    <el-dialog v-model="convertDialogVisible" title="转换为标准答案" width="600px">
      <el-form :model="convertForm" label-width="120px">
        <el-form-item label="选择标准问题">
          <el-select v-model="convertForm.standardQuestionId" filterable placeholder="请选择标准问题">
            <el-option
              v-for="item in standardQuestions"
              :key="item.standardQuestionId"
              :label="`${item.standardQuestionId}: ${truncateText(item.question, 50)}`"
              :value="item.standardQuestionId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="自定义答案内容">
          <el-switch v-model="convertForm.customContent" />
        </el-form-item>
        <el-form-item label="答案内容" v-if="convertForm.customContent">
          <el-input v-model="convertForm.answerText" type="textarea" :rows="8" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="convertDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitConvert" :loading="submitting">转换</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getRawAnswers, getRawAnswer, updateRawAnswer, deleteRawAnswer, convertToStandardAnswer } from '@/api/rawAnswers'
import { getStandardQuestions } from '@/api/standardQuestions'

const router = useRouter()
const loading = ref(false)
const submitting = ref(false)
const answerList = ref<any[]>([])
const total = ref(0)
const standardQuestions = ref<any[]>([])

// 查询参数
const queryParams = reactive({
  page: 1,
  size: 10,
  questionId: '',
  keyword: ''
})

// 当前查看的回答
const currentAnswer = ref<any>({})
const viewDialogVisible = ref(false)

// 编辑回答
const editForm = ref<any>({})
const editDialogVisible = ref(false)

// 转换为标准答案
const convertForm = reactive({
  standardQuestionId: undefined as number | undefined,
  customContent: false,
  answerText: '',
  rawAnswerId: 0
})
const convertDialogVisible = ref(false)

// 初始化
onMounted(() => {
  fetchData()
  fetchStandardQuestions()
})

// 获取回答列表
const fetchData = async () => {
  loading.value = true
  try {
    const res = await getRawAnswers(queryParams)
    answerList.value = res.data.content || []
    total.value = res.data.totalElements || 0
  } catch (error: any) {
    ElMessage.error('获取回答列表失败: ' + error.message)
  } finally {
    loading.value = false
  }
}

// 获取标准问题列表（用于转换）
const fetchStandardQuestions = async () => {
  try {
    const res = await getStandardQuestions({ page: 1, size: 100 })
    standardQuestions.value = res.data.content || []
  } catch (error: any) {
    ElMessage.error('获取标准问题列表失败: ' + error.message)
  }
}

// 查看回答详情
const viewAnswer = async (row: any) => {
  try {
    const res = await getRawAnswer(row.answerId)
    currentAnswer.value = res.data
    viewDialogVisible.value = true
  } catch (error: any) {
    ElMessage.error('获取回答详情失败: ' + error.message)
  }
}

// 查看问题详情
const viewQuestion = (questionId: number) => {
  router.push(`/raw-questions/detail/${questionId}`)
}

// 编辑回答
const editAnswer = async (row: any) => {
  try {
    const res = await getRawAnswer(row.answerId)
    editForm.value = { ...res.data }
    editDialogVisible.value = true
  } catch (error: any) {
    ElMessage.error('获取回答详情失败: ' + error.message)
  }
}

// 保存编辑后的回答
const saveAnswer = async () => {
  submitting.value = true
  try {
    await updateRawAnswer(editForm.value.answerId, editForm.value)
    ElMessage.success('保存成功')
    editDialogVisible.value = false
    fetchData()
  } catch (error: any) {
    ElMessage.error('保存失败: ' + error.message)
  } finally {
    submitting.value = false
  }
}

// 删除回答
const deleteAnswer = (row: any) => {
  ElMessageBox.confirm(
    `确认删除ID为 ${row.answerId} 的回答吗？`,
    '删除确认',
    {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await deleteRawAnswer(row.answerId)
      ElMessage.success('删除成功')
      fetchData()
    } catch (error: any) {
      ElMessage.error('删除失败: ' + error.message)
    }
  }).catch(() => {})
}

// 转换为标准答案
const convertToStandard = (row: any) => {
  convertForm.rawAnswerId = row.answerId
  convertForm.customContent = false
  convertForm.answerText = ''
  convertForm.standardQuestionId = undefined
  convertDialogVisible.value = true
}

// 提交转换
const submitConvert = async () => {
  if (!convertForm.standardQuestionId) {
    ElMessage.warning('请选择标准问题')
    return
  }

  submitting.value = true
  try {
    const params = {
      standardQuestionId: convertForm.standardQuestionId,
      customContent: convertForm.customContent,
      answerText: convertForm.customContent ? convertForm.answerText : undefined
    }
    await convertToStandardAnswer(convertForm.rawAnswerId, params)
    ElMessage.success('转换成功')
    convertDialogVisible.value = false
  } catch (error: any) {
    ElMessage.error('转换失败: ' + error.message)
  } finally {
    submitting.value = false
  }
}

// 搜索
const handleSearch = () => {
  queryParams.page = 1
  fetchData()
}

// 重置查询条件
const resetQuery = () => {
  queryParams.page = 1
  queryParams.questionId = ''
  queryParams.keyword = ''
  fetchData()
}

// 刷新数据
const refreshData = () => {
  fetchData()
}

// 每页数量变化
const handleSizeChange = (val: number) => {
  queryParams.size = val
  fetchData()
}

// 当前页变化
const handleCurrentChange = (val: number) => {
  queryParams.page = val
  fetchData()
}

// 截断HTML内容
const truncateHtml = (html: string, length: number) => {
  if (!html) return ''
  const div = document.createElement('div')
  div.innerHTML = html
  const text = div.textContent || div.innerText || ''
  if (text.length <= length) return html
  return text.substring(0, length) + '...'
}

// 截断文本
const truncateText = (text: string, length: number) => {
  if (!text) return ''
  if (text.length <= length) return text
  return text.substring(0, length) + '...'
}
</script>

<style scoped>
.raw-answer-list-container {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.filter-container {
  margin-bottom: 20px;
}

.list-card {
  margin-bottom: 20px;
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

.answer-content {
  max-height: 400px;
  overflow-y: auto;
  padding: 10px;
  background-color: #f5f7fa;
  border-radius: 4px;
}
</style> 