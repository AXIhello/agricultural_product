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
                <span class="peer-user-name">
                  {{ session.peerUserInfo?.name || `用户 ${getPeerUser(session).id}` }}
                  <span v-if="session.peerUserInfo?.formattedRole" class="peer-user-role">
                    ({{ session.peerUserInfo.formattedRole }})
                  </span>
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
        <ChatWindow v-if="activePeerId" :receiver-id="activePeerId" :showBackButton="false"/>
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
  isLoading.value = true;
  try {
    const response = await axios.get("/chat/sessions");
    let fetchedSessions = response.data || [];

    if (!Array.isArray(fetchedSessions)) {
      console.warn("API returned non-array data for chat sessions:", fetchedSessions);
      fetchedSessions = [];
    }

     // 批量获取所有会话中 peer 用户的详细信息
    const peerUserIds = fetchedSessions.length > 0
        ? fetchedSessions.map(session => getPeerUser(session).id)
        : [];
    const uniquePeerUserIds = [...new Set(peerUserIds)];

    const userPromises = uniquePeerUserIds.map(userId => fetchUserInfo(userId));
    const usersInfo = await Promise.all(userPromises);

    const userInfoMap = new Map();
    usersInfo.forEach(user => {
      if (user) userInfoMap.set(user.userId, user);
    });

    sessions.value = fetchedSessions
        .map(session => {
          const peerId = getPeerUser(session).id;
          const peerInfo = userInfoMap.get(peerId);
          return {
            ...session,
            peerUserInfo: peerInfo ? {
              name: peerInfo.name,
              role: peerInfo.role,
              formattedRole: formatUserRole(peerInfo.role)
            } : null
          };
        })
        .sort((a, b) => new Date(b.lastMessageTime) - new Date(a.lastMessageTime));

  } catch (error) {
    console.error('Failed to load chat sessions or user info:', error);
  }finally {
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

/** 获取单个用户详细信息 */
async function fetchUserInfo(userId) {
  try {
    const response = await axios.get(`/user/info/${userId}`);
    if (response.data && response.data.success) {
      return response.data.user;
    } else {
      console.warn(`Failed to fetch info for user ID ${userId}:`, response.data?.message);
      return null;
    }
  } catch (error) {
    console.error(`Error fetching user info for ID ${userId}:`, error);
    return null;
  }
}

/** 格式化用户角色 */
function formatUserRole(role) {
  switch (role) {
    case 'buyer':
      return '普通用户';
    case 'expert':
      return '专家';
    case 'farmer':
      return '农户';
    default:
      return role;
  }
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
  align-items: center; /* 垂直居中对齐 */
  justify-content: space-between; /* 将时间和名字角色分开 */
}

.peer-user-id {
  font-weight: 600;
  display: flex; /* 让名字和角色在同一行 */
  align-items: center;
  gap: 5px; /* 名字和角色之间的间距 */
  font-size: 0.95rem; /* 调整字体大小 */
}

.peer-user-role {
  font-size: 0.75rem; /* 角色字体小一点 */
  font-weight: 500;
  color: #6c757d; /* 灰色 */
  background-color: #e9ecef; /* 浅背景色 */
  padding: 2px 6px;
  border-radius: 10px;
  white-space: nowrap; /* 防止角色名称换行 */
}

.last-message-time {
  margin-left: auto;
  font-size: 0.8rem;
  color: #888;
  white-space: nowrap; /* 防止时间换行 */
}

.last-message-preview {
  font-size: 0.85rem;
  color: #666;
  margin-top: 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.status-indicator {
  padding: 2rem;
  text-align: center;
  color: #888;
}
</style>


