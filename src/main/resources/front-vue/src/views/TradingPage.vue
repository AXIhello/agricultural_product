<template>
  <div class="main-bg">
    <!-- 顶部主导航栏 -->
    <header class="header">
      <h1>农产品交易平台</h1>
      <nav>
        <ul>
          <li><router-link to="/main">首页</router-link></li>
          <li><router-link to="/finance">融资服务</router-link></li>
          <li><router-link to="/expert">专家助力</router-link></li>
          <li><router-link to="/trading">农产品交易</router-link></li>
          <li><router-link to="/profile">个人信息</router-link></li>
        </ul>
      </nav>
    </header>

    <!-- 主内容 -->
    <section class="content">
      <p class="welcome-text">欢迎您 {{ userName }}！</p>

      <!-- 农户视图 -->
      <div v-if="role === 'farmer'" class="farmer-view-container">
        <nav class="farmer-nav">
          <button @click="switchView('myProducts')" :class="{ active: currentView === 'myProducts' }">
            我的农产品
          </button>
          <button @click="switchView('addProduct')" :class="{ active: currentView === 'addProduct' }">
            上架商品
          </button>
        </nav>

        <div class="view-content-wrapper">
          <!-- 我的商品 -->
          <div v-if="currentView === 'myProducts'">
            <h2>我的农产品货架</h2>
            <div class="product-list">
              <div v-for="product in myProducts" :key="product.id" class="product-card">
                <h3>{{ product.name }}</h3>
                <p>价格: ¥{{ product.price }} / {{ product.unit }}</p>
                <p>库存: {{ product.stock }} {{ product.unit }}</p>
                <div class="card-actions">
                  <button class="edit-btn">编辑</button>
                  <button class="delete-btn">下架</button>
                </div>
              </div>
              <p v-if="!myProducts.length" class="empty-state">您还没有发布任何产品，快去“添加商品”吧！</p>
            </div>
          </div>

          <!-- 添加商品 -->
          <div v-if="currentView === 'addProduct'">
            <h2>发布新商品</h2>
            <form @submit.prevent="handleAddProduct" class="add-product-form">
              <div class="form-group">
                <label for="name">商品名称:</label>
                <input type="text" id="name" v-model="newProduct.name" required>
              </div>
              <div class="form-group">
                <label for="price">价格 (元):</label>
                <input type="number" id="price" v-model.number="newProduct.price" required min="0.01" step="0.01">
              </div>
              <div class="form-group">
                <label for="unit">单位:</label>
                <input type="text" id="unit" v-model="newProduct.unit" placeholder="例如: 斤, 个, 箱" required>
              </div>
              <div class="form-group">
                <label for="stock">库存量:</label>
                <input type="number" id="stock" v-model.number="newProduct.stock" required min="1">
              </div>
              <button type="submit" class="submit-btn">确认发布</button>
            </form>
          </div>
        </div>
      </div>

      <!-- 普通用户视图 -->
      <div v-else class="customer-view-container">
        <h2>农产品交易市场</h2>
        <nav class="customer-nav">
          <button @click="switchView('products')" :class="{ active: currentView === 'products' }">
            商品浏览
          </button>
          <button @click="switchView('cart')" :class="{ active: currentView === 'cart' }">
            我的购物车
          </button>
        </nav>

        <div class="view-content-wrapper">
          <!-- 商品浏览 -->
          <div v-if="currentView === 'products'">
            <h2>全部商品</h2>
            <div class="product-list">
              <div v-for="product in products" :key="product.id" class="product-card">
                <h3>{{ product.name }}</h3>
                <p>价格: ¥{{ product.price }} / {{ product.unit }}</p>
                <p>库存: {{ product.stock }} {{ product.unit }}</p>
                <div class="card-actions">
                  <button class="view-btn">查看详情</button>
                  <button class="add-to-cart-btn" @click="addToCart(product)">添加到购物车</button>
                </div>
              </div>
              <p v-if="!products.length" class="empty-state">还没有任何商品。</p>
            </div>
          </div>

          <!-- 购物车 -->
          <div v-if="currentView === 'cart'">
            <h2>我的购物车</h2>
            <div class="cart-item" v-for="item in cartItems" :key="item.id">
              <h3>{{ item.name }}</h3>
              <p>价格: ¥{{ item.price }} / {{ item.unit }}</p>
              <p>数量: {{ item.quantity }}</p>
              <button @click="removeFromCart(item.id)">移除</button>
            </div>
            <p v-if="!cartItems.length" class="empty-state">购物车是空的。</p>
            <button v-if="cartItems.length" class="submit-btn" @click="submitOrder">提交订单</button>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'

const role = ref(null)
const userName = ref('')
const currentView = ref('products')

// 农户数据
const myProducts = ref([])
const newProduct = ref({ name: '', price: null, unit: '', stock: null })

// 普通用户数据
const products = ref([])
const cartItems = ref([])

function switchView(view) {
  currentView.value = view
}

// 农户发布商品
async function handleAddProduct() {
  try {
    const res = await axios.post('/api/products/publish', newProduct.value)
    if (res.data) {
      alert('商品发布成功！')
      myProducts.value.push({ ...newProduct.value, id: Date.now() })
      newProduct.value = { name: '', price: null, unit: '', stock: null }
      currentView.value = 'myProducts'
    }
  } catch (err) {
    alert('发布失败')
  }
}

// 普通用户添加购物车
function addToCart(product) {
  const exist = cartItems.value.find(item => item.id === product.id)
  if (exist) {
    exist.quantity += 1
  } else {
    cartItems.value.push({ ...product, quantity: 1 })
  }
}

function removeFromCart(id) {
  cartItems.value = cartItems.value.filter(item => item.id !== id)
}

function submitOrder() {
  alert('订单提交成功！（这里可以对接订单接口）')
  cartItems.value = []
}

onMounted(async () => {
  // 模拟登录信息
  role.value = 'customer' // 改成 'farmer' 测试农户视图
  userName.value = '测试用户'

  // 加载商品列表
  try {
    const res = await axios.get('/api/products')
    products.value = res.data.records
  } catch (err) {
    console.error('获取商品失败', err)
  }
})
</script>


<style scoped>
.main-bg {
  display: flex;
  flex-direction: column;
  height: 100vh;
  width: 1800px;
  background-color: #F0F9F4; /* 浅绿色背景色 */
  border-radius: 8px;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

.header {
  width: 100%;
  background: #2D7D4F; /* 深绿色背景色 */
  color: white;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  height: 60px;
  font-size: 15px;
  font-weight: 600;
  border-radius: 8px;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

nav ul {
  list-style: none;
  display: flex;
  padding: 0;
  margin: 0;
}

nav li {
  margin-right: 50px;
}

nav a {
  text-decoration: none;
  color: white;
  font-weight: 600;
  font-size: 20px;
  transition: color 0.3s;
}

nav a:hover {
  color: #B7E4C7; /* 鼠标悬停时变为淡绿色 */
}

.content {
  width: 100%;
  flex: 1;
  padding: 20px;
  background: white;
  color: #333; /* 深灰色文字 */
  font-size: 18px;
  border-radius: 8px;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}
.welcome-text {
  font-size: 1.2rem;
  color: #333;
  margin-bottom: 20px;
}
.content h2 {
  color: #2D7D4F;
  font-weight: 700;
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 1px solid #eee;
}

.farmer-nav {
  display: flex;
  border-bottom: 2px solid #e0e0e0;
  margin-bottom: 25px;
}
.farmer-nav button {
  padding: 10px 20px;
  border: none;
  background-color: transparent;
  cursor: pointer;
  font-size: 1.1rem;
  font-weight: 500;
  color: #555;
  transition: color 0.3s, border-bottom-color 0.3s;
  border-bottom: 3px solid transparent;
  margin-bottom: -2px;
}
.farmer-nav button:hover { color: #2D7D4F; }
.farmer-nav button.active {
  color: #2D7D4F;
  border-bottom-color: #2D7D4F;
}

.customer-nav {
  display: flex;
  border-bottom: 2px solid #e0e0e0;
  margin-bottom: 25px;
}

.customer-nav button {
  padding: 10px 20px;
  border: none;
  background-color: transparent;
  cursor: pointer;
  font-size: 1.1rem;
  font-weight: 500;
  color: #555;
  transition: color 0.3s, border-bottom-color 0.3s;
  border-bottom: 3px solid transparent;
  margin-bottom: -2px;
}
.customer-nav button:hover { color: #2D7D4F; }
.customer-nav button.active {
  color: #2D7D4F;
  border-bottom-color: #2D7D4F;
}

.product-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
}
.product-card {
  border: 1px solid #e9e9e9;
  border-radius: 8px;
  padding: 20px;
  background-color: #fafafa;
  transition: box-shadow 0.3s;
}
.product-card:hover { box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1); }
.product-card h3 { margin-top: 0; color: #2D7D4F; }
.card-actions { margin-top: 15px; display: flex; gap: 10px; }


.add-product-form { margin: 0 auto;  display: flex ; flex-direction: column; align-content: center; max-width: 600px; margin-top: 20px; }
.form-group { display: flex; align-items: center; margin-bottom: 20px; }
.form-group label { width: 120px; font-weight: bold; }
.form-group input { flex: 1; padding: 10px; border: 1px solid #ccc; border-radius: 4px; }

.empty-state {
  grid-column: 1 / -1;
  text-align: center;
  color: #888;
  padding: 3rem;
}
</style>