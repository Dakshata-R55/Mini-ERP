import { apiFetch } from './client'

const BASE = '/api/customers'

export function listCustomers() {
  return apiFetch(BASE)
}

export function createCustomer(payload) {
  return apiFetch(BASE, {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

export function updateCustomer(id, payload) {
  return apiFetch(`${BASE}/${id}`, {
    method: 'PUT',
    body: JSON.stringify(payload),
  })
}

export function deleteCustomer(id) {
  return apiFetch(`${BASE}/${id}`, { method: 'DELETE' })
}