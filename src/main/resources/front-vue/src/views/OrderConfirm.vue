<template>
  <div class="order-container">
    <div class="order-main">
      <!-- 左边滚动内容 -->
      <div class="order-left">
        <!-- 地址选择 -->
        <div class="card address-section">
          <h3 class="section-title">确认收货地址</h3>
          <div v-if="address" class="address-info">
            <p><span class="label">收货人：</span>{{ address.recipientName }}（{{ address.phoneNumber }}）</p>
            <p><span class="label">收货地址：</span>{{ fullAddress }}</p>
            <el-select
                v-model="selectedAddressId"
                placeholder="切换地址"
                @change="selectAddress"
                size="medium"
                class="address-select"
            >
              <el-option
                  v-for="addr in userAddresses"
                  :key="addr.addressId"
                  :label="`${addr.province}${addr.city}${addr.district}${addr.streetAddress}`"
                  :value="addr.addressId"
              />
            </el-select>
          </div>
          <div v-else class="no-address">
            <p>无地址，请先添加收货地址</p>
            <el-button type="primary" size="medium" @click="goToAddressManage">
              添加/选择地址
            </el-button>
          </div>
        </div>

        <!-- 商品确认信息 -->
        <div class="card items-section">
          <h3 class="section-title">确认订单信息</h3>

          <p v-if="orderItems.length === 0 || !address" class="loading">正在加载订单信息...</p>

          <div v-else class="order-table">
            <!-- 表头 -->
            <div class="order-table-header">
              <div class="col col-product">商品</div>
              <div class="col col-spec">规格</div>
              <div class="col col-quantity">数量</div>
              <div class="col col-price">单价</div>
            </div>

            <!-- 商品行 -->
            <div v-for="item in orderItems" :key="item.productId" class="order-table-row">
              <div class="col col-product">
                <img :src="item.imageUrl || defaultImage" alt="商品图片" />
                <span>{{ item.productName }}</span>
              </div>
              <div class="col col-spec">{{ item.specInfo }}</div>
              <div class="col col-quantity">
                <button @click="changeQuantity(item, -1)">-</button>
                <input type="number" v-model.number="item.quantity" @input="inputQuantity(item, $event)" :min="1" :max="item.stock" />
                <button @click="changeQuantity(item, 1)">+</button>
              </div>
              <div class="col col-price">¥{{ item.price.toFixed(2) }}</div>
            </div>
          </div>
        </div>


      </div>

      <!-- 右边固定付款详情 -->
      <div class="order-right card">
        <h3 class="section-title">付款详情</h3>
        <div class="price-detail">
          <div><span class="label">商品总价：</span><span class="price">¥{{ originalPrice }}</span></div>
          <div class="discount-text">
            优惠券优惠：<span v-if="couponDiscount > 0" class="price">-¥{{ couponDiscount?.toFixed(2) }}</span><p v-else>暂无</p>
          </div>

        </div>
        <div class="payment-method">
          <p><span class="label">支付方式：</span>在线支付</p>
        </div>
        <div class="final-price">
        实付金额：<span class="price">¥{{ totalAmount?.toFixed(2) }}</span>
        </div>
        <div class="btn-group">
          <button class="back-btn" @click="goToTrading">返回</button>
          <button class="pay-btn" @click="confirmOrder">结算</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.order-container {
  width: 100%;
  min-height: 100vh;
  padding: 30px;
  box-sizing: border-box;
  background-color: #dff0d8;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial;
}

.order-main {
  display: grid;
  grid-template-columns: 5fr 2fr;
  gap: 30px;
  width: 100%;
}

/* 左边滚动区域 */
.order-left {
  display: flex;
  flex-direction: column;
  gap: 30px;
  max-height: calc(100vh - 60px);
  overflow-y: auto;
  padding-right: 10px;
}

/* 卡片样式 */
.card {
  background: #fff;
  border-radius: 12px;
  padding: 25px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.08);
}

.section-title {
  font-size: 20px;
  font-weight: 600;
  margin-bottom: 20px;
  border-bottom: 1px solid #e8e8e8;
  padding-bottom: 10px;
}

/* 地址部分 */
.address-info p, .no-address p {
  margin: 8px 0;
  font-size: 15px;
}

.label {
  display: inline-block;
  width: 80px;
  font-weight: 500;
  color: #333;
}

.address-select {
  width: 100%;
  margin-top: 12px;
}

/* 商品信息 */
.order-item-card {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
  background: #fafafa;
  padding: 15px;
  border-radius: 10px;
  align-items: center;
}

.order-item-card img {
  width: 100px;
  height: 100px;
  object-fit: cover;
  border-radius: 10px;
}

.item-info {
  flex: 1;
}

.item-info h4 {
  font-size: 16px;
  font-weight: 500;
  margin-bottom: 5px;
}

.item-info p {
  margin: 3px 0;
  font-size: 14px;
  color: #555;
}

.item-total {
  font-weight: 600;
  font-size: 15px;
  text-align: right;
}

/* 右边固定付款 */
.order-right {
  position: sticky;
  top: 30px;
  max-height: calc(100vh - 60px);
  justify-content: flex-end;
  display: flex;
  flex-direction: column;
}

.price-detail div {
  margin-bottom: 12px;
  font-size: 15px;
}

.discount-text {
  display: flex;

}
.discount-text p{
  color: #e74c3c;
}

.final-price {
  margin-top: auto;
  margin-bottom: 5px;
  font-size: 20px;
  font-weight: 700;
}

.price {
  font-weight: 700;
  color: #e60012;
}

.payment-method p {
  font-size: 15px;
  margin: 8px 0;
}
.btn-group {
  display: flex;
  gap: 15px;
}

/* 返回按钮 */
.back-btn {
  flex: 2; /* 占 2/7 */
  background-color: #fff;
  color: #7f8c8d; /* 灰色文字 */
  border: 1px solid #bdc3c7;
  padding: 14px 0;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.back-btn:hover {
  background-color: #f0f0f0;
}

/* 支付按钮 */
.pay-btn {
  flex: 5; /* 占 5/7 */
  background-color: #27ae60;
  color: #fff;
  border: none;
  padding: 14px 0;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.pay-btn:hover {
  background-color: #2ecc71;
}


.loading {
  color: #999;
  text-align: center;
}
.order-table {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.order-table-header {
  display: flex;
  padding: 10px 12px;
  border-radius: 6px;
  font-weight: 600;
  color: #1b5e20;
}

.order-table-header .col {
  flex: 1;
  text-align: center;
}

.order-table-header .col-product {
  flex: 2;
  text-align: left;
}

.order-table-header .col-spec {
  flex: 1.5;
  text-align: left;
}

.order-table-row {
  display: flex;
  align-items: center;
  padding: 12px;
  border-radius: 8px;
}

.order-table-row .col {
  flex: 1;
  text-align: center;
}

.order-table-row .col-product {
  flex: 2;
  display: flex;
  align-items: center;
  gap: 10px;
  text-align: left;
}

.order-table-row .col-product img {
  width: 60px;
  height: 60px;
  object-fit: cover;
  border-radius: 6px;
}

.order-table-row .col-spec {
  flex: 1.5;
  text-align: left;
  color: #2e7d32;
}

.order-table-row .col-quantity {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 5px;
}

.order-table-row .col-quantity button {
  width: 28px;
  height: 28px;
  border: none;
  border-radius: 4px;
  background-color: #4caf50;
  color: #fff;
  cursor: pointer;
  font-weight: bold;
}

.order-table-row .col-quantity button:hover {
  background-color: #388e3c;
}

.order-table-row .col-quantity input {
  width: 50px;
  height: 28px;
  text-align: center;
  border: 1px solid #ccc;
  border-radius: 4px;
}

.order-table-row .col-price {
  font-weight: 600;
  color: #2e7d32;
}

.price-detail > div,
.payment-method > p,
.final-price {
  display: flex;
  justify-content: space-between; /* 左右分开 */
  align-items: center;
}

</style>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import axios from '../utils/axios'
import defaultImage from '../assets/img.png'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/authStore'
import { storeToRefs } from 'pinia'

// -------------------- 状态 --------------------
const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const { isLoggedIn } = storeToRefs(authStore)

const orderItems = ref([])
const address = ref(null)
const selectedAddressId = ref(null)
const userAddresses = ref([])

const couponDiscount = ref(0) // 可拓展
const totalAmount = ref(0)

// -------------------- 初始化数据 --------------------
onMounted(async () => {
  if (!isLoggedIn.value) {
    ElMessage.error('请先登录')
    router.push('/login')
    return
  }

  // 获取预览订单（buyNow 或 购物车传递的参数）
  const preview = route.state?.orderPreview || JSON.parse(localStorage.getItem('orderPreview'))
  if (!preview) {
    ElMessage.error('未找到订单信息')
    router.push('/trading')
    return
  }

  // 地址处理
  try {
    if (preview.addressId) {
      const addrRes = await axios.get(`/address/${preview.addressId}`)
      address.value = addrRes.data
      selectedAddressId.value = addrRes.data.addressId
    } else {
      const defaultRes = await axios.get('/address/default')
      if (defaultRes.status === 200 && defaultRes.data) {
        address.value = defaultRes.data
        selectedAddressId.value = defaultRes.data.addressId
      } else {
        address.value = null
      }
    }

    // 获取用户所有地址，用于切换
    const userAddrRes = await axios.get('/address/user')
    userAddresses.value = userAddrRes.data || []
  } catch (err) {
    console.warn('地址加载失败', err)
  }

  // 商品信息（加载每个商品详情和图片）
  orderItems.value = await Promise.all(
      preview.orderItems.map(async item => {
        try {
          const productRes = await axios.get(`/products/${item.productId}`)
          const product = productRes.data

          // 加载图片（参考 loadAllProducts）
          let imageUrl = defaultImage
          try {
            const imageRes = await axios.get(`/products/${product.productId}/image`, {
              responseType: 'blob'
            })
            if (imageRes.data.size > 0) {
              imageUrl = URL.createObjectURL(imageRes.data)
            }
          } catch {
            imageUrl = product.imageUrl || defaultImage
          }

          return {
            ...item,
            productName: product.productName,
            price: product.price,
            unitInfo: product.unitInfo,
            stock: product.stock,
            imageUrl
          }
        } catch (err) {
          console.error('商品加载失败', item.productId, err)
          return { ...item, productName: '未知商品', imageUrl: defaultImage, price: 0, unitInfo: '', stock: 0 }
        }
      })
  )

  recalcTotal()
})

// -------------------- 计算属性 --------------------
const fullAddress = computed(() => {
  if (!address.value) return ''
  const { province, city, district, streetAddress } = address.value
  return `${province}${city}${district}${streetAddress}`
})

const originalPrice = computed(() =>
    orderItems.value.reduce((sum, i) => sum + i.price * i.quantity, 0).toFixed(2)
)

// -------------------- 方法 --------------------

// 切换地址
const selectAddress = async (addrId) => {
  try {
    const res = await axios.get(`/address/${addrId}`)
    if (res.data) {
      address.value = res.data
      selectedAddressId.value = addrId
      ElMessage.success('已切换收货地址')
    }
  } catch (err) {
    console.error('切换地址失败', err)
    ElMessage.error('切换地址失败，请稍后重试')
  }
}

// 跳转页面
const goToAddressManage = () => router.push('/profile')
const goToTrading = () => router.push('/trading')

// 修改数量
const changeQuantity = (item, delta) => {
  let newQty = item.quantity + delta;
  if (newQty < 1) newQty = 1;
  if (newQty > item.stock) newQty = item.stock;
  item.quantity = newQty;
  recalcTotal();
}

// 输入框修改数量
const inputQuantity = (item, event) => {
  let val = parseInt(event.target.value);
  if (isNaN(val) || val < 1) val = 1;
  if (val > item.stock) val = item.stock;
  item.quantity = val;
  recalcTotal();
}

// 重新计算总金额
const recalcTotal = () => {
  const sum = orderItems.value.reduce((acc, i) => acc + i.price * i.quantity, 0)
  totalAmount.value = sum - couponDiscount.value
  console.log('总价：',totalAmount.value,sum.toString(),couponDiscount.value)
}

// 点击确认订单 -> 真正创建订单
const confirmOrder = async () => {
  if (!address.value) {
    ElMessage.error('请先选择收货地址');
    return;
  }

  const reqBody = {
    addressId: selectedAddressId.value,
    orderItems: orderItems.value.map(i => ({
      productId: i.productId,
      quantity: i.quantity
    })),
    // userCouponId: 123 // 如果有优惠券，可以在这里传
  };

  try {
    // 1. 创建订单
    const res = await axios.post('/orders', reqBody);
    const orderId = res.data;

    // 2. 清理本地预览订单
    localStorage.removeItem('orderPreview');

    // 3. 调用支付宝支付接口
    const payRes = await axios.post(`/alipay/pay/${orderId}`, {}, { responseType: 'text' });
    if (!payRes.data) {
      ElMessage.error('支付接口返回为空');
      return;
    }

    // 打开一个新窗口支付
    const payWindow = window.open('', '_blank');
    payWindow.document.write(payRes.data);
    payWindow.document.close();

    ElMessage.success('订单创建成功，支付页面已打开，请完成支付');

  } catch (err) {
    console.error('创建订单或支付失败', err);
    ElMessage.error('订单创建或支付失败，请稍后重试');
  }
};

</script>





