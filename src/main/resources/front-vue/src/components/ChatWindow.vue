<template>
  <div class="chat-wrapper">
    <div class="chat-container">

      <button v-if="props.showBackButton" @click="goBack" class="back-button">
        <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <line x1="19" y1="12" x2="5" y2="12"></line>
          <polyline points="12 19 5 12 12 5"></polyline>
        </svg>
        返回
      </button>

      <!-- 头部 -->
      <div class="chat-header" :class="headerPaddingClass">
        <div class="header-info">
          <h3>
            {{ receiverInfo?.name || `用户 ${internalReceiverId}` }}
            <span v-if="formattedReceiverRole" class="receiver-role">
              ({{ formattedReceiverRole }}) 
            </span>
          </h3>
        </div>
      </div>

      <!-- 消息列表 -->
      <div class="messages" ref="messageContainer">
        <div v-if="isLoading" class="state-text">
          <div class="spinner"></div> 正在加载消息...
        </div>

        <div v-else-if="messages.length === 0" class="state-text empty">
          <img src="https://cdn-icons-png.flaticon.com/512/2665/2665038.png" alt="Empty" width="60" style="opacity: 0.5; margin-bottom: 10px;">
          <p>还没有消息，打个招呼吧</p>
        </div>

        <div v-else class="message-group">
          <div
              v-for="(message, index) in messages"
              :key="message.messageId"
              :class="['message-row', message.senderId === currentUser?.userId ? 'row-sent' : 'row-received']"
          >
            <div class="avatar">
              {{ message.senderId === currentUser?.userId ? '我' : 'Ta' }}
            </div>

            <div class="message-content-wrapper">
              <div class="message-bubble">
                <p class="text">{{ message.content }}</p>
                <div class="meta-info">
                  <span class="time">{{ formatMessageTime(message.sendTime) }}</span>
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

          <button type="submit" class="send-btn"
                  :disabled="!newMessageContent.trim() || !currentSession">
            <svg viewBox="0 0 24 24" width="20" height="20"
                 stroke="currentColor" stroke-width="2" fill="none"
                 stroke-linecap="round" stroke-linejoin="round">
              <line x1="22" y1="2" x2="11" y2="13"></line>
              <polygon points="22 2 15 22 11 13 2 9 22 2"></polygon>
            </svg>
          </button>
        </form>
      </div>

    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick, watch, computed } from 'vue';
import { useRoute } from 'vue-router';
import axios from '../utils/axios.js';
import { useAuthStore } from '@/stores/authStore.js';
import { storeToRefs } from 'pinia';
import router from "@/router/index.js";

// --- Reactive State ---
const route = useRoute();
const internalReceiverId = ref(null);
const currentSession = ref(null);
const messages = ref([]);
const newMessageContent = ref('');
const isLoading = ref(true);
const messageContainer = ref(null);
const receiverInfo = ref(null);//对方用户信息

// ---------- store ----------
const authStore = useAuthStore()
const { userInfo: currentUser, isLoggedIn, token } = storeToRefs(authStore)

// ---------- state ----------
const currentSession = ref(null)
const messages = ref([])
const newMessageContent = ref('')
const isLoading = ref(true)
const messageContainer = ref(null)

// 定义 props
const props = defineProps({
  receiverId: {
    type: Number,
    default: null // 默认值为 null
  },
  showBackButton:{
    type : Boolean,
    default: true
  }
});

//调整头部样式（有无返回按钮
const headerPaddingClass = computed(() => {
  return props.showBackButton ? 'has-back-button-padding' : '';
});

// 优先使用 prop 传入的 receiverId，如果 prop 为空，则尝试从路由参数获取
const getActualReceiverId = () => {
  if (props.receiverId !== null && props.receiverId !== undefined) {
    return props.receiverId;
  }
  const idFromRoute = parseInt(route.params.receiverId, 10);
  return !isNaN(idFromRoute) ? idFromRoute : null;
};

//格式化角色名称
const formattedReceiverRole = computed(() => {
  if (!receiverInfo.value?.role) {
    return ''; // 如果没有角色信息，返回空字符串
  }
  switch (receiverInfo.value.role) {
    case 'buyer':   
      return '普通用户';
    case 'expert':
      return '专家';
    case 'farmer':
      return '农户';
    default:
      return receiverInfo.value.role; 
  }
});

// --- Lifecycle ---
onMounted(async () => {
  if (!isLoggedIn.value) {
    alert('请先登录后再进行聊天！')
    router.push('/login')
    return
  }

  internalReceiverId.value = getActualReceiverId(); // 获取实际的 receiverId

  console.log('ChatWindow mounted. Actual receiverId:', internalReceiverId.value);

  if (internalReceiverId.value) {
    await fetchReceiverInfo(internalReceiverId.value); 
    await nextTick(); 
    initializeChat(internalReceiverId.value);
    setupSseConnection();
  } else {
    // 如果没有 receiverId，直接结束加载状态，显示空消息提示
    isLoading.value = false;
    console.warn('ChatWindow: No valid receiverId found from props or route params.');
    // 可以在这里添加一个用户友好的提示，例如 ElMessage.warning('请选择一个联系人开始聊天。');
  }
});

onUnmounted(() => {
  if (eventSource) {
    eventSource.close();
    eventSource = null; // 清除引用
  }
});

// 当左侧切换联系人时
watch(
    () => [props.receiverId, route.params.receiverId], // 监听两个可能来源
    async ([newPropId, newRouteId], [oldPropId, oldRouteId]) => {
      const newActualId = getActualReceiverId(); // 获取新的实际ID

      // 只有当实际的 receiverId 发生变化时才重新初始化
      if (newActualId !== null && !isNaN(newActualId) && newActualId !== internalReceiverId.value) {
        console.log(`ReceiverId changed from ${internalReceiverId.value} to ${newActualId}. Reinitializing chat.`);
        internalReceiverId.value = newActualId;
        messages.value = [];
        currentSession.value = null;
        receiverInfo.value = null; // 重置对方用户信息

        if (eventSource) { // 如果有旧的 SSE 连接，先关闭
          eventSource.close();
          eventSource = null;
        }

        if (internalReceiverId.value) {
          await fetchReceiverInfo(internalReceiverId.value);
          await nextTick(); // <--- 添加这一行
          initializeChat(internalReceiverId.value);
          setupSseConnection();
        } else {
          isLoading.value = false; // 如果新的ID为空，则停止加载
          console.warn('ChatWindow: New receiverId is invalid after change.');
        }
      }else if (newActualId === null || isNaN(newActualId)) {
        // 如果 newActualId 变为无效值，并且 internalReceiverId 之前是有效的，也重置状态
        if (internalReceiverId.value !== null) {
          console.warn(`Condition met: newActualId (${newActualId}) is invalid. Resetting chat state.`);
          internalReceiverId.value = null;
          messages.value = [];
          currentSession.value = null;
          receiverInfo.value = null;
          isLoading.value = false; // 停止加载
          if (eventSource) {
            eventSource.close();
            eventSource = null;
          }
        }
      } else {
        console.log(`Condition not met: newActualId (${newActualId}) is either null/NaN or same as internalReceiverId (${internalReceiverId.value}). No reinitialization.`);
      }
    },
    { immediate: true } // 确保在组件挂载时也触发一次
);


// ---------- core ----------
async function initForReceiver(peerId) {
  await initializeChat(peerId)
  setupSseConnection()
}

function resetChat() {
  messages.value = []
  currentSession.value = null
  isLoading.value = true
  closeSse()
}

// ---------- api ----------
async function initializeChat(peerId) {
  isLoading.value = true;
  console.log('initializeChat started for peerId:', peerId);

  try {
    const requestBody = {
      currentRole: currentUser.value?.role, // 获取当前用户的角色
      peerRole: receiverInfo.value?.role    // 获取对方用户的角色
    };

    const sessionRes = await axios.post(`/chat/session/${peerId}`, requestBody);
    currentSession.value = sessionRes.data;
    console.log('Session response:', sessionRes.data);

    if (currentSession.value?.sessionId) {
      const msgRes = await axios.get(`/chat/messages/${currentSession.value.sessionId}`);
      messages.value = msgRes.data.sort((a, b) => new Date(a.sendTime) - new Date(b.sendTime));
      console.log('Messages loaded:', messages.value.length); 
    }else {
      console.log('No chat session found or created.'); 
    }

    console.log('消息加载完成，消息数:', messages.value.length)
  } catch (err) {
    console.error('Chat initialization failed:', err.response?.data || err.message || err);
  } finally {
    isLoading.value = false;
    console.log('initializeChat finished. isLoading is now:', isLoading.value);
    await scrollToBottom();
  }
}

async function sendMessage() {
  if (!newMessageContent.value.trim() || !currentSession.value) return

  const payload = {
    sessionId: currentSession.value.sessionId,
    peerUserId: props.receiverId,
    content: newMessageContent.value,
    msgType: 'text'
  }

  try {
    await axios.post('/chat/messages', payload)
    newMessageContent.value = ''
  } catch (err) {
    console.error('发送失败:', err)
  }
}

// ---------- SSE ----------
function setupSseConnection() {
  if (!token.value) {
    console.warn('SSE: No auth token available.');
    return;
  }
  // 如果已经存在 EventSource，先关闭
  if (eventSource) {
    eventSource.close();
    eventSource = null;
  }

  const url = `/api/chat/stream?token=${token.value}`
  eventSource = new EventSource(url)

  eventSource.onmessage = async (event) => {
    try {
      const msg = JSON.parse(event.data);
      // 检查接收到的消息是否属于当前活跃的聊天对象
      // 并且确保是与当前用户的会话
      if (currentSession.value && msg.sessionId === currentSession.value.sessionId) {
        messages.value.push(msg);
        console.log('New message received via SSE:', msg);
        await scrollToBottom();
        // 通知父组件更新会话列表中的最新消息时间
        // emit('messageReceived', msg.sessionId, msg.sendTime);
      }
    } catch (e) {
      console.error('Failed to parse SSE message:', e, event.data);
    }   
  };

  eventSource.onerror = (error) => {
    console.error('SSE Error:', error);
    eventSource.close();
    eventSource = null; // 清除引用
    // 重新连接逻辑可以在这里实现，如果需要的话
  };

  eventSource.onopen = () => {
    console.log('SSE connection opened.');
  };
}

function formatMessageTime(time) {
  if (!time) return ''
  const d = new Date(time)
  const now = new Date()

  const hhmm = `${d.getHours().toString().padStart(2, '0')}:${d
      .getMinutes()
      .toString()
      .padStart(2, '0')}`

  if (d.toDateString() === now.toDateString()) return hhmm

  const yesterday = new Date(now)
  yesterday.setDate(now.getDate() - 1)
  if (d.toDateString() === yesterday.toDateString())
    return `昨天 ${hhmm}`

  const m = (d.getMonth() + 1).toString().padStart(2, '0')
  const day = d.getDate().toString().padStart(2, '0')

  if (d.getFullYear() === now.getFullYear())
    return `${m}-${day} ${hhmm}`

  return `${d.getFullYear()}-${m}-${day} ${hhmm}`
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

// 获取对方用户信息函数
async function fetchReceiverInfo(userId) {
  try {
    const response = await axios.get(`user/info/${userId}`); 
    if (response.data && response.data.success) {
      receiverInfo.value = response.data.user;
      console.log('Receiver info fetched:', receiverInfo.value);
    } else {
      console.warn(`Failed to fetch info for user ID ${userId}:`, response.data?.message);
      receiverInfo.value = { name: `用户 ${userId}`, role: '' }; // 提供一个默认值
    }
  } catch (error) {
    console.error(`Error fetching receiver info for ID ${userId}:`, error);
    receiverInfo.value = { name: `用户 ${userId}`, role: '' }; // 错误时也提供默认值
  }
}

</script>

<style scoped>
/* ================== 全局布局 ================== */
.chat-wrapper {
  display: flex;
  justify-content: center;
  align-items: center;

  width: 100%;
  background-color: #f0f9f0;
  padding: 20px;

  justify-content: center; 
  position: relative; 

  height: 100%; 
  min-height: calc(100vh - 80px);
  height: calc(100vh - 80px); /* 减去顶部导航高度 */
}

.chat-container {
  width: 100%;
  max-width: 1500px; /* 限制最大宽度，大屏更好看 */
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

.chat-header.has-back-button-padding {
  padding-top: 20px; /* 只有当有返回按钮时才增加顶部内边距 */
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
  min-height: 400px; 
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

//聊天框顶部 返回按钮
<style scoped>
.header-info h3 {
  display: flex; /* 让名字和角色在一行并对齐 */
  align-items: center;
  gap: 8px; /* 名字和角色之间的间距 */
  margin: 0;
  font-size: 16px;
  font-weight: 700;
  color: #1a1a1a;
}

.receiver-role {
  font-size: 13px; /* 角色字体小一点 */
  font-weight: 500;
  color: #6c757d; /* 灰色 */
  background-color: #e9ecef; /* 浅背景色 */
  padding: 2px 8px;
  border-radius: 12px;
}


/* ================== 返回按钮样式 ================== */
.back-button {
  position: absolute; /* <--- 绝对定位 */
  top: 15px; /* 距离顶部 */
  left: 20px; /* 距离左侧 */
  z-index: 100; /* 确保在最上层 */
  display: flex;
  align-items: center;
  gap: 5px;
  background: none;
  border: none;
  color: #2D7D4F; /* 主题绿色 */
  font-size: 16px;
  cursor: pointer;
  padding: 8px 12px;
  border-radius: 5px;
  transition: background-color 0.2s, color 0.2s;
}

.back-button:hover {
  background-color: #e6f4ea; /* 浅绿色背景 */
  color: #246640;
}

.back-button svg {
  stroke-width: 2.5;
}


</style>