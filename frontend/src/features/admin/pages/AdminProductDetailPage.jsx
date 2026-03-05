import { useEffect, useRef, useState } from 'react';
import { Pencil, Trash2 } from 'lucide-react';
import { useNavigate, useParams } from 'react-router-dom';
import AdminHeader from '../components/AdminHeader';
import { useAuth } from '@/features/auth/AuthContext';
import {
    getAdminProductById,
    updateAdminProduct, deleteAdminProduct, toggleAdminProductActive,
    getProductVariants, createProductVariant, updateProductVariant, deleteProductVariant,
    getProductImages, uploadProductImages, setPrimaryImage, deleteProductImage,
} from '../services/adminProducts.service';
import { getCategories } from '@/features/products/products.service';
import styles from './AdminProductDetailPage.module.css';

const fmt = (n) => new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(n ?? 0);
const EMPTY_V = { sizeName: '', price: '', active: true };

// ── Icons ──────────────────────────────────────────────────────────────────────
const IconBack = () => (
    <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
        <polyline points="15 18 9 12 15 6" />
    </svg>
);

// ── Variant row with inline edit ───────────────────────────────────────────────
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
        catch { setErr('Delete failed.'); }
        finally { setBusy(false); }
    };

    if (editing) return (
        <tr className={styles.editRow}>
            <td colSpan={4}>
                <form className={styles.inlineForm} onSubmit={save}>
                    <input className={styles.miniInput} style={{ width: 110 }} placeholder="Size" value={form.sizeName} onChange={e => setForm(p => ({ ...p, sizeName: e.target.value }))} required />
                    <input className={styles.miniInput} style={{ width: 90 }} type="number" placeholder="e.g. 55000" min="1000" step="1000" value={form.price} onChange={e => setForm(p => ({ ...p, price: e.target.value }))} required />
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

// ── Variants Panel ─────────────────────────────────────────────────────────────
function VariantsPanel({ productId }) {
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
        <div className={styles.variantsPanel}>
            <div className={styles.panelHeader}>
                <span className={styles.panelTitle}>Variants</span>
                <button className={styles.addBtn} onClick={() => setShowAdd(p => !p)}>{showAdd ? '✕ Cancel' : '+ Add'}</button>
            </div>
            <div className={styles.variantsPanelBody}>
                {err && <div className={styles.errorBanner} style={{ marginBottom: '0.75rem' }}>{err}</div>}
                {showAdd && (
                    <form className={styles.addForm} onSubmit={handleAdd}>
                        <div className={styles.addFormRow}>
                            <div>
                                <label className={styles.miniLabel}>Size Name</label>
                                <input className={styles.miniInput} placeholder="e.g. S, M, L" value={addForm.sizeName} onChange={e => setAddForm(p => ({ ...p, sizeName: e.target.value }))} required />
                            </div>
                            <div>
                                <label className={styles.miniLabel}>Price (VND)</label>
                                <input className={styles.miniInput} type="number" placeholder="e.g. 55000" min="1000" step="1000" value={addForm.price} onChange={e => setAddForm(p => ({ ...p, price: e.target.value }))} required />
                            </div>
                        </div>
                        <div className={styles.addFormFooter}>
                            <label className={styles.checkLabel}><input type="checkbox" checked={addForm.active} onChange={e => setAddForm(p => ({ ...p, active: e.target.checked }))} />Active</label>
                            <button className={`${styles.smBtn} ${styles.smSave}`} type="submit" disabled={busy}>{busy ? 'Saving…' : 'Save'}</button>
                        </div>
                    </form>
                )}
                {loading ? <div className={styles.stateMsg}>Loading…</div> : variants.length === 0 ? (
                    <div className={styles.stateMsg}>No variants yet.</div>
                ) : (
                    <div className={styles.tabTopRow} style={{ marginBottom: '0.6rem' }}>
                        <span className={styles.tabCount}>{variants.length} variant{variants.length !== 1 ? 's' : ''}</span>
                    </div>
                )}
                {!loading && variants.length > 0 && (
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
            </div>
        </div>
    );
}

// ── Images Panel ───────────────────────────────────────────────────────────────
function ImagesPanel({ productId }) {
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
        try { const newImgs = await uploadProductImages(productId, files, images.length === 0); setImages(p => [...p, ...newImgs]); }
        catch { setErr('Upload failed.'); }
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
            <div className={styles.panelHeader}>
                <span className={styles.panelTitle}>Images</span>
                <button className={styles.addBtn} onClick={() => fileRef.current?.click()} disabled={uploading}>
                    {uploading ? 'Uploading…' : '↑ Upload'}
                </button>
                <input ref={fileRef} type="file" accept="image/*" multiple className={styles.hiddenInput} onChange={handleUpload} />
            </div>
            <div className={styles.panelBody}>
                {err && <div className={styles.errorBanner} style={{ marginBottom: '0.75rem' }}>{err}</div>}
                {loading ? <div className={styles.stateMsg}>Loading…</div> : images.length === 0 ? (
                    <div className={styles.stateMsg}>No images yet.</div>
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
            </div>
        </>
    );
}

// ── Main Page ──────────────────────────────────────────────────────────────────
export default function AdminProductDetailPage() {
    const { productId } = useParams();
    const navigate = useNavigate();
    const { user } = useAuth();
    const role = user?.role ?? 'FRANCHISE_ADMIN';
    const canEdit = role === 'FRANCHISE_ADMIN';

    const [product, setProduct] = useState(null);
    const [categories, setCategories] = useState([]);
    const [loading, setLoading] = useState(true);
    const [form, setForm] = useState({ name: '', description: '', categoryId: '', type: '', active: true });
    const [saveBusy, setSaveBusy] = useState(false);
    const [delBusy, setDelBusy] = useState(false);
    const [togBusy, setTogBusy] = useState(false);
    const [error, setError] = useState('');
    const [success, setSuccess] = useState('');

    useEffect(() => {
        const fetchAll = async () => {
            setLoading(true);
            try {
                const [prod, cats] = await Promise.all([getAdminProductById(productId), getCategories()]);
                setProduct(prod);
                setCategories(cats || []);
                setForm({ name: prod.productName || '', description: prod.description || '', categoryId: prod.categoryId || '', type: prod.type || '', active: prod.status === 'Available' });
            } catch { setError('Failed to load product.'); }
            finally { setLoading(false); }
        };
        fetchAll();
    }, [productId]);

    const handleSave = async (e) => {
        e.preventDefault(); setSaveBusy(true); setError(''); setSuccess('');
        try {
            const updated = await updateAdminProduct(productId, {
                name: form.name.trim(),
                description: form.description.trim(),
                categoryId: form.categoryId || null,
                type: form.type || null,
                active: Boolean(form.active),
            });
            setProduct(updated);
            setSuccess('Saved successfully.');
        } catch { setError('Save failed.'); }
        finally { setSaveBusy(false); }
    };

    const handleDelete = async () => {
        if (!confirm('Delete this product permanently?')) return;
        setDelBusy(true);
        try { await deleteAdminProduct(productId); navigate('/admin/catalog'); }
        catch { setError('Delete failed.'); setDelBusy(false); }
    };

    const handleToggle = async () => {
        setTogBusy(true); setError(''); setSuccess('');
        try {
            await toggleAdminProductActive(productId);
            const updated = await getAdminProductById(productId);
            setProduct(updated);
            setSuccess(updated.status === 'Available' ? 'Product activated.' : 'Product deactivated.');
        } catch { setError('Toggle failed.'); }
        finally { setTogBusy(false); }
    };

    const breadcrumbRoot = role === 'STORE_MANAGER' ? 'Store' : 'Franchise';
    const isActive = product?.status === 'Available';

    if (loading) return (
        <div className={styles.main}>
            <AdminHeader breadcrumbs={[breadcrumbRoot, 'Products', '…']} />
            <div className={styles.loading}>Loading product…</div>
        </div>
    );

    return (
        <div className={styles.main}>
            <AdminHeader breadcrumbs={[breadcrumbRoot, 'Products', product?.productName || '']} />

            {/* ─ Page Header ─ */}
            <div className={styles.pageHeader}>
                <div className={styles.headerMeta}>
                    <h1 className={styles.productName}>{product?.productName}</h1>
                    <div className={styles.headerPills}>
                        {product?.categoryName && <span className={styles.pill}>{product.categoryName}</span>}
                        {product?.type && <span className={styles.pill}>{product.type}</span>}
                        {product?.price != null && <span className={styles.pill}>{fmt(product.price)}</span>}
                        <span className={`${styles.pill} ${isActive ? styles.pillActive : styles.pillInactive}`}>
                            {isActive ? 'Active' : 'Inactive'}
                        </span>
                    </div>
                </div>
                {canEdit && (
                    <div className={styles.headerActions}>
                        <button className={`${styles.btnToggle} ${isActive ? styles.btnToggleOn : styles.btnToggleOff} ${styles.btnSm}`} type="button" onClick={handleToggle} disabled={togBusy}>
                            {togBusy ? '…' : isActive ? 'Deactivate' : 'Activate'}
                        </button>
                        <button className={`${styles.btnDanger} ${styles.btnSm}`} type="button" onClick={handleDelete} disabled={delBusy}>
                            {delBusy ? '…' : 'Delete'}
                        </button>
                    </div>
                )}
            </div>

            { error && <div className={styles.errorBanner}>⚠ {error}</div> }
            { success && <div className={styles.successBanner}>✓ {success}</div> }

           
            {/* ─ Main Grid ─ */}
            <div className={styles.grid}>
            
        {/* ─── Left Column ─── */}
        <div className={styles.colLeft}>

            {/* Images panel */}
            <div className={styles.panel}>
                <ImagesPanel productId={productId} />
            </div>

            {/* Product Details panel */}
            <div className={styles.panel}>
                <div className={styles.panelHeader}>
                    <span className={styles.panelTitle}>Product Details</span>
                </div>
                <div className={styles.panelBody}>
                    <form className={styles.form} id="detail-form" onSubmit={handleSave}>
                        <div className={styles.formGroup}>
                            <label className={styles.formLabel}>Product Name *</label>
                            <input className={styles.input} value={form.name} onChange={e => setForm(p => ({ ...p, name: e.target.value }))} required placeholder="e.g. Classic Latte" disabled={!canEdit} />
                        </div>
                        <div className={styles.formGroup}>
                            <label className={styles.formLabel}>Description</label>
                            <textarea className={styles.textarea} rows={3} value={form.description} onChange={e => setForm(p => ({ ...p, description: e.target.value }))} placeholder="Short description…" disabled={!canEdit} />
                        </div>
                        <div className={styles.formRow}>
                            <div className={styles.formGroup}>
                                <label className={styles.formLabel}>Category</label>
                                <select className={styles.select} value={form.categoryId} onChange={e => setForm(p => ({ ...p, categoryId: e.target.value }))} disabled={!canEdit}>
                                    <option value="">Select…</option>
                                    {categories.map(c => <option key={c.id} value={c.id}>{c.name}</option>)}
                                </select>
                            </div>
                            <div className={styles.formGroup}>
                                <label className={styles.formLabel}>Type</label>
                                <select className={styles.select} value={form.type} onChange={e => setForm(p => ({ ...p, type: e.target.value }))} disabled={!canEdit}>
                                    <option value="">None</option>
                                    <option value="MASTER">Master</option>
                                    <option value="SIGNATURE">Signature</option>
                                </select>
                            </div>
                        </div>
                    </form>

                    {canEdit && (
                        <div className={styles.formActions}>
                            <div className={styles.formActionsRight} style={{ marginLeft: 'auto' }}>
                                <button className={styles.btnGhost} type="button" onClick={() => navigate('/admin/catalog')}>Cancel</button>
                                <button className={styles.btnPrimary} type="submit" form="detail-form" disabled={saveBusy}>
                                    {saveBusy ? 'Saving…' : 'Save Changes'}
                                </button>
                            </div>
                        </div>
                    )}
                </div>
            </div>

        </div>

        {/* ─── Right Column ─── */}
        <div className={styles.colRight}>
            <VariantsPanel productId={productId} />
        </div>
    </div>
</div >
    );
}
