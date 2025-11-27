<template>
  <div class="expert-overview-container">
    <div v-if="!experts.length">当前暂无专家！</div>

    <div 
      class="expert" 
      v-for="expert in experts" 
      :key="expert.id"
      @click="goToExpertDetail(expert.id)"
    >
      <img :src="getFullImageUrl(expert.avatar)" alt="专家头像" style="width: 80px; height: 80px; border-radius: 50%; margin-bottom: 10px;">
      <h4 style="font-size: 16px; margin-bottom: 5px;">{{ expert.name }}</h4>
      <p style="font-size: 14px;">擅长领域：{{ expert.specialty }}</p>
    </div>

  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import axios from '../utils/axios'; 
import { useRouter } from 'vue-router';
import defaultAvatar from '@/assets/default.jpg';

const router = useRouter();
const experts = ref([]);

const API_BASE_URL = 'http://localhost:8080';//后端基础地址

onMounted(() => {
  fetchExperts();
});


//获取专家列表
async function fetchExperts() {
  try {
    const response = await axios.get('/expert/profile/list');

    // 按你的后端格式结构解析
    if (response.data?.success) {
      experts.value = response.data.data || [];
    } else {
      console.error('后端返回失败:', response.data?.message);
    }
  } catch (error) {
    console.error('加载专家列表失败:', error);
  }
}

// 处理专家图片
function getFullImageUrl(url) {
  // 情况1：如果 url 为空，返回默认头像
  if (!url) {
    return defaultAvatar;
  }

  // 情况2：如果是网络图片 (http 开头)，直接返回
  if (url.startsWith('http')) {
    return url;
  }

  // 情况3：如果是相对路径 (/uploads/...)，拼接后端地址
  return `${API_BASE_URL}${url}`;
}

//跳转到专家详情页
function goToExpertDetail(id) {
  console.log('尝试跳转到专家详情页，专家ID是:', id);
  if (id) {
    router.push(`/expert/${id}`);
  }
}

</script>

<style scoped>
.expert-overview-container {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 20px;
}
.expert {
  text-align: center;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  background-color: #f9f9f9;
}
</style>