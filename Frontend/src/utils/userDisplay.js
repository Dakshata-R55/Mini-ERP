export const ROLE_LABELS = {
  SYSTEM_ADMIN: 'System Admin',
  ADMIN: 'Admin',
  SALES_USER: 'Sales User',
  PURCHASE_USER: 'Purchase User',
  MANUFACTURING_USER: 'Manufacturing User',
  PROJECT_MANAGER: 'Project Manager',
  NONE: 'None (no access)',
}

export const ASSIGNABLE_ROLES = [
  'ADMIN',
  'SALES_USER',
  'PURCHASE_USER',
  'MANUFACTURING_USER',
  'PROJECT_MANAGER',
  'NONE',
]

export function displayName(profile) {
  if (profile?.fullName?.trim()) return profile.fullName.trim()
  return profile?.loginId || 'User'
}

export function avatarInitials(profile) {
  const name = displayName(profile)
  const parts = name.split(/\s+/).filter(Boolean)
  if (parts.length >= 2) {
    return (parts[0][0] + parts[1][0]).toUpperCase()
  }
  return name.slice(0, 2).toUpperCase()
}
