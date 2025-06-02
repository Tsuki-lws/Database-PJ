import request from '../utils/request'

// 获取标准问题列表
export function getStandardQuestions(params: any) {
  return request({
    url: '/api/standard-questions',
    method: 'get',
    params
  })
}

// 获取标准问题详情
export function getStandardQuestion(id: number) {
  return request({
    url: `/api/standard-questions/${id}`,
    method: 'get'
  })
}

// 创建标准问题
export function createStandardQuestion(data: any) {
  return request({
    url: '/api/standard-questions',
    method: 'post',
    data
  })
}

// 更新标准问题
export function updateStandardQuestion(id: number, data: any) {
  return request({
    url: `/api/standard-questions/${id}`,
    method: 'put',
    data
  })
}

// 删除标准问题
export function deleteStandardQuestion(id: number) {
  return request({
    url: `/api/standard-questions/${id}`,
    method: 'delete'
  })
}

// 获取没有标准答案的问题
export function getQuestionsWithoutAnswers(params: any) {
  return request({
    url: '/api/standard-questions/without-answers',
    method: 'get',
    params
  })
}

// 添加标签到问题
export function addTagsToQuestion(questionId: number, tagIds: number[]) {
  return request({
    url: `/api/standard-questions/${questionId}/tags`,
    method: 'post',
    data: { tagIds }
  })
}

// 更新问题分类
export function updateQuestionCategory(questionId: number, categoryId: number) {
  return request({
    url: `/api/standard-questions/${questionId}/category`,
    method: 'put',
    data: { categoryId }
  })
} 