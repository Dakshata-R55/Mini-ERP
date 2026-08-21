import { useState } from 'react'
import logo from '../assets/logo.png'
import MasterMenu from './MasterMenu'

export default function AppShell({
  session,
  onSignOut,
  onNavigate,
  currentModule,
  children,
}) {
  const [menuOpen, setMenuOpen] = useState(false)

  return (
    <div className="erp-app">
      <header className="erp-header">
        <div className="erp-header-left">
          <button
            type="button"
            className="icon-btn"
            aria-label="Menu"
            onClick={() => setMenuOpen(true)}
          >
            ☰
          </button>
        </div>

        <div className="erp-header-center">
          <img src={logo} alt="" className="erp-logo" />
          <div>
            <strong>SHIV FURNITURE WORKS</strong>
            <span>Mini ERP</span>
          </div>
        </div>

        <div className="erp-header-right">
          <span className="user-pill">{session.loginId}</span>
          <button type="button" className="ghost-btn" onClick={onSignOut}>
            Sign out
          </button>
        </div>
      </header>

      <MasterMenu
        open={menuOpen}
        session={session}
        current={currentModule}
        onNavigate={onNavigate}
        onClose={() => setMenuOpen(false)}
      />

      <main className="erp-main">{children}</main>
    </div>
  )
}