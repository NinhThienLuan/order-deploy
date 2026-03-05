import { useState, useEffect } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import Navbar from '../../../layouts/Navbar';
import Footer from '../../../layouts/Footer';
import { useCart } from '../../cart/CartContext';
import { getProductById, getProducts } from '../products.service';
import styles from './ProductDetailPage.module.css';

const formatVND = (n) =>
    new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(n ?? 0);

export default function ProductDetailPage() {
    const { id } = useParams();
    const navigate = useNavigate();
    const { addItem, items } = useCart();
    const [qty, setQty] = useState(1);

    const [product, setProduct] = useState(null);
    const [related, setRelated] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        setLoading(true);
        setProduct(null);
        setRelated([]);
        getProductById(id)
            .then((p) => {
                setProduct(p);
                if (p.categoryId) {
                    getProducts({ categoryId: p.categoryId, size: 5 })
                        .then((data) => setRelated(data.content.filter((r) => r.id !== p.id).slice(0, 4)))
                        .catch(() => {});
                }
            })
            .catch(() => setProduct(undefined))
            .finally(() => setLoading(false));
    }, [id]);

    if (loading) {
        return (
            <>
                <Navbar />
                <div className={styles.notFound}><p className={styles.notFoundText}>Loading…</p></div>
            </>
        );
    }

    if (!product) {
        return (
            <>
                <Navbar />
                <div className={styles.notFound}>
                    <p className={styles.notFoundText}>Product not found.</p>
                    <Link to="/menu" className={styles.backLink}>← Back to Menu</Link>
                </div>
            </>
        );
    }

    const isOut = product.status === 'OutOfStock';
    const isInCart = items.some((i) => i.id === product.id);

    const handleAdd = () => {
        if (isOut) return;
        addItem({
            id: product.id,
            productName: product.productName,
            imageUrl: product.imageUrl,
            price: product.price,
            category: product.categoryName,
        }, qty);
    };

    return (
        <>
            <Navbar />

            <div className={styles.page}>

                {/* ── Breadcrumb ── */}
                <div className={styles.breadcrumbWrap}>
                    <div className={styles.breadcrumbInner}>
                        <button className={styles.backBtn} onClick={() => navigate(-1)}>
                            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                                <polyline points="15 18 9 12 15 6" />
                            </svg>
                            Back
                        </button>
                        <nav className={styles.breadcrumb}>
                            <Link to="/" className={styles.crumb}>Home</Link>
                            <span className={styles.crumbSep}>/</span>
                            <Link to="/menu" className={styles.crumb}>Menu</Link>
                            <span className={styles.crumbSep}>/</span>
                            <span className={styles.crumbCurrent}>{product.productName}</span>
                        </nav>
                    </div>
                </div>

                {/* ── Main Detail ── */}
                <section className={styles.detail}>
                    <div className={styles.detailInner}>

                        {/* Left — Image */}
                        <div className={styles.imageCol}>
                            <div className={`${styles.imageFrame} ${isOut ? styles.imageFrameOut : ''}`}>
                                <img
                                    src={product.imageUrl || 'https://placehold.co/600x600/EFECE3/231F1E?text=No+Image'}
                                    alt={product.productName}
                                    className={styles.image}
                                    onError={(e) => { e.target.src = 'https://placehold.co/600x600/EFECE3/231F1E?text=No+Image'; }}
                                />
                                {product.badge && !isOut && (
                                    <span className={styles.badge}>{product.badge}</span>
                                )}
                                {isOut && (
                                    <span className={`${styles.badge} ${styles.badgeOut}`}>Out of Stock</span>
                                )}
                            </div>
                        </div>

                        {/* Right — Info */}
                        <div className={styles.infoCol}>
                            <span className={styles.categoryTag}>{product.categoryName}</span>
                            <h1 className={styles.productName}>{product.productName}</h1>
                            <p className={styles.price}>{formatVND(product.price)}</p>

                            {product.description && (
                                <p className={styles.description}>{product.description}</p>
                            )}

                            <div className={styles.divider} />

                            {/* Quantity */}
                            {!isOut && (
                                <div className={styles.qtyRow}>
                                    <span className={styles.qtyLabel}>Quantity</span>
                                    <div className={styles.qtyControls}>
                                        <button
                                            className={styles.qtyBtn}
                                            onClick={() => setQty((q) => Math.max(1, q - 1))}
                                            aria-label="Decrease quantity"
                                        >−</button>
                                        <span className={styles.qtyValue}>{qty}</span>
                                        <button
                                            className={styles.qtyBtn}
                                            onClick={() => setQty((q) => q + 1)}
                                            aria-label="Increase quantity"
                                        >+</button>
                                    </div>
                                </div>
                            )}

                            {/* Add to Cart */}
                            <button
                                className={`${styles.addBtn} ${isOut ? styles.addBtnDisabled : ''} ${isInCart && !isOut ? styles.addBtnAdded : ''}`}
                                onClick={handleAdd}
                                disabled={isOut}
                            >
                                {isOut
                                    ? 'Out of Stock'
                                    : isInCart
                                        ? '✓ Added to Cart'
                                        : 'Add to Cart'}
                            </button>

                            {/* Meta */}
                            <dl className={styles.meta}>
                                <div className={styles.metaRow}>
                                    <dt className={styles.metaKey}>Category</dt>
                                    <dd className={styles.metaVal}>{product.categoryName}</dd>
                                </div>
                                <div className={styles.metaRow}>
                                    <dt className={styles.metaKey}>Status</dt>
                                    <dd className={`${styles.metaVal} ${isOut ? styles.metaValOut : styles.metaValAvail}`}>
                                        {isOut ? 'Out of Stock' : 'Available'}
                                    </dd>
                                </div>
                                <div className={styles.metaRow}>
                                    <dt className={styles.metaKey}>SKU</dt>
                                    <dd className={styles.metaVal}>{String(product.id).toUpperCase()}</dd>
                                </div>
                            </dl>
                        </div>
                    </div>
                </section>

                {/* ── Related Products ── */}
                {related.length > 0 && (
                    <section className={styles.related}>
                        <div className={styles.relatedInner}>
                            <p className={styles.relatedEyebrow}>From the same collection</p>
                            <h2 className={styles.relatedTitle}>You may also like</h2>
                            <div className={styles.relatedGrid}>
                                {related.map((p) => {
                                    const pOut = p.status === 'OutOfStock';
                                    return (
                                        <Link
                                            key={p.id}
                                            to={`/menu/${p.id}`}
                                            className={`${styles.relatedCard} ${pOut ? styles.relatedCardOut : ''}`}
                                        >
                                            <div className={styles.relatedImageWrap}>
                                                <img src={p.imageUrl} alt={p.productName} className={styles.relatedImage} loading="lazy" />
                                                {p.badge && !pOut && (
                                                    <span className={styles.relatedBadge}>{p.badge}</span>
                                                )}
                                            </div>
                                            <div className={styles.relatedInfo}>
                                                <span className={styles.relatedName}>{p.productName}</span>
                                                <span className={styles.relatedPrice}>{formatVND(p.price)}</span>
                                            </div>
                                        </Link>
                                    );
                                })}
                            </div>
                        </div>
                    </section>
                )}
            </div>

            <Footer />
        </>
    );
}
