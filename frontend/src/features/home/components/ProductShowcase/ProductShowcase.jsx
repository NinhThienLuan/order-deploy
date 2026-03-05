import React from 'react';
import styles from './ProductShowcase.module.css';
import { PRODUCTS } from '../../home.constants';

const ProductShowcase = () => {
    return (
        <div className={styles.focalShowcase}>
            <div className={`${styles.focalImageWrap} reveal-on-scroll`}>
                <img src={PRODUCTS[0].image} alt="Signature Espresso" />
            </div>
            <div className={`${styles.focalContent} reveal-on-scroll reveal-delay-1`}>
                <span className={styles.focalSubtitle}>Signature Single-Origin</span>
                <h2 className={styles.focalTitle}>Ethiopian<br />Yirgacheffe</h2>
                <p className={styles.focalDesc}>
                    Our most sought-after beans. Notes of jasmine, lemon, and a subtle honey sweetness.
                    Grown at 2000m altitude and hand-picked for perfection.
                </p>
                <button className="btn-primary">Reserve Batch</button>
            </div>
        </div>
    );
};

export default ProductShowcase;
