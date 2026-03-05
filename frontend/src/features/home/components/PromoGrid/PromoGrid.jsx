import React from 'react';
import styles from './PromoGrid.module.css';

const PromoGrid = () => {
    return (
        <div className={styles.promoGrid}>
            <a href="/deals" className={styles.promo} style={{ backgroundImage: "url('https://images.unsplash.com/photo-1495474472287-4d71bcdd2085?auto=format&fit=crop&w=800&q=80')" }}>
                <div className={styles.promoContent}>
                    <h3>Seasonal<br />Selections</h3>
                    <p>Limited-time flavors crafted for the changing seasons.</p>
                </div>
            </a>
            <a href="/subscription" className={styles.promo} style={{ backgroundImage: "url('https://images.unsplash.com/photo-1442111556602-d11f29ec078d?auto=format&fit=crop&w=800&q=80')" }}>
                <div className={styles.promoContent}>
                    <h3>Archive<br />Daily</h3>
                    <p>The freshest beans delivered to your doorstep every morning.</p>
                </div>
            </a>
        </div>
    );
};

export default PromoGrid;
