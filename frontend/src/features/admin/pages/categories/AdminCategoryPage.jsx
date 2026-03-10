import { useEffect, useState } from 'react';
import { Pencil, Plus } from 'lucide-react';
import { useAuth } from '@/features/authentication/services/AuthContext';
import { getAdminCategories, createCategory, updateCategory, deleteCategory } from '@/features/products/services/categories.service';
import Button from '@/components/ui/Button';
import Input from '@/components/ui/Input';
import Card from '@/components/ui/Card';
import AdminPageHeader from '@/components/layout/AdminPageHeader';
import AdminFilterBar from '@/components/ui/AdminFilterBar';
import AdminTable from '@/components/ui/AdminTable';
import styles from './AdminCategoryPage.module.css';

const EMPTY_FORM = { id: '', name: '', description: '' };

const IconClose = () => (
    <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round">
        <line x1="18" y1="6" x2="6" y2="18" /><line x1="6" y1="6" x2="18" y2="18" />
    </svg>
);

export default function AdminCategoryPage() {
    const { user } = useAuth();
    const role = user?.role ?? 'FRANCHISE_ADMIN';
    const canEdit = role === 'FRANCHISE_ADMIN';

    const [categories, setCategories] = useState([]);
    const [loading, setLoading] = useState(true);
    const [submitting, setSubmitting] = useState(false);
    const [error, setError] = useState('');
    const [success, setSuccess] = useState('');

    const [searchInput, setSearchInput] = useState('');

    const [isModalOpen, setIsModalOpen] = useState(false);
    const [modalMode, setModalMode] = useState('create');
    const [form, setForm] = useState(EMPTY_FORM);

    const load = async () => {
        setLoading(true); setError('');
        try { 
            const data = await getAdminCategories();
            setCategories(data || []);
        }
        catch { setError('Failed to load categories.'); }
        finally { setLoading(false); }
    };

    useEffect(() => { load(); }, []);

    const filteredCategories = categories.filter(c => 
        searchInput.trim() ? c.name.toLowerCase().includes(searchInput.trim().toLowerCase()) : true
    );

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

    const tableHeaders = [
        "Category",
        <div className={styles.actionsCol}>Actions</div>
    ];

    return (
        <div className={styles.main}>
            <div className={styles.container}>
                <AdminPageHeader 
                    preTitle="CATALOG MANAGEMENT"
                    title="Categories Management"
                    subtitle="Organize your product catalog into clear, browsable categories."
                    actions={
                        canEdit && (
                            <Button
                                variant="primary"
                                onClick={openCreate}
                            >
                                <Plus size={18} style={{ marginRight: '8px' }} />
                                Add Category
                            </Button>
                        )
                    }
                />

                <AdminFilterBar>
                    <div className={styles.searchBox}>
                        <Input
                            placeholder="Search by name..."
                            value={searchInput}
                            onChange={e => setSearchInput(e.target.value)}
                            className={styles.searchInput}
                        />
                    </div>
                </AdminFilterBar>

                {error && <div className={styles.errorBanner}>⚠ {error}</div>}
                {success && <div className={styles.successBanner}>✓ {success}</div>}

                <div className={styles.tableWrap}>
                    <AdminTable
                        headers={tableHeaders}
                        gridTemplate="1fr 100px"
                        loading={loading}
                        noData={filteredCategories.length === 0}
                        noDataText="No categories found."
                    >
                        {filteredCategories.map(cat => (
                            <div key={cat.id} className={styles.tableRow}>
                                <div className={styles.productCell}>
                                    <div className={styles.productInfo}>
                                        <div className={styles.productName}>{cat.name}</div>
                                        {cat.description && (
                                            <div className={styles.productMeta}>
                                                <span className={styles.categoryName}>{cat.description}</span>
                                            </div>
                                        )}
                                    </div>
                                </div>
                                <div className={styles.actionsCol}>
                                    {canEdit && (
                                        <Button
                                            variant="ghost"
                                            size="sm"
                                            className={styles.iconBtn}
                                            onClick={() => openEdit(cat)}
                                            title="Edit Category"
                                        >
                                            <Pencil size={16} />
                                        </Button>
                                    )}
                                </div>
                            </div>
                        ))}
                    </AdminTable>
                </div>
            </div>

            {/* ─ Create / Edit Modal ─ */}
            {isModalOpen && (
                <div className={styles.modalOverlay} onClick={closeModal}>
                    <Card className={styles.modal} padding={false} onClick={e => e.stopPropagation()}>
                        <div className={styles.modalHeader}>
                            <h2 className={styles.modalTitle}>{modalMode === 'create' ? 'New Category' : 'Edit Category'}</h2>
                            <button className={styles.modalCloseBtn} onClick={closeModal}><IconClose /></button>
                        </div>
                        <div className={styles.modalBody}>
                            <form className={styles.form} id="category-form" onSubmit={handleSubmit}>
                                <Input
                                    label="Category Name *"
                                    value={form.name}
                                    onChange={e => setForm(p => ({ ...p, name: e.target.value }))}
                                    required
                                    placeholder="e.g. Beverages, Desserts"
                                />
                                <div className={styles.formGroup}>
                                    <label className={styles.formLabel}>Description</label>
                                    <textarea
                                        className={styles.textarea}
                                        rows={3}
                                        value={form.description}
                                        onChange={e => setForm(p => ({ ...p, description: e.target.value }))}
                                        placeholder="Optional short description..."
                                    />
                                </div>
                            </form>
                        </div>
                        <div className={styles.modalFooter}>
                            <Button variant="ghost" onClick={closeModal}>Cancel</Button>
                            <Button variant="primary" type="submit" form="category-form" disabled={submitting}>
                                {submitting ? 'Saving…' : modalMode === 'create' ? 'Create' : 'Save'}
                            </Button>
                        </div>
                    </Card>
                </div>
            )}
        </div>
    );
}
