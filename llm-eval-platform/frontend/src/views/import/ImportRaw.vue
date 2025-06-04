<template>
  <div class="import-container">
    <div class="page-header">
      <h2>原始问答数据导入</h2>
    </div>

    <el-card shadow="never" class="import-card">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="文件导入" name="file">
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
                action="/api/import/raw-qa"
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
                    请上传 {{ importType.toUpperCase() }} 格式的文件，文件大小不超过10MB
                  </div>
                </template>
              </el-upload>
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
            {{ scope.row.type === 'file' ? '文件导入' : 'API导入' }}
          </template>
        </el-table-column>
        <el-table-column prop="source" label="数据源" />
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
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'

// 当前活动的标签页
const activeTab = ref('file')

// 文件导入类型
const importType = ref('json')

// // API导入表单
// const apiForm = reactive({
//   source: 'stackoverflow',
//   url: '',
//   auth: '',
//   keywords: '',
//   limit: 100
// })

// 上传相关
const uploadHeaders = reactive({
  // 如果需要认证令牌，可以在这里添加
})

// 导入状态
const importing = ref(false)

// // 模拟的导入历史记录
// const importHistory = ref([
//   {
//     id: 1,
//     type: 'file',
//     source: 'local',
//     fileName: 'stackoverflow_python_qa.json',
//     status: 'success',
//     importTime: '2023-05-10 14:30:22',
//     count: 120
//   },
//   {
//     id: 2,
//     type: 'api',
//     source: 'stackoverflow',
//     fileName: null,
//     status: 'success',
//     importTime: '2023-05-09 10:15:45',
//     count: 85
//   },
//   {
//     id: 3,
//     type: 'file',
//     source: 'local',
//     fileName: 'zhihu_ai_qa.csv',
//     status: 'failed',
//     importTime: '2023-05-08 16:22:10',
//     count: 0
//   }
// ])

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
  ElMessage.error('文件上传失败，请请检查对应的文件是否正确')
}

// API导入处理
const handleApiImport = () => {
  if (!apiForm.keywords) {
    ElMessage.warning('请输入搜索关键词')
    return
  }

  importing.value = true
  
  // 模拟API导入过程
  setTimeout(() => {
    importing.value = false
    ElMessage.success('API导入任务已提交，请查看导入历史记录获取进度')
    // 这里可以添加更新导入历史的逻辑
  }, 2000)
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