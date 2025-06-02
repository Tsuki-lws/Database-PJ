import request from '../utils/request'

export interface RawQuestion {
  questionId?: number
  sourceQuestionId?: number
  questionTitle?: string
  questionBody: string
  source?: string
  sourceId?: number
  sourceUrl?: string
  crawledAt?: string
  createdAt?: string
  updatedAt?: string
}

export interface RawAnswer {
  answerId?: number
  sourceAnswerId?: number
  questionId: number
  answerBody: string
  authorInfo?: string
  upvotes?: number
  isAccepted?: boolean
  createdAt?: string
  updatedAt?: string
}

export interface QueryParams {
  page?: number
  size?: number
  source?: string
  keyword?: string
}

// 获取原始问题列表
export function getRawQuestionList(params: QueryParams) {
  return request({
    url: '/api/raw-questions',
    method: 'get',
    params
  })
}

// 获取原始问题详情
export function getRawQuestionById(id: number) {
  return request({
    url: `/api/raw-questions/${id}`,
    method: 'get'
  })
}

// 创建原始问题
export function createRawQuestion(data: RawQuestion) {
  return request({
    url: '/api/raw-questions',
    method: 'post',
    data
  })
}

// 更新原始问题
export function updateRawQuestion(id: number, data: RawQuestion) {
  return request({
    url: `/api/raw-questions/${id}`,
    method: 'put',
    data
  })
}

// 删除原始问题
export function deleteRawQuestion(id: number) {
  return request({
    url: `/api/raw-questions/${id}`,
    method: 'delete'
  })
}

// 获取原始问题的回答列表
export function getRawAnswersByQuestionId(questionId: number) {
  return request({
    url: `/api/raw-questions/${questionId}/answers`,
    method: 'get'
  })
}

// 获取原始回答列表
export function getRawAnswerList(params: QueryParams) {
  return request({
    url: '/api/raw-answers',
    method: 'get',
    params
  })
}

// 获取原始回答详情
export function getRawAnswerById(id: number) {
  return request({
    url: `/api/raw-answers/${id}`,
    method: 'get'
  })
}

// 创建原始回答
export function createRawAnswer(data: RawAnswer) {
  return request({
    url: '/api/raw-answers',
    method: 'post',
    data
  })
}

// 更新原始回答
export function updateRawAnswer(id: number, data: RawAnswer) {
  return request({
    url: `/api/raw-answers/${id}`,
    method: 'put',
    data
  })
}

// 删除原始回答
export function deleteRawAnswer(id: number) {
  return request({
    url: `/api/raw-answers/${id}`,
    method: 'delete'
  })
}

// 将原始问题转换为标准问题
export function convertToStandardQuestion(id: number, data: any) {
  return request({
    url: `/api/raw-questions/${id}/convert`,
    method: 'post',
    data
  })
}

// 将原始回答转换为标准回答
export function convertToStandardAnswer(id: number, data: any) {
  return request({
    url: `/api/raw-answers/${id}/convert`,
    method: 'post',
    data
  })
} 