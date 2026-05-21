import { createRouter, createWebHistory } from 'vue-router'
import DesignerPage from '@/views/DesignerPage.vue'
import PreviewPage from '@/views/PreviewPage.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/index',
      name: 'Designer',
      component: DesignerPage,
    },
    {
      path: '/preview',
      name: 'Preview',
      component: PreviewPage,
    },
  ],
})

export default router
