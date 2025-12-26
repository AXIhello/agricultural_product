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

        <!-- 审批材料-->
        <div v-if="application.attachmentPath" class="application-image">
            <h4>审核材料</h4>
            <img
              :src="application.attachmentUrl || getFullImageUrl(application.attachmentPath)"
              alt="申请附件"
              class="attachment-image"
              @load="() => console.log('✅ 图片加载成功', application.id)"
              @error="(e) => handleImageError(e, application)"
            />
        </div>

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
import defaultAvatar from '@/assets/admin_default.jpg';

defineOptions({
  name: 'AdminReviewPage'
});

const authStore = useAuthStore();
const { role } = storeToRefs(authStore);

const isAdmin = computed(() => role.value === 'admin');

const applications = ref([]);
const loading = ref(true);
const error = ref(null);

const BASE_IMAGE_URL =  '/'; // 假设图片的根路径和API的根路径相同，或者为空表示相对路径

const getFullImageUrl = (relativePath) => {
  // 确保相对路径以 / 开头，或者拼接时处理好斜杠
  if (relativePath.startsWith('/')) {
    return `${BASE_IMAGE_URL}${relativePath}`;
  }
  // 假设 attachment_path 已经包含了 '/uploads/applications/'
  // 例如：'uploads/applications/xxxxxxxx.jpg'
  // 那么你可能需要这样拼接：
  // return `${BASE_IMAGE_URL}/${relativePath}`;
  // 或者如果你的服务器配置允许直接访问，可以不加 BASE_IMAGE_URL
  return `${BASE_IMAGE_URL}/${relativePath}`; // 根据实际情况调整
};

// 图片加载失败时显示一个占位图或者隐藏
const handleImageError = (event, application) => {
  console.error('❌ 图片加载失败', {
    applicationId: application?.id,
    attachmentPath: application?.attachmentPath,
    attachmentUrl: application?.attachmentUrl,
    imgSrc: event.target.src
  });

  event.target.src = defaultAvatar;
  event.target.alt = '图片加载失败（已替换为默认图）';
};

const fetchApplications = async () => {
  console.log('✅ 生成 blob URL:', app.attachmentUrl);
  try {
    loading.value = true;
    const response = await request.get('/admin/applications/page', {
      params: {
        status: 'pending',
        page: 1,
        size: 10
      }
    });

    let rawApplications = response.data?.data?.records || [];

    await Promise.all(rawApplications.map(async (app) => {
      console.log('🧾 当前申请记录:', app);
      app.originalAttachmentPath = app.attachmentPath; 
      
      if (app.attachmentPath) {
        try {
          console.log(`📥 正在请求附件 applicationId=${app.id}`);

          const imageRes = await request.get(
              `/admin/applications/${app.id}/attachment`, // <--- 修改为新的后端接口路径
              { responseType: 'blob' }
          );
          console.log(
          `返回的 blob 信息: size=${imageRes.data.size}, type=${imageRes.data.type}`
         );

          if (imageRes.data && imageRes.data.size > 0) {
            app.attachmentUrl = URL.createObjectURL(imageRes.data);
            console.log('✅ 生成 blob URL:', app.attachmentUrl);
          } else {
            console.warn('⚠️ 图片 blob 为空');
            app.attachmentUrl = null;
          }
        } catch (err) {
          console.error(`❌ 加载附件失败 applicationId=${app.id}`, err);
          app.attachmentUrl = null;
        }
      } else {
        console.log('📭 该申请没有 attachment_path');
        app.attachmentUrl = null;
      }
    }));

    applications.value = rawApplications;

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

//图片样式
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

<style scoped>
.application-image {
  margin-top: 10px;
  border: 1px solid #eee;
  padding: 5px;
  border-radius: 5px;
  background-color: #fff;
}

.application-image h4 {
  margin-bottom: 5px;
  color: #666;
  font-size: 14px;
}

.attachment-image {
  max-width: 200px; /* 限制图片最大宽度 */
  max-height: 200px; /* 限制图片最大高度 */
  display: block; /* 确保图片独占一行，方便控制外边距 */
  margin: 0 auto 10px auto; /* 居中显示，下方留白 */
  border-radius: 5px;
  object-fit: contain; /* 保持图片比例，完整显示 */
  box-shadow: 0 2px 4px rgba(0,0,0,0.05);
}

.image-path {
  font-size: 12px;
  color: #888;
  word-break: break-all; /* 防止长路径撑开布局 */
}

.action-buttons {
  margin-top: 15px;
  display: flex;
  gap: 10px; /* 按钮之间间距 */
}

.action-buttons button {
  padding: 8px 15px;
  border: none;
  border-radius: 5px;
  cursor: pointer;
  font-size: 14px;
  transition: background-color 0.2s ease;
}

.action-buttons button:first-child { /* 批准按钮 */
  background-color: #2D7D4F;
  color: white;
}
.action-buttons button:first-child:hover {
  background-color: #24633E;
}

.action-buttons button:last-child { /* 拒绝按钮 */
  background-color: #D32F2F;
  color: white;
}
.action-buttons button:last-child:hover {
  background-color: #B71C1C;
}

.access-denied h2 {
  color: #B71C1C;
}
.access-denied p {
  color: #555;
}
</style>
