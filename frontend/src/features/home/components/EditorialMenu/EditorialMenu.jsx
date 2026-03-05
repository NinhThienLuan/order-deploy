import React, { useState, useEffect } from 'react';
import styles from './EditorialMenu.module.css';
import { getProducts } from '../../../products/products.service';

const formatVND = (n) =>
    new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(n ?? 0);

const EditorialMenu = () => {
    const [featuredProducts, setFeaturedProducts] = useState([]);

    useEffect(() => {
        getProducts({ size: 4 })
            .then((res) => setFeaturedProducts(res.content))
            .catch(() => setFeaturedProducts([]));
    }, []);

    return (
        <div className={styles.editorialMenuList}>
            {featuredProducts.map((product) => (
                <div key={product.id} className={styles.menuRow}>
                    <div className={`${styles.menuImageContainer} reveal-on-scroll`}>
                        <div className={styles.menuGeoBlock}></div>
                        <img src={product.imageUrl} alt={product.productName} />
                    </div>
                    <div className={`${styles.menuContentContainer} reveal-on-scroll reveal-delay-1`}>
                        <div className={styles.menuTitleBox}>
                            <h3>{product.productName}</h3>
                        </div>
                        <p className={styles.menuDesc}>
                            Discover the exquisite notes and refined balance of our {product.productName}.
                            A testament to our dedication to quality and the perfect brew.
                        </p>
                        <div className={styles.menuPriceBadge}>
                            {formatVND(product.price)}
                        </div>
                    </div>
                </div>
            ))}
        </div>
    );
};

export default EditorialMenu;
