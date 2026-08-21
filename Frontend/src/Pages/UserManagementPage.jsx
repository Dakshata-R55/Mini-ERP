import { useEffect, useState } from 'react'
import AppShell from '../components/AppShell'
import { assignUserType, listUsers } from '../api/users'

const ROLE_LABELS = {
  SYSTEM_ADMIN: 'System Admin',
  ADMIN: 'Admin',
  SALES_USER: 'Sales User',
  PURCHASE_USER: 'Purchase User',
  MANUFACTURING_USER: 'Manufacturing User',
  PROJECT_MANAGER: 'Project Manager',
  NONE: 'None (no access)',
}

const ASSIGNABLE_ROLES = [
  'ADMIN',
  'SALES_USER',
  'PURCHASE_USER',
  'MANUFACTURING_USER',
  'PROJECT_MANAGER',
  'NONE',
]

export default function UserManagementPage({ session, onSignOut, onNavigate }) {
  const [users, setUsers] = useState([])
  const [roleDraft, setRoleDraft] = useState({})
  const [loading, setLoading] = useState(true)
  const [savingId, setSavingId] = useState(null)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')

  useEffect(() => {
    loadUsers()
  }, [])

  async function loadUsers() {
    setLoading(true)
    setError('')
    setSuccess('')
    try {
      const data = await listUsers()
      setUsers(data)

      const draft = {}
      data.forEach((user) => {
        draft[user.id] = user.userType
      })
      setRoleDraft(draft)
    } catch (err) {
      setError(err.message || 'Failed to load users')
    } finally {
      setLoading(false)
    }
  }

  async function handleAssign(user) {
    if (user.userType === 'SYSTEM_ADMIN') {
      setError('System Admin role cannot be changed')
      return
    }

    const newRole = roleDraft[user.id]
    if (!newRole || newRole === 'SYSTEM_ADMIN') {
      setError('Please select a valid role')
      return
    }

    setSavingId(user.id)
    setError('')
    setSuccess('')
    try {
      await assignUserType(user.id, newRole)
      setSuccess(`Role updated for ${user.loginId}`)
      await loadUsers()
    } catch (err) {
      setError(err.message || 'Failed to assign role')
    } finally {
      setSavingId(null)
    }
  }

  return (
    <AppShell
      session={session}
      onSignOut={onSignOut}
      onNavigate={onNavigate}
      currentModule="users"
    >
      <div className="page-toolbar">
        <h2>User Management</h2>
        <p className="muted toolbar-note">
          Assign roles to users who signed up. New users start as <strong>None</strong>.
        </p>
      </div>

      {error ? <div className="error-banner">{error}</div> : null}
      {success ? <div className="success-banner">{success}</div> : null}

      <div className="table-card">
        {loading ? (
          <p className="muted center-pad">Loading users...</p>
        ) : (
          <table className="data-table user-table">
            <thead>
              <tr>
                <th>Login ID</th>
                <th>Email</th>
                <th>Current Role</th>
                <th>Assign Role</th>
                <th>Status</th>
                <th />
              </tr>
            </thead>
            <tbody>
              {users.length === 0 ? (
                <tr>
                  <td colSpan="6" className="center-pad muted">
                    No users found.
                  </td>
                </tr>
              ) : (
                users.map((user) => {
                  const isSystemAdmin = user.userType === 'SYSTEM_ADMIN'
                  const changed = roleDraft[user.id] !== user.userType

                  return (
                    <tr key={user.id}>
                      <td>{user.loginId}</td>
                      <td>{user.email}</td>
                      <td>
                        <span className={`status-pill role-${user.userType}`}>
                          {ROLE_LABELS[user.userType] || user.userType}
                        </span>
                      </td>
                      <td>
                        <select
                          value={roleDraft[user.id] || user.userType}
                          disabled={isSystemAdmin || savingId === user.id}
                          onChange={(e) =>
                            setRoleDraft({ ...roleDraft, [user.id]: e.target.value })
                          }
                        >
                          {isSystemAdmin ? (
                            <option value="SYSTEM_ADMIN">System Admin</option>
                          ) : (
                            ASSIGNABLE_ROLES.map((role) => (
                              <option key={role} value={role}>
                                {ROLE_LABELS[role]}
                              </option>
                            ))
                          )}
                        </select>
                      </td>
                      <td>{user.active ? 'Active' : 'Inactive'}</td>
                      <td>
                        <button
                          type="button"
                          className="primary-btn small-btn"
                          disabled={isSystemAdmin || !changed || savingId === user.id}
                          onClick={() => handleAssign(user)}
                        >
                          {savingId === user.id ? 'Saving...' : 'Assign'}
                        </button>
                      </td>
                    </tr>
                  )
                })
              )}
            </tbody>
          </table>
        )}
      </div>
    </AppShell>
  )
}