import { createRouter, createWebHistory } from 'vue-router'
import LoginAndRegister from '../views/LoginAndRegister.vue'
import MainInterface from '../views/MainInterface.vue'
import ProductList from '../views/ProductList.vue'
import TradingPage from '../views/TradingPage.vue'
import FinanceService from '../views/FinanceService.vue'
import ExpertAssistance from '../views/ExpertAssistance.vue'
import UserProfile from '../views/UserProfile.vue';
import OrderConfirm from "@/views/OrderConfirm.vue";
import UserApplication from "@/views/UserApplication.vue";
import AdminReview from "@/views/AdminReview.vue";
import ProductDetail from "@/views/ProductDetail.vue";
import Chat from "@/views/Chat.vue";
import MessageCenter from '../views/MessageCenter.vue'
import PayResult from '@/views/PayResult.vue'
import { useAuthStore } from '@/stores/authStore';

const routes = [
  {
    path: '/',
    redirect: '/login'
  },

  {
    path: '/login',
    name: 'LoginAndRegister',
    component: LoginAndRegister
  },

  {
    path: '/apply',
    name: 'UserApplication',
    component: UserApplication
  },

  {
    path: '/main',
    name: 'MainInterface',
    component: MainInterface,
    children: [  // 添加子路由
      {
        path: '/products',
        name: 'ProductList',
        component: ProductList
      },
      {
        path: '/products/:id',
        name: 'ProductDetail',
        component: () => import('../views/ProductDetail.vue')
      }
    ]
  },

  {
    path: '/trading',
    name: 'TradingPage',
    component: TradingPage
  },
  
  {
    path: '/finance',
    name: 'FinanceService',
    component: FinanceService
  },

  {
    path: '/expert',
    name: 'ExpertAssistance',
    component: ExpertAssistance
  },

  {
    path: '/profile',
    name: 'UserProfile',
    component: UserProfile
  },

  {
    path: '/order/confirm',
    name: 'OrderConfirm',
    component: OrderConfirm
  },

  {
    path:'/admin-review',
    name:'AdminReview',
    component: AdminReview,
    meta:{
      requiresAuth: true,    
      requiredRole: 'admin'//只有管理员可以访问
    }
  },

  {
      path: '/product/:id', // 使用动态路由参数 :id 来接收商品ID
      name: 'productDetail',
      component: ProductDetail,
      props: true // 这会将路由参数 :id 作为 props 传递给组件，更方便
    
  },
  {
    path: '/chat/:receiverId', // 使用动态参数 receiverId
    name: 'Chat',
    component: Chat,
    meta: { requiresAuth: true } // 如果需要登录才能访问，可以添加这个
  },
    {
    path: '/messages', // 定义消息中心的访问路径
    name: 'MessageCenter',
    component: MessageCenter,
    meta: { requiresAuth: true } // 强烈建议添加，确保只有登录用户能访问
  },
  {
    path: '/pay-result',
    name: 'PayResult',
    component: PayResult,
    meta: { requiresAuth: true }
  },

  {
    path: '/expert/:id', 
    name: 'ExpertDetail',
    component: () => import('../views/ExpertDetailPage.vue'), 
    props: true,
    meta: { requiresAuth: true } 
  }

]

const router = createRouter({
  history: createWebHistory(),
  routes
})

//创建全局导航守卫
router.beforeEach((to, from, next) => {
  // 在守卫内部获取 store 实例
  const authStore = useAuthStore();
  const { isLoggedIn, role } = authStore;

  const requiresAuth = to.matched.some(record => record.meta.requiresAuth);
  const requiredRole = to.meta.requiredRole;

  // 1. 检查页面是否需要登录
  if (requiresAuth && !isLoggedIn) {
    alert('请先登录后再访问此页面。');
    next('/login'); // 重定向到登录页
  } 
  // 2. 检查页面是否需要特定角色
  else if (requiredRole && role !== requiredRole) {
    alert('抱歉，您没有权限访问此页面。');
    // 留在当前页面，或者跳转到首页
    next(from.path || '/main'); 
  } 
  // 3. 如果一切正常，放行
  else {
    next();
  }
});

export default router