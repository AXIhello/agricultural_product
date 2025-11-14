<template>
  <div class="main-bg">
    <HeaderComponent />

    <!-- 提问表单 -->
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
      <!-- 调试信息 -->
      <div v-if="questions.length > 0" style="color: #666; font-size: 0.9rem; margin-bottom: 10px;">
        显示所有问题：{{ questions.length }} 个
      </div>

      <div v-if="loading">加载中...</div>
      <div v-else-if="questions.length === 0">暂无问题</div>

      <div v-else>
        <div v-for="question in questions" :key="question.questionId" class="question-item">
          <h4>{{ question.title }}</h4>
          <p>{{ question.content }}</p>
          <p>提问时间: {{ formatDate(question.createTime) }}</p>
          <p v-if="question.status === 'answered'">状态: 已采纳</p>
          <p v-else>状态: 待回答</p>

           <!--  回答区域 -->
          <div class="answers-section">
            <div v-for="answer in question.answers" :key="answer.answerId" class="answer-item">
              <p>回答内容: {{ answer.content }}</p>
              <p>回答者ID: {{ answer.responderName }}</p>
              <p>回答时间: {{ formatDate(answer.createTime) }}</p>
              <button v-if="question.farmerId === user?.userId && question.status !== 'answered' " @click="acceptAnswer(question.questionId, answer.answerId)">采纳</button>
              <span v-else-if="answer.isAccepted === 1">已采纳</span>

            </div>

            <!--  回答输入框  -->
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
    <!-- 分页 -->
    <div class="pagination">
          <button @click="changePage(currentPage - 1)" :disabled="currentPage === 1">上一页</button>
          <span>{{ totalPages === 0 ? 0 : currentPage  }} / {{ totalPages }}</span>
          
          <button @click="changePage(currentPage + 1)" :disabled="currentPage === totalPages">下一页</button>
    </div>

  </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import axios from '../utils/axios';
import HeaderComponent from "@/components/HeaderComponent.vue";
import { useAuthStore } from '@/stores/authStore';
import { storeToRefs } from 'pinia';

const authStore = useAuthStore();
const { userInfo: user, isLoggedIn } = storeToRefs(authStore);// user 是 userInfo 的别名，isLoggedIn 判断是否登录

const questions = ref([]);
const newQuestion = ref({
  title: '',
  content: '',
});
const publishMessage = ref('');
const searchKeyword = ref('');
const loading = ref(true); // 加载状态
const currentPage = ref(1);
const pageSize = ref(5);
const totalItems = ref(0); // 总的问题数量
const totalPages = computed(() => Math.ceil(totalItems.value / pageSize.value));
const newAnswers = ref({}); // 用于存储每个问题的回答内容
const answerMessages = ref({}); // 用于存储每个问题的回答提交消息


//获取用户信息
const getUserInfo = () => {
    const userInfoString = localStorage.getItem('userInfo');
    if (userInfoString) {
        try {
            return JSON.parse(userInfoString);
        } catch (error) {
            console.error("Error parsing userInfo from localStorage:", error);
            localStorage.removeItem('userInfo'); // 清除损坏的数据
            return null; // 或者返回一个默认的 user 对象
        }
    }
    return null; // 没有找到用户信息
};

// 格式化日期
const formatDate = (dateTimeString) => {
    if (!dateTimeString) return '';
    const date = new Date(dateTimeString);
    return date.toLocaleString();
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

    if (data && data.total !== null && data.total !== undefined) {
        const parsedTotal = parseInt(data.total, 10);
        if (!isNaN(parsedTotal)) {
            totalItems.value = parsedTotal;
        } else {
            console.error("从API收到的total值不是一个有效的数字:", data.total);
            totalItems.value = 0;
        }
    } else {
        console.warn("API响应中缺少total字段或其值为null/undefined");
        totalItems.value = 0;
    }

    questions.value = response.data.records;
    
     if (isNaN(totalItems.value)) {
      console.error("Invalid totalItems:", totalItems.value);
      totalItems.value = 0; //  或者，使用一个默认值
    }

    // 获取问题的回答
    await Promise.all(questions.value.map(async question => {
        try {
            const answersResponse = await axios.get(`/expert-questions/${question.questionId}/answers`);
            question.answers = answersResponse.data.records;
        } catch (error) {
            console.error(`获取问题 ${question.questionId} 的回答失败`, error);
            question.answers = []; // 如果获取失败，则设置为一个空数组，避免显示错误
        }
    }));

  } catch (error) {
    console.error('获取问题列表失败', error);
  } finally {
    loading.value = false;
  }
};

// 切换分页
const changePage = (page) => {
     console.log("Changing page to:", page); //  1: 打印要跳转的页码
    if (page < 1 || page > totalPages.value) {
        console.warn("Invalid page number:", page);
        return;
    }
    currentPage.value = page;
    console.log("Current page after change:", currentPage.value); // 2: 打印当前页码
    fetchQuestions();
};

// 发布问题
async function publishQuestion() {
    
    if (!isLoggedIn.value) {
        publishMessage.value = '请先登录！';
        return;
    }  

    try {
        const currentUserId = user.value?.userId;

        if (!currentUserId) {
          alert('无法获取用户信息，请重新登录后再发布问题！');
          return;
        }

        const requestBody = {
            title: newQuestion.value.title,
            content: newQuestion.value.content,
            farmerId: currentUserId //  提问者id就是用户id
        };

        const response = await axios.post('/expert-questions', requestBody);

        if (response.data) {
            publishMessage.value = '问题发布成功！';
            newQuestion.value = { title: '', content: '' }; // 清空表单
            fetchQuestions(); // 刷新列表
        } else {
            publishMessage.value = '问题发布失败。';
        }
    } catch (error) {
        console.error('发布问题失败', error);
        publishMessage.value = '发布问题时发生错误。'; // 错误信息
    }
    setTimeout(() => {
        publishMessage.value = ''; // 清空消息
    }, 3000);
};

// 搜索问题
const searchQuestions = () => {
    currentPage.value = 1; // 搜索时重置页码
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

  const requestBody = {  // 创建一个变量保存请求体
      content: content,
      responderName: user.value?.userName,
      responderId: user.value?.userId 
  };

  console.log("请求体:", requestBody); 

  try {
    const response = await axios.post(`/expert-questions/${questionId}/answers`, {
      content: content,
      responderName: user.value?.userName ,//  回答者id是用户id
      responderId: user.value?.userId
    });

    if (response.data) {
      answerMessages.value[questionId] = '回答提交成功！';
      newAnswers.value[questionId] = ''; // 清空回答输入框
      fetchQuestions(); // 刷新问题列表
    } else {
      answerMessages.value[questionId] = '回答提交失败。';
    }
  } catch (error) {
    console.error('提交回答失败', error);
    answerMessages.value[questionId] = '提交回答时发生错误。';
  }
  setTimeout(() => {
    answerMessages.value[questionId] = ''; // 清空消息
  }, 3000);
};

// 采纳回答
const acceptAnswer = async (questionId, answerId) => {
    try {
        const response = await axios.post(`/expert-questions/${questionId}/accept/${answerId}`);
        if (response.data) {
            fetchQuestions();  // 刷新问题列表
        } else {
            alert('采纳失败'); //  提示用户
        }
    } catch (error) {
        console.error('采纳回答失败', error);
        alert('采纳回答时发生错误'); //  提示用户
    }
};

onMounted(() => {
  fetchQuestions();
});

</script>

<style scoped>
.main-bg {
  display: flex;
  flex-direction: column;
  height: 100vh;
  width: 1800px;
  background-color: #F0F9F4; /* 浅绿色背景色 */
  border-radius: 8px;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

nav ul {
  list-style: none;
  display: flex;
  padding: 0;
  margin: 0;
}

nav li {
  margin-right: 50px;
}

nav a {
  text-decoration: none;
  color: white;
  font-weight: 600;
  font-size: 20px;
  transition: color 0.3s;
}

nav a:hover {
  color: #B7E4C7; /* 鼠标悬停时变为淡绿色 */
}

.content {
  width: 100%;
  flex: 1;
  padding: 20px;
  background: white;
  color: #333; /* 深灰色文字 */
  font-size: 18px;
  border-radius: 8px;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}


.content h2 {
  color: #2D7D4F; /* 深绿色标题 */
  font-weight: 700;
  margin-bottom: 10px;
}

.expert-questions {
  padding: 20px;
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
</style>
