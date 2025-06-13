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
          <el-descriptions-item label="版本">{{ question.version || '1.0' }}</el-descriptions-item>
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
        <div class="answers-header">
          <h3>标准回答列表 ({{ answers.length }})</h3>
          <el-button type="success" size="small" @click="navigateToCreateAnswer">添加回答</el-button>
        </div>
        
        <div v-for="answer in answers" :key="answer.standardAnswerId" class="answer-item">
          <div class="answer-meta">
            <span>ID: {{ answer.standardAnswerId }}</span>
            <span>来源: {{ formatAnswerSource(answer.sourceType) }}</span>
            <span>创建时间: {{ formatDate(answer.createdAt) }}</span>
          </div>
          <div class="answer-content">{{ answer.answer }}</div>
          <div class="answer-actions">
            <el-button size="small" type="primary" @click="navigateToEditAnswer(answer.standardAnswerId)">
              编辑
            </el-button>
            <el-button size="small" type="info" @click="viewKeyPoints(answer)">
              查看关键点
            </el-button>
            <el-popconfirm
              title="确认删除该回答吗？"
              @confirm="handleDeleteAnswer(answer.standardAnswerId)"
            >
              <template #reference>
                <el-button size="small" type="danger">删除</el-button>
              </template>
            </el-popconfirm>
          </div>
        </div>
      </div>
      
      <div class="no-answers" v-else>
        <el-empty description="暂无标准回答">
          <el-button type="primary" @click="navigateToCreateAnswer">添加回答</el-button>
        </el-empty>
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

    <!-- 关键点对话框 -->
    <el-dialog
      v-model="keyPointsDialogVisible"
      title="答案关键点"
      width="700px"
    >
      <div v-if="currentAnswer && currentAnswer.keyPoints && currentAnswer.keyPoints.length > 0">
        <el-table :data="currentAnswer.keyPoints" style="width: 100%">
          <el-table-column prop="pointText" label="关键点内容" min-width="200" />
          <el-table-column prop="pointType" label="类型" width="100">
            <template #default="scope">
              <el-tag :type="getKeyPointTypeTag(scope.row.pointType)">
                {{ formatKeyPointType(scope.row.pointType) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="pointWeight" label="权重" width="80" />
          <el-table-column prop="exampleText" label="示例" min-width="150" />
        </el-table>
      </div>
      <div v-else class="no-key-points">
        该答案暂无关键点
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="keyPointsDialogVisible = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getQuestionById } from '@/api/question'
import { getAnswersByQuestionId, deleteAnswer, getAnswerKeyPoints } from '@/api/answer'
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
const keyPointsDialogVisible = ref(false)
const currentAnswer = ref<any>(null)

// 获取问题详情
const getQuestionDetail = async () => {
  const id = Number(route.params.id)
  if (!id) return
  
  loading.value = true
  try {
    const response = await getQuestionById(id)
    // console.log('获取问题详情响应:', response)
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
      
      // 获取问题标签
      await getQuestionTags(id)
      
      // 获取问题的标准回答列表
      await getAnswersList(id)
    } else {
      question.value = {}
      tags.value = []
    }
  } catch (error) {
    question.value = {}
    tags.value = []
  } finally {
    loading.value = false
  }
}

// 获取问题标签
const getQuestionTags = async (id: number) => {
  try {
    const response = await getTagsByQuestionId(id)
    
    if (Array.isArray(response)) {
      tags.value = response
    } else if (response && response.data && Array.isArray(response.data)) {
      tags.value = response.data
    } else {
      tags.value = []
    }
  } catch (error) {
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
    const response = await getAnswersByQuestionId(id);
    
    // 检查响应格式并适当处理
    if (Array.isArray(response)) {
      // 如果直接返回数组，直接使用
      answers.value = response;
    } else if (response && typeof response === 'object') {
      // 处理标准返回格式：{ code: 200, data: [...], message: "Success" }
      const responseObj = response as any;
      if (responseObj.code === 200 && responseObj.data) {
        if (Array.isArray(responseObj.data)) {
          answers.value = responseObj.data;
        } else if (responseObj.data.content && Array.isArray(responseObj.data.content)) {
          answers.value = responseObj.data.content;
        } else {
          answers.value = [];
        }
      } else if (responseObj.content && Array.isArray(responseObj.content)) {
        // 处理分页响应格式
        answers.value = responseObj.content;
      } else {
        answers.value = [];
      }
    } else {
      answers.value = [];
    }
    
    // 将答案按创建时间倒序排列（最新的在前面）
    answers.value.sort((a: any, b: any) => {
      const dateA = new Date(a.createdAt || 0).getTime();
      const dateB = new Date(b.createdAt || 0).getTime();
      return dateB - dateA;
    });
    
    // 将所有答案的isFinal设为false
    answers.value.forEach(answer => {
      answer.isFinal = false;
    });
  } catch (error) {
    answers.value = [];
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

// 格式化日期
const formatDate = (dateStr?: string) => {
  if (!dateStr) return '未知'
  const date = new Date(dateStr)
  return date.toLocaleString()
}

// 格式化答案来源
const formatAnswerSource = (sourceType?: string) => {
  const map: Record<string, string> = {
    'raw': '原始回答',
    'crowdsourced': '众包',
    'expert': '专家',
    'manual': '手动添加',
    'undefined': '未知'
  }
  return map[sourceType || 'undefined'] || '未知'
}

// 导航到创建回答页面
const navigateToCreateAnswer = () => {
  const id = Number(route.params.id)
  if (!id) return
  router.push(`/answers/create/${id}`)
}

// 导航到编辑回答页面
const navigateToEditAnswer = (answerId: number) => {
  router.push(`/answers/edit/${answerId}`)
}

// 查看关键点
const viewKeyPoints = async (answer: any) => {
  currentAnswer.value = { ...answer };
  
  // 检查答案对象是否已经包含关键点数据
  if (answer.keyPoints && Array.isArray(answer.keyPoints) && answer.keyPoints.length > 0) {
    currentAnswer.value.keyPoints = answer.keyPoints;
    keyPointsDialogVisible.value = true;
    return;
  }
  
  // 如果没有关键点数据，则从后端获取
  if (!currentAnswer.value.keyPoints || currentAnswer.value.keyPoints.length === 0) {
    try {
      const response = await getAnswerKeyPoints(answer.standardAnswerId);
      
      // 处理后端返回的数据
      if (Array.isArray(response)) {
        currentAnswer.value.keyPoints = response;
      } else if (response && response.data && Array.isArray(response.data)) {
        currentAnswer.value.keyPoints = response.data;
      } else if (response && typeof response === 'object') {
        // 尝试直接使用响应对象
        currentAnswer.value.keyPoints = response;
      } else {
        currentAnswer.value.keyPoints = [];
      }
      
      // 如果关键点数组为空，显示提示
      if (!currentAnswer.value.keyPoints || currentAnswer.value.keyPoints.length === 0) {
        ElMessage.info('该答案暂无关键点');
      }
    } catch (error) {
      currentAnswer.value.keyPoints = [];
      ElMessage.error('获取关键点失败');
    }
  }
  
  keyPointsDialogVisible.value = true;
}

// 删除回答
const handleDeleteAnswer = async (answerId: number) => {
  try {
    await deleteAnswer(answerId)
    ElMessage.success('删除回答成功')
    const id = Number(route.params.id)
    if (id) {
      await getAnswersList(id)
    }
  } catch (error: any) {
    ElMessage.error('删除回答失败')
  }
}

// 获取关键点类型标签
const getKeyPointTypeTag = (type?: string) => {
  const map: Record<string, string> = {
    'required': 'danger',
    'bonus': 'success',
    'penalty': 'warning'
  }
  return map[type || ''] || 'info'
}

// 格式化关键点类型
const formatKeyPointType = (type?: string) => {
  const map: Record<string, string> = {
    'required': '必要点',
    'bonus': '加分点',
    'penalty': '减分点'
  }
  return map[type || ''] || '其他'
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

.answers-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.answers-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: bold;
}

.answer-item {
  padding: 15px;
  border-bottom: 1px solid #eee;
  margin-bottom: 15px;
}

.answer-item:last-child {
  border-bottom: none;
  margin-bottom: 0;
}

.answer-meta {
  display: flex;
  gap: 15px;
  font-size: 14px;
  color: #666;
  margin-bottom: 10px;
  flex-wrap: wrap;
}

.answer-content {
  white-space: pre-wrap;
  line-height: 1.6;
  margin-bottom: 15px;
  font-size: 15px;
}

.answer-actions {
  display: flex;
  gap: 10px;
}

.no-answers {
  padding: 30px 0;
  text-align: center;
}

.no-key-points {
  text-align: center;
  padding: 20px;
  color: #909399;
}
</style> 