<template>
  <div class="tag-list-container">
    <div class="page-header">
      <h2>问题标签管理</h2>
      <el-button type="primary" @click="showAddDialog">
        <el-icon><Plus /></el-icon>添加标签
      </el-button>
    </div>

    <!-- 标签列表 -->
    <el-card shadow="never" class="list-card">
      <el-table v-loading="loading" :data="displayedTags" style="width: 100%">
        <el-table-column prop="tagId" label="ID" width="80" />
        <el-table-column prop="name" label="标签名称" width="180">
          <template #default="scope">
            <el-tag :style="{ backgroundColor: scope.row.color, color: getContrastColor(scope.row.color) }">
              {{ scope.row.name }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" show-overflow-tooltip />
        <el-table-column prop="color" label="颜色" width="120">
          <template #default="scope">
            <div class="color-preview" :style="{ backgroundColor: scope.row.color }"></div>
            {{ scope.row.color }}
          </template>
        </el-table-column>
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
      :title="dialogType === 'add' ? '添加标签' : '编辑标签'" 
      width="500px">
      <el-form 
        :model="tagForm" 
        :rules="rules" 
        ref="tagFormRef" 
        label-width="80px">
        <el-form-item label="名称" prop="tagName">
          <el-input v-model="tagForm.tagName" placeholder="请输入标签名称"></el-input>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input 
            v-model="tagForm.description" 
            type="textarea" 
            :rows="3" 
            placeholder="请输入标签描述">
          </el-input>
        </el-form-item>
        <el-form-item label="颜色" prop="color">
          <el-color-picker v-model="tagForm.color" show-alpha></el-color-picker>
          <span class="color-value">{{ tagForm.color }}</span>
        </el-form-item>
        <el-form-item>
          <div class="tag-preview">
            <span>预览：</span>
            <el-tag :style="{ backgroundColor: tagForm.color, color: getContrastColor(tagForm.color) }">
              {{ tagForm.tagName || '标签预览' }}
            </el-tag>
          </div>
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
import { getAllTags, getTagsWithQuestionCountDetails, createTag, updateTag, deleteTag } from '@/api/tag'

const loading = ref(false)
const tagList = ref<any[]>([])
const total = ref(0)
const dialogVisible = ref(false)
const dialogType = ref<'add' | 'edit'>('add')
const tagFormRef = ref()

// 查询参数
const queryParams = reactive({
  page: 1,
  size: 10
})

// 分页后的数据
const displayedTags = computed(() => {
  const start = (queryParams.page - 1) * queryParams.size
  const end = start + queryParams.size
  return tagList.value.slice(start, end)
})

// 表单数据
const tagForm = reactive({
  tagId: 0,
  tagName: '',
  description: '',
  color: '#409EFF'
})

// 表单验证规则
const rules = {
  tagName: [
    { required: true, message: '请输入标签名称', trigger: 'blur' }
  ],
  color: [
    { required: true, message: '请选择标签颜色', trigger: 'change' }
  ]
}

// 获取标签列表
const getList = async () => {
  loading.value = true
  try {
    // 使用带问题数量和详细信息的API
    const response = await getTagsWithQuestionCountDetails()
    
    // 处理不同的响应格式
    let tagData: any[] = []
    const responseObj = response as any
    
    if (responseObj && typeof responseObj === 'object') {
      // 处理标准返回格式：{ code: 200, data: { tags: [...] }, message: "Success" }
      if (responseObj.data && responseObj.data.tags && Array.isArray(responseObj.data.tags)) {
        tagData = responseObj.data.tags
      }
      // 处理直接返回对象的格式：{ tags: [...] }
      else if (responseObj.tags && Array.isArray(responseObj.tags)) {
        tagData = responseObj.tags
      }
      // 处理其他可能的格式
      else if (Array.isArray(responseObj)) {
        tagData = responseObj
      }
    }
    
    if (tagData.length > 0) {
      tagList.value = tagData.map(tag => ({
        tagId: tag.tagId,
        name: tag.tagName || tag.name,
        description: tag.description,
        color: tag.color || '#409EFF',
        questionCount: tag.questionCount || 0,
        createdAt: tag.createdAt
      }))
      total.value = tagList.value.length
    } else {
      console.error('获取的标签数据格式不正确:', response)
      ElMessage.error('获取标签列表失败：数据格式错误')
      tagList.value = []
      total.value = 0
    }
  } catch (error) {
    console.error('获取标签列表失败', error)
    tagList.value = []
    total.value = 0
    ElMessage.error('获取标签列表失败，请检查网络连接')
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

// 编辑标签
const handleEdit = (row: any) => {
  dialogType.value = 'edit'
  resetForm()
  tagForm.tagId = row.tagId
  tagForm.tagName = row.name
  tagForm.description = row.description
  tagForm.color = row.color
  dialogVisible.value = true
}

// 删除标签
const handleDelete = (row: any) => {
  ElMessageBox.confirm(
    `确认删除标签 "${row.name}" 吗？`,
    '删除确认',
    {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await deleteTag(row.tagId)
      ElMessage.success('删除成功')
      getList()
    } catch (error) {
      console.error('删除标签失败', error)
    }
  }).catch(() => {})
}

// 提交表单
const submitForm = async () => {
  if (!tagFormRef.value) return
  
  await tagFormRef.value.validate(async (valid: boolean) => {
    if (!valid) return
    
    try {
      if (dialogType.value === 'add') {
        await createTag(tagForm)
        ElMessage.success('添加成功')
      } else {
        await updateTag(tagForm.tagId, tagForm)
        ElMessage.success('更新成功')
      }
      dialogVisible.value = false
      getList()
    } catch (error) {
      console.error(dialogType.value === 'add' ? '添加标签失败' : '更新标签失败', error)
    }
  })
}

// 重置表单
const resetForm = () => {
  tagForm.tagId = 0
  tagForm.tagName = ''
  tagForm.description = ''
  tagForm.color = '#409EFF'
  if (tagFormRef.value) {
    tagFormRef.value.resetFields()
  }
}

// 获取与背景色对比的文字颜色（黑或白）
const getContrastColor = (hexColor: string) => {
  // 如果颜色为空或无效，返回黑色
  if (!hexColor || hexColor === 'transparent') {
    return '#000000'
  }
  
  // 处理rgba格式
  if (hexColor.startsWith('rgba')) {
    const rgba = hexColor.match(/rgba?\((\d+),\s*(\d+),\s*(\d+)(?:,\s*(\d+(?:\.\d+)?))?\)/)
    if (rgba) {
      const r = parseInt(rgba[1])
      const g = parseInt(rgba[2])
      const b = parseInt(rgba[3])
      // 计算亮度
      const brightness = (r * 299 + g * 587 + b * 114) / 1000
      return brightness > 128 ? '#000000' : '#FFFFFF'
    }
    return '#000000'
  }
  
  // 处理十六进制格式
  let hex = hexColor.replace('#', '')
  if (hex.length === 3) {
    hex = hex[0] + hex[0] + hex[1] + hex[1] + hex[2] + hex[2]
  }
  
  const r = parseInt(hex.substring(0, 2), 16)
  const g = parseInt(hex.substring(2, 4), 16)
  const b = parseInt(hex.substring(4, 6), 16)
  
  // 计算亮度
  const brightness = (r * 299 + g * 587 + b * 114) / 1000
  return brightness > 128 ? '#000000' : '#FFFFFF'
}

onMounted(() => {
  getList()
})
</script>

<style scoped>
.tag-list-container {
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

.color-preview {
  display: inline-block;
  width: 20px;
  height: 20px;
  border-radius: 4px;
  margin-right: 8px;
  vertical-align: middle;
  border: 1px solid #dcdfe6;
}

.color-value {
  margin-left: 10px;
  color: #606266;
}

.tag-preview {
  display: flex;
  align-items: center;
  gap: 10px;
}
</style> 