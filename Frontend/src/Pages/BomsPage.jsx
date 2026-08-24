import { useEffect, useState } from 'react'
import AppShell from '../components/AppShell'
import { listBoms } from '../api/boms'

export default function BomsPage({
  session,
  onSignOut,
  onNavigate,
  onOpenProfile,
  onCreate,
  onOpenBom,
}) {
  const [boms, setBoms] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    loadBoms()
  }, [])

  async function loadBoms() {
    setLoading(true)
    setError('')
    try {
      setBoms(await listBoms())
    } catch (err) {
      setError(err.message || 'Failed to load BOMs')
    } finally {
      setLoading(false)
    }
  }

  return (
    <AppShell
      session={session}
      onSignOut={onSignOut}
      onNavigate={onNavigate}
      onOpenProfile={onOpenProfile}
      currentModule="boms"
      pageTitle="Bills of Materials"
    >
      <div className="page-toolbar">
        <button type="button" className="primary-btn small-btn" onClick={onCreate}>
          + New BOM
        </button>
        <h2>Bills of Materials</h2>
      </div>

      {error ? <div className="error-banner">{error}</div> : null}

      <div className="table-card">
        {loading ? (
          <p className="muted center-pad">Loading BOMs...</p>
        ) : (
          <table className="data-table">
            <thead>
              <tr>
                <th>Reference</th>
                <th>Finished Product</th>
                <th>Output Qty</th>
                <th>Components</th>
                <th>Operations</th>
              </tr>
            </thead>
            <tbody>
              {boms.length === 0 ? (
                <tr>
                  <td colSpan="5" className="center-pad muted">No BOMs yet.</td>
                </tr>
              ) : (
                boms.map((bom) => (
                  <tr key={bom.id} onClick={() => onOpenBom(bom.id)}>
                    <td>{bom.reference}</td>
                    <td>{bom.finishedProductName}</td>
                    <td>{Number(bom.outputQty).toFixed(2)}</td>
                    <td>{bom.components?.length ?? 0}</td>
                    <td>{bom.operations?.length ?? 0}</td>
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
