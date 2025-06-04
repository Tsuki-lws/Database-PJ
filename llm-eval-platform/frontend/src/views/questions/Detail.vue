<template>
  <div class="question-detail-container">
    <div class="page-header">
      <h2>问题详情</h2>
      <div>
        <el-button @click="navigateToEdit">编辑</el-button>
        <el-button @click="navigateBack">返回</el-button>
      </div>
    </div>

    <el-card shadow="never" class="detail-card" v-loading="loading">
      <div class="question-content">
        <h3>问题内容</h3>
        <p>{{ question.question }}</p>
      </div>

      <el-divider />

      <div class="question-info">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="问题ID">{{ question.standardQuestionId }}</el-descriptions-item>
          <el-descriptions-item label="分类">{{ question.categoryName || '未分类' }}</el-descriptions-item>
          <el-descriptions-item label="类型">
            <el-tag :type="getQuestionTypeTag(question.questionType)">
              {{ formatQuestionType(question.questionType) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="难度">
            <el-tag :type="getDifficultyTag(question.difficulty)">
              {{ formatDifficulty(question.difficulty) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusTag(question.status)">
              {{ formatStatus(question.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="版本">{{ question.version }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ question.createdAt }}</el-descriptions-item>
          <el-descriptions-item label="更新时间">{{ question.updatedAt }}</el-descriptions-item>
        </el-descriptions>
      </div>

      <el-divider />

      <div class="question-tags" v-if="tags.length > 0">
        <h3>标签</h3>
        <div class="tag-list">
          <el-tag v-for="tag in tags" :key="tag.tagId" class="tag-item">{{ tag.tagName }}</el-tag>
        </div>
      </div>

      <el-divider v-if="answers.length > 0" />

      <div class="question-answers" v-if="answers.length > 0">
        <h3>标准答案</h3>
        <el-card v-for="answer in answers" :key="answer.standardAnswerId" class="answer-card">
          <div class="answer-header">
            <span class="answer-title">答案 #{{ answer.standardAnswerId }}</span>
            <el-tag v-if="answer.isFinal" type="success">最终版本</el-tag>
          </div>
          <div class="answer-content">{{ answer.answer }}</div>
        </el-card>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getQuestionById } from '@/api/question'
import { getAnswersByQuestionId } from '@/api/answer'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const question = ref<any>({})
const tags = ref<any[]>([])
const answers = ref<any[]>([])

// 获取问题详情
const getQuestionDetail = async () => {
  const id = Number(route.params.id)
  if (!id) return
  
  loading.value = true
  try {
    const response = await getQuestionById(id)
    console.log('问题详情原始响应:', response)
    
    // 检查响应格式并适当处理
    if (response && typeof response === 'object') {
      // 处理标准返回格式：{ code: 200, data: {...}, message: "Success" }
      if (response.code === 200 && response.data) {
        question.value = response.data
      } 
      // 如果响应本身就是问题对象
      else {
        question.value = response
      }
      
      // 处理分类信息
      if (question.value.category) {
        question.value.categoryName = question.value.category.categoryName || '未分类'
      } else if (!question.value.categoryName) {
        question.value.categoryName = '未分类'
      }
      
      console.log('问题详情数据:', question.value)
      
      // 如果问题有标签，直接从问题对象获取
      if (question.value && question.value.tags) {
        tags.value = question.value.tags
      } else {
        tags.value = []
      }
      await getAnswersList(id)
    } else {
      console.error('获取问题详情返回格式异常:', response)
      question.value = {}
      tags.value = []
    }
  } catch (error) {
    console.error('获取问题详情失败', error)
  } finally {
    loading.value = false
  }
}

// 获取问题答案
const getAnswersList = async (id: number) => {
  try {
    const response = await getAnswersByQuestionId(id)
    console.log('问题答案原始响应:', response)
    
    // 检查响应格式并适当处理
    if (response && typeof response === 'object') {
      // 处理标准返回格式：{ code: 200, data: [...], message: "Success" }
      if (response.code === 200 && response.data) {
        if (Array.isArray(response.data)) {
          answers.value = response.data
        } else if (response.data.content && Array.isArray(response.data.content)) {
          answers.value = response.data.content
        } else {
          answers.value = []
          console.warn('响应中没有找到预期的答案列表:', response)
        }
      }
      // 如果响应本身就是数组
      else if (Array.isArray(response)) {
        answers.value = response
      } 
      else {
        answers.value = []
        console.warn('响应格式不符合预期:', response)
      }
      console.log('问题答案数据:', answers.value)
    } else {
      console.error('获取问题答案返回格式异常:', response)
      answers.value = []
    }
  } catch (error) {
    console.error('获取问题答案失败', error)
  }
}

// 格式化问题类型
const formatQuestionType = (type: string) => {
  const map: Record<string, string> = {
    'single_choice': '单选题',
    'multiple_choice': '多选题',
    'simple_fact': '简单事实题',
    'subjective': '主观题'
  }
  return map[type] || type
}

// 获取问题类型标签类型
const getQuestionTypeTag = (type: string) => {
  const map: Record<string, string> = {
    'single_choice': 'info',
    'multiple_choice': 'warning',
    'simple_fact': 'success',
    'subjective': 'primary'
  }
  return map[type] || ''
}

// 格式化难度
const formatDifficulty = (difficulty: string) => {
  const map: Record<string, string> = {
    'easy': '简单',
    'medium': '中等',
    'hard': '困难'
  }
  return map[difficulty] || difficulty
}

// 获取难度标签类型
const getDifficultyTag = (difficulty: string) => {
  const map: Record<string, string> = {
    'easy': 'success',
    'medium': 'warning',
    'hard': 'danger'
  }
  return map[difficulty] || ''
}

// 格式化状态
const formatStatus = (status: string) => {
  const map: Record<string, string> = {
    'draft': '草稿',
    'pending_review': '待审核',
    'approved': '已通过',
    'rejected': '已拒绝'
  }
  return map[status] || status
}

// 获取状态标签类型
const getStatusTag = (status: string) => {
  const map: Record<string, string> = {
    'draft': 'info',
    'pending_review': 'warning',
    'approved': 'success',
    'rejected': 'danger'
  }
  return map[status] || ''
}

// 导航到编辑页面
const navigateToEdit = () => {
  router.push(`/questions/edit/${question.value.standardQuestionId}`)
}

// 返回上一页
const navigateBack = () => {
  router.back()
}

onMounted(() => {
  getQuestionDetail()
})
</script>

<style scoped>
.question-detail-container {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.detail-card {
  margin-bottom: 20px;
}

.question-content {
  margin-bottom: 20px;
}

.question-content h3 {
  margin-bottom: 10px;
  font-size: 18px;
  font-weight: bold;
}

.question-content p {
  font-size: 16px;
  line-height: 1.6;
}

.question-info {
  margin-bottom: 20px;
}

.question-tags {
  margin-bottom: 20px;
}

.question-tags h3 {
  margin-bottom: 10px;
  font-size: 18px;
  font-weight: bold;
}

.tag-list {
  display: flex;
  flex-wrap: wrap;
}

.tag-item {
  margin-right: 10px;
  margin-bottom: 10px;
}

.question-answers h3 {
  margin-bottom: 15px;
  font-size: 18px;
  font-weight: bold;
}

.answer-card {
  margin-bottom: 15px;
}

.answer-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.answer-title {
  font-weight: bold;
}

.answer-content {
  line-height: 1.6;
  white-space: pre-wrap;
}
</style> 