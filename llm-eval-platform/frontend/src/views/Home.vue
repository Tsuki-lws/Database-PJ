<template>
  <div class="home-container">
    <div class="welcome-section">
      <h1>欢迎使用 LLM 评测平台</h1>
      <!-- <p>本平台提供大语言模型评测、对比和分析功能，帮助您更好地了解和评估模型性能。</p> -->
    </div>

    <el-row :gutter="20" justify="center">
      <el-col :span="12">
        <!-- 快速操作 -->
        <el-card shadow="hover" class="quick-actions-card">
          <template #header>
            <div class="card-header">
              <span>快速操作</span>
            </div>
          </template>
          <div class="quick-actions">
            <el-button type="primary" @click="navigateTo('/models')">模型管理</el-button>
            <el-button type="primary" @click="navigateTo('/datasets')">数据集管理</el-button>
          </div>
        </el-card>
        
        <!-- 系统状态 -->
        <el-card shadow="hover" class="system-status-card" v-loading="loading.systemStatus">
          <template #header>
            <div class="card-header">
              <span>系统状态</span>
              <el-tag :type="systemStatus.overall === 'normal' ? 'success' : 'danger'">
                {{ systemStatus.overall === 'normal' ? '正常运行' : '异常' }}
              </el-tag>
            </div>
          </template>
          <div class="system-status">
            <div class="status-item">
              <span class="status-label">API 服务：</span>
              <el-tag size="small" :type="systemStatus.api === 'online' ? 'success' : 'danger'">
                {{ systemStatus.api === 'online' ? '在线' : '离线' }}
              </el-tag>
            </div>
            <div class="status-item">
              <span class="status-label">数据库：</span>
              <el-tag size="small" :type="systemStatus.database === 'connected' ? 'success' : 'danger'">
                {{ systemStatus.database === 'connected' ? '连接正常' : '连接异常' }}
              </el-tag>
            </div>
            <div class="status-item">
              <span class="status-label">评测引擎：</span>
              <el-tag size="small" :type="systemStatus.engine === 'running' ? 'success' : 'danger'">
                {{ systemStatus.engine === 'running' ? '运行中' : '已停止' }}
              </el-tag>
            </div>
            <div class="status-item">
              <span class="status-label">当前运行任务：</span>
              <span>{{ systemStatus.runningTasks }}</span>
            </div>
            <div class="status-item">
              <span class="status-label">等待中任务：</span>
              <span>{{ systemStatus.waitingTasks }}</span>
            </div>
          </div>
        </el-card>
        
        <!-- 最新动态 -->
        <el-card shadow="hover" class="news-card" v-loading="loading.news">
          <template #header>
            <div class="card-header">
              <span>最新动态</span>
            </div>
          </template>
          <div class="news-list" v-if="news.length > 0">
            <div class="news-item" v-for="(item, index) in news" :key="index">
              <div class="news-time">{{ item.time }}</div>
              <div class="news-content">{{ item.content }}</div>
            </div>
          </div>
          <el-empty v-else description="暂无动态"></el-empty>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { fetchSystemStatus, fetchNews } from '@/api/home'
import type { SystemStatus, NewsItem } from '@/types/home'

const router = useRouter()

// 加载状态
const loading = reactive({
  systemStatus: false,
  news: false
})

// 系统状态
const systemStatus = reactive<SystemStatus>({
  overall: 'normal',
  api: 'online',
  database: 'connected',
  engine: 'running',
  runningTasks: 0,
  waitingTasks: 0
})

// 最新动态
const news = ref<NewsItem[]>([])

// 初始化数据
const initData = async () => {
  await Promise.all([
    loadSystemStatus(),
    loadNews()
  ])
}

// 加载系统状态
const loadSystemStatus = async () => {
  try {
    loading.systemStatus = true
    const response = await fetchSystemStatus()
    Object.assign(systemStatus, response)
  } catch (error) {
    console.error('获取系统状态失败:', error)
  } finally {
    loading.systemStatus = false
  }
}

// 加载最新动态
const loadNews = async () => {
  try {
    loading.news = true
    const response = await fetchNews()
    news.value = response
  } catch (error) {
    console.error('获取最新动态失败:', error)
  } finally {
    loading.news = false
  }
}

// 初始化
onMounted(() => {
  initData()
})

// 导航到指定路由
const navigateTo = (path: string) => {
  router.push(path)
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

.quick-actions-card,
.system-status-card,
.news-card {
  margin-bottom: 20px;
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