import { useEffect, useState } from 'react'
import AppShell from '../components/AppShell'
import { listProducts } from '../api/products'

function formatMoney(value) {
  return `₹ ${Number(value || 0).toFixed(2)}`
}

function formatQty(value) {
  return Number(value || 0).toFixed(2)
}

<<<<<<< HEAD
export default function ProductsPage({
  session,
  onSignOut,
  onNavigate,
  onOpenProfile,
  onCreate,
  onOpenProduct,
}) {
=======
export default function ProductsPage({ session, onSignOut, onNavigate, onCreate, onOpenProduct, onOpenProfile }) {
>>>>>>> 1d1cc83 (Fix Bug in ui)
  const [products, setProducts] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    loadProducts()
  }, [])

  async function loadProducts() {
    setLoading(true)
    setError('')
    try {
      const data = await listProducts({ type: 'FINISHED_GOOD' })
      setProducts(data)
    } catch (err) {
      setError(err.message || 'Failed to load products')
    } finally {
      setLoading(false)
    }
  }

  return (
    <AppShell session={session} onSignOut={onSignOut}
    onNavigate={onNavigate}
      onOpenProfile={onOpenProfile}
      currentModule="products"
      pageTitle="Products"
    >
      <div className="page-toolbar">
        <button type="button" className="primary-btn small-btn" onClick={onCreate}>
          + New
        </button>
        <h2>Products</h2>
      </div>

      {error ? <div className="error-banner">{error}</div> : null}

      <div className="table-card">
        {loading ? (
          <p className="muted center-pad">Loading products...</p>
        ) : (
          <table className="data-table">
            <thead>
              <tr>
                <th />
                <th>Type</th>
                <th>Reference</th>
                <th>Product</th>
                <th>Sales Price</th>
                <th>Cost Price</th>
                <th>On Hand Qty</th>
              </tr>
            </thead>
            <tbody>
              {products.length === 0 ? (
                <tr>
                  <td colSpan="7" className="center-pad muted">
                    No products yet. Click + New to create one.
                  </td>
                </tr>
              ) : (
                products.map((product) => (
                  <tr key={product.id} onClick={() => onOpenProduct(product.id)}>
                    <td>
                      <input type="checkbox" onClick={(e) => e.stopPropagation()} />
                    </td>
                    <td>{product.productType === 'RAW_MATERIAL' ? 'Raw Material' : 'Finished Good'}</td>
                    <td>{product.reference}</td>
                    <td>{product.name}</td>
                    <td>{formatMoney(product.salesPrice)}</td>
                    <td>{formatMoney(product.costPrice)}</td>
                    <td>{formatQty(product.onHandQty)}</td>
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