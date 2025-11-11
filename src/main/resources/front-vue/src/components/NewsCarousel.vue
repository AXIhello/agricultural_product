<template>
  <div class="news-carousel-container">
    <div
        class="news-carousel-slide"
        v-for="(news, index) in newsList"
        :key="index"
        :class="{ active: index === currentIndex }"
    >
      <!-- 点击图片跳转 -->
      <a :href="news.link" target="_blank" rel="noopener noreferrer">
        <img
            :src="news.imageUrl"
            alt="新闻图片"
            class="news-image"
        />
      </a>
      <div class="news-carousel-content">
        <h3 class="news-title">{{ news.title }}</h3>
        <p class="news-summary">{{ news.summary }}</p>
      </div>
    </div>

    <div class="news-carousel-controls">
      <button @click="prevSlide" class="control-btn">&lt;</button>
      <button @click="nextSlide" class="control-btn">&gt;</button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue';

import img1 from '../assets/img_1.png'
import img2 from '../assets/img_2.png'
import img3 from '../assets/img_3.png'

const newsList = [
  {
    title: '农业科技新突破：高效种植技术',
    summary: '最新研究表明，新型高效种植技术能显著提高农作物产量。',
    imageUrl: img1,
    link: 'https://www.stdaily.com/web/gdxw/2024-12/12/content_272543.html'
  },
  {
    title: '乡村振兴战略持续推进',
    summary: '乡村振兴战略深入实施，农民收入稳步增长。',
    imageUrl: img2,
    link: 'https://www.qstheory.cn/20250303/9d79971b90c84421843c79ce9026b894/c.html'
  },
  {
    title: '农产品市场分析报告发布',
    summary: '市场分析报告指出，今年农产品价格趋势分析。',
    imageUrl: img3,
    link: 'https://scs.moa.gov.cn/gzdt/202504/t20250424_6473502.htm'
  }
];

const currentIndex = ref(0);

const nextSlide = () => {
  currentIndex.value = (currentIndex.value + 1) % newsList.length;
};

const prevSlide = () => {
  currentIndex.value = (currentIndex.value - 1 + newsList.length) % newsList.length;
};

// 自动轮播
onMounted(() => {
  const intervalId = setInterval(() => {
    nextSlide();
  }, 5000);
  onUnmounted(() => clearInterval(intervalId));
});
</script>

<style scoped>
.news-carousel-container {
  position: relative;
  width: 100%;
  overflow: hidden;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.news-carousel-slide {
  display: none;
  padding: 10px;
  transition: opacity 0.5s ease-in-out;
}

.news-carousel-slide.active {
  display: block;
}

.news-image {
  width: 80%;
  height: 200px;
  object-fit: cover;
  border-radius: 6px;
  transition: transform 0.3s ease;
}

.news-image:hover {
  transform: scale(1.03); /* 鼠标悬停放大效果 */
}

.news-carousel-content {
  margin-top: 8px;
}

.news-title {
  margin: 5px 0;
  font-size: 16px;
  font-weight: 600;
}

.news-summary {
  font-size: 14px;
  color: #666;
  margin-bottom: 5px;
}

.news-carousel-controls {
  position: absolute;
  bottom: 10px;
  right: 10px;
  display: flex;
  gap: 5px;
}

.control-btn {
  background: #ddd;
  border: none;
  padding: 5px 10px;
  cursor: pointer;
  border-radius: 4px;
  transition: background-color 0.3s;
}

.control-btn:hover {
  background: #b7e4c7; /* 淡绿色悬停效果 */
}
</style>
