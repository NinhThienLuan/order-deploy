import { useEffect, useRef, useState } from 'react';
import { Pencil, Trash2 } from 'lucide-react';
import {
    getProductVariants, createProductVariant, updateProductVariant, deleteProductVariant,
    getProductImages, uploadProductImages, setPrimaryImage, deleteProductImage,
    updateAdminProduct, deleteAdminProduct, toggleAdminProductActive,
} from '@/features/admin/services/products/adminProducts.service';
import styles from './ProductManageModal.module.css';

const fmt = (n) => new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(n ?? 0);
const EMPTY_V = { sizeName: '', price: '', active: true };

const IconClose = () => (
    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round">
        <line x1="18" y1="6" x2="6" y2="18" /><line x1="6" y1="6" x2="18" y2="18" />
    </svg>
);

// ── Variant row (inline edit) ──────────────────────────────────────────────────
function VariantRow({ v, productId, onSaved, onDeleted }) {
    const [editing, setEditing] = useState(false);
    const [form, setForm] = useState({ sizeName: v.sizeName, price: String(v.price ?? ''), active: v.active ?? true });
    const [busy, setBusy] = useState(false);
    const [err, setErr] = useState('');

    const save = async (e) => {
        e.preventDefault(); setBusy(true); setErr('');
        try { onSaved(await updateProductVariant(productId, v.id, { sizeName: form.sizeName.trim(), price: Number(form.price), active: form.active })); setEditing(false); }
        catch { setErr('Failed.'); }
        finally { setBusy(false); }
    };
    const del = async () => {
        if (!confirm(`Delete variant "${v.sizeName}"?`)) return;
        setBusy(true);
        try { await deleteProductVariant(productId, v.id); onDeleted(v.id); }
        catch { setErr('Failed.'); }
        finally { setBusy(false); }
    };

    if (editing) return (
        <tr className={styles.editRow}>
            <td colSpan={4}>
                <form className={styles.inlineForm} onSubmit={save}>
                    <input className={styles.miniInput} style={{ width: 120 }} placeholder="Size" value={form.sizeName} onChange={e => setForm(p => ({ ...p, sizeName: e.target.value }))} required />
                    <input className={styles.miniInput} style={{ width: 100 }} type="number" placeholder="Price" min="0.01" step="0.01" value={form.price} onChange={e => setForm(p => ({ ...p, price: e.target.value }))} required />
                    <label className={styles.checkLabel}><input type="checkbox" checked={form.active} onChange={e => setForm(p => ({ ...p, active: e.target.checked }))} />Active</label>
                    {err && <span className={styles.miniErr}>{err}</span>}
                    <button className={`${styles.smBtn} ${styles.smSave}`} type="submit" disabled={busy}>{busy ? '…' : 'Save'}</button>
                    <button className={`${styles.smBtn} ${styles.smCancel}`} type="button" onClick={() => setEditing(false)}>✕</button>
                </form>
            </td>
        </tr>
    );

    return (
        <tr>
            <td><strong>{v.sizeName}</strong></td>
            <td>{fmt(v.price)}</td>
            <td>
                <div className={styles.rowActions}>
                    <button className={`${styles.iconBtn} ${styles.smEdit}`} onClick={() => setEditing(true)} title="Edit"><Pencil size={13} /></button>
                    <button className={`${styles.iconBtn} ${styles.smDel}`} onClick={del} disabled={busy} title="Delete"><Trash2 size={13} /></button>
                </div>
            </td>
        </tr>
    );
}

// ── Variants Tab ──────────────────────────────────────────────────────────────
function VariantsTab({ productId }) {
    const [variants, setVariants] = useState([]);
    const [loading, setLoading] = useState(true);
    const [showAdd, setShowAdd] = useState(false);
    const [addForm, setAddForm] = useState(EMPTY_V);
    const [busy, setBusy] = useState(false);
    const [err, setErr] = useState('');

    useEffect(() => {
        setLoading(true);
        getProductVariants(productId).then(setVariants).catch(() => setErr('Failed to load.')).finally(() => setLoading(false));
    }, [productId]);

    const handleAdd = async (e) => {
        e.preventDefault(); setBusy(true); setErr('');
        try {
            const created = await createProductVariant(productId, { sizeName: addForm.sizeName.trim(), price: Number(addForm.price), active: addForm.active });
            setVariants(p => [...p, created]); setAddForm(EMPTY_V); setShowAdd(false);
        } catch { setErr('Create failed.'); }
        finally { setBusy(false); }
    };

    return (
        <>
            <div className={styles.tabTopRow}>
                <span className={styles.tabCount}>{variants.length} variant{variants.length !== 1 ? 's' : ''}</span>
                <button className={styles.addBtn} onClick={() => setShowAdd(p => !p)}>{showAdd ? '✕ Cancel' : '+ Add Variant'}</button>
            </div>
            {err && <div className={styles.errBanner}>{err}</div>}
            {showAdd && (
                <form className={styles.addForm} onSubmit={handleAdd}>
                    <div className={styles.addFormRow}>
                        <div>
                            <label className={styles.miniLabel}>Size Name</label>
                            <input className={styles.miniInput} placeholder="e.g. S, M, L" value={addForm.sizeName} onChange={e => setAddForm(p => ({ ...p, sizeName: e.target.value }))} required />
                        </div>
                        <div>
                            <label className={styles.miniLabel}>Price (USD)</label>
                            <input className={styles.miniInput} type="number" placeholder="0.00" min="0.01" step="0.01" value={addForm.price} onChange={e => setAddForm(p => ({ ...p, price: e.target.value }))} required />
                        </div>
                    </div>
                    <div className={styles.addFormFooter}>
                        <label className={styles.checkLabel}><input type="checkbox" checked={addForm.active} onChange={e => setAddForm(p => ({ ...p, active: e.target.checked }))} />Active</label>
                        <button className={`${styles.smBtn} ${styles.smSave}`} type="submit" disabled={busy}>{busy ? 'Saving…' : 'Save Variant'}</button>
                    </div>
                </form>
            )}
            {loading ? <div className={styles.stateMsg}>Loading…</div> : variants.length === 0 ? (
                <div className={styles.stateMsg}>No variants yet.</div>
            ) : (
                <table className={styles.variantTable}>
                    <thead><tr><th>Size</th><th>Price</th><th>Actions</th></tr></thead>
                    <tbody>
                        {variants.map(v => (
                            <VariantRow key={v.id} v={v} productId={productId}
                                onSaved={u => setVariants(p => p.map(x => x.id === u.id ? u : x))}
                                onDeleted={id => setVariants(p => p.filter(x => x.id !== id))}
                            />
                        ))}
                    </tbody>
                </table>
            )}
        </>
    );
}

// ── Images Tab ────────────────────────────────────────────────────────────────
function ImagesTab({ productId }) {
    const [images, setImages] = useState([]);
    const [loading, setLoading] = useState(true);
    const [uploading, setUploading] = useState(false);
    const [err, setErr] = useState('');
    const fileRef = useRef(null);

    useEffect(() => {
        setLoading(true);
        getProductImages(productId).then(setImages).catch(() => setErr('Failed to load.')).finally(() => setLoading(false));
    }, [productId]);

    const handleUpload = async (e) => {
        const files = Array.from(e.target.files); if (!files.length) return;
        setUploading(true); setErr('');
        try {
            setImages(p => [...p, ...[]]);
            const newImgs = await uploadProductImages(productId, files, images.length === 0);
            setImages(p => [...p, ...newImgs]);
        } catch { setErr('Upload failed.'); }
        finally { setUploading(false); e.target.value = ''; }
    };

    const handleSetPrimary = async (id) => {
        try { await setPrimaryImage(id); setImages(p => p.map(i => ({ ...i, isPrimary: i.id === id }))); }
        catch { setErr('Failed.'); }
    };

    const handleDelete = async (id) => {
        if (!confirm('Delete this image?')) return;
        try { await deleteProductImage(id); setImages(p => p.filter(i => i.id !== id)); }
        catch { setErr('Delete failed.'); }
    };

    return (
        <>
            <div className={styles.tabTopRow}>
                <span className={styles.tabCount}>{images.length} image{images.length !== 1 ? 's' : ''}</span>
                <button className={styles.addBtn} onClick={() => fileRef.current?.click()} disabled={uploading}>
                    {uploading ? 'Uploading…' : '↑ Upload'}
                </button>
                <input ref={fileRef} type="file" accept="image/*" multiple className={styles.hiddenInput} onChange={handleUpload} />
            </div>
            {err && <div className={styles.errBanner}>{err}</div>}
            {loading ? <div className={styles.stateMsg}>Loading…</div> : images.length === 0 ? (
                <div className={styles.stateMsg}>No images yet. Upload some above.</div>
            ) : (
                <div className={styles.imageGrid}>
                    {images.map(img => (
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
        </>
    );
}

// ── Details Tab (edit form) ────────────────────────────────────────────────────
function DetailsTab({ product, categories, onSaved, onDeleted, onToggled }) {
    const [form, setForm] = useState({
        name: product.productName || '',
        description: product.description || '',
        categoryId: product.categoryId || '',
        basePrice: product.price || '',
        active: product.status === 'Available',
    });
    const [busy, setBusy] = useState(false);
    const [delBusy, setDelBusy] = useState(false);
    const [togBusy, setTogBusy] = useState(false);
    const [err, setErr] = useState('');

    const handleSave = async (e) => {
        e.preventDefault(); setBusy(true); setErr('');
        try {
            const updated = await updateAdminProduct(product.id, {
                name: form.name.trim(), description: form.description.trim(),
                categoryId: form.categoryId || null, basePrice: Number(form.basePrice || 0), active: Boolean(form.active),
            });
            onSaved(updated);
        } catch { setErr('Save failed.'); }
        finally { setBusy(false); }
    };

    const handleDelete = async () => {
        if (!confirm('Delete this product permanently?')) return;
        setDelBusy(true); setErr('');
        try { await deleteAdminProduct(product.id); onDeleted(product.id); }
        catch { setErr('Delete failed.'); }
        finally { setDelBusy(false); }
    };

    const handleToggle = async () => {
        setTogBusy(true); setErr('');
        try { await toggleAdminProductActive(product.id); onToggled(); }
        catch { setErr('Toggle failed.'); }
        finally { setTogBusy(false); }
    };

    const isActive = product.status === 'Available';

    return (
        <>
            {err && <div className={styles.errBanner}>{err}</div>}
            <form className={styles.form} id="detail-form" onSubmit={handleSave}>
                <div className={styles.formGroup}>
                    <label className={styles.formLabel}>Product Name *</label>
                    <input className={styles.input} value={form.name} onChange={e => setForm(p => ({ ...p, name: e.target.value }))} required placeholder="e.g. Classic Latte" />
                </div>
                <div className={styles.formGroup}>
                    <label className={styles.formLabel}>Description</label>
                    <textarea className={styles.textarea} rows={2} value={form.description} onChange={e => setForm(p => ({ ...p, description: e.target.value }))} placeholder="Short description..." />
                </div>
                <div className={styles.formRow}>
                    <div className={styles.formGroup}>
                        <label className={styles.formLabel}>Category</label>
                        <select className={styles.select} value={form.categoryId} onChange={e => setForm(p => ({ ...p, categoryId: e.target.value }))}>
                            <option value="">Select…</option>
                            {categories.map(c => <option key={c.id} value={c.id}>{c.name}</option>)}
                        </select>
                    </div>
                    <div className={styles.formGroup}>
                        <label className={styles.formLabel}>Base Price (USD) *</label>
                        <input type="number" min="0" step="0.01" className={styles.input} value={form.basePrice} onChange={e => setForm(p => ({ ...p, basePrice: e.target.value }))} required placeholder="0.00" />
                    </div>
                </div>
                <label className={styles.checkboxRow}>
                    <input type="checkbox" checked={form.active} onChange={e => setForm(p => ({ ...p, active: e.target.checked }))} />
                    <span>Active (visible to customers)</span>
                </label>
            </form>

            {/* Footer actions rendered from parent via render props — passed as footer */}
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginTop: 'auto', paddingTop: '1rem', borderTop: '1px solid rgba(35,31,30,0.07)', gap: '0.5rem' }}>
                <div style={{ display: 'flex', gap: '0.5rem' }}>
                    <button className={`${styles.btnToggle} ${isActive ? styles.btnToggleOn : styles.btnToggleOff}`} type="button" onClick={handleToggle} disabled={togBusy}>
                        {isActive ? 'Deactivate' : 'Activate'}
                    </button>
                    <button className={styles.btnDanger} type="button" onClick={handleDelete} disabled={delBusy}>Delete</button>
                </div>
                <div style={{ display: 'flex', gap: '0.5rem' }}>
                    <button className={styles.btnPrimary} type="submit" form="detail-form" disabled={busy}>{busy ? 'Saving…' : 'Save Changes'}</button>
                </div>
            </div>
        </>
    );
}

// ── Main Modal ────────────────────────────────────────────────────────────────
export default function ProductManageModal({ product, categories, onClose, onProductUpdated, onProductDeleted }) {
    const [activeTab, setActiveTab] = useState('details');

    useEffect(() => {
        const onKey = e => { if (e.key === 'Escape') onClose(); };
        window.addEventListener('keydown', onKey);
        return () => window.removeEventListener('keydown', onKey);
    }, [onClose]);

    if (!product) return null;

    const formatPrice = (p) => new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(p ?? 0);

    return (
        <div className={styles.overlay} onClick={onClose}>
            <div className={styles.modal} onClick={e => e.stopPropagation()}>

                {/* Header */}
                <div className={styles.header}>
                    <div className={styles.headerMeta}>
                        <h2 className={styles.headerName}>{product.productName}</h2>
                        <div className={styles.headerSub}>
                            {product.categoryName && <span className={styles.pill}>{product.categoryName}</span>}
                            {product.type && <span className={styles.pill}>{product.type}</span>}
                            {product.price != null && <span className={styles.pill}>{formatPrice(product.price)}</span>}
                            <span className={styles.pill} style={{ color: product.status === 'Available' ? 'var(--color-success-text)' : 'var(--color-danger)', borderColor: product.status === 'Available' ? 'var(--color-success-text)' : 'var(--color-danger)' }}>
                                {product.status}
                            </span>
                        </div>
                    </div>
                    <button className={styles.closeBtn} onClick={onClose} aria-label="Close"><IconClose /></button>
                </div>

                {/* Tabs */}
                <div className={styles.tabs}>
                    {[['details', 'Details'], ['variants', 'Variants'], ['images', 'Images']].map(([key, label]) => (
                        <button key={key} className={`${styles.tab} ${activeTab === key ? styles.tabActive : ''}`} onClick={() => setActiveTab(key)}>
                            {label}
                        </button>
                    ))}
                </div>

                {/* Body */}
                <div className={styles.body}>
                    {activeTab === 'details' && (
                        <DetailsTab
                            product={product}
                            categories={categories}
                            onSaved={(updated) => { onProductUpdated?.(updated); onClose(); }}
                            onDeleted={(id) => { onProductDeleted?.(id); onClose(); }}
                            onToggled={() => { onProductUpdated?.(); onClose(); }}
                        />
                    )}
                    {activeTab === 'variants' && <VariantsTab productId={product.id} />}
                    {activeTab === 'images' && <ImagesTab productId={product.id} />}
                </div>
            </div>
        </div>
    );
}
