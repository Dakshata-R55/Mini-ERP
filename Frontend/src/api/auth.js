import { apiFetch, saveToken } from './client'

const API_BASE = '/api/auth'

export async function login(loginId, password) {
  const data = await apiFetch(`${API_BASE}/login`, {
    method: 'POST',
    body: JSON.stringify({ loginId, password }),
  })

  saveToken(data.token)
  return data
}

export async function signup({ loginId, email, password }) {
  const data = await apiFetch(`${API_BASE}/signup`, {
    method: 'POST',
    body: JSON.stringify({ loginId, email, password }),
  })

  saveToken(data.token)
  return data
}