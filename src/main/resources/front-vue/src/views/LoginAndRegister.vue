import '../style.css';

<template>
  <div class="auth-container">
    <h2>{{ isLogin ? '用户登录' : '用户注册' }}</h2>

    <!-- 下拉框 -->
    <div class="form-item">
      <label for="role">选择身份：</label>
      <select id="role" v-model="selectedRole" class="input-style">
        <option value="user">用户</option>
        <option value="admin">管理员</option>
        <option value="guest">游客</option>
      </select>
    </div>

    <!-- 用户名 -->
    <div class="form-item">
      <input 
        type="text" 
        placeholder="请输入用户名" 
        v-model="userName"
        class="input-style"
      />
    </div>

    <!-- 密码 -->
    <div class="form-item">
      <input 
        type="password" 
        placeholder="请输入密码" 
        v-model="password"
        class="input-style"
      />
    </div>

    <!-- 确认密码（注册） -->
    <div class="form-item" v-if="!isLogin">
      <input 
        type="password" 
        placeholder="请确认密码" 
        v-model="confirmPassword"
        class="input-style"
      />
    </div>

    <!-- 按钮 -->
    <div class="form-item button-group">
      <button v-if="isLogin" @click="handleLogin" class="btn">登录</button>
      <button v-else @click="handleRegister" class="btn">注册</button>
      <button @click="resetForm" class="btn">重置</button>
    </div>

    <!-- 切换登录/注册 -->
    <p class="switch-text">
      <span v-if="isLogin">
        还没有账号？ <a href="#" @click.prevent="switchToRegister">去注册</a>
      </span>
      <span v-else>
        已有账号？ <a href="#" @click.prevent="switchToLogin">去登录</a>
      </span>
    </p>

    <button @click="goBack" class="btn go-back">返回</button>
  </div>
</template>

<style>
.auth-container {
  width: 320px;
  margin: auto;
  padding: 2rem;
  text-align: center;
  display: flex;
  flex-direction: column;
  gap: 15px;
  background-color: #ffffff;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.3);
  color: rgba(0, 0, 0, 0.87);
}

.input-style {
  height: 35px;
  padding: 5px 10px;
  font-size: 1em;
  border-radius: 8px;
  border: 1px solid #ccc;
  font-family: inherit;
  background-color: #1a1a1a;
  color: rgba(255,255,255,0.87);
  box-sizing: border-box;
}

select.input-style {
  appearance: none;
  -webkit-appearance: none;
  -moz-appearance: none;
}

.btn {
  width: 100%;
  padding: 0.6em 1.2em;
  font-size: 1em;
  font-weight: 500;
  font-family: inherit;
  border-radius: 8px;
  border: 1px solid transparent;
  cursor: pointer;
  transition: border-color 0.25s, background-color 0.25s;
  background-color: #42b983;
  color: white;
}

.btn:hover {
  border-color: #646cff;
  background-color: #369870;
}

.switch-text {
  text-align: center;
  margin-top: 5px;
  font-size: 0.95em;
}

.go-back {
  background-color: #ccc;
  color: #333;
}
</style>


<script>
import axios from 'axios';

export default {
  name: 'LoginAndRegister',
  data() {
    return {
      isLogin: true,

      loginForm: {
        userName: '',
        password: '',
        role:''
      },

      registerForm: {
        userName: '',
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
        this.loginForm.userName = '';
        this.loginForm.password = '';
        this.loginForm.role = '';
      } else if (formType === 'register') {
        this.registerForm.userName = '';
        this.registerForm.password = '';
        this.registerForm.confirmPassword = '';
        this.registerForm.role = '';
      }
    },

    //处理登录
    async handleLogin() {
      this.loginError = '';

      if (!this.loginForm.userName || !this.loginForm.password || !this.loginForm.role) {
        this.loginError = '用户名、密码和角色都不能为空！'; // 也使用数据属性提示
        return;
      }
      
      try {
        const response = await axios.post('/api/user/login', this.loginForm);

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
        userName: this.registerForm.userName,
        password: this.registerForm.password,
        role: this.registerForm.role
      };

      if (!registrationData.userName || !registrationData.password || !registrationData.role) {
        this.registerError = '所有字段都不能为空！';
        return;
      }

      try {
        await axios.post('/api/user/register', registrationData);
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
</script>
