import { useState } from 'react'
import LoginPage from './Pages/LoginPage'
import SignupPage from './Pages/SignupPage'
import ProductsPage from './Pages/ProductsPage'
import ProductFormPage from './Pages/ProductFormPage'
import ProductLogsPage from './Pages/ProductLogsPage'
import { clearToken } from './api/client'

export default function App() {
  const [screen, setScreen] = useState('login')
  const [loginMode, setLoginMode] = useState('user')
  const [session, setSession] = useState(null)
  const [selectedProductId, setSelectedProductId] = useState(null)

  function handleSuccess(data) {
    setSession(data)
    setScreen('products')
  }

  function handleSignOut() {
    clearToken()
    setSession(null)
    setSelectedProductId(null)
    setScreen('login')
    setLoginMode('user')
  }

  if (screen === 'signup') {
    return (
      <SignupPage
        onGoLogin={() => setScreen('login')}
        onSuccess={handleSuccess}
      />
    )
  }

  if (session && screen === 'products') {
    return (
      <ProductsPage
        session={session}
        onSignOut={handleSignOut}
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
        productId={selectedProductId}
        onBack={() => setScreen('product-form')}
      />
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