import request from '../utils/request'

export interface Category {
  categoryId?: number
  name: string
  description?: string
  parentId?: number
  children?: Category[]
}

// 获取所有分类
export function getAllCategories() {
  return request({
    url: '/api/categories',
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
export function createCategory(data: Category) {
  return request({
    url: '/api/categories',
    method: 'post',
    data
  })
}

// 更新分类
export function updateCategory(id: number, data: Category) {
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