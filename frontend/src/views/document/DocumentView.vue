<template>
  <div class="page-container">
    <div class="page-header">
      <div style="display:flex;align-items:center;gap:12px">
        <el-button :icon="ArrowLeft" text @click="$router.push('/knowledge')" />
        <h2>{{ knowledgeBaseName || '文档管理' }}</h2>
      </div>
      <el-upload
        :show-file-list="false"
        :before-upload="beforeUpload"
        :http-request="handleUpload"
        accept=".pdf,.doc,.docx,.png,.jpg,.jpeg"
        multiple
      >
        <el-button type="primary" :icon="Upload">上传文件</el-button>
      </el-upload>
    </div>

    <div class="card" style="padding: 0">
      <el-table :data="documents" style="width:100%" v-loading="loading" empty-text="暂无文件，点击上方按钮上传">
        <el-table-column label="文件名" min-width="240">
          <template #default="{ row }">
            <div class="file-name-cell">
              <el-icon :size="20" :style="{ color: fileIconColor(row.fileType) }">
                <component :is="fileIcon(row.fileType)" />
              </el-icon>
              <span>{{ row.fileName }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="fileTagType(row.fileType)" effect="light">
              {{ row.fileType.toUpperCase() }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="大小" width="120" align="center">
          <template #default="{ row }">{{ formatSize(row.fileSize) }}</template>
        </el-table-column>

        <el-table-column label="分块数" width="100" align="center">
          <template #default="{ row }">{{ row.chunkCount || '-' }}</template>
        </el-table-column>

        <el-table-column label="状态" width="120" align="center">
          <template #default="{ row }">
            <el-tag
              :type="statusType(row.status)"
              size="small"
              effect="light"
              round
            >
              <span v-if="row.status === 'PROCESSING'" class="status-dot processing" />
              {{ statusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="上传时间" width="170" align="center">
          <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
        </el-table-column>

        <el-table-column label="操作" width="80" align="center" fixed="right">
          <template #default="{ row }">
            <el-popconfirm title="确定删除此文件？" @confirm="handleDelete(row.id)">
              <template #reference>
                <el-button type="danger" text size="small" :icon="Delete" />
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { Upload, Delete, ArrowLeft } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { listDocuments, uploadDocument, deleteDocument } from '@/api/document'
import { getKnowledgeBase } from '@/api/knowledge'

const route = useRoute()
const kbId = ref(Number(route.params.id))
const knowledgeBaseName = ref('')
const documents = ref([])
const loading = ref(false)

onMounted(async () => {
  await loadKb()
  await loadDocuments()
})

async function loadKb() {
  try {
    const res = await getKnowledgeBase(kbId.value)
    knowledgeBaseName.value = res.data?.name || ''
  } catch (_) {}
}

async function loadDocuments() {
  loading.value = true
  try {
    const res = await listDocuments(kbId.value)
    documents.value = res.data || []
  } finally {
    loading.value = false
  }
}

function beforeUpload(file) {
  const maxSize = 100 * 1024 * 1024
  if (file.size > maxSize) {
    ElMessage.warning('文件大小不能超过 100MB')
    return false
  }
  return true
}

async function handleUpload({ file }) {
  try {
    await uploadDocument(kbId.value, file)
    ElMessage.success(`${file.name} 上传成功，正在处理中...`)
    await loadDocuments()
  } catch (_) {}
}

async function handleDelete(id) {
  try {
    await deleteDocument(id)
    ElMessage.success('删除成功')
    await loadDocuments()
  } catch (_) {}
}

function fileIcon(type) {
  const t = type?.toLowerCase()
  if (t === 'pdf') return 'Document'
  if (['doc', 'docx'].includes(t)) return 'Notebook'
  if (['png', 'jpg', 'jpeg'].includes(t)) return 'Picture'
  return 'Document'
}

function fileIconColor(type) {
  const t = type?.toLowerCase()
  if (t === 'pdf') return '#e74c3c'
  if (['doc', 'docx'].includes(t)) return '#2980b9'
  if (['png', 'jpg', 'jpeg'].includes(t)) return '#27ae60'
  return '#909399'
}

function fileTagType(type) {
  const t = type?.toLowerCase()
  if (t === 'pdf') return 'danger'
  if (['doc', 'docx'].includes(t)) return 'primary'
  return 'success'
}

function statusType(s) {
  const map = { PENDING: 'info', PROCESSING: 'warning', COMPLETED: 'success', FAILED: 'danger' }
  return map[s] || 'info'
}

function statusText(s) {
  const map = { PENDING: '待处理', PROCESSING: '处理中', COMPLETED: '已完成', FAILED: '失败' }
  return map[s] || s
}

function formatSize(bytes) {
  if (!bytes) return '-'
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / 1024 / 1024).toFixed(1) + ' MB'
}

function formatTime(dateStr) {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleString('zh-CN')
}
</script>

<style lang="scss" scoped>
.file-name-cell {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 14px;
}

.status-dot {
  display: inline-block;
  width: 6px;
  height: 6px;
  border-radius: 50%;
  margin-right: 4px;

  &.processing {
    background: #e6a23c;
    animation: pulse 1.5s infinite;
  }
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.3; }
}
</style>
