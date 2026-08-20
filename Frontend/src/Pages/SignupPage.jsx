import { useState } from 'react'
import AuthLayout from '../components/AuthLayout'
import { signup } from '../api/auth'

export default function SignupPage({ onGoLogin, onSuccess }) {
  const [loginId, setLoginId] = useState('')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [confirm, setConfirm] = useState('')
  const [showPassword, setShowPassword] = useState(false)
  const [showConfirm, setShowConfirm] = useState(false)
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  async function handleSubmit(event) {
    event.preventDefault()
    setError('')

    if (password !== confirm) {
      setError('Passwords do not match')
      return
    }

    setLoading(true)
    try {
      const data = await signup({ loginId, email, password })
      onSuccess(data)
    } catch (err) {
      setError(err.message || 'Could not create account')
    } finally {
      setLoading(false)
    }
  }

  return (
    <AuthLayout>
      <h2 className="auth-title">Create Account</h2>
      <p className="auth-subtitle">
        Register as a staff member or admin within Shiv Furniture Works
      </p>

      {error ? <div className="error-banner">{error}</div> : null}

      <form onSubmit={handleSubmit}>
        <div className="field">
          <label htmlFor="loginId">Login ID</label>
          <input
            id="loginId"
            value={loginId}
            onChange={(e) => setLoginId(e.target.value)}
            placeholder="Choose a unique ID"
            required
            minLength={3}
          />
        </div>

        <div className="field">
          <label htmlFor="email">Email ID</label>
          <input
            id="email"
            type="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            placeholder="yourname@shivfurniture.com"
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
              placeholder="Create a strong password"
              minLength={6}
              required
            />
            <button type="button" onClick={() => setShowPassword((v) => !v)}>
              {showPassword ? 'Hide' : 'Show'}
            </button>
          </div>
        </div>

        <div className="field">
          <label htmlFor="confirm">Re-Enter Password</label>
          <div className="password-wrap">
            <input
              id="confirm"
              type={showConfirm ? 'text' : 'password'}
              value={confirm}
              onChange={(e) => setConfirm(e.target.value)}
              placeholder="Confirm your password"
              minLength={6}
              required
            />
            <button type="button" onClick={() => setShowConfirm((v) => !v)}>
              {showConfirm ? 'Hide' : 'Show'}
            </button>
          </div>
        </div>

        <button className="primary-btn" type="submit" disabled={loading}>
          {loading ? 'SIGNING UP...' : 'SIGN UP'}
        </button>
      </form>

      <div className="auth-links" style={{ justifyContent: 'center' }}>
        <span className="muted">Already have an account? </span>
        <button className="link-btn" type="button" onClick={onGoLogin}>
          Sign In
        </button>
      </div>
    </AuthLayout>
  )
}