<template>
  <div class="knowledge-container">

    <div v-if="!knowledgeList.length" class="empty">
      当前暂无农业知识！
    </div>

    <div
        class="knowledge-card"
        v-for="item in knowledgeList"
        :key="item.knowledgeId"
        @click="goToKnowledgeDetail(item.knowledgeId)"
    >
      <h3 class="title">{{ item.title }}</h3>
      <p class="summary">{{ formatSummary(item.content) }}</p>
      <div class="time">发布时间：{{ formatTime(item.createTime) }}</div>
    </div>

    <!-- 分页按钮 -->
    <div class="pagination" v-if="total > pageSize">
      <button :disabled="pageNum === 1" @click="changePage(pageNum - 1)">上一页</button>
      <span>{{ pageNum }} / {{ totalPages }}</span>
      <button :disabled="pageNum === totalPages" @click="changePage(pageNum + 1)">下一页</button>
    </div>

  </div>
</template>

<script setup>
import {ref, onMounted, computed} from 'vue';
import axios from '../utils/axios';
import { useRoute, useRouter } from 'vue-router';

const route = useRoute();
const router = useRouter();

// 获取专家ID（父页面应使用 /expert/:id ）
const expertId = route.params.id;

const knowledgeList = ref([]);
const pageNum = ref(1);
const pageSize = ref(10);
const total = ref(0);

onMounted(() => {
  fetchKnowledge();
});

/** 获取专家的知识列表 */
async function fetchKnowledge() {
  try {
    const response = await axios.get(`/knowledge/list`, {
      params: {
        pageNum: pageNum.value,
        pageSize: pageSize.value,
      }
    });

    const data = response.data;

    knowledgeList.value = data.records || [];
    total.value = data.total || 0;

  } catch (err) {
    console.error("加载农业知识失败:", err);
  }
}

/** 分页跳转 */
function changePage(newPage) {
  pageNum.value = newPage;
  fetchKnowledge();
}

/** 计算总页数 */
const totalPages = computed(() => {
  return Math.ceil(total.value / pageSize.value);
});

/** 内容摘要（只显示前50字符） */
function formatSummary(text) {
  if (!text) return '';
  return text.length > 50 ? text.substring(0, 50) + "..." : text;
}

/** 时间格式化 */
function formatTime(time) {
  return time ? time.replace('T', ' ') : '';
}

/** 跳转到知识详情页面 */
function goToKnowledgeDetail(knowledgeId) {
  router.push(`/knowledge/${knowledgeId}`);
}
</script>

<style scoped>
.knowledge-container {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
  gap: 20px;
}

.knowledge-card {
  display: flex;
  flex-direction: column; /* 垂直排列内容 */
  justify-content: space-between; /* 时间贴底 */
  background: #f9f9f9;
  border-radius: 8px;
  padding: 14px;
  cursor: pointer;
  border: 1px solid #ddd;
  transition: 0.2s;
  min-height: 160px; /* 让卡片高度一致 */
}

.knowledge-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 2px 6px rgba(0,0,0,0.1);
}

.title {
  font-size: 17px;
  margin-bottom: 8px;
  font-weight: bold;
}

.summary {
  font-size: 14px;
  color: #666;
  margin-bottom: 12px;
  flex-grow: 1; /* 摘要撑满空间 */
}

.time {
  font-size: 12px;
  color: #999;
  text-align: right;
  margin-top: auto; /* 自动推到底 */
}

.empty {
  text-align: center;
  color: #888;
  padding: 20px 0;
}

.pagination {
  grid-column: 1 / -1;
  text-align: center;
  margin-top: 16px;
}

.pagination button {
  padding: 6px 12px;
  margin: 0 5px;
}
</style>
