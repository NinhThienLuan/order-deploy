import { useEffect, useState } from 'react';
import AdminHeader from '../components/AdminHeader';
import { useAuth } from '@/features/auth/AuthContext';
import { getAdminCategories, createCategory, updateCategory, deleteCategory } from '@/features/categories/categories.service';
import styles from './AdminCategoryPage.module.css';

const EMPTY_FORM = { id: '', name: '', description: '' };

// ── Icons ──────────────────────────────────────────────────────────────────────
const IconEdit = () => (
    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
        <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7" />
        <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z" />
    </svg>
);
const IconTrash = () => (
    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
        <polyline points="3 6 5 6 21 6" /><path d="M19 6l-1 14a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2L5 6" />
        <path d="M10 11v6" /><path d="M14 11v6" />
        <path d="M9 6V4a1 1 0 0 1 1-1h4a1 1 0 0 1 1 1v2" />
    </svg>
);
const IconClose = () => (
    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round">
        <line x1="18" y1="6" x2="6" y2="18" /><line x1="6" y1="6" x2="18" y2="18" />
    </svg>
);
const IconTag = () => (
    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
        <path d="M20.59 13.41l-7.17 7.17a2 2 0 0 1-2.83 0L2 12V2h10l8.59 8.59a2 2 0 0 1 0 2.82z" />
        <line x1="7" y1="7" x2="7.01" y2="7" />
    </svg>
);

// ──────────────────────────────────────────────────────────────────────────────

export default function AdminCategoryPage() {
    const { user } = useAuth();
    const role = user?.role ?? 'FRANCHISE_ADMIN';
    const canEdit = role === 'FRANCHISE_ADMIN';

    const [categories, setCategories] = useState([]);
    const [loading, setLoading] = useState(true);
    const [submitting, setSubmitting] = useState(false);
    const [error, setError] = useState('');
    const [success, setSuccess] = useState('');

    const [isModalOpen, setIsModalOpen] = useState(false);
    const [modalMode, setModalMode] = useState('create');
    const [form, setForm] = useState(EMPTY_FORM);

    // ── Load ─────────────────────────────────────────────────────────────────────

    const load = async () => {
        setLoading(true); setError('');
        try { setCategories(await getAdminCategories()); }
        catch { setError('Failed to load categories.'); }
        finally { setLoading(false); }
    };

    useEffect(() => { load(); }, []);

    // ── Modal helpers ─────────────────────────────────────────────────────────────

    const openCreate = () => {
        setModalMode('create'); setForm(EMPTY_FORM);
        setError(''); setSuccess(''); setIsModalOpen(true);
    };

    const openEdit = (cat) => {
        setModalMode('edit');
        setForm({ id: cat.id, name: cat.name, description: cat.description || '' });
        setError(''); setSuccess(''); setIsModalOpen(true);
    };

    const closeModal = () => { setIsModalOpen(false); setForm(EMPTY_FORM); };

    // ── CRUD ──────────────────────────────────────────────────────────────────────

    const handleSubmit = async (e) => {
        e.preventDefault();
        setSubmitting(true); setError(''); setSuccess('');
        const payload = { name: form.name.trim(), description: form.description.trim() };
        try {
            if (modalMode === 'create') {
                await createCategory(payload);
                setSuccess('Category created successfully.');
            } else {
                await updateCategory(form.id, payload);
                setSuccess('Category updated successfully.');
            }
            closeModal();
            await load();
        } catch {
            setError(`${modalMode === 'create' ? 'Create' : 'Update'} failed. Please try again.`);
        } finally { setSubmitting(false); }
    };

    const handleDelete = async (cat) => {
        if (!window.confirm(`Delete category "${cat.name}"? Products in this category may be affected.`)) return;
        setSubmitting(true); setError(''); setSuccess('');
        try {
            await deleteCategory(cat.id);
            setSuccess(`Category "${cat.name}" deleted.`);
            await load();
        } catch {
            setError('Delete failed. This category may have products attached to it.');
        } finally { setSubmitting(false); }
    };

    // ── Render ────────────────────────────────────────────────────────────────────

    return (
        <div className={styles.main}>
            <AdminHeader breadcrumbs={['Franchise', 'Categories']} />

            <section className={styles.container}>

                {/* ─ Page Header ─ */}
                <div className={styles.topRow}>
                    <div className={styles.pageMeta}>
                        <p className={styles.pageEyebrow}>Catalog Management</p>
                        <h1 className={styles.pageTitle}>Categories</h1>
                        <p className={styles.subTitle}>Organise your product catalog into clear, browsable categories.</p>
                    </div>
                    {canEdit && (
                        <button className={styles.primaryBtn} onClick={openCreate}>
                            + New Category
                        </button>
                    )}
                </div>

                {/* ─ Alerts ─ */}
                {error && <div className={styles.errorBanner}>⚠ {error}</div>}
                {success && <div className={styles.successBanner}>✓ {success}</div>}

                {/* ─ Category Panel ─ */}
                <div className={styles.panel}>
                    <div className={styles.panelHeader}>
                        <span className={styles.panelTitle}>All Categories</span>
                        <span className={styles.categoryCount}>{categories.length} total</span>
                    </div>

                    {loading ? (
                        <div className={styles.loading}>Loading categories…</div>
                    ) : categories.length === 0 ? (
                        <div className={styles.empty}>No categories found. Create one to get started.</div>
                    ) : (
                        <div className={styles.categoryList}>
                            {categories.map(cat => (
                                <div key={cat.id} className={styles.categoryItem}>
                                    <div className={styles.catIcon}><IconTag /></div>
                                    <div className={styles.catInfo}>
                                        <div className={styles.catName}>{cat.name}</div>
                                        {cat.description && (
                                            <div className={styles.catDesc}>{cat.description}</div>
                                        )}
                                    </div>
                                    {canEdit && (
                                        <div className={styles.catActions}>
                                            <button
                                                className={styles.iconBtn}
                                                title="Edit category"
                                                onClick={() => openEdit(cat)}
                                            >
                                                <IconEdit />
                                            </button>
                                            <button
                                                className={`${styles.iconBtn} ${styles.danger}`}
                                                title="Delete category"
                                                onClick={() => handleDelete(cat)}
                                                disabled={submitting}
                                            >
                                                <IconTrash />
                                            </button>
                                        </div>
                                    )}
                                </div>
                            ))}
                        </div>
                    )}
                </div>
            </section>

            {/* ─ Create / Edit Modal ─ */}
            {isModalOpen && (
                <div className={styles.modalOverlay} onClick={closeModal}>
                    <div className={styles.modal} onClick={e => e.stopPropagation()}>
                        <div className={styles.modalHeader}>
                            <div>
                                <p className={styles.modalEyebrow}>{modalMode === 'create' ? 'New Category' : 'Editing'}</p>
                                <h2 className={styles.modalTitle}>
                                    {modalMode === 'create' ? 'Create Category' : `Edit: ${form.name}`}
                                </h2>
                            </div>
                            <button className={styles.modalCloseBtn} onClick={closeModal} aria-label="Close">
                                <IconClose />
                            </button>
                        </div>

                        <div className={styles.modalBody}>
                            <form className={styles.form} id="category-form" onSubmit={handleSubmit}>
                                <div className={styles.formGroup}>
                                    <label className={styles.formLabel}>Category Name *</label>
                                    <input
                                        className={styles.input}
                                        placeholder="e.g. Beverages, Desserts, Savoury..."
                                        value={form.name}
                                        onChange={e => setForm(p => ({ ...p, name: e.target.value }))}
                                        required
                                    />
                                </div>
                                <div className={styles.formGroup}>
                                    <label className={styles.formLabel}>Description</label>
                                    <textarea
                                        className={styles.textarea}
                                        placeholder="Optional short description of this category..."
                                        value={form.description}
                                        onChange={e => setForm(p => ({ ...p, description: e.target.value }))}
                                        rows={3}
                                    />
                                </div>
                            </form>
                        </div>

                        <div className={styles.modalFooter}>
                            <button type="button" className={styles.lightBtn} onClick={closeModal}>Cancel</button>
                            <button type="submit" form="category-form" className={styles.primaryBtn} disabled={submitting}>
                                {submitting ? 'Saving…' : modalMode === 'create' ? 'Create Category' : 'Save Changes'}
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
}
