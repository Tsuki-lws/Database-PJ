import axios from 'axios'
import { ElMessage } from 'element-plus'

// 创建自定义实例以提高可靠性
const service = axios.create({
  baseURL: '', // 使用相对路径，让Vite的代理配置生效
  timeout: 180000, // 增加到180秒
  maxContentLength: 20 * 1024 * 1024, // 增加至20MB
  maxBodyLength: 20 * 1024 * 1024, // 增加至20MB
  headers: {
    'Content-Type': 'application/json',
    'Accept': 'application/json'
  }
})

// 请求拦截器
service.interceptors.request.use(
  config => {
    // 打印请求信息但不包括大型数据对象
    const logConfig = { ...config };
    if (logConfig.data && typeof logConfig.data === 'object') {
      logConfig.data = '数据对象太大，不打印详情';
    }
    console.log(`发送${config.method?.toUpperCase()}请求: ${config.url}`);
    return config
  },
  error => {
    console.error('请求拦截器错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  response => {
    const responseSize = JSON.stringify(response.data).length;
    console.log(`收到响应: ${response.config.url}, 状态: ${response.status}, 数据大小: ${responseSize} 字节`);
    
    try {
      // 检查响应数据结构
      if (responseSize > 10 * 1024 * 1024) {
        console.warn('响应数据过大，可能导致解析问题');
      }
      
      // 尝试解析JSON
      if (typeof response.data === 'string') {
        try {
          response.data = JSON.parse(response.data);
        } catch (e) {
          console.error('响应不是有效的JSON:', e);
        }
      }
      
      // 直接返回响应数据，而不是整个响应对象
      return response.data;
    } catch (e) {
      console.error('解析响应数据失败:', e);
      ElMessage.error('解析服务器响应失败');
      return Promise.reject(new Error('解析服务器响应失败'));
    }
  },
  error => {
    console.error('请求错误:', error);
    
    let message = '网络请求失败，请稍后再试';
    
    if (error.message.includes('timeout')) {
      message = '请求超时，服务器响应时间过长，请稍后再试';
    } else if (error.response) {
      switch(error.response.status) {
        case 400:
          message = '请求参数错误';
          break;
        case 401:
          message = '未授权，请重新登录';
          break;
        case 403:
          message = '拒绝访问';
          break;
        case 404:
          message = '请求的资源不存在';
          break;
        case 500:
          if (error.response.data && error.response.data.error) {
            message = `服务器错误: ${error.response.data.error}`;
          } else {
            message = '服务器内部错误';
          }
          break;
        default:
          message = `请求失败 (${error.response.status})`;
      }
      
      // 打印响应详情以便调试
      if (error.response.data) {
        console.error('错误详情:', error.response.data);
      }
    }
    
    ElMessage({
      message: message,
      type: 'error',
      duration: 5 * 1000
    });
    
    return Promise.reject(error);
  }
)

export default service 