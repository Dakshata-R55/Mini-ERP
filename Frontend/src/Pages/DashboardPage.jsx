import { useEffect, useState } from 'react'
import AppShell from '../components/AppShell'
import { listProducts } from '../api/products'
import { listSalesOrders } from '../api/salesOrders'
import { listPurchaseOrders } from '../api/purchaseOrders'
import { listCustomers } from '../api/customers'

function StatCard({ title, value, hint, badge }) {
  return (
    <div className="stat-card">
      <div className="stat-card-top">
        <span className="stat-card-title">{title}</span>
        {badge ? <span className="stat-badge">{badge}</span> : null}
      </div>
      <div className="stat-card-value">{value}</div>
      {hint ? <div className="stat-card-hint">{hint}</div> : null}
    </div>
  )
}

export default function DashboardPage({ session, onSignOut, onNavigate }) {
  const [stats, setStats] = useState({
    products: null,
    salesOrders: null,
    purchaseOrders: null,
    customers: null,
    draftOrders: null,
  })
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    async function load() {
      setLoading(true)
      const next = { products: null, salesOrders: null, purchaseOrders: null, customers: null, draftOrders: null }

      try {
        const products = await listProducts()
        next.products = products.length
      } catch { /* no access */ }

      try {
        const orders = await listSalesOrders()
        next.salesOrders = orders.length
        next.draftOrders = orders.filter((o) => o.status === 'DRAFT').length
      } catch { /* no access */ }

      try {
        const po = await listPurchaseOrders()
        next.purchaseOrders = po.length
      } catch { /* no access */ }

      try {
        const customers = await listCustomers()
        next.customers = customers.length
      } catch { /* no access */ }

      setStats(next)
      setLoading(false)
    }

    load()
  }, [])

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

      {loading ? (
        <p className="muted">Loading dashboard...</p>
      ) : (
        <div className="dashboard-grid">
          {stats.products !== null ? (
            <StatCard title="Products" value={stats.products} hint="Total active products" />
          ) : null}
          {stats.salesOrders !== null ? (
            <StatCard
              title="Sales Orders"
              value={stats.salesOrders}
              hint={`${stats.draftOrders ?? 0} draft`}
              badge="Sales"
            />
          ) : null}
          {stats.purchaseOrders !== null ? (
            <StatCard title="Purchase Orders" value={stats.purchaseOrders} hint="All purchase orders" badge="Purchase" />
          ) : null}
          {stats.customers !== null ? (
            <StatCard title="Customers" value={stats.customers} hint="Active customers" badge="Sales" />
          ) : null}
        </div>
      )}
    </AppShell>
  )
}