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
  onOpenProfile,
  onCreate,
  onOpenOrder,
  initialFilter,
  onFilterApplied,
}) {
  const [search, setSearch] = useState('')
  const [orders, setOrders] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  const [statusFilter, setStatusFilter] = useState('')
  const [mineFilter, setMineFilter] = useState(false)

  useEffect(() => {
    if (!initialFilter) return
    setStatusFilter(initialFilter.status || '')
    setMineFilter(Boolean(initialFilter.mine))
    onFilterApplied?.()
  }, [initialFilter])

  useEffect(() => {
    loadOrders()
  }, [search, statusFilter, mineFilter])

  async function loadOrders() {
    setLoading(true)
    setError('')
    try {
      const data = await listManufacturingOrders({
        search: search.trim() || undefined,
        status: statusFilter || undefined,
        mine: mineFilter ? true : undefined,
      })
      setOrders(data)
    } catch (err) {
      setError(err.message || 'Failed to load manufacturing orders')
    } finally {
      setLoading(false)
    }
  }

  function clearFilters() {
    setStatusFilter('')
    setMineFilter(false)
  }

  const hasFilters = statusFilter || mineFilter

  return (
    <AppShell
      session={session}
      onSignOut={onSignOut}
      onNavigate={onNavigate}
      onOpenProfile={onOpenProfile}
      currentModule="manufacturing-orders"
      pageTitle="Manufacturing Orders"
    >
      <div className="page-toolbar">
        <button type="button" className="primary-btn small-btn" onClick={onCreate}>
          + New Manufacturing Order
        </button>
        <h2>Manufacturing Orders</h2>

        <div className="toolbar-right">
          <input
            className="search-input"
            placeholder="Search reference or product"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
          />
        </div>
      </div>

      {hasFilters ? (
        <div className="filter-banner">
          <span>Filtered:</span>
          {mineFilter ? <span className="filter-chip">My orders</span> : null}
          {statusFilter ? (
            <span className="filter-chip">{STATUS_LABELS[statusFilter] || statusFilter}</span>
          ) : null}
          <button type="button" className="ghost-btn small-btn" onClick={clearFilters}>
            Clear
          </button>
        </div>
      ) : null}

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
                  <td colSpan="5" className="center-pad muted">No manufacturing orders found.</td>
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