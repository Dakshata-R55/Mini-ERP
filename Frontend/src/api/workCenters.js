import { apiFetch } from './client'

const BASE = '/api/work-centers'

export function listWorkCenters() {
  return apiFetch(BASE)
}

export function getWorkCenter(id) {
  return apiFetch(`${BASE}/${id}`)
}

export function createWorkCenter(payload) {
  return apiFetch(BASE, {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

export function updateWorkCenter(id, payload) {
  return apiFetch(`${BASE}/${id}`, {
    method: 'PUT',
    body: JSON.stringify(payload),
  })
}

export function deleteWorkCenter(id) {
  return apiFetch(`${BASE}/${id}`, { method: 'DELETE' })
}
