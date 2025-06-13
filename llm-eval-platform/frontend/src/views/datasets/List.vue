<template>
  <div class="dataset-list-container">
    <div class="page-header">
      <h2>数据集版本列表</h2>
      <div class="action-buttons">
        <el-button type="primary" @click="refreshList">
          <el-icon><Refresh /></el-icon>刷新
        </el-button>
        <el-button type="primary" @click="navigateToCreate">
          <el-icon><Plus /></el-icon>创建数据集
        </el-button>
      </div>
    </div>

    <!-- 搜索和筛选 -->
    <el-card shadow="never" class="filter-container">
      <el-form :model="queryParams" label-width="80px" :inline="true">
        <el-form-item label="关键词">
          <el-input 
            v-model="queryParams.keyword" 
            placeholder="搜索数据集名称" 
            clearable
            @keyup.enter="handleSearch">
          </el-input>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.isPublished" placeholder="全部状态" clearable>
            <el-option label="已发布" :value="true"></el-option>
            <el-option label="未发布" :value="false"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="排序">
          <el-select v-model="queryParams.sortBy" placeholder="排序字段">
            <el-option label="创建时间" value="createdAt"></el-option>
            <el-option label="发布日期" value="releaseDate"></el-option>
            <el-option label="名称" value="name"></el-option>
            <el-option label="问题数量" value="questionCount"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="顺序">
          <el-select v-model="queryParams.sortDir" placeholder="排序方向">
            <el-option label="降序" value="desc"></el-option>
            <el-option label="升序" value="asc"></el-option>
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
      <div v-if="datasetList.length === 0 && !loading" class="empty-data">
        <el-empty description="暂无数据" />
        <el-button type="primary" @click="refreshList">刷新数据</el-button>
      </div>
      <el-table 
        v-else
        v-loading="loading" 
        :data="datasetList" 
        style="width: 100%" 
        border
        @row-click="(row: any) => navigateToDetail(row.versionId)">
        <el-table-column prop="versionId" label="ID" width="80" />
        <el-table-column prop="name" label="数据集名称" min-width="150" show-overflow-tooltip>
          <template #default="scope">
            <el-link type="primary" @click.stop="navigateToDetail(scope.row.versionId)">
              {{ scope.row.name || '未命名数据集' }}
            </el-link>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip>
          <template #default="scope">
            <el-tooltip 
              :content="scope.row.description || '暂无描述'" 
              placement="top"
              :show-after="500">
              <div class="description-cell">{{ scope.row.description || '暂无描述' }}</div>
            </el-tooltip>
          </template>
        </el-table-column>
        <el-table-column prop="questionCount" label="问题数量" width="100">
          <template #default="scope">
            <el-badge :value="scope.row.questionCount || 0" type="info" />
          </template>
        </el-table-column>
        <el-table-column prop="isPublished" label="状态" width="100">
          <template #default="scope">
            <el-tag v-if="scope.row.isPublished" type="success">已发布</el-tag>
            <el-tag v-else type="info">未发布</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="releaseDate" label="发布日期" width="120">
          <template #default="scope">
            <span v-if="scope.row.releaseDate">{{ formatDate(scope.row.releaseDate) }}</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180">
          <template #default="scope">
            <span v-if="scope.row.createdAt">{{ formatDateTime(scope.row.createdAt) }}</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column fixed="right" label="操作" width="200">
          <template #default="scope">
            <el-button link type="primary" @click.stop="navigateToDetail(scope.row.versionId)">
              <el-icon><View /></el-icon>查看
            </el-button>
            <el-button 
              v-if="!scope.row.isPublished" 
              link 
              type="success" 
              @click.stop="handlePublish(scope.row)">
              <el-icon><Upload /></el-icon>发布
            </el-button>
            <el-button 
              v-if="scope.row.isPublished" 
              link 
              type="warning" 
              @click.stop="handleUnpublish(scope.row)">
              <el-icon><Download /></el-icon>取消发布
            </el-button>
            <el-button 
              v-if="!scope.row.isPublished" 
              link 
              type="danger" 
              @click.stop="handleDelete(scope.row)">
              <el-icon><Delete /></el-icon>删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <div class="pagination-info">
          共 <span class="highlight-count">{{ total }}</span> 条数据，
          当前第 <span class="highlight-count">{{ queryParams.page }}</span> 页，
          每页 <span class="highlight-count">{{ queryParams.size }}</span> 条
        </div>
        <el-pagination
          v-model:current-page="queryParams.page"
          v-model:page-size="queryParams.size"
          :page-sizes="[10, 20, 50, 100]"
          layout="sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          background
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import { Plus, Refresh, View, Upload, Download, Delete } from '@element-plus/icons-vue'
import { getDatasetVersions, publishDatasetVersion, unpublishDatasetVersion, deleteDatasetVersion } from '@/api/dataset'
import type { QueryParams } from '@/api/dataset'
import { AxiosError } from 'axios'

// 定义后端返回的分页数据结构
interface PagedResponseDTO<T> {
  content: T[];
  total: number;
  totalElements?: number;
  page?: number;
  size?: number;
}

const router = useRouter()
const loading = ref(false)
const datasetList = ref<any[]>([])
const total = ref(0)

// 查询参数
const queryParams = reactive<QueryParams>({
  page: 1,
  size: 10,
  isPublished: undefined,
  keyword: '',
  sortBy: 'createdAt',
  sortDir: 'desc'
})

// 获取数据集列表
const getList = async () => {
  loading.value = true
  try {
    console.log('请求数据集列表，参数:', JSON.stringify(queryParams))
    const res = await getDatasetVersions(queryParams)
    console.log('API返回数据:', JSON.stringify(res).substring(0, 1000) + '...')
    
    // 增强数据解析逻辑，支持多种返回格式
    if (res && typeof res === 'object') {
      // 直接处理PagedResponseDTO格式 {content: [], total: 0}
      if ('content' in res && Array.isArray(res.content)) {
        console.log('标准PagedResponseDTO格式数据: content数组, 长度:', res.content.length)
        datasetList.value = res.content
        total.value = ('total' in res && typeof res.total === 'number') ? res.total : res.content.length
        
        // 检查分页信息是否与请求的一致
        if ('currentPage' in res && res.currentPage !== queryParams.page) {
          console.warn(`分页信息不一致(直接格式): 请求页码=${queryParams.page || 1}, 返回页码=${res.currentPage}`);
          // 强制更新为返回的页码
          if (typeof res.currentPage === 'number') {
            queryParams.page = res.currentPage;
          }
        }
        
        console.log(`成功获取 ${datasetList.value.length} 条数据，总数: ${total.value}，当前页: ${queryParams.page}`)
      }
      // 情况1: 标准响应格式 {code: 200, data: {content: [], total: 0}}
      else if ('code' in res && res.code === 200 && res.data) {
        console.log('返回结果code=200，解析data:', typeof res.data)
        
        if (typeof res.data === 'object') {
          // 检查分页信息是否与请求的一致
          if ('currentPage' in res.data && res.data.currentPage !== queryParams.page) {
            console.warn(`分页信息不一致: 请求页码=${queryParams.page || 1}, 返回页码=${res.data.currentPage}`);
            // 强制更新为返回的页码
            if (typeof res.data.currentPage === 'number') {
              queryParams.page = res.data.currentPage;
            }
          }
          
          if ('content' in res.data && Array.isArray(res.data.content)) {
            console.log('数据格式: content数组, 长度:', res.data.content.length)
            
            // 如果返回的内容为空但总数不为0，可能是分页问题
            if (res.data.content.length === 0 && 
                (('total' in res.data && res.data.total > 0) || 
                 ('totalElements' in res.data && res.data.totalElements > 0))) {
              console.warn('检测到分页问题: 返回内容为空但总数不为0');
              
              // 如果当前不是第一页，尝试请求第一页
              if (queryParams.page && queryParams.page > 1) {
                console.warn('尝试请求第一页');
                queryParams.page = 1;
                getList();
                return;
              }
            }
            
            datasetList.value = res.data.content
            total.value = ('total' in res.data && typeof res.data.total === 'number') ? 
                          res.data.total : 
                          (('totalElements' in res.data && typeof res.data.totalElements === 'number') ? 
                           res.data.totalElements : res.data.content.length)
            console.log(`成功获取 ${datasetList.value.length} 条数据，总数: ${total.value}，当前页: ${queryParams.page}`)
          } 
          // 情况2: {code: 200, data: {list: [], total: 0}}
          else if ('list' in res.data && Array.isArray(res.data.list)) {
            datasetList.value = res.data.list
            total.value = ('total' in res.data && typeof res.data.total === 'number') ? res.data.total : res.data.list.length
            console.log(`成功获取(list格式) ${datasetList.value.length} 条数据，总数: ${total.value}，当前页: ${queryParams.page}`)
          }
          // 情况3: {code: 200, data: {records: [], total: 0}}
          else if ('records' in res.data && Array.isArray(res.data.records)) {
            datasetList.value = res.data.records
            total.value = ('total' in res.data && typeof res.data.total === 'number') ? res.data.total : res.data.records.length
            console.log(`成功获取(records格式) ${datasetList.value.length} 条数据，总数: ${total.value}，当前页: ${queryParams.page}`)
          }
          // 情况4: {code: 200, data: [{...}, {...}]}
          else if (Array.isArray(res.data)) {
            datasetList.value = res.data as any[]
            total.value = res.data.length
            console.log(`成功获取(数组格式) ${datasetList.value.length} 条数据，当前页: ${queryParams.page}`)
          } else {
            console.error('无法解析的数据格式:', res.data)
            datasetList.value = []
            total.value = 0
          }
        } else {
          console.error('返回的data不是对象:', typeof res.data)
          datasetList.value = []
          total.value = 0
        }
      } 
      // 情况5: 直接返回数组
      else if (Array.isArray(res)) {
        datasetList.value = res as any[]
        total.value = res.length
        console.log(`成功获取(直接数组格式) ${datasetList.value.length} 条数据，当前页: ${queryParams.page}`)
      }
      // 情况6: 不符合预期的格式
      else {
        console.error('返回数据不符合预期格式:', res)
        datasetList.value = []
        total.value = 0
      }
    } else {
      console.error('返回数据无效:', res)
      datasetList.value = []
      total.value = 0
    }
  } catch (error: any) {
    console.error('获取数据集列表失败:', error)
    if (error.response) {
      console.error('错误详情:', error.response.status, error.response.data)
    }
    datasetList.value = []
    total.value = 0
    ElMessage.error('获取数据集列表失败')
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
  queryParams.keyword = ''
  queryParams.sortBy = 'createdAt'
  queryParams.sortDir = 'desc'
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

// 取消发布数据集
const handleUnpublish = (row: any) => {
  ElMessageBox.confirm(
    `确认取消发布数据集 "${row.name}" 吗？`,
    '取消发布确认',
    {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await unpublishDatasetVersion(row.versionId)
      ElMessage.success('取消发布成功')
      getList()
    } catch (error) {
      console.error('取消发布数据集失败', error)
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
      await deleteDatasetVersion(row.versionId)
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
  console.log('导航到数据集详情页:', id)
  router.push(`/datasets/detail/${id}`)
}

// 格式化日期
const formatDate = (dateStr: string) => {
  if (!dateStr) return '-'
  try {
    const date = new Date(dateStr)
    return date.toLocaleDateString()
  } catch (e) {
    return dateStr
  }
}

// 格式化日期时间
const formatDateTime = (dateTimeStr: string) => {
  if (!dateTimeStr) return '-'
  try {
    const date = new Date(dateTimeStr)
    return date.toLocaleString()
  } catch (e) {
    return dateTimeStr
  }
}

// 刷新列表
const refreshList = () => {
  ElMessage.info('正在刷新数据...')
  getList()
}

onMounted(() => {
  console.log('数据集列表组件已挂载，开始获取数据')
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

.action-buttons {
  display: flex;
  gap: 10px;
}

.filter-container {
  margin-bottom: 20px;
}

.list-card {
  margin-bottom: 20px;
}

.pagination-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 20px;
}

.empty-data {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px;
}

.pagination-info {
  font-size: 14px;
  color: #606266;
}

.highlight-count {
  font-weight: bold;
  color: #409EFF;
}

.description-cell {
  max-height: 60px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  line-height: 1.5;
}

.el-table {
  margin-bottom: 10px;
}

.el-table .el-table__row:hover {
  cursor: pointer;
  background-color: #f5f7fa;
}

.el-badge__content {
  font-size: 12px;
  padding: 0 6px;
}
</style> 