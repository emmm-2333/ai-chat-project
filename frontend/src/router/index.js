import { createRouter, createWebHistory } from 'vue-router';

const Chat = () => import('../views/Chat.vue');

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', component: Chat },
    { path: '/chat', redirect: '/' }
  ]
});

export default router;
