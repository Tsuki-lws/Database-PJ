<template>
  <div class="import-container">
    <div class="page-header">
      <h2>标准问答数据导入</h2>
    </div>

    <el-card shadow="never" class="import-card">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="问答对导入" name="qa-pairs">
          <el-form label-position="top">
            <el-form-item label="选择导入文件类型">
              <el-radio-group v-model="importType">
                <el-radio label="json">JSON格式</el-radio>
                <el-radio label="csv">CSV格式</el-radio>
                <el-radio label="excel">Excel格式</el-radio>
              </el-radio-group>
            </el-form-item>

            <el-form-item label="选择文件">
              <el-upload
                class="upload-demo"
                drag
                action="/api/import/standard-qa"
                :headers="uploadHeaders"
                :on-success="handleUploadSuccess"
                :on-error="handleUploadError"
                :before-upload="beforeUpload"
              >
                <el-icon class="el-icon--upload"><upload-filled /></el-icon>
                <div class="el-upload__text">
                  拖拽文件到此处或 <em>点击上传</em>
                </div>
                <template #tip>
                  <div class="el-upload__tip">
                    请上传包含标准问答对的 {{ importType.toUpperCase() }} 文件，文件大小不超过10MB
                  </div>
                </template>
              </el-upload>
            </el-form-item>

            <el-form-item label="导入选项">
              <el-checkbox v-model="importOptions.autoCreateCategories">自动创建缺失的分类</el-checkbox>
              <el-checkbox v-model="importOptions.autoCreateTags">自动创建缺失的标签</el-checkbox>
              <el-checkbox v-model="importOptions.skipDuplicates">跳过重复问题</el-checkbox>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="批量创建" name="batch-create">
          <el-form :model="batchForm" label-position="top">
            <el-form-item label="批量创建类型">
              <el-radio-group v-model="batchForm.type">
                <el-radio label="questions">标准问题</el-radio>
                <el-radio label="answers">标准答案</el-radio>
                <el-radio label="qa-pairs">问答对</el-radio>
              </el-radio-group>
            </el-form-item>

            <el-form-item label="数据内容">
              <el-input
                v-model="batchForm.content"
                type="textarea"
                :rows="10"
                placeholder="请输入批量创建的内容，每行一个项目。问答对请使用JSON格式。"
              />
            </el-form-item>

            <el-form-item label="默认分类" v-if="batchForm.type !== 'answers'">
              <el-select v-model="batchForm.categoryId" placeholder="选择默认分类">
                <el-option
                  v-for="category in categories"
                  :key="category.categoryId"
                  :label="category.name"
                  :value="category.categoryId"
                />
              </el-select>
            </el-form-item>

            <el-form-item label="默认标签" v-if="batchForm.type !== 'answers'">
              <el-select
                v-model="batchForm.tagIds"
                multiple
                collapse-tags
                placeholder="选择默认标签"
              >
                <el-option
                  v-for="tag in tags"
                  :key="tag.tagId"
                  :label="tag.tagName"
                  :value="tag.tagId"
                />
              </el-select>
            </el-form-item>

            <el-form-item>
              <el-button type="primary" @click="handleBatchCreate" :loading="creating">开始创建</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 导入历史记录
    <el-card shadow="never" class="history-card">
      <div slot="header" class="clearfix">
        <span>导入历史记录</span>
      </div>
      <el-table :data="importHistory" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="type" label="导入类型" width="120">
          <template #default="scope">
            {{ getImportTypeText(scope.row.type) }}
          </template>
        </el-table-column>
        <el-table-column prop="fileName" label="文件名" />
        <el-table-column prop="status" label="状态" width="120">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="importTime" label="导入时间" width="180" />
        <el-table-column prop="count" label="导入数量" width="120" />
        <el-table-column label="操作" width="150">
          <template #default="scope">
            <el-button link type="primary" @click="viewImportDetail(scope.row)">查看详情</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card> -->
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'

// 当前活动的标签页
const activeTab = ref('qa-pairs')

// 文件导入类型
const importType = ref('json')

// 导入选项
const importOptions = reactive({
  autoCreateCategories: true,
  autoCreateTags: true,
  skipDuplicates: true
})

// 批量创建表单
const batchForm = reactive({
  type: 'questions',
  content: '',
  categoryId: null as number | null,
  tagIds: [] as number[]
})

// 上传相关
const uploadHeaders = reactive({
  // 如果需要认证令牌，可以在这里添加
})

// 状态
const creating = ref(false)

// 分类和标签数据
const categories = ref<any[]>([])
const tags = ref<any[]>([])

// 模拟的导入历史记录
const importHistory = ref([
  {
    id: 1,
    type: 'qa-pairs',
    fileName: 'standard_qa_pairs.json',
    status: 'success',
    importTime: '2023-05-15 09:30:22',
    count: 50
  },
  {
    id: 2,
    type: 'questions',
    fileName: 'standard_questions.csv',
    status: 'success',
    importTime: '2023-05-14 16:45:10',
    count: 75
  },
  {
    id: 3,
    type: 'answers',
    fileName: 'standard_answers.json',
    status: 'processing',
    importTime: '2023-05-15 10:22:10',
    count: 0
  }
])

// 初始化
onMounted(() => {
  fetchCategories()
  fetchTags()
})

// 获取分类列表
const fetchCategories = () => {
  // 模拟API调用
  categories.value = [
    { categoryId: 1, name: '编程' },
    { categoryId: 2, name: '数学' },
    { categoryId: 3, name: '物理' },
    { categoryId: 4, name: '化学' },
    { categoryId: 5, name: '生物' }
  ]
}

// 获取标签列表
const fetchTags = () => {
  // 模拟API调用
  tags.value = [
    { tagId: 1, tagName: 'Python' },
    { tagId: 2, tagName: 'Java' },
    { tagId: 3, tagName: 'JavaScript' },
    { tagId: 4, tagName: 'C++' },
    { tagId: 5, tagName: 'AI' },
    { tagId: 6, tagName: '机器学习' }
  ]
}

// 上传前的检查
const beforeUpload = (file: any) => {
  const isValidType = checkFileType(file)
  const isLt10M = file.size / 1024 / 1024 < 10

  if (!isValidType) {
    ElMessage.error('请上传正确的文件格式!')
    return false
  }
  if (!isLt10M) {
    ElMessage.error('文件大小不能超过 10MB!')
    return false
  }
  return true
}

// 检查文件类型
const checkFileType = (file: any) => {
  if (importType.value === 'json') {
    return file.type === 'application/json'
  } else if (importType.value === 'csv') {
    return file.type === 'text/csv'
  } else if (importType.value === 'excel') {
    return file.type === 'application/vnd.ms-excel' || 
           file.type === 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
  }
  return false
}

// 上传成功处理
const handleUploadSuccess = (response: any) => {
  ElMessage.success('文件上传成功')
  // 这里可以添加更新导入历史的逻辑
}

// 上传失败处理
const handleUploadError = () => {
  ElMessage.error('文件上传失败，请检查对应的文件是否正确')
}

// 批量创建处理
const handleBatchCreate = () => {
  if (!batchForm.content) {
    ElMessage.warning('请输入批量创建的内容')
    return
  }

  if (batchForm.type !== 'answers' && !batchForm.categoryId) {
    ElMessage.warning('请选择默认分类')
    return
  }

  creating.value = true
  
  // 模拟批量创建过程
  setTimeout(() => {
    creating.value = false
    ElMessage.success('批量创建任务已提交，请查看导入历史记录获取进度')
    // 这里可以添加更新导入历史的逻辑
  }, 2000)
}

// 获取导入类型文本
const getImportTypeText = (type: string) => {
  switch (type) {
    case 'qa-pairs': return '问答对'
    case 'questions': return '标准问题'
    case 'answers': return '标准答案'
    default: return '未知'
  }
}

// 获取状态类型（用于标签颜色）
const getStatusType = (status: string) => {
  switch (status) {
    case 'success': return 'success'
    case 'processing': return 'warning'
    case 'failed': return 'danger'
    default: return 'info'
  }
}

// 获取状态文本
const getStatusText = (status: string) => {
  switch (status) {
    case 'success': return '成功'
    case 'processing': return '处理中'
    case 'failed': return '失败'
    default: return '未知'
  }
}

// 查看导入详情
const viewImportDetail = (row: any) => {
  ElMessage.info(`查看导入ID为 ${row.id} 的详情`)
  // 这里可以添加导航到详情页面的逻辑
}
</script>

<style scoped>
.import-container {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.import-card {
  margin-bottom: 20px;
}

.history-card {
  margin-top: 30px;
}

.el-upload {
  width: 100%;
}
</style> 