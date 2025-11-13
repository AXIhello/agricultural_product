<template>
  <header class="header">
    <h1>农产品交易平台</h1>
    <nav>
      <ul>
        <li>
          <router-link to="/main" active-class="active-link">首页</router-link>
        </li>
        <li v-if="isFinancing">
          <router-link to="/finance" active-class="active-link">融资服务</router-link>
        </li>
        <li>
          <router-link to="/expert" active-class="active-link">专家助力</router-link>
        </li>
        <li>
          <router-link to="/trading" active-class="active-link">农产品交易</router-link>
        </li>
        <li v-if="isAdmin">
          <router-link to="/admin-review" active-class="active-link">审核信息</router-link>
        </li>
        <li>
          <router-link to="/messages">我的消息</router-link>
        </li>
        <li>
          <router-link to="/profile" active-class="active-link">个人信息</router-link>
        </li>
      </ul>
    </nav>
  </header>
</template>

<script>
import { mapState } from 'pinia';
import { useAuthStore } from '@/stores/authStore';

export default {
  name: 'HeaderComponent',
 
  computed: {
    // 使用 mapState 将 store 中的 `role` 状态映射到本组件的 `this.role`
    // 现在 this.role 是一个计算属性，它会自动响应 store 中的变化
    ...mapState(useAuthStore, ['role']),
   
    isAdmin() {
      return this.role === 'admin';
    },
    isFinancing(){
      console.log('身份已检验', this.role);
      return this.role === 'farmer' || this.role === 'bank' || this.role === 'admin';
    }
    
  },
  
};
</script>

<style scoped>
.header {
  width: 100%;
  background: #2D7D4F;
  color: white;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  height: 60px;
  font-size: 15px;
  font-weight: 600;
  border-radius: 8px;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
  font-family: 'PingFang SC', 'Heiti SC', 'Microsoft YaHei', sans-serif;
}

nav ul {
  list-style: none;
  display: flex;
  padding: 0;
  margin: 0;
}

nav li {
  margin-right: 50px;
}

nav a {
  text-decoration: none;
  color: white;
  font-weight: 600;
  font-size: 20px;
  transition: color 0.3s, background-color 0.3s;
  padding: 6px 10px;
  border-radius: 6px;
  font-family: 'PingFang SC', 'Heiti SC', 'Microsoft YaHei', sans-serif;
}

/* 鼠标悬停时字体颜色变浅绿 */
nav a:hover {
  color: #B7E4C7;
}

/* 当前激活路由的样式（浅绿色背景 + 深绿色文字） */
.active-link {
  background-color: #B7E4C7;
  color: #2D7D4F;
}
</style>
