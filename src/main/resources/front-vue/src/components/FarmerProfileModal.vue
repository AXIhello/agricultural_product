<template>
  <!-- 只有 visible 为 true 时才渲染 -->
  <div v-if="visible" class="modal-overlay" @click.self="close">
    <div class="model-container user-profile-modal">
      <div class="modal-title-div">
        <h3 class="modal-title">农户档案</h3>
      </div>

      <!-- 加载状态 -->
      <div v-if="loading" class="loading-state">数据加载中...</div>

      <!-- 内容区域 -->
      <div v-else class="profile-content">
        <!-- 1. 头部基本信息 -->
        <div class="profile-header">
          <div class="avatar-placeholder">{{ userInfo.userName?.charAt(0) }}</div>
          <div class="header-info">
            <h4>{{ userInfo.name || userInfo.userName }}</h4>
            <p>用户ID: {{ userInfo.userId }}</p>
            <div class="credit-badge" v-if="showCredit">
              信用分: <strong>{{ userInfo.creditScore || '未评估' }}</strong>
            </div>
          </div>
        </div>

        <hr class="divider"/>

        <!-- 2. 经营状况统计 -->
        <div class="stats-row">
          <div class="stat-item">
            <label>在售商品</label>
            <span>{{ products.length }} 种</span>
          </div>
          <div class="stat-item">
            <label>累计销量</label>
            <span>{{ totalSales }} 件</span>
          </div>
          <div class="stat-item">
            <label>商品均分</label>
            <span class="rating-star">★ {{ avgRating }}</span>
          </div>
        </div>

        <!-- 3. 商品列表 -->
        <div class="section-block">
          <h5>主要产品</h5>
          <div class="mini-list-box">
            <div v-if="products.length === 0" class="empty-text">暂无上架商品</div>
            <div v-else v-for="prod in products" :key="prod.id" class="mini-row">
              <a href="javascript:void(0);" @click="goToProductDetail(prod)" class="prod-name-link">
                {{ prod.productName }}
              </a>
              <span class="prod-meta">
                销量: {{ prod.sales || 0 }} 
                <span v-if="prod.ratingCount > 0">
                  | {{ prod.averageRating.toFixed(1) }}分 ({{ prod.ratingCount }}评)
                </span>
                <span v-else>
                  | 暂无评分
                </span>
              </span>
            </div>
          </div>
        </div>

        <!-- 4. 融资历史  -->
        <div class="section-block" v-if="showFinancing">
          <h5>历史融资记录</h5>
          <div class="mini-list-box">
            <div v-if="financingHistory.length === 0" class="empty-text">暂无记录</div>
            <div v-else v-for="his in financingHistory" :key="his.id" class="mini-row history-row">
              <!-- 日期 -->
              <span class="his-date">{{ formatDate(his.date || his.createTime) }}</span>
              <!-- 金额 -->
              <span class="his-amount">¥{{ his.amount }}</span>
              <!-- 状态：优先使用 applicationStatus，防止字段名对不上 -->
              <span :class="'his-status ' + getHistoryStatusClass(his.applicationStatus || his.status)">
                {{ getHistoryStatusText(his.applicationStatus || his.status) }}
              </span>
            </div>
          </div>
        </div>

        <button class="cancel-btn full-width" @click="close">关闭</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import axios from '../utils/axios'
import { useRouter } from 'vue-router'

//初始化
const router = useRouter()

// 状态控制
const visible = ref(false)
const loading = ref(false)
const showFinancing = ref(true)
const showCredit = ref(true)

// 数据模型
const userInfo = ref({})
const products = ref([])
const financingHistory = ref([])
const totalSales = ref(0)
const avgRating = ref(0)

// === 记录是否从FinancingService打开 ===
const isOpenedFromFinancingApp = ref(false);
const fromFinancingProductId = ref(null);

// 打开弹窗的方法
const open = async (userId, options = {}) => {
  if (!userId) return
  visible.value = true
  loading.value = true

  showFinancing.value = !options.hideFinancing
  showCredit.value = !options.hideCredit 

  isOpenedFromFinancingApp.value = options.fromFinancingApp || false;
  fromFinancingProductId.value = options.fromFinancingProductId || null;
  
  // 重置数据
  userInfo.value = {}
  products.value = []
  financingHistory.value = []

  try {
    await fetchData(userId, showFinancing.value)
  } catch (err) {
    console.error("获取农户详情失败", err)
  } finally {
    loading.value = false
  }
}

// 关闭方法
const close = () => {
  visible.value = false
}

async function goToProductDetail(product) {
    console.log('传入 goToProductDetail 的 product 对象:', product); // 这行可以保留用于调试
    console.log('product.productId 是:', product ? product.productId : 'undefined'); // 检查 product.productId

    if (product && product.productId) { 
      const targetRoute = {
        name: 'ProductDetail',
        params: { id: product.productId }, 
        query: {}
      };

      // 如果是从 FinancingApp 打开的，则添加返回的农户ID
    if (isOpenedFromFinancingApp.value && userInfo.value.userId && fromFinancingProductId.value) {
        targetRoute.query.returnToFarmerId = userInfo.value.userId;
        targetRoute.query.fromParentPage = 'FinancingApp'; 
        targetRoute.query.returnToFinancingProductId = fromFinancingProductId.value;
        
    }

    router.push(targetRoute);
    close();
  }
}

// 核心数据获取逻辑
const fetchData = async (userId,  loadHistory = true) => {
  try {
    // A. 获取用户信息
    const userRes = await axios.get(`/user/info/${userId}`)
    userInfo.value = userRes.data.user || {}

    // B. 获取产品列表 
    let prodList = []
    try {
      
      const prodRes = await axios.get(`/products/farmer/${userId}`, { 
        params: { 
          pageNum: 1, 
          pageSize: 50 
        } 
      })
      
      prodList = prodRes.data.records || []
      
    } catch(e) { 
      console.warn('获取商品列表失败', e)
      prodList = []
    }
    products.value = prodList

    // --- 计算统计数据 ---
    let salesSum = 0
    let ratingSum = 0 // 评分总和
    let validRatingCount = 0 // 有评分的商品数量
    
    prodList.forEach(p => {
      // 1. 累加销量
      salesSum += (p.sales || 0)

      // 2. 累加评分
      if (p.averageRating && p.averageRating > 0) {
        ratingSum += p.averageRating
        validRatingCount++
      }
    })
    
    totalSales.value = salesSum
    // 计算店铺综合评分
    avgRating.value = validRatingCount > 0 ? (ratingSum / validRatingCount).toFixed(1) : '暂无'
    
    // C. 获取融资历史
    if (loadHistory) {
      try {
        const hisRes = await axios.get('/financing/list/by-user', { 
          params: { 
            userId: userId,
            pageNum: 1,
            pageSize: 20 
          } 
        })

        financingHistory.value = hisRes.data.records || hisRes.data || []
        
      } catch (e) {
        console.warn('获取融资历史失败', e)
        financingHistory.value = []
      }
    }
    else {
        // 如果不需要加载，直接置空
        financingHistory.value = []
    }

  } catch (error) {
    throw error
  }
}

// 辅助函数：格式化日期
const formatDate = (val) => {
  if(!val) return ''
  return String(val).substring(0, 10)
}

// 辅助函数：状态转中文 (已补全)
const getHistoryStatusText = (status) => {
  if (!status) return '未知';
  const map = { 
    'draft': '草稿',
    'submitted': '待审核',
    'approved': '已批准',
    'rejected': '已拒绝', 
    'repaid': '已还清', 
    'overdue': '已逾期',
    'cancelled': '已取消'
  }
  return map[status] || status
}

// 辅助函数：状态转颜色样式 (已补全)
const getHistoryStatusClass = (status) => {
  if (!status) return 'text-gray';
  
  if(['approved', 'repaid'].includes(status)) return 'text-green' // 成功类
  if(['rejected', 'overdue'].includes(status)) return 'text-red'  // 失败/警告类
  if(['submitted'].includes(status)) return 'text-orange'         // 进行中类
  
  return 'text-gray' // 草稿/取消/未知
}

defineExpose({
  open
})
</script>

<style scoped>
.modal-overlay {
  position: fixed; inset: 0; background: rgba(0,0,0,0.4); z-index: 2000;
  display: flex; justify-content: center; align-items: center;
}
.model-container { background: #fff; border-radius: 16px; padding: 24px; box-shadow: 0 8px 20px rgba(0,0,0,0.15); }
.user-profile-modal { width: 480px; max-width: 90%; }
.modal-title { font-size: 18px; color: #2D7D4F; text-align: center; margin-bottom: 15px; font-weight: bold;}

.profile-header { display: flex; align-items: center; gap: 16px; margin-bottom: 16px; }
.avatar-placeholder { width: 50px; height: 50px; background-color: #52c41a; color: white; font-size: 20px; font-weight: bold; border-radius: 50%; display: flex; align-items: center; justify-content: center; }
.header-info h4 { margin: 0 0 4px 0; font-size: 16px; color: #333; }
.header-info p { margin: 0 0 6px 0; font-size: 12px; color: #888; }
.credit-badge { background: #fff7e6; color: #fa8c16; border: 1px solid #ffd591; padding: 1px 6px; border-radius: 4px; font-size: 12px; display: inline-block; }

.divider { border: 0; border-top: 1px solid #eee; margin: 15px 0; }

.stats-row { display: flex; justify-content: space-around; background: #f9f9f9; padding: 10px; border-radius: 8px; margin-bottom: 15px; }
.stat-item { display: flex; flex-direction: column; align-items: center; }
.stat-item label { font-size: 12px; color: #888; margin-bottom: 2px; }
.stat-item span { font-size: 15px; font-weight: bold; color: #333; }
.rating-star { color: #faad14 !important; }

.section-block { margin-bottom: 15px; }
.section-block h5 { margin: 0 0 8px 0; font-size: 13px; color: #2D7D4F; border-left: 3px solid #2D7D4F; padding-left: 8px; }
.mini-list-box { border: 1px solid #eee; border-radius: 6px; max-height: 120px; overflow-y: auto; padding: 0 10px; }
.mini-row { display: flex; justify-content: space-between; padding: 8px 0; border-bottom: 1px dashed #f0f0f0; font-size: 12px; }
.mini-row:last-child { border-bottom: none; }
.empty-text { padding: 10px; text-align: center; color: #999; font-size: 12px; }

.prod-name-link { 
  font-weight: 500; 
  color: #2D7D4F; /* 使用主题色或蓝色 */
  cursor: pointer;
  text-decoration: none; /* 默认不带下划线 */
  transition: color 0.2s, text-decoration 0.2s;
}
.prod-name-link:hover {
  text-decoration: underline; /* 鼠标悬停时显示下划线 */
  color: #246640; /* 鼠标悬停时颜色变深 */
}

.prod-meta { color: #999; }
.history-row .his-date { color: #888; flex: 1; }
.history-row .his-amount { font-weight: bold; flex: 1; text-align: left; }
.history-row .his-status { flex: 0 0 60px; text-align: right; font-weight: bold; font-size: 12px;}

/* 状态颜色类 */
.text-green { color: #52c41a; }
.text-red { color: #ff4d4f; }
.text-orange { color: #fa8c16; }
.text-gray { color: #ccc; }

.full-width { width: 100%; margin-top: 5px; background-color: #f0f0f0; border: none; padding: 8px; border-radius: 8px; cursor: pointer; color: #666;}
.full-width:hover { background-color: #e0e0e0; }
.loading-state { text-align: center; padding: 30px; color: #999; }
</style>