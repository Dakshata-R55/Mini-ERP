import { useState } from 'react'
import { avatarInitials } from '../utils/userDisplay'

const MAX_BYTES = 2 * 1024 * 1024
const ALLOWED_TYPES = ['image/jpeg', 'image/png', 'image/webp']

export default function AvatarUpload({
  profile,
  editable = false,
  uploading = false,
  onUpload,
  uploadError = '',
}) {
  const [localError, setLocalError] = useState('')
  const error = uploadError || localError
  const initials = avatarInitials(profile)

  async function handlePick(e) {
    const file = e.target.files?.[0]
    e.target.value = ''
    if (!file || !onUpload) return

    setLocalError('')

    if (!ALLOWED_TYPES.includes(file.type)) {
      setLocalError('Only JPG, PNG, or WEBP images are allowed')
      return
    }
    if (file.size > MAX_BYTES) {
      setLocalError('Image must be 2 MB or smaller')
      return
    }

    try {
      await onUpload(file)
    } catch (err) {
      setLocalError(err.message || 'Failed to upload avatar')
    }
  }

  return (
    <div className="profile-avatar-block">
      <div className="profile-avatar-wrap">
        {profile?.avatarUrl ? (
          <img src={profile.avatarUrl} alt={initials} className="profile-avatar-image" />
        ) : (
          <div className="profile-avatar-fallback">{initials}</div>
        )}

        {editable ? (
          <label className="profile-avatar-edit" title="Change photo">
            ✎
            <input
              type="file"
              accept="image/jpeg,image/png,image/webp"
              onChange={handlePick}
              disabled={uploading}
              hidden
            />
          </label>
        ) : null}
      </div>

      {editable ? (
        <p className="muted profile-avatar-hint">
          {uploading ? 'Uploading...' : 'Upload or change your photo'}
        </p>
      ) : null}

      {error ? <div className="error-banner">{error}</div> : null}
    </div>
  )
}
