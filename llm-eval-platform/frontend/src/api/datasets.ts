import request from '../utils/request'

/**
 * 获取数据集列表
 * @param params 查询参数
 * @returns 数据集列表
 */
export function getDatasetList(params: any) {
  return request({
    url: '/api/datasets',
    method: 'get',
    params
  })
}

/**
 * 获取数据集详情
 * @param datasetId 数据集ID
 * @returns 数据集详情
 */
export function getDatasetById(datasetId: number) {
  return request({
    url: `/api/datasets/${datasetId}`,
    method: 'get'
  })
}

/**
 * 获取数据集问题列表
 * @param params 查询参数，包含datasetId
 * @returns 问题列表
 */
export function getDatasetQuestions(params: any) {
  const datasetId = params.datasetId;
  return request({
    url: `/api/datasets/${datasetId}/questions`,
    method: 'get',
    params: {
      page: params.page,
      size: params.size,
      modelId: params.modelId
    }
  })
}

/**
 * 获取问题回答列表
 * @param params 查询参数，包含questionId
 * @returns 回答列表
 */
export function getQuestionAnswers(params: any) {
  return request({
    url: '/api/questions/answers',
    method: 'get',
    params
  })
}

/**
 * 获取数据集统计信息
 * @param datasetId 数据集ID
 * @returns 统计信息
 */
export function getDatasetStatistics(datasetId: number) {
  return request({
    url: `/api/datasets/${datasetId}/statistics`,
    method: 'get'
  })
}

/**
 * 创建数据集
 * @param data 数据集数据
 * @returns 创建结果
 */
export function createDataset(data: any) {
  return request({
    url: '/api/datasets',
    method: 'post',
    data
  })
}

/**
 * 更新数据集
 * @param datasetId 数据集ID
 * @param data 数据集数据
 * @returns 更新结果
 */
export function updateDataset(datasetId: number, data: any) {
  return request({
    url: `/api/datasets/${datasetId}`,
    method: 'put',
    data
  })
}

/**
 * 删除数据集
 * @param datasetId 数据集ID
 * @returns 删除结果
 */
export function deleteDataset(datasetId: number) {
  return request({
    url: `/api/datasets/${datasetId}`,
    method: 'delete'
  })
}

/**
 * 导入数据集
 * @param file 文件
 * @returns 导入结果
 */
export function importDataset(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  
  return request({
    url: '/api/datasets/import',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 导出数据集
 * @param datasetId 数据集ID
 * @param format 导出格式 (csv, excel, json)
 * @returns 导出文件
 */
export function exportDataset(datasetId: number, format: string = 'csv') {
  return request({
    url: `/api/datasets/${datasetId}/export`,
    method: 'get',
    params: { format },
    responseType: 'blob'
  })
}

/**
 * 获取数据集评测统计信息
 * @param datasetId 数据集ID
 * @returns 数据集评测统计信息，包括已评测问题数、总问题数、平均分等
 */
export function getDatasetEvaluationStats(datasetId: number) {
  return request({
    url: `/api/datasets/${datasetId}/evaluation-stats`,
    method: 'get'
  })
} 