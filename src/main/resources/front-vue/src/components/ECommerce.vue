<template>
  <div class="ecommerce-container">
    <div class="product" @click="goToProductDetail(product)" v-for="product in products" :key="product.id">
      <p v-if="!products.length" class="empty-state">还没有产品哦~</p>
      <img :src="product.imageUrl" alt="农产品图片" class="product-image" />

      <h4 class="product-name">{{ product.productName }}</h4>
      <p class="product-description">{{ product.description }}</p>
      <p class="product-price">￥{{ product.price }} /{{ product.unitInfo }}</p>
    </div>
  </div>
</template>


<script setup>
import { ref, onMounted } from "vue";
import { useAuthStore } from "@/stores/authStore";
import axios from "@/utils/axios";
import router from "@/router";
import defaultImg from '@/assets/img.png'

const { userInfo, role, isLoggedIn } = useAuthStore();
const products = ref([]);


async function loadProducts() {
  try {
    const res = await axios.get('/products')
    const records = res.data.records || []

    //并行加载每个产品的图片
    products.value = await Promise.all(
        records.map(async (product) => {
          // 1. 默认设置
          let imageUrl = defaultImg;
          // 2. 如果后端说有图，尝试加载
          if (product.hasImage === true) {
            try {
              const imageRes = await axios.get(`/products/${product.productId}/image`, {
                responseType: 'blob'
              });
              if (imageRes.data && imageRes.data.size > 0) {
                imageUrl = URL.createObjectURL(imageRes.data);
              }
            } catch (err) {
              console.warn('产品图片加载失败，使用默认图：', product.productId, err);
              imageUrl = defaultImg;
            }
          }
          return { ...product, imageUrl };
        })
    );
    products.value = products.value.slice(0, 4);
  } catch (err) {
    console.error('加载产品失败', err)
  }
}
// 只保留前 4 个

async function goToProductDetail(product) {
  if (product?.productId) router.push(`/product/${product.productId}`);
}

onMounted(loadProducts);
</script>

<style scoped>
.ecommerce-container {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 22px;
}

.product {
  background-color: #f8fbf8;
  padding: 16px;
  border-radius: 16px;
  border: 1px solid #d9e8df;
  box-shadow: 0 3px 8px rgba(0, 0, 0, 0.06);
  transition: all 0.3s ease;
}

/* 悬浮动效 */
.product:hover {
  transform: translateY(-4px);
  border-color: #8bc3a1;
  box-shadow: 0 8px 14px rgba(0, 0, 0, 0.12);
}

/* 图片统一化 */
.product-image {
  width: 100%;
  height: 140px;
  object-fit: cover;
  border-radius: 12px;
  margin-bottom: 12px;
}

/* 标题 */
.product-name {
  font-size: 16px;
  font-weight: 600;
  color: #2d7d4f;
  margin-bottom: 6px;
}

/* 描述文本 */
.product-description {
  font-size: 14px;
  color: #666;
  margin-bottom: 6px;
}

/* 价格 */
.product-price {
  font-size: 15px;
  font-weight: bold;
  color: #2d7d4f;
  margin-bottom: 12px;
}

/* 按钮 */
.buy-btn {
  width: 100%;
  padding: 8px 0;
  border: none;
  font-size: 14px;
  border-radius: 8px;
  background-color: #2d7d4f;
  color: white;
  cursor: pointer;
  transition: background-color 0.25s ease;
}

/* 按钮悬停 */
.buy-btn:hover {
  background-color: #256a41;
}
</style>
