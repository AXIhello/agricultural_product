// utils/axios.js
import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '../router'

// 创建 axios 实例
const instance = axios.create({
    baseURL: '/api', // 统一代理到 /api
    timeout: 10000,
    headers: {
        'Content-Type': 'application/json'
    }
})

// 请求拦截器：自动加 token
instance.interceptors.request.use(
    config => {
        const token = localStorage.getItem('token')
        if (token) {
            config.headers['Authorization'] = `Bearer ${token}`
        }
        return config
    },
    error => Promise.reject(error)
)

// 响应拦截器：统一处理错误
instance.interceptors.response.use(
    response => response,
    error => {
        if (error.response) {
            const status = error.response.status
            switch (status) {
                case 401:
                    ElMessage.error('登录过期，请重新登录')
                    localStorage.removeItem('token')
                    localStorage.removeItem('userInfo')
                    localStorage.removeItem('user')
                    router.push('/login')
                    break
                case 403:
                    ElMessage.error('没有权限访问此资源')
                    break
                case 500:
                    ElMessage.error('服务器内部错误')
                    break
                default:
                    ElMessage.error(error.response.data.message || '请求失败')
            }
        } else {
            ElMessage.error('网络连接失败，请检查网络')
        }
        return Promise.reject(error)
    }
)

export default instance
