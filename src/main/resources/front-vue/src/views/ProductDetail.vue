<template>
  <div class="product-detail-page">
    <!-- 数据加载完成前显示 loading 状态，防止页面闪烁和报错 -->
    <div v-if="loading" class="loading-state">
      <p>正在努力加载商品信息...</p>
    </div>

    <!-- 数据加载成功后显示商品内容 -->
    <div v-else-if="product.productId" class="product-container">
      <div class="product-image">
        <img :src="imgUrl" alt="商品图片">
      </div>

      <div class="product-info">
        <h1>{{ product.productName }}</h1>
        
        <div class="price-section">
          <!-- 在价格和库存后面加上单位信息 -->
          <span class="price">¥{{ product.price }} / {{ product.unitInfo }}</span>
          <span class="stock">库存: {{ product.stock }} {{ product.unitInfo }}</span>
        </div>

        <div class="description">
          <h3>商品描述</h3>
          <p>{{ product.description || '该商品暂无详细描述' }}</p>
        </div>

        <!-- 新增：显示商品分类信息 -->
        <div class="category-info">
          <h3>商品分类</h3>
          <p>
            <span class="category-tag">{{ product.prodCat }}</span>
            <span class="category-separator">></span>
            <span class="category-tag">{{ product.prodPcat }}</span>
          </p>
        </div>

        <div class="farmer-info">
          <h3>农户信息</h3>
          <p>农户ID: {{ product.farmerId }}</p>
        </div>

        <div class="action-section">
          <button class="contact-btn" @click="contactSeller(product)">联系卖家</button>
          <div class="quantity-control">
             <label>数量:</label>
             <el-input-number 
              v-model="quantity" 
              :min="1" 
              :max="product.stock"
              size="default"
            />
          </div>
          <el-button type="primary" size="large" @click="handleAddToCart" :loading="isAddingToCart">加入购物车</el-button>
          <el-button type="danger" size="large" @click="buyNow">立即购买</el-button>
        </div>
      </div>
    </div>
    
    <!-- 加载失败或商品不存在时显示 -->
    <div v-else class="error-state">
        <p>抱歉，商品信息加载失败或该商品不存在。</p>
        <el-button @click="router.back()">返回上一页</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import axios from '../utils/axios'
import placeholder from '../assets/img.png' // 引入占位图
import { useAuthStore } from '@/stores/authStore'
import { storeToRefs } from 'pinia'

const imgUrl = ref(placeholder)
const route = useRoute()
const router = useRouter()
const product = ref({})
const quantity = ref(1)
const loading = ref(true)
const isAddingToCart = ref(false)

const authStore = useAuthStore();
const { userInfo, isLoggedIn } = storeToRefs(authStore)

// 获取商品详情
const loadProductDetail = async () => {
  loading.value = true
  try {
    const response = await axios.get(`/products/${route.params.id}`)
    product.value = response.data
    console.log(product.value)

    const imageRes = await axios.get(`/products/${product.value.productId}/image`, {
      responseType: 'blob'
    })

    // 如果请求成功且有数据
    if (imageRes.status === 200 && imageRes.data.size > 0) {
      imgUrl.value = URL.createObjectURL(imageRes.data)
    } else {
      imgUrl.value = placeholder // 请求失败或者没有图片
    }
  } catch (error) {
    console.error('获取商品详情失败:', error)
    ElMessage.error('获取商品详情失败，请稍后重试')
  } finally {
    loading.value = false // 无论成功失败，都结束 loading
  }
}

async function contactSeller(product) {
  if (!product || !product.farmerId) {
    console.error("该商品缺少卖家信息:", product);
    alert('无法联系卖家，卖家信息丢失。');
    return;
  }
  await goToChat(product.farmerId);
}

async function goToChat(receiverId) {
  // 检查用户是否已登录
  if (!isLoggedIn.value) {
    ElMessage.warning('请先登录才能发起聊天！');
    router.push('/login');
    return;
  }

  // 检查 receiverId 是否有效
  if (!receiverId) {
    alert('无法联系，对方信息丢失。');
    return;
  }

  // 3. 检查用户是否在和自己聊天
  //    使用 String() 转换以确保类型一致
  if (String(userId.value) === String(receiverId)) {
    alert('您不能和自己发起聊天。');
    return;
  }

  // 4. 跳转到聊天页面
  console.log(`准备跳转到与用户 ${receiverId} 的聊天室`);
  await router.push(`/chat/${receiverId}`);
}

/**
 * 用于“我的订单”列表，点击后调用核心函数
 * @param {number | string} farmerId - 农户的ID
 */
async function contactFarmer(farmerId) {

  await goToChat(farmerId);
}

// 加入购物车
const handleAddToCart = async () => {
  
  if (!isLoggedIn.value) {
    ElMessage.warning('请先登录后再操作！');
    router.push('/login');
    return;
  }
  
  isAddingToCart.value = true
  try {
    // 加入购物车的请求格式通常需要 userId
    await axios.post('/cart/add', null, {
      params: {
        userId: userInfo.value.userId,
        productId: product.value.productId,
        quantity: quantity.value
      }
    })
    ElMessage.success('成功加入购物车')
  } catch (error) {
    console.error('加入购物车失败:', error)
    ElMessage.error(error.response?.data?.message || '加入购物车失败')
  } finally {
    isAddingToCart.value = false
  }
}

// 立即购买
const buyNow = () => {
  if (!isLoggedIn.value) {
    ElMessage.warning('请先登录后再操作！');
    router.push('/login');
    return;
  }

  ElMessage.info('“立即购买”功能正在开发中，您可以先将商品加入购物车')
  // 实际开发中，这里会跳转到订单确认页面
  /*
  router.push({
    name: 'orderConfirm',
    state: { 
      orderItems: [{ ...product.value, quantity: quantity.value }]
    }
  })
  */
}

onMounted(() => {
  loadProductDetail()
})
</script>

<style scoped>
.product-detail-page {
  padding: 20px;
  background-color: #f5f5f5;
  min-height: calc(100vh - 60px); /* 假设你有60px的导航栏 */
}

.product-container {
  display: flex;
  gap: 40px;
  background: white;
  padding: 30px;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  max-width: 1200px;
  margin: 0 auto;
}

.product-image {
  flex: 0 0 400px;
  align-self: center;
}

.product-image img {
  width: 100%;
  height: 400px;
  object-fit: cover;
  border-radius: 8px;
  border: 1px solid #eee;
}

.product-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.product-info h1 {
  margin: 0 0 15px 0;
  color: #303133;
  font-size: 28px;
  font-weight: 600;
}

.price-section {
  margin-bottom: 20px;
  padding: 20px;
  background: #fff8f8; /* 淡红色背景突出价格 */
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.price {
  color: #f56c6c;
  font-size: 32px;
  font-weight: bold;
}

.stock {
  color: #909399;
  font-size: 16px;
}

.description, .farmer-info, .category-info {
  margin-bottom: 25px;
}

.description h3, .farmer-info h3, .category-info h3 {
  color: #303133;
  margin-bottom: 10px;
  font-size: 18px;
  padding-left: 10px;
  border-left: 4px solid #409EFF; /* 添加蓝色竖线装饰 */
}

.description p, .farmer-info p, .category-info p {
  color: #606266;
  line-height: 1.8;
  font-size: 16px;
}

.action-section {
  margin-top: auto; /* 将操作区推到底部 */
  padding-top: 20px;
  border-top: 1px solid #f0f0f0;
  display: flex;
  gap: 20px;
  align-items: center;
}

.quantity-control {
  display: flex;
  align-items: center;
  gap: 10px;
}

.quantity-control label {
  font-size: 16px;
  color: #606266;
}

.el-input-number {
  width: 120px;
}

/* loading 和 error 状态的样式 */
.loading-state, .error-state {
  text-align: center;
  padding: 80px 20px;
  color: #909399;
  font-size: 18px;
}

/* 分类标签样式 */
.category-tag {
  display: inline-block;
  background-color: #ecf5ff;
  color: #409eff;
  padding: 4px 10px;
  border-radius: 4px;
  font-size: 14px;
  border: 1px solid #d9ecff;
}

.category-separator {
  margin: 0 8px;
  color: #909399;
}
</style>