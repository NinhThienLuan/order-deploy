import { useEffect, useRef, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import {
    ChevronLeft,
    Pencil,
    Trash2,
    Plus,
    X,
    Star,
    Upload,
    Image,
} from 'lucide-react';
import { useAuth } from '@/features/authentication/services/AuthContext';
import {
    getAdminProductById,
    createAdminProduct, updateAdminProduct, deleteAdminProduct, toggleAdminProductActive,
    getProductVariants, createProductVariant, updateProductVariant, deleteProductVariant,
    getProductImages, uploadProductImages, setPrimaryImage, deleteProductImage,
} from '@/features/admin/services/products/adminProducts.service';
import { getCategories } from '@/features/products/services/products.service';
import Button from '@/components/ui/Button';
import Input from '@/components/ui/Input';
import Card from '@/components/ui/Card';
import Badge from '@/components/ui/Badge';
import Select from '@/components/ui/Select';
import Toggle from '@/components/ui/Toggle';
import AdminPageHeader from '@/components/layout/AdminPageHeader';
import styles from './AdminProductDetailPage.module.css';

const fmt = (n) => new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(n ?? 0);
const EMPTY_V = { sizeName: '', price: '' };

// ── Inline Variant Pre-Add (create mode only) — fully controlled ────────────
function NewVariantInlineList({ variants, onVariantsChange, draftForm, onDraftChange }) {
    const [err, setErr] = useState('');

    const handleAdd = (e) => {
        e.preventDefault();
        setErr('');
        if (!draftForm.sizeName.trim()) { setErr('Size name is required.'); return; }
        if (!draftForm.price || isNaN(Number(draftForm.price)) || Number(draftForm.price) < 0) { setErr('Enter a valid price.'); return; }
        const duplicate = variants.some(v => v.sizeName.toLowerCase() === draftForm.sizeName.trim().toLowerCase());
        if (duplicate) { setErr('This size already exists.'); return; }
        onVariantsChange([...variants, { sizeName: draftForm.sizeName.trim(), price: Number(draftForm.price), _id: Date.now() }]);
        onDraftChange(EMPTY_V);
    };

    const handleRemove = (id) => onVariantsChange(variants.filter(v => v._id !== id));

    return (
        <div className={styles.variantPreCreate}>
            <div className={styles.panelHeader} style={{ padding: '0 0 1rem 0', borderBottom: '1px solid rgba(35,31,30,0.08)' }}>
                <span className={styles.panelTitle}>Pricing &amp; Variants</span>
                <span className={styles.createHint}>Add at least one size &amp; price</span>
            </div>

            {/* Confirmed pending variants */}
            {variants.length > 0 && (
                <div className={styles.variantPreviewList}>
                    {variants.map(v => (
                        <div key={v._id} className={styles.variantPreviewItem}>
                            <div className={styles.variantPreviewInfo}>
                                <span className={styles.variantPreviewSize}>{v.sizeName}</span>
                                <span className={styles.variantPreviewPrice}>{fmt(v.price)}</span>
                            </div>
                            <button
                                type="button"
                                className={styles.variantPreviewRemove}
                                onClick={() => handleRemove(v._id)}
                                title="Remove"
                            >
                                <X size={12} />
                            </button>
                        </div>
                    ))}
                </div>
            )}

            {/* Draft – in-progress row */}
            <form className={styles.variantAddInline} onSubmit={handleAdd}>
                <div className={styles.variantAddFields}>
                    <Input
                        label="Size Name"
                        placeholder="e.g. Medium"
                        value={draftForm.sizeName}
                        onChange={e => { onDraftChange(p => ({ ...p, sizeName: e.target.value })); setErr(''); }}
                    />
                    <Input
                        label="Price (VND)"
                        type="number"
                        placeholder="e.g. 55000"
                        value={draftForm.price}
                        onChange={e => { onDraftChange(p => ({ ...p, price: e.target.value })); setErr(''); }}
                    />
                </div>
                {err && <div className={styles.variantAddErr}>{err}</div>}
                <Button type="submit" variant="ghost" className={styles.variantAddBtn}>
                    <Plus size={14} />
                    Add Size
                </Button>
            </form>
        </div>
    );
}

// ── Variant row with inline edit ───────────────────────────────────────────────
function VariantRow({ v, productId, onSaved, onDeleted }) {
    const [editing, setEditing] = useState(false);
    const [form, setForm] = useState({ sizeName: v.sizeName, price: String(v.price ?? '') });
    const [busy, setBusy] = useState(false);
    const [err, setErr] = useState('');

    const save = async (e) => {
        e.preventDefault(); setBusy(true); setErr('');
        try { onSaved(await updateProductVariant(productId, v.id, { sizeName: form.sizeName.trim(), price: Number(form.price) })); setEditing(false); }
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
            <td colSpan={3}>
                <form className={styles.inlineForm} onSubmit={save}>
                    <Input className={styles.miniInput} style={{ width: 80 }} placeholder="Size" value={form.sizeName} onChange={e => setForm(p => ({ ...p, sizeName: e.target.value }))} required />
                    <Input className={styles.miniInput} style={{ width: 100 }} type="number" placeholder="Price" value={form.price} onChange={e => setForm(p => ({ ...p, price: e.target.value }))} required />
                    <Button className={styles.btnPrimary} type="submit" disabled={busy}>{busy ? '…' : 'Save'}</Button>
                    <Button variant="ghost" onClick={() => setEditing(false)}>✕</Button>
                </form>
            </td>
        </tr>
    );

    return (
        <tr>
            <td><strong>{v.sizeName}</strong></td>
            <td>{fmt(v.price)}</td>
            <td>
                <div className={styles.rowActions} style={{ justifyContent: 'flex-end' }}>
                    <Button variant="ghost" size="sm" className={styles.iconBtn} onClick={() => setEditing(true)} title="Edit"><Pencil size={13} /></Button>
                    <Button variant="danger" size="sm" className={styles.iconBtn} onClick={del} disabled={busy} title="Delete"><Trash2 size={13} /></Button>
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
            const created = await createProductVariant(productId, { sizeName: addForm.sizeName.trim(), price: Number(addForm.price) });
            setVariants(p => [...p, created]); setAddForm(EMPTY_V); setShowAdd(false);
        } catch { setErr('Create failed.'); }
        finally { setBusy(false); }
    };

    return (
        <div className={styles.panel}>
            <div className={styles.panelHeader}>
                <span className={styles.panelTitle}>Pricing and Stock</span>
                <Button variant="ghost" className={styles.addBtn} onClick={() => setShowAdd(p => !p)}>{showAdd ? '✕' : '+ Add Variant'}</Button>
            </div>
            <div className={styles.panelBody}>
                {err && <div className={styles.errorBanner}>{err}</div>}
                {showAdd && (
                    <form className={styles.addForm} onSubmit={handleAdd}>
                        <div className={styles.addFormRow}>
                            <Input label="Size" placeholder="XL" value={addForm.sizeName} onChange={e => setAddForm(p => ({ ...p, sizeName: e.target.value }))} required />
                            <Input label="Price (VND)" type="number" placeholder="55000" value={addForm.price} onChange={e => setAddForm(p => ({ ...p, price: e.target.value }))} required />
                        </div>
                        <div className={styles.addFormFooter}>
                            <Button className={styles.btnPrimary} type="submit" disabled={busy}>{busy ? '…' : 'Save'}</Button>
                        </div>
                    </form>
                )}
                <div style={{ marginTop: '1rem' }}>
                    <table className={styles.variantTable}>
                        <thead><tr><th>Size</th><th>Price</th><th style={{ textAlign: 'right' }}></th></tr></thead>
                        <tbody>
                            {variants.map(v => (
                                <VariantRow key={v.id} v={v} productId={productId}
                                    onSaved={u => setVariants(p => p.map(x => x.id === u.id ? u : x))}
                                    onDeleted={id => setVariants(p => p.filter(x => x.id !== id))}
                                />
                            ))}
                        </tbody>
                    </table>
                </div>
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
        catch (error) { console.error('Upload Error:', error); setErr('Upload failed: ' + (error?.response?.data?.message || error.message)); }
        finally { setUploading(false); e.target.value = ''; }
    };
    const handleSetPrimary = async (id) => {
        try { await setPrimaryImage(productId, id); setImages(p => p.map(i => ({ ...i, isPrimary: i.id === id }))); }
        catch { setErr('Failed.'); }
    };
    const handleDelete = async (id) => {
        if (!confirm('Delete this image?')) return;
        try { await deleteProductImage(id); setImages(p => p.filter(i => i.id !== id)); }
        catch { setErr('Delete failed.'); }
    };

    const primaryImg = images.find(i => i.isPrimary) || images[0];

    return (
        <div className={styles.panel}>
            <input
                type="file"
                multiple
                hidden
                ref={fileRef}
                onChange={handleUpload}
                accept="image/*"
            />
            <div className={styles.panelHeader}>
                <span className={styles.panelTitle}>Media Assets</span>
                <div className={styles.headerActions}>
                    <Button 
                        className={styles.btnPrimary} 
                        onClick={() => fileRef.current?.click()} 
                        disabled={uploading}
                        icon={<Upload size={16} />}
                    >
                        {uploading ? 'Uploading...' : 'Upload Images'}
                    </Button>
                </div>
            </div>

            <div className={styles.panelBody}>
                {err && <div className={styles.errorBanner}>{err}</div>}

                <div className={styles.mediaGrid}>
                    {/* Primary Preview Section */}
                    <div className={styles.featuredSection}>
                        <div className={styles.featuredLabel}>Cover Image</div>
                        <div className={styles.featuredImage}>
                            {primaryImg ? (
                                <img src={primaryImg.imageUrl} alt="Featured" />
                            ) : (
                                <div className={styles.stateMsg}>
                                    <Image size={48} strokeWidth={1} style={{ marginBottom: '1rem', opacity: 0.3 }} />
                                    <div>No images yet</div>
                                </div>
                            )}
                        </div>
                    </div>

                    <div className={styles.gallerySection}>
                        <div className={styles.featuredLabel}>Gallery</div>
                        <div className={styles.imageGrid}>
                            {images.map(img => (
                                <div 
                                    key={img.id} 
                                    className={`${styles.imageCard} ${img.isPrimary ? styles.imageCardActive : ''}`}
                                    onClick={() => handleSetPrimary(img.id)}
                                >
                                    <div className={styles.imgWrapper}>
                                        <img src={img.imageUrl} alt="" className={styles.img} />
                                        {img.isPrimary && <div className={styles.primaryBadge}>Primary</div>}
                                        
                                        <div className={styles.imgActions}>
                                            <button 
                                                className={`${styles.imgActionBtn} ${img.isPrimary ? styles.imgActionBtnActive : ''}`}
                                                title="Set as Primary"
                                                onClick={(e) => { e.stopPropagation(); handleSetPrimary(img.id); }}
                                            >
                                                <Star size={14} fill={img.isPrimary ? "currentColor" : "none"} />
                                            </button>
                                            <button 
                                                className={`${styles.imgActionBtn} ${styles.imgActionBtnDestructive}`}
                                                title="Delete Image"
                                                onClick={(e) => { e.stopPropagation(); handleDelete(img.id); }}
                                            >
                                                <Trash2 size={14} />
                                            </button>
                                        </div>
                                    </div>
                                </div>
                            ))}
                            <div className={styles.imageCardAdd} onClick={() => fileRef.current?.click()}>
                                <Plus size={24} />
                                <span>Add Media</span>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
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
    const isNew = !productId || productId === 'new';

    const [categories, setCategories] = useState([]);
    const [loading, setLoading] = useState(true);
    const [form, setForm] = useState({ name: '', description: '', categoryId: '', active: true });
    const [pendingVariants, setPendingVariants] = useState([]); // create mode only
    const [variantDraft, setVariantDraft] = useState(EMPTY_V);   // in-progress variant row
    const [saveBusy, setSaveBusy] = useState(false);
    const [delBusy, setDelBusy] = useState(false);
    const [togBusy, setTogBusy] = useState(false);
    const [error, setError] = useState('');
    const [success, setSuccess] = useState('');
    const [activeTab, setActiveTab] = useState('details');

    useEffect(() => {
        const fetchAll = async () => {
            setLoading(true);
            try {
                const cats = await getCategories();
                setCategories(cats || []);

                if (isNew) {
                    setForm({ name: '', description: '', categoryId: '', active: true });
                } else {
                    const prod = await getAdminProductById(productId);
                    setProduct(prod);
                    setForm({ name: prod.productName || '', description: prod.description || '', categoryId: prod.categoryId || '', active: prod.status === 'Available' });
                }
            } catch { setError('Failed to load data.'); }
            finally { setLoading(false); }
        };
        fetchAll();
    }, [productId, isNew]);

    const handleSave = async (e) => {
        if (e) e.preventDefault();
        if (!form.name.trim()) { setError('Product name is required.'); return; }
        setSaveBusy(true); setError(''); setSuccess('');
        try {
            const payload = {
                name: form.name.trim(),
                description: form.description.trim(),
                categoryId: form.categoryId || null,
                active: Boolean(form.active),
            };

            if (isNew) {
                const created = await createAdminProduct(payload);

                // Build full variant list: flush draft row into pending list
                const draftSizeName = variantDraft.sizeName.trim();
                const draftPrice = Number(variantDraft.price);
                const draftVariant = (draftSizeName && !isNaN(draftPrice) && draftPrice >= 0)
                    ? [{ sizeName: draftSizeName, price: draftPrice }]
                    : [];

                const allVariants = [...pendingVariants, ...draftVariant];

                if (allVariants.length > 0) {
                    await Promise.all(
                        allVariants.map(v =>
                            createProductVariant(created.id, { sizeName: v.sizeName, price: v.price })
                        )
                    );
                }
                navigate(`/admin/catalog/${created.id}`);
            } else {
                const updated = await updateAdminProduct(productId, payload);
                setProduct(updated);
                setSuccess('Saved successfully.');
            }
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

    const isActiveVisibility = product?.status === 'Available';
    const breadcrumbs = ['Franchise', 'Catalog', isNew ? 'New Product' : product?.productName || 'Product Details'];

    if (loading) return (
        <div className={styles.main}>
            <div className={styles.loading}>Loading product…</div>
        </div>
    );

    // ─────────────────────────────────────────────────────────────────────────────
    // CREATE MODE — focused wizard layout, no tabs
    // ─────────────────────────────────────────────────────────────────────────────
    if (isNew) {
        return (
            <div className={styles.main}>
                <div className={styles.container}>
                    <AdminPageHeader 
                        title="New Product"
                        subtitle="Add a new item to your beverage catalog"
                        actions={
                            <Button variant="outline" onClick={() => navigate('/admin/catalog')}>
                                <ChevronLeft size={16} style={{ marginRight: '8px' }} />
                                Back to Products
                            </Button>
                        }
                    />

                    <main className={styles.content}>
                        {error && <div className={styles.errorBanner}>⚠ {error}</div>}

                        <div className={styles.newProductGrid}>
                            <div className={styles.newProductMain}>
                                <div className={styles.newSectionLabel}>Product Info</div>
                                <div className={styles.form}>
                                    <Input
                                        label="Product Name *"
                                        value={form.name}
                                        onChange={e => setForm(p => ({ ...p, name: e.target.value }))}
                                        placeholder="e.g. Caramel Macchiato"
                                        required
                                        autoFocus
                                    />

                                    <div className={styles.formGroup}>
                                        <label className={styles.formLabel}>Description</label>
                                        <textarea
                                            className={styles.textarea}
                                            value={form.description}
                                            onChange={e => setForm(p => ({ ...p, description: e.target.value }))}
                                            placeholder="Describe your product — flavors, ingredients, etc."
                                            rows={4}
                                        />
                                    </div>

                                    <Select
                                        label="Category"
                                        value={form.categoryId}
                                        onChange={e => setForm(p => ({ ...p, categoryId: e.target.value }))}
                                    >
                                        <option value="">Select Category</option>
                                        {categories.map(c => <option key={c.id} value={c.id}>{c.name}</option>)}
                                    </Select>
                                </div>

                                <div className={styles.newVisibilityRow}>
                                    <div className={styles.formGroup}>
                                        <label className={styles.formLabel}>Visible on Store</label>
                                        <p className={styles.createHint}>Make this product available to customers immediately.</p>
                                    </div>
                                    <Toggle
                                        checked={form.active}
                                        onChange={val => setForm(p => ({ ...p, active: val }))}
                                    />
                                </div>
                            </div>

                            <div className={styles.newProductSide}>
                                <div className={styles.newSectionLabel}>Pricing &amp; Sizes</div>
                                <NewVariantInlineList
                                    variants={pendingVariants}
                                    onVariantsChange={setPendingVariants}
                                    draftForm={variantDraft}
                                    onDraftChange={setVariantDraft}
                                />

                                <div className={styles.newProductCta}>
                                    <Button
                                        className={styles.btnPrimary}
                                        onClick={handleSave}
                                        disabled={saveBusy || !form.name.trim() || (pendingVariants.length === 0 && !variantDraft.sizeName.trim())}
                                        style={{ width: '100%' }}
                                    >
                                        {saveBusy ? 'Creating…' : 'Create & Continue →'}
                                    </Button>
                                    <p className={styles.createHint} style={{ textAlign: 'center', marginTop: '0.75rem' }}>
                                        You can add images and more variants after saving.
                                    </p>
                                </div>
                            </div>
                        </div>
                    </main>
                </div>
            </div>
        );
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // EDIT MODE — existing tabbed layout
    // ─────────────────────────────────────────────────────────────────────────────
    return (
        <div className={styles.main}>
            <div className={styles.container}>
                <AdminPageHeader 
                    preTitle="PRODUCT MANAGER"
                    title={product?.productName || 'Product Detail'}
                    subtitle="Manage your beverage catalog and inventory"
                    actions={
                        <Button variant="outline" onClick={() => navigate('/admin/catalog')}>
                            <ChevronLeft size={16} style={{ marginRight: '8px' }} />
                            Back to List
                        </Button>
                    }
                />

                <nav className={styles.tabs}>
                    <button
                        className={`${styles.tab} ${activeTab === 'details' ? styles.tabActive : ''}`}
                        onClick={() => setActiveTab('details')}
                    >
                        Product Details
                    </button>
                    <button
                        className={`${styles.tab} ${activeTab === 'images' ? styles.tabActive : ''}`}
                        onClick={() => setActiveTab('images')}
                    >
                        Media Assets
                    </button>
                    <button
                        className={`${styles.tab} ${activeTab === 'variations' ? styles.tabActive : ''}`}
                        onClick={() => setActiveTab('variations')}
                    >
                        Pricing &amp; Variants
                    </button>
                </nav>

                <main className={styles.content}>
                    {error && <div className={styles.errorBanner}>⚠ {error}</div>}
                    {success && <div className={styles.successBanner}>✓ {success}</div>}

                    {/* Tab 1: Details */}
                    <div className={`${styles.tabPanel} ${activeTab === 'details' ? styles.tabPanelActive : ''}`}>
                        <div className={styles.tabGrid}>
                            <div className={styles.tabColMain}>
                                <div className={styles.form}>
                                    <Input
                                        label="Product Name"
                                        value={form.name}
                                        onChange={e => setForm(p => ({ ...p, name: e.target.value }))}
                                        required
                                        placeholder="e.g. Mocha Latte"
                                        disabled={!canEdit}
                                    />

                                    <div className={styles.formGroup}>
                                        <label className={styles.formLabel}>Description</label>
                                        <textarea
                                            className={styles.textarea}
                                            value={form.description}
                                            onChange={e => setForm(p => ({ ...p, description: e.target.value }))}
                                            placeholder="Add details about your product..."
                                            disabled={!canEdit}
                                        />
                                    </div>

                                    <Select
                                        label="Category"
                                        value={form.categoryId}
                                        onChange={e => setForm(p => ({ ...p, categoryId: e.target.value }))}
                                        disabled={!canEdit}
                                    >
                                        <option value="">Select Category</option>
                                        {categories.map(c => <option key={c.id} value={c.id}>{c.name}</option>)}
                                    </Select>
                                </div>
                            </div>

                            <div className={styles.tabColSide}>
                                <div className={styles.statusCard} style={{ flexDirection: 'column', alignItems: 'flex-start', gap: '1.5rem' }}>
                                    <div className={styles.formGroup}>
                                        <label className={styles.formLabel}>Current Visibility</label>
                                        <div style={{ marginTop: '0.25rem' }}>
                                            <Badge variant={isActiveVisibility ? 'success' : 'danger'}>
                                                {isActiveVisibility ? 'Live on Store' : 'Hidden'}
                                            </Badge>
                                        </div>
                                    </div>
                                    <div className={styles.formGroup}>
                                        <label className={styles.formLabel}>Active &amp; Visible</label>
                                        <Toggle
                                            checked={form.active}
                                            onChange={val => setForm(p => ({ ...p, active: val }))}
                                            disabled={!canEdit || togBusy}
                                        />
                                    </div>
                                </div>

                                <div className={styles.actionSidebar}>
                                    <Button
                                        className={styles.btnPrimary}
                                        onClick={handleSave}
                                        disabled={saveBusy}
                                        style={{ width: '100%' }}
                                    >
                                        {saveBusy ? 'Saving...' : 'Save Product Details'}
                                    </Button>

                                    <Button
                                        variant="danger"
                                        className={styles.btnGhostDanger}
                                        onClick={handleDelete}
                                        disabled={delBusy}
                                        style={{ width: '100%' }}
                                    >
                                        Delete Product
                                    </Button>
                                </div>
                            </div>
                        </div>
                    </div>

                    {/* Tab 2: Images */}
                    <div className={`${styles.tabPanel} ${activeTab === 'images' ? styles.tabPanelActive : ''}`}>
                        <ImagesPanel productId={productId} />
                        <p style={{ color: 'rgba(35, 31, 30, 0.6)', fontSize: '0.875rem', marginTop: '2rem', fontStyle: 'italic' }}>
                            💡 Tip: The image with the primary highlight will be shown first in your store pages.
                        </p>
                    </div>

                    {/* Tab 3: Variations */}
                    <div className={`${styles.tabPanel} ${activeTab === 'variations' ? styles.tabPanelActive : ''}`}>
                        <VariantsPanel productId={productId} />
                    </div>
                </main>
            </div>
        </div>
    );
}
