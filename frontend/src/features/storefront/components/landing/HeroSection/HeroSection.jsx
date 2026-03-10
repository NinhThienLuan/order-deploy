import styles from './HeroSection.module.css';

const HeroSection = () => (
    <header
        className={styles.hero}
        style={{
            backgroundImage:
                "url('https://images.unsplash.com/photo-1512568400610-62da28bc8a13?auto=format&fit=crop&w=1200&q=60')",
        }}
    >
        <div className={styles.content}>
            <span className={styles.subtitle}>Presenting the Finest</span>
            <h1 className={styles.title}>
                Richer &amp; <br /> Thicker
            </h1>
            <p className={styles.description}>
                "A meticulous study of the traditional Vietnamese brew. Every drop a
                testament to the pursuit of richness."
            </p>
            <div className={styles.actions}>
                <a href="/order" className={styles.btnPrimary}>
                    Order Now
                </a>
                <a href="/menu" className={styles.btnSecondary}>
                    View Menu
                </a>
            </div>
        </div>

        <div className={styles.scrollIndicator} aria-hidden="true">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
                <path d="M7 13l5 5 5-5M7 6l5 5 5-5" />
            </svg>
        </div>
    </header>
);

export default HeroSection;
