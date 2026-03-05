import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import AdminHeader from '../components/AdminHeader';
import { useAuth } from '@/features/auth/AuthContext';
import { getAdminProducts, createAdminProduct } from '../services/adminProducts.service';
import { getCategories } from '@/features/products/products.service';
import styles from './AdminProductCrudPage.module.css';

const PAGE_SIZE = 10;
const EMPTY_FORM = { name: '', description: '', categoryId: '', type: '', active: true };

const IconClose = () => (
  <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round">
    <line x1="18" y1="6" x2="6" y2="18" /><line x1="6" y1="6" x2="18" y2="18" />
  </svg>
);

export default function AdminProductCrudPage() {
  const navigate = useNavigate();
  const { user } = useAuth();
  const role = user?.role ?? 'FRANCHISE_ADMIN';

  const [products, setProducts] = useState([]);
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);

  const [searchInput, setSearchInput] = useState('');
  const [search, setSearch] = useState('');
  const [categoryFilter, setCategoryFilter] = useState('');
  const [typeFilter, setTypeFilter] = useState('');

  // Manage button → navigate to detail page
  const [showCreate, setShowCreate] = useState(false);
  const [createForm, setCreateForm] = useState(EMPTY_FORM);

  const breadcrumbRoot = role === 'STORE_MANAGER' ? 'Store' : 'Franchise';

  // ── Load ─────────────────────────────────────────────────────────────────────

  const loadProducts = async ({ nextPage = page, nextSearch = search, nextCategory = categoryFilter, nextType = typeFilter } = {}) => {
    setLoading(true); setError('');
    try {
      const data = await getAdminProducts({
        page: nextPage, size: PAGE_SIZE,
        search: nextSearch || undefined,
        categoryId: nextCategory || undefined,
        type: nextType || undefined,
      });
      setProducts(data.content || []);
      setTotalPages(Math.max(data.totalPages || 1, 1));
      setPage(data.page || 0);
    } catch { setError('Cannot load products.'); }
    finally { setLoading(false); }
  };

  useEffect(() => { loadProducts({ nextPage: 0 }); }, []);
  useEffect(() => { getCategories().then(d => setCategories(d || [])).catch(() => { }); }, []);

  // ── Filter handlers ───────────────────────────────────────────────────────────

  const applyFilter = () => {
    setSearch(searchInput.trim());
    loadProducts({ nextPage: 0, nextSearch: searchInput.trim(), nextCategory: categoryFilter, nextType: typeFilter });
  };
  const resetFilter = () => {
    setSearchInput(''); setSearch(''); setCategoryFilter(''); setTypeFilter('');
    loadProducts({ nextPage: 0, nextSearch: '', nextCategory: '', nextType: '' });
  };

  // ── Create product ────────────────────────────────────────────────────────────

  const handleCreate = async (e) => {
    e.preventDefault(); setSubmitting(true); setError(''); setSuccess('');
    try {
      await createAdminProduct({
        name: createForm.name.trim(),
        description: createForm.description.trim(),
        categoryId: createForm.categoryId || null,
        type: createForm.type || null,
        active: Boolean(createForm.active),
      });
      setSuccess('Product created.'); setShowCreate(false); setCreateForm(EMPTY_FORM);
      await loadProducts();
    } catch { setError('Create failed.'); }
    finally { setSubmitting(false); }
  };

  const formatPrice = (p) =>
    new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(p ?? 0);

  return (
    <div className={styles.main}>
      <AdminHeader breadcrumbs={[breadcrumbRoot, 'Products']} />

      <div className={styles.container}>

        {/* ─ Title Row ─ */}
        <div className={styles.topRow}>
          <h1 className={styles.pageTitle}>Product Management</h1>
          {role === 'FRANCHISE_ADMIN' && (
            <button className={styles.primaryBtn} onClick={() => { setShowCreate(true); setCreateForm(EMPTY_FORM); }}>
              + New Product
            </button>
          )}
        </div>

        {/* ─ Filter Bar ─ */}
        <div className={styles.filterBar}>
          <input
            className={styles.searchInput}
            placeholder="Search by name..."
            value={searchInput}
            onChange={e => setSearchInput(e.target.value)}
            onKeyDown={e => e.key === 'Enter' && applyFilter()}
          />
          <select className={styles.statusSelect} value={categoryFilter} onChange={e => setCategoryFilter(e.target.value)}>
            <option value="">All Categories</option>
            {categories.map(c => <option key={c.id} value={c.id}>{c.name}</option>)}
          </select>
          <select className={styles.statusSelect} value={typeFilter} onChange={e => setTypeFilter(e.target.value)} style={{ minWidth: 120 }}>
            <option value="">All Types</option>
            <option value="MASTER">Master</option>
            <option value="SIGNATURE">Signature</option>
          </select>
          <button className={styles.filterBtn} onClick={applyFilter}>Filter</button>
          <button className={styles.resetBtn} onClick={resetFilter}>Reset</button>
        </div>

        {/* ─ Alerts ─ */}
        {error && <div className={styles.errorBanner}>⚠ {error}</div>}
        {success && <div className={styles.successBanner}>✓ {success}</div>}

        {/* ─ Table ─ */}
        {loading ? (
          <div className={styles.loading}>Loading products...</div>
        ) : (
          <div className={styles.tableContainer}>
            <div className={styles.tableHeader}>
              <div>Image</div>
              <div>Name</div>
              <div>Category</div>
              <div>Type</div>
              <div>Price</div>
              <div>Status</div>
              <div>Action</div>
            </div>

            {products.length === 0 ? (
              <div className={styles.noItems}>No products found.</div>
            ) : products.map(p => (
              <div key={p.id} className={styles.tableRow}>
                <div>
                  <img
                    src={p.imageUrl || 'https://placehold.co/42x42/EFECE3/231F1E?text=—'}
                    alt="" className={styles.thumb}
                  />
                </div>
                <div className={styles.productName}>{p.productName}</div>
                <div className={styles.cellMuted}>{p.categoryName || '—'}</div>
                <div>
                  {p.type ? <span className={styles.typeBadge}>{p.type}</span> : <span className={styles.cellMuted}>—</span>}
                </div>
                <div className={styles.productName}>{formatPrice(p.price)}</div>
                <div>
                  <span className={`${styles.badge} ${p.status === 'Available' ? styles.active : styles.inactive}`}>
                    {p.status}
                  </span>
                </div>
                {/* ONE action button — navigates to detail page */}
                <div>
                  <button className={styles.manageBtn} onClick={() => navigate(`/admin/catalog/${p.id}`)}>Manage →</button>
                </div>
              </div>
            ))}

            <div className={styles.pagination}>
              <span className={styles.pageInfo}>Page {page + 1} / {totalPages}</span>
              <div className={styles.paginationBtns}>
                <button className={styles.resetBtn} onClick={() => loadProducts({ nextPage: page - 1 })} disabled={page <= 0}>← Prev</button>
                <button className={styles.filterBtn} onClick={() => loadProducts({ nextPage: page + 1 })} disabled={page + 1 >= totalPages}>Next →</button>
              </div>
            </div>
          </div>
        )}
      </div>



      {/* ─ Quick Create Modal ─ */}
      {showCreate && (
        <div className={styles.modalOverlay} onClick={() => setShowCreate(false)}>
          <div className={styles.modal} onClick={e => e.stopPropagation()}>
            <div className={styles.modalHeader}>
              <h2 className={styles.modalTitle}>New Product</h2>
              <button className={styles.modalCloseBtn} onClick={() => setShowCreate(false)}><IconClose /></button>
            </div>
            <div className={styles.modalBody}>
              <form className={styles.form} id="create-form" onSubmit={handleCreate}>
                <div className={styles.formGroup}>
                  <label className={styles.formLabel}>Product Name *</label>
                  <input className={styles.input} value={createForm.name} onChange={e => setCreateForm(p => ({ ...p, name: e.target.value }))} required placeholder="e.g. Classic Latte" />
                </div>
                <div className={styles.formGroup}>
                  <label className={styles.formLabel}>Description</label>
                  <textarea className={styles.textarea} rows={2} value={createForm.description} onChange={e => setCreateForm(p => ({ ...p, description: e.target.value }))} placeholder="Short description..." />
                </div>
                <div className={styles.formRow}>
                  <div className={styles.formGroup}>
                    <label className={styles.formLabel}>Category</label>
                    <select className={styles.select} value={createForm.categoryId} onChange={e => setCreateForm(p => ({ ...p, categoryId: e.target.value }))}>
                      <option value="">Select…</option>
                      {categories.map(c => <option key={c.id} value={c.id}>{c.name}</option>)}
                    </select>
                  </div>
                  <div className={styles.formGroup}>
                    <label className={styles.formLabel}>Type</label>
                    <select className={styles.select} value={createForm.type} onChange={e => setCreateForm(p => ({ ...p, type: e.target.value }))}>
                      <option value="">None</option>
                      <option value="MASTER">Master</option>
                      <option value="SIGNATURE">Signature</option>
                    </select>
                  </div>
                </div>
                <label className={styles.checkboxRow}>
                  <input type="checkbox" checked={createForm.active} onChange={e => setCreateForm(p => ({ ...p, active: e.target.checked }))} />
                  <span>Active on creation</span>
                </label>
              </form>
            </div>
            <div className={styles.modalFooter}>
              <button className={styles.resetBtn} type="button" onClick={() => setShowCreate(false)}>Cancel</button>
              <button className={styles.filterBtn} type="submit" form="create-form" disabled={submitting}>{submitting ? 'Creating…' : 'Create'}</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
