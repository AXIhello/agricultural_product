<template>
  <div class="chat-container">
    <div class="chat-window">
      <div class="chat-header">
        <h3>正在与 用户 {{ receiverId }} 聊天</h3>
        <button @click="goBack()" class="back-btn">返回</button>
      </div>

      <div class="messages" ref="messageContainer">
        <div v-if="isLoading" class="loading-state">正在加载消息...</div>
        <div v-else-if="messages.length === 0" class="empty-state">还没有消息，开始聊天吧！</div>

        <div v-else v-for="message in messages" :key="message.messageId"
             :class="['message-item', message.senderId === currentUser?.userId ? 'sent' : 'received']">
          <div class="message-bubble">
            <p class="message-content">{{ message.content }}</p>
            <span class="message-time">{{ formatMessageTime(message.sendTime) }}</span>
            <div class="bubble-tail"></div>
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

<style scoped>
/* ================== 容器 ================== */
.chat-container {
  display: flex;
  height: calc(100vh - 100px);
  width: 100%;
  background: linear-gradient(to bottom, #e6f0e6, #f5f5f5);
  border-radius: 24px;
  overflow: hidden;
  box-shadow: 0 12px 28px rgba(0,0,0,0.12);
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
}

/* ================== 聊天窗口 ================== */
.chat-window {
  flex-grow: 1;
  display: flex;
  flex-direction: column;
}

/* ------------------ 头部 ------------------ */
.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 18px 24px;
  border-bottom: 1px solid #e0e0e0;
  background-color: #ffffff;
  box-shadow: 0 1px 4px rgba(0,0,0,0.08);
}

.chat-header h3 {
  margin: 0;
  font-size: 1.25rem;
  color: #333;
  font-weight: 600;
}

.back-btn {
  background: none;
  border: none;
  color: #2D7D4F;
  font-weight: bold;
  cursor: pointer;
}
.back-btn:hover { color: #1f5233; }

/* ------------------ 消息区 ------------------ */
.messages {
  flex-grow: 1;
  padding: 20px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 14px;
  scroll-behavior: smooth;
}

.messages::-webkit-scrollbar {
  width: 6px;
}
.messages::-webkit-scrollbar-thumb {
  background-color: rgba(45, 125, 79, 0.3);
  border-radius: 3px;
}
.messages::-webkit-scrollbar-track { background: transparent; }

.message-item {
  display: flex;
  max-width: 75%;
  position: relative;
  animation: slideUp 0.25s ease-out;
}

@keyframes slideUp { from { opacity:0; transform: translateY(8px);} to {opacity:1; transform: translateY(0);} }

/* ------------------ 气泡 ------------------ */
.message-bubble {
  position: relative;
  padding: 14px 18px;
  border-radius: 20px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.08);
  word-wrap: break-word;
  font-size: 0.95rem;
  line-height: 1.4;
}

.message-time {
  display: block;
  font-size: 0.68rem;
  color: #888;
  margin-top: 4px;
  text-align: right;
  opacity: 0.8;
}

/* 气泡尾巴 */
.bubble-tail {
  position: absolute;
  width: 12px;
  height: 12px;
  bottom: 0;
  background: inherit;
  clip-path: polygon(0 0, 100% 0, 0 100%);
}

/* 自己发送 */
.message-item.sent {
  align-self: flex-end;
}
.message-item.sent .message-bubble {
  background: linear-gradient(145deg, #D4F8C6, #B8E6A0);
  border-bottom-right-radius: 4px;
}
.message-item.sent .bubble-tail {
  right: -6px;
  transform: rotate(90deg);
}
.message-item.sent .message-time { color: #4f7a43; }

/* 接收消息 */
.message-item.received {
  align-self: flex-start;
}
.message-item.received .message-bubble {
  background: #ffffff;
  border-bottom-left-radius: 4px;
}
.message-item.received .bubble-tail {
  left: -6px;
  transform: rotate(-90deg);
}

/* ------------------ 输入区 ------------------ */
.chat-input-area {
  padding: 16px 24px;
  border-top: 1px solid #e0e0e0;
  background-color: #f9f9f9;
}

.message-form {
  display: flex;
  gap: 12px;
}

.message-input {
  flex-grow: 1;
  padding: 14px 18px;
  border: 1px solid #ccc;
  border-radius: 24px;
  font-size: 1rem;
  outline: none;
  transition: all 0.2s;
}
.message-input:focus {
  border-color: #2D7D4F;
  box-shadow: 0 0 12px rgba(45,125,79,0.25);
}

.send-button {
  padding: 12px 24px;
  border: none;
  border-radius: 24px;
  background: linear-gradient(135deg,#2D7D4F,#1F5233);
  color: #fff;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}
.send-button:hover {
  background: linear-gradient(135deg,#3da86c,#2d7d4f);
  box-shadow: 0 4px 14px rgba(45,125,79,0.3);
}
.send-button:disabled {
  background: #a5d6a7;
  cursor: not-allowed;
  box-shadow: none;
}
</style>


<script setup>
import { ref, onMounted, onUnmounted, nextTick, watch } from 'vue';
import { useRoute } from 'vue-router';
import axios from '../utils/axios';
import { useAuthStore } from '@/stores/authStore';
import { storeToRefs } from 'pinia';
import router from "@/router/index.js";

// --- Reactive State ---
const route = useRoute();
const receiverId = ref(null);        // 聊天对象ID
const currentSession = ref(null);    // 当前会话
const messages = ref([]);            // 消息列表
const newMessageContent = ref('');   // 输入框内容
const isLoading = ref(true);         // 加载状态
const messageContainer = ref(null);  // DOM引用，用于滚动

// --- SSE 连接对象 ---
let eventSource = null;

const authStore = useAuthStore();
const { userInfo: currentUser, isLoggedIn, token } = storeToRefs(authStore);

// --- Lifecycle Hooks ---
onMounted(() => {
  if (!isLoggedIn.value) {
    alert('请先登录后再进行聊天！');
    router.push('/login');
    return;
  }

  // 初始化聊天
  receiverId.value = parseInt(route.params.receiverId, 10);
  if (receiverId.value) {
    initializeChat(receiverId.value);
    setupSseConnection();
  }
});

onUnmounted(() => {
  if (eventSource) {
    eventSource.close();
    console.log('SSE connection closed.');
  }
});

// 监听路由变化，切换聊天对象
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

// ------------------- Methods -------------------

/**
 * 初始化聊天
 */
async function initializeChat(peerId) {
  isLoading.value = true;
  try {
    const sessionRes = await axios.post(`/chat/session/${peerId}`);
    currentSession.value = sessionRes.data;

    if (currentSession.value && currentSession.value.sessionId) {
      const messagesRes = await axios.get(`/chat/messages/${currentSession.value.sessionId}`);

      // 按时间升序（早 -> 晚）
      messages.value = messagesRes.data.sort((a, b) => new Date(a.sendTime) - new Date(b.sendTime));
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
 * 发送消息
 */
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
    // 不手动 push，新消息通过 SSE 推送
  } catch (error) {
    console.error('发送消息失败:', error);
    alert('消息发送失败！');
  }
}

/**
 * SSE 实时消息
 */
function setupSseConnection() {
  if (!token.value) return;

  const url = `/api/chat/stream?token=${token.value}`;
  eventSource = new EventSource(url);

  eventSource.onopen = () => console.log('SSE connection established.');

  eventSource.onmessage = async (event) => {
    const newMessage = JSON.parse(event.data);
    if (currentSession.value && newMessage.sessionId === currentSession.value.sessionId) {
      // 最新消息追加到末尾
      messages.value.push(newMessage);
      await scrollToBottom();
    }
  };

  eventSource.onerror = (error) => {
    console.error('SSE Error:', error);
    eventSource.close();
    // 可加重连逻辑
  };
}

/**
 * 格式化消息时间
 */
function formatMessageTime(dateTimeStr) {
  if (!dateTimeStr) return '';
  const messageDate = new Date(dateTimeStr);
  const now = new Date();
  const todayStart = new Date(now.getFullYear(), now.getMonth(), now.getDate());
  const yesterdayStart = new Date(todayStart.getTime() - 86400000);
  const thisYearStart = new Date(now.getFullYear(), 0, 1);

  const fmtTime = (d) => `${d.getHours().toString().padStart(2,'0')}:${d.getMinutes().toString().padStart(2,'0')}`;

  if (messageDate >= todayStart) return fmtTime(messageDate);
  if (messageDate >= yesterdayStart) return `昨天 ${fmtTime(messageDate)}`;
  if (messageDate >= thisYearStart) return `${(messageDate.getMonth()+1).toString().padStart(2,'0')}-${messageDate.getDate().toString().padStart(2,'0')} ${fmtTime(messageDate)}`;
  return `${messageDate.getFullYear()}-${(messageDate.getMonth()+1).toString().padStart(2,'0')}-${messageDate.getDate().toString().padStart(2,'0')} ${fmtTime(messageDate)}`;
}

/**
 * 滚动到底部
 */
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

