import request from './request'

export function sendMessage(data) {
  return request.post('/chat', data)
}

export function listSessions(knowledgeBaseId) {
  return request.get('/chat/sessions', { params: { knowledgeBaseId } })
}

export function getSessionMessages(sessionId) {
  return request.get(`/chat/sessions/${sessionId}/messages`)
}

export function deleteSession(sessionId) {
  return request.delete(`/chat/sessions/${sessionId}`)
}
