import request from './request'

export function listDocuments(knowledgeBaseId) {
  return request.get(`/documents/knowledge-base/${knowledgeBaseId}`)
}

export function uploadDocument(knowledgeBaseId, file, onProgress) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post(`/documents/knowledge-base/${knowledgeBaseId}/upload`, formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
    onUploadProgress: onProgress
  })
}

export function deleteDocument(id) {
  return request.delete(`/documents/${id}`)
}
