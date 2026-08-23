import { apiFetch } from './client'

const BASE = '/api/boms'

export function listBoms() {
  return apiFetch(BASE)
}

export function getBom(id) {
  return apiFetch(`${BASE}/${id}`)
}

export function createBom(payload) {
  return apiFetch(BASE, {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

export function updateBom(id, payload) {
  return apiFetch(`${BASE}/${id}`, {
    method: 'PUT',
    body: JSON.stringify(payload),
  })
}

export function deleteBom(id) {
  return apiFetch(`${BASE}/${id}`, { method: 'DELETE' })
}
