import request from '../utils/request'

export interface StandardQuestion {
  standardQuestionId?: number
  originalQuestionId?: number
  version?: number
  content: string
  questionType: string
  difficultyLevel?: string
  categoryId?: number
  status?: string
  createdAt?: string
  createdBy?: number
  isLatest?: boolean
  parentQuestionId?: number
}

export interface QueryParams {
  current?: number
  size?: number
  questionType?: string
  categoryId?: number
}

export function getQuestionList(params: QueryParams) {
  return request({
    url: '/api/v1/questions/standard/page',
    method: 'get',
    params
  })
}

export function getQuestionById(id: number) {
  return request({
    url: `/api/v1/questions/standard/${id}`,
    method: 'get'
  })
}

export function createQuestion(data: StandardQuestion) {
  return request({
    url: '/api/v1/questions/standard',
    method: 'post',
    data
  })
}

export function updateQuestion(id: number, data: StandardQuestion) {
  return request({
    url: `/api/v1/questions/standard/${id}`,
    method: 'put',
    data
  })
}

export function getLatestVersion(originalId: number) {
  return request({
    url: `/api/v1/questions/standard/latest/${originalId}`,
    method: 'get'
  })
} 