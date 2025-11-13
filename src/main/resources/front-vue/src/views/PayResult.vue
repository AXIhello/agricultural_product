<template>
  <div class="pay-result">
    <div v-if="result === 'success'" class="result-card success">
      <div class="icon">✔</div>
      <h2>支付成功</h2>
      <p class="info">订单号：{{ orderId }}</p>
      <p class="info">支付金额：¥{{ amount }}</p>
      <router-link :to="`/orders/${orderId}`" class="link">返回订单详情</router-link>
    </div>

    <div v-else class="result-card fail">
      <div class="icon">✖</div>
      <h2>支付失败</h2>
      <p class="info">请检查订单状态或稍后重试</p>
      <router-link :to="`/orders/${orderId}`" class="link fail-link">返回订单</router-link>
    </div>
  </div>
</template>
<script setup>
import { useRoute } from 'vue-router'
const route = useRoute()

const result = route.query.result || 'fail'
const orderId = Number(route.query.orderId) || 0
const amount = route.query.amount || '0.00'
</script>

<style scoped>
.pay-result {

  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 80vh;
  font-family: "Segoe UI", Arial, sans-serif;
}

.result-card {
  width: 360px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  text-align: center;
  padding: 40px 30px;
  transition: all 0.3s ease;
}

.result-card:hover {
  transform: translateY(-3px);
}

.icon {
  font-size: 60px;
  margin-bottom: 20px;
}

.success .icon {
  color: #4caf50;
}

.fail .icon {
  color: #f44336;
}

h2 {
  font-size: 22px;
  margin-bottom: 10px;
  color: #333;
}

.info {
  color: #666;
  margin: 6px 0;
  font-size: 15px;
}

.link {
  display: inline-block;
  margin-top: 25px;
  padding: 10px 22px;
  background: #4caf50;
  color: white;
  border-radius: 6px;
  text-decoration: none;
  transition: background 0.2s ease;
}

.link:hover {
  background: #43a047;
}

.fail-link {
  background: #f44336;
}

.fail-link:hover {
  background: #e53935;
}
</style>
