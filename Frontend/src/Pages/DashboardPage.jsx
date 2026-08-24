import { useEffect, useState } from 'react'
import AppShell from '../components/AppShell'
import DashboardOrderSection from '../components/DashboardOrderSection'
import { getDashboardSummary } from '../api/dashboard'

const SALES_ALL_PILLS = [
  { key: 'draft', field: 'draft', label: 'Draft', filter: { status: 'DRAFT' } },
  { key: 'confirmed', field: 'confirmed', label: 'Confirmed', filter: { status: 'CONFIRMED' } },
  { key: 'partial', field: 'partial', label: 'Partially Delivered', filter: { status: 'PARTIALLY_DELIVERED' } },
  { key: 'completed', field: 'completed', label: 'Delivered', filter: { status: 'FULLY_DELIVERED' } },
  { key: 'late', field: 'late', label: 'Late', filter: { late: true } },
]

const SALES_MY_PILLS = [
  { key: 'confirmed', field: 'confirmed', label: 'Confirmed', filter: { status: 'CONFIRMED' } },
  { key: 'draft', field: 'draft', label: 'Draft', filter: { status: 'DRAFT' } },
  { key: 'completed', field: 'completed', label: 'Delivered', filter: { status: 'FULLY_DELIVERED' } },
]

const PURCHASE_ALL_PILLS = [
  { key: 'draft', field: 'draft', label: 'Draft', filter: { status: 'DRAFT' } },
  { key: 'confirmed', field: 'confirmed', label: 'Confirmed', filter: { status: 'CONFIRMED' } },
  { key: 'partial', field: 'partial', label: 'Partially Received', filter: { status: 'PARTIALLY_RECEIVED' } },
  { key: 'completed', field: 'completed', label: 'Received', filter: { status: 'FULLY_RECEIVED' } },
  { key: 'late', field: 'late', label: 'Late', filter: { late: true } },
]

const PURCHASE_MY_PILLS = [
  { key: 'confirmed', field: 'confirmed', label: 'Confirmed', filter: { status: 'CONFIRMED' } },
  { key: 'draft', field: 'draft', label: 'Draft', filter: { status: 'DRAFT' } },
  { key: 'completed', field: 'completed', label: 'Received', filter: { status: 'FULLY_RECEIVED' } },
]

const MO_ALL_PILLS = [
  { key: 'draft', field: 'draft', label: 'Draft', filter: { status: 'DRAFT' } },
  { key: 'confirmed', field: 'confirmed', label: 'Confirmed', filter: { status: 'CONFIRMED' } },
  { key: 'inProgress', field: 'inProgress', label: 'In Progress', filter: { status: 'IN_PROGRESS' } },
  { key: 'toClose', field: 'toClose', label: 'To Close', filter: { status: 'TO_CLOSE' } },
  { key: 'completed', field: 'completed', label: 'Done', filter: { status: 'DONE' } },
]

const MO_MY_PILLS = [
  { key: 'confirmed', field: 'confirmed', label: 'Confirmed', filter: { status: 'CONFIRMED' } },
  { key: 'inProgress', field: 'inProgress', label: 'In Progress', filter: { status: 'IN_PROGRESS' } },
  { key: 'completed', field: 'completed', label: 'Done', filter: { status: 'DONE' } },
]

export default function DashboardPage({ session, onSignOut, onNavigate, onOpenOrders }) {
  const [summary, setSummary] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    async function load() {
      setLoading(true)
      setError('')
      try {
        setSummary(await getDashboardSummary())
      } catch (err) {
        setError(err.message || 'Failed to load dashboard')
      } finally {
        setLoading(false)
      }
    }
    load()
  }, [])

  function handleSalesPill(filter) {
    onOpenOrders('sales-orders', {
      status: filter.status,
      late: filter.late,
      mine: filter.scope === 'mine',
    })
  }

  function handlePurchasePill(filter) {
    onOpenOrders('purchase-orders', {
      status: filter.status,
      late: filter.late,
      mine: filter.scope === 'mine',
    })
  }

  function handleMoPill(filter) {
    onOpenOrders('manufacturing-orders', {
      status: filter.status,
      mine: filter.scope === 'mine',
    })
  }

  return (
    <AppShell
      session={session}
      onSignOut={onSignOut}
      onNavigate={onNavigate}
      currentModule="dashboard"
      pageTitle="Dashboard"
    >
      <p className="dashboard-welcome">
        Welcome back, <strong>{session.loginId}</strong>
      </p>

      {error ? <div className="error-banner">{error}</div> : null}

      {loading ? (
        <p className="muted">Loading dashboard...</p>
      ) : (
        <div className="dashboard-orders-stack">
          {summary?.salesOrders ? (
            <DashboardOrderSection
              title="Sale Orders"
              allStats={summary.salesOrders.all}
              myStats={summary.salesOrders.mine}
              allPills={SALES_ALL_PILLS}
              myPills={SALES_MY_PILLS}
              onPillClick={handleSalesPill}
            />
          ) : null}

          {summary?.purchaseOrders ? (
            <DashboardOrderSection
              title="Purchase Orders"
              allStats={summary.purchaseOrders.all}
              myStats={summary.purchaseOrders.mine}
              allPills={PURCHASE_ALL_PILLS}
              myPills={PURCHASE_MY_PILLS}
              onPillClick={handlePurchasePill}
            />
          ) : null}

          {summary?.manufacturingOrders ? (
            <DashboardOrderSection
              title="Manufacturing Orders"
              allStats={summary.manufacturingOrders.all}
              myStats={summary.manufacturingOrders.mine}
              allPills={MO_ALL_PILLS}
              myPills={MO_MY_PILLS}
              onPillClick={handleMoPill}
            />
          ) : null}
        </div>
      )}

      <p className="dashboard-search-hint muted">
        Click a status pill to open the filtered order list. Use search on each list page.
      </p>
    </AppShell>
  )
}