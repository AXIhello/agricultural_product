<template>
  <div class="main-bg">
    <HeaderComponent />
    <main class="content">

      <div class="session-list-wrapper">
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import axios from '../utils/axios'; // 确认axios实例路径
import HeaderComponent from '../components/HeaderComponent.vue'; // 引入头部组件
import { useAuthStore } from '@/stores/authStore';
import {storeToRefs} from 'pinia'

const authStore = useAuthStore();
const { userInfo: currentUser, isLoggedIn } = storeToRefs(authStore);

// --- State ---

// --- Lifecycle Hook ---
onMounted(() => {
  if (isLoggedIn.value) {  

  } else {
    alert('请先登录以查看消息！');
    router.push('/login');
  }
});
</script>

<style scoped>

.session-list-wrapper {
  margin-top: 1rem;
}

</style>