<template>
  <div class="ecommerce-container">
    <div class="product" v-for="product in products" :key="product.id">
      <img :src="product.imageUrl" alt="农产品图片" style="width: 100%; height: 150px; object-fit: cover; margin-bottom: 10px;">
      <h4 style="font-size: 16px; margin-bottom: 5px;">{{ product.name }}</h4>
      <p style="font-size: 14px; color: #666; margin-bottom: 5px;">{{ product.description }}</p>
      <p style="font-weight: bold; font-size: 14px;">价格：{{ product.price }}</p>
      <button style="background: #2D7D4F; color: white; border: none; padding: 5px 10px; cursor: pointer; border-radius: 4px;">购买</button>
    </div>
  </div>
</template>

<script setup>
const products = [
  {
    id: 1,
    name: '有机苹果',
    description: '新鲜采摘，口感香甜',
    price: '¥30/斤',
    imageUrl: 'https://via.placeholder.com/200x150/FF5722/FFFFFF?text=苹果',
  },
  {
    id: 2,
    name: '东北大米',
    description: '精选优质稻米',
    price: '¥8/斤',
    imageUrl: 'https://via.placeholder.com/200x150/4CAF50/FFFFFF?text=大米',
  },
  {
    id: 3,
    name: '绿色蔬菜',
    description: '无公害种植',
    price: '¥5/份',
    imageUrl: 'https://via.placeholder.com/200x150/8BC34A/FFFFFF?text=蔬菜',
  },
];
</script>

<style scoped>
.ecommerce-container {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
}
.product {
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  background-color: #f9f9f9;
}
</style>