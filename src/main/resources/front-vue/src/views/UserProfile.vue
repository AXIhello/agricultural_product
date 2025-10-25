<template>
  <div class="main-bg">
    <header class="header">
      <h1>农产品交易平台</h1>
      <nav>
        <ul>
          <li><router-link to="/main">首页</router-link></li>
          <li><router-link to="/finance">融资服务</router-link></li>
          <li><router-link to="/expert">专家助力</router-link></li>
          <li><router-link to="/trading">农产品交易</router-link></li>
          <li><router-link to="/profile" style="color: #B7E4C7;">个人信息</router-link></li>
        </ul>
      </nav>
    </header>

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

    </section>
  </div>

</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import axios from '../utils/axios'
import router from "@/router/index.js";

const token = localStorage.getItem('token')

const userInfo = ref({})
const userId = ref('')
const userName = ref('未登录')
const role = ref('游客')
const email = ref('')

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

onMounted(async () => {
  loadAddresses()
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
      email.value = res.data.user.email
      localStorage.setItem('userInfo', JSON.stringify(res.data.user))
    } else {
      console.warn('Token 无效或过期')
    }
  } catch (err) {
    console.error('获取用户信息失败', err)
  }
})

watch(currentView, val => {
  if (val === 'address') loadAddresses()
})

function exit(){
  // 清除本地存储的用户信息
  localStorage.removeItem('token')
  localStorage.removeItem('userInfo')
  localStorage.removeItem('userId')

  // 跳转到登录页
  router.push('/login')
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
</style>
