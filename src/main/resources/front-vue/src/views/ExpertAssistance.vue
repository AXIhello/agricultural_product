<template>
  <div class="main-bg">
    <HeaderComponent />

    <section class="content">

      <!-- 左侧导航：三个 view -->
      <nav class="main-nav">
        <button @click="currentView = 'experts'" :class="{ active: currentView === 'experts' }">
          专家推荐
        </button>
        <button @click="currentView = 'knowledge'" :class="{ active: currentView === 'knowledge' }">
          专业知识
        </button>
        <button @click="currentView = 'qa'" :class="{ active: currentView === 'qa' }">
          问答区
        </button>
      </nav>

      <!-- ======================== view 内容区域 ======================== -->
      <div class="view-content-wrapper">

        <!-- ============ 1. 专家推荐 ============ -->
        <div v-if="currentView === 'experts'" class="expert-overview">
          <h3>专家推荐</h3>
          <ExpertOverview />
        </div>

        <!-- ============ 2. 专业知识 ============ -->
        <div v-if="currentView === 'knowledge'" class="expert-qa">
          <h3>专业知识</h3>

          <!-- 知识列表 -->
          <ExpertKnowledge ref="knowledgeRef"/>

          <!-- 发布知识按钮（专家可见） -->
          <div v-if="role === 'expert'" style="margin-top: 12px;">
            <button class="publish-btn" @click="showDetail = true">
              发布新知识
            </button>
          </div>

          <!-- 发布新知识弹窗 -->
          <div v-if="showDetail" class="modal-overlay">
            <div class="modal-container">
              <!-- 右上角关闭按钮 -->
              <button class="close-btn" @click="showDetail = false">×</button>

              <h2 class="modal-title">发布新知识</h2>

              <div class="modal-body">
                <form @submit.prevent="publishKnowledge" class="modal-form">
                  <div class="modal-form-group row-layout">
                    <label>题目：</label>
                    <input v-model="knowledge.title" type="text" placeholder="请输入知识标题" required />
                  </div>

                  <div class="modal-form-group row-layout">
                    <label>内容：</label>
                    <textarea v-model="knowledge.content" placeholder="请输入知识内容" required></textarea>
                  </div>
                </form>
              </div>

              <!-- 底部按钮 -->
              <div class="modal-footer">
                <button class="cancel-btn" @click="showDetail = false">取消</button>
                <button class="save-btn" @click="publishKnowledge">提交</button>
              </div>
            </div>
          </div>

        </div>


        <!-- ============ 3. 问答区 ============ -->
        <div v-if="currentView === 'qa'" class="qa-view">

          <!-- 提问 -->
          <div class="question-form">
            <h3>提问</h3>
            <label for="title">标题:</label>
            <input type="text" id="title" v-model="newQuestion.title" required>

            <label for="content">问题描述:</label>
            <textarea id="content" v-model="newQuestion.content" rows="4" required></textarea>

            <button @click="publishQuestion">提交</button>
            <p v-if="publishMessage" class="success-message">{{ publishMessage }}</p>
          </div>

          <!-- 搜索 -->
          <div class="search-bar">
            <input type="text" v-model="searchKeyword" placeholder="搜索问题">
            <button @click="searchQuestions">搜索</button>
          </div>

          <!-- 问题列表 -->
          <div class="question-list">
            <h3>问题列表(共 {{ totalItems }} 个问题)</h3>

            <div v-if="questions.length > 0" style="color: #666; font-size: 0.9rem; margin-bottom: 10px;">
              显示所有问题：{{ questions.length }} 个
            </div>

            <div v-if="loading">加载中...</div>
            <div v-else-if="questions.length === 0">暂无问题</div>

            <div v-else>
              <div
                  v-for="question in questions"
                  :key="question.questionId"
                  class="question-item"
              >
                <h4>{{ question.title }}</h4>
                <p>{{ question.content }}</p>
                <p>提问时间: {{ formatDate(question.createTime) }}</p>
                <p v-if="question.status === 'answered'">状态: 已采纳</p>
                <p v-else>状态: 待回答</p>

                <!-- 回答列表 -->
                <div class="answers-section">
                  <div v-for="answer in question.answers" :key="answer.answerId" class="answer-item">
                    <p>回答内容: {{ answer.content }}</p>
                    <p>回答者: {{ answer.responderName }}</p>
                    <p>回答时间: {{ formatDate(answer.createTime) }}</p>
                    <button
                        v-if="question.farmerId === user?.userId && question.status !== 'answered'"
                        @click="acceptAnswer(question.questionId, answer.answerId)"
                    >
                      采纳
                    </button>
                    <span v-else-if="answer.isAccepted === 1">已采纳</span>
                  </div>

                  <!-- 回答输入 -->
                  <div class="answer-form" v-if="user">
                    <textarea v-model="newAnswers[question.questionId]" placeholder="写下你的回答"></textarea>
                    <button @click="submitAnswer(question.questionId)">提交回答</button>
                  </div>
                  <div v-else>
                    <p>登录后可以回答问题。</p>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 分页 -->
          <div class="pagination">
            <button @click="changePage(currentPage - 1)" :disabled="currentPage === 1">上一页</button>
            <span>{{ totalPages === 0 ? 0 : currentPage }} / {{ totalPages }}</span>
            <button @click="changePage(currentPage + 1)" :disabled="currentPage === totalPages">下一页</button>
          </div>

        </div>

      </div>

    </section>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import axios from '../utils/axios';
import HeaderComponent from "@/components/HeaderComponent.vue";
import { useAuthStore } from '@/stores/authStore';
import { storeToRefs } from 'pinia';
import ExpertOverview from "@/components/ExpertOverview.vue";
import ExpertKnowledge from "@/components/ExpertKnowledge.vue";
import { useRoute } from 'vue-router';

//路由对象
const route = useRoute();

// ---------------- 用户信息与登录状态 ----------------
const authStore = useAuthStore();// 使用 Pinia 的认证存储
const { userInfo: user, isLoggedIn } = storeToRefs(authStore);

const { role } = storeToRefs(authStore);//从 store 中解构出响应式的数据

// ---------------- 页面状态 ----------------
const currentView = ref('experts'); // 默认显示专家推荐

// ---------------- 问题模块 ----------------
const questions = ref([]);
const newQuestion = ref({ title: '', content: '' });
const publishMessage = ref('');
const searchKeyword = ref('');
const loading = ref(true);
const currentPage = ref(1);
const pageSize = ref(5);
const totalItems = ref(0);
const totalPages = computed(() => Math.ceil(totalItems.value / pageSize.value));
const newAnswers = ref({});
const answerMessages = ref({});



// 格式化日期
const formatDate = (dateTimeString) => {
  if (!dateTimeString) return '';
  return new Date(dateTimeString).toLocaleString();
};

// 获取问题列表
const fetchQuestions = async () => {
  loading.value = true;
  try {
    const pageSizeNumber = Number(pageSize.value);
    if (isNaN(pageSizeNumber)) {
      console.error("Invalid pageSize:", pageSize.value);
      loading.value = false;
      return;
    }

    let url = `/expert-questions?pageNum=${currentPage.value}&pageSize=${pageSizeNumber}&status=all`;
    if (searchKeyword.value) {
      url = `/expert-questions/search?keyword=${searchKeyword.value}&pageNum=${currentPage.value}&pageSize=${pageSizeNumber}`;
    }

    const response = await axios.get(url);
    const data = response.data;

    if (data && data.total != null) {
      const parsedTotal = parseInt(data.total, 10);
      totalItems.value = !isNaN(parsedTotal) ? parsedTotal : 0;
    } else {
      totalItems.value = 0;
    }

    questions.value = response.data.records || [];

    // 获取每个问题的回答
    await Promise.all(
        questions.value.map(async (q) => {
          try {
            const answersResp = await axios.get(`/expert-questions/${q.questionId}/answers`);
            q.answers = answersResp.data.records || [];
          } catch (err) {
            console.error(`获取问题 ${q.questionId} 的回答失败`, err);
            q.answers = [];
          }
        })
    );
  } catch (err) {
    console.error('获取问题列表失败', err);
  } finally {
    loading.value = false;
  }
};

// 切换分页
const changePage = (page) => {
  if (page < 1 || page > totalPages.value) return;
  currentPage.value = page;
  fetchQuestions();
};

// 发布问题
const publishQuestion = async () => {
  if (!isLoggedIn.value) {
    publishMessage.value = '请先登录！';
    return;
  }

  const currentUserId = user.value?.userId;
  if (!currentUserId) {
    alert('无法获取用户信息，请重新登录后再发布问题！');
    return;
  }

  try {
    const response = await axios.post('/expert-questions', {
      title: newQuestion.value.title,
      content: newQuestion.value.content,
      farmerId: currentUserId,
    });

    if (response.data) {
      publishMessage.value = '问题发布成功！';
      newQuestion.value = { title: '', content: '' };
      fetchQuestions();
    } else {
      publishMessage.value = '问题发布失败。';
    }
  } catch (err) {
    console.error('发布问题失败', err);
    publishMessage.value = '发布问题时发生错误。';
  }

  setTimeout(() => {
    publishMessage.value = '';
  }, 3000);
};

// 搜索问题
const searchQuestions = () => {
  currentPage.value = 1;
  fetchQuestions();
};

// 提交回答
const submitAnswer = async (questionId) => {
  const content = newAnswers.value[questionId];
  if (!content) {
    answerMessages.value[questionId] = '请输入回答内容。';
    return;
  }
  if (!isLoggedIn.value) {
    answerMessages.value[questionId] = '请先登录！';
    return;
  }

  try {
    const response = await axios.post(`/expert-questions/${questionId}/answers`, {
      content,
      responderName: user.value?.userName,
      responderId: user.value?.userId,
    });

    if (response.data) {
      answerMessages.value[questionId] = '回答提交成功！';
      newAnswers.value[questionId] = '';
      fetchQuestions();
    } else {
      answerMessages.value[questionId] = '回答提交失败。';
    }
  } catch (err) {
    console.error('提交回答失败', err);
    answerMessages.value[questionId] = '提交回答时发生错误。';
  }

  setTimeout(() => {
    answerMessages.value[questionId] = '';
  }, 3000);
};

// 采纳回答
const acceptAnswer = async (questionId, answerId) => {
  try {
    const response = await axios.post(`/expert-questions/${questionId}/accept/${answerId}`);
    if (response.data) fetchQuestions();
    else alert('采纳失败');
  } catch (err) {
    console.error('采纳回答失败', err);
    alert('采纳回答时发生错误');
  }
};

// ---------------- 知识模块 ----------------
// const knowledgeList = ref([]);
// const knowledgePageNum = ref(1);
// const knowledgePageSize = ref(10);
// const knowledgeTotalPages = ref(1);


// // 获取知识列表
// const fetchKnowledgeList = async () => {
//   try {
//     const res = await axios.get(
//         `/knowledge/list?pageNum=${knowledgePageNum.value}&pageSize=${knowledgePageSize.value}`
//     );
//     // 按时间降序排序，假设 createTime 是 ISO 字符串
//     res.data.records.sort((a, b) => b.knowledgeId - a.knowledgeId);
//     console.log("知识：",res.data.records);
//
//     knowledgeList.value = res.data.records || [];
//     knowledgeTotalPages.value = res.data.totalPages || 1;
//     console.log('知识列表已更新',knowledgeList.value);
//   } catch (err) {
//     console.error(err);
//   }
// };

const knowledge = ref({
  title: '',
  content: ''
})
const showDetail = ref(false)

const knowledgeRef = ref(null);

// 专家发布新知识
const publishKnowledge = async () => {
  try {
    if (!knowledge.value.title || !knowledge.value.content) {
      alert('标题和内容不能为空');
      return;
    }
    await axios.post('/knowledge/publish', knowledge.value)
    alert('知识发布成功！')
    showDetail.value = false
    knowledge.value = {
      title: '',
      content: ''
    }
    knowledgeRef.value.fetchKnowledge();
  } catch (err) {
    console.error(err)
    alert('发布失败，请稍后重试')
  }
}

// ---------------- 生命周期 ----------------
onMounted(() => {
  if (route.query.view) {
    currentView.value = route.query.view; 
  }
  fetchQuestions();
});
</script>

<style scoped>
.expert-qa {
  padding: 1rem;
}

.publish-knowledge input,
.publish-knowledge textarea {
  display: block;
  margin-bottom: 0.5rem;
  width: 100%;
}

.knowledge-card {
  border: 1px solid #ccc;
  padding: 0.5rem;
  margin-bottom: 0.5rem;
  border-radius: 4px;
}

.knowledge-actions button {
  margin-right: 0.5rem;
}

.pagination {
  margin-top: 1rem;
}

.edit-popup {
  position: fixed;
  top: 20%;
  left: 30%;
  width: 40%;
  background: #fff;
  padding: 1rem;
  border: 1px solid #ccc;
  z-index: 10;
}
</style>

<style scoped>



.expert-overview,
.expert-qa{
  padding: 15px;
  border: 1px solid #ddd;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  background-color: #f9f9f9;
}

.question-form {
  background-color: #f9f9f9; /* 浅灰色背景 */
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
}

.question-form h3 {
  font-size: 1.5rem;
  margin-bottom: 15px;
  color: #333; /* 更深的标题颜色 */
  text-align: center;
}

.question-form label {
  display: block;
  margin-bottom: 8px;
  font-weight: bold;
  color: #555; /* 更柔和的标签颜色 */
  text-align: left;
}

.question-form input[type="text"],
.question-form textarea {
  width: 100%;
  padding: 10px;
  margin-bottom: 15px;
  border: 1px solid #ccc;
  border-radius: 4px;
  font-size: 1rem;
  box-sizing: border-box; /* 包含内边距在总宽度中 */
  transition: border-color 0.2s; /* 添加过渡效果 */

}

.question-form input[type="text"]:focus,
.question-form textarea:focus {
  border-color: #4CAF50; /* 获得焦点时改变边框颜色 */
  outline: none; /* 移除默认的 focus outline */

}

.question-form textarea {
  resize: vertical; /* 允许垂直调整大小 */
  min-height: 100px; /* 最小高度 */

}

.question-form button {
  background-color: #52c41a;
  color: white;
  padding: 12px 20px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 1.1rem; /* 增加字体大小 */
  font-weight: 500;
  transition: background-color 0.3s;
  display: inline-block; /*  按钮变为行内块元素, 可以设置 margin */
  margin-top: 10px;
}

.question-form button:hover {
  background-color: #3e8e41;
}

.success-message {
  color: #52c41a;
  background-color: #e8f5e9;
  padding: 10px;
  border-radius: 4px;
  margin-top: 10px;
  border: 1px solid #c8e6c9;
}

.search-bar {
  margin-bottom: 20px;
  display: flex;
  align-items: center; /* 垂直居中 */
}

.search-bar input[type="text"] {
  flex-grow: 1;
  padding: 10px;
  margin-right: 10px;
  border: 1px solid #ccc;
  border-radius: 4px;
  font-size: 1rem;
  box-sizing: border-box;

}

.search-bar button {
  background-color: #008CBA;
  color: white;
  padding: 10px 20px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 1.1rem;
  font-weight: 500;
  transition: background-color 0.3s;
}

.search-bar button:hover {
  background-color: #0077a3;
}

.question-list {
  background-color: #f9f9f9; /* 浅灰色背景 */
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
}

.question-list h3 {
  font-size: 1.5rem;
  margin-bottom: 15px;
  color: #333;
}

.question-item {
  background-color: #fff;
  padding: 15px;
  border-radius: 8px;
  margin-bottom: 15px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.question-item h4 {
  font-size: 1.2rem;
  margin-bottom: 8px;
  color: #2D7D4F;
  text-align: left;
}

.question-item p {
  font-size: 1rem;
  line-height: 1.6;
  color: #555;
  text-align: left;
}

.pagination {
  text-align: center;
  margin-top: 20px;
}

.pagination button {
  background-color: #eee;
  color: #333;
  padding: 8px 15px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  margin: 0 5px;
  font-size: 1rem;
  transition: background-color 0.3s;

}

.pagination button:hover {
  background-color: #ddd;
}

.pagination button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.answers-section {
  margin-top: 10px;
  padding: 10px;
  border: 1px solid #ccc;
  border-radius: 4px;
}

.answer-item {
  padding: 10px;
  border-bottom: 1px solid #eee;
}

.answer-item:last-child {
  border-bottom: none;
}

.answer-form {
  margin-top: 10px;
  display: flex;
  justify-content: flex-end;
}

.answer-form textarea {
  width: 100%;
  padding: 10px;
  margin-bottom: 5px;
  border: 1px solid #ccc;
  border-radius: 4px;
  resize: vertical; /* 允许垂直调整大小 */
}

.answer-form button {
  background-color: #52c41a;
  color: white;
  padding: 10px 20px;
  border: none;
  border-radius: 10px;
  cursor: pointer;
  font-size: 1.1rem;
  font-weight: 500;
  transition: background-color 0.3s;
  white-space: nowrap;
  margin-left: 10px;
}
.publish-btn {
  background-color: #4caf50;
  color: #fff;
  border: none;
  padding: 6px 16px;
  border-radius: 4px;
  cursor: pointer;
  margin-top: 10px;
}

.publish-btn:hover {
  background-color: #45a049;
}


</style>