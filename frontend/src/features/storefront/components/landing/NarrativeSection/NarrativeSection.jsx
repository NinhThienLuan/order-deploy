import useScrollReveal from '@/hooks/useScrollReveal';
import styles from './NarrativeSection.module.css';

const NarrativeSection = () => {
    const contentRef = useScrollReveal({ threshold: 0.15 });
    const imgRef = useScrollReveal({ threshold: 0.15, delay: 100 });

    return (
        <section id="narrative" className={styles.section}>
            <div className={styles.grid}>
                <div className={styles.imageWrapper} ref={imgRef}>
                    <img
                        src="https://images.unsplash.com/photo-1495474472287-4d71bcdd2085?auto=format&fit=crop&w=1200&q=60"
                        alt="E-Coffee Narrative"
                        className={styles.image}
                        loading="lazy"
                    />
                </div>
                <div className={styles.content} ref={contentRef}>
                    <span className={styles.tag}>Our Narrative</span>
                    <h2 className={styles.title}>The Archive.</h2>
                    <p className={`${styles.desc} ${styles.italic}`}>
                        "E-Coffee is not merely a destination; it is a meticulous study of the
                        traditional Vietnamese brew. Every bean is curated with architectural
                        intentionality."
                    </p>
                    <a href="/about" className={styles.btn}>
                        Full Narrative
                    </a>
                </div>
            </div>
        </section>
    );
};

export default NarrativeSection;
