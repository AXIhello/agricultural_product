<template>
  <div class="recommend-panel">
    <h3>{{ title }}</h3>
    <!-- 加载 -->
    <div v-if="loading" class="loading">加载中...</div>
    <!-- 列表 -->
    <div v-else class="card-list">
      <div v-for="p in products" :key="p.productId" class="card hover-card" @click="goDetail(p)">
        <img
            :src="p.imageUrl"
            @error="handleImageError"
            alt="商品图"
            class="hover-img"
            />
        <div class="info">
          <div class="name">{{ p.productName }}</div>
          <div class="price">¥{{ p.price }}</div>
          <div class="rating">
            <Stars :rating="p.averageRating" />
            <span>{{ p.averageRating?.toFixed(1) }}（{{ p.ratingCount }}）</span>
          </div>
          <div class="reason">{{ p.recommendReason }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getRecommend, getHotProducts, getHighRated } from '@/api/product'
import Stars from '@/components/Stars.vue' // 简易星级组件
import defaultImg from '@/assets/img.png'
import axios from '../utils/axios' 

const props = defineProps({
  type: { type: String, default: 'recommend' }, // recommend | hot | high
  category: { type: String, default: '' },
  pageSize: { type: Number, default: 10 }
})

const products = ref([])
const loading = ref(true)
const titleMap = {
  recommend: '智能推荐',
  hot: '热销榜',
  high: '高分榜'
}
const title = titleMap[props.type]

const list = ref([])

onMounted(async () => {
  loading.value = true
  try {
    const res = await getRecommend(1, 8)          // 取 8 条
    const records = res.data.records || []

    // 并发获取图片
    await Promise.all(
      records.map(async (p) => {
        // 先给默认值，防止空白
        p.imageUrl = defaultImg

        // 后端没有图直接跳过
        if (p.hasImage === false) return

        try {
          const imageRes = await axios.get(`/products/${p.productId}/image`, {
            responseType: 'blob',
            validateStatus: s => (s >= 200 && s < 300) || s === 404
          })
          if (imageRes.status === 200 && imageRes.data && imageRes.data.size > 0) {
            p.imageUrl = URL.createObjectURL(imageRes.data)
          }
        } catch (err) {
          // 任何异常都保持 defaultImg
          console.warn('推荐商品图片加载失败', p.productId, err)
        }
      })
    )

    products.value = records
  } finally {
    loading.value = false
  }
})

const handleImageError = (e) => {
  e.target.src = defaultImg
  e.target.onerror = null
}

// 跳转详情
import router from '@/router'
function goDetail(p) {
  if (p?.productId) router.push(`/product/${p.productId}`)
}
</script>

<style scoped>
.recommend-panel {
  padding: 16px;
  background: #f4f6f8;
}
.card-list {
  display: flex;
  gap: 12px;
  overflow-x: auto;
}
.card {
  flex: 0 0 160px;
  background: #fff;
  border-radius: 8px;
  padding: 10px;
  box-shadow: 0 2px 6px rgba(0,0,0,0.06);
}
.card img {
  width: 100%;
  height: 100px;
  object-fit: cover;
  border-radius: 6px;
}
.info {
  margin-top: 8px;
}
.name {
  font-size: 14px;
  color: #333;
}
.price {
  color: #ff4d4f;
  font-weight: 600;
}
.rating {
  font-size: 12px;
  color: #666;
  display: flex;
  align-items: center;
  gap: 4px;
}
.reason {
  font-size: 12px;
  color: #52c41a;
  margin-top: 4px;
}
.loading {
  text-align: center;
  color: #999;
}

/*悬停动态*/
.hover-card {
  cursor: pointer;
  transition: transform 0.25s ease, box-shadow 0.25s ease;
}
.hover-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.12);
}
.hover-img {
  transition: transform 0.25s ease;
}
.hover-card:hover .hover-img {
  transform: scale(1.08); /* 图片略变大 */
}
</style>