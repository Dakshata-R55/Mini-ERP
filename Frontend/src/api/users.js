import { apiFetch } from './client'

const BASE = '/api/users'

export function listUsers() {
  return apiFetch(BASE)
}

export function assignUserType(userId, userType) {
  return apiFetch(`${BASE}/${userId}/user-type`, {
    method: 'PATCH',
    body: JSON.stringify({ userType }),
  })
}