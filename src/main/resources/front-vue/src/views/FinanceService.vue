<template>
  <div class="main-bg">
    <HeaderComponent />

    <section class="content">

      <!-- 顶部导航 -->
      <nav class="main-nav">
        <button @click="switchView('products')" :class="{ active: currentView === 'products' }">
          所有融资产品
        </button>
        <button @click="switchView('applications')" :class="{ active: currentView === 'applications' }">
          所有融资申请
        </button>
      </nav>


      <!--所有融资产品（无角色限制-->
      <div v-if="currentView === 'products'" class="view-wrapper">

        <!-- 产品列表 -->
        <div
            class="item-card"
            v-for="p in products"
            :key="p.productId"
        >
          <div class="item-left">
            <h3>{{ p.productName }}</h3>
            <p><strong>银行：</strong>{{ p.bankName }}</p>
            <p><strong>期限：</strong>{{ p.termMonths }} 月</p>
            <p><strong>利率：</strong>{{ p.interestRate }}%</p>
            <p><strong>额度范围：</strong>{{ p.minAmount }} - {{ p.maxAmount }} 元</p>
          </div>

          <div class="item-right">
            <button @click="viewProduct(p)" class="detail-btn">查看详情</button>

            <button
                v-if="role === 'bank'"
                @click="deleteProduct(p.productId)"
                class="delete-btn"
            >
              删除
            </button>
          </div>
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
          <div class="modal-content">
            <h3>发布新产品</h3>

            <form @submit.prevent="submitNewProduct" class="modal-form">

              <div class="form-group">
                <label>产品名称：</label>
                <input v-model="newProduct.productName" required />
              </div>

              <div class="form-group">
                <label>描述：</label>
                <textarea v-model="newProduct.description" required></textarea>
              </div>

              <div class="form-group">
                <label>期限（月）：</label>
                <input type="number" v-model="newProduct.termMonths" required />
              </div>

              <div class="form-group">
                <label>利率：</label>
                <input type="number" step="0.01" v-model="newProduct.interestRate" required />
              </div>

              <div class="form-group">
                <label>最小额度：</label>
                <input type="number" v-model="newProduct.minAmount" required />
              </div>

              <div class="form-group">
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
          <div class="modal-content product-detail-modal">
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
                <div class="form-group">
                  <label>融资金额 (元):</label>
                  <input type="number" v-model.number="newApp.amount" required min="1000" />
                </div>
                <div class="form-group">
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

      <!--所有融资申请（farmer / bank 都能访问）-->
      <div v-if="currentView === 'applications'" class="view-wrapper">

        <!-- 申请列表 -->
        <div
            class="item-card"
            v-for="app in applications"
            :key="app.financingId"
        >
          <div class="item-left">
            <h3>申请编号 #{{ app.financingId }}</h3>
            <p><strong>申请人：</strong>{{ app.farmerName }}</p>
            <p><strong>金额：</strong>{{ app.amount }} 元</p>
            <p><strong>期限：</strong>{{ app.term }} 月</p>
            <p><strong>状态：</strong>{{ app.applicationStatus }}</p>
          </div>

          <div class="item-right">
            <button @click="openApplicationDetail(app)" class="detail-btn">
              查看详情
            </button>
          </div>
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


        <!-- 弹窗：申请详情 -->
        <div v-if="showDetail" class="modal-overlay" @click.self="closeDetail()">
          <div class="modal-content">
            <h3>申请详情</h3>

            <div class="form-row"><label>ID：</label><span>{{ currentApp.financingId }}</span></div>
            <div class="form-row"><label>金额：</label><span>{{ currentApp.amount }} 元</span></div>
            <div class="form-row"><label>用途：</label><span>{{ currentApp.purpose }}</span></div>
            <div class="form-row"><label>期限：</label><span>{{ currentApp.term }} 月</span></div>
            <div class="form-row"><label>状态：</label><span>{{ currentApp.applicationStatus }}</span></div>

            <hr />

            <!-- 银行回复区 -->
            <div v-if="role === 'farmer'">
              <h4>银行回复</h4>

              <div v-if="currentApp.reply && currentApp.reply.length">
                <div v-for="(r, i) in currentApp.reply" :key="i" class="reply-block">
                  <div class="form-row"><label>利率：</label><span>{{ r.offeredInterestRate }}%</span></div>
                  <div class="form-row"><label>批复金额：</label><span>{{ r.offeredAmount }} 元</span></div>
                  <div class="form-row"><label>备注：</label><span>{{ r.bankNotes || '无' }}</span></div>
                  <div class="form-row"><label>时间：</label><span>{{ r.offerTime }}</span></div>
                  <div class="form-row"><label>状态：</label><span>{{ r.offerStatus }}</span></div>

                  <div v-if="role === 'farmer' && r.offerStatus === 'pending'" class="bank-action-buttons">
                    <button @click="acceptOffer(r.offerId)" class="accept-btn">接受</button>
                    <button @click="rejectOffer(r.offerId)" class="reject-btn">拒绝</button>
                  </div>
                  <hr />
                </div>
              </div>
              <p v-else>暂无银行回复</p>
            </div>

            <!-- 银行可回复 -->
            <div v-if="role === 'bank'" class="reply-form">
              <h4>提交批复</h4>
              <form @submit.prevent="submitReply" class="modal-form">
                <div class="form-row">
                  <label>利率：</label>
                  <input type="number" step="0.01" v-model="newReply.offeredInterestRate" required />
                </div>
                <div class="form-row">
                  <label>批复金额：</label>
                  <input type="number" v-model="newReply.offeredAmount" required />
                </div>
                <div class="form-row">
                  <label>备注：</label>
                  <textarea v-model="newReply.bankNotes"></textarea>
                </div>
                <div class="action-buttons">
                  <button class="submit-btn">提交</button>
                </div>
              </form>
            </div>

            <button class="cancel-btn" @click="closeDetail()">关闭</button>
          </div>
        </div>

        <!-- 弹窗：农户新建申请 -->
        <div v-if="showCreateApplication" class="modal-overlay" @click.self="showCreateApplication = false">
          <div class="modal-content">
            <h3>发起融资申请</h3>
            <form @submit.prevent="submitNewApplication" class="modal-form">
              <div class="form-row">
                <label>金额（元）：</label>
                <input type="number" v-model="newApp.amount" required />
              </div>
              <div class="form-row">
                <label>用途：</label>
                <input v-model="newApp.purpose" required />
              </div>
              <div class="form-row">
                <label>期限（月）：</label>
                <input type="number" v-model="newApp.term" required />
              </div>
              <div class="form-actions">
                <button class="submit-btn">提交</button>
                <button type="button" class="cancel-btn" @click="showCreateApplication = false">取消</button>
              </div>
            </form>
          </div>
        </div>

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
.product-info .form-row {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
}

.product-info .form-row label {
  width: 20%;        /* label 固定宽度，可根据需要调整 */
  font-weight: bold;
  text-align: justify;  /* 两端对齐 */
}

.product-info .form-row span {
  width: 80%;
  flex: 1;
}

.reply-section .section-title {
  text-align: center;
  margin: 20px 0 10px;
}

.offer-item .form-group {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
}

.offer-item .form-group label {
  width: 120px;
  text-align: left;
}

.offer-item .form-group input {
  flex: 1;
  padding: 6px 8px;
  border-radius: 4px;
  border: 1px solid #ccc;
}

.form-actions {
  display: flex;
  gap: 10px;
  margin-top: 15px;
}

.form-actions button {
  flex: 1;
}

</style>

<style scoped>
.main-bg {
  display: flex;
  flex-direction: column;
  height: 100vh;
  width: 1800px;
  background-color: #F0F9F4;
  border-radius: 8px;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

.main-nav {
  display: flex;
  border-bottom: 2px solid #e0e0e0;
  margin-bottom: 25px;
}
.main-nav button {
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
.main-nav button:hover { color: #2D7D4F; }
.main-nav button.active {
  color: #2D7D4F;
  border-bottom-color: #2D7D4F;
}
/* 内容主容器 */
.content {
  width: 100%;
  flex: 1;
  padding: 20px;
  background: white;
  border-radius: 8px;
}

/* 顶部角色/类型切换导航 */
.role-nav {
  display: flex;
  border-bottom: 2px solid #e0e0e0;
  margin-bottom: 25px;
}

.role-nav button,
.buyer-nav button {
  padding: 10px 20px;
  border: none;
  background-color: transparent;
  cursor: pointer;
  font-size: 1.1rem;
  color: #555;
  transition: 0.3s;
  border-bottom: 3px solid transparent;
}

button.active {
  color: #2D7D4F;
  border-bottom-color: #2D7D4F;
}

/* 页面标题 */
.page-header h2 {
  color: #2D7D4F;
  font-weight: 700;
}

/* 顶部按钮（新增产品/新增申请） */
.loan-type-buttons button {
  background: #2D7D4F;
  color: white;
  border: none;
  border-radius: 6px;
  padding: 8px 16px;
  margin-left: 10px;
  cursor: pointer;
  transition: background 0.3s;
}
.loan-type-buttons button:hover {
  background: #40916C;
}

/* 融资产品卡片 / 融资申请卡片*/
.finance-card,
.application-card {
  background: white;
  border-radius: 10px;
  padding: 16px;
  margin-bottom: 15px;
  box-shadow: 0 2px 6px rgba(0,0,0,0.1);
}

.finance-card h3 {
  color: #2D7D4F;
  margin-bottom: 10px;
}

.finance-card p,
.application-card p {
  margin: 6px 0;
  color: #333;
}

/* 操作按钮 */
.finance-card button,
.detail-btn {
  margin-top: 10px;
  background-color: #2D7D4F;
  color: white;
  border: none;
  border-radius: 8px;
  padding: 8px 14px;
  cursor: pointer;
  transition: background 0.3s;
}

.finance-card button:hover,
.detail-btn:hover {
  background-color: #25683F;
}

/* 蓝色详情按钮 */
.detail-btn {
  background: #4caf50;
}
.detail-btn:hover {
  background: #52c41a;
}

/* 申请列表整体布局 */
.application-list {
  padding: 10px 20px;
}

/*新增 / 提交 / 取消按钮*/

.add-btn,
.submit-btn,
.cancel-btn {
  margin: 10px 5px;
  padding: 8px 16px;
  border: none;
  border-radius: 6px;
  color: white;
  cursor: pointer;
}

.add-btn,
.submit-btn {
  background-color: #2D7D4F;
}
.add-btn:hover,
.submit-btn:hover {
  background-color: #25683F;
}

.cancel-btn {
  background-color: #6c757d;
}
.cancel-btn:hover {
  background-color: #5a6268;
}

/*表单样式*/

.application-form {
  background: white;
  padding: 20px;
  border-radius: 8px;
  width: 90%;
  max-width: 500px;
  margin: 0 auto;
}

.form-group {
  margin-bottom: 16px;
}

.form-group label {
  font-weight: bold;
  display: block;
  margin-bottom: 6px;
}

.form-group input,
textarea,
input,
textarea {
  width: 100%;
  padding: 6px;
  border: 1px solid #ccc;
  border-radius: 6px;
  box-sizing: border-box;
}

/*弹窗样式*/

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0,0,0,0.5);
  display: flex;
  align-items: center;
  justify-content: center;
}

.modal-content {
  background: white;
  padding: 20px;
  border-radius: 12px;
  width: 400px;
  max-width: 90%;
  box-shadow: 0 4px 10px rgba(0,0,0,0.3);
}

/* 回复区域 */
.reply-section {
  margin-top: 15px;
  border: 1px solid #2D7D4F;
  border-radius: 10px;
  padding: 10px;
  text-align: left;
  background: #f6fff6;
}

/* 动作按钮区域 */
.action-buttons {
  text-align: center;
  gap: 8px;
}

/* 没有数据 */
.empty-state {
  text-align: center;
  color: gray;
  margin-top: 20px;
}
/* 
   发起申请按钮（农户）*/
.create-btn {
  background-color: #52c41a;
  color: white;
  border: none;
  padding: 10px 16px;
  border-radius: 6px;
  cursor: pointer;
  margin-bottom: 15px;
}
.create-btn:hover {
  background-color: #4CAF50;
}

/* 每条申请卡片 */
.item-card {
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  align-items: center;
  background: #fff;
  border: 1px solid #ddd;
  padding: 15px;
  margin-bottom: 15px;
  border-radius: 8px;
  box-shadow: 0 2px 6px rgba(0,0,0,0.05);
}

.item-left h3 {
  color: #2D7D4F;
  margin-bottom: 8px;
}

.item-left {
  display: flex;
  flex-direction: row;       /* 横向排列 */
  flex-wrap: wrap;           /* 内容多时换行 */
  gap: 20px;                 /* 各信息间距 */
  align-items: center;       /* 垂直居中对齐 */
}

.item-left p {
  margin: 0;                 /* 去掉上下边距 */
}


/*“查看详情”按钮*/
.detail-btn {
  background: #4caf50;
  color: white;
  border: none;
  padding: 8px 12px;
  border-radius: 6px;
  cursor: pointer;
}
.detail-btn:hover {
  background: #52c41a;
}

/* 
   空状态*/
.empty-state {
  text-align: center;
  color: gray;
  margin-top: 20px;
}

/*通用弹窗*/
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0,0,0,0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
}

.modal-content {
  background: white;
  padding: 20px;
  width: 420px;
  max-width: 90%;
  border-radius: 12px;
  box-shadow: 0 4px 10px rgba(0,0,0,0.25);
}

/* 
   Modal 内标题*/
.modal-content h3,
.modal-content h4 {
  color: #2D7D4F;
  margin-bottom: 12px;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0,0,0,0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 999;
}

.modal-title-div {
  position: relative;
  text-align: center;      /* 让标题居中 */
  padding: 10px 40px;      /* 给右侧留空间放按钮 */
}

.modal-title-div h3 {
  margin: 0;
}

.modal-close-btn {
  position: absolute;
  right: 10px;
  top: 50%;
  transform: translateY(-50%);  /* 垂直居中对齐 */
  cursor: pointer;
  font-size: 14px;
}

.modal-content {
  background: #fff;
  border-radius: 8px;
  padding: 20px 25px;
  width: 400px;
  max-width: 90%;
}

.modal-content h3 {
  margin-bottom: 15px;
  text-align: center;
}

.modal-form .form-group {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.modal-form .form-group label {
  width: 25%;
  text-align: left;
  font-weight: 500;
}

.modal-form .form-group input,
.modal-form .form-group textarea {
  width: 75%;
  padding: 5px 8px;
  border: 1px solid #ccc;
  border-radius: 4px;
}

.modal-form .form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 15px;
}
/*提交 & 取消按钮*/
.submit-btn {
  background-color: #52c41a;
  color: white;
  border: none;
  padding: 8px 14px;
  border-radius: 6px;
  cursor: pointer;
  margin-right: 8px;
}
.submit-btn:hover {
  background-color: #4CAF50;
}

.cancel-btn {
  background-color: #ccc;
  color: #333;
  border: none;
  padding: 8px 14px;
  border-radius: 6px;
  cursor: pointer;
}
.cancel-btn:hover {
  background-color: #aaa;
}

/* 
   回复列表区域*/
.reply-block {
  background: #f7fff5; /* 浅绿色背景 */
  padding: 10px;
  border-left: 4px solid #52c41a;
  border-radius: 6px;
  margin-bottom: 12px;
}

.reply-block p {
  margin: 4px 0;
}

.bank-action-buttons {
  display: flex;
  justify-content: center; /* 两个按钮左右均匀分布 */
  gap: 50px;                       /* 按钮间距 */
  width: 100%;                     /* 自动拉满容器 */
}

.accept-btn,
.reject-btn {
  width: 80px;
  border: none;
  border-radius: 6px;
  color: white;
  cursor: pointer;
  text-align: center;
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

/* 银行回复表单*/
.reply-form {
  margin-top: 15px;
  padding: 12px;
  border: 1px solid #52c41a;
  border-radius: 8px;
  background: #f9fff8;
}
.form-row {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
}
.form-row label {
  display: inline-block;      /* 必须是块或行内块 */
  width: 25%;
  font-weight: bold;
  text-align: justify;        /* 两端对齐 */
}

.form-row span {
  width: 75%;
  font-weight: bold;
  text-align: center;       /* 文字左对齐 */
}

.form-row input,
.form-row textarea {
  flex: 1;                /* 占据剩余空间 */
  padding: 6px;
  border: 1px solid #ccc;
  border-radius: 6px;
  box-sizing: border-box;
}



.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}


.action-buttons {
  display: flex;
  justify-content: flex-start;
  gap: 10px;
  margin-top: 10px;
}
</style>
