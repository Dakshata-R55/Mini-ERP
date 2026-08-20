import { useState } from 'react'
import AuthLayout from '../components/AuthLayout'
import { login } from '../api/auth'

export default function LoginPage({ mode, onSwitchMode, onGoSignup, onSuccess }) {
  const isAdmin = mode === 'admin'
  const [loginId, setLoginId] = useState('')
  const [password, setPassword] = useState('')
  const [showPassword, setShowPassword] = useState(false)
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  async function handleSubmit(event) {
    event.preventDefault()
    setError('')
    setLoading(true)

    try {
      const data = await login(loginId, password)

      if (isAdmin && data.userType !== 'SYSTEM_ADMIN') {
        throw new Error('Use System User Login for this account')
      }

      if (!isAdmin && data.userType === 'SYSTEM_ADMIN') {
        throw new Error('Use System Administrator Login for this account')
      }

      onSuccess(data)
    } catch (err) {
      setError(err.message || 'Invalid credentials')
    } finally {
      setLoading(false)
    }
  }

  return (
    <AuthLayout>
      <h2 className="auth-title">
        {isAdmin ? 'System Administrator Login' : 'System User Login'}
      </h2>
      <p className="auth-subtitle">
        {isAdmin
          ? 'Access the central node to manage operations & workflow'
          : 'Log in to process demands, orders, and delivery pipelines'}
      </p>

      {error ? <div className="error-banner">{error}</div> : null}

      <form onSubmit={handleSubmit}>
        <div className="field">
          <label htmlFor="loginId">Login ID</label>
          <input
            id="loginId"
            value={loginId}
            onChange={(e) => setLoginId(e.target.value)}
            placeholder={isAdmin ? 'admin@shivfurniture.com' : 'user@shivfurniture.com'}
            autoComplete="username"
            required
          />
        </div>

        <div className="field">
          <label htmlFor="password">Password</label>
          <div className="password-wrap">
            <input
              id="password"
              type={showPassword ? 'text' : 'password'}
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="********"
              autoComplete="current-password"
              required
            />
            <button
              type="button"
              onClick={() => setShowPassword((v) => !v)}
              aria-label="Toggle password"
            >
              {showPassword ? 'Hide' : 'Show'}
            </button>
          </div>
        </div>

        <button className="primary-btn" type="submit" disabled={loading}>
          {loading ? 'SIGNING IN...' : 'SIGN IN'}
        </button>
      </form>

      <div className="auth-links">
        <span className="muted">New here? </span>
        <button className="link-btn" type="button" onClick={onGoSignup}>
          Sign Up
        </button>
      </div>

      <button className="switch-role" type="button" onClick={onSwitchMode}>
        {isAdmin ? 'Login as User' : 'Login as System Administrator'}
      </button>
    </AuthLayout>
  )
}