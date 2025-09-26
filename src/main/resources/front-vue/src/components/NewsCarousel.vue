<template>
  <div class="news-carousel-container">
    <div class="news-carousel-slide" v-for="(news, index) in newsList" :key="index" :class="{ active: index === currentIndex }">
      <img :src="news.imageUrl" alt="新闻图片" style="width: 100%; height: 150px; object-fit: cover;">
      <div class="news-carousel-content">
        <h3 style="margin: 5px 0; font-size: 16px;">{{ news.title }}</h3>
        <p style="font-size: 14px; color: #666; margin-bottom: 5px;">{{ news.summary }}</p>
      </div>
    </div>
    <div class="news-carousel-controls">
      <button @click="prevSlide" style="background: #ddd; border: none; padding: 5px 10px; cursor: pointer;">&lt;</button>
      <button @click="nextSlide" style="background: #ddd; border: none; padding: 5px 10px; cursor: pointer;">&gt;</button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';

const newsList = [
  {
    title: '农业科技新突破：高效种植技术',
    summary: '最新研究表明，新型高效种植技术能显著提高农作物产量。',
    imageUrl: 'https://via.placeholder.com/400x200/4CAF50/FFFFFF?text=新闻1',
  },
  {
    title: '乡村振兴战略持续推进',
    summary: '乡村振兴战略深入实施，农民收入稳步增长。',
    imageUrl: 'https://via.placeholder.com/400x200/FFC107/000000?text=新闻2',
  },
  {
    title: '农产品市场分析报告发布',
    summary: '市场分析报告指出，今年农产品价格趋势分析。',
    imageUrl: 'https://via.placeholder.com/400x200/9C27B0/FFFFFF?text=新闻3',
  },
];

const currentIndex = ref(0);

const nextSlide = () => {
  currentIndex.value = (currentIndex.value + 1) % newsList.length;
};

const prevSlide = () => {
  currentIndex.value = (currentIndex.value - 1 + newsList.length) % newsList.length;
};

// 自动轮播（可选）
onMounted(() => {
  const intervalId = setInterval(() => {
    nextSlide();
  }, 5000); // 5秒切换一次

  // 清除 interval, 在组件卸载时
  // onUnmounted(() => {  clearInterval(intervalId) })
});
</script>

<style scoped>
.news-carousel-container {
  position: relative;
  width: 100%;  /* 确保占据父容器的宽度 */
  overflow: hidden; /* 隐藏溢出内容 */
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.news-carousel-slide {
  display: none; /* 默认隐藏所有幻灯片 */
  padding: 10px;
  transition: opacity 0.5s ease-in-out;
}

.news-carousel-slide.active {
  display: block;  /* 显示当前幻灯片 */
}

.news-carousel-controls {
  position: absolute;
  bottom: 10px;
  right: 10px;
  display: flex;
  gap: 5px;
}
</style>