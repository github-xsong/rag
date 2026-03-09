<template>
  <div class="app-layout">
    <aside class="sidebar">
      <div class="sidebar-logo">
        <div class="logo-icon">R</div>
        <span class="logo-text">RAG</span>
      </div>

      <nav class="sidebar-nav">
        <router-link
          v-for="item in menuItems"
          :key="item.path"
          :to="item.path"
          class="nav-item"
          :class="{ active: isActive(item.path) }"
        >
          <el-icon :size="20"><component :is="item.icon" /></el-icon>
          <span>{{ item.title }}</span>
        </router-link>
      </nav>

      <div class="sidebar-footer">
        <a href="https://github.com" target="_blank" class="nav-item">
          <el-icon :size="18"><Link /></el-icon>
          <span>GitHub</span>
        </a>
      </div>
    </aside>

    <main class="main-content">
      <router-view />
    </main>
  </div>
</template>

<script setup>
import { useRoute } from 'vue-router'

const route = useRoute()

const menuItems = [
  { path: '/chat', title: 'AI 对话', icon: 'ChatDotRound' },
  { path: '/knowledge', title: '知识库', icon: 'Collection' },
  { path: '/models', title: '模型管理', icon: 'Cpu' },
  { path: '/api-keys', title: '开放接口', icon: 'Key' }
]

function isActive(path) {
  if (path === '/knowledge') {
    return route.path.startsWith('/knowledge')
  }
  return route.path === path
}
</script>

<style lang="scss" scoped>
.app-layout {
  display: flex;
  height: 100vh;
  overflow: hidden;
}

.sidebar {
  width: 220px;
  min-width: 220px;
  background: var(--sidebar-bg);
  display: flex;
  flex-direction: column;
  color: #a0aec0;
}

.sidebar-logo {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 24px 20px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
}

.logo-icon {
  width: 36px;
  height: 36px;
  background: var(--primary);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 18px;
  font-weight: 700;
}

.logo-text {
  font-size: 20px;
  font-weight: 700;
  color: #fff;
  letter-spacing: 1px;
}

.sidebar-nav {
  flex: 1;
  padding: 12px 10px;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.sidebar-footer {
  padding: 12px 10px 20px;
  border-top: 1px solid rgba(255, 255, 255, 0.06);
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  border-radius: 8px;
  text-decoration: none;
  color: #8b95a5;
  font-size: 14px;
  transition: all 0.2s ease;
  cursor: pointer;

  &:hover {
    background: rgba(255, 255, 255, 0.06);
    color: #d0d7e2;
  }

  &.active {
    background: var(--primary);
    color: #fff;
    font-weight: 500;
  }
}

.main-content {
  flex: 1;
  overflow: hidden;
  background: var(--bg-page);
}
</style>
