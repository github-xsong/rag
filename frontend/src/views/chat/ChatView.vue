<template>
  <div class="chat-page">
    <!-- 会话列表侧栏 -->
    <div class="session-panel">
      <div class="session-header">
        <span class="session-title">对话记录</span>
        <el-button type="primary" :icon="Plus" circle size="small" @click="startNewChat" />
      </div>

      <div class="kb-selector">
        <el-select v-model="currentKbId" placeholder="选择知识库" size="small" @change="onKbChange">
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

      <div class="session-list">
        <div
          v-for="session in sessions"
          :key="session.id"
          class="session-item"
          :class="{ active: currentSessionId === session.id }"
          @click="selectSession(session)"
        >
          <el-icon :size="16"><ChatDotRound /></el-icon>
          <span class="session-name">{{ session.title }}</span>
          <el-icon
            class="session-delete"
            :size="14"
            @click.stop="removeSession(session.id)"
          >
            <Close />
          </el-icon>
        </div>
        <div v-if="sessions.length === 0" class="session-empty">
          暂无对话记录
        </div>
      </div>
    </div>

    <!-- 对话主区域 -->
    <div class="chat-main">
      <div v-if="!currentSessionId && messages.length === 0" class="chat-welcome">
        <div class="welcome-icon">
          <el-icon :size="48"><ChatDotRound /></el-icon>
        </div>
        <h3>基于知识库的智能问答</h3>
        <p>选择一个知识库，开始提问</p>
      </div>

      <div v-else class="chat-messages" ref="messagesRef">
        <div
          v-for="msg in messages"
          :key="msg.id"
          class="message-row"
          :class="msg.role === 'USER' ? 'user' : 'assistant'"
        >
          <div class="message-avatar">
            <span v-if="msg.role === 'USER'">U</span>
            <span v-else>AI</span>
          </div>
          <div class="message-body">
            <div
              v-if="msg.role === 'ASSISTANT'"
              class="message-content markdown-body"
              v-html="renderMarkdown(msg.content)"
            />
            <div v-else class="message-content">{{ msg.content }}</div>
            <div v-if="msg.sources && msg.sources.length > 0" class="message-sources">
              <div class="sources-label" @click="msg._showSources = !msg._showSources">
                <el-icon :size="14"><Document /></el-icon>
                引用了 {{ msg.sources.length }} 个片段
                <el-icon :size="12"><ArrowDown v-if="!msg._showSources" /><ArrowUp v-else /></el-icon>
              </div>
              <div v-if="msg._showSources" class="sources-list">
                <div v-for="(src, idx) in msg.sources" :key="idx" class="source-item">
                  <span class="source-file">{{ src.fileName }}</span>
                  <span class="source-text">{{ src.content }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div v-if="loading" class="message-row assistant">
          <div class="message-avatar"><span>AI</span></div>
          <div class="message-body">
            <div class="message-content typing">
              <span></span><span></span><span></span>
            </div>
          </div>
        </div>
      </div>

      <!-- 输入区域 -->
      <div class="chat-input-area">
        <div class="input-wrapper">
          <el-input
            v-model="inputText"
            type="textarea"
            :autosize="{ minRows: 1, maxRows: 4 }"
            placeholder="输入你的问题..."
            resize="none"
            @keydown.enter.exact.prevent="sendMessage"
          />
          <el-button
            class="send-btn"
            type="primary"
            :icon="Promotion"
            :loading="loading"
            :disabled="!inputText.trim() || !currentKbId"
            circle
            @click="sendMessage"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { Plus, Promotion, Close, ArrowDown, ArrowUp } from '@element-plus/icons-vue'
import { ElMessageBox } from 'element-plus'
import { listKnowledgeBases } from '@/api/knowledge'
import { sendMessage as sendChatMessage, listSessions, getSessionMessages, deleteSession } from '@/api/chat'
import MarkdownIt from 'markdown-it'
import hljs from 'highlight.js'
import 'highlight.js/styles/github-dark.css'

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

onMounted(async () => {
  try {
    const res = await listKnowledgeBases()
    knowledgeBases.value = res.data || []
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
      message: text
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
  width: 280px;
  min-width: 280px;
  background: #fff;
  border-right: 1px solid var(--border-color);
  display: flex;
  flex-direction: column;
}

.session-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 16px 12px;
}

.session-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
}

.kb-selector {
  padding: 0 16px 12px;

  .el-select { width: 100%; }
}

.session-list {
  flex: 1;
  overflow-y: auto;
  padding: 0 8px;
}

.session-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  border-radius: 8px;
  cursor: pointer;
  color: var(--text-secondary);
  transition: all 0.15s;

  &:hover {
    background: #f5f7fa;
    .session-delete { opacity: 1; }
  }

  &.active {
    background: #eef2ff;
    color: var(--primary);
  }
}

.session-name {
  flex: 1;
  font-size: 13px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.session-delete {
  opacity: 0;
  color: var(--text-muted);
  transition: opacity 0.15s;

  &:hover { color: #f56c6c; }
}

.session-empty {
  text-align: center;
  padding: 40px 0;
  color: var(--text-muted);
  font-size: 13px;
}

// 对话主区域
.chat-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.chat-welcome {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: var(--text-muted);

  .welcome-icon {
    width: 80px;
    height: 80px;
    border-radius: 20px;
    background: #eef2ff;
    display: flex;
    align-items: center;
    justify-content: center;
    color: var(--primary);
    margin-bottom: 20px;
  }

  h3 {
    font-size: 18px;
    color: var(--text-primary);
    margin-bottom: 8px;
    font-weight: 500;
  }

  p { font-size: 14px; }
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 24px 0;
}

.message-row {
  display: flex;
  gap: 12px;
  padding: 8px 32px;
  max-width: 900px;
  margin: 0 auto;
  width: 100%;

  &.user {
    flex-direction: row-reverse;

    .message-avatar span {
      background: var(--primary);
      color: #fff;
    }

    .message-content {
      background: var(--primary);
      color: #fff;
      border-radius: 16px 4px 16px 16px;
    }
  }

  &.assistant {
    .message-avatar span {
      background: #e8eaf6;
      color: var(--primary);
    }

    .message-content {
      background: #fff;
      border: 1px solid #f0f0f0;
      border-radius: 4px 16px 16px 16px;
    }
  }
}

.message-avatar {
  flex-shrink: 0;

  span {
    width: 36px;
    height: 36px;
    border-radius: 10px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 13px;
    font-weight: 600;
  }
}

.message-body {
  max-width: 70%;
  min-width: 60px;
}

.message-content {
  padding: 12px 16px;
  font-size: 14px;
  line-height: 1.7;
  word-break: break-word;
}

.message-sources {
  margin-top: 8px;
}

.sources-label {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: var(--text-muted);
  cursor: pointer;
  padding: 4px 0;

  &:hover { color: var(--primary); }
}

.sources-list {
  margin-top: 6px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.source-item {
  background: #f9fafb;
  border: 1px solid #f0f0f0;
  border-radius: 8px;
  padding: 8px 12px;
  font-size: 12px;
}

.source-file {
  display: block;
  font-weight: 500;
  color: var(--primary);
  margin-bottom: 4px;
}

.source-text {
  color: var(--text-secondary);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

// 打字动画
.typing {
  display: flex;
  gap: 4px;
  padding: 14px 18px !important;

  span {
    width: 8px;
    height: 8px;
    background: #c0c4cc;
    border-radius: 50%;
    animation: typing 1.2s infinite ease-in-out;

    &:nth-child(2) { animation-delay: 0.2s; }
    &:nth-child(3) { animation-delay: 0.4s; }
  }
}

@keyframes typing {
  0%, 60%, 100% { transform: translateY(0); opacity: 0.4; }
  30% { transform: translateY(-6px); opacity: 1; }
}

// 输入区域
.chat-input-area {
  padding: 16px 32px 24px;
  background: var(--bg-page);
}

.input-wrapper {
  max-width: 836px;
  margin: 0 auto;
  display: flex;
  align-items: flex-end;
  gap: 12px;
  background: #fff;
  border: 1px solid var(--border-color);
  border-radius: 14px;
  padding: 8px 8px 8px 16px;
  box-shadow: var(--shadow-sm);
  transition: border-color 0.2s;

  &:focus-within {
    border-color: var(--primary-light);
    box-shadow: 0 0 0 2px rgba(92, 107, 192, 0.1);
  }

  :deep(.el-textarea__inner) {
    box-shadow: none !important;
    border: none;
    padding: 6px 0;
    font-size: 14px;
    background: transparent;
  }
}

.send-btn {
  flex-shrink: 0;
}
</style>
