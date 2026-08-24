import { apiFetch } from './client'

const BASE = '/api/manufacturing-orders'

export function listManufacturingOrders({ status, mine, search } = {}) {
  const params = new URLSearchParams()
  if (status) params.set('status', status)
  if (mine != null) params.set('mine', String(mine))
  if (search) params.set('search', search)

  const query = params.toString()
  return apiFetch(query ? `${BASE}?${query}` : BASE)
  
}

export function getManufacturingOrder(id) {
  return apiFetch(`${BASE}/${id}`)
}

export function createManufacturingOrder(payload) {
  return apiFetch(BASE, {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

export function updateManufacturingOrder(id, payload) {
  return apiFetch(`${BASE}/${id}`, {
    method: 'PUT',
    body: JSON.stringify(payload),
  })
}

export function confirmManufacturingOrder(id) {
  return apiFetch(`${BASE}/${id}/confirm`, { method: 'POST' })
}

export function startMoWorkOrder(orderId, workOrderId) {
  return apiFetch(`${BASE}/${orderId}/work-orders/${workOrderId}/start`, { method: 'POST' })
}

export function completeMoWorkOrder(orderId, workOrderId, realDurationMinutes) {
  return apiFetch(`${BASE}/${orderId}/work-orders/${workOrderId}/complete`, {
    method: 'POST',
    body: JSON.stringify({ realDurationMinutes }),
  })
}

export function produceManufacturingOrder(id) {
  return apiFetch(`${BASE}/${id}/produce`, { method: 'POST' })
}

export function applyMoProductionCost(id) {
  return apiFetch(`${BASE}/${id}/apply-cost`, { method: 'POST' })
}

export function cancelManufacturingOrder(id) {
  return apiFetch(`${BASE}/${id}/cancel`, { method: 'POST' })
}
