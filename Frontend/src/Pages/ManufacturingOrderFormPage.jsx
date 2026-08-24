import { useEffect, useState } from 'react'
import AppShell from '../components/AppShell'
import { listProducts } from '../api/products'
import {
  createManufacturingOrder,
  getManufacturingOrder,
  updateManufacturingOrder,
  confirmManufacturingOrder,
  startMoWorkOrder,
  completeMoWorkOrder,
  produceManufacturingOrder,
  applyMoProductionCost,
  cancelManufacturingOrder,
} from '../api/manufacturingOrders'

const STATUS_LABELS = {
  DRAFT: 'Draft',
  CONFIRMED: 'Confirmed',
  IN_PROGRESS: 'In Progress',
  TO_CLOSE: 'To Close',
  DONE: 'Done',
  CANCELLED: 'Cancelled',
}

const WO_STATUS_LABELS = {
  PENDING: 'Pending',
  IN_PROGRESS: 'In Progress',
  DONE: 'Done',
}

export default function ManufacturingOrderFormPage({
  session,
  onSignOut,
  onNavigate,
  onOpenProfile,
  orderId,
  onBack,
}) {
  const isEdit = Boolean(orderId)
  const [finishedProducts, setFinishedProducts] = useState([])
  const [order, setOrder] = useState(null)
  const [form, setForm] = useState({
    finishedProductId: '',
    qtyToProduce: '1',
  })
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')

  const status = order?.status || 'DRAFT'
  const isDraft = status === 'DRAFT'

  useEffect(() => {
    loadData()
  }, [orderId])

  async function loadData() {
    setLoading(true)
    setError('')
    try {
      const products = await listProducts({ type: 'FINISHED_GOOD' })
      setFinishedProducts(products)

      if (isEdit) {
        const data = await getManufacturingOrder(orderId)
        setOrder(data)
        setForm({
          finishedProductId: String(data.finishedProductId),
          qtyToProduce: String(data.qtyToProduce),
        })
      }
    } catch (err) {
      setError(err.message || 'Failed to load manufacturing order')
    } finally {
      setLoading(false)
    }
  }

  function buildPayload() {
    return {
      finishedProductId: Number(form.finishedProductId),
      qtyToProduce: Number(form.qtyToProduce),
      bomId: order?.bomId || null,
      assigneeId: order?.assigneeId || null,
      salesOrderId: order?.salesOrderId || null,
    }
  }

  async function handleSave() {
    setSaving(true)
    setError('')
    try {
      if (isEdit) {
        await updateManufacturingOrder(orderId, buildPayload())
        await loadData()
      } else {
        const created = await createManufacturingOrder(buildPayload())
        onBack(created.id)
      }
    } catch (err) {
      setError(err.message || 'Failed to save order')
    } finally {
      setSaving(false)
    }
  }

  async function runAction(action, successMessage) {
    setSaving(true)
    setError('')
    setSuccess('')
    try {
      await action()
      setSuccess(successMessage)
      await loadData()
    } catch (err) {
      setError(err.message || 'Action failed')
    } finally {
      setSaving(false)
    }
  }

  async function handleStartWorkOrder(workOrderId) {
    await runAction(() => startMoWorkOrder(orderId, workOrderId), 'Work order started')
  }

  async function handleCompleteWorkOrder(workOrderId, expectedMinutes) {
    const input = window.prompt('Actual duration in minutes?', String(expectedMinutes || 0))
    if (input === null) return
    await runAction(
      () => completeMoWorkOrder(orderId, workOrderId, Number(input) || 0),
      'Work order completed',
    )
  }

  if (loading) {
    return (
      <AppShell session={session} onSignOut={onSignOut} onNavigate={onNavigate} onOpenProfile={onOpenProfile} currentModule="manufacturing-orders" pageTitle="MO">
        <p className="muted center-pad">Loading...</p>
      </AppShell>
    )
  }

  return (
    <AppShell
      session={session}
      onSignOut={onSignOut}
      onNavigate={onNavigate}
      onOpenProfile={onOpenProfile}
      currentModule="manufacturing-orders"
      pageTitle={order?.reference || 'New MO'}
    >
      <div className="page-toolbar">
        <button type="button" className="ghost-btn" onClick={() => onBack()}>← Back</button>
        <h2>{order?.reference || 'New Manufacturing Order'}</h2>
        {order ? (
          <span className={`status-pill status-${order.status}`}>
            {STATUS_LABELS[order.status] || order.status}
          </span>
        ) : null}
        <div className="toolbar-right">
          {isDraft ? (
            <button type="button" className="primary-btn small-btn" onClick={handleSave} disabled={saving}>
              {saving ? 'Saving...' : 'Save'}
            </button>
          ) : null}
          {status === 'DRAFT' && isEdit ? (
            <button
              type="button"
              className="primary-btn small-btn"
              disabled={saving}
              onClick={() => runAction(() => confirmManufacturingOrder(orderId), 'Order confirmed')}
            >
              Confirm
            </button>
          ) : null}
          {status === 'TO_CLOSE' || (status === 'CONFIRMED' && order?.workOrders?.length === 0) ? (
            <button
              type="button"
              className="primary-btn small-btn"
              disabled={saving}
              onClick={() => runAction(() => produceManufacturingOrder(orderId), 'Production recorded')}
            >
              Produce
            </button>
          ) : null}
          {status === 'DONE' ? (
            <button
              type="button"
              className="ghost-btn"
              disabled={saving}
              onClick={() => runAction(() => applyMoProductionCost(orderId), 'Cost applied to product')}
            >
              Apply Cost to Product
            </button>
          ) : null}
          {status === 'DRAFT' || status === 'CONFIRMED' ? (
            <button
              type="button"
              className="ghost-btn"
              disabled={saving}
              onClick={() => runAction(() => cancelManufacturingOrder(orderId), 'Order cancelled')}
            >
              Cancel
            </button>
          ) : null}
        </div>
      </div>

      {error ? <div className="error-banner">{error}</div> : null}
      {success ? <div className="success-banner">{success}</div> : null}

      <div className="form-card">
        <div className="form-row">
          <label>
            Finished Product
            <select
              value={form.finishedProductId}
              disabled={!isDraft}
              onChange={(e) => setForm({ ...form, finishedProductId: e.target.value })}
            >
              <option value="">Select product</option>
              {finishedProducts.map((p) => (
                <option key={p.id} value={p.id}>{p.name}</option>
              ))}
            </select>
          </label>
          <label>
            Qty to Produce
            <input
              type="number"
              min="0.01"
              step="0.01"
              disabled={!isDraft}
              value={form.qtyToProduce}
              onChange={(e) => setForm({ ...form, qtyToProduce: e.target.value })}
            />
          </label>
        </div>

        {order?.bomReference ? (
          <p className="muted">BOM: {order.bomReference}</p>
        ) : null}

        {order?.totalProductionCost != null ? (
          <p>Total production cost: ₹ {Number(order.totalProductionCost).toFixed(2)}</p>
        ) : null}

        {order?.components?.length ? (
          <>
            <h3>Components to Consume</h3>
            <table className="data-table inline-table">
              <thead>
                <tr>
                  <th>Raw Material</th>
                  <th>To Consume</th>
                  <th>Consumed</th>
                </tr>
              </thead>
              <tbody>
                {order.components.map((c) => (
                  <tr key={c.id}>
                    <td>{c.productName}</td>
                    <td>{Number(c.toConsumeQty).toFixed(2)}</td>
                    <td>{Number(c.consumedQty).toFixed(2)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </>
        ) : null}

        {order?.workOrders?.length ? (
          <>
            <h3>Work Orders</h3>
            <table className="data-table inline-table">
              <thead>
                <tr>
                  <th>Seq</th>
                  <th>Work Center</th>
                  <th>Location</th>
                  <th>Expected (min)</th>
                  <th>Actual (min)</th>
                  <th>Status</th>
                  <th />
                </tr>
              </thead>
              <tbody>
                {order.workOrders.map((wo) => (
                  <tr key={wo.id}>
                    <td>{wo.sequence}</td>
                    <td>{wo.workCenterName}</td>
                    <td>{wo.location || '-'}</td>
                    <td>{wo.expectedDurationMinutes}</td>
                    <td>{wo.realDurationMinutes}</td>
                    <td>{WO_STATUS_LABELS[wo.status] || wo.status}</td>
                    <td className="row-actions">
                      {wo.status === 'PENDING' && status !== 'DRAFT' && status !== 'CANCELLED' && status !== 'DONE' ? (
                        <button type="button" className="ghost-btn small-btn" onClick={() => handleStartWorkOrder(wo.id)}>
                          Start
                        </button>
                      ) : null}
                      {wo.status === 'IN_PROGRESS' ? (
                        <button
                          type="button"
                          className="ghost-btn small-btn"
                          onClick={() => handleCompleteWorkOrder(wo.id, wo.expectedDurationMinutes)}
                        >
                          Complete
                        </button>
                      ) : null}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </>
        ) : null}
      </div>
    </AppShell>
  )
}
