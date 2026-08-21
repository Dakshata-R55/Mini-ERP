import { apiFetch } from './client'

export function getProductLogs(productId) {
  return apiFetch(`/api/audit-logs/products/${productId}`)
}

export function getSalesOrderLogs(orderId) {
  return apiFetch(`/api/audit-logs/sales-orders/${orderId}`)
}