import { apiFetch } from './client'

export function getProductLogs(productId) {
  return apiFetch(`/api/audit-logs/products/${productId}`)
}