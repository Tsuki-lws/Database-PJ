<template>
  <div class="dataset-create-container">
    <div class="page-header">
      <h2>创建数据集</h2>
      <el-button @click="navigateBack">返回</el-button>
    </div>

    <el-card shadow="never" class="form-card" v-loading="loading">
      <el-form :model="datasetForm" :rules="rules" ref="datasetFormRef" label-width="100px">
        <el-form-item label="数据集名称" prop="name">
          <el-input v-model="datasetForm.name" placeholder="请输入数据集名称"></el-input>
        </el-form-item>

        <el-form-item label="数据集描述" prop="description">
          <el-input 
            v-model="datasetForm.description" 
            type="textarea" 
            :rows="4" 
            placeholder="请输入数据集描述">
          </el-input>
        </el-form-item>

        <el-form-item label="发布日期">
          <el-date-picker
            v-model="datasetForm.releaseDate"
            type="date"
            placeholder="选择发布日期">
          </el-date-picker>
        </el-form-item>

        <el-divider content-position="left">选择问题</el-divider>

        <el-form-item label="问题选择" prop="questionIds">
          <div class="question-selection">
            <div class="selection-header">
              <h3>标准问题列表</h3>
              <div class="selection-info" v-if="datasetForm.questionIds && datasetForm.questionIds.length > 0">
                已选择 <span class="highlight-count">{{ datasetForm.questionIds.length }}</span> 个问题
              </div>
            </div>
            
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
              
              <el-select v-model="questionFilter.questionType" placeholder="题型" clearable>
                <el-option label="单选题" value="single_choice"></el-option>
                <el-option label="多选题" value="multiple_choice"></el-option>
                <el-option label="主观题" value="subjective"></el-option>
              </el-select>
              
              <el-button type="primary" @click="searchQuestions">筛选</el-button>
            </div>
            
            <div class="question-table-container">
              <el-table
                ref="tableRef"
                v-loading="questionLoading"
                :data="questionList"
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
              
              <div class="pagination-container">
                <div class="pagination-info">
                  总计 <span class="highlight-count">{{ questionTotal }}</span> 个问题
                  <span v-if="Math.ceil(questionTotal/questionQuery.size) > 0">
                    (共 {{ Math.ceil(questionTotal/questionQuery.size) }} 页)
                  </span>
                </div>
                <el-pagination
                  background
                  v-model:current-page="questionQuery.page"
                  v-model:page-size="questionQuery.size"
                  :page-sizes="[10, 20, 50, 100]"
                  :layout="paginationLayout"
                  :pager-count="5"
                  :total="questionTotal"
                  @size-change="handleSizeChange"
                  @current-change="handleCurrentChange">
                </el-pagination>
              </div>
            </div>
            
            <div class="selection-summary" v-if="datasetForm.questionIds && datasetForm.questionIds.length > 0">
              <el-alert
                title="问题选择"
                type="success"
                :closable="false">
                <template #default>
                  已选择 <strong>{{ datasetForm.questionIds.length }}</strong> 个问题
                  <el-button type="text" @click="clearSelection">清空选择</el-button>
                </template>
              </el-alert>
            </div>
          </div>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="submitForm">保存</el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, nextTick, onBeforeUnmount, watch, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { createDatasetVersion, checkVersionName, addQuestionsToDataset } from '@/api/dataset'
import type { DatasetVersion } from '@/api/dataset'
import { getQuestionList } from '@/api/question'
import { getAllCategories } from '@/api/category'

const router = useRouter()
const loading = ref(false)
const questionLoading = ref(false)
const datasetFormRef = ref()
const tableRef = ref()
const tableHeight = ref(450)

// 表单数据
const datasetForm = reactive<DatasetVersion>({
  name: '',
  description: '',
  releaseDate: '',
  questionIds: [] as number[]
})

// 表单验证规则
const rules = {
  name: [
    { required: true, message: '请输入数据集名称', trigger: 'blur' }
  ],
  questionIds: [
    { type: 'array', required: true, message: '请选择至少一个问题', trigger: 'change' }
  ]
}

// 问题搜索关键词
const questionSearchKeyword = ref('')

// 问题筛选条件
const questionFilter = reactive({
  categoryId: undefined,
  difficulty: undefined,
  questionType: undefined
})

// 问题查询参数
const questionQuery = reactive({
  page: 1,         // 页码，从1开始
  size: 20,        // 默认每页显示20条
  keyword: '',     // 关键词搜索
  categoryId: undefined, // 分类ID筛选
  difficulty: undefined, // 难度筛选
  questionType: undefined, // 问题类型筛选
  sortBy: 'createdAt',  // 排序字段
  sortDir: 'desc'       // 排序方向
})

// 问题列表
const questionList = ref<any[]>([])
const questionTotal = ref(0)
// 用于跨页保存选中状态
const selectedQuestionMap = ref(new Map<number, any>())

// 分类选项
const categoryOptions = ref<Array<{value: number, label: string}>>([])

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
    difficulty: questionFilter.difficulty,
    questionType: questionFilter.questionType
  }))
  
  // 重置分页到第一页
  questionQuery.page = 1
  questionQuery.keyword = questionSearchKeyword.value
  questionQuery.categoryId = questionFilter.categoryId
  questionQuery.difficulty = questionFilter.difficulty
  questionQuery.questionType = questionFilter.questionType
  
  console.log('更新后的查询参数:', JSON.stringify(questionQuery))
  fetchQuestions()
}

// 获取问题列表
const fetchQuestions = async () => {
  questionLoading.value = true
  try {
    // 确保page和size参数有效
    if (questionQuery.page < 1) questionQuery.page = 1
    if (questionQuery.size < 1) questionQuery.size = 20
    
    const queryParams = { ...questionQuery }
    
    console.log('请求问题列表，参数:', JSON.stringify(queryParams))
    
    const res = await getQuestionList(queryParams)
    
    if (res && typeof res === 'object' && 'code' in res && res.code === 200) {
      // 兼容不同的后端返回格式
      let content = [], total = 0
      
      // 检查后端返回的具体格式
      if (res.data && typeof res.data === 'object') {
        // SpringBoot标准分页格式
        if ('content' in res.data && 'totalElements' in res.data) {
          content = Array.isArray(res.data.content) ? res.data.content : []
          total = typeof res.data.totalElements === 'number' ? res.data.totalElements : 0
          console.log(`标准分页格式: 内容=${content.length}项, 总数=${total}, 总页数=${res.data.totalPages || 0}`)
        } 
        // 我们自己的API的格式
        else if ('content' in res.data && 'total' in res.data) {
          content = Array.isArray(res.data.content) ? res.data.content : []
          total = typeof res.data.total === 'number' ? res.data.total : 0
          console.log(`自定义分页格式: 内容=${content.length}项, 总数=${total}`)
        }
        // 兼容其他格式
        else {
          const possibleContentKeys = ['content', 'records', 'list', 'items', 'data', 'results']
          const possibleTotalKeys = ['totalElements', 'total', 'totalCount', 'totalItems', 'count']
          
          // 查找内容数组
          for (const key of possibleContentKeys) {
            if (key in res.data && Array.isArray(res.data[key])) {
              content = res.data[key]
              console.log(`找到内容数组: ${key}, 长度=${content.length}`)
              break
            }
          }
          
          // 查找总数
          for (const key of possibleTotalKeys) {
            if (key in res.data && typeof res.data[key] === 'number') {
              total = res.data[key]
              console.log(`找到总数字段: ${key}, 值=${total}`)
              break
            }
          }
          
          // 如果找不到内容数组，检查res.data本身是否为数组
          if (content.length === 0 && Array.isArray(res.data)) {
            content = res.data
            total = res.data.length
            console.log(`res.data本身是数组，长度=${content.length}`)
          }
        }
      } else if (Array.isArray(res.data)) {
        content = res.data
        total = res.data.length
        console.log(`res.data是数组，长度=${content.length}`)
      }
      
      // 更新数据
      questionList.value = content
      
      // 确保总数有效
      if (total === 0 && content.length > 0) {
        total = content.length * 3  // 假设至少有3页数据
        console.warn('总数为0但有内容，假设总数为:', total)
      }
      
      // 更新总数
      questionTotal.value = Math.max(total, content.length)
      
      const totalPages = Math.ceil(questionTotal.value / questionQuery.size)
      console.log(`分页信息: 当前页=${questionQuery.page}/${totalPages}, 每页=${questionQuery.size}, 总条数=${questionTotal.value}`)
      
      // 选中当前页已选择的项
      nextTick(() => {
        if (tableRef.value && datasetForm.questionIds && Array.isArray(datasetForm.questionIds) && datasetForm.questionIds.length > 0) {
          try {
            // 找出当前页面上存在的已选项
            const rowsToSelect = questionList.value.filter(q => 
              datasetForm.questionIds!.includes(q.standardQuestionId)
            )
            
            // 选中这些行
            rowsToSelect.forEach(row => {
              tableRef.value.toggleRowSelection(row, true)
            })
            
            console.log(`在当前页面选中了 ${rowsToSelect.length} 行`)
          } catch (err) {
            console.error('设置选中行失败:', err)
          }
        }
      })
    } else {
      questionList.value = []
      questionTotal.value = 0
      console.error('获取问题列表返回数据格式不正确', res)
    }
  } catch (error) {
    console.error('获取问题列表失败', error)
    questionList.value = []
    questionTotal.value = 0
  } finally {
    questionLoading.value = false
  }
}

// 处理表格选择变化
const handleSelectionChange = (selection: any[]) => {
  // 将当前页面的选择状态更新到映射中
  questionList.value.forEach(item => {
    const isSelected = selection.some(sel => sel.standardQuestionId === item.standardQuestionId)
    if (isSelected) {
      selectedQuestionMap.value.set(item.standardQuestionId, item)
    } else {
      selectedQuestionMap.value.delete(item.standardQuestionId)
    }
  })
  
  // 更新表单数据
  datasetForm.questionIds = Array.from(selectedQuestionMap.value.keys())
  
  // 打印调试信息
  console.log(`选择了 ${datasetForm.questionIds.length} 个问题`)
  console.log('选择的问题ID:', datasetForm.questionIds)
  console.log('selectedQuestionMap大小:', selectedQuestionMap.value.size)
}

// 清空选择
const clearSelection = () => {
  if (tableRef.value) {
    tableRef.value.clearSelection()
  }
  selectedQuestionMap.value.clear()
  datasetForm.questionIds = []
}

// 处理每页数量变化
const handleSizeChange = (val: number) => {
  console.log('修改每页显示数量:', val)
  questionQuery.size = val
  // 重置到第一页，避免页码超出范围
  questionQuery.page = 1
  fetchQuestions()
}

// 处理页码变化
const handleCurrentChange = (val: number) => {
  console.log('修改当前页码:', val)
  questionQuery.page = val
  fetchQuestions()
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

// 计算表格高度
const calculateTableHeight = () => {
  nextTick(() => {
    const windowHeight = window.innerHeight
    tableHeight.value = Math.max(400, windowHeight * 0.4)
    console.log('设置表格高度:', tableHeight.value)
  })
}

// 提交表单
const submitForm = async () => {
  if (!datasetFormRef.value) return
  
  await datasetFormRef.value.validate(async (valid: boolean) => {
    if (!valid) return
    
    if (!datasetForm.questionIds || datasetForm.questionIds.length === 0) {
      ElMessage.warning('请选择至少一个问题')
      return
    }
    
    // 检查名称是否已存在
    try {
      const res = await checkVersionName(datasetForm.name)
      if (res.data?.exists) {
        ElMessage.warning('数据集名称已存在，请修改后重试')
        return
      }
      
      // 打印表单数据，检查数据是否正确
      console.log('准备提交数据集表单:', JSON.stringify(datasetForm, null, 2))
      console.log('选择的问题ID数量:', datasetForm.questionIds?.length)
      console.log('选择的问题ID:', datasetForm.questionIds)
      
      // 检查问题ID数组格式是否正确
      if (datasetForm.questionIds && !Array.isArray(datasetForm.questionIds)) {
        console.error('问题ID不是数组格式', typeof datasetForm.questionIds)
        ElMessage.error('问题ID格式不正确，请重新选择问题')
        return
      }
      
      loading.value = true
      try {
        // 创建数据集版本，直接传递所有数据（包括问题ID）
        const result = await createDatasetVersion({
          name: datasetForm.name,
          description: datasetForm.description,
          releaseDate: datasetForm.releaseDate,
          questionIds: datasetForm.questionIds
        })
        
        console.log('创建数据集结果:', result)
        
        // 解析返回结果
        let createdVersion: any = null
        if (result && typeof result === 'object') {
          if ('code' in result && result.code === 200 && result.data) {
            createdVersion = result.data
          } else if ('versionId' in result) {
            createdVersion = result
          } else {
            createdVersion = result
          }
        }
        
        // 检查是否需要额外调用添加问题接口
        if (createdVersion && createdVersion.versionId && datasetForm.questionIds && datasetForm.questionIds.length > 0) {
          // 检查返回的数据中是否已包含问题数量
          const hasQuestions = createdVersion.questionCount && createdVersion.questionCount > 0
          
          if (!hasQuestions) {
            console.log(`向数据集版本 ${createdVersion.versionId} 添加 ${datasetForm.questionIds.length} 个问题`)
            try {
              // 添加问题到数据集版本
              await addQuestionsToDataset(createdVersion.versionId, datasetForm.questionIds)
              console.log('问题添加成功')
            } catch (error) {
              console.error('添加问题到数据集失败', error)
              ElMessage.warning('数据集创建成功，但问题关联失败，请在详情页重试')
            }
          } else {
            console.log('数据集已包含问题，无需额外添加')
          }
        }
        
        ElMessage.success('创建成功')
        router.push('/datasets')
      } catch (error: any) {
        console.error('创建数据集失败', error)
        if (error.response) {
          console.error('服务器返回状态码:', error.response.status)
          console.error('服务器返回数据:', error.response.data)
        }
        ElMessage.error('创建失败: ' + (error.message || '未知错误'))
      } finally {
        loading.value = false
      }
    } catch (error: any) {
      console.error('检查数据集名称失败', error)
      ElMessage.error('操作失败: ' + (error.message || '未知错误'))
      loading.value = false
    }
  })
}

// 重置表单
const resetForm = () => {
  if (!datasetFormRef.value) return
  datasetFormRef.value.resetFields()
  selectedQuestionMap.value.clear()
  datasetForm.questionIds = []
  if (tableRef.value) {
    tableRef.value.clearSelection()
  }
}

// 返回上一页
const navigateBack = () => {
  router.back()
}

// 分页布局计算
const paginationLayout = computed(() => {
  // 根据屏幕尺寸决定分页组件布局
  const isSmallScreen = window.innerWidth < 768;
  return isSmallScreen 
    ? 'prev, pager, next, jumper, sizes, total' 
    : 'sizes, prev, pager, next, jumper, total';
});

// 监听窗口大小变化，动态调整表格高度和分页布局
const handleResize = () => {
  calculateTableHeight();
};

// 监听分页变化
watch(
  () => [questionQuery.page, questionQuery.size],
  ([newPage, newSize], [oldPage, oldSize]) => {
    console.log(`分页变化: 页码 ${oldPage} -> ${newPage}, 每页数量 ${oldSize} -> ${newSize}`);
    // 如果是页码变化或每页数量变化，重新获取数据
    if (newPage !== oldPage || newSize !== oldSize) {
      fetchQuestions();
    }
  }
);

// 添加页面加载完成后的初始化处理
onMounted(() => {
  getCategories()
  calculateTableHeight()
  
  // 延迟加载问题列表，确保UI先渲染
  setTimeout(() => {
    fetchQuestions()
  }, 100)
  
  window.addEventListener('resize', handleResize)
  
  // 添加调试信息
  console.log('组件已挂载，初始化完成')
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.dataset-create-container {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.form-card {
  margin-bottom: 20px;
}

.question-selection {
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  padding: 15px;
}

.question-search-bar {
  margin-bottom: 15px;
}

.question-filter-bar {
  display: flex;
  gap: 15px;
  margin-bottom: 15px;
  flex-wrap: wrap;
}

.question-table-container {
  margin-bottom: 15px;
}

.question-content {
  max-height: 100px;
  overflow-y: auto;
  line-height: 1.5;
  white-space: pre-wrap;
  word-break: break-all;
}

.pagination-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 15px;
  flex-wrap: wrap;
  gap: 10px;
}

.pagination-info {
  font-size: 14px;
  color: #606266;
  white-space: nowrap;
}

.highlight-count {
  font-weight: bold;
  color: #409EFF;
  margin: 0 3px;
}

.selection-summary {
  margin-top: 15px;
}

/* 确保在小屏幕上分页组件响应式显示 */
@media screen and (max-width: 768px) {
  .pagination-container {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .question-filter-bar {
    flex-wrap: wrap;
  }
}

.selection-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.selection-header h3 {
  margin: 0;
  font-size: 18px;
  color: #303133;
}

.selection-info {
  font-size: 14px;
  color: #606266;
}
</style> 