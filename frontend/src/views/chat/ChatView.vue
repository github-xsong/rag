<template>
  <div class="chat-page">
    <!-- 会话列表侧栏 -->
    <div class="session-panel">
      <div class="session-header">
        <span class="session-title">对话记录</span>
        <button class="new-chat-btn" @click="startNewChat">
          <el-icon :size="16"><Plus /></el-icon>
          新建对话
        </button>
      </div>

      <div class="selector-group">
        <div class="selector-item">
          <label class="selector-label">知识库</label>
          <el-select v-model="currentKbId" placeholder="选择知识库" size="default" @change="onKbChange">
            <el-option
              v-for="kb in knowledgeBases"
              :key="kb.id"
              :label="kb.name"
              :value="kb.id"
            >
              <span>{{ kb.name }}</span>
              <el-tag v-if="kb.isDefault" size="small" type="primary" style="margin-left:8px">默认</el-tag>
            </el-option>
          </el-select>
        </div>

        <div class="selector-item">
          <label class="selector-label">模型</label>
          <el-select v-model="currentModelName" placeholder="选择模型" size="default">
            <el-option
              v-for="model in models"
              :key="model.id"
              :label="model.name"
              :value="model.name"
            >
              <span>{{ model.name }}</span>
              <el-tag v-if="model.isDefault" size="small" type="success" style="margin-left:8px">默认</el-tag>
            </el-option>
          </el-select>
          <div v-if="models.length === 0" class="model-hint">
            请先在「模型管理」中添加模型
          </div>
        </div>
      </div>

      <div class="session-divider">
        <span>历史记录</span>
      </div>

      <div class="session-list">
        <div
          v-for="session in sessions"
          :key="session.id"
          class="session-item"
          :class="{ active: currentSessionId === session.id }"
          @click="selectSession(session)"
        >
          <div class="session-icon">
            <el-icon :size="16"><ChatDotRound /></el-icon>
          </div>
          <span class="session-name">{{ session.title }}</span>
          <button
            class="session-delete"
            @click.stop="removeSession(session.id)"
          >
            <el-icon :size="14"><Close /></el-icon>
          </button>
        </div>
        <div v-if="sessions.length === 0" class="session-empty">
          <el-icon :size="32" class="empty-icon"><ChatDotRound /></el-icon>
          <span>暂无对话记录</span>
        </div>
      </div>
    </div>

    <!-- 对话主区域 -->
    <div class="chat-main">
      <div v-if="!currentSessionId && messages.length === 0" class="chat-welcome">
        <div class="welcome-icon">
          <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
            <path d="M12 2L2 7l10 5 10-5-10-5z"/>
            <path d="M2 17l10 5 10-5"/>
            <path d="M2 12l10 5 10-5"/>
          </svg>
        </div>
        <h3>基于知识库的智能问答</h3>
        <p>选择一个知识库，开始提问</p>
        <div class="welcome-tips">
          <div class="tip-item">
            <el-icon :size="16"><Document /></el-icon>
            <span>支持多种文档格式</span>
          </div>
          <div class="tip-item">
            <el-icon :size="16"><Search /></el-icon>
            <span>智能语义检索</span>
          </div>
          <div class="tip-item">
            <el-icon :size="16"><ChatDotRound /></el-icon>
            <span>多轮对话记忆</span>
          </div>
        </div>
      </div>

      <div v-else class="chat-messages" ref="messagesRef">
        <div
          v-for="msg in messages"
          :key="msg.id"
          class="message-row animate-fade-up"
          :class="msg.role === 'USER' ? 'user' : 'assistant'"
        >
          <div class="message-avatar">
            <span v-if="msg.role === 'USER'">U</span>
            <span v-else class="ai-avatar">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M12 2L2 7l10 5 10-5-10-5z"/>
                <path d="M2 17l10 5 10-5"/>
                <path d="M2 12l10 5 10-5"/>
              </svg>
            </span>
          </div>
          <div class="message-body">
            <div
              v-if="msg.role === 'ASSISTANT'"
              class="message-content markdown-body"
              v-html="renderMarkdown(msg.content)"
            />
            <div v-else class="message-content">{{ msg.content }}</div>
            <div v-if="msg.role === 'ASSISTANT' && msg.modelName" class="message-model">
              <el-icon :size="12"><Cpu /></el-icon>
              {{ msg.modelName }}
            </div>
            <div v-if="msg.sources && msg.sources.length > 0" class="message-sources">
              <div class="sources-label" @click="msg._showSources = !msg._showSources">
                <el-icon :size="14"><Document /></el-icon>
                引用了 {{ msg.sources.length }} 个片段
                <el-icon :size="12" class="arrow-icon" :class="{ expanded: msg._showSources }">
                  <ArrowDown />
                </el-icon>
              </div>
              <transition name="sources-expand">
                <div v-if="msg._showSources" class="sources-list">
                  <div v-for="(src, idx) in msg.sources" :key="idx" class="source-item">
                    <span class="source-file">
                      <el-icon :size="12"><Document /></el-icon>
                      {{ src.fileName }}
                    </span>
                    <span class="source-text">{{ src.content }}</span>
                  </div>
                </div>
              </transition>
            </div>
          </div>
        </div>

        <div v-if="loading" class="message-row assistant">
          <div class="message-avatar">
            <span class="ai-avatar">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M12 2L2 7l10 5 10-5-10-5z"/>
                <path d="M2 17l10 5 10-5"/>
                <path d="M2 12l10 5 10-5"/>
              </svg>
            </span>
          </div>
          <div class="message-body">
            <div class="message-content typing">
              <span></span><span></span><span></span>
            </div>
          </div>
        </div>
      </div>

      <!-- 输入区域 -->
      <div class="chat-input-area">
        <div class="input-wrapper" :class="{ focused: inputFocused }">
          <el-input
            v-model="inputText"
            type="textarea"
            :autosize="{ minRows: 1, maxRows: 4 }"
            placeholder="输入你的问题..."
            resize="none"
            @keydown.enter.exact.prevent="sendMessage"
            @focus="inputFocused = true"
            @blur="inputFocused = false"
          />
          <div class="input-actions">
            <button
              class="send-btn"
              :class="{ active: inputText.trim() && currentKbId }"
              :disabled="!inputText.trim() || !currentKbId || loading"
              @click="sendMessage"
            >
              <el-icon v-if="!loading" :size="18"><Promotion /></el-icon>
              <el-icon v-else :size="18" class="loading-icon"><Loading /></el-icon>
            </button>
          </div>
        </div>
        <div class="input-hint">
          <span>Enter 发送</span>
          <span class="divider">·</span>
          <span>Shift + Enter 换行</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { Plus, Promotion, Close, ArrowDown, ArrowUp, Search, Loading, Cpu } from '@element-plus/icons-vue'
import { ElMessageBox } from 'element-plus'
import { listKnowledgeBases } from '@/api/knowledge'
import { sendMessage as sendChatMessage, listSessions, getSessionMessages, deleteSession } from '@/api/chat'
import { listEnabledModels } from '@/api/model'
import MarkdownIt from 'markdown-it'
import hljs from 'highlight.js'
import 'highlight.js/styles/github-dark.css'

const inputFocused = ref(false)

const md = new MarkdownIt({
  html: false,
  linkify: true,
  highlight(str, lang) {
    if (lang && hljs.getLanguage(lang)) {
      try { return hljs.highlight(str, { language: lang }).value } catch (_) {}
    }
    return ''
  }
})

const knowledgeBases = ref([])
const currentKbId = ref(null)
const sessions = ref([])
const currentSessionId = ref(null)
const messages = ref([])
const inputText = ref('')
const loading = ref(false)
const messagesRef = ref(null)
const models = ref([])
const currentModelName = ref(null)

onMounted(async () => {
  try {
    const [kbRes, modelRes] = await Promise.all([
      listKnowledgeBases(),
      listEnabledModels()
    ])
    knowledgeBases.value = kbRes.data || []
    models.value = modelRes.data || []
    
    const defaultModel = models.value.find(m => m.isDefault)
    if (defaultModel) {
      currentModelName.value = defaultModel.name
    } else if (models.value.length > 0) {
      currentModelName.value = models.value[0].name
    }
    
    const defaultKb = knowledgeBases.value.find(kb => kb.isDefault)
    if (defaultKb) {
      currentKbId.value = defaultKb.id
      await loadSessions()
    } else if (knowledgeBases.value.length > 0) {
      currentKbId.value = knowledgeBases.value[0].id
      await loadSessions()
    }
  } catch (_) {}
})

async function loadSessions() {
  if (!currentKbId.value) return
  try {
    const res = await listSessions(currentKbId.value)
    sessions.value = res.data || []
  } catch (_) {}
}

async function onKbChange() {
  currentSessionId.value = null
  messages.value = []
  await loadSessions()
}

async function selectSession(session) {
  currentSessionId.value = session.id
  try {
    const res = await getSessionMessages(session.id)
    messages.value = (res.data || []).map(m => ({ ...m, _showSources: false }))
    scrollToBottom()
  } catch (_) {}
}

function startNewChat() {
  currentSessionId.value = null
  messages.value = []
}

async function removeSession(sessionId) {
  try {
    await ElMessageBox.confirm('确定删除这个对话吗？', '提示', { type: 'warning' })
    await deleteSession(sessionId)
    sessions.value = sessions.value.filter(s => s.id !== sessionId)
    if (currentSessionId.value === sessionId) {
      currentSessionId.value = null
      messages.value = []
    }
  } catch (_) {}
}

async function sendMessage() {
  const text = inputText.value.trim()
  if (!text || !currentKbId.value || loading.value) return

  messages.value.push({ id: Date.now(), role: 'USER', content: text })
  inputText.value = ''
  loading.value = true
  scrollToBottom()

  try {
    const res = await sendChatMessage({
      sessionId: currentSessionId.value,
      knowledgeBaseId: currentKbId.value,
      message: text,
      modelName: currentModelName.value
    })
    const data = res.data
    if (!currentSessionId.value && data.sessionId) {
      currentSessionId.value = data.sessionId
      await loadSessions()
    }
    messages.value.push({
      id: Date.now() + 1,
      role: 'ASSISTANT',
      content: data.answer,
      sources: data.sources,
      modelName: data.modelName,
      _showSources: false
    })
  } catch (_) {
    messages.value.push({
      id: Date.now() + 1,
      role: 'ASSISTANT',
      content: '抱歉，请求出现了问题，请稍后重试。'
    })
  } finally {
    loading.value = false
    scrollToBottom()
  }
}

function renderMarkdown(text) {
  if (!text) return ''
  return md.render(text)
}

function scrollToBottom() {
  nextTick(() => {
    if (messagesRef.value) {
      messagesRef.value.scrollTop = messagesRef.value.scrollHeight
    }
  })
}
</script>

<style lang="scss" scoped>
.chat-page {
  display: flex;
  height: 100%;
}

// 会话侧栏
.session-panel {
  width: 300px;
  min-width: 300px;
  background: var(--bg-primary);
  border-right: 1px solid var(--border-color);
  display: flex;
  flex-direction: column;
}

.session-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 16px 16px;
}

.session-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
}

.new-chat-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 14px;
  background: linear-gradient(135deg, var(--primary) 0%, var(--primary-dark) 100%);
  color: #fff;
  border: none;
  border-radius: var(--radius-sm);
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all var(--transition-fast);
  box-shadow: 0 2px 8px rgba(59, 130, 246, 0.3);

  &:hover {
    transform: translateY(-1px);
    box-shadow: 0 4px 12px rgba(59, 130, 246, 0.4);
  }

  &:active {
    transform: translateY(0);
  }
}

.selector-group {
  padding: 0 16px 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.selector-item {
  display: flex;
  flex-direction: column;
  gap: 6px;

  .el-select { width: 100%; }
}

.selector-label {
  font-size: 12px;
  font-weight: 500;
  color: var(--text-tertiary);
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.model-hint {
  font-size: 12px;
  color: var(--text-muted);
  margin-top: 4px;
  padding: 8px 12px;
  background: var(--bg-tertiary);
  border-radius: var(--radius-sm);
  text-align: center;
}

.session-divider {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  border-top: 1px solid var(--border-color);

  span {
    font-size: 11px;
    font-weight: 600;
    color: var(--text-muted);
    text-transform: uppercase;
    letter-spacing: 0.5px;
  }
}

.session-list {
  flex: 1;
  overflow-y: auto;
  padding: 0 8px 16px;
}

.session-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  border-radius: var(--radius-md);
  cursor: pointer;
  color: var(--text-secondary);
  transition: all var(--transition-fast);

  &:hover {
    background: var(--sidebar-item-hover);
    
    .session-delete { 
      opacity: 1;
      transform: scale(1);
    }
  }

  &.active {
    background: var(--sidebar-item-active);
    color: var(--primary);

    .session-icon {
      color: var(--primary);
    }
  }
}

.session-icon {
  flex-shrink: 0;
  color: var(--text-muted);
}

.session-name {
  flex: 1;
  font-size: 14px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.session-delete {
  opacity: 0;
  transform: scale(0.8);
  background: none;
  border: none;
  padding: 4px;
  border-radius: var(--radius-xs);
  color: var(--text-muted);
  cursor: pointer;
  transition: all var(--transition-fast);

  &:hover { 
    color: var(--error);
    background: rgba(239, 68, 68, 0.1);
  }
}

.session-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 40px 0;
  color: var(--text-muted);
  font-size: 13px;

  .empty-icon {
    opacity: 0.3;
  }
}

// 对话主区域
.chat-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background: var(--bg-secondary);
}

.chat-welcome {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: var(--text-muted);
  animation: fadeUp 0.5s ease-out;

  .welcome-icon {
    width: 88px;
    height: 88px;
    border-radius: var(--radius-xl);
    background: linear-gradient(135deg, var(--primary-bg) 0%, var(--primary-bg-light) 100%);
    display: flex;
    align-items: center;
    justify-content: center;
    color: var(--primary);
    margin-bottom: 24px;
    box-shadow: 0 8px 24px rgba(59, 130, 246, 0.15);
  }

  h3 {
    font-size: 20px;
    color: var(--text-primary);
    margin-bottom: 8px;
    font-weight: 600;
  }

  p { 
    font-size: 14px;
    margin-bottom: 32px;
  }
}

.welcome-tips {
  display: flex;
  gap: 24px;
}

.tip-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 20px;
  background: var(--bg-primary);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  font-size: 13px;
  color: var(--text-secondary);
  transition: all var(--transition-fast);

  .el-icon {
    color: var(--primary);
  }

  &:hover {
    border-color: var(--primary-bg);
    background: var(--primary-bg-light);
  }
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 24px 0;
}

.message-row {
  display: flex;
  gap: 12px;
  padding: 12px 32px;
  max-width: 900px;
  margin: 0 auto;
  width: 100%;

  &.user {
    flex-direction: row-reverse;

    .message-avatar span {
      background: linear-gradient(135deg, var(--primary) 0%, var(--primary-dark) 100%);
      color: #fff;
    }

    .message-content {
      background: linear-gradient(135deg, var(--primary) 0%, var(--primary-dark) 100%);
      color: #fff;
      border-radius: var(--radius-lg) var(--radius-xs) var(--radius-lg) var(--radius-lg);
      box-shadow: 0 2px 8px rgba(59, 130, 246, 0.2);
    }
  }

  &.assistant {
    .message-avatar span {
      background: var(--primary-bg);
      color: var(--primary);
    }

    .message-content {
      background: var(--bg-primary);
      border: 1px solid var(--border-color);
      border-radius: var(--radius-xs) var(--radius-lg) var(--radius-lg) var(--radius-lg);
      box-shadow: var(--shadow-sm);
    }
  }
}

.message-avatar {
  flex-shrink: 0;

  span {
    width: 38px;
    height: 38px;
    border-radius: var(--radius-md);
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 13px;
    font-weight: 600;
  }

  .ai-avatar {
    background: var(--primary-bg);
    color: var(--primary);
  }
}

.message-body {
  max-width: 70%;
  min-width: 60px;
}

.message-content {
  padding: 14px 18px;
  font-size: 14px;
  line-height: 1.75;
  word-break: break-word;
}

.message-sources {
  margin-top: 12px;
}

.sources-label {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: var(--text-muted);
  cursor: pointer;
  padding: 6px 12px;
  background: var(--bg-tertiary);
  border-radius: var(--radius-sm);
  transition: all var(--transition-fast);

  &:hover { 
    color: var(--primary);
    background: var(--primary-bg-light);
  }

  .arrow-icon {
    transition: transform var(--transition-fast);

    &.expanded {
      transform: rotate(180deg);
    }
  }
}

.sources-expand-enter-active,
.sources-expand-leave-active {
  transition: all 0.3s ease;
}

.sources-expand-enter-from,
.sources-expand-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}

.sources-list {
  margin-top: 10px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.source-item {
  background: var(--bg-secondary);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-sm);
  padding: 12px 14px;
  font-size: 12px;
  transition: all var(--transition-fast);

  &:hover {
    border-color: var(--primary-bg);
    background: var(--primary-bg-light);
  }
}

.source-file {
  display: flex;
  align-items: center;
  gap: 6px;
  font-weight: 500;
  color: var(--primary);
  margin-bottom: 6px;
}

.source-text {
  color: var(--text-secondary);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  line-height: 1.6;
}

.message-model {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 11px;
  color: var(--text-muted);
  margin-top: 8px;
  padding: 4px 8px;
  background: var(--bg-tertiary);
  border-radius: var(--radius-xs);
}

// 打字动画
.typing {
  display: flex;
  gap: 6px;
  padding: 16px 20px !important;

  span {
    width: 8px;
    height: 8px;
    background: var(--primary);
    border-radius: 50%;
    animation: typing 1.2s infinite ease-in-out;

    &:nth-child(2) { animation-delay: 0.2s; }
    &:nth-child(3) { animation-delay: 0.4s; }
  }
}

@keyframes typing {
  0%, 60%, 100% { transform: translateY(0); opacity: 0.3; }
  30% { transform: translateY(-8px); opacity: 1; }
}

// 输入区域
.chat-input-area {
  padding: 16px 32px 24px;
  background: var(--bg-secondary);
}

.input-wrapper {
  max-width: 836px;
  margin: 0 auto;
  display: flex;
  align-items: flex-end;
  gap: 12px;
  background: var(--bg-primary);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  padding: 10px 10px 10px 18px;
  box-shadow: var(--shadow-sm);
  transition: all var(--transition-fast);

  &:hover {
    border-color: #d1d5db;
  }

  &.focused {
    border-color: var(--primary);
    box-shadow: var(--shadow-glow);
  }

  :deep(.el-textarea__inner) {
    box-shadow: none !important;
    border: none;
    padding: 6px 0;
    font-size: 15px;
    background: transparent;
    color: var(--text-primary);

    &::placeholder {
      color: var(--text-muted);
    }
  }
}

.input-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.send-btn {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--bg-tertiary);
  border: none;
  border-radius: var(--radius-md);
  color: var(--text-muted);
  cursor: pointer;
  transition: all var(--transition-fast);

  &:hover:not(:disabled) {
    background: var(--bg-hover);
  }

  &.active {
    background: linear-gradient(135deg, var(--primary) 0%, var(--primary-dark) 100%);
    color: #fff;
    box-shadow: 0 2px 8px rgba(59, 130, 246, 0.3);

    &:hover {
      transform: scale(1.05);
      box-shadow: 0 4px 12px rgba(59, 130, 246, 0.4);
    }
  }

  &:disabled {
    cursor: not-allowed;
    opacity: 0.5;
  }

  .loading-icon {
    animation: spin 1s linear infinite;
  }
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.input-hint {
  max-width: 836px;
  margin: 8px auto 0;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  font-size: 12px;
  color: var(--text-muted);

  .divider {
    margin: 0 4px;
  }
}
</style>
