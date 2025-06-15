import request from '@/utils/request'
import type { SystemStatus, NewsItem } from '@/types/home'

/**
 * 获取系统状态
 */
export async function fetchSystemStatus(): Promise<SystemStatus> {
  return request({
    url: '/api/home/system-status',
    method: 'get'
  })
}

/**
 * 获取最新动态
 */
export async function fetchNews(): Promise<NewsItem[]> {
  return request({
    url: '/api/home/news',
    method: 'get'
  })
} 