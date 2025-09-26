<template>
  <div class="finance-products-container">
    <div class="finance-product" v-for="product in products" :key="product.id">
      <h4 style="font-size: 16px; margin-bottom: 5px;">{{ product.name }}</h4>
      <p style="font-size: 14px; color: #666; margin-bottom: 5px;">{{ product.description }}</p>
      <p style="font-weight: bold; font-size: 14px;">利率：{{ product.interestRate }}</p>
      <button style="background: #2D7D4F; color: white; border: none; padding: 5px 10px; cursor: pointer; border-radius: 4px;">申请</button>
    </div>
  </div>
</template>

<script setup>
const products = [
  {
    id: 1,
    name: '农业贷款',
    description: '为农业生产提供资金支持',
    interestRate: '5%',
  },
  {
    id: 2,
    name: '农机设备融资',
    description: '用于购买农机设备的融资方案',
    interestRate: '6%',
  },
  {
    id: 3,
    name: '农产品保险',
    description: '保障农产品免受自然灾害影响',
    interestRate: '3%',
  },
];
</script>

<style scoped>
.finance-products-container {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
}
.finance-product {
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  background-color: #f9f9f9;
}
</style>