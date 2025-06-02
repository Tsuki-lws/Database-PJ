import request from '../utils/request'

// 获取原始问题数量
export function getRawQuestionCount() {
  return request({
    url: '/api/statistics/raw-questions/count',
    method: 'get'
  })
}

// 获取原始回答数量
export function getRawAnswerCount() {
  return request({
    url: '/api/statistics/raw-answers/count',
    method: 'get'
  })
}

// 获取标准问题数量
export function getStandardQuestionCount() {
  return request({
    url: '/api/statistics/standard-questions/count',
    method: 'get'
  })
}

// 获取标准答案数量
export function getStandardAnswerCount() {
  return request({
    url: '/api/statistics/standard-answers/count',
    method: 'get'
  })
}

// 获取众包任务数量
export function getCrowdsourcingTaskCount() {
  return request({
    url: '/api/statistics/crowdsourcing-tasks/count',
    method: 'get'
  })
}

// 获取众包答案数量
export function getCrowdsourcedAnswerCount() {
  return request({
    url: '/api/statistics/crowdsourced-answers/count',
    method: 'get'
  })
}

// 获取数据集版本数量
export function getDatasetVersionCount() {
  return request({
    url: '/api/statistics/dataset-versions/count',
    method: 'get'
  })
}

// 获取评测批次数量
export function getEvaluationBatchCount() {
  return request({
    url: '/api/statistics/evaluation-batches/count',
    method: 'get'
  })
}

// 获取评测结果数量
export function getEvaluationCount() {
  return request({
    url: '/api/statistics/evaluations/count',
    method: 'get'
  })
}

// 获取首页统计数据
export function getHomeStatistics() {
  return request({
    url: '/api/statistics/home',
    method: 'get'
  })
}

// 获取模型评分统计
export function getModelScoreStatistics() {
  return request({
    url: '/api/statistics/models/scores',
    method: 'get'
  })
}

// 获取模型对比数据
export function getModelComparisonData(modelIds: number[]) {
  return request({
    url: '/api/statistics/models/comparison',
    method: 'get',
    params: { modelIds: modelIds.join(',') }
  })
}

// 获取分类问题统计
export function getCategoryQuestionStatistics() {
  return request({
    url: '/api/statistics/categories/questions',
    method: 'get'
  })
} 