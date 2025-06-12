import request from '../utils/request'

// 获取原始回答列表
export function getRawAnswers(params: any) {
  return request({
    url: '/api/raw-answers',
    method: 'get',
    params
  })
}

// 获取原始回答详情
export function getRawAnswer(id: number) {
  return request({
    url: `/api/raw-answers/${id}`,
    method: 'get'
  })
}

// 获取问题的原始回答列表
export function getAnswersByQuestionId(questionId: number) {
  return request({
    url: `/api/raw-questions/${questionId}/answers`,
    method: 'get'
  })
}

// 创建原始回答
export function createRawAnswer(questionId: number | undefined, data: any) {
  if (questionId === undefined) {
    throw new Error('问题ID不能为空')
  }
  return request({
    url: `/api/raw-questions/${questionId}/answers`,
    method: 'post',
    data
  })
}

// 更新原始回答
export function updateRawAnswer(id: number, data: any) {
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

// 将原始回答转换为标准答案
export function convertToStandardAnswer(id: number, data: any) {
  return request({
    url: `/api/raw-answers/${id}/convert`,
    method: 'post',
    data
  })
}

// 获取原始回答统计
export function getRawAnswerStats() {
  return request({
    url: '/api/raw-answers/count',
    method: 'get'
  })
}

// 获取问题的回答数量
export function getAnswerCountByQuestionId(questionId: number) {
  return request({
    url: `/api/raw-answers/count/by-question/${questionId}`,
    method: 'get'
  })
} 