<template>
  <div class="raw-question-list">
    <div class="page-header">
      <h2>原始问题列表</h2>
      <div class="actions">
        <el-button type="primary" @click="$router.push('/import')">导入问题</el-button>
      </div>
    </div>

    <el-card shadow="never" class="filter-container">
      <el-form :inline="true" :model="queryParams" class="filter-form">
        <el-form-item label="关键词">
          <el-input v-model="queryParams.keyword" placeholder="问题标题或内容" clearable />
        </el-form-item>
        <el-form-item label="来源">
          <el-select v-model="queryParams.source" placeholder="全部来源" clearable>
            <el-option label="Stack Overflow" value="stackoverflow" />
            <el-option label="GitHub Issues" value="github" />
            <el-option label="Quora" value="quora" />
            <el-option label="手动添加" value="manual" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="table-container">
      <el-table
        v-loading="loading"
        :data="questionList"
        border
        @sort-change="handleSort"
        style="width: 100%">
        <el-table-column prop="questionId" label="ID" width="80" sortable="custom" />
        <el-table-column prop="questionTitle" label="问题标题" min-width="200">
          <template #default="{ row }">
            <el-link type="primary" @click="$router.push(`/raw-questions/detail/${row.questionId}`)">
              {{ row.questionTitle || '无标题' }}
            </el-link>
          </template>
        </el-table-column>
        <el-table-column prop="questionBody" label="问题内容" min-width="300">
          <template #default="{ row }">
            <div class="question-body-preview">{{ truncateText(row.questionBody, 100) }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="source" label="来源" width="120" sortable="custom" />
        <el-table-column prop="createdAt" label="创建时间" width="180" sortable="custom">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="$router.push(`/raw-questions/detail/${row.questionId}`)">
              查看
            </el-button>
            <el-button size="small" type="primary" @click="handleConvert(row)">
              转为标准问题
            </el-button>
            <el-popconfirm
              title="确认删除该问题吗？"
              @confirm="handleDelete(row.questionId)"
            >
              <template #reference>
                <el-button size="small" type="danger">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

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

    <!-- 转换为标准问题对话框 -->
    <el-dialog
      v-model="convertDialogVisible"
      title="转换为标准问题"
      width="650px">
      <el-form
        ref="convertFormRef"
        :model="convertForm"
        :rules="convertRules"
        label-width="100px">
        <el-form-item label="问题内容" prop="question">
          <el-input
            v-model="convertForm.question"
            type="textarea"
            :rows="4"
            placeholder="请输入问题内容" />
        </el-form-item>

        <el-form-item label="问题类型" prop="questionType">
          <el-select v-model="convertForm.questionType" placeholder="请选择问题类型">
            <el-option label="单选题" value="single_choice" />
            <el-option label="多选题" value="multiple_choice" />
            <el-option label="简单事实题" value="simple_fact" />
            <el-option label="主观题" value="subjective" />
          </el-select>
        </el-form-item>

        <el-form-item label="问题分类" prop="categoryId">
          <el-select v-model="convertForm.categoryId" placeholder="请选择问题分类">
            <el-option
              v-for="category in categories"
              :key="category.categoryId"
              :label="category.name"
              :value="category.categoryId" />
          </el-select>
        </el-form-item>

        <el-form-item label="难度" prop="difficulty">
          <el-select v-model="convertForm.difficulty" placeholder="请选择难度">
            <el-option label="简单" value="easy" />
            <el-option label="中等" value="medium" />
            <el-option label="困难" value="hard" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="convertDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitConvert" :loading="convertLoading">
            确认
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  getRawQuestionList, 
  deleteRawQuestion, 
  convertToStandardQuestion,
  RawQuestion
} from '@/api/raw'
import { getAllCategories } from '@/api/category'

// 查询参数
const queryParams = reactive({
  page: 1,
  size: 10,
  keyword: '',
  source: '',
  sortField: '',
  sortOrder: 'asc'
})

// 数据列表
const questionList = ref<RawQuestion[]>([])
const total = ref(0)
const loading = ref(false)
const categories = ref<any[]>([])

// 转换对话框
const convertDialogVisible = ref(false)
const convertLoading = ref(false)
const convertForm = reactive({
  question: '',
  questionType: '',
  categoryId: undefined as number | undefined,
  difficulty: '',
  sourceQuestionId: 0
})
const convertRules = {
  question: [{ required: true, message: '请输入问题内容', trigger: 'blur' }],
  questionType: [{ required: true, message: '请选择问题类型', trigger: 'change' }],
  categoryId: [{ required: true, message: '请选择问题分类', trigger: 'change' }]
}
const convertFormRef = ref()

// 初始化
onMounted(() => {
  getList()
  getCategories()
})

// 获取列表数据
const getList = async () => {
  if (loading.value) {
    console.log('正在加载中，跳过重复请求');
    return;
  }

  loading.value = true;
  questionList.value = []; // 清空当前列表，避免数据混乱
  
  try {
    console.log('准备查询参数...');
    const params = {
      pageNum: queryParams.page, // 后端接口已修改为从1开始计数
      pageSize: queryParams.size,
      keyword: queryParams.keyword || '',
      source: queryParams.source || '',
      sortField: queryParams.sortField || '',
      sortOrder: queryParams.sortOrder || 'asc'
    };
    
    console.log('发送请求参数:', params);
    
    // 添加超时处理
    let loadingTimeout = setTimeout(() => {
      ElMessage.warning('请求数据时间较长，请耐心等待...');
    }, 3000);
    
    // 添加超长超时处理
    let longLoadingTimeout = setTimeout(() => {
      ElMessage.warning('请求仍在处理中，后端可能需要更长时间...');
    }, 30000);
    
    try {
      const response = await getRawQuestionList(params);
      clearTimeout(loadingTimeout);
      clearTimeout(longLoadingTimeout);
      
      console.log('获取到响应数据:', response);
      
      // 检查响应结构
      if (!response) {
        throw new Error('未收到服务器返回的数据');
      }
      
      // 适应后端返回的数据结构
      const res = response.data || response;
      
      // 检查数据格式
      if (!res.content) {
        console.error('响应数据格式不符合预期:', res);
        throw new Error('服务器返回的数据格式不正确');
      }
      
      // 检查数据内容
      if (Array.isArray(res.content)) {
        console.log(`接收到${res.content.length}条数据记录`);
      } else {
        console.warn('content不是数组:', res.content);
      }
      
      // 更新状态
      questionList.value = Array.isArray(res.content) ? res.content : [];
      total.value = res.totalElements || 0;
      
      // 记录分页信息
      console.log(`分页信息: 总计${res.totalElements}条数据，共${res.totalPages}页，当前第${res.pageNumber}页，每页${res.pageSize}条`);
      
      // 如果当前页没有数据但总数不为0，回到第一页
      if (questionList.value.length === 0 && total.value > 0 && queryParams.page > 1) {
        ElMessage.warning('当前页没有数据，将返回第一页');
        queryParams.page = 1;
        setTimeout(() => {
          getList();
        }, 100);
        return;
      }
      
      console.log(`成功加载 ${questionList.value.length} 条数据，总数: ${total.value}`);
    } catch (error) {
      clearTimeout(loadingTimeout);
      clearTimeout(longLoadingTimeout);
      throw error;
    }
  } catch (error: any) {
    console.error('获取问题列表失败:', error);
    
    let errorMsg = '未知错误';
    if (error.message) {
      errorMsg = error.message;
    } else if (error.response && error.response.data) {
      errorMsg = error.response.data.error || error.response.data.message || '服务器响应错误';
    }
    
    ElMessage.error('获取问题列表失败: ' + errorMsg);
    questionList.value = [];
    total.value = 0;
  } finally {
    loading.value = false;
    console.log('请求完成，加载状态设置为:', loading.value);
  }
};

// 获取分类数据
const getCategories = async () => {
  try {
    const response = await getAllCategories()
    categories.value = (response.data || response) || []
  } catch (error: any) {
    ElMessage.error('获取分类列表失败: ' + error.message)
  }
}

// 搜索
const handleSearch = () => {
  queryParams.page = 1
  getList()
}

// 重置查询
const resetQuery = () => {
  queryParams.keyword = ''
  queryParams.source = ''
  queryParams.page = 1
  getList()
}

// 分页大小变化
const handleSizeChange = (size: number) => {
  queryParams.size = size
  getList()
}

// 页码变化
const handleCurrentChange = (page: number) => {
  queryParams.page = page
  getList()
}

// 删除
const handleDelete = async (id: number) => {
  try {
    const response = await deleteRawQuestion(id)
    // 适应后端返回的数据结构
    const result = response.data || response
    ElMessage.success('删除成功')
    getList()
  } catch (error: any) {
    ElMessage.error('删除失败: ' + error.message)
  }
}

// 转换为标准问题
const handleConvert = (row: RawQuestion) => {
  convertForm.question = row.questionBody
  convertForm.questionType = 'subjective' // 默认为主观题
  convertForm.difficulty = 'medium' // 默认为中等难度
  convertForm.sourceQuestionId = row.questionId
  convertDialogVisible.value = true
}

// 提交转换
const submitConvert = async () => {
  if (!convertFormRef.value) return
  
  await convertFormRef.value.validate(async (valid: boolean) => {
    if (!valid) return
    
    convertLoading.value = true
    try {
      const response = await convertToStandardQuestion(convertForm.sourceQuestionId, {
        question: convertForm.question,
        questionType: convertForm.questionType,
        categoryId: convertForm.categoryId,
        difficulty: convertForm.difficulty
      })
      
      // 适应后端返回的数据结构
      const result = response.data || response
      
      ElMessage.success('转换成功')
      convertDialogVisible.value = false
    } catch (error: any) {
      ElMessage.error('转换失败: ' + error.message)
    } finally {
      convertLoading.value = false
    }
  })
}

// 格式化日期
const formatDate = (dateStr?: string) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleString()
}

// 截断文本
const truncateText = (text: string, length: number) => {
  if (!text) return ''
  return text.length > length ? text.substring(0, length) + '...' : text
}

// 添加排序方法
const handleSort = (column: { prop: string, order: string }) => {
  queryParams.sortField = column.prop;
  queryParams.sortOrder = column.order === 'ascending' ? 'asc' : 'desc';
  getList();
}
</script>

<style scoped>
.raw-question-list {
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

.table-container {
  margin-bottom: 20px;
}

.question-body-preview {
  white-space: pre-wrap;
  word-break: break-all;
  line-height: 1.5;
}

.pagination-container {
  margin-top: 20px;
  text-align: right;
}
</style> 