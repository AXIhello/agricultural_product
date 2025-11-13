import { createApp } from 'vue'
import { createPinia } from 'pinia'

import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import './style.css'
import App from './App.vue'
import router from './router'
import { useAuthStore } from './stores/authStore'

const app = createApp(App)
const pinia = createPinia() //创建pinia实例

app.use(router)
app.use(ElementPlus)
app.use(pinia)

// 在应用启动时，执行自动登录检查, 检查是否有持久化的 token 并设置 axios 头部
const authStore = useAuthStore()     
authStore.tryAutoLogin();

app.mount('#app')