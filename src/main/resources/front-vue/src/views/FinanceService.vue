<template>
  <div class="main-bg">
    <HeaderComponent />

    <section class="content">

      <!-- 顶部导航 -->
      <nav class="main-nav">
        <button @click="switchView('products')" :class="{ active: currentView === 'products' }">
          融资产品
        </button>
        <button @click="switchView('applications')" :class="{ active: currentView === 'applications' }">
          融资申请
        </button>
      </nav>


      <!--融资产品（无角色限制-->
      <div v-if="currentView === 'products'" class="view-wrapper">

          <!-- 表格头 -->
          <div class="items-table-header">
            <span>产品名称</span>
            <span>银行</span>
            <span>期限</span>
            <span>利率</span>
            <span>额度范围</span>
            <span>操作</span>
          </div>

          <!-- 产品行 -->
          <div
              class="items-table-row"
              v-for="p in products"
              :key="p.productId"
          >
            <span>{{ p.productName }}</span>
            <span>{{ p.bankName }}</span>
            <span>{{ p.termMonths }} 月</span>
            <span>{{ p.interestRate }}%</span>
            <span>{{ p.minAmount }} - {{ p.maxAmount }} 元</span>
            <span>
      <button @click="viewProduct(p)" class="detail-btn">查看详情</button>
      <button
          v-if="role === 'bank'"
          @click="deleteProduct(p.productId)"
          class="delete-btn"
      >
        删除
      </button>
    </span>
          </div>

          <p v-if="!products.length" class="empty-state">暂无融资产品</p>

          <!-- bank 才能创建产品 -->
          <button
              v-if="role === 'bank'"
              class="create-btn"
              @click="showCreateProduct = true"
          >
            + 发布新产品
          </button>

        <!-- 弹窗：银行发布新产品 -->
        <div v-if="showCreateProduct" class="modal-overlay" @click.self="showCreateProduct = false">
          <div class="model-container">
            <h3>发布新产品</h3>

            <form @submit.prevent="submitNewProduct" class="modal-form">

              <div class="modal-form-group">
                <label>产品名称：</label>
                <input v-model="newProduct.productName" required />
              </div>

              <div class="modal-form-group">
                <label>描述：</label>
                <textarea v-model="newProduct.description" required></textarea>
              </div>

              <div class="modal-form-group">
                <label>期限（月）：</label>
                <input type="number" v-model="newProduct.termMonths" required />
              </div>

              <div class="modal-form-group">
                <label>利率：</label>
                <input type="number" step="0.01" v-model="newProduct.interestRate" required />
              </div>

              <div class="modal-form-group">
                <label>最小额度：</label>
                <input type="number" v-model="newProduct.minAmount" required />
              </div>

              <div class="modal-form-group">
                <label>最大额度：</label>
                <input type="number" v-model="newProduct.maxAmount" required />
              </div>

              <div class="form-actions">
                <button class="submit-btn" type="submit">提交</button>
                <button class="cancel-btn" type="button" @click="showCreateProduct = false">取消</button>
              </div>

            </form>
          </div>
        </div>

        <!-- 弹窗：查看产品详情 -->
        <div v-if="showProductDetail" class="modal-overlay" @click.self="closeProductDetail">
          <div class="model-container product-detail-modal">
            <div class="modal-title-div">
              <h3 class="modal-title">产品编号 #{{ currentProduct.productId }}</h3>
              <button @click="closeProductDetail" class="modal-close-btn">关闭</button>
            </div>


            <!-- 产品信息 -->
            <div class="product-info">
              <div class="form-row">
                <label>银行：</label>
                <span>{{ currentProduct.bankName }}</span>
              </div>
              <div class="form-row">
                <label>名称：</label>
                <span>{{ currentProduct.productName }}</span>
              </div>
              <div class="form-row">
                <label>描述：</label>
                <span>{{ currentProduct.description }}</span>
              </div>
              <div class="form-row">
                <label>期限：</label>
                <span>{{ currentProduct.termMonths }} 月</span>
              </div>
              <div class="form-row">
                <label>利率：</label>
                <span>{{ currentProduct.interestRate }}%</span>
              </div>
              <div class="form-row">
                <label>申请额度：</label>
                <span>({{ currentProduct.minAmount }} - {{ currentProduct.maxAmount }}) 元</span>
              </div>
            </div>

            <!-- 农户发起申请 -->
            <div v-if="role === 'farmer'" class="reply-section">
              <h4 class="section-title">申请贷款</h4>

              <form @submit.prevent="applyProduct" class="offer-item">
                <div class="modal-form-group">
                  <label>融资金额 (元):</label>
                  <input type="number" v-model.number="newApp.amount" required min="1000" />
                </div>
                <div class="modal-form-group">
                  <label>资金用途:</label>
                  <input type="text" v-model="newApp.purpose" required />
                </div>

                <div class="form-actions">
                  <button type="submit" class="submit-btn">提交申请</button>
                </div>
              </form>
            </div>

          </div>
        </div>


      </div>

      <!-- 融资申请（farmer / bank 都能访问）-->
      <div v-if="currentView === 'applications'" class="view-wrapper">

        <!-- 表格头 -->
        <div class="items-table-header">
          <span>申请编号</span>
          <span>申请人</span>
          <span>金额</span>
          <span>期限</span>
          <span>状态</span>
          <span>操作</span>
        </div>

        <!-- 申请列表 -->
        <div
            class="items-table-row"
            v-for="app in applications"
            :key="app.financingId"
        >
          <span>{{ app.financingId }}</span>
          <span>{{ app.farmerName }}</span>
          <span>{{ app.amount }} 元</span>
          <span>{{ app.term }} 月</span>
          <span>{{ app.applicationStatus }}</span>
          <span>
      <button @click="openApplicationDetail(app)" class="detail-btn">
        查看详情
      </button>
    </span>
        </div>

        <p v-if="!applications.length" class="empty-state">暂无申请</p>

        <!-- farmer 可以发起申请 -->
        <button
            v-if="role === 'farmer'"
            class="create-btn"
            @click="showCreateApplication = true"
        >
          + 发起申请
        </button>

      </div>


    </section>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import axios from '../utils/axios'
import { useAuthStore } from '@/stores/authStore'
import { storeToRefs } from 'pinia'
import HeaderComponent from '../components/HeaderComponent.vue';

const authStore = useAuthStore()
const { role } = storeToRefs(authStore)
const products = ref([])
const newProduct = ref({ productName: '', description: '', termMonths: '', interestRate: '', minAmount: '', maxAmount: '' })
// 当前视图：applications / product
const currentView = ref('applications')
// 产品详情弹窗
const showProductDetail = ref(false)
const currentProduct = ref(null)

// 查看产品详情
const viewProduct = (product) => {
  currentProduct.value = { ...product }
  showProductDetail.value = true
}

// 关闭产品详情弹窗
const closeProductDetail = () => {
  showProductDetail.value = false
  currentProduct.value = null
}

// 农户申请产品
const applyProduct = async () => {
  try {
    if (!currentProduct.value) return alert('无效产品')
    if (!newApp.value.amount || !newApp.value.purpose) return alert('请填写完整信息')

    await axios.post('/financing/apply-product', {
      productId: currentProduct.value.productId,
      amount: newApp.value.amount,
      purpose: newApp.value.purpose,
      coApplicantIds: []
    })

    alert('贷款申请已提交')
    showDetail.value = false
    newApp.value = { amount:'', purpose:'', term:'', details:'' }
    await loadApplications()
  } catch (err) {
    console.error(err)
    alert('提交失败')
  }
}
// 弹窗控制
const showDetail = ref(false)
const showCreateApplication = ref(false)

// 数据
const applications = ref([])
const currentApp = ref({})
const newApp = ref({
  amount: '',
  purpose: '',
  term: '',
  details: ''
})

// 银行回复表单
const newReply = ref({
  offeredInterestRate: '',
  offeredAmount: '',
  bankNotes: ''
})

// 切换视图
const switchView = (v) => currentView.value = v

// 打开申请详情弹窗
const openApplicationDetail = (app) => {
  currentApp.value = { ...app }
  // 如果没有 reply，初始化为空数组
  currentApp.value.reply = app.reply || []
  showDetail.value = true
  // 初始化银行回复表单（只在银行角色显示）
  if (role.value === 'bank') {
    newReply.value = { offeredInterestRate: '', offeredAmount: '', bankNotes: '' }
  }
}

// 关闭详情弹窗
const closeDetail = () => showDetail.value = false

// 农户提交融资申请
const submitNewApplication = async () => {
  try {
    await axios.post('/financing/create', newApp.value)
    alert('申请已提交！')
    showCreateApplication.value = false
    newApp.value = { amount: '', purpose: '', term: '', details: '' }
    await loadApplications()
  } catch (err) {
    console.error(err)
    alert('提交失败')
  }
}

const showCreateProduct = ref(false)

// 银行发布新产品
const submitNewProduct = async () => {
  try {
    if (newProduct.value.maxAmount < newProduct.value.minAmount) {
      return alert('最大额度不能小于最小额度')
    }
    await axios.post('/bank/products', newProduct.value)
    alert('产品发布成功！')
    showCreateProduct.value = false
    newProduct.value = {
      productName: '',
      description: '',
      termMonths: '',
      interestRate: '',
      minAmount: '',
      maxAmount: ''
    }
  } catch (err) {
    console.error(err)
    alert('发布失败，请稍后重试')
  }
}

// 银行提交批复
const submitReply = async () => {
  try {
    if (!currentApp.value.financingId) return
    await axios.post('/financing/offer/submit', {
      financingId: currentApp.value.financingId,
      offeredAmount: newReply.value.offeredAmount,
      interestRate: newReply.value.offeredInterestRate,
      bankNotes: newReply.value.bankNotes
    })
    alert('已提交银行批复')
    await loadApplications()
    showDetail.value = false
  } catch (err) {
    console.error('提交批复失败:', err)
    alert('提交失败，请稍后重试')
  }
}

// 农户接受银行方案
const acceptOffer = async (offerId) => {
  try {
    await axios.post('/financing/offer/accept', null, { params: { offerId } })
    alert('已接受银行方案')
    await loadApplications()

    //  自动更新弹窗数据
    if (showDetail.value && currentApp.value?.appId) {
      const updated = applications.value.find(a => a.appId === currentApp.value.appId)
      if (updated) {
        currentApp.value = { ...updated }
        currentApp.value.reply = updated.reply || []
      }
    }

  } catch (err) {
    console.error('接受失败', err)
  }
}

// 农户拒绝银行方案
const rejectOffer = async (offerId) => {
  try {
    await axios.post('/financing/offer/reject', null, { params: { offerId } })
    alert('已拒绝银行方案')
    await loadApplications()
    //  自动更新弹窗数据
    const updated = applications.value.find(a => a.appId === currentApp.value.appId)
    if (updated) {
      openApplicationDetail(updated)
    }

  } catch (err) {
    console.error('拒绝失败', err)
  }
}

// 加载融资申请列表
const loadApplications = async () => {
  try {
    const url = role.value === 'bank' ? '/financing/submitted' : '/financing/my'
    const res = await axios.get(url)
    const apps = res.data.records || []

    // 获取农户姓名
    for (const app of apps) {
      try {
        const userRes = await axios.get(`/user/info/${app.initiatingFarmerId}`)
        app.farmerName = userRes.data.user?.userName || '未知农户'
      } catch {
        app.farmerName = '未知农户'
      }
    }

    // 获取银行回复
    applications.value = await Promise.all(apps.map(async app => {
      try {
        const offerRes = await axios.get('/financing/offers', { params: { financingId: app.financingId } })
        app.reply = offerRes.data && offerRes.data.length > 0 ? offerRes.data : []
      } catch {
        app.reply = []
      }
      return app
    }))
  } catch (err) {
    console.error('加载申请失败:', err)
  }
}

// 加载银行产品
const loadProducts = async () => {
  try {
    const url = role.value === 'bank' ? '/bank/products/my' : '/bank/products/all'
    const res = await axios.get(url, { params: { pageNum: 1, pageSize: 20 } })
    const list = res.data.records || res.data || []

    for (const item of list) {
      try {
        const bankRes = await axios.get(`/user/info/${item.bankUserId}`)
        item.bankName = bankRes.data.user?.userName || '未知银行'
      } catch {
        item.bankName = '未知银行'
      }
    }
    products.value = list
  } catch (err) { console.error('加载产品失败:', err) }
}

// 初始加载
onMounted(async () => {
  await loadApplications()
  await loadProducts()
})

watch(currentView, val => {
  if (val === 'applications') loadApplications()
  if (val === 'product') loadProducts()
})

</script>

/*产品详情*/
<style scoped>
/* 通用弹窗 */
.modal-overlay {
  position: fixed;
  inset: 0; /* top/right/bottom/left 0 */
  background: rgba(0,0,0,0.4);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.model-container {
  background: #ffffff;
  border-radius: 16px;
  padding: 24px;
  width: 420px;
  max-width: 90%;
  box-shadow: 0 8px 20px rgba(0,0,0,0.15);
  transition: transform 0.2s ease;
}

/* 弹窗标题 */
.model-container h3 {
  font-size: 20px;
  font-weight: 600;
  margin-bottom: 16px;
  color: #2D7D4F;
  text-align: center;
}

/* 弹窗按钮 */
.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 10px;
}

.submit-btn, .add-btn, .create-btn {
  background-color: #52c41a;
  color: #fff;
  border: none;
  padding: 10px 18px;
  border-radius: 12px;
  cursor: pointer;
  font-weight: 500;
  transition: background 0.2s;
}
.submit-btn:hover,
.add-btn:hover,
.create-btn:hover {
  background-color: #4CAF50;
}

.cancel-btn {
  background-color: #e0e0e0;
  color: #555;
  border: none;
  padding: 10px 18px;
  border-radius: 12px;
  cursor: pointer;
  transition: background 0.2s;
}
.cancel-btn:hover {
  background-color: #cfcfcf;
}

/* 申请列表卡片 */
/* 表格头 */
.items-table-header {
  display: flex;
  font-weight: bold;
  padding: 12px;
  background: #f5f5f5;
  border-radius: 8px;
  gap: 12px;
}

/* 表格行 */
.items-table-row {
  display: flex;
  padding: 12px;
  border-bottom: 1px solid #eee;
  align-items: center;
  gap: 12px;
}

/* 每列根据需要可设置固定宽度或 flex-grow */
.items-table-header span,
.items-table-row span {
  flex: 1; /* 平均分布 */
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* 操作按钮 */
.detail-btn {
  padding: 4px 12px;
  background-color: #409eff;
  color: #fff;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.detail-btn:hover {
  background-color: #66b1ff;
}


/* 查看详情按钮 */
.detail-btn {
  background-color: #52c41a;
  color: white;
  padding: 8px 14px;
  border-radius: 10px;
  border: none;
  cursor: pointer;
  transition: background 0.2s;
}

.detail-btn:hover {
  background-color: #4CAF50;
}

/* 查看详情按钮 */
.delete-btn {
  background-color: #c82333;
  border-color: #bd2130;
  color: white;
  padding: 8px 14px;
  border-radius: 10px;
  border: none;
  cursor: pointer;
  transition: background 0.2s;
}

.delete-btn:hover {
  background-color: #a71d2a;
}


/* 回复区 */
.reply-section {
  background: #f6fff6;
  border: 1px solid #52c41a;
  border-radius: 14px;
  padding: 12px;
  margin-top: 16px;
}

.reply-block {
  background: #f7fff5;
  border-left: 4px solid #52c41a;
  border-radius: 10px;
  padding: 12px;
  margin-bottom: 12px;
}

.reply-block p {
  margin: 4px 0;
  font-size: 14px;
  color: #333;
}

/* 银行操作按钮 */
.bank-action-buttons {
  display: flex;
  justify-content: center;
  gap: 20px;
  margin-top: 10px;
}

.accept-btn, .reject-btn {
  width: 90px;
  padding: 8px 0;
  border-radius: 10px;
  font-weight: 500;
  color: #fff;
  cursor: pointer;
  border: none;
  transition: background 0.2s;
}

.accept-btn {
  background-color: #52c41a;
}
.accept-btn:hover {
  background-color: #4CAF50;
}

.reject-btn {
  background-color: #d9534f;
}
.reject-btn:hover {
  background-color: #c9302c;
}

/* 银行回复表单 */
.reply-form {
  background: #f9fff8;
  border: 1px solid #52c41a;
  border-radius: 14px;
  padding: 14px;
  margin-top: 16px;
}

.form-row {
  font-size: 16px;
  display: flex;
  align-items: center;
  margin-bottom: 12px;
}

.form-row label {
  width: 25%;
  font-weight: 600;
  color: #333;
}

.form-row span {
  width: 75%;
  color: #333;
  font-weight: 500;
}

.form-row input,
.form-row textarea {
  width: 70%;
  padding: 8px 10px;
  border-radius: 10px;
  border: 1px solid #ccc;
  outline: none;
}

.form-row input:focus,
.form-row textarea:focus {
  border-color: #52c41a;
}

.action-buttons {
  font-size: 16px;
  display: flex;
  justify-content: space-between;
  gap: 12px;
  margin-top: 12px;
}

</style>

