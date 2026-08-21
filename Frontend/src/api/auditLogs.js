import { apiFetch } from './client'

export function getProductLogs(productId) {
  return apiFetch(`/api/audit-logs/products/${productId}`)
}

export function getSalesOrderLogs(orderId) {
  return apiFetch(`/api/audit-logs/sales-orders/${orderId}`)
}

export function getPurchaseOrderLogs(orderId) {
  return apiFetch(`/api/audit-logs/purchase-orders/${orderId}`)
}