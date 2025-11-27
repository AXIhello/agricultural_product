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
  border-radius: 10px;
  background-color: #f9f9f9;
  padding: 15px;
  border: 1px solid #e0e0e0;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.06);
}

.news-carousel-slide {
  display: none;
  padding: 10px;
  transition: opacity 0.4s ease-in-out;
}

.news-carousel-slide.active {
  display: block;
}

.news-image {
  width: 100%;
  height: 220px;
  object-fit: cover;
  border-radius: 8px;
  transition: transform 0.25s ease;
}

.news-image:hover {
  transform: scale(1.03);
}

.news-carousel-content {
  margin-top: 10px;
}

.news-title {
  margin: 8px 0 6px 0;
  font-size: 18px;
  font-weight: bold;
  color: #2d7d4f; /* 深绿标题（与你首页一致） */
}

.news-summary {
  font-size: 15px;
  color: #555;
  line-height: 1.45;
  margin-bottom: 10px;
}

.news-carousel-controls {
  position: absolute;
  bottom: 12px;
  right: 12px;
  display: flex;
  gap: 6px;
}

.control-btn {
  background-color: #d7f0e3; /* 淡绿按钮 */
  border: 1px solid #b8d9c6;
  padding: 6px 12px;
  cursor: pointer;
  border-radius: 6px;
  font-size: 13px;
  transition: all 0.25s ease;
}

.control-btn:hover {
  background-color: #b7e4c7; /* 悬停更明显 */
  border-color: #8bc3a1;
}
</style>

