import useScrollReveal from '../../../../hooks/useScrollReveal';
import styles from './AtmosphereSection.module.css';
import { ATMOSPHERE_IMAGES } from '../../landing.constants';

const AtmosphereSection = () => {
    const headerRef = useScrollReveal({ threshold: 0.1 });

    return (
        <section id="atmosphere" className={styles.section}>
            <div className={styles.header} ref={headerRef}>
                <span className={styles.tag}>The Venue</span>
                <h2 className={styles.title}>Atmosphere</h2>
            </div>
            <div className={styles.imageGrid}>
                {ATMOSPHERE_IMAGES.map((img, i) => (
                    <div key={i} className={styles.imageItem}>
                        <img src={img.src} alt={img.alt} loading="lazy" />
                    </div>
                ))}
            </div>
        </section>
    );
};

export default AtmosphereSection;
