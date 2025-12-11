
import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import axios from '../utils/axios'; 
import router from '../router'; 

// defineStore 的第一个参数是 store 的唯一 ID
export const useAuthStore = defineStore('auth', () => {
    // --- STATE (状态) ---
    // 使用 ref 创建响应式状态，并从 localStorage 初始化，防止刷新后状态丢失
    const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || 'null'));
    const token = ref(localStorage.getItem('token') || null);

    // --- GETTERS (计算属性) ---
    // 判断是否登录
    const isLoggedIn = computed(() => !!token.value && !!userInfo.value);
    // 获取用户角色，提供默认值以防万一
    const role = computed(() => userInfo.value?.role || null);


    // --- ACTIONS (方法) ---
    /**
     * 登录操作
     * @param {object} credentials - 登录凭证 { userName, password ,role}
     */
    async function login(credentials) {
        // 调用后端的登录接口
        console.log("Sending to backend:", credentials);
        console.log("Step 1: Attempting to log in with credentials:", credentials);
        const response = await axios.post('/user/login', credentials);
        
        console.log("Step 2: Received response from backend:", response);

        if (response.data && response.data.success) {

            const user = response.data.user; 
            const userToken = response.data.token;

            // ★★★ 关键日志 2：打印提取出的 user 和 token ★★★
            console.log("Step 4: Extracted user object:", user);
            console.log("Step 5: Extracted token:", userToken);

            // 更新 Pinia 的 state
            userInfo.value = user;
            token.value = userToken;

            console.log("Step 6: Preparing to write to localStorage. Key: 'userInfo', Value:", JSON.stringify(user));

            // 持久化到 localStorage，用于页面刷新后恢复状态
            localStorage.setItem('userInfo', JSON.stringify(user));
            localStorage.setItem('token', userToken);
            
            console.log("Step 7: Wrote to localStorage successfully.");
            // 更新 axios 的默认请求头
            axios.defaults.headers.common['Authorization'] = `Bearer ${userToken}`;
        } else {
            throw new Error(response.data.message || 'Login failed');
        }
    }

    /**
     * 登出操作
     */
    function logout() {
        // 1. 清空 Pinia 的 state
        userInfo.value = null;
        token.value = null;

        // 2. 清空 localStorage
        localStorage.removeItem('userInfo');
        localStorage.removeItem('token');
        
        // 3. 移除 axios 的默认请求头
        delete axios.defaults.headers.common['Authorization'];
        
        // 4. 跳转到登录页
        router.push('/login');
    }

    /**
     * 应用初始化时调用，尝试从 localStorage 恢复登录状态
     */
    function tryAutoLogin() {
        if (token.value) {
            // 如果 localStorage 中有 token，则设置好 axios 的默认请求头
            axios.defaults.headers.common['Authorization'] = `Bearer ${token.value}`;
            console.log("Session restored from localStorage.");
        }
    }

    /**
     * 更新用户信息
     * @param {Object} newInfo - 需要更新的字段
     */
    function setUserInfo(newInfo) {
        if (!userInfo.value) return;
        userInfo.value = { ...userInfo.value, ...newInfo };
        localStorage.setItem('userInfo', JSON.stringify(userInfo.value));
    }

    return {
        userInfo,
        token,
        isLoggedIn,
        role,
        login,
        logout,
        tryAutoLogin,
        setUserInfo   // ✅ 新增方法
    };
});