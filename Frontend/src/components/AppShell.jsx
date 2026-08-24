import { useState } from 'react'
import logo from '../assets/logo.png'
import MasterMenu from './MasterMenu'
import { avatarInitials } from '../utils/userDisplay'

export default function AppShell({
  session,
  onSignOut,
  onNavigate,
  onOpenProfile,
  currentModule,
  pageTitle = 'Dashboard',
  children,
}) {
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false)
  const initials = avatarInitials(session)

  return (
    <div className="erp-layout">
      <aside className="erp-sidebar">
        <div className="sidebar-brand">
          <img src={logo} alt="" className="erp-logo" />
          <div>
            <strong>SHIV FURNITURE</strong>
            <span>Mini ERP Portal</span>
          </div>
        </div>

        <MasterMenu
          session={session}
          current={currentModule}
          onNavigate={onNavigate}
        />
      </aside>

      {mobileMenuOpen ? (
        <div className="menu-overlay mobile-only" onClick={() => setMobileMenuOpen(false)}>
          <aside className="master-menu mobile-drawer" onClick={(e) => e.stopPropagation()}>
            <div className="sidebar-brand">
              <img src={logo} alt="" className="erp-logo" />
              <div>
                <strong>SHIV FURNITURE</strong>
                <span>Mini ERP Portal</span>
              </div>
            </div>
            <MasterMenu
              session={session}
              current={currentModule}
              onNavigate={(key) => {
                onNavigate(key)
                setMobileMenuOpen(false)
              }}
            />
          </aside>
        </div>
      ) : null}

      <div className="erp-main-column">
        <header className="erp-topbar">
          <button
            type="button"
            className="icon-btn mobile-menu-btn"
            aria-label="Menu"
            onClick={() => setMobileMenuOpen(true)}
          >
            ☰
          </button>

          <h1 className="topbar-title">{pageTitle}</h1>

          <div className="topbar-right">
            <button
              type="button"
              className="user-avatar-btn"
              onClick={() => onOpenProfile?.()}
              title="My profile"
              aria-label="Open profile"
            >
              {session.avatarUrl ? (
                <img src={session.avatarUrl} alt="" className="user-avatar-image" />
              ) : (
                <div className="user-avatar">{initials}</div>
              )}
            </button>
            <div className="topbar-user">
              <strong>{session.loginId}</strong>
              <span>{session.userType?.replaceAll('_', ' ')}</span>
            </div>
            <button type="button" className="ghost-btn" onClick={onSignOut}>
              Sign out
            </button>
          </div>
        </header>

        <main className="erp-main">{children}</main>
      </div>
    </div>
  )
}