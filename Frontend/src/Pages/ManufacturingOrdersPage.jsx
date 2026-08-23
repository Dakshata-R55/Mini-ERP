import { useEffect, useState } from 'react'
import AppShell from '../components/AppShell'
import { listManufacturingOrders } from '../api/manufacturingOrders'

const STATUS_LABELS = {
  DRAFT: 'Draft',
  CONFIRMED: 'Confirmed',
  IN_PROGRESS: 'In Progress',
  TO_CLOSE: 'To Close',
  DONE: 'Done',
  CANCELLED: 'Cancelled',
}

export default function ManufacturingOrdersPage({
  session,
  onSignOut,
  onNavigate,
  onCreate,
  onOpenOrder,
}) {
  const [orders, setOrders] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    loadOrders()
  }, [])

  async function loadOrders() {
    setLoading(true)
    setError('')
    try {
      setOrders(await listManufacturingOrders())
    } catch (err) {
      setError(err.message || 'Failed to load manufacturing orders')
    } finally {
      setLoading(false)
    }
  }

  return (
    <AppShell
      session={session}
      onSignOut={onSignOut}
      onNavigate={onNavigate}
      currentModule="manufacturing-orders"
      pageTitle="Manufacturing Orders"
    >
      <div className="page-toolbar">
        <button type="button" className="primary-btn small-btn" onClick={onCreate}>
          + New Manufacturing Order
        </button>
        <h2>Manufacturing Orders</h2>
      </div>

      {error ? <div className="error-banner">{error}</div> : null}

      <div className="table-card">
        {loading ? (
          <p className="muted center-pad">Loading manufacturing orders...</p>
        ) : (
          <table className="data-table">
            <thead>
              <tr>
                <th>Reference</th>
                <th>Product</th>
                <th>Qty</th>
                <th>Assignee</th>
                <th>Status</th>
              </tr>
            </thead>
            <tbody>
              {orders.length === 0 ? (
                <tr>
                  <td colSpan="5" className="center-pad muted">No manufacturing orders yet.</td>
                </tr>
              ) : (
                orders.map((order) => (
                  <tr key={order.id} onClick={() => onOpenOrder(order.id)}>
                    <td>{order.reference}</td>
                    <td>{order.finishedProductName}</td>
                    <td>{Number(order.qtyToProduce).toFixed(2)}</td>
                    <td>{order.assigneeName || '-'}</td>
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
