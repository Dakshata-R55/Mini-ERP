import { apiFetch } from './client'

const BASE = '/api/vendors'

export function listVendors() {
  return apiFetch(BASE)
}

export function createVendor(payload) {
  return apiFetch(BASE, {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

export function updateVendor(id, payload) {
  return apiFetch(`${BASE}/${id}`, {
    method: 'PUT',
    body: JSON.stringify(payload),
  })
}

export function deleteVendor(id) {
  return apiFetch(`${BASE}/${id}`, { method: 'DELETE' })
}