import { useEffect, useState } from 'react'
import AppShell from '../components/AppShell'
import {
  listWorkCenters,
  createWorkCenter,
  updateWorkCenter,
  deleteWorkCenter,
} from '../api/workCenters'

const emptyForm = () => ({ name: '', location: '' })

export default function WorkCentersPage({ session, onSignOut, onNavigate, onOpenProfile }) {
  const [workCenters, setWorkCenters] = useState([])
  const [form, setForm] = useState(emptyForm())
  const [editingId, setEditingId] = useState(null)
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => {
    loadWorkCenters()
  }, [])

  async function loadWorkCenters() {
    setLoading(true)
    setError('')
    try {
      setWorkCenters(await listWorkCenters())
    } catch (err) {
      setError(err.message || 'Failed to load work centers')
    } finally {
      setLoading(false)
    }
  }

  function resetForm() {
    setForm(emptyForm())
    setEditingId(null)
  }

  async function handleSubmit(e) {
    e.preventDefault()
    setSaving(true)
    setError('')
    try {
      if (editingId) {
        await updateWorkCenter(editingId, form)
      } else {
        await createWorkCenter(form)
      }
      resetForm()
      await loadWorkCenters()
    } catch (err) {
      setError(err.message || 'Failed to save work center')
    } finally {
      setSaving(false)
    }
  }

  function startEdit(item) {
    setEditingId(item.id)
    setForm({ name: item.name, location: item.location || '' })
  }

  async function handleDelete(id) {
    if (!window.confirm('Delete this work center?')) return
    setError('')
    try {
      await deleteWorkCenter(id)
      if (editingId === id) resetForm()
      await loadWorkCenters()
    } catch (err) {
      setError(err.message || 'Failed to delete work center')
    }
  }

  return (
    <AppShell
      session={session}
      onSignOut={onSignOut}
      onNavigate={onNavigate}
      onOpenProfile={onOpenProfile}
      currentModule="work-centers"
      pageTitle="Work Centers"
    >
      <div className="page-toolbar">
        <h2>Work Centers</h2>
      </div>

      {error ? <div className="error-banner">{error}</div> : null}

      <form className="form-card compact-form" onSubmit={handleSubmit}>
        <h3>{editingId ? 'Edit Work Center' : 'Add Work Center'}</h3>
        <div className="form-row">
          <label>
            Name
            <input
              value={form.name}
              onChange={(e) => setForm({ ...form, name: e.target.value })}
              required
            />
          </label>
          <label>
            Location
            <input
              value={form.location}
              onChange={(e) => setForm({ ...form, location: e.target.value })}
            />
          </label>
        </div>
        <div className="form-actions">
          <button type="submit" className="primary-btn small-btn" disabled={saving}>
            {saving ? 'Saving...' : editingId ? 'Update' : 'Add'}
          </button>
          {editingId ? (
            <button type="button" className="ghost-btn" onClick={resetForm}>Cancel</button>
          ) : null}
        </div>
      </form>

      <div className="table-card">
        {loading ? (
          <p className="muted center-pad">Loading work centers...</p>
        ) : (
          <table className="data-table">
            <thead>
              <tr>
                <th>Name</th>
                <th>Location</th>
                <th />
              </tr>
            </thead>
            <tbody>
              {workCenters.length === 0 ? (
                <tr>
                  <td colSpan="3" className="center-pad muted">No work centers yet.</td>
                </tr>
              ) : (
                workCenters.map((wc) => (
                  <tr key={wc.id}>
                    <td>{wc.name}</td>
                    <td>{wc.location || '-'}</td>
                    <td className="row-actions">
                      <button type="button" className="ghost-btn small-btn" onClick={() => startEdit(wc)}>
                        Edit
                      </button>
                      <button type="button" className="ghost-btn small-btn" onClick={() => handleDelete(wc.id)}>
                        Delete
                      </button>
                    </td>
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
