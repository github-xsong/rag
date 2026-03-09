<template>
  <div class="page-container">
    <div class="page-header">
      <h2>知识库管理</h2>
      <el-button type="primary" :icon="Plus" @click="openDialog()">新建知识库</el-button>
    </div>

    <div class="kb-grid">
      <div
        v-for="kb in list"
        :key="kb.id"
        class="kb-card card"
        @click="goDocuments(kb.id)"
      >
        <div class="kb-card-header">
          <div class="kb-icon">
            <el-icon :size="24"><Collection /></el-icon>
          </div>
          <el-dropdown trigger="click" @command="(cmd) => handleCommand(cmd, kb)" @click.stop>
            <el-icon class="kb-more" :size="18"><MoreFilled /></el-icon>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="edit">编辑</el-dropdown-item>
                <el-dropdown-item command="default" :disabled="kb.isDefault">设为默认</el-dropdown-item>
                <el-dropdown-item command="delete" divided style="color:#f56c6c">删除</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>

        <div class="kb-card-body">
          <div class="kb-name">
            {{ kb.name }}
            <el-tag v-if="kb.isDefault" size="small" type="primary" effect="light">默认</el-tag>
          </div>
          <div class="kb-desc">{{ kb.description || '暂无描述' }}</div>
        </div>

        <div class="kb-card-footer">
          <span>创建于 {{ formatDate(kb.createdAt) }}</span>
        </div>
      </div>

      <div v-if="list.length === 0" class="kb-empty">
        <el-empty description="还没有知识库，创建一个吧" :image-size="120" />
      </div>
    </div>

    <!-- 新建/编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="editingId ? '编辑知识库' : '新建知识库'"
      width="480px"
      :close-on-click-modal="false"
    >
      <el-form :model="form" label-position="top">
        <el-form-item label="名称" required>
          <el-input v-model="form.name" placeholder="输入知识库名称" maxlength="50" show-word-limit />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="简要描述知识库用途" maxlength="200" show-word-limit />
        </el-form-item>
        <el-form-item v-if="!editingId">
          <el-checkbox v-model="form.isDefault">设为默认知识库</el-checkbox>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  listKnowledgeBases, createKnowledgeBase, updateKnowledgeBase,
  setDefaultKnowledgeBase, deleteKnowledgeBase
} from '@/api/knowledge'

const router = useRouter()
const list = ref([])
const dialogVisible = ref(false)
const editingId = ref(null)
const submitting = ref(false)
const form = ref({ name: '', description: '', isDefault: false })

onMounted(() => loadList())

async function loadList() {
  try {
    const res = await listKnowledgeBases()
    list.value = res.data || []
  } catch (_) {}
}

function openDialog(kb = null) {
  if (kb) {
    editingId.value = kb.id
    form.value = { name: kb.name, description: kb.description || '', isDefault: kb.isDefault }
  } else {
    editingId.value = null
    form.value = { name: '', description: '', isDefault: false }
  }
  dialogVisible.value = true
}

async function submitForm() {
  if (!form.value.name.trim()) {
    ElMessage.warning('请输入知识库名称')
    return
  }
  submitting.value = true
  try {
    if (editingId.value) {
      await updateKnowledgeBase(editingId.value, form.value)
      ElMessage.success('更新成功')
    } else {
      await createKnowledgeBase(form.value)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    await loadList()
  } finally {
    submitting.value = false
  }
}

async function handleCommand(cmd, kb) {
  if (cmd === 'edit') {
    openDialog(kb)
  } else if (cmd === 'default') {
    await setDefaultKnowledgeBase(kb.id)
    ElMessage.success('已设为默认')
    await loadList()
  } else if (cmd === 'delete') {
    try {
      await ElMessageBox.confirm(`确定删除知识库「${kb.name}」吗？相关文档和向量数据也会被删除。`, '警告', { type: 'warning' })
      await deleteKnowledgeBase(kb.id)
      ElMessage.success('已删除')
      await loadList()
    } catch (_) {}
  }
}

function goDocuments(kbId) {
  router.push(`/knowledge/${kbId}/documents`)
}

function formatDate(dateStr) {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleDateString('zh-CN')
}
</script>

<style lang="scss" scoped>
.kb-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
}

.kb-card {
  padding: 20px;
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    box-shadow: var(--shadow-md);
    transform: translateY(-2px);
  }
}

.kb-card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.kb-icon {
  width: 44px;
  height: 44px;
  background: #eef2ff;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--primary);
}

.kb-more {
  color: var(--text-muted);
  cursor: pointer;
  padding: 4px;
  border-radius: 4px;

  &:hover { background: #f5f7fa; }
}

.kb-name {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 6px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.kb-desc {
  font-size: 13px;
  color: var(--text-muted);
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.kb-card-footer {
  margin-top: 16px;
  padding-top: 12px;
  border-top: 1px solid #f5f5f5;
  font-size: 12px;
  color: var(--text-muted);
}

.kb-empty {
  grid-column: 1 / -1;
  padding: 60px 0;
}
</style>
