<template>
  <div class="main-bg">
    <HeaderComponent />

    <section class="content">

      <!-- 顶部用户信息 + 退出 -->
      <div class="top-info-bar">
        <div class="info">
          <p><label>用户名：</label>{{ userInfo.userName }}</p>
          <p><label>邮箱：</label>{{ userInfo.email }}</p>
          <p><label>身份：</label>{{ role }}</p>
        </div>
        <div>
          <button @click="exit()">退出登录</button>
        </div>
      </div>

      <!-- 统一顶部导航 -->
      <nav class="main-nav">
        <!-- 买家 -->
        <template v-if="role === 'buyer'">
          <button @click="switchView('address')" :class="{ active: currentView === 'address' }">我的地址</button>
        </template>

        <!-- 农户 -->
        <template v-if="role === 'farmer'">
          <button @click="switchView('address')" :class="{ active: currentView === 'address' }">我的地址</button>
          <button @click="switchView('appointments')" :class="{ active: currentView === 'appointments' }">我的预约</button>
        </template>

        <!-- 专家 -->
        <template v-if="role === 'expert'">
          <button @click="switchView('profile')" :class="{ active: currentView === 'profile' }">个人档案</button>
          <button @click="switchView('knowledgeManage')" :class="{ active: currentView === 'knowledgeManage' }">知识管理</button>
          <button @click="switchView('availability')" :class="{ active: currentView === 'availability' }">可预约时间</button>
          <button @click="switchView('schedule')" :class="{ active: currentView === 'schedule' }">我的日程</button>
        </template>
      </nav>

      <!-- 内容区域 -->
      <div class="view-content-wrapper">

        <!-- ======================== 买家/农户：我的地址 ======================== -->
        <div v-if="currentView === 'address'" class="address-view">

          <!-- 地址列表 -->
          <div v-if="addresses.length" class="address-list">
            <div
                v-for="addr in addresses"
                :key="addr.addressId"
                class="address-card"
            >
              <p v-if="addr.isDefault">🌟 默认地址</p>
              <p><strong>{{ role === 'buyer' ? '收货人' : '发货人' }}：</strong>{{ addr.recipientName }}</p>
              <p><strong>电话：</strong>{{ addr.phoneNumber }}</p>
              <p><strong>地址：</strong>{{ addr.province }}{{ addr.city }}{{ addr.district }}{{ addr.streetAddress }}</p>
              <p><strong>邮编：</strong>{{ addr.postalCode }}</p>

              <div class="card-actions">
                <button class="set-default-btn" @click="setDefault(addr.addressId)">设为默认</button>
                <button class="delete-btn" @click="deleteAddress(addr.addressId)">删除</button>
              </div>
            </div>
          </div>

          <p v-else class="empty-state">暂无地址，请添加新的地址。</p>

          <!-- 新增地址按钮（弹窗） -->
          <button class="add-btn" @click="showAddAddressPopup = true">＋ 新增地址</button>

          <!-- ========== 新增地址弹窗 ========== -->
          <!-- 新增地址弹窗 -->
          <div v-if="showAddAddressPopup" class="modal-overlay">
            <div class="modal-container">

              <!-- 右上角关闭按钮 -->
              <button class="close-btn" @click="showAddAddressPopup = false">×</button>

              <h2 class="modal-title">新增地址</h2>

              <div class="modal-body">

                <!-- 姓名 -->
                <div class="modal-form-group row-layout">
                  <label>姓名：</label>
                  <input v-model="newAddress.recipientName" type="text" placeholder="请输入姓名" />
                </div>

                <!-- 电话 -->
                <div class="modal-form-group row-layout">
                  <label>电话：</label>
                  <input v-model="newAddress.phoneNumber" type="text" placeholder="请输入联系电话" />
                </div>

                <!-- 省份 -->
                <div class="modal-form-group row-layout">
                  <label>省份：</label>
                  <input v-model="newAddress.province" type="text" placeholder="例如：浙江省" />
                </div>

                <!-- 城市 -->
                <div class="modal-form-group row-layout">
                  <label>城市：</label>
                  <input v-model="newAddress.city" type="text" placeholder="请输入城市" />
                </div>

                <!-- 区县 -->
                <div class="modal-form-group row-layout">
                  <label>区县：</label>
                  <input v-model="newAddress.district" type="text" placeholder="请输入区/县" />
                </div>

                <!-- 详细地址 -->
                <div class="modal-form-group">
                  <label>详细：</label>
                  <textarea
                      v-model="newAddress.streetAddress"
                      placeholder="如：xx小区 xx号楼 xx单元"
                      rows="2"
                  ></textarea>
                </div>

                <!-- 邮编 -->
                <div class="modal-form-group row-layout">
                  <label>邮编：</label>
                  <input v-model="newAddress.postalCode" type="text" placeholder="邮政编码(选填)" />
                </div>

              </div>

              <!-- 底部按钮 -->
              <div class="modal-footer">
                <button class="cancel-btn" @click="showAddAddressPopup = false">取消</button>
                <button class="save-btn" @click="addAddress">保存</button>
              </div>

            </div>
          </div>


        </div>

        <!-- ======================== 农户：我的预约 ======================== -->
        <div v-if="currentView === 'appointments'" class="appointments-view">
          <div v-if="isLoadingAppointments" class="loading-state">正在加载预约记录...</div>

          <div v-else-if="appointments.length" class="appointments-list">
            <div v-for="appt in appointments" :key="appt.id" class="appointment-card" :class="{ 'is-cancelled': appt.status === 'cancelled' }">

              <div class="card-header">
                <h4>专家：{{ appt.expertName }}</h4>
                <span :class="['status-badge', translateStatus(appt.status)]">{{ appt.status }}</span>
              </div>

              <div v-if="appt.status === 'cancelled'" class="cancelled-overlay">
                已取消
              </div>
              <div v-if="appt.status === 'completed'" class="cancelled-overlay">
                已结束
              </div>

              <div class="card-body">
                <p><strong>预约日期：</strong>{{ appt.date }}</p>
                <p><strong>预约时间：</strong>{{ appt.timeSlot }}</p>
              </div>

              <div class="card-actions">
                <button v-if="appt.status === 'scheduled'" class="delete-btn" @click="cancelAppointment(appt.id)">
                  取消预约
                </button>
              </div>

            </div>
          </div>

          <p v-else class="empty-state">暂无预约记录。</p>
        </div>

        <!-- ======================== 专家：个人档案 ======================== -->
        <div v-if="currentView === 'profile'" class="expert-profile-container">
          <!-- 已创建档案 -->
          <div v-if="!isEditing && expertProfile" class="profile-card">
            <div class="profile-details">
              <img :src="expertProfile.photoUrl || defaultAvatar" class="profile-photo" />

              <div class="profile-info-text">
                <p><strong>{{ expertName || '专家姓名' }}</strong></p>
                <p><strong>专业领域：</strong>{{ expertProfile.specialization }}</p>
                <p><strong>咨询费：</strong>¥{{ expertProfile.consultationFee }} / 次</p>
                <p><strong>简介：</strong></p>
                <p class="bio">{{ expertProfile.bio }}</p>
              </div>
            </div>

            <div class="profile-actions">
              <button @click="enterEditMode">更新档案</button>
              <button class="delete-btn" @click="deleteProfile">删除档案</button>
            </div>
          </div>

          <!-- 创建/编辑档案 -->
          <div v-if="isEditing" class="profile-form">
            <h4>{{ expertProfile ? '更新档案' : '创建档案' }}</h4>

            <div class="form-group">
              <label>专业领域：</label>
              <input v-model="profileForm.specialization" />
            </div>

            <div class="form-group">
              <label>咨询费(元/次)：</label>
              <input v-model="profileForm.consultationFee" type="number" />
            </div>

            <div class="form-group">
              <label>简介：</label>
              <textarea v-model="profileForm.bio" rows="4"></textarea>
            </div>

            <div class="form-group">
              <label>上传头像：</label>
              <input type="file" @change="handleFileChange" />
            </div>

            <div class="form-actions">
              <button class="save-btn" @click="saveProfile">保存</button>
              <button @click="cancelEdit">取消</button>
            </div>
          </div>

          <!-- 未创建档案 -->
          <div v-if="!isEditing && !expertProfile" class="profile-prompt">
            <p>您还没有创建专家档案。</p>
            <button @click="enterEditMode">立即创建档案</button>
          </div>
        </div>

        <!-- ======================== 专家：知识管理 ======================== -->
        <div v-if="currentView === 'knowledgeManage'" class="knowledge-manage-container">

          <!-- 知识列表视图 -->
          <div v-if="!isEditingKnowledge">
            <div v-if="!knowledgeList.length" class="empty">
              暂无已发布的知识~
            </div>

            <div
                class="knowledge-card"
                v-for="item in knowledgeList"
                :key="item.knowledgeId"
            >
              <div class="knowledge-content">
                <h4>{{ item.title }}</h4>
                <p class="summary">{{ summary(item.content) }}</p>

                <div class="bottom">
                  <span class="time">{{ formatTime(item.createTime) }}</span>
                  <div class="action-buttons">
                    <button @click="editKnowledge(item)">编辑</button>
                    <button class="delete-btn" @click="deleteKnowledge(item.knowledgeId)">删除</button>
                  </div>
                </div>
              </div>
            </div>

            <!-- 发布新知识按钮 -->
            <div class="new-knowledge-btn">
              <button @click="openKnowledgeEditor">发布新知识</button>
            </div>
          </div>


          <!-- 创建/编辑知识表单 -->
          <div v-if="isEditingKnowledge" class="knowledge-form">
            <h4>{{ editingKnowledgeId ? '编辑知识' : '发布新知识' }}</h4>

            <div class="form-group">
              <label>标题：</label>
              <input v-model="knowledgeForm.title" placeholder="请输入标题" />
            </div>

            <div class="form-group">
              <label>内容：</label>
              <textarea v-model="knowledgeForm.content" rows="6" placeholder="请输入内容"></textarea>
            </div>

            <div class="form-actions">
              <button class="save-btn" @click="saveKnowledge">保存</button>
              <button @click="cancelKnowledgeEdit">取消</button>
            </div>
          </div>
        </div>


        <!-- ======================== 专家：可预约时间 ======================== -->
        <div v-if="currentView === 'availability'">
          <ExpertAvailability />
        </div>

        <!-- ======================== 专家：我的日程 ======================== -->
        <div v-if="currentView === 'schedule'" class="schedule-view">
          <h3>查看日程安排</h3>

          <div class="date-selector">
            <label>选择日期：</label>
            <input type="date" v-model="selectedDate" @change="fetchDailySchedule" />
          </div>

          <div v-if="isLoadingSchedule" class="loading-state">正在加载日程...</div>

          <div v-else-if="dailyAppointments.length" class="schedule-list">
            <div v-for="appt in dailyAppointments" :key="appt.consultationId" class="schedule-item">
              <div class="time-slot">{{ appt.timeSlot }}</div>
              <div class="details">
                <p><strong>农户：</strong>{{ appt.farmerName || `ID:${appt.farmerId}` }}</p>
              </div>
              <div class="status">
                <span :class="['status-badge', getStatusClass(appt.status)]">{{ appt.displayStatus }}</span>
              </div>
            </div>
          </div>

          <p v-else class="empty-state">该日期没有预约安排。</p>
        </div>
      </div>

    </section>
  </div>
</template>


<script setup>
import { ref, onMounted, watch } from 'vue'
import axios from '../utils/axios'
import HeaderComponent from "@/components/HeaderComponent.vue";
import ExpertAvailability from '../components/ExpertAvailability.vue';
import { useAuthStore } from '@/stores/authStore';
import { storeToRefs } from 'pinia';
import defaultAvatar from '@/assets/default.jpg';

const authStore = useAuthStore();// 使用 Pinia 的认证存储

const { userInfo, role } = storeToRefs(authStore);//从 store 中解构出响应式的数据

const hasInitialLoadFinished = ref(false)
const currentView = ref('');
const addresses = ref([])

const showAddAddressPopup = ref(false)
// 手动关闭弹窗方法（用于右上角 × 按钮）
function closeAddAddressPopup() {
  showAddAddressPopup.value = false
}
// 新增地址表单字段
const newAddress = ref({
  recipientName: '',
  phoneNumber: '',
  province: '',
  city: '',
  district: '',
  streetAddress: '',
  postalCode: ''
})

// 用于专家视图的标签页切换
const currentExpertView = ref('profile');

// === 专家档案相关状态 ===
const expertProfile = ref(null);
const isEditing = ref(false);
const profileForm = ref({
  specialization: '',
  bio: '',
  consultationFee: '',
});
const selectedFile = ref(null);
const expertName = ref('');

// === 专家预约相关状态 ===
const appointments = ref([]);
const isLoadingAppointments = ref(false);
const currentPage = ref(1);
const totalPages = ref(1);
const pageSize = ref(5); // 每页显示5条记录

// === 专家日程相关状态 ===
const selectedDate = ref(new Date().toISOString().split('T')[0]); // 默认为今天
const dailyAppointments = ref([]);
const isLoadingSchedule = ref(false);

// 切换视图
async function switchView(view) {
  currentView.value = view
  if (view === 'appointments') {
    await loadAppointments(1);
  }
}

// 加载地址
async function loadAddresses() {
  try {
    const res = await axios.get('/address/user')
    addresses.value = res.data || []
  } catch (err) {
    console.error('加载地址失败', err)
  }
}

// 添加地址
async function addAddress() {
  // 非空检查
  const { recipientName, phoneNumber, province, city, district, streetAddress } = newAddress.value;

  if (!recipientName || !phoneNumber || !province || !city || !district || !streetAddress) {
    alert('请填写完整的必填字段（姓名、电话、地址）');
    return;
  }

  try {
    const res = await axios.post('/address/add', newAddress.value);
    alert('新增地址成功！');

    // 添加到地址列表
    addresses.value.push(res.data);

    // 重置表单
    newAddress.value = {
      recipientName: '',
      phoneNumber: '',
      province: '',
      city: '',
      district: '',
      streetAddress: '',
      postalCode: ''
    };

    currentView.value = 'address'; // 回到地址列表
    showAddAddressPopup.value = false; // 关闭弹窗
  } catch (err) {
    alert('添加失败');
  }
}

// 删除地址
async function deleteAddress(id) {
  if (!confirm('确定删除该地址吗？')) return
  try {
    await axios.delete(`/address/delete/${id}`)
    addresses.value = addresses.value.filter(addr => addr.addressId !== id)
    alert('删除成功')
  } catch (err) {
    alert('删除失败')
  }
}

// 设置默认地址
async function setDefault(id) {
  try {
    await axios.put(`/address/set-default/${id}`)
    addresses.value.forEach(a => (a.isDefault = a.addressId === id))
    alert('设置默认成功')
  } catch (err) {
    alert('设置默认失败')
  }
}

// 专家视图标签页切换
async function switchExpertView(view) {
  currentExpertView.value = view;
  // 当切换到日程视图且数据为空时，自动加载当天的日程
  if (view === 'schedule' && dailyAppointments.value.length === 0) {
    await fetchDailySchedule();
  }
}

// === 专家档案方法 ===
// 获取专家档案
async function fetchExpertProfile() {
  try {
    const res = await axios.get('/expert/profile');
    const profile = res.data.data;

    if (!profile) {
      expertProfile.value = null;
      console.log('当前专家还未创建档案。');
      return;
    }

    try {
      const imageRes = await axios.get(`/expert/profile/${profile.expertId}/photo`, {
        responseType: 'blob'
      });
      if (imageRes.data.size > 0) {
        profile.photoUrl = URL.createObjectURL(imageRes.data);
      } else {
        throw new Error('空图片');
      }
    } catch {
      profile.photoUrl = defaultAvatar;
    }

    expertProfile.value = profile;

  } catch (error) {
    console.error('获取专家档案失败:', error);
    alert('获取专家档案失败，请稍后重试。');
  }
}


//加载专家名字
async function loadExpertName() {
  try {
    const response = await axios.get(`/expert/profile/list`);
    if (response.data && response.data.success) {
      const list = response.data.data;
      const item = list.find(obj => obj.id === String(userInfo.value.userId));
      expertName.value = item ? item.name : '专家'
    } else {
      expertName.value = '专家'
    }
  } catch (error) {
    console.error(`获取专家(ID: ${expertId})姓名失败:`, error);
    expertName.value = '专家'
  }
} 

//编辑专家档案
function enterEditMode() {
  // 如果已有档案，用现有数据填充表单；否则，用空数据
  profileForm.value = expertProfile.value
      ? { ...expertProfile.value }
      : { specialization: '', bio: '', consultationFee: '' };
  isEditing.value = true;
}

//取消编辑
function cancelEdit() {
  isEditing.value = false;
  selectedFile.value = null; // 清除已选文件
}

//处理文件选择
function handleFileChange(event) {
  selectedFile.value = event.target.files[0];
}

//保存专家档案
async function saveProfile() {
  const formData = new FormData();
  // 将表单数据添加到 FormData 对象
  formData.append('specialization', profileForm.value.specialization);
  formData.append('bio', profileForm.value.bio);
  formData.append('consultationFee', profileForm.value.consultationFee);
  if (selectedFile.value) {
    formData.append('photo', selectedFile.value);
  }

  try {
    // 使用一个接口同时处理创建和更新
    await axios.post('/expert/profile', formData, {
      headers: {
        'Content-Type': 'multipart/form-data' // 文件上传请求头
      }
    });
    alert('档案保存成功！');
    isEditing.value = false;
    selectedFile.value = null;
    await fetchExpertProfile(); // 保存成功后刷新档案数据
  } catch (error) {
    console.error('保存专家档案失败', error);
    alert('保存失败：' + (error.response?.data?.message || '请检查输入内容'));
  }
}

//删除专家档案
async function deleteProfile() {
  if (!confirm('确定要删除您的专家档案吗？此操作不可撤销。')) return;

  try {
    await axios.delete('/expert/profile'); // API: 删除当前专家档案
    alert('档案删除成功！');
    expertProfile.value = null; // 清空本地数据
    isEditing.value = false; // 如果在编辑模式下删除，则退出编辑
  } catch (error) {
    console.error('删除专家档案失败', error);
    alert('删除失败，请稍后重试。');
  }
}

// === 专家知识管理相关状态 ===
const knowledgeList = ref([])
const isEditingKnowledge = ref(false)
const editingKnowledgeId = ref(null)

const knowledgeForm = ref({
  title: '',
  content: ''
})

/** 获取当前专家发布的知识 */
async function fetchExpertKnowledge() {
  try {
    const res = await axios.get(`/knowledge/expert/${userInfo.value.userId}`)
    const data = res.data
    knowledgeList.value = data.records || []
  } catch (e) {
    console.error('加载知识失败', e)
  }
}

/** 打开发布页 */
function openKnowledgeEditor() {
  editingKnowledgeId.value = null
  knowledgeForm.value = { title: '', content: '' }
  isEditingKnowledge.value = true
}

/** 保存知识 */
async function saveKnowledge() {
  try {
    if (!knowledgeForm.value.title || !knowledgeForm.value.content) {
      return alert('标题和内容不能为空!')
    }

    if (editingKnowledgeId.value) {
      // 编辑
      await axios.put(`/knowledge/${editingKnowledgeId.value}`, knowledgeForm.value)
    } else {
      // 发布
      await axios.post('/knowledge/publish', knowledgeForm.value)
    }

    isEditingKnowledge.value = false
    fetchExpertKnowledge() // 刷新列表
  } catch (e) {
    console.error(e)
  }
}

/** 编辑知识 */
function editKnowledge(item) {
  editingKnowledgeId.value = item.knowledgeId
  knowledgeForm.value = {
    title: item.title,
    content: item.content
  }
  isEditingKnowledge.value = true
}

/** 删除知识 */
async function deleteKnowledge(id) {
  if (!confirm("确定删除该知识？")) return

  try {
    await axios.delete(`/knowledge/${id}`)
    fetchExpertKnowledge()
  } catch (e) {
    console.error(e)
  }
}

/** 取消编辑 */
function cancelKnowledgeEdit() {
  isEditingKnowledge.value = false
}

/** 内容摘要 */
function summary(text) {
  return text?.length > 60 ? text.slice(0, 60) + "..." : text
}

function formatTime(time) {
  return time ? time.replace('T', ' ') : ''
}

//====我的专家预约相关方法====
//加载预约记录
async function loadAppointments(page) {
  isLoadingAppointments.value = true;
  try {
    
    const response = await axios.get('/expert-appointments/my', {
      params: {
        pageNum: page,
        pageSize: pageSize.value
      }
    });
    
    const rawAppointments = response.data?.records || [];
    totalPages.value = response.data?.pages || 1;

    appointments.value = rawAppointments.map(appt => {
      
      const dateTime = new Date(appt.consultationTime);
      
      return {
        
        ...appt,
        
        id: appt.consultationId, 
        date: dateTime.toLocaleDateString(), 
        timeSlot: dateTime.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }), 
        displayStatus: translateStatus(appt.status) 
      };
    });

    currentPage.value = page;
  } catch (error) {
    console.error('加载预约记录失败:', error);
    alert('加载预约记录失败，请稍后重试。');
    appointments.value = [];
  } finally {
    isLoadingAppointments.value = false;
  }
}

//取消预约
async function cancelAppointment(appointmentId) {
  if (!confirm('您确定要取消这个预约吗？')) {
    return;
  }
  try {
    const response = await axios.post('/expert-appointments/cancel', null, {
      params: {
        consultationId: appointmentId
      }
    });
    
    console.log('取消预约后端返回:', response.data);

    if (response.data === true) {
      alert('预约已成功取消！');
      await loadAppointments(currentPage.value);
    } else {
      alert('取消操作失败，请稍后重试。');
    }
  } catch (error) {
    console.error('取消预约失败:', error);
    alert('取消预约失败：' + (error.response?.data?.message || '请稍后重试。'));
  }
}

//下一页
function goToNextPage() {
  if (currentPage.value < totalPages.value) {
    loadAppointments(currentPage.value + 1);
  }
}

//上一页
function goToPreviousPage() {
  if (currentPage.value > 1) {
    loadAppointments(currentPage.value - 1);
  }
}

//查询状态对应的样式类
function translateStatus(status) {
  switch (status) {
    case 'scheduled': return '已预约';
    case 'completed': return '已完成';
    case 'cancelled': return '已取消';
    default: return '未知状态';
  }
}

function getStatusClass(status) {
  // 根据英文状态返回不同的 CSS 类名
  switch (status) {
    case 'scheduled': return 'status-confirmed';
    case 'completed': return 'status-completed';
    case 'cancelled': return 'status-cancelled';
    default: return '';
  }
}

//=====专家日程相关方法=====
// 获取指定日期的日程安排
async function fetchDailySchedule() {
  isLoadingSchedule.value = true;
  dailyAppointments.value = []; // 清空旧数据

  try {
    const response = await axios.get('/expert-appointments/expert/day', {
      params: {
        date: selectedDate.value
      }
    });
    
    const rawAppointments = response.data?.records || [];
    
    const sortedAppointments = rawAppointments.sort((a, b) => 
      new Date(a.consultationTime) - new Date(b.consultationTime)
    );
    
    dailyAppointments.value = sortedAppointments.map(appt => {
      const dateTime = new Date(appt.consultationTime);
      return {
        ...appt,
        timeSlot: dateTime.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
        displayStatus: translateStatus(appt.status) 
      };
    });

  } catch (error) {
    console.error(`获取日期 ${selectedDate.value} 的日程失败:`, error);
    alert('加载日程失败，请稍后重试。');
  } finally {
    isLoadingSchedule.value = false;
  }
}


onMounted(() => {
  if (role.value === 'farmer' || role.value === 'buyer') {
    currentView.value = 'address';
  } else if (role.value === 'expert') {
    currentView.value = 'profile';
  } else {
    currentView.value = '';
  }

  hasInitialLoadFinished.value = true;
});

watch(role, (newRole, oldRole) => {
  console.log(`角色从 '${oldRole}' 变为 '${newRole}'`);
  
  // 只有当角色从一个无效值变为一个有效值时，才执行加载
  if (newRole && newRole !== '未登录' && !hasInitialLoadFinished.value) {
    hasInitialLoadFinished.value = true; // 标记为已加载
    loadDataForRole(newRole);
  }
}, { immediate: true }); 

async function loadDataForRole(currentRole) {
  try {
    if (currentRole === 'expert') {
      console.log("角色确认为专家，开始加载个人档案...");
      await fetchExpertProfile();
      await loadExpertName();
      await fetchExpertKnowledge();
    } else if (currentRole === 'buyer' || currentRole === 'farmer') {
      console.log("角色确认为买家/农户，开始加载地址...");
      await loadAddresses();
    } else {
      console.log(`未知的用户角色: ${currentRole}，不执行额外加载操作。`);
    }
  } catch (err) {
    console.error('后续加载失败', err);
  }
}

function exit(){
  authStore.logout();
}

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

.top-info-bar{
  display: flex;
  justify-content: space-between;
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

.farmer-nav, .buyer-nav ,.expert-nav{
  display: flex;
  border-bottom: 2px solid #e0e0e0;
  margin-bottom: 25px;
}

.farmer-nav button, .buyer-nav button, .expert-nav button  {
  padding: 10px 20px;
  border: none;
  background-color: transparent;
  cursor: pointer;
  font-size: 1.1rem;
  color: #555;
  transition: color 0.3s, border-bottom-color 0.3s;
  border-bottom: 3px solid transparent;
}

.farmer-nav button.active,
.buyer-nav button.active,
.expert-nav button.active {
  color: #2D7D4F;
  border-bottom-color: #2D7D4F;
}

.address-card {
  display: flex;
  align-items: center;
  padding: 8px 16px;
  border: 1px solid #e9e9e9;
  border-radius: 8px;
  margin-bottom: 10px;
  background: #fafafa;
  gap: 30px;
}

.card-actions {
  margin-left: auto;
  margin-top: 10px;
}

.delete-btn, .save-btn {
  background-color: #4CAF50;
  color: #fff;
  border: none;
  padding: 6px 12px;
  border-radius: 5px;
  cursor: pointer;
}

.delete-btn:hover, .save-btn:hover {
  background-color: #45a049;
}

.add-address-form {
  max-width: 600px;
}

.form-group {
  margin-bottom: 15px;
  display: flex;
  flex-direction: column;
}
.form-group label {
  font-weight: bold;
  margin-bottom: 5px;
}
.form-group input, .form-group textarea {
  padding: 8px;
  border: 1px solid #ccc;
  border-radius: 4px;
}
.empty-state {
  text-align: center;
  color: #888;
  padding: 2rem;
}

.info {
  padding-left: 2rem;
  text-align: left;
}

.expert-profile-container {
  margin-top: 2rem;
  padding: 1.5rem;
  background-color: #fff;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
}

.expert-profile-container h3 {
  margin-top: 0;
  color: #2D7D4F;
  border-bottom: 2px solid #F0F9F4;
  padding-bottom: 0.5rem;
  margin-bottom: 1.5rem;
}

.profile-card {
  margin-top: 1rem;
}

.profile-details {
  display: flex;
  gap: 1.5rem;
  align-items: flex-start;
}

.profile-photo {
  width: 120px;
  height: 120px;
  border-radius: 50%;
  object-fit: cover;
  border: 3px solid #e0e0e0;
  background-color: #f0f0f0;
}

.profile-info-text p {
  margin: 0.5rem 0;
  line-height: 1.6;
}
.profile-info-text .bio {
  white-space: pre-wrap; /* 保留简介中的换行和空格 */
}


.profile-actions, .form-actions {
  margin-top: 1.5rem;
  display: flex;
  gap: 1rem;
}

.profile-actions button, .form-actions button, .profile-prompt button {
  padding: 8px 16px;
  border-radius: 5px;
  border: 1px solid #2D7D4F;
  background-color: #2D7D4F;
  color: white;
  cursor: pointer;
  transition: background-color 0.3s;
}

.profile-actions button:hover, .form-actions button:hover, .profile-prompt button:hover {
  background-color: #256842;
}

.profile-actions .delete-btn {
  background-color: #c82333;
  border-color: #bd2130;
}
.profile-actions .delete-btn:hover {
  background-color: #a71d2a;
}
.form-actions button:last-child {
  background-color: #f0f0f0;
  color: #333;
  border-color: #ccc;
}
.form-actions button:last-child:hover {
   background-color: #e0e0e0;
}

.profile-prompt {
  text-align: center;
  padding: 2rem;
  background-color: #F0F9F4;
  border-radius: 8px;
  margin-top: 1rem;
}
.profile-form h4 {
  margin-bottom: 1.5rem;
}

.appointments-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 1.5rem;
}

.appointment-card {
  border: 1px solid #e9e9e9;
  border-radius: 8px;
  padding: 1rem 1.5rem;
  background: #fafafa;
  display: flex;
  flex-direction: column;
}

.appointment-card .card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #eee;
  padding-bottom: 0.5rem;
  margin-bottom: 1rem;
}
.appointment-card .card-header h4 {
  margin: 0;
  color: #333;
}

.appointment-card .card-body p {
  margin: 0.5rem 0;
  color: #555;
}

.appointment-card .card-actions {
  margin-top: auto; /* Pushes actions to the bottom */
  padding-top: 1rem;
  text-align: right;
}

/* Status Badges */
.status-badge {
  padding: 4px 10px;
  border-radius: 12px;
  font-size: 0.85rem;
  font-weight: bold;
  color: white;
}
.status-confirmed { background-color: #28a745; } /* Green */
.status-completed { background-color: #17a2b8; } /* Blue */
.status-cancelled { background-color: #6c757d; } /* Gray */

/* Pagination Styles */
.pagination-controls {
  margin-top: 2rem;
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 1rem;
  grid-column: 1 / -1; /* Make it span all columns in the grid */
}

.pagination-controls button {
  padding: 8px 16px;
  border-radius: 5px;
  border: 1px solid #ccc;
  background-color: #fff;
  cursor: pointer;
}
.pagination-controls button:disabled {
  cursor: not-allowed;
  opacity: 0.5;
}
.pagination-controls span {
  font-weight: bold;
}

.loading-state, .empty-state {
  text-align: center;
  color: #888;
  padding: 2rem;
  font-size: 1.1rem;
}

.date-selector {
  margin-bottom: 2rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}
.date-selector label {
  font-weight: bold;
}
.date-selector input[type="date"] {
  padding: 8px;
  border: 1px solid #ccc;
  border-radius: 4px;
  font-size: 1rem;
}

.schedule-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.schedule-item {
  display: flex;
  align-items: center;
  padding: 1rem;
  border: 1px solid #e9e9e9;
  border-radius: 8px;
  background-color: #fafafa;
}

.schedule-item .time-slot {
  font-size: 1.5rem;
  font-weight: bold;
  color: #2D7D4F;
  width: 120px;
  flex-shrink: 0;
  text-align: center;
}

.schedule-item .details {
  flex-grow: 1;
  border-left: 2px solid #e0e0e0;
  padding-left: 1.5rem;
  margin-left: 1.5rem;
}
.schedule-item .details p {
  margin: 0;
}

.schedule-item .status {
  margin-left: 1rem;
}

</style>

<style scoped>
/* ================= 弹窗遮罩 ================= */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0,0,0,0.5); /* 半透明遮罩 */
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

/* ================= 弹窗容器 ================= */
.modal-container {
  background-color: #fff;
  width: 500px;
  max-width: 90%;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.2);
  position: relative;
}

/* ================= 弹窗标题 ================= */
.modal-title {
  font-size: 20px;
  font-weight: 600;
  margin-bottom: 15px;
  text-align: center;
}

/* ================= 右上角关闭按钮 ================= */
.close-btn {
  position: absolute;
  top: 12px;
  right: 12px;
  border: none;
  background: transparent;
  font-size: 22px;
  cursor: pointer;
  color: #888;
  transition: color 0.2s;
}

.close-btn:hover {
  color: #333;
}

/* 标题 */
.modal-title {
  text-align: center;
  font-size: 20px;
  margin-bottom: 15px;
  color: #2a7f2a;
}

/* 表单部分 */
.modal-body {
  flex: 1;
  overflow-y: auto;
  padding-right: 5px;
}

.modal-form-group {
  margin-bottom: 15px;
  display: flex;
  flex-direction: row;
}

.modal-form-group label {
  width:80px;
  display: inline-block;
  text-align: justify;
  font-size: 14px;
  margin-bottom: 5px;
}

.modal-form-group input,
.modal-form-group textarea,
.modal-form-group select {
  flex: 1;
  width: 100%;
  padding: 6px 10px;
  border: 1px solid #ccc;
  border-radius: 6px;
  outline: none;
}

/* 行布局 */
.row-layout {
  display: flex;
  align-items: center;
}

/* ================= 底部按钮 ================= */
.modal-footer {
  display: flex;
  justify-content: flex-end;
  margin-top: 15px;
  gap: 10px;
}

.save-btn {
  background-color: #4caf50;
  color: #fff;
  border: none;
  padding: 6px 16px;
  border-radius: 4px;
  cursor: pointer;
}

.save-btn:hover {
  background-color: #45a049;
}

.cancel-btn {
  background-color: #f0f0f0;
  color: #333;
  border: none;
  padding: 6px 16px;
  border-radius: 4px;
  cursor: pointer;
}

.cancel-btn:hover {
  background-color: #e0e0e0;
}

/* ================= 滚动条美化（可选） ================= */
.modal-body::-webkit-scrollbar {
  width: 6px;
}

.modal-body::-webkit-scrollbar-thumb {
  background-color: rgba(0,0,0,0.2);
  border-radius: 3px;
}

/* ================= 知识管理 ================= */

.knowledge-manage-container {
  padding: 16px;
}

.knowledge-card {
  background: #f8f8f8;
  border-radius: 8px;
  padding: 14px;
  margin-bottom: 12px;
  border: 1px solid #ddd;
}

.knowledge-card:hover {
  background: #f2f7ff;
}

.bottom {
  display: flex;
  justify-content: space-between;
  margin-top: 10px;
}

.time {
  font-size: 12px;
  color: #777;
}

.delete-btn {
  background: #ff4d4f;
  color: white;
}

.new-knowledge-btn {
  text-align: center;
  margin-top: 16px;
}

.knowledge-form {
  background: #fff;
  padding: 16px;
  border-radius: 8px;
  border: 1px solid #ddd;
}

.form-group {
  margin-bottom: 12px;
}

.form-group input,
.form-group textarea {
  width: 100%;
  padding: 6px;
}

</style>