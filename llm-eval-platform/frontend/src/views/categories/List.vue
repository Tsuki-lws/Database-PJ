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
      <el-table v-loading="loading" :data="categoryList" style="width: 100%">
        <el-table-column prop="categoryId" label="ID" width="80" />
        <el-table-column prop="name" label="分类名称" />
        <el-table-column prop="description" label="描述" show-overflow-tooltip />
        <el-table-column prop="questionCount" label="问题数量" width="100" />
        <el-table-column prop="createdAt" label="创建时间" width="180" />
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
import { ref, reactive, onMounted } from 'vue'
import { ElMessageBox, ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getAllCategories, createCategory, updateCategory, deleteCategory } from '@/api/category'

const loading = ref(false)
const categoryList = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const dialogType = ref<'add' | 'edit'>('add')
const categoryFormRef = ref()

// 查询参数
const queryParams = reactive({
  page: 1,
  size: 10
})

// 表单数据
const categoryForm = reactive({
  categoryId: 0,
  name: '',
  description: ''
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
    const res = await getAllCategories()
    categoryList.value = res || []
    total.value = res.length || 0
  } catch (error) {
    console.error('获取分类列表失败', error)
  } finally {
    loading.value = false
  }
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
      await deleteCategory(row.categoryId)
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
      if (dialogType.value === 'add') {
        await createCategory(categoryForm)
        ElMessage.success('添加成功')
      } else {
        await updateCategory(categoryForm.categoryId, categoryForm)
        ElMessage.success('更新成功')
      }
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