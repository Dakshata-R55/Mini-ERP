import { useState } from 'react'
import LoginPage from './Pages/LoginPage'
import SignupPage from './Pages/SignupPage'
import ProductsPage from './Pages/ProductsPage'
import ProductFormPage from './Pages/ProductFormPage'
import ProductLogsPage from './Pages/ProductLogsPage'
import SalesOrdersPage from './Pages/SalesOrdersPage'
import SalesOrderFormPage from './Pages/SalesOrderFormPage'
import SalesOrderLogsPage from './Pages/SalesOrderLogsPage'
import PurchaseOrdersPage from './Pages/PurchaseOrdersPage'
import PurchaseOrderFormPage from './Pages/PurchaseOrderFormPage'
import PurchaseOrderLogsPage from './Pages/PurchaseOrderLogsPage'
import { clearToken } from './api/client'

function homeScreenFor(userType) {
  if (userType === 'SYSTEM_ADMIN') return 'users'
  if (userType === 'SALES_USER') return 'sales-orders'
  if (userType === 'PURCHASE_USER') return 'purchase-orders'
  if (userType === 'ADMIN' || userType === 'PROJECT_MANAGER') return 'products'
  return 'no-access'
}

export default function App() {
  const [screen, setScreen] = useState('login')
  const [loginMode, setLoginMode] = useState('user')
  const [session, setSession] = useState(null)
  const [selectedProductId, setSelectedProductId] = useState(null)
  const [selectedSalesOrderId, setSelectedSalesOrderId] = useState(null)
  const [selectedPurchaseOrderId, setSelectedPurchaseOrderId] = useState(null)

  function handleSuccess(data) {
    setSession(data)
    setScreen(homeScreenFor(data.userType))
  }

  function handleSignOut() {
    clearToken()
    setSession(null)
    setSelectedProductId(null)
    setSelectedSalesOrderId(null)
    setSelectedPurchaseOrderId(null)
    setScreen('login')
    setLoginMode('user')
  }

  function handleNavigate(module) {
    if (module === 'sales-orders') setScreen('sales-orders')
    if (module === 'purchase-orders') setScreen('purchase-orders')
    if (module === 'products') setScreen('products')
    if (module === 'users') setScreen('users')
  }

  if (screen === 'signup') {
    return (
      <SignupPage
        onGoLogin={() => setScreen('login')}
        onSuccess={handleSuccess}
      />
    )
  }

  if (session && screen === 'no-access') {
    return (
      <div className="auth-page">
        <div className="auth-card coming-soon">
          <h2 className="auth-title">No module access</h2>
          <p className="auth-subtitle">
            Your role is <strong>{session.userType}</strong>. Ask System Admin to assign access.
          </p>
          <button className="primary-btn" type="button" onClick={handleSignOut}>
            Sign out
          </button>
        </div>
      </div>
    )
  }

  if (session && screen === 'purchase-orders') {
    return (
      <PurchaseOrdersPage
        session={session}
        onSignOut={handleSignOut}
        onNavigate={handleNavigate}
        onCreate={() => {
          setSelectedPurchaseOrderId(null)
          setScreen('purchase-order-form')
        }}
        onOpenOrder={(id) => {
          setSelectedPurchaseOrderId(id)
          setScreen('purchase-order-form')
        }}
      />
    )
  }

  if (session && screen === 'purchase-order-form') {
    return (
      <PurchaseOrderFormPage
        session={session}
        onSignOut={handleSignOut}
        onNavigate={handleNavigate}
        orderId={selectedPurchaseOrderId}
        onBack={(newId) => {
          if (newId) setSelectedPurchaseOrderId(newId)
          setScreen(newId ? 'purchase-order-form' : 'purchase-orders')
        }}
        onSaved={() => setScreen('purchase-orders')}
        onOpenLogs={(id) => {
          setSelectedPurchaseOrderId(id)
          setScreen('purchase-order-logs')
        }}
      />
    )
  }

  if (session && screen === 'purchase-order-logs') {
    return (
      <PurchaseOrderLogsPage
        session={session}
        onSignOut={handleSignOut}
        onNavigate={handleNavigate}
        orderId={selectedPurchaseOrderId}
        onBack={() => setScreen('purchase-order-form')}
      />
    )
  }

  if (session && screen === 'sales-orders') {
    return (
      <SalesOrdersPage
        session={session}
        onSignOut={handleSignOut}
        onNavigate={handleNavigate}
        onCreate={() => {
          setSelectedSalesOrderId(null)
          setScreen('sales-order-form')
        }}
        onOpenOrder={(id) => {
          setSelectedSalesOrderId(id)
          setScreen('sales-order-form')
        }}
      />
    )
  }

  if (session && screen === 'sales-order-form') {
    return (
      <SalesOrderFormPage
        session={session}
        onSignOut={handleSignOut}
        onNavigate={handleNavigate}
        orderId={selectedSalesOrderId}
        onBack={(newId) => {
          if (newId) setSelectedSalesOrderId(newId)
          setScreen(newId ? 'sales-order-form' : 'sales-orders')
        }}
        onOpenLogs={(id) => {
          setSelectedSalesOrderId(id)
          setScreen('sales-order-logs')
        }}
      />
    )
  }

  if (session && screen === 'sales-order-logs') {
    return (
      <SalesOrderLogsPage
        session={session}
        onSignOut={handleSignOut}
        onNavigate={handleNavigate}
        orderId={selectedSalesOrderId}
        onBack={() => setScreen('sales-order-form')}
      />
    )
  }

  if (session && screen === 'products') {
    return (
      <ProductsPage
        session={session}
        onSignOut={handleSignOut}
        onNavigate={handleNavigate}
        onCreate={() => {
          setSelectedProductId(null)
          setScreen('product-form')
        }}
        onOpenProduct={(id) => {
          setSelectedProductId(id)
          setScreen('product-form')
        }}
      />
    )
  }

  if (session && screen === 'product-form') {
    return (
      <ProductFormPage
        session={session}
        onSignOut={handleSignOut}
        onNavigate={handleNavigate}
        productId={selectedProductId}
        onBack={() => setScreen('products')}
        onSaved={() => setScreen('products')}
        onOpenLogs={(id) => {
          setSelectedProductId(id)
          setScreen('product-logs')
        }}
      />
    )
  }

  if (session && screen === 'product-logs') {
    return (
      <ProductLogsPage
        session={session}
        onSignOut={handleSignOut}
        onNavigate={handleNavigate}
        productId={selectedProductId}
        onBack={() => setScreen('product-form')}
      />
    )
  }

  if (session && screen === 'users') {
    return (
      <div className="auth-page">
        <div className="auth-card coming-soon">
          <h2 className="auth-title">User Management</h2>
          <p className="auth-subtitle">Build UserManagementPage next for System Admin.</p>
          <button className="primary-btn" type="button" onClick={handleSignOut}>
            Sign out
          </button>
        </div>
      </div>
    )
  }

  return (
    <LoginPage
      mode={loginMode}
      onSwitchMode={() => setLoginMode((m) => (m === 'user' ? 'admin' : 'user'))}
      onGoSignup={() => setScreen('signup')}
      onSuccess={handleSuccess}
    />
  )
}