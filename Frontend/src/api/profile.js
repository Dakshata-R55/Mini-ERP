import { apiFetch, getToken } from './client'

const BASE = '/api/profile'

export function getMyProfile() {
  return apiFetch(`${BASE}/me`)
}

export function updateMyProfile(payload) {
  return apiFetch(`${BASE}/me`, {
    method: 'PUT',
    body: JSON.stringify(payload),
  })
}

export async function uploadMyAvatar(file) {
  const formData = new FormData()
  formData.append('file', file)

  const headers = {}
  const token = getToken()
  if (token) {
    headers.Authorization = `Bearer ${token}`
  }

  const response = await fetch(`${BASE}/me/avatar`, {
    method: 'POST',
    headers,
    body: formData,
  })

  if (!response.ok) {
    let message = 'Avatar upload failed'
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
