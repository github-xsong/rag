import request from './request'

export function listModels() {
  return request.get('/models')
}

export function listEnabledModels() {
  return request.get('/models/enabled')
}

export function getDefaultModel() {
  return request.get('/models/default')
}

export function createModel(data) {
  return request.post('/models', data)
}

export function updateModel(id, data) {
  return request.put(`/models/${id}`, data)
}

export function setDefaultModel(id) {
  return request.put(`/models/${id}/default`)
}

export function deleteModel(id) {
  return request.delete(`/models/${id}`)
}
