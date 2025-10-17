<template>
  <div class="application-container">
    <h2>专家/银行账号申请</h2>

    <!-- 消息提示 -->
    <p v-if="errorMsg" class="message error-message">{{ errorMsg }}</p>
    <p v-if="successMsg" class="message success-message">{{ successMsg }}</p>

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

    <!-- 姓名 -->
    <div class="form-item">
      <label class="form-label">
        <span class="label-text">姓名</span>
        <span class="label-colon">：</span>
      </label>
      <input type="text" placeholder="请输入姓名" v-model="form.name" class="input-style" />
    </div>

    <!-- 邮箱 -->
    <div class="form-item">
      <label class="form-label">
        <span class="label-text">邮箱</span>
        <span class="label-colon">：</span>
      </label>
      <input type="email" placeholder="请输入邮箱" v-model="form.email" class="input-style" />
    </div>

    <!-- 申请类型 下拉框 -->
    <!-- applyRole  -->
    <div class="form-item">
      <label class="form-label">
        <span class="label-text">身份</span>
        <span class="label-colon">：</span>
      </label>
      <select v-model="form.applyRole" class="input-style">
        <option value="bank">银行</option>
        <option value="expert">专家</option>
      </select>
    </div>

    <!-- 文件上传 -->
    <div class="form-item">
      <label class="form-label">
        <span class="label-text">相关文件</span>
        <span class="label-colon">：</span>
      </label>
      <input type="file" @change="handleFileChange" class="input-style" />
      <p v-if="form.file" class="file-name">已选择: {{ form.file.name }}</p>
    </div>

    <!-- 提交按钮 -->
    <div class="form-item button-group">
      <button @click="handleSubmit" class="btn">提交申请</button>
      <button @click="goBack" class="btn">返 回</button>
    </div>
  </div>
</template>

<style scoped>

.application-container {
  box-sizing: border-box;
  width: 700px;
  margin: 20px auto; /* 调整上下边距 */
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
.application-container h2 {
  white-space: nowrap;
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
.form-item .btn,
.form-item input[type="file"] { /* 针对文件选择框 */
  flex: 1;
  font-size: 1em;
  height: 36px;
  padding: 0 10px;
  box-sizing: border-box;
  border: 1px solid #ccc;
  border-radius: 4px;
}
.btn { /* 复用 LoginAndRegister 的按钮样式 */
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
.file-name {
  text-align: left;
  margin-top: 5px;
  font-size: 0.9em;
  color: #666;
}
</style>

<script>
import axios from '../utils/axios'; // 导入 axios  

import '../style.css';

export default {
  name: 'UserApplication',
  data() {
    return {
      form: {
        userName: '',
        password: '',
        name: '',
        email: '',
        applyRole: '', 
        file: null //  上传的文件
      },
      errorMsg: '',
      successMsg: ''
    };
  },
  methods: {
    clearMessages() {
      this.errorMsg = '';
      this.successMsg = '';
    },
    handleFileChange(event) {
      this.form.file = event.target.files[0];  // 获取选择的文件
    },
    async handleSubmit() {
      this.clearMessages();

      // 验证表单
      if (!this.form.userName || !this.form.password || !this.form.name || !this.form.email || !this.form.file) {
        this.errorMsg = '请填写所有必填字段并选择文件！';
        return;
      }

      const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
      if (!emailRegex.test(this.form.email)) {
        this.errorMsg = '请输入有效的邮箱地址！';
        return;
      }
       if (this.form.password.length < 6) {
           this.errorMsg = '密码长度至少为6位！';
           return;
       }

      const formData = new FormData();
      formData.append('userName', this.form.userName);
      formData.append('password', this.form.password);
      formData.append('name', this.form.name);
      formData.append('email', this.form.email);
      formData.append('applyRole', this.form.applyRole);
      formData.append('file', this.form.file);

      try {
        const response = await axios.post('/apply/submit', formData, {
            headers: {
                'Content-Type': 'multipart/form-data' // 关键：设置Content-Type
            }
        });

        if (response.data.code === 200) { // 根据你的后端返回结果修改
          this.successMsg = '申请已提交，请等待管理员审核！';
          //  重置表单
          this.resetForm();
          // 可以考虑自动跳转，或者显示成功的消息
        } else {
          this.errorMsg = response.data.message || '提交申请失败，请稍后重试！';
        }
      } catch (error) {
        console.error(error);
        this.errorMsg = error.response?.data?.message || '服务器连接失败，或提交申请失败。';
      }
    },
    resetForm() {
      this.form = {
        userName: '',
        password: '',
        name: '',
        email: '',
        applyRole: '',
        file: null
      };
    },
    goBack() {
      this.$router.go(-1);  // 返回上一页， 假设使用了vue-router
    }
  }
};
</script>