<template>
  <div>
    <HeaderComponent />
    <h2>审核信息</h2>
    <!--  显示待审核的申请列表，以及审核的按钮等  -->
    <p v-if="loading">加载中...</p>
    <p v-else-if="error">加载失败: {{ error }}</p>
    <div v-else>
      <div v-for="application in applications" :key="application.id" class="application-item">
        <h3>{{ application.type === 'expert' ? '专家申请' : '银行账号申请' }}</h3>
        <p>申请人: {{ application.userName }}</p>
        <p>申请信息: {{ application.details }}</p>
        <div class="actions">
          <button @click="approveApplication(application.id, application.type)">批准</button>
          <button @click="rejectApplication(application.id, application.type)">拒绝</button>
        </div>
      </div>
      <p v-if="applications.length === 0">暂无待审核申请。</p>
    </div>
  </div>
</template>

<script>
import axios from '../utils/axios';  // 假设你有封装的 axios
import HeaderComponent from '../components/HeaderComponent.vue';

export default {
  name: 'AdminReviewPage',
  data() {
    return {
      applications: [],  //  待审核的申请列表
      loading: true,
      error: null,
    };
  },
  mounted() {
    this.loadApplications();
  },
  components: {
    HeaderComponent,
  },
  methods: {
    async loadApplications() {
      this.loading = true;
      this.error = null;
      try {
        const response = await axios.get('/admin/applications'); //  后端 API，获取所有待审核申请
        if (response.data.success) {
          this.applications = response.data.data;  //  假设后端返回的数据结构是 { success: true, data: [...] }
        } else {
          this.error = response.data.message || '获取申请失败';
        }
      } catch (err) {
        this.error = '网络错误或服务器错误';
      } finally {
        this.loading = false;
      }
    },
    async approveApplication(id, type) {
      //  处理批准逻辑
      try {
        const response = await axios.post('/api/admin/approve-application', { id, type });
        if (response.data.success) {
          alert('批准成功'); //  可以替换成更友好的提示
          this.loadApplications(); //  重新加载申请列表
        } else {
          alert('批准失败');
        }
      } catch (err) {
        alert('批准失败，请检查网络或服务器。');
      }
    },
    async rejectApplication(id, type) {
      // 处理拒绝逻辑
      try {
        const response = await axios.post('/api/admin/reject-application', { id, type });
        if (response.data.success) {
          alert('拒绝成功');
          this.loadApplications();
        } else {
          alert('拒绝失败');
        }
      } catch (err) {
        alert('拒绝失败，请检查网络或服务器。');
      }
    },
  },
};
</script>

<style scoped>
.application-item {
  border: 1px solid #ccc;
  padding: 10px;
  margin-bottom: 10px;
  border-radius: 4px;
}
.actions {
  margin-top: 10px;
}
button {
  margin-right: 10px;
  padding: 8px 12px;
  border: none;
  border-radius: 4px;
  background-color: #4CAF50; /* 绿色 */
  color: white;
  cursor: pointer;
}
button:hover {
  background-color: #3e8e41; /* 鼠标悬停时的颜色 */
}
</style>