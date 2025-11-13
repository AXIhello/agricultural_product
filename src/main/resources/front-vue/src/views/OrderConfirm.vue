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
            <p>单价：¥{{ item.price?.toFixed(2) ?? '0.00' }}/{{ item.unitInfo }}</p>
            <p>数量：{{ item.quantity }}</p>
          </div>
          <div class="item-total">
            <p>小计：¥{{ (item.price * item.quantity).toFixed(2) }}</p>
          </div>
        </div>
      </div>

      <!-- 总价与支付 -->

      <!-- 总价与支付 -->
      <div class="order-summary">
        <div class="order-info">
          <div class="price">总价：¥{{ totalPrice }}</div>

          <div class="status" v-if="order?.status === 'completed'">
            已支付
          </div>
        </div>

        <div class="btn-group">
          <button
              v-if="order?.status === 'pending'"
              class="pay-btn"
              @click="payOrder"
          >
            立即支付
          </button>

          <button class="back-btn" @click="goToTrading">
            返回交易页
          </button>
        </div>
      </div>

    </div>

    <p v-else class="loading">正在加载订单信息...</p>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import axios from '../utils/axios'
import defaultImage from '../assets/img.png'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/authStore'
import { storeToRefs } from 'pinia'

const route = useRoute()
const router = useRouter();
const order = ref(null)
const orderItems = ref([])
const address = ref(null)

const authStore = useAuthStore();
const { isLoggedIn } = storeToRefs(authStore);

// 获取订单及地址信息
onMounted(async () => {

  if (!isLoggedIn.value) {
    ElMessage.error('请先登录再查看订单！');
    router.push('/login');
    return;
  }

  const orderId = route.params.orderId
  if (!orderId) return

  try {
    // ① 获取订单详情
    const res = await axios.get(`/orders/${orderId}`)
    const data = res.data
    order.value = data.order
    orderItems.value = data.orderItems || []
    console.log("订单详情数据:", data)

    // ② 获取地址
    if (order.value?.shippingAddressId) {
      const addrRes = await axios.get(`/address/${order.value.shippingAddressId}`)
      address.value = addrRes.data
      console.log("订单地址数据:", addrRes.data)
    } else {
      console.log("订单没有地址id")
    }

    // ③ 获取商品详情（包括库存）
    const items = data.orderItems || []
    orderItems.value = await Promise.all(
        items.map(async (item) => {
          const productRes = await axios.get(`/products/${item.productId}`)
          const product = productRes.data
          return {
            ...item,
            productName: product.productName,
            imageUrl: product.imageUrl || defaultImage,
            stock: product.stock, // 加入库存信息
            price: product.price,
            unitInfo:product.unitInfo
          }
        })
    )
  } catch (err) {
    console.error('获取订单失败:', err)
    ElMessage.error('加载订单失败，请稍后重试')
  }
})

// 拼接完整地址
const fullAddress = computed(() => {
  if (!address.value) return ''
  const { province, city, district, streetAddress } = address.value
  return `${province}${city}${district}${streetAddress}`
})

// 计算总价
const totalPrice = computed(() =>
    orderItems.value.reduce((sum, item) => sum + item.price * item.quantity, 0).toFixed(2)
)

// 时间格式化
const formatTime = (t) => (t ? new Date(t).toLocaleString() : '')

// 支付逻辑
async function payOrder() {
  const orderId = route.params.orderId
  if (!orderId) {
    ElMessage.error('未找到订单号')
    return
  }

  try {
    const res = await axios.post(`/alipay/pay/${orderId}`, {}, {
      responseType: 'text'
    })

    if (!res.data) {
      ElMessage.error('支付接口返回为空')
      return
    }

    // 打开一个新的窗口并写入返回的 HTML
    const payWindow = window.open('', '_blank')
    payWindow.document.write(res.data)
    payWindow.document.close()

    ElMessage.success('支付页面已打开，请在支付宝中完成付款')

    // 等待支付后手动刷新或监听回调（可选）
  } catch (err) {
    console.error('支付失败:', err)
    ElMessage.error('支付失败，请稍后重试')
  }
}

function goToTrading() {
  router.push('/trading')
}

</script>


<style scoped>
.order-container {
  width: 1000px;
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

/*按钮部分*/

.order-summary {
  width: 100%;
  margin-left: auto;
  display: flex;
  flex-direction: column;
  gap: 20px;
  border-top: 1px solid #4caf50; /* ✅ 绿色线条 */
  padding-top: 20px; /* 给线条和内容留一点空间 */
}

.price {
  font-size: 20px;
  font-weight: bold;
}

.status {
  font-size: 22px;
  color: #4caf50; /* 绿色 */
  font-weight: bold;
}

.btn-group {
  display: flex;
  justify-content: flex-end;
  gap: 15px;
}

.pay-btn {
  background-color: #4caf50;
  color: white;
  padding: 10px 25px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  transition: 0.3s;
}
.pay-btn:hover {
  background-color: #45a049;
}

.back-btn {
  background-color: #4CAF50;
  color: white;
  padding: 10px 25px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  transition: 0.3s;
}
.back-btn:hover {
  background-color: #52b788;
}
</style>
