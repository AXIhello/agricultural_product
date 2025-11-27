<template>
  <div class="main-bg">
    <HeaderComponent />

    <section class="content">
      <!-- 顶部导航 -->
      <nav class="main-nav">
        <button @click="switchView('products')" :class="{ active: currentView === 'products' }">所有产品</button>
        <button @click="switchView('predict')" :class="{ active: currentView === 'predict' }">行情大厅</button>
        <button @click="switchView('demands')" :class="{ active: currentView === 'demands' }">求购需求</button>
        <button v-if="role === 'farmer'" @click="switchView('myProducts')" :class="{ active: currentView === 'myProducts' }">我的产品</button>
        <button v-if="role === 'buyer'" @click="switchView('cart')" :class="{ active: currentView === 'cart' }">我的购物车</button>
        <button v-if="role === 'buyer'" @click="switchView('myOrders')" :class="{ active: currentView === 'myOrders' }">我的订单</button>
      </nav>

      <!-- 内容区域 -->
      <div class="view-content-wrapper">
        <!-- 所有产品 -->
        <div v-if="currentView === 'products'" class="product-page">
          <!-- 左侧分类栏 -->
          <aside class="category-sidebar">
            <ul style="padding-left: 0;list-style: none;">
              <li
                  v-for="(subCats, cat) in categoryMap"
                  :key="cat"
                  class="category-group"
              >
                <h4 class="cat-title">{{ cat }}</h4>
                <ul class="subcat-list">
                  <li
                      v-for="sub in subCats"
                      :key="sub"
                      :class="['subcat-item', { active: selectedSubcat === sub }]"
                      @click="selectCategory(sub)"
                  >
                    {{ sub }}
                  </li>
                </ul>
              </li>
            </ul>
          </aside>

          <!-- 产品展示区 -->
          <div class="product-list">
            <div
                v-for="product in products"
                :key="product.productId"
                class="product-card"
                @click="goToProductDetail(product)"
            >
              <div class="product-image">
                <img :src="product.imageUrl" :alt="product.productName" @error="handleImageError" />
              </div>

              <div class="product-info">
                <h3 class="product-name">{{ product.productName }}</h3>
                <p class="product-price">¥{{ product.price }} / {{ product.unitInfo }}</p>
                <p class="product-stock">库存: {{ product.stock }} {{ product.unitInfo }}</p>
              </div>

<!--              <div class="action-buttons">-->
<!--                <button class="view-btn" @click="goToProductDetail(product)">查看详情</button>-->
<!--                <button-->
<!--                    v-if="role !== 'farmer'"-->
<!--                    class="add-to-cart-btn"-->
<!--                    @click="addToCart(product)"-->
<!--                >-->
<!--                  加入购物车-->
<!--                </button>-->
<!--              </div>-->
            </div>
          </div>
        </div>

        <!-- 价格预测 -->
        <div v-if="currentView === 'predict'" class="prediction-wrapper">

          <!-- 左侧：产品横向列表 -->
          <div class="prediction-left">
            <div class="product-card-horizontal"
                 v-for="product in allSpecs"
                 :key="product.productId">

              <img :src="product.imageUrl" class="h-img">

              <div class="h-info">
                <h3>{{ product.productName }}</h3>
                <p>规格: {{ product.specInfo }}</p>
                <p>当前价格: ¥{{ product.price }}</p>
              </div>

              <button class="h-btn" @click="startPrediction(product)" :disabled="loading">
                开始预测
              </button>

            </div>
          </div>

          <!-- 右侧：预测结果单独卡片 -->
          <div class="prediction-right">
            <h2 class="predict-title">价格预测</h2>

            <div class="chart-box">
              <div v-if="loading">预测中，请稍候...</div>
              <div ref="chartRef" style="width:100%;height:100%;"></div>
            </div>
          </div>
        </div>

        <!-- 我的产品（仅农户） -->
        <div v-if="role === 'farmer' && currentView === 'myProducts'" class="my-product-page">
          <!-- 管理栏 -->
          <div class="product-management-bar">
            <button @click="openAddModal">添加商品</button>

            <!-- 筛选类别 -->
            <select v-model="filterCategory">
              <option value="">全部类别</option>
              <option v-for="category in mainCategories" :key="category" :value="category">
                {{ category }}
              </option>
            </select>

            <!-- 产品状态筛选 -->
            <div class="status-filters">
              <button @click="filterStatus='all'" :class="{active: filterStatus==='all'}">全部</button>
              <button @click="filterStatus='active'" :class="{active: filterStatus==='active'}">上架中</button>
              <button @click="filterStatus='inactive'" :class="{active: filterStatus==='inactive'}">已下架</button>
              <div class="batch-operations">
                <button @click="toggleBatchActions">批量操作</button>
                <div v-if="showBatchActions" class="batch-dropdown">
                  <button @click="batchChoose">全选</button>
                  <button @click="batchDelete">一键删除</button>
                  <button @click="batchDown">一键下架</button>
                </div>
              </div>
            </div>
          </div>

          <!-- 产品列表表头 -->
          <div class="product-list-header">
            <div class="col checkbox-col" v-if="showBatch">选择</div>
            <div class="col img-col">图片</div>
            <div class="col name-col">产品名称</div>
            <div class="col name-col">规格名称</div>
            <div class="col price-col">价格</div>
            <div class="col stock-col">库存</div>
            <div class="col status-col">上架</div>
            <div class="col action-col">操作</div>
          </div>

          <!-- 产品列表 -->
          <div class="my-product-list">
            <div v-for="product in filteredProducts" :key="product.productId" class="product-row">
              <div class="col checkbox-col" v-if="showBatch">
                <input type="checkbox" v-model="selectedProducts" :value="product.productId" />
              </div>
              <div class="col img-col">
                <img :src="product.imageUrl" alt="" class="product-image" @error="handleImageError" />
              </div>
              <div class="col name-col">{{ product.productName }}</div>
              <div class="col name-col">{{ product.specInfo }}</div>
              <div class="col price-col">¥{{ product.price.toFixed(2) }}</div>
              <div class="col stock-col">{{ product.stock }}</div>
              <div class="col status-col">
                <label class="switch">
                  <input
                      type="checkbox"
                      :checked="product.status === 'active'"
                      @change="toggleProductStatus(product, $event.target.checked)"
                  />
                  <span class="slider"></span>
                </label>
              </div>

              <div class="col action-col">
                <button class="edit-btn" @click="editProduct(product)">编辑</button>
                <button class="delete-btn" @click="deleteProduct(product)">删除</button>
              </div>
            </div>
            <p v-if="!filteredProducts.length" class="empty-state">该类别还没有产品哦~</p>
          </div>
        </div>

        <!-- 添加商品弹窗 -->
        <div v-if="showAddProductModal" class="modal-overlay">
          <div class="modal-container">

            <!-- 右上角关闭按钮 -->
            <button class="close-btn" @click="closeAddModal">×</button>

            <h2 class="modal-title">添加商品</h2>

            <div class="modal-body">

              <!-- 商品名称 -->
              <div class="modal-form-group row-layout">
                <label>商品名称：</label>
                <input v-model="newProduct.productName" type="text" placeholder="请输入商品名称" />
              </div>

              <!-- 商品图片 -->
              <div class="modal-form-group">
                <label>商品图片：</label>

                <div class="image-upload-box" @click="triggerImageInput">
                  <span v-if="!newProduct.imagePath">＋ 上传</span>
                  <img v-else :src="newProduct.imagePath" alt="预览" />
                </div>

                <input type="file" ref="imageInput" style="display:none"
                       @change="handleImageUpload" accept="image/*" />
              </div>

              <!-- 商品主类 -->
              <div class="modal-form-group row-layout">
                <label>商品主类：</label>
                <select v-model="newProduct.prodCat" class="form-input">
                  <option v-for="cat in mainCategories" :key="cat" :value="cat">
                    {{ cat }}
                  </option>
                </select>
              </div>

              <!-- 商品副类 -->
              <div class="modal-form-group row-layout">
                <label>商品副类：</label>
                <input v-model="newProduct.prodPcat" type="text" placeholder="例如：叶菜类" />
              </div>


              <!-- 商品单位 -->
              <div class="modal-form-group row-layout">
                <label>商品单位：</label>
                <input
                    v-model="newProduct.unitInfo"
                    type="text"
                    placeholder="例如：斤 / 袋 / 份"
                />
              </div>

              <div class="modal-form-group row-layout">
                <label>描述：</label>
                <textarea v-model="newProduct.description" rows="2"></textarea>
              </div>


              <!-- 添加规格标题 + 按钮 -->
              <div class="spec-header">
                <label>商品规格：</label>
                <button class="add-spec-btn" @click="addSpec">＋ 添加规格</button>
              </div>

              <!-- 动态规格框 -->
              <div class="spec-box" v-for="(spec, index) in specs" :key="index">

                <div class="spec-item-row">
                  <label>规格名称：</label>
                  <input v-model="spec.specInfo" type="text" />
                </div>

                <div class="spec-item-row">
                  <label>价格(元):</label>
                  <input type="number" v-model.number="spec.price" placeholder="请输入价格" />
                </div>


                <div class="spec-item-row">
                  <label>库存量：</label>
                  <input type="number" v-model.number="spec.stock" />
                </div>

                <button class="delete-spec-btn" @click="removeSpec(index)">删除该规格</button>
              </div>

            </div>

            <!-- 底部按钮 -->
            <div class="modal-footer">
              <button class="cancel-btn" @click="closeAddModal">取消</button>
              <button class="save-btn" @click="handleAddProduct">保存</button>
            </div>

          </div>
        </div>

        <!-- 编辑商品弹窗 -->
        <div v-if="showEditProductModal" class="modal-overlay">
          <div class="modal-container">

            <!-- 右上角关闭按钮 -->
            <button class="close-btn" @click="closeAddModal">×</button>

            <h2 class="modal-title">编辑商品</h2>

            <div class="modal-body">

              <!-- 商品名称 -->
              <div class="modal-form-group row-layout">
                <label>商品名称：</label>
                <input v-model="newProduct.productName" type="text" placeholder="请输入商品名称" />
              </div>

              <!-- 商品图片 -->
              <div class="modal-form-group">
                <label>商品图片：</label>

                <div class="image-upload-box" @click="triggerImageInput">
                  <span v-if="!newProduct.imagePath">＋ 上传</span>
                  <img v-else :src="newProduct.imagePath" alt="预览" />
                </div>

                <input type="file" ref="imageInput" style="display:none"
                       @change="handleImageUpload" accept="image/*" />
              </div>

              <!-- 商品主类 -->
              <div class="modal-form-group row-layout">
                <label>商品主类：</label>
                <select v-model="newProduct.prodCat" class="form-input">
                  <option v-for="cat in mainCategories" :key="cat" :value="cat">
                    {{ cat }}
                  </option>
                </select>
              </div>

              <!-- 商品副类 -->
              <div class="modal-form-group row-layout">
                <label>商品副类：</label>
                <input v-model="newProduct.prodPcat" type="text" placeholder="例如：叶菜类" />
              </div>


              <!-- 商品单位 -->
              <div class="modal-form-group row-layout">
                <label>商品单位：</label>
                <input
                    v-model="newProduct.unitInfo"
                    type="text"
                    placeholder="例如：斤 / 袋 / 份"
                />
              </div>

              <div class="modal-form-group row-layout">
                <label>描述：</label>
                <textarea v-model="newProduct.description" rows="2"></textarea>
              </div>


              <!-- 添加规格标题 + 按钮 -->
              <div class="spec-header">
                <label>商品规格：</label>
              </div>

              <!-- 动态规格框 -->
              <div class="spec-box" v-for="(spec, index) in specs" :key="index">

                <div class="spec-item-row">
                  <label>规格名称：</label>
                  <input v-model="newProduct.specInfo" type="text" />
                </div>

                <div class="spec-item-row">
                  <label>价格(元):</label>
                  <input type="number" v-model.number="newProduct.price" placeholder="请输入价格" />
                </div>


                <div class="spec-item-row">
                  <label>库存量：</label>
                  <input type="number" v-model.number="newProduct.stock" />
                </div>

              </div>

            </div>

            <!-- 底部按钮 -->
            <div class="modal-footer">
              <button class="cancel-btn" @click="closeAddModal">取消</button>
              <button @click="updateProduct">保存修改</button>
            </div>

          </div>
        </div>

        <!-- 购物车（仅买家） -->
        <div v-if="role === 'buyer' && currentView === 'cart'">
          <p v-if="!cartItems.length" class="empty-state">购物车暂无商品~</p>
          <div v-if="cartItems.length" class="cart-list">
            <div v-for="item in cartItems" :key="item.productId" class="cart-card">
              <div class="cart-product-image">
                <img :src="item.imageUrl || placeholder"  alt="" @error="handleImageError" />
              </div>
              <div class="cart-info">
                <h3>{{ item.productName }}</h3>
                <p>¥{{ item.price }} / {{ item.unitInfo }}</p>
                <div class="cart-quantity">
                  <button @click="changeQuantity(item.productId, item.quantity - 1)" :disabled="item.quantity <= 1">-</button>
                  <span>{{ item.quantity }}</span>
                  <button @click="changeQuantity(item.productId, item.quantity + 1)">+</button>
                </div>
              </div>
              <button class="remove-btn" @click="removeFromCart(item.productId)">移除</button>
            </div>
            <button class="submit-btn" @click="createOrder()">提交订单</button>
          </div>
          <p v-else class="empty-state">购物车是空的。</p>
        </div>

        <!-- 求购需求列表 -->
        <div v-if="currentView === 'demands'">
          <div class="request-list">
            <div v-for="request in demands" :key="request.demandId" class="request-card">
              <p><strong>用户 {{ request.buyerId }}：</strong> 想要 {{ request.productNameDesired }}</p>
              <p>需求量：{{ request.quantityDesired }} {{ request.unitDesired }}</p>
              <p>需求描述：{{ request.details }}</p>
            </div>
            <p v-if="!demands.length" class="empty-state">暂无求购需求</p>
            <!-- 买家可以发布求购需求 -->
            <button
                v-if="role !== 'farmer'"
                class="create-btn"
                @click="showCreateDemand = true"
            >
              + 发布求购需求
            </button>
          </div>



          <!-- 弹窗：发布求购需求 -->
          <div v-if="showCreateDemand" class="modal-overlay" @click.self="showCreateDemand = false">
            <div class="modal-container">
              <h3>发布求购需求</h3>

              <form @submit.prevent="handleAddDemand" class="modal-form">

                <div class="modal-form-group">
                  <label>需求产品名称：</label>
                  <input v-model="newDemand.productNameDesired" required />
                </div>

                <div class="modal-form-group">
                  <label>需求数量 (Kg)：</label>
                  <input type="number" v-model.number="newDemand.quantityDesired" required />
                </div>

                <div class="modal-form-group">
                  <label>期望最高单价 (元/Kg)：</label>
                  <input type="number" v-model.number="newDemand.maxPricePerUnit" required />
                </div>

                <div class="modal-form-group">
                  <label>期望交货日期：</label>
                  <input type="date" v-model="newDemand.deliveryDateDesired" required />
                </div>

                <div class="modal-form-group">
                  <label>补充说明：</label>
                  <textarea v-model="newDemand.details"></textarea>
                </div>

                <div class="actions-btn">
                  <button type="button" class="cancel-btn" @click="showCreateDemand = false">取消</button>
                  <button type="submit" class="submit-btn">确认发布</button>
                </div>

              </form>
            </div>
          </div>
        </div>

        <!-- 我的订单（农户 + 买家） -->
        <div v-if="role === 'buyer' && currentView === 'myOrders'">
          <div class="order-list">
            <p v-if="!myOrders.length" class="empty-state">暂无订单</p>
            <template v-if="myOrders && myOrders.length > 0">
              <div v-for="order in myOrders" :key="order.orderId" class="order-card">
                <div class="order-header">
                  <span>订单号: {{ order.orderId }}</span>
                  <span class="order-date">下单时间: {{ new Date(order.orderDate).toLocaleString() }}</span>
                  <span class="order-status" :class="getStatusClass(order.status)">
                    {{ getStatusText(order.status) }}
                  </span>
                </div>

                <div class="order-body">
                  <div v-if="order.orderItems?.length">
                    <div v-for="item in order.orderItems" :key="item.itemId" class="order-item-container">
                      <span class="item-name">{{ item.productName || '产品名未知' }}</span>
                      <span>数量: {{ item.quantity }}</span>
                      <span>单价: ¥{{ item.unitPrice }}</span>
<!--                      <button v-if="role !== 'farmer'" class="contact-btn" @click="goToChat(item.farmerId)">联系卖家</button>-->
                    </div>
                  </div>
                  <div v-else><p>此订单无产品详情</p></div>
                </div>

                <div class="order-footer">
                  <p><strong>总金额:</strong> ¥{{ order.totalAmount.toFixed(2) }}</p>

                  <div class="order-actions">
                    <button v-if="order.status === 'pending'" class="pay-btn" @click="goToPay(order.orderId)">立即支付</button>
                    <button v-if="order.status === 'pending'" class="cancel-btn" @click="cancelOrder(order.orderId)">取消订单</button>
                    <button v-if="order.status === 'completed'" class="view-btn" @click="viewDetail(order.orderId)">查看详情</button>
                    <span v-if="order.status === 'cancelled'" class="cancel-text">订单已取消</span>
                  </div>
                </div>
              </div>
            </template>
            <p v-else class="empty-state">您还没有任何订单。</p>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>


<script setup>
import {computed, onMounted, ref, watch} from 'vue'
import * as echarts from "echarts";
import axios from '../utils/axios'
import router from "@/router/index.js";
import HeaderComponent from '../components/HeaderComponent.vue';
import placeholder from "@/assets/img.png";
import {useAuthStore} from '@/stores/authStore'
import {storeToRefs} from 'pinia'
import {ElMessage} from "element-plus";
import defaultImg from '@/assets/img.png'


// ====== 主逻辑 ======
const authStore = useAuthStore()
const {userInfo, isLoggedIn, role} = storeToRefs(authStore)
const currentView = ref('products')

function switchView(view) { currentView.value = view }

onMounted(async () => {

  await loadProducts();
  await loadAllProducts();
  await loadDemands();
  if (isLoggedIn.value) {
    await loadAddresses();
    await loadCart();
    await loadMyOrders();
    if (role.value === 'farmer') {
      await loadMyProducts();
    }
  }
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

  if(val === 'myProducts'&& role.value === 'farmer') {
    loadMyProducts()
  }
})


// ====== 产品逻辑 ======
const mainCategories = ref(['蔬菜','水产','水果','肉类'])
const products = ref([])
const allSpecs = ref([])
const selectedSubcat = ref(null)
const imageFile = ref(null)

// 产品列表和分类
const myProducts = ref([]) // 后端获取的产品数组
const filterCategory = ref('')
const filterStatus = ref('all') // all / active / inactive

// 批量操作
const showBatchActions = ref(false)
const showBatch = computed(() => showBatchActions.value)
const selectedProducts = ref([])

// 计算筛选后的产品列表
const filteredProducts = computed(() => {
  let list = [...myProducts.value]
  if (filterCategory.value) {
    list = list.filter(p => p.prodCat === filterCategory.value)
  }
  if (filterStatus.value !== 'all') {
    list = list.filter(p => p.status === filterStatus.value)
  }
  return list
})



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


const showAddProductModal = ref(false)
const showEditProductModal = ref(false)

function openAddModal() {
  showAddProductModal.value = true
}

function closeAddModal() {
  showAddProductModal.value = false
  showEditProductModal.value = false
}

const imageInput = ref(null)

function triggerImageInput() {
  imageInput.value.click()
}

const newProduct = ref({
  description:'',
  productName: '',
  prodCat: '',
  prodPcat: '',
  unitInfo:''  ,
  imagePath: '',

})
//规格列表
const specs = ref([
  { specInfo: '', price: null, stock: null }
])
//添加规格
function addSpec() {
  specs.value.push({ specInfo: '', price: null, stock: null })
}
//删除规格
function removeSpec(index) {
  specs.value.splice(index, 1)
}
//添加产品
async function handleAddProduct() {
  if (!isLoggedIn.value || role.value !== 'farmer') {
    alert('只有农户才能发布产品！')
    return
  }

  if (!userInfo.value || !userInfo.value.userId) {
    alert('无法获取您的用户信息，请尝试重新登录后再发布。')
    return
  }

  try {
    for (const spec of specs.value) {
      const productData = {
        ...newProduct.value,
        specInfo: spec.specInfo,
        price: spec.price,
        stock: spec.stock,
      }

      console.log('上传的商品：',productData)
      const formData = new FormData()
      const blob = new Blob([JSON.stringify(productData)], { type: 'application/json' })
      formData.append('product', blob)

      if (imageFile.value) {
        formData.append('image', imageFile.value)
      }

      const res = await axios.post('/products/publish', formData, {
        headers: {
          'Content-Type': 'multipart/form-data'
        }
      })

    }

    alert('所有规格产品发布成功！')

    // 重置表单
    imageFile.value = null
    newProduct.value = { productName: '', prodCat: '', prodPcat: '', unitInfo: '', imagePath: '' }
    specs.value = [{ specInfo: '', price: null, stock: null }]
    currentView.value = 'myProducts'
    closeAddModal()
  } catch (err) {
    console.error('发布失败：', err)
    alert('发布失败，请稍后重试')
  }
}

async function loadProducts() {
  try {
    // 调用带分页的接口
    const res = await axios.get('/products/productName', {
    });

    // 注意：分页返回对象里才有 records
    const page = res.data;
    const records = page.records || [];

    console.log('分页信息：', page);
    records.forEach((product, index) => {
      console.log(`商品 ${index + 1}:`, product);
    });

    // 并行加载每个产品的图片
    products.value = await Promise.all(
        records.map(async (product) => {
          let imageUrl = defaultImg;

          try {
            const imageRes = await axios.get(`/products/${product.productId}/image`, {
              responseType: 'blob'
            });
            if (imageRes.data && imageRes.data.size > 0) {
              imageUrl = URL.createObjectURL(imageRes.data);
            }
            console.log('产品图片：',imageUrl);
          } catch (err) {
            console.log('产品图片加载失败，使用默认图：', product.productId, err);
            imageUrl = defaultImg;
          }

          return { ...product, imageUrl };
        })
    );

  } catch (err) {
    console.error('加载产品失败', err);
  }
}

async function loadAllProducts() {
  try {
    const res = await axios.get('/products')
    const records = res.data.records || []

    // 并行加载每个产品的图片
    allSpecs.value = await Promise.all(
        records.map(async (product) => {
          try {
            let imageUrl = ''
            try {
              const imageRes = await axios.get(`/products/${product.productId}/image`, {
                responseType: 'blob'
              })
              if (imageRes.data.size > 0)
                imageUrl = URL.createObjectURL(imageRes.data)
            } catch {
              // 如果图片加载失败，使用默认占位图
              imageUrl = new URL('../assets/img.png', import.meta.url).href
            }
            console.log('全部产品：', imageUrl)
            return {...product, imageUrl}
          } catch (err) {
            console.warn('产品加载失败:', product.productId, err)
            return {...product, imageUrl: new URL('../assets/img.png', import.meta.url).href}
          }
        })
    )
  } catch (err) {
    console.error('加载产品失败', err)
  }
}
// 分类结构：{ 大类: [小类1, 小类2, ...] }
const categoryMap = computed(() => {
  const map = {}
  for (const p of products.value) {
    if (!map[p.prodCat]) map[p.prodCat] = new Set()
    map[p.prodCat].add(p.prodPcat)
  }
  // 转换 Set 为数组
  for (const key in map) {
    map[key] = Array.from(map[key])
  }
  return map
})

function selectCategory(subcat) {
  selectedSubcat.value = subcat
}

async function loadMyProducts() {
  if (!isLoggedIn.value || role.value !== 'farmer') return;
  try {
    const res = await axios.get(`/products/farmer`);
    const records = res.data.records || []

    // 并行加载每个产品的图片
    myProducts.value = await Promise.all(
        records.map(async (product) => {
          let imageUrl = defaultImg

          if (product.hasImage === true) {
          try {
            const imageRes = await axios.get(`/products/${product.productId}/image`, {
              responseType: 'blob'
            });
            if (imageRes.data && imageRes.data.size > 0) {
              imageUrl = URL.createObjectURL(imageRes.data);
            }
          } catch (err) {
             // 忽略错误
          }
        }
        
        return { ...product, imageUrl };
      })
    );

  } catch(err) {
    console.error('加载我的产品失败', err);
  }
}

async function goToProductDetail(product) {
  if (product && product.productId) {
    await router.push(`/product/${product.productId}`);
  }
}

/**
 * 根据接收者的ID，跳转到对应的聊天页面
 * @param {number | string} receiverId - 聊天对象的ID
 */
async function goToChat(receiverId) {

  if (!isLoggedIn.value) {
    alert('请先登录才能发起聊天！');
    return;
  }

  if (!receiverId) {
    alert('无法联系，对方信息丢失。');
    return;
  }

  if (String(userInfo.value?.userId) === String(receiverId)) {
    alert('您不能和自己发起聊天。');
    return;
  }
  await router.push(`/chat/${receiverId}`);
}

//处理图片错误
const handleImageError = (e) => {
  
  e.target.src = defaultImg;

  e.target.onerror = null; 
};

const editProduct = (product) => {
  console.log('product', product);
  newProduct.value = {
    productId: product.productId,  // 必须保留 ID
    productName: product.productName,
    prodCat: product.prodCat,
    prodPcat: product.prodPcat,
    unitInfo: product.unitInfo,
    description: product.description,
    imagePath: product.imagePath || '',
    specInfo: product.specInfo,
    price: product.price,
    stock: product.stock
  }

  imageFile.value = null   // 清空旧图片
  showEditProductModal.value = true
}

async function updateProduct() {
  if (!isLoggedIn.value || role.value !== 'farmer') {
    alert('只有农户才能修改产品！')
    return
  }

  try {
    // 更新规格 + 基础信息（走 JSON PUT）
    for (const spec of specs.value) {
      const productData = {
        ...newProduct.value
      }

      console.log("更新基础信息：", productData)

      await axios.put(`/products/${newProduct.value.productId}`, productData)
    }

    // 如果有图片，就单独 PATCH 图片
    if (imageFile.value) {
      const formData = new FormData()
      formData.append("image", imageFile.value)

      console.log("上传新图片")

      await axios.patch(`/products/${newProduct.value.productId}/image`, formData, {
        headers: { "Content-Type": "multipart/form-data" }
      })
    }

    alert("商品修改成功！")
    showEditProductModal.value = false
    loadMyProducts()

  } catch (err) {
    console.error("更新失败", err)
    alert("更新失败，请稍后重试")
  }
}


// 删除商品
const deleteProduct = async (product) => {
  if (!confirm('确定删除该商品吗？')) return
  try {
    await axios.delete(`/products/${product.productId}`)
    alert('删除成功')
    await loadMyProducts()
  } catch(e) {
    console.error('删除失败', e)
  }
}

// 切换上架状态
const toggleProductStatus = async (product, checked) => {
  try {
    const status = checked ? 'active' : 'inactive'
    await axios.put(`/products/${product.productId}/status`, null, { params: { status } })
    // 同步更新本地状态，避免切换后 UI 不一致
    product.status = status
    console.log(`商品 ${product.productName} 状态更新为 ${status}`)
  } catch (e) {
    console.error('更新状态失败', e)
  }
}


// 批量操作
const toggleBatchActions = () => { showBatchActions.value = !showBatchActions.value }
const batchChoose = () => {
  const ids = filteredProducts.value.map(p => p.productId)

  // 如果当前已经全部选中 → 取消全选
  if (selectedProducts.value.length === ids.length) {
    selectedProducts.value = []
  } else {
    // 否则 → 全选
    selectedProducts.value = ids
  }
}

const batchDelete = async () => {
  if (!selectedProducts.value.length) return
  if (!confirm('确定删除选中的商品吗？')) return
  for (const id of selectedProducts.value) {
    await axios.delete(`/products/${id}`)
  }
  alert('批量删除完成')
  await loadMyProducts()
}
const batchDown = async () => {
  if (!selectedProducts.value.length) return
  for (const id of selectedProducts.value) {
    await axios.put(`/products/${id}/status`, null, { params:{ status:'inactive' }})
  }
  alert('批量下架完成')
  await loadMyProducts()
}

// 过滤条件改变时刷新列表
watch([filterStatus, filterCategory], loadMyProducts)

// ====== 预测逻辑 ======

//启动预测任务
async function startPrediction(product) {
  try {

    loading.value = true;
    const requestData = {
      labelPre: "预测",
      productName: product.productName,
      prodCat: product.prodCat,
      prodPcat: product.prodPcat,
      specInfo: product.specInfo,
      unitInfo: product.unitInfo,
      farmerId: product.farmerId,
      price: product.price,
      stock: product.stock,
      status: product.status,
    }

    const res = await axios.post('/predictions', requestData, {
      timeout: 300000 // 300秒
    }).catch(err => { console.error(err) })


    if (res.status === 202 || res.data.status === 'RUNNING') {
      // 返回 taskId
      product.taskId = res.data.taskId
      console.log(`预测任务已启动，taskId=${product.taskId}`)
      await pollPredictionStatus(product)

    } else if (res.data.status === 'SUCCESS') {
      console.log('CRUD操作成功（非预测任务）')
    }
  } catch (err) {
    console.error('启动预测任务失败', err)
  }
}

const chartRef = ref(null);
const chartData = ref([]);

//根据 taskId 轮询预测结果
async function pollPredictionStatus(product) {
  if (!product.taskId) {
    console.warn("没有 taskId，无法轮询");
    return;
  }

  let completed = false;

  try {
    while (!completed) {
      const res = await axios.get(`/predictions/status/${product.taskId}`);
      const status = res.data.status;

      if (status === "COMPLETED") {
        chartData.value = res.data.data.map(p => ({
          date: p.forecastDate,
          price: Number(p.predictedPrice),
        }));
        completed = true;
        drawChart(product.productName);
      } else if (status === "FAILED") {
        alert("预测失败: " + res.data.message);
        completed = true;
      } else {
        // RUNNING 或 202
        await new Promise(r => setTimeout(r, 10000));
      }
    }
  } catch (err) {
    console.error("轮询预测结果失败", err);
  } finally {
    loading.value = false;
  }
}

function drawChart(productName) {
  if (!chartRef.value || !chartData.value.length) return;

  const myChart = echarts.init(chartRef.value);
  chartData.value = [];

  // 只保留最近 10 条
  const recentData = chartData.value.slice(-10);
  const dates = recentData.map(d => d.date);
  const prices = recentData.map(d => d.price);

  // 计算趋势
  const trendSymbols = prices.map((price, idx) => {
    if (idx === 0) return "—";
    const diff = price - prices[idx - 1];
    if (diff > 0) return "{red|▲}";  // 红色
    if (diff < 0) return "{green|▼}"; // 绿色
    return "{gray|—}";                // 灰色
  });

  myChart.setOption({
    title: { text: productName, left: "left" },

    tooltip: {
      trigger: "axis",
      formatter: params => {
        const idx = params[0].dataIndex;
        return `${dates[idx]}<br/>价格: ¥${prices[idx]} ${trendSymbols[idx].replace(/\{.*?\|/, "").replace("}", "")}`;
      },
    },

    xAxis: { type: "category", data: dates },

    yAxis: {
      type: "value",
      axisLabel: { formatter: "¥{value}" },
      min: v => Math.floor(v.min),
      max: v => Math.ceil(v.max),
    },

    series: [
      {
        data: prices,
        type: "line",
        smooth: true,

        label: {
          show: true,
          position: "top",

          formatter: params =>
              `${params.data} ${trendSymbols[params.dataIndex]}`,

          // 使用 rich 才能让不同符号不同颜色！
          rich: {
            red: { color: "red", fontWeight: "bold" },
            green: { color: "green", fontWeight: "bold" },
            gray: { color: "gray" }
          }
        },

        // 线条颜色按趋势变化（可选）
        itemStyle: {
          color: function (params) {
            const sym = trendSymbols[params.dataIndex];
            if (sym.includes("red")) return "red";
            if (sym.includes("green")) return "green";
            return "gray";
          }
        }
      },
    ],
  });
}

// ====== 需求逻辑 ======
const demands = ref([])
const newDemand = ref({
  productNameDesired: '',
  quantityDesired: null,
  unitDesired: '',
  maxPricePerUnit: null,
  deliveryDateDesired: '',
  details: ''
})

const showCreateDemand = ref(false)

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


// ====== 订单逻辑 ======
const myOrders = ref([])
const loading = ref(true)// 时间格式化
const formatTime = t => (t ? new Date(t).toLocaleString() : '')

// 计算总价
const totalPrice = computed(() => {
  return detailedItems.value.reduce(
      (sum, item) => sum + (item.price * item.quantity),
      0
  );
})

async function handleAddDemand() {
  if (!isLoggedIn.value) {
    alert('请先登录！');
    return;
  }

  const currentUserId = userInfo.value?.userId;

  try {
    const demandToSend = {
      ...newDemand.value,
      buyerId: currentUserId
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
  if (!isLoggedIn.value) {
    console.log("用户未登录，无法加载订单");
    return;
  }

  try {

    const res = await axios.get('/orders');

    if (res.data && Array.isArray(res.data)) {
      myOrders.value = res.data;
      console.log("加载到的订单:", myOrders.value);
    } else {
      myOrders.value = [];
      console.log("未查询到订单或返回数据格式不正确");
    }
  } catch (error) {
    console.error("加载我的订单失败:", error);
    if (error.response && (error.response.status === 401 || error.response.status === 403)) {
      alert("登录已过期，请重新登录。");
      // 这里可以添加跳转到登录页的逻辑
      router.push('/login');
    } else {
      alert("加载订单失败，请稍后重试。");
    }
    myOrders.value = [];
  }finally {
    loading.value = false
  }
}

// 状态中文映射
const getStatusText = (status) => {
  switch (status) {
    case 'pending':
      return '待支付'
    case 'completed':
      return '已完成'
    case 'cancelled':
      return '已取消'
    default:
      return status || '未知状态'
  }
}

// 状态样式类（用于颜色）
const getStatusClass = (status) => {
  switch (status) {
    case 'pending':
      return 'status-pending'
    case 'completed':
      return 'status-completed'
    case 'cancelled':
      return 'status-cancelled'
    default:
      return ''
  }
}

// 取消订单
const cancelOrder = async (orderId) => {
  try {
    const res = await axios.put(`/orders/${orderId}/cancel`)
    if (res.data) {
      ElMessage.success('订单已取消')
      myOrders.value = myOrders.value.map(o =>
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


// ====== 购物车逻辑 ======
// 从后端加载购物车 + 获取产品详情
const cartItems = ref([])
const addresses = ref([]) // 地址列表
const selectedAddressId = ref(null)

// 从后端加载购物车 + 产品详情 + 图片
async function loadCart() {
  if (!isLoggedIn.value) return;
  try {

    const res = await axios.get('/cart',
        { params: { pageNum: 1, pageSize: 50 } });
    const pageData = res.data
    const items = pageData.records || []

    // 并行加载每个产品的详情和图片
    cartItems.value = await Promise.all(
        items.map(async (item) => {
          try {
            // 获取产品详情
            const productRes = await axios.get(`/products/${item.productId}`)
            const product = productRes.data

            // 尝试获取产品图片
            let imageUrl = defaultImg
            if (product.hasImage === true) {
                try {
                  const imageRes = await axios.get(`/products/${product.productId}/image`, {
                    responseType: 'blob'
                  });
                  if (imageRes.data.size > 0) {
                    imageUrl = URL.createObjectURL(imageRes.data);
                  }
                } catch {
                  // 忽略
                }
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
            console.warn('产品详情获取失败:', item.productId, err)
            return {...item, imageUrl: new URL('../assets/img.png', import.meta.url).href}
          }
        })
    )
    
  } catch (err) {
    console.error('加载购物车失败:', err)
    alert('加载购物车失败，请稍后重试')
  }
}

//从后端加载用户地址
async function loadAddresses() {
  if (!isLoggedIn.value) return;
  try {

    const res = await axios.get('/address/user');
    addresses.value = res.data || [];
    if (addresses.value.length > 0 && !selectedAddressId.value) {
      selectedAddressId.value = addresses.value[0].addressId;
    }

  } catch (err) {
    console.error('加载地址失败:', err)
  }
}

// 添加到购物车
async function addToCart(product) {
  if (!isLoggedIn.value) { alert('请先登录！'); return; }
  try {

    const res = await axios.post('/cart/add', null, {
      params: { productId: product.productId, quantity: 1 }
    });

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
  if (newQty <= 0 || !isLoggedIn.value) return
  try {

    const res = await axios.post('/cart/update', null, {
      params: { productId, quantity: newQty }
    });

    if (res.data) {
      const item = cartItems.value.find(i => i.productId === productId)
      if (item) item.quantity = newQty
    }
  } catch (err) {
    console.error('修改数量失败', err)
  }
}

// 移除产品
async function removeFromCart(productId) {
  if (!isLoggedIn.value) return;
  try {
    const res =  await axios.delete('/cart/item',
        { params: { productId } })
    if (res.data) {
      alert('移除成功')
      loadCart()
    }
  } catch (err) {
    console.error('移除失败', err)
  }
}

async function createOrder() {

  if (!isLoggedIn.value) {
    alert('请先登录！');
    return;
  }

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

    console.log('即将发送到后端的订单数据:', JSON.stringify(reqBody, null, 2));

    const res = await axios.post('/orders', reqBody);

    if (res.data) {
      const orderId = res.data;
      alert(`订单提交成功！`)
      cartItems.value = []
      await router.push(`/orders/${orderId}`)
    } else {
      alert('提交失败，请稍后再试')
    }

    await axios.post('/cart/clear');

  } catch (err) {
    console.error('提交订单失败:', err)
    alert('提交失败，请检查登录状态或网络')
  }
}


</script>

<style scoped>


/* 左侧分类栏 */
.category-sidebar {
  width: 100px;
  background-color: #f8f8f8;
  border-radius: 8px;
  font-size: 13px;
}

.cat-title {
  font-weight: bold;
  font-size: 20px;
  margin: 6px 0 3px;
  color: #2e7d32;
}

.subcat-list {
  margin: 10px;
  list-style: none;
  padding: 0;
}

.subcat-item {
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 4px;
  transition: background-color 0.2s;
}

.subcat-item:hover {
  background-color: #e0f2f1;
}

.subcat-item.active {
  background-color: #a5d6a7;
  color: #fff;
}

/*产品样式*/
.product-page {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
}

.product-list {
  margin-left:10px;
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-start;
  gap: 16px;
  align-items: flex-start;
}

/* 产品卡片 */
.product-card {
  width: 200px;
  height: auto;
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  transition: transform 0.2s, box-shadow 0.2s;
  display: flex;
  flex-direction: column;
}

.product-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
}

/* 图片部分 */
.product-image {
  position: relative;
  width: 100%;          /* 容器宽度可固定或百分比 */
  max-width: 400px;     /* 可选最大宽度 */
  height: 120px;        /* 固定高度 */
  background: #f5f5f5;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  border-radius: 12px;
}

.product-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;    /* 图片裁剪填充容器 */
  object-position: center; /* 居中裁剪 */
  border-radius: 12px;  /* 可选，让图片圆角和容器一致 */
}

/* 在图片下方叠加一个渐变层 */
.product-image::after {
  content: "";
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 20%;
  background: linear-gradient(to bottom, rgba(255, 255, 255, 0) 0%, #ffffff 100%);
}
/* 文字信息部分 */
.product-info {
  padding: 8px 10px;
  font-size: 13px;
  text-align: left;
}

.product-name {
  font-size: 15px;
  font-weight: 600;
  color: #333;
  margin: 4px 0;
}

.product-price {
  color: #e91e63;
  font-weight: bold;
  margin: 2px 0;
}

.product-stock {
  color: #777;
  font-size: 12px;
}

.view-btn,
.add-to-cart-btn,
。contanct-btn{
  flex: 1;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 12px;
  padding: 6px;
  transition: background 0.2s;
}

.view-btn {
  background-color: #81c784;
  color: white;
}

.view-btn:hover {
  background-color: #66bb6a;
}

.add-to-cart-btn {
  background-color: #4caf50;
  color: white;
}

.add-to-cart-btn:hover {
  background-color: #43a047;
}

.contact-btn {
  background-color: #2196F3; /* 蓝色背景 */
  color: white;
}

.contact-btn:hover {
  background-color: #1976D2; /* 鼠标悬停时颜色变深 */
}

.add-product-form { margin: 0 auto;  display: flex ; flex-direction: column; align-content: center; max-width: 600px; margin-top: 20px; }
.form-group { display: flex; align-items: center; margin-bottom: 20px; }
.form-group label { width: 120px; font-weight: bold; }
.form-group input,.form-group select{ flex: 1; padding: 10px; border: 1px solid #ccc; border-radius: 4px; }
.form-group textarea { flex: 1; padding: 10px; border: 1px solid #ccc; border-radius: 4px; }


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
  background-color: #52c41a;
  color: #fff;
  cursor: pointer;
}

.add-demand-btn:hover {
  background-color: #52c41a;
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

.actions-btn{
  display: flex;
  justify-content: space-between;
}
.submit-btn {
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
  background-color: #52c41a;
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

.cart-product-image {
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

.cart-product-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 8px;
}

.order-card {
  border: 1px solid #eee;
  border-radius: 10px;
  margin-bottom: 20px;
  background: #fff;
  padding: 15px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
}

.order-item-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  background-color: #f9f9f9;
  border-radius: 6px;
  border: 1px solid #f0f0f0;
}

.item-info {
  display: flex;
  flex-direction: column; /* 让产品名和详情上下排列 */
  gap: 5px; /* 上下间距 */
}

.item-name {
  font-weight: 600;
  color: #333;
}

.item-details {
  display: flex;
  gap: 20px;
  font-size: 0.9em;
  color: #666;
}

.item-actions .contact-btn {
  background-color: #2196F3; /* 蓝色背景 */
  color: white;
  border: none;
  padding: 6px 12px;
  border-radius: 5px;
  cursor: pointer;
  font-size: 0.85em;
  transition: background-color 0.3s;
}

.item-actions .contact-btn:hover {
  background-color: #1976D2;
}

.order-footer {
  display: flex;
  justify-content: flex-end; /* 让内容靠右 */
  align-items: center;
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

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: #f8f8f8;
  padding: 10px;
  border-bottom: 1px solid #ddd;
}

.order-status {
  font-weight: bold;
}

.status-pending {
  color: #ff9800; /* 橙色 */
}
.status-completed {
  color: #4caf50; /* 绿色 */
}
.status-cancelled {
  color: #f44336; /* 红色 */
}

.order-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px;
  border-top: 1px solid #ddd;
}

.order-actions {
  display: flex;
  gap: 8px;
}

.pay-btn {
  background-color: #52c41a;
  color: white;
  border: none;
  padding: 6px 12px;
  border-radius: 6px;
  cursor: pointer;
}
.cancel-btn {
  background-color: #f44336;
  color: white;
  border: none;
  padding: 6px 12px;
  border-radius: 6px;
  cursor: pointer;
}
.view-btn {
  background-color: #52c41a;
  color: white;
  border: none;
  padding: 6px 12px;
  border-radius: 6px;
  cursor: pointer;
}
.cancel-text {
  color: #999;
  font-size: 14px;
}

 .prediction-page {
   display: flex;
   gap: 20px;
 }

.prediction-chart {
  flex: 1;
}
</style>

//我的产品样式
<style scoped>
.my-product-page {
  padding: 15px;
  font-size: 14px;
  color: #333;
}

.product-management-bar {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 12px;
  margin-bottom: 15px;
}

.product-management-bar button,
.product-management-bar select {
  padding: 6px 12px;
  border-radius: 6px;
  border: 1px solid #52c41a;
  background-color: white;
  cursor: pointer;
}

.product-management-bar button:hover,
.product-management-bar select:hover {
  border-color: #389e0d;
}

.product-management-bar .add-btn {
  border: 1px solid #52c41a;
  background-color: white;
  color: #52c41a;
}

.status-filters {
  display: flex;
  align-items: center;
  gap: 6px;
}

.status-filters button {
  border: 1px solid #52c41a;
  background: white;
  color: #52c41a;
}

.status-filters button.active {
  background: #52c41a;
  color: white;
}

.batch-operations {
  position: relative;
}

.batch-dropdown {
  position: absolute;
  top: 100%;
  left: 0;
  display: flex;
  flex-direction: column;
  border: 1px solid #52c41a;
  border-radius: 6px;
  background: white;
  margin-top: 4px;
}

.batch-dropdown button {
  border: none;
  background: none;
  padding: 6px 10px;
  text-align: left;
  cursor: pointer;
}

.batch-dropdown button:hover {
  background-color: #f0f9eb;
  color: #389e0d;
}

/* 表头 */
.product-list-header {
  display: flex;
  padding: 8px 0;
  border-bottom: 2px solid #e0e0e0;
  font-weight: normal;
}

.product-list-header .col {
  display: flex;
  align-items: center;
  justify-content: center;
  text-align: center;
}

.checkbox-col { width: 5%; }
.img-col { width: 10%; height:100%}
.name-col { width: 20%; text-align: left; padding-left: 5px; }
.price-col { width: 15%; }
.stock-col { width: 15%; }
.status-col { width: 15%; }
.action-col { width: 20%; display: flex; gap: 8px; justify-content: flex-start; }

/* 产品列表 */
.my-product-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.product-row {
  width: 100%;
  display: flex;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px solid #e0e0e0;
  height: 80px;
}
.product-row .checkbox-col { width: 5%; }
.product-row .img-col { width: 10%; }
.product-row .name-col { width: 20%; text-align: left; padding-left: 5px; }
.product-row .price-col { width: 15%; }
.product-row .stock-col { width: 15%; }
.product-row .status-col { width: 15%; }
.product-row .action-col { width: 20%; display: flex; gap: 8px; justify-content: flex-start; }

.product-row .col {
  display: flex;
  align-items: center;
  justify-content: center;
}

.product-row .img-col {
  aspect-ratio: 1 / 1;  /* 保持正方形 */
  border-radius: 6px;
}

.product-row .img-col img {
  padding-top: 0;
  max-height: 100%;     /* 不超过 row 高度 */
  width: auto;           /* 宽度自动按比例缩放 */
  object-fit: cover;    /* 填充列，如果宽高超出会裁剪 */
  border-radius: 6px;
}

.product-row .action-col button {
  border: none;
  background: #52c41a;
  color: white;
  padding: 4px 10px;
  border-radius: 6px;
  cursor: pointer;
}

.product-row .action-col button:hover {
  background: #389e0d;
}

/* 开关容器 */
.switch {
  position: relative;
  display: inline-block;
  width: 40px;   /* 开关宽度 */
  height: 20px;  /* 开关高度 */
}

/* 隐藏原始复选框 */
.switch input {
  opacity: 0;
  width: 0;
  height: 0;
}

/* 滑块 */
.slider {
  position: absolute;
  cursor: pointer;
  top: 0; left: 0; right: 0; bottom: 0;
  background-color: #ccc;
  border-radius: 20px;
  transition: 0.3s;
}

/* 滑块圆点 */
.slider::before {
  position: absolute;
  content: "";
  height: 16px;
  width: 16px;
  left: 2px;
  bottom: 2px;
  background-color: white;
  border-radius: 50%;
  transition: 0.3s;
}

/* 打勾状态 */
input:checked + .slider {
  background-color: #52c41a;
}

input:checked + .slider::before {
  transform: translateX(20px);
}

</style>

//添加产品样式
<style scoped>

/* 上传图片 */
.image-upload-box {
width: 120px;
height: 120px;
border: 1px dashed #67c23a;
border-radius: 8px;
display: flex;
justify-content: center;
align-items: center;
cursor: pointer;
overflow: hidden;
}

.image-upload-box img {
width: 100%;
height: 100%;
object-fit: cover;
}

/* 规格标题 */
.spec-header {
display: flex;
justify-content: space-between;
align-items: center;
}

.add-spec-btn {
background: none;
border: 1px solid #67c23a;
padding: 5px 10px;
border-radius: 4px;
cursor: pointer;
color: #67c23a;
}

/* 每个规格盒子 */
.spec-box {
border: 1px solid #e5e5e5;
border-radius: 8px;
padding: 10px;
margin-bottom: 12px;
}

.spec-item-row {
display: flex;
flex-direction: row;
margin-bottom: 10px;
  font-size: 16px;
}

.spec-item-row  label{
  width:80px;
  display: inline-block;
  text-align: justify;
}

.spec-item-row label::after {
  content: '';
  display: inline-block;
  width: 100%;
}

.spec-item-row  input,
.spec-item-row  textarea,
.spec-item-row  select {
  flex: 1;
  width: 100%;
  height: 35px;
  padding: 6px 10px;
  border: 1px solid #ccc;
  border-radius: 6px;
  outline: none;
}
.delete-spec-btn {
background: none;
border: none;
color: red;
cursor: pointer;
font-size: 14px;
}

/* 底部按钮 */
.modal-footer {
display: flex;
justify-content: space-between;
margin-top: 15px;
}

.cancel-btn {
background: none;
border: 0.5px solid #cccccc;
padding: 8px 20px;
border-radius: 5px;
color: black ;
}

.save-btn {
background: #52c41a;
border: none;
padding: 8px 20px;
border-radius: 5px;
color: #fff;
}
</style>

/* ======= 行情大厅 ======= */
<style scoped>
.prediction-wrapper {
  display: flex;
  gap: 20px;
  padding: 15px;
  width: 100%;
  box-sizing: border-box;
  align-items: stretch;
}

/* 左侧宽度占比大 */
.prediction-left {
  flex: 3;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

/* 右侧独立预测栏 */
.prediction-right {
  flex: 2;
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 3px 12px rgba(0,0,0,0.12);
  top: 10px;
}

/* 标题 */
.predict-title {
  font-size: 20px;
  font-weight: 600;
  margin-bottom: 15px;
  color: #333;
}

/* 图表区域 */
.chart-box {
  width: 100%;
  height: 100%;
}


/* ======= 横向产品卡片 ======= */
.product-card-horizontal {
  display: flex;
  align-items: center;
  gap: 15px;
  background: #fff;
  border-radius: 12px;
  padding: 12px 14px;
  box-shadow: 0 3px 10px rgba(0,0,0,0.1);
  transition: 0.2s;
}

.product-card-horizontal:hover {
  transform: translateY(-3px);
  box-shadow: 0 5px 16px rgba(0,0,0,0.15);
}

/* 商品图 */
.h-img {
  width: 120px;
  height: 120px;
  border-radius: 10px;
  object-fit: cover;
  background: #f5f5f5;
}

/* 信息部分 */
.h-info {
  flex: 1;
}

.h-info h3 {
  font-size: 18px;
  font-weight: 600;
  margin: 4px 0;
}

.h-info p {
  font-size: 14px;
  color: #666;
  margin: 2px 0;
}

/* 按钮（与你现在的绿色按钮风格一致） */
.h-btn{
  background-color: #4caf50;
  color: white;
  border: none;
  border-radius: 6px;
  padding: 8px 12px;
  cursor: pointer;
  transition: 0.2s;
  width: 100px;
}

.h-btn:hover {
  background-color: #43a047;
}
.create-btn{
  background-color: #4caf50;
  color: white;
  border: none;
  border-radius: 6px;
  padding: 8px 12px;
  cursor: pointer;
  transition: 0.2s;
  width: 180px;
}

.create-btn:hover {
  background-color: #43a047;
}
.h-btn:disabled {
  background: #9ca3af;
  cursor: not-allowed;
}
.modal-container h3{
  padding-bottom: 10px;
  border-bottom: 1px solid #369870;
  margin-bottom: 20px;
}

.modal-form-group label {
  width:22%;
}

.modal-form-group input,
.modal-form-group textarea,
.modal-form-group select {
  width: 78%;
}

</style>