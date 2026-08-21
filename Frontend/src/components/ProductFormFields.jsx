export default function ProductFormFields({ form, onChange, readOnlyQty = false }) {
    function setField(name, value) {
      onChange({ ...form, [name]: value })
    }
  
    function handleProcureToggle(checked) {
      const next = {
        ...form,
        procureOnDemand: checked,
      }
  
      if (!checked) {
        next.procurementType = ''
        next.vendorName = ''
        next.bomName = ''
      }
  
      onChange(next)
    }
  
    function handleProcurementType(value) {
      onChange({
        ...form,
        procurementType: value,
        vendorName: value === 'PURCHASE' ? form.vendorName : '',
        bomName: value === 'MANUFACTURING' ? form.bomName : '',
      })
    }
  
    return (
      <div className="product-form-grid">
        <div className="product-form-left">
          <div className="field">
            <label htmlFor="name">Product</label>
            <input
              id="name"
              value={form.name}
              onChange={(e) => setField('name', e.target.value)}
              required
            />
          </div>
  
          <div className="field">
            <label htmlFor="salesPrice">Sales Price</label>
            <div className="money-input">
              <span>₹</span>
              <input
                id="salesPrice"
                type="number"
                min="0"
                step="0.01"
                value={form.salesPrice}
                onChange={(e) => setField('salesPrice', e.target.value)}
                required
              />
            </div>
          </div>
  
          <div className="field">
            <label htmlFor="costPrice">Cost Price</label>
            <div className="money-input">
              <span>₹</span>
              <input
                id="costPrice"
                type="number"
                min="0"
                step="0.01"
                value={form.costPrice}
                onChange={(e) => setField('costPrice', e.target.value)}
                required
              />
            </div>
          </div>
  
          <div className="field">
            <label htmlFor="onHandQty">On Hand Qty</label>
            <input
              id="onHandQty"
              type="number"
              min="0"
              step="0.01"
              value={form.onHandQty}
              onChange={(e) => setField('onHandQty', e.target.value)}
              required
            />
          </div>
  
          <div className="field">
            <label htmlFor="freeToUseQty">Free to Use Qty</label>
            <input
              id="freeToUseQty"
              value={form.freeToUseQty ?? '0.00'}
              readOnly
              className="readonly-input"
            />
          </div>
        </div>
  
        <div className="product-form-right">
          <div className="image-placeholder">
            <span>Image</span>
          </div>
  
          <label className="checkbox-row">
            <input
              type="checkbox"
              checked={form.procureOnDemand}
              onChange={(e) => handleProcureToggle(e.target.checked)}
            />
            Procure on Demand
          </label>
  
          {form.procureOnDemand ? (
            <>
              <div className="field">
                <label htmlFor="procurementType">Procurement Type</label>
                <select
                  id="procurementType"
                  value={form.procurementType}
                  onChange={(e) => handleProcurementType(e.target.value)}
                  required
                >
                  <option value="">Select type</option>
                  <option value="PURCHASE">Purchase</option>
                  <option value="MANUFACTURING">Manufacturing</option>
                </select>
              </div>
  
              {form.procurementType === 'PURCHASE' ? (
                <div className="field">
                  <label htmlFor="vendorName">Vendor</label>
                  <input
                    id="vendorName"
                    value={form.vendorName}
                    onChange={(e) => setField('vendorName', e.target.value)}
                    required
                  />
                </div>
              ) : null}
  
              {form.procurementType === 'MANUFACTURING' ? (
                <div className="field">
                  <label htmlFor="bomName">BoM</label>
                  <input
                    id="bomName"
                    value={form.bomName}
                    onChange={(e) => setField('bomName', e.target.value)}
                    required
                  />
                </div>
              ) : null}
            </>
          ) : null}
        </div>
      </div>
    )
  }