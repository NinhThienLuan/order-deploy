import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { getProducts } from '@/features/products/services/products.service';
import { useCart } from '@/features/orders/services/CartContext';
import useScrollReveal from '@/hooks/useScrollReveal';
import SharedProductCard from '@/features/products/components/ProductCard/ProductCard';
import styles from './CatalogSection.module.css';

const CatalogSection = () => {
    const headerRef = useScrollReveal({ threshold: 0.1 });
    const { addItem, items } = useCart();
    const [featured, setFeatured] = useState([]);
    const [quickAddProductId, setQuickAddProductId] = useState(null);
    const [addedIds, setAddedIds] = useState(new Set());

    useEffect(() => {
        getProducts({ size: 4 })
            .then((res) => setFeatured(res.content))
            .catch(() => setFeatured([]));
    }, []);

    const handleAdd = (product, variant) => {
        addItem(product, 1, variant);
        setAddedIds(prev => new Set(prev).add(product.id));
        setTimeout(() => setAddedIds(prev => {
            const next = new Set(prev);
            next.delete(product.id);
            return next;
        }), 1400);
    };

    const ProductCardWrapper = ({ product }) => {
        const ref = useScrollReveal({ threshold: 0.1 });
        return (
            <div ref={ref}>
                <SharedProductCard
                    product={product}
                    isAdded={addedIds.has(product.id) || items.some(i => i.id === product.id)}
                    quickAddProductId={quickAddProductId}
                    setQuickAddProductId={setQuickAddProductId}
                    onAddVariant={(variant) => handleAdd(product, variant)}
                />
            </div>
        );
    };

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
                            <ProductCardWrapper 
                                key={product.id} 
                                product={product} 
                            />
                        ))}
                    </div>
                </div>

                <div className={styles.cta}>
                    <Link to="/menu" className={styles.btnCatalog}>Full Catalog</Link>
                </div>
            </div>
        </section>
    );
};

export default CatalogSection;
