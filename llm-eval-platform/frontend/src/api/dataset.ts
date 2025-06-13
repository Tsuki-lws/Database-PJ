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
  keyword?: string
  sortBy?: string
  sortDir?: string
}

// 获取数据集版本列表
export function getDatasetVersions(params: QueryParams) {
  console.log('API请求参数:', params)
  
  // 确保页码从1开始，后端会调整为从0开始
  const adjustedParams = { ...params }
  if (adjustedParams.page !== undefined) {
    if (adjustedParams.page < 1) {
      adjustedParams.page = 1
    }
    console.log(`调整页码参数: ${params.page} -> ${adjustedParams.page}`)
  }
  
  // 设置默认排序
  if (!adjustedParams.sortBy) {
    adjustedParams.sortBy = 'createdAt'
  }
  
  if (!adjustedParams.sortDir) {
    adjustedParams.sortDir = 'desc'
  }
  
  // 如果关键词为空字符串，则移除该参数
  if (adjustedParams.keyword === '') {
    delete adjustedParams.keyword
  }
  
  return request({
    url: '/api/datasets/paged',
    method: 'get',
    params: adjustedParams
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
  console.log('API: 发送创建数据集请求', JSON.stringify(data))
  
  // 确保questionIds是数组
  if (data.questionIds && !Array.isArray(data.questionIds)) {
    console.error('警告: questionIds 不是数组', data.questionIds)
    data.questionIds = Array.from(data.questionIds)
  }
  
  // 检查并转换Map为数组
  if (data.questionIds && typeof data.questionIds === 'object' && data.questionIds instanceof Map) {
    console.log('将Map转换为数组')
    data.questionIds = Array.from(data.questionIds.keys())
  }
  
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
    method: 'post'
  })
}

// 取消发布数据集版本
export function unpublishDatasetVersion(id: number) {
  return request({
    url: `/api/datasets/${id}/unpublish`,
    method: 'post'
  })
}

// 删除数据集版本
export function deleteDatasetVersion(id: number) {
  return request({
    url: `/api/datasets/${id}`,
    method: 'delete'
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
    url: `/api/datasets/${id}/questions`,
    method: 'delete',
    data: [questionId]
  })
}

// 检查数据集版本名称是否已存在
export function checkVersionName(name: string) {
  return request({
    url: '/api/datasets/check-name',
    method: 'get',
    params: { name }
  })
}

// 获取最新发布的数据集版本
export function getLatestPublishedVersion() {
  return request({
    url: '/api/datasets/latest-published',
    method: 'get'
  })
} 