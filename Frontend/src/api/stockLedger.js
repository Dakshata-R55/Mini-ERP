import { apiFetch } from './client'

const BASE = '/api/stock-ledger'

export function listStockLedger() {
  return apiFetch(BASE)
}
