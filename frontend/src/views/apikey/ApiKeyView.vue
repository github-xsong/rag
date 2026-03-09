<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <h2>开放接口</h2>
        <p class="page-subtitle">创建 API Key 后，外部系统可通过 HTTP 接口调用知识库问答能力</p>
      </div>
      <el-button type="primary" :icon="Plus" @click="openDialog">创建 API Key</el-button>
    </div>

    <!-- 使用说明 -->
    <div class="card usage-card">
      <div class="usage-title">调用方式</div>
      <div class="usage-code">
        <code>POST /api/open/chat</code>
        <br />
        <code>Header: X-API-Key: rag-xxxxxxxxxx</code>
        <br />
        <code>Body: { "message": "你的问题", "knowledgeBaseId": 1 }</code>
      </div>
    </div>

    <div class="card" style="padding:0;margin-top:20px">
      <el-table :data="apiKeys" style="width:100%" v-loading="loading" empty-text="暂无 API Key">
        <el-table-column prop="name" label="名称" min-width="150">
          <template #default="{ row }">
            <span style="font-weight:500">{{ row.name }}</span>
          </template>
        </el-table-column>

        <el-table-column label="API Key" min-width="300">
          <template #default="{ row }">
            <div class="key-cell">
              <code class="key-text">{{ maskKey(row.apiKey) }}</code>
              <el-button text size="small" :icon="CopyDocument" @click="copyKey(row.apiKey)" />
            </div>
          </template>
        </el-table-column>

        <el-table-column label="绑定知识库" width="140" align="center">
          <template #default="{ row }">
            {{ row.knowledgeBaseId || '全部' }}
          </template>
        </el-table-column>

        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-switch
              :model-value="row.enabled"
              size="small"
              @change="handleToggle(row.id)"
            />
          </template>
        </el-table-column>

        <el-table-column label="最后使用" width="170" align="center">
          <template #default="{ row }">
            <span class="text-muted">{{ row.lastUsedAt ? formatTime(row.lastUsedAt) : '从未使用' }}</span>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="80" align="center" fixed="right">
          <template #default="{ row }">
            <el-popconfirm title="确定删除此 API Key？" @confirm="handleDelete(row.id)">
              <template #reference>
                <el-button text size="small" type="danger" :icon="Delete" />
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 创建弹窗 -->
    <el-dialog v-model="dialogVisible" title="创建 API Key" width="460px" :close-on-click-modal="false">
      <el-form :model="form" label-position="top">
        <el-form-item label="名称" required>
          <el-input v-model="form.name" placeholder="如：外部系统对接" />
        </el-form-item>
        <el-form-item label="绑定知识库（可选）">
          <el-select v-model="form.knowledgeBaseId" placeholder="不绑定则可访问所有知识库" clearable style="width:100%">
            <el-option
              v-for="kb in knowledgeBases"
              :key="kb.id"
              :label="kb.name"
              :value="kb.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitForm">创建</el-button>
      </template>
    </el-dialog>

    <!-- 创建成功弹窗 -->
    <el-dialog v-model="successDialogVisible" title="API Key 已创建" width="460px" :close-on-click-modal="false">
      <el-alert type="warning" :closable="false" style="margin-bottom:16px">
        请立即复制保存，关闭后将无法再次查看完整 Key
      </el-alert>
      <el-input :model-value="newApiKey" readonly>
        <template #append>
          <el-button :icon="CopyDocument" @click="copyKey(newApiKey)" />
        </template>
      </el-input>
      <template #footer>
        <el-button type="primary" @click="successDialogVisible = false">我已保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Plus, Delete, CopyDocument } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { listApiKeys, createApiKey, toggleApiKey, deleteApiKey } from '@/api/apikey'
import { listKnowledgeBases } from '@/api/knowledge'

const apiKeys = ref([])
const knowledgeBases = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const successDialogVisible = ref(false)
const submitting = ref(false)
const newApiKey = ref('')
const form = ref({ name: '', knowledgeBaseId: null })

onMounted(async () => {
  await Promise.all([loadList(), loadKbs()])
})

async function loadList() {
  loading.value = true
  try {
    const res = await listApiKeys()
    apiKeys.value = res.data || []
  } finally {
    loading.value = false
  }
}

async function loadKbs() {
  try {
    const res = await listKnowledgeBases()
    knowledgeBases.value = res.data || []
  } catch (_) {}
}

function openDialog() {
  form.value = { name: '', knowledgeBaseId: null }
  dialogVisible.value = true
}

async function submitForm() {
  if (!form.value.name.trim()) {
    ElMessage.warning('请输入名称')
    return
  }
  submitting.value = true
  try {
    const res = await createApiKey(form.value)
    dialogVisible.value = false
    newApiKey.value = res.data?.apiKey || ''
    successDialogVisible.value = true
    await loadList()
  } finally {
    submitting.value = false
  }
}

async function handleToggle(id) {
  await toggleApiKey(id)
  await loadList()
}

async function handleDelete(id) {
  await deleteApiKey(id)
  ElMessage.success('已删除')
  await loadList()
}

function maskKey(key) {
  if (!key || key.length < 12) return key
  return key.substring(0, 8) + '••••••••' + key.substring(key.length - 4)
}

async function copyKey(key) {
  try {
    await navigator.clipboard.writeText(key)
    ElMessage.success('已复制到剪贴板')
  } catch (_) {
    ElMessage.error('复制失败')
  }
}

function formatTime(dateStr) {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleString('zh-CN')
}
</script>

<style lang="scss" scoped>
.page-subtitle {
  font-size: 13px;
  color: var(--text-muted);
  margin-top: 4px;
}

.usage-card {
  padding: 20px;
}

.usage-title {
  font-weight: 600;
  font-size: 14px;
  margin-bottom: 12px;
  color: var(--text-primary);
}

.usage-code {
  background: #1e1e2e;
  color: #cdd6f4;
  border-radius: 8px;
  padding: 16px;
  font-family: 'SFMono-Regular', Consolas, monospace;
  font-size: 13px;
  line-height: 1.8;
}

.key-cell {
  display: flex;
  align-items: center;
  gap: 4px;
}

.key-text {
  font-family: 'SFMono-Regular', Consolas, monospace;
  font-size: 13px;
  color: var(--text-secondary);
  background: #f5f7fa;
  padding: 2px 8px;
  border-radius: 4px;
}

.text-muted {
  color: var(--text-muted);
  font-size: 13px;
}
</style>
