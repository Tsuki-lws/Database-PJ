<template>
  <div class="import-container">
    <div class="page-header">
      <h2>数据导入</h2>
    </div>

    <el-card shadow="never" class="import-card">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="导入原始问题和回答" name="raw-qa">
          <div class="import-form">
            <el-upload
              class="upload-demo"
              drag
              :action="null"
              :auto-upload="false"
              :on-change="handleRawQAFileChange"
              :limit="1"
              accept=".json">
              <el-icon class="el-icon--upload"><upload-filled /></el-icon>
              <div class="el-upload__text">
                拖拽文件到此处或 <em>点击上传</em>
              </div>
              <template #tip>
                <div class="el-upload__tip">
                  请上传JSON格式文件，包含原始问题和回答数据
                </div>
              </template>
            </el-upload>
            <div class="upload-actions">
              <el-button type="primary" @click="handleRawQAImport" :loading="loading.rawQA">
                开始导入
              </el-button>
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane label="导入标准问题和回答" name="standard-qa">
          <div class="import-form">
            <el-upload
              class="upload-demo"
              drag
              :action="null"
              :auto-upload="false"
              :on-change="handleStandardQAFileChange"
              :limit="1"
              accept=".json">
              <el-icon class="el-icon--upload"><upload-filled /></el-icon>
              <div class="el-upload__text">
                拖拽文件到此处或 <em>点击上传</em>
              </div>
              <template #tip>
                <div class="el-upload__tip">
                  请上传JSON格式文件，包含标准问题和回答数据
                </div>
              </template>
            </el-upload>
            <div class="upload-actions">
              <el-button type="primary" @click="handleStandardQAImport" :loading="loading.standardQA">
                开始导入
              </el-button>
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane label="导入众包任务" name="crowdsourcing-tasks">
          <div class="import-form">
            <el-upload
              class="upload-demo"
              drag
              :action="null"
              :auto-upload="false"
              :on-change="handleTasksFileChange"
              :limit="1"
              accept=".json">
              <el-icon class="el-icon--upload"><upload-filled /></el-icon>
              <div class="el-upload__text">
                拖拽文件到此处或 <em>点击上传</em>
              </div>
              <template #tip>
                <div class="el-upload__tip">
                  请上传JSON格式文件，包含众包任务数据
                </div>
              </template>
            </el-upload>
            <div class="upload-actions">
              <el-button type="primary" @click="handleTasksImport" :loading="loading.tasks">
                开始导入
              </el-button>
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane label="导入众包答案" name="crowdsourcing-answers">
          <div class="import-form">
            <el-upload
              class="upload-demo"
              drag
              :action="null"
              :auto-upload="false"
              :on-change="handleAnswersFileChange"
              :limit="1"
              accept=".json">
              <el-icon class="el-icon--upload"><upload-filled /></el-icon>
              <div class="el-upload__text">
                拖拽文件到此处或 <em>点击上传</em>
              </div>
              <template #tip>
                <div class="el-upload__tip">
                  请上传JSON格式文件，包含众包答案数据
                </div>
              </template>
            </el-upload>
            <div class="upload-actions">
              <el-button type="primary" @click="handleAnswersImport" :loading="loading.answers">
                开始导入
              </el-button>
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane label="导入数据集" name="dataset">
          <div class="import-form">
            <el-upload
              class="upload-demo"
              drag
              :action="null"
              :auto-upload="false"
              :on-change="handleDatasetFileChange"
              :limit="1"
              accept=".json">
              <el-icon class="el-icon--upload"><upload-filled /></el-icon>
              <div class="el-upload__text">
                拖拽文件到此处或 <em>点击上传</em>
              </div>
              <template #tip>
                <div class="el-upload__tip">
                  请上传JSON格式文件，包含数据集定义数据
                </div>
              </template>
            </el-upload>
            <div class="upload-actions">
              <el-button type="primary" @click="handleDatasetImport" :loading="loading.dataset">
                开始导入
              </el-button>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>

      <!-- 导入结果展示 -->
      <div v-if="importResult" class="import-result">
        <el-divider>导入结果</el-divider>
        <el-alert
          :title="importResult.success ? '导入成功' : '导入失败'"
          :type="importResult.success ? 'success' : 'error'"
          :description="importResult.message"
          show-icon
        />
        <div v-if="importResult.data" class="result-data">
          <el-descriptions title="导入统计" :column="2" border>
            <el-descriptions-item label="成功导入数量">{{ importResult.data.successCount || 0 }}</el-descriptions-item>
            <el-descriptions-item label="失败导入数量">{{ importResult.data.failedCount || 0 }}</el-descriptions-item>
            <el-descriptions-item label="总处理数量">{{ importResult.data.totalCount || 0 }}</el-descriptions-item>
            <el-descriptions-item label="耗时(ms)">{{ importResult.data.timeElapsed || 0 }}</el-descriptions-item>
          </el-descriptions>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'
import { 
  importRawQA, 
  importStandardQA, 
  importCrowdsourcingTasks, 
  importCrowdsourcingAnswers, 
  importDataset 
} from '@/api/import'

const activeTab = ref('raw-qa')
const loading = reactive({
  rawQA: false,
  standardQA: false,
  tasks: false,
  answers: false,
  dataset: false
})
const importResult = ref<any>(null)

// 文件对象
const files = reactive({
  rawQA: null as File | null,
  standardQA: null as File | null,
  tasks: null as File | null,
  answers: null as File | null,
  dataset: null as File | null
})

// 文件变更处理
const handleRawQAFileChange = (file: any) => {
  files.rawQA = file.raw
}

const handleStandardQAFileChange = (file: any) => {
  files.standardQA = file.raw
}

const handleTasksFileChange = (file: any) => {
  files.tasks = file.raw
}

const handleAnswersFileChange = (file: any) => {
  files.answers = file.raw
}

const handleDatasetFileChange = (file: any) => {
  files.dataset = file.raw
}

// 导入处理
const handleRawQAImport = async () => {
  if (!files.rawQA) {
    ElMessage.warning('请先选择要导入的文件')
    return
  }

  loading.rawQA = true
  importResult.value = null

  try {
    const result = await importRawQA(files.rawQA)
    importResult.value = {
      success: true,
      message: '原始问题和回答导入成功',
      data: result
    }
    ElMessage.success('导入成功')
  } catch (error: any) {
    importResult.value = {
      success: false,
      message: error.message || '导入失败',
      data: null
    }
    ElMessage.error('导入失败: ' + error.message)
  } finally {
    loading.rawQA = false
  }
}

const handleStandardQAImport = async () => {
  if (!files.standardQA) {
    ElMessage.warning('请先选择要导入的文件')
    return
  }

  loading.standardQA = true
  importResult.value = null

  try {
    const result = await importStandardQA(files.standardQA)
    importResult.value = {
      success: true,
      message: '标准问题和回答导入成功',
      data: result
    }
    ElMessage.success('导入成功')
  } catch (error: any) {
    importResult.value = {
      success: false,
      message: error.message || '导入失败',
      data: null
    }
    ElMessage.error('导入失败: ' + error.message)
  } finally {
    loading.standardQA = false
  }
}

const handleTasksImport = async () => {
  if (!files.tasks) {
    ElMessage.warning('请先选择要导入的文件')
    return
  }

  loading.tasks = true
  importResult.value = null

  try {
    const result = await importCrowdsourcingTasks(files.tasks)
    importResult.value = {
      success: true,
      message: '众包任务导入成功',
      data: result
    }
    ElMessage.success('导入成功')
  } catch (error: any) {
    importResult.value = {
      success: false,
      message: error.message || '导入失败',
      data: null
    }
    ElMessage.error('导入失败: ' + error.message)
  } finally {
    loading.tasks = false
  }
}

const handleAnswersImport = async () => {
  if (!files.answers) {
    ElMessage.warning('请先选择要导入的文件')
    return
  }

  loading.answers = true
  importResult.value = null

  try {
    const result = await importCrowdsourcingAnswers(files.answers)
    importResult.value = {
      success: true,
      message: '众包答案导入成功',
      data: result
    }
    ElMessage.success('导入成功')
  } catch (error: any) {
    importResult.value = {
      success: false,
      message: error.message || '导入失败',
      data: null
    }
    ElMessage.error('导入失败: ' + error.message)
  } finally {
    loading.answers = false
  }
}

const handleDatasetImport = async () => {
  if (!files.dataset) {
    ElMessage.warning('请先选择要导入的文件')
    return
  }

  loading.dataset = true
  importResult.value = null

  try {
    const result = await importDataset(files.dataset)
    importResult.value = {
      success: true,
      message: '数据集导入成功',
      data: result
    }
    ElMessage.success('导入成功')
  } catch (error: any) {
    importResult.value = {
      success: false,
      message: error.message || '导入失败',
      data: null
    }
    ElMessage.error('导入失败: ' + error.message)
  } finally {
    loading.dataset = false
  }
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

.import-form {
  padding: 20px 0;
}

.upload-actions {
  margin-top: 20px;
  text-align: center;
}

.import-result {
  margin-top: 30px;
}

.result-data {
  margin-top: 20px;
}
</style> 