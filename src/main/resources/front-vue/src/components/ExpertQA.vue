<template>
  <div class="expert-qa-container">
    <div class="qa-item" v-for="qa in qaList" :key="qa.id">
      <h4 style="font-size: 16px; margin-bottom: 5px;">问题：{{ qa.question }}</h4>
      <p style="font-size: 14px; color: #666; margin-bottom: 5px;">回答：{{ qa.answer }}</p>
    </div>
  </div>
</template>

<script setup>
const qaList = [
  {
    id: 1,
    question: '如何防治水稻病虫害？',
    answer: '建议采取综合防治措施，包括选择抗病品种、合理施肥、化学防治等。',
  },
  {
    id: 2,
    question: '如何提高蔬菜产量？',
    answer: '合理施肥、科学灌溉、病虫害防治是提高蔬菜产量的关键。',
  },
];
</script>

<style scoped>
.expert-qa-container {
  padding: 18px;
}

/* 单条专家问答卡片 */
.qa-item {
  padding: 14px 18px;
  background-color: #f8fbf8;
  border: 1px solid #d9e8df;
  border-radius: 12px;

  margin-bottom: 14px;
  box-shadow: 0 3px 8px rgba(0, 0, 0, 0.06);

  transition: all 0.25s ease;
  cursor: pointer;
}

/* 鼠标悬停效果 */
.qa-item:hover {
  transform: translateY(-4px);
  border-color: #8bc3a1;
  box-shadow: 0 6px 14px rgba(0, 0, 0, 0.10);
}

/* 问题 */
.qa-question {
  font-size: 16px;
  font-weight: 600;
  color: #2d7d4f;
  margin-bottom: 6px;
}

/* 回答 */
.qa-answer {
  font-size: 14px;
  line-height: 1.5;
  color: #444;
}

</style>