<<<<<<< HEAD

<script>
import axios from 'axios';

export default {
  name: 'LoginAndRegister',
  data() {
    return {
      isLogin: true,

      loginForm: {
        username: '',
        password: '',
        role:''
      },

      registerForm: {
        username: '',
        password: '',
        confirmPassword: '',
        role:''
      },

      loginError: '',
      registerError: '',
      registerSuccess: ''
    };
  },

  methods: {
    
    //切换到注册
     switchToRegister() {
      this.isLogin = false; 
      this.resetForm('login');
      this.resetForm('register');
      this.loginError = '';
      this.registerSuccess = '';
    },

    //切换到登录
     switchToLogin() {
      this.isLogin = true; // 将 isLogin 设为 false，界面将显示注册表单
      this.resetForm('login');
      this.resetForm('register');
      this.registerError = ''; 
    },

    //重置表单
     resetForm(formType) {
      if (formType === 'login') {
        this.loginForm.username = '';
        this.loginForm.password = '';
        this.loginForm.role = '';
      } else if (formType === 'register') {
        this.registerForm.username = '';
        this.registerForm.password = '';
        this.registerForm.confirmPassword = '';
        this.registerForm.role = '';
      }
    },

    //处理登录
    async handleLogin() {
      this.loginError = '';

      if (!this.loginForm.username || !this.loginForm.password || !this.loginForm.role) {
        this.loginError = '用户名、密码和角色都不能为空！'; // 也使用数据属性提示
        return;
      }
      
      try {
        const response = await axios.post('/api/login', this.loginForm);

        if (response.data.success) {
          alert('登录成功！');
          this.$router.push('/dashboard');
          
        } else {
          this.loginError = response.data.message || '登录失败，请稍后重试！';
        }

      } catch (error) {
        console.error('登录失败:', error);
        const errorMessage = error.response?.data?.message || '用户名或密码错误。';
        this.loginError = errorMessage;
      }
    },

    //处理注册
    async handleRegister() {

      this.registerError = '';
      this.registerSuccess = '';

      if (this.registerForm.password !== this.registerForm.confirmPassword) {
        this.registerError = '两次输入的密码不匹配！';
        return;
      }

      const registrationData = {
        username: this.registerForm.username,
        password: this.registerForm.password,
        role: this.registerForm.role
      };

      if (!registrationData.username || !registrationData.password || !registrationData.role) {
        this.registerError = '所有字段都不能为空！';
        return;
      }

      try {
        await axios.post('/api/register', registrationData); 
        if (response.data.success) {
          this.registerSuccess = '注册成功，你现在可以登录了！';
          this.switchToLogin();
          
        } else {
          this.registerError = response.data.message || '注册失败，请稍后重试！';
        }
      } catch (error) {
        console.error('注册失败:', error);
        // 尝试显示后端返回的错误信息
        const errorMessage = error.response?.data?.message || '注册失败，请稍后再试。';
        this.registerError = errorMessage;
      }
    }
  }
};
=======
<template>
  <div class="auth-container">
    <h2>{{ isLogin ? '用户登录' : '用户注册' }}</h2>

    <!-- 用户名和密码输入框 -->
    <input 
      type="text" 
      placeholder="请输入用户名" 
      v-model="username"
    />

    <input 
      type="password" 
      placeholder="请输入密码" 
      v-model="password"
    />

    <!-- 确认密码（注册） -->
    <input 
      v-if="!isLogin" 
      type="password" 
      placeholder="请确认密码" 
      v-model="confirmPassword"
    />

    <!-- 按钮 -->
    <button v-if="isLogin" @click="handleLogin">登录</button>
    <button v-else @click="handleRegister">注册</button>
    <button @click="resetForm">重置</button>

    <!-- 切换登录/注册 -->
    <p>
      <span v-if="isLogin">
        还没有账号？ <a href="#" @click.prevent="switchToRegister">去注册</a>
      </span>
      <span v-else>
        已有账号？ <a href="#" @click.prevent="switchToLogin">去登录</a>
      </span>
    </p>

    <button @click="goBack">返回</button>
  </div>
</template>
>>>>>>> 047a07fdfb030db433ec854731c2375aeafbf39d
