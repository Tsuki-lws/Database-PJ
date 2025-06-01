<template>
  <div class="home-container">
    <el-row :gutter="20">
      <el-col :span="24">
        <h1 class="welcome-title">欢迎使用 LLM 评测平台</h1>
        <p class="welcome-subtitle">一站式大语言模型评测解决方案</p>
      </el-col>
    </el-row>

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stat-cards">
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon">
            <el-icon><Document /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.questionCount }}</div>
            <div class="stat-label">标准问题</div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon">
            <el-icon><ChatLineSquare /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.answerCount }}</div>
            <div class="stat-label">标准答案</div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon">
            <el-icon><Files /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.datasetCount }}</div>
            <div class="stat-label">数据集版本</div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon">
            <el-icon><DataAnalysis /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.evaluationCount }}</div>
            <div class="stat-label">评测批次</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 快速入口 -->
    <el-row :gutter="20" class="quick-access">
      <el-col :span="24">
        <h2 class="section-title">快速入口</h2>
      </el-col>
      <el-col :xs="24" :sm="12" :md="8" :lg="6" v-for="item in quickAccessItems" :key="item.title">
        <el-card shadow="hover" class="quick-access-card" @click="navigateTo(item.route)">
          <div class="quick-access-icon">
            <el-icon>
              <component :is="item.icon"></component>
            </el-icon>
          </div>
          <div class="quick-access-title">{{ item.title }}</div>
          <div class="quick-access-desc">{{ item.description }}</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 待办事项 -->
    <el-row :gutter="20" class="todo-section">
      <el-col :span="24">
        <h2 class="section-title">待办事项</h2>
        <el-card shadow="hover" class="todo-card">
          <el-empty v-if="todos.length === 0" description="暂无待办事项"></el-empty>
          <el-table v-else :data="todos" style="width: 100%">
            <el-table-column prop="type" label="类型" width="120">
              <template #default="scope">
                <el-tag :type="getTagType(scope.row.type)">{{ scope.row.type }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="title" label="标题"></el-table-column>
            <el-table-column prop="createdAt" label="创建时间" width="180"></el-table-column>
            <el-table-column fixed="right" label="操作" width="120">
              <template #default="scope">
                <el-button link type="primary" @click="handleTodoAction(scope.row)">处理</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Document, ChatLineSquare, Files, DataAnalysis, Service, Setting, Plus } from '@element-plus/icons-vue'

const router = useRouter()

// 统计数据
const stats = ref({
  questionCount: 0,
  answerCount: 0,
  datasetCount: 0,
  evaluationCount: 0
})

// 快速入口项
const quickAccessItems = [
  {
    title: '创建标准问题',
    description: '创建新的标准问题',
    icon: 'Plus',
    route: '/questions/create'
  },
  {
    title: '创建众包任务',
    description: '发布众包任务收集答案',
    icon: 'Service',
    route: '/crowdsourcing/create'
  },
  {
    title: '创建数据集',
    description: '创建新的数据集版本',
    icon: 'Files',
    route: '/datasets/create'
  },
  {
    title: '创建评测',
    description: '创建新的模型评测批次',
    icon: 'DataAnalysis',
    route: '/evaluations/create'
  }
]

// 待办事项
const todos = ref([])

// 获取统计数据
const fetchStats = async () => {
  try {
    // 这里应该调用后端API获取统计数据
    // 暂时使用模拟数据
    stats.value = {
      questionCount: 128,
      answerCount: 96,
      datasetCount: 5,
      evaluationCount: 12
    }
  } catch (error) {
    console.error('获取统计数据失败', error)
  }
}

// 获取待办事项
const fetchTodos = async () => {
  try {
    // 这里应该调用后端API获取待办事项
    // 暂时使用模拟数据
    todos.value = [
      {
        id: 1,
        type: '问题审核',
        title: '审核新提交的标准问题',
        createdAt: '2023-06-01 10:30:00',
        route: '/questions/1'
      },
      {
        id: 2,
        type: '众包任务',
        title: '众包任务"Linux基础知识"已完成',
        createdAt: '2023-06-02 14:20:00',
        route: '/crowdsourcing/detail/1'
      }
    ]
  } catch (error) {
    console.error('获取待办事项失败', error)
  }
}

// 获取标签类型
const getTagType = (type: string) => {
  const typeMap: Record<string, string> = {
    '问题审核': 'primary',
    '众包任务': 'success',
    '评测结果': 'warning'
  }
  return typeMap[type] || 'info'
}

// 处理待办事项
const handleTodoAction = (todo: any) => {
  router.push(todo.route)
}

// 导航到指定路由
const navigateTo = (route: string) => {
  router.push(route)
}

onMounted(() => {
  fetchStats()
  fetchTodos()
})
</script>

<style scoped>
.home-container {
  padding: 20px;
}

.welcome-title {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 10px;
}

.welcome-subtitle {
  font-size: 16px;
  color: #606266;
  margin-bottom: 30px;
}

.section-title {
  font-size: 20px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 20px;
  margin-top: 30px;
}

.stat-cards {
  margin-bottom: 30px;
}

.stat-card {
  height: 120px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s;
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.1);
}

.stat-icon {
  font-size: 36px;
  color: #409EFF;
  margin-right: 20px;
}

.stat-content {
  text-align: center;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
}

.stat-label {
  font-size: 14px;
  color: #606266;
  margin-top: 5px;
}

.quick-access {
  margin-bottom: 30px;
}

.quick-access-card {
  height: 160px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s;
  margin-bottom: 20px;
}

.quick-access-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.1);
}

.quick-access-icon {
  font-size: 32px;
  color: #409EFF;
  margin-bottom: 10px;
}

.quick-access-title {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 5px;
}

.quick-access-desc {
  font-size: 12px;
  color: #606266;
  text-align: center;
}

.todo-section {
  margin-bottom: 30px;
}

.todo-card {
  min-height: 200px;
}
</style> 