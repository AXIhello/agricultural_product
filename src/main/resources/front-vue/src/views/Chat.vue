<template>
  <div class="chat-container">
    
    <!-- 聊天窗口 -->
    <div class="chat-window">
      <div class="chat-header">
        <h3>正在与 用户 {{ receiverId }} 聊天</h3>
      </div>

      <div class="messages" ref="messageContainer">
        <div v-if="isLoading" class="loading-state">正在加载消息...</div>
        <div v-else-if="messages.length === 0" class="empty-state">还没有消息，开始聊天吧！</div>
        <div v-else v-for="message in messages" :key="message.messageId"
             :class="['message-item', message.senderId === currentUser.userId ? 'sent' : 'received']">
          <div class="message-bubble">
            <p class="message-content">{{ message.content }}</p>
            <span class="message-time">{{ formatMessageTime(message.sendTime) }}</span>
          </div>
        </div>
      </div>

      <div class="chat-input-area">
        <form @submit.prevent="sendMessage" class="message-form">
          <input
              type="text"
              v-model="newMessageContent"
              placeholder="输入消息..."
              class="message-input"
              :disabled="!currentSession"
          />
          <button type="submit" class="send-button" :disabled="!newMessageContent.trim() || !currentSession">
            发送
          </button>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick, watch } from 'vue';
import { useRoute } from 'vue-router';
import axios from '../utils/axios'; // 确保您的axios实例路径正确

// --- Reactive State ---
const route = useRoute();
const currentUser = ref({}); // 当前登录用户信息
const receiverId = ref(null); // 聊天对象的ID
const currentSession = ref(null); // 当前会话对象
const messages = ref([]); // 消息列表
const newMessageContent = ref(''); // 输入框内容
const isLoading = ref(true); // 加载状态
const messageContainer = ref(null); // DOM引用，用于滚动

// --- SSE 连接对象 ---
let eventSource = null;

// --- Lifecycle Hooks ---
onMounted(() => {
  // 1. 获取当前登录用户的信息
  const userInfoStr = localStorage.getItem('userInfo');
  if (!userInfoStr) {
    alert('请先登录！');
    // router.push('/login'); // 可选：跳转到登录页
    return;
  }
  currentUser.value = JSON.parse(userInfoStr);

  // 2. 从路由参数初始化聊天
  receiverId.value = parseInt(route.params.receiverId, 10);
  if (receiverId.value) {
    initializeChat(receiverId.value);
    setupSseConnection(); // 建立实时连接
  }
});

onUnmounted(() => {
  // 组件销毁时，关闭SSE连接以防内存泄漏
  if (eventSource) {
    eventSource.close();
    console.log('SSE connection closed.');
  }
});

// 监听路由变化，以便在同一个页面切换聊天对象时能重新加载
watch(
  () => route.params.receiverId,
  (newId) => {
    if (newId && parseInt(newId, 10) !== receiverId.value) {
      receiverId.value = parseInt(newId, 10);
      messages.value = []; // 清空旧消息
      currentSession.value = null;
      initializeChat(receiverId.value);
    }
  }
);


// --- Methods ---

/**
 * 初始化聊天，包括获取会话和历史消息
 * @param {number} peerId - 对方用户的ID
 */
async function initializeChat(peerId) {
  isLoading.value = true;
  try {
    // API: POST /api/chat/session/{peerId} -> 获取或创建会话
    const sessionRes = await axios.post(`/chat/session/${peerId}`);
    currentSession.value = sessionRes.data;

    if (currentSession.value && currentSession.value.sessionId) {
      // API: GET /api/chat/messages/{sessionId} -> 加载历史消息
      const messagesRes = await axios.get(`/chat/messages/${currentSession.value.sessionId}`);
      messages.value = messagesRes.data;
    }
  } catch (error) {
    console.error('初始化聊天失败:', error);
    alert('无法加载聊天记录，请稍后重试。');
  } finally {
    isLoading.value = false;
    await scrollToBottom();
  }
}

/**
 * 发送新消息
 */
async function sendMessage() {
  if (!newMessageContent.value.trim() || !currentSession.value) return;

  const messageData = {
    sessionId: currentSession.value.sessionId,
    content: newMessageContent.value,
    msgType: 'text',
  };

  try {
    // API: POST /api/chat/messages -> 发送消息
    await axios.post('/chat/messages', messageData);
    
    // 清空输入框
    newMessageContent.value = '';
    // 发送成功后，新消息会通过SSE推送过来，所以前端不需要手动添加
    // 这样可以保证数据来源的唯一性
  } catch (error) {
    console.error('发送消息失败:', error);
    alert('消息发送失败！');
  }
}

/**
 * 建立Server-Sent Events (SSE)连接以接收实时消息
 */
function setupSseConnection() {
  const token = localStorage.getItem('token');
  if (!token) return;

  // 后端接口要求token通过查询参数传递
  const url = `/api/chat/stream?token=${token}`;
  
  eventSource = new EventSource(url);

  eventSource.onopen = () => {
    console.log('SSE connection established.');
  };

  // 监听后端推送的消息事件
  eventSource.onmessage = (event) => {
    const newMessage = JSON.parse(event.data);
    
    // 只处理当前会话的消息
    if (currentSession.value && newMessage.sessionId === currentSession.value.sessionId) {
      messages.value.unshift(newMessage);
      scrollToBottom();
    }
  };

  eventSource.onerror = (error) => {
    console.error('SSE Error:', error);
    eventSource.close();
    // 这里可以添加重连逻辑
  };
}


/**
 * 根据消息时间，格式化为 "今天/昨天/日期" 的形式
 * @param {string} dateTimeStr - 后端返回的时间字符串 (e.g., "2023-10-27T15:30:00")
 */
function formatMessageTime(dateTimeStr) {
  if (!dateTimeStr) return '';

  const messageDate = new Date(dateTimeStr);
  const now = new Date();

  // 定义时间边界
  const todayStart = new Date(now.getFullYear(), now.getMonth(), now.getDate()); // 今天 00:00
  const yesterdayStart = new Date(todayStart.getTime() - 24 * 60 * 60 * 1000); // 昨天 00:00
  const thisYearStart = new Date(now.getFullYear(), 0, 1); // 今年第一天 00:00

  // 辅助函数：格式化时间为 HH:mm
  const formatTimeOnly = (date) => {
    const hours = date.getHours().toString().padStart(2, '0');
    const minutes = date.getMinutes().toString().padStart(2, '0');
    return `${hours}:${minutes}`;
  };

  const timeStr = formatTimeOnly(messageDate);

  if (messageDate.getTime() >= todayStart.getTime()) {
    // 今天：只显示时间
    return timeStr;
  } else if (messageDate.getTime() >= yesterdayStart.getTime()) {
    // 昨天：显示“昨天 + 时间”
    return `昨天 ${timeStr}`;
  } else if (messageDate.getTime() >= thisYearStart.getTime()) {
    // 今年内，但早于昨天：显示“月-日 + 时间”
    const month = (messageDate.getMonth() + 1).toString().padStart(2, '0');
    const day = messageDate.getDate().toString().padStart(2, '0');
    return `${month}-${day} ${timeStr}`;
  } else {
    // 去年或更早：显示“年-月-日 + 时间”
    const year = messageDate.getFullYear();
    const month = (messageDate.getMonth() + 1).toString().padStart(2, '0');
    const day = messageDate.getDate().toString().padStart(2, '0');
    return `${year}-${month}-${day} ${timeStr}`;
  }
}


/**
 * 滚动到聊天窗口底部
 */
async function scrollToBottom() {
  await nextTick(); // 等待DOM更新
  if (messageContainer.value) {
    messageContainer.value.scrollTop = messageContainer.value.scrollHeight;
  }
}

</script>

<style scoped>
.chat-container {
  display: flex;
  height: calc(100vh - 100px); /* 减去头部导航栏的高度 */
  width: 100%;
  background-color: #f5f5f5;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.chat-window {
  flex-grow: 1;
  display: flex;
  flex-direction: column;
}

.chat-header {
  padding: 15px 20px;
  border-bottom: 1px solid #e0e0e0;
  background-color: #ffffff;
}

.chat-header h3 {
  margin: 0;
  font-size: 1.2rem;
  color: #333;
}

.messages {
  flex-grow: 1;
  padding: 20px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.message-item {
  display: flex;
  max-width: 70%;
}

.message-bubble {
  padding: 10px 15px;
  border-radius: 18px;
  position: relative;
}

.message-content {
  margin: 0;
  word-wrap: break-word;
}

.message-time {
  display: block;
  font-size: 0.75rem;
  color: #888;
  margin-top: 5px;
  text-align: right;
}

/* 自己发送的消息 */
.message-item.sent {
  align-self: flex-end;
}
.message-item.sent .message-bubble {
  background-color: #DCF8C6; /* 绿色气泡 */
  border-bottom-right-radius: 4px;
}
.message-item.sent .message-time {
  color: #6a8a5b;
}

/* 接收到的消息 */
.message-item.received {
  align-self: flex-start;
}
.message-item.received .message-bubble {
  background-color: #FFFFFF; /* 白色气泡 */
  border-bottom-left-radius: 4px;
}

.chat-input-area {
  padding: 15px 20px;
  border-top: 1px solid #e0e0e0;
  background-color: #f9f9f9;
}

.message-form {
  display: flex;
  gap: 10px;
}

.message-input {
  flex-grow: 1;
  padding: 12px;
  border: 1px solid #ccc;
  border-radius: 20px;
  font-size: 1rem;
}

.send-button {
  padding: 10px 20px;
  border: none;
  background-color: #2D7D4F; /* 主题绿色 */
  color: white;
  border-radius: 20px;
  cursor: pointer;
  font-weight: bold;
  transition: background-color 0.3s;
}

.send-button:hover {
  background-color: #256841;
}

.send-button:disabled {
  background-color: #a5d6a7;
  cursor: not-allowed;
}

.loading-state, .empty-state {
  text-align: center;
  color: #aaa;
  margin: auto;
}
</style>