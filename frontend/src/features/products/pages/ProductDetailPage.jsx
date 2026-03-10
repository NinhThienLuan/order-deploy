import { useState, useEffect } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import Navbar from '@/layouts/Navbar';
import Footer from '@/layouts/Footer';
import { useCart } from '@/features/orders/services/CartContext';
import { getProductById, getProducts } from '@/features/products/services/products.service';
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
    const [selectedImage, setSelectedImage] = useState(null);
    const [selectedVariant, setSelectedVariant] = useState(null);

    useEffect(() => {
        setLoading(true);
        setProduct(null);
        setRelated([]);
        getProductById(id)
            .then((p) => {
                setProduct(p);
                setSelectedImage(p.imageUrl);
                // Set default variant (first active)
                const firstActive = p.variants?.find(v => v.active);
                setSelectedVariant(firstActive || null);
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
            </>
        );
    }

    const isOut = product.status === 'OutOfStock' || (product.variants && !product.variants.some(v => v.active));
    const isInCart = items.some((i) => i.id === product.id && i.variantId === (selectedVariant?.id || null));

    const handleAdd = () => {
        if (isOut || !selectedVariant) return;
        addItem({
            id: product.id,
            productName: product.productName,
            imageUrl: product.imageUrl,
            price: selectedVariant.price,
            category: product.categoryName,
        }, qty, selectedVariant);
    };

    return (
        <>
            <Navbar />

            <div className={styles.page}>

                {/* ── Breadcrumb ── */}
                

                {/* ── Main Detail ── */}
                <section className={styles.detail}>
                    <div className={styles.detailInner}>

                        {/* Left — Image */}
                        <div className={styles.imageCol}>
                            <div className={`${styles.imageFrame} ${isOut ? styles.imageFrameOut : ''}`}>
                                <img
                                    src={selectedImage || 'https://placehold.co/600x600/EFECE3/231F1E?text=No+Image'}
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

                            {/* Thumbnail Gallery */}
                            {product.images && product.images.length > 1 && (
                                <div className={styles.thumbnailRow}>
                                    {product.images.map((img, idx) => {
                                        const isSelected = img.imageUrl === selectedImage;
                                        return (
                                            <button
                                                key={idx}
                                                className={`${styles.thumbnailBtn} ${isSelected ? styles.thumbnailActive : ''}`}
                                                onClick={() => setSelectedImage(img.imageUrl)}
                                                aria-label={`View image ${idx + 1}`}
                                            >
                                                <img 
                                                    src={img.imageUrl} 
                                                    alt={`Thumbnail ${idx + 1} for ${product.productName}`}
                                                    className={styles.thumbnailImg}
                                                    loading="lazy"
                                                />
                                            </button>
                                        );
                                    })}
                                </div>
                            )}
                        </div>

                        {/* Right — Info */}
                        <div className={styles.infoCol}>
                            <h1 className={styles.productName}>{product.productName}</h1>
                            <span className={styles.categoryTag}>by {product.categoryName}</span>
                            <p className={styles.price}>{formatVND(selectedVariant?.price || product.price)}</p>

                            {/* Variation Selection (Sizes) */}
                            {product.variants && product.variants.length > 1 && (
                                <div className={styles.variantSection}>
                                    <p className={styles.variantLabel}>Select Size</p>
                                    <div className={styles.variantGrid}>
                                        {product.variants.map((v) => (
                                            <button
                                                key={v.id}
                                                className={`${styles.variantBtn} ${selectedVariant?.id === v.id ? styles.variantBtnActive : ''} ${!v.active ? styles.variantBtnDisabled : ''}`}
                                                onClick={() => v.active && setSelectedVariant(v)}
                                                disabled={!v.active}
                                            >
                                                {v.sizeName}
                                            </button>
                                        ))}
                                    </div>
                                </div>
                            )}

                            {/* Quantity Row */}
                            {!isOut && (
                                <div className={styles.qtyRowWrapper}>
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
                                    <span className={styles.qtyPriceInline}>{formatVND((selectedVariant?.price || product.price) * qty)}</span>
                                </div>
                            )}

                            {/* Actions Row */}
                            <div className={styles.actionRow}>
                                <button
                                    className={styles.buyBtn}
                                    onClick={handleAdd}
                                    disabled={isOut}
                                >
                                    BUY NOW
                                </button>
                                <button
                                    className={`${styles.addBtn} ${isOut ? styles.addBtnDisabled : ''} ${isInCart && !isOut ? styles.addBtnAdded : ''}`}
                                    onClick={handleAdd}
                                    disabled={isOut}
                                >
                                    {isOut
                                        ? 'Out of Stock'
                                        : isInCart
                                            ? '✓ Added to Cart'
                                            : 'ADD TO CART'}
                                </button>
                            </div>

                            <div className={styles.divider} />

                            {/* Description & Meta */}
                            {product.description && (
                                <p className={styles.description}>{product.description}</p>
                            )}

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
