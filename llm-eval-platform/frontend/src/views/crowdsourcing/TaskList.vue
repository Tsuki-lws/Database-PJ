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
        <el-table-column prop="standardQuestionId" label="标准问题ID" width="120" />
        <el-table-column prop="requiredAnswers" label="所需答案" width="100" />
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
            >
              <span>
                {{ scope.row.submittedAnswerCount || scope.row.currentAnswers || 0 }}/{{ scope.row.requiredAnswers }}
              </span>
            </el-progress>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180">
          <template #default="scope">
            {{ formatDateTime(scope.row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column fixed="right" label="操作" width="250">
          <template #default="scope">
            <el-button link type="primary" @click="navigateToDetail(scope.row.taskId)">查看</el-button>
            <el-button link type="primary" @click="navigateToAnswers(scope.row.taskId)">查看答案</el-button>
            <el-button 
              v-if="scope.row.status === 'PUBLISHED'"
              link 
              type="success" 
              @click="navigateToCollection(scope.row.taskId)"
            >
              参与收集
            </el-button>
            <el-button 
              v-if="scope.row.status === 'DRAFT'" 
              link 
              type="success" 
              @click="handlePublish(scope.row)"
            >
              发布
            </el-button>
            <el-button 
              v-if="scope.row.status === 'PUBLISHED'" 
              link 
              type="warning" 
              @click="handleComplete(scope.row)"
            >
              完成
            </el-button>
            <el-button 
              v-if="scope.row.status === 'DRAFT'" 
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
import { getPagedTaskList, publishTask, completeTask, deleteTask } from '@/api/crowdsourcing'
import type { PageResult, QueryParams } from '@/api/crowdsourcing'

const router = useRouter()
const loading = ref(false)
const taskList = ref<any[]>([])
const total = ref(0)

// 查询参数
const queryParams = reactive<QueryParams>({
  page: 1,
  size: 10,
  status: undefined,
  sort: 'taskId',
  direction: 'DESC'
})

// 状态选项
const statusOptions = [
  { value: 'DRAFT', label: '草稿' },
  { value: 'PUBLISHED', label: '已发布' },
  { value: 'COMPLETED', label: '已完成' },
  { value: 'CLOSED', label: '已关闭' }
]

// 获取任务列表
const getList = async () => {
  loading.value = true
  taskList.value = []
  total.value = 0
  
  try {
    console.log('开始获取任务列表，当前查询参数:', queryParams)
    
    // 构建请求参数，确保参数类型正确
    const params: Record<string, any> = {
      page: Number(queryParams.page) - 1, // 前端页码从1开始，后端从0开始，需要转换
      size: Number(queryParams.size)
    }
    
    // 只有当状态不为空时才添加状态参数
    if (queryParams.status) {
      params.status = queryParams.status
    }
    
    console.log('发送请求参数:', params)
    
    // 发送请求
    const response = await getPagedTaskList(params as QueryParams)
    console.log('获取任务列表响应:', response)
    
    // 处理响应数据 - 后端已修改，不再有多余的包装层
    if (response && response.data) {
      // 直接处理分页数据
      taskList.value = response.data.content || []
      total.value = response.data.totalElements || 0
      console.log(`成功获取任务列表，共 ${total.value} 条记录`)
    } else {
      console.error('响应数据格式不符合预期:', response)
      ElMessage.error('获取任务列表失败：响应数据格式不正确')
    }
  } catch (error) {
    console.error('获取任务列表失败:', error)
    ElMessage.error('获取任务列表失败，请查看控制台了解详情')
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

// 计算进度
const calculateProgress = (task: any) => {
  if (task.status === 'COMPLETED' || task.status === 'CLOSED') {
    return 100
  }
  if (task.requiredAnswers === 0) {
    return 0
  }
  
  // 使用已提交的答案数量
  const currentAnswers = task.submittedAnswerCount || task.currentAnswers || 0
  return Math.min(Math.floor((currentAnswers / task.requiredAnswers) * 100), 100)
}

// 获取进度条状态
const getProgressStatus = (status: string) => {
  const map: Record<string, string> = {
    'DRAFT': '',
    'PUBLISHED': 'primary',
    'COMPLETED': 'success',
    'CLOSED': 'exception'
  }
  return map[status] || ''
}

// 格式化日期时间
const formatDateTime = (dateTimeStr: string) => {
  if (!dateTimeStr) return '';
  const date = new Date(dateTimeStr);
  return date.toLocaleString();
}

// 搜索
const handleSearch = () => {
  queryParams.page = 1
  getList()
}

// 重置查询条件
const resetQuery = () => {
  queryParams.status = undefined
  queryParams.page = 1
  queryParams.size = 10
  queryParams.sort = 'taskId'
  queryParams.direction = 'DESC'
  getList()
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
      await publishTask(row.taskId)
      ElMessage.success('任务发布成功')
      getList()
    } catch (error) {
      console.error('发布任务失败', error)
      ElMessage.error('发布失败，请重试')
    }
  }).catch(() => {})
}

// 完成任务
const handleComplete = (row: any) => {
  ElMessageBox.confirm(
    `确认完成任务 "${row.title}" 吗？完成后将不再接受新的答案。`,
    '完成确认',
    {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await completeTask(row.taskId)
      ElMessage.success('任务已完成')
      getList()
    } catch (error) {
      console.error('完成任务失败', error)
      ElMessage.error('操作失败，请重试')
    }
  }).catch(() => {})
}

// 删除任务
const handleDelete = (row: any) => {
  ElMessageBox.confirm(
    `确认删除任务 "${row.title}" 吗？此操作不可逆。`,
    '删除确认',
    {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await deleteTask(row.taskId)
      ElMessage.success('任务删除成功')
      getList()
    } catch (error) {
      console.error('删除任务失败', error)
      ElMessage.error('删除失败，请重试')
    }
  }).catch(() => {})
}

// 页面导航
const navigateToCreate = () => {
  router.push('/crowdsourcing/create')
}

const navigateToDetail = (id: number) => {
  router.push(`/crowdsourcing/detail/${id}`)
}

const navigateToAnswers = (id: number) => {
  router.push(`/crowdsourcing/answers/${id}`)
}

const navigateToCollection = (id: number) => {
  console.log('跳转到收集页面，任务ID:', id)
  if (!id || isNaN(id)) {
    console.error('无效的任务ID:', id)
    ElMessage.error('无效的任务ID，无法跳转')
    return
  }
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

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style> 