<template>
  <div class="coupon-admin-container">

    <!-- 加载 / 空状态 -->
    <div v-if="loading" class="empty-state">加载中...</div>
    <div v-else-if="templates.length === 0" class="empty-state">
      暂无优惠券模板
    </div>

    <!-- 模板卡片列表 -->
    <div
        class="template-card"
        v-for="tpl in templates"
        :key="tpl.templateId"
    >
      <h4 class="template-name">{{ tpl.templateName }}</h4>
      <p class="template-desc">{{ tpl.description || '暂无描述' }}</p>

      <div class="info-block">
        <div class="info-row">
          <span class="label">类型:</span>
          <span class="value">{{ formatType(tpl.couponType) }}</span>
        </div>
        <div class="info-row">
          <span class="label">优惠额度:</span>
          <span class="value">{{ formatDiscount(tpl) }}</span>
        </div>
        <div class="info-row">
          <span class="label">状态:</span>
          <span class="value" :class="statusColor(tpl.status)">
            {{ formatStatus(tpl.status) }}
          </span>
        </div>
        <div class="info-row">
          <span class="label">有效期:</span>
          <span class="value">{{ formatValidity(tpl) }}</span>
        </div>
        <div class="info-row">
          <span class="label">最大优惠:</span>
          <span class="value">{{ tpl.maxDiscountAmount || '-' }}</span>
        </div>
        <div class="info-row">
          <span class="label">适用范围:</span>
          <span class="value">{{ formatScope(tpl.applicableScope) }}</span>
        </div>
        <div class="info-row">
          <span class="label">总量 / 限领:</span>
          <span class="value">{{ tpl.totalQuantity }} / {{ tpl.perUserLimit }}</span>
        </div>
      </div>

      <div class="card-actions">
        <button class="action-btn" @click="toggleStatus(tpl)">
          {{ tpl.status === 'active' ? '停用' : '启用' }}
        </button>
        <button class="action-btn" @click="openEditModal(tpl)">
          编辑
        </button>
        <button class="action-btn" @click="openBatchIssueModal(tpl)">
          批量发放
        </button>
      </div>
    </div>

    <!-- 创建悬浮按钮 -->
    <button class="create-btn" @click="openCreateModal">
      + 创建优惠券模板
    </button>

    <!-- ================= 弹窗（新增/编辑优惠券模板） ================= -->
    <div v-if="modalVisible" class="modal-overlay">
      <div class="modal-container">

        <!-- 关闭按钮 -->
        <button class="close-btn" @click="closeModal">×</button>

        <h2 class="modal-title">{{ currentTemplate ? '编辑模板' : '创建模板' }}</h2>

        <div class="modal-body">
          <!-- 模板名称 -->
          <div class="modal-form-group row-layout">
            <label>模板名称：</label>
            <input v-model="form.templateName" type="text" placeholder="请输入模板名称" required />
          </div>

          <!-- 优惠类型 -->
          <div class="modal-form-group row-layout">
            <label>优惠类型：</label>
            <select v-model="form.couponType" required>
              <option value="full_reduction">满减</option>
              <option value="discount">折扣</option>
              <option value="fixed_amount">固定金额</option>
              <option value="free_shipping">免运费</option>
            </select>
          </div>

          <!-- 优惠额度 -->
          <div class="modal-form-group row-layout">
            <label>优惠额度：</label>
            <input type="number" step="0.01" v-model="form.discountValue" required />
          </div>

          <!-- 最低订单金额 -->
          <div class="modal-form-group row-layout">
            <label>最低使用金额：</label>
            <input type="number" step="0.01" v-model="form.minOrderAmount" />
          </div>

          <!-- 最大优惠金额 -->
          <div class="modal-form-group row-layout">
            <label>最大优惠金额：</label>
            <input type="number" step="0.01" v-model="form.maxDiscountAmount" />
          </div>

          <!-- 适用范围 -->
          <div class="modal-form-group row-layout">
            <label>适用范围：</label>
            <select v-model="form.applicableScope" required>
              <option value="all">全场</option>
              <option value="category">分类</option>
              <option value="product">指定商品</option>
            </select>
          </div>

          <!-- 适用对象ID -->
          <div v-if="form.applicableScope !== 'all'" class="modal-form-group row-layout">
            <label>适用对象ID列表：</label>
            <input v-model="form.applicableValues" placeholder="1,2,3" />
          </div>

          <!-- 发行总量 -->
          <div class="modal-form-group row-layout">
            <label>发行总量：</label>
            <input type="number" v-model="form.totalQuantity" required />
          </div>

          <!-- 每人限领 -->
          <div class="modal-form-group row-layout">
            <label>每人限领：</label>
            <input type="number" v-model="form.perUserLimit" required />
          </div>

          <!-- 有效期类型 -->
          <div class="modal-form-group row-layout">
            <label>有效期类型：</label>
            <select v-model="form.validityType" required>
              <option value="fixed">固定日期</option>
              <option value="relative">相对天数</option>
            </select>
          </div>

          <!-- 固定日期 -->
          <div v-if="form.validityType==='fixed'" class="modal-form-group row-layout">
            <label>开始时间：</label>
            <input type="datetime-local" v-model="form.validityStart" required />
            <label>结束时间：</label>
            <input type="datetime-local" v-model="form.validityEnd" required />
          </div>

          <!-- 相对天数 -->
          <div v-else class="modal-form-group row-layout">
            <label>有效天数：</label>
            <input type="number" v-model="form.validityDays" required />
          </div>

          <!-- 描述 -->
          <div class="modal-form-group">
            <label>描述：</label>
            <textarea v-model="form.description" placeholder="模板描述"></textarea>
          </div>

        </div>

        <div class="modal-footer">
          <button class="cancel-btn" @click="closeModal">取消</button>
          <button class="save-btn" @click="saveTemplate">保存</button>
        </div>

      </div>
    </div>

    <!-- ================= 批量发放弹窗 ================= -->
    <div v-if="batchModalVisible" class="modal-overlay">
      <div class="modal-container">

        <!-- 关闭按钮 -->
        <button class="close-btn" @click="closeBatchModal">×</button>

        <h2 class="modal-title">批量发放优惠券</h2>

        <div class="modal-body">

          <div class="modal-form-group row-layout">
            <label class="select-all-btn">
              <input
                  type="checkbox"
                  v-model="selectAll"
                  @change="toggleSelectAll"
              />
              <span>全选</span>
            </label>
          </div>


          <!-- 用户列表（网格） -->
          <div class="modal-form-group">
            <div class="user-grid">
              <label
                  v-for="user in userList"
                  :key="user.userId"
                  class="user-card"
              >
                <input
                    type="checkbox"
                    :value="user.userId"
                    v-model="selectedUserIds"
                />
                <div class="user-info">
                  <div class="username">{{ user.username }}</div>
                  <div class="userid">ID：{{ user.userId }}</div>
                </div>
              </label>
            </div>
          </div>


        </div>

        <div class="modal-footer">
          <button class="cancel-btn" @click="closeBatchModal">取消</button>
          <button class="save-btn" @click="batchIssue">发放</button>
        </div>

      </div>
    </div>


  </div>
</template>

<script setup>
import {ref, onMounted, reactive, watch} from 'vue'
import axios from '@/utils/axios'

const templates = ref([])
const loading = ref(false)
const modalVisible = ref(false)
const batchModalVisible = ref(false)
const currentTemplate = ref(null)

const form = reactive({
  templateName: '',
  couponType: 'full_reduction',
  discountValue: 0,
  minOrderAmount: 0,
  maxDiscountAmount: null,
  applicableScope: '',
  applicableValues: '',
  totalQuantity: 1,
  perUserLimit: 1,
  validityType: 'fixed',
  validityStart: '',
  validityEnd: '',
  validityDays: 1,
  description: '',
})


const batchForm = reactive({
  userIds: '',
})

const userList = ref([])              // 所有用户
const selectedUserIds = ref([])       // 选中的用户ID
const selectAll = ref(false)           // 是否全选


const loadTemplates = async () => {
  loading.value = true
  try {
    const res = await axios.get('/coupons/templates')
    templates.value = res.data.data.records || []
    console.log('模板数据：',templates.value.length)
  } catch (e) {
    console.error('加载模板失败:', e)
  } finally {
    loading.value = false
  }
}

const formatType = (type) => {
  switch(type){
    case 'full_reduction': return '满减'
    case 'discount': return '折扣'
    case 'fixed_amount': return '固定金额'
    case 'free_shipping': return '免运费'
    default: return type
  }
}

const formatDiscount = (tpl) => {
  if(tpl.couponType==='discount') return `${tpl.discountValue}%`
  if(tpl.couponType==='full_reduction') return `满${tpl.minOrderAmount}减${tpl.discountValue}`
  if(tpl.couponType==='fixed_amount') return `${tpl.discountValue}元`
  if(tpl.couponType==='free_shipping') return '免运费'
  return ''
}

const formatStatus = (status) => {
  switch(status){
    case 'draft': return '草稿'
    case 'active': return '启用'
    case 'inactive': return '停用'
    case 'expired': return '已过期'
    default: return status
  }
}

const formatScope = (scope) => {
  switch(scope){
    case 'all': return '全场'
    case 'category': return '指定类别'
    case 'product': return '指定商品'
    case 'farmer': return '指定农户'
    default: return scope
  }
}

const statusColor = (status) => {
  if(status==='active') return 'text-green-500'
  if(status==='inactive') return 'text-gray-500'
  if(status==='expired') return 'text-red-500'
  return ''
}

const formatValidity = (tpl) => {
  if(tpl.validityType==='fixed'){
    return `${tpl.validityStart} ~ ${tpl.validityEnd}`
  }else{
    return `领取后${tpl.validityDays}天有效`
  }
}

const toggleStatus = async (tpl) => {
  const newStatus = tpl.status==='active' ? 'inactive' : 'active'
  try{
    await axios.put(`/coupons/templates/${tpl.templateId}/status?status=${newStatus}`)
    tpl.status = newStatus
  }catch(e){
    console.error('状态更新失败', e)
  }
}

const openCreateModal = () => {
  currentTemplate.value = null
  Object.assign(form, {
    templateName: '',
    couponType: 'full_reduction',
    discountValue: 0,
    minOrderAmount: 0,
    maxDiscountAmount: null,   
    applicableScope: '',
    applicableValues: '',      
    totalQuantity: 1,
    perUserLimit: 1,
    validityType: 'fixed',
    validityStart: '',
    validityEnd: '',
    validityDays: 1,
    description: '',
  })
  modalVisible.value = true
}

const openEditModal = (tpl) => {
  currentTemplate.value = { ...tpl }
  Object.assign(form, {
    templateName: tpl.templateName,
    couponType: tpl.couponType,
    discountValue: tpl.discountValue,
    minOrderAmount: tpl.minOrderAmount,
    maxDiscountAmount: tpl.maxDiscountAmount,
    applicableScope: tpl.applicableScope,
    applicableValues: tpl.applicableValues,  
    totalQuantity: tpl.totalQuantity,
    perUserLimit: tpl.perUserLimit,
    validityType: tpl.validityType,
    validityStart: tpl.validityStart,
    validityEnd: tpl.validityEnd,
    validityDays: tpl.validityDays,
    description: tpl.description,
  })
  modalVisible.value = true
}


const closeModal = () => modalVisible.value = false
const openBatchIssueModal = async (tpl) => {
  currentTemplate.value = {...tpl}
  batchForm.userIds = ''
  batchModalVisible.value = true

  selectedUserIds.value = []
  selectAll.value = false

  const res = await axios.get('/user/admin/list')
  if (res.data.success) {
    userList.value = res.data.users
  }
}
const closeBatchModal = () => batchModalVisible.value = false

const saveTemplate = async () => {
  try {
    if(currentTemplate.value){
      // 编辑
      await axios.post(`/coupons/templates`, {...form, templateId: currentTemplate.value.templateId})
    }else{
      // 新建
      await axios.post(`/coupons/templates`, form)
    }
    alert('模板保存成功！')
    modalVisible.value = false
    await loadTemplates()
  }catch(e){
    console.error('保存失败', e)
  }
}

const toggleSelectAll = () => {
  if (selectAll.value) {
    selectedUserIds.value = userList.value.map(u => u.userId)
  } else {
    selectedUserIds.value = []
  }
}

watch(selectedUserIds, (val) => {
  selectAll.value = val.length === userList.value.length && val.length > 0
})

const batchIssue = async () => {
  if (selectedUserIds.value.length === 0) {
    alert('请至少选择一个用户')
    return
  }

  try {
    await axios.post('/coupons/batch-issue', {
      templateId: currentTemplate.value.templateId,
      userIds: selectedUserIds.value
    })

    batchModalVisible.value = false
    await loadTemplates()
  } catch (e) {
    console.error('批量发放失败', e)
  }
}


onMounted(loadTemplates)
</script>

<style scoped>
.coupon-admin-container {
  display: flex;
  flex-wrap: wrap;
  gap: 1rem;
  padding: 1rem;
}

.template-card {
  width: 300px;
  background: #fff;
  border-radius: 12px;
  padding: 1rem;
  box-shadow: 0 2px 6px rgba(0,0,0,0.1);
}

.template-name {
  font-weight: 600;
  font-size: 1.1rem;
  color: #2f855a;
}

.template-desc {
  font-size: 0.875rem;
  color: #718096;
  margin: 0.25rem 0 0.5rem;
}

.info-block {
  font-size: 0.875rem;
  margin-bottom: 0.5rem;
}

.info-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 0.25rem;
}

.card-actions {
  display: flex;
  gap: 0.5rem;
  margin-top: 0.5rem;
}

.action-btn, .create-btn {
  background-color: #38a169;
  color: white;
  border: none;
  border-radius: 6px;
  padding: 0.5rem 0.75rem;
  cursor: pointer;
}

.create-btn {
  position: fixed;
  bottom: 2rem;
  right: 2rem;
}


/*网格用户选择*/
.user-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);

  gap: 12px;
  max-height: 360px;
  overflow-y: auto;
  padding: 4px;
}

.user-card {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  padding: 10px;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s ease;
  background: #fff;
}

.user-card:hover {
  border-color: #409eff;
  background: #f5faff;
}

.user-card input[type="checkbox"] {
  margin-top: 4px;
}

.user-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.username {
  font-size: 14px;
  font-weight: 500;
}

.userid {
  font-size: 12px;
  color: #909399;
}

.modal-body{
  padding-right:0;
}

/*全选按钮*/
.select-all-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 6px 14px;
  border: 1px solid #dcdfe6;
  border-radius: 20px;
  cursor: pointer;
  background: #fff;
  font-size: 14px;
  transition: all 0.2s;
}

.select-all-btn:hover {
  border-color: #409eff;
  color: #409eff;
}

.select-all-btn input {
  display: none;
}

/* 选中状态 */
.select-all-btn:has(input:checked) {
  background: #409eff;
  color: #fff;
  border-color: #409eff;
}

</style>
