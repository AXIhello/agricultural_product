<template>
  <div class="expert-overview-container">
    <div v-if="!experts.length">正在加载专家列表...</div>

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

const router = useRouter();
const experts = ref([]);

onMounted(() => {
  fetchExperts();
});


async function fetchExperts() {
  try {
    
    const response = await axios.get('/expert/profile/list');
    
    
    if (response.data && response.data.success) {
      experts.value = response.data.data;
    }
  } catch (error) {
    console.error('加载专家列表失败:', error);
  }
}

// 辅助函数，用于处理图片URL
// 如果你的 photo_url 已经是完整的URL，则不需要这个函数
function getFullImageUrl(url) {
  if (!url || url.startsWith('http')) {
    return url || 'https://via.placeholder.com/80'; // 提供一个默认图片
  }
  // 如果 url 是相对路径（如 /uploads/...），你需要拼接上你的后端服务器地址
  // 例如： return `http://localhost:8080${url}`;
  // 更好的方式是配置开发代理（proxy）
  return url;
}

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