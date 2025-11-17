<template>
  <div class="main-bg">
    <HeaderComponent />
    <section class="content">
      <div v-if="isLoadingProfile">正在加载专家信息...</div>
      <div v-else-if="!expertProfile">未找到该专家的信息。</div>
      
      <!-- 专家档案展示 -->
      <div v-else class="expert-detail-container">
        <div class="profile-card">
          <h3>专家档案</h3>
          <div class="profile-details">
            <img :src="expertProfile.photoUrl || defaultAvatar" alt="Expert Photo" class="profile-photo">
            <div class="profile-info-text">
              <h4>{{ expertName || '专家姓名' }}</h4>
              <p><strong>专业领域：</strong>{{ expertProfile.specialization }}</p>
              <p><strong>咨询费：</strong>¥{{ expertProfile.consultationFee }} / 次</p>
              <p><strong>简介：</strong></p>
              <p class="bio">{{ expertProfile.bio }}</p>
            </div>
          </div>
        </div>

        <!-- 可预约时间段列表 -->
        <div class="slots-card">
          <h3>可预约时间</h3>
          <div v-if="isLoadingSlots">正在加载排期...</div>
          <div v-else-if="availableSlots.length === 0" class="empty-state">该专家暂无开放预约的时间段。</div>
          <table v-else class="slots-table">
            <thead>
              <tr>
                <th>日期</th>
                <th>时间</th>
                <th>剩余名额</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="slot in availableSlots" :key="slot.slotId">
                <td>{{ slot.workDate }}</td>
                <td>{{ slot.startTime }} - {{ slot.endTime }}</td>
                <td>{{ slot.capacity - slot.bookedCount }}</td>
                <td>
                  <button 
                    @click="bookAppointment(slot.slotId)"
                    :disabled="(slot.capacity - slot.bookedCount) <= 0 || isBooking"
                    class="book-btn"
                  >
                    {{ (slot.capacity - slot.bookedCount) > 0 ? '预约' : '已满' }}
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
           <!-- 分页 -->
          <div class="pagination" v-if="pagination.pages > 1">
              <button @click="changePage(pagination.current - 1)" :disabled="pagination.current <= 1">上一页</button>
              <span>第 {{ pagination.current }} / {{ pagination.pages }} 页</span>
              <button @click="changePage(pagination.current + 1)" :disabled="pagination.current >= pagination.pages">下一页</button>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue';
import { useRoute } from 'vue-router';
import axios from '../utils/axios';
import HeaderComponent from '@/components/HeaderComponent.vue';
import defaultAvatar from '@/assets/default.jpg';
import { ElMessage } from 'element-plus';
import { useAuthStore } from '@/stores/authStore';

const route = useRoute();
const authStore = useAuthStore();

const expertProfile = ref(null);
const availableSlots = ref([]);
const isLoadingProfile = ref(true);
const isLoadingSlots = ref(true);
const isBooking = ref(false);

const expertId = route.params.id; // 从URL中获取专家ID
const expertName = ref('')

console.log('ExpertDetailPage.vue 已加载，从URL获取到的专家ID是:', expertId);
console.log('完整的路由参数对象:', route.params);

const pagination = reactive({
  current: 1,
  pageSize: 5, // 每页显示5个时间段
  total: 0,
  pages: 1,
});

async function fetchExpertProfile() {

  console.log('正在调用fetchExpertProfile');
  isLoadingProfile.value = true;
  try {
    // API: 根据ID获取指定专家的档案
    // 注意: 这个API需要后端实现，根据ID返回单个专家的档案和用户信息
    const response = await axios.get(`/expert/profile/${expertId}`);
    console.log('fetchExpertProfile 获取到的响应数据:', response.data);
    if (response.data && response.data.success ) {
      expertProfile.value = response.data.data;
    }
  } catch (error) {
    console.error(`获取专家(ID: ${expertId})档案失败:`, error);
    expertProfile.value = null;
  } finally {
    isLoadingProfile.value = false;
  }
}

async function loadExpertName() {
  try {
    const response = await axios.get(`/expert/profile/list`);
    if (response.data && response.data.success) {
      const list = response.data.data;
      const item = list.find(obj => obj.id === String(expertId));
      expertName.value = item ? item.name : '专家'
    } else {
      expertName.value = '专家'
    }
  } catch (error) {
    console.error(`获取专家(ID: ${expertId})姓名失败:`, error);
    expertName.value = '专家'
  }
} 

async function fetchAvailableSlots() {
  isLoadingSlots.value = true;
  try {
    const params = {
      expertId: expertId,
      pageNum: pagination.current,
      pageSize: pagination.pageSize,
    };
    // API: 获取指定专家开放的时间段
    const response = await axios.get('/expert-appointments/slots/open', { params });
    const data = response.data;
    availableSlots.value = data.records || [];
    pagination.total = data.total;
    pagination.pages = data.pages;
    pagination.current = data.current;
  } catch (error) {
    console.error(`获取专家(ID: ${expertId})可用时间失败:`, error);
  } finally {
    isLoadingSlots.value = false;
  }
}

async function bookAppointment(slotId) {
  if (!authStore.isLoggedIn) {
      ElMessage.warning('请先登录再进行预约！');
      // 可以跳转到登录页
      // router.push('/login');
      return;
  }
  
  isBooking.value = true;
  try {
    const response = await axios.post(`/expert-appointments/book?slotId=${slotId}`);
    if (response.data && response.data.success) {
      ElMessage.success(`预约成功！预约ID为: ${response.data.consultationId}`);
      // 预约成功后，刷新时间段列表，更新剩余名额
      await fetchAvailableSlots();
    } else {
      ElMessage.error(response.data.message || '预约失败，该时段可能已被约满。');
    }
  } catch (error) {
    console.error('预约失败:', error);
    ElMessage.error(error.response?.data?.message || '预约时发生错误，请稍后重试。');
  } finally {
    isBooking.value = false;
  }
}

function changePage(page) {
  if (page > 0 && page <= pagination.pages) {
    pagination.current = page;
    fetchAvailableSlots();
  }
}

onMounted(() => {
  console.log('组件已挂载 (onMounted)，准备根据 expertId 获取数据...');
  if (expertId) {
    console.log('expertId 有效，即将调用 fetchExpertProfile 和 fetchAvailableSlots');
    fetchExpertProfile();
    fetchAvailableSlots();
    loadExpertName();

  } else {
    // ✅ 添加一个 else 分支，方便调试
    console.error('在 onMounted 中检测到 expertId 无效或为空，因此未发送API请求！');
  }
});
</script>

<style scoped>

.main-bg { 
    display: flex; 
    flex-direction: column; 
    height: 100vh; 
    background-color: #F0F9F4; 
}

.content { 
    flex: 1; 
    padding: 20px; 
    background: white; 
}

.expert-detail-container { 
    max-width: 900px; 
    margin: auto; 
}

/* 档案卡片样式 */
.profile-card, .slots-card { 
    margin-top: 2rem; 
    padding: 1.5rem; 
    background-color: #fff; 
    border: 1px solid #e0e0e0; 
    border-radius: 8px; 
}

.profile-card h3, .slots-card h3 { 
    margin-top: 0; 
    color: #2D7D4F; 
    border-bottom: 2px solid #F0F9F4; 
    padding-bottom: 0.5rem; 
    margin-bottom: 1.5rem; 
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
}

.profile-info-text h4 { 
    margin-top: 0; 
    font-size: 1.5rem; 
}

.profile-info-text p { 
    margin: 0.5rem 0; 
    line-height: 1.6; 
}

.bio { 
    white-space: pre-wrap; 
}

/* 时间段表格样式 */
.slots-table { 
    width: 100%; 
    border-collapse: collapse; 
    margin-top: 1rem; 
}

.slots-table th, .slots-table td { 
    padding: 0.8rem; 
    text-align: left; 
    border-bottom: 1px solid #dee2e6; 
}

.slots-table th { 
    background-color: #f8f9fa; 
    font-weight: 600; 
}

.book-btn { 
    padding: 6px 12px; 
    border-radius: 5px; 
    border: none; 
    background-color: #28a745; 
    color: white; 
    cursor: pointer; 
}

.book-btn:hover { 
    background-color: #218838; 
}

.book-btn:disabled { 
    background-color: #6c757d; 
    cursor: not-allowed; 
}

.empty-state { 
    text-align: center; 
    color: #888; 
    padding: 2rem; 
}

/* 分页样式 */
.pagination { 
    display: flex; 
    justify-content: center; 
    align-items: center; 
    gap: 1rem; 
    margin-top: 1.5rem; 
}

.pagination button { 
    padding: 0.5rem 1rem; 
    border: 1px solid #ddd; 
    background-color: #fff; 
    cursor: pointer; 
}

.pagination button:disabled { 
    color: #ccc; 
    cursor: not-allowed; 
    }
</style>