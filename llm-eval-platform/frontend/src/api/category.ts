import request from '../utils/request'

export interface Category {
  categoryId?: number
  name: string
  description?: string
  parent?: {
    categoryId: number
    name?: string
  } | null
  children?: Array<{
    categoryId: number
    name: string
  }>
  questionCount?: number
}

// 获取所有分类
export function getAllCategories() {
  return request({
    url: '/api/categories',
    method: 'get'
  })
}

// 获取带问题数量的分类列表
export function getCategoriesWithQuestionCount() {
  return request({
    url: '/api/categories/with-count',
    method: 'get'
  })
}

// 获取分类详情
export function getCategoryById(id: number) {
  return request({
    url: `/api/categories/${id}`,
    method: 'get'
  })
}

// 创建分类
export function createCategory(data: any) {
  return request({
    url: '/api/categories',
    method: 'post',
    data
  })
}

// 更新分类
export function updateCategory(id: number, data: any) {
  return request({
    url: `/api/categories/${id}`,
    method: 'put',
    data
  })
}

// 删除分类
export function deleteCategory(id: number) {
  return request({
    url: `/api/categories/${id}`,
    method: 'delete'
  })
}

// 获取根分类
export function getRootCategories() {
  return request({
    url: '/api/categories/root',
    method: 'get'
  })
}

// 获取子分类
export function getChildCategories(id: number) {
  return request({
    url: `/api/categories/${id}/children`,
    method: 'get'
  })
} 