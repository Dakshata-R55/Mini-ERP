import { useEffect, useState } from 'react'
import AppShell from '../components/AppShell'
import { listProducts } from '../api/products'
import { listWorkCenters } from '../api/workCenters'
import { createBom, getBom, updateBom } from '../api/boms'

const emptyComponent = () => ({ productId: '', qtyPerOutput: '1' })
const emptyOperation = () => ({ workCenterId: '', sequence: '1', expectedDurationMinutes: '0' })

export default function BomFormPage({
  session,
  onSignOut,
  onNavigate,
  bomId,
  onBack,
}) {
  const isEdit = Boolean(bomId)
  const [finishedProducts, setFinishedProducts] = useState([])
  const [rawMaterials, setRawMaterials] = useState([])
  const [workCenters, setWorkCenters] = useState([])
  const [bom, setBom] = useState(null)
  const [form, setForm] = useState({
    finishedProductId: '',
    outputQty: '1',
    components: [emptyComponent()],
    operations: [],
  })
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => {
    loadData()
  }, [bomId])

  async function loadData() {
    setLoading(true)
    setError('')
    try {
      const [finished, raw, centers] = await Promise.all([
        listProducts({ type: 'FINISHED_GOOD' }),
        listProducts({ type: 'RAW_MATERIAL' }),
        listWorkCenters(),
      ])
      setFinishedProducts(finished)
      setRawMaterials(raw)
      setWorkCenters(centers)

      if (isEdit) {
        const data = await getBom(bomId)
        setBom(data)
        setForm({
          finishedProductId: String(data.finishedProductId),
          outputQty: String(data.outputQty),
          components: data.components.map((c) => ({
            productId: String(c.productId),
            qtyPerOutput: String(c.qtyPerOutput),
          })),
          operations: data.operations.map((o) => ({
            workCenterId: String(o.workCenterId),
            sequence: String(o.sequence),
            expectedDurationMinutes: String(o.expectedDurationMinutes),
          })),
        })
      }
    } catch (err) {
      setError(err.message || 'Failed to load BOM')
    } finally {
      setLoading(false)
    }
  }

  function buildPayload() {
    return {
      finishedProductId: Number(form.finishedProductId),
      outputQty: Number(form.outputQty),
      components: form.components
        .filter((c) => c.productId)
        .map((c) => ({
          productId: Number(c.productId),
          qtyPerOutput: Number(c.qtyPerOutput),
        })),
      operations: form.operations
        .filter((o) => o.workCenterId)
        .map((o) => ({
          workCenterId: Number(o.workCenterId),
          sequence: Number(o.sequence) || 1,
          expectedDurationMinutes: Number(o.expectedDurationMinutes) || 0,
        })),
    }
  }

  async function handleSave() {
    setSaving(true)
    setError('')
    try {
      const payload = buildPayload()
      if (isEdit) {
        await updateBom(bomId, payload)
        await loadData()
      } else {
        const created = await createBom(payload)
        onBack(created.id)
      }
    } catch (err) {
      setError(err.message || 'Failed to save BOM')
    } finally {
      setSaving(false)
    }
  }

  function updateComponent(index, field, value) {
    const components = [...form.components]
    components[index] = { ...components[index], [field]: value }
    setForm({ ...form, components })
  }

  function addComponent() {
    setForm({ ...form, components: [...form.components, emptyComponent()] })
  }

  function removeComponent(index) {
    const components = form.components.filter((_, i) => i !== index)
    setForm({ ...form, components: components.length ? components : [emptyComponent()] })
  }

  function updateOperation(index, field, value) {
    const operations = [...form.operations]
    operations[index] = { ...operations[index], [field]: value }
    setForm({ ...form, operations })
  }

  function addOperation() {
    setForm({ ...form, operations: [...form.operations, emptyOperation()] })
  }

  function removeOperation(index) {
    setForm({ ...form, operations: form.operations.filter((_, i) => i !== index) })
  }

  if (loading) {
    return (
      <AppShell session={session} onSignOut={onSignOut} onNavigate={onNavigate} currentModule="boms" pageTitle="BOM">
        <p className="muted center-pad">Loading...</p>
      </AppShell>
    )
  }

  return (
    <AppShell
      session={session}
      onSignOut={onSignOut}
      onNavigate={onNavigate}
      currentModule="boms"
      pageTitle={bom?.reference || 'New BOM'}
    >
      <div className="page-toolbar">
        <button type="button" className="ghost-btn" onClick={() => onBack()}>← Back</button>
        <h2>{bom?.reference || 'New Bill of Materials'}</h2>
        <div className="toolbar-right">
          <button type="button" className="primary-btn small-btn" onClick={handleSave} disabled={saving}>
            {saving ? 'Saving...' : 'Save'}
          </button>
        </div>
      </div>

      {error ? <div className="error-banner">{error}</div> : null}

      <div className="form-card">
        <div className="form-row">
          <label>
            Finished Product
            <select
              value={form.finishedProductId}
              onChange={(e) => setForm({ ...form, finishedProductId: e.target.value })}
              required
            >
              <option value="">Select product</option>
              {finishedProducts.map((p) => (
                <option key={p.id} value={p.id}>{p.name}</option>
              ))}
            </select>
          </label>
          <label>
            Output Qty (per batch)
            <input
              type="number"
              min="0.01"
              step="0.01"
              value={form.outputQty}
              onChange={(e) => setForm({ ...form, outputQty: e.target.value })}
            />
          </label>
        </div>

        <h3>Raw Material Components</h3>
        <table className="data-table inline-table">
          <thead>
            <tr>
              <th>Raw Material</th>
              <th>Qty per Output</th>
              <th />
            </tr>
          </thead>
          <tbody>
            {form.components.map((line, index) => (
              <tr key={index}>
                <td>
                  <select
                    value={line.productId}
                    onChange={(e) => updateComponent(index, 'productId', e.target.value)}
                  >
                    <option value="">Select</option>
                    {rawMaterials.map((p) => (
                      <option key={p.id} value={p.id}>{p.name}</option>
                    ))}
                  </select>
                </td>
                <td>
                  <input
                    type="number"
                    min="0.01"
                    step="0.01"
                    value={line.qtyPerOutput}
                    onChange={(e) => updateComponent(index, 'qtyPerOutput', e.target.value)}
                  />
                </td>
                <td>
                  <button type="button" className="ghost-btn small-btn" onClick={() => removeComponent(index)}>×</button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
        <button type="button" className="ghost-btn small-btn" onClick={addComponent}>+ Add Component</button>

        <h3>Work Center Operations</h3>
        <table className="data-table inline-table">
          <thead>
            <tr>
              <th>Work Center</th>
              <th>Sequence</th>
              <th>Duration (min)</th>
              <th />
            </tr>
          </thead>
          <tbody>
            {form.operations.map((line, index) => (
              <tr key={index}>
                <td>
                  <select
                    value={line.workCenterId}
                    onChange={(e) => updateOperation(index, 'workCenterId', e.target.value)}
                  >
                    <option value="">Select</option>
                    {workCenters.map((wc) => (
                      <option key={wc.id} value={wc.id}>{wc.name}</option>
                    ))}
                  </select>
                </td>
                <td>
                  <input
                    type="number"
                    min="1"
                    value={line.sequence}
                    onChange={(e) => updateOperation(index, 'sequence', e.target.value)}
                  />
                </td>
                <td>
                  <input
                    type="number"
                    min="0"
                    value={line.expectedDurationMinutes}
                    onChange={(e) => updateOperation(index, 'expectedDurationMinutes', e.target.value)}
                  />
                </td>
                <td>
                  <button type="button" className="ghost-btn small-btn" onClick={() => removeOperation(index)}>×</button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
        <button type="button" className="ghost-btn small-btn" onClick={addOperation}>+ Add Operation</button>
      </div>
    </AppShell>
  )
}
