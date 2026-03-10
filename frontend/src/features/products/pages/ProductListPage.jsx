import { useState, useMemo, useEffect } from 'react';
import { useSearchParams, Link } from 'react-router-dom';
import Navbar from '@/layouts/Navbar';
import { useCart } from '@/features/orders/services/CartContext';
import { getProducts, getCategories } from '@/features/products/services/products.service';
import ProductCard from '@/features/products/components/ProductCard/ProductCard';
import heroImage from '@/assets/images/hero/espresso-machine-hero.png';
import styles from './ProductListPage.module.css';

const formatVND = (n) =>
    new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(n ?? 0);

const PAGE_SIZE = 12;

export default function ProductListPage() {
    const [searchParams, setSearchParams] = useSearchParams();
    const activeCategory = searchParams.get('category') || 'all';
    const [search, setSearch] = useState('');
    const [sort, setSort] = useState('default');
    const [page, setPage] = useState(1);
    const [addedIds, setAddedIds] = useState(new Set());
    const [quickAddProductId, setQuickAddProductId] = useState(null);
    const { addItem } = useCart();

    const [products, setProducts] = useState([]);
    const [categories, setCategories] = useState([]);
    const [totalPages, setTotalPages] = useState(1);
    const [totalElements, setTotalElements] = useState(0);
    const [loading, setLoading] = useState(false);

    const handleCategoryChange = (value) => {
        setPage(1);
        if (value === 'all') {
            setSearchParams({});
        } else {
            setSearchParams({ category: value });
        }
    };

    // Reset to page 1 whenever filter/search changes
    useEffect(() => { setPage(1); }, [activeCategory, search]);

    // Fetch categories once
    useEffect(() => {
        getCategories().then(setCategories).catch(() => { });
    }, []);

    // Fetch products when page/category/search changes
    useEffect(() => {
        setLoading(true);
        getProducts({
            page: page - 1,
            size: PAGE_SIZE,
            categoryId: activeCategory === 'all' ? undefined : activeCategory,
            search: search || undefined,
        })
            .then((data) => {
                setProducts(data.content);
                setTotalPages(data.totalPages);
                setTotalElements(data.totalElements);
            })
            .catch(() => {
                setProducts([]);
                setTotalPages(1);
                setTotalElements(0);
            })
            .finally(() => setLoading(false));
    }, [page, activeCategory, search]);

    const sorted = useMemo(() => {
        let list = [...products];
        if (sort === 'price-asc') list.sort((a, b) => a.price - b.price);
        if (sort === 'price-desc') list.sort((a, b) => b.price - a.price);
        if (sort === 'name-asc') list.sort((a, b) => a.productName.localeCompare(b.productName));
        if (sort === 'name-desc') list.sort((a, b) => b.productName.localeCompare(a.productName));
        return list;
    }, [products, sort]);

    const handleAdd = (product, variant) => {
        addItem(product, 1, variant);
        setQuickAddProductId(null);
        setAddedIds((prev) => new Set(prev).add(product.id));
        setTimeout(() => {
            setAddedIds((prev) => {
                const next = new Set(prev);
                next.delete(product.id);
                return next;
            });
        }, 1400);
    };

    // Build page number array with ellipsis
    const pageNumbers = useMemo(() => {
        if (totalPages <= 7) return Array.from({ length: totalPages }, (_, i) => i + 1);
        const pages = [];
        pages.push(1);
        if (page > 3) pages.push('…');
        for (let i = Math.max(2, page - 1); i <= Math.min(totalPages - 1, page + 1); i++) {
            pages.push(i);
        }
        if (page < totalPages - 2) pages.push('…');
        pages.push(totalPages);
        return pages;
    }, [page, totalPages]);

    return (
        <>
            <Navbar />

            <div className={styles.page}>
                {/* ── Page Header ── */}
                <header className={styles.pageHeader}>
                    <div className={styles.headerInner + ' ' + styles.headerFlex}>
                        <div className={styles.headerText}>
                            <p className={styles.headerEyebrow}>Our Menu</p>
                            <h1 className={styles.pageTitle}>
                                Crafted<br />with Purpose
                            </h1>
                            <p className={styles.pageSubtitle}>
                                Every cup sourced from single-origin farms.<br />
                                Every sip a story worth telling.
                            </p>
                        </div>
                    </div>
                    <div className={styles.headerImageWrap}>
                        <img
                            src={heroImage}
                            alt="Professional espresso coffee machine pulling a dark shot of coffee"
                            className={styles.headerImage}
                            loading="lazy"
                        />
                    </div>
                </header>

                {/* ── One-line Toolbar ── */}
                <div className={styles.filterWrap}>
                    <div className={styles.filterInner}>

                        {/* Search */}
                        <div className={styles.searchWrap}>
                            <svg className={styles.searchIcon} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                                <circle cx="11" cy="11" r="8" />
                                <line x1="21" y1="21" x2="16.65" y2="16.65" />
                            </svg>
                            <input
                                type="text"
                                placeholder="Search drinks…"
                                value={search}
                                onChange={(e) => setSearch(e.target.value)}
                                className={styles.searchInput}
                            />
                            {search && (
                                <button className={styles.searchClear} onClick={() => setSearch('')} aria-label="Clear search">×</button>
                            )}
                        </div>

                        {/* Right-side controls */}
                        <div className={styles.controls}>

                            {/* Category */}
                            <div className={styles.selectWrap}>
                                <svg className={styles.selectIcon} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                                    <path d="M4 6h16M7 12h10M10 18h4" />
                                </svg>
                                <select
                                    className={styles.select}
                                    value={activeCategory}
                                    onChange={(e) => handleCategoryChange(e.target.value)}
                                    aria-label="Filter by category"
                                >
                                    <option value="all">All categories</option>
                                    {categories.map((cat) => (
                                        <option key={cat.id} value={cat.id}>{cat.name}</option>
                                    ))}
                                </select>
                                <svg className={styles.chevron} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
                                    <polyline points="6 9 12 15 18 9" />
                                </svg>
                            </div>

                            {/* Sort */}
                            <div className={styles.selectWrap}>
                                <svg className={styles.selectIcon} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                                    <path d="M3 6h18M6 12h12M10 18h4" />
                                </svg>
                                <select
                                    className={styles.select}
                                    value={sort}
                                    onChange={(e) => setSort(e.target.value)}
                                    aria-label="Sort products"
                                >
                                    <option value="default">Sort: Default</option>
                                    <option value="price-asc">Price: Low to High</option>
                                    <option value="price-desc">Price: High to Low</option>
                                    <option value="name-asc">Name: A → Z</option>
                                    <option value="name-desc">Name: Z → A</option>
                                </select>
                                <svg className={styles.chevron} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
                                    <polyline points="6 9 12 15 18 9" />
                                </svg>
                            </div>

                            {/* Results pill */}
                            <span className={styles.resultsPill}>
                                {totalElements} {totalElements === 1 ? 'item' : 'items'}
                            </span>

                            {/* Clear all (only when filters are active) */}
                            {(search || activeCategory !== 'all' || sort !== 'default') && (
                                <button
                                    className={styles.clearAll}
                                    onClick={() => { setSearch(''); setSearchParams({}); setSort('default'); }}
                                    aria-label="Clear all filters"
                                >
                                    Clear
                                </button>
                            )}
                        </div>
                    </div>
                </div>

                {/* ── Product Grid ── */}
                <div className={styles.gridWrap}>
                    {loading ? (
                        <div className={styles.empty}><span className={styles.emptyIcon}>☕</span><p>Loading…</p></div>
                    ) : sorted.length === 0 ? (
                        <div className={styles.empty}>
                            <span className={styles.emptyIcon}>☕</span>
                            <p>No drinks match your search.</p>
                            <button className={styles.emptyReset} onClick={() => { setSearch(''); setSearchParams({}); }}>
                                Clear filters
                            </button>
                        </div>
                    ) : (
                        <div className={styles.grid}>
                            {sorted.map((product) => (
                                <ProductCard
                                    key={product.id}
                                    product={product}
                                    isAdded={addedIds.has(product.id)}
                                    quickAddProductId={quickAddProductId}
                                    setQuickAddProductId={setQuickAddProductId}
                                    onAddVariant={(variant) => handleAdd(product, variant)}
                                />
                            ))}
                        </div>
                    )}
                </div>

                {/* ── Pagination ── */}
                {totalPages > 1 && (
                    <div className={styles.pagination}>
                        <button
                            className={`${styles.pageBtn} ${styles.pageBtnArrow}`}
                            onClick={() => setPage((p) => Math.max(1, p - 1))}
                            disabled={page === 1}
                            aria-label="Previous page"
                        >
                            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                                <polyline points="15 18 9 12 15 6" />
                            </svg>
                        </button>

                        {pageNumbers.map((n, i) =>
                            n === '…' ? (
                                <span key={`ellipsis-${i}`} className={styles.pageEllipsis}>…</span>
                            ) : (
                                <button
                                    key={n}
                                    className={`${styles.pageBtn} ${page === n ? styles.pageBtnActive : ''}`}
                                    onClick={() => setPage(n)}
                                >
                                    {n}
                                </button>
                            )
                        )}

                        <button
                            className={`${styles.pageBtn} ${styles.pageBtnArrow}`}
                            onClick={() => setPage((p) => Math.min(totalPages, p + 1))}
                            disabled={page === totalPages}
                            aria-label="Next page"
                        >
                            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                                <polyline points="9 18 15 12 9 6" />
                            </svg>
                        </button>
                    </div>
                )}
            </div>
        </>
    );
}
