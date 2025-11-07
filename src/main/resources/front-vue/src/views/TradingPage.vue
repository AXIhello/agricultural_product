<template>
  <div class="main-bg">
    <header class="header">
      <h1>农产品交易平台</h1>
      <nav>
        <ul>
          <li><router-link to="/main">首页</router-link></li>
          <li><router-link to="/finance">融资服务</router-link></li>
          <li><router-link to="/expert">专家助力</router-link></li>
          <li><router-link to="/trading" style="color: #B7E4C7;">农产品交易</router-link></li>
          <li><router-link to="/profile">个人信息</router-link></li>
        </ul>
      </nav>
    </header>

    <section class="content">
      <!-- 农户视图 -->
      <div v-if="role === 'farmer'" class="farmer-view-container">
        <nav class="farmer-nav">
          <button @click="switchView('products')" :class="{ active: currentView === 'products' }">所有商品</button>
          <button @click="switchView('demands')" :class="{ active: currentView === 'demands' }">求购需求</button>
          <button @click="switchView('myProducts')" :class="{ active: currentView === 'myProducts' }">我的商品</button>
          <button @click="switchView('addProduct')" :class="{ active: currentView === 'addProduct' }">上架商品</button>
          <button @click="switchView('myOrders')" :class="{ active: currentView === 'myOrders' }">我的订单</button>
        </nav>

        <div class="view-content-wrapper">
          <!-- 所有商品 -->
          <div v-if="currentView === 'products'">
            <div class="product-list">
              <div
                  v-for="product in products"
                  :key="product.productId"
                  class="product-card"
              >

                <h3>{{ product.productName }}</h3>
                <p>价格: ¥{{ product.price }} /Kg</p>
                <p>库存: {{ product.stock }} Kg</p>

                <div class="card-actions">
                  
                  <button class="view-btn" @click="goToProductDetail(product)">查看详情</button>
                  <button v-if="role !== 'farmer'" class="add-to-cart-btn" @click="addToCart(product)">
                    添加到购物车
                  </button>
                </div>
              </div>

              <p v-if="!products.length" class="empty-state">还没有任何商品。</p>
            </div>
          </div>

          <!-- 求购需求 -->
          <div v-if="currentView === 'demands'">
            <h2>求购需求</h2>
            <div class="request-list">
              <!-- 求购需求卡片 -->
              <div
                  v-for="request in demands"
                  :key="request.demandId"
                  class="request-card"
              >
                <p class="request-title"><strong>用户 {{ request.buyerId }}： 我想要  </strong> {{ request.productNameDesired }}</p>
                <p class="request-quantity">需求量： {{ request.quantityDesired }} {{ request.unitDesired }}</p>
                <p class="request-details">需求描述： {{ request.details }}</p>
              </div>


              <p v-if="!demands.length" class="empty-state">暂无求购需求</p>

              <button v-if="!demands.length" @click="switchView('addDemand')" class="add-demand-btn">发布求购需求</button>
            </div>
          </div>

          <!-- 我的商品 -->
          <div v-if="currentView === 'myProducts'">
            <div class="product-list">
              <div v-for="product in myProducts" :key="product.productId" class="product-card">
                <h3>{{ product.productName }}</h3>
                <p>价格: ¥{{ product.price }} /Kg</p>
                <p>库存: {{ product.stock }} Kg</p>
                <div class="card-actions">
                  <button class="edit-btn">编辑</button>
                  <button class="delete-btn">下架</button>
                </div>
              </div>
              <p v-if="!myProducts.length" class="empty-state">您还没有发布任何产品</p>
            </div>
          </div>

          <!-- 添加商品 -->
          <div v-if="currentView === 'addProduct'">
            <form @submit.prevent="handleAddProduct" class="add-product-form">
              <div class="form-group">
                <label for="name">商品名称:</label>
                <input type="text" id="name" v-model="newProduct.productName" required>
              </div>
              <div class="form-group">
                <label for="prodCat">商品大分类:</label>
                <select id="prodCat" v-model="newProduct.prodCat" required>
                  <option value="" disabled>请选择一个大分类</option>
                  <option v-for="category in mainCategories" :key="category" :value="category">
                    {{ category }}
                  </option>
                </select>
              </div>
              <div class="form-group">
                <label for="prodPcat">商品小分类:</label>
                <input type="text" id="prodPcat" v-model="newProduct.prodPcat" placeholder="例如: 红富士苹果, 波士顿龙虾" required>
              </div>
              <div class="form-group">
                <label for="price">价格 (元):</label>
                <input type="number" id="price" v-model.number="newProduct.price" required min="0.01" step="0.01">
              </div>
              <div class="form-group">
                <label for="unitInfo">单位:</label>
                <input type="text" id="unitInfo" v-model="newProduct.unitInfo" placeholder="例如: 斤, 公斤, 箱, 个" required>
              </div>
              <div class="form-group">
                <label for="stock">库存量(Kg):</label>
                <input type="number" id="stock" v-model.number="newProduct.stock" required min="1">
              </div>
              
              <div class="form-group">
                <label for="description">描述:</label>
                <textarea
                    id="description"
                    v-model="newProduct.description"
                    placeholder="请输入商品的详细说明"
                ></textarea>
              </div>
              <button type="submit" class="submit-btn">确认发布</button>
            </form>
          </div>

          <!-- 我的订单 (农户视图) -->
          <div v-if="currentView === 'myOrders'">
            <div class="order-list">
              <div v-for="order in myOrders" :key="order.orderId" class="order-card">
                <div class="order-header">
                  <span>订单号: {{ order.orderId }}</span>
                  <span class="order-date">下单时间: {{ new Date(order.orderDate).toLocaleString() }}</span>
                  <span :class="['order-status', { 'status-pending': order.status === '待发货' }]">{{ order.status }}</span>
                </div>
                <div class="order-body">
                  <div v-for="item in order.orderItems" :key="item.orderItemId" class="order-item">
                    <span>{{ item.productName }}</span>
                    <span>数量: {{ item.quantity }} Kg</span>
                    <span>单价: ¥{{ item.price }}</span>
                  </div>
                </div>
                 <div class="order-footer">
                  <p><strong>买家ID:</strong> {{ order.buyerId }}</p>
                  <p><strong>总金额:</strong> <span class="total-price">¥{{ order.totalPrice.toFixed(2) }}</span></p>
                </div>
              </div>
              <p v-if="!myOrders.length" class="empty-state">您还没有任何订单。</p>
            </div>
          </div>

        </div>
      </div>

      <!-- 普通用户视图 -->
      <div v-else class="buyer-view-container">
        <nav class="buyer-nav">
          <button @click="switchView('products')" :class="{ active: currentView === 'products' }">所有商品</button>
          <button @click="switchView('cart')" :class="{ active: currentView === 'cart' }">我的购物车</button>
          <button @click="switchView('demands')" :class="{ active: currentView === 'demands' }">求购需求</button>
          <button @click="switchView('addDemand')" :class="{ active: currentView === 'addDemand' }">发布求购需求</button>
          <button @click="switchView('myOrders')" :class="{ active: currentView === 'myOrders' }">我的订单</button>
        </nav>

        <div class="view-content-wrapper">
          <!-- 所有商品 -->
          <div v-if="currentView === 'products'">
            <div class="product-list">
              <div
                v-for="product in products"
                :key="product.productId"
                class="product-card"
              >
                <h3>{{ product.productName }}</h3>
                <p>价格: ¥{{ product.price }} /Kg</p>
                <p>库存: {{ product.stock }} Kg</p>

                <div class="card-actions">
                  <button class="view-btn" @click="viewProduct(product)">查看详情</button>
                  <button class="add-to-cart-btn" @click="addToCart(product)">
                    添加到购物车
                  </button>
                </div>
              </div>

              <p v-if="!products.length" class="empty-state">还没有任何商品。</p>
            </div>
          </div>

          <!-- 购物车 -->
          <div v-if="currentView === 'cart'" class="cart-view">
            <!-- 地址选择区域 -->
            <div v-if="addresses.length" class="address-container">
              <label for="addressSelect">选择收货地址：</label>
              <select id="addressSelect" v-model="selectedAddressId" class="address-select">
                <option v-for="address in addresses" :key="address.addressId" :value="address.addressId">
                  {{ address.recipientName }} - {{ address.province }}{{ address.city }}{{ address.district }}{{ address.streetAddress }}（{{ address.phoneNumber }}）
                </option>
              </select>
            </div>
            <p v-else class="empty-state">暂无收货地址，请前往“个人中心”添加</p>



            <div v-if="cartItems.length" class="cart-list">
              <div class="cart-card" v-for="item in cartItems" :key="item.productId">
                <!-- 商品图片，可后续替换真实URL -->
                <img src="../assets/img.png" alt="商品图片" class="cart-image" />

                <!-- 商品信息 -->
                <div class="cart-info">
                  <h3 class="cart-name">{{ item.productName }}</h3>
                  <p class="cart-price">¥{{ item.price }} / Kg</p>

                  <div class="cart-quantity">
                    <span>数量(Kg)：</span>
                    <button @click="changeQuantity(item.productId, item.quantity - 1)" :disabled="item.quantity <= 1">-</button>
                    <span class="qty">{{ item.quantity }}</span>
                    <button @click="changeQuantity(item.productId, item.quantity + 1)">+</button>
                  </div>
                </div>

                <!-- 操作区 -->
                <div class="cart-actions">
                  <button class="remove-btn" @click="removeFromCart(item.productId)">移除</button>
                </div>
              </div>
            </div>

            <p v-else class="empty-state">购物车是空的。</p>

            <div v-if="cartItems.length" class="cart-summary">
              <p>合计: ¥{{ totalPrice }}</p>
              <router-link to="/order">
                <button class="submit-btn" @click="createOrder">提交订单</button>
              </router-link>
            </div>

          </div>

          


          <!-- 求购需求 -->
          <div v-if="currentView === 'demands'">

            <div class="request-list">
              <!-- 求购需求卡片 -->
              <div
                  v-for="request in demands"
                  :key="request.demandId"
                  class="request-card"
              >
                <p class="request-title"><strong>用户 {{ request.buyerId }}： 我想要  </strong> {{ request.productNameDesired }}</p>
                <p class="request-quantity">需求量： {{ request.quantityDesired }} {{ request.unitDesired }}</p>
                <p class="request-details">需求描述： {{ request.details }}</p>
              </div>


              <p v-if="!demands.length" class="empty-state">暂无求购需求</p>

              <button v-if="!demands.length" @click="switchView('addDemand')" class="add-demand-btn">发布求购需求</button>
            </div>
          </div>

          <!-- 发布求购需求 -->
          <div v-if="currentView === 'addDemand'">

            <form @submit.prevent="handleAddDemand" class="add-product-form">
              <div class="form-group">
                <label for="demandName">需求商品名称:</label>
                <input type="text" id="demandName" v-model="newDemand.productNameDesired" required>
              </div>
              <div class="form-group">
                <label for="demandQuantity">需求数量 (Kg):</label>
                <input type="number" id="demandQuantity" v-model.number="newDemand.quantityDesired" required min="1">
              </div>
              <div class="form-group">
                <label for="maxPrice">期望最高单价 (元/Kg):</label>
                <input type="number" id="maxPrice" v-model.number="newDemand.maxPricePerUnit" required min="0.01" step="0.01">
              </div>
              <div class="form-group">
                <label for="deliveryDate">期望交货日期:</label>
                <input type="date" id="deliveryDate" v-model="newDemand.deliveryDateDesired" required>
              </div>
              <div class="form-group">
                <label for="details">补充说明:</label>
                <textarea id="details" v-model="newDemand.details" rows="3" placeholder="可选"></textarea>
              </div>
              <button type="submit" class="submit-btn">确认发布</button>
            </form>
          </div>

          <!-- 我的订单 (买家视图) -->
          <div v-if="currentView === 'myOrders'">
             <div class="order-list">
              <div v-for="order in myOrders" :key="order.orderId" class="order-card">
                <div class="order-header">
                  <span>订单号: {{ order.orderId }}</span>
                  <span class="order-date">下单时间: {{ new Date(order.orderDate).toLocaleString() }}</span>
                  <span :class="['order-status', { 'status-pending': order.status === '待发货' }]">{{ order.status }}</span>
                </div>
                <div class="order-body">
                  <div v-for="item in order.orderItems" :key="item.orderItemId" class="order-item">
                    <span>{{ item.productName }}</span>
                    <span>数量: {{ item.quantity }} Kg</span>
                    <span>单价: ¥{{ item.price }}</span>
                  </div>
                </div>
                <div class="order-footer">
                  <p><strong>收货地址:</strong> {{ order.address.province }}{{ order.address.city }}{{ order.address.district }}{{ order.address.streetAddress }}</p>
                  <p v-if="order.totalAmount != null">
                     <strong>总金额:</strong> <span class="total-price">¥{{ order.totalAmount.toFixed(2) }}</span>
                  </p>
                </div>
              </div>
              <p v-if="!myOrders.length" class="empty-state">您还没有任何订单。</p>
            </div>
          </div>

        </div>

      </div>

    </section>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch} from 'vue'
import axios from '../utils/axios'
import router from "@/router/index.js";


const userInfo = ref({})
const userId = ref('')
const userName = ref('游客')
const role = ref('未登录')

// 从 localStorage 拿 token
const token = localStorage.getItem('token')

const mainCategories = ref(['蔬菜','水产','水果','肉类'])

const currentView = ref('products')

// 农户数据
const myProducts = ref([])
const newProduct = ref({ productName: '', price: null,  unitInfo: '',stock: null, prodCat:'', prodPcat:'' })

// 普通用户数据
const products = ref([])
const cartItems = ref([])

// 求购需求数据
const demands = ref([])

// 求购需求数据
const newDemand = ref({
  productNameDesired: '',
  quantityDesired: null,
  unitDesired: '',
  maxPricePerUnit: null,
  deliveryDateDesired: '',
  details: ''
})

//订单数据
const myOrders = ref([])


function switchView(view) { currentView.value = view }

async function loadProducts() {
  try{
    const res = await axios.get('/products')
    products.value = res.data.records||[]
  }catch(err){
    console.error('加载全部商品失败',err)
  }
}

async function goToProductDetail(product) {
  if (product && product.productId) {
    router.push(`/product/${product.productId}`);
  }
}

async function loadDemands() {
  try{
    const reqRes = await axios.get('/purchase-demands')
    // 打印响应体
    console.log(reqRes.data)
    demands.value = reqRes.data.records
  }catch(err){
    console.error('加载购物需求失败',err);
  }
}

async function handleAddProduct() {
  try {

     if (!newProduct.value.prodCat) {
      alert('请选择一个商品大分类！');
      return;
    }

    const productToSend = {
    ...newProduct.value,
    farmerId: userId.value
  }
    const res = await axios.post('/products/publish', productToSend)
    if (res.data) {
      alert('商品发布成功！')
      myProducts.value.push({ ...newProduct.value, id: Date.now() })
      newProduct.value = { productName: '', price: null, unitInfo: '', stock: null, prodCat:'', prodPcat:''  }
      currentView.value = 'myProducts'
    }
  } catch (err) { alert('发布失败') }
}

// ====== 购物车逻辑 ======
// 从后端加载购物车 + 获取商品详情
const addresses = ref([]) // 地址列表
const selectedAddressId = ref(null)

// ✅ 从后端加载购物车 + 商品详情
async function loadCart() {
  try {
    const token = localStorage.getItem('token')
    const res = await axios.get('/cart', {
      params: { userId: userId.value, pageNum: 1, pageSize: 50 },
      headers: { Authorization: token }
    })

    const pageData = res.data || {}
    const items = pageData.records || []

    const detailedItems = await Promise.all(
        items.map(async (item) => {
          try {
            const productRes = await axios.get(`/products/${item.productId}`)
            const product = productRes.data
            return {
              ...item,
              productName: product.productName,
              price: product.price,
              stock: product.stock
            }
          } catch (err) {
            console.warn('商品详情获取失败:', item.productId, err)
            return item
          }
        })
    )

    cartItems.value = detailedItems
    totalPrice.value = detailedItems.reduce(
        (sum, i) => sum + i.price * i.quantity,
        0
    )
  } catch (err) {
    console.error('加载购物车失败:', err)
    alert('加载购物车失败，请稍后重试')
  }
}

//从后端加载用户地址
async function loadAddresses() {
  try {
    const token = localStorage.getItem('token')
    const res = await axios.get('/address/user', {
      headers: { Authorization: token }
    })
    addresses.value = res.data || []
    if (addresses.value.length > 0) {
      selectedAddressId.value = addresses.value[0].addressId // 默认选中第一个
    }
  } catch (err) {
    console.error('加载地址失败:', err)
  }
}

// 添加到购物车
async function addToCart(product) {
  try {
    const res = await axios.post('/cart/add', null, {
      params: {
        userId: userId.value,
        productId: product.productId,
        quantity: 1
      }
    })
    if (res.data) {
      alert('已添加到购物车！')
      loadCart()
    } else {
      alert('添加失败')
    }
  } catch (err) {
    console.error(err)
    alert('添加购物车失败')
  }
}

// 修改数量
async function changeQuantity(productId, newQty) {
  if (newQty <= 0) return
  try {
    const res = await axios.post('/cart/update', null, {
      params: {
        userId: userId.value,
        productId,
        quantity: newQty
      }
    })
    if (res.data) {
      const item = cartItems.value.find(i => i.productId === productId)
      if (item) item.quantity = newQty
    }
  } catch (err) {
    console.error('修改数量失败', err)
  }
}

// 移除商品
async function removeFromCart(productId) {
  try {
    const res = await axios.delete('/cart/item', {
      params: { userId: userId.value, productId }
    })
    if (res.data) {
      alert('移除成功')
      loadCart()
    }
  } catch (err) {
    console.error('移除失败', err)
  }
}


async function createOrder() {
  if (!selectedAddressId.value) {
    alert('请先选择收货地址')
    return
  }

  try {
    const reqBody = {
      addressId: selectedAddressId.value,
      orderItems: cartItems.value.map(item => ({
        productId: item.productId,
        quantity: item.quantity
      }))
    }

    const res = await axios.post('/orders', reqBody, {
      params: { userId: userId.value },
      headers: { Authorization: token }
    })
    const orderId =  res.data || [9999]

    if (res.data) {
      alert(`订单提交成功！`)
      cartItems.value = []
      await router.push(`/orders/${orderId}`)
    } else {
      alert('提交失败，请稍后再试')
    }
  } catch (err) {
    console.error('提交订单失败:', err)
    alert('提交失败，请检查登录状态或网络')
  }
}


// 计算总价
const totalPrice = computed(() =>
    cartItems.value.reduce((sum, item) => sum + item.price * item.quantity, 0)
)

async function handleAddDemand() {
  try {
    const demandToSend = {
      ...newDemand.value,
      buyerId: userId.value
    }

    const res = await axios.post('/purchase-demands', demandToSend)

    if (res.data) {
      alert('求购需求发布成功！')
      newDemand.value = {
        productNameDesired: '',
        quantityDesired: null,
        unitDesired: '',
        maxPricePerUnit: null,
        deliveryDateDesired: '',
        details: ''
      }
      currentView.value = role.value === 'farmer' ? 'myProducts' : 'products'
    }
  } catch (err) {
    console.error(err.response?.data || err)
    alert('发布失败')
  }
}

//加载我的订单
async function loadMyOrders() {
  if (!userId.value||!token) {
    console.log("用户未登录，无法加载订单");
    return;
  }
  try {
    const res = await axios.get('/orders', { 
      params: {
        pageNum: 1,
        pageSize: 10 
      },
      headers: {
        // 传递 JWT token
        Authorization: `Bearer ${token}` 
      }
    });
    if (res.data && res.data.records) {
      myOrders.value = res.data.records;
      console.log("加载到的订单:", myOrders.value);
    } else {
      // 如果没有 records 字段，可能返回的是一个空列表或null
      myOrders.value = []; 
      console.log("未查询到订单或返回数据格式不正确");
    }
  } catch (error) {
    console.error("加载我的订单失败:", error);
     if (error.response && (error.response.status === 401 || error.response.status === 403)) {
        alert("登录已过期，请重新登录。");
        // 这里可以添加跳转到登录页的逻辑
        // router.push('/login');
    } else {
        alert("加载订单失败，请稍后重试。");
    }
    myOrders.value = []; 
  }
}


onMounted(async () => {

  if (!token) return

  try {
    // 调后端 /api/user/info 接口
    const res = await axios.get('/user/info', {
      headers: {
        Authorization: `Bearer ${token}`
      }
    })

    if (res.data.success) {
      userInfo.value = res.data.user
      userId.value = res.data.user.userId
      userName.value = res.data.user.userName
      role.value = res.data.user.role
      // 如果需要可保存到 localStorage
      localStorage.setItem('userInfo', JSON.stringify(res.data.user))
    } else {
      console.warn('Token 无效或过期')
    }
  } catch (err) {
    console.error('获取用户信息失败', err)
  }

  await loadDemands()
  await loadAddresses()
  await loadProducts()
  await loadCart()
})

watch(currentView, (val) => {
  if (val === 'cart') {
    loadAddresses()
    loadCart()
  }
  if(val === 'products') {
    loadProducts()
  }
  if(val === 'demands') {
    loadDemands()
  }
  if (val === 'myOrders') {
    loadMyOrders() 
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

.buyer-nav {
  display: flex;
  border-bottom: 2px solid #e0e0e0;
  margin-bottom: 25px;
}

.buyer-nav button {
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
.buyer-nav button:hover { color: #2D7D4F; }
.buyer-nav button.active {
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
.add-to-cart-btn {
  background-color: #4CAF50;
  color: #fff;
  border: none;
  padding: 6px 12px;
  border-radius: 5px;
  cursor: pointer;
}
.add-to-cart-btn:hover {
  background-color: #45a049;
}
.view-btn {
  background-color: #4CAF50;
  color: #fff;
  border: none;
  padding: 6px 12px;
  border-radius: 5px;
  cursor: pointer;
}
.view-btn:hover {
  background-color: #45a049;
}

.add-product-form { margin: 0 auto;  display: flex ; flex-direction: column; align-content: center; max-width: 600px; margin-top: 20px; }
.form-group { display: flex; align-items: center; margin-bottom: 20px; }
.form-group label { width: 120px; font-weight: bold; }
.form-group input,.form-group select{ flex: 1; padding: 10px; border: 1px solid #ccc; border-radius: 4px; }
.form-group textarea { flex: 1; padding: 10px; border: 1px solid #ccc; border-radius: 4px; }

.empty-state {
  grid-column: 1 / -1;
  text-align: center;
  color: #888;
  padding: 3rem;
}

.request-list {
  display: flex;
  flex-direction: column;
  gap: 8px; /* 卡片间距稍微小一点 */
  align-items: center;
}

.request-card {
  display: flex;
  flex-direction: column;
  justify-content: center; /* 上下居中 */
  align-items: flex-start; /* 左对齐 */
  border: 1px solid #e9e9e9;
  border-radius: 8px;
  padding: 12px 16px; /* 内边距缩小 */
  height: 80px; /* 总高度小一点 */
  background-color: #fafafa;
  transition: box-shadow 0.3s;
  line-height: 0.8; /* 行间距小一点 */
}

.request-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.request-title {
  font-weight: bold;
  margin-bottom: 0;
}

.request-quantity {
  font-size: 0.9rem;
  margin-bottom: 0;
}

.request-details {
  font-size: 0.8rem;
  color: #555;
}


.add-demand-btn {
  max-width:  250px;
  margin-top: 16px;
  border-radius: 12px;
  background-color: #4CAF50;
  color: #fff;
  cursor: pointer;
}

.add-demand-btn:hover {
  background-color: #45a049;
}

.cart-view {
  padding: 20px;
}

.cart-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.cart-card {
  display: flex;
  align-items: center;
  background-color: #fff;
  border-radius: 12px;
  padding: 12px 16px;
  box-shadow: 0 2px 6px rgba(0,0,0,0.1);
  transition: transform 0.2s;
}

.cart-card:hover {
  transform: translateY(-3px);
}

.cart-image {
  width: 100px;
  height: 100px;
  object-fit: cover;
  border-radius: 8px;
  margin-right: 20px;
}

.cart-info {
  flex: 1;
}

.cart-name {
  font-size: 18px;
  font-weight: 600;
  color: #333;
}

.cart-price {
  color: #f40;
  font-size: 16px;
  margin: 6px 0;
}

.cart-quantity {
  display: flex;
  align-items: center;
  gap: 8px;
}

.cart-quantity button {
  background: #eee;
  border: none;
  padding: 4px 10px;
  cursor: pointer;
  border-radius: 4px;
}

.cart-quantity button:hover {
  background: #ddd;
}

.cart-actions {
  margin-left: 20px;
}

.remove-btn {
  background-color: #ff4d4f;
  color: white;
  border: none;
  padding: 6px 12px;
  border-radius: 6px;
  cursor: pointer;
}

.remove-btn:hover {
  background-color: #e04343;
}

.cart-summary {
  margin-top: 24px;
  text-align: right;
  font-size: 18px;
}

.submit-btn {
  margin-left: 20px;
  background-color: #4caf50;
  color: white;
  border: none;
  padding: 10px 18px;
  border-radius: 8px;
  cursor: pointer;
}

.submit-btn:hover {
  background-color: #43a047;
}

.empty-state {
  text-align: center;
  color: #888;
  margin-top: 40px;
}


.address-section h3 {
  font-size: 18px;
  font-weight: bold;
  margin-bottom: 12px;
  color: #333;
}

.address-container {
  width: 100%;
  margin: 10px 0;
}

.address-select {
  width: 85%; /* ✅ 让选择框和容器等宽 */
  padding: 10px;
  border: 1px solid #ccc;
  border-radius: 8px;
  font-size: 16px;
  box-sizing: border-box; /* 避免padding撑大 */
}

.order-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}
.order-card {
  border: 1px solid #ddd;
  border-radius: 8px;
  background-color: #fff;
  padding: 16px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.05);
}
.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #eee;
  padding-bottom: 12px;
  margin-bottom: 12px;
  color: #666;
  font-size: 0.9em;
}
.order-status {
  font-weight: bold;
  padding: 4px 8px;
  border-radius: 4px;
  color: #333;
}
.order-status.status-pending {
  background-color: #FFFBE6; /* 淡黄色背景 */
  color: #FAAD14; /* 橙色文字 */
}
.order-body {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.order-item {
  display: flex;
  justify-content: space-between;
  padding: 8px;
  background-color: #f9f9f9;
  border-radius: 4px;
}
.order-footer {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #eee;
  text-align: right;
}
.order-footer p {
  margin: 5px 0;
  font-size: 0.9em;
  color: #555;
}
.total-price {
  font-size: 1.2em;
  font-weight: bold;
  color: #d9534f; /* 红色 */
}

</style>