<template>
  <div class="app-layout">
    <aside class="sidebar">
      <div class="sidebar-logo">
        <div class="logo-icon">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M12 2L2 7l10 5 10-5-10-5z"/>
            <path d="M2 17l10 5 10-5"/>
            <path d="M2 12l10 5 10-5"/>
          </svg>
        </div>
        <div class="logo-info">
          <span class="logo-text">RAG 智能问答</span>
          <span class="logo-sub">Powered by AI</span>
        </div>
      </div>

      <nav class="sidebar-nav">
        <router-link
          v-for="item in menuItems"
          :key="item.path"
          :to="item.path"
          class="nav-item"
          :class="{ active: isActive(item.path) }"
        >
          <el-icon :size="18"><component :is="item.icon" /></el-icon>
          <span>{{ item.title }}</span>
        </router-link>
      </nav>

      <div class="sidebar-footer">
        <a href="https://github.com" target="_blank" class="nav-item external">
          <el-icon :size="16"><Link /></el-icon>
          <span>GitHub</span>
          <svg class="external-icon" width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M18 13v6a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h6"/>
            <polyline points="15 3 21 3 21 9"/>
            <line x1="10" y1="14" x2="21" y2="3"/>
          </svg>
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
  width: 240px;
  min-width: 240px;
  background: var(--sidebar-bg);
  border-right: 1px solid var(--border-color);
  display: flex;
  flex-direction: column;
}

.sidebar-logo {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 20px 16px;
  border-bottom: 1px solid var(--border-color);
}

.logo-icon {
  width: 40px;
  height: 40px;
  background: linear-gradient(135deg, var(--primary) 0%, var(--primary-dark) 100%);
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.3);
}

.logo-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.logo-text {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
  letter-spacing: -0.3px;
}

.logo-sub {
  font-size: 11px;
  color: var(--text-muted);
}

.sidebar-nav {
  flex: 1;
  padding: 16px 12px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.sidebar-footer {
  padding: 12px;
  border-top: 1px solid var(--border-color);
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 14px;
  border-radius: var(--radius-md);
  text-decoration: none;
  color: var(--text-secondary);
  font-size: 14px;
  transition: all var(--transition-fast);
  cursor: pointer;

  &:hover {
    background: var(--sidebar-item-hover);
    color: var(--text-primary);
  }

  &.active {
    background: var(--sidebar-item-active);
    color: var(--primary);
    font-weight: 500;

    .el-icon {
      color: var(--primary);
    }
  }

  &.external {
    color: var(--text-muted);
    font-size: 13px;

    .external-icon {
      margin-left: auto;
      opacity: 0;
      transition: opacity var(--transition-fast);
    }

    &:hover {
      color: var(--text-secondary);
      
      .external-icon {
        opacity: 0.6;
      }
    }
  }
}

.main-content {
  flex: 1;
  overflow: hidden;
  background: var(--bg-page);
}
</style>
