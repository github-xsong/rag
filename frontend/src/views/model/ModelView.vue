<template>
  <div class="page-container">
    <div class="page-header">
      <h2>模型管理</h2>
      <el-button type="primary" :icon="Plus" @click="openDialog()">添加模型</el-button>
    </div>

    <div class="card" style="padding:0">
      <el-table :data="models" style="width:100%" v-loading="loading" empty-text="暂无模型配置">
        <el-table-column label="名称" min-width="150">
          <template #default="{ row }">
            <div style="display:flex;align-items:center;gap:8px">
              <span style="font-weight:500">{{ row.name }}</span>
              <el-tag v-if="row.isDefault" size="small" type="primary" effect="light">默认</el-tag>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="provider" label="提供商" width="130" align="center">
          <template #default="{ row }">
            <el-tag size="small" effect="plain">{{ row.provider }}</el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="modelName" label="模型" width="180" />

        <el-table-column label="API 地址" min-width="200">
          <template #default="{ row }">
            <span class="text-muted">{{ row.baseUrl || '-' }}</span>
          </template>
        </el-table-column>

        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.enabled ? 'success' : 'info'" size="small" effect="light" round>
              {{ row.enabled ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template #default="{ row }">
            <el-button text size="small" @click="openDialog(row)">编辑</el-button>
            <el-button text size="small" :disabled="row.isDefault" @click="handleSetDefault(row.id)">设为默认</el-button>
            <el-popconfirm title="确定删除此模型配置？" @confirm="handleDelete(row.id)">
              <template #reference>
                <el-button text size="small" type="danger">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 添加/编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="editingId ? '编辑模型' : '添加模型'"
      width="520px"
      :close-on-click-modal="false"
    >
      <el-form :model="form" label-position="top">
        <el-form-item label="配置名称" required>
          <el-input v-model="form.name" placeholder="如：GPT-4o" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="提供商" required>
              <el-select v-model="form.provider" placeholder="选择提供商" style="width:100%">
                <el-option label="OpenAI" value="OPENAI" />
                <el-option label="Claude" value="CLAUDE" />
                <el-option label="通义千问" value="QWEN" />
                <el-option label="智谱" value="ZHIPU" />
                <el-option label="其他" value="OTHER" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="模型名称" required>
              <el-input v-model="form.modelName" placeholder="如：gpt-4o" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="API Key">
          <el-input v-model="form.apiKey" placeholder="sk-xxx" type="password" show-password />
        </el-form-item>
        <el-form-item label="API Base URL">
          <el-input v-model="form.baseUrl" placeholder="https://api.openai.com" />
        </el-form-item>
        <el-form-item>
          <el-checkbox v-model="form.enabled">启用</el-checkbox>
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
import { Plus } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { listModels, createModel, updateModel, setDefaultModel, deleteModel } from '@/api/model'

const models = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const editingId = ref(null)
const submitting = ref(false)
const form = ref(getEmptyForm())

function getEmptyForm() {
  return { name: '', provider: 'OPENAI', modelName: '', apiKey: '', baseUrl: '', enabled: true }
}

onMounted(() => loadList())

async function loadList() {
  loading.value = true
  try {
    const res = await listModels()
    models.value = res.data || []
  } finally {
    loading.value = false
  }
}

function openDialog(row = null) {
  if (row) {
    editingId.value = row.id
    form.value = { ...row }
  } else {
    editingId.value = null
    form.value = getEmptyForm()
  }
  dialogVisible.value = true
}

async function submitForm() {
  if (!form.value.name.trim() || !form.value.provider || !form.value.modelName.trim()) {
    ElMessage.warning('请填写必填项')
    return
  }
  submitting.value = true
  try {
    if (editingId.value) {
      await updateModel(editingId.value, form.value)
      ElMessage.success('更新成功')
    } else {
      await createModel(form.value)
      ElMessage.success('添加成功')
    }
    dialogVisible.value = false
    await loadList()
  } finally {
    submitting.value = false
  }
}

async function handleSetDefault(id) {
  await setDefaultModel(id)
  ElMessage.success('已设为默认')
  await loadList()
}

async function handleDelete(id) {
  await deleteModel(id)
  ElMessage.success('已删除')
  await loadList()
}
</script>

<style lang="scss" scoped>
.text-muted {
  color: var(--text-muted);
  font-size: 13px;
}
</style>
