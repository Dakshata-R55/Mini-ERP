import { apiFetch } from './client'

const BASE = '/api/sales-orders'

export function listSalesOrders({ status, late, search } = {}) {
  const params = new URLSearchParams()
  if (status) params.set('status', status)
  if (late != null) params.set('late', String(late))
  if (search) params.set('search', search)

  const query = params.toString()
  return apiFetch(query ? `${BASE}?${query}` : BASE)
}

export function getSalesOrder(id) {
  return apiFetch(`${BASE}/${id}`)
}

export function createSalesOrder(payload) {
  return apiFetch(BASE, {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

export function updateSalesOrder(id, payload) {
  return apiFetch(`${BASE}/${id}`, {
    method: 'PUT',
    body: JSON.stringify(payload),
  })
}

export function confirmSalesOrder(id) {
  return apiFetch(`${BASE}/${id}/confirm`, { method: 'POST' })
}

export function deliverSalesOrder(id, payload) {
  return apiFetch(`${BASE}/${id}/deliver`, {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

export function cancelSalesOrder(id) {
  return apiFetch(`${BASE}/${id}/cancel`, { method: 'POST' })
}