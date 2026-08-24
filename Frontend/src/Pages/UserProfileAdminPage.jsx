import { useEffect, useState } from 'react'
import AppShell from '../components/AppShell'
import AvatarUpload from '../components/AvatarUpload'
import AccessMatrixTable from '../components/AccessMatrixTable'
import { assignUserType, getUserAccessMatrix, getUserProfile } from '../api/users'
import { ASSIGNABLE_ROLES, ROLE_LABELS, displayName } from '../utils/userDisplay'

export default function UserProfileAdminPage({
  session,
  avatarUrl,
  userId,
  onSignOut,
  onNavigate,
  onOpenProfile,
  onBack,
}) {
  const [profile, setProfile] = useState(null)
  const [matrix, setMatrix] = useState(null)
  const [roleDraft, setRoleDraft] = useState('')
  const [activeTab, setActiveTab] = useState('SALES')
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')

  useEffect(() => {
    loadData()
  }, [userId])

  async function loadData() {
    setLoading(true)
    setError('')
    setSuccess('')
    try {
      const [profileData, matrixData] = await Promise.all([
        getUserProfile(userId),
        getUserAccessMatrix(userId),
      ])
      setProfile(profileData)
      setMatrix(matrixData)
      setRoleDraft(profileData.userType)
      setActiveTab(matrixData.tabs?.[0]?.key || 'SALES')
    } catch (err) {
      setError(err.message || 'Failed to load user profile')
    } finally {
      setLoading(false)
    }
  }

  async function handleSaveRole() {
    if (!profile || profile.userType === 'SYSTEM_ADMIN') return

    setSaving(true)
    setError('')
    setSuccess('')
    try {
      await assignUserType(userId, roleDraft)
      setSuccess('Role updated')
      await loadData()
    } catch (err) {
      setError(err.message || 'Failed to update role')
    } finally {
      setSaving(false)
    }
  }

  const isSystemAdmin = profile?.userType === 'SYSTEM_ADMIN'
  const roleChanged = profile && roleDraft !== profile.userType

  return (
    <AppShell
      session={{ ...session, avatarUrl }}
      onSignOut={onSignOut}
      onNavigate={onNavigate}
      onOpenProfile={onOpenProfile}
      currentModule="users"
      pageTitle="User Profile"
    >
      <div className="page-toolbar">
        <button type="button" className="ghost-btn" onClick={onBack}>
          Back to users
        </button>
        <h2>{profile ? displayName(profile) : 'User Profile'}</h2>
      </div>

      {error ? <div className="error-banner">{error}</div> : null}
      {success ? <div className="success-banner">{success}</div> : null}

      {loading ? (
        <p className="muted center-pad">Loading profile...</p>
      ) : (
        <>
          <div className="profile-layout">
            <div className="profile-card form-card">
              <div className="profile-card-grid">
                <div className="profile-fields">
                  <div className="field">
                    <label>Name</label>
                    <input value={profile?.fullName || '—'} readOnly className="readonly-input" />
                  </div>

                  <div className="field">
                    <label>Address</label>
                    <textarea
                      rows="3"
                      value={profile?.address || '—'}
                      readOnly
                      className="readonly-input"
                    />
                  </div>

                  <div className="field">
                    <label>Mobile Number</label>
                    <input value={profile?.mobile || '—'} readOnly className="readonly-input" />
                  </div>

                  <div className="field">
                    <label>Email ID</label>
                    <input value={profile?.email || ''} readOnly className="readonly-input" />
                  </div>

                  <div className="field">
                    <label>Position / Role</label>
                    {isSystemAdmin ? (
                      <input
                        value={ROLE_LABELS.SYSTEM_ADMIN}
                        readOnly
                        className="readonly-input"
                      />
                    ) : (
                      <select
                        value={roleDraft}
                        disabled={saving}
                        onChange={(e) => setRoleDraft(e.target.value)}
                      >
                        {ASSIGNABLE_ROLES.map((role) => (
                          <option key={role} value={role}>
                            {ROLE_LABELS[role]}
                          </option>
                        ))}
                      </select>
                    )}
                  </div>

                  <div className="field">
                    <label>Status</label>
                    <input
                      value={profile?.active ? 'Active' : 'Inactive'}
                      readOnly
                      className="readonly-input"
                    />
                  </div>

                  {!isSystemAdmin ? (
                    <button
                      type="button"
                      className="primary-btn"
                      disabled={!roleChanged || saving}
                      onClick={handleSaveRole}
                    >
                      {saving ? 'Saving...' : 'Save Role'}
                    </button>
                  ) : null}
                </div>

                <AvatarUpload profile={profile} editable={false} />
              </div>
            </div>
          </div>

          <div className="profile-access-section">
            <h3>Access &amp; Permissions</h3>
            <p className="muted toolbar-note">
              Read-only view of what this user can do based on their role.
            </p>
            <AccessMatrixTable
              tabs={matrix?.tabs || []}
              activeTab={activeTab}
              onTabChange={setActiveTab}
            />
          </div>
        </>
      )}
    </AppShell>
  )
}
