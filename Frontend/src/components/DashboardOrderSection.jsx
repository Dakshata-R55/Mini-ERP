import DashboardStatusPill from './DashboardStatusPill'

export default function DashboardOrderSection({
  title,
  allStats,
  myStats,
  allPills,
  myPills,
  onPillClick,
}) {
  return (
    <section className="dashboard-order-section">
      <h2 className="dashboard-section-title">{title}</h2>

      <div className="dashboard-scope-row">
        <span className="dashboard-scope-label">All</span>
        <div className="dashboard-pill-row">
          {allPills.map((pill) => (
            <DashboardStatusPill
              key={`all-${pill.key}`}
              count={allStats?.[pill.field] ?? 0}
              label={pill.label}
              onClick={() => onPillClick({ scope: 'all', ...pill.filter })}
            />
          ))}
        </div>
      </div>

      <div className="dashboard-scope-row">
        <span className="dashboard-scope-label">My</span>
        <div className="dashboard-pill-row">
          {myPills.map((pill) => (
            <DashboardStatusPill
              key={`my-${pill.key}`}
              count={myStats?.[pill.field] ?? 0}
              label={pill.label}
              onClick={() => onPillClick({ scope: 'mine', ...pill.filter })}
            />
          ))}
        </div>
      </div>
    </section>
  )
}