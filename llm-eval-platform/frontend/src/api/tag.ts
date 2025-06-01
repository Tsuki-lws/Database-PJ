import request from '../utils/request'

export interface Tag {
  tagId?: number
  tagName: string
  description?: string
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