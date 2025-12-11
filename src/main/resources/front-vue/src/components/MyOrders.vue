<template>
  <div class="my-orders-container">
<!--    <div class="page-header">-->
<!--      <h2>我的订单</h2>-->
<!--    </div>-->

    <div class="order-list">
      <div v-if="loading" class="loading-state">
        <div class="spinner"></div> 加载中...
      </div>
      <div v-else-if="!orders.length" class="empty-state">
        <img src="https://cdn-icons-png.flaticon.com/512/4076/4076432.png" alt="无订单" />
        <p>您还没有相关的订单</p>
      </div>
      
      <!-- 订单卡片 -->
      <template v-else>
        <div v-for="order in orders" :key="order.orderId" class="order-card">
          
          <!-- 1. 卡片头部：浅灰色背景，强调订单号和状态 -->
          <div class="card-header">
            <div class="header-left">
              <span class="order-time">{{ formatTime(order.orderDate) }}</span>
              <span class="divider">|</span>
              <span class="order-id">订单号：{{ order.orderId }}</span>
            </div>
            <!-- 使用统一的状态函数 -->
            <div class="header-right">
              <span :class="['status-tag', getComputedStatus(order).class]">
                {{ getComputedStatus(order).text }}
              </span>
            </div>
          </div>

          <!-- 2. 商品列表区域 -->
          <div class="card-body">
            <div v-for="item in order.orderItems" :key="item.itemId" class="product-item">
              
              <div class="product-image">
                <img 
                  :src="item.imageUrl" 
                  alt="商品图" 
                  @error="handleImageError" 
                />
              </div>
              
              <div class="product-info">
                <div class="info-row-main">
                  <span class="product-name">{{ item.productName || '未知商品' }}</span>
                  <span class="product-price">¥{{ item.unitPrice.toFixed(2)  }}</span>
                </div>
                <div class="info-row-sub">
                   <span>数量: x{{ item.quantity }}</span>
                   <!-- 单品状态 (如退款中) -->
                   <span class="item-status-text" :class="getItemStatus(item, order).class">
                    {{ getItemStatus(item, order).text }}
                  </span>
                </div>
              </div>

              <!-- 单品操作按钮 (退款/评价) 放在右侧 -->
              <div class="product-actions" v-if="role === 'buyer'">
                 <!-- 已收货且未评价 -> 评价 -->
                 <button v-if="item.status === 'RECEIVED' && !item.isReviewed" class="btn-text" @click="openReviewModal(order.orderId, item)">评价</button>

                 <button 
                      v-if="item.isReviewed" 
                      class="btn-text blue" 
                      @click="openViewReviewModal(item.itemId)"
                    >
                      查看评价
                 </button>
                 
                 <!-- 允许退款的状态 -->
                 <button v-if="['PAID', 'SHIPPED', 'RECEIVED'].includes(item.status) && !item.refundStatus" class="btn-text gray" @click="openRefundModal(order.orderId, item)">申请售后</button>
                 
                 <!-- 确认收货 (如果发货了) -->
                 <button v-if="item.status === 'SHIPPED'" class="btn-outline-primary small" @click="confirmOrderItem(order.orderId, item)">确认收货</button>
              </div>

              <!-- 卖家操作 -->
              <div class="product-actions" v-if="role === 'farmer'">
                 <button 
                    v-if="item.status === 'PAID'" 
                    class="btn-primary small" 
                    @click="shipOrderItem(order.orderId, item)"
                  >
                    发货
                  </button>
                  
                  <!-- 已发货状态提示 -->
                  <span v-if="item.status === 'SHIPPED'" class="status-text-gray">
                    已发货
                  </span>

                  <!-- 处理退款按钮  -->
                  <button 
                    v-if="item.refundStatus === 'REQUESTED'" 
                    class="btn-warning small" 
                    @click="openRefundAuditModal(order.orderId, item)"
                  >
                    处理退款
                  </button>
              </div>
            </div>
          </div>

          <!-- 3. 卡片底部：总价与主操作 -->
          <div class="card-footer">
            <div class="total-price-section">
              <span>总金额:</span>
              <span class="price-large">¥{{ order.totalAmount.toFixed(2)  }}</span>
            </div>
            
            <div class="main-actions">
              <!-- 买家主操作 -->
              <template v-if="role === 'buyer'">
                <button v-if="order.status === 'pending'" class="btn-text gray" @click="cancelOrder(order.orderId)">取消订单</button>
                <button v-if="order.status === 'pending'" class="btn-primary" @click="goToPay(order.orderId)">立即支付</button>
              </template>
            </div>
          </div>
        </div>
      </template>
    </div>
    
    <!-- 弹窗部分保持不变-->
     <div v-if="showReviewModal" class="modal-overlay" @click.self="showReviewModal = false">
      <div class="modal-container">
        <h3>评价商品</h3>
        <div class="modal-form-group">
          <label>评分：</label>
          <div class="rating-input">
            <span v-for="n in 5" :key="n" @click="reviewForm.rating = n" :class="{ active: n <= reviewForm.rating }">★</span>
          </div>
        </div>
        <div class="modal-form-group">
          <label>内容：</label>
          <textarea v-model="reviewForm.content"></textarea>
        </div>
        <div class="form-actions">
          <button class="submit-btn" @click="submitReview">提交</button>
          <button class="cancel-btn" @click="showReviewModal = false">取消</button>
        </div>
      </div>
    </div>

    <!-- 2. 退款申请弹窗 -->
    <div v-if="showRefundModal" class="modal-overlay" @click.self="showRefundModal = false">
      <div class="modal-container">
        <h3>申请退款</h3>
        <div class="modal-form-group">
          <label>原因：</label>
          <textarea v-model="refundReason"></textarea>
        </div>
        <div class="form-actions">
          <button class="submit-btn" @click="submitRefund">提交</button>
          <button class="cancel-btn" @click="showRefundModal = false">取消</button>
        </div>
      </div>
    </div>

    <!-- 3. 退款审核弹窗 -->
    <div v-if="showRefundAuditModal" class="modal-overlay" @click.self="showRefundAuditModal = false">
      <div class="modal-container">
        <h3>处理退款</h3>
        <p>原因：{{ currentItem.refundReason }}</p>
        <div class="modal-form-group">
          <label>拒绝理由：</label>
          <textarea v-model="rejectRefundReason"></textarea>
        </div>
        <div class="form-actions">
          <button class="submit-btn" style="background:#52c41a" @click="auditRefund(true)">同意</button>
          <button class="submit-btn" style="background:#ff4d4f" @click="auditRefund(false)">拒绝</button>
        </div>
      </div>
    </div>

    <!-- 4. 查看评价详情弹窗 -->
    <div v-if="showViewReviewModal" class="modal-overlay" @click.self="showViewReviewModal = false">
      <div class="my-review-modal-content">
        
        <div class="detail-row">
          <div class="detail-label">评分:</div>
          <div class="rating-display">
            <span v-for="n in 5" :key="n" :class="{ active: n <= (currentReviewDetail?.rating || 0) }">★</span>
            <span class="rating-text">{{ currentReviewDetail?.rating || 0 }} 星</span>
          </div>
        </div>

        <div class="detail-row">
          <div class="detail-label">评价内容:</div>
          <div class="review-text-box">
            {{ currentReviewDetail?.content || '加载中...' }}
          </div>
        </div>

        <div class="detail-row">
          <div class="detail-label">评价时间:</div>
          <div class="time-text">
            {{ formatTime(currentReviewDetail?.updateTime) }}
          </div>
        </div>
        
        <div class="modal-actions">
          <button @click="showViewReviewModal = false">关 闭</button>
        </div>

      </div>

    </div>

  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import axios from '../utils/axios' 
import router from "@/router"
import { useAuthStore } from '@/stores/authStore'
import { storeToRefs } from 'pinia'
import defaultImg from '@/assets/img.png' // 默认图片占位符

// 获取全局状态
const authStore = useAuthStore()
// 必须获取 userInfo 以便拿到 userId 进行过滤
const { role, isLoggedIn, userInfo } = storeToRefs(authStore)

const orders = ref([])
const loading = ref(false)

// 弹窗状态
const showReviewModal = ref(false)
const showRefundModal = ref(false)
const showRefundAuditModal = ref(false)
const currentItem = ref({})
const currentOrderId = ref(null)
const reviewForm = ref({ rating: 5, content: '', isAnonymous: false })
const refundReason = ref('')
const rejectRefundReason = ref('')
const showViewReviewModal = ref(false)
const currentReviewDetail = ref(null) 

// 初始化加载
onMounted(() => {
  if (role.value) {
    loadOrders()
  }
})

// 监听角色变化（防止刷新页面时Pinia未就绪）
watch(role, (newRole) => {
  if (newRole) loadOrders()
})

// 加载订单核心逻辑
const loadOrders = async () => {
  if (!isLoggedIn.value) return
  loading.value = true
  try {
    let url = '/orders'; // 默认买家接口
    const currentRole = role.value ? role.value.toLowerCase() : '';

    if (currentRole === 'farmer') {
      url = '/orders/seller';
    }

    const res = await axios.get(url)
    let rawOrders = res.data || []

    // 农户过滤逻辑
    if (currentRole === 'farmer' && userInfo.value?.userId) {
      const myId = String(userInfo.value.userId);
      
      rawOrders.forEach(order => {
        if (order.orderItems) {
          order.orderItems = order.orderItems.filter(item => {         
            return String(item.farmerId) === myId
          });
        }
      });

      // 再次过滤：如果一个订单过滤完 items 是空的，就不显示这个订单
      rawOrders = rawOrders.filter(order => order.orderItems && order.orderItems.length > 0);
    }

    //图片加载逻辑
    // 遍历所有订单，再遍历所有订单项，去请求图片
    // 使用 Promise.all 并发请求，提高速度
    await Promise.all(rawOrders.map(async (order) => {
      if (order.orderItems && order.orderItems.length > 0) {
        await Promise.all(order.orderItems.map(async (item) => {
          // 先设置默认图，防止接口慢的时候空白
          item.imageUrl = defaultImg; 

          if (item.hasImage === false) return; 

          try {
            // 请求图片流
            const imageRes = await axios.get(`/products/${item.productId}/image`, {
              responseType: 'blob',
              // 添加 validateStatus，让 axios 把 404 当作正常响应处理，不抛出异常
              validateStatus: (status) => status >= 200 && status < 300 || status === 404
            });

            // 如果返回了有效数据，生成 URL
            if (imageRes.status === 200 && imageRes.data && imageRes.data.size > 0) {
              item.imageUrl = URL.createObjectURL(imageRes.data);
            }
          } catch (err) {
            
          }
        }));
      }
    }));

    orders.value = rawOrders
    
  } catch (e) {
    console.error("加载订单失败", e)
  } finally {
    loading.value = false
  }
}

// 添加图片错误处理函数
const handleImageError = (e) => {
  e.target.src = defaultImg;
  e.target.onerror = null; 
};

// --- 状态字典工具 ---
// 订单/商品状态映射表 
const STATUS_MAP = {

  'PENDING': { text: '待付款', class: 'tag-orange' },
  'PAID': { text: '待发货', class: 'tag-blue' }, // 已付款但未发货
  'SHIPPED': { text: '待收货', class: 'tag-purple' },
  'RECEIVED': { text: '已完成', class: 'tag-green' },
  'COMPLETED': { text: '已完成', class: 'tag-green' },
  'CANCELLED': { text: '已取消', class: 'tag-gray' },
  'REFUND_REQUESTED': { text: '退款审核中', class: 'tag-red' },
  'REFUNDED': { text: '已退款', class: 'tag-gray' },
  'REVIEWED': { text: '已评价', class: 'tag-green' }
}

// 获取主订单状态 
const getComputedStatus = (order) => {
  // 优先处理特殊状态
  if (order.status && order.status.toUpperCase() === 'PENDING') return STATUS_MAP['PENDING'];
  if (order.status && order.status.toUpperCase() === 'CANCELLED') return STATUS_MAP['CANCELLED'];

  // 如果有子项，检查子项状态来决定主状态
  if (order.orderItems && order.orderItems.length > 0) {
    const items = order.orderItems;
    // 如果全部退款
    if (items.every(i => i.status === 'REFUNDED')) return { text: '全额退款', class: 'tag-gray' };
    // 如果有部分退款
    if (items.some(i => i.status === 'REFUNDED')) return { text: '部分退款', class: 'tag-orange' };
    // 如果还没发货
    if (items.some(i => i.status === 'PAID')) return STATUS_MAP['PAID'];
    // 如果发货了
    if (items.some(i => i.status === 'SHIPPED')) return STATUS_MAP['SHIPPED'];
  }
  
  // 直接映射后端状态，转大写匹配
  const key = order.status ? order.status.toUpperCase() : '';
  return STATUS_MAP[key] || { text: key, class: 'tag-default' };
}

// 获取单品状态 (用于列表内部显示)
const getItemStatus = (item, order) => {

  // 如果主订单是已取消，那么子项也强制显示已取消
  if (order && order.status && order.status.toUpperCase() === 'CANCELLED') {
    return STATUS_MAP['CANCELLED'];
  }

  // 优先显示退款状态
  if (item.refundStatus && item.refundStatus !== 'REJECTED') {
     const refundMap = {
       'REQUESTED': STATUS_MAP['REFUND_REQUESTED'],
       'APPROVED': STATUS_MAP['REFUNDED'],
       'COMPLETED': STATUS_MAP['REFUNDED'],
     };
     return refundMap[item.refundStatus] || STATUS_MAP['REFUNDED'];
  }
  // 其次显示评价状态
  if (item.isReviewed) return STATUS_MAP['REVIEWED'];
  
  // 最后显示常规状态
  const key = item.status ? item.status.toUpperCase() : '';
  return STATUS_MAP[key] || { text: key, class: 'tag-default' };
}


// 计算主订单显示的颜色样式
const getComputedStatusClass = (order) => {
  if (order.status === 'pending') return 'status-pending'; // 橙色
  if (order.status === 'cancelled') return 'status-cancelled'; // 红色

  if (order.orderItems && order.orderItems.length > 0) {
    const allRefunded = order.orderItems.every(item => item.status === 'REFUNDED');
    if (allRefunded) return 'status-cancelled'; // 红色（全退了）
    
    const hasRefunded = order.orderItems.some(item => item.status === 'REFUNDED');
    if (hasRefunded) return 'status-warning'; // 黄色/橙色（部分退）
  }

  return 'status-completed'; // 绿色（正常）
}

// 辅助函数
const formatTime = (t) => t ? new Date(t).toLocaleString() : ''
const getStatusText = (s) => ({ 'pending': '待支付', 'completed': '已完成', 'cancelled': '已取消' }[s] || s)
const getStatusClass = (s) => `status-${s}`

const getItemStatusClass = (s) => ['SHIPPED', 'RECEIVED'].includes(s) ? 'text-green' : (['REFUNDED', 'CANCELLED'].includes(s) ? 'text-red' : 'text-gray')

// --- 动作逻辑 ---

const cancelOrder = async (orderId) => {
  if(!confirm('确定取消订单？')) return
  try {
    await axios.put(`/orders/${orderId}/cancel`)
    loadOrders()
  } catch(e) { alert('取消失败') }
}

const goToPay = (orderId) => {
  router.push(`/orders/${orderId}`)
}

// 单品逻辑
const confirmOrderItem = async (orderId, item) => {
  if(!confirm(`确认收到 ${item.productName}？`)) return
  try {
    await axios.put(`/orders/${orderId}/items/${item.itemId}/confirm`)
    loadOrders()
  } catch(e) { console.error(e) }
}

const shipOrderItem = async (orderId, item) => {
  if(!confirm(`确认发货 ${item.productName}？`)) return
  try {
    await axios.put(`/orders/${orderId}/items/${item.itemId}/ship`)
    loadOrders()
  } catch(e) { alert('操作失败') }
}

// 评价
const openReviewModal = (orderId, item) => {
  currentOrderId.value = orderId
  currentItem.value = item
  reviewForm.value = { rating: 5, content: '', isAnonymous: false }
  showReviewModal.value = true
}
const submitReview = async () => {
  try {
    await axios.post(`/orders/${currentOrderId.value}/items/${currentItem.value.itemId}/review`, {
      productId: currentItem.value.productId, ...reviewForm.value
    })
    showReviewModal.value = false
    loadOrders()
  } catch(e) { alert('评价失败') }
}

// 申请退款
const openRefundModal = (orderId, item) => {
  currentOrderId.value = orderId
  currentItem.value = item
  refundReason.value = ''
  showRefundModal.value = true
}
const submitRefund = async () => {
  try {
    await axios.put(`/orders/${currentOrderId.value}/items/${currentItem.value.itemId}/refund/apply`, null, { params: { reason: refundReason.value }})
    showRefundModal.value = false
    loadOrders()
  } catch(e) { alert('申请失败') }
}

// 审核退款
const openRefundAuditModal = (orderId, item) => {
  currentOrderId.value = orderId
  currentItem.value = item
  rejectRefundReason.value = ''
  showRefundAuditModal.value = true
}
const auditRefund = async (approve) => {
  try {
    await axios.put(`/orders/${currentOrderId.value}/items/${currentItem.value.itemId}/refund/review`, null, { params: { approve, rejectReason: rejectRefundReason.value }})
    showRefundAuditModal.value = false
    loadOrders()
  } catch(e) { alert('操作失败') }
}

// 打开查看评价弹窗
const openViewReviewModal = async (itemId) => {
  currentReviewDetail.value = null // 先清空，避免显示上一次的数据
  showViewReviewModal.value = true // 先打开弹窗显示加载中
  
  try {

    const res = await axios.get(`/orders/items/${itemId}/review`)
    console.log('评价数据', res.data);
    currentReviewDetail.value = res.data
  } catch (e) {
    console.error(e)
    alert('获取评价详情失败')
    showViewReviewModal.value = false
  }
}

</script>

<style scoped>
/* --- 页面容器 --- */
.my-orders-container {
  max-width: 1000px;
  margin: 0 auto;
  padding: 20px;
  /*  background-color: #f4f6f8; 整个页面的浅灰背景 */
  min-height: 100vh;
}

.page-header h2 {
  font-size: 20px;
  font-weight: 600;
  color: #333;
  margin-bottom: 20px;
  border-left: 4px solid #52c41a;
  padding-left: 12px;
}

/* --- 空状态 & 加载 --- */
.empty-state {
  text-align: center;
  padding: 60px 0;
  color: #999;
}
.empty-state img { width: 80px; margin-bottom: 10px; opacity: 0.5; }

/* --- 订单卡片核心样式 --- */
.order-card {
  background: #fff;
  border-radius: 12px; /* 圆角大一点，更现代 */
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
  margin-bottom: 20px;
  overflow: hidden; /* 防止子元素溢出圆角 */
  transition: transform 0.2s;
}
.order-card:hover {
  box-shadow: 0 6px 16px rgba(0,0,0,0.08);
}

/* 卡片头部 */
.card-header {
  background-color: #f9f9f9;
  padding: 12px 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #eee;
  font-size: 13px;
  color: #666;
}
.header-left .order-time { font-weight: 500; }
.header-left .divider { margin: 0 10px; color: #ddd; }

/* 状态标签样式体系 */
.status-tag { font-weight: bold; font-size: 14px; }
.tag-orange { color: #faad14; }
.tag-blue { color: #1890ff; }
.tag-purple { color: #722ed1; }
.tag-green { color: #52c41a; }
.tag-gray { color: #999; }
.tag-red { color: #ff4d4f; font-size: 12px; }
.tag-green { color: #52c41a; font-size: 12px; }

/* 商品列表 */
.card-body {
  padding: 0 20px;
}
.product-item {
  display: flex;
  padding: 16px 0;
  border-bottom: 1px dashed #eee;
}
.product-item:last-child { border-bottom: none; }

.product-image {
  width: 70px;
  height: 70px;
  background: #f0f0f0; /* 图片加载前的背景色 */
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
  flex-shrink: 0;
  overflow: hidden; /* 关键：超出圆角部分隐藏 */
  border: 1px solid #eee; /* 加个细边框更有质感 */
}

.product-image img {
  width: 100%;
  height: 100%;
  object-fit: cover; /* 关键：保持比例填充，裁切多余部分 */
  display: block;
}


.product-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 6px;
}
.info-row-main {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}
.product-name {
  font-size: 15px;
  color: #333;
  font-weight: 500;
  line-height: 1.4;
}
.product-price {
  font-size: 15px;
  color: #333;
}
.info-row-sub {
  font-size: 12px;
  color: #999;
  display: flex;
  gap: 10px;
}
.item-status-text { margin-left: 10px; }

.status-text-gray {
  font-size: 12px;
  color: #999;
  margin-right: 5px;
}

/* 单品操作按钮区 */
.product-actions {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  justify-content: center;
  gap: 8px;
  margin-left: 20px;
  min-width: 80px;
}

/* --- 按钮样式库 --- */

/* 评价按钮 - 空心橙色胶囊 */
.product-actions .btn-text {
  border: 1px solid #faad14; /* 橙色边框 */
  color: #faad14;            /* 橙色文字 */
  background: white;
  padding: 4px 12px;         /* 增加内边距 */
  border-radius: 14px;       /* 胶囊形状 */
  font-size: 12px;
  cursor: pointer;
  transition: all 0.2s;
  text-decoration: none;     /* 去掉下划线 */
}

.product-actions .btn-text:hover {
  background: #fffbe6;       /* 悬停变淡橙色背景 */
  color: #d48806;
  border-color: #d48806;
}

/* 申请售后按钮 - 灰色小字 (保持低调但可点击) */
.product-actions .btn-text.gray {
  border: 1px solid #d9d9d9; /* 浅灰边框 */
  color: #666;               /* 深灰文字 */
  background: white;
  margin-top: 6px;           /* 和上面的按钮拉开点距离 */
}

.product-actions .btn-text.gray:hover {
  color: #333;
  border-color: #999;
  background: #f5f5f5;
}

/* 确认收货按钮 (如果有的话) - 保持醒目的空心绿色 */
.btn-outline-primary.small {
  border: 1px solid #52c41a;
  color: #52c41a;
  background: white;
  padding: 4px 12px;
  border-radius: 14px;
}
.btn-outline-primary.small:hover {
  background: #f6ffed;
}

/* 3. 卡片底部 */
.card-footer {
  padding: 12px 20px;
  border-top: 1px solid #f0f0f0;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.total-price-section {
  font-size: 14px;
  color: #333;
}
.price-large {
  font-size: 20px;
  color: #ff4d4f; /* 价格红 */
  font-weight: bold;
  font-family: Arial, sans-serif;
  margin-left: 4px;
}
.main-actions {
  display: flex;
  gap: 10px;
}

/* --- 按钮样式库 --- */
/* 文字按钮 */
.btn-text {
  background: none;
  border: none;
  cursor: pointer;
  color: #666;
  font-size: 13px;
}
.btn-text:hover { color: #1890ff; text-decoration: underline; }
.btn-text.gray:hover { color: #333; }

/* 实心主按钮 */
.btn-primary {
  background: linear-gradient(135deg, #52c41a, #73d13d);
  color: white;
  border: none;
  padding: 8px 20px;
  border-radius: 20px; /* 胶囊按钮 */
  font-size: 14px;
  cursor: pointer;
  box-shadow: 0 2px 6px rgba(82, 196, 26, 0.3);
  transition: all 0.2s;
}
.btn-primary:hover { transform: translateY(-1px); box-shadow: 0 4px 10px rgba(82, 196, 26, 0.4); }

.btn-primary.small,
.btn-warning.small {
  padding: 4px 12px;
  font-size: 12px;
  border-radius: 14px;
  min-width: 60px; /* 保证按钮别太窄 */
}

/* 空心按钮 */
.btn-outline-primary {
  background: white;
  border: 1px solid #52c41a;
  color: #52c41a;
  padding: 4px 12px;
  border-radius: 15px;
  font-size: 12px;
  cursor: pointer;
}
.btn-outline-primary:hover { background: #f6ffed; }

.btn-warning {
  background: #faad14;
  color: white;
  border: none;
  padding: 4px 12px;
  border-radius: 15px;
  font-size: 12px;
  cursor: pointer;
}

/* 弹窗样式*/
.modal-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.5); display: flex; justify-content: center; align-items: center; z-index: 1000; }
.modal-container { background: white; padding: 24px; border-radius: 12px; width: 420px; max-width: 90%; box-shadow: 0 10px 30px rgba(0,0,0,0.1); }
.modal-container h3 { margin-top: 0; margin-bottom: 20px; text-align: center; color: #333; }
.modal-form-group { margin-bottom: 16px; }
.modal-form-group label { display: block; margin-bottom: 8px; font-weight: 500; }
.modal-form-group textarea { width: 100%; height: 100px; padding: 10px; border: 1px solid #ddd; border-radius: 6px; resize: none; }
.form-actions { display: flex; gap: 12px; justify-content: flex-end; margin-top: 20px; }
.submit-btn { padding: 8px 20px; background: #52c41a; color: white; border: none; border-radius: 6px; cursor: pointer; font-weight: 500; }
.cancel-btn { padding: 8px 20px; background: #f5f5f5; color: #666; border: none; border-radius: 6px; cursor: pointer; }
.rating-input span { font-size: 28px; color: #e0e0e0; cursor: pointer; margin-right: 4px; transition: color 0.2s; }
.rating-input span.active { color: #fadb14; }
</style>

/* --- 评价相关--- */
<style scoped>
/* =====================  评价弹窗 · 清爽卡片风  ===================== */
.my-review-modal-content {
  background: #ffffff;
  border-radius: 16px;
  padding: 32px 36px;
  width: 480px;
  max-width: 90vw;
  margin: auto;
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.15);
  font-family: "PingFang SC", "Helvetica Neue", Arial, sans-serif;
}

/* 统一行间距 */
.detail-row {
  display: flex;
  margin-bottom: 28px;
  align-items: flex-start;
}
.detail-row:last-child {
  margin-bottom: 0;
}

/* 左侧标签 */
.detail-label {
  width: 76px;
  text-align: right;
  font-weight: 600;
  font-size: 15px;
  color: #8a8f9e;
  margin-right: 20px;
  flex-shrink: 0;
  line-height: 1.4;
}

/* 评分星星 */
.rating-display {
  display: flex;
  align-items: center;
  font-size: 22px;
  color: #e0e0e0;
  letter-spacing: 2px;
}
.rating-display .active {
  color: #ffd43b; /* 更温暖的金黄 */
  text-shadow: 0 0 2px rgba(255, 180, 0, 0.35);
}
.rating-text {
  margin-left: 12px;
  font-size: 15px;
  color: #606266;
  font-weight: 500;
}

/* 评价内容框 */
.review-text-box {
  position: relative;
  background: #f7f9fc;
  padding: 20px 24px 20px 36px;
  border-radius: 12px;
  font-size: 15px;
  line-height: 1.8;
  color: #303133;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.03);
  border: 1px solid #e6e9f0;
}
/* 装饰引号 */
.review-text-box::before {
  content: '"';
  position: absolute;
  left: 12px;
  top: 6px;
  font-size: 48px;
  color: #d9dfe9;
  font-family: Georgia, "Times New Roman", serif;
  line-height: 1;
}

/* 时间 */
.time-text {
  font-size: 13px;
  color: #a2a7b5;
  font-family: "Helvetica Neue", Arial, sans-serif;
  margin-top: 2px;
}

/* 关闭按钮区域（可选） */
.modal-actions {
  margin-top: 32px;
  text-align: right;
}
.modal-actions button {
  border: none;
  background: #f2f4f8;
  color: #606266;
  padding: 8px 20px;
  border-radius: 20px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
}
.modal-actions button:hover {
  background: #e0e4eb;
  color: #303133;
}
</style>