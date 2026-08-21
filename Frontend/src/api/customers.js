import { apiFetch } from './client'

const BASE = '/api/customers'

export function listCustomers() {
  return apiFetch(BASE)
}