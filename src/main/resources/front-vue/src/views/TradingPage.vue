<template>
  <div class="main-bg">
    <HeaderComponent />

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
              <div v-for="product in products" :key="product.productId" class="product-card">
                <h3>{{ product.productName }}</h3>
                <p>价格: ¥{{ product.price }} / {{ product.unitInfo }}</p>
                <p>库存: {{ product.stock }} {{ product.unitInfo }}</p>

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
              <div v-for="request in demands" :key="request.demandId" class="request-card">
                <p class="request-title"><strong>用户 {{ request.buyerId }}：</strong> 想要 {{ request.productNameDesired }}</p>
                <p class="request-quantity">需求量：{{ request.quantityDesired }} {{ request.unitDesired }}</p>
                <p class="request-details">需求描述：{{ request.details }}</p>
              </div>
              <p v-if="!demands.length" class="empty-state">暂无求购需求</p>
            </div>
          </div>

          <!-- 我的商品 -->
          <div v-if="currentView === 'myProducts'">
            <div class="product-list">
              <div v-for="product in myProducts" :key="product.productId" class="product-card">
                <h3>{{ product.productName }}</h3>
                <p>价格: ¥{{ product.price }} / {{ product.unitInfo }}</p>
                <p>库存: {{ product.stock }} {{ product.unitInfo }}</p>
                <div class="card-actions">
                  <button class="edit-btn">编辑</button>
                  <button class="view-btn" @click="goToProductDetail(product)">详情</button>
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
                <select id="prodCat" v-model="newProduct.prodCat" required >
                  <option v-for="category in mainCategories" :key="category" :value="category">
                    {{ category }}
                  </option>
                </select>
              </div>

              <div class="form-group">
                <label for="prodPcat">商品小分类:</label>
                <input type="text" id="prodPcat" v-model="newProduct.prodPcat" placeholder="例如: 红富士苹果, 龙虾" required>
              </div>

              <div class="form-group">
                <label for="price">价格 (元/{{ newProduct.unitInfo || '单位' }}):</label>
                <input type="number" id="price" v-model.number="newProduct.price" required min="0.01" step="0.01">
              </div>

              <div class="form-group">
                <label for="unitInfo">单位:</label>
                <input type="text" id="unitInfo" v-model="newProduct.unitInfo" placeholder="例如: 斤, 公斤, 箱, 个" required>
              </div>

              <div class="form-group">
                <label for="stock">库存量 ({{ newProduct.unitInfo || '单位' }}):</label>
                <input type="number" id="stock" v-model.number="newProduct.stock" required min="1">
              </div>

              <div class="form-group">
                <label for="image">商品图片:</label>
                <input type="file" id="image" @change="handleImageUpload" accept="image/*">
                <img v-if="newProduct.imagePath" :src="newProduct.imagePath" alt="预览" class="preview">
              </div>

              <div class="form-group">
                <label for="description">描述:</label>
                <textarea id="description" v-model="newProduct.description" placeholder="请输入商品的详细说明"></textarea>
              </div>

              <button type="submit" class="submit-btn">确认发布</button>
            </form>
          </div>

          <!-- 我的订单 -->
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
                    <span>数量: {{ item.quantity }} {{ item.unitInfo }}</span>
                    <span>单价: ¥{{ item.price }} / {{ item.unitInfo }}</span>
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
              <div v-for="product in products" :key="product.productId" class="product-card">
                <h3>{{ product.productName }}</h3>
                <p>价格: ¥{{ product.price }} / {{ product.unitInfo }}</p>
                <p>库存: {{ product.stock }} {{ product.unitInfo }}</p>

                <div class="card-actions">
                  <button class="view-btn" @click="viewProduct(product)">查看详情</button>
                  <button class="add-to-cart-btn" @click="addToCart(product)">添加到购物车</button>
                </div>
              </div>
              <p v-if="!products.length" class="empty-state">还没有任何商品。</p>
            </div>
          </div>

          <!-- 购物车 -->
          <div v-if="currentView === 'cart'" class="cart-view">
            <div v-if="cartItems.length" class="cart-list">
              <div class="cart-card" v-for="item in cartItems" :key="item.productId">
                <div class="product-image">
                  <img :src="item.imageUrl || placeholder" alt="商品图片" />
                </div>
                <div class="cart-info">
                  <h3 class="cart-name">{{ item.productName }}</h3>
                  <p class="cart-price">¥{{ item.price }} / {{ item.unitInfo }}</p>
                  <div class="cart-quantity">
                    <span>数量({{ item.unitInfo }}):</span>
                    <button @click="changeQuantity(item.productId, item.quantity - 1)" :disabled="item.quantity <= 1">-</button>
                    <span class="qty">{{ item.quantity }}</span>
                    <button @click="changeQuantity(item.productId, item.quantity + 1)">+</button>
                  </div>
                </div>
                <div class="cart-actions">
                  <button class="remove-btn" @click="removeFromCart(item.productId)">移除</button>
                </div>
              </div>
              <button class="submit-btn" @click="createOrder()">提交</button>
            </div>
            <p v-else class="empty-state">购物车是空的。</p>
          </div>

          <!-- 我的订单 -->
          <div v-if="currentView === 'myOrders'" class="order-list">
            <h2 class="title">我的订单</h2>

            <!-- 数据加载中 -->
            <div v-if="loading" class="loading">正在加载订单...</div>

            <!-- 没有订单 -->
            <div v-else-if="myOrders.length === 0" class="empty">暂无订单</div>

            <!-- 订单列表 -->
            <div v-else>
              <div v-for="order in myOrders" :key="order.orderId" class="order-card">
                <div class="order-header">
                  <span>订单号：{{ order.orderId }}</span>
                  <span>下单时间：{{ formatTime(order.createTime) }}</span>
                  <span class="status">状态：{{ order.status }}</span>
                </div>

                <div class="order-items">
                  <div v-for="item in order.items" :key="item.productId" class="order-item">
                    <img :src="item.imageUrl || defaultImage" alt="商品图" />
                    <div class="item-info">
                      <p class="name">{{ item.productName }}</p>
                      <p>数量：{{ item.quantity }}</p>
                      <p>单价：¥{{ item.unitPrice.toFixed(2) }}</p>
                    </div>
                  </div>
                </div>

                <div class="order-footer">
                  <p>总金额：<strong>¥{{ order.totalAmount.toFixed(2) }}</strong></p>
                  <div class="actions">
                    <button v-if="order.status === 'pending'" @click="goToPay(order.orderId)">立即支付</button>
                    <button v-if="order.status === 'pending'" @click="cancelOrder(order.orderId)">取消订单</button>
                    <button v-else @click="viewDetail(order.orderId)">查看详情</button>
                  </div>
                </div>
              </div>
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
import HeaderComponent from '../components/HeaderComponent.vue';
import placeholder from "@/assets/img.png";


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
const newProduct = ref({ farmerId: '', productName: '', price: null,  unitInfo: '',stock: null, prodCat:'', prodPcat:'' , imagePath:''})

const imageFile = ref(null)

// 普通用户数据
const products = ref([])
const cartItems = ref([])

// 求购需求数据
const demands = ref([])
const newDemand = ref({
  productNameDesired: '',
  quantityDesired: null,
  unitDesired: '',
  maxPricePerUnit: null,
  deliveryDateDesired: '',
  details: ''
})


// 订单数据
const myOrders = ref([])
const loading = ref(true)// 时间格式化
const formatTime = t => (t ? new Date(t).toLocaleString() : '')

function handleImageUpload(event) {
  // 获取上传的文件对象
  imageFile.value = event.target.files[0]

  if (!imageFile.value)
    newProduct.value.imagePath = ''

  // 创建本地预览路径
  const previewUrl = URL.createObjectURL(imageFile.value)
  console.log('图片文件对象：', imageFile.value)
  console.log('图片预览路径：', previewUrl)

  // 前端保存临时路径（仅用于预览）
  newProduct.value.imagePath = previewUrl

  console.log('新产品：', newProduct.value)
}

function switchView(view) { currentView.value = view }

async function loadProducts() {
  try{
    const res = await axios.get('/products')
    products.value = res.data.records||[]
  }catch(err){
    console.error('加载全部商品失败',err)
  }
}

async function loadMyProducts() {
  try{
    const res = await axios.get(`/products/farmer/${userId.value}`)
    myProducts.value = res.data.records||[]
  }catch(err){
    console.error('加载我的商品失败',err)
  }
}

async function goToProductDetail(product) {
  if (product && product.productId) {
    await router.push(`/product/${product.productId}`);
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
    // 校验必填项
    if (!newProduct.value.prodCat) {
      alert('请选择一个商品大分类！')
      return
    }

    newProduct.value.farmerId = userId

    //构造 multipart/form-data
    const formData = new FormData()

    // product 是一个 JSON 对象，要先序列化成字符串再用 Blob 封装
    const productBlob = new Blob([JSON.stringify(newProduct.value)], {
      type: 'application/json'
    })
    formData.append('product', productBlob) // 对应后端的 @RequestPart("product")

    // 如果选择了图片，附加上
    if (imageFile.value) {
      formData.append('image', imageFile.value) // 对应后端的 @RequestPart("image")
    }

    //发请求
    const res = await axios.post('/products/publish', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
        Authorization: token // 如果不需要鉴权，可以删掉
      }
    })

    // 处理返回结果
    if (res.data) {
      alert('商品发布成功！')

      // 重置表单
      newProduct.value = {
        farmerId: '',
        productName: '',
        price: null,
        unitInfo: '',
        stock: null,
        prodCat: '',
        prodPcat: '',
        description: '',
        imagePath: ''
      }
      imageFile.value = null
      currentView.value = 'myProducts'
    }
  } catch (err) {
    console.error('发布失败：', err)
    alert('发布失败，请稍后重试')
  }
}


// ====== 购物车逻辑 ======
const addresses = ref([]) // 地址列表
const selectedAddressId = ref(null)

// 从后端加载购物车 + 商品详情 + 图片
async function loadCart() {
  try {
    const token = localStorage.getItem('token')
    const res = await axios.get('/cart', {
      params: { userId: userId.value, pageNum: 1, pageSize: 50 },
      headers: { Authorization: `Bearer ${token}` }
    })

    const pageData = res.data || {}
    const items = pageData.records || []

    // 并行加载每个商品的详情和图片
    const detailedItems = await Promise.all(
        items.map(async (item) => {
          try {
            // 获取商品详情
            const productRes = await axios.get(`/products/${item.productId}`)
            const product = productRes.data

            // 尝试获取商品图片
            let imageUrl = ''
            try {
              const imageRes = await axios.get(`/products/${product.productId}/image`, {
                headers: { Authorization: `Bearer ${token}` },
                responseType: 'blob' // 返回二进制
              })
              imageUrl = URL.createObjectURL(imageRes.data)
              console.log('图片获取成功')
            } catch {
              // 如果图片获取失败，使用默认占位图
              console.log('<图片获取失败>', product)
              imageUrl = new URL('../assets/img.png', import.meta.url).href
            }

            return {
              ...item,
              productName: product.productName,
              price: product.price,
              stock: product.stock,
              imagePath: product.imagePath,
              unitInfo: product.unitInfo,
              imageUrl
            }
          } catch (err) {
            console.warn('商品详情获取失败:', item.productId, err)
            return { ...item, imageUrl: new URL('../assets/img.png', import.meta.url).href }
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
      headers: { Authorization: `Bearer ${token}` }
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
      headers: { Authorization: `Bearer ${token}` }
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
  }finally {
    loading.value = false
  }
}

// 取消订单
const cancelOrder = async (orderId) => {
  try {
    const res = await axios.put(`/orders/${orderId}/cancel`, null, {
      headers: { Authorization: token }
    })
    if (res.data) {
      ElMessage.success('订单已取消')
      orders.value = orders.value.map(o =>
          o.orderId === orderId ? { ...o, status: 'cancelled' } : o
      )
    } else {
      ElMessage.warning('取消订单失败')
    }
  } catch (err) {
    console.error('取消订单出错:', err)
    ElMessage.error('取消失败')
  }
}

// 查看详情
const viewDetail = (orderId) => {
  router.push(`/orderDetail/${orderId}`)
}

// 支付
const goToPay = (orderId) => {
  router.push(`/orders/${orderId}`)
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
  if (role === 'farmer') await loadMyProducts()
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

  if(val === 'myProducts') {
    loadMyProducts()
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
  padding: 8px 16px;
  box-shadow: 0 2px 6px rgba(0,0,0,0.1);
  transition: transform 0.2s;
}

.cart-card:hover {
  transform: translateY(-3px);
}

.cart-info {
  flex: 1;
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 80px;
}


.cart-name {
  font-size: 18px;
  font-weight: 600;
  color: #333;
}

.cart-price {
  color: #f40;
  font-size: 16px;
}

.cart-quantity {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-left: auto;
  margin-right: 80px;
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
.submit-btn {
  display: block;            /* 转块级以占整行 */
  margin-left: auto;         /* 自动推向右边 */
  width: 120px;              /* 固定宽度 */
  background-color: #4caf50;
  color: white;
  border: none;
  padding: 10px 0;
  border-radius: 8px;
  cursor: pointer;
  text-align: center;
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

.image-preview img {
  max-width: 200px;
  max-height: 200px;
  border-radius: 8px;
  border: 1px solid #ddd;
}

.preview {
  width: 150px;
  height: 150px;
  object-fit: cover; /* 保持比例裁剪，不会拉伸变形 */
  border-radius: 8px; /* 圆角，可选 */
  border: 1px solid #ccc; /* 灰色边框，可选 */
  margin-top: 10px;
}

.preview {
  width: 50px;
  height: 50px;
  object-fit: cover; /* 保持比例裁剪，不会拉伸变形 */
  border-radius: 8px; /* 圆角，可选 */
  border: 1px solid #ccc; /* 灰色边框，可选 */
  margin-top: 10px;
}

.product-image {
  flex: 0 0 100px;
  width: 100px;
  height: 100px;
  display: flex;
  align-items: center;
  justify-content: center; /* 图片居中 */
  overflow: hidden;
  border-radius: 8px;
  border: 1px solid #eee;
  background-color: #f9f9f9;
  margin-left: 20px;
  margin-right: 50px;
}

.product-image img {
  width: 100%;
  height: 100%;
  object-fit: cover; /* 保持比例并填满容器 */
  border-radius: 8px;
}

.loading,
.empty {
  text-align: center;
  margin-top: 50px;
  color: #999;
}
.order-card {
  border: 1px solid #eee;
  border-radius: 10px;
  margin-bottom: 20px;
  background: #fff;
  padding: 15px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
}
.order-header {
  display: flex;
  justify-content: space-between;
  font-size: 14px;
  margin-bottom: 10px;
  color: #666;
}
.status {
  color: #007bff;
}
.order-items {
  display: flex;
  flex-wrap: wrap;
  border-top: 1px solid #f0f0f0;
  border-bottom: 1px solid #f0f0f0;
  padding: 10px 0;
}
.order-item {
  display: flex;
  align-items: center;
  width: 50%;
  margin-bottom: 10px;
}
.order-item img {
  width: 80px;
  height: 80px;
  border-radius: 8px;
  object-fit: cover;
  margin-right: 10px;
}
.item-info {
  flex: 1;
}
.item-info .name {
  font-weight: bold;
  margin-bottom: 5px;
}
.order-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 10px;
}
.actions button {
  margin-left: 10px;
  padding: 6px 12px;
  border: none;
  border-radius: 5px;
  background: #4caf50;
  color: white;
  cursor: pointer;
}
.actions button:hover {
  background: #43a047;
}
.actions button:nth-child(2) {
  background: #f44336;
}
.actions button:nth-child(2):hover {
  background: #d32f2f;
}
</style>