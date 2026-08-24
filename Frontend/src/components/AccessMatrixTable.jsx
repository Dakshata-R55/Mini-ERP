function cellLabel(value) {
  switch (value) {
    case 'YES':
      return '✓'
    case 'NO':
      return '✗'
    case 'AUTO':
      return 'Auto'
    case 'RECOMPUTED':
      return 'Recomputed'
    case 'NOT_POSSIBLE':
      return 'Not possible'
    case 'SYSTEM_COMPUTED':
      return 'System computed'
    default:
      return value || '—'
  }
}

function cellClass(value) {
  if (value === 'YES') return 'matrix-yes'
  if (value === 'NO') return 'matrix-no'
  return 'matrix-special'
}

export default function AccessMatrixTable({ tabs, activeTab, onTabChange }) {
  const current = tabs.find((tab) => tab.key === activeTab) || tabs[0]

  return (
    <div className="access-matrix">
      <div className="access-matrix-tabs">
        {tabs.map((tab) => (
          <button
            key={tab.key}
            type="button"
            className={`access-tab ${current?.key === tab.key ? 'active' : ''}`}
            onClick={() => onTabChange(tab.key)}
          >
            {tab.label}
          </button>
        ))}
      </div>

      <div className="table-card">
        <table className="data-table access-matrix-table">
          <thead>
            <tr>
              <th>Field</th>
              <th>Create</th>
              <th>View</th>
              <th>Edit</th>
              <th>Delete</th>
            </tr>
          </thead>
          <tbody>
            {current?.rows?.length ? (
              current.rows.map((row) => (
                <tr key={row.field}>
                  <td>{row.field}</td>
                  <td className={cellClass(row.create)}>{cellLabel(row.create)}</td>
                  <td className={cellClass(row.view)}>{cellLabel(row.view)}</td>
                  <td className={cellClass(row.edit)}>{cellLabel(row.edit)}</td>
                  <td className={cellClass(row.delete)}>{cellLabel(row.delete)}</td>
                </tr>
              ))
            ) : (
              <tr>
                <td colSpan="5" className="center-pad muted">
                  No access data for this module.
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  )
}
