function formatMoney(value) {
    return `₹ ${Number(value || 0).toFixed(2)}`
  }
  
  export default function PurchaseOrderLineTable({
    lines,
    products,
    readonly,
    receiveMode,
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
        unitCostPrice: product ? String(product.costPrice) : '',
        productName: product?.name || '',
      })
    }
  
    const orderTotal = lines.reduce((sum, line) => {
      const qty = receiveMode
        ? Number(line.receivedQty || 0)
        : Number(line.orderedQty || 0)
      return sum + qty * Number(line.unitCostPrice || 0)
    }, 0)
  
    return (
      <div className="lines-card">
        <table className="data-table lines-table">
          <thead>
            <tr>
              <th>Products</th>
              <th>Ordered Qty</th>
              <th>Received Qty</th>
              <th>Units</th>
              <th>Cost Unit Price</th>
              <th>Total</th>
              {!readonly ? <th /> : null}
            </tr>
          </thead>
          <tbody>
            {lines.map((line, index) => {
              const qty = receiveMode
                ? Number(line.receivedQty || 0)
                : Number(line.orderedQty || 0)
              const lineTotal = qty * Number(line.unitCostPrice || 0)
  
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
                      value={line.receivedQty ?? '0'}
                      disabled={!receiveMode}
                      onChange={(e) => updateLine(index, { receivedQty: e.target.value })}
                    />
                  </td>
                  <td>
                    <input
                      value={line.units || 'Units'}
                      disabled={readonly}
                      onChange={(e) => updateLine(index, { units: e.target.value })}
                    />
                  </td>
                  <td>{formatMoney(line.unitCostPrice)}</td>
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