import { useState, useEffect } from 'react';
import useScrollReveal from '../../../../hooks/useScrollReveal';
import { Link } from 'react-router-dom';
import styles from './CatalogSection.module.css';
import { getProducts } from '../../../products/products.service';
import { useCart } from '../../../../features/cart/CartContext';

const formatVND = (n) =>
    new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(n ?? 0);

const ProductCard = ({ product }) => {
    const ref = useScrollReveal({ threshold: 0.1 });
    const { addItem, items } = useCart();
    const isAdded = items.some((i) => i.id === product.id);

    const handleAdd = () => {
        if (isAdded) return;
        addItem({
            id: product.id,
            productName: product.productName,
            imageUrl: product.imageUrl,
            price: product.price,
            category: product.categoryName,
        });
    };

    return (
        <div className={styles.card} ref={ref}>
            {/* Image area */}
            <Link to={`/menu/${product.id}`} className={styles.imageLink}>
            <div className={styles.imageWrap}>
                <img
                    src={product.imageUrl}
                    alt={product.productName}
                    className={styles.image}
                    loading="lazy"
                />
                {/* Add to cart — top-right icon button */}
                <button
                    className={`${styles.cartIconBtn} ${isAdded ? styles.cartIconBtnAdded : ''}`}
                    onClick={(e) => { e.preventDefault(); handleAdd(); }}
                    disabled={isAdded}
                    aria-label={isAdded ? 'Added to cart' : 'Add to cart'}
                >
                    {isAdded ? (
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
                            <polyline points="20 6 9 17 4 12" />
                        </svg>
                    ) : (
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                            <circle cx="9" cy="21" r="1" /><circle cx="20" cy="21" r="1" />
                            <path d="M1 1h4l2.68 13.39a2 2 0 0 0 2 1.61h9.72a2 2 0 0 0 2-1.61L23 6H6" />
                        </svg>
                    )}
                </button>
            </div>
            </Link>

            {/* Info */}
            <div className={styles.info}>
                <span className={styles.category}>{product.categoryName ?? ''}</span>
                <div className={styles.nameRow}>
                    <h3 className={styles.name}>{product.productName}</h3>
                    <span className={styles.price}>{formatVND(product.price)}</span>
                </div>
            </div>
        </div>
    );
};

const CatalogSection = () => {
    const headerRef = useScrollReveal({ threshold: 0.1 });
    const [featured, setFeatured] = useState([]);

    useEffect(() => {
        getProducts({ size: 4 })
            .then((res) => setFeatured(res.content))
            .catch(() => setFeatured([]));
    }, []);

    return (
        <section id="coffees" className={styles.section}>
            <div className={styles.container}>
                <div className={styles.header} ref={headerRef}>
                    <span className={styles.tag}>Catalog 2026</span>
                    <h2 className={styles.title}>Essentials.</h2>
                </div>

                <div className={styles.gridWrap}>
                    <div className={styles.grid}>
                        {featured.map((product) => (
                            <ProductCard key={product.id} product={product} />
                        ))}
                    </div>
                </div>

                <div className={styles.cta}>
                    <a href="/menu" className={styles.btnCatalog}>Full Catalog</a>
                </div>
            </div>
        </section>
    );
};

export default CatalogSection;
