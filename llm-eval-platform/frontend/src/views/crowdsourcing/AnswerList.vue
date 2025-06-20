<template>
  <div class="answer-list-container">
    <div class="page-header">
      <h2>众包答案列表</h2>
      <el-button @click="goBack">返回</el-button>
    </div>

    <el-card v-loading="taskLoading" shadow="never" class="task-card">
      <template #header>
        <div class="card-header">
          <span>{{ task.title || '加载中...' }}</span>
          <el-tag v-if="task.status" :type="getStatusTag(task.status)">{{ formatStatus(task.status) }}</el-tag>
        </div>
      </template>

      <div class="task-info">
        <div class="info-item">
          <span class="label">任务描述:</span>
          <span>{{ task.description || '无' }}</span>
        </div>
        <div class="info-item">
          <span class="label">所需答案数:</span>
          <span>{{ task.requiredAnswers || 0 }}</span>
        </div>
        <div class="info-item">
          <span class="label">已提交答案数:</span>
          <span>{{ task.submittedAnswerCount || 0 }}</span>
        </div>
        <!-- <div class="info-item">
          <span class="label">已批准答案数:</span>
          <span>{{ task.approvedAnswerCount || 0 }}</span>
        </div>
        <div class="info-item">
          <span class="label">已提交答案数:</span>
          <span>{{ task.promotedAnswerCount || 0 }}</span>
        </div> -->
        <div class="info-item" v-if="task.createdBy">
          <span class="label">创建人:</span>
          <span>{{ task.createdBy }}</span>
        </div>
      </div>

      <div class="question-info" v-if="standardQuestion">
        <h3>标准问题</h3>
        <div class="question-content">
          {{ standardQuestion.question }}
        </div>
      </div>
    </el-card>

    <el-card shadow="never" class="answer-card">
      <template #header>
        <div class="card-header">
          <span>答案列表</span>
          <div class="filter-actions">
            <el-select v-model="queryParams.status" placeholder="状态筛选" clearable @change="handleFilter">
              <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value"></el-option>
            </el-select>
            <el-button type="primary" size="small" @click="handleFilter">筛选</el-button>
            <el-button size="small" @click="resetFilter">重置</el-button>
          </div>
        </div>
      </template>

      <el-table v-loading="loading" :data="answerList" style="width: 100%">
        <el-table-column prop="answerId" label="ID" width="80" />
        <el-table-column prop="contributorName" label="提交者" width="120" />
        <el-table-column prop="contributorEmail" label="邮箱" width="180" show-overflow-tooltip />
        <el-table-column prop="occupation" label="职业/身份" width="120" />
        <el-table-column prop="answerText" label="答案内容" show-overflow-tooltip>
          <template #default="scope">
            <el-popover
              placement="top-start"
              :title="`${scope.row.contributorName || '匿名'}的回答`"
              :width="400"
              trigger="hover"
            >
              <template #default>
                <div class="answer-popover-content">
                  <p><strong>回答内容：</strong>{{ scope.row.answerText }}</p>
                  <p v-if="scope.row.references"><strong>参考资料：</strong>{{ scope.row.references }}</p>
                  <p v-if="scope.row.expertise"><strong>专业领域：</strong>{{ scope.row.expertise }}</p>
                </div>
              </template>
              <template #reference>
                <div class="answer-content">{{ truncateText(scope.row.answerText, 100) }}</div>
              </template>
            </el-popover>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getAnswerStatusTag(scope.row.status)">
              {{ formatAnswerStatus(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="qualityScore" label="质量评分" width="100">
          <template #default="scope">
            <span v-if="scope.row.qualityScore">{{ scope.row.qualityScore }}</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="提交时间" width="180">
          <template #default="scope">
            {{ formatDateTime(scope.row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column fixed="right" label="操作" width="200">
          <template #default="scope">
            <el-button 
              v-if="scope.row.status === 'SUBMITTED'" 
              link 
              type="success" 
              @click="handleApprove(scope.row)"
            >
              批准
            </el-button>
            <el-button 
              v-if="scope.row.status === 'SUBMITTED'" 
              link 
              type="danger" 
              @click="handleReject(scope.row)"
            >
              拒绝
            </el-button>
            <el-button 
              v-if="scope.row.status === 'APPROVED'" 
              link 
              type="primary" 
              @click="handlePromote(scope.row)"
            >
              提升为标准答案
            </el-button>
            <el-button
              link
              type="info"
              @click="viewAnswerDetails(scope.row)"
            >
              详情
            </el-button>
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

    <!-- 审核对话框 -->
    <el-dialog v-model="reviewDialogVisible" :title="reviewType === 'approve' ? '批准答案' : '拒绝答案'" width="600px">
      <el-form :model="reviewForm" ref="reviewFormRef" label-width="100px">
        <el-form-item label="答案ID">
          <span>{{ reviewForm.answerId }}</span>
        </el-form-item>
        <el-form-item label="提交者">
          <span>{{ reviewForm.contributorName }}</span>
        </el-form-item>
        <el-form-item label="邮箱">
          <span>{{ reviewForm.contributorEmail }}</span>
        </el-form-item>
        <el-form-item label="职业/身份">
          <span>{{ reviewForm.occupation }}</span>
        </el-form-item>
        <el-form-item label="专业领域">
          <span>{{ reviewForm.expertise }}</span>
        </el-form-item>
        <el-form-item label="答案内容">
          <div class="review-answer-content">{{ reviewForm.answerText }}</div>
        </el-form-item>
        <el-form-item label="参考资料" v-if="reviewForm.references">
          <div class="review-answer-content">{{ reviewForm.references }}</div>
        </el-form-item>
        <el-form-item label="审核评语">
          <el-input 
            v-model="reviewForm.comment" 
            type="textarea" 
            :rows="3" 
            placeholder="请输入审核评语">
          </el-input>
        </el-form-item>
        <el-form-item v-if="reviewType === 'approve'" label="质量评分">
          <el-rate 
            v-model="reviewForm.qualityScore" 
            :max="5" 
            show-score 
            :colors="['#99A9BF', '#F7BA2A', '#FF9900']">
          </el-rate>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="reviewDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitReview" :loading="submitting">确认</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 答案详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="答案详情" width="700px">
      <div class="answer-detail-container">
        <div class="detail-section">
          <h3>提交者信息</h3>
          <div class="detail-item">
            <span class="detail-label">姓名：</span>
            <span>{{ currentAnswer.contributorName }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">邮箱：</span>
            <span>{{ currentAnswer.contributorEmail }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">职业/身份：</span>
            <span>{{ currentAnswer.occupation }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">专业领域：</span>
            <span>{{ currentAnswer.expertise }}</span>
          </div>
        </div>
        
        <el-divider></el-divider>
        
        <div class="detail-section">
          <h3>答案内容</h3>
          <div class="answer-content-box">
            {{ currentAnswer.answerText }}
          </div>
        </div>
        
        <div class="detail-section" v-if="currentAnswer.references">
          <h3>参考资料</h3>
          <div class="answer-content-box">
            {{ currentAnswer.references }}
          </div>
        </div>
        
        <el-divider></el-divider>
        
        <div class="detail-section">
          <h3>审核信息</h3>
          <div class="detail-item">
            <span class="detail-label">状态：</span>
            <el-tag :type="getAnswerStatusTag(currentAnswer.status)">
              {{ formatAnswerStatus(currentAnswer.status) }}
            </el-tag>
          </div>
          <div class="detail-item" v-if="currentAnswer.qualityScore">
            <span class="detail-label">质量评分：</span>
            <el-rate 
              v-model="currentAnswer.qualityScore" 
              disabled 
              show-score>
            </el-rate>
          </div>
          <div class="detail-item" v-if="currentAnswer.reviewComment">
            <span class="detail-label">审核评语：</span>
            <div class="comment-box">{{ currentAnswer.reviewComment }}</div>
          </div>
          <div class="detail-item">
            <span class="detail-label">提交时间：</span>
            <span>{{ formatDateTime(currentAnswer.createdAt) }}</span>
          </div>
          <div class="detail-item" v-if="currentAnswer.updatedAt">
            <span class="detail-label">更新时间：</span>
            <span>{{ formatDateTime(currentAnswer.updatedAt) }}</span>
          </div>
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="detailDialogVisible = false">关闭</el-button>
          <el-button 
            v-if="currentAnswer.status === 'SUBMITTED'" 
            type="success" 
            @click="handleApprove(currentAnswer)">
            批准
          </el-button>
          <el-button 
            v-if="currentAnswer.status === 'SUBMITTED'" 
            type="danger" 
            @click="handleReject(currentAnswer)">
            拒绝
          </el-button>
          <el-button 
            v-if="currentAnswer.status === 'APPROVED'" 
            type="primary" 
            @click="handlePromote(currentAnswer)">
            提升为标准答案
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  getTaskDetail, 
  getAnswersByTaskId, 
  reviewAnswer, 
  promoteToStandardAnswer 
} from '@/api/crowdsourcing'
import { getQuestionById } from '@/api/question'

const router = useRouter()
const route = useRoute()
const taskId = ref<number>(0)
const taskLoading = ref(true)
const loading = ref(true)
const submitting = ref(false)
const task = ref<any>({})
const standardQuestion = ref<any>(null)
const answerList = ref<any[]>([])
const total = ref(0)

// 查询参数
const queryParams = reactive({
  page: 1,
  size: 10,
  status: undefined
})

// 状态选项
const statusOptions = [
  { value: 'SUBMITTED', label: '待审核' },
  { value: 'APPROVED', label: '已批准' },
  { value: 'REJECTED', label: '已拒绝' },
  { value: 'PROMOTED', label: '已提升' }
]

// 审核对话框
const reviewDialogVisible = ref(false)
const reviewType = ref<'approve' | 'reject'>('approve')
const reviewForm = reactive({
  answerId: 0,
  contributorName: '',
  contributorEmail: '',
  occupation: '',
  expertise: '',
  answerText: '',
  references: '',
  comment: '',
  qualityScore: 3 // 默认3分
})

// 详情对话框
const detailDialogVisible = ref(false)
const currentAnswer = ref<any>({})

// 初始化任务ID
const initTaskId = () => {
  try {
    const idParam = route.params.taskId
    
    if (typeof idParam === 'string' && idParam) {
      const id = parseInt(idParam, 10)
      if (!isNaN(id) && id > 0) {
        console.log('成功解析任务ID:', id)
        taskId.value = id
        return true
      }
    }
    
    console.error('无效的任务ID参数:', route.params.taskId)
    ElMessage.error('无效的任务ID，请检查URL')
    return false
  } catch (error) {
    console.error('解析任务ID时出错:', error)
    ElMessage.error('任务ID解析错误')
    return false
  }
}

// 获取任务详情
const getTaskInfo = async () => {
  if (!taskId.value || taskId.value <= 0) {
    console.error('任务ID无效，无法获取任务详情')
    ElMessage.error('无效的任务ID，无法获取任务详情')
    taskLoading.value = false
    return
  }
  
  taskLoading.value = true
  try {
    console.log('API调用: 获取任务详情，任务ID:', taskId.value)
    const res = await getTaskDetail(taskId.value)
    if (res && res.data) {
      task.value = res.data
      console.log('获取到任务详情:', task.value)
      
      // 获取关联的标准问题
      if (task.value.standardQuestionId) {
        await getStandardQuestion(task.value.standardQuestionId)
      }
    } else {
      console.error('获取任务详情失败，返回数据异常')
      ElMessage.error('获取任务详情失败')
    }
  } catch (error) {
    console.error('获取任务详情失败:', error)
    ElMessage.error('获取任务详情失败')
  } finally {
    taskLoading.value = false
  }
}

// 获取标准问题
const getStandardQuestion = async (questionId: number) => {
  if (!questionId || questionId <= 0) {
    console.error('问题ID无效:', questionId)
    return
  }
  
  try {
    const res = await getQuestionById(questionId)
    if (res && res.data) {
      standardQuestion.value = res.data
      console.log('获取标准问题成功:', standardQuestion.value)
    }
  } catch (error) {
    console.error('获取标准问题失败:', error)
  }
}

// 获取答案列表
const getAnswers = async () => {
  if (!taskId.value || taskId.value <= 0) {
    console.error('任务ID无效，无法获取答案列表')
    ElMessage.error('无效的任务ID，无法获取答案列表')
    loading.value = false
    return
  }
  
  loading.value = true
  try {
    const params = { 
      ...queryParams,
      page: queryParams.page - 1 // 前端页码从1开始，后端从0开始
    }
    
    console.log('API调用: 获取任务答案列表，任务ID:', taskId.value)
    console.log('获取答案列表，参数:', params)
    const res = await getAnswersByTaskId(taskId.value, params)
    
    if (res && res.data) {
      answerList.value = res.data.content || []
      total.value = res.data.totalElements || 0
      console.log(`获取到答案列表, 共${total.value}条记录:`, answerList.value)
    } else {
      console.error('获取答案列表失败，返回数据异常')
      answerList.value = []
      total.value = 0
    }
  } catch (error) {
    console.error('获取答案列表失败:', error)
    answerList.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

// 格式化状态
const formatStatus = (status: string) => {
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
  const map: Record<string, string> = {
    'DRAFT': 'info',
    'PUBLISHED': 'primary',
    'COMPLETED': 'success',
    'CLOSED': 'danger'
  }
  return map[status] || ''
}

// 格式化答案状态
const formatAnswerStatus = (status: string) => {
  const map: Record<string, string> = {
    'SUBMITTED': '待审核',
    'APPROVED': '已批准',
    'REJECTED': '已拒绝',
    'PROMOTED': '已提升'
  }
  return map[status] || status
}

// 获取答案状态标签类型
const getAnswerStatusTag = (status: string) => {
  const map: Record<string, string> = {
    'SUBMITTED': 'info',
    'APPROVED': 'success',
    'REJECTED': 'danger',
    'PROMOTED': 'warning'
  }
  return map[status] || ''
}

// 格式化日期时间
const formatDateTime = (dateTimeStr: string) => {
  if (!dateTimeStr) return '';
  const date = new Date(dateTimeStr);
  return date.toLocaleString();
}

// 截断文本
const truncateText = (text: string, length: number) => {
  if (!text) return '';
  if (text.length <= length) return text;
  return text.substring(0, length) + '...';
}

// 筛选
const handleFilter = () => {
  queryParams.page = 1
  getAnswers()
}

// 重置筛选
const resetFilter = () => {
  queryParams.status = undefined
  queryParams.page = 1
  queryParams.size = 10
  getAnswers()
}

// 每页数量变化
const handleSizeChange = (val: number) => {
  queryParams.size = val
  getAnswers()
}

// 当前页变化
const handleCurrentChange = (val: number) => {
  queryParams.page = val
  getAnswers()
}

// 处理批准
const handleApprove = (row: any) => {
  reviewType.value = 'approve'
  reviewForm.answerId = row.answerId
  reviewForm.contributorName = row.contributorName || '匿名'
  reviewForm.contributorEmail = row.contributorEmail || ''
  reviewForm.occupation = row.occupation || ''
  reviewForm.expertise = row.expertise || ''
  reviewForm.answerText = row.answerText
  reviewForm.references = row.references || ''
  reviewForm.comment = ''
  reviewForm.qualityScore = 3
  reviewDialogVisible.value = true
}

// 处理拒绝
const handleReject = (row: any) => {
  reviewType.value = 'reject'
  reviewForm.answerId = row.answerId
  reviewForm.contributorName = row.contributorName || '匿名'
  reviewForm.contributorEmail = row.contributorEmail || ''
  reviewForm.occupation = row.occupation || ''
  reviewForm.expertise = row.expertise || ''
  reviewForm.answerText = row.answerText
  reviewForm.references = row.references || ''
  reviewForm.comment = ''
  reviewForm.qualityScore = 0
  reviewDialogVisible.value = true
}

// 提交审核
const submitReview = async () => {
  if (!reviewForm.answerId) {
    ElMessage.error('答案ID无效')
    return
  }
  
  submitting.value = true
  try {
    const isApproved = reviewType.value === 'approve'
    const qualityScore = isApproved ? reviewForm.qualityScore : 0
    
    await reviewAnswer(
      reviewForm.answerId, 
      isApproved, 
      reviewForm.comment,
      qualityScore
    )
    
    ElMessage.success(`答案${isApproved ? '批准' : '拒绝'}成功`)
    reviewDialogVisible.value = false
    getAnswers() // 刷新答案列表
    getTaskInfo() // 刷新任务信息
  } catch (error) {
    console.error('审核答案失败:', error)
    ElMessage.error('审核失败，请重试')
  } finally {
    submitting.value = false
  }
}

// 提升为标准答案
const handlePromote = (row: any) => {
  if (!row.answerId) {
    ElMessage.error('答案ID无效')
    return
  }
  
  ElMessageBox.confirm(
    `确认将该答案提升为标准答案吗？`,
    '提升确认',
    {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      console.log(`开始提升答案 ID: ${row.answerId} 为标准答案`)
      const response = await promoteToStandardAnswer(row.answerId) as any
      console.log('提升答案响应完整数据(stringify):', JSON.stringify(response, null, 2))
      console.log('提升答案响应完整数据(原始):', response)
      
      // 尝试所有可能的数据结构形式来获取标准答案信息
      let standardAnswer: any = null
      
      // 情况1: response直接就是数据
      if (response && typeof response === 'object' && response.standardAnswerId) {
        console.log('情况1: response直接是数据')
        standardAnswer = response
      } 
      // 情况2: response.data中包含数据
      else if (response && response.data && typeof response.data === 'object' && response.data.standardAnswerId) {
        console.log('情况2: response.data包含数据')
        standardAnswer = response.data
      }
      // 情况3: 尝试解析JSON字符串
      else if (typeof response === 'string') {
        console.log('情况3: 尝试解析JSON字符串')
        try {
          const parsed = JSON.parse(response)
          if (parsed && parsed.standardAnswerId) {
            standardAnswer = parsed
          }
        } catch(e) {
          console.error('解析响应JSON失败:', e)
        }
      }
      
      console.log('处理后的标准答案对象:', standardAnswer)
      
      if (standardAnswer) {
        // 检查时间字段
        console.log('创建时间字段值:', standardAnswer.createdAt)
        console.log('更新时间字段值:', standardAnswer.updatedAt)
        
        // 手动格式化时间
        let createdTime = '未知'
        if (standardAnswer.createdAt) {
          try {
            createdTime = new Date(standardAnswer.createdAt).toLocaleString()
            console.log('格式化后的创建时间:', createdTime)
          } catch (e) {
            console.error('格式化时间出错:', e)
          }
        }
        
        const answerId = standardAnswer.standardAnswerId || '未知'
        ElMessage.success(`答案已提升为标准答案，标准答案ID: ${answerId}，创建时间: ${createdTime}，答案ID: ${row.answerId}，版本: ${standardAnswer.version || '无'}`)
      } else {
        console.error('无法获取有效的标准答案数据')
        ElMessage.success('答案已提升为标准答案')
      }
      
      getAnswers() // 刷新答案列表
    } catch (error) {
      console.error('提升答案失败:', error)
      ElMessage.error('提升失败，请重试')
    }
  }).catch(() => {})
}

// 查看答案详情
const viewAnswerDetails = (answer: any) => {
  currentAnswer.value = { ...answer }
  detailDialogVisible.value = true
}

// 返回上一页
const goBack = () => {
  router.back()
}

// 监听路由参数变化
watch(() => route.params.taskId, (newId) => {
  if (newId) {
    console.log('检测到路由参数变化:', newId)
    if (initTaskId()) {
      getTaskInfo()
      getAnswers()
    }
  }
}, { immediate: false })

// 组件挂载时初始化
onMounted(() => {
  console.log('答案列表组件挂载，路由参数:', route.params)
  console.log('任务ID参数类型:', typeof route.params.taskId)
  console.log('任务ID参数值:', route.params.taskId)
  
  if (initTaskId()) {
    getTaskInfo()
    getAnswers()
  } else {
    // 如果任务ID无效，返回任务列表页面
    router.push('/crowdsourcing')
  }
})
</script>

<style scoped>
.answer-list-container {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.task-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.task-info {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
  margin-bottom: 20px;
}

.info-item {
  display: flex;
  align-items: flex-start;
  gap: 8px;
}

.label {
  font-weight: bold;
  color: #606266;
}

.question-info {
  margin-top: 10px;
}

.question-content {
  white-space: pre-wrap;
  padding: 10px;
  background-color: #f8f9fa;
  border-radius: 4px;
  border-left: 4px solid #409EFF;
}

.filter-actions {
  display: flex;
  gap: 10px;
}

.answer-content {
  max-width: 400px;
}

.answer-popover-content {
  max-height: 300px;
  overflow-y: auto;
}

.answer-popover-content p {
  margin-bottom: 10px;
}

.review-answer-content {
  white-space: pre-wrap;
  max-height: 150px;
  overflow-y: auto;
  padding: 10px;
  background-color: #f8f9fa;
  border-radius: 4px;
  margin-bottom: 15px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.answer-detail-container {
  padding: 10px;
}

.detail-section {
  margin-bottom: 20px;
}

.detail-section h3 {
  margin-bottom: 15px;
  font-size: 16px;
  color: #303133;
  font-weight: 500;
}

.detail-item {
  margin-bottom: 10px;
  display: flex;
}

.detail-label {
  font-weight: 500;
  width: 100px;
  color: #606266;
}

.answer-content-box {
  background-color: #f8f9fa;
  padding: 15px;
  border-radius: 4px;
  border-left: 4px solid #409EFF;
  white-space: pre-wrap;
}

.comment-box {
  background-color: #f0f9eb;
  padding: 10px;
  border-radius: 4px;
  border-left: 4px solid #67c23a;
}
</style> 