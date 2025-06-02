<template>
  <div class="home-container">
    <div class="welcome-section">
      <h1>欢迎使用 LLM 评测平台</h1>
      <p>本平台提供大语言模型评测、对比和分析功能，帮助您更好地了解和评估模型性能。</p>
    </div>

    <el-row :gutter="20">
      <el-col :span="16">
        <!-- 评测概览 -->
        <el-card shadow="hover" class="overview-card">
          <template #header>
            <div class="card-header">
              <span>评测概览</span>
              <el-button text @click="navigateTo('/evaluations')">查看全部</el-button>
            </div>
          </template>
          <el-row :gutter="20">
            <el-col :span="6">
              <div class="stat-item">
                <div class="stat-value">{{ stats.totalModels }}</div>
                <div class="stat-label">评测模型</div>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="stat-item">
                <div class="stat-value">{{ stats.totalDatasets }}</div>
                <div class="stat-label">数据集</div>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="stat-item">
                <div class="stat-value">{{ stats.totalQuestions }}</div>
                <div class="stat-label">评测问题</div>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="stat-item">
                <div class="stat-value">{{ stats.completedEvaluations }}</div>
                <div class="stat-label">已完成评测</div>
              </div>
            </el-col>
          </el-row>
          
          <div class="chart-container">
            <h3>模型评分对比</h3>
            <div class="chart-placeholder">
              <div class="chart-content">
                <div class="model-score" v-for="(score, index) in modelScores" :key="index">
                  <div class="model-name">{{ score.name }}</div>
                  <div class="score-bar-container">
                    <div class="score-bar" :style="{ width: score.score * 10 + '%', backgroundColor: score.color }">
                      {{ score.score.toFixed(2) }}
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </el-card>
        
        <!-- 最近评测 -->
        <el-card shadow="hover" class="recent-card">
          <template #header>
            <div class="card-header">
              <span>最近评测</span>
              <el-button text @click="navigateTo('/evaluations/results')">查看全部结果</el-button>
            </div>
          </template>
          <el-table :data="recentEvaluations" style="width: 100%">
            <el-table-column prop="batchName" label="评测批次" />
            <el-table-column prop="modelName" label="模型" width="120" />
            <el-table-column prop="datasetName" label="数据集" width="150" />
            <el-table-column prop="score" label="得分" width="80">
              <template #default="scope">
                {{ scope.row.score.toFixed(2) }}
              </template>
            </el-table-column>
            <el-table-column prop="completedAt" label="完成时间" width="180" />
            <el-table-column fixed="right" label="操作" width="120">
              <template #default="scope">
                <el-button link type="primary" @click="viewResult(scope.row)">查看结果</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      
      <el-col :span="8">
        <!-- 快速操作 -->
        <el-card shadow="hover" class="quick-actions-card">
          <template #header>
            <div class="card-header">
              <span>快速操作</span>
            </div>
          </template>
          <div class="quick-actions">
            <el-button type="primary" @click="navigateTo('/evaluations/create')">创建评测</el-button>
            <el-button type="primary" @click="navigateTo('/evaluations/manual')">人工评测</el-button>
            <el-button type="primary" @click="navigateTo('/import/model-answer')">导入模型回答</el-button>
            <el-button type="primary" @click="navigateTo('/import/model-evaluation')">导入评测结果</el-button>
          </div>
        </el-card>
        
        <!-- 系统状态 -->
        <el-card shadow="hover" class="system-status-card">
          <template #header>
            <div class="card-header">
              <span>系统状态</span>
              <el-tag type="success">正常运行</el-tag>
            </div>
          </template>
          <div class="system-status">
            <div class="status-item">
              <span class="status-label">API 服务：</span>
              <el-tag size="small" type="success">在线</el-tag>
            </div>
            <div class="status-item">
              <span class="status-label">数据库：</span>
              <el-tag size="small" type="success">连接正常</el-tag>
            </div>
            <div class="status-item">
              <span class="status-label">评测引擎：</span>
              <el-tag size="small" type="success">运行中</el-tag>
            </div>
            <div class="status-item">
              <span class="status-label">当前运行任务：</span>
              <span>2</span>
            </div>
            <div class="status-item">
              <span class="status-label">等待中任务：</span>
              <span>5</span>
            </div>
          </div>
        </el-card>
        
        <!-- 最新动态 -->
        <el-card shadow="hover" class="news-card">
          <template #header>
            <div class="card-header">
              <span>最新动态</span>
            </div>
          </template>
          <div class="news-list">
            <div class="news-item" v-for="(item, index) in news" :key="index">
              <div class="news-time">{{ item.time }}</div>
              <div class="news-content">{{ item.content }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

// 统计数据
const stats = reactive({
  totalModels: 15,
  totalDatasets: 8,
  totalQuestions: 2500,
  completedEvaluations: 56
})

// 模型评分数据
const modelScores = [
  { name: 'GPT-4', score: 8.75, color: '#409EFF' },
  { name: 'Claude 2', score: 8.42, color: '#67C23A' },
  { name: 'LLaMA 2 70B', score: 7.89, color: '#E6A23C' },
  { name: 'Mistral 7B', score: 7.65, color: '#F56C6C' },
  { name: 'Baichuan 2 13B', score: 7.32, color: '#909399' }
]

// 最近评测数据
const recentEvaluations = ref([
  {
    id: 1,
    batchName: 'GPT-4 基准测试',
    modelName: 'GPT-4',
    datasetName: '通用能力评测集 v1.0',
    score: 8.75,
    completedAt: '2023-05-15 12:45:18'
  },
  {
    id: 2,
    batchName: 'Claude 2 评测',
    modelName: 'Claude 2',
    datasetName: '通用能力评测集 v1.0',
    score: 8.42,
    completedAt: '2023-05-14 18:30:45'
  },
  {
    id: 3,
    batchName: 'LLaMA 2 测试',
    modelName: 'LLaMA 2 70B',
    datasetName: '通用能力评测集 v1.0',
    score: 7.89,
    completedAt: '2023-05-13 15:20:33'
  }
])

// 最新动态
const news = [
  { time: '2023-05-16', content: '系统更新：新增模型评测结果导入功能' },
  { time: '2023-05-15', content: '新增模型：Baichuan 2 13B 已添加到评测库' },
  { time: '2023-05-14', content: '数据集更新：通用能力评测集 v1.0 新增 200 道问题' },
  { time: '2023-05-12', content: '功能更新：新增模型回答导入功能' },
  { time: '2023-05-10', content: '系统维护：数据库优化完成，性能提升 30%' }
]

// 初始化
onMounted(() => {
  // 可以在这里加载一些初始数据
})

// 导航到指定路由
const navigateTo = (path: string) => {
  router.push(path)
}

// 查看评测结果
const viewResult = (row: any) => {
  router.push(`/evaluations/results/${row.id}`)
}
</script>

<style scoped>
.home-container {
  padding: 20px;
}

.welcome-section {
  margin-bottom: 30px;
  text-align: center;
}

.welcome-section h1 {
  font-size: 28px;
  color: #303133;
  margin-bottom: 10px;
}

.welcome-section p {
  font-size: 16px;
  color: #606266;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.overview-card,
.recent-card,
.quick-actions-card,
.system-status-card,
.news-card {
  margin-bottom: 20px;
}

.stat-item {
  text-align: center;
  padding: 15px 0;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #409EFF;
  margin-bottom: 5px;
}

.stat-label {
  font-size: 14px;
  color: #606266;
}

.chart-container {
  margin-top: 20px;
}

.chart-container h3 {
  margin-bottom: 15px;
  font-size: 16px;
  color: #303133;
}

.chart-placeholder {
  background-color: #f5f7fa;
  border-radius: 4px;
  padding: 20px;
  min-height: 300px;
}

.chart-content {
  width: 100%;
}

.model-score {
  margin-bottom: 15px;
}

.model-name {
  margin-bottom: 5px;
  font-weight: bold;
}

.score-bar-container {
  background-color: #e4e7ed;
  border-radius: 4px;
  height: 30px;
  width: 100%;
}

.score-bar {
  height: 30px;
  border-radius: 4px;
  color: white;
  display: flex;
  align-items: center;
  padding-left: 10px;
  font-weight: bold;
}

.quick-actions {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.system-status {
  padding: 10px 0;
}

.status-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 15px;
}

.status-label {
  color: #606266;
}

.news-list {
  max-height: 300px;
  overflow-y: auto;
}

.news-item {
  padding: 10px 0;
  border-bottom: 1px solid #ebeef5;
}

.news-item:last-child {
  border-bottom: none;
}

.news-time {
  font-size: 12px;
  color: #909399;
  margin-bottom: 5px;
}

.news-content {
  font-size: 14px;
  color: #303133;
}
</style> 