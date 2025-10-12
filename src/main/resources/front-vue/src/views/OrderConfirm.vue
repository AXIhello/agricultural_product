<template>
  <div class="order-container">
    <h2 style="  text-align: center; /* 标题居中 */
        margin-bottom: 20px;
        ">订单结算</h2>

    <div v-if="order && address">
      <!-- 订单信息 -->
      <div class="order-info">
        <p><strong>订单编号：</strong>{{ order.orderId }}</p>
        <p><strong>下单时间：</strong>{{ formatTime(order.createTime || order.orderDate) }}</p>
        <p><strong>收货人：</strong>{{ address.recipientName }}（{{ address.phoneNumber }}）</p>
        <p><strong>收货地址：</strong>{{ fullAddress }}</p>
      </div>

      <!-- 商品列表 -->
      <div class="order-items">
        <h3>商品清单</h3>
        <div
            v-for="item in orderItems"
            :key="item.productId"
            class="order-item-card"
        >
          <img :src="item.imageUrl || defaultImage" alt="商品图片" />
          <div class="item-info">
            <h4>{{ item.productName }}</h4>
            <p>单价：¥{{ (item.price).toFixed(2) }}</p>
            <p>数量：{{ item.quantity }}</p>
          </div>
          <div class="item-total">
            <p>小计：¥{{ (item.price * item.quantity).toFixed(2) }}</p>
          </div>
        </div>
      </div>

      <!-- 总价与支付 -->
      <div class="order-summary">
        <h3>总价：¥{{ totalPrice }}</h3>
        <button class="pay-btn" @click="payOrder">立即支付</button>
      </div>
    </div>

    <p v-else class="loading">正在加载订单信息...</p>
  </div>
</template>

<script setup>
import {computed, onMounted, ref} from 'vue'
import {useRoute} from 'vue-router'
import axios from '../utils/axios'
import router from "@/router/index.js";
import defaultImage from '../assets/img.png'


const route = useRoute()
const order = ref(null)
const orderItems = ref([])
const address = ref(null)
const token = localStorage.getItem('token')

// 获取订单及地址信息
onMounted(async () => {
  const orderId = route.params.orderId
  if (!orderId) return

  try {
    // ① 获取订单详情
    const res = await axios.get(`/orders/${orderId}`, {
      headers: { Authorization: token }
    })
    const data = res.data
    order.value = data.order
    orderItems.value = data.orderItems || []

    console.log("订单详情数据: ", data)
    // ② 根据订单中的地址ID获取收货地址
    if (order.value?.shippingAddressId) {
      const addrRes = await axios.get(`/address/${order.value.shippingAddressId}`, {
        headers: { Authorization: token }
      })
      address.value = addrRes.data
      console.log("订单地址数据: ", addrRes.data)
    }else{
      console.log("订单没有地址id")
    }

    // ③ 根据 orderItems 的 productId 获取商品信息
    const items = data.orderItems || []
    orderItems.value = await Promise.all(items.map(async item => {
      const productRes = await axios.get(`/products/${item.productId}`)
      const product = productRes.data
      return {
        ...item,
        productName: product.productName,
        //imageUrl: product.imageUrl,
        quantity: item.quantity,
        price: item.unitPrice
      }
    }))

  } catch (err) {
    console.error('获取订单失败:', err)
  }
})

// 拼接完整地址
const fullAddress = computed(() => {
  if (!address.value) return ''
  const { province, city, district, streetAddress } = address.value
  return `${province}${city}${district}${streetAddress}`
})

// 计算总价
const totalPrice = computed(() => {
  if (!orderItems.value) return 0
  return orderItems.value.reduce(
      (sum, item) => sum + item.price * item.quantity,
      0
  ).toFixed(2)
})

// 时间格式化
const formatTime = (t) => (t ? new Date(t).toLocaleString() : '')

// 模拟支付
function payOrder() {
  alert('模拟支付成功！感谢您的购买')

  // 调用后端清空购物车接口
  axios.delete('/cart/clear')
      .then(res => {
        if (res.data) {
          console.log('购物车已清空')
        } else {
          console.warn('购物车清空失败')
        }
      })
      .catch(err => {
        console.error('清空购物车失败:', err)
      })
      .finally(() => {
        // 支付完成后跳转主页
        router.push('/main')
      })
}
</script>

<style scoped>
.order-container {
  width: 1800px;
  max-width: 2400px;
  margin: 30px auto;
  background: #fff;
  padding: 30px;
  border-radius: 16px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  text-align: left;
}

.order-info p {
  margin: 6px 0;
  font-size: 15px;
}

.order-items {
  margin-top: 20px;
}

.order-item-card {
  display: flex;
  align-items: center;
  background: #fafafa;
  padding: 15px;
  border-radius: 12px;
  margin-bottom: 12px;
}

.order-item-card img {
  width: 80px;
  height: 80px;
  border-radius: 10px;
  margin-right: 15px;
  object-fit: cover;
}

.item-info {
  flex: 1;
}

.item-total {
  font-weight: bold;
  color: #e67e22;
}

.order-summary {
  margin-top: 30px;
  text-align: right;
}

.pay-btn {
  background-color: #28a745;
  color: white;
  padding: 10px 22px;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  cursor: pointer;
}

.pay-btn:hover {
  background-color: #218838;
}

.loading {
  text-align: center;
  color: #888;
  margin-top: 60px;
  font-size: 16px;
}
</style>
