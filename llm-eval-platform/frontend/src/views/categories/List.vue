<template>
  <div class="category-list-container">
    <div class="page-header">
      <h2>问题分类管理</h2>
      <el-button type="primary" @click="showAddDialog">
        <el-icon><Plus /></el-icon>添加分类
      </el-button>
    </div>

    <!-- 分类列表 -->
    <el-card shadow="never" class="list-card">
      <el-table v-loading="loading" :data="displayedCategories" style="width: 100%">
        <el-table-column prop="categoryId" label="ID" width="80" />
        <el-table-column prop="name" label="分类名称" />
        <el-table-column prop="description" label="描述" show-overflow-tooltip />
        <el-table-column label="父分类" width="120">
          <template #default="scope">
            {{ scope.row.parent ? scope.row.parent.name : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="questionCount" label="问题数量" width="100" />
        <el-table-column fixed="right" label="操作" width="200">
          <template #default="scope">
            <el-button link type="primary" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 添加/编辑对话框 -->
    <el-dialog 
      v-model="dialogVisible" 
      :title="dialogType === 'add' ? '添加分类' : '编辑分类'" 
      width="500px">
      <el-form 
        :model="categoryForm" 
        :rules="rules" 
        ref="categoryFormRef" 
        label-width="80px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="categoryForm.name" placeholder="请输入分类名称"></el-input>
        </el-form-item>
        <el-form-item label="父分类" prop="parent">
          <el-select v-model="categoryForm.parent" placeholder="请选择父分类" clearable>
            <el-option
              v-for="item in categoryList"
              :key="item.categoryId"
              :label="item.name"
              :value="item.categoryId"
              :disabled="dialogType === 'edit' && item.categoryId === categoryForm.categoryId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input 
            v-model="categoryForm.description" 
            type="textarea" 
            :rows="3" 
            placeholder="请输入分类描述">
          </el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitForm">确认</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessageBox, ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getAllCategories, getCategoriesWithQuestionCount, createCategory, updateCategory, deleteCategory } from '@/api/category'
import type { Category } from '@/api/category'

const loading = ref(false)
const categoryList = ref<any[]>([])
const total = ref(0)
const dialogVisible = ref(false)
const dialogType = ref<'add' | 'edit'>('add')
const categoryFormRef = ref()
const currentPage = ref(1)
const pageSize = ref(10)

// 分页后的数据
const displayedCategories = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return categoryList.value.slice(start, end)
})

// 表单数据
const categoryForm = reactive({
  categoryId: 0,
  name: '',
  description: '',
  parent: null as number | null
})

// 表单验证规则
const rules = {
  name: [
    { required: true, message: '请输入分类名称', trigger: 'blur' }
  ]
}

// 获取分类列表
const getList = async () => {
  loading.value = true
  try {
    // 使用带问题数量的API
    const response = await getCategoriesWithQuestionCount()
    
    // 检查响应格式
    if (response && response.data && Array.isArray(response.data)) {
      // 响应是包含data数组的对象
      categoryList.value = response.data
      total.value = response.data.length
    } else if (Array.isArray(response)) {
      // 响应直接是数组
      categoryList.value = response
      total.value = response.length
    } else {
      categoryList.value = []
      total.value = 0
      console.error('获取的分类数据格式错误:', response)
      ElMessage.error('获取分类列表失败：数据格式错误')
    }
  } catch (error) {
    console.error('获取分类列表失败', error)
    categoryList.value = []
    total.value = 0
    ElMessage.error('获取分类列表失败，请检查网络连接')
  } finally {
    loading.value = false
  }
}

// 每页数量变化
const handleSizeChange = (val: number) => {
  pageSize.value = val
  currentPage.value = 1 // 重置到第一页
}

// 当前页变化
const handleCurrentChange = (val: number) => {
  currentPage.value = val
}

// 显示添加对话框
const showAddDialog = () => {
  dialogType.value = 'add'
  resetForm()
  dialogVisible.value = true
}

// 编辑分类
const handleEdit = (row: any) => {
  dialogType.value = 'edit'
  resetForm()
  categoryForm.categoryId = row.categoryId
  categoryForm.name = row.name
  categoryForm.description = row.description
  categoryForm.parent = row.parent ? row.parent.categoryId : null
  dialogVisible.value = true
}

// 删除分类
const handleDelete = (row: any) => {
  ElMessageBox.confirm(
    `确认删除分类 "${row.name}" 吗？`,
    '删除确认',
    {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      const response = await deleteCategory(row.categoryId)
      console.log('删除成功，响应数据:', response)
      ElMessage.success('删除成功')
      getList()
    } catch (error) {
      console.error('删除分类失败', error)
    }
  }).catch(() => {})
}

// 提交表单
const submitForm = async () => {
  if (!categoryFormRef.value) return
  
  await categoryFormRef.value.validate(async (valid: boolean) => {
    if (!valid) return
    
    try {
      const formData: any = {
        name: categoryForm.name,
        description: categoryForm.description
      }
      
      if (categoryForm.parent) {
        formData.parent = {
          categoryId: categoryForm.parent
        }
      }
      
      let response
      if (dialogType.value === 'add') {
        response = await createCategory(formData)
        ElMessage.success('添加成功')
      } else {
        response = await updateCategory(categoryForm.categoryId, formData)
        ElMessage.success('更新成功')
      }
      
      console.log('操作成功，响应数据:', response)
      dialogVisible.value = false
      getList()
    } catch (error) {
      console.error(dialogType.value === 'add' ? '添加分类失败' : '更新分类失败', error)
    }
  })
}

// 重置表单
const resetForm = () => {
  categoryForm.categoryId = 0
  categoryForm.name = ''
  categoryForm.description = ''
  categoryForm.parent = null
  if (categoryFormRef.value) {
    categoryFormRef.value.resetFields()
  }
}

onMounted(() => {
  getList()
})
</script>

<style scoped>
.category-list-container {
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