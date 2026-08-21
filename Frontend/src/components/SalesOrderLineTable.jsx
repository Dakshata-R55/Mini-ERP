function formatMoney(value) {
    return `₹ ${Number(value || 0).toFixed(2)}`
  }
  
  export default function SalesOrderLineTable({
    lines,
    products,
    readonly,
    deliverMode,
    onChange,
    onAddLine,
    onRemoveLine,
  }) {
    function updateLine(index, patch) {
      const next = lines.map((line, i) => (i === index ? { ...line, ...patch } : line))
      onChange(next)
    }
  
    function handleProductChange(index, productId) {
      const product = products.find((p) => String(p.id) === String(productId))
      updateLine(index, {
        productId,
        unitPrice: product ? String(product.salesPrice) : '',
        productName: product?.name || '',
        freeToUseQty: product?.freeToUseQty ?? 0,
      })
    }
  
    const orderTotal = lines.reduce((sum, line) => {
      const qty = deliverMode
        ? Number(line.deliveredQty || 0)
        : Number(line.orderedQty || 0)
      return sum + qty * Number(line.unitPrice || 0)
    }, 0)
  
    return (
      <div className="lines-card">
        <table className="data-table lines-table">
          <thead>
            <tr>
              <th>Products</th>
              <th>Availability</th>
              <th>Ordered Qty</th>
              <th>Delivered Qty</th>
              <th>Units</th>
              <th>Sales Unit Price</th>
              <th>Total</th>
              {!readonly ? <th /> : null}
            </tr>
          </thead>
          <tbody>
            {lines.map((line, index) => {
              const qty = deliverMode
                ? Number(line.deliveredQty || 0)
                : Number(line.orderedQty || 0)
              const lineTotal = qty * Number(line.unitPrice || 0)
              const shortage =
                Number(line.orderedQty || 0) > Number(line.freeToUseQty || 0)
  
              return (
                <tr key={line.id || `new-${index}`}>
                  <td>
                    {readonly ? (
                      line.productName
                    ) : (
                      <select
                        value={line.productId || ''}
                        onChange={(e) => handleProductChange(index, e.target.value)}
                        required
                      >
                        <option value="">Select product</option>
                        {products.map((p) => (
                          <option key={p.id} value={p.id}>
                            {p.reference} — {p.name}
                          </option>
                        ))}
                      </select>
                    )}
                  </td>
                  <td className={shortage ? 'shortage' : ''}>
                    {line.freeToUseQty != null ? Number(line.freeToUseQty).toFixed(2) : '-'}
                    {shortage ? ' ⚠' : ''}
                  </td>
                  <td>
                    <input
                      type="number"
                      min="0.01"
                      step="0.01"
                      value={line.orderedQty}
                      disabled={readonly}
                      onChange={(e) => updateLine(index, { orderedQty: e.target.value })}
                      required
                    />
                  </td>
                  <td>
                    <input
                      type="number"
                      min="0"
                      step="0.01"
                      value={line.deliveredQty ?? '0'}
                      disabled={!deliverMode}
                      onChange={(e) => updateLine(index, { deliveredQty: e.target.value })}
                    />
                  </td>
                  <td>
                    <input
                      value={line.units || 'Units'}
                      disabled={readonly}
                      onChange={(e) => updateLine(index, { units: e.target.value })}
                    />
                  </td>
                  <td>{formatMoney(line.unitPrice)}</td>
                  <td>{formatMoney(lineTotal)}</td>
                  {!readonly ? (
                    <td>
                      <button type="button" className="ghost-btn" onClick={() => onRemoveLine(index)}>
                        Remove
                      </button>
                    </td>
                  ) : null}
                </tr>
              )
            })}
          </tbody>
        </table>
  
        {!readonly ? (
          <button type="button" className="link-btn add-line-btn" onClick={onAddLine}>
            Add a product
          </button>
        ) : null}
  
        <div className="order-total-row">
          <strong>Total:</strong> {formatMoney(orderTotal)}
        </div>
      </div>
    )
  }