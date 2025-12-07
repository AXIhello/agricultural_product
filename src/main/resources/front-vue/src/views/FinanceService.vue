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

        <button v-if="role === 'farmer'" 
          @click="switchView('invitations')" 
          :class="{ active: currentView === 'invitations' }">
          共同融资邀请 <span v-if="pendingCount > 0" class="badge">{{ pendingCount }}</span>
        </button>
      </nav>


      <!--融资产品（无角色限制-->
      <div v-if="currentView === 'products'" class="view-wrapper product-table">

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
            <span style="display: flex; gap: 10px;">
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
              <h3 class="modal-title">产品详情</h3>
<!--              <button @click="closeProductDetail" class="modal-close-btn">关闭</button>-->
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
                <!-- 邀请共同申请人 -->
                <div class="form-row">
                  <label>邀请共同申请人：</label>
                  <div class="recommendation-box">
                    <div v-if="loadingRecs">加载推荐中...</div>
                    <div v-else-if="recommendedUsers.length === 0">暂无推荐用户</div>
                    <div v-else class="user-checkbox-list">
                      <div v-for="user in recommendedUsers" :key="user.userId" class="checkbox-item">
                        <input 
                          type="checkbox" 
                          :value="user.userId" 
                          v-model="newApp.coApplicantIds"
                        >
                        <span>{{ user.name }} (信用分: {{ user.creditScore }})</span>
                        <span v-if="user.sameRegion" class="tag">同地区</span>
                      </div>
                    </div>
                  </div>
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
      <div v-if="currentView === 'applications'" class="view-wrapper app-table">
        
        

        <!-- 表格头 -->
        <div class="items-table-header">
          <span>申请编号</span>         
          <span>角色</span>
          <span>发起人</span>
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
          <span>
            <span v-if="isMyApplication(app)" class="role-badge main">主贷人</span>
            <span v-else class="role-badge co">共同申请人</span>
          </span>
          <span>{{ app.farmerName }}</span>
          <span>{{ app.amount }} 元</span>
          <span>{{ app.term }} 月</span>
          <span>{{ getAppStatusText(app.applicationStatus) }}</span>
          <span>
          <button @click="openApplicationDetail(app)" class="detail-btn">
            查看详情
          </button>
          <button 
            v-if="role === 'farmer'&& isMyApplication(app) && ['draft', 'submitted'].includes(app.applicationStatus)"
            @click="cancelFinancing(app.financingId)"
            class="cancel-btn-action"
          >
            取消申请
          </button>
          </span>
        </div>

        <div class="view-header" style="display: flex; justify-content: flex-end; margin-bottom: 10px;">
          <!-- farmer 可以发起申请 -->
        <div class="bottom-action-bar">
          <button
              v-if="role === 'farmer'"
              class="create-btn"
              @click="showCreateApplication = true"
          >
            + 发起申请
          </button>
        </div>
        </div>

        <p v-if="!applications.length" class="empty-state">暂无申请</p>   

        

        <!-- 弹窗：农户新建申请 -->
        <div v-if="showCreateApplication" class="modal-overlay" @click.self="showCreateApplication = false">
          <div class="modal-container">
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

      <!-- 合作邀请视图 -->
      <div v-if="currentView === 'invitations'" class="view-wrapper invite-table">
        <div class="items-table-header">
          <span>申请ID</span>
          <span>发起人</span>
          <span>金额</span>
          <span>用途</span>
          <span>邀请状态</span>
          <span>操作</span>
        </div>

        <div class="items-table-row" v-for="invite in invitations" :key="invite.financingId">
          <span>#{{ invite.financingId }}</span>
          <span>{{ invite.initiatorName || '加载中...' }}</span>
          <span>{{ invite.amount }} 元</span>
          <span>{{ invite.purpose }}</span>
          
          <!-- 状态显示 -->
          <span :class="getInviteStatusClass(invite.myInvitationStatus)">
            {{ getInviteStatusText(invite.myInvitationStatus) }}
          </span>

          <!-- 操作按钮 -->
          <span>
            <template v-if="invite.myInvitationStatus === 'pending'">
              <button class="accept-btn" @click="respondInvite(invite.financingId, 'accept')">接受</button>
              <button class="reject-btn" @click="respondInvite(invite.financingId, 'reject')">拒绝</button>
            </template>
            <button class="detail-btn" @click="openApplicationDetail(invite)">详情</button>
          </span>
        </div>
        
        <p v-if="invitations.length === 0" class="empty-state">暂无合作邀请</p>
      </div>

      <!-- 弹窗：申请详情 -->
        <div v-if="showDetail" class="modal-overlay" @click.self="closeDetail()">
          <div class="modal-container">
            <h3>申请详情</h3>

            <div class="form-row"><label>ID：</label><span>{{ currentApp.financingId }}</span></div>
            <div class="form-row"><label>金额：</label><span>{{ currentApp.amount }} 元</span></div>
            <div class="form-row"><label>用途：</label><span>{{ currentApp.purpose }}</span></div>
            <div class="form-row"><label>期限：</label><span>{{ currentApp.term }} 月</span></div>
            <div class="form-row"><label>状态：</label><span :class="getAppStatusClass(currentApp.applicationStatus)">
                {{ getAppStatusText(currentApp.applicationStatus) }}
              </span>
            </div>
            <div class="form-row" style="align-items: flex-start;">
              <label>共同申请人：</label>
              <span>
                <!-- 如果有列表 -->
                <template v-if="currentApp.coApplicants && currentApp.coApplicants.length">
                  <div v-for="co in currentApp.coApplicants" :key="co.id" class="co-list-item">
                    用户 {{ co.farmerId }} 
                    <!-- 状态标签 -->
                    <span :class="'status-badge ' + co.invitationStatus">
                      {{ formatCoStatus(co.invitationStatus) }}
                    </span>
                  </div>
                </template>
                <!-- 如果没有 -->
                <template v-else>无</template>
              </span>
            </div>
            <hr />

            <!-- 银行回复区 -->
            <div v-if="role === 'farmer'">
              <h4>银行回复</h4>

              <div v-if="currentApp.reply && currentApp.reply.length">
                <div v-for="(r, i) in currentApp.reply" :key="i" class="offer-card">
                  <!-- 顶部：标题 + 状态标签 -->
                  <div class="offer-header">
                    <span class="offer-title">银行批复方案</span>
                    <span :class="'status-tag ' + r.offerStatus">
                      {{ getOfferStatusText(r.offerStatus) }}
                    </span>
                  </div>

                  <!-- 中间：核心数据（利率 + 金额）并排显示 -->
                  <div class="offer-body">
                    <div class="data-item">
                      <label>执行利率</label>
                      <div class="value rate">{{ r.offeredInterestRate }}<span>%</span></div>
                    </div>
                    <div class="data-divider"></div> <!-- 分割线 -->
                    <div class="data-item">
                      <label>批复金额</label>
                      <div class="value amount">¥ {{ r.offeredAmount }}</div>
                    </div>
                  </div>

                  <!-- 底部：备注 + 时间 -->
                  <div class="offer-footer">
                    <div class="note-box" v-if="r.bankNotes">
                      <span class="label">备注：</span>{{ r.bankNotes }}
                    </div>
                    <div class="time-stamp">
                      批复时间：{{ formatDate(r.offerTime) }}
                    </div>
                  </div>

                  <!-- 按钮区域 (保持你原有的逻辑不变) -->
                  <div v-if="role === 'farmer' && r.offerStatus === 'pending'" class="bank-action-buttons">
                    <button @click="acceptOffer(r.offerId)" class="accept-btn">接受方案</button>
                    <button @click="rejectOffer(r.offerId)" class="reject-btn">拒绝</button>
                  </div>
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
              <hr style="margin: 20px 0;">             
              <h4>拒绝申请</h4>
              <div class="form-row">
                <label>拒绝原因：</label>
                <textarea v-model="rejectReason"></textarea>
              </div>
              <div class="action-buttons">
                <button @click="rejectApplication" class="reject-main-btn">拒绝该申请</button>
              </div>
            </div>

            <button class="cancel-btn" @click="closeDetail()">关闭</button>
            
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
const { role,userInfo } = storeToRefs(authStore)
const products = ref([])
const newProduct = ref({ productName: '', description: '', termMonths: '', interestRate: '', minAmount: '', maxAmount: '' })

// 当前视图：applications / product
const currentView = ref('products')
// 产品详情弹窗
const showProductDetail = ref(false)
const currentProduct = ref(null)

const getAppStatusText = (status) => {
  const map = {
    'draft': '草稿',
    'submitted': '待审核',
    'approved': '已通过',
    'rejected': '已拒绝',
    'cancelled': '已取消',
    'overdue': '已逾期'
  }
  return map[status] || status
}

// 获取银行Offer状态的中文
const getOfferStatusText = (status) => {
  const map = {
    'pending': '待农户确认',
    'accepted': '农户已接受',
    'rejected': '农户已拒绝'
  }
  return map[status] || status
}

// 状态样式映射 (可选，增加不同颜色)
const getAppStatusClass = (status) => {
  const map = {
    'submitted': 'text-orange',
    'approved': 'text-green',
    'rejected': 'text-red',
    'cancelled': 'text-gray',
    'overdue': 'text-red-bold'
  }
  return map[status] || ''
}

// 查看产品详情
const viewProduct = (product) => {
  currentProduct.value = { ...product }
  showProductDetail.value = true

  if (role.value === 'farmer') {
    // 重置之前的选中状态，避免残留
    newApp.value.coApplicantIds = [] 
    loadRecommendations()
  }
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
    
    console.log("正在提交申请...");
    console.log("金额:", newApp.value.amount);
    console.log("选中的共同申请人ID:", newApp.value.coApplicantIds); 

    await axios.post('/financing/apply-product', {
      productId: currentProduct.value.productId,
      amount: newApp.value.amount,
      purpose: newApp.value.purpose,   
      coApplicantIds: newApp.value.coApplicantIds
    })
    console.log("提交的共同申请人:", newApp.value.coApplicantIds);

    alert('贷款申请已提交')
    showDetail.value = false
    newApp.value = { amount:'', purpose:'', term:'', details:'' }
    await loadApplications()
  } catch (err) {
    console.error(err)
    alert('提交失败')
  }
}

// 删除银行产品
async function deleteProduct(productId) {
  if (!confirm('确定删除该产品吗？')) return;

  try {
    await axios.delete(`/bank/products/${productId}`);
    // 删除成功后，从本地列表移除
    products.value = products.value.filter(p => p.productId !== productId);
    alert('删除成功');
  } catch (err) {
    console.error('删除失败', err);
    alert('删除失败，请稍后重试');
  }
}

// 弹窗控制
const showDetail = ref(false)
const showCreateApplication = ref(false)

// 数据
const applications = ref([])
const currentApp = ref({})
const recommendedUsers = ref([])// 推荐的共同申请人列表
const loadingRecs = ref(false)// 推荐加载状态
const newApp = ref({
  amount: '',
  purpose: '',
  term: '',
  details: '',
  coApplicantIds: []
})

// 银行回复表单
const newReply = ref({
  offeredInterestRate: '',
  offeredAmount: '',
  bankNotes: ''
})

// 日期格式化
const formatDate = (isoString) => {
  if (!isoString) return '';
  return isoString.replace('T', ' ').substring(0, 16);
}

// 切换视图
const switchView = (v) => currentView.value = v

// 打开申请详情弹窗
const openApplicationDetail = async (app) => {
  console.log("点击详情，对象：", app);
  currentApp.value = { ...app }
  // 如果没有 reply，初始化为空数组
  currentApp.value.reply = app.reply || []
  
  showDetail.value = true
  //获取共同申请人
  try {
    const res = await axios.get('/financing/co-applicants', { 
      params: { financingId: app.financingId } 
    })
    currentApp.value.coApplicants = res.data || []
  } catch (e) {
    currentApp.value.coApplicants = []
  }

  //showDetail.value = true
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

//银行拒绝申请
const rejectReason = ref('')

const rejectApplication = async () => {
  if (!rejectReason.value) return alert("请填写拒绝原因")
  if (!confirm("确定拒绝该申请？")) return
  
  try {
    await axios.post('/financing/reject', {
      financingId: currentApp.value.financingId,
      rejectReason: rejectReason.value
    })
    alert("已拒绝")
    await loadApplications()
    showDetail.value = false
  } catch (e) {
    console.error(e)
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

// 农户取消融资申请
const cancelFinancing = async (financingId) => {
  if(!confirm("确定要取消该融资申请吗？这可能会影响您的信用分。")) return
  try {
    await axios.post('/financing/cancel', { financingId })
    alert("申请已取消")
    await loadApplications() // 刷新列表
    showDetail.value = false
  } catch (e) {
    alert("取消失败")
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

// 加载推荐用户 (在打开弹窗时调用)
const loadRecommendations = async () => {
  console.log("正在加载推荐共同申请人...") 
  loadingRecs.value = true
  try {
    const res = await axios.get('/financing/recommend-users', { params: { pageNum: 1, pageSize: 5 } })
    console.log("推荐用户加载成功:", res.data.records)
    recommendedUsers.value = res.data.records || []
  } catch (e) {
    console.error("加载推荐用户失败:",e)
  } finally {
    loadingRecs.value = false
  }
}

//获取共同融资邀请
const invitations = ref([])
const pendingCount = ref(0)

// 加载邀请列表
// 加载邀请列表
const loadInvitations = async () => {
  try {
    // 1. 获取列表
    const res = await axios.get('/financing/invited')
    const list = res.data || []
    
    // 2. 统计待处理数量 (用于红点提示)
    pendingCount.value = list.filter(i => i.myInvitationStatus === 'pending').length

    // 3. 补充发起人名字
    // 这是一个异步操作，建议使用 Promise.all 并发请求，比 for 循环一个个查要快
    await Promise.all(list.map(async (invite) => {
      // 默认显示 ID
      invite.initiatorName = `用户 ${invite.initiatingFarmerId}`;
      
      if (invite.initiatingFarmerId) {
        try {
          const userRes = await axios.get(`/user/info/${invite.initiatingFarmerId}`);
          if (userRes.data && userRes.data.user) {
            // 优先显示真实姓名，其次显示用户名
            invite.initiatorName = userRes.data.user.name || userRes.data.user.userName;
          }
        } catch (err) {
          console.warn(`获取用户 ${invite.initiatingFarmerId} 信息失败`, err);
        }
      }
    }));

    // 4. 更新视图数据
    invitations.value = list;
    
  } catch (e) {
    console.error("加载邀请列表失败", e);
  }
}

// 响应邀请
const respondInvite = async (financingId, action) => {
  const actionText = action === 'accept' ? '接受' : '拒绝';
  if(!confirm(`确定要${actionText}该合作邀请吗？`)) return;
  
  try {
    await axios.post('/financing/invite/respond', {
      financingId: financingId, // 注意后端接收的字段名
      action: action
    });
    
    alert(`操作成功，已${actionText}邀请`);
    await loadInvitations(); // 重新加载列表以刷新状态
    
  } catch (e) {
    console.error(e);
    alert("操作失败，请稍后重试");
  }
}
// 获取状态显示的文字
const getInviteStatusText = (status) => {
  const map = {
    'pending': '待确认',
    'accepted': '已接受',
    'rejected': '已拒绝'
  };
  return map[status] || status;
}
// 格式化共同申请人状态文本
const formatCoStatus = (status) => {
  const map = {
    'pending': '待确认',
    'accepted': '已接受',
    'rejected': '已拒绝'
  }
  return map[status] || status
}

// 获取状态的 CSS 类名
const getInviteStatusClass = (status) => {
  if (status === 'pending') return 'status-pending'; // 黄色/橙色
  if (status === 'accepted') return 'status-accepted'; // 绿色
  if (status === 'rejected') return 'status-rejected'; // 灰色/红色
  return '';
}

// 判断是否为当前用户的发起申请
const isMyApplication = (app) => {
  
  const myUserId = userInfo.value.userId;
  
  if (!myUserId) return false;
   
  return String(app.initiatingFarmerId) === String(myUserId);
}


// 修改打开弹窗的方法，加入加载推荐
const openCreateModal = () => {
  showCreateApplication.value = true
  loadRecommendations() // 调用加载
}

// 初始加载
onMounted(async () => {
  await loadProducts()
  await loadApplications()
})

watch(currentView, val => {
  if (val === 'applications') loadApplications()
  if (val === 'product') loadProducts()
  if(val === 'invitations') loadInvitations()
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
  width: 700px;
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


/* 操作按钮 */
.detail-btn {
  padding: 5px 10px;
  background-color: #409eff;
  color: #fff;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 13px;      /* 字体稍微改小一号 */
  white-space: nowrap;

  color: white;
  border: none;
  cursor: pointer;
  transition: opacity 0.2s;
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

.recommendation-box {
  border: 1px solid #ddd;
  padding: 10px;
  border-radius: 8px;
  max-height: 150px;
  overflow-y: auto;
  width: 70%;
  background: #fff;
}

.checkbox-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 0;
  border-bottom: 1px dashed #eee;
}

.tag {
  background: #e6f7ff;
  color: #1890ff;
  font-size: 10px;
  padding: 2px 6px;
  border-radius: 4px;
}

.cancel-btn-action {
  background-color: #ff4d4f;
  color: white;
  border: none;
  padding: 6px 12px;
  border-radius: 6px;
  cursor: pointer;
  margin-left: 10px;
}
.cancel-btn-action:hover {
  opacity: 0.85; 
}

.reject-main-btn {
  background-color: #cf1322;
  color: white;
  border: none;
  padding: 10px 18px;
  border-radius: 12px;
  cursor: pointer;
  font-weight: 500;
  transition: background 0.2s;
  width: auto;
}


.reject-main-btn:hover {
  background-color: #a71d2a;
}


/* 状态小标签样式 */
.status-badge {
  flex: 0 0 auto;            /* 禁止 Flex 自动放大 */
  width: auto !important;    /* 强制宽度随内容自适应 (加 !important 防止被覆盖) */
  display: inline-block;     
  
  /* 外观设置 */
  margin-left: 10px;         /* 离名字远一点 */
  padding: 2px 8px;          /* 内边距小一点 */
  font-size: 12px;           /* 字号 */
  border-radius: 4px;        /* 圆角 */
  line-height: 1.5;
}

.status-badge.pending {
  background-color: #fffbe6;
  color: #faad14;
  border: 1px solid #ffe58f;
}

.status-badge.accepted {
  background-color: #f6ffed;
  color: #52c41a;
  border: 1px solid #b7eb8f;
}

.status-badge.rejected {
  background-color: #fff1f0;
  color: #f5222d;
  border: 1px solid #ffa39e;
}

/*====================融资产品详情窗口样式===================*/
/* 标题居中 */
.section-title {
  text-align: center;       /* 文字水平居中 */
  font-size: 18px;          /* 稍微加大字体 */
  font-weight: bold;
  color: #2D7D4F;
  margin-bottom: 20px;      /* 增加下边距 */
  border-bottom: 2px solid #e0f2e9; /* 可选：加个底部分割线更精致 */
  padding-bottom: 10px;
}

/* 调整绿色框容器 */
.reply-section {
  background: #f6fff6;
  border: 1px solid #52c41a;
  border-radius: 14px;
  padding: 24px;       /* 增加内边距 */
  margin-top: 20px;
}

/* 让表单内的输入框居中对齐  */
.offer-item {
  display: flex;
  flex-direction: column;
  align-items: stretch; /* 让子元素占满宽度 */
}

/* 提交按钮居中 */
.reply-section .form-actions {
  display: flex;
  justify-content: center; /* 按钮居中 */
  margin-top: 20px;
}

.reply-section .submit-btn {
  width: 200px; /* 让按钮宽一点，更有点击欲 */
  font-size: 16px;
}

/*========================融资申请页面========================*/
/*发起申请按钮样式*/
.bottom-action-bar {
  display: flex;
  justify-content: flex-end; /* 让内容靠右对齐 */
  margin-top: 20px;          /* 控制按钮和表格之间的间距 */
  padding-right: 55px;       /* 给右边留点空隙，和表格内边距对齐 */
}

/*共同融资*/
.badge {
  background: #ff4d4f;
  color: white;
  border-radius: 10px;
  padding: 2px 6px;
  font-size: 12px;
  margin-left: 4px;
}

/*========================共同融资邀请页面========================*/
.status-pending { color: #faad14; font-weight: bold; }
.status-accepted { color: #52c41a; background: #f6ffed; padding: 2px 6px; border-radius: 4px; }
.status-rejected { color: #999; text-decoration: line-through; }

/* 红点样式 */
.badge {
  background-color: #ff4d4f;
  color: white;
  border-radius: 10px;
  padding: 0 6px;
  font-size: 12px;
  margin-left: 5px;
  vertical-align: middle;
}

/**共同申请人*/
.role-badge {
  display: inline-block;   /* 必须是 inline-block 或 flex */
  white-space: nowrap;     /* 【关键】强迫文字不准换行，一行显示 */

  padding: 3px 10px;       /* 调整内边距，让它看起来像个胶囊 */
  font-size: 12px;
  padding: 3px 10px;
  border-radius: 20px;
  font-weight: bold;
  line-height: 1.2;
  text-align: center;
}

.role-badge.main {
  background-color: #e6f7ff;
  color: #1890ff;
  border: 1px solid #91d5ff;
}

.role-badge.co {
  background-color: #f9f0ff;
  color: #722ed1;
  border: 1px solid #d3adf7;
}
</style>


/* ================= 表格整体布局 ================= */
<style scoped>
/* ================= 1. 公共基础样式 (保持不变) ================= */
.items-table-header,
.items-table-row {
  display: flex;
  align-items: center;
  padding: 12px 10px;
  gap: 10px;
}
.items-table-header {
  background: #f5f5f5;
  border-radius: 8px;
  font-weight: bold;
  color: #333;
}
.items-table-row {
  border-bottom: 1px solid #eee;
}
/* 通用单元格设置 */
.items-table-header span,
.items-table-row span {
  display: flex;
  justify-content: center;
  align-items: center;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}

/* ================= 2. 融资申请表格 (7列) - 之前的完美版 ================= */
/* 
   列: [ID, 角色, 发起人, 金额, 期限, 状态, 操作]
*/
.app-table .items-table-header span:nth-child(1), .app-table .items-table-row span:nth-child(1) { flex: 0 0 80px; }
.app-table .items-table-header span:nth-child(2), .app-table .items-table-row span:nth-child(2) { flex: 0 0 110px; }
.app-table .items-table-header span:nth-child(3), .app-table .items-table-row span:nth-child(3) { flex: 0 0 100px; }
.app-table .items-table-header span:nth-child(4), .app-table .items-table-row span:nth-child(4) { flex: 1; }
.app-table .items-table-header span:nth-child(5), .app-table .items-table-row span:nth-child(5) { flex: 0 0 60px; }
.app-table .items-table-header span:nth-child(6), .app-table .items-table-row span:nth-child(6) { flex: 0 0 80px; }
.app-table .items-table-header span:nth-child(7), .app-table .items-table-row span:nth-child(7) { flex: 0 0 180px; gap: 8px; }


/* ================= 3. 融资产品表格 (6列) - 修复布局 ================= */
/* 
   列: [产品名称, 银行, 期限, 利率, 额度范围, 操作]
*/
/* 名称：自适应 */
.product-table .items-table-header span:nth-child(1), .product-table .items-table-row span:nth-child(1) { flex: 1; }
/* 银行：自适应 */
.product-table .items-table-header span:nth-child(2), .product-table .items-table-row span:nth-child(2) { flex: 1; }
/* 期限：固定 */
.product-table .items-table-header span:nth-child(3), .product-table .items-table-row span:nth-child(3) { flex: 0 0 60px; }
/* 利率：固定 */
.product-table .items-table-header span:nth-child(4), .product-table .items-table-row span:nth-child(4) { flex: 0 0 80px; }
/* 额度范围：宽一点 */
.product-table .items-table-header span:nth-child(5), .product-table .items-table-row span:nth-child(5) { flex: 1.5; }
/* 操作：固定 */
.product-table .items-table-header span:nth-child(6), .product-table .items-table-row span:nth-child(6) { flex: 0 0 120px; gap: 8px; }


/* ================= 4. 共同融资邀请表格 (6列) - 修复丑陋的绿色条 ================= */
/* 
   列: [申请ID, 发起人, 金额, 用途, 邀请状态, 操作]
*/
.invite-table .items-table-header span:nth-child(1), .invite-table .items-table-row span:nth-child(1) { flex: 0 0 80px; }
.invite-table .items-table-header span:nth-child(2), .invite-table .items-table-row span:nth-child(2) { flex: 1; }
.invite-table .items-table-header span:nth-child(3), .invite-table .items-table-row span:nth-child(3) { flex: 1; }
.invite-table .items-table-header span:nth-child(4), .invite-table .items-table-row span:nth-child(4) { flex: 1.5; } /* 用途长一点 */

/* 第5列：邀请状态 - 修复绿色长条的关键 */
.invite-table .items-table-header span:nth-child(5), .invite-table .items-table-row span:nth-child(5) { flex: 0 0 120px; }

/* 
   【关键修复】针对邀请表格里的状态 span 
   让它变成胶囊状，而不是撑满整行
*/
.invite-table .items-table-row span:nth-child(5) span {
    display: inline-block;
    width: fit-content;    /* 关键：随字走 */
    padding: 4px 12px;
    border-radius: 20px;
    font-size: 12px;
}

.invite-table .items-table-header span:nth-child(6), .invite-table .items-table-row span:nth-child(6) { flex: 0 0 150px; gap: 8px; }


/* ================= 邀请状态的颜色定义 ================= */
/* 待确认 */
.status-pending { 
  background: #fffbe6; 
  color: #faad14; 
  border: 1px solid #ffe58f;
}
/* 已接受 */
.status-accepted { 
  background: #f6ffed; 
  color: #52c41a; 
  border: 1px solid #b7eb8f;
}
/* 已拒绝 */
.status-rejected { 
  background: #fff1f0; 
  color: #ff4d4f; 
  border: 1px solid #ffa39e;
}
</style>


/* ================银行回复卡片容器 ================*/
<style scoped>
.offer-card {
  background: #ffffff;
  border: 1px solid #e0e0e0;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05); /* 轻微阴影，更有质感 */
  position: relative;
  overflow: hidden;
}

/* 顶部装饰条（可选，增加层次感） */
.offer-card::before {
  content: "";
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 4px;
  background: linear-gradient(90deg, #52c41a, #95de64);
}

/* --- 顶部头部 --- */
.offer-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.offer-title {
  font-weight: bold;
  color: #333;
  font-size: 15px;
  display: flex;
  align-items: center;
  gap: 6px;
}

/* 状态小标签 (右上角) */
.status-tag {
  font-size: 12px;
  padding: 4px 12px;
  border-radius: 20px;
  font-weight: 500;
}
.status-tag.pending { background: #fffbe6; color: #faad14; border: 1px solid #ffe58f; }
.status-tag.accepted { background: #f6ffed; color: #52c41a; border: 1px solid #b7eb8f; }
.status-tag.rejected { background: #fff1f0; color: #ff4d4f; border: 1px solid #ffa39e; }


/* --- 中间核心数据区 (左右布局) --- */
.offer-body {
  display: flex;
  align-items: center;
  background: #f9fbf9; /* 非常淡的绿色背景 */
  padding: 15px;
  border-radius: 8px;
  margin-bottom: 16px;
}

.data-item {
  flex: 1;
  text-align: center;
}

.data-item label {
  display: block;
  font-size: 12px;
  color: #888;
  margin-bottom: 4px;
}

.data-item .value {
  font-size: 24px;  /* 大字号 */
  font-weight: bold;
  color: #333;
  font-family: 'Arial', sans-serif;
}

.data-item .value.rate { color: #faad14; } /* 利率用橙色强调 */
.data-item .value.amount { color: #52c41a; } /* 金额用绿色强调 */
.data-item .value span { font-size: 14px; margin-left: 2px; }

/* 中间分割竖线 */
.data-divider {
  width: 1px;
  height: 30px;
  background: #e0e0e0;
}


/* --- 底部信息 --- */
.offer-footer {
  font-size: 13px;
  color: #666;
}

.note-box {
  background: #f5f5f5;
  padding: 8px 10px;
  border-radius: 6px;
  margin-bottom: 8px;
  line-height: 1.5;
}

.note-box .label {
  font-weight: bold;
  color: #333;
}

.time-stamp {
  text-align: right;
  font-size: 12px;
  color: #999;
}

/* --- 按钮区微调 --- */
.bank-action-buttons {
  margin-top: 15px;
  display: flex;
  gap: 15px;
  justify-content: flex-end;
}
</style>