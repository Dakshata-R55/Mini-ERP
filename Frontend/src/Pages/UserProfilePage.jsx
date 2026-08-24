import { useEffect, useState } from 'react'
import AppShell from '../components/AppShell'
import AvatarUpload from '../components/AvatarUpload'
import { getMyProfile, updateMyProfile, uploadMyAvatar } from '../api/profile'
import { ROLE_LABELS } from '../utils/userDisplay'

const emptyForm = {
  fullName: '',
  address: '',
  mobile: '',
}

export default function UserProfilePage({
  session,
  avatarUrl,
  onSignOut,
  onNavigate,
  onOpenProfile,
  onProfileUpdated,
  onBack,
}) {
  const [profile, setProfile] = useState(null)
  const [form, setForm] = useState(emptyForm)
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [uploading, setUploading] = useState(false)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')

  useEffect(() => {
    loadProfile()
  }, [])

  async function loadProfile() {
    setLoading(true)
    setError('')
    try {
      const data = await getMyProfile()
      setProfile(data)
      setForm({
        fullName: data.fullName || '',
        address: data.address || '',
        mobile: data.mobile || '',
      })
    } catch (err) {
      setError(err.message || 'Failed to load profile')
    } finally {
      setLoading(false)
    }
  }

  async function handleSave(e) {
    e.preventDefault()
    setSaving(true)
    setError('')
    setSuccess('')
    try {
      const payload = {
        fullName: form.fullName.trim(),
        address: form.address.trim() || null,
        mobile: form.mobile.trim() || null,
      }
      const updated = await updateMyProfile(payload)
      setProfile(updated)
      setSuccess('Profile saved')
      onProfileUpdated?.(updated)
    } catch (err) {
      setError(err.message || 'Failed to save profile')
    } finally {
      setSaving(false)
    }
  }

  async function handleAvatarUpload(file) {
    setUploading(true)
    setError('')
    setSuccess('')
    try {
      const result = await uploadMyAvatar(file)
      const updated = { ...profile, avatarUrl: result.avatarUrl }
      setProfile(updated)
      setSuccess('Photo updated')
      onProfileUpdated?.(updated)
    } finally {
      setUploading(false)
    }
  }

  const shellProfile = profile
    ? { ...session, avatarUrl: profile.avatarUrl || avatarUrl }
    : { ...session, avatarUrl }

  return (
    <AppShell
      session={shellProfile}
      onSignOut={onSignOut}
      onNavigate={onNavigate}
      onOpenProfile={onOpenProfile}
      currentModule="profile"
      pageTitle="My Profile"
    >
      <div className="page-toolbar">
        <button type="button" className="ghost-btn" onClick={onBack}>
          Back
        </button>
        <h2>My Profile</h2>
      </div>

      {error ? <div className="error-banner">{error}</div> : null}
      {success ? <div className="success-banner">{success}</div> : null}

      {loading ? (
        <p className="muted center-pad">Loading profile...</p>
      ) : (
        <div className="profile-layout">
          <form className="profile-card form-card" onSubmit={handleSave}>
            <div className="profile-card-grid">
              <div className="profile-fields">
                <div className="field">
                  <label htmlFor="fullName">Name</label>
                  <input
                    id="fullName"
                    value={form.fullName}
                    onChange={(e) => setForm({ ...form, fullName: e.target.value })}
                    required
                    maxLength={200}
                  />
                </div>

                <div className="field">
                  <label htmlFor="address">Address</label>
                  <textarea
                    id="address"
                    rows="3"
                    value={form.address}
                    onChange={(e) => setForm({ ...form, address: e.target.value })}
                    maxLength={500}
                  />
                </div>

                <div className="field">
                  <label htmlFor="mobile">Mobile Number</label>
                  <input
                    id="mobile"
                    value={form.mobile}
                    onChange={(e) => setForm({ ...form, mobile: e.target.value })}
                    maxLength={30}
                  />
                </div>

                <div className="field">
                  <label htmlFor="email">Email ID</label>
                  <input
                    id="email"
                    value={profile?.email || ''}
                    readOnly
                    className="readonly-input"
                  />
                </div>

                <div className="field">
                  <label htmlFor="position">Position / Role</label>
                  <input
                    id="position"
                    value={ROLE_LABELS[profile?.userType] || profile?.userType || ''}
                    readOnly
                    className="readonly-input"
                  />
                </div>

                <button type="submit" className="primary-btn" disabled={saving || uploading}>
                  {saving ? 'Saving...' : 'Save Profile'}
                </button>
              </div>

              <AvatarUpload
                profile={profile}
                editable
                uploading={uploading}
                onUpload={handleAvatarUpload}
              />
            </div>
          </form>
        </div>
      )}
    </AppShell>
  )
}
