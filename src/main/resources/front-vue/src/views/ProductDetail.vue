<template>
  <div class="product-detail">
    <div class="product-container">
      <div class="product-image">
        <img src="https://via.placeholder.com/400x400" alt="商品图片">
      </div>
      
      <div class="product-info">
        <h1>{{ product.productName }}</h1>
        
        <div class="price-section">
          <span class="price">¥{{ product.price }}</span>
          <span class="stock">库存: {{ product.stock }}</span>
        </div>

        <div class="description">
          <h3>商品描述</h3>
          <p>{{ product.description }}</p>
        </div>

        <div class="farmer-info">
          <h3>农户信息</h3>
          <p>农户ID: {{ product.farmerId }}</p>
        </div>

        <div class="action-section">
          <el-input-number 
            v-model="quantity" 
            :min="1" 
            :max="product.stock"
            size="large"
          />
          <el-button type="primary" size="large" @click="addToCart">加入购物车</el-button>
          <el-button type="danger" size="large" @click="buyNow">立即购买</el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import axios from '../utils/axios'

const route = useRoute()
const router = useRouter()
const product = ref({})
const quantity = ref(1)

// 获取商品详情
const loadProductDetail = async () => {
  try {
    const response = await axios.get(`/api/products/${route.params.id}`)
    product.value = response.data
  } catch (error) {
    console.error('获取商品详情失败:', error)
    ElMessage.error('获取商品详情失败')
  }
}

// 加入购物车
const addToCart = async () => {
  try {
    await axios.post('/api/cart/add', {
      productId: product.value.productId,
      quantity: quantity.value
    })
    ElMessage.success('成功加入购物车')
  } catch (error) {
    console.error('加入购物车失败:', error)
    ElMessage.error('加入购物车失败')
  }
}

// 立即购买
const buyNow = () => {
  // 跳转到订单确认页面，携带商品和数量信息
  router.push({
    path: '/order/confirm',
    query: {
      productId: product.value.productId,
      quantity: quantity.value
    }
  })
}

onMounted(() => {
  loadProductDetail()
})
</script>

<style scoped>
.product-detail {
  padding: 20px;
}

.product-container {
  display: flex;
  gap: 40px;
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.product-image {
  flex: 0 0 400px;
}

.product-image img {
  width: 100%;
  height: 400px;
  object-fit: cover;
  border-radius: 8px;
}

.product-info {
  flex: 1;
}

.product-info h1 {
  margin: 0 0 20px 0;
  color: #333;
  font-size: 24px;
}

.price-section {
  margin-bottom: 20px;
  padding: 15px;
  background: #f8f8f8;
  border-radius: 8px;
}

.price {
  color: #f56c6c;
  font-size: 28px;
  font-weight: bold;
  margin-right: 20px;
}

.stock {
  color: #909399;
  font-size: 16px;
}

.description, .farmer-info {
  margin-bottom: 20px;
}

.description h3, .farmer-info h3 {
  color: #333;
  margin-bottom: 10px;
  font-size: 18px;
}

.description p, .farmer-info p {
  color: #666;
  line-height: 1.6;
}

.action-section {
  margin-top: 30px;
  display: flex;
  gap: 20px;
  align-items: center;
}

.el-input-number {
  width: 150px;
}
</style>