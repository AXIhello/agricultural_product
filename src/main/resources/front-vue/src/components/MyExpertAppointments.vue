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
/* —— 全局布局 —— */
.main-bg {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background-color: #F0F9F4; /* 温和浅绿背景 */
  padding: 20px;
}

.content {
  flex: 1;
  padding: 25px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.08);
  max-width: 1150px;
  margin: auto;
}

/* —— 标题 —— */
h2 {
  margin-top: 0;
  font-size: 22px;
  font-weight: 700;
  color: #2d7d4f;
  border-bottom: 2px solid #d8eede;
  padding-bottom: 10px;
  margin-bottom: 20px;
}

/* —— 表格 —— */
.appointments-table {
  width: 100%;
  border-collapse: collapse;
  margin-top: 20px;
}

.appointments-table th,
.appointments-table td {
  padding: 14px 18px;
  text-align: left;
  border-bottom: 1px solid #e4ece7;
  font-size: 15px;
}

.appointments-table th {
  background-color: #eaf5ef;
  color: #2d7d4f;
  font-weight: 600;
}

/* —— 按钮（取消预约） —— */
.cancel-btn {
  padding: 6px 14px;
  border-radius: 6px;
  border: none;
  font-size: 14px;
  cursor: pointer;
  background-color: #d9534f;
  color: white;
  transition: background-color 0.25s ease;
}

.cancel-btn:hover {
  background-color: #c0392b;
}

.cancel-btn:disabled {
  background-color: #a4b1aa;
  cursor: not-allowed;
}

/* —— 状态标识 —— */
.status-badge {
  padding: 4px 10px;
  border-radius: 999px;
  font-size: 13px;
  font-weight: 600;
  color: white;
}

.status-scheduled { background-color: #0d6efd; }
.status-completed { background-color: #28a745; }
.status-cancelled { background-color: #6c757d; }

/* —— 分页 —— */
.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 12px;
  margin-top: 22px;
}

.pagination button {
  padding: 6px 14px;
  border: 1px solid #d1e3d8;
  background-color: white;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.25s ease;
}

.pagination button:hover:not(:disabled) {
  background-color: #cceedd;
  border-color: #8cc9a3;
}

.pagination button:disabled {
  background-color: #f1f5f3;
  color: #b7c2bc;
  cursor: not-allowed;
}
</style>
