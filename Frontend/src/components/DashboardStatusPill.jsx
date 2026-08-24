export default function DashboardStatusPill({ count, label, onClick, active }) {
    return (
      <button
        type="button"
        className={`dashboard-pill ${active ? 'active' : ''}`}
        onClick={onClick}
      >
        <span className="dashboard-pill-count">{count}</span>
        <span className="dashboard-pill-label">{label}</span>
      </button>
    )
  }