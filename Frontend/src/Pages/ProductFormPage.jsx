import { useEffect, useState } from 'react'
import AppShell from '../components/AppShell'
import ProductFormFields from '../components/ProductFormFields'
import { createProduct, getProduct, updateProduct } from '../api/products'

const emptyForm = {
  name: '',
  productType: 'FINISHED_GOOD',
  salesPrice: '',
  costPrice: '',
  onHandQty: '0',
  freeToUseQty: '0.00',
  imageUrl: '',
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
          productType: data.productType || 'FINISHED_GOOD',
          salesPrice: String(data.salesPrice),
          costPrice: String(data.costPrice),
          onHandQty: String(data.onHandQty),
          freeToUseQty: Number(data.freeToUseQty || 0).toFixed(2),
          imageUrl: data.imageUrl || '',
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
    const isRaw = form.productType === 'RAW_MATERIAL'
    const base = {
      name: form.name.trim(),
      productType: form.productType,
      salesPrice: isRaw ? Number(form.salesPrice || 0) : Number(form.salesPrice),
      costPrice: Number(form.costPrice),
      procureOnDemand: isRaw ? false : form.procureOnDemand,
      procurementType: isRaw || !form.procureOnDemand ? null : form.procurementType,
      vendorName: isRaw || form.procurementType !== 'PURCHASE' ? null : form.vendorName.trim(),
      bomName: isRaw || form.procurementType !== 'MANUFACTURING' ? null : form.bomName.trim(),
      imageUrl: form.imageUrl || null,
    }

    if (isEdit) {
      return base
    }

    return {
      ...base,
      openingStock: Number(form.onHandQty || 0),
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
          <ProductFormFields form={form} onChange={setForm} readOnlyQty={isEdit} />
        )}
      </div>
    </AppShell>
  )
}