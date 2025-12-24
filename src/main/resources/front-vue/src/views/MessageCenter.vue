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
                @click="handleSessionClick(session)"
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
        <ChatWindow
            v-if="activePeerId"
            :receiver-id="activePeerId"
            :current-role="activeCurrentRole"
            :peer-role="activePeerRole"
        />

        <div v-else class="empty-chat-hint">
          ← 请选择左侧联系人开始聊天
        </div>
      </main>

    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { useRoute, useRouter } from 'vue-router'
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
const activeCurrentRole = ref(null)
const activePeerRole = ref(null)


const route = useRoute()

onMounted(async () => {
  if (!isLoggedIn.value) return;
  sessions.value = [];
  await loadSessions();

  const peerId = route.query.peerId;
  if (peerId) {
    await openChatWithPeer(
        Number(peerId),
        route.query.currentRole,
        route.query.peerRole
    );
  }
});

function handleSessionClick(session) {
  const currentUserId = userInfo.value.userId

  const isA = session.userAId === currentUserId

  activePeerId.value = Number(
      isA ? session.userBId : session.userAId
  )

  // 同步角色
  activeCurrentRole.value = isA
      ? session.userARole
      : session.userBRole

  activePeerRole.value = isA
      ? session.userBRole
      : session.userARole
}



async function openChatWithPeer(peerId, currentRole, peerRole) {
  console.log('role:',currentRole, peerRole);
  console.log('sessions:',sessions.value);
  //  先查已有 session
  let session = sessions.value.find(s => {
    const aId = Number(s.userAId)
    const bId = Number(s.userBId)
    const me = Number(userInfo.value.userId)
    const peer = Number(peerId)

    console.log('—— checking session ——')
    console.log('sessionId:', s.sessionId)
    console.log('A:', aId, 'B:', bId)
    console.log('me:', me, 'peer:', peer)

    const match =
        (aId === me && bId === peer) ||
        (bId === me && aId === peer)

    console.log('match result:', match)
    console.log('------------------------')

    return match
  })



  // 没有就创建
  if (!session) {
    console.log('no session found');
    const res = await axios.post(`/chat/session/${peerId}`, {
      currentRole,
      peerRole
    });

    session = res.data;
    sessions.value.unshift(session);
  }

  // 设置右侧聊天对象
  activePeerId.value = peerId
  activeCurrentRole.value = currentRole
  activePeerRole.value = peerRole

}


/** 加载会话列表 */
async function loadSessions() {
  try {
    const response = await axios.get("/chat/sessions")

    console.log('加载出来的response:', response.data);
    // 按 sessionId 去重
    const map = new Map()
    ;(response.data || []).forEach(s => {
      map.set(s.sessionId, s)
    })

    sessions.value = Array.from(map.values()).sort(
        (a, b) => new Date(b.lastMessageTime) - new Date(a.lastMessageTime)
    )

    console.log('去重后的 sessions:', sessions.value)
  } finally {
    isLoading.value = false
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
