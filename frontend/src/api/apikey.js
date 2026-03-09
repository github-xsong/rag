import request from './request'

export function listApiKeys() {
  return request.get('/api-keys')
}

export function createApiKey(data) {
  return request.post('/api-keys', data)
}

export function toggleApiKey(id) {
  return request.put(`/api-keys/${id}/toggle`)
}

export function deleteApiKey(id) {
  return request.delete(`/api-keys/${id}`)
}
