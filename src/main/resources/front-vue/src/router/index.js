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
    path: '/orders/:orderId',
    name: 'OrderConfirm',
    component: OrderConfirm
  },

  {
    path:'/admin-review',
    name:'AdminReview',
    component: AdminReview
  }

  
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router