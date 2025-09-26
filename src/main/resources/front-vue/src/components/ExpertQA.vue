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
  padding: 10px;
}

.qa-item {
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  background-color: #f9f9f9;
  margin-bottom: 10px;
}
</style>