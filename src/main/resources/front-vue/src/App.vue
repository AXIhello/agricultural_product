<script setup>
import { onMounted, onUnmounted } from 'vue';
import router from './router';

function handleStorageChange(event) {
  
  if (event.key === 'token') {
    // 如果 token 变成了 null/undefined (表示在其他窗口登出了)
    if (!event.newValue) {
      alert('您已在其他窗口登出，请重新登录。');
      
      router.push('/login');
    } 
    // 如果 token 变了 (表示在其他窗口登录了新账号)
    else if (event.oldValue !== event.newValue) {
      alert('检测到新的登录会话，当前页面将刷新以同步状态。');
      // 直接刷新页面，让页面用新的 token 重新加载所有信息
      window.location.reload();
    }
  }
}

onMounted(() => {
  window.addEventListener('storage', handleStorageChange);
});

onUnmounted(() => {
  window.removeEventListener('storage', handleStorageChange);
});
</script>

<template>
  <div class="app-container">
    <!-- 
      路由的出口
    -->
    <router-view />
  </div>
</template>

<style scoped>
.app-container {
  display: flex;
  justify-content: center;
  align-items: flex-start;
  min-height: 100vh;
  background: linear-gradient(135deg, #E8F5E9 0%, #C8E6C9 100%) fixed;
}

/* 保留 logo 动画效果，但换成与主题协调颜色 */
.logo {
  height: 6em;
  padding: 1.5em;
  will-change: filter;
  transition: filter 300ms ease;
}
.logo:hover {
  filter: drop-shadow(0 0 1.5em rgba(76, 175, 80, 0.45));
}
</style>

