import request from '../utils/request'

/**
 * 搜索标准问题
 * @param query 搜索关键词
 * @returns 搜索结果
 */
export function searchStandardQuestions(query: string) {
  return request({
    url: '/api/questions/search',
    method: 'get',
    params: { query }
  })
}

/**
 * 获取问题详情
 * @param questionId 问题ID
 * @returns 问题详情
 */
export function getQuestionDetail(questionId: number) {
  return request({
    url: `/api/questions/${questionId}`,
    method: 'get'
  })
}

/**
 * 获取问题列表
 * @param params 查询参数
 * @returns 问题列表
 */
export function getQuestionList(params: any) {
  return request({
    url: '/api/questions',
    method: 'get',
    params
  })
}

/**
 * 创建问题
 * @param data 问题数据
 * @returns 创建结果
 */
export function createQuestion(data: any) {
  return request({
    url: '/api/questions',
    method: 'post',
    data
  })
}

/**
 * 更新问题
 * @param questionId 问题ID
 * @param data 问题数据
 * @returns 更新结果
 */
export function updateQuestion(questionId: number, data: any) {
  return request({
    url: `/api/questions/${questionId}`,
    method: 'put',
    data
  })
}

/**
 * 删除问题
 * @param questionId 问题ID
 * @returns 删除结果
 */
export function deleteQuestion(questionId: number) {
  return request({
    url: `/api/questions/${questionId}`,
    method: 'delete'
  })
} 