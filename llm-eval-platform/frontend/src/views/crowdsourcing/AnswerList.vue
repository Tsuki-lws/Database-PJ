<template>
  <div class="answer-list-container">
    <div class="page-header">
      <h2>众包答案列表</h2>
      <el-button @click="navigateBack">返回任务</el-button>
    </div>

    <el-card shadow="never" class="task-info-card" v-loading="taskLoading">
      <div class="task-header">
        <h3>{{ task.title }}</h3>
        <el-tag :type="getStatusTag(task.status)">{{ formatStatus(task.status) }}</el-tag>
      </div>
      <p class="task-description">{{ task.description }}</p>
      <div class="task-meta">
        <span>问题数量: {{ task.questionCount || 0 }}</span>
        <span>每题答案数量: {{ task.minAnswersPerQuestion }}</span>
        <span>答案总数: {{ totalAnswers }}</span>
      </div>
    </el-card>

    <!-- 搜索和筛选 -->
    <el-card shadow="never" class="filter-container">
      <el-form :model="queryParams" label-width="80px" :inline="true">
        <el-form-item label="问题ID">
          <el-input v-model="queryParams.questionId" placeholder="问题ID" clearable @keyup.enter="handleSearch"></el-input>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.reviewStatus" placeholder="全部状态" clearable>
            <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 答案列表 -->
    <el-card shadow="never" class="list-card">
      <el-table v-loading="loading" :data="answerList" style="width: 100%">
        <el-table-column prop="answerId" label="ID" width="80" />
        <el-table-column prop="standardQuestionId" label="问题ID" width="100" />
        <el-table-column prop="answerText" label="答案内容" show-overflow-tooltip>
          <template #default="scope">
            <el-popover
              placement="top-start"
              title="答案内容"
              :width="400"
              trigger="hover"
              :content="scope.row.answerText">
              <template #reference>
                <span>{{ truncateText(scope.row.answerText, 100) }}</span>
              </template>
            </el-popover>
          </template>
        </el-table-column>
        <el-table-column prop="qualityScore" label="质量评分" width="100">
          <template #default="scope">
            <el-rate
              v-if="scope.row.qualityScore"
              v-model="scope.row.qualityScore"
              :max="10"
              :colors="['#99A9BF', '#F7BA2A', '#FF9900']"
              disabled
              show-score>
            </el-rate>
            <span v-else>未评分</span>
          </template>
        </el-table-column>
        <el-table-column prop="reviewStatus" label="审核状态" width="120">
          <template #default="scope">
            <el-tag :type="getReviewStatusTag(scope.row.reviewStatus)">
              {{ formatReviewStatus(scope.row.reviewStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="isSelected" label="是否选中" width="100">
          <template #default="scope">
            <el-tag v-if="scope.row.isSelected" type="success">已选中</el-tag>
            <el-tag v-else type="info">未选中</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="submissionTime" label="提交时间" width="180" />
        <el-table-column fixed="right" label="操作" width="280">
          <template #default="scope">
            <el-button 
              v-if="scope.row.reviewStatus === 'pending'" 
              link 
              type="success" 
              @click="handleApprove(scope.row)">
              通过
            </el-button>
            <el-button 
              v-if="scope.row.reviewStatus === 'pending'" 
              link 
              type="danger" 
              @click="handleReject(scope.row)">
              拒绝
            </el-button>
            <el-button 
              v-if="scope.row.reviewStatus === 'approved' && !scope.row.isSelected" 
              link 
              type="primary" 
              @click="handleSelect(scope.row)">
              选为标准答案
            </el-button>
            <el-button 
              link 
              type="primary" 
              @click="handleRate(scope.row)">
              评分
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

    <!-- 评分对话框 -->
    <el-dialog v-model="rateDialogVisible" title="评分" width="500px">
      <el-form :model="rateForm" label-width="100px">
        <el-form-item label="答案ID">
          <span>{{ rateForm.answerId }}</span>
        </el-form-item>
        <el-form-item label="答案内容">
          <div class="answer-content">{{ rateForm.answerText }}</div>
        </el-form-item>
        <el-form-item label="质量评分">
          <el-rate
            v-model="rateForm.score"
            :max="10"
            :colors="['#99A9BF', '#F7BA2A', '#FF9900']"
            show-score>
          </el-rate>
        </el-form-item>
        <el-form-item label="评分说明">
          <el-input 
            v-model="rateForm.comment" 
            type="textarea" 
            :rows="3" 
            placeholder="请输入评分说明">
          </el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="rateDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitRate">确认</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 审核对话框 -->
    <el-dialog v-model="reviewDialogVisible" :title="reviewForm.action === 'approve' ? '通过审核' : '拒绝审核'" width="500px">
      <el-form :model="reviewForm" label-width="100px">
        <el-form-item label="答案ID">
          <span>{{ reviewForm.answerId }}</span>
        </el-form-item>
        <el-form-item label="答案内容">
          <div class="answer-content">{{ reviewForm.answerText }}</div>
        </el-form-item>
        <el-form-item label="审核意见">
          <el-input 
            v-model="reviewForm.comment" 
            type="textarea" 
            :rows="3" 
            placeholder="请输入审核意见">
          </el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="reviewDialogVisible = false">取消</el-button>
          <el-button 
            :type="reviewForm.action === 'approve' ? 'success' : 'danger'" 
            @click="submitReview">
            {{ reviewForm.action === 'approve' ? '通过' : '拒绝' }}
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 选为标准答案对话框 -->
    <el-dialog v-model="selectDialogVisible" title="选为标准答案" width="500px">
      <el-form :model="selectForm" label-width="100px">
        <el-form-item label="答案ID">
          <span>{{ selectForm.answerId }}</span>
        </el-form-item>
        <el-form-item label="答案内容">
          <div class="answer-content">{{ selectForm.answerText }}</div>
        </el-form-item>
        <el-form-item label="选择原因">
          <el-input 
            v-model="selectForm.reason" 
            type="textarea" 
            :rows="3" 
            placeholder="请输入选择该答案作为标准答案的原因">
          </el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="selectDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitSelect">确认</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { 
  getTaskById, 
  getAnswersByTaskId, 
  reviewAnswer, 
  selectAsStandardAnswer, 
  rateAnswer 
} from '@/api/crowdsourcing'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const taskLoading = ref(false)
const task = ref<any>({})
const answerList = ref<any[]>([])
const total = ref(0)

// 查询参数
const queryParams = reactive({
  page: 1,
  size: 10,
  questionId: undefined,
  reviewStatus: undefined
})

// 状态选项
const statusOptions = [
  { value: 'pending', label: '待审核' },
  { value: 'approved', label: '已通过' },
  { value: 'rejected', label: '已拒绝' }
]

// 评分对话框
const rateDialogVisible = ref(false)
const rateForm = reactive({
  answerId: 0,
  answerText: '',
  score: 0,
  comment: ''
})

// 审核对话框
const reviewDialogVisible = ref(false)
const reviewForm = reactive({
  answerId: 0,
  answerText: '',
  action: 'approve', // 'approve' 或 'reject'
  comment: ''
})

// 选为标准答案对话框
const selectDialogVisible = ref(false)
const selectForm = reactive({
  answerId: 0,
  answerText: '',
  reason: ''
})

// 计算总答案数
const totalAnswers = computed(() => {
  return total.value
})

// 获取任务详情
const getTaskDetail = async () => {
  const taskId = Number(route.params.taskId)
  if (!taskId) return
  
  taskLoading.value = true
  try {
    task.value = await getTaskById(taskId)
  } catch (error) {
    console.error('获取任务详情失败', error)
  } finally {
    taskLoading.value = false
  }
}

// 获取答案列表
const getAnswers = async () => {
  const taskId = Number(route.params.taskId)
  if (!taskId) return
  
  loading.value = true
  try {
    const params = {
      ...queryParams,
      taskId
    }
    const res = await getAnswersByTaskId(taskId, params)
    answerList.value = res.list || []
    total.value = res.total || 0
  } catch (error) {
    console.error('获取答案列表失败', error)
  } finally {
    loading.value = false
  }
}

// 格式化状态
const formatStatus = (status: string) => {
  const map: Record<string, string> = {
    'draft': '草稿',
    'ongoing': '进行中',
    'completed': '已完成',
    'cancelled': '已取消'
  }
  return map[status] || status
}

// 获取状态标签类型
const getStatusTag = (status: string) => {
  const map: Record<string, string> = {
    'draft': 'info',
    'ongoing': 'primary',
    'completed': 'success',
    'cancelled': 'danger'
  }
  return map[status] || ''
}

// 格式化审核状态
const formatReviewStatus = (status: string) => {
  const map: Record<string, string> = {
    'pending': '待审核',
    'approved': '已通过',
    'rejected': '已拒绝'
  }
  return map[status] || status
}

// 获取审核状态标签类型
const getReviewStatusTag = (status: string) => {
  const map: Record<string, string> = {
    'pending': 'warning',
    'approved': 'success',
    'rejected': 'danger'
  }
  return map[status] || ''
}

// 截断文本
const truncateText = (text: string, length: number) => {
  if (!text) return ''
  return text.length > length ? text.substring(0, length) + '...' : text
}

// 搜索
const handleSearch = () => {
  queryParams.page = 1
  getAnswers()
}

// 重置查询条件
const resetQuery = () => {
  queryParams.questionId = undefined
  queryParams.reviewStatus = undefined
  handleSearch()
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

// 通过审核
const handleApprove = (row: any) => {
  reviewForm.answerId = row.answerId
  reviewForm.answerText = row.answerText
  reviewForm.action = 'approve'
  reviewForm.comment = ''
  reviewDialogVisible.value = true
}

// 拒绝审核
const handleReject = (row: any) => {
  reviewForm.answerId = row.answerId
  reviewForm.answerText = row.answerText
  reviewForm.action = 'reject'
  reviewForm.comment = ''
  reviewDialogVisible.value = true
}

// 提交审核
const submitReview = async () => {
  try {
    await reviewAnswer(reviewForm.answerId, {
      reviewStatus: reviewForm.action === 'approve' ? 'approved' : 'rejected',
      reviewerId: 1, // 假设当前用户ID为1
      reviewComment: reviewForm.comment
    })
    ElMessage.success('审核操作成功')
    reviewDialogVisible.value = false
    getAnswers()
  } catch (error) {
    console.error('审核操作失败', error)
  }
}

// 选为标准答案
const handleSelect = (row: any) => {
  selectForm.answerId = row.answerId
  selectForm.answerText = row.answerText
  selectForm.reason = ''
  selectDialogVisible.value = true
}

// 提交选择标准答案
const submitSelect = async () => {
  try {
    await selectAsStandardAnswer(selectForm.answerId, {
      selectedBy: 1, // 假设当前用户ID为1
      selectionReason: selectForm.reason
    })
    ElMessage.success('已选为标准答案')
    selectDialogVisible.value = false
    getAnswers()
  } catch (error) {
    console.error('选择标准答案失败', error)
  }
}

// 评分
const handleRate = (row: any) => {
  rateForm.answerId = row.answerId
  rateForm.answerText = row.answerText
  rateForm.score = row.qualityScore || 0
  rateForm.comment = ''
  rateDialogVisible.value = true
}

// 提交评分
const submitRate = async () => {
  try {
    await rateAnswer(rateForm.answerId, {
      raterId: 1, // 假设当前用户ID为1
      score: rateForm.score,
      comment: rateForm.comment
    })
    ElMessage.success('评分成功')
    rateDialogVisible.value = false
    getAnswers()
  } catch (error) {
    console.error('评分失败', error)
  }
}

// 返回任务详情
const navigateBack = () => {
  router.push(`/crowdsourcing/detail/${route.params.taskId}`)
}

onMounted(() => {
  getTaskDetail()
  getAnswers()
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

.task-info-card {
  margin-bottom: 20px;
}

.task-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.task-header h3 {
  margin: 0;
  font-size: 18px;
}

.task-description {
  margin-bottom: 10px;
  color: #606266;
}

.task-meta {
  display: flex;
  gap: 20px;
  color: #606266;
  font-size: 14px;
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
  max-height: 200px;
  overflow-y: auto;
  padding: 10px;
  background-color: #f5f7fa;
  border-radius: 4px;
  white-space: pre-wrap;
}
</style> 