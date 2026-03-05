import React from 'react';
import styles from './HeroSection.module.css';

const HeroSection = () => {
    return (
        <header className={styles.heroClassic}>
            <h1>Brewed to Perfection.</h1>
            <p>THE HIGHEST QUALITY OF EXTRACTION</p>
        </header>
    );
};

export default HeroSection;
