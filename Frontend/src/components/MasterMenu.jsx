export default function MasterMenu({ open, session, current, onNavigate, onClose }) {
    if (!open) return null
  
    const items = [
      { key: 'sales-orders', label: 'Sale Orders', roles: ['SALES_USER', 'ADMIN'] },
      { key: 'products', label: 'Products', roles: ['ADMIN', 'PROJECT_MANAGER'] },
      { key: 'users', label: 'User Management', roles: ['SYSTEM_ADMIN'] },
    ]
  
    const visible = items.filter((item) => item.roles.includes(session.userType))
  
    return (
      <div className="menu-overlay" onClick={onClose}>
        <aside className="master-menu" onClick={(e) => e.stopPropagation()}>
          <h3>Master Menu</h3>
          <ul>
            {visible.map((item) => (
              <li key={item.key}>
                <button
                  type="button"
                  className={current === item.key ? 'active' : ''}
                  onClick={() => {
                    onNavigate(item.key)
                    onClose()
                  }}
                >
                  {item.label}
                </button>
              </li>
            ))}
          </ul>
        </aside>
      </div>
    )
  }