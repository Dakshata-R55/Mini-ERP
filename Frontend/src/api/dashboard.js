import { apiFetch } from './client'

const BASE = '/api/dashboard'

export function getDashboardSummary() {
  return apiFetch(`${BASE}/summary`)
}