export default function MasterMenu({ session, current, onNavigate }) {
  const items = [
    { key: 'dashboard', label: 'Dashboard', icon: '▣', roles: ['ADMIN', 'PROJECT_MANAGER', 'SALES_USER', 'PURCHASE_USER'] },
    { key: 'sales-orders', label: 'Sale Orders', icon: '◎', roles: ['SALES_USER', 'ADMIN'] },
    { key: 'purchase-orders', label: 'Purchase Orders', icon: '◫', roles: ['PURCHASE_USER', 'ADMIN'] },
    { key: 'products', label: 'Products', icon: '▤', roles: ['ADMIN', 'PROJECT_MANAGER'] },
    { key: 'users', label: 'User Management', icon: '◉', roles: ['SYSTEM_ADMIN'] },
  ]

  const visible = items.filter((item) => item.roles.includes(session.userType))

  return (
    <nav className="sidebar-nav">
      <ul>
        {visible.map((item) => (
          <li key={item.key}>
            <button
              type="button"
              className={`sidebar-link ${current === item.key ? 'active' : ''}`}
              onClick={() => onNavigate(item.key)}
            >
              <span className="sidebar-icon">{item.icon}</span>
              <span>{item.label}</span>
            </button>
          </li>
        ))}
      </ul>
    </nav>
  )
}