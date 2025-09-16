// src/router/index.js

import { createRouter, createWebHistory } from 'vue-router';
import LoginAndRegister from '../views/LoginAndRegister.vue';
import MainInterface from '../views/MainInterface.vue';

const routes = [
  {
    // 规则1：用户访问根目录时，重定向到 /auth
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
    component: MainInterface
  },
  
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

export default router; // 必须导出