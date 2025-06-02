import request from '../utils/request'

export interface CrowdsourcingTask {
  taskId?: number
  title: string
  description?: string
  taskType: 'answer_collection' | 'answer_review' | 'answer_rating'
  creatorId: number
  questionCount?: number
  minAnswersPerQuestion: number
  rewardInfo?: string
  startTime?: string
  endTime?: string
  status?: 'draft' | 'ongoing' | 'completed' | 'cancelled'
  createdAt?: string
  updatedAt?: string
  questionIds?: number[]
}

export interface CrowdsourcingAnswer {
  answerId?: number
  taskId: number
  standardQuestionId: number
  userId?: number
  answerText: string
  submissionTime?: string
  qualityScore?: number
  reviewStatus?: 'pending' | 'approved' | 'rejected'
  reviewerId?: number
  reviewTime?: string
  reviewComment?: string
  isSelected?: boolean
  createdAt?: string
  updatedAt?: string
}

export interface QueryParams {
  page?: number
  size?: number
  status?: string
}

// 获取众包任务列表
export function getTaskList(params: QueryParams) {
  return request({
    url: '/api/crowdsourcing/tasks',
    method: 'get',
    params
  })
}

// 获取众包任务详情
export function getTaskById(id: number) {
  return request({
    url: `/api/crowdsourcing/tasks/${id}`,
    method: 'get'
  })
}

// 获取众包任务
export function getCrowdsourcingTask(id: number) {
  return request({
    url: `/api/crowdsourcing/tasks/${id}`,
    method: 'get'
  })
}

// 获取任务问题列表
export function getTaskQuestions(params: any) {
  return request({
    url: `/api/crowdsourcing/tasks/${params.taskId}/questions`,
    method: 'get',
    params: {
      page: params.page,
      size: params.size
    }
  })
}

// 创建众包任务
export function createTask(data: CrowdsourcingTask) {
  return request({
    url: '/api/crowdsourcing/tasks',
    method: 'post',
    data
  })
}

// 更新众包任务状态
export function updateTaskStatus(id: number, status: string) {
  return request({
    url: `/api/crowdsourcing/tasks/${id}/status`,
    method: 'put',
    data: { status }
  })
}

// 获取众包答案列表
export function getAnswersByTaskId(taskId: number) {
  return request({
    url: `/api/crowdsourcing/tasks/${taskId}/answers`,
    method: 'get'
  })
}

// 获取问题的众包答案列表
export function getAnswersByQuestionId(questionId: number, params: any) {
  return request({
    url: `/api/crowdsourcing/questions/${questionId}/answers`,
    method: 'get',
    params
  })
}

// 提交众包答案
export function submitAnswer(data: CrowdsourcingAnswer) {
  return request({
    url: '/api/crowdsourcing/answers',
    method: 'post',
    data
  })
}

// 提交众包回答（用于收集界面）
export function submitCrowdsourcedAnswer(data: any) {
  return request({
    url: '/api/crowdsourcing/answers/submit',
    method: 'post',
    data
  })
}

// 批量提交众包答案
export function batchSubmitAnswers(data: CrowdsourcingAnswer[]) {
  return request({
    url: '/api/crowdsourcing/answers/batch',
    method: 'post',
    data
  })
}

// 审核众包答案
export function reviewAnswer(answerId: number, data: any) {
  return request({
    url: `/api/crowdsourcing/answers/${answerId}/review`,
    method: 'put',
    data
  })
}

// 批量审核众包答案
export function batchReviewAnswers(reviewerId: number, reviews: any[]) {
  return request({
    url: '/api/crowdsourcing/answers/batch-review',
    method: 'put',
    data: {
      reviewerId,
      reviews
    }
  })
}

// 将众包答案选为标准答案
export function selectAsStandardAnswer(answerId: number, data: any) {
  return request({
    url: `/api/crowdsourcing/answers/${answerId}/select`,
    method: 'post',
    data
  })
}

// 评分众包答案
export function rateAnswer(answerId: number, data: any) {
  return request({
    url: `/api/crowdsourcing/answers/${answerId}/rate`,
    method: 'post',
    data
  })
}

// 比较两个众包答案
export function compareAnswers(data: any) {
  return request({
    url: '/api/crowdsourcing/answers/compare',
    method: 'post',
    data
  })
}

// 获取问题的众包答案统计
export function getAnswerStats(questionId: number) {
  return request({
    url: `/api/crowdsourcing/questions/${questionId}/answers/stats`,
    method: 'get'
  })
} 