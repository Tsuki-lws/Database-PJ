import request from '../utils/request'

/**
 * 获取评测批次列表
 * @param params 查询参数
 * @returns 评测批次列表
 */
export function getEvaluationBatches(params: any) {
  return request({
    url: '/api/evaluations/batches',
    method: 'get',
    params
  })
}

/**
 * 获取评测批次详情
 * @param batchId 评测批次ID
 * @returns 评测批次详情
 */
export function getEvaluationBatchById(batchId: number) {
  return request({
    url: `/api/evaluations/batches/${batchId}`,
    method: 'get'
  })
}

/**
 * 创建评测批次
 * @param data 评测批次数据
 * @returns 创建结果
 */
export function createEvaluationBatch(data: any) {
  return request({
    url: '/api/evaluations/batches',
    method: 'post',
    data
  })
}

/**
 * 开始执行评测批次
 * @param batchId 评测批次ID
 * @returns 执行结果
 */
export function startEvaluationBatch(batchId: number) {
  return request({
    url: `/api/evaluations/batches/${batchId}/start`,
    method: 'post'
  })
}

/**
 * 获取评测结果
 * @param batchId 评测批次ID
 * @param params 查询参数
 * @returns 评测结果
 */
export function getEvaluationResults(batchId: number, params?: any) {
  return request({
    url: `/api/evaluations/batches/${batchId}/results`,
    method: 'get',
    params
  })
}

/**
 * 获取评测结果详情
 * @param batchId 评测批次ID
 * @param questionId 问题ID
 * @returns 评测结果详情
 */
export function getEvaluationResultDetail(batchId: number, questionId: number) {
  return request({
    url: `/api/evaluations/batches/${batchId}/results/${questionId}`,
    method: 'get'
  })
}

/**
 * 获取待评测的回答
 * @param params 查询参数
 * @returns 待评测回答列表
 */
export function getUnevaluatedAnswers(params?: any) {
  return request({
    url: '/api/evaluations/unevaluated',
    method: 'get',
    params
  })
}

/**
 * 获取评测详情
 * @param answerId 回答ID
 * @returns 评测详情
 */
export function getEvaluationDetail(answerId: number) {
  return request({
    url: `/api/evaluations/detail/${answerId}`,
    method: 'get'
  })
}

/**
 * 提交人工评测结果
 * @param data 评测数据
 * @returns 提交结果
 */
export function submitManualEvaluation(data: any) {
  return request({
    url: '/api/evaluations/manual',
    method: 'post',
    data
  })
}

/**
 * 获取模型列表
 * @returns 模型列表
 */
export function getModelList() {
  return request({
    url: '/api/models',
    method: 'get'
  })
}

/**
 * 导出评测结果
 * @param batchId 评测批次ID
 * @param format 导出格式 (csv, excel, json)
 * @returns 导出文件
 */
export function exportEvaluationResults(batchId: number, format: string = 'csv') {
  return request({
    url: `/api/evaluations/batches/${batchId}/export`,
    method: 'get',
    params: { format },
    responseType: 'blob'
  })
}

// 获取评测结果列表
export function getEvaluationResultsList(batchId: number, params: any) {
  return request({
    url: `/api/evaluation-batches/${batchId}/results`,
    method: 'get',
    params
  })
}

// 获取评测结果详情
export function getEvaluationResultDetailList(id: number) {
  return request({
    url: `/api/evaluations/${id}`,
    method: 'get'
  })
}

// 获取待评测的答案列表
export function getUnevaluatedAnswersList(params: any) {
  return request({
    url: '/api/evaluations/unevaluated',
    method: 'get',
    params
  })
}

// 获取评测详情（用于人工评测）
export function getEvaluationDetailList(answerId: number) {
  return request({
    url: `/api/evaluations/detail/${answerId}`,
    method: 'get'
  })
}

// 提交人工评测结果
export function submitManualEvaluationList(data: any) {
  return request({
    url: '/api/evaluations/manual',
    method: 'post',
    data
  })
}

// 导入评测结果
export function importEvaluationResultsList(batchId: number, file: File) {
  const formData = new FormData()
  formData.append('file', file)
  
  return request({
    url: `/api/evaluation-batches/${batchId}/import`,
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 获取模型评分统计
export function getModelScoreStatisticsList(batchId: number) {
  return request({
    url: `/api/evaluation-batches/${batchId}/statistics`,
    method: 'get'
  })
}

// 获取模型对比数据
export function getModelComparisonDataList(modelIds: number[]) {
  return request({
    url: '/api/evaluations/comparison',
    method: 'get',
    params: { modelIds: modelIds.join(',') }
  })
}

/**
 * 获取问题的标准答案
 * @param questionId 问题ID
 * @returns 标准答案列表
 */
export function getStandardAnswersByQuestionId(questionId: number) {
  return request({
    url: `/api/answers/question/${questionId}`,
    method: 'get'
  })
}

/**
 * 获取问题的最终标准答案
 * @param questionId 问题ID
 * @returns 最终标准答案
 */
export function getFinalStandardAnswerByQuestionId(questionId: number) {
  return request({
    url: `/api/answers/question/${questionId}/final`,
    method: 'get'
  })
}

/**
 * 获取标准答案的关键点
 * @param answerId 标准答案ID
 * @returns 关键点列表
 */
export function getKeyPointsByAnswerId(answerId: number) {
  return request({
    url: `/api/answers/${answerId}/key-points`,
    method: 'get'
  })
}

/**
 * 获取模型评测结果列表
 * @param params 查询参数
 * @returns 评测结果列表
 */
export function getModelEvaluations(params: any) {
  // 首先尝试调用特定的模型评测接口
  return request({
    url: '/api/evaluations/model',
    method: 'get',
    params
  }).catch(error => {
    console.warn('模型评测接口调用失败，尝试使用通用评测接口:', error);
    // 如果特定接口失败，尝试使用通用评测接口
    return request({
      url: '/api/evaluations',
      method: 'get',
      params
    });
  });
}

/**
 * 获取模型评测统计数据
 * @returns 模型评测统计数据
 */
export function getModelEvaluationStatistics() {
  return request({
    url: '/api/evaluations/model/statistics',
    method: 'get'
  })
}

/**
 * 获取所有评测结果
 * @param params 查询参数
 * @returns 所有评测结果
 */
export function getAllEvaluations(params: any) {
  return request({
    url: '/api/evaluations',
    method: 'get',
    params
  })
}

/**
 * 删除评测结果
 * @param id 评测结果ID
 * @returns 删除结果
 */
export function deleteEvaluation(id: number) {
  return request({
    url: `/api/evaluations/${id}`,
    method: 'delete'
  })
}

/**
 * 对单个回答进行评测
 * @param answerId 回答ID
 * @param method 评测方法 (manual, auto)
 * @returns 评测结果
 */
export function evaluateAnswer(answerId: number, method: string = 'auto') {
  return request({
    url: `/api/evaluations/evaluate`,
    method: 'post',
    data: {
      answerId,
      method
    }
  })
} 