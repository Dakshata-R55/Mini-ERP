import AuthLayout from '../components/AuthLayout'

export default function ComingSoonPage({ session, onSignOut }) {
  return (
    <AuthLayout>
      <div className="coming-soon">
        <h2 className="auth-title">You are signed in</h2>
        <p className="auth-subtitle">
          Modules and workflow screens will be added later.
        </p>
        <p className="meta">
          Login ID: <strong>{session.loginId}</strong>
          <br />
          Role: <strong>{session.userType}</strong>
        </p>
        <button className="primary-btn" type="button" onClick={onSignOut}>
          SIGN OUT
        </button>
      </div>
    </AuthLayout>
  )
}