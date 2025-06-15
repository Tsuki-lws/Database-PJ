export interface SystemStatus {
  overall: 'normal' | 'abnormal'
  api: 'online' | 'offline'
  database: 'connected' | 'disconnected'
  engine: 'running' | 'stopped'
  runningTasks: number
  waitingTasks: number
}

export interface NewsItem {
  time: string
  content: string
} 