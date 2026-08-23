import { useEffect, useState } from 'react'
import AppShell from '../components/AppShell'
import { listStockLedger } from '../api/stockLedger'

function formatMoney(value) {
  return `₹ ${Number(value || 0).toFixed(2)}`
}

function formatQty(value) {
  return Number(value || 0).toFixed(2)
}

export default function StockLedgerPage({ session, onSignOut, onNavigate }) {
  const [entries, setEntries] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    loadLedger()
  }, [])

  async function loadLedger() {
    setLoading(true)
    setError('')
    try {
      const data = await listStockLedger()
      setEntries(data)
    } catch (err) {
      setError(err.message || 'Failed to load stock ledger')
    } finally {
      setLoading(false)
    }
  }

  const totalValue = entries.reduce((sum, e) => sum + Number(e.stockValue || 0), 0)

  return (
    <AppShell
      session={session}
      onSignOut={onSignOut}
      onNavigate={onNavigate}
      currentModule="stock-ledger"
      pageTitle="Stock Ledger"
    >
      <div className="page-toolbar">
        <h2>Stock Ledger</h2>
        <div className="toolbar-right">
          <span className="muted">Total stock value: {formatMoney(totalValue)}</span>
        </div>
      </div>

      {error ? <div className="error-banner">{error}</div> : null}

      <div className="table-card">
        {loading ? (
          <p className="muted center-pad">Loading stock ledger...</p>
        ) : (
          <table className="data-table">
            <thead>
              <tr>
                <th>Reference</th>
                <th>Product</th>
                <th>Type</th>
                <th>On Hand</th>
                <th>Unit Cost</th>
                <th>Stock Value</th>
              </tr>
            </thead>
            <tbody>
              {entries.length === 0 ? (
                <tr>
                  <td colSpan="6" className="center-pad muted">No stock records.</td>
                </tr>
              ) : (
                entries.map((entry) => (
                  <tr key={entry.productId}>
                    <td>{entry.reference}</td>
                    <td>{entry.name}</td>
                    <td>{entry.productType === 'RAW_MATERIAL' ? 'Raw Material' : 'Finished Good'}</td>
                    <td>{formatQty(entry.onHandQty)}</td>
                    <td>{formatMoney(entry.unitCost)}</td>
                    <td>{formatMoney(entry.stockValue)}</td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        )}
      </div>
    </AppShell>
  )
}
