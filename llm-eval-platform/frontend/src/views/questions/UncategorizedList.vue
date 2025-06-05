<template>
  <div class="uncategorized-questions-container">
    <div class="page-header">
      <h2>未分类问题列表</h2>
      <el-button type="primary" @click="$router.push('/questions')">
        返回问题列表
      </el-button>
    </div>

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
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column fixed="right" label="操作" width="250">
          <template #default="scope">
            <el-button link type="primary" @click="showAddCategoryDialog(scope.row)">添加分类</el-button>
            <el-button link type="primary" @click="navigateToDetail(scope.row.standardQuestionId)">查看</el-button>
            <el-button link type="primary" @click="navigateToEdit(scope.row.standardQuestionId)">编辑</el-button>
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

    <!-- 添加分类对话框 -->
    <el-dialog 
      v-model="dialogVisible" 
      title="添加分类" 
      width="500px">
      <el-form 
        :model="categoryForm" 
        :rules="rules" 
        ref="categoryFormRef" 
        label-width="80px">
        <el-form-item label="问题ID" prop="questionId">
          <el-input v-model="categoryForm.questionId" disabled></el-input>
        </el-form-item>
        <el-form-item label="问题内容" prop="question">
          <el-input v-model="categoryForm.question" type="textarea" :rows="3" disabled></el-input>
        </el-form-item>
        <el-form-item label="分类方式" prop="categoryType">
          <el-radio-group v-model="categoryForm.categoryType">
            <el-radio label="existing">选择已有分类</el-radio>
            <el-radio label="new">创建新分类</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="categoryForm.categoryType === 'existing'" label="选择分类" prop="categoryId">
          <el-select v-model="categoryForm.categoryId" placeholder="请选择分类" filterable>
            <el-option 
              v-for="item in categoryOptions" 
              :key="item.value" 
              :label="item.label" 
              :value="item.value">
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item v-if="categoryForm.categoryType === 'new'" label="分类名称" prop="categoryName">
          <el-input v-model="categoryForm.categoryName" placeholder="请输入新分类名称"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitForm" :loading="submitting">确认</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getQuestionsWithoutCategory, updateQuestionCategoryByName, updateQuestionCategory } from '@/api/question'
import { getAllCategories } from '@/api/category'

const router = useRouter()
const loading = ref(false)
const submitting = ref(false)
const questionList = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const categoryFormRef = ref()
const categoryOptions = ref<any[]>([])

// 查询参数
const queryParams = reactive({
  page: 1,
  size: 10,
  sortBy: 'createdAt',
  sortDir: 'desc'
})

// 分类表单数据
const categoryForm = reactive({
  questionId: 0,
  question: '',
  categoryType: 'existing',
  categoryId: null as number | null,
  categoryName: ''
})

// 表单验证规则
const rules = {
  categoryType: [
    { required: true, message: '请选择分类方式', trigger: 'change' }
  ],
  categoryId: [
    { required: true, message: '请选择分类', trigger: 'change', 
      validator: (rule: any, value: any, callback: any) => {
        if (categoryForm.categoryType === 'existing' && !value) {
          callback(new Error('请选择分类'))
        } else {
          callback()
        }
      }
    }
  ],
  categoryName: [
    { required: true, message: '请输入分类名称', trigger: 'blur',
      validator: (rule: any, value: any, callback: any) => {
        if (categoryForm.categoryType === 'new' && !value) {
          callback(new Error('请输入分类名称'))
        } else {
          callback()
        }
      }
    }
  ]
}

// 获取未分类问题列表
const getList = async () => {
  loading.value = true
  try {
    const res = await getQuestionsWithoutCategory(queryParams)
    console.log('获取未分类问题列表响应:', res)
    
    // 处理响应数据
    if (res && typeof res === 'object') {
      // 处理标准返回格式：{ code: 200, data: { content: [...], total: number, ... }, message: "Success" }
      if (res.code === 200 && res.data) {
        if (res.data.content && Array.isArray(res.data.content)) {
          questionList.value = res.data.content
          total.value = res.data.total || res.data.content.length
        } else {
          questionList.value = []
          total.value = 0
        }
      } 
      // 处理其他可能的格式
      else if (res.content && Array.isArray(res.content)) {
        questionList.value = res.content
        total.value = res.total || res.content.length
      } else {
        questionList.value = []
        total.value = 0
      }
    } else {
      questionList.value = []
      total.value = 0
    }
  } catch (error) {
    console.error('获取未分类问题列表失败', error)
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
    console.log('获取分类列表响应:', res)
    
    // 处理响应数据
    let categoriesData
    if (res && typeof res === 'object') {
      if (res.code === 200 && res.data) {
        categoriesData = res.data
      } else if (Array.isArray(res)) {
        categoriesData = res
      }
    }
    
    if (categoriesData && Array.isArray(categoriesData)) {
      categoryOptions.value = categoriesData.map((item: any) => ({
        value: item.categoryId,
        label: item.name || item.categoryName
      }))
    } else {
      console.warn('获取分类列表返回格式不符合预期')
    }
  } catch (error) {
    console.error('获取分类列表失败', error)
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
  getList()
}

// 当前页变化
const handleCurrentChange = (val: number) => {
  queryParams.page = val
  getList()
}

// 显示添加分类对话框
const showAddCategoryDialog = (row: any) => {
  categoryForm.questionId = row.standardQuestionId
  categoryForm.question = row.question
  categoryForm.categoryType = 'existing'
  categoryForm.categoryId = null
  categoryForm.categoryName = ''
  dialogVisible.value = true
}

// 提交表单
const submitForm = async () => {
  if (!categoryFormRef.value) return
  
  await categoryFormRef.value.validate(async (valid: boolean) => {
    if (!valid) return
    
    submitting.value = true
    try {
      let response
      
      if (categoryForm.categoryType === 'new') {
        // 使用新分类名称
        response = await updateQuestionCategoryByName(
          categoryForm.questionId, 
          categoryForm.categoryName
        )
      } else {
        // 使用现有分类ID
        response = await updateQuestionCategory(
          categoryForm.questionId, 
          categoryForm.categoryId as number
        )
      }
      
      console.log('更新问题分类响应:', response)
      ElMessage.success('添加分类成功')
      dialogVisible.value = false
      getList() // 刷新列表
    } catch (error) {
      console.error('添加分类失败', error)
      ElMessage.error('添加分类失败')
    } finally {
      submitting.value = false
    }
  })
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
.uncategorized-questions-container {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
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