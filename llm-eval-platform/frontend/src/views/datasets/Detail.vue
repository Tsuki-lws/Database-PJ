<template>
  <div class="dataset-detail-container">
    <div class="page-header">
      <h2>数据集详情</h2>
      <el-button @click="navigateBack">返回</el-button>
    </div>

    <el-card shadow="never" class="detail-card" v-loading="loading">
      <div class="dataset-header">
        <h3>{{ dataset.name }}</h3>
        <el-tag v-if="dataset.isPublished" type="success">已发布</el-tag>
        <el-tag v-else type="info">未发布</el-tag>
      </div>

      <el-descriptions :column="2" border>
        <el-descriptions-item label="数据集ID">{{ dataset.versionId }}</el-descriptions-item>
        <el-descriptions-item label="问题数量">{{ dataset.questionCount || 0 }}</el-descriptions-item>
        <el-descriptions-item label="发布日期">{{ dataset.releaseDate || '未设置' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ dataset.createdAt }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ dataset.updatedAt }}</el-descriptions-item>
        <el-descriptions-item label="描述" :span="2">{{ dataset.description || '无描述' }}</el-descriptions-item>
      </el-descriptions>

      <el-divider />

      <div class="dataset-questions">
        <div class="section-header">
          <h3>数据集问题列表</h3>
          <div v-if="!dataset.isPublished">
            <el-button type="primary" size="small" @click="showAddQuestionDialog">
              <el-icon><Plus /></el-icon>添加问题
            </el-button>
          </div>
        </div>
        
        <el-table :data="questionList" style="width: 100%">
          <el-table-column prop="standardQuestionId" label="问题ID" width="100" />
          <el-table-column prop="question" label="问题内容" show-overflow-tooltip />
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
          <el-table-column prop="hasStandardAnswer" label="标准答案" width="100">
            <template #default="scope">
              <el-tag v-if="scope.row.hasStandardAnswer" type="success">已有</el-tag>
              <el-tag v-else type="danger">缺失</el-tag>
            </template>
          </el-table-column>
          <el-table-column v-if="!dataset.isPublished" fixed="right" label="操作" width="100">
            <template #default="scope">
              <el-button 
                link 
                type="danger" 
                @click="handleRemoveQuestion(scope.row)">
                移除
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
      </div>

      <el-divider />

      <div class="dataset-actions" v-if="!dataset.isPublished">
        <el-button 
          type="primary" 
          @click="handlePublish">
          发布数据集
        </el-button>
        <el-button 
          type="danger" 
          @click="handleDelete">
          删除数据集
        </el-button>
      </div>
    </el-card>

    <!-- 添加问题对话框 -->
    <el-dialog v-model="addQuestionDialogVisible" title="添加问题" width="70%">
      <div class="question-search">
        <el-input
          v-model="questionSearchKeyword"
          placeholder="搜索问题"
          clearable
          @keyup.enter="searchQuestions">
          <template #append>
            <el-button @click="searchQuestions">
              <el-icon><Search /></el-icon>
            </el-button>
          </template>
        </el-input>
      </div>
      
      <div class="question-filter">
        <el-select v-model="questionFilter.categoryId" placeholder="分类" clearable>
          <el-option 
            v-for="item in categoryOptions" 
            :key="item.value" 
            :label="item.label" 
            :value="item.value">
          </el-option>
        </el-select>
        
        <el-select v-model="questionFilter.difficulty" placeholder="难度" clearable>
          <el-option label="简单" value="easy"></el-option>
          <el-option label="中等" value="medium"></el-option>
          <el-option label="困难" value="hard"></el-option>
        </el-select>
        
        <el-button type="primary" @click="searchQuestions">筛选</el-button>
      </div>
      
      <el-table
        v-loading="questionLoading"
        :data="availableQuestions"
        @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55"></el-table-column>
        <el-table-column prop="standardQuestionId" label="ID" width="80"></el-table-column>
        <el-table-column prop="question" label="问题内容" show-overflow-tooltip></el-table-column>
        <el-table-column prop="difficulty" label="难度" width="100">
          <template #default="scope">
            <el-tag :type="getDifficultyTag(scope.row.difficulty)">
              {{ formatDifficulty(scope.row.difficulty) }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
      
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="questionQuery.page"
          v-model:page-size="questionQuery.size"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          :total="questionTotal"
          @size-change="handleQuestionSizeChange"
          @current-change="handleQuestionCurrentChange">
        </el-pagination>
      </div>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="addQuestionDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleAddQuestions">添加选中问题</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'
import { 
  getDatasetVersionById, 
  getDatasetQuestions, 
  addQuestionsToDataset, 
  removeQuestionFromDataset,
  publishDatasetVersion 
} from '@/api/dataset'
import { getQuestionList } from '@/api/question'
import { getAllCategories } from '@/api/category'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const questionLoading = ref(false)
const dataset = ref<any>({})
const questionList = ref<any[]>([])
const total = ref(0)

// 查询参数
const queryParams = reactive({
  page: 1,
  size: 10
})

// 添加问题对话框
const addQuestionDialogVisible = ref(false)
const questionSearchKeyword = ref('')
const questionFilter = reactive({
  categoryId: undefined,
  difficulty: undefined
})
const questionQuery = reactive({
  page: 1,
  size: 10,
  keyword: '',
  categoryId: undefined,
  difficulty: undefined
})
const availableQuestions = ref<any[]>([])
const questionTotal = ref(0)
const selectedQuestions = ref<any[]>([])
const categoryOptions = ref([])

// 获取数据集详情
const getDatasetDetail = async () => {
  const id = Number(route.params.id)
  if (!id) return
  
  loading.value = true
  try {
    dataset.value = await getDatasetVersionById(id)
    await getQuestions()
  } catch (error) {
    console.error('获取数据集详情失败', error)
  } finally {
    loading.value = false
  }
}

// 获取数据集问题
const getQuestions = async () => {
  const id = Number(route.params.id)
  if (!id) return
  
  try {
    const res = await getDatasetQuestions(id)
    questionList.value = res || []
    total.value = res.length || 0
  } catch (error) {
    console.error('获取数据集问题失败', error)
  }
}

// 格式化问题类型
const formatQuestionType = (type: string) => {
  const map: Record<string, string> = {
    'single_choice': '单选题',
    'multiple_choice': '多选题',
    // 'simple_fact': '简单事实题',
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

// 每页数量变化
const handleSizeChange = (val: number) => {
  queryParams.size = val
  getQuestions()
}

// 当前页变化
const handleCurrentChange = (val: number) => {
  queryParams.page = val
  getQuestions()
}

// 显示添加问题对话框
const showAddQuestionDialog = () => {
  addQuestionDialogVisible.value = true
  searchQuestions()
  getCategories()
}

// 获取分类列表
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

// 搜索问题
const searchQuestions = () => {
  questionQuery.keyword = questionSearchKeyword.value
  questionQuery.categoryId = questionFilter.categoryId
  questionQuery.difficulty = questionFilter.difficulty
  questionQuery.page = 1
  fetchAvailableQuestions()
}

// 获取可添加的问题列表
const fetchAvailableQuestions = async () => {
  questionLoading.value = true
  try {
    const res = await getQuestionList(questionQuery)
    // 过滤掉已经在数据集中的问题
    const existingIds = new Set(questionList.value.map(q => q.standardQuestionId))
    availableQuestions.value = (res.list || []).filter(q => !existingIds.has(q.standardQuestionId))
    questionTotal.value = res.total || 0
  } catch (error) {
    console.error('获取问题列表失败', error)
  } finally {
    questionLoading.value = false
  }
}

// 处理表格选择变化
const handleSelectionChange = (selection: any[]) => {
  selectedQuestions.value = selection
}

// 处理问题列表每页数量变化
const handleQuestionSizeChange = (val: number) => {
  questionQuery.size = val
  fetchAvailableQuestions()
}

// 处理问题列表页码变化
const handleQuestionCurrentChange = (val: number) => {
  questionQuery.page = val
  fetchAvailableQuestions()
}

// 添加选中问题
const handleAddQuestions = async () => {
  if (selectedQuestions.value.length === 0) {
    ElMessage.warning('请选择要添加的问题')
    return
  }
  
  try {
    const questionIds = selectedQuestions.value.map(q => q.standardQuestionId)
    await addQuestionsToDataset(dataset.value.versionId, questionIds)
    ElMessage.success('添加成功')
    addQuestionDialogVisible.value = false
    getQuestions()
  } catch (error) {
    console.error('添加问题失败', error)
  }
}

// 移除问题
const handleRemoveQuestion = (row: any) => {
  ElMessageBox.confirm(
    `确认从数据集中移除问题 ID: ${row.standardQuestionId} 吗？`,
    '移除确认',
    {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await removeQuestionFromDataset(dataset.value.versionId, row.standardQuestionId)
      ElMessage.success('移除成功')
      getQuestions()
    } catch (error) {
      console.error('移除问题失败', error)
    }
  }).catch(() => {})
}

// 发布数据集
const handlePublish = () => {
  if (questionList.value.length === 0) {
    ElMessage.warning('数据集中没有问题，无法发布')
    return
  }
  
  ElMessageBox.confirm(
    '确认发布此数据集吗？发布后将不可修改。',
    '发布确认',
    {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await publishDatasetVersion(dataset.value.versionId)
      ElMessage.success('发布成功')
      getDatasetDetail()
    } catch (error) {
      console.error('发布数据集失败', error)
    }
  }).catch(() => {})
}

// 删除数据集
const handleDelete = () => {
  ElMessageBox.confirm(
    '确认删除此数据集吗？此操作不可恢复。',
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
      router.push('/datasets')
    } catch (error) {
      console.error('删除数据集失败', error)
    }
  }).catch(() => {})
}

// 返回上一页
const navigateBack = () => {
  router.back()
}

onMounted(() => {
  getDatasetDetail()
})
</script>

<style scoped>
.dataset-detail-container {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.detail-card {
  margin-bottom: 20px;
}

.dataset-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.dataset-header h3 {
  margin: 0;
  font-size: 20px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.section-header h3 {
  margin: 0;
}

.dataset-questions {
  margin: 20px 0;
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

.dataset-actions {
  display: flex;
  gap: 10px;
}

.question-search {
  margin-bottom: 15px;
}

.question-filter {
  display: flex;
  gap: 10px;
  margin-bottom: 15px;
}
</style> 