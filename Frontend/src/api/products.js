import { apiFetch, getToken } from './client'

const BASE = '/api/products'

export function listProducts({ type } = {}) {
  const query = type ? `?type=${encodeURIComponent(type)}` : ''
  return apiFetch(`${BASE}${query}`)
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

export async function uploadProductImage(file) {
  const formData = new FormData()
  formData.append('file', file)

  const headers = {}
  const token = getToken()
  if (token) {
    headers.Authorization = `Bearer ${token}`
  }

  const response = await fetch(`${BASE}/upload-image`, {
    method: 'POST',
    headers,
    body: formData,
  })

  if (!response.ok) {
    let message = 'Image upload failed'
    try {
      const body = await response.json()
      message = body.message || message
    } catch {
      // ignore
    }
    throw new Error(message)
  }

  return response.json()
}