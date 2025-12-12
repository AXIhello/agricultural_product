<template>
  <div class="chat-wrapper">
    <div class="chat-container">
      
      <!-- 头部 -->
      <div class="chat-header">
        <button @click="goBack()" class="icon-btn back-btn" title="返回">
          <svg viewBox="0 0 24 24" width="24" height="24" stroke="currentColor" stroke-width="2" fill="none" stroke-linecap="round" stroke-linejoin="round"><path d="M15 18l-6-6 6-6"/></svg>
        </button>
        <div class="header-info">
          <h3>用户 {{ receiverId }}</h3>
        </div>
        
      </div>

      <!-- 消息列表 -->
      <div class="messages" ref="messageContainer">
        <div v-if="isLoading" class="state-text">
          <div class="spinner"></div> 正在加载消息...
        </div>
        
        <div v-else-if="messages.length === 0" class="state-text empty">
          <img src="https://cdn-icons-png.flaticon.com/512/2665/2665038.png" alt="Empty" width="60" style="opacity: 0.5; margin-bottom: 10px;">
          <p>还没有消息，打个招呼吧 </p>
        </div>

        <div v-else class="message-group">
          <div 
            v-for="(message, index) in messages" 
            :key="message.messageId"
            :class="['message-row', message.senderId === currentUser?.userId ? 'row-sent' : 'row-received']"
          >
            <!-- 头像 (这里用首字母或图标模拟) -->
            <div class="avatar">
              {{ message.senderId === currentUser?.userId ? '我' : 'Ta' }}
            </div>

            <div class="message-content-wrapper">
              <div class="message-bubble">
                <p class="text">{{ message.content }}</p>
                <div class="meta-info">
                  <span class="time">{{ formatMessageTime(message.sendTime) }}</span>
                  <!-- 已读未读勾勾 (仅发送方显示，模拟) -->
                  <span v-if="message.senderId === currentUser?.userId" class="check-icon">✓</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 输入区域 -->
      <div class="chat-input-area">
        <form @submit.prevent="sendMessage" class="input-box">
          <input
              type="text"
              v-model="newMessageContent"
              placeholder="发送消息..."
              class="message-input"
              :disabled="!currentSession"
          />
          <button type="submit" class="send-btn" :disabled="!newMessageContent.trim() || !currentSession">
            <svg viewBox="0 0 24 24" width="20" height="20" stroke="currentColor" stroke-width="2" fill="none" stroke-linecap="round" stroke-linejoin="round"><line x1="22" y1="2" x2="11" y2="13"></line><polygon points="22 2 15 22 11 13 2 9 22 2"></polygon></svg>
          </button>
        </form>
      </div>

    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick, watch } from 'vue';
import { useRoute } from 'vue-router';
import axios from '../utils/axios';
import { useAuthStore } from '@/stores/authStore';
import { storeToRefs } from 'pinia';
import router from "@/router/index.js";

// --- Reactive State ---
const route = useRoute();
const receiverId = ref(null);
const currentSession = ref(null);
const messages = ref([]);
const newMessageContent = ref('');
const isLoading = ref(true);
const messageContainer = ref(null);

let eventSource = null;

const authStore = useAuthStore();
const { userInfo: currentUser, isLoggedIn, token } = storeToRefs(authStore);

// --- Lifecycle ---
onMounted(() => {
  if (!isLoggedIn.value) {
    alert('请先登录后再进行聊天！');
    router.push('/login');
    return;
  }
  receiverId.value = parseInt(route.params.receiverId, 10);
  if (receiverId.value) {
    initializeChat(receiverId.value);
    setupSseConnection();
  }
});

onUnmounted(() => {
  if (eventSource) eventSource.close();
});

watch(
    () => route.params.receiverId,
    (newId) => {
      if (newId && parseInt(newId, 10) !== receiverId.value) {
        receiverId.value = parseInt(newId, 10);
        messages.value = [];
        currentSession.value = null;
        initializeChat(receiverId.value);
      }
    }
);

// --- Methods ---
async function initializeChat(peerId) {
  isLoading.value = true;
  try {
    const sessionRes = await axios.post(`/chat/session/${peerId}`);
    currentSession.value = sessionRes.data;

    if (currentSession.value && currentSession.value.sessionId) {
      const messagesRes = await axios.get(`/chat/messages/${currentSession.value.sessionId}`);
      messages.value = messagesRes.data.sort((a, b) => new Date(a.sendTime) - new Date(b.sendTime));
    }
  } catch (error) {
    console.error('初始化失败:', error);
  } finally {
    isLoading.value = false;
    await scrollToBottom();
  }
}

async function sendMessage() {
  if (!newMessageContent.value.trim() || !currentSession.value) return;
  const messageData = {
    sessionId: currentSession.value.sessionId,
    content: newMessageContent.value,
    msgType: 'text',
  };
  try {
    await axios.post('/chat/messages', messageData);
    newMessageContent.value = '';
  } catch (error) {
    console.error('发送失败:', error);
  }
}

function setupSseConnection() {
  if (!token.value) return;
  const url = `/api/chat/stream?token=${token.value}`;
  eventSource = new EventSource(url);
  eventSource.onmessage = async (event) => {
    const newMessage = JSON.parse(event.data);
    if (currentSession.value && newMessage.sessionId === currentSession.value.sessionId) {
      messages.value.push(newMessage);
      await scrollToBottom();
    }
  };
  eventSource.onerror = () => eventSource.close();
}

function formatMessageTime(dateTimeStr) {
  if (!dateTimeStr) return '';
  
  const date = new Date(dateTimeStr);
  const now = new Date();
  
  // 获取时间部分 HH:mm
  const timePart = `${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`;
  
  // 判断是否是今天
  const isToday = date.getDate() === now.getDate() &&
                  date.getMonth() === now.getMonth() &&
                  date.getFullYear() === now.getFullYear();
                  
  if (isToday) {
    return timePart;
  }

  // 判断是否是昨天
  const yesterday = new Date(now);
  yesterday.setDate(now.getDate() - 1);
  const isYesterday = date.getDate() === yesterday.getDate() &&
                      date.getMonth() === yesterday.getMonth() &&
                      date.getFullYear() === yesterday.getFullYear();
                      
  if (isYesterday) {
    return `昨天 ${timePart}`;
  }

  // 获取日期部分
  const month = (date.getMonth() + 1).toString().padStart(2, '0');
  const day = date.getDate().toString().padStart(2, '0');

  // 判断是否是今年 (如果是今年，省略年份)
  if (date.getFullYear() === now.getFullYear()) {
    return `${month}-${day} ${timePart}`;
  }

  // 跨年了，显示完整日期
  return `${date.getFullYear()}-${month}-${day} ${timePart}`;
}


async function scrollToBottom() {
  await nextTick();
  if (messageContainer.value) {
    messageContainer.value.scrollTop = messageContainer.value.scrollHeight;
  }
}

function goBack() {
  router.back();
}
</script>

<style scoped>
/* ================== 全局布局 ================== */
.chat-wrapper {
  display: flex;
  justify-content: center;
  align-items: center;
  height: calc(100vh - 80px); /* 减去顶部导航高度 */
  width: 100%;
  background-color: #f0f9f0;
  padding: 20px;
  height: 100%; 
  min-height: calc(100vh - 80px);
  justify-content: center; 
   position: relative; 
}

.chat-container {
  width: 100%;
  max-width: 900px; /* 限制最大宽度，大屏更好看 */
  height: 100%;
  background-color: #ffffff;
  border-radius: 20px;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.08);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  position: relative;
}

/* ================== 头部 ================== */
.chat-header {
  height: 64px;
  padding: 0 20px;
  background-color: #ffffff;
  border-bottom: 1px solid #f0f0f0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  z-index: 10;
}

.header-info {
  flex: 1;                    /* 占满剩余宽度 */
  display: flex;              /* 让内部元素居中 */
  justify-content: center;    /* 水平居中 */
  align-items: center;        /* 垂直居中 */
  text-align: center;         /* 备用，防止文字折行 */
}

.header-info h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 700;
  color: #1a1a1a;
}

.status-dot {
  font-size: 12px;
  color: #2D7D4F;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
}

.status-dot::before {
  content: '';
  display: block;
  width: 8px;
  height: 8px;
  background-color: #2D7D4F;
  border-radius: 50%;
}

.icon-btn {
  background: none;
  border: none;
  cursor: pointer;
  color: #666;
  padding: 8px;
  border-radius: 50%;
  transition: background 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
}

.icon-btn:hover {
  background-color: #f5f5f5;
  color: #333;
}

/* ================== 消息列表 ================== */
.messages {
  flex: 1;
  background-color: #f9f9f9; /* 消息区浅灰底色 */
  /* 可选：加个背景纹理 */
  /* background-image: radial-gradient(#e1e1e1 1px, transparent 1px); background-size: 20px 20px; */
  padding: 20px;
  overflow-y: auto;
  scroll-behavior: smooth;
}

/* 滚动条美化 */
.messages::-webkit-scrollbar { width: 6px; }
.messages::-webkit-scrollbar-thumb { background-color: #d1d5db; border-radius: 3px; }
.messages::-webkit-scrollbar-track { background: transparent; }

/* 状态提示 */
.state-text {
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  color: #9ca3af;
  font-size: 14px;
}
.spinner {
  width: 24px;
  height: 24px;
  border: 3px solid #e5e7eb;
  border-top-color: #2D7D4F;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 10px;
}
@keyframes spin { to { transform: rotate(360deg); } }

/* ================== 消息气泡组 ================== */
.message-group {
  display: flex;
  flex-direction: column;
  gap: 16px; /* 消息间距 */
}

.message-row {
  display: flex;
  align-items: flex-end; /* 底部对齐，配合头像 */
  gap: 10px;
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn { from { opacity: 0; transform: translateY(10px); } to { opacity: 1; transform: translateY(0); } }

/* 头像样式 */
.avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background-color: #e5e7eb;
  color: #6b7280;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: bold;
  flex-shrink: 0;
  border: 2px solid #fff;
  box-shadow: 0 2px 4px rgba(0,0,0,0.05);
}

/* 我发的消息 - 右侧 */
.row-sent {
  flex-direction: row-reverse;
}
.row-sent .avatar {
  background-color: #2D7D4F;
  color: #fff;
}

/* 接收的消息 - 左侧 */
.row-received .avatar {
  background-color: #fff; /* 或者根据用户ID生成颜色 */
}

/* 气泡包裹 */
.message-content-wrapper {
  max-width: 65%;
}

.message-bubble {
  position: relative;
  padding: 12px 16px;
  font-size: 15px;
  line-height: 1.5;
  box-shadow: 0 1px 2px rgba(0,0,0,0.05);
  word-wrap: break-word;
}

/* 发送方气泡样式 */
.row-sent .message-bubble {
  background-color: #2D7D4F; /* 主题绿 */
  color: #fff;
  /* 只有左上、左下、右上圆角，右下角尖一点 */
  border-radius: 18px 18px 4px 18px; 
}

/* ⚪ 接收方气泡样式 */
.row-received .message-bubble {
  background-color: #ffffff;
  color: #1f2937;
  border: 1px solid #f0f0f0;
  /* 只有右上、右下、左下圆角，左下角尖一点 */
  border-radius: 18px 18px 18px 4px;
}

/* Meta 信息 (时间 + 已读) */
.meta-info {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 4px;
  margin-top: 4px;
  opacity: 0.8;
}

.time {
  font-size: 10px;
}

.row-sent .time { color: rgba(255,255,255,0.8); }
.row-received .time { color: #9ca3af; }

.check-icon {
  font-size: 10px;
  font-weight: bold;
}

/* ================== 输入区域 ================== */
.chat-input-area {
  background-color: #ffffff;
  padding: 16px 20px;
  border-top: 1px solid #f0f0f0;
}

.input-box {
  display: flex;
  align-items: center;
  background-color: #f3f4f6;
  border-radius: 24px;
  padding: 6px 6px 6px 16px; /* 右边padding留给按钮 */
  border: 1px solid transparent;
  transition: all 0.2s;
}

.input-box:focus-within {
  background-color: #fff;
  border-color: #2D7D4F;
  box-shadow: 0 0 0 3px rgba(45, 125, 79, 0.1);
}

.message-input {
  flex: 1;
  border: none;
  background: transparent;
  outline: none;
  font-size: 15px;
  color: #333;
  padding: 8px 0;
}

.message-input::placeholder {
  color: #9ca3af;
}

.send-btn {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  border: none;
  background-color: #2D7D4F;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s;
  flex-shrink: 0;
  margin-left: 8px;
}

.send-btn:hover:not(:disabled) {
  background-color: #246640;
  transform: scale(1.05);
}

.send-btn:disabled {
  background-color: #d1d5db;
  cursor: default;
  transform: none;
}
</style>