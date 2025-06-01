import request from '../utils/request'

export interface LlmModel {
  modelId?: number
  name: string
  version: string
  provider?: string
  description?: string
  apiConfig?: any
  createdAt?: string
}

export interface EvaluationBatch {
  batchId?: number
  name: string
  description?: string
  modelId: number
  judgeModelId?: number
  versionId: number
  evaluationMethod: 'human' | 'auto' | 'judge_model'
  startTime?: string
  endTime?: string
  status?: 'pending' | 'in_progress' | 'completed' | 'failed'
  metricsSummary?: any
  createdAt?: string
  updatedAt?: string
}

export interface Evaluation {
  evaluationId?: number
  llmAnswerId: number
  standardAnswerId: number
  score?: number
  method?: string
  keyPointsEvaluation?: any
  judgeModelId?: number
  batchId?: number
  comments?: string
  createdAt?: string
  updatedAt?: string
}

export interface QueryParams {
  page?: number
  size?: number
  status?: string
  modelId?: number
  versionId?: number
}

// 获取模型列表
export function getModelList() {
  return request({
    url: '/api/models',
    method: 'get'
  })
}

// 创建模型
export function createModel(data: LlmModel) {
  return request({
    url: '/api/models',
    method: 'post',
    data
  })
}

// 获取评测批次列表
export function getEvaluationBatches(params: QueryParams) {
  return request({
    url: '/api/batches',
    method: 'get',
    params
  })
}

// 获取评测批次详情
export function getEvaluationBatchById(id: number) {
  return request({
    url: `/api/batches/${id}`,
    method: 'get'
  })
}

// 创建评测批次
export function createEvaluationBatch(data: EvaluationBatch) {
  return request({
    url: '/api/batches',
    method: 'post',
    data
  })
}

// 开始执行评测批次
export function startEvaluationBatch(id: number) {
  return request({
    url: `/api/batches/${id}/start`,
    method: 'post'
  })
}

// 获取批次的评测结果
export function getEvaluationsByBatchId(batchId: number) {
  return request({
    url: `/api/evaluations/batch/${batchId}`,
    method: 'get'
  })
}

// 获取回答的评测结果
export function getEvaluationsByAnswerId(answerId: number) {
  return request({
    url: `/api/evaluations/answer/${answerId}`,
    method: 'get'
  })
}

// 创建评测结果
export function createEvaluation(data: Evaluation) {
  return request({
    url: '/api/evaluations',
    method: 'post',
    data
  })
}

// 获取批次的平均评分
export function getAverageScoreByBatch(batchId: number) {
  return request({
    url: `/api/evaluations/batch/${batchId}/average`,
    method: 'get'
  })
}

// 获取通过评测的回答数量
export function getPassingEvaluationsCount(batchId: number, threshold = 0.6) {
  return request({
    url: `/api/evaluations/batch/${batchId}/passing`,
    method: 'get',
    params: { threshold }
  })
}

// 执行批次评测
export function runBatchEvaluation(batchId: number) {
  return request({
    url: `/api/evaluations/batch/${batchId}/run`,
    method: 'post'
  })
} 