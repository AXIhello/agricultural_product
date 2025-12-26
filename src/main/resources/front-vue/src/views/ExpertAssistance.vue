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
          <ExpertOverview />
        </div>

        <!-- ============ 2. 专业知识 ============ -->
        <div v-if="currentView === 'knowledge'" class="expert-qa">
          <h3>专业知识</h3>

          <div class="knowledge-card-list">
            <!-- 知识列表 -->
            <ExpertKnowledge
                ref="knowledgeRef"
                @edit="handleEditKnowledge"
                @delete="handleDeleteKnowledge"
            />

            <!-- 知识卡片（专家可见） -->
            <div
                v-if="role === 'expert'"
                class="qa-item add-card"
                @click="showDetail = true"
            >
              <div class="add-content">
                <span class="plus">＋</span>
                <p>发布新知识</p>
              </div>
            </div>

          </div>

          <!-- 发布新知识弹窗 -->
          <div v-if="showDetail" class="modal-overlay">
            <div class="modal-container">
              <!-- 右上角关闭按钮 -->
              <button class="close-btn" @click="showDetail = false; resetKnowledge()">×</button>


              <h2 class="modal-title">
                {{ isEdit ? '编辑知识' : '发布知识' }}
              </h2>


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
                <button class="save-btn" @click="submitKnowledge">
                  {{ isEdit ? '保存修改' : '提交' }}
                </button>

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
            <button 
              v-if="searchKeyword" 
              @click="resetSearch" 
              class="reset-btn"
            >
              返回全部
            </button>
          </div>
          

          <!-- 问题列表 -->
          <div class="question-list">
            <h3>问题列表(共 {{ totalItems }} 个问题)</h3>

            <div v-if="loading">加载中...</div>
            <div v-else-if="questions.length === 0">暂无问题</div>

            <div v-else>
              <div
                  v-for="question in questions"
                  :key="question.questionId"
                  :id="'question-' + question.questionId"
                  class="question-item clickable-card"
                  @click="openQADetail(question)"
              >
                <div class="card-header">
                  <h4>{{ question.title }}</h4>
                  <span :class="question.status === 'answered' ? 'status-tag done' : 'status-tag wait'">
                    {{ question.status === 'answered' ? '已解决' : '待解决' }}
                  </span>
                </div>
                <p class="preview-text">{{ question.content }}</p>
                <div class="card-footer">
                  <div class="footer-info">
                    <span class="asker-name">提问者：{{ question.farmerName || '匿名用户' }}</span>
                    <span class="time">提问时间：{{ formatDate(question.createTime) }}</span>
                  </div>
                  <span class="click-tip">点击查看详情 >></span>
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

          <!-- ============ 问答详情弹窗 ============ -->
          <div v-if="showQADetail && selectedQuestion" class="modal-overlay" @click.self="closeQADetail">
            <div class="modal-content qa-detail-modal">
              <button class="close-btn-icon" @click="closeQADetail">×</button>
              
              <!-- 问题详情 -->
              <div class="detail-header">
                <h3>{{ selectedQuestion.title }}</h3>
                <div class="detail-meta">
                  <span class="meta-item">提问者：<b>{{ selectedQuestion.farmerName || '匿名用户' }}</b></span>
                  <span>发布时间：{{ formatDate(selectedQuestion.createTime) }}</span>
                  <span>状态：{{ selectedQuestion.status === 'answered' ? '已采纳' : '待回答' }}</span>
                </div>
                <div class="detail-content">
                  {{ selectedQuestion.content }}
                </div>
              </div>

              <hr class="divider" />

              <!-- 回答列表区域 -->
              <div class="answers-section">
                <h4>回答列表 ({{ selectedQuestion.answers ? selectedQuestion.answers.length : 0 }})</h4>
                
                <div v-if="!selectedQuestion.answers || selectedQuestion.answers.length === 0" class="no-answer-tip">
                  暂无回答，期待您的专业解答！
                </div>

                <div v-for="answer in selectedQuestion.answers" :key="answer.answerId" class="answer-item">
                  <div class="answer-main">
                    <p class="answer-content">{{ answer.content }}</p>
                    <p class="answer-meta">
                      回答者: {{ answer.responderName }} | 时间: {{ formatDate(answer.createTime) }}
                    </p>
                  </div>
                  
                  <!-- 采纳按钮/标记 -->
                  <div class="answer-action">
                    <button
                        v-if="selectedQuestion.farmerId === user?.userId && selectedQuestion.status !== 'answered'"
                        class="accept-btn"
                        @click="acceptAnswer(selectedQuestion.questionId, answer.answerId)"
                    >
                      采纳
                    </button>
                    <span v-else-if="answer.isAccepted === 1" class="accepted-badge">✅ 已采纳</span>
                  </div>
                </div>

                <!-- 回答输入框 -->
                <div class="answer-form-wrapper" v-if="user">
                  <h4>我来回答</h4>
                  <textarea 
                    v-model="newAnswers[selectedQuestion.questionId]" 
                    placeholder="请输入您的回答..."
                    rows="3"
                  ></textarea>
                  <div class="form-footer">
                    <span class="msg" v-if="answerMessages[selectedQuestion.questionId]">{{ answerMessages[selectedQuestion.questionId] }}</span>
                    <button @click="submitAnswer(selectedQuestion.questionId)">提交回答</button>
                  </div>
                </div>
                <div v-else class="login-tip">
                  <p>登录后可以回答问题。</p>
                </div>
              </div>

            </div>
          </div>

        </div>

      </div>

    </section>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, nextTick } from 'vue';
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

// --- 弹窗控制变量 ---
const showQADetail = ref(false);
const selectedQuestion = ref(null);

// --- 打开/关闭弹窗逻辑 ---
const openQADetail = (question) => {
  selectedQuestion.value = question;
  showQADetail.value = true;
};

const closeQADetail = () => {
  showQADetail.value = false;
  selectedQuestion.value = null;
  // 清理当前问题的输入框提示
  if (selectedQuestion.value) {
    answerMessages.value[selectedQuestion.value.questionId] = '';
  }
};

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
    
    // --- 如果在弹窗打开时刷新数据，需要同步更新 selectedQuestion ---
    if (showQADetail.value && selectedQuestion.value) {
      const updatedQ = questions.value.find(q => q.questionId === selectedQuestion.value.questionId);
      if (updatedQ) {
        selectedQuestion.value = updatedQ;
      }
    }

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
      newQuestion.value = { title: '', content: '' };//清空表单

      // 清空搜索关键词
      searchKeyword.value = '';
      
      // 重置到第一页
      currentPage.value = 1;

      // 重新获取列表
      await fetchQuestions();

      //等待 DOM 更新（列表渲染完毕）后，执行滚动和高亮
      nextTick(() => {

        const newestQuestion = questions.value[0];

        if (newestQuestion) {
          // 找到对应的 DOM 元素
          const element = document.getElementById(`question-${newestQuestion.questionId}`);
          
          if (element) {
            // 平滑滚动到该元素
            element.scrollIntoView({ behavior: 'smooth', block: 'center' });
            // 添加高亮样式
            element.classList.add('highlight-new');
            // 2秒后移除高亮样式
            setTimeout(() => {
              element.classList.remove('highlight-new');
            }, 2000);
          }
        }
      });
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

// 重置搜索，返回全部列表
const resetSearch = () => {
  searchKeyword.value = ''; // 清空搜索关键词
  currentPage.value = 1;    // 重置回第一页
  fetchQuestions();         // 重新调用接口，此时 keyword 为空，会自动拉取全部数据
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

  const currentQuestion = selectedQuestion.value;

  // 检查问题状态，如果是已解决（answered），则不允许提交回答
  if (currentQuestion && currentQuestion.status === 'answered') {
    answerMessages.value[questionId] = '该问题已截止收集回答。';
    setTimeout(() => {
      answerMessages.value[questionId] = '';
    }, 3000);
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

const knowledge = ref({
  knowledgeId: null,
  title: '',
  content: ''
})

const showDetail = ref(false)
const isEdit = ref(false)

const knowledgeRef = ref(null)


const resetKnowledge = () => {
  knowledge.value = {}
};

// 专家发布新知识
const submitKnowledge = async () => {
  if (!knowledge.value.title || !knowledge.value.content) {
    alert('标题和内容不能为空')
    return
  }

  try {
    if (isEdit.value) {
      // 编辑
      await axios.put(
          `/knowledge/${knowledge.value.knowledgeId}`,
          {
            title: knowledge.value.title,
            content: knowledge.value.content
          }
      )
      alert('修改成功')
    } else {
      // 新增
      await axios.post('/knowledge/publish', {
        title: knowledge.value.title,
        content: knowledge.value.content
      })
      alert('发布成功')
    }

    showDetail.value = false
    resetKnowledge()
    knowledgeRef.value.fetchKnowledge()

  } catch (err) {
    console.error(err)
    alert('操作失败')
  }
}

const handleEditKnowledge = (item) => {
  isEdit.value = true
  showDetail.value = true

  knowledge.value = {
    knowledgeId: item.knowledgeId,
    title: item.title,
    content: item.content
  }

}

const handleDeleteKnowledge = async (item) => {
  if (!confirm('确定删除该知识吗？')) return

  try {
    await axios.delete(`/knowledge/${item.knowledgeId}`)
    alert('删除成功')
    knowledgeRef.value.fetchKnowledge()
  } catch (err) {
    console.error(err)
    alert('删除失败')
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

.knowledge-card-list {
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 16px;
}

/* 新增卡片 */
.add-card {
  margin: 18px;
  border: 2px dashed #cfd8d3;
  background-color: #fafafa;
  cursor: pointer;
  transition: all 0.25s ease;
}

.add-card:hover {
  border-color: #2D7D4F;
  background-color: #f3faf6;
}

/* 中心内容 */
.add-content {
  height: 100%;
  min-height: 120px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #2D7D4F;
}

.add-content .plus {
  font-size: 36px;
  font-weight: 300;
  line-height: 1;
}

.add-content p {
  margin-top: 6px;
  font-size: 14px;
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

.search-bar :hover {
  background-color:#e0e0e0; 
  color: #333;
}

/* 重置按钮样式 */
.search-bar .reset-btn {
  background-color: #f0f0f0; /* 浅灰色背景 */
  color: #666;               /* 深灰色文字 */
  margin-left: 10px;         /* 与搜索按钮拉开距离 */
  border: 1px solid #ddd;
}

.search-bar .reset-btn:hover {
  background-color: #e0e0e0; /* 悬停变深一点 */
  color: #333;
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

/*通用弹窗*/
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0,0,0,0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
}

.modal-content {
  background: white;
  padding: 20px;
  width: 420px;
  max-width: 90%;
  border-radius: 12px;
  box-shadow: 0 4px 10px rgba(0,0,0,0.25);
}

/*
   Modal 内标题*/
.modal-content h3,
.modal-content h4 {
  color: #2D7D4F;
  margin-bottom: 12px;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0,0,0,0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 999;
}

.modal-title-div {
  position: relative;
  text-align: center;      /* 让标题居中 */
  padding: 10px 40px;      /* 给右侧留空间放按钮 */
}

.modal-title-div h3 {
  margin: 0;
}

.modal-close-btn {
  position: absolute;
  right: 10px;
  top: 50%;
  transform: translateY(-50%);  /* 垂直居中对齐 */
  cursor: pointer;
  font-size: 14px;
}

.modal-content {
  background: #fff;
  border-radius: 8px;
  padding: 20px 25px;
  width: 400px;
  max-width: 90%;
}

.modal-content h3 {
  margin-bottom: 15px;
  text-align: center;
}

.modal-form .form-group {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.modal-form .form-group label {
  width: 25%;
  text-align: left;
  font-weight: 500;
}

.modal-form .form-group input,
.modal-form .form-group textarea {
  width: 75%;
  padding: 5px 8px;
  border: 1px solid #ccc;
  border-radius: 4px;
}
.modal-form .form-group textarea {
  height: 100px;
}

.modal-form .form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 15px;
}
</style>


/* ---------------- 问题展示修正---------------- */
<style scoped>
  /* ---------------- 列表项样式 (卡片预览) ---------------- */
.clickable-card {
  cursor: pointer;
  transition: all 0.2s ease;
  position: relative;
  border-left: 5px solid transparent;
}

.clickable-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 5px 15px rgba(0,0,0,0.1);
  border-left-color: #2D7D4F; /* 悬停时左侧显示绿条 */
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.card-header h4 {
  margin: 0;
  font-size: 1.15rem;
  color: #333;
}

.status-tag {
  font-size: 0.8rem;
  padding: 2px 8px;
  border-radius: 10px;
}
.status-tag.done { background: #e6ffed; color: #52c41a; border: 1px solid #b7eb8f; }
.status-tag.wait { background: #fff7e6; color: #faad14; border: 1px solid #ffe58f; }

.preview-text {
  color: #666;
  font-size: 0.95rem;
  display: -webkit-box;
  -webkit-line-clamp: 2; /* 限制显示2行 */
  -webkit-box-orient: vertical;
  overflow: hidden;
  margin-bottom: 10px;
}

.card-footer {
   display: flex;
  justify-content: space-between;
  align-items: center;
  color: #999;
  font-size: 0.85rem;
  margin-top: 10px; /* 稍微增加一点顶部间距 */
}

.footer-info {
  display: flex;
  gap: 15px; /* 名字和时间之间的间距 */
}

.asker-name {
  color: #555;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 4px; /* 图标和文字间距 */
}

.click-tip {
  color: #2D7D4F;
  font-weight: bold;
  opacity: 0;
  transition: opacity 0.2s;
}

.clickable-card:hover .click-tip {
  opacity: 1;
}

/* ---------------- 详情弹窗特定样式 ---------------- */
/* 弹窗容器调整 */
.qa-detail-modal {
  width: 700px; /* 比知识弹窗更宽 */
  max-width: 95%;
  max-height: 90vh; /* 防止超出屏幕高度 */
  overflow-y: auto; /* 允许滚动 */
  padding: 25px;
  position: relative;
}

.close-btn-icon {
  position: absolute;
  top: 10px;
  right: 15px;
  background: none;
  border: none;
  font-size: 24px;
  color: #888;
  cursor: pointer;
  line-height: 1;
}
.close-btn-icon:hover { color: #333; }

.detail-header h3 {
  font-size: 1.5rem;
  color: #2D7D4F;
  margin-bottom: 10px;
  padding-right: 30px;
}

.detail-meta {
  color: #888;
  font-size: 0.9rem;
  margin-bottom: 15px;
  display: flex;
  gap: 20px; /* 各个元数据之间的间距 */
  align-items: center;
  flex-wrap: wrap;
}

.detail-meta .meta-item b {
  color: #333; /* 名字加深显示 */
}
.detail-meta span { margin-right: 20px; }

.detail-content {
  font-size: 1.1rem;
  line-height: 1.6;
  color: #333;
  background: #fdfdfd;
  padding: 10px;
  border-radius: 4px;
}

.divider {
  border: 0;
  border-top: 1px solid #eee;
  margin: 20px 0;
}

/* 弹窗内的回答区 */
.answers-section {
  padding: 0 5px;
}
.answers-section h4 {
  margin-bottom: 15px;
  font-size: 1.2rem;
  border-left: 4px solid #2D7D4F;
  padding-left: 10px;
}

.answer-item {
  display: flex;
  justify-content: space-between;
  border-bottom: 1px solid #eee;
  padding: 15px 0;
}
.answer-main {
  flex: 1;
}
.answer-content {
  font-size: 1rem;
  color: #444;
  margin-bottom: 5px;
}
.answer-meta {
  font-size: 0.85rem;
  color: #999;
}
.answer-action {
  margin-left: 15px;
  display: flex;
  align-items: center;
}

/* 采纳按钮 */
.accept-btn {
  background-color: white;
  border: 1px solid #2D7D4F;
  color: #2D7D4F;
  padding: 5px 12px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.9rem;
}
.accept-btn:hover {
  background-color: #2D7D4F;
  color: white;
}
.accepted-badge {
  color: #52c41a;
  font-weight: bold;
}

/* 弹窗内的输入框 */
.answer-form-wrapper {
  margin-top: 20px;
  background: #f0f9f0;
  padding: 15px;
  border-radius: 6px;
}
.answer-form-wrapper h4 {
  border: none;
  padding: 0;
  font-size: 1rem;
  margin-bottom: 8px;
}
.answer-form-wrapper textarea {
  width: 100%;
  padding: 10px;
  border: 1px solid #ccc;
  border-radius: 4px;
  resize: vertical;
}
.form-footer {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  margin-top: 10px;
}
.form-footer .msg {
  color: #ff4d4f;
  margin-right: 10px;
  font-size: 0.9rem;
}
.form-footer button {
  background-color: #2D7D4F;
  color: white;
  border: none;
  padding: 8px 20px;
  border-radius: 4px;
  cursor: pointer;
}

/* 通用弹窗覆盖层 (复用) */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0,0,0,0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
  backdrop-filter: blur(2px); /* 增加背景模糊效果 */
}
.modal-content {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.2);
}

/* 输入框通用样式复原 */
input[type="text"], textarea {
  box-sizing: border-box; 
  /* 确保padding不会撑大宽度 */
}
</style>

//发布问题后高亮新问题
<style scoped>
/* 定义高亮动画 */
@keyframes flash-highlight {
  0% { background-color: #e6ffed; border-color: #52c41a; transform: scale(1.02); }
  50% { background-color: #e6ffed; border-color: #52c41a; transform: scale(1.02); }
  100% { background-color: #fff; border-color: transparent; transform: scale(1); }
}

/* 应用到卡片上的类 */
.highlight-new {
  animation: flash-highlight 2s ease-out;
  /* 确保动画期间卡片层级较高，不会被遮挡 */
  z-index: 10; 
  position: relative;
  border: 2px solid #52c41a; /* 临时加个绿边框 */
}
</style>