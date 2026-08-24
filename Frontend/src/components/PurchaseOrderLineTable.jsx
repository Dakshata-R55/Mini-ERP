function formatMoney(value) {
  return `₹ ${Number(value || 0).toFixed(2)}`
}

export default function PurchaseOrderLineTable({
  lines,
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

  const orderTotal = lines.reduce((sum, line) => {
    const qty = receiveMode
      ? Number(line.receivedQty || 0)
      : Number(line.orderedQty || 0)
    return sum + qty * Number(line.unitCostPrice || 0)
  }, 0)

  return (
    <div className="lines-card">
      <p className="muted toolbar-note">
        {receiveMode
          ? 'Enter received qty for each line, or click Receive to take the full ordered quantity.'
          : 'Type raw material names. New items are added to the product catalog automatically.'}
      </p>

      <table className="data-table lines-table">
        <thead>
          <tr>
            <th>Raw Material</th>
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
                    <input
                      type="text"
                      placeholder="e.g. Screw, Plywood"
                      value={line.productName || ''}
                      onChange={(e) =>
                        updateLine(index, {
                          productName: e.target.value,
                          productId: '',
                        })
                      }
                      required
                    />
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
                    max={receiveMode ? line.orderedQty : undefined}
                    value={line.receivedQty ?? '0'}
                    disabled={!receiveMode}
                    placeholder={receiveMode ? `Max ${line.orderedQty}` : undefined}
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
                <td>
                  {readonly ? (
                    formatMoney(line.unitCostPrice)
                  ) : (
                    <input
                      type="number"
                      min="0"
                      step="0.01"
                      value={line.unitCostPrice}
                      onChange={(e) => updateLine(index, { unitCostPrice: e.target.value })}
                      required
                    />
                  )}
                </td>
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
          Add a raw material
        </button>
      ) : null}

      <div className="order-total-row">
        <strong>Total:</strong> {formatMoney(orderTotal)}
      </div>
    </div>
  )
}
