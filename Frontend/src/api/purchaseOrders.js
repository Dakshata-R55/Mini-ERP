import { apiFetch } from './client'

const BASE = '/api/purchase-orders'

export function listPurchaseOrders({ status, late, search } = {}) {
  const params = new URLSearchParams()
  if (status) params.set('status', status)
  if (late != null) params.set('late', String(late))
  if (search) params.set('search', search)

  const query = params.toString()
  return apiFetch(query ? `${BASE}?${query}` : BASE)
}

export function getPurchaseOrder(id) {
  return apiFetch(`${BASE}/${id}`)
}

export function createPurchaseOrder(payload) {
  return apiFetch(BASE, {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

export function updatePurchaseOrder(id, payload) {
  return apiFetch(`${BASE}/${id}`, {
    method: 'PUT',
    body: JSON.stringify(payload),
  })
}

export function confirmPurchaseOrder(id) {
  return apiFetch(`${BASE}/${id}/confirm`, { method: 'POST' })
}

export function receivePurchaseOrder(id, payload) {
  return apiFetch(`${BASE}/${id}/receive`, {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

export function cancelPurchaseOrder(id) {
  return apiFetch(`${BASE}/${id}/cancel`, { method: 'POST' })
}