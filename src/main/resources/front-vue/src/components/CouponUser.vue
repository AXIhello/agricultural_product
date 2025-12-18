<template>
  <div class="my-coupon-container">

    <!-- 顶部状态切换 -->
    <div class="coupon-stat-bar">
      <div
          v-for="item in statusTabs"
          :key="item.key"
          class="stat-item"
          :class="{ active: statusFilter === item.key }"
          @click="changeStatus(item.key)"
      >
        {{ item.label }} {{ getStatCount(item.key) }}
      </div>
    </div>

    <!-- 状态 -->
    <div v-if="loading" class="empty-state">加载中...</div>
    <div v-else-if="coupons.length === 0" class="empty-state">
      暂无优惠券
    </div>

    <!-- 优惠券卡片 -->
    <div
        v-for="coupon in coupons"
        :key="coupon.userCouponId"
        class="coupon-card"
        :class="[
        coupon.status,
        selectable && selectedId === coupon.userCouponId ? 'selected' : ''
      ]"
        @click="handleSelect(coupon)"
    >
      <div class="coupon-left">
        <div class="amount">{{ formatAmount(coupon) }}</div>
        <div class="condition">满 {{ coupon.minOrderAmount }} 可用</div>
      </div>

      <div class="coupon-right">
        <h4 class="coupon-name">{{ coupon.templateName }}</h4>

<!--        <div class="info-row">-->
<!--          <span class="label">类型：</span>-->
<!--          <span class="value">{{ formatType(coupon.couponType) }}</span>-->
<!--        </div>-->

        <div class="info-row">
<!--          <span class="label">有效期：</span>-->
          <span class="value">
          {{ formatTime(coupon.validEnd) }}到期
          </span>
        </div>

        <div class="info-row">
          <span class="label">状态：</span>
          <span class="value" :class="statusColor(coupon.status)">
            {{ formatStatus(coupon.status) }}
          </span>
        </div>
      </div>

      <!-- 选择标记 -->
      <div v-if="selectable && selectedId === coupon.userCouponId" class="check-mark">✔</div>
    </div>

  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from '@/utils/axios'

/* ---------- props / emits ---------- */
const props = defineProps({
  selectable: {
    type: Boolean,
    default: false
  },
  selectedId: {
    type: Number,
    default: null
  }
})

const emit = defineEmits(['select'])

/* ---------- 状态 ---------- */
const coupons = ref([])
const loading = ref(false)
const statusFilter = ref('all')

const statistics = ref({
  total: 0,
  unused: 0,
  used: 0,
  expired: 0
})

const getStatCount = (key) => {
  if (key === 'all') {
    return statistics.value.total
  }
  return statistics.value[key] || 0
}


const statusTabs = [
  { key: 'all', label: '全部' },
  { key: 'unused', label: '未使用' },
  { key: 'used', label: '已使用' },
  { key: 'expired', label: '已过期' }
]

/* ---------- 接口 ---------- */
const fetchCoupons = async () => {
  loading.value = true
  try {
    const res = await axios.get('/coupons/my', {
      params: {
        status: statusFilter.value,
        pageNum: 1,
        pageSize: 50
      }
    })
    coupons.value = res.data.data.records || []
    console.log('优惠券：',coupons.value)
  } finally {
    loading.value = false
  }
}

const fetchStatistics = async () => {
  try {
    const res = await axios.get('/coupons/statistics')
    const data = res.data.data || {}
    console.log('优惠券类型加数量：',data)

    statistics.value.total = Number(data.total || 0)
    statistics.value.unused = Number(data.unused || 0)
    statistics.value.used = Number(data.used || 0)
    statistics.value.expired = Number(data.expired || 0)
  } catch (e) {
    console.error('获取统计失败', e)
  }
}


/* ---------- 交互 ---------- */
const changeStatus = (status) => {
  statusFilter.value = status
  fetchCoupons()
}

const handleSelect = (coupon) => {
  if (!props.selectable) return
  if (coupon.status !== 'unused') return
  emit('select', coupon)
}

/* ---------- 格式化 ---------- */
const formatType = (type) => ({
  full_reduction: '满减',
  discount: '折扣',
  fixed_amount: '固定金额',
  free_shipping: '免运费'
}[type] || type)

const formatStatus = (status) => ({
  unused: '未使用',
  used: '已使用',
  expired: '已过期',
  locked: '已锁定'
}[status] || status)

const statusColor = (status) => ({
  unused: 'status-unused',
  used: 'status-used',
  expired: 'status-expired'
}[status])

const formatAmount = (coupon) => {
  if (coupon.couponType === 'discount') {
    return `${coupon.discountValue * 10} 折`
  }
  if (coupon.couponType === 'free_shipping') {
    return '免邮'
  }
  return `¥${coupon.discountValue}`
}

function formatTime(time) {
  return time ? time.replace('T', ' ') : '';
}


/* ---------- 生命周期 ---------- */
onMounted(() => {
  fetchStatistics()
  fetchCoupons()
})
</script>

<style scoped>
.my-coupon-container {
  padding: 16px;
  margin: 0 auto;
}

/* 状态栏 - 无边框，无分隔线，居中 */
.coupon-stat-bar {
  display: flex;
  justify-content: center; /* 居中排列 */
  align-items: center;
  font-size: 14px;
  color: #333;
  gap: 32px; /* 文字之间的间距 */
  margin-bottom: 16px;
  padding: 0;
}

.stat-item {
  padding: 0;
  cursor: pointer;
  user-select: none;
}

/* 激活状态 */
.stat-item.active {
  color: #409eff;
  font-weight: 500;
}



/* 空 */
.empty-state {
  text-align: center;
  padding: 40px;
  color: #999;
}

/* 卡片 */
.coupon-card {
  position: relative;
  display: flex;
  background: #fff;
  border-radius: 10px;
  padding: 14px;
  margin-bottom: 14px;
  cursor: pointer;
  transition: all 0.2s;
  box-shadow: 0 2px 8px rgba(0,0,0,.06);
}

.coupon-card:hover {
  transform: translateY(-2px);
}

.coupon-card.used,
.coupon-card.expired {
  opacity: 0.6;
  cursor: not-allowed;
}

.coupon-card.selected {
  border: 2px solid #409eff;
}

/* 左 */
.coupon-left {
  width: 110px;
  text-align: center;
  border-right: 1px dashed #ddd;
  padding-right: 12px;
}

.amount {
  font-size: 24px;
  font-weight: bold;
  color: #ff4d4f;
}

.condition {
  font-size: 12px;
  color: #666;
}

/* 右 */
.coupon-right {
  flex: 1;
  padding-left: 14px;
}

.coupon-name {
  margin: 0 0 6px;
}

.info-row {
  font-size: 12px;
  margin-bottom: 4px;
}

/* 状态色 */
.status-unused { color: #67c23a; }
.status-used { color: #909399; }
.status-expired { color: #f56c6c; }

/* 选中标记 */
.check-mark {
  position: absolute;
  top: 8px;
  right: 10px;
  color: #409eff;
  font-size: 18px;
}
</style>
