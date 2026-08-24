import { useEffect, useState } from 'react'
import AppShell from '../components/AppShell'
import PurchaseOrderLineTable from '../components/PurchaseOrderLineTable'
import { listVendors } from '../api/vendors'
import {
  createPurchaseOrder,
  getPurchaseOrder,
  updatePurchaseOrder,
  confirmPurchaseOrder,
  receivePurchaseOrder,
  cancelPurchaseOrder,
} from '../api/purchaseOrders'

const emptyLine = () => ({
  productId: '',
  productName: '',
  orderedQty: '1',
  receivedQty: '0',
  unitCostPrice: '0',
  units: 'Units',
})

export default function PurchaseOrderFormPage({
  session,
  onSignOut,
  onNavigate,
  onOpenProfile,
  orderId,
  onBack,
  onSaved,
  onOpenLogs,
}) {
  const isEdit = Boolean(orderId)
  const [vendors, setVendors] = useState([])
  const [order, setOrder] = useState(null)
  const [form, setForm] = useState({
    vendorId: '',
    vendorAddress: '',
    startDate: new Date().toISOString().slice(0, 10),
    lines: [emptyLine()],
  })
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')

  const status = order?.status || 'DRAFT'
  const isDraft = status === 'DRAFT'
  const isReceiveMode = status === 'CONFIRMED' || status === 'PARTIALLY_RECEIVED'
  const readonly = !isDraft

  useEffect(() => {
    loadData()
  }, [orderId])

  async function loadData() {
    setLoading(true)
    setError('')
    try {
      const vendorData = await listVendors()
      setVendors(vendorData)

      if (isEdit) {
        const data = await getPurchaseOrder(orderId)
        setOrder(data)
        setForm({
          vendorId: String(data.vendorId),
          vendorAddress: data.vendorAddress || '',
          startDate: data.startDate,
          lines: data.lines.map((line) => ({
            id: line.id,
            productId: String(line.productId),
            productName: line.productName,
            orderedQty: String(line.orderedQty),
            receivedQty: String(line.receivedQty ?? 0),
            unitCostPrice: String(line.unitCostPrice),
            units: line.units || 'Units',
          })),
        })
      }
    } catch (err) {
      setError(err.message || 'Failed to load purchase order')
    } finally {
      setLoading(false)
    }
  }

  function buildPayload() {
    return {
      vendorId: Number(form.vendorId),
      vendorAddress: form.vendorAddress,
      startDate: form.startDate,
      responsiblePersonId: null,
      lines: form.lines.map((line) => ({
        productName: line.productName.trim(),
        orderedQty: Number(line.orderedQty),
        unitCostPrice: Number(line.unitCostPrice || 0),
        units: line.units || 'Units',
      })),
    }
  }

  async function handleSave() {
    const hasEmptyMaterial = form.lines.some((line) => !line.productName?.trim())
    if (hasEmptyMaterial) {
      setError('Each line needs a raw material name')
      return
    }

    setSaving(true)
    setError('')
    try {
      const payload = buildPayload()
      if (isEdit) {
        await updatePurchaseOrder(orderId, payload)
        onSaved()
      } else {
        const created = await createPurchaseOrder(payload)
        onBack(created.id)
      }
    } catch (err) {
      setError(err.message || 'Failed to save')
    } finally {
      setSaving(false)
    }
  }

  async function handleConfirm() {
    setSaving(true)
    setError('')
    try {
      if (isDraft) {
        await updatePurchaseOrder(orderId, buildPayload())
      }
      await confirmPurchaseOrder(orderId)
      await loadData()
    } catch (err) {
      setError(err.message || 'Failed to confirm')
    } finally {
      setSaving(false)
    }
  }

  async function handleReceive() {
    const missingLineId = form.lines.some((line) => !line.id)
    if (missingLineId) {
      setError('Order lines are missing ids. Reload the page and try again.')
      return
    }

    const receiveLines = form.lines.map((line) => {
      const ordered = Number(line.orderedQty || 0)
      const entered = Number(line.receivedQty || 0)
      // If received qty left at 0, receive the full ordered amount
      const receivedQty = entered > 0 ? entered : ordered
      return { lineId: Number(line.id), receivedQty }
    })

    if (receiveLines.every((line) => line.receivedQty <= 0)) {
      setError('Nothing to receive on this order')
      return
    }

    setSaving(true)
    setError('')
    setSuccess('')
    try {
      await receivePurchaseOrder(orderId, { lines: receiveLines })
      setSuccess('Goods received and stock updated')
      await loadData()
    } catch (err) {
      setError(err.message || 'Failed to receive')
    } finally {
      setSaving(false)
    }
  }

  async function handleCancel() {
    if (!window.confirm('Cancel this purchase order?')) return
    setSaving(true)
    setError('')
    try {
      await cancelPurchaseOrder(orderId)
      await loadData()
    } catch (err) {
      setError(err.message || 'Failed to cancel')
    } finally {
      setSaving(false)
    }
  }

  function handleVendorChange(vendorId) {
    const vendor = vendors.find((v) => String(v.id) === String(vendorId))
    setForm({
      ...form,
      vendorId,
      vendorAddress: vendor?.address || '',
    })
  }

  return (
    <AppShell
      session={session}
      onSignOut={onSignOut}
      onNavigate={onNavigate}
      onOpenProfile={onOpenProfile}
      currentModule="purchase-orders"
      pageTitle={order?.reference || 'New Purchase Order'}
    >
      <div className="form-toolbar">
        <div className="form-toolbar-left">
          <button type="button" className="ghost-btn" onClick={() => onBack()}>
            Back
          </button>

          {isDraft ? (
            <button
              type="button"
              className="primary-btn small-btn"
              onClick={handleSave}
              disabled={saving || loading}
            >
              {saving ? 'Saving...' : 'Save'}
            </button>
          ) : null}

          {isDraft && isEdit ? (
            <button
              type="button"
              className="primary-btn small-btn"
              onClick={handleConfirm}
              disabled={saving || loading}
            >
              Confirm
            </button>
          ) : null}

          {isReceiveMode ? (
            <button
              type="button"
              className="primary-btn small-btn"
              onClick={handleReceive}
              disabled={saving || loading}
            >
              Receive
            </button>
          ) : null}

          {isEdit && status !== 'CANCELLED' && status !== 'FULLY_RECEIVED' ? (
            <button
              type="button"
              className="ghost-btn danger-btn"
              onClick={handleCancel}
              disabled={saving || loading}
            >
              Cancel
            </button>
          ) : null}
        </div>

        {isEdit ? (
          <button type="button" className="ghost-btn" onClick={() => onOpenLogs(orderId)}>
            Logs
          </button>
        ) : null}
      </div>

      <div className="form-card">
        <div className="so-header">
          <div className="so-ref">{order?.reference || 'New Purchase Order'}</div>
          <div className="so-status">Status: {status.replaceAll('_', ' ')}</div>
        </div>

        {error ? <div className="error-banner">{error}</div> : null}
        {success ? <div className="success-banner">{success}</div> : null}

        {loading ? (
          <p className="muted">Loading...</p>
        ) : (
          <>
            <div className="so-form-grid">
              <div className="field">
                <label>Vendor</label>
                <select
                  value={form.vendorId}
                  disabled={readonly}
                  onChange={(e) => handleVendorChange(e.target.value)}
                  required
                >
                  <option value="">Select vendor</option>
                  {vendors.map((v) => (
                    <option key={v.id} value={v.id}>
                      {v.name}
                    </option>
                  ))}
                </select>
              </div>

              <div className="field">
                <label>Creation Date</label>
                <input
                  type="date"
                  value={form.startDate}
                  disabled={readonly}
                  onChange={(e) => setForm({ ...form, startDate: e.target.value })}
                  required
                />
              </div>

              <div className="field full-width">
                <label>Vendor Address</label>
                <textarea
                  rows="3"
                  value={form.vendorAddress}
                  disabled={readonly}
                  onChange={(e) => setForm({ ...form, vendorAddress: e.target.value })}
                />
              </div>

              <div className="field">
                <label>Responsible Person</label>
                <input
                  value={order?.responsiblePersonName || session.loginId}
                  readOnly
                  className="readonly-input"
                />
              </div>
            </div>

            <PurchaseOrderLineTable
              lines={form.lines}
              readonly={readonly}
              receiveMode={isReceiveMode}
              onChange={(lines) => setForm({ ...form, lines })}
              onAddLine={() => setForm({ ...form, lines: [...form.lines, emptyLine()] })}
              onRemoveLine={(index) =>
                setForm({ ...form, lines: form.lines.filter((_, i) => i !== index) })
              }
            />
          </>
        )}
      </div>
    </AppShell>
  )
}