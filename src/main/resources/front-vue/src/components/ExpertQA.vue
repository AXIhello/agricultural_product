<template>
  <div class="expert-qa-container">
    
    <!-- 加载状态提示 -->
    <div v-if="loading" style="text-align:center; color:#999;">加载中...</div>
    <div v-else-if="qaList.length === 0" style="text-align:center; color:#999;">暂无已采纳的问答</div>

    <div 
      class="qa-item" 
      v-for="qa in qaList" 
      :key="qa.questionId"
      @click="goToExpertPage"
    >
      <!-- 绑定真实数据字段：title 是问题，acceptedAnswerContent 是已采纳回答 -->
      <h4 style="font-size: 16px; margin-bottom: 5px;">问题：{{ qa.title }}</h4>
      <p style="font-size: 14px; color: #666; margin-bottom: 5px;">
        回答：{{ qa.acceptedAnswerContent || '暂无详细回答内容' }}
      </p>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import axios from '@/utils/axios'; // 确保路径正确
import { useRouter } from 'vue-router';

const router = useRouter();

const qaList = ref([]);
const loading = ref(false);

const goToExpertPage = () => {
   router.push({
    path: '/expert', 
    query: { view: 'qa' }     
  });
}

// 获取已采纳的问题列表
const fetchAnsweredQuestions = async () => {
  loading.value = true;
  try {
    const response = await axios.get('/expert-questions?status=answered&pageNum=1&pageSize=5');
    
    const questions = response.data.records || [];
    
    const questionsWithAnswers = await Promise.all(questions.map(async (q) => {
      try {
        // 获取该问题的所有回答
        const answersResp = await axios.get(`/expert-questions/${q.questionId}/answers`);
        const answers = answersResp.data.records || [];
        
        // 找到 isAccepted === 1 的回答
        const acceptedAnswer = answers.find(a => a.isAccepted === 1);
        
        return {
          ...q,
          acceptedAnswerContent: acceptedAnswer ? acceptedAnswer.content : '该问题暂无回答内容'
        };
      } catch (err) {
        console.error(`获取问题 ${q.questionId} 回答失败`, err);
        return { ...q, acceptedAnswerContent: '加载回答失败' };
      }
    }));

    qaList.value = questionsWithAnswers;
  } catch (error) {
    console.error('获取专家问答失败:', error);
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  fetchAnsweredQuestions();
});
</script>

<style scoped>
/* 你的原有样式，完全保留 */
.expert-qa-container {
  padding: 18px;
}

/* 单条专家问答卡片 */
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

/* 鼠标悬停效果 */
.qa-item:hover {
  transform: translateY(-4px);
  border-color: #8bc3a1;
  box-shadow: 0 6px 14px rgba(0, 0, 0, 0.10);
}

/* 问题 */
.qa-question {
  font-size: 16px;
  font-weight: 600;
  color: #2d7d4f;
  margin-bottom: 6px;
}

/* 回答 */
.qa-answer {
  font-size: 14px;
  line-height: 1.5;
  color: #444;
}
</style>