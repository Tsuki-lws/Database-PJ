<template>
  <div class="task-list-container">
    <div class="page-header">
      <h2>众包任务列表</h2>
      <el-button type="primary" @click="navigateToCreate">
        <el-icon><Plus /></el-icon>创建任务
      </el-button>
    </div>

    <!-- 搜索和筛选 -->
    <el-card shadow="never" class="filter-container">
      <el-form :model="queryParams" label-width="80px" :inline="true">
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="全部状态" clearable>
            <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 任务列表 -->
    <el-card shadow="never" class="list-card">
      <el-table v-loading="loading" :data="taskList" style="width: 100%">
        <el-table-column prop="taskId" label="ID" width="80" />
        <el-table-column prop="title" label="任务标题" show-overflow-tooltip>
          <template #default="scope">
            <el-link type="primary" @click="navigateToDetail(scope.row.taskId)">
              {{ scope.row.title }}
            </el-link>
          </template>
        </el-table-column>
        <el-table-column prop="taskType" label="任务类型" width="120">
          <template #default="scope">
            <el-tag :type="getTaskTypeTag(scope.row.taskType)">
              {{ formatTaskType(scope.row.taskType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="questionCount" label="问题数量" width="100" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusTag(scope.row.status)">
              {{ formatStatus(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="progress" label="进度" width="200">
          <template #default="scope">
            <el-progress 
              :percentage="calculateProgress(scope.row)" 
              :status="getProgressStatus(scope.row.status)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column fixed="right" label="操作" width="250">
          <template #default="scope">
            <el-button link type="primary" @click="navigateToDetail(scope.row.taskId)">查看</el-button>
            <el-button link type="primary" @click="navigateToAnswers(scope.row.taskId)">查看答案</el-button>
            <el-button 
              v-if="scope.row.status === 'ongoing'"
              link 
              type="success" 
              @click="navigateToCollection(scope.row.taskId)"
            >
              参与收集
            </el-button>
            <el-button 
              v-if="scope.row.status === 'draft'" 
              link 
              type="success" 
              @click="handlePublish(scope.row)"
            >
              发布
            </el-button>
            <el-button 
              v-if="scope.row.status === 'ongoing'" 
              link 
              type="warning" 
              @click="handleComplete(scope.row)"
            >
              完成
            </el-button>
            <el-button 
              v-if="scope.row.status === 'draft'" 
              link 
              type="danger" 
              @click="handleDelete(scope.row)"
            >
              删除
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
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getTaskList, updateTaskStatus } from '@/api/crowdsourcing'

const router = useRouter()
const loading = ref(false)
const taskList = ref([])
const total = ref(0)

// 查询参数
const queryParams = reactive({
  page: 1,
  size: 10,
  status: undefined
})

// 状态选项
const statusOptions = [
  { value: 'draft', label: '草稿' },
  { value: 'ongoing', label: '进行中' },
  { value: 'completed', label: '已完成' },
  { value: 'cancelled', label: '已取消' }
]

// 获取任务列表
const getList = async () => {
  loading.value = true
  try {
    const res = await getTaskList(queryParams)
    taskList.value = res.list || []
    total.value = res.total || 0
  } catch (error) {
    console.error('获取任务列表失败', error)
  } finally {
    loading.value = false
  }
}

// 格式化任务类型
const formatTaskType = (type: string) => {
  const map: Record<string, string> = {
    'answer_collection': '答案收集',
    'answer_review': '答案审核',
    'answer_rating': '答案评分'
  }
  return map[type] || type
}

// 获取任务类型标签类型
const getTaskTypeTag = (type: string) => {
  const map: Record<string, string> = {
    'answer_collection': 'primary',
    'answer_review': 'success',
    'answer_rating': 'warning'
  }
  return map[type] || ''
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

// 计算进度
const calculateProgress = (task: any) => {
  if (task.status === 'completed') {
    return 100
  }
  if (task.requiredAnswers === 0) {
    return 0
  }
  return Math.min(Math.floor((task.receivedAnswers / task.requiredAnswers) * 100), 100)
}

// 获取进度条状态
const getProgressStatus = (status: string) => {
  const map: Record<string, string> = {
    'draft': '',
    'ongoing': 'primary',
    'completed': 'success',
    'cancelled': 'exception'
  }
  return map[status] || ''
}

// 搜索
const handleSearch = () => {
  queryParams.page = 1
  getList()
}

// 重置查询条件
const resetQuery = () => {
  queryParams.status = undefined
  handleSearch()
}

// 每页数量变化
const handleSizeChange = (val: number) => {
  queryParams.size = val
  getList()
}

// 当前页变化
const handleCurrentChange = (val: number) => {
  queryParams.page = val
  getList()
}

// 发布任务
const handlePublish = (row: any) => {
  ElMessageBox.confirm(
    `确认发布任务 "${row.title}" 吗？`,
    '发布确认',
    {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'info'
    }
  ).then(async () => {
    try {
      await updateTaskStatus(row.taskId, 'ongoing')
      ElMessage.success('发布成功')
      getList()
    } catch (error) {
      console.error('发布任务失败', error)
    }
  }).catch(() => {})
}

// 完成任务
const handleComplete = (row: any) => {
  ElMessageBox.confirm(
    `确认完成任务 "${row.title}" 吗？`,
    '完成确认',
    {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await updateTaskStatus(row.taskId, 'completed')
      ElMessage.success('任务已完成')
      getList()
    } catch (error) {
      console.error('完成任务失败', error)
    }
  }).catch(() => {})
}

// 删除任务
const handleDelete = (row: any) => {
  ElMessageBox.confirm(
    `确认删除任务 "${row.title}" 吗？`,
    '删除确认',
    {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      // 这里应该调用删除API
      ElMessage.success('删除成功')
      getList()
    } catch (error) {
      console.error('删除任务失败', error)
    }
  }).catch(() => {})
}

// 导航到创建页面
const navigateToCreate = () => {
  router.push('/crowdsourcing/create')
}

// 导航到详情页面
const navigateToDetail = (id: number) => {
  router.push(`/crowdsourcing/detail/${id}`)
}

// 导航到答案列表页面
const navigateToAnswers = (id: number) => {
  router.push(`/crowdsourcing/answers/${id}`)
}

// 导航到收集页面
const navigateToCollection = (id: number) => {
  router.push(`/crowdsourcing/collection/${id}`)
}

onMounted(() => {
  getList()
})
</script>

<style scoped>
.task-list-container {
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
</style> 