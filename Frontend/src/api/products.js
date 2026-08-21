import { apiFetch } from './client'

const BASE = '/api/products'

export function listProducts() {
  return apiFetch(BASE)
}

export function getProduct(id) {
  return apiFetch(`${BASE}/${id}`)
}

export function createProduct(payload) {
  return apiFetch(BASE, {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

export function updateProduct(id, payload) {
  return apiFetch(`${BASE}/${id}`, {
    method: 'PUT',
    body: JSON.stringify(payload),
  })
}

export function deleteProduct(id) {
  return apiFetch(`${BASE}/${id}`, { method: 'DELETE' })
}