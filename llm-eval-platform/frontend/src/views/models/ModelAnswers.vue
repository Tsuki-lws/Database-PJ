<template>
  <div class="model-answers-container">
    <div class="page-header">
      <h2>模型回答管理</h2>
      <div>
        <el-button type="primary" @click="navigateTo('/model-answers/import')">导入模型回答</el-button>
      </div>
    </div>

    <el-card shadow="never" class="filter-container">
      <el-form :model="queryParams" label-width="80px" :inline="true">
        <el-form-item label="数据集名称">
          <el-input v-model="queryParams.name" placeholder="数据集名称关键词" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.isPublished" placeholder="发布状态" clearable>
            <el-option label="已发布" :value="true" />
            <el-option label="未发布" :value="false" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="list-card">
      <el-table v-loading="loading" :data="datasetList" style="width: 100%">
        <el-table-column prop="datasetId" label="ID" width="80" />
        <el-table-column prop="name" label="数据集名称" show-overflow-tooltip />
        <el-table-column prop="version" label="版本" width="80" />
        <el-table-column prop="isPublished" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.isPublished ? 'success' : 'info'">
              {{ scope.row.isPublished ? '已发布' : '未发布' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="questionCount" label="问题数量" width="100" />
        <el-table-column prop="answerCount" label="回答数量" width="100" />
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column fixed="right" label="操作" width="150">
          <template #default="scope">
            <el-button link type="primary" @click="viewDatasetQuestions(scope.row)">查看问题</el-button>
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
import { ElMessage } from 'element-plus'
import { getDatasetVersionsWithAnswers } from '@/api/import'

// 辅助类型定义
interface DatasetInfo {
  datasetId: number
  name: string
  version: number
  isPublished: boolean
  questionCount: number
  answerCount: number
  createdAt: string
  [key: string]: any
}

const router = useRouter()
const loading = ref(false)
const datasetList = ref<DatasetInfo[]>([])
const total = ref(0)

// 查询参数
const queryParams = reactive({
  page: 1,
  size: 10,
  name: '',
  isPublished: undefined as boolean | undefined
})

// 初始化
onMounted(() => {
  fetchData()
})

// 获取数据集列表
const fetchData = async () => {
  loading.value = true
  try {
    const res = await getDatasetVersionsWithAnswers()
    
    // 处理后端不同格式的响应
    const responseData = res.data || res
    
    if (Array.isArray(responseData)) {
      // 过滤数据 - 只保留已发布的数据集
      let filteredData = [...responseData].filter(item => item.isPublished === true)
      
      if (queryParams.name) {
        const keyword = queryParams.name.toLowerCase()
        filteredData = filteredData.filter(item => 
          item.name && item.name.toLowerCase().includes(keyword))
      }
      
      // 手动分页
      const start = (queryParams.page - 1) * queryParams.size
      const end = start + queryParams.size
      
      datasetList.value = filteredData.slice(start, end)
      total.value = filteredData.length
    } else {
      datasetList.value = []
      total.value = 0
      console.error('未能识别的响应格式:', res)
    }
  } catch (error: any) {
    ElMessage.error('获取数据集列表失败: ' + error.message)
    datasetList.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  queryParams.page = 1
  fetchData()
}

// 重置查询
const resetQuery = () => {
  queryParams.name = ''
  queryParams.isPublished = undefined
  queryParams.page = 1
  fetchData()
}

// 处理页码变化
const handleCurrentChange = (val: number) => {
  queryParams.page = val
  fetchData()
}

// 处理每页大小变化
const handleSizeChange = (val: number) => {
  queryParams.size = val
  queryParams.page = 1
  fetchData()
}

// 查看数据集问题
const viewDatasetQuestions = (dataset: DatasetInfo) => {
  router.push({
    path: `/model-answers/dataset/${dataset.datasetId}`,
    query: { name: dataset.name }
  })
}

// 导航
const navigateTo = (path: string) => {
  router.push(path)
}
</script>

<style scoped>
.model-answers-container {
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
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style> 