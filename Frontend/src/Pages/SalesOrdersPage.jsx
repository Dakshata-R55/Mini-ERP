import { useEffect, useState } from 'react'
import AppShell from '../components/AppShell'
import CustomerFormModal from '../components/CustomerFormModal'
import { listSalesOrders } from '../api/salesOrders'

const STATUS_LABELS = {
  DRAFT: 'Draft',
  CONFIRMED: 'Confirmed',
  PARTIALLY_DELIVERED: 'Partially Delivered',
  FULLY_DELIVERED: 'Delivered',
  CANCELLED: 'Cancelled',
}

const KANBAN_COLUMNS = [
  'DRAFT',
  'CONFIRMED',
  'PARTIALLY_DELIVERED',
  'FULLY_DELIVERED',
  'CANCELLED',
]

function formatDate(value) {
  if (!value) return '-'
  return new Date(value).toLocaleDateString()
}

export default function SalesOrdersPage({
  session,
  onSignOut,
  onNavigate,
  onCreate,
  onOpenOrder,
}) {
  const [view, setView] = useState('list')
  const [search, setSearch] = useState('')
  const [orders, setOrders] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [customerModalOpen, setCustomerModalOpen] = useState(false)
  const [customerSuccess, setCustomerSuccess] = useState('')

  useEffect(() => {
    loadOrders()
  }, [search])

  async function loadOrders() {
    setLoading(true)
    setError('')
    try {
      const data = await listSalesOrders({ search: search.trim() || undefined })
      setOrders(data)
    } catch (err) {
      setError(err.message || 'Failed to load sales orders')
    } finally {
      setLoading(false)
    }
  }

  function handleCustomerCreated(created) {
    setCustomerSuccess(`Customer "${created.name}" added successfully.`)
    setTimeout(() => setCustomerSuccess(''), 4000)
  }

  return (
    <AppShell
      session={session}
      onSignOut={onSignOut}
      onNavigate={onNavigate}
      currentModule="sales-orders"
      pageTitle="Sales Orders"
    >
      <div className="page-toolbar">
        <button type="button" className="primary-btn small-btn" onClick={onCreate}>
          + New Sales Order
        </button>
        <button
          type="button"
          className="ghost-btn"
          onClick={() => setCustomerModalOpen(true)}
        >
          + Add Customer
        </button>
        <h2>Sales Orders</h2>

        <div className="toolbar-right">
          <input
            className="search-input"
            placeholder="Search reference or customer"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
          />
          <button
            type="button"
            className={`icon-btn ${view === 'list' ? 'active' : ''}`}
            onClick={() => setView('list')}
          >
            List
          </button>
          <button
            type="button"
            className={`icon-btn ${view === 'kanban' ? 'active' : ''}`}
            onClick={() => setView('kanban')}
          >
            Kanban
          </button>
        </div>
      </div>

      {customerSuccess ? <div className="success-banner">{customerSuccess}</div> : null}
      {error ? <div className="error-banner">{error}</div> : null}

      {loading ? (
        <p className="muted center-pad">Loading sales orders...</p>
      ) : view === 'list' ? (
        <div className="table-card">
          <table className="data-table">
            <thead>
              <tr>
                <th />
                <th>Reference</th>
                <th>Date</th>
                <th>Customer</th>
                <th>Salesperson</th>
                <th>Status</th>
              </tr>
            </thead>
            <tbody>
              {orders.length === 0 ? (
                <tr>
                  <td colSpan="6" className="center-pad muted">
                    No sales orders yet.
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
                    <td>{order.customerName}</td>
                    <td>{order.salesPersonName || '-'}</td>
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
        </div>
      ) : (
        <div className="kanban-board">
          {KANBAN_COLUMNS.map((status) => (
            <div key={status} className="kanban-column">
              <h4>{STATUS_LABELS[status]}</h4>
              {orders
                .filter((o) => o.status === status)
                .map((order) => (
                  <button
                    key={order.id}
                    type="button"
                    className="kanban-card"
                    onClick={() => onOpenOrder(order.id)}
                  >
                    <div className="kanban-card-top">
                      <strong>{order.reference}</strong>
                      <span>{STATUS_LABELS[order.status]}</span>
                    </div>
                    <div>{order.customerName}</div>
                    <div className="muted">{formatDate(order.startDate)}</div>
                  </button>
                ))}
            </div>
          ))}
        </div>
      )}

      <CustomerFormModal
        open={customerModalOpen}
        onClose={() => setCustomerModalOpen(false)}
        onCreated={handleCustomerCreated}
      />
    </AppShell>
  )
}