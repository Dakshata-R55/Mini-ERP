import { useEffect, useState } from 'react'
import AppShell from '../components/AppShell'
import ProductFormFields from '../components/ProductFormFields'
import { createProduct, getProduct, updateProduct } from '../api/products'

const emptyForm = {
  name: '',
  salesPrice: '',
  costPrice: '',
  onHandQty: '0',
  freeToUseQty: '0.00',
  procureOnDemand: false,
  procurementType: '',
  vendorName: '',
  bomName: '',
}

export default function ProductFormPage({
  session,
  onSignOut,
  onNavigate,
  productId,
  onBack,
  onSaved,
  onOpenLogs,
}) {
  const isEdit = Boolean(productId)
  const [form, setForm] = useState(emptyForm)
  const [reference, setReference] = useState('')
  const [loading, setLoading] = useState(isEdit)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => {
    if (!isEdit) return

    async function loadProduct() {
      setLoading(true)
      setError('')
      try {
        const data = await getProduct(productId)
        setReference(data.reference)
        setForm({
          name: data.name,
          salesPrice: String(data.salesPrice),
          costPrice: String(data.costPrice),
          onHandQty: String(data.onHandQty),
          freeToUseQty: Number(data.freeToUseQty || 0).toFixed(2),
          procureOnDemand: data.procureOnDemand,
          procurementType: data.procurementType || '',
          vendorName: data.vendorName || '',
          bomName: data.bomName || '',
        })
      } catch (err) {
        setError(err.message || 'Failed to load product')
      } finally {
        setLoading(false)
      }
    }

    loadProduct()
  }, [isEdit, productId])

  function buildPayload() {
    return {
      name: form.name.trim(),
      salesPrice: Number(form.salesPrice),
      costPrice: Number(form.costPrice),
      onHandQty: Number(form.onHandQty),
      procureOnDemand: form.procureOnDemand,
      procurementType: form.procureOnDemand ? form.procurementType : null,
      vendorName: form.procurementType === 'PURCHASE' ? form.vendorName.trim() : null,
      bomName: form.procurementType === 'MANUFACTURING' ? form.bomName.trim() : null,
    }
  }

  async function handleSave() {
    setSaving(true)
    setError('')
    try {
      const payload = buildPayload()
      if (isEdit) {
        await updateProduct(productId, payload)
        onSaved()
      } else {
        const saved = await createProduct(payload)
        onOpenLogs(saved.id)
        setScreenAfterCreate(saved.id)
      }
    } catch (err) {
      setError(err.message || 'Failed to save product')
    } finally {
      setSaving(false)
    }
  }

  function setScreenAfterCreate(id) {
    onSaved()
    if (onOpenLogs) onOpenLogs(id)
  }

  return (
    <AppShell
      session={session}
      onSignOut={onSignOut}
      onNavigate={onNavigate}
      currentModule="products"
    >
      <div className="form-toolbar">
        <div className="form-toolbar-left">
          <button type="button" className="ghost-btn" onClick={onBack}>
            Back
          </button>
          <button
            type="button"
            className="primary-btn small-btn"
            onClick={handleSave}
            disabled={saving || loading}
          >
            {saving ? 'Saving...' : 'Save'}
          </button>
        </div>

        {isEdit ? (
          <button type="button" className="ghost-btn" onClick={() => onOpenLogs(productId)}>
            Logs
          </button>
        ) : null}
      </div>

      <div className="form-card">
        <h2>{isEdit ? `Edit Product — ${reference}` : 'New Product'}</h2>

        {error ? <div className="error-banner">{error}</div> : null}

        {loading ? (
          <p className="muted">Loading...</p>
        ) : (
          <ProductFormFields form={form} onChange={setForm} />
        )}
      </div>
    </AppShell>
  )
}