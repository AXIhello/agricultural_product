<template>
  <!-- 权限控制：只给 bank 和 farmer 看 -->
  <!-- 结构对标 ExpertOverview：只有容器和卡片，没有标题，没有外层背景 -->
  <div class="finance-container" >
    
    <!-- 加载/空状态 -->
    <div v-if="loading" class="empty-state">加载中...</div>
    <div v-else-if="products.length === 0" class="empty-state">暂无推荐产品</div>

    <!-- 卡片列表：直接开始循环 -->
    <div 
      class="product-card" 
      v-for="p in products" 
      :key="p.productId"
      @click="goToFinancePage"
    >
      <!-- 1. 产品名称 (模仿专家名字：加大、加粗、深绿色) -->
      <h4 class="product-name">{{ p.productName }}</h4>

      <!-- 2. 描述 (模仿专家擅长领域：灰色小字) -->
      <p class="product-desc">{{ p.description || '暂无描述' }}</p>

      <!-- 3. 核心数据 (银行 & 利率) -->
      <div class="info-block">
        <div class="info-row">
          <span class="label">银行:</span>
          <span class="value bank-text">{{ p.bankName }}</span>
        </div>
        <div class="info-row">
          <span class="label">利率:</span>
          <span class="value rate-text">{{ p.interestRate }}%</span>
        </div>
      </div>

      <!-- 4. 按钮 (专家没有，但产品需要一个操作点) -->
      <button class="apply-btn" @click.stop="goToDetailWithPopup(p)">
        申请
      </button>
    </div>

    <!-- 查看更多：放在网格最后，或者用绝对定位放在右上角，这里为了布局整洁，也可以不放，或者放在父组件 -->
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/authStore'
import { storeToRefs } from 'pinia'
import axios from '@/utils/axios'

const router = useRouter()
const authStore = useAuthStore()
const { role } = storeToRefs(authStore)

const products = ref([])
const loading = ref(false)

// 点击卡片背景 -> 仅跳转页面
const goToFinancePage = () => {
  router.push('/finance')
}

// 点击申请按钮 -> 跳转页面 + 弹窗
const goToDetailWithPopup = (product) => {
  router.push({
    path: '/finance',
    query: { 
      view: 'products',          
      openId: product.productId  
    }
  })
}

const loadProducts = async () => {
  loading.value = true
  try {
    const res = await axios.get('/bank/products/all', { 
      params: { pageNum: 1, pageSize: 3 } 
    })
    const list = res.data.records || res.data || []

    const productsWithBankInfo = await Promise.all(list.map(async (item) => {
      try {
        const bankRes = await axios.get(`/user/info/${item.bankUserId}`)
        item.bankName = bankRes.data.user?.userName || '合作银行'
      } catch {
        item.bankName = '合作银行'
      }
      return item
    }))
    products.value = productsWithBankInfo
  } catch (err) {
    console.error('加载失败:', err)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  if (['bank', 'farmer'].includes(role.value)) {
    loadProducts()
  }
})
</script>

<style scoped>
/* ================== 布局容器 ================== */
/* 对应 ExpertOverview 的 .expert-overview-container */
.finance-container {
  display: grid;
  /* 产品信息比人名长，最小宽度设为 200px 比较合适 */
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 22px;
  padding: 10px; /* 保持和专家组件一致的内边距 */
}

/* ================== 单个卡片样式 ================== */
/* 对应 ExpertOverview 的 .expert */
/* 完全复制了你的专家组件 CSS */
.product-card {
  background-color: #f8fbf8;  /* 浅灰绿背景 */
  padding: 16px 12px;
  border-radius: 16px;

  border: 1px solid #d9e8df;  /* 浅绿边框 */
  box-shadow: 0 3px 8px rgba(0, 0, 0, 0.06);

  transition: all 0.25s ease;
  cursor: pointer;
  text-align: center;         /* 内容居中 */
  
  /* Flex布局让按钮沉底 */
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: space-between;
}

/* 悬停效果 (和专家组件一致) */
.product-card:hover {
  transform: translateY(-4px);
  border-color: #8bc3a1;
  box-shadow: 0 6px 14px rgba(0, 0, 0, 0.10);
}

/* ================== 内容样式 ================== */

/* 1. 产品名称 (对应 .expert h4) */
.product-name {
  font-size: 16px;
  font-weight: 600;
  color: #2D7D4F; /* 深绿色 */
  margin-bottom: 8px;
  margin-top: 5px;
}

/* 2. 描述 (对应 .expert p) */
.product-desc {
  font-size: 13px;
  color: #666;
  margin-bottom: 12px;
  /* 限制两行省略，防止卡片高度不一 */
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  height: 36px; 
}

/* 3. 信息块 (银行/利率) */
.info-block {
  width: 100%;
  margin-bottom: 12px;
  background: rgba(255,255,255,0.6); /* 稍微加点白底让数据更清晰 */
  border-radius: 8px;
  padding: 8px 0;
}

.info-row {
  display: flex;
  justify-content: center;
  align-items: baseline;
  margin-bottom: 4px;
  font-size: 13px;
}

.label {
  color: #888;
  margin-right: 5px;
}

/* 字体统一调整：加大加粗 */
.value {
  font-size: 16px;
  font-weight: bold;
  font-family: Arial, sans-serif;
}

.bank-text { color: #333; }
.rate-text { color: #e6a23c; }

/* 4. 按钮 */
.apply-btn {
  width: 80%;
  padding: 6px 0;
  background-color: #2D7D4F;
  color: white;
  border: none;
  border-radius: 20px;
  font-size: 13px;
  cursor: pointer;
}

.apply-btn:hover {
  background-color: #246640;
}

.empty-state {
  text-align: center;
  color: #999;
  grid-column: 1 / -1; /* 占满整行 */
  padding: 20px;
}
</style>