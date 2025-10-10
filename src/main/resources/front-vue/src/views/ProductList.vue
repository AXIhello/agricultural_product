<template>
  <div class="product-container">
    <div class="search-bar">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索商品"
        class="search-input"
        @keyup.enter="handleSearch"
      >
        <template #append>
          <el-button @click="handleSearch">搜索</el-button>
        </template>
      </el-input>
    </div>

    <div class="product-grid">
      <div v-for="product in products" :key="product.productId" class="product-card">
        <div class="product-image">
          <img src="https://via.placeholder.com/200x200" alt="商品图片">
        </div>
        <div class="product-info">
          <h3>{{ product.productName }}</h3>
          <p class="price">¥{{ product.price }}</p>
          <p class="stock">库存: {{ product.stock }}</p>
          <el-button type="primary" @click="viewDetail(product.productId)">查看详情</el-button>
        </div>
      </div>
    </div>

    <div class="pagination">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[8, 16, 24, 32]"
        layout="total, sizes, prev, pager, next"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'  
import axios from '../utils/axios'

const router = useRouter()
const products = ref([])
const currentPage = ref(1)
const pageSize = ref(8)
const total = ref(0)
const searchKeyword = ref('')

// 获取商品列表
const loadProducts = async () => {
  try {
    const response = await axios.get('/products', {
      params: {
        pageNum: currentPage.value,
        pageSize: pageSize.value
      }
    })
    products.value = response.data.records
    total.value = response.data.total
  } catch (error) {
    console.error('获取商品列表失败:', error)
    ElMessage.error('获取商品列表失败')
  }
}

// 搜索商品
const handleSearch = async () => {
  if (!searchKeyword.value.trim()) {
    await loadProducts()
    return
  }
  try {
    const response = await axios.get('/products/search', {
      params: {
        keyword: searchKeyword.value,
        pageNum: currentPage.value,
        pageSize: pageSize.value
      }
    })
    products.value = response.data.records
    total.value = response.data.total
  } catch (error) {
    console.error('搜索商品失败:', error)
    ElMessage.error('搜索商品失败')
  }
}

// 查看商品详情
const viewDetail = (productId) => {
  router.push(`/products/${productId}`)
}

// 处理页码变化
const handleCurrentChange = async (val) => {
  currentPage.value = val
  if (searchKeyword.value) {
    await handleSearch()
  } else {
    await loadProducts()
  }
}

// 处理每页显示数量变化
const handleSizeChange = async (val) => {
  pageSize.value = val
  if (searchKeyword.value) {
    await handleSearch()
  } else {
    await loadProducts()
  }
}

onMounted(() => {
  loadProducts()
})
</script>

<style scoped>
.product-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.search-bar {
  margin-bottom: 20px;
}

.search-input {
  width: 300px;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 20px;
  margin-bottom: 20px;
}

.product-card {
  border: 1px solid #eee;
  border-radius: 8px;
  padding: 10px;
  background: white;
  transition: transform 0.2s, box-shadow 0.2s;
}

.product-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.product-image img {
  width: 100%;
  height: 200px;
  object-fit: cover;
  border-radius: 4px;
}

.product-info {
  padding: 10px;
}

.product-info h3 {
  margin: 0;
  font-size: 1.1em;
  color: #333;
}

.price {
  color: #f56c6c;
  font-size: 1.2em;
  font-weight: bold;
  margin: 10px 0;
}

.stock {
  color: #909399;
  font-size: 0.9em;
  margin-bottom: 10px;
}

.pagination {
  margin-top: 20px;
  text-align: center;
}
</style>