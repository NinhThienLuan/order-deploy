import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Pencil, Plus, ChevronLeft, ChevronRight } from 'lucide-react';
import { useAuth } from '@/features/authentication/services/AuthContext';
import { getAdminProducts, deleteAdminProduct, toggleAdminProductActive } from '@/features/admin/services/products/adminProducts.service';
import { getCategories } from '@/features/products/services/products.service';
import Button from '@/components/ui/Button';
import Input from '@/components/ui/Input';
import Select from '@/components/ui/Select';
import Toggle from '@/components/ui/Toggle';
import AdminPageHeader from '@/components/layout/AdminPageHeader';
import AdminFilterBar from '@/components/ui/AdminFilterBar';
import AdminTable from '@/components/ui/AdminTable';
import styles from './AdminProductCrudPage.module.css';

const PAGE_SIZE = 10;

export default function AdminProductCrudPage() {
  const navigate = useNavigate();
  const { user } = useAuth();
  const role = user?.role ?? 'FRANCHISE_ADMIN';

  const [products, setProducts] = useState([]);
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);

  const [searchInput, setSearchInput] = useState('');
  const [search, setSearch] = useState('');
  const [categoryFilter, setCategoryFilter] = useState('');
  const [statusFilter, setStatusFilter] = useState('');

  const [rowBusy, setRowBusy] = useState({});

  const loadProducts = async ({ nextPage = page, nextSearch = search, nextCategory = categoryFilter, nextStatus = statusFilter, silent = false } = {}) => {
    if (!silent) setLoading(true); 
    setError('');
    try {
      const data = await getAdminProducts({
        page: nextPage, size: PAGE_SIZE,
        search: nextSearch || undefined,
        categoryId: nextCategory || undefined,
        active: nextStatus === '' ? undefined : nextStatus === 'active',
      });
      setProducts(data.content || []);
      setTotalPages(Math.max(data.totalPages || 1, 1));
      setPage(data.page || 0);
    } catch { 
      if (!silent) setError('Cannot load products.'); 
    }
    finally { 
      if (!silent) setLoading(false); 
    }
  };

  useEffect(() => { getCategories().then(d => setCategories(d || [])).catch(() => { }); }, []);

  // ── Auto-Filter handlers ────────────────────────────────────────────────────────
  useEffect(() => {
    const delayDebounceFn = setTimeout(() => {
      setSearch(searchInput.trim());
      loadProducts({ 
        nextPage: 0, 
        nextSearch: searchInput.trim(), 
        nextCategory: categoryFilter, 
        nextStatus: statusFilter 
      });
    }, 300);

    return () => clearTimeout(delayDebounceFn);
  }, [searchInput, categoryFilter, statusFilter]);

  // ── Toggle active ─────────────────────────────────────────────────────────────
  const handleToggle = async (id) => {
    // Optimistic Update
    setProducts(prev => prev.map(p => 
      p.id === id ? { ...p, status: p.status === 'Available' ? 'Inactive' : 'Available' } : p
    ));
    setRowBusy(p => ({ ...p, [id]: true }));
    
    try { 
      await toggleAdminProductActive(id); 
      await loadProducts({ silent: true }); 
    }
    catch { 
      setError('Toggle failed.'); 
      await loadProducts({ silent: true });
    }
    finally { 
      setRowBusy(p => ({ ...p, [id]: false })); 
    }
  };

  const formatPrice = (p) =>
    new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(p ?? 0);

  const tableHeaders = [
    "Product",
    <div className={styles.centerCol}>Price</div>,
    <div className={styles.centerCol}>Status</div>,
    <div className={styles.actionsCol}>Actions</div>
  ];

  const paginationUI = (
    <>
      <span className={styles.pageInfo}>Page {page + 1} of {totalPages}</span>
      <div className={styles.paginationBtns}>
        <Button 
          variant="ghost" 
          size="sm" 
          onClick={() => loadProducts({ nextPage: page - 1 })} 
          disabled={page <= 0}
          title="Previous Page"
        >
          <ChevronLeft size={16} style={{ marginRight: '4px' }} />
          Prev
        </Button>
        <Button 
          variant="ghost" 
          size="sm" 
          onClick={() => loadProducts({ nextPage: page + 1 })} 
          disabled={page + 1 >= totalPages}
          title="Next Page"
        >
          Next
          <ChevronRight size={16} style={{ marginLeft: '4px' }} />
        </Button>
      </div>
    </>
  );

  return (
    <div className={styles.main}>
      <div className={styles.container}>
        <AdminPageHeader 
          preTitle="CATALOG MANAGEMENT"
          title="Product Management"
          subtitle="Manage your product catalog, pricing, and availability in one place."
          actions={
            role === 'FRANCHISE_ADMIN' && (
              <Button 
                variant="primary" 
                onClick={() => navigate('/admin/catalog/new')}
              >
                <Plus size={18} style={{ marginRight: '8px' }} />
                Add Product
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
          <Select 
            value={categoryFilter} 
            onChange={e => setCategoryFilter(e.target.value)}
            className={styles.filterSelect}
          >
            <option value="">All Categories</option>
            {categories.map(c => <option key={c.id} value={c.id}>{c.name}</option>)}
          </Select>
          <Select
            value={statusFilter}
            onChange={e => setStatusFilter(e.target.value)}
            className={styles.filterSelect}
          >
            <option value="">All Statuses</option>
            <option value="active">Active</option>
            <option value="inactive">Inactive</option>
          </Select>
        </AdminFilterBar>

        {error && <div className={styles.errorBanner}>⚠ {error}</div>}
        {success && <div className={styles.successBanner}>✓ {success}</div>}

        <div className={styles.tableWrap}>
          <AdminTable
            headers={tableHeaders}
            gridTemplate="1fr 140px 100px 100px"
            loading={loading}
            noData={products.length === 0}
            noDataText="No products found."
            pagination={paginationUI}
          >
            {products.map(p => (
              <div key={p.id} className={styles.tableRow}>
                <div className={styles.productCell}>
                  <img
                    src={p.imageUrl || 'https://placehold.co/52x52/EFECE3/231F1E?text=—'}
                    alt="" className={styles.thumb}
                  />
                  <div className={styles.productInfo}>
                    <div className={styles.productName}>{p.productName}</div>
                    <div className={styles.productMeta}>
                      <span className={styles.categoryName}>{p.categoryName || 'No Category'}</span>
                    </div>
                  </div>
                </div>
                <div className={`${styles.priceCell} ${styles.centerCol}`}>{formatPrice(p.price)}</div>
                <div className={styles.centerCol}>
                  <Toggle
                    checked={p.status === 'Available'}
                    onChange={() => handleToggle(p.id)}
                    disabled={!!rowBusy[p.id]}
                  />
                </div>
                <div className={styles.actionsCol}>
                  <Button 
                    variant="ghost" 
                    size="sm" 
                    className={styles.iconBtn}
                    onClick={() => navigate(`/admin/catalog/${p.id}`)}
                    title="Edit Product"
                  >
                    <Pencil size={16} />
                  </Button>
                </div>
              </div>
            ))}
          </AdminTable>
        </div>
      </div>
    </div>
  );
}
