<template>
  <header class="header">
    <h1 class="header-logo">农慧通</h1>
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
        <li>
          <router-link to="/messages" active-class="active-link">消息中心</router-link>
        </li>
        <li v-if="isAdmin">
          <router-link to="/admin-review" active-class="active-link">审核信息</router-link>
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
  height: 64px;
  padding: 0 24px;

  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-shrink: 0;

  /* 舒适自然的绿渐变 */
  background: linear-gradient(120deg, #2E8B57 0%, #4CAF50 100%);

  color: #fff;
  border-radius: 0 0 14px 14px;

  font-family: 'PingFang SC','Microsoft YaHei',sans-serif;
  font-size: 18px;
  font-weight: 600;

  /* 柔和阴影，避免突兀 */
  box-shadow: 0 3px 10px rgba(0, 0, 0, 0.08);
}


/* 激活：亮绿色 + 阴影更突出 */
.active-link {
  background: #A5D6A7;
  color: #1B5E20;
  box-shadow: 0 0 10px rgba(150, 200, 150, 0.4);
}
.header-logo {
  font-family: "PingFang SC", "Microsoft YaHei", "Helvetica Neue", sans-serif;
  font-size: 40px;
  font-weight: 700;
  color: white; /* 主绿色 */
  letter-spacing: 1px; /* 字母间距略大 */
  margin: 0;
  cursor: pointer;
  transition: transform 0.2s;
}

.header-logo:hover {
  transform: scale(1.05);
}


</style>


