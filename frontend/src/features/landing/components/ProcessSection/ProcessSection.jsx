import useScrollReveal from '../../../../hooks/useScrollReveal';
import styles from './ProcessSection.module.css';
import { PROCESS_STEPS } from '../../landing.constants';

const ProcessSection = () => {
    const contentRef = useScrollReveal({ threshold: 0.15 });
    const imgRef = useScrollReveal({ threshold: 0.15, delay: 100 });

    return (
        <section id="process" className={styles.section}>
            {/* .full-bleed-grid.reverse: image order:2 (right), content order:1 (left) */}
            <div className={styles.grid}>
                <div className={styles.imageWrapper} ref={imgRef}>
                    <img
                        src="https://images.unsplash.com/photo-1442512595331-e89e73853f31?auto=format&fit=crop&w=1200&q=60"
                        alt="Methodology"
                        className={styles.image}
                        loading="lazy"
                    />
                </div>
                <div className={styles.content} ref={contentRef}>
                    <span className={styles.tag}>Methodology</span>
                    <h2 className={styles.title}>The Process.</h2>
                    <div className={styles.stepsBlock}>
                        {PROCESS_STEPS.map((step) => (
                            <p key={step.numeral} className={styles.stepRow}>
                                <strong className={styles.stepTitle}>
                                    {step.numeral} {step.title}:
                                </strong>{' '}
                                {step.desc}
                            </p>
                        ))}
                    </div>
                    <a href="#" className={styles.btn}>
                        Explore Technique
                    </a>
                </div>
            </div>
        </section>
    );
};

export default ProcessSection;
