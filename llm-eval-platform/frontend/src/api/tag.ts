import request from '../utils/request'

export interface Tag {
  tagId?: number
  tagName: string
  description?: string
  color?: string
  questionCount?: number
}

// 获取所有标签
export function getAllTags() {
  return request({
    url: '/api/tags',
    method: 'get'
  })
}

// 获取标签详情
export function getTagById(id: number) {
  return request({
    url: `/api/tags/${id}`,
    method: 'get'
  })
}

// 创建标签
export function createTag(data: Tag) {
  return request({
    url: '/api/tags',
    method: 'post',
    data
  })
}

// 更新标签
export function updateTag(id: number, data: Tag) {
  return request({
    url: `/api/tags/${id}`,
    method: 'put',
    data
  })
}

// 删除标签
export function deleteTag(id: number) {
  return request({
    url: `/api/tags/${id}`,
    method: 'delete'
  })
}

// 获取问题的标签
export function getTagsByQuestionId(questionId: number) {
  return request({
    url: `/api/tags/question/${questionId}`,
    method: 'get'
  })
}

// 给问题添加标签
export function addTagsToQuestion(questionId: number, tagIds: number[]) {
  return request({
    url: `/api/tags/question/${questionId}`,
    method: 'post',
    data: tagIds
  })
}

// 从问题中移除标签
export function removeTagFromQuestion(questionId: number, tagId: number) {
  console.log(`API调用: 从问题 ${questionId} 中移除标签 ${tagId}`)
  return request({
    url: `/api/tags/question/${questionId}/${tagId}`,
    method: 'delete',
    validateStatus: function (status: number) {
      // 允许所有状态码，以便在catch中处理错误
      return true
    }
  })
}

// 获取带问题数量的标签列表
export function getTagsWithQuestionCount() {
  return request({
    url: '/api/tags/with-count',
    method: 'get'
  })
}

// 获取带问题数量和详细信息的标签列表
export function getTagsWithQuestionCountDetails() {
  return request({
    url: '/api/tags/with-count/details',
    method: 'get'
  })
}

// 根据标签查询问题
export function getQuestionsByTags(tagId: number) {
  return request({
    url: '/api/tags/search',
    method: 'get',
    params: { tagId }
  })
} 