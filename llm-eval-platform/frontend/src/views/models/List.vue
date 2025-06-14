<template>
  <div class="model-list-container">
    <div class="page-header">
      <h2>模型管理</h2>
      <el-button type="primary" @click="navigateToCreate">
        <el-icon><Plus /></el-icon>创建模型
      </el-button>
    </div>

    <el-card shadow="never" class="filter-container">
      <el-form :model="queryParams" label-width="80px" :inline="true">
        <el-form-item label="名称">
          <el-input v-model="queryParams.name" placeholder="模型名称" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="提供商">
          <el-input v-model="queryParams.provider" placeholder="提供商" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="list-card">
      <el-table v-loading="loading" :data="modelList" style="width: 100%">
        <el-table-column prop="modelId" label="ID" width="80" />
        <el-table-column prop="name" label="模型名称" />
        <el-table-column prop="version" label="版本" width="120" />
        <el-table-column label="提供商" width="150">
          <template #default="scope">
            {{ scope.row.provider || '无' }}
          </template>
        </el-table-column>
        <el-table-column label="描述" show-overflow-tooltip>
          <template #default="scope">
            {{ scope.row.description || '无' }}
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column fixed="right" label="操作" width="180">
          <template #default="scope">
            <el-button link type="primary" @click="handleEdit(scope.row)">编辑</el-button>
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

    <!-- 编辑对话框 -->
    <el-dialog
      v-model="editDialogVisible"
      :title="dialogTitle"
      width="600px"
      destroy-on-close
    >
      <el-form
        ref="modelFormRef"
        :model="modelForm"
        :rules="modelRules"
        label-width="100px"
      >
        <el-form-item label="模型名称" prop="name">
          <el-input v-model="modelForm.name" placeholder="请输入模型名称" />
        </el-form-item>
        <el-form-item label="版本" prop="version">
          <el-input v-model="modelForm.version" placeholder="请输入版本号" />
        </el-form-item>
        <el-form-item label="提供商" prop="provider">
          <el-input v-model="modelForm.provider" placeholder="请输入提供商" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="modelForm.description"
            type="textarea"
            rows="3"
            placeholder="请输入模型描述"
          />
        </el-form-item>
        <el-form-item label="API配置" prop="apiConfig">
          <el-input
            v-model="modelForm.apiConfig"
            type="textarea"
            rows="5"
            placeholder="请输入API配置信息（JSON格式）"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getModelList, createModel, updateModel, deleteModel } from '@/api/evaluation'
import type { LlmModel } from '@/api/evaluation'
import type { FormInstance, FormRules } from 'element-plus'

const router = useRouter()
const loading = ref(false)
const submitLoading = ref(false)
const modelList = ref<LlmModel[]>([])
const total = ref(0)
const editDialogVisible = ref(false)
const isEdit = ref(false)
const modelFormRef = ref<FormInstance>()

// 查询参数
const queryParams = reactive({
  page: 1,
  size: 10,
  name: '',
  provider: ''
})

// 表单数据
const modelForm = reactive<LlmModel>({
  name: '',
  version: '',
  provider: '',
  description: '',
  apiConfig: '{}'
})

// 表单验证规则
const modelRules = reactive<FormRules>({
  name: [
    { required: true, message: '请输入模型名称', trigger: 'blur' },
    { min: 1, max: 50, message: '长度在 1 到 50 个字符', trigger: 'blur' }
  ],
  version: [
    { required: true, message: '请输入版本号', trigger: 'blur' }
  ]
})

// 对话框标题
const dialogTitle = computed(() => isEdit.value ? '编辑模型' : '创建模型')

// 获取模型列表
const fetchModelList = async () => {
  loading.value = true
  try {
    // 调用API获取数据
    const res = await getModelList()
    console.log('模型列表响应数据:', res)
    
    if (Array.isArray(res)) {
      modelList.value = res
      total.value = res.length
    } else if (res && typeof res === 'object') {
      const responseObj = res as any // 使用类型断言
      if (responseObj.data && Array.isArray(responseObj.data)) {
        // 处理嵌套在data中的数组
        modelList.value = responseObj.data
        total.value = responseObj.data.length
      } else if (responseObj.content && Array.isArray(responseObj.content)) {
        // 处理分页数据
        modelList.value = responseObj.content
        total.value = responseObj.totalElements || responseObj.total || responseObj.content.length
      } else {
        // 尝试将整个对象作为数组处理（对象本身可能是数组）
        const modelArray = Object.values(responseObj).filter(item => 
          item && typeof item === 'object' && 'modelId' in item
        ) as LlmModel[]
        
        if (modelArray.length > 0) {
          modelList.value = modelArray
          total.value = modelArray.length
        } else {
          modelList.value = []
          total.value = 0
          console.error('未能识别的响应格式:', res)
        }
      }
    } else {
      modelList.value = []
      total.value = 0
      console.error('未能识别的响应格式:', res)
    }
  } catch (error: any) {
    ElMessage.error('获取模型列表失败: ' + error.message)
    modelList.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

// 编辑模型
const handleEdit = (row: LlmModel) => {
  isEdit.value = true
  editDialogVisible.value = true
  
  // 复制数据到表单
  Object.assign(modelForm, {
    modelId: row.modelId,
    name: row.name,
    version: row.version,
    provider: row.provider,
    description: row.description,
    apiConfig: row.apiConfig ? JSON.stringify(row.apiConfig, null, 2) : '{}'
  })
}

// 创建新模型
const navigateToCreate = () => {
  isEdit.value = false
  editDialogVisible.value = true
  
  // 重置表单数据
  Object.assign(modelForm, {
    modelId: undefined,
    name: '',
    version: '',
    provider: '',
    description: '',
    apiConfig: '{}'
  })
}

// 删除模型
const handleDelete = (row: LlmModel) => {
  if (!row.modelId) {
    ElMessage.error('模型ID无效，无法删除')
    return
  }
  
  ElMessageBox.confirm(
    `确认删除模型 "${row.name} ${row.version}" 吗？`,
    '删除确认',
    {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await deleteModel(row.modelId as number)
      ElMessage.success('删除成功')
      fetchModelList()
    } catch (error: any) {
      ElMessage.error('删除失败: ' + error.message)
    }
  }).catch(() => {})
}

// 提交表单
const submitForm = async () => {
  if (!modelFormRef.value) return
  
  await modelFormRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        // 处理API配置 - apiConfig在后端是String类型
        let apiConfigStr = modelForm.apiConfig as string
        try {
          // 验证JSON格式是否正确
          JSON.parse(apiConfigStr)
        } catch (e) {
          ElMessage.error('API配置JSON格式错误')
          submitLoading.value = false
          return
        }
        
        const submitData = {
          ...modelForm,
          apiConfig: apiConfigStr // 直接使用字符串
        }
        
        console.log('提交模型数据:', submitData)
        
        let result
        if (isEdit.value && modelForm.modelId) {
          result = await updateModel(modelForm.modelId, submitData)
          console.log('更新模型响应:', result)
          ElMessage.success('更新成功')
        } else {
          result = await createModel(submitData)
          console.log('创建模型响应:', result)
          ElMessage.success('创建成功')
        }
        
        editDialogVisible.value = false
        
        // 确保在表单提交成功后立即刷新列表
        setTimeout(() => {
          fetchModelList()
        }, 500)
      } catch (error: any) {
        console.error('模型操作失败:', error)
        if (error.response && error.response.data) {
          ElMessage.error(isEdit.value ? 
            `更新失败: ${error.response.data.message || error.message}` : 
            `创建失败: ${error.response.data.message || error.message}`)
        } else {
          ElMessage.error(isEdit.value ? 
            `更新失败: ${error.message}` : 
            `创建失败: ${error.message}`)
        }
      } finally {
        submitLoading.value = false
      }
    }
  })
}

// 搜索
const handleSearch = () => {
  queryParams.page = 1
  fetchModelList()
}

// 重置查询条件
const resetQuery = () => {
  queryParams.page = 1
  queryParams.name = ''
  queryParams.provider = ''
  fetchModelList()
}

// 每页数量变化
const handleSizeChange = (val: number) => {
  queryParams.size = val
  fetchModelList()
}

// 当前页变化
const handleCurrentChange = (val: number) => {
  queryParams.page = val
  fetchModelList()
}

onMounted(() => {
  fetchModelList()
})
</script>

<style scoped>
.model-list-container {
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