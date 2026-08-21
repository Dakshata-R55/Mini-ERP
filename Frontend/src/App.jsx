import { useState } from 'react'
import LoginPage from './Pages/LoginPage'
import SignupPage from './Pages/SignupPage'
import ProductsPage from './Pages/ProductsPage'
import ProductFormPage from './Pages/ProductFormPage'
import ProductLogsPage from './Pages/ProductLogsPage'
import SalesOrdersPage from './Pages/SalesOrdersPage'
import SalesOrderFormPage from './Pages/SalesOrderFormPage'
import SalesOrderLogsPage from './Pages/SalesOrderLogsPage'
import { clearToken } from './api/client'

function homeScreenFor(userType) {
  if (userType === 'SYSTEM_ADMIN') return 'users'
  if (userType === 'SALES_USER') return 'sales-orders'
  if (userType === 'ADMIN' || userType === 'PROJECT_MANAGER') return 'products'
  return 'no-access'
}

export default function App() {
  const [screen, setScreen] = useState('login')
  const [loginMode, setLoginMode] = useState('user')
  const [session, setSession] = useState(null)
  const [selectedProductId, setSelectedProductId] = useState(null)
  const [selectedSalesOrderId, setSelectedSalesOrderId] = useState(null)

  function handleSuccess(data) {
    setSession(data)
    setScreen(homeScreenFor(data.userType))
  }

  function handleSignOut() {
    clearToken()
    setSession(null)
    setSelectedProductId(null)
    setSelectedSalesOrderId(null)
    setScreen('login')
    setLoginMode('user')
  }

  function handleNavigate(module) {
    if (module === 'sales-orders') setScreen('sales-orders')
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