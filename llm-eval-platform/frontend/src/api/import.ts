import request from '../utils/request'

/**
 * 导入原始问题和回答
 * @param file 要上传的JSON文件
 * @returns 导入结果
 */
export function importRawQA(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  
  return request({
    url: '/api/import/raw-qa',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 导入标准问题和回答
 * @param file 要上传的JSON文件
 * @returns 导入结果
 */
export function importStandardQA(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  
  return request({
    url: '/api/import/standard-qa',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 导入众包任务数据
 * @param file 要上传的JSON文件
 * @returns 导入结果
 */
export function importCrowdsourcingTasks(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  
  return request({
    url: '/api/import/crowdsourcing-tasks',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 导入众包答案数据
 * @param file 要上传的JSON文件
 * @returns 导入结果
 */
export function importCrowdsourcingAnswers(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  
  return request({
    url: '/api/import/crowdsourcing-answers',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 导入数据集
 * @param file 要上传的JSON文件
 * @returns 导入结果
 */
export function importDataset(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  
  return request({
    url: '/api/import/dataset',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 导入单条模型回答
 * @param data 模型回答数据
 * @returns 导入结果
 */
export function importModelAnswer(data: any) {
  return request({
    url: '/api/llm-answers',
    method: 'post',
    data
  })
}

/**
 * 搜索模型回答
 * @param params 查询参数
 * @returns 搜索结果
 */
export function searchModelAnswers(params: any) {
  return request({
    url: '/api/llm-answers',
    method: 'get',
    params
  })
}

/**
 * 获取模型回答详情
 * @param answerId 回答ID
 * @returns 回答详情
 */
export function getAnswerDetail(answerId: number) {
  return request({
    url: `/api/llm-answers/${answerId}`,
    method: 'get'
  })
}

/**
 * 删除模型回答
 * @param answerId 回答ID
 * @returns 删除结果
 */
export function deleteModelAnswer(answerId: number) {
  return request({
    url: `/api/llm-answers/${answerId}`,
    method: 'delete'
  })
}

/**
 * 获取所有包含模型回答的数据集列表
 * @returns 数据集列表
 */
export function getDatasetVersionsWithAnswers() {
  return request({
    url: '/api/llm-answers/datasets',
    method: 'get'
  })
}

/**
 * 获取特定数据集中特定问题的所有模型回答
 * @param datasetId 数据集ID
 * @param questionId 问题ID
 * @returns 模型回答列表
 */
export function getModelAnswersForQuestion(datasetId: number, questionId: number) {
  return request({
    url: `/api/llm-answers/datasets/${datasetId}/questions/${questionId}`,
    method: 'get'
  })
}

/**
 * 获取特定数据集中特定问题的所有模型回答
 * @param datasetId 数据集ID
 * @param params 查询参数
 * @returns 问题和模型回答情况
 */
export function getQuestionsWithAnswersInDataset(datasetId: number, params: any) {
  return request({
    url: `/api/llm-answers/datasets/${datasetId}`,
    method: 'get',
    params
  })
} 