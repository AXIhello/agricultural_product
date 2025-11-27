<template>
  <div class="main-bg">
    <HeaderComponent />
    <div v-if="isAdmin" class="content">
    <h2>审核信息</h2>
    <!--  显示待审核的申请列表，以及审核的按钮等  -->
    <p v-if="loading">加载中...</p>
    <p v-else-if="error">加载失败: {{ error }}</p>
    <div v-else>
      <div v-for="application in applications" :key="application.id" class="application-item">
        <h3>{{ application.apply_role === 'expert' ? '专家申请' : '银行账号申请' }}</h3>
        <p>申请人: {{ application.userName }}</p>
        <p>申请信息: {{ application.reason }}</p>
        <div class="action-buttons">
          <button @click="approveApplication(application.id, application.type)">批准</button>
          <button @click="rejectApplication(application.id, application.type)">拒绝</button>
        </div>
      </div>
      <p v-if="applications.length === 0">暂无待审核申请。</p>
    </div>
    </div>

    <div v-else class="access-denied">
      <h2>无权访问</h2>
      <p>此页面仅限管理员访问。</p>
    </div>

  </div>
</template>

<script setup>
// 1. 清理导入，只保留一个并命名为 request
import request from '../utils/axios.js';
import { ref, onMounted ,computed} from 'vue';
import HeaderComponent from '../components/HeaderComponent.vue';
import { useAuthStore } from '@/stores/authStore';
import { storeToRefs } from 'pinia';

defineOptions({
  name: 'AdminReviewPage'
});

const authStore = useAuthStore();
const { role } = storeToRefs(authStore);

const isAdmin = computed(() => role.value === 'admin');

const applications = ref([]);
const loading = ref(true);
const error = ref(null);

const fetchApplications = async () => {
  try {
    loading.value = true;
    const response = await request.get('/admin/applications/page', {
      params: {
        status: 'pending',
        page: 1,
        size: 10
      }
    });

    // 2. 添加日志来检查后端返回的真实数据
    console.log('后端返回的完整响应:', response);
    console.log('响应中的 data 部分:', response.data);
    console.log('响应中的 records 部分:', response.data?.records);

    applications.value = response.data?.data?.records || [];

    if (applications.value.length === 0) {
      console.warn('获取到的申请列表为空数组。请检查后端查询逻辑或返回的数据结构。');
    }

  } catch (err) {
    error.value = err.message || '获取申请列表失败。';
    console.error('获取申请列表时发生错误:', err);
  } finally {
    loading.value = false;
  }
};

const handleReview = async (applicationId, status, type) => {
  try {
    if (status === 'approved') {

      await request.post(`/admin/applications/${applicationId}/approve`);
    } else if (status === 'rejected') {
      const reason = prompt("请输入拒绝理由：");
      if (reason === null) return;
      await request.post(`/admin/applications/${applicationId}/reject`, null, {
        params: { reason }
      });
    }
    alert('操作成功！');
    fetchApplications(); // 刷新列表
  } catch (err) {
    console.error(`审核申请 ${applicationId} 失败:`, err);
    alert(`操作失败: ${err.message}`);
  }
};
/**
 * 处理“批准”申请的函数
 * @param {number | string} applicationId 申请记录的 ID
 */
const approveApplication = async (applicationId) => {
  try {
    // 弹窗确认，防止误操作
    if (!confirm('确定要批准这条申请吗？')) {
      return;
    }

    // 调用后端的批准接口
    // 注意：URL 路径根据你的 baseURL 配置，这里假设 baseURL 是 /api
    const response = await request.post(`/admin/applications/${applicationId}/approve`);

    // 使用后端返回的消息提示用户
    alert(response.data?.message || response.data || '操作成功！'); // 后端可能直接返回字符串或在Result对象中

    // 操作成功后，重新加载列表以移除已处理的项
    fetchApplications();

  } catch (err) {
    console.error(`批准申请 ${applicationId} 失败:`, err);
    // 提示更详细的错误信息
    const errorMessage = err.response?.data?.message || err.response?.data || err.message;
    alert(`操作失败: ${errorMessage}`);
  }
};

/**
 * 处理“拒绝”申请的函数
 * @param {number | string} applicationId 申请记录的 ID
 */
const rejectApplication = async (applicationId) => {
  try {
    // 弹出一个输入框让管理员填写拒绝理由
    const reason = prompt('请输入拒绝该申请的理由：');

    // 如果用户点击了“取消”或没有输入任何内容
    if (reason === null || reason.trim() === '') {
      alert('操作已取消或拒绝理由不能为空。');
      return;
    }

    // 调用后端的拒绝接口，并通过 params 发送查询参数
    const response = await request.post(
        `/admin/applications/${applicationId}/reject`,
        null, // 第一个参数是请求体，这里我们没有，所以传 null
        {
          params: {
            reason: reason // reason 会被拼接到 URL 后面，变成 ?reason=...
          }
        }
    );

    alert(response.data?.message || response.data || '操作成功！');

    // 重新加载列表
    fetchApplications();

  } catch (err) {
    console.error(`拒绝申请 ${applicationId} 失败:`, err);
    const errorMessage = err.response?.data?.message || err.response?.data || err.message;
    alert(`操作失败: ${errorMessage}`);
  }
};


onMounted(() => {
  if (isAdmin.value) {
    fetchApplications();
  } else {
    console.warn("一个非管理员用户尝试访问 AdminReview 页面。");
  }
});
</script>

<style scoped>
.content {
  margin-left: 20px;
  margin-right: 20px;
  width: calc(100% - 40px);
  padding: 26px;
}

/* 页面整体结构 */
.access-denied {
  max-width: 900px;
  margin: 20px auto;
  background: #ffffff;
  padding: 24px;
  border-radius: 10px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.06);
}

/* 每条申请卡片 */
.application-item {
  background: #F9F9F9;
  border: 1px solid #E3E3E3;
  border-radius: 10px;
  padding: 16px;
  margin-bottom: 20px;
  transition: 0.25s ease-in-out;
}

.application-item:hover {
  transform: translateY(-3px);
  box-shadow: 0 4px 10px rgba(0,0,0,0.08);
}

/* 小标题 */
.application-item h3 {
  margin-bottom: 8px;
  font-size: 18px;
  font-weight: bold;
  color: #2D7D4F;
}

/* 内容文本 */
.application-item p {
  margin: 4px 0;
  font-size: 14px;
  color: #555;
}

/* 无权限页面 */
.access-denied h2 {
  color: #B71C1C;
}
.access-denied p {
  color: #555;
}
</style>
