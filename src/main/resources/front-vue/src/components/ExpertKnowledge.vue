<template>
  <div class="expert-qa-container">
    <!-- 加载状态 -->
    <div v-if="loading" class="empty-tip">加载中...</div>
    <div v-else-if="knowledgeList.length === 0" class="empty-tip">
      暂无专业知识
    </div>

    <!-- 知识卡片 -->
    <div
        class="qa-item"
        v-for="item in knowledgeList"
        :key="item.knowledgeId"
        @click="openDetail(item)"
    >
      <h4 class="qa-question">{{ item.title }}</h4>

      <p class="qa-answer">{{ item.summary }}</p>

      <!-- 卡片底部信息 -->
      <div class="qa-footer">
        <span class="specialization">{{ item.specialization || '未标注领域' }}</span>
        <span class="userName">作者: {{ item.userName || '未知' }}</span>
        <span class="time">发布时间: {{ formatTime(item.createTime) || '未知时间' }}</span>
      </div>

      <!-- 右侧操作按钮 -->
      <div
          v-if="role === 'expert'"
          class="qa-actions"
          @click.stop
      >
        <button class="edit-btn" @click="emitEdit(item)">编辑</button>
        <button class="delete-btn" @click="emitDelete(item)">删除</button>
      </div>
    </div>


    <!-- ================= 弹窗（知识详情） ================= -->
    <teleport to="body">
      <div v-if="showDetail" class="news-modal-overlay" @click.self="showDetail = false">
        <div class="news-modal-container">

          <button class="close-btn" @click="showDetail = false">×</button>

          <h1 class="news-title">{{ selectedKnowledge?.title || '暂无标题' }}</h1>

          <div class="news-footer">
            <span>{{ selectedKnowledge?.userName || '未知发布人' }}</span>
            <span v-if="selectedKnowledge?.specialization" class="news-specialization">
              {{ selectedKnowledge.specialization }}
            </span>
            <span>{{ formatTime(selectedKnowledge?.createTime) }}</span>
          </div>

          <div class="news-content">
            <div v-if="selectedKnowledge?.content">
              <p v-for="(para, idx) in splitParagraphs(selectedKnowledge.content)" :key="idx">
                {{ para }}
              </p>
            </div>
            <p v-else>暂无内容</p>
          </div>


        </div>
      </div>
    </teleport>



  </div>
</template>

<script setup>
import { ref, onMounted, defineExpose } from 'vue';
import axios from '@/utils/axios';
import { storeToRefs } from 'pinia';
import { useAuthStore } from '@/stores/authStore';
const authStore = useAuthStore();
const { role } = storeToRefs(authStore);

const emit = defineEmits(['edit', 'delete'])

const emitEdit = (item) => {
  emit('edit', item)
}

const emitDelete = (item) => {
  emit('delete', item)
}


const knowledgeList = ref([]);
const loading = ref(false);

/** 拆分段落 */
const splitParagraphs = (text) => {
  if (!text) return [];
  return text.split(/\r?\n/).filter(line => line.trim() !== '');
};

/** 打开详情弹窗 */
const showDetail = ref(false);
const selectedKnowledge = ref(null);
const openDetail = (item) => {
  selectedKnowledge.value = item;
  showDetail.value = true;
};

/** 文本截断（≈100字） */
const truncateText = (text, maxLen) => {
  if (!text) return '';
  return text.length > maxLen ? text.slice(0, maxLen) + '...' : text;
};

/** 时间格式化 */
function formatTime(time) {
  return time ? time.replace('T', ' ') : '';
}

/** 获取知识列表并附加专家信息 */
const fetchKnowledge = async () => {
  loading.value = true;
  try {
    const { data } = await axios.get('/knowledge/list?pageNum=1&pageSize=10');
    const records = data.records || [];

    // 按 ID 降序，保证最新知识在最前
    records.sort((a, b) => b.knowledgeId - a.knowledgeId);

    knowledgeList.value = await Promise.all(
        records.map(async (it) => {
          const summaryText = truncateText(it.summary || it.content || '暂无内容', 100);

          // 获取专家档案
          let profile = {};
          if (it.expertId) {
            try {
              const { data: profileData } = await axios.get(`/expert/profile/${it.expertId}`);
              if (profileData.success) profile = profileData.data;
            } catch (err) {
              console.warn('获取专家档案失败', it.expertId);
            }
          }

          // 获取用户信息
          let userInfo = {};
          if (it.expertId) {
            try {
              const { data: userData } = await axios.get(`/user/info/${it.expertId}`);
              if (userData.success) userInfo = userData.user;
            } catch (err) {
              console.warn('获取用户信息失败', it.expertId);
            }
          }

          return {
            ...it,
            summary: summaryText,
            specialization: profile.specialization,
            userName: userInfo.userName
          };
        })
    );
  } catch (err) {
    console.error('获取专业知识失败:', err);
  } finally {
    loading.value = false;
  }
};

// 在 setup 中暴露 fetchKnowledge 方法，父组件可以调用刷新列表
defineExpose({
  fetchKnowledge
});

onMounted(fetchKnowledge);

</script>




<style scoped>
.expert-qa-container {
  padding: 18px;
}

.empty-tip {
  text-align: center;
  color: #999;
  padding: 30px 0;
}

.qa-item {
  padding: 18px 20px;
  background-color: #f8fbf8;
  border: 1px solid #d9e8df;
  border-radius: 12px;
  margin-bottom: 16px;
  box-shadow: 0 3px 8px rgba(0, 0, 0, 0.06);
  transition: all 0.25s ease;
  cursor: pointer;
}

.qa-item:hover {
  transform: translateY(-4px);
  border-color: #8bc3a1;
  box-shadow: 0 6px 14px rgba(0, 0, 0, 0.1);
}

.qa-question {
  font-size: 18px;
  font-weight: 700;
  color: #2d7d4f;
  margin-bottom: 8px;

  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.qa-answer {
  font-size: 14px;
  line-height: 1.6;
  color: #444;
  max-height: 4.8em;
  overflow: hidden;
  white-space: normal;
  word-break: break-all;
  width: 100%;
  box-sizing: border-box;
  margin-bottom: 10px;
}

.qa-footer {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: #666;
}

/* 用户名普通显示 */
.userName {
  color: #555;
}

/* 领域特殊标记，圆角矩形 */
.specialization {
  background-color: #e0f7eb;
  color: #2d7d4f;
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

/* 更新时间显示 */
.time {
  color: #999;
}
/* ================= 弹窗整体 ================= */
.news-modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background: rgba(0,0,0,0.7);
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 40px 0;
  z-index: 2000;
  overflow-y: auto;
}

.news-modal-container {
  text-indent: 2em;
  background: #fff;
  width: 60vw;
  max-width: 1000px;
  border-radius: 8px;
  padding: 24px 32px;
  box-shadow: 0 8px 24px rgba(0,0,0,0.3);
  font-family: "Microsoft YaHei", sans-serif;
  line-height: 1.8;
  color: #333;
  position: relative;
}

.close-btn {
  position: absolute;
  top: 12px;
  right: 12px;
  font-size: 22px;
  background: none;
  border: none;
  cursor: pointer;
  color: #666;
  transition: color 0.2s ease;
}
.close-btn:hover {
  color: #000;
}

.news-title {
  font-size: 28px;
  font-weight: 700;
  margin-bottom: 20px;
  color: #2D7D4F;
}

.news-content p {
  margin-top: 16px;
  margin-bottom: 16px;
  text-indent: 2em;
  white-space: pre-wrap;
  word-break: break-word;
  overflow-wrap: break-word;
}


.news-footer {
  display: flex;
  justify-content: flex-start;
  gap: 20px;
  margin-top: 32px;
  font-size: 14px;
  color: #555;
}

.news-specialization {
  background-color: #d0f0d6;
  padding: 2px 8px;
  border-radius: 12px;
  font-weight: 600;
  color: #2D7D4F;
}

.qa-item {
  position: relative;
  padding-right: 140px; /* 给右侧按钮留空间 */
}

/* 右侧按钮容器 */
.qa-actions {
  position: absolute;
  top: 50%;
  right: 16px;
  transform: translateY(-50%);

  display: flex;
  gap: 1rem;
}

/* 按钮通用样式（复用 profile-actions） */
.qa-actions button {
  padding: 8px 16px;
  border-radius: 5px;
  border: 1px solid #2D7D4F;
  background-color: #2D7D4F;
  color: white;
  cursor: pointer;
  transition: background-color 0.3s;
  font-size: 13px;
}

/* hover */
.qa-actions button:hover {
  background-color: #256842;
}

/* 删除按钮 */
.qa-actions .delete-btn {
  background-color: #c82333;
  border-color: #bd2130;
}

.qa-actions .delete-btn:hover {
  background-color: #a71d2a;
}


</style>