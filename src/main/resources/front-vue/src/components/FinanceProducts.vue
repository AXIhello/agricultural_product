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
  grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
  gap: 22px;
  padding: 10px 0;
}

/* 单个理财产品卡片 */
.finance-product {
  background-color: #f8fbf8;
  padding: 16px 18px;
  border: 1px solid #d9e8df;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.25s ease;
  box-shadow: 0 3px 8px rgba(0, 0, 0, 0.06);
}

/* 悬停交互效果 */
.finance-product:hover {
  transform: translateY(-4px);
  border-color: #8bc3a1;
  box-shadow: 0 6px 14px rgba(0, 0, 0, 0.10);
}

/* 产品标题 */
.finance-product h4 {
  font-size: 18px;
  font-weight: 700;
  color: #2d7d4f;
  margin-bottom: 8px;
}

/* 简介文案 */
.finance-product p {
  font-size: 14px;
  color: #555;
  line-height: 1.45;
}
</style>