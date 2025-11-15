<template>
  <div class="availability-management">
    <!-- 1. 创建时间段的表单 -->
    <div class="create-slot-card">
      <h2>创建工作时间段</h2>
      <form @submit.prevent="handleCreateSlots" class="slot-form">
        <div class="form-row">
          <div class="form-group">
            <label for="workDate">工作日期</label>
            <input type="date" id="workDate" v-model="newSlot.workDate" required />
          </div>
          <div class="form-group">
            <label for="startTime">开始时间</label>
            <input type="time" id="startTime" v-model="newSlot.startTime" required />
          </div>
          <div class="form-group">
            <label for="endTime">结束时间</label>
            <input type="time" id="endTime" v-model="newSlot.endTime" required />
          </div>
        </div>
        <div class="form-row">
          <div class="form-group">
            <label for="capacity">可预约人数</label>
            <input type="number" id="capacity" v-model.number="newSlot.capacity" min="1" required />
          </div>
          <button type="button" @click="addSlotToList" class="add-btn">＋ 添加到列表</button>
        </div>
      </form>

      <!-- 待提交的时间段列表 -->
      <div v-if="pendingSlots.length > 0" class="pending-slots-list">
        <h4>待提交的时间段</h4>
        <ul>
          <li v-for="(slot, index) in pendingSlots" :key="index">
            <span>{{ slot.workDate }} {{ slot.startTime }} - {{ slot.endTime }} (容量: {{ slot.capacity }})</span>
            <button @click="removeSlotFromList(index)" class="remove-btn">移除</button>
          </li>
        </ul>
        <button @click="handleCreateSlots" class="submit-all-btn" :disabled="isLoading">
          {{ isLoading ? '提交中...' : '确认并提交所有' }}
        </button>
      </div>
    </div>

    <!-- 2. 查看和筛选已创建的时间段 -->
    <div class="view-slots-card">
      <h2>我的工作排期</h2>
      <div class="filters">
        <div class="form-group">
          <label for="filterFrom">开始日期</label>
          <input type="date" id="filterFrom" v-model="filters.from" />
        </div>
        <div class="form-group">
          <label for="filterTo">结束日期</label>
          <input type="date" id="filterTo" v-model="filters.to" />
        </div>
        <button @click="fetchSlots" class="filter-btn">筛选</button>
      </div>

      <!-- 时间段列表 -->
      <div v-if="isLoadingSlots" class="loading-state">加载中...</div>
      <div v-else-if="slots.length === 0" class="empty-state">在指定日期内没有找到工作排期。</div>
      <table v-else class="slots-table">
        <thead>
          <tr>
            <th>日期</th>
            <th>时间</th>
            <th>状态</th>
            <th>容量</th>
            <th>已预约</th>
            <th>剩余</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="slot in slots" :key="slot.slotId">
            <td>{{ slot.workDate }}</td>
            <td>{{ slot.startTime }} - {{ slot.endTime }}</td>
            <td>
              <span :class="['status-badge', `status-${slot.status.toLowerCase()}`]">{{ getStatusText(slot.status) }}</span>
            </td>
            <td>{{ slot.capacity }}</td>
            <td>{{ slot.bookedCount }}</td>
            <td>{{ slot.capacity - slot.bookedCount }}</td>
          </tr>
        </tbody>
      </table>
      
      <!-- 分页 -->
      <div class="pagination">
        <button @click="changePage(pagination.current - 1)" :disabled="pagination.current <= 1">上一页</button>
        <span>第 {{ pagination.current }} / {{ pagination.pages }} 页</span>
        <button @click="changePage(pagination.current + 1)" :disabled="pagination.current >= pagination.pages">下一页</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue';
import axios from '../utils/axios'; 
import { ElMessage } from 'element-plus'; 

// --- 1. 创建时间段逻辑 ---
const newSlot = ref({
  workDate: '',
  startTime: '',
  endTime: '',
  capacity: 1,
});
const pendingSlots = ref([]);
const isLoading = ref(false);

const addSlotToList = () => {
  // 简单验证
  if (!newSlot.value.workDate || !newSlot.value.startTime || !newSlot.value.endTime || newSlot.value.capacity < 1) {
    ElMessage.error('请填写完整的时间段信息！');
    return;
  }
  if (newSlot.value.endTime <= newSlot.value.startTime) {
    ElMessage.error('结束时间必须晚于开始时间！');
    return;
  }
  
  pendingSlots.value.push({ ...newSlot.value });
  // 清空表单以便添加下一个
  newSlot.value = {
    workDate: '',
    startTime: '',
    endTime: '',
    capacity: 1,
  };
};

const removeSlotFromList = (index) => {
  pendingSlots.value.splice(index, 1);
};

const handleCreateSlots = async () => {
  if (pendingSlots.value.length === 0) {
    ElMessage.warning('请至少添加一个时间段再提交！');
    return;
  }
  isLoading.value = true;
  try {
    const response = await axios.post('/expert-appointments/slots', pendingSlots.value);
    if (response.data) {
      ElMessage.success('工作时间段创建成功！');
      pendingSlots.value = []; // 清空待提交列表
      fetchSlots(); // 刷新已创建的时间段列表
    } else {
      ElMessage.error('创建失败，请稍后重试。');
    }
  } catch (error) {
    console.error('创建时间段失败:', error);
    ElMessage.error(error.response?.data?.message || '创建失败，请检查网络或联系管理员。');
  } finally {
    isLoading.value = false;
  }
};

// --- 2. 查看和筛选时间段逻辑 ---
const slots = ref([]);
const isLoadingSlots = ref(false);
const filters = reactive({
  from: '',
  to: '',
});
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  pages: 1,
});

const fetchSlots = async () => {
  isLoadingSlots.value = true;
  try {
    const params = {
      from: filters.from || null,
      to: filters.to || null,
      pageNum: pagination.current,
      pageSize: pagination.pageSize,
    };
    const response = await axios.get('/expert-appointments/slots', { params });
    const data = response.data;
    slots.value = data.records || [];
    pagination.total = data.total;
    pagination.pages = data.pages;
    pagination.current = data.current;
  } catch (error) {
    console.error('获取时间段列表失败:', error);
    ElMessage.error(error.response?.data?.message || '获取排期失败，请稍后重试。');
  } finally {
    isLoadingSlots.value = false;
  }
};

const changePage = (page) => {
  if (page > 0 && page <= pagination.pages) {
    pagination.current = page;
    fetchSlots();
  }
};

const getStatusText = (status) => {
  const map = {
    open: '开放中',
    closed: '已关闭',
  };
  return map[status.toLowerCase()] || '未知';
};

// 组件加载时自动获取第一页数据
onMounted(() => {
  fetchSlots();
});
</script>


<style scoped>
/* 使用与父组件一致的字体 */
.availability-management {
  font-family: sans-serif;
  margin-top: 2rem; /* 与父组件中的 expert-profile-container 保持间距 */
}

/* 统一卡片样式 */
.create-slot-card, .view-slots-card {
  padding: 1.5rem;
  background-color: #fff;
  border: 1px solid #e0e0e0; /* 保持边框一致 */
  border-radius: 8px; /* 保持圆角一致 */
  margin-bottom: 2rem; /* 卡片间距 */
}

/* 统一标题样式 (h2, h3, h4) */
h2 {
  margin-top: 0;
  color: #2D7D4F; /* 使用主题绿色 */
  border-bottom: 2px solid #F0F9F4; /* 使用主题浅绿色 */
  padding-bottom: 0.5rem;
  margin-bottom: 1.5rem;
  font-size: 1.5rem; /* 调整字号使其与 h3 协调 */
}

h4 {
  color: #555;
  margin-top: 1.5rem;
  margin-bottom: 1rem;
}

/* 统一表单和输入框样式 */
.slot-form {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.form-row {
  display: flex;
  flex-wrap: wrap; /* 在小屏幕上可以换行 */
  gap: 1.5rem;
  align-items: flex-end;
}

.form-group {
  display: flex;
  flex-direction: column;
  flex: 1;
  min-width: 150px; /* 防止输入框被过度挤压 */
}

.form-group label {
  margin-bottom: 0.5rem;
  font-size: 0.9rem;
  color: #333; /* 字体颜色加深 */
  font-weight: bold; /* 与父组件 label 风格一致 */
}

.form-group input {
  padding: 8px; /* 与父组件输入框 padding 一致 */
  border: 1px solid #ccc;
  border-radius: 4px;
  font-size: 1rem;
}

/* 统一按钮样式 */
.add-btn, .filter-btn, .submit-all-btn {
  padding: 8px 16px;
  border-radius: 5px;
  border: 1px solid #2D7D4F;
  background-color: #2D7D4F;
  color: white;
  cursor: pointer;
  font-size: 0.9rem;
  transition: background-color 0.3s;
  height: fit-content; /* 让按钮高度自适应内容 */
}

.add-btn:hover, .filter-btn:hover, .submit-all-btn:hover {
  background-color: #256842;
}

.submit-all-btn {
  align-self: flex-end; /* 按钮靠右 */
  margin-top: 1rem;
}

.submit-all-btn:disabled {
  background-color: #ccc;
  border-color: #ccc;
  cursor: not-allowed;
}

/* 待提交列表样式 */
.pending-slots-list {
  margin-top: 1.5rem;
}

.pending-slots-list ul {
  list-style: none;
  padding: 0;
}

.pending-slots-list li {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.8rem 1rem;
  background-color: #F0F9F4; /* 使用主题浅绿色作为背景 */
  border-radius: 4px;
  margin-bottom: 0.5rem;
  color: #333;
}

.remove-btn {
  background: none;
  border: none;
  color: #c82333; /* 使用父组件的删除按钮红色 */
  cursor: pointer;
  font-weight: bold;
}

/* 筛选区域样式 */
.filters {
  display: flex;
  gap: 1.5rem;
  margin-bottom: 1.5rem;
  align-items: flex-end;
}

/* 加载和空状态 */
.loading-state, .empty-state {
  text-align: center;
  color: #888;
  padding: 2rem;
  background-color: #F0F9F4; /* 使用主题浅绿色背景 */
  border-radius: 8px;
  margin-top: 1rem;
}

/* 表格样式 */
.slots-table {
  width: 100%;
  border-collapse: collapse;
  margin-top: 1.5rem;
}

.slots-table th, .slots-table td {
  padding: 0.8rem 1rem;
  text-align: left;
  border-bottom: 1px solid #dee2e6;
  vertical-align: middle;
}

.slots-table th {
  background-color: #f8f9fa;
  font-weight: bold;
  color: #333;
}

/* 状态徽章 */
.status-badge {
  padding: 0.2rem 0.6rem;
  border-radius: 12px;
  font-size: 0.8rem;
  color: white;
  text-transform: capitalize;
  font-weight: 500;
}

.status-open {
  background-color: #28a745; /* 沿用之前的绿色，表示成功状态 */
}

.status-closed {
  background-color: #6c757d;
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
  padding: 6px 12px;
  border: 1px solid #ccc;
  background-color: #f0f0f0;
  color: #333;
  border-radius: 5px;
  cursor: pointer;
  transition: background-color 0.3s;
}
.pagination button:hover {
  background-color: #e0e0e0;
}

.pagination button:disabled {
  color: #aaa;
  background-color: #f8f8f8;
  cursor: not-allowed;
}
</style>