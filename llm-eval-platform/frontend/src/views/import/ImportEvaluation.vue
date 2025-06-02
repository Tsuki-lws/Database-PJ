<template>
  <div class="import-container">
    <div class="page-header">
      <h2>评测结果导入</h2>
    </div>

    <el-card shadow="never" class="import-card">
      <el-form label-position="top">
        <el-form-item label="选择评测批次">
          <el-select v-model="selectedBatch" placeholder="选择评测批次" filterable>
            <el-option
              v-for="batch in batches"
              :key="batch.batchId"
              :label="batch.name"
              :value="batch.batchId"
            />
          </el-select>
        </el-form-item>

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
            :action="uploadAction"
            :headers="uploadHeaders"
            :on-success="handleUploadSuccess"
            :on-error="handleUploadError"
            :before-upload="beforeUpload"
            :disabled="!selectedBatch"
          >
            <el-icon class="el-icon--upload"><upload-filled /></el-icon>
            <div class="el-upload__text">
              拖拽文件到此处或 <em>点击上传</em>
            </div>
            <template #tip>
              <div class="el-upload__tip">
                请上传 {{ importType.toUpperCase() }} 格式的评测结果文件，文件大小不超过10MB
              </div>
            </template>
          </el-upload>
        </el-form-item>
      </el-form>

      <el-divider />

      <div class="template-section">
        <h3>导入模板说明</h3>
        <p>评测结果导入文件需要包含以下字段：</p>
        <el-table :data="templateFields" style="width: 100%">
          <el-table-column prop="field" label="字段名" width="180" />
          <el-table-column prop="description" label="描述" />
          <el-table-column prop="required" label="是否必填" width="100">
            <template #default="scope">
              <el-tag :type="scope.row.required ? 'danger' : 'info'">
                {{ scope.row.required ? '必填' : '选填' }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>

        <div class="template-download">
          <p>您可以下载导入模板：</p>
          <el-button type="primary" @click="downloadTemplate('json')">JSON模板</el-button>
          <el-button type="primary" @click="downloadTemplate('csv')">CSV模板</el-button>
          <el-button type="primary" @click="downloadTemplate('excel')">Excel模板</el-button>
        </div>
      </div>
    </el-card>

    <!-- 导入历史记录 -->
    <el-card shadow="never" class="history-card">
      <div slot="header" class="clearfix">
        <span>导入历史记录</span>
      </div>
      <el-table :data="importHistory" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="batchName" label="评测批次" />
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
        <el-table-column prop="failedCount" label="失败数量" width="120" />
        <el-table-column label="操作" width="150">
          <template #default="scope">
            <el-button link type="primary" @click="viewImportDetail(scope.row)">查看详情</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'
import { getEvaluationBatches, importEvaluationResults } from '@/api/evaluations'

// 评测批次
const batches = ref([
  { batchId: 1, name: 'GPT-4 基准测试' },
  { batchId: 2, name: 'Claude 2 评测' },
  { batchId: 3, name: 'LLaMA 2 测试' },
  { batchId: 4, name: 'Mistral 7B 评测' }
])

// 选中的评测批次
const selectedBatch = ref('')

// 文件导入类型
const importType = ref('json')

// 上传地址
const uploadAction = computed(() => {
  return `/api/evaluation-batches/${selectedBatch.value}/import`
})

// 上传相关
const uploadHeaders = ref({
  // 如果需要认证令牌，可以在这里添加
})

// 模板字段
const templateFields = ref([
  { field: 'questionId', description: '标准问题ID', required: true },
  { field: 'answerId', description: 'LLM回答ID', required: true },
  { field: 'score', description: '评分（0-10分）', required: true },
  { field: 'keyPoints', description: '关键点评估结果，格式为JSON数组', required: false },
  { field: 'method', description: '评测方法（human/auto/judge_model）', required: true },
  { field: 'comments', description: '评测意见', required: false }
])

// 导入历史
const importHistory = ref([
  {
    id: 1,
    batchName: 'GPT-4 基准测试',
    fileName: 'gpt4_evaluation_results.json',
    status: 'success',
    importTime: '2023-05-15 14:30:22',
    count: 498,
    failedCount: 2
  },
  {
    id: 2,
    batchName: 'Claude 2 评测',
    fileName: 'claude2_evaluation_results.csv',
    status: 'success',
    importTime: '2023-05-14 10:15:45',
    count: 500,
    failedCount: 0
  },
  {
    id: 3,
    batchName: 'LLaMA 2 测试',
    fileName: 'llama2_evaluation_results.xlsx',
    status: 'processing',
    importTime: '2023-05-16 09:22:10',
    count: 0,
    failedCount: 0
  }
])

// 上传前的检查
const beforeUpload = (file: any) => {
  if (!selectedBatch.value) {
    ElMessage.error('请先选择评测批次!')
    return false
  }

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
  ElMessage.success(`文件上传成功，成功导入 ${response.imported} 条评测结果`)
  // 这里可以添加更新导入历史的逻辑
}

// 上传失败处理
const handleUploadError = () => {
  ElMessage.error('文件上传失败，请重试')
}

// 下载模板
const downloadTemplate = (type: string) => {
  ElMessage.info(`下载${type.toUpperCase()}格式模板`)
  // 实际实现应该是下载对应格式的模板文件
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

.template-section {
  margin-top: 20px;
}

.template-download {
  margin-top: 20px;
  display: flex;
  align-items: center;
}

.template-download p {
  margin-right: 20px;
}

.template-download .el-button {
  margin-right: 10px;
}
</style> 