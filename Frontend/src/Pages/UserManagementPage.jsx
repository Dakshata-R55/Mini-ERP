import { useEffect, useState } from 'react'
import AppShell from '../components/AppShell'
import { listUsers } from '../api/users'
import { ROLE_LABELS, displayName } from '../utils/userDisplay'

export default function UserManagementPage({
  session,
  onSignOut,
  onNavigate,
  onOpenProfile,
  onOpenUserProfile,
}) {
  const [users, setUsers] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    loadUsers()
  }, [])

  async function loadUsers() {
    setLoading(true)
    setError('')
    try {
      const data = await listUsers()
      setUsers(data)
    } catch (err) {
      setError(err.message || 'Failed to load users')
    } finally {
      setLoading(false)
    }
  }

  return (
    <AppShell
      session={session}
      onSignOut={onSignOut}
      onNavigate={onNavigate}
      onOpenProfile={onOpenProfile}
      currentModule="users"
    >
      <div className="page-toolbar">
        <h2>User Management</h2>
        <p className="muted toolbar-note">
          Click a user name to view profile and access rights. Assign roles from the profile page.
        </p>
      </div>

      {error ? <div className="error-banner">{error}</div> : null}

      <div className="table-card">
        {loading ? (
          <p className="muted center-pad">Loading users...</p>
        ) : (
          <table className="data-table user-table">
            <thead>
              <tr>
                <th>User</th>
                <th>Login ID</th>
                <th>Email</th>
                <th>Role</th>
                <th>Status</th>
              </tr>
            </thead>
            <tbody>
              {users.length === 0 ? (
                <tr>
                  <td colSpan="5" className="center-pad muted">
                    No users found.
                  </td>
                </tr>
              ) : (
                users.map((user) => (
                  <tr key={user.id}>
                    <td>
                      <button
                        type="button"
                        className="link-btn profile-link-btn"
                        onClick={() => onOpenUserProfile(user.id)}
                      >
                        {displayName(user)}
                      </button>
                    </td>
                    <td>{user.loginId}</td>
                    <td>{user.email}</td>
                    <td>
                      <span className={`status-pill role-${user.userType}`}>
                        {ROLE_LABELS[user.userType] || user.userType}
                      </span>
                    </td>
                    <td>{user.active ? 'Active' : 'Inactive'}</td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        )}
      </div>
    </AppShell>
  )
}
