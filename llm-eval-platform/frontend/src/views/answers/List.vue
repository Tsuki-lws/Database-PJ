<template>
  <div class="answer-list-container">
    <div class="page-header">
      <h2>标准答案列表</h2>
    </div>

    <!-- 搜索和筛选 -->
    <el-card shadow="never" class="filter-container">
      <el-form :model="queryParams" label-width="80px" :inline="true">
        <el-form-item label="问题ID">
          <el-input v-model="queryParams.questionId" placeholder="问题ID" clearable @keyup.enter="handleSearch"></el-input>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.isFinal" placeholder="全部状态" clearable>
            <el-option label="最终版本" :value="true"></el-option>
            <el-option label="非最终版本" :value="false"></el-option>
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
        <el-table-column prop="standardAnswerId" label="ID" width="80" />
        <el-table-column prop="standardQuestionId" label="问题ID" width="100" />
        <el-table-column prop="answer" label="答案内容" show-overflow-tooltip>
          <template #default="scope">
            <el-link type="primary" @click="navigateToDetail(scope.row.standardAnswerId)">
              {{ truncateText(scope.row.answer, 100) }}
            </el-link>
          </template>
        </el-table-column>
        <el-table-column prop="sourceType" label="来源类型" width="120">
          <template #default="scope">
            <el-tag :type="getSourceTypeTag(scope.row.sourceType)">
              {{ formatSourceType(scope.row.sourceType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="isFinal" label="状态" width="100">
          <template #default="scope">
            <el-tag v-if="scope.row.isFinal" type="success">最终版本</el-tag>
            <el-tag v-else type="info">非最终版本</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="version" label="版本" width="80" />
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column fixed="right" label="操作" width="180">
          <template #default="scope">
            <el-button link type="primary" @click="navigateToDetail(scope.row.standardAnswerId)">查看</el-button>
            <el-button link type="primary" @click="navigateToEdit(scope.row.standardAnswerId)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(scope.row)">删除</el-button>
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
import { getAnswerList, deleteAnswer, QueryParams } from '@/api/answer'

const router = useRouter()
const loading = ref(false)
const answerList = ref([])
const total = ref(0)

// 查询参数
const queryParams = reactive<QueryParams>({
  page: 1,
  size: 10,
  questionId: undefined,
  isFinal: undefined
})

// 获取答案列表
const getList = async () => {
  loading.value = true
  try {
    const res = await getAnswerList(queryParams)
    answerList.value = res.list || []
    total.value = res.total || 0
  } catch (error) {
    console.error('获取答案列表失败', error)
  } finally {
    loading.value = false
  }
}

// 格式化来源类型
const formatSourceType = (type: string) => {
  const map: Record<string, string> = {
    'raw': '原始回答',
    'crowdsourced': '众包答案',
    'expert': '专家答案',
    'manual': '手动创建'
  }
  return map[type] || type
}

// 获取来源类型标签类型
const getSourceTypeTag = (type: string) => {
  const map: Record<string, string> = {
    'raw': 'info',
    'crowdsourced': 'success',
    'expert': 'warning',
    'manual': 'primary'
  }
  return map[type] || ''
}

// 截断文本
const truncateText = (text: string, length: number) => {
  if (!text) return ''
  return text.length > length ? text.substring(0, length) + '...' : text
}

// 搜索
const handleSearch = () => {
  queryParams.page = 1
  getList()
}

// 重置查询条件
const resetQuery = () => {
  queryParams.questionId = undefined
  queryParams.isFinal = undefined
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

// 删除答案
const handleDelete = (row: any) => {
  ElMessageBox.confirm(
    `确认删除答案 ID: ${row.standardAnswerId} 吗？`,
    '删除确认',
    {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await deleteAnswer(row.standardAnswerId)
      ElMessage.success('删除成功')
      getList()
    } catch (error) {
      console.error('删除答案失败', error)
    }
  }).catch(() => {})
}

// 导航到详情页面
const navigateToDetail = (id: number) => {
  // 暂时导航到编辑页面，因为还没有详情页面
  router.push(`/answers/edit/${id}`)
}

// 导航到编辑页面
const navigateToEdit = (id: number) => {
  router.push(`/answers/edit/${id}`)
}

onMounted(() => {
  getList()
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