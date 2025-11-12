<template>
  <div class="main-bg">
    <HeaderComponent />

    <section class="content">
      <div style="display: flex; gap: 50rem">
        <div class="info">
          <p><label>用户名： </label>{{userName}}</p>
          <p><label>邮箱： </label>{{email}}</p>
          <p><label>身份： </label>{{role}}</p>
        </div>

        <div>
          <button @click="exit()">退出登录</button>
        </div>
      </div>

      <div v-if="role === 'buyer'" class="buyer-view-container">
        <nav class="buyer-nav">
          <button @click="switchView('address')" :class="{ active: currentView === 'address' }">我的地址</button>
          <button @click="switchView('addAddress')" :class="{ active: currentView === 'addAddress' }">新增地址</button>
        </nav>

        <div class="view-content-wrapper">
          <!-- 地址列表 -->
          <div v-if="currentView === 'address'">
            <div v-if="addresses.length" class="address-list">
              <div
                  v-for="addr in addresses"
                  :key="addr.addressId"
                  class="address-card"
              >
                <p><strong>收货人：</strong>{{ addr.recipientName }}</p>
                <p><strong>电话：</strong>{{ addr.phoneNumber }}</p>
                <p>
                  <strong>地址：</strong>
                  {{ addr.province }}{{ addr.city }}{{ addr.district }}{{ addr.streetAddress }}
                </p>
                <p><strong>邮编：</strong>{{ addr.postalCode }}</p>
                <p v-if="addr.isDefault">🌟 默认地址</p>

                <div class="card-actions">
                  <button class="set-default-btn" @click="setDefault(addr.addressId)">设为默认</button>
                  <button class="delete-btn" @click="deleteAddress(addr.addressId)">删除</button>
                </div>
              </div>
            </div>
            <p v-else class="empty-state">暂无地址，请添加新的地址。</p>
          </div>

          <!-- 新增地址 -->
          <div v-else-if="currentView === 'addAddress'">
            <div class="add-address-form">
              <div class="form-group">
                <label>收货人：</label>
                <input v-model="newAddress.recipientName" placeholder="请输入姓名" />
              </div>
              <div class="form-group">
                <label>电话：</label>
                <input v-model="newAddress.phoneNumber" placeholder="请输入手机号" />
              </div>
              <div class="form-group">
                <label>省份：</label>
                <input v-model="newAddress.province" placeholder="请输入省份" />
              </div>
              <div class="form-group">
                <label>城市：</label>
                <input v-model="newAddress.city" placeholder="请输入城市" />
              </div>
              <div class="form-group">
                <label>区县：</label>
                <input v-model="newAddress.district" placeholder="请输入区县" />
              </div>
              <div class="form-group">
                <label>街道详细地址：</label>
                <textarea v-model="newAddress.streetAddress" placeholder="请输入详细地址"></textarea>
              </div>
              <div class="form-group">
                <label>邮编：</label>
                <input v-model="newAddress.postalCode" placeholder="请输入邮政编码" />
              </div>
              <button class="save-btn" @click="addAddress">保存地址</button>
            </div>
          </div>
        </div>
      </div>

      <div v-if="role === 'farmer'" class="farmer-view-container">
        <nav class="farmer-nav">
          <button @click="switchView('address')" :class="{ active: currentView === 'address' }">我的地址</button>
          <button @click="switchView('addAddress')" :class="{ active: currentView === 'addAddress' }">新增地址</button>
        </nav>

        <div class="view-content-wrapper">
          <!-- 地址列表 -->
          <div v-if="currentView === 'address'">
            <div v-if="addresses.length" class="address-list">
              <div
                  v-for="addr in addresses"
                  :key="addr.addressId"
                  class="address-card"
              >
                <p><strong>发货人：</strong>{{ addr.recipientName }}</p>
                <p><strong>电话：</strong>{{ addr.phoneNumber }}</p>
                <p>
                  <strong>地址：</strong>
                  {{ addr.province }}{{ addr.city }}{{ addr.district }}{{ addr.streetAddress }}
                </p>
                <p><strong>邮编：</strong>{{ addr.postalCode }}</p>
                <p v-if="addr.isDefault">🌟 默认地址</p>

                <div class="card-actions">
                  <button class="set-default-btn" @click="setDefault(addr.addressId)">设为默认</button>
                  <button class="delete-btn" @click="deleteAddress(addr.addressId)">删除</button>
                </div>
              </div>
            </div>
            <p v-else class="empty-state">暂无地址，请添加新的地址。</p>
          </div>

          <!-- 新增地址 -->
          <div v-else-if="currentView === 'addAddress'">
            <div class="add-address-form">
              <div class="form-group">
                <label>发货人：</label>
                <input v-model="newAddress.recipientName" placeholder="请输入姓名" />
              </div>
              <div class="form-group">
                <label>电话：</label>
                <input v-model="newAddress.phoneNumber" placeholder="请输入手机号" />
              </div>
              <div class="form-group">
                <label>省份：</label>
                <input v-model="newAddress.province" placeholder="请输入省份" />
              </div>
              <div class="form-group">
                <label>城市：</label>
                <input v-model="newAddress.city" placeholder="请输入城市" />
              </div>
              <div class="form-group">
                <label>区县：</label>
                <input v-model="newAddress.district" placeholder="请输入区县" />
              </div>
              <div class="form-group">
                <label>街道详细地址：</label>
                <textarea v-model="newAddress.streetAddress" placeholder="请输入详细地址"></textarea>
              </div>
              <div class="form-group">
                <label>邮编：</label>
                <input v-model="newAddress.postalCode" placeholder="请输入邮政编码" />
              </div>
              <button class="save-btn" @click="addAddress">保存地址</button>
            </div>
          </div>
        </div>
      </div>

      <div v-if="role === 'expert'" class="expert-profile-container">
        <h3>我的个人档案</h3>

        <!-- 档案展示视图 -->
        <div v-if="!isEditing && expertProfile" class="profile-card">
          <div class="profile-details">
             <img :src="expertProfile.photoUrl || defaultAvatar" alt="Expert Photo" class="profile-photo">
            <div class="profile-info-text">
              <p><strong>专业领域：</strong>{{ expertProfile.specialization }}</p>
              <p><strong>咨询费：</strong>¥{{ expertProfile.consultationFee }} / 次</p>
              <p><strong>简介：</strong></p>
              <p class="bio">{{ expertProfile.bio }}</p>
            </div>
          </div>
          <div class="profile-actions">
            <button @click="enterEditMode">更新档案</button>
            <button @click="deleteProfile" class="delete-btn">删除档案</button>
          </div>
        </div>

        <!-- 创建/编辑表单视图 -->
        <div v-if="isEditing" class="profile-form">
          <h4>{{ expertProfile ? '更新' : '创建' }}您的专家档案</h4>
          <div class="form-group">
            <label>专业领域：</label>
            <input v-model="profileForm.specialization" placeholder="例如：水稻种植、病虫害防治" />
          </div>
          <div class="form-group">
            <label>咨询费 (元/次)：</label>
            <input type="number" v-model="profileForm.consultationFee" placeholder="例如：50" />
          </div>
          <div class="form-group">
            <label>简介：</label>
            <textarea v-model="profileForm.bio" placeholder="介绍您的专业背景和经验" rows="4"></textarea>
          </div>
          <div class="form-group">
            <label>更新照片：</label>
            <input type="file" @change="handleFileChange" accept="image/*" />
          </div>
          <div class="form-actions">
            <button @click="saveProfile" class="save-btn">保存</button>
            <button @click="cancelEdit">取消</button>
          </div>
        </div>

        <!-- 提示创建档案 -->
        <div v-if="!isEditing && !expertProfile" class="profile-prompt">
          <p>您还没有创建专家档案，这会影响农户找到您并向您咨询。</p>
          <button @click="enterEditMode">立即创建档案</button>
        </div>
      </div>



    </section>
  </div>

</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import axios from '../utils/axios'
import router from "@/router/index.js";
import HeaderComponent from "@/components/HeaderComponent.vue";
import defaultAvatar from '@/assets/default.jpg';

const token = localStorage.getItem('token')

const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || '{}'))
const userId = ref(userInfo.value?.userId || null)
const userName = ref(userInfo.value?.userName || '游客')
const role = ref(userInfo.value?.role || '未登录')
const email = ref(userInfo.value?.email || '')

const currentView = ref('address')
const addresses = ref([])

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

// === 专家档案相关状态 ===
const expertProfile = ref(null);
const isEditing = ref(false);
const profileForm = ref({
  specialization: '',
  bio: '',
  consultationFee: '',
});
const selectedFile = ref(null);

// 切换视图
function switchView(view) {
  currentView.value = view
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
  try {
    const res = await axios.post('/address/add', newAddress.value)
    alert('新增地址成功！')
    addresses.value.push(res.data)
    newAddress.value = {
      recipientName: '',
      phoneNumber: '',
      province: '',
      city: '',
      district: '',
      streetAddress: '',
      postalCode: ''
    }
    currentView.value = 'address'
  } catch (err) {
    alert('添加失败')
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

// === 专家档案方法 ===
async function fetchExpertProfile() {
  try {
    const res = await axios.get('/expert/profile'); // API: 获取当前专家档案
    
    // 首先判断响应体和 success 标志是否存在且为 true
    if (res.data && res.data.success) {
      // 正确：从响应的 data 属性中，取出里面的 data 对象（个人档案数据）
      expertProfile.value = res.data.data;
    } else {
      // 处理后端返回 success: false 的情况
      console.error('获取档案失败，服务器返回的业务状态为失败:', res.data.message || '未知错误');
      alert('获取档案数据失败。');
    }
    
  } catch (error) {
    if (error.response && error.response.status === 404) {
      expertProfile.value = null; // 档案不存在是正常情况
      console.log('当前专家还未创建档案。');
    } else {
      console.error('获取专家档案失败', error);
      alert('获取专家档案失败，请稍后重试。');
    }
  }
}

function enterEditMode() {
  // 如果已有档案，用现有数据填充表单；否则，用空数据
  profileForm.value = expertProfile.value
      ? { ...expertProfile.value }
      : { specialization: '', bio: '', consultationFee: '' };
  isEditing.value = true;
}

function cancelEdit() {
  isEditing.value = false;
  selectedFile.value = null; // 清除已选文件
}

function handleFileChange(event) {
  selectedFile.value = event.target.files[0];
}

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
        'Content-Type': 'multipart/form-data' // 文件上传必须的请求头
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

onMounted(async () => {
  if (!token) {
    // 如果没有token，直接退出，防止后续代码出错
    console.log("用户未登录，终止初始化。");
    return;
  }

  try {
      if (role.value === 'expert') {
        // 是专家，加载专家档案
        console.log("正在为专家加载个人档案...");
        await fetchExpertProfile();
      } else if (role.value === 'buyer' || role.value === 'farmer') {
        // 是买家或农户，加载地址信息
        console.log("正在为买家/农户加载地址...");
        await loadAddresses();
      } else {
        console.log(`未知的用户角色: ${role.value}，不执行额外加载操作。`);
      }
  } catch (err) {
    console.error('后续加载失败', err);
  }
});


watch(currentView, val => {
  if ((role.value === 'buyer' || role.value === 'farmer') && val === 'address') {
    loadAddresses();
  }
});

function exit(){
  localStorage.removeItem('token');
  localStorage.removeItem('userInfo');
  localStorage.removeItem('userId');
  router.push('/login');
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

.address-card {
  border: 1px solid #e9e9e9;
  border-radius: 8px;
  padding: 15px;
  margin-bottom: 10px;
  background: #fafafa;
}

.card-actions {
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
</style>
