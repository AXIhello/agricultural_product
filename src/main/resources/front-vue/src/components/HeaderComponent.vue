<template>
  <header class="header">
    <h1>农产品交易平台</h1>
    <nav>
      <ul>
        <li><router-link to="/main">首页</router-link></li>
        <li><router-link to="/finance">融资服务</router-link></li>
        <li><router-link to="/expert">专家助力</router-link></li>
        <li><router-link to="/trading">农产品交易</router-link></li>
        <li v-if="isAdmin"><router-link to="/admin-review">审核信息</router-link></li>
        <li><router-link to="/profile">个人信息</router-link></li>
        
      </ul>
    </nav>
  </header>
</template>

<style scoped>

.header {
  width: 100%;
  background: #2D7D4F; /* 深绿色背景色 */
  color: white;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  height: 60px;
  font-size: 15px;
  font-weight: 600;
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

.header h1 {
  margin: 0;
  font-size: 1.5em;
}

</style>

<script>
export default {
  name: 'HeaderComponent',
  data() {
    return {
      role: null,  
    };
  },
  computed: {
    isAdmin() {
      return this.role === 'admin';
    },
  },
  mounted() {
    const user = JSON.parse(localStorage.getItem('user'));
    if (user && user.role) { // 假设用户信息包含 'role' 字段
        this.role = user.role;
    }
  },
};
</script>