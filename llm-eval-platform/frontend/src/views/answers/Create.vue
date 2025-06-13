<template>
  <div class="answer-create-container">
    <div class="page-header">
      <h2>创建标准答案</h2>
      <el-button @click="navigateBack">返回</el-button>
    </div>

    <el-card shadow="never" class="form-card" v-loading="loading">
      <el-form :model="answerForm" :rules="rules" ref="answerFormRef" label-width="100px">
        <el-form-item label="问题" prop="standardQuestionId">
          <el-select 
            v-model="answerForm.standardQuestionId" 
            filterable 
            remote 
            placeholder="请选择问题" 
            :remote-method="searchQuestions"
            :loading="questionLoading">
            <el-option 
              v-for="item in questionOptions" 
              :key="item.value" 
              :label="item.label" 
              :value="item.value">
            </el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="答案内容" prop="answer">
          <el-input 
            v-model="answerForm.answer" 
            type="textarea" 
            :rows="8" 
            placeholder="请输入标准答案内容">
          </el-input>
        </el-form-item>

        <el-form-item label="答案来源" prop="sourceType">
          <el-select v-model="answerForm.sourceType" placeholder="请选择答案来源">
            <el-option label="手动创建" value="manual"></el-option>
            <el-option label="原始回答" value="raw"></el-option>
            <el-option label="众包答案" value="crowdsourced"></el-option>
            <el-option label="专家答案" value="expert"></el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="选择原因" prop="selectionReason">
          <el-input 
            v-model="answerForm.selectionReason" 
            type="textarea" 
            :rows="3" 
            placeholder="请输入选择该答案作为标准答案的原因">
          </el-input>
        </el-form-item>

        <el-form-item label="是否最终版本">
          <el-tooltip
            content="设为最终版本后，该答案将被标记为问题的最终标准答案，同时会取消其他答案的最终版本标记"
            placement="top"
          >
            <div class="flex-center">
              <el-switch v-model="answerForm.isFinal"></el-switch>
              <el-icon class="ml-5"><QuestionFilled /></el-icon>
            </div>
          </el-tooltip>
        </el-form-item>

        <el-divider content-position="left">关键点设置（可选）</el-divider>

        <div v-for="(point, index) in answerForm.keyPoints" :key="index" class="key-point-item">
          <el-divider content-position="left">关键点 #{{ index + 1 }}</el-divider>
          
          <el-form-item :label="'关键点内容'" :prop="`keyPoints.${index}.pointText`">
            <el-input 
              v-model="point.pointText" 
              type="textarea" 
              :rows="2" 
              placeholder="请输入关键点描述">
            </el-input>
          </el-form-item>

          <el-row :gutter="20">
            <el-col :span="8">
              <el-form-item :label="'类型'" :prop="`keyPoints.${index}.pointType`">
                <el-select v-model="point.pointType" placeholder="请选择类型">
                  <el-option label="必须" value="required"></el-option>
                  <el-option label="加分" value="bonus"></el-option>
                  <el-option label="减分" value="penalty"></el-option>
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item :label="'权重'" :prop="`keyPoints.${index}.pointWeight`">
                <el-input-number 
                  v-model="point.pointWeight" 
                  :precision="1" 
                  :step="0.1" 
                  :min="0.1" 
                  :max="10">
                </el-input-number>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item :label="'顺序'" :prop="`keyPoints.${index}.pointOrder`">
                <el-input-number 
                  v-model="point.pointOrder" 
                  :precision="0" 
                  :step="1" 
                  :min="1">
                </el-input-number>
              </el-form-item>
            </el-col>
          </el-row>

          <el-form-item :label="'示例'" :prop="`keyPoints.${index}.exampleText`">
            <el-input 
              v-model="point.exampleText" 
              type="textarea" 
              :rows="2" 
              placeholder="请输入示例内容">
            </el-input>
          </el-form-item>

          <el-button 
            type="danger" 
            size="small" 
            @click="removeKeyPoint(index)" 
            class="remove-point-btn">
            删除此关键点
          </el-button>
        </div>

        <div class="add-key-point">
          <el-button type="primary" plain @click="addKeyPoint">
            <el-icon><Plus /></el-icon>添加关键点
          </el-button>
        </div>

        <el-form-item>
          <el-button type="primary" @click="submitForm">保存</el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus, QuestionFilled } from '@element-plus/icons-vue'
import { createAnswer, type AnswerKeyPoint, type StandardAnswer } from '@/api/answer'
import { getQuestionsWithoutAnswer } from '@/api/question'
import * as answerApi from '@/api/answer'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const questionLoading = ref(false)
const answerFormRef = ref()

// 问题选项
const questionOptions = ref<{value: number, label: string}[]>([])

// 表单数据
const answerForm = reactive<StandardAnswer>({
  standardQuestionId: route.params.questionId ? Number(route.params.questionId) : 0,
  answer: '',
  sourceType: 'manual',
  selectionReason: '',
  isFinal: false,
  keyPoints: [] as AnswerKeyPoint[]
})

// 表单验证规则
const rules = {
  standardQuestionId: [
    { required: true, message: '请选择问题', trigger: 'change' }
  ],
  answer: [
    { required: true, message: '请输入答案内容', trigger: 'blur' }
  ],
  sourceType: [
    { required: true, message: '请选择答案来源', trigger: 'change' }
  ]
}

// 搜索问题
const searchQuestions = async (query: string) => {
  if (query.length < 1) return
  
  questionLoading.value = true
  try {
    const res = await getQuestionsWithoutAnswer({
      keyword: query,
      page: 1,
      size: 20
    })
    const data = res.data || res
    questionOptions.value = (data.list || []).map((item: any) => ({
      value: item.standardQuestionId,
      label: `${item.standardQuestionId}: ${item.question}`
    }))
  } catch (error) {
    console.error('搜索问题失败', error)
  } finally {
    questionLoading.value = false
  }
}

// 添加关键点
const addKeyPoint = () => {
  if (!answerForm.keyPoints) {
    answerForm.keyPoints = [];
  }
  answerForm.keyPoints.push({
    pointText: '',
    pointOrder: answerForm.keyPoints.length + 1,
    pointWeight: 1.0,
    pointType: 'required',
    exampleText: ''
  })
}

// 移除关键点
const removeKeyPoint = (index: number) => {
  if (!answerForm.keyPoints) return;
  
  answerForm.keyPoints.splice(index, 1)
  // 更新顺序
  answerForm.keyPoints.forEach((point, idx) => {
    point.pointOrder = idx + 1
  })
}

// 提交表单
const submitForm = async () => {
  if (!answerFormRef.value) return
  
  await answerFormRef.value.validate(async (valid: boolean) => {
    if (!valid) return
    
    loading.value = true
    try {
      // 创建一个标准答案对象，但不包含关键点
      const answerData: any = {
        ...answerForm,
        standardQuestion: {
          standardQuestionId: answerForm.standardQuestionId
        }
      }
      
      // 保存关键点的副本，因为要从answerData中移除
      const keyPoints = [...(answerForm.keyPoints || [])];
      // 从提交数据中移除关键点，后续单独添加
      delete answerData.keyPoints;
      
      console.log('提交标准回答数据:', answerData);
      const result = await createAnswer(answerData);
      console.log('创建标准回答结果:', result);
      
      // 如果有关键点，并且创建答案成功，则添加关键点
      const createdAnswerId = result && typeof result === 'object' ? 
        ((result as any).standardAnswerId || (result as any).data?.standardAnswerId) : null;
      
      if (keyPoints.length > 0 && createdAnswerId) {
        console.log('开始添加关键点，数量:', keyPoints.length, '答案ID:', createdAnswerId);
        
        let successCount = 0;
        let failCount = 0;
        
        // 依次添加关键点
        for (const keyPoint of keyPoints) {
          try {
            // 创建一个干净的关键点对象，避免包含不必要的字段
            const keyPointData = {
              pointText: keyPoint.pointText,
              pointOrder: keyPoint.pointOrder,
              pointWeight: keyPoint.pointWeight,
              pointType: keyPoint.pointType,
              exampleText: keyPoint.exampleText || ''
            };
            
            console.log(`添加关键点 ${keyPoint.pointOrder}: ${keyPoint.pointText}`);
            
            // 使用命名空间调用，避免命名冲突
            const response = await answerApi.addKeyPoint(createdAnswerId, keyPointData);
            console.log('添加关键点成功:', response);
            successCount++;
          } catch (error) {
            console.error('添加关键点失败:', keyPoint, error);
            failCount++;
          }
        }
        
        if (failCount > 0) {
          ElMessage.warning(`创建成功，但有${failCount}个关键点添加失败，请检查控制台错误信息`);
        } else if (successCount > 0) {
          ElMessage.success(`创建成功，已添加${successCount}个关键点`);
        } else {
          ElMessage.success('创建成功');
        }
      } else {
        ElMessage.success('创建成功');
      }
      
      // 如果是从问题详情页跳转过来，则返回问题详情页
      if (answerForm.standardQuestionId) {
        router.push(`/questions/detail/${answerForm.standardQuestionId}`);
      } else {
        router.push('/answers');
      }
    } catch (error) {
      console.error('创建答案失败', error);
      ElMessage.error('创建答案失败，请检查控制台错误信息');
    } finally {
      loading.value = false;
    }
  })
}

// 重置表单
const resetForm = () => {
  if (!answerFormRef.value) return
  answerFormRef.value.resetFields()
  answerForm.keyPoints = []
}

// 返回上一页
const navigateBack = () => {
  router.back()
}

// 如果是从问题详情页跳转过来，加载问题信息
onMounted(async () => {
  if (answerForm.standardQuestionId) {
    try {
      // 这里可以加载问题详情
    } catch (error) {
      console.error('获取问题详情失败', error)
    }
  }
})
</script>

<style scoped>
.answer-create-container {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.form-card {
  margin-bottom: 20px;
}

.key-point-item {
  position: relative;
  padding: 10px;
  margin-bottom: 20px;
  border: 1px dashed #dcdfe6;
  border-radius: 4px;
}

.remove-point-btn {
  position: absolute;
  top: 10px;
  right: 10px;
}

.add-key-point {
  margin: 20px 0;
  display: flex;
  justify-content: center;
}

.flex-center {
  display: flex;
  align-items: center;
}

.ml-5 {
  margin-left: 5px;
}
</style> 