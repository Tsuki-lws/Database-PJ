<template>
  <div class="question-list-container">
    <div class="page-header">
      <h2>无标准答案问题列表</h2>
      <div>
        <el-button type="primary" @click="navigateBack">
          <el-icon><Back /></el-icon>返回问题列表
        </el-button>
      </div>
    </div>

    <!-- 搜索和筛选 -->
    <el-card shadow="never" class="filter-container">
      <el-form :model="queryParams" label-width="80px" :inline="true">
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
        <el-table-column prop="categoryName" label="分类" width="120">
          <template #default="scope">
            {{ scope.row.category ? scope.row.category.categoryName || scope.row.category.name || '未分类' : '未分类' }}
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
        <el-table-column fixed="right" label="操作" width="180">
          <template #default="scope">
            <el-button link type="primary" @click="navigateToDetail(scope.row.standardQuestionId)">查看</el-button>
            <!-- <el-button link type="success" @click="navigateToAddAnswer(scope.row.standardQuestionId)">添加答案</el-button> -->
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
import { Back } from '@element-plus/icons-vue'
import { getQuestionsWithoutAnswer } from '@/api/question'
import { getAllCategories } from '@/api/category'

const router = useRouter()
const loading = ref(false)
const questionList = ref<any[]>([])
const total = ref(0)

// 查询参数
const queryParams = reactive({
  page: 1,
  size: 10,
  categoryId: undefined as number | undefined,
  questionType: undefined as string | undefined,
  difficulty: undefined as string | undefined,
  sortBy: 'createdAt',
  sortDir: 'desc'
})

// 分类选项
const categoryOptions = ref<{value: number, label: string}[]>([])

// 问题类型选项
const typeOptions = [
  { value: 'single_choice', label: '单选题' },
  { value: 'multiple_choice', label: '多选题' },
  { value: 'subjective', label: '主观题' }
]

// 难度选项
const difficultyOptions = [
  { value: 'easy', label: '简单' },
  { value: 'medium', label: '中等' },
  { value: 'hard', label: '困难' }
]

// 获取问题列表
const getList = async () => {
  loading.value = true
  try {
    // 使用分页API获取无标准答案的问题
    const res = await getQuestionsWithoutAnswer({
      ...queryParams,
      page: queryParams.page
    })
    
    console.log('获取无标准答案问题列表响应:', res)
    
    if (res && typeof res === 'object' && 'code' in res && res.code === 200) {
      // 处理标准返回格式
      let questions = [];
      
      if (res.data && typeof res.data === 'object') {
        if ('content' in res.data) {
          questions = res.data.content || [];
          total.value = res.data.total || 0;
        } else if ('list' in res.data) {
          questions = res.data.list || [];
          total.value = res.data.total || 0;
        } else if (Array.isArray(res.data)) {
          questions = res.data;
          total.value = res.data.length;
        }
      } else if (Array.isArray(res.data)) {
        questions = res.data;
        total.value = res.data.length;
      }
      
      console.log('原始问题列表:', questions);
      
      // 增强的过滤逻辑：检查问题是否真的没有标准答案
      questionList.value = questions.filter((q: any) => {
        // 检查standardAnswers字段
        const hasStandardAnswers = q.standardAnswers && 
                                 Array.isArray(q.standardAnswers) && 
                                 q.standardAnswers.length > 0;
        
        // 记录有标准答案的问题
        if (hasStandardAnswers) {
          console.warn(`问题ID ${q.standardQuestionId} 有标准答案:`, q.standardAnswers);
        }
        
        return !hasStandardAnswers;
      });
      
      // 如果过滤后数量变少，记录日志
      if (questionList.value.length < questions.length) {
        console.warn(`过滤掉了${questions.length - questionList.value.length}个有标准答案的问题`);
        
        // 添加更详细的日志
        const filteredOutQuestions = questions.filter((q: any) => {
          const hasStandardAnswers = q.standardAnswers && 
                                   Array.isArray(q.standardAnswers) && 
                                   q.standardAnswers.length > 0;
          return hasStandardAnswers;
        });
        
        console.warn('被过滤掉的问题:', filteredOutQuestions.map((q: any) => ({
          id: q.standardQuestionId,
          question: q.question,
          answerCount: q.standardAnswers?.length || 0
        })));
      }
      
      console.log('过滤后的问题列表:', questionList.value);
    } else {
      questionList.value = []
      total.value = 0
      ElMessage.error('获取无标准答案问题列表失败')
    }
  } catch (error) {
    console.error('获取无标准答案问题列表失败', error)
    questionList.value = []
    total.value = 0
    ElMessage.error('获取无标准答案问题列表失败')
  } finally {
    loading.value = false
  }
}

// 获取分类选项
const getCategoryOptions = async () => {
  try {
    const res = await getAllCategories()
    console.log('获取分类选项响应:', res)
    
    if (Array.isArray(res)) {
      categoryOptions.value = res.map(item => ({
        value: item.categoryId,
        label: item.name
      }))
    } else if (res && res.data && Array.isArray(res.data)) {
      categoryOptions.value = res.data.map(item => ({
        value: item.categoryId,
        label: item.name
      }))
    } else {
      categoryOptions.value = []
    }
  } catch (error) {
    console.error('获取分类选项失败', error)
    categoryOptions.value = []
  }
}

// 格式化问题类型
const formatQuestionType = (type: string) => {
  const map: Record<string, string> = {
    'single_choice': '单选题',
    'multiple_choice': '多选题',
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

// 搜索
const handleSearch = () => {
  queryParams.page = 1
  getList()
}

// 重置查询条件
const resetQuery = () => {
  queryParams.categoryId = undefined
  queryParams.questionType = undefined
  queryParams.difficulty = undefined
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

// 导航到详情页面
const navigateToDetail = (id: number) => {
  router.push(`/questions/detail/${id}`)
}

// 导航到添加答案页面
const navigateToAddAnswer = (id: number) => {
  router.push(`/answers/create?questionId=${id}`)
}

// 返回问题列表
const navigateBack = () => {
  router.push('/questions')
}

onMounted(() => {
  getList()
  getCategoryOptions()
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