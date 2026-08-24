import { apiFetch } from './client'

const BASE = '/api/users'

export function listUsers() {
  return apiFetch(BASE)
}

export function getUserProfile(userId) {
  return apiFetch(`${BASE}/${userId}/profile`)
}

export function getUserAccessMatrix(userId) {
  return apiFetch(`${BASE}/${userId}/access-matrix`)
}

export function assignUserType(userId, userType) {
  return apiFetch(`${BASE}/${userId}/user-type`, {
    method: 'PATCH',
    body: JSON.stringify({ userType }),
  })
}