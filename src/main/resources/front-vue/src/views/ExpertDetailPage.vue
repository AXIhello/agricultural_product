<template>
  <div class="main-bg">
    <section class="content">
      <button @click="goBack" class="back-button">
        <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <line x1="19" y1="12" x2="5" y2="12"></line>
          <polyline points="12 19 5 12 12 5"></polyline>
        </svg>
        返回
      </button>
      <div v-if="isLoadingProfile">正在加载专家信息...</div>
      <div v-else-if="!expertProfile">未找到该专家的信息。</div>
      
      <!-- 专家档案展示 -->
      <div v-else class="expert-detail-container">
        <div class="profile-card">
          <h3>专家档案</h3>
          <div class="profile-details">
            <img :src="expertProfile.photoUrl" alt="Expert Photo" class="profile-photo">
            <div class="profile-info-text">
              <h4>{{ expertName || '专家姓名' }}</h4>
              <p><strong>专业领域：</strong>{{ expertProfile.specialization }}</p>
              <p><strong>咨询费：</strong>¥{{ expertProfile.consultationFee }} / 次</p>
              <p><strong>简介：</strong></p>
              <p class="bio">{{ expertProfile.bio }}</p>
            </div>
            <button
                v-if="authStore.isLoggedIn &&
                      authStore.userInfo?.userId !== expertId && 
                      ((authStore.userInfo?.role === 'farmer') || (authStore.userInfo?.role === 'buyer'))"
                @click="goToChatWithExpert"
                class="consult-btn"
              >
                咨询专家
              </button>
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
import { ref, onMounted, reactive,computed} from 'vue';
import { useRoute, useRouter} from 'vue-router';
import axios from '../utils/axios';
import HeaderComponent from '@/components/HeaderComponent.vue';
import defaultAvatar from '@/assets/default.jpg';
import { ElMessage } from 'element-plus';
import { useAuthStore } from '@/stores/authStore';

const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();

const expertProfile = ref(null);
const availableSlots = ref([]);
const isLoadingProfile = ref(true);
const isLoadingSlots = ref(true);
const isBooking = ref(false);

const expertId = route.params.id; // 从URL中获取专家ID
const expertName = ref('')

const pagination = reactive({
  current: 1,
  pageSize: 5, // 每页显示5个时间段
  total: 0,
  pages: 1,
});

//===========专家个人信息加载===========
//获取专家档案
// 获取专家档案（新版：profile + photo）
async function fetchExpertProfile() {
  isLoadingProfile.value = true;
  try {
    const res = await axios.get(`/expert/profile/${expertId}`);
    const profile = res.data?.data || null;

    console.log('后端原始返回 profile：', res.data);

    if (!profile) {
      expertProfile.value = null;
      console.log('当前专家还未创建档案。');
      return;
    }

    // 尝试加载头像
    try {
      const imageRes = await axios.get(
          `/expert/profile/${profile.expertId}/photo`,
          { responseType: 'blob' }
      );

      if (imageRes.data && imageRes.data.size > 0) {
        profile.photoUrl = URL.createObjectURL(imageRes.data);
      } else {
        throw new Error('空图片');
      }
    } catch (err) {
      console.warn('专家头像加载失败，使用默认头像');
      profile.photoUrl = defaultAvatar;
    }

    expertProfile.value = profile;

  } catch (error) {
    console.error('获取专家档案失败:', error);
    alert('获取专家档案失败，请稍后重试。');
    expertProfile.value = null;
  } finally {
    isLoadingProfile.value = false;
  }
}


//加载专家名字
async function loadExpertName() {
  try {
    const response = await axios.get(`/expert/profile/list`);
    console.log('loadExpertName (专家列表) 返回的完整数据:', response.data);
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

//===========预约相关===========
//获取可预约时间段
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

//预约时间段
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
    
    // 成功逻辑
    if (response.data && response.data.success) {
      ElMessage.success(`预约成功！`);
      await fetchAvailableSlots(); // 刷新列表
    } 
    // 后端返回 200 但 success 为 false（如果你的后端架构是这样的）
    else {
      // 显示后端传回来的具体错误信息
      const msg = response.data.message || '预约失败，该时段可能已被约满。';
      alert(msg); // 或者 ElMessage.error(msg);
    }
  } catch (error) {
    // 捕获 400 错误 (BusinessException 抛出的)
    console.error('预约请求发生异常:', error);
    
    // 获取后端返回的 message
    const errorMsg = error.response?.data?.message || '预约时发生错误，请稍后重试。';
    
    // 弹窗提示用户
    alert("预约失败：" + errorMsg); // 建议用 alert 或 ElMessage.error
  } finally {
    isBooking.value = false;
  }
}

//===========咨询专家功能===========
function goToChatWithExpert() {
  if (!authStore.isLoggedIn) {
    ElMessage.warning('请先登录才能咨询专家！');
    router.push('/login'); // 跳转到登录页
    return;
  }
  if (authStore.userInfo?.userId === expertId) {
    ElMessage.info('您不能和自己聊天哦！');
    return;
  }
  
  router.push({ name: 'Chat', params: { receiverId: expertId } });
}

//分页切换
function changePage(page) {
  if (page > 0 && page <= pagination.pages) {
    pagination.current = page;
    fetchAvailableSlots();
  }
}

// 返回上一页
function goBack() {
  router.back();
}

onMounted(() => {
  console.log('组件已挂载 (onMounted)，准备根据 expertId 获取数据...');
  console.log('组件已挂载，expertId:', expertId);
  if (expertId) {
    console.log('expertId 有效，即将调用 fetchExpertProfile 和 fetchAvailableSlots');
    fetchExpertProfile();
    fetchAvailableSlots();
    loadExpertName();

  } else {
    console.error('在 onMounted 中检测到 expertId 无效或为空，因此未发送API请求！');
  }
});
</script>

<style scoped>

.content {
  margin-left: auto ;
  margin-right: auto ;

  justify-content: center;
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

//咨询按钮容器样式
<style scoped>
.profile-details {
    display: flex;
    gap: 1.5rem;
    align-items: flex-start;
    position: relative; /* 添加 relative 以便定位子元素 */
}

/* 咨询按钮容器样式 */
.consult-action {
  position: absolute; /* 绝对定位 */
  top: 0;             /* 距离顶部 */
  right: 0;           /* 距离右侧 */
  /* 或者使用 flexbox 将按钮放在最右边 */
  /* margin-left: auto; */
}

.consult-btn {
  padding: 8px 16px;
  background-color: #2D7D4F; /* 你的主题绿色 */
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 15px;
  font-weight: 500;
  transition: background-color 0.3s ease;
  box-shadow: 0 4px 10px rgba(45, 125, 79, 0.2);
}

.consult-btn:hover {
  background-color: #246640; /* hover 颜色 */
}

.consult-btn:active {
  transform: translateY(1px);
}

.login-prompt, .self-expert-tip {
  font-size: 14px;
  color: #888;
  padding: 8px 16px;
  background-color: #f0f0f0;
  border-radius: 8px;
  white-space: nowrap; /* 防止文字换行 */
  display: inline-block; /* 确保 padding 和背景正常显示 */
}
</style>