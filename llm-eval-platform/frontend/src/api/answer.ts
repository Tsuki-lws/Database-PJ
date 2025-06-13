import request from '../utils/request'

export interface StandardAnswer {
  standardAnswerId?: number
  standardQuestionId: number
  answer: string
  sourceAnswerId?: number
  sourceType?: 'raw' | 'crowdsourced' | 'expert' | 'manual'
  sourceId?: number
  selectionReason?: string
  selectedBy?: number
  isFinal?: boolean
  createdAt?: string
  updatedAt?: string
  version?: number
  keyPoints?: AnswerKeyPoint[]
  standardQuestion?: {
    standardQuestionId: number
  }
}

export interface AnswerKeyPoint {
  keyPointId?: number
  standardAnswerId?: number
  pointText: string
  pointOrder?: number
  pointWeight?: number
  pointType?: 'required' | 'bonus' | 'penalty'
  exampleText?: string
  version?: number
}

export interface QueryParams {
  page?: number
  size?: number
  questionId?: number
  isFinal?: boolean
}

// 获取标准答案列表
export function getAnswerList(params: QueryParams) {
  return request({
    url: '/api/answers',
    method: 'get',
    params
  })
}

// 获取标准答案详情
export function getAnswerById(id: number) {
  return request({
    url: `/api/answers/${id}`,
    method: 'get'
  })
}

// 创建标准答案
export function createAnswer(data: StandardAnswer) {
  return request({
    url: '/api/answers',
    method: 'post',
    data
  })
}

// 更新标准答案
export function updateAnswer(id: number, data: StandardAnswer) {
  return request({
    url: `/api/answers/${id}`,
    method: 'put',
    data
  })
}

// 删除标准答案
export function deleteAnswer(id: number) {
  return request({
    url: `/api/answers/${id}`,
    method: 'delete'
  })
}

// 获取问题的标准答案
export function getAnswersByQuestionId(questionId: number) {
  return request({
    url: `/api/answers/question/${questionId}`,
    method: 'get',
    params: {
      _t: new Date().getTime() // 添加时间戳避免缓存问题
    }
  })
}

// 获取答案的关键点
export function getAnswerKeyPoints(answerId: number) {
  return request({
    url: `/api/answers/${answerId}/key-points`,
    method: 'get'
  })
}

// 添加关键点
export function addKeyPoint(answerId: number, data: AnswerKeyPoint) {
  return request({
    url: `/api/answers/${answerId}/key-points`,
    method: 'post',
    data
  })
}

// 更新关键点
export function updateKeyPoint(keyPointId: number, data: AnswerKeyPoint) {
  return request({
    url: `/api/answers/key-points/${keyPointId}`,
    method: 'put',
    data
  })
}

// 删除关键点
export function deleteKeyPoint(keyPointId: number) {
  return request({
    url: `/api/answers/key-points/${keyPointId}`,
    method: 'delete'
  })
}

// 设置为最终版本
export function setAsFinal(answerId: number) {
  return request({
    url: `/api/answers/${answerId}/set-final`,
    method: 'post'
  })
} 