<template>
  <div class="message-center-container">
    <HeaderComponent />
    <main class="content">
      <div class="page-header">
        <h1>消息中心</h1>
        <p>在这里查看您所有的会话</p>
      </div>

      <div class="session-list-wrapper">
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
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import axios from '../utils/axios'; // 确认axios实例路径
import HeaderComponent from '../components/HeaderComponent.vue'; // 引入头部组件

// --- State ---
const router = useRouter();
const sessions = ref([]);
const isLoading = ref(true);
const currentUser = ref({});

// --- Methods ---

/**
 * 从后端加载用户的会话列表.
 */
async function loadSessions() {
  try {
    // API: GET /api/chat/sessions
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
  if (!currentUser.value.userId) return { id: '未知' };
  const peerId = session.userAId === currentUser.value.userId ? session.userBId : session.userAId;
  return { id: peerId };
}

/**
 * 导航到对应的聊天室.
 * @param {object} session - 被点击的会话对象.
 */
function goToChat(session) {
  const peer = getPeerUser(session);
  // 添加对自己ID的判断
  if (peer.id !== '未知' && peer.id !== currentUser.value.userId) { 
    router.push(`/chat/${peer.id}`);
  } else if (peer.id === currentUser.value.userId) {
    // 如果是自己，则不跳转，并给出提示
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

// --- Lifecycle Hook ---
onMounted(() => {
  const userInfoStr = localStorage.getItem('userInfo');
  if (userInfoStr) {
    currentUser.value = JSON.parse(userInfoStr);
    loadSessions();
  } else {
    alert('请先登录以查看消息！');
    router.push('/login');
  }
});
</script>

<style scoped>
.message-center-container {
  display: flex;
  flex-direction: column;
  background-color: #f7f9fa;
  min-height: 100vh;
}

.content {
  width: 100%;
  max-width: 800px;
  margin: 2rem auto;
  padding: 2rem;
  background-color: #ffffff;
  border-radius: 8px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

.page-header {
  border-bottom: 1px solid #e0e0e0;
  padding-bottom: 1rem;
  margin-bottom: 2rem;
}

.page-header h1 {
  margin: 0;
  color: #333;
}

.page-header p {
  margin: 0.5rem 0 0;
  color: #666;
}

.session-list-wrapper {
  margin-top: 1rem;
}

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
</style>