import { apiFetch } from './client'

const BASE = '/api/vendors'

export function listVendors() {
  return apiFetch(BASE)
}