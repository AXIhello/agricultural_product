<template>
  <div class="main-bg">
    <HeaderComponent />
    <!-- 顶部用户信息 + 退出 -->
    <div class="top-info-bar">
      <div class="info">
        <p><label>用户名：</label>{{ userInfo.userName }}</p>
        <p><label>昵称：</label>{{ userInfo.name }}</p>
        <p><label>邮箱：</label>{{ userInfo.email }}</p>
        <p><label>身份：</label>{{ role }}</p>
        <p><label>地区：</label>{{ userInfo.region }}</p>
      </div>
      <div class="user-actions">
        <button class="change-pass-btn" @click="openChangePassword">修改密码</button>
        <button class="edit-btn" @click="openEditProfile">编辑资料</button>
        <button class="exit-btn" @click="exit()">退出登录</button>
      </div>
    </div>
    <section
        :style="{ marginLeft: ['farmer','buyer','expert'].includes(role) ? '220px' : '20px',
                  width: ['farmer','buyer','expert'].includes(role) ? 'calc(100% - 240px)' : 'calc(100% - 40px)'}"
        class="content"
        v-if="role !== 'bank' && role !== 'admin'"
      >
      <!-- 统一顶部导航 -->
      <nav v-if="role === 'farmer' || role === 'buyer' || role === 'expert' "
           :style="{ top: '65px'}"
           class="main-nav">
          <button v-if="role === 'farmer' || role === 'buyer'" @click="switchView('address')" :class="{ active: currentView === 'address' }">我的地址</button>
          <button v-if="role === 'farmer'" @click="switchView('appointments')" :class="{ active: currentView === 'appointments' }">我的预约</button>
          <button v-if="role === 'farmer' || role === 'buyer'" @click="switchView('message')" :class="{ active: currentView === 'message' }">我的消息</button>
          <button v-if="role === 'farmer'" @click="switchView('autoReply')" :class="{ active: currentView === 'autoReply' }">自动回复设置</button>
          <button v-if="role === 'expert'" @click="switchView('profile')" :class="{ active: currentView === 'profile' }">个人档案</button>
          <button v-if="role === 'expert'" @click="switchView('knowledgeManage')" :class="{ active: currentView === 'knowledgeManage' }">知识管理</button>
          <button v-if="role === 'expert'" @click="switchView('availability')" :class="{ active: currentView === 'availability' }">可预约时间</button>
          <button v-if="role === 'expert'" @click="switchView('schedule')" :class="{ active: currentView === 'schedule' }">我的日程</button>
      </nav>

      <!-- 内容区域 -->
      <div class="view-content-wrapper">

        <!--编辑个人资料-->
        <div v-if="showEditProfileModal" class="modal-overlay">
          <div class="modal-container">
            <!-- 右上角关闭按钮 -->
            <button class="close-btn" @click="closeEditProfile">×</button>

            <h2 class="modal-title">编辑个人资料</h2>

            <div class="modal-body">
              <!-- 昵称 -->
              <div class="modal-form-group row-layout">
                <label>昵称：</label>
                <input v-model="editProfileForm.name" type="text" placeholder="请输入新昵称" />
              </div>

              <!-- 地区 - 省份 -->
              <div class="modal-form-group row-layout">
                <label>省份：</label>
                <input v-model="editProfileForm.province" type="text" placeholder="例如：广东省" />
              </div>

              <!-- 地区 - 城市 -->
              <div class="modal-form-group row-layout">
                <label>城市：</label>
                <input v-model="editProfileForm.city" type="text" placeholder="例如：广州市" />
              </div>
            </div>

            <!-- 底部按钮 -->
            <div class="modal-footer">
              <button class="cancel-btn" @click="closeEditProfile">取消</button>
              <button class="save-btn" @click="saveUserProfile">保存修改</button>
            </div>
          </div>
        </div>

        <!-- 修改密码弹窗 -->
        <div v-if="showChangePass" class="modal-overlay">
          <div class="modal-container">

            <!-- 标题 -->
            <h3 class="modal-title">修改密码</h3>

            <!-- Body 可滚动 -->
            <div class="modal-body">

              <div class="modal-form-group">
                <label>新密码</label>
                <input v-model="newPassword" type="password" placeholder="请输入新密码" />
              </div>

              <div class="modal-form-group">
                <label>确认密码</label>
                <input v-model="confirmPassword" type="password" placeholder="再次输入新密码" />
              </div>

              <p v-if="errMsg" class="error">{{ errMsg }}</p>
            </div>

            <!-- 底部按钮 -->
            <div class="modal-footer">
              <button class="modal-btn confirm" @click="submitChangePassword">确定</button>
              <button class="modal-btn cancel" @click="showChangePass = false">取消</button>
            </div>

          </div>
        </div>


        <!-- ======================== 买家/农户：我的地址 ======================== -->
        <div v-if="currentView === 'address'" class="address-view">

          <!-- 地址列表表头 -->
          <div class="table-header">
            <div class="col default-col">默认</div>
            <div class="col name-col">{{ role === 'buyer' ? '收货人' : '发货人' }}</div>
            <div class="col phone-col">电话</div>
            <div class="col address-col">地址</div>
            <div class="col postal-col">邮编</div>
            <div class="col action-col">操作</div>
          </div>

          <!-- 地址列表 -->
          <div class="table">
            <div class="table-row" v-for="addr in addresses" :key="addr.addressId">
              <div class="col default-col">
                <span v-if="addr.isDefault">🌟</span>
              </div>
              <div class="col name-col">{{ addr.recipientName }}</div>
              <div class="col phone-col">{{ addr.phoneNumber }}</div>
              <div class="col address-col">{{ addr.province }}{{ addr.city }}{{ addr.district }}{{ addr.streetAddress }}</div>
              <div class="col postal-col">{{ addr.postalCode }}</div>
              <div class="col action-col">
                <button class="set-default-btn" @click="setDefault(addr.addressId)">设为默认</button>
                <button class="delete-btn" @click="deleteAddress(addr.addressId)">删除</button>
              </div>
            </div>

            <p v-if="!addresses.length" class="empty-state">暂无地址，请添加新的地址。</p>
          </div>

          <!-- 新增地址按钮 -->
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

              <div class="action-buttons">
                <button v-if="appt.status === 'scheduled'" class="delete-btn" @click="cancelAppointment(appt.id)">
                  取消预约
                </button>
              </div>

            </div>
          </div>

          <p v-else class="empty-state">暂无预约记录。</p>
        </div>

        <!-- ======================== 农户：自动回复设置 ======================== -->
        <div v-if="currentView === 'autoReply'" class="auto-reply-view">

          <!-- 规则列表表头 -->
          <div class="table-header">
            <div class="col enabled-col">启用</div>
            <div class="col keyword-col">关键词</div>
            <div class="col match-col">匹配方式</div>
            <div class="col reply-col">回复内容</div>
            <div class="col priority-col">优先级</div>
            <div class="col action-col">操作</div>
          </div>

          <!-- 规则列表 -->
          <div class="table">
            <div class="table-row" v-for="rule in rules" :key="rule.ruleId">
              <div class="col enabled-col">
                <input type="checkbox" :checked="rule.enabled" @change="toggleRule(rule.ruleId, !rule.enabled)" />
              </div>
              <div class="col keyword-col">{{ rule.keyword }}</div>
              <div class="col match-col">
                {{ matchTypeLabel(rule.matchType) }}
              </div>
              <div class="col reply-col">{{ rule.replyText }}</div>
              <div class="col priority-col">{{ rule.priority }}</div>


              <div class="col action-col">
                <button class="rule-edit-btn" @click="openEdit(rule)">编辑</button>
                <button class="rule-delete-btn" @click="deleteRule(rule.ruleId)">删除</button>
              </div>
            </div>


            <p v-if="!rules.length" class="empty-state">暂无自动回复规则，请添加新的规则。</p>
          </div>

          <!-- 新增规则按钮 -->
          <button class="add-btn" @click="openAdd">＋ 新增规则</button>

          <!-- ================= 弹窗（新增/编辑） ================= -->
          <div v-if="showPopup" class="modal-overlay">
            <div class="modal-container">


              <button class="close-btn" @click="closePopup">×</button>


              <h2 class="modal-title">{{ editingRule ? '编辑规则' : '新增规则' }}</h2>


              <div class="modal-body">


                <!-- 关键词 -->
                <div class="modal-form-group row-layout">
                  <label>关键词：</label>
                  <input v-model="form.keyword" type="text" placeholder="请输入关键词" />
                </div>


                <!-- 匹配方式 -->
                <div class="modal-form-group row-layout">
                  <label>匹配方式：</label>
                  <select v-model="form.matchType">
                    <option value="contains">包含匹配</option>
                    <option value="exact">完全匹配</option>
                    <option value="regex">正则匹配</option>
                  </select>
                </div>


                <!-- 回复文本 -->
                <div class="modal-form-group">
                  <label>回复内容：</label>
                  <textarea v-model="form.replyText" rows="2" placeholder="请输入自动回复内容"></textarea>
                </div>


<!--                &lt;!&ndash; 启用开关 &ndash;&gt;-->
<!--                <div class="modal-form-group row-layout">-->
<!--                  <label>启用：</label>-->
<!--                  <input type="checkbox" v-model="form.enabled" />-->
<!--                </div>-->


                <!-- 优先级 -->
                <div class="modal-form-group row-layout">
                  <label>优先级：</label>
                  <input type="number" v-model="form.priority" placeholder="数字越大优先级越高" />
                </div>
              </div>


              <div class="modal-footer">
                <button class="cancel-btn" @click="closePopup">取消</button>
                <button class="save-btn" @click="saveRule">保存</button>
              </div>


            </div>
          </div>
        </div>

        <!-- ======================== 买家/农户：我的消息 ======================== -->
        <div v-if="currentView === 'message'">

          <div v-if="isLoading" class="status-indicator">
            <p>正在加载消息...</p>
          </div>
          <div v-else-if="sessions.length === 0" class="status-indicator">
            <p>您还没有任何消息</p>
          </div>
          <ul v-else class="session-list">
            <!-- 循环渲染会话列表 -->
            <li v-for="session in sessions" :key="session.sessionId" class="session-item" @click="goToChat(session)">
              <div class="avatar-placeholder">
                <!-- 可以放一个用户头像图标或图片 -->
                <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path><circle cx="12" cy="7" r="4"></circle></svg>
              </div>
              <div class="session-details">
                <div class="session-header">
                  <span class="peer-user-id">用户 {{ getPeerUser(session).id }}</span>
                  <span class="last-message-time">{{ formatTime(session.lastMessageTime) }}</span>
                </div>
                <div class="last-message-preview">
                  <!-- 这里可以未来扩展，显示最后一条消息的预览 -->
                  点击查看对话
                </div>
              </div>
            </li>
          </ul>
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
import {useRouter} from "vue-router";

const authStore = useAuthStore();// 使用 Pinia 的认证存储
const router = useRouter();

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

// === 编辑个人资料相关状态 ===
const showEditProfileModal = ref(false)
const editProfileForm = ref({
  name: '',
  province: '',
  city: ''
})

// 打开编辑资料弹窗
function openEditProfile() {
  // 回显当前数据
  editProfileForm.value.name = userInfo.value.name || '';
  
  // 尝试解析当前的 region 字段 (假设格式为 "省份 城市" 或 "省份城市")
  // 如果后端存的是简单字符串，这里做简单分割，或者让用户重新填
  const region = userInfo.value.region || '';
  // 这里做个简单的处理，实际可能需要更复杂的解析或让用户自己填
  editProfileForm.value.province = ''; 
  editProfileForm.value.city = ''; 
  
  showEditProfileModal.value = true;
}

// 关闭编辑资料弹窗
function closeEditProfile() {
  showEditProfileModal.value = false;
}

// 保存个人资料
async function saveUserProfile() {
  if (!editProfileForm.value.name) {
    alert("昵称不能为空");
    return;
  }

  // 拼接地区字段
  const regionStr = `${editProfileForm.value.province || ''}${editProfileForm.value.city || ''}`;

  try {
    // 假设后端有一个更新用户信息的接口 /user/update
    // 你需要确认后端是否有这个接口，如果没有，需要后端加一个
    await axios.post('/user/update/profile', {
      userId: userInfo.value.userId, // 传 ID 确保后端知道改谁
      name: editProfileForm.value.name,
      region: regionStr
    });

    alert("资料修改成功！");
    
    // 更新本地 Store 中的用户信息，以便页面即时刷新
    // authStore.setUserInfo 是假设你 Store 里有这个 update 方法，或者直接改 userInfo
    userInfo.value.name = editProfileForm.value.name;
    userInfo.value.region = regionStr;
    
    closeEditProfile();
  } catch (err) {
    console.error("修改资料失败", err);
    alert("修改失败，请稍后重试");
  }
}
// 修改密码弹窗相关
const showChangePass = ref(false);
const newPassword = ref("");
const confirmPassword = ref("");
const errMsg = ref("");

// 打开修改密码弹窗
const openChangePassword = () => {
  newPassword.value = "";
  confirmPassword.value = "";
  errMsg.value = "";
  showChangePass.value = true;
};

// 提交密码修改
const submitChangePassword = async () => {
  errMsg.value = "";

  if (!newPassword.value || !confirmPassword.value) {
    errMsg.value = "密码不能为空";
    return;
  }
  if (newPassword.value.length < 6) {
    errMsg.value = "密码至少 6 位";
    return;
  }
  if (newPassword.value !== confirmPassword.value) {
    errMsg.value = "两次输入不一致";
    return;
  }

  try {
    const res = await axios.post("/user/change-password", {
      newPassword: newPassword.value
    });

    if (res.data.success) {
      alert("密码修改成功，请重新登录！");
      authStore.logout(); // 清理 token
      router.push("/login");
    } else {
      errMsg.value = res.data.message || "修改失败";
    }
  } catch (e) {
    errMsg.value = "服务器错误，请稍后再试";
  }
};

// ======自动回复=====
const rules = ref([])
const showPopup = ref(false)
const editingRule = ref(null)


const form = ref({
  ruleId: null,
  keyword: '',
  matchType: 'contains',
  replyText: '',
  enabled: true,
  priority: 1,
})


// 获取我的规则
async function loadRules() {
  const res = await axios.get('/chat/auto-replies')
  rules.value = res.data
}


function matchTypeLabel(type) {
  return {
    contains: '包含',
    exact: '完全匹配',
    regex: '正则',
  }[type] || type
}


// 打开新增
function openAdd() {
  editingRule.value = null
  form.value = {
    keyword: '',
    matchType: 'contains',
    replyText: '',
    enabled: true,
    priority: 1,
  }
  showPopup.value = true
}


// 打开编辑
function openEdit(rule) {
  editingRule.value = rule
  form.value = { ...rule }
  showPopup.value = true
}


// 保存规则
async function saveRule() {
  await axios.post('/chat/auto-replies', form.value)
  showPopup.value = false
  loadRules()
}


// 删除规则
async function deleteRule(id) {
  await axios.delete(`/chat/auto-replies/${id}`)
  loadRules()
}


// 切换启用状态
async function toggleRule(id, enabled) {
  await axios.put(`/chat/auto-replies/${id}/toggle?enabled=${enabled}`)
  loadRules()
}


function closePopup() {
  showPopup.value = false
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
    console.log('后端原始返回：', res.data);

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
    await fetchExpertKnowledge()
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
  loadSessions();
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
      await loadRules();
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

function editProfile(){

}


const sessions = ref([]);
const isLoading = ref(true);

// --- Methods ---

/**
 * 从后端加载用户的会话列表.
 */
async function loadSessions() {
  try {
    // API: GET /chat/auto-replies/chat/sessions
    const response = await axios.get('/chat/sessions');
    // 按最后消息时间降序排序
    sessions.value = (response.data || []).sort((a, b) =>
        new Date(b.lastMessageTime) - new Date(a.lastMessageTime)
    );
  } catch (error) {
    console.error('加载会话列表失败:', error);
    alert('无法加载消息列表，请稍后再试。');
  } finally {
    isLoading.value = false;
  }
}

/**
 * 确定会话中的对方用户ID.
 * @param {object} session - 会话对象.
 * @returns {object} 包含对方用户ID的对象.
 */
function getPeerUser(session) {
  if (!userInfo.value?.userId) return { id: '未知' };
  const peerId = session.userAId === userInfo.value.userId ? session.userBId : session.userAId;
  return { id: peerId };
}

/**
 * 导航到对应的聊天室.
 * @param {object} session - 被点击的会话对象.
 */
function goToChat(session) {
  const peer = getPeerUser(session);
  // 添加对自己ID的判断
  if (peer.id !== '未知' && peer.id !== userInfo.value?.userId) {
    router.push(`/chat/${peer.id}`);
  } else if (peer.id === userInfo.value?.userId) {
    console.warn("Attempted to open a chat with self. Operation blocked.");
    alert('您不能和自己聊天。');
  }
}

/**
 * 格式化时间字符串.
 * @param {string} dateTimeStr - ISO格式的时间字符串.
 */
function formatTime(dateTimeStr) {
  if (!dateTimeStr) return '';
  const date = new Date(dateTimeStr);
  const now = new Date();
  const diffInMs = now - date;
  const diffInHours = diffInMs / (1000 * 60 * 60);

  if (diffInHours < 24 && date.getDate() === now.getDate()) {
    return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
  } else {
    return date.toLocaleDateString();
  }
}


</script>

<style scoped>

.top-info-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 20px;
  background-color: #f9f9f9;   /* 浅背景色，和整体页面统一 */
  border-radius: 10px;          /* 圆角 */
  box-shadow: 0 2px 6px rgba(0,0,0,0.08); /* 微阴影增加高级感 */
  border-bottom: 1px solid #e0e0e0; /* 下方分割线 */
  margin-left: 220px; 
  width: calc(100% - 240px);
}

.top-info-bar .info p {
  margin: 4px 0;
  font-size: 14px;
  color: #333;
  display: flex;
  gap: 5px;
}

.top-info-bar .info label {
  font-weight: 600;
  color: #2D7D4F; /* 深绿色主题色 */
  min-width: 60px; /* 标签统一宽度 */
}

.top-info-bar button {
  background-color: #2D7D4F;
  color: #fff;
  border: none;
  padding: 6px 14px;
  border-radius: 6px;
  font-size: 14px;
  cursor: pointer;
  transition: 0.3s;
}

.top-info-bar button:hover {
  background-color: #246a3d; /* 悬停深色 */
}


.set-default-btn, .delete-btn {
  padding: 4px 10px;
  border-radius: 6px;
  border: none;
  cursor: pointer;
  transition: 0.3s;
}

.set-default-btn {
  background-color: #2D7D4F;
  color: white;
}

.set-default-btn:hover {
  background-color: #246a3d;
}

.delete-btn {
  background-color: #e74c3c;
  color: white;
  margin-left: 6px;
}

.delete-btn:hover {
  background-color: #c0392b;
}

.add-btn {
  margin-top: 12px;
  background-color: #2D7D4F;
  color: white;
  border: none;
  border-radius: 6px;
  padding: 6px 14px;
  cursor: pointer;
  transition: 0.3s;
}

.add-btn:hover {
  background-color: #246a3d;
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

.appointment-card .action-buttons {
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

/*消息中心*/
.session-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.session-item {
  display: flex;
  align-items: center;
  padding: 1rem 0.5rem;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: background-color 0.2s ease-in-out;
}

.session-item:last-child {
  border-bottom: none;
}

.session-item:hover {
  background-color: #f7f9fa;
}

.avatar-placeholder {
  flex-shrink: 0;
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background-color: #e9ecef;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 1rem;
  color: #495057;
}

.session-details {
  flex-grow: 1;
  display: flex;
  flex-direction: column;
}

.session-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.25rem;
}

.peer-user-id {
  font-weight: 600;
  color: #212529;
}

.last-message-time {
  font-size: 0.8rem;
  color: #888;
}

.last-message-preview {
  font-size: 0.9rem;
  color: #6c757d;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.status-indicator {
  text-align: center;
  padding: 3rem 0;
  color: #888;
}

/* 修改 .top-info-bar 相关的样式 */

.top-info-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  background-color: #fff;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.05);
  margin-bottom: 20px;
}

/* 右侧操作区 */
.user-actions {
  display: flex;
  gap: 12px; /* 按钮之间的间距 */
}

/* 编辑按钮样式 */
.edit-btn {
  background-color: #3498db; /* 蓝色 */
  color: #fff;
  border: none;
  padding: 8px 16px;
  border-radius: 6px;
  cursor: pointer;
  transition: 0.3s;
}
.edit-btn:hover {
  background-color: #2980b9;
}

/* 退出按钮样式 */
.exit-btn {
  background-color: #e74c3c; /* 红色 */
  color: #fff;
  border: none;
  padding: 8px 16px;
  border-radius: 6px;
  cursor: pointer;
  transition: 0.3s;
}
.exit-btn:hover {
  background-color: #c0392b;
}

/* 按钮通用样式 */
.rule-edit-btn,
.rule-delete-btn {
  color: #fff;
  border: none;
  padding: 8px 16px;
  border-radius: 6px;
  cursor: pointer;
  transition: 0.2s;
  font-size: 14px;
}

/* 编辑按钮（绿色） */
.rule-edit-btn {
  background-color: #2D7D4F;   /* 清爽绿色 */
}
.rule-edit-btn:hover {
  background-color: #246a3d;   /* 深一点，更稳重 */
}

/* 删除按钮（柔和红色） */
.rule-delete-btn {
  background-color: #e57373;   /* 柔和红色，比纯红更好看 */
}
.rule-delete-btn:hover {
  background-color: #d32f2f;   /* 稍深一点 */
}

/* 弹窗通用样式复用之前的即可，确保 .modal-overlay, .modal-container 等类名存在 */

</style>