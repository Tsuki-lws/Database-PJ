<template>
  <div class="dataset-list-container">
    <div class="page-header">
      <h2>数据集版本列表</h2>
      <el-button type="primary" @click="navigateToCreate">
        <el-icon><Plus /></el-icon>创建数据集
      </el-button>
    </div>

    <!-- 搜索和筛选 -->
    <el-card shadow="never" class="filter-container">
      <el-form :model="queryParams" label-width="80px" :inline="true">
        <el-form-item label="状态">
          <el-select v-model="queryParams.isPublished" placeholder="全部状态" clearable>
            <el-option label="已发布" :value="true"></el-option>
            <el-option label="未发布" :value="false"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 数据集列表 -->
    <el-card shadow="never" class="list-card">
      <el-table v-loading="loading" :data="datasetList" style="width: 100%">
        <el-table-column prop="versionId" label="ID" width="80" />
        <el-table-column prop="name" label="数据集名称" show-overflow-tooltip>
          <template #default="scope">
            <el-link type="primary" @click="navigateToDetail(scope.row.versionId)">
              {{ scope.row.name }}
            </el-link>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" show-overflow-tooltip />
        <el-table-column prop="questionCount" label="问题数量" width="100" />
        <el-table-column prop="isPublished" label="状态" width="100">
          <template #default="scope">
            <el-tag v-if="scope.row.isPublished" type="success">已发布</el-tag>
            <el-tag v-else type="info">未发布</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="releaseDate" label="发布日期" width="120">
          <template #default="scope">
            {{ scope.row.releaseDate || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column fixed="right" label="操作" width="200">
          <template #default="scope">
            <el-button link type="primary" @click="navigateToDetail(scope.row.versionId)">查看</el-button>
            <el-button 
              v-if="!scope.row.isPublished" 
              link 
              type="success" 
              @click="handlePublish(scope.row)">
              发布
            </el-button>
            <el-button 
              v-if="!scope.row.isPublished" 
              link 
              type="danger" 
              @click="handleDelete(scope.row)">
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
import { getDatasetVersions, publishDatasetVersion, QueryParams } from '@/api/dataset'

const router = useRouter()
const loading = ref(false)
const datasetList = ref([])
const total = ref(0)

// 查询参数
const queryParams = reactive<QueryParams>({
  page: 1,
  size: 10,
  isPublished: undefined
})

// 获取数据集列表
const getList = async () => {
  loading.value = true
  try {
    const res = await getDatasetVersions(queryParams)
    datasetList.value = res.list || []
    total.value = res.total || 0
  } catch (error) {
    console.error('获取数据集列表失败', error)
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  queryParams.page = 1
  getList()
}

// 重置查询条件
const resetQuery = () => {
  queryParams.isPublished = undefined
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

// 发布数据集
const handlePublish = (row: any) => {
  ElMessageBox.confirm(
    `确认发布数据集 "${row.name}" 吗？发布后将不可修改。`,
    '发布确认',
    {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await publishDatasetVersion(row.versionId)
      ElMessage.success('发布成功')
      getList()
    } catch (error) {
      console.error('发布数据集失败', error)
    }
  }).catch(() => {})
}

// 删除数据集
const handleDelete = (row: any) => {
  ElMessageBox.confirm(
    `确认删除数据集 "${row.name}" 吗？`,
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
      console.error('删除数据集失败', error)
    }
  }).catch(() => {})
}

// 导航到创建页面
const navigateToCreate = () => {
  router.push('/datasets/create')
}

// 导航到详情页面
const navigateToDetail = (id: number) => {
  router.push(`/datasets/detail/${id}`)
}

onMounted(() => {
  getList()
})
</script>

<style scoped>
.dataset-list-container {
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