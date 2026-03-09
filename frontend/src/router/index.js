import { createRouter, createWebHistory } from 'vue-router'
import AppLayout from '@/views/layout/AppLayout.vue'

const routes = [
  {
    path: '/',
    component: AppLayout,
    redirect: '/chat',
    children: [
      {
        path: 'chat',
        name: 'Chat',
        component: () => import('@/views/chat/ChatView.vue'),
        meta: { title: 'AI 对话', icon: 'ChatDotRound' }
      },
      {
        path: 'knowledge',
        name: 'Knowledge',
        component: () => import('@/views/knowledge/KnowledgeView.vue'),
        meta: { title: '知识库', icon: 'Collection' }
      },
      {
        path: 'knowledge/:id/documents',
        name: 'Documents',
        component: () => import('@/views/document/DocumentView.vue'),
        meta: { title: '文档管理', icon: 'Document', hidden: true }
      },
      {
        path: 'models',
        name: 'Models',
        component: () => import('@/views/model/ModelView.vue'),
        meta: { title: '模型管理', icon: 'Cpu' }
      },
      {
        path: 'api-keys',
        name: 'ApiKeys',
        component: () => import('@/views/apikey/ApiKeyView.vue'),
        meta: { title: '开放接口', icon: 'Key' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
