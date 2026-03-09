import request from './request'

export function listKnowledgeBases() {
  return request.get('/knowledge-base')
}

export function getKnowledgeBase(id) {
  return request.get(`/knowledge-base/${id}`)
}

export function getDefaultKnowledgeBase() {
  return request.get('/knowledge-base/default')
}

export function createKnowledgeBase(data) {
  return request.post('/knowledge-base', data)
}

export function updateKnowledgeBase(id, data) {
  return request.put(`/knowledge-base/${id}`, data)
}

export function setDefaultKnowledgeBase(id) {
  return request.put(`/knowledge-base/${id}/default`)
}

export function deleteKnowledgeBase(id) {
  return request.delete(`/knowledge-base/${id}`)
}
