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
        <el-descriptions-item label="问题数量">{{ questionList.length || 0 }}</el-descriptions-item>
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
            :total="questionList.length"
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
    <el-dialog v-model="addQuestionDialogVisible" title="添加问题" width="85%" top="5vh" class="question-dialog">
      <div class="question-dialog-content">
        <div class="question-search-bar">
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
      
        <div class="question-filter-bar">
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
      
        <div class="question-table-container">
      <el-table
        v-loading="questionLoading"
        :data="availableQuestions"
            :height="tableHeight"
            border
        @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55"></el-table-column>
        <el-table-column prop="standardQuestionId" label="ID" width="80"></el-table-column>
            <el-table-column prop="question" label="问题内容" min-width="300">
              <template #default="scope">
                <div class="question-content">{{ scope.row.question }}</div>
              </template>
            </el-table-column>
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
            <el-table-column prop="category" label="分类" width="150">
              <template #default="scope">
                <span v-if="scope.row.category">{{ scope.row.category.categoryName }}</span>
                <span v-else>无分类</span>
              </template>
            </el-table-column>
      </el-table>
        </div>
        
        <div class="dialog-pagination">
          <div class="pagination-info" v-if="questionTotal > 0">
            总计 <span class="highlight-count">{{ questionTotal }}</span> 个问题
            <template v-if="questionList.length > 0">
              (已添加 <span class="highlight-count">{{ questionList.length }}</span> 个)
            </template>
          </div>
        <el-pagination
          v-model:current-page="questionQuery.page"
          v-model:page-size="questionQuery.size"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next, jumper"
          :total="questionTotal"
          @size-change="handleQuestionSizeChange"
          @current-change="handleQuestionCurrentChange">
        </el-pagination>
        </div>
      </div>
      
      <template #footer>
        <span class="dialog-footer">
          <div class="selected-count" v-if="selectedQuestions.length > 0">
            已选择 {{ selectedQuestions.length }} 个问题
          </div>
          <div class="dialog-buttons">
          <el-button @click="addQuestionDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleAddQuestions">添加选中问题</el-button>
          </div>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'
import { 
  getDatasetVersionById, 
  getDatasetQuestions, 
  addQuestionsToDataset, 
  removeQuestionFromDataset,
  publishDatasetVersion,
  deleteDatasetVersion
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
  page: 1,       // 后端期望从1开始的页码
  size: 20,      // 默认每页显示20条
  keyword: '',
  categoryId: undefined,
  difficulty: undefined
})
const availableQuestions = ref<any[]>([])
const questionTotal = ref(0)
const selectedQuestions = ref<any[]>([])
const categoryOptions = ref<Array<{value: number, label: string}>>([])

// 表格高度自适应
const tableHeight = ref(450);

// 获取数据集详情
const getDatasetDetail = async () => {
  const id = Number(route.params.id)
  if (!id) return
  
  loading.value = true
  try {
    const res = await getDatasetVersionById(id)
    if (res && typeof res === 'object' && 'code' in res && res.code === 200) {
      dataset.value = res.data || {}
    } else {
      dataset.value = {}
      console.error('获取数据集详情返回数据格式不正确', res)
    }
    await getQuestions()
  } catch (error) {
    console.error('获取数据集详情失败', error)
    dataset.value = {}
  } finally {
    loading.value = false
  }
}

// 获取数据集问题
const getQuestions = async () => {
  const id = Number(route.params.id)
  if (!id) return
  
  try {
    console.log('正在获取数据集问题，数据集ID:', id);
    const res = await getDatasetQuestions(id)
    console.log('数据集问题API返回:', res);
    
    if (res && typeof res === 'object' && 'code' in res && res.code === 200) {
      let questionData = [];
      
      // 处理不同格式的返回数据
      if (Array.isArray(res.data)) {
        questionData = res.data;
        console.log('数据格式: 数组，长度:', questionData.length);
      } else if (res.data && typeof res.data === 'object' && 'content' in res.data) {
        // 处理分页返回格式
        questionData = res.data.content || [];
        console.log('数据格式: 分页对象，内容长度:', questionData.length);
      } else {
        console.error('未知的数据集问题返回格式', res.data);
      }
      
      // 确保每个问题都有hasStandardAnswer字段
      questionList.value = questionData.map((q: any) => {
        // 如果后端已经提供了hasStandardAnswer字段，直接使用
        if ('hasStandardAnswer' in q) {
          return q;
        }
        
        // 否则，根据standardAnswers字段判断
        const hasAnswer = q.standardAnswers && Array.isArray(q.standardAnswers) && q.standardAnswers.length > 0;
        return {
          ...q,
          hasStandardAnswer: hasAnswer
        };
      });
      
      // 更新数据集问题总数
      dataset.value.questionCount = questionList.value.length;
      console.log(`数据集问题数量: ${questionList.value.length}`);
      console.log('问题标准答案状态:', questionList.value.map(q => ({ id: q.standardQuestionId, hasAnswer: q.hasStandardAnswer })));
    } else {
      questionList.value = []
      console.error('获取数据集问题返回数据格式不正确', res)
    }
  } catch (error) {
    console.error('获取数据集问题失败', error)
    questionList.value = []
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
  // 重置选中问题
  selectedQuestions.value = []
  // 计算表格高度
  nextTick(() => {
    const windowHeight = window.innerHeight;
    // 根据窗口高度动态计算表格高度，减去头部和底部空间
    tableHeight.value = Math.max(300, windowHeight * 0.5);
    console.log('设置表格高度:', tableHeight.value);
  });
  
  searchQuestions()
  getCategories()
}

// 获取分类列表
const getCategories = async () => {
  try {
    const res = await getAllCategories()
    if (res && typeof res === 'object' && 'code' in res && res.code === 200) {
      categoryOptions.value = Array.isArray(res.data) 
        ? res.data.map((item: any) => ({
            value: item.categoryId,
            label: item.name
          }))
        : []
    } else {
      categoryOptions.value = []
      console.error('获取分类列表返回数据格式不正确', res)
    }
  } catch (error) {
    console.error('获取分类列表失败', error)
    categoryOptions.value = []
  }
}

// 搜索问题
const searchQuestions = () => {
  console.log('执行搜索，参数:', JSON.stringify({
    keyword: questionSearchKeyword.value,
    categoryId: questionFilter.categoryId,
    difficulty: questionFilter.difficulty
  }));
  
  questionQuery.keyword = questionSearchKeyword.value
  questionQuery.categoryId = questionFilter.categoryId
  questionQuery.difficulty = questionFilter.difficulty
  questionQuery.page = 1  // 重置为第一页
  fetchAvailableQuestions()
}

// 获取可添加的问题列表
const fetchAvailableQuestions = async () => {
  questionLoading.value = true
  try {
    // 确保page参数从1开始
    const queryParams = { ...questionQuery };
    
    console.log('请求参数:', JSON.stringify(queryParams));
    
    const res = await getQuestionList(queryParams);
    
    if (res && typeof res === 'object' && 'code' in res && res.code === 200 && 
        res.data && typeof res.data === 'object') {
      // 兼容不同的后端返回格式
      let content = [], total = 0;
      
      if ('content' in res.data) {
        // 分页格式1: { content: [...], totalElements: 100 }
        content = res.data.content || [];
        total = res.data.totalElements || 0;
      } else if (Array.isArray(res.data)) {
        // 分页格式2: 直接返回数组
        content = res.data;
        total = res.data.length;
      } else if ('records' in res.data) {
        // 分页格式3: { records: [...], total: 100 }
        content = res.data.records || [];
        total = res.data.total || 0;
      } else {
        // 其他格式情况
        console.error('未知的返回数据格式', res.data);
        content = [];
        total = 0;
      }
      
      // 过滤掉已经在数据集中的问题
      const existingIds = new Set(questionList.value.map((q: any) => q.standardQuestionId))
      
      console.log(`数据集已有问题数: ${existingIds.size}`);
      console.log(`本页返回问题数: ${content.length}, 总问题数: ${total}`);
      
      availableQuestions.value = content.filter((q: any) => !existingIds.has(q.standardQuestionId))
      
      // 设置总数（这里使用API返回的总数，不考虑过滤）
      questionTotal.value = total;
      
      console.log(`过滤后可添加问题数: ${availableQuestions.value.length}`);
      console.log(`当前页: ${questionQuery.page}, 每页数量: ${questionQuery.size}, 总页数: ${Math.ceil(total/questionQuery.size)}`);
    } else {
      availableQuestions.value = []
      questionTotal.value = 0
      console.error('获取问题列表返回数据格式不正确', res)
    }
  } catch (error) {
    console.error('获取问题列表失败', error)
    availableQuestions.value = []
    questionTotal.value = 0
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
  console.log('修改每页显示数量:', val);
  questionQuery.size = val
  fetchAvailableQuestions()
}

// 处理问题列表页码变化
const handleQuestionCurrentChange = (val: number) => {
  console.log('修改当前页码:', val);
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
    ElMessage.success(`成功添加 ${selectedQuestions.value.length} 个问题`)
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
    } catch (error: any) {
      console.error('发布数据集失败', error)
      
      // 处理错误响应
      let errorMessage = '发布失败，请稍后重试'
      
      if (error.response) {
        console.error('错误状态码:', error.response.status)
        console.error('错误数据:', error.response.data)
        
        // 尝试从响应中提取错误信息
        if (error.response.data && error.response.data.message) {
          errorMessage = error.response.data.message
        }
      }
      
      // 显示错误消息
      ElMessage.error(errorMessage)
      
      // 如果是因为没有问题而失败，提示用户添加问题
      if (errorMessage.includes('no questions') || errorMessage.includes('没有问题')) {
        ElMessageBox.alert(
          '发布失败：数据集中没有问题。请先添加问题后再尝试发布。',
          '发布失败',
          {
            confirmButtonText: '添加问题',
            callback: () => {
              showAddQuestionDialog()
            }
          }
        )
      }
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
      await deleteDatasetVersion(dataset.value.versionId)
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

/* 问题选择对话框样式优化 */
:deep(.question-dialog .el-dialog__body) {
  padding: 15px 20px;
}

.question-dialog-content {
  display: flex;
  flex-direction: column;
  gap: 15px;
  height: 100%;
}

.question-search-bar {
  width: 100%;
  margin-bottom: 10px;
}

.question-filter-bar {
  display: flex;
  gap: 15px;
  margin-bottom: 15px;
}

.question-table-container {
  width: 100%;
  margin-bottom: 10px;
  flex: 1;
}

.question-content {
  max-height: 100px;
  overflow-y: auto;
  line-height: 1.5;
  white-space: pre-wrap;
  word-break: break-all;
}

.dialog-pagination {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 15px;
  margin-bottom: 10px;
}

.pagination-info {
  font-size: 14px;
  color: #606266;
}

.highlight-count {
  font-weight: bold;
  color: #409EFF;
  margin: 0 3px;
}

/* 确保分页组件在小屏幕上响应式展示 */
@media screen and (max-width: 768px) {
  .dialog-pagination {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  
  .question-filter-bar {
    flex-wrap: wrap;
  }
}

.dialog-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.selected-count {
  font-weight: bold;
  color: #409EFF;
}

.dialog-buttons {
  display: flex;
  gap: 10px;
}
</style> 