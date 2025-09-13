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
