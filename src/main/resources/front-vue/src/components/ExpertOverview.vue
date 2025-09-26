<template>
  <div class="expert-overview-container">
    <div class="expert" v-for="expert in experts" :key="expert.id">
      <img :src="expert.avatar" alt="专家头像" style="width: 80px; height: 80px; border-radius: 50%; margin-bottom: 10px;">
      <h4 style="font-size: 16px; margin-bottom: 5px;">{{ expert.name }}</h4>
      <p style="font-size: 14px; color: #666;">{{ expert.title }}</p>
      <p style="font-size: 14px;">擅长领域：{{ expert.specialty }}</p>
    </div>
  </div>
</template>

<script setup>
const experts = [
  {
    id: 1,
    name: '李专家',
    title: '农业技术专家',
    specialty: '种植、病虫害防治',
    avatar: 'https://via.placeholder.com/80/007BFF/FFFFFF?text=专家1',
  },
  {
    id: 2,
    name: '王教授',
    title: '农业经济学教授',
    specialty: '农业政策、市场分析',
    avatar: 'https://via.placeholder.com/80/28A745/FFFFFF?text=专家2',
  },
  {
    id: 3,
    name: '张博士',
    title: '农业科技研发员',
    specialty: '新品种培育、生物技术',
    avatar: 'https://via.placeholder.com/80/DC3545/FFFFFF?text=专家3',
  },
];
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