import { useEffect, useState } from 'react'
import AppShell from '../components/AppShell'
import { getPurchaseOrderLogs } from '../api/auditLogs'

export default function PurchaseOrderLogsPage({
  session,
  onSignOut,
  onNavigate,
  onOpenProfile,
  orderId,
  onBack,
}) {
  const [logs, setLogs] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    async function load() {
      setLoading(true)
      setError('')
      try {
        const data = await getPurchaseOrderLogs(orderId)
        setLogs(data)
      } catch (err) {
        setError(err.message || 'Failed to load logs')
      } finally {
        setLoading(false)
      }
    }
    load()
  }, [orderId])

  return (
    <AppShell
      session={session}
      onSignOut={onSignOut}
      onNavigate={onNavigate}
      onOpenProfile={onOpenProfile}
      currentModule="purchase-orders"
    >
      <div className="form-toolbar">
        <button type="button" className="ghost-btn" onClick={onBack}>
          Back
        </button>
        <h2>Purchase Order Logs</h2>
      </div>

      {error ? <div className="error-banner">{error}</div> : null}

      <div className="table-card">
        {loading ? (
          <p className="muted center-pad">Loading logs...</p>
        ) : (
          <table className="data-table">
            <thead>
              <tr>
                <th>When</th>
                <th>Action</th>
                <th>Field</th>
                <th>Old</th>
                <th>New</th>
                <th>By</th>
              </tr>
            </thead>
            <tbody>
              {logs.length === 0 ? (
                <tr>
                  <td colSpan="6" className="center-pad muted">
                    No logs yet.
                  </td>
                </tr>
              ) : (
                logs.map((log) => (
                  <tr key={log.id}>
                    <td>{new Date(log.changedAt).toLocaleString()}</td>
                    <td>{log.action}</td>
                    <td>{log.fieldName}</td>
                    <td>{log.oldValue ?? '-'}</td>
                    <td>{log.newValue ?? '-'}</td>
                    <td>{log.changedBy}</td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        )}
      </div>
    </AppShell>
  )
}