

<template>
  <div class="auth-container">
    <h2>{{ isLogin ? '用户登录' : '用户注册' }}</h2>
    <p></p>


    <!--消息提示区域-->
    <p v-if="errorMsg" class="message error-message">{{ errorMsg }}</p>
    <p v-if="successMsg" class="message success-message">{{ successMsg }}</p>

    <!-- 下拉框 -->
    <div class="form-item">
      <label>
        <span class="label-text">身份</span>
        <span class="label-colon">：</span>
      </label>
      <select v-model="form.role" class="input-style">
        <option value="bank" v-if="isLogin">银行</option>
        <option value="expert" v-if="isLogin">专家</option>
        <option value="farmer">农户</option>
        <option value="buyer">买家</option>
      </select>
    </div>

    <!-- 用户名 -->
    <div class="form-item">
      <label>
        <span class="label-text">用户名</span>
        <span class="label-colon">：</span>
      </label>
      <input
        type="text"
        placeholder="请输入用户名"
        v-model="form.userName"
        class="input-style"
      />
    </div>

    <!-- 密码 -->
    <div class="form-item">
      <label>
        <span class="label-text">密码</span>
        <span class="label-colon">：</span>
      </label>
      <input
        type="password"
        placeholder="请输入密码"
        v-model="form.password"
        class="input-style"
      />
    </div>

    <!-- 确认密码（注册） -->
    <div class="form-item" v-if="!isLogin">
      <label>
        <span class="label-text">确认密码</span>
        <span class="label-colon">：</span>
      </label>
      <input
        type="password"
        placeholder="请确认密码"
        v-model="form.confirmPassword"
        class="input-style"
      />
    </div>

    <!-- 邮箱 -->
    <div class="form-item" v-if="!isLogin">
      <label>
        <span class="label-text">邮箱</span>
        <span class="label-colon">：</span>
      </label>
      <input
        type="email"
        placeholder="请输入邮箱"
        v-model="form.email"
        class="input-style"
      />
    </div>

    <!-- 按钮 -->
    <div class="form-item button-group">
      <button v-if="isLogin" @click="handleLogin" class="btn">登  录</button>
      <button v-else @click="handleRegister" class="btn">注  册</button>
      <button @click="resetForm" class="btn">重  置</button>
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

    <button @click="goBack" class="btn go-back">返 回</button>
  </div>
</template>

<script>
import axios from 'axios';
import '../style.css';

export default {
  name: 'LoginAndRegister',
  data() {
    return {
      isLogin: true,

      form: {
        userName: '',
        password: '',
        role: 'buyer',
        confirmPassword: '',
        email: ''
      },

      errorMsg: '',
      successMsg: ''
    };
  },

  methods: {
    // 清除消息
    clearMessages() {
      this.errorMsg = '';
      this.successMsg = '';
    },

    // 切换到注册
    switchToRegister() {
      this.isLogin = false;
      this.resetForm();
      this.form.role = 'buyer'; // 默认角色为买家
    },

    // 切换到登录
    switchToLogin() {
      this.isLogin = true;
      this.resetForm();
      this.form.role = 'buyer'; // 默认角色为买家
    },

    // 重置表单
    resetForm() {
      this.form = {
        userName: '',
        password: '',
        role: '',
        confirmPassword: '',
        email: ''
      };
      this.form.role = this.isLogin ? 'bank' : 'farmer';
      this.clearMessages();
    },

    // 处理登录
    async handleLogin() {
      this.errorMsg = '';
      const { userName, password, role } = this.form;

      if (!userName || !password || !role) {
        this.errorMsg = '用户名、密码和角色都不能为空！';
        return;
      }

      try {
        const response = await axios.post('/api/user/login', { userName, password, role });

        if (response.data.success) {
          this.successMsg = '登录成功！正在跳转...';
          // 延迟跳转
          setTimeout(() => {
            this.$router.push('/dashboard');
          }, 1500);
        } else {
          this.errorMsg = response.data.message || '登录失败，请稍后重试！';
        }
      } catch (error) {
        console.error('登录失败:', error);
        this.errorMsg = error.response?.data?.message || '用户名或密码错误。';
      }
    },

    // 处理注册
    async handleRegister() {
      this.errorMsg = '';
      this.successMsg = '';
      const { userName, password, confirmPassword, role, email } = this.form;

      if (password !== confirmPassword) {
        this.errorMsg = '两次输入的密码不匹配！';
        return;
      }

      if (!userName || !password || !role || !email) {
        this.errorMsg = '所有字段都不能为空！';
        return;
      }

      try {
        const response = await axios.post('/api/user/register', { userName, password, role, email });
        if (response.data.success) {
          this.successMsg = '注册成功，你现在可以登录了！';
          setTimeout(() => {
            this.switchToLogin();
          }, 2000); // 延迟2秒再切换
        } else {
          this.errorMsg = response.data.message || '注册失败，请稍后重试！';
        }
      } catch (error) {
        console.error('注册失败:', error);
        this.errorMsg = error.response?.data?.message || '注册失败，请稍后再试。';
      }
    },

    goBack() {
      this.$router.go(-1);
    }
  }
};
</script>


<style>
.auth-container {
  box-sizing: border-box;
  width: 500px;
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
  max-height: calc(100vh - 100px);
  overflow-y: auto;
}

.auth-container h2 {
  width: 50%;               /* 整个标题占容器宽度 50% */
  margin: 0 auto;           /* 水平居中 */
  text-align: justify;      /* 左右对齐文字 */
  text-align-last: center;  /* 最后一行文字居中 */
  letter-spacing: 0.5em;    /* 每个字之间间距，可调整 */
  font-size: 1.8em;           /* 标题大小，可根据需要调整 */
  margin-bottom: 10px;
}

/* 消息提示的样式 */
.message {
  width: 80%;
  margin: -5px auto 10px auto; /* 调整外边距，让它更紧凑 */
  padding: 10px;
  border-radius: 4px;
  text-align: center;
  font-size: 0.9em;
}

.error-message {
  color: #a94442;
  background-color: #f2dede;
  border: 1px solid #ebccd1;
}

.success-message {
  color: #3c763d;
  background-color: #dff0d8;
  border: 1px solid #d6e9c6;
}


.form-item {
  width: 80%;
  display: flex;
  margin: 10px auto;
  justify-content: center;
  align-items: center;
  margin-bottom: 15px;
}

.label-text {
  display: inline-block;
  width: 4em;
  text-align: justify;
  text-align-last: justify;
  margin-right: 10px;
}

.label-colon {
  display: inline-block;
}

.input-style,
.form-item select,
.form-item .btn {
  flex: 1;
  font-size: 1em;
  height: 36px;
  padding: 0 10px;
  box-sizing: border-box;
  border: 1px solid #ccc;
  border-radius: 4px;
}

select.input-style {
  appearance: none;
  -webkit-appearance: none;
  -moz-appearance: none;
}

input:-webkit-autofill {
  background-color: #fff !important;
  -webkit-box-shadow: 0 0 0px 1000px #fff inset !important;
  -webkit-text-fill-color: #333 !important;
}

.btn {
  width: 100%;
  padding: 0 20%;
  font-size: 1em;
  font-weight: 500;
  font-family: inherit;
  border-radius: 8px;
  border: 1px solid transparent;
  cursor: pointer;
  transition: border-color 0.25s, background-color 0.25s;
  background-color: #42b983;
  color: white;
  margin-right: 50px;
  text-align: center;     /* 文字居中 */
}

.button-group .btn:last-child {
  margin-right: 0;       /* 最后一个按钮不加右边距 */
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
   margin-top: 10px; /* 与上方元素增加一点间距 */
  width: 80%; /* 与表单项宽度保持一致 */
  margin-left: auto;
  margin-right: auto;
}
</style>