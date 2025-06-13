import request from '../utils/request'

export interface Category {
  categoryId?: number
  name?: string
  description?: string
}

export interface StandardQuestion {
  standardQuestionId?: number
  question: string
  categoryId?: number | null
  category?: Category | null
  questionType: 'single_choice' | 'multiple_choice' | 'simple_fact' | 'subjective'
  difficulty?: 'easy' | 'medium' | 'hard'
  sourceQuestionId?: number
  status?: 'draft' | 'pending_review' | 'approved' | 'rejected'
  createdAt?: string
  updatedAt?: string
  version?: number
  tags?: number[]
}

export interface QueryParams {
  page?: number
  size?: number
  tagId?: number
  categoryId?: number
  questionType?: string
  difficulty?: string
  keyword?: string
  sortBy?: string
  sortDir?: string
}

// 获取标准问题列表
export function getQuestionList(params: QueryParams) {
  // 确保page参数有值且从1开始
  const adjustedParams = { ...params };
  
  // 添加默认排序参数，如果未指定
  if (!adjustedParams.sortBy) {
    adjustedParams.sortBy = 'createdAt';
  }
  if (!adjustedParams.sortDir) {
    adjustedParams.sortDir = 'desc';
  }
  
  console.log('发送问题列表请求，参数:', adjustedParams);
  
  return request({
    url: '/api/questions',
    method: 'get',
    params: adjustedParams
  });
}

// 获取标准问题详情
export function getQuestionById(id: number) {
  return request({
    url: `/api/questions/${id}`,
    method: 'get'
  })
}

// 创建标准问题
export function createQuestion(data: StandardQuestion) {
  return request({
    url: '/api/questions',
    method: 'post',
    data
  })
}

// 更新标准问题
export function updateQuestion(id: number, data: StandardQuestion) {
  return request({
    url: `/api/questions/${id}`,
    method: 'put',
    data
  })
}

// 删除标准问题
export function deleteQuestion(id: number) {
  return request({
    url: `/api/questions/${id}`,
    method: 'delete'
  })
}

// 获取问题的标签
export function getQuestionTags(id: number) {
  return request({
    url: `/api/tags/question/${id}`,
    method: 'get'
  })
}

// 添加标签到问题
export function addTagToQuestion(questionId: number, tagIds: number[]) {
  return request({
    url: `/api/tags/question/${questionId}`,
    method: 'post',
    data: tagIds
  })
}

// 获取未录入标准答案的问题列表
export function getQuestionsWithoutAnswer(params: QueryParams) {
  return request({
    url: '/api/questions/without-answer/paged',
    method: 'get',
    params
  })
}

// 根据分类获取问题
export function getQuestionsByCategory(categoryId: number) {
  return request({
    url: `/api/questions/category/${categoryId}`,
    method: 'get'
  })
}

// 根据标签获取问题
export function getQuestionsByTag(tagId: number) {
  return request({
    url: `/api/questions/tag/${tagId}`,
    method: 'get'
  })
}

// 获取标准问题列表（分页）
export function getStandardQuestions(params: QueryParams) {
  return request({
    url: '/api/questions',
    method: 'get',
    params
  })
}

// 获取没有分类的问题列表
export function getQuestionsWithoutCategory(params: QueryParams) {
  return request({
    url: '/api/questions/without-category',
    method: 'get',
    params
  })
}

// 通过分类名称更新问题分类
export function updateQuestionCategoryByName(questionId: number, categoryName: string) {
  return request({
    url: `/api/questions/${questionId}/category`,
    method: 'put',
    data: { categoryName }
  })
}

// 通过分类ID更新问题分类
export function updateQuestionCategory(questionId: number, categoryId: number) {
  return request({
    url: `/api/questions/${questionId}/category`,
    method: 'put',
    data: { categoryId }
  })
} 