<template>
  <div class="product-detail-page">
    <button v-if="props.showBackButton" @click="goBack" class="back-button">
        <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <line x1="19" y1="12" x2="5" y2="12"></line>
          <polyline points="12 19 5 12 12 5"></polyline>
        </svg>
        返回
    </button>

    <!-- 加载中 -->
    <div v-if="loading" class="loading-state">
      <p>正在努力加载商品信息...</p>
    </div>

    <!-- 商品详情 -->
    <div v-else-if="currentSpec.productId" class="product-container">
      <div class="product-left">
        <div class="product-image">
          <img :src="imgUrl" alt="商品图片" @click="showPreview = true">
          <ImagePreview :visible="showPreview" :src="imgUrl" @close="showPreview = false" />
        </div>

        <!-- 下方评价 -->
        <div class="review-column">
          <h3>买家评价</h3>
          <div v-if="reviewLoading" class="loading">评价加载中...</div>
          <div v-else-if="!reviews.length" class="no-review">暂无评价</div>
          <div v-else class="review-list">

            <div v-for="(r, idx) in reviews" :key="idx">
              <div v-if="r" class="review-card">
                <div class="review-header">
                  <span class="user-nick">{{ r.isAnonymous ? '匿名用户' : '用户' + r.userId }}</span>
                  <Stars :rating="r.rating" />
                  <span class="review-time">{{ formatTime(r.updateTime) }}</span>
                </div>
                <div class="review-content">{{ r.content }}</div>
              </div>
            </div>

          </div>
        </div>

      </div>

      <div class="product-info">
        <h1>{{ currentSpec.productName }}</h1>

        <!-- 价格库存 -->
        <div class="price-section">
          <span class="price">¥{{ currentSpec.price }} / {{ currentSpec.unitInfo }}</span>
          <span class="stock">库存: {{ currentSpec.stock }} {{ currentSpec.unitInfo }}</span>
        </div>

        <div class="description">
          <h3>商品描述</h3>
          <p>{{ currentSpec.description || '该商品暂无详细描述' }}</p>
        </div>

        <div class="category-info">
          <h3>商品分类</h3>
          <p>
            <span class="category-tag">{{ currentSpec.prodCat }}</span>
            <span class="category-separator">></span>
            <span class="category-tag">{{ currentSpec.prodPcat }}</span>
          </p>
        </div>

        <div class="farmer-info">
          <h3>农户信息</h3>
          <div class="farmer-info-row">
            <p class="clickable-farmer" @click="openFarmerDetail">
              农户ID: {{ currentSpec.farmerId }} 
              <span class="info-icon">ℹ</span> 
            </p>

            <!-- 只有当当前用户不是该商品的农户才显示“联系卖家” -->
            <button
                v-if="currentUserId !== currentSpec.farmerId"
                class="contact-btn"
                @click="contactSeller(currentSpec)"
            >
              联系卖家
            </button>
          </div>
        </div>

        <!-- 规格选择 -->
        <div v-if="productSpecs.length > 1" class="spec-section">
          <h3>选择规格</h3>
          <div class="spec-buttons">
            <button
                v-for="spec in productSpecs"
                :key="spec.productId"
                :class="['spec-button', { active: spec.productId === currentSpec.productId }]"
                @click="selectSpec(spec)"
            >
              {{ spec.specInfo }}
            </button>
          </div>
        </div>

        <!-- 购买区域：只有当当前用户不是该农户才显示 -->
        <div
            v-if="currentUserId !== currentSpec.farmerId"
            class="action-buttons"
        >
          <div class="quantity-control">
            <label>数量:</label>
            <el-input-number
                v-model="quantity"
                :min="1"
                :max="currentSpec.stock"
                size="default"
            />
          </div>

          <el-button type="primary" size="large"
                     @click="handleAddToCart(currentSpec)"
                     :loading="isAddingToCart">
            加入购物车
          </el-button>

          <el-button type="danger" size="large" @click="buyNow(currentSpec)">
            立即购买
          </el-button>
        </div>

      </div>

    </div>

    <!-- 错误 -->
    <div v-else class="error-state">
      <p>抱歉，商品信息加载失败或该商品不存在。</p>
      <el-button @click="router.back()">返回上一页</el-button>
    </div>
    
    <FarmerProfileModal ref="farmerProfileRef" />

  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import axios from '../utils/axios'
import placeholder from '../assets/img.png'
import { useAuthStore } from '@/stores/authStore'
import { storeToRefs } from 'pinia'
import ImagePreview from '@/components/ImagePreview.vue'
import Stars from '@/components/Stars.vue'
import FarmerProfileModal from '@/components/FarmerProfileModal.vue'

const authStore = useAuthStore()
const {userInfo, isLoggedIn, role} = storeToRefs(authStore)
const currentUserId = userInfo.value?.userId

const imgUrl = ref(placeholder)
const showPreview = ref(false)
const route = useRoute()
const router = useRouter()
const farmerProfileRef = ref(null)

// 规格相关
const productSpecs = ref([])     // 所有规格（同名的不同商品）
const currentSpec = ref({})      // 当前选中的规格
const farmerInfo = ref(null) 

const quantity = ref(1)
const loading = ref(true)
const isAddingToCart = ref(false)

const props = defineProps({
  showBackButton: { 
    type: Boolean,
    default: true // 默认显示返回按钮
  }
});

//打开农户详细信息弹窗
const openFarmerDetail = () => {
  if (currentSpec.value && currentSpec.value.farmerId) {
    // 同时隐藏融资记录和信用分
    farmerProfileRef.value.open(currentSpec.value.farmerId, { 
      hideFinancing: true,
      hideCredit: true 
    })
  }
}

//时间格式化
const formatTime = (t) => (t ? new Date(t).toLocaleString() : '')

// 加载某个规格的图片
const loadImage = async (productId) => {
  imgUrl.value = placeholder

  try {
    const imageRes = await axios.get(`/products/${productId}/image`, {
      responseType: 'blob'
    })
    if (imageRes.status === 200 && imageRes.data.size > 0) {
      imgUrl.value = URL.createObjectURL(imageRes.data)
    }
  } catch (e) {
    console.warn("加载规格图片失败，使用默认图")
  }
}

// 获取农户详细信息（包括角色）
const fetchFarmerInfo = async (farmerId) => {
  try {
    const res = await axios.get(`/user/info/${farmerId}`);
    console.log(`fetchFarmerInfo for ${farmerId} response:`, res.data); // 添加这行调试
    if (res.data && res.data.success) {
      farmerInfo.value = res.data.user; // 确认这里是 res.data.data 还是 res.data.user
      console.log('Fetched farmerInfo:', farmerInfo.value); // 添加这行调试
    } else {
      console.error(`获取农户 ${farmerId} 信息失败:`, res.data?.message);
      farmerInfo.value = null;
    }
  } catch (error) {
    console.error(`获取农户 ${farmerId} 信息时发生错误:`, error);
    farmerInfo.value = null;
  }
}

// 主方法：加载详情 + 同名规格
const loadProductDetail = async () => {
  loading.value = true
  try {
    // 第1步：根据 ID 获取该商品
    const res = await axios.get(`/products/${route.params.id}`)
    const base = res.data

    // 第2步：获取同名所有规格
    const specRes = await axios.get('/products/productName', {
      params: { name: base.productName,
                createTime:base.createTime}
    })

    // 后端可能返回 Page，也可能返回 List
    productSpecs.value = specRes.data?.records || specRes.data || []

    // 第3步：设置当前规格 = 当前 ID 对应项
    currentSpec.value =
        productSpecs.value.find(s => s.productId == base.productId) ||
        productSpecs.value[0]

    // 第4步：加载当前规格图片
    await loadImage(currentSpec.value.productId)

  } catch (error) {
    console.error('获取商品详情失败:', error)
    ElMessage.error('获取商品详情失败，请稍后重试')
  } finally {
    loading.value = false
  }
}


// 切换规格
const selectSpec = async (spec) => {
  currentSpec.value = spec
  quantity.value = 1
  await loadImage(spec.productId)
}



//联系卖家
async function contactSeller(product) {
  if (!product || !product.farmerId) {
    console.error("该商品缺少卖家信息:", product);
    alert('无法联系卖家，卖家信息丢失。');
    return;
  }

  if (!farmerInfo.value || farmerInfo.value.userId !== product.farmerId || !farmerInfo.value.role) {
    console.warn("未能获取到农户的详细角色信息，尝试重新获取...");
    await fetchFarmerInfo(product.farmerId); // 确保获取到最新的农户信息
    
    if (!farmerInfo.value || !farmerInfo.value.role) {
      alert('无法获取农户角色信息，无法发起聊天。');
      return;
    }
  }

  goToChat(product.farmerId, farmerInfo.value.role);
}

// 发起与指定用户的聊天
async function goToChat(receiverId, receiverRole) {
  // 检查用户是否已登录
  if (!isLoggedIn.value) {
    ElMessage.warning('请先登录才能发起聊天！');
    router.push('/login');
    return;
  }

  // 检查 receiverId 是否有效
  if (!receiverId) {
    ElMessage.error('无法联系，对方信息丢失。');
    return;
  }


  // 3. 检查用户是否在和自己聊天
  //    使用 String() 转换以确保类型一致
  if (String(currentUserId) === String(receiverId)) { 
      ElMessage.info('您不能和自己发起聊天。'); 
      return;
    }

  // 4. 跳转到聊天页面
  console.log(`准备跳转到与用户 ${receiverId} 的聊天室，对方角色: ${receiverRole}`);
  router.push({ name: 'Chat', params: { receiverId: receiverId } }); 
}


// 加入购物车
const handleAddToCart = async (product) => {
  if (!isLoggedIn.value) {
    ElMessage.warning('请先登录后再操作！');
    router.push('/login');
    return;
  }

  isAddingToCart.value = true;
  try {
    await axios.post('/cart/add', null, {
      params: {
        userId: userInfo.value.userId,
        productId: product.productId, // 使用传入的 product
        quantity: quantity.value
      }
    });
    ElMessage.success('成功加入购物车');
  } catch (error) {
    console.error('加入购物车失败:', error);
    ElMessage.error(error.response?.data?.message || '加入购物车失败');
  } finally {
    isAddingToCart.value = false;
  }
}

// 立即购买
const buyNow = (product) => {
  if (!isLoggedIn.value) {
    alert('请先登录！');
    return;
  }

  const orderPreview = {
    addressId: null,
    orderItems: [{
      productId: product.productId,
      specInfo: product.specInfo,
      quantity: quantity.value
    }]
  };
  localStorage.setItem('orderPreview', JSON.stringify(orderPreview));

  router.push({ path: '/order/confirm' });

  console.log('跳转到订单确认页面，传递参数:', orderPreview);
};

//加载评价
const reviews = ref([])
const reviewLoading = ref(true)

const loadReviews = async () => {
  reviewLoading.value = true
  try {
    const res = await axios.get(`/orders/product/${route.params.id}/reviews`, {
      params: { page: 1, size: 20 }   
    })
    reviews.value = (res.data.records || []).filter(r => r && r.rating != null)
  } finally {
    reviewLoading.value = false
  }
}

onMounted(() => {
  loadProductDetail()
  loadReviews()
})

function goBack() {
  const returnToFarmerId = route.query.returnToFarmerId;
  const returnToFinancingProductId = route.query.returnToFinancingProductId; // <--- 新增
  const fromParentPage = route.query.fromParentPage;

  if (fromParentPage === 'FinancingApp') {
    // 目标路由始终是 FinancingApp
    const targetQuery = {};
    if (returnToFarmerId && returnToFinancingProductId) {
      targetQuery.returnToFarmerId = returnToFarmerId;
      targetQuery.returnToFinancingProductId = returnToFinancingProductId;
    }

    router.push({ 
      name: 'FinanceService', 
      query: targetQuery
    });
  } else {
    // 否则执行默认的返回行为
    router.back();
  }
}

</script>

<style scoped>
/* 整个页面背景：纯白 */
.product-detail-page {
  width: 100%;
  padding: 40px 60px;
  min-height: 100vh;
  background: #ffffff; /* 白底 */
  box-sizing: border-box;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial;
}

/* 商品内容整体布局：左右两栏 */
.product-container {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 40px;
  width: 100%;
}

/* 左半部分（产品图片 + 评论） */
.product-left {
  display: flex;
  flex-direction: column;
  gap: 40px;
  width: 50%;
  box-sizing: border-box;
}

/* 图片保持你的样式 */
.product-image {
  padding-top: 40px;
  display: flex;
  justify-content: center;
  align-items: center;
}

.product-image img {
  width: 100%;
  max-height: 450px;
  border-radius: 16px;
  object-fit: cover;
  cursor: zoom-in;
  box-shadow: 0 5px 15px rgba(0,0,0,0.08);
}

/* 右半部分固定，字不溢出 */
.product-info {
  padding: 40px;
  background: #ffffff;
  width: 50%;            /* 占一半宽度 */
  max-width: 600px;      /* 限制最大宽度，防止溢出 */
  box-sizing: border-box;

  display: flex;
  flex-direction: column;
  gap: 18px;
}

.product-info h1 {
  font-size: 30px;
  font-weight: 700;
  line-height: 1.2;
}


/* loading / error 和整体风格一致 */
.loading-state,
.error-state {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 300px;
  color: #666;
  font-size: 16px;
}

.price-section {
  padding: 15px 20px;
  background: #f0f9f2;
  border-left: 6px solid #2d7d4f;
  border-radius: 12px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.price-section .price {
  font-size: 28px;
  font-weight: 800;
  color: #2d7d4f;
}

.price-section .stock {
  font-size: 14px;
  color: #555;
}

.description, .category-info, .farmer-info {
  background: #f9f9f9;
  padding: 15px 20px;
  border-radius: 12px;
}

.description h3,
.category-info h3,
.farmer-info h3 {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 8px;
}

.description p,
.category-info p,
.farmer-info p {
  font-size: 14px;
  line-height: 1.6;
  color: #555;
}

.farmer-info-row {
  display: flex;
  justify-content: space-between; /* 左右分开 */
  align-items: center;           /* 垂直居中 */
  margin-top: 8px;
}

.category-tag {
  display: inline-block;
  padding: 2px 8px;
  background: #e6f4ea;
  border-radius: 8px;
  font-size: 13px;
  color: #2d7d4f;
}

.category-separator {
  margin: 0 5px;
  color: #aaa;
}

.spec-section {
  margin-top: 15px;
}

.spec-section h3 {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 10px;
}

.spec-buttons {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.spec-button {
  padding: 10px 18px;
  border-radius: 10px;
  border: 1px solid #ddd;
  background: #fafafa;
  transition: 0.2s;
}

.spec-button.active {
  background: #2d7d4f;
  color: white;
  border-color: #2d7d4f;
}

.spec-button:hover {
  background: #e6f4ea;
}

.action-buttons {
  display: flex;
  gap: 15px;
  margin-top: 15px;
  align-items: center;
}

.contact-btn {
  width: 100px;
  padding: 10px 0;
  background: #fff;
  border: 1px solid #2d7d4f;
  border-radius: 12px;
  color: #2d7d4f;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.contact-btn:hover {
  background: #2d7d4f;
  color: #fff;
}

.quantity-control {
  display: flex;
  align-items: center;
  gap: 8px;
}

.el-input-number {
  width: 100px;
}

.el-button {
  border-radius: 8px !important;
  padding: 10px 22px !important;
  font-size: 16px;
  font-weight: 600;
}

.el-button.primary {
  background-color: #2d7d4f;
  border-color: #2d7d4f;
  color: #fff;
}

.el-button.danger {
  background-color: #e74c3c;
  border-color: #e74c3c;
  color: #fff;
}

.el-button.primary:hover {
  background-color: #246641;
  border-color: #246641;
}

.el-button.danger:hover {
  background-color: #c0392b;
  border-color: #c0392b;
}
</style>

/*=======评价样式=======*/
<style scoped>
/* 评论区 */
.review-column {
  background: #fafafa;
  padding: 20px;
  border-radius: 16px;
  max-height: 600px;
  overflow-y: auto;
  box-shadow: inset 0 0 10px rgba(0,0,0,0.05);
}


.review-card {
  background: #fff;
  padding: 15px;
  border-radius: 12px;
  margin-bottom: 12px;
  box-shadow: 0 2px 5px rgba(0,0,0,0.05);
}

.review-list {
  max-height: 480px;
  overflow-y: auto;
}

.review-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
}
.review-time {
  font-size: 12px;
  color: #999;
}
.review-content {
  font-size: 14px;
  color: #333;
  line-height: 1.6;
}
.no-review {
  text-align: center;
  color: #999;
  padding: 40px 0;
}
.user-nick {
  font-size: 13px;
  color: #666;
  margin-right: 8px;
}

@media (max-width: 1024px) {
  .product-container {
    grid-template-columns: 1fr;
  }
  .review-column {
    max-height: none;
  }
}

</style>

/* 农户ID可点击*/
<style scoped>
.clickable-farmer {
  cursor: pointer;
  color: #2d7d4f; /* 使用主题绿，或者蓝色 #1890ff */
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 6px;
  transition: opacity 0.2s;
}

.clickable-farmer:hover {
  text-decoration: underline;
  opacity: 0.8;
}

.info-icon {
  font-size: 12px;
  border: 1px solid currentColor;
  border-radius: 50%;
  width: 14px;
  height: 14px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}


/* ================== 返回按钮样式 ================== */
.back-button {
  position: absolute; /* <--- 绝对定位 */
  top: 15px; /* 距离顶部 */
  left: 20px; /* 距离左侧 */
  z-index: 100; /* 确保在最上层 */
  display: flex;
  align-items: center;
  gap: 5px;
  background: none;
  border: none;
  color: #2D7D4F; /* 主题绿色 */
  font-size: 16px;
  cursor: pointer;
  padding: 8px 12px;
  border-radius: 5px;
  transition: background-color 0.2s, color 0.2s;
}

.back-button:hover {
  background-color: #e6f4ea; /* 浅绿色背景 */
  color: #246640;
}

.back-button svg {
  stroke-width: 2.5;
}
</style>