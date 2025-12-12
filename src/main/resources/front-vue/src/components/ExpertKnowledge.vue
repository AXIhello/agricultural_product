<template>
  <div class="expert-qa-container">
    <!-- 加载状态提示 -->
    <div v-if="loading" style="text-align:center; color:#999;">加载中...</div>
    <div v-else-if="knowledgeList.length === 0" style="text-align:center; color:#999;">暂无专业知识</div>

    <div
      class="qa-item"
      v-for="item in knowledgeList"
      :key="item.knowledgeId"
      @click="goToKnowledgePage"
    >
      <h4 class="qa-question">标题：{{ item.title }}</h4>
      <p class="qa-answer">
        摘要：{{ item.summary || item.content?.substring(0, 60) + '...' || '暂无详细内容' }}
      </p>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import axios from '@/utils/axios';

const router = useRouter();

const knowledgeList = ref([]);
const loading = ref(false);

const goToKnowledgePage = () => {
  router.push({
    path: '/expert', 
    query: { view: 'knowledge' }     
  });
}
/** 获取最新 5 条专业知识 */
const fetchHomeKnowledge = async () => {
  loading.value = true;
  try {
    const { data } = await axios.get('/knowledge/list?pageNum=1&pageSize=5');
    const records = data.records || [];
    knowledgeList.value = records.map(it => ({
      ...it,
      summary: it.summary || it.content?.substring(0, 60) + '...' || '暂无详细内容'
    }));
  } catch (err) {
    console.error('首页获取专业知识失败:', err);
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  fetchHomeKnowledge();
});
</script>

<style scoped>
/* 完全复用你已有的样式，一个字不改 */
.expert-qa-container {
  padding: 18px;
}
.qa-item {
  padding: 14px 18px;
  background-color: #f8fbf8;
  border: 1px solid #d9e8df;
  border-radius: 12px;
  margin-bottom: 14px;
  box-shadow: 0 3px 8px rgba(0, 0, 0, 0.06);
  transition: all 0.25s ease;
  cursor: pointer;
}
.qa-item:hover {
  transform: translateY(-4px);
  border-color: #8bc3a1;
  box-shadow: 0 6px 14px rgba(0, 0, 0, 0.10);
}
.qa-question {
  font-size: 16px;
  font-weight: 600;
  color: #2d7d4f;
  margin-bottom: 6px;
}
.qa-answer {
  font-size: 14px;
  line-height: 1.5;
  color: #444;
}
</style>