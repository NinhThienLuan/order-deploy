import useScrollReveal from '@/hooks/useScrollReveal';
import styles from './VisitSection.module.css';

const VisitSection = () => {
    const leftRef = useScrollReveal({ threshold: 0.1 });
    const cardRef = useScrollReveal({ threshold: 0.1, delay: 150 });

    return (
        <section id="visit" className={styles.section}>
            <div className={styles.container}>
                <div className={styles.grid}>
                    {/* Left — venue info */}
                    <div ref={leftRef}>
                        <span className={styles.tag}>The Venue</span>
                        <h2 className={styles.title}>Visit Us.</h2>
                        <div className={styles.venues}>
                            URI PALACE • BAC NINH
                            <br />
                            HANOI GALLERY
                            <br />
                            SAIGON ATELIER
                        </div>
                    </div>

                    {/* Right — reservation card */}
                    <div className={styles.card} ref={cardRef}>
                        <span className={styles.cardTag}>Experience Center</span>
                        <h3 className={styles.cardTitle}>Reserve a Table</h3>
                        <div className={styles.cardActions}>
                            <a href="/stores" className={styles.btnSolid}>Find a Store</a>
                            <a href="/menu" className={styles.btnOutline}>Seasonal Menu</a>
                        </div>
                    </div>
                </div>
            </div>
        </section>
    );
};

export default VisitSection;
