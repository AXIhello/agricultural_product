<template>
  <div class="order-confirm-page">
    <div class="card container">
      <h1 class="title">确认订单</h1>

      <!-- 收货地址 -->
      <section class="section address-section">
        <h2>收货地址</h2>
        <div v-if="addresses.length" class="addresses">
          <label
              v-for="addr in addresses"
              :key="addr.id"
              class="addr-item"
              :class="{ selected: selectedAddressId === addr.id }"
          >
            <input type="radio" :value="addr.id" v-model="selectedAddressId" />
            <div class="addr-info">
              <div class="addr-name">{{ addr.receiverName }} <span class="phone">{{ addr.phone }}</span></div>
              <div class="addr-line">{{ addr.fullAddress }}</div>
            </div>
          </label>
        </div>
        <div v-else class="empty">未找到地址，请先到个人中心添加收货地址。</div>
      </section>

      <!-- 订单明细 -->
      <section class="section items-section">
        <h2>订单明细</h2>
        <div v-if="cartItems.length" class="items-list">
          <div class="item-row" v-for="item in cartItems" :key="item.productId || item.id">
            <div class="left">
              <div class="name">{{ item.name }}</div>
              <div class="meta">单价：¥{{ toFixed2(item.price) }} /Kg</div>
            </div>
            <div class="right">
              <div class="qty">x{{ item.quantity }}</div>
              <div class="sub">¥{{ toFixed2(item.price * item.quantity) }}</div>
            </div>
          </div>
        </div>
        <div v-else class="empty">购物车为空</div>
      </section>

      <!-- 备注 -->
      <section class="section remark-section">
        <h2>备注</h2>
        <textarea v-model="remark" placeholder="需要卖家注意的事项（可选）" rows="3"></textarea>
      </section>

      <!-- 费用汇总 -->
      <section class="section summary">
        <div class="summary-row">
          <div>商品合计</div>
          <div class="value">¥{{ totalAmount }}</div>
        </div>
        <div class="summary-row">
          <div>运费</div>
          <div class="value">¥{{ toFixed2(shippingFee) }}</div>
        </div>
        <div class="summary-row total">
          <div>应付总额</div>
          <div class="value">¥{{ toFixed2(+totalAmount + +shippingFee) }}</div>
        </div>
      </section>

      <!-- 操作 -->
      <section class="section actions">
        <button class="btn btn-secondary" @click="goBack">返回购物车</button>
        <button class="btn btn-primary" :disabled="!canCheckout" @click="handleCheckout">
          确认并提交订单
        </button>
      </section>
    </div>

    <!-- 简单的成功提示浮层 -->
    <div v-if="successMessage" class="toast success">{{ successMessage }}</div>
    <div v-if="errorMessage" class="toast error">{{ errorMessage }}</div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import axios from 'axios'
import { useRouter } from 'vue-router'

const router = useRouter()

// 简单读取 userId（请根据实际项目替换）
function getUserId() {
  const u = localStorage.getItem('userId')
  return u ? Number(u) : 1
}

const userId = ref(getUserId())

const cartItems = ref([])
const addresses = ref([])
const selectedAddressId = ref(null)
const remark = ref('')
const shippingFee = ref(0.00) // 可根据规则动态计算

const successMessage = ref('')
const errorMessage = ref('')

// 获取购物车项（使用 /api/cart 接口）
async function fetchCartItems() {
  try {
    const res = await axios.get('/api/cart', { params: { userId: userId.value, pageNum: 1, pageSize: 100 } })
    // 兼容返回：Page<CartItem> (含 records) 或 直接数组
    cartItems.value = res.data.records || res.data || []
  } catch (err) {
    console.error('获取购物车失败', err)
    cartItems.value = []
  }
}

// 获取地址列表（示例接口 /api/addresses，若不同请替换）
async function fetchAddresses() {
  try {
    const res = await axios.get('/api/addresses', { params: { userId: userId.value } })
    addresses.value = res.data.records || res.data || []
    if (addresses.value.length) {
      selectedAddressId.value = addresses.value[0].id
    }
  } catch (err) {
    console.warn('获取地址失败（可能后端没有该接口）', err)
    addresses.value = []
  }
}

// 计算总价（保留两位）
function toFixed2(val) {
  // 以字符串方式按位处理避免 JS 浮点误差（逐位加法）
  const n = Number(val) || 0
  return n.toFixed(2)
}

// 总金额（商品小计）
const totalAmount = computed(() => {
  let cents = 0 // 以分为单位累加，避免浮点误差
  for (const it of cartItems.value) {
    const price = Math.round((Number(it.price) || 0) * 100) // 单价分
    const qty = Number(it.quantity) || 0
    cents += price * qty
  }
  // 转回元，保留两位
  return (cents / 100).toFixed(2)
})

// 是否可以下单
const canCheckout = computed(() => {
  return cartItems.value.length > 0 && selectedAddressId.value !== null
})

// 执行结算：调用 CartController.checkout -> 返回订单 id（整型）
async function handleCheckout() {
  if (!canCheckout.value) {
    errorMessage.value = '请确认已选择地址并且购物车不为空'
    setTimeout(() => (errorMessage.value = ''), 2500)
    return
  }

  try {
    // 构造productIds数组（后端 checkout 接口需要 productIds）
    const productIds = cartItems.value.map(i => Number(i.productId || i.id))

    const params = new URLSearchParams()
    params.append('userId', userId.value)
    params.append('addressId', selectedAddressId.value)
    // 后端 CartController.checkout 接受 List<Integer> productIds —— 我们以 productIds 多次传参
    // axios 可以通过 paramsSerializer 或直接将 productIds[]=...，这里使用 FormData 风格的请求体或URL参数。
    // 使用 POST + JSON body 更加直观（但根据你的后端实现可能要求 form params）。这里尝试 POST JSON。
    const payload = {
      userId: userId.value,
      addressId: selectedAddressId.value,
      productIds: productIds,
      remark: remark.value
    }

    // 调用后端结算接口
    const res = await axios.post('/api/cart/checkout', payload)

    // 后端返回订单 id（Integer）
    const orderId = res.data

    successMessage.value = '下单成功，订单号：' + orderId
    setTimeout(() => (successMessage.value = ''), 3000)

    // 清空购物车视图（前端清理）
    cartItems.value = []

    // 跳转到订单详情或订单列表页（若你有 /orders 路由）
    // 这里导航到 /orders 并附带刚创建的 orderId（可在订单列表或详情中展示）
    router.push({ path: '/orders', query: { created: orderId } })
  } catch (err) {
    console.error('结算失败', err)
    errorMessage.value = err.response?.data?.message || '下单失败，请稍后重试'
    setTimeout(() => (errorMessage.value = ''), 3000)
  }
}

function goBack() {
  router.back()
}

onMounted(async () => {
  await fetchCartItems()
  await fetchAddresses()
})
</script>

<style scoped>
.order-confirm-page {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: flex-start;
  padding: 40px 20px;
  background: #f0f9f4;
}

/* 容器卡片 */
.card.container {
  width: 100%;
  max-width: 900px;
  background: #ffffff;
  border-radius: 10px;
  padding: 28px;
  box-shadow: 0 8px 24px rgba(0,0,0,0.06);
}

/* 标题 */
.title {
  margin: 0 0 20px 0;
  font-size: 1.6rem;
  color: #2D7D4F;
}

/* 分区样式 */
.section {
  margin-bottom: 18px;
  border-top: 1px dashed #eee;
  padding-top: 14px;
}

.section h2 {
  margin: 0 0 10px 0;
  font-size: 1.05rem;
  color: #333;
}

/* 地址列表 */
.addresses {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.addr-item {
  display: flex;
  gap: 12px;
  align-items: center;
  padding: 10px;
  border-radius: 8px;
  border: 1px solid transparent;
  cursor: pointer;
}

.addr-item.selected {
  border-color: #cfeede;
  background: #f7fff9;
}

.addr-info .addr-name { font-weight: 600; }
.addr-info .phone { margin-left: 10px; font-weight: 500; color: #666; font-size: 0.95rem; }
.addr-info .addr-line { color: #666; font-size: 0.95rem; margin-top: 4px; }

/* items 列表 */
.items-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.item-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px;
  border-radius: 8px;
  background: #fbfefb;
  border: 1px solid #f0f7f1;
}

.item-row .left { display: flex; flex-direction: column; gap: 6px; }
.item-row .name { color: #2D7D4F; font-weight: 600; }
.item-row .meta { color: #666; font-size: 0.95rem; }
.item-row .right { display: flex; flex-direction: column; align-items: flex-end; gap: 6px; }

.qty { font-weight: 600; }
.sub { font-weight: 700; color: #333; }

/* 备注 */
.remark-section textarea {
  width: 100%;
  border-radius: 6px;
  border: 1px solid #ddd;
  padding: 10px;
  resize: vertical;
}

/* 汇总 */
.summary { padding: 12px; background: #fff; border-radius: 8px; border: 1px solid #f0f7f1; }
.summary-row { display:flex; justify-content: space-between; padding: 8px 0; color: #333; }
.summary-row.total { font-weight: 800; font-size: 1.1rem; border-top: 1px dashed #eee; margin-top: 8px; padding-top: 12px; }

/* 按钮 */
.actions { display: flex; gap: 12px; justify-content: flex-end; margin-top: 14px; }
.btn { padding: 10px 18px; border-radius: 8px; border: none; cursor: pointer; font-weight: 600; }
.btn-primary { background: #2D7D4F; color: #fff; }
.btn-secondary { background: #f3f3f3; color: #333; }

/* 提示弹层 */
.toast {
  position: fixed;
  left: 50%;
  transform: translateX(-50%);
  bottom: 24px;
  padding: 12px 16px;
  border-radius: 8px;
  box-shadow: 0 6px 20px rgba(0,0,0,0.12);
  color: #fff;
  z-index: 999;
}
.toast.success { background: #28a745; }
.toast.error { background: #e55353; }
</style>
