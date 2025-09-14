<script setup>

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
.logo {
  height: 6em;
  padding: 1.5em;
  will-change: filter;
  transition: filter 300ms;
}
.logo:hover {
  filter: drop-shadow(0 0 2em #646cffaa);
}
.logo.vue:hover {
  filter: drop-shadow(0 0 2em #42b883aa);
}

.app-container {
  display: flex;
  justify-content: center;
  align-items: flex-start; /* 改为 flex-start 让它从顶部开始 */
  min-height: 100vh;
  padding-top: 50px; /* 增加一些顶部间距 */
  background-color: #f0f2f5; /* 给页面一个浅灰色背景 */
}

</style>
