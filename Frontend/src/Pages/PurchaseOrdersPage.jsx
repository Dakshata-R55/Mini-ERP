import { useEffect, useState } from 'react'
import AppShell from '../components/AppShell'
import VendorFormModal from '../components/VendorFormModal'
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
  const [vendorModalOpen, setVendorModalOpen] = useState(false)
  const [vendorSuccess, setVendorSuccess] = useState('')

  const [statusFilter, setStatusFilter] = useState('')
  const [lateFilter, setLateFilter] = useState(false)
  const [mineFilter, setMineFilter] = useState(false)

  useEffect(() => {
    if (!initialFilter) return
    setStatusFilter(initialFilter.status || '')
    setLateFilter(Boolean(initialFilter.late))
    setMineFilter(Boolean(initialFilter.mine))
    onFilterApplied?.()
  }, [initialFilter])

  useEffect(() => {
    loadOrders()
  }, [search, statusFilter, lateFilter, mineFilter])

  async function loadOrders() {
    setLoading(true)
    setError('')
    try {
      const data = await listPurchaseOrders({
        search: search.trim() || undefined,
        status: statusFilter || undefined,
        late: lateFilter ? true : undefined,
        mine: mineFilter ? true : undefined,
      })
      setOrders(data)
    } catch (err) {
      setError(err.message || 'Failed to load purchase orders')
    } finally {
      setLoading(false)
    }
  }

  function clearFilters() {
    setStatusFilter('')
    setLateFilter(false)
    setMineFilter(false)
  }

  function handleVendorCreated(created) {
    setVendorSuccess(`Vendor "${created.name}" added successfully.`)
    setTimeout(() => setVendorSuccess(''), 4000)
  }

  const hasFilters = statusFilter || lateFilter || mineFilter

  return (
    <AppShell
      session={session}
      onSignOut={onSignOut}
      onNavigate={onNavigate}
      onOpenProfile={onOpenProfile}
      currentModule="purchase-orders"
      pageTitle="Purchase Orders"
    >
      <div className="page-toolbar">
        <button type="button" className="primary-btn small-btn" onClick={onCreate}>
          + New Purchase Order
        </button>
        <button type="button" className="ghost-btn" onClick={() => setVendorModalOpen(true)}>
          + Add Vendor
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

      {hasFilters ? (
        <div className="filter-banner">
          <span>Filtered:</span>
          {mineFilter ? <span className="filter-chip">My orders</span> : null}
          {statusFilter ? (
            <span className="filter-chip">{STATUS_LABELS[statusFilter] || statusFilter}</span>
          ) : null}
          {lateFilter ? <span className="filter-chip">Late</span> : null}
          <button type="button" className="ghost-btn small-btn" onClick={clearFilters}>
            Clear
          </button>
        </div>
      ) : null}

      {vendorSuccess ? <div className="success-banner">{vendorSuccess}</div> : null}
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
                    No purchase orders found.
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

      <VendorFormModal
        open={vendorModalOpen}
        onClose={() => setVendorModalOpen(false)}
        onCreated={handleVendorCreated}
      />
    </AppShell>
  )
}