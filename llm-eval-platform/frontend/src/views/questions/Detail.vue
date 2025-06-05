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

      <div class="question-tags">
        <div class="tags-header">
          <h3>标签</h3>
          <el-button type="primary" size="small" @click="showTagDialog">
            <el-icon><Plus /></el-icon>添加标签
          </el-button>
        </div>
        <div class="tag-list" v-if="tags.length > 0">
          <el-tag 
            v-for="tag in tags" 
            :key="tag.tagId" 
            class="tag-item"
            :style="tag.color ? { backgroundColor: tag.color, color: getContrastColor(tag.color) } : {}"
            closable
            @close="removeTag(tag.tagId)"
          >
            {{ tag.tagName }}
          </el-tag>
        </div>
        <div v-else class="no-tags">
          暂无标签，点击添加标签按钮为问题添加标签
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

    <!-- 添加标签对话框 -->
    <el-dialog
      v-model="tagDialogVisible"
      title="添加标签"
      width="500px"
    >
      <el-form :model="tagForm">
        <el-form-item label="选择标签">
          <el-select
            v-model="tagForm.selectedTags"
            multiple
            filterable
            placeholder="请选择标签"
            style="width: 100%"
          >
            <el-option
              v-for="tag in allTags"
              :key="tag.tagId"
              :label="tag.tagName"
              :value="tag.tagId"
              :disabled="tags.some(t => t.tagId === tag.tagId)"
            >
              <div class="tag-option">
                <span :style="tag.color ? { backgroundColor: tag.color, width: '12px', height: '12px', display: 'inline-block', marginRight: '8px', borderRadius: '2px' } : {}"></span>
                {{ tag.tagName }}
              </div>
            </el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="tagDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="addTags">确认</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getQuestionById } from '@/api/question'
import { getAnswersByQuestionId } from '@/api/answer'
import { getAllTags, getTagsByQuestionId, addTagsToQuestion, removeTagFromQuestion } from '@/api/tag'
import { ElMessage, ElLoading } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const question = ref<any>({})
const tags = ref<any[]>([])
const answers = ref<any[]>([])
const allTags = ref<any[]>([])
const tagDialogVisible = ref(false)
const tagForm = ref({
  selectedTags: [] as number[]
})

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
      const responseObj = response as any
      if (responseObj.code === 200 && responseObj.data) {
        question.value = responseObj.data
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
      
      // 获取问题标签
      await getQuestionTags(id)
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

// 获取问题标签
const getQuestionTags = async (id: number) => {
  try {
    const response = await getTagsByQuestionId(id)
    console.log('问题标签原始响应:', response)
    
    if (Array.isArray(response)) {
      tags.value = response
    } else if (response && response.data && Array.isArray(response.data)) {
      tags.value = response.data
    } else {
      tags.value = []
    }
  } catch (error) {
    console.error('获取问题标签失败', error)
    tags.value = []
  }
}

// 获取所有标签
const getAllTagsList = async () => {
  try {
    const response = await getAllTags()
    if (Array.isArray(response)) {
      allTags.value = response
    } else if (response && response.data && Array.isArray(response.data)) {
      allTags.value = response.data
    } else {
      allTags.value = []
    }
  } catch (error) {
    console.error('获取所有标签失败', error)
    allTags.value = []
  }
}

// 显示添加标签对话框
const showTagDialog = async () => {
  tagForm.value.selectedTags = []
  await getAllTagsList()
  tagDialogVisible.value = true
}

// 添加标签
const addTags = async () => {
  const id = Number(route.params.id)
  if (!id) return
  
  if (tagForm.value.selectedTags.length === 0) {
    ElMessage.warning('请至少选择一个标签')
    return
  }
  
  try {
    await addTagsToQuestion(id, tagForm.value.selectedTags)
    ElMessage.success('添加标签成功')
    tagDialogVisible.value = false
    await getQuestionTags(id)
  } catch (error) {
    console.error('添加标签失败', error)
    ElMessage.error('添加标签失败')
  }
}

// 移除标签
const removeTag = async (tagId: number) => {
  const id = Number(route.params.id)
  if (!id) return
  
  try {
    console.log(`尝试移除标签，问题ID: ${id}, 标签ID: ${tagId}`)
    
    // 显示加载中提示
    const loading = ElLoading.service({
      lock: true,
      text: '正在移除标签...',
      background: 'rgba(0, 0, 0, 0.7)'
    })
    
    await removeTagFromQuestion(id, tagId)
    
    // 移除成功
    loading.close()
    ElMessage.success('移除标签成功')
    await getQuestionTags(id)
  } catch (error: any) {
    console.error('移除标签失败', error)
    
    // 获取错误信息
    let errorMessage = '移除标签失败'
    
    if (error.response) {
      console.log('错误响应状态码:', error.response.status)
      console.log('错误响应头:', error.response.headers)
      console.log('错误响应数据:', error.response.data)
      
      // 获取自定义错误信息头
      const errorHeader = error.response.headers['x-error-message']
      
      if (errorHeader) {
        errorMessage = `移除标签失败: ${errorHeader}`
      } else if (error.response.status === 404) {
        errorMessage = '问题或标签不存在'
      } else if (error.response.status === 400) {
        errorMessage = '标签不在此问题中或问题没有标签'
      }
    } else if (error.message) {
      errorMessage = `移除标签失败: ${error.message}`
    }
    
    ElMessage.error(errorMessage)
    
    // 无论如何都刷新标签列表
    await getQuestionTags(id)
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
      const responseObj = response as any
      if (responseObj.code === 200 && responseObj.data) {
        if (Array.isArray(responseObj.data)) {
          answers.value = responseObj.data
        } else if (responseObj.data.content && Array.isArray(responseObj.data.content)) {
          answers.value = responseObj.data.content
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

.tags-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.tags-header h3 {
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

.no-tags {
  text-align: center;
  color: #909399;
}
</style> 