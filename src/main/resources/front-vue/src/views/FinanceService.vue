<template>
  <div class="main-bg">
    <!-- 页头 -->
    <header class="header">
      <h1>农产品交易平台</h1>
      <nav>
        <ul>
          <li><router-link to="/main">首页</router-link></li>
          <li><router-link to="/finance" style="color: #B7E4C7;">融资服务</router-link></li>
          <li><router-link to="/expert">专家助力</router-link></li>
          <li><router-link to="/trading">农产品交易</router-link></li>
          <li><router-link to="/profile">个人信息</router-link></li>
        </ul>
      </nav>
    </header>

    <!-- 内容区 -->
    <section class="content">

      <!-- 农民视角 -->
      <div v-if="role === 'farmer'">
        <nav class="farmer-nav">
          <button @click="switchView('list')" :class="{ active: currentView === 'list' }">所有申请</button>
          <button @click="switchView('add')" :class="{ active: currentView === 'add' }">发布申请</button>
        </nav>

        <div v-if="currentView === 'list'">
          <div class="application-list">
            <div
                v-for="app in applications"
                :key="app.id"
                class="application-card"
            >
              <h3>申请编号 #{{ app.financingId }}</h3>
              <p><strong>金额：</strong>{{ app.amount }} 元</p>
              <p><strong>用途：</strong>{{ app.purpose }}</p>
              <p><strong>期限：</strong>{{ app.term }} 月</p>
              <p><strong>状态：</strong>{{ app.applicationStatus }}</p>
              <button @click="openDetail(app)" class="detail-btn">查看详情</button>
            </div>


            <!-- 弹窗：农户查看详情 -->
            <div v-if="showDetail" class="modal-overlay" @click.self="closeDetail">
              <div class="modal-content">
                <h3>融资申请详情</h3>
                <p><strong>申请编号：</strong>{{ currentApp.financingId }}</p>
                <p><strong>金额：</strong>{{ currentApp.amount }} 元</p>
                <p><strong>用途：</strong>{{ currentApp.purpose }}</p>
                <p><strong>期限：</strong>{{ currentApp.term }} 月</p>
                <p><strong>状态：</strong>{{ currentApp.applicationStatus }}</p>

                <!-- 如果有银行回复 -->
                <div v-if="currentApp.reply && currentApp.reply.length > 0" class="reply-section">
                  <h4 style="text-align: center">银行批复</h4>

                  <div v-for="(offer, index) in currentApp.reply" :key="index" class="offer-item">
                    <p><strong>银行 {{ index + 1 }}：</strong></p>
                    <p>利率：{{ offer.offeredInterestRate }}%</p>
                    <p>批复金额：{{ offer.offeredAmount }} 元</p>
                    <p>备注：{{ offer.bankNotes || '无' }}</p>
                    <p>批复时间：{{ formatDate(offer.offerTime) }}</p>
                    <p>状态：{{offer.offerStatus}}</p>

                    <div v-if="offer.offerStatus === 'pending'" class="action-buttons">
                      <button @click="acceptOffer(offer.offerId)" class="accept-btn">接受</button>
                      <button @click="rejectOffer(offer.offerId)" class="reject-btn">拒绝</button>
                    </div>

                    <hr />
                  </div>
                </div>

                <!-- 如果还没有银行回复 -->
                <div v-else class="reply-section">
                  <h4>银行回复</h4>
                  <p>暂无回复，请耐心等待。</p>
                </div>


                <button @click="closeDetail" class="cancel-btn">关闭</button>
              </div>
            </div>

            <p v-if="!applications.length" class="empty-state">暂无融资申请</p>
            <button v-if="!applications.length" class="add-btn" @click="switchView('add')">我要申请融资</button>
          </div>
        </div>

        <!-- 农民发布申请 -->
        <div v-if="currentView === 'add'">
          <form @submit.prevent="submitApplication" class="application-form">
            <div class="form-group">
              <label>融资金额 (元):</label>
              <input type="number" v-model.number="newApp.amount" required min="1000" />
            </div>
            <div class="form-group">
              <label>资金用途:</label>
              <input type="text" v-model="newApp.purpose" required />
            </div>
            <div class="form-group">
              <label>贷款期限 (月):</label>
              <input type="number" v-model.number="newApp.term" required />
            </div>
            <button type="submit" class="submit-btn">提交申请</button>
            <button type="button" class="cancel-btn" @click="switchView('list')">
              返回
            </button>
          </form>
        </div>
      </div>

      <!-- 银行视角 -->
      <div v-if="role === 'bank'" class="bank-view">
        <h2>待回复融资申请</h2>
        <div class="application-list">
          <div
              v-for="app in applications"
              :key="app.financingId"
              class="application-card"
          >
            <h3>申请编号 #{{ app.financingId }}</h3>
            <p><strong>农户：</strong>{{ app.farmerName || '加载中...' }}</p>
            <p><strong>金额：</strong>{{ app.amount }} 元</p>
            <p><strong>状态：</strong>{{ app.applicationStatus }}</p>

            <button @click="openDetail(app)" class="detail-btn">查看详情</button>
          </div>

          <p v-if="!applications.length" class="empty-state">暂无待处理申请</p>
        </div>

        <!-- 弹窗 -->
        <div v-if="showDetail" class="modal-overlay" @click.self="closeDetail">
          <div class="modal-content">
            <h3>融资申请详情</h3>
            <p><strong>申请编号：</strong>{{ currentApp.financingId }}</p>
            <p><strong>农户：</strong>{{ currentApp.farmerName }}</p>
            <p><strong>金额：</strong>{{ currentApp.amount }} 元</p>
            <p><strong>用途：</strong>{{ currentApp.purpose }}</p>
            <p><strong>期限：</strong>{{ currentApp.term }} 月</p>
            <p><strong>状态：</strong>{{ currentApp.applicationStatus }}</p>

            <div class="reply-section">
              <h4>回复：</h4>
              <div class="form-group">
                <label>利率 (%)：</label>
                <input v-model.number="currentApp.reply.offeredInterestRate" type="number" min="0" step="0.01" />
              </div>
              <div class="form-group">
                <label>回复内容：</label>
                <textarea v-model="currentApp.reply.bankNotes" placeholder="请输入回复内容"></textarea>
              </div>
              <div class="action-buttons">
                <button @click="submitReply(currentApp)" class="submit-btn">提交回复</button>
                <button @click="closeDetail" class="cancel-btn">关闭</button>
              </div>
            </div>
          </div>
        </div>

      </div>

    </section>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import axios from '../utils/axios'

const token = localStorage.getItem('token')

const userInfo = ref({})
const userId = ref('')
const userName = ref('未登录')
const role = ref('游客')
const email = ref('')

const showDetail = ref(false)
const currentApp = ref({})
const currentView = ref('list')
const applications = ref([])

const newApp = ref({
  amount: '',
  purpose: '',
  term: '',
  details: ''
})

const formatDate = (time) => {
  if (!time) return '-'
  return new Date(time).toLocaleString()
}

const switchView = (v) => {
  currentView.value = v
}

//打开弹窗
const openDetail = (app) => {
  currentApp.value = { ...app }

  // 如果银行还没回复，创建一个新的空回复对象
  currentApp.value.reply = app.reply || {
    offeredInterestRate: '',
    bankNotes: ''
  }

  showDetail.value = true
}

// 关闭弹窗
const closeDetail = () => {
  showDetail.value = false
}

// 获取融资申请
const loadApplications = async () => {
  try {
    console.log('role:', role.value)
    const url = role.value === 'bank' ? '/financing/submitted' : '/financing/my'
    const res = await axios.get(url)
    const apps = res.data.records || []

    console.log('获取到的融资申请数据:', apps)

    for (const app of apps) {
      try {
        const userRes = await axios.get(`/user/info/${app.initiatingFarmerId}`)
        app.farmerName = userRes.data.user.userName || '未知农户'
      } catch {
        app.farmerName = '未知农户'
        console.log('获取发起人姓名失败')
      }
    }

    const appsWithOffers = await Promise.all(apps.map(async app => {
      try {
        const offerRes = await axios.get('/financing/offers', {
          params: { financingId: app.financingId }
        })
        console.log('获取到的报价数据:', offerRes.data)
        app.reply = (offerRes.data && offerRes.data.length > 0) ? offerRes.data : []
      } catch (error) {
        console.error('获取报价出错:', error)
        app.reply = null
      }

      return app
    }))

    applications.value = appsWithOffers

  } catch (err) {
    console.error('加载申请失败:', err)
  }
}


// 农民提交融资申请
const submitApplication = async () => {
  try {
    await axios.post('/financing/create', newApp.value)
    alert('申请已提交！')
    switchView('list')
    await loadApplications()
  } catch (err) {
    console.error(err)
  }
}

// 银行提交批复
const submitReply = async (app) => {
  try {
    console.log('app的值:', app)
    console.log('app回复的值:', app.reply)
    await axios.post('/financing/offer/submit', {
      financingId: app.financingId,                  // 对应申请的ID
      offeredAmount: app.amount,                     // 批复金额（可以等于申请金额，也可自定义）
      interestRate: app.reply.offeredInterestRate,           // 利率
      bankNotes: app.reply.bankNotes                   // 银行备注
    })
    alert('已提交银行批复')
    await loadApplications() // 刷新申请列表
  } catch (err) {
    console.error('提交批复失败：', err)
    alert('提交失败，请稍后重试')
  }
}

// 农民接受银行方案
const acceptOffer = async (offerId) => {
  try {
    await axios.post('/financing/offer/accept', null, {
      params: { offerId }
    })
    alert('已接受银行方案')
    await loadApplications()
  } catch (err) {
    console.error('接受失败', err)
  }
}

// 农民拒绝银行方案
const rejectOffer = async (offerId) => {
  try {
    await axios.post('/financing/offer/reject', null, {
      params: { offerId }
    })
    alert('已拒绝银行方案')
    await loadApplications()
  } catch (err) {
    console.error('拒绝失败', err)
  }
}


// 初始加载
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
      console.log('用户：', res.data)
      userInfo.value = res.data.user
      userId.value = res.data.user.userId
      userName.value = res.data.user.userName
      role.value = res.data.user.role
      email.value = res.data.user.email
      localStorage.setItem('userInfo', JSON.stringify(res.data.user))
    } else {
      console.warn('Token 无效或过期')
    }
  } catch (err) {
    console.error('获取用户信息失败', err)
  }

  await loadApplications()
})

watch(currentView, val => {
  if (val === 'address') loadAddresses()
})

</script>


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
  border-radius: 8px;
}

.farmer-nav, .buyer-nav {
  display: flex;
  border-bottom: 2px solid #e0e0e0;
  margin-bottom: 25px;
}

.farmer-nav button, .buyer-nav button {
  padding: 10px 20px;
  border: none;
  background-color: transparent;
  cursor: pointer;
  font-size: 1.1rem;
  color: #555;
  transition: color 0.3s, border-bottom-color 0.3s;
  border-bottom: 3px solid transparent;
}

button.active {
  color: #2D7D4F;
  border-bottom-color: #2D7D4F;
}

.page-header h2 {
  color: #2D7D4F;
  font-weight: 700;
}

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

.finance-card h3 {
  color: #2D7D4F;
  margin-bottom: 10px;
}

.finance-card p {
  margin: 6px 0;
  color: #333;
}

.finance-card button {
  margin-top: 10px;
  background-color: #52B788;
  color: white;
  border: none;
  border-radius: 8px;
  padding: 8px 14px;
  cursor: pointer;
  transition: background 0.3s;
}

.finance-card button:hover {
  background-color: #40916C;
}


.application-list {
  padding: 10px 20px;
}

.application-card {
  background: white;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 12px;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
}

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

.add-btn {
  background-color: #52b788;
}

.submit-btn {
  background-color: #40916c;
}

.cancel-btn {
  background-color: #6c757d;
}

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
textarea {
  width: 100%;
  padding: 6px;
  margin-right: 1rem;
  border: 1px solid #ccc;
  border-radius: 6px;
  box-sizing: border-box; /* ✅ 包括 padding 和 border 在内 */
}


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

.detail-btn {
  background: #007bff;
  color: white;
  border: none;
  padding: 6px 12px;
  border-radius: 6px;
  cursor: pointer;
}
.detail-btn:hover {
  background: #0056b3;
}


.application-card {
  background: #fff;
  border-radius: 10px;
  padding: 15px;
  margin-bottom: 15px;
  box-shadow: 0 2px 5px rgba(0,0,0,0.1);
}

.detail-btn {
  background: #007bff;
  color: white;
  border: none;
  padding: 6px 12px;
  border-radius: 6px;
  cursor: pointer;
}
.detail-btn:hover {
  background: #0056b3;
}

.empty-state {
  text-align: center;
  color: gray;
  margin-top: 20px;
}

/* 弹窗样式 */
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

.reply-section {
  margin-top: 15px;
  border: 1px solid #52b788;
  border-radius: 10px;        /* 圆角边框 */
  padding: 10px;              /* 内边距，让内容不贴边 */

  outline-color: #1a1a1a;
  text-align: left;
}

.form-group {
  margin-bottom: 10px;
}

input, textarea {
  width: 100%;
  border: 1px solid #ccc;
  border-radius: 6px;
  padding: 6px;
}

.submit-btn {
  background-color: #28a745;
  color: white;
  border: none;
  padding: 8px 12px;
  border-radius: 6px;
  cursor: pointer;
  margin-right: 10px;
}
.submit-btn:hover {
  background-color: #218838;
}

.cancel-btn {
  background-color: #ccc;
  color: #333;
  border: none;
  padding: 8px 12px;
  border-radius: 6px;
  cursor: pointer;
}
.cancel-btn:hover {
  background-color: #aaa;
}

.action-buttons {
  text-align: center;
  gap: 8px;
}

.accept-btn {
  margin: 1rem;
}

</style>
