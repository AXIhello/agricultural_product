<template>
  <div class="main-bg">
    <HeaderComponent />
    <div class="chat-layout">

      <!-- 左侧：会话列表 -->
      <aside class="chat-sidebar">
        <div class="sidebar-header">
          <h2>聊天列表</h2>
        </div>

        <div class="session-list-wrapper">
          <div v-if="isLoading" class="status-indicator">
            正在加载消息...
          </div>

          <div v-else-if="sessions.length === 0" class="status-indicator">
            您还没有任何消息
          </div>

          <ul v-else class="session-list">
            <li
                v-for="session in sessions"
                :key="session.sessionId"
                class="session-item"
                :class="{ active: activePeerId === getPeerUser(session).id }"
                @click="activePeerId = getPeerUser(session).id"
            >
              <div class="avatar-placeholder">
                <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24"
                     fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
                  <circle cx="12" cy="7" r="4"/>
                </svg>
              </div>

              <div class="session-details">
                <div class="session-header">
                <span class="peer-user-id">
                  用户 {{ getPeerUser(session).id }}
                </span>
                  <span class="last-message-time">
                  {{ formatTime(session.lastMessageTime) }}
                </span>
                </div>

                <div class="last-message-preview">
                  点击查看对话
                </div>
              </div>
            </li>
          </ul>
        </div>
      </aside>

      <!-- 右侧：聊天窗口 -->
      <main class="chat-content">
        <ChatWindow v-if="activePeerId" :receiver-id="activePeerId" />
        <div v-else class="empty-chat-hint">
          ← 请选择左侧联系人开始聊天
        </div>
      </main>

    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import axios from "../utils/axios";
import { useAuthStore } from "@/stores/authStore";
import { storeToRefs } from 'pinia';
import ChatWindow from "@/components/ChatWindow.vue";
import HeaderComponent from "@/components/HeaderComponent.vue";

const authStore = useAuthStore();
const { userInfo, isLoggedIn } = storeToRefs(authStore);

// 状态
const sessions = ref([]);
const isLoading = ref(true);
const activePeerId = ref(null); // 右侧聊天窗口的对象

onMounted(() => {
  if (!isLoggedIn.value) return;
  loadSessions();
});

/** 加载会话列表 */
async function loadSessions() {
  try {
    const response = await axios.get("/chat/sessions");
    sessions.value = (response.data || []).sort(
        (a, b) => new Date(b.lastMessageTime) - new Date(a.lastMessageTime)
    );
  } finally {
    isLoading.value = false;
  }
}

/** 获取对方用户ID */
function getPeerUser(session) {
  if (!userInfo.value?.userId) return { id: "未知" };
  const peer =
      session.userAId === userInfo.value.userId ? session.userBId : session.userAId;
  return { id: peer };
}

/** 格式化时间 */
function formatTime(dateTimeStr) {
  if (!dateTimeStr) return "";
  const date = new Date(dateTimeStr);
  return date.toLocaleDateString();
}
</script>

<style scoped>
.chat-layout {
  display: flex;
  height: calc(100vh - 60px);
  background: #f7f7f7;
}

/* 左侧联系人栏 */
.chat-sidebar {
  width: 280px;
  background: #fff;
  border-right: 1px solid #e2e2e2;
  overflow-y: auto;
}

.sidebar-header {
  padding: 1rem;
  font-size: 1.2rem;
  font-weight: bold;
  border-bottom: 1px solid #eee;
}

/* 右侧聊天区 */
.chat-content {
  flex: 1;
  background: #fefefe;
  position: relative;
}

.empty-chat-hint {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #aaa;
  font-size: 1.1rem;
}

/* --- 左侧会话列表样式保留你的原样 + active高亮 --- */

.session-item {
  display: flex;
  padding: 0.8rem 1rem;
  cursor: pointer;
  border-bottom: 1px solid #f3f3f3;
  transition: background 0.2s;
}

.session-item:hover {
  background: #f5f5f5;
}

.session-item.active {
  background: #e8f3ff;
}

.avatar-placeholder {
  width: 42px;
  height: 42px;
  border-radius: 50%;
  background: #e9ecef;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 1rem;
}

.session-details {
  flex: 1;
}

.session-header {
  display: flex;
}

.peer-user-id {
  font-weight: 600;
}

.last-message-time {
  margin-left: auto;
  font-size: 0.8rem;
  color: #888;
}

.status-indicator {
  padding: 2rem;
  text-align: center;
}
</style>
