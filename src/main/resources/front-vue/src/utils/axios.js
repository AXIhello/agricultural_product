import axios from 'axios'

// 创建axios实例
const instance = axios.create({
  baseURL: 'http://localhost:8080', // 后端接口的基础URL
  timeout: 10000, // 请求超时时间
  headers: {
    'Content-Type': 'application/json'
  }
})

// 添加请求拦截器
instance.interceptors.request.use(
  config => {
    // 在发送请求之前做些什么
    const token = localStorage.getItem('token')
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    return config
  },
  error => {
    // 对请求错误做些什么
    return Promise.reject(error)
  }
)

// 添加响应拦截器
instance.interceptors.response.use(
  response => {
    // 对响应数据做点什么
    return response
  },
  error => {
    // 对响应错误做点什么
    if (error.response) {
      switch (error.response.status) {
        case 401:
          // 未授权，清除token并跳转到登录页面
          localStorage.removeItem('token')
          // 如果使用了vue-router，可以使用router进行跳转
          // router.push('/login')
          break
        case 403:
          // 权限不足
          console.error('没有权限访问此资源')
          break
        case 500:
          console.error('服务器内部错误')
          break
        default:
          console.error(error.response.data.message || '请求失败')
      }
    }
    return Promise.reject(error)
  }
)

export default instance