import { useEffect, useState } from 'react'
import AppShell from '../components/AppShell'
import { listBoms, getBom } from '../api/boms'
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

function scaledQty(qtyToProduce, outputQty, qtyPerOutput) {
  const output = Number(outputQty) || 1
  const scale = Number(qtyToProduce) / output
  return (Number(qtyPerOutput) * scale).toFixed(2)
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
  const [boms, setBoms] = useState([])
  const [bomPreview, setBomPreview] = useState(null)
  const [order, setOrder] = useState(null)
  const [form, setForm] = useState({
    bomId: '',
    finishedProductId: '',
    finishedProductName: '',
    qtyToProduce: '1',
  })
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')
  const [durationInputs, setDurationInputs] = useState({})

  const status = order?.status || 'DRAFT'
  const isDraft = status === 'DRAFT'
  const showBomPreview = isDraft && bomPreview

  useEffect(() => {
    loadData()
  }, [orderId])

  async function loadBomPreview(bomId) {
    if (!bomId) {
      setBomPreview(null)
      return
    }
    try {
      const detail = await getBom(bomId)
      setBomPreview(detail)
    } catch {
      setBomPreview(null)
    }
  }

  async function loadData() {
    setLoading(true)
    setError('')
    try {
      const bomList = await listBoms()
      setBoms(bomList)

      if (isEdit) {
        const data = await getManufacturingOrder(orderId)
        setOrder(data)
        setForm({
          bomId: data.bomId ? String(data.bomId) : '',
          finishedProductId: String(data.finishedProductId),
          finishedProductName: data.finishedProductName || '',
          qtyToProduce: String(data.qtyToProduce),
        })
        if (data.bomId) {
          await loadBomPreview(data.bomId)
        }

        const drafts = {}
        for (const wo of data.workOrders || []) {
          if (wo.status === 'IN_PROGRESS') {
            drafts[wo.id] = String(
              wo.realDurationMinutes > 0 ? wo.realDurationMinutes : wo.expectedDurationMinutes ?? 0,
            )
          }
        }
        setDurationInputs(drafts)
      }
    } catch (err) {
      setError(err.message || 'Failed to load manufacturing order')
    } finally {
      setLoading(false)
    }
  }

  async function handleBomChange(bomId) {
    const selected = boms.find((b) => String(b.id) === String(bomId))
    if (!selected) {
      setForm({
        bomId: '',
        finishedProductId: '',
        finishedProductName: '',
        qtyToProduce: form.qtyToProduce,
      })
      setBomPreview(null)
      return
    }

    setForm({
      bomId: String(selected.id),
      finishedProductId: String(selected.finishedProductId),
      finishedProductName: selected.finishedProductName,
      qtyToProduce: form.qtyToProduce,
    })
    await loadBomPreview(selected.id)
  }

  function buildPayload() {
    return {
      bomId: form.bomId ? Number(form.bomId) : null,
      finishedProductId: Number(form.finishedProductId),
      qtyToProduce: Number(form.qtyToProduce),
      assigneeId: order?.assigneeId || null,
      salesOrderId: order?.salesOrderId || null,
    }
  }

  async function handleSave() {
    if (!form.bomId) {
      setError('Select a BOM first')
      return
    }

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

  async function handleCompleteWorkOrder(workOrderId) {
    const wo = order?.workOrders?.find((w) => w.id === workOrderId)
    const minutes = durationInputs[workOrderId] ?? String(wo?.expectedDurationMinutes ?? 0)
    await runAction(
      () => completeMoWorkOrder(orderId, workOrderId, Number(minutes) || 0),
      'Work order completed',
    )
  }

  function setDurationInput(workOrderId, value) {
    setDurationInputs((prev) => ({ ...prev, [workOrderId]: value }))
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
        {isDraft ? (
          <>
            <p className="muted toolbar-note">
              Select a BOM first. The finished product and material preview come from that recipe.
            </p>

            <div className="form-row">
              <label>
                Bill of Materials
                <select
                  value={form.bomId}
                  onChange={(e) => handleBomChange(e.target.value)}
                  required
                >
                  <option value="">Select BOM</option>
                  {boms.map((bom) => (
                    <option key={bom.id} value={bom.id}>
                      {bom.reference} — {bom.finishedProductName}
                    </option>
                  ))}
                </select>
              </label>

              <label>
                Product to manufacture
                <input
                  value={form.finishedProductName || '—'}
                  readOnly
                  className="readonly-input"
                />
              </label>

              <label>
                Qty to Produce
                <input
                  type="number"
                  min="0.01"
                  step="0.01"
                  value={form.qtyToProduce}
                  onChange={(e) => setForm({ ...form, qtyToProduce: e.target.value })}
                />
              </label>
            </div>
          </>
        ) : (
          <div className="form-row">
            <label>
              Bill of Materials
              <input
                value={order?.bomReference || '—'}
                readOnly
                className="readonly-input"
              />
            </label>
            <label>
              Product to manufacture
              <input
                value={order?.finishedProductName || form.finishedProductName || '—'}
                readOnly
                className="readonly-input"
              />
            </label>
            <label>
              Qty to Produce
              <input
                value={order?.qtyToProduce ?? form.qtyToProduce}
                readOnly
                className="readonly-input"
              />
            </label>
          </div>
        )}

        {showBomPreview ? (
          <>
            <h3>BOM Preview — {bomPreview.reference}</h3>
            <p className="muted">
              Batch output: {Number(bomPreview.outputQty).toFixed(2)} units
            </p>

            {bomPreview.components?.length ? (
              <>
                <h4>Raw Materials</h4>
                <table className="data-table inline-table">
                  <thead>
                    <tr>
                      <th>Material</th>
                      <th>Per Batch</th>
                      <th>To Consume</th>
                    </tr>
                  </thead>
                  <tbody>
                    {bomPreview.components.map((c) => (
                      <tr key={c.id || c.productId}>
                        <td>{c.productName}</td>
                        <td>{Number(c.qtyPerOutput).toFixed(2)}</td>
                        <td>
                          {scaledQty(form.qtyToProduce, bomPreview.outputQty, c.qtyPerOutput)}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </>
            ) : (
              <p className="muted">No raw materials defined on this BOM.</p>
            )}

            {bomPreview.operations?.length ? (
              <>
                <h4>Operations</h4>
                <table className="data-table inline-table">
                  <thead>
                    <tr>
                      <th>Seq</th>
                      <th>Work Center</th>
                      <th>Location</th>
                      <th>Duration (min)</th>
                    </tr>
                  </thead>
                  <tbody>
                    {bomPreview.operations.map((op) => (
                      <tr key={op.id || `${op.workCenterId}-${op.sequence}`}>
                        <td>{op.sequence}</td>
                        <td>{op.workCenterName}</td>
                        <td>{op.location || '—'}</td>
                        <td>{op.expectedDurationMinutes}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </>
            ) : null}
          </>
        ) : null}

        {order?.totalProductionCost != null ? (
          <p>Total production cost: ₹ {Number(order.totalProductionCost).toFixed(2)}</p>
        ) : null}

        {!isDraft && order?.components?.length ? (
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

        {!isDraft && order?.workOrders?.length ? (
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
                    <td>
                      {wo.status === 'IN_PROGRESS' ? (
                        <input
                          type="number"
                          min="0"
                          step="1"
                          className="inline-number-input"
                          value={durationInputs[wo.id] ?? String(wo.expectedDurationMinutes ?? 0)}
                          onChange={(e) => setDurationInput(wo.id, e.target.value)}
                        />
                      ) : (
                        wo.realDurationMinutes
                      )}
                    </td>
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
                          onClick={() => handleCompleteWorkOrder(wo.id)}
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
