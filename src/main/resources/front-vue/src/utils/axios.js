import axios from 'axios'

const instance = axios.create({
    baseURL: 'http://localhost:8080',
    timeout: 5000,
    withCredentials: true
})

// 请求拦截器
instance.interceptors.request.use(
    config => {
        // 可以在这里添加token等认证信息
        return config
    },
    error => {
        return Promise.reject(error)
    }
)

// 响应拦截器
instance.interceptors.response.use(
    response => {
        return response
    },
    error => {
        return Promise.reject(error)
    }
)

export default instance