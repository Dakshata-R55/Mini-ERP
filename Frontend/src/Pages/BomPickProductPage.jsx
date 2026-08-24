import { useEffect, useState } from 'react'
import AppShell from '../components/AppShell'
import { listProducts } from '../api/products'
import { listBoms } from '../api/boms'

export default function BomPickProductPage({
  session,
  onSignOut,
  onNavigate,
  onOpenProfile,
  onBack,
  onContinue,
}) {
  const [finishedProducts, setFinishedProducts] = useState([])
  const [existingBomProductIds, setExistingBomProductIds] = useState(new Set())
  const [selectedProductId, setSelectedProductId] = useState('')
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    loadData()
  }, [])

  async function loadData() {
    setLoading(true)
    setError('')
    try {
      const [finished, boms] = await Promise.all([
        listProducts({ type: 'FINISHED_GOOD' }),
        listBoms(),
      ])
      setFinishedProducts(finished)
      setExistingBomProductIds(new Set(boms.map((b) => b.finishedProductId)))
    } catch (err) {
      setError(err.message || 'Failed to load products')
    } finally {
      setLoading(false)
    }
  }

  const availableProducts = finishedProducts.filter(
    (p) => !existingBomProductIds.has(p.id),
  )

  function handleContinue() {
    if (!selectedProductId) {
      setError('Select the finished good you want to manufacture')
      return
    }
    onContinue(Number(selectedProductId))
  }

  return (
    <AppShell
      session={session}
      onSignOut={onSignOut}
      onNavigate={onNavigate}
      onOpenProfile={onOpenProfile}
      currentModule="boms"
      pageTitle="New BOM"
    >
      <div className="page-toolbar">
        <button type="button" className="ghost-btn" onClick={onBack}>
          ← Back
        </button>
        <h2>New Bill of Materials</h2>
      </div>

      {error ? <div className="error-banner">{error}</div> : null}

      <div className="form-card">
        <p className="muted toolbar-note">
          First choose which <strong>finished good</strong> this BOM will produce.
          Then you will add raw materials and operations.
        </p>

        {loading ? (
          <p className="muted center-pad">Loading products...</p>
        ) : (
          <>
            <div className="field">
              <label htmlFor="finishedProductPick">Product to manufacture</label>
              <select
                id="finishedProductPick"
                value={selectedProductId}
                onChange={(e) => {
                  setSelectedProductId(e.target.value)
                  setError('')
                }}
              >
                <option value="">Select finished good</option>
                {availableProducts.map((p) => (
                  <option key={p.id} value={p.id}>
                    {p.name} ({p.reference})
                  </option>
                ))}
              </select>
            </div>

            {finishedProducts.length > 0 && availableProducts.length === 0 ? (
              <p className="muted">
                All finished goods already have a BOM. Edit an existing BOM instead.
              </p>
            ) : null}

            <button
              type="button"
              className="primary-btn"
              disabled={!selectedProductId}
              onClick={handleContinue}
            >
              Continue to BOM recipe
            </button>
          </>
        )}
      </div>
    </AppShell>
  )
}