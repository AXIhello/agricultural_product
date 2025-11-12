<template>
  <div class="pay-result">
    <div v-if="result === 'success'" class="success">
      <h2>支付成功！</h2>
      <p>订单号：{{ orderId }}</p>
      <p>金额：¥{{ amount }}</p>
      <router-link to="/orders" class="btn success-btn">返回订单列表</router-link>
    </div>

    <div v-else class="fail">
      <h2>支付失败</h2>
      <router-link to="/orders" class="btn fail-btn">返回订单</router-link>
    </div>
  </div>
</template>

<script setup>
import { useRoute } from 'vue-router'
const route = useRoute()

const result = route.query.result || 'fail'
const orderId = route.query.orderId || '未知'
const amount = route.query.amount || '0.00'
</script>

<style scoped>
.pay-result {
  text-align: center;
  margin-top: 80px;
}
.success h2 {
  color: #4caf50;
}
.fail h2 {
  color: #f44336;
}
.btn {
  display: inline-block;
  margin-top: 20px;
  padding: 10px 20px;
  border-radius: 6px;
  color: white;
  text-decoration: none;
}
.success-btn {
  background: #4caf50;
}
.fail-btn {
  background: #f44336;
}
</style>
