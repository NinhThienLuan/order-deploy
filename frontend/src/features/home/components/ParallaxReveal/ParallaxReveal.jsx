import React from 'react';
import { Link } from 'react-router-dom';
import styles from './ParallaxReveal.module.css';

const ParallaxReveal = () => {
    return (
        <section className={styles.parallaxReveal}>
            <div className={styles.parallaxContent}>
                <span className={styles.parallaxLabel}>The Archive Collection</span>
                <h2 className={styles.parallaxTitle}>Journey to the<br />Origin.</h2>
                <p className={styles.parallaxDesc}>
                    Discover our most exclusive, limited-lot beans sourced strictly from high-altitude micro-farms.
                    Roasted in extremely small batches to preserve their delicate, wild tasting notes.
                </p>
                <Link to="/menu" className={styles.parallaxBtn}>Explore The Archive</Link>
            </div>
        </section>
    );
};

export default ParallaxReveal;
