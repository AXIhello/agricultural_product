import { createRouter, createWebHistory } from 'vue-router'
import LoginAndRegister from '../views/LoginAndRegister.vue'
import MainInterface from '../views/MainInterface.vue'
import ProductList from '../views/ProductList.vue'
import TradingPage from '../views/TradingPage.vue';

const routes = [
  {
    path: '/',
    redirect: '/auth'
  },

  {
    path: '/auth',
    name: 'LoginAndRegister',
    component: LoginAndRegister
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
  }
  
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router