<template>
  <div class="auth-container">
    <h2>{{ isLogin ? '用户登录' : '用户注册' }}</h2>

    <!-- 消息提示 -->
    <p v-if="errorMsg" class="message error-message">{{ errorMsg }}</p>
    <p v-if="successMsg" class="message success-message">{{ successMsg }}</p>

    <!-- 下拉框 -->
    <div class="form-item">
      <label class="form-label">
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
      <label class="form-label">
        <span class="label-text">用户名</span>
        <span class="label-colon">：</span>
      </label>
      <input type="text" placeholder="请输入用户名" v-model="form.userName" class="input-style" />
    </div>

    <!-- 密码 -->
    <div class="form-item">
      <label class="form-label">
        <span class="label-text">密码</span>
        <span class="label-colon">：</span>
      </label>
      <input type="password" placeholder="请输入密码" v-model="form.password" class="input-style" />
    </div>

    <!-- 确认密码（注册） -->
    <div class="form-item" v-if="!isLogin">
      <label class="form-label">
        <span class="label-text">确认密码</span>
        <span class="label-colon">：</span>
      </label>
      <input type="password" placeholder="请确认密码" v-model="form.confirmPassword" class="input-style" />
    </div>

    <!-- 邮箱 + 内嵌发送按钮 -->
    <div class="form-item" v-if="!isLogin">
      <label class="form-label">
        <span class="label-text">邮箱</span>
        <span class="label-colon">：</span>
      </label>
      <div class="input-wrapper">
        <input
            type="email"
            placeholder="请输入邮箱"
            v-model="form.email"
            class="input-style"
        />
        <button
            @click="sendVerificationCode"
            :disabled="cooldown > 0"
            class="btn-inside"
        >
          {{ cooldown > 0 ? `${cooldown}秒后重试` : '发送验证码' }}
        </button>
      </div>
    </div>

    <!-- 验证码输入框（注册） -->
    <div class="form-item" v-if="!isLogin">
      <label class="form-label">
        <span class="label-text">验证码</span>
        <span class="label-colon">：</span>
      </label>
      <input type="text" placeholder="请输入验证码" v-model="form.verificationCode" class="input-style" maxlength="6" />
    </div>

    <!-- 按钮组 -->
    <div class="form-item button-group">
      <button v-if="isLogin" @click="handleLogin" class="btn">登 录</button>
      <button v-else @click="handleRegister" class="btn">注 册</button>
      <button @click="resetForm" class="btn">重 置</button>
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

    <div class="apply-link" v-if="!isLogin">
      <p>
        您是银行或专家用户？<a href="#" @click.prevent="goToUserApplication">请提交申请</a>
      </p>
    </div>
   
    <button @click="goBack" class="fixed-btn">返 回</button>
  </div>
</template>

<style>
/* 样式保持不变，可直接使用你原来的 */
.auth-container {
  box-sizing: border-box;
  width: 700px;
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
  width: 50%;
  margin: 0 auto 10px auto;
  text-align: center;
  letter-spacing: 0.5em;
  font-size: 1.8em;
}
.message {
  width: 80%;
  margin: -5px auto 10px auto;
  padding: 10px;
  border-radius: 4px;
  text-align: center;
  font-size: 0.9em;
}
.error-message { color: #a94442; background-color: #f2dede; border: 1px solid #ebccd1; }
.success-message { color: #3c763d; background-color: #dff0d8; border: 1px solid #d6e9c6; }
.form-item {
  width: 80%;
  display: flex;
  align-items: center;
  margin: 0 auto 15px auto;
}
.form-label {
  display: flex;
  align-items: center;
  margin-right: 10px;
  min-width: 6em;
}
.label-text {
  flex: 1;
  text-align: justify;
  text-align-last: justify;
}
.label-colon {
  width: 1em;
  text-align: justify;
  margin-left: 2px;
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
.input-wrapper {
  position: relative;
  flex: 1;
}
.input-wrapper .input-style {
  width: 100%;
  padding-right: 130px;
  box-sizing: border-box;
}
.btn-inside {
  position: absolute;
  right: 5px;
  top: 50%;
  transform: translateY(-50%);
  height: 70%;
  padding: 0 10px;
  font-size: 14px;
  cursor: pointer;
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
  text-align: center;
}
.button-group .btn:last-child { margin-right: 0; }
.btn:hover { border-color: #646cff; background-color: #369870; }
.switch-text { text-align: center; margin-top: 5px; font-size: 0.95em; }

.apply-links {
  text-align: center;
  margin-top: 4px;
  font-size: 0.9em;
}
.apply-links a {
  color: #42b983; 
  text-decoration: none;
  margin: 0 5px; 
}
.apply-links a:hover {
  text-decoration: underline; 
}
</style>

<script>
import axios from '../utils/axios';

import '../style.css';

export default {
  name: 'LoginAndRegister',
  data() {
    return {
      isLogin: true,
      form: {
        userName: '',
        password: '',
        role: 'bank',
        confirmPassword: '',
        email: '',
        verificationCode: ''
      },
      errorMsg: '',
      successMsg: '',
      cooldown: 0
    };
  },
  methods: {
    clearMessages() {
      this.errorMsg = '';
      this.successMsg = '';
    },
    switchToRegister() {
      this.isLogin = false;
      this.resetForm();
    },
    switchToLogin() {
      this.isLogin = true;
      this.resetForm();
    },
    resetForm() {
      this.form = {
        userName: '',
        password: '',
        role: '',
        confirmPassword: '',
        email: '',
        verificationCode: ''
      };
      this.clearMessages();
    },
    async handleLogin() {
      this.clearMessages();
      const { userName, password, role } = this.form;
      if (!userName || !password || !role) {
        this.errorMsg = '用户名、密码和角色都不能为空！';
        return;
      }

      try {
        const response = await axios.post('/user/login', { userName, password, role });
        if (response.data.success) {
          this.successMsg = '登录成功！正在跳转...';

          // 保存 token 和用户信息
          localStorage.setItem('token', response.data.token);
          localStorage.setItem('user', JSON.stringify(response.data.user));

          // 延迟跳转主页
          setTimeout(() => this.$router.push('/main'), 1500);

        } else {
          this.errorMsg = response.data.message || '登录失败，请稍后重试！';
        }
      } catch (error) {
        console.error(error);
        this.errorMsg = error.response?.data?.message || '服务器连接失败或用户名、密码错误。';
      }
    },
    async handleRegister() {
      this.clearMessages();
      const { userName, password, confirmPassword, role, email, verificationCode } = this.form;

      if (password !== confirmPassword) {
        this.errorMsg = '两次输入的密码不匹配！';
        return;
      }

      if (!userName || !password || !role || !email || !verificationCode) {
        this.errorMsg = '所有字段都不能为空！';
        return;
      }

      try {
        // 验证验证码
        const verifyRes = await axios.post('/email/verify-code', { email, code: verificationCode });
        if (!verifyRes.data.success) {
          this.errorMsg = '验证码错误或已过期！';
          return;
        }

        // 注册用户
        const response = await axios.post('/user/register', { userName, password, role, email });
        if (response.data.success) {
          this.successMsg = '注册成功，你现在可以登录了！';
          setTimeout(() => this.switchToLogin(), 2000);
        } else {
          this.errorMsg = response.data.message || '注册失败，请稍后重试！';
        }
      } catch (error) {
        console.error(error);
        this.errorMsg = error.response?.data?.message || '注册失败，服务器发生错误。';
      }
    },
    async sendVerificationCode() {
      if (!this.form.email) {
        this.errorMsg = '请输入邮箱地址！';
        return;
      }
      const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
      if (!emailRegex.test(this.form.email)) {
        this.errorMsg = '请输入有效的邮箱地址！';
        return;
      }
      try {
        const response = await axios.get('/email/send-code', { params: { email: this.form.email } });
        if (response.data.success) {
          this.successMsg = '验证码已发送，请查收邮件！';
          this.startCooldown();
        } else {
          this.errorMsg = response.data.message || '验证码发送失败，请稍后重试！';
        }
      } catch (error) {
        console.error(error);
        this.errorMsg = error.response?.data?.message || '发送验证码失败，请稍后重试！';
      }
    },
    startCooldown() {
      this.cooldown = 60;
      const timer = setInterval(() => {
        this.cooldown--;
        if (this.cooldown <= 0) clearInterval(timer);
      }, 1000);
    },
    goBack() {
      window.location.href = "/main";
    },
    goToUserApplication() {
      this.$router.push({
        path: '/apply',
  
      });
    }
  }
};
</script>