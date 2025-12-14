<template>
  <div class="expert-overview-container">
    <div class="empty-state" v-if="!experts.length">当前暂无专家！</div>

    <div
        class="expert"
        v-for="expert in experts"
        :key="expert.id"
        @click="goToExpertDetail(expert.id)"
    >
      <img :src="expert.photoUrl" alt="专家头像" style="width: 80px; height: 80px; border-radius: 50%; margin-bottom: 10px;">
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

const API_BASE_URL = 'http://localhost:8080'; // 后端基础地址

onMounted(() => {
  fetchExperts();
});

// 获取专家列表
async function fetchExperts() {
  try {
    const response = await axios.get('/expert/profile/list');

    if (!response.data?.success) {
      console.error('后端返回失败:', response.data?.message);
      return;
    }

    const dataList = response.data.data || [];

    // 遍历每个专家，尝试获取头像 Blob
    experts.value = await Promise.all(dataList.map(async (item) => {
      let photoUrl = defaultAvatar;

      if (item.avatar) {
        try {
          const imageRes = await axios.get(`/expert/profile/${item.id}/photo`, {
            responseType: 'blob'
          });

          if (imageRes.data.size > 0) {
            photoUrl = URL.createObjectURL(imageRes.data);
          }
        } catch (err) {
          console.warn('获取头像失败，使用默认头像:', item.id);
        }
      }

      return {
        ...item,
        photoUrl
      };
    }));

  } catch (error) {
    console.error('加载专家列表失败:', error);
  }
}

// 跳转到专家详情页
function goToExpertDetail(id) {
  if (id) {
    router.push(`/expert/${id}`);
  }
}
</script>

<style scoped>
.expert-overview-container {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
  gap: 22px;
  padding: 10px;
}

/* 单个专家 */
.expert {
  background-color: #f8fbf8;
  padding: 16px 12px;
  border-radius: 16px;

  border: 1px solid #d9e8df;
  box-shadow: 0 3px 8px rgba(0, 0, 0, 0.06);

  transition: all 0.25s ease;
  cursor: pointer;
  text-align: center;
}

/* 悬停提升感 */
.expert:hover {
  transform: translateY(-4px);
  border-color: #8bc3a1;
  box-shadow: 0 6px 14px rgba(0, 0, 0, 0.10);
}

/* 专家头像 */
.expert img {
  width: 72px;
  height: 72px;
  border-radius: 50%;
  object-fit: cover;
  margin-bottom: 10px;
  border: 3px solid #e4f3e8;
}

/* 名称 */
.expert-name {
  font-size: 16px;
  font-weight: 600;
  color: #2d7d4f;
  margin-bottom: 6px;
}

/* 专业方向 */
.expert-specialty {
  font-size: 14px;
  color: #555;
  margin-bottom: 6px;
}
</style>