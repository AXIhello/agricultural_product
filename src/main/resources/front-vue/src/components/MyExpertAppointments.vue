<template>
  <div class="main-bg">
    <HeaderComponent />
    <section class="content my-appointments-container">
      <h2>我的预约记录</h2>
      
      <div v-if="isLoading" class="loading-state">正在加载您的预约...</div>
      <div v-else-if="appointments.length === 0" class="empty-state">
        您还没有任何预约记录。
      </div>

      <table v-else class="appointments-table">
        <thead>
          <tr>
            <th>预约ID</th>
            <th>专家姓名</th>
            <th>预约日期</th>
            <th>预约时间</th>
            <th>状态</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="appt in appointments" :key="appt.consultationId">
            <td>{{ appt.consultationId }}</td>
            <td>{{ appt.expertName || '未知专家' }}</td>
            <td>{{ appt.workDate }}</td>
            <td>{{ appt.startTime }} - {{ appt.endTime }}</td>
            <td>
              <span :class="['status-badge', `status-${appt.status.toLowerCase()}`]">{{ getStatusText(appt.status) }}</span>
            </td>
            <td>
              <button
                v-if="appt.status === 'scheduled'"
                @click="cancelAppointment(appt.consultationId)"
                class="cancel-btn"
                :disabled="isCancelling"
              >
                取消预约
              </button>
              <span v-else>--</span>
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
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue';
import axios from '../utils/axios';
import HeaderComponent from '@/components/HeaderComponent.vue';
import { ElMessage } from 'element-plus';

const appointments = ref([]);
const isLoading = ref(true);
const isCancelling = ref(false);

const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  pages: 1,
});

async function fetchMyAppointments() {
  isLoading.value = true;
  try {
    const params = {
      pageNum: pagination.current,
      pageSize: pagination.pageSize,
    };
    // API: 获取当前农户的预约列表
    const response = await axios.get('/api/expert-appointments/my', { params });
    const data = response.data;
    appointments.value = data.records || [];
    pagination.total = data.total;
    pagination.pages = data.pages;
    pagination.current = data.current;
  } catch (error) {
    console.error("获取我的预约失败:", error);
    ElMessage.error("获取预约记录失败，请稍后重试。");
  } finally {
    isLoading.value = false;
  }
}

async function cancelAppointment(consultationId) {
  if (!confirm(`您确定要取消预约ID为 ${consultationId} 的记录吗？`)) return;
  
  isCancelling.value = true;
  try {
    // API: 农户取消预约
    const response = await axios.post(`/api/expert-appointments/cancel?consultationId=${consultationId}`);
    if (response.data) {
      ElMessage.success("预约已成功取消！");
      // 刷新列表
      await fetchMyAppointments();
    } else {
      ElMessage.error("取消失败，请稍后重试。");
    }
  } catch (error) {
    console.error("取消预约失败:", error);
    ElMessage.error(error.response?.data?.message || "取消预约时发生错误。");
  } finally {
    isCancelling.value = false;
  }
}

function changePage(page) {
  if (page > 0 && page <= pagination.pages) {
    pagination.current = page;
    fetchMyAppointments();
  }
}

const getStatusText = (status) => {
  const map = {
    scheduled: '已预约',
    completed: '已完成',
    cancelled: '已取消',
  };
  return map[status.toLowerCase()] || '未知';
};

onMounted(() => {
  fetchMyAppointments();
});
</script>

<style scoped>
/* 沿用详情页和个人中心的统一样式 */
.main-bg { display: flex; flex-direction: column; height: 100vh; background-color: #F0F9F4; }
.content { flex: 1; padding: 20px; background: white; }
.my-appointments-container { max-width: 1000px; margin: auto; }

h2 { margin-top: 0; color: #2D7D4F; border-bottom: 2px solid #F0F9F4; padding-bottom: 0.5rem; margin-bottom: 1.5rem; }
.loading-state, .empty-state { text-align: center; color: #888; padding: 2rem; margin-top: 2rem; background-color: #f8f9fa; border-radius: 8px; }

.appointments-table { width: 100%; border-collapse: collapse; margin-top: 1.5rem; }
.appointments-table th, .appointments-table td { padding: 0.8rem 1rem; text-align: left; border-bottom: 1px solid #dee2e6; vertical-align: middle; }
.appointments-table th { background-color: #f8f9fa; font-weight: 600; color: #333; }

.cancel-btn { padding: 6px 12px; border-radius: 5px; border: none; background-color: #dc3545; color: white; cursor: pointer; }
.cancel-btn:hover { background-color: #c82333; }
.cancel-btn:disabled { background-color: #6c757d; cursor: not-allowed; }

.status-badge { padding: 0.2rem 0.6rem; border-radius: 12px; font-size: 0.8rem; color: white; font-weight: 500; }
.status-scheduled { background-color: #007bff; }
.status-completed { background-color: #28a745; }
.status-cancelled { background-color: #6c757d; }

.pagination { display: flex; justify-content: center; align-items: center; gap: 1rem; margin-top: 1.5rem; }
.pagination button { padding: 0.5rem 1rem; border: 1px solid #ddd; background-color: #fff; cursor: pointer; }
.pagination button:disabled { color: #ccc; cursor: not-allowed; }
</style>