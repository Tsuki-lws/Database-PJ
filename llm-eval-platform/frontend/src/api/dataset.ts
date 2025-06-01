import request from '../utils/request'

export interface DatasetVersion {
  versionId?: number
  name: string
  description?: string
  releaseDate?: string
  isPublished?: boolean
  questionCount?: number
  createdAt?: string
  updatedAt?: string
  questionIds?: number[]
}

export interface QueryParams {
  page?: number
  size?: number
  isPublished?: boolean
}

// 获取数据集版本列表
export function getDatasetVersions(params: QueryParams) {
  return request({
    url: '/api/datasets',
    method: 'get',
    params
  })
}

// 获取数据集版本详情
export function getDatasetVersionById(id: number) {
  return request({
    url: `/api/datasets/${id}`,
    method: 'get'
  })
}

// 创建数据集版本
export function createDatasetVersion(data: DatasetVersion) {
  return request({
    url: '/api/datasets',
    method: 'post',
    data
  })
}

// 更新数据集版本
export function updateDatasetVersion(id: number, data: DatasetVersion) {
  return request({
    url: `/api/datasets/${id}`,
    method: 'put',
    data
  })
}

// 发布数据集版本
export function publishDatasetVersion(id: number) {
  return request({
    url: `/api/datasets/${id}/publish`,
    method: 'put'
  })
}

// 获取数据集版本中的问题
export function getDatasetQuestions(id: number) {
  return request({
    url: `/api/datasets/${id}/questions`,
    method: 'get'
  })
}

// 添加问题到数据集版本
export function addQuestionsToDataset(id: number, questionIds: number[]) {
  return request({
    url: `/api/datasets/${id}/questions`,
    method: 'post',
    data: questionIds
  })
}

// 从数据集版本中移除问题
export function removeQuestionFromDataset(id: number, questionId: number) {
  return request({
    url: `/api/datasets/${id}/questions/${questionId}`,
    method: 'delete'
  })
} 