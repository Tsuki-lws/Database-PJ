<template>
  <div class="question-list-container">
    <div class="page-header">
      <h2>标准问题列表</h2>
      <el-button type="primary" @click="navigateToCreate">
        <el-icon><Plus /></el-icon>创建问题
      </el-button>
    </div>

    <!-- 搜索和筛选 -->
    <el-card shadow="never" class="filter-container">
      <el-form :model="queryParams" label-width="80px" :inline="true">
        <el-form-item label="关键词">
          <el-input v-model="queryParams.keyword" placeholder="问题内容" clearable @keyup.enter="handleSearch"></el-input>
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="queryParams.categoryId" placeholder="全部分类" clearable>
            <el-option v-for="item in categoryOptions" :key="item.value" :label="item.label" :value="item.value"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="queryParams.questionType" placeholder="全部类型" clearable>
            <el-option v-for="item in typeOptions" :key="item.value" :label="item.label" :value="item.value"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="难度">
          <el-select v-model="queryParams.difficulty" placeholder="全部难度" clearable>
            <el-option v-for="item in difficultyOptions" :key="item.value" :label="item.label" :value="item.value"></el-option>
          </el-select>
        </el-form-item>
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

    <!-- 问题列表 -->
    <el-card shadow="never" class="list-card">
      <el-table v-loading="loading" :data="questionList" style="width: 100%">
        <el-table-column prop="standardQuestionId" label="ID" width="80" />
        <el-table-column prop="question" label="问题内容" show-overflow-tooltip>
          <template #default="scope">
            <el-link type="primary" @click="navigateToDetail(scope.row.standardQuestionId)">
              {{ scope.row.question }}
            </el-link>
          </template>
        </el-table-column>
        <el-table-column prop="categoryName" label="分类" width="120" />
        <el-table-column prop="questionType" label="类型" width="120">
          <template #default="scope">
            <el-tag :type="getQuestionTypeTag(scope.row.questionType)">
              {{ formatQuestionType(scope.row.questionType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="difficulty" label="难度" width="100">
          <template #default="scope">
            <el-tag :type="getDifficultyTag(scope.row.difficulty)">
              {{ formatDifficulty(scope.row.difficulty) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="120">
          <template #default="scope">
            <el-tag :type="getStatusTag(scope.row.status)">
              {{ formatStatus(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column fixed="right" label="操作" width="180">
          <template #default="scope">
            <el-button link type="primary" @click="navigateToDetail(scope.row.standardQuestionId)">查看</el-button>
            <el-button link type="primary" @click="navigateToEdit(scope.row.standardQuestionId)">编辑</el-button>
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
import { Plus } from '@element-plus/icons-vue'
import { getStandardQuestions, deleteQuestion } from '@/api/question'
import { getAllCategories } from '@/api/category'

const router = useRouter()
const loading = ref(false)
const questionList = ref([])
const total = ref(0)

// 查询参数
const queryParams = reactive({
  page: 1,
  size: 10,
  keyword: '',
  categoryId: undefined,
  questionType: undefined,
  difficulty: undefined,
  status: undefined
})

// 分类选项
const categoryOptions = ref([])

// 问题类型选项
const typeOptions = [
  { value: 'single_choice', label: '单选题' },
  { value: 'multiple_choice', label: '多选题' },
  { value: 'simple_fact', label: '简单事实题' },
  { value: 'subjective', label: '主观题' }
]

// 难度选项
const difficultyOptions = [
  { value: 'easy', label: '简单' },
  { value: 'medium', label: '中等' },
  { value: 'hard', label: '困难' }
]

// 状态选项
const statusOptions = [
  { value: 'draft', label: '草稿' },
  { value: 'pending_review', label: '待审核' },
  { value: 'approved', label: '已通过' },
  { value: 'rejected', label: '已拒绝' }
]

// 获取问题列表
const getList = async () => {
  loading.value = true
  try {
    const res = await getStandardQuestions(queryParams)
    console.log('获取问题列表响应:', res)
    
    // 检查响应格式并适当处理
    if (res && typeof res === 'object') {
      // 根据后端实际返回的数据结构进行处理
      if (res.code === 200 && res.data) {
        // 处理标准返回格式：{ code: 200, data: { content: [...], total: number, ... }, message: "Success" }
        if (res.data.content && Array.isArray(res.data.content)) {
          questionList.value = res.data.content
          total.value = res.data.total || res.data.content.length
        } 
        // 处理其他可能的格式
        else if (Array.isArray(res.data)) {
          questionList.value = res.data
          total.value = res.data.length
        }
        else if (res.data.list) {
          questionList.value = res.data.list
          total.value = res.data.total || res.data.list.length
        }
        else {
          questionList.value = []
          total.value = 0
          console.warn('响应中没有找到预期的数据列表:', res)
        }
      }
      // 兼容其他可能的格式
      else if (res.list) {
        questionList.value = res.list
        total.value = res.total || res.list.length
      }
      else if (Array.isArray(res)) {
        questionList.value = res
        total.value = res.length
      }
      else {
        questionList.value = []
        total.value = 0
        console.warn('响应格式不符合预期:', res)
      }
    } else {
      questionList.value = []
      total.value = 0
      console.error('获取问题列表返回格式异常:', res)
    }
  } catch (error) {
    console.error('获取问题列表失败', error)
    questionList.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

// 获取分类选项
const getCategories = async () => {
  try {
    const res = await getAllCategories()
    categoryOptions.value = res.map((item: any) => ({
      value: item.categoryId,
      label: item.name
    }))
  } catch (error) {
    console.error('获取分类列表失败', error)
  }
}

// 格式化问题类型
const formatQuestionType = (type: string) => {
  const map: Record<string, string> = {
    'single_choice': '单选题',
    'multiple_choice': '多选题',
    'simple_fact': '简单事实题',
    'subjective': '主观题'
  }
  return map[type] || type
}

// 获取问题类型标签类型
const getQuestionTypeTag = (type: string) => {
  const map: Record<string, string> = {
    'single_choice': 'info',
    'multiple_choice': 'warning',
    'simple_fact': 'success',
    'subjective': 'primary'
  }
  return map[type] || ''
}

// 格式化难度
const formatDifficulty = (difficulty: string) => {
  const map: Record<string, string> = {
    'easy': '简单',
    'medium': '中等',
    'hard': '困难'
  }
  return map[difficulty] || difficulty
}

// 获取难度标签类型
const getDifficultyTag = (difficulty: string) => {
  const map: Record<string, string> = {
    'easy': 'success',
    'medium': 'warning',
    'hard': 'danger'
  }
  return map[difficulty] || ''
}

// 格式化状态
const formatStatus = (status: string) => {
  const map: Record<string, string> = {
    'draft': '草稿',
    'pending_review': '待审核',
    'approved': '已通过',
    'rejected': '已拒绝'
  }
  return map[status] || status
}

// 获取状态标签类型
const getStatusTag = (status: string) => {
  const map: Record<string, string> = {
    'draft': 'info',
    'pending_review': 'warning',
    'approved': 'success',
    'rejected': 'danger'
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
  queryParams.keyword = ''
  queryParams.categoryId = undefined
  queryParams.questionType = undefined
  queryParams.difficulty = undefined
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

// 删除问题
const handleDelete = (row: any) => {
  ElMessageBox.confirm(
    `确认删除问题 "${row.question}" 吗？`,
    '删除确认',
    {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await deleteQuestion(row.standardQuestionId)
      ElMessage.success('删除成功')
      getList()
    } catch (error) {
      console.error('删除问题失败', error)
    }
  }).catch(() => {})
}

// 导航到创建页面
const navigateToCreate = () => {
  router.push('/questions/create')
}

// 导航到详情页面
const navigateToDetail = (id: number) => {
  router.push(`/questions/detail/${id}`)
}

// 导航到编辑页面
const navigateToEdit = (id: number) => {
  router.push(`/questions/edit/${id}`)
}

onMounted(() => {
  getList()
  getCategories()
})
</script>

<style scoped>
.question-list-container {
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