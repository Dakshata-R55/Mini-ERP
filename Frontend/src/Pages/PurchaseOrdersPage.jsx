import { useEffect, useState } from 'react'
import AppShell from '../components/AppShell'
import { listPurchaseOrders } from '../api/purchaseOrders'

const STATUS_LABELS = {
  DRAFT: 'Draft',
  CONFIRMED: 'Confirmed',
  PARTIALLY_RECEIVED: 'Partially Received',
  FULLY_RECEIVED: 'Received',
  CANCELLED: 'Cancelled',
}

function formatDate(value) {
  if (!value) return '-'
  return new Date(value).toLocaleDateString()
}

export default function PurchaseOrdersPage({
  session,
  onSignOut,
  onNavigate,
  onCreate,
  onOpenOrder,
}) {
  const [search, setSearch] = useState('')
  const [orders, setOrders] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    loadOrders()
  }, [search])

  async function loadOrders() {
    setLoading(true)
    setError('')
    try {
      const data = await listPurchaseOrders({ search: search.trim() || undefined })
      setOrders(data)
    } catch (err) {
      setError(err.message || 'Failed to load purchase orders')
    } finally {
      setLoading(false)
    }
  }

  return (
    <AppShell
      session={session}
      onSignOut={onSignOut}
      onNavigate={onNavigate}
      currentModule="purchase-orders"
    >
      <div className="page-toolbar">
        <button type="button" className="primary-btn small-btn" onClick={onCreate}>
          + New Purchase Order
        </button>
        <h2>Purchase Orders</h2>

        <div className="toolbar-right">
          <input
            className="search-input"
            placeholder="Search reference or vendor"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
          />
        </div>
      </div>

      {error ? <div className="error-banner">{error}</div> : null}

      <div className="table-card">
        {loading ? (
          <p className="muted center-pad">Loading purchase orders...</p>
        ) : (
          <table className="data-table">
            <thead>
              <tr>
                <th />
                <th>Reference</th>
                <th>Date</th>
                <th>Vendor</th>
                <th>Responsible</th>
                <th>Status</th>
              </tr>
            </thead>
            <tbody>
              {orders.length === 0 ? (
                <tr>
                  <td colSpan="6" className="center-pad muted">
                    No purchase orders yet.
                  </td>
                </tr>
              ) : (
                orders.map((order) => (
                  <tr key={order.id} onClick={() => onOpenOrder(order.id)}>
                    <td>
                      <input type="checkbox" onClick={(e) => e.stopPropagation()} />
                    </td>
                    <td>{order.reference}</td>
                    <td>{formatDate(order.startDate)}</td>
                    <td>{order.vendorName}</td>
                    <td>{order.responsiblePersonName || '-'}</td>
                    <td>
                      <span className={`status-pill status-${order.status}`}>
                        {STATUS_LABELS[order.status] || order.status}
                      </span>
                    </td>
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