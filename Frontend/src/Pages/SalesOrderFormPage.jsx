import { useEffect, useState } from 'react'
import AppShell from '../components/AppShell'
import SalesOrderLineTable from '../components/SalesOrderLineTable'
import CustomerFormModal from '../components/CustomerFormModal'
import { listCustomers } from '../api/customers'
import { listProducts } from '../api/products'
import {
  createSalesOrder,
  getSalesOrder,
  updateSalesOrder,
  confirmSalesOrder,
  deliverSalesOrder,
  cancelSalesOrder,
} from '../api/salesOrders'

const emptyLine = () => ({
  productId: '',
  orderedQty: '1',
  deliveredQty: '0',
  unitPrice: '',
  units: 'Units',
  freeToUseQty: 0,
})

export default function SalesOrderFormPage({
  session,
  onSignOut,
  onNavigate,
  orderId,
  onBack,
  onOpenLogs,
}) {
  const isEdit = Boolean(orderId)
  const [customers, setCustomers] = useState([])
  const [products, setProducts] = useState([])
  const [order, setOrder] = useState(null)
  const [form, setForm] = useState({
    customerId: '',
    customerAddress: '',
    startDate: new Date().toISOString().slice(0, 10),
    lines: [emptyLine()],
  })
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')
  const [customerModalOpen, setCustomerModalOpen] = useState(false)
  const [customerSuccess, setCustomerSuccess] = useState('')

  const status = order?.status || 'DRAFT'
  const isDraft = status === 'DRAFT'
  const isDeliverMode =
    status === 'CONFIRMED' || status === 'PARTIALLY_DELIVERED'
  const readonly = !isDraft

  useEffect(() => {
    loadData()
  }, [orderId])

  async function loadData() {
    setLoading(true)
    setError('')
    try {
      const [customerData, productData] = await Promise.all([
        listCustomers(),
        listProducts(),
      ])
      setCustomers(customerData)
      setProducts(productData)

      if (isEdit) {
        const data = await getSalesOrder(orderId)
        setOrder(data)
        setForm({
          customerId: String(data.customerId),
          customerAddress: data.customerAddress || '',
          startDate: data.startDate,
          lines: data.lines.map((line) => ({
            id: line.id,
            productId: String(line.productId),
            productName: line.productName,
            orderedQty: String(line.orderedQty),
            deliveredQty: String(line.deliveredQty ?? 0),
            unitPrice: String(line.unitPrice),
            units: line.units || 'Units',
            freeToUseQty: line.freeToUseQty,
          })),
        })
      }
    } catch (err) {
      setError(err.message || 'Failed to load sales order')
    } finally {
      setLoading(false)
    }
  }

  function buildPayload() {
    return {
      customerId: Number(form.customerId),
      customerAddress: form.customerAddress,
      startDate: form.startDate,
      salesPersonId: null,
      lines: form.lines.map((line) => ({
        productId: Number(line.productId),
        orderedQty: Number(line.orderedQty),
        unitPrice: Number(line.unitPrice),
        units: line.units || 'Units',
      })),
    }
  }

  async function handleSave() {
    setSaving(true)
    setError('')
    try {
      const payload = buildPayload()
      if (isEdit) {
        await updateSalesOrder(orderId, payload)
      } else {
        const created = await createSalesOrder(payload)
        onBack(created.id)
        return
      }
      await loadData()
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
      if (isDraft) await handleSave()
      await confirmSalesOrder(orderId)
      await loadData()
    } catch (err) {
      setError(err.message || 'Failed to confirm')
    } finally {
      setSaving(false)
    }
  }

  async function handleDeliver() {
    setSaving(true)
    setError('')
    try {
      await deliverSalesOrder(orderId, {
        lines: form.lines.map((line) => ({
          lineId: line.id,
          deliveredQty: Number(line.deliveredQty || 0),
        })),
      })
      await loadData()
    } catch (err) {
      setError(err.message || 'Failed to deliver')
    } finally {
      setSaving(false)
    }
  }

  async function handleCancel() {
    if (!window.confirm('Cancel this sales order?')) return
    setSaving(true)
    setError('')
    try {
      await cancelSalesOrder(orderId)
      await loadData()
    } catch (err) {
      setError(err.message || 'Failed to cancel')
    } finally {
      setSaving(false)
    }
  }

  function handleCustomerChange(customerId) {
    const customer = customers.find((c) => String(c.id) === String(customerId))
    setForm({
      ...form,
      customerId,
      customerAddress: customer?.address || '',
    })
  }

  function handleCustomerCreated(created) {
    setCustomers((prev) => {
      const exists = prev.some((c) => c.id === created.id)
      if (exists) return prev
      return [...prev, created].sort((a, b) => a.name.localeCompare(b.name))
    })
    setForm({
      ...form,
      customerId: String(created.id),
      customerAddress: created.address || '',
    })
    setCustomerSuccess(`Customer "${created.name}" added and selected.`)
    setTimeout(() => setCustomerSuccess(''), 4000)
  }

  return (
    <AppShell
      session={session}
      onSignOut={onSignOut}
      onNavigate={onNavigate}
      currentModule="sales-orders"
      pageTitle={order?.reference || 'New Sales Order'}
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

          {isDeliverMode ? (
            <button
              type="button"
              className="primary-btn small-btn"
              onClick={handleDeliver}
              disabled={saving || loading}
            >
              Deliver
            </button>
          ) : null}

          {isEdit && status !== 'CANCELLED' && status !== 'FULLY_DELIVERED' ? (
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
          <div className="so-ref">{order?.reference || 'New Sales Order'}</div>
          <div className="so-status">Status: {status.replaceAll('_', ' ')}</div>
        </div>

        {customerSuccess ? <div className="success-banner">{customerSuccess}</div> : null}
        {error ? <div className="error-banner">{error}</div> : null}
        {loading ? (
          <p className="muted">Loading...</p>
        ) : (
          <>
            <div className="so-form-grid">
              <div className="field">
                <label>Customer</label>
                <div className="customer-field-row">
                  <select
                    value={form.customerId}
                    disabled={readonly}
                    onChange={(e) => handleCustomerChange(e.target.value)}
                    required
                  >
                    <option value="">Select customer</option>
                    {customers.map((c) => (
                      <option key={c.id} value={c.id}>
                        {c.name}
                      </option>
                    ))}
                  </select>
                  {!readonly ? (
                    <button
                      type="button"
                      className="primary-btn small-btn"
                      onClick={() => setCustomerModalOpen(true)}
                    >
                      + Add Customer
                    </button>
                  ) : null}
                </div>
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
                <label>Customer Address</label>
                <textarea
                  rows="3"
                  value={form.customerAddress}
                  disabled={readonly}
                  onChange={(e) =>
                    setForm({ ...form, customerAddress: e.target.value })
                  }
                />
              </div>

              <div className="field">
                <label>Sales Person</label>
                <input
                  value={order?.salesPersonName || session.loginId}
                  readOnly
                  className="readonly-input"
                />
              </div>
            </div>

            <SalesOrderLineTable
              lines={form.lines}
              products={products}
              readonly={readonly}
              deliverMode={isDeliverMode}
              onChange={(lines) => setForm({ ...form, lines })}
              onAddLine={() =>
                setForm({ ...form, lines: [...form.lines, emptyLine()] })
              }
              onRemoveLine={(index) =>
                setForm({
                  ...form,
                  lines: form.lines.filter((_, i) => i !== index),
                })
              }
            />
          </>
        )}
      </div>

      <CustomerFormModal
        open={customerModalOpen}
        onClose={() => setCustomerModalOpen(false)}
        onCreated={handleCustomerCreated}
      />
    </AppShell>
  )
}