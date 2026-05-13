import { createRouter, createWebHistory } from 'vue-router'

export const rootPath = '/report'

const routes = [
  {
    path: '/',
    redirect: rootPath + '/designer',
  },
  {
    path: rootPath + '/preview',
    name: 'Preview',
    component: () => import('@/views/report/preview/index.vue'),
  },
  {
    path: rootPath + '/designer',
    name: 'Designer',
    component: () => import('@/views/report/designer/index.vue'),
  },
  {
    path: rootPath + '/searchFormDesigner',
    name: 'searchFormDesigner',
    component: () => import('@/views/report/designer/search-form/index.vue'),
  },
]

const router = createRouter({
  history: createWebHistory((import.meta as Record<string, any>).env?.VITE_APP_PUBLIC_PATH || '/'),
  routes,
})

export default router
