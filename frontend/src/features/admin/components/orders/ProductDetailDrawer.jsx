import { useEffect, useRef, useState } from 'react';
import { Pencil, Trash2 } from 'lucide-react';
import {
  getProductVariants,
  createProductVariant,
  updateProductVariant,
  deleteProductVariant,
  getProductImages,
  uploadProductImages,
  setPrimaryImage,
  deleteProductImage,
} from '@/features/admin/services/products/adminProducts.service';
import styles from './ProductDetailDrawer.module.css';

const EMPTY_VARIANT = { sizeName: '', price: '', active: true };

// ── Helpers ──────────────────────────────────────────────────────────────────
const formatVND = (n) =>
  new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(n ?? 0);

// ── Sub-components ───────────────────────────────────────────────────────────

function VariantRow({ v, productId, onSaved, onDeleted }) {
  const [editing, setEditing] = useState(false);
  const [form, setForm] = useState({ sizeName: v.sizeName, price: String(v.price ?? ''), active: v.active ?? true });
  const [busy, setBusy] = useState(false);
  const [err, setErr] = useState('');

  const handleSave = async (e) => {
    e.preventDefault();
    setBusy(true);
    setErr('');
    try {
      const updated = await updateProductVariant(productId, v.id, {
        sizeName: form.sizeName.trim(),
        price: Number(form.price),
        active: form.active,
      });
      onSaved(updated);
      setEditing(false);
    } catch {
      setErr('Update failed.');
    } finally {
      setBusy(false);
    }
  };

  const handleDelete = async () => {
    if (!confirm(`Delete variant "${v.sizeName}"?`)) return;
    setBusy(true);
    try {
      await deleteProductVariant(productId, v.id);
      onDeleted(v.id);
    } catch {
      setErr('Delete failed.');
    } finally {
      setBusy(false);
    }
  };

  if (editing) {
    return (
      <tr className={styles.editRow}>
        <td colSpan={4}>
          <form className={styles.inlineForm} onSubmit={handleSave}>
            <input
              className={styles.miniInput}
              placeholder="Size name"
              value={form.sizeName}
              onChange={(e) => setForm((p) => ({ ...p, sizeName: e.target.value }))}
              required
            />
            <input
              className={styles.miniInput}
              type="number"
              placeholder="Price"
              min="0.01"
              step="0.01"
              value={form.price}
              onChange={(e) => setForm((p) => ({ ...p, price: e.target.value }))}
              required
            />
            <label className={styles.checkLabel}>
              <input
                type="checkbox"
                checked={form.active}
                onChange={(e) => setForm((p) => ({ ...p, active: e.target.checked }))}
              />
              Active
            </label>
            {err && <span className={styles.miniErr}>{err}</span>}
            <button type="submit" className={styles.saveBtn} disabled={busy}>Save</button>
            <button type="button" className={styles.cancelBtn} onClick={() => setEditing(false)}>✕</button>
          </form>
        </td>
      </tr>
    );
  }

  return (
    <tr>
      <td>{v.sizeName}</td>
      <td>{formatVND(v.price)}</td>
      <td>
        <div className={styles.rowActions}>
          <button className={`${styles.iconBtn} ${styles.editBtn}`} onClick={() => setEditing(true)} title="Edit"><Pencil size={13} /></button>
          <button className={`${styles.iconBtn} ${styles.delBtn}`} onClick={handleDelete} disabled={busy} title="Delete"><Trash2 size={13} /></button>
        </div>
      </td>
    </tr>
  );
}

function VariantsTab({ productId }) {
  const [variants, setVariants] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showAdd, setShowAdd] = useState(false);
  const [addForm, setAddForm] = useState(EMPTY_VARIANT);
  const [addBusy, setAddBusy] = useState(false);
  const [err, setErr] = useState('');

  const load = async () => {
    setLoading(true);
    try {
      setVariants(await getProductVariants(productId));
    } catch {
      setErr('Failed to load variants.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { load(); }, [productId]);

  const handleAdd = async (e) => {
    e.preventDefault();
    setAddBusy(true);
    setErr('');
    try {
      const created = await createProductVariant(productId, {
        sizeName: addForm.sizeName.trim(),
        price: Number(addForm.price),
        active: addForm.active,
      });
      setVariants((p) => [...p, created]);
      setAddForm(EMPTY_VARIANT);
      setShowAdd(false);
    } catch {
      setErr('Create failed.');
    } finally {
      setAddBusy(false);
    }
  };

  return (
    <div className={styles.tabContent}>
      <div className={styles.tabTopRow}>
        <span className={styles.tabCount}>{variants.length} variant{variants.length !== 1 ? 's' : ''}</span>
        <button className={styles.addBtn} onClick={() => setShowAdd((p) => !p)}>
          {showAdd ? '✕ Cancel' : '+ Add Variant'}
        </button>
      </div>

      {err && <div className={styles.errBanner}>{err}</div>}

      {showAdd && (
        <form className={styles.addForm} onSubmit={handleAdd}>
          <input
            className={styles.miniInput}
            placeholder="Size name (e.g. S, M, L)"
            value={addForm.sizeName}
            onChange={(e) => setAddForm((p) => ({ ...p, sizeName: e.target.value }))}
            required
          />
          <input
            className={styles.miniInput}
            type="number"
            placeholder="Price (USD)"
            min="0.01"
            step="0.01"
            value={addForm.price}
            onChange={(e) => setAddForm((p) => ({ ...p, price: e.target.value }))}
            required
          />
          <label className={styles.checkLabel}>
            <input
              type="checkbox"
              checked={addForm.active}
              onChange={(e) => setAddForm((p) => ({ ...p, active: e.target.checked }))}
            />
            Active
          </label>
          <button type="submit" className={styles.saveBtn} disabled={addBusy}>
            {addBusy ? 'Saving…' : 'Save'}
          </button>
        </form>
      )}

      {loading ? (
        <div className={styles.stateMsg}>Loading…</div>
      ) : variants.length === 0 ? (
        <div className={styles.stateMsg}>No variants yet. Add one above.</div>
      ) : (
        <table className={styles.variantTable}>
          <thead>
            <tr>
              <th>Size</th>
              <th>Price</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {variants.map((v) => (
              <VariantRow
                key={v.id}
                v={v}
                productId={productId}
                onSaved={(updated) =>
                  setVariants((prev) => prev.map((x) => (x.id === updated.id ? updated : x)))
                }
                onDeleted={(id) => setVariants((prev) => prev.filter((x) => x.id !== id))}
              />
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
}

function ImagesTab({ productId }) {
  const [images, setImages] = useState([]);
  const [loading, setLoading] = useState(true);
  const [uploading, setUploading] = useState(false);
  const [err, setErr] = useState('');
  const fileRef = useRef(null);

  const load = async () => {
    setLoading(true);
    try {
      setImages(await getProductImages(productId));
    } catch {
      setErr('Failed to load images.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { load(); }, [productId]);

  const handleUpload = async (e) => {
    const files = Array.from(e.target.files);
    if (!files.length) return;
    setUploading(true);
    setErr('');
    try {
      const newImgs = await uploadProductImages(productId, files, images.length === 0);
      setImages((prev) => [...prev, ...newImgs]);
    } catch {
      setErr('Upload failed.');
    } finally {
      setUploading(false);
      e.target.value = '';
    }
  };

  const handleSetPrimary = async (imageId) => {
    setErr('');
    try {
      await setPrimaryImage(imageId);
      setImages((prev) =>
        prev.map((img) => ({ ...img, isPrimary: img.id === imageId }))
      );
    } catch {
      setErr('Failed to set primary.');
    }
  };

  const handleDelete = async (imageId) => {
    if (!confirm('Delete this image?')) return;
    setErr('');
    try {
      await deleteProductImage(imageId);
      setImages((prev) => prev.filter((img) => img.id !== imageId));
    } catch {
      setErr('Delete failed.');
    }
  };

  return (
    <div className={styles.tabContent}>
      <div className={styles.tabTopRow}>
        <span className={styles.tabCount}>{images.length} image{images.length !== 1 ? 's' : ''}</span>
        <button
          className={styles.addBtn}
          onClick={() => fileRef.current?.click()}
          disabled={uploading}
        >
          {uploading ? 'Uploading…' : '↑ Upload Images'}
        </button>
        <input
          ref={fileRef}
          type="file"
          accept="image/*"
          multiple
          className={styles.hiddenInput}
          onChange={handleUpload}
        />
      </div>

      {err && <div className={styles.errBanner}>{err}</div>}

      {loading ? (
        <div className={styles.stateMsg}>Loading…</div>
      ) : images.length === 0 ? (
        <div className={styles.stateMsg}>No images yet. Upload some above.</div>
      ) : (
        <div className={styles.imageGrid}>
          {images.map((img) => (
            <div key={img.id ?? img.imageUrl} className={styles.imageCard}>
              <div className={styles.imgWrapper}>
                <img src={img.imageUrl} alt="" className={styles.img} />
                {img.isPrimary && <span className={styles.primaryBadge}>Primary</span>}
                <div className={styles.imgActions}>
                  <button className={styles.imgActionBtn} onClick={() => handleDelete(img.id)} title="Delete">✕</button>
                  <button
                    className={`${styles.imgActionBtn} ${img.isPrimary ? styles.imgActionBtnActive : ''}`}
                    onClick={() => !img.isPrimary && handleSetPrimary(img.id)}
                    title={img.isPrimary ? "Primary Image" : "Set as primary"}
                  >✓</button>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}

// ── Main drawer ───────────────────────────────────────────────────────────────

export default function ProductDetailDrawer({ product, onClose }) {
  const [activeTab, setActiveTab] = useState('variants');

  // Close on Escape
  useEffect(() => {
    const onKey = (e) => { if (e.key === 'Escape') onClose(); };
    window.addEventListener('keydown', onKey);
    return () => window.removeEventListener('keydown', onKey);
  }, [onClose]);

  if (!product) return null;

  return (
    <>
      <div className={styles.backdrop} onClick={onClose} />
      <aside className={styles.drawer}>
        <div className={styles.drawerHeader}>
          <div>
            <p className={styles.drawerLabel}>Product Details</p>
            <h2 className={styles.drawerTitle}>{product.productName}</h2>
          </div>
          <button className={styles.closeBtn} onClick={onClose} aria-label="Close">✕</button>
        </div>

        <div className={styles.tabs}>
          <button
            className={`${styles.tab} ${activeTab === 'variants' ? styles.tabActive : ''}`}
            onClick={() => setActiveTab('variants')}
          >
            Variants
          </button>
          <button
            className={`${styles.tab} ${activeTab === 'images' ? styles.tabActive : ''}`}
            onClick={() => setActiveTab('images')}
          >
            Images
          </button>
        </div>

        <div className={styles.drawerBody}>
          {activeTab === 'variants' && <VariantsTab productId={product.id} />}
          {activeTab === 'images' && <ImagesTab productId={product.id} />}
        </div>
      </aside>
    </>
  );
}
