import { useRef, useState } from 'react'
import { uploadProductImage } from '../api/products'

const MAX_BYTES = 2 * 1024 * 1024
const ALLOWED_TYPES = ['image/jpeg', 'image/png', 'image/webp', 'image/jpg']

function normalizeImageType(file) {
  if (file.type === 'image/jpg') return 'image/jpeg'
  if (file.type) return file.type
  const name = file.name?.toLowerCase() || ''
  if (name.endsWith('.jpg') || name.endsWith('.jpeg')) return 'image/jpeg'
  if (name.endsWith('.png')) return 'image/png'
  if (name.endsWith('.webp')) return 'image/webp'
  return ''
}

export default function ProductFormFields({
  form,
  onChange,
  readOnlyQty = false,
  fixedProductType = null,
}) {
  const fileInputRef = useRef(null)
  const [uploading, setUploading] = useState(false)
  const [uploadError, setUploadError] = useState('')

  const isFinishedGood =
    fixedProductType === 'FINISHED_GOOD' || form.productType !== 'RAW_MATERIAL'

  function setField(name, value) {
    onChange({ ...form, [name]: value })
  }

  function handleProcureToggle(checked) {
    const next = { ...form, procureOnDemand: checked }
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

  function openFilePicker() {
    if (uploading) return
    fileInputRef.current?.click()
  }

  async function handleImagePick(e) {
    const file = e.target.files?.[0]
    e.target.value = ''
    if (!file) return

    setUploadError('')
    const normalizedType = normalizeImageType(file)

    if (!ALLOWED_TYPES.includes(file.type) && !normalizedType) {
      setUploadError('Only JPG, PNG, or WEBP images are allowed')
      return
    }
    if (file.size > MAX_BYTES) {
      setUploadError('Image must be 2 MB or smaller')
      return
    }

    setUploading(true)
    try {
      const result = await uploadProductImage(file)
      onChange({ ...form, imageUrl: result.imageUrl })
    } catch (err) {
      setUploadError(err.message || 'Failed to upload image')
    } finally {
      setUploading(false)
    }
  }

  function handleRemoveImage() {
    onChange({ ...form, imageUrl: '' })
    setUploadError('')
  }

  return (
    <div className="product-form-grid">
      <input
        ref={fileInputRef}
        type="file"
        accept="image/jpeg,image/png,image/webp,.jpg,.jpeg"
        onChange={handleImagePick}
        disabled={uploading}
        className="hidden-file-input"
        aria-hidden="true"
        tabIndex={-1}
      />

      <div className="product-form-left">
        {fixedProductType ? (
          <div className="field">
            <label>Product Type</label>
            <input value="Finished Good" readOnly className="readonly-input" />
          </div>
        ) : (
          <div className="field">
            <label htmlFor="productType">Product Type</label>
            <select
              id="productType"
              value={form.productType || 'FINISHED_GOOD'}
              onChange={(e) => {
                const productType = e.target.value
                onChange({
                  ...form,
                  productType,
                  procureOnDemand: productType === 'RAW_MATERIAL' ? false : form.procureOnDemand,
                  procurementType: productType === 'RAW_MATERIAL' ? '' : form.procurementType,
                  vendorName: productType === 'RAW_MATERIAL' ? '' : form.vendorName,
                  bomName: productType === 'RAW_MATERIAL' ? '' : form.bomName,
                  salesPrice: productType === 'RAW_MATERIAL' ? form.salesPrice || '0' : form.salesPrice,
                })
              }}
              required
            >
              <option value="FINISHED_GOOD">Finished Good</option>
              <option value="RAW_MATERIAL">Raw Material</option>
            </select>
          </div>
        )}

        <div className="field">
          <label htmlFor="name">Product</label>
          <input
            id="name"
            value={form.name}
            onChange={(e) => setField('name', e.target.value)}
            required
          />
        </div>

        {isFinishedGood ? (
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
        ) : null}

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
            readOnly={readOnlyQty}
            className={readOnlyQty ? 'readonly-input' : undefined}
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
        <div className="product-image-block">
          <label className="product-image-label">Product Image</label>

          {form.imageUrl ? (
            <button
              type="button"
              className="product-image-preview-wrap clickable-image"
              onClick={openFilePicker}
              aria-label="Change product image"
            >
              <img
                src={form.imageUrl}
                alt={form.name || 'Product'}
                className="product-image-preview"
              />
            </button>
          ) : (
            <button
              type="button"
              className="image-placeholder clickable-image"
              onClick={openFilePicker}
              aria-label="Upload product image"
            >
              <span>{uploading ? 'Uploading...' : 'Click to upload image'}</span>
            </button>
          )}

          {uploadError ? <div className="error-banner">{uploadError}</div> : null}

          <div className="product-image-actions">
            <button
              type="button"
              className="primary-btn small-btn"
              onClick={openFilePicker}
              disabled={uploading}
            >
              {uploading ? 'Uploading...' : form.imageUrl ? 'Change Image' : 'Upload Image'}
            </button>

            {form.imageUrl ? (
              <button
                type="button"
                className="ghost-btn danger-btn"
                onClick={handleRemoveImage}
                disabled={uploading}
              >
                Remove
              </button>
            ) : null}
          </div>
        </div>

        {isFinishedGood ? (
          <label className="checkbox-row">
            <input
              type="checkbox"
              checked={form.procureOnDemand}
              onChange={(e) => handleProcureToggle(e.target.checked)}
            />
            Procure on Demand
          </label>
        ) : null}

        {isFinishedGood && form.procureOnDemand ? (
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