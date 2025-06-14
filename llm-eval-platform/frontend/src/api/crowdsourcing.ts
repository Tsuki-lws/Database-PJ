import request from '@/utils/request'

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
  status?: 'DRAFT' | 'PUBLISHED' | 'COMPLETED' | 'CLOSED'
  createdAt?: string
  updatedAt?: string
  standardQuestionId?: number
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
  page?: number | string
  size?: number | string
  status?: string
  sort?: string
  direction?: string
}

export interface PageResult<T> {
  content: T[]
  totalElements: number
  totalPages: number
  size: number
  number: number
  first: boolean
  last: boolean
  empty: boolean
}

// 获取任务列表
export function getTaskList(params: any) {
  return request({
    url: '/api/crowdsourcing/tasks',
    method: 'get',
    params
  })
}

// 分页获取任务列表
export function getPagedTaskList(params: QueryParams) {
  // 确保参数类型正确
  const processedParams: Record<string, any> = {};
  
  // 处理页码参数
  if (params.page !== undefined) {
    processedParams.page = Number(params.page);
  }
  
  // 处理每页大小参数
  if (params.size !== undefined) {
    processedParams.size = Number(params.size);
  }
  
  // 处理状态参数（确保大写）
  if (params.status !== undefined && params.status !== null) {
    processedParams.status = params.status.toUpperCase();
  }
  
  // 处理排序字段
  if (params.sort !== undefined && params.sort !== null) {
    processedParams.sort = params.sort;
  }
  
  // 处理排序方向
  if (params.direction !== undefined && params.direction !== null) {
    processedParams.direction = params.direction;
  }
  
  console.log('API调用原始参数:', params);
  console.log('API调用处理后参数:', processedParams);
  
  return request({
    url: '/api/crowdsourcing/tasks/page',
    method: 'get',
    params: processedParams
  });
}

// 获取任务详情
export function getTaskDetail(taskId: number | null) {
  console.log(`API调用: 获取任务详情，任务ID: ${taskId}`)
  
  if (!taskId || isNaN(Number(taskId))) {
    console.error('无效的任务ID:', taskId)
    return Promise.reject(new Error('无效的任务ID'))
  }
  
  return request({
    url: `/api/crowdsourcing/tasks/${taskId}`,
    method: 'get'
  }).then(response => {
    console.log('获取任务详情成功:', response)
    return response
  }).catch(error => {
    console.error('获取任务详情失败:', error)
    return Promise.reject(error)
  })
}

// 创建任务
export function createTask(data: any) {
  return request({
    url: '/api/crowdsourcing/tasks',
    method: 'post',
    data
  })
}

// 更新任务
export function updateTask(taskId: number, data: any) {
  return request({
    url: `/api/crowdsourcing/tasks/${taskId}`,
    method: 'put',
    data
  })
}

// 删除任务
export function deleteTask(taskId: number | null) {
  if (!taskId || isNaN(Number(taskId))) {
    console.error('无效的任务ID:', taskId)
    return Promise.reject(new Error('无效的任务ID'))
  }
  
  return request({
    url: `/api/crowdsourcing/tasks/${taskId}`,
    method: 'delete'
  })
}

// 发布任务
export function publishTask(taskId: number | null) {
  if (!taskId || isNaN(Number(taskId))) {
    console.error('无效的任务ID:', taskId)
    return Promise.reject(new Error('无效的任务ID'))
  }
  
  return request({
    url: `/api/crowdsourcing/tasks/${taskId}/publish`,
    method: 'post'
  })
}

// 完成任务
export function completeTask(taskId: number | null) {
  if (!taskId || isNaN(Number(taskId))) {
    console.error('无效的任务ID:', taskId)
    return Promise.reject(new Error('无效的任务ID'))
  }
  
  return request({
    url: `/api/crowdsourcing/tasks/${taskId}/complete`,
    method: 'post'
  })
}

// 获取任务的答案列表
export function getAnswersByTaskId(taskId: number, params: any) {
  return request({
    url: `/api/crowdsourcing/tasks/${taskId}/answers`,
    method: 'get',
    params
  })
}

// 获取所有答案
export function getAllAnswers(params: any) {
  return request({
    url: '/api/crowdsourcing/answers',
    method: 'get',
    params
  })
}

// 获取特定答案
export function getAnswerDetail(answerId: number) {
  return request({
    url: `/api/crowdsourcing/answers/${answerId}`,
    method: 'get'
  })
}

// 提交答案
export function submitAnswer(taskId: number, data: any) {
  return request({
    url: `/api/crowdsourcing/tasks/${taskId}/answers`,
    method: 'post',
    data
  })
}

// 更新答案
export function updateAnswer(answerId: number, data: any) {
  return request({
    url: `/api/crowdsourcing/answers/${answerId}`,
    method: 'put',
    data
  })
}

// 删除答案
export function deleteAnswer(answerId: number) {
  return request({
    url: `/api/crowdsourcing/answers/${answerId}`,
    method: 'delete'
  })
}

// 审核答案
export function reviewAnswer(answerId: number, approved: boolean, comment: string) {
  return request({
    url: `/api/crowdsourcing/answers/${answerId}/review`,
    method: 'post',
    data: {
      approved,
      comment
    }
  })
}

// 将答案提升为标准答案
export function promoteToStandardAnswer(answerId: number) {
  return request({
    url: `/api/crowdsourcing/answers/${answerId}/promote`,
    method: 'post'
  })
}

// 获取任务关联的问题
export function getTaskQuestions(taskId: number | null) {
  if (!taskId || isNaN(Number(taskId))) {
    console.error('无效的任务ID:', taskId)
    return Promise.reject(new Error('无效的任务ID'))
  }
  
  return request({
    url: `/api/crowdsourcing/tasks/${taskId}/questions`,
    method: 'get'
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

// 获取问题的众包答案列表
export function getAnswersByQuestionId(questionId: number, params: any) {
  return request({
    url: `/api/crowdsourcing/questions/${questionId}/answers`,
    method: 'get',
    params
  })
}

// 提交众包答案
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