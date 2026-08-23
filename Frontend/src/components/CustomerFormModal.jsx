import { useState } from 'react'
import { createCustomer } from '../api/customers'

const emptyForm = {
  name: '',
  address: '',
  phone: '',
  email: '',
}

export default function CustomerFormModal({ open, onClose, onCreated }) {
  const [form, setForm] = useState(emptyForm)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')

  if (!open) return null

  function handleClose() {
    if (saving) return
    setForm(emptyForm)
    setError('')
    onClose()
  }

  async function handleSubmit(e) {
    e.preventDefault()
    setSaving(true)
    setError('')
    try {
      const payload = {
        name: form.name.trim(),
        address: form.address.trim() || null,
        phone: form.phone.trim() || null,
        email: form.email.trim() || null,
      }
      const created = await createCustomer(payload)
      onCreated(created)
      setForm(emptyForm)
      onClose()
    } catch (err) {
      setError(err.message || 'Failed to create customer')
    } finally {
      setSaving(false)
    }
  }

  return (
    <div className="modal-overlay" onClick={handleClose}>
      <div
        className="modal-card"
        onClick={(e) => e.stopPropagation()}
        role="dialog"
        aria-modal="true"
        aria-labelledby="customer-modal-title"
      >
        <div className="modal-header">
          <h3 id="customer-modal-title">Add Customer</h3>
          <button type="button" className="ghost-btn" onClick={handleClose}>
            Close
          </button>
        </div>

        {error ? <div className="error-banner">{error}</div> : null}

        <form onSubmit={handleSubmit}>
          <div className="field">
            <label htmlFor="customer-name">Name *</label>
            <input
              id="customer-name"
              value={form.name}
              onChange={(e) => setForm({ ...form, name: e.target.value })}
              required
              maxLength={200}
            />
          </div>

          <div className="field">
            <label htmlFor="customer-address">Address</label>
            <textarea
              id="customer-address"
              rows="3"
              value={form.address}
              onChange={(e) => setForm({ ...form, address: e.target.value })}
              maxLength={500}
            />
          </div>

          <div className="field">
            <label htmlFor="customer-phone">Phone</label>
            <input
              id="customer-phone"
              value={form.phone}
              onChange={(e) => setForm({ ...form, phone: e.target.value })}
              maxLength={30}
            />
          </div>

          <div className="field">
            <label htmlFor="customer-email">Email</label>
            <input
              id="customer-email"
              type="email"
              value={form.email}
              onChange={(e) => setForm({ ...form, email: e.target.value })}
              maxLength={100}
            />
          </div>

          <div className="modal-actions">
            <button type="button" className="ghost-btn" onClick={handleClose}>
              Cancel
            </button>
            <button type="submit" className="primary-btn small-btn" disabled={saving}>
              {saving ? 'Saving...' : 'Add Customer'}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}