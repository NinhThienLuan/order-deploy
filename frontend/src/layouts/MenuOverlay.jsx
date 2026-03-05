import styles from './MenuOverlay.module.css';
import { OVERLAY_PRIMARY_LINKS, OVERLAY_PAGE_LINKS } from '../features/landing/landing.constants';

const MenuOverlay = ({ isOpen, onClose }) => (
    <div
        className={`${styles.overlay} ${isOpen ? styles.active : ''}`}
        aria-hidden={!isOpen}
        role="dialog"
        aria-modal="true"
    >
        <div className={styles.content}>
            <div className={styles.grid}>
                {/* Left — Primary Nav */}
                <nav className={styles.primaryNav} aria-label="Primary navigation">
                    {OVERLAY_PRIMARY_LINKS.map((link) => (
                        <a
                            key={link.href}
                            href={link.href}
                            className={styles.primaryLink}
                            onClick={onClose}
                        >
                            {link.label}
                        </a>
                    ))}
                </nav>

                {/* Right — Page Archive */}
                <div className={styles.rightCol}>
                    <span className={styles.archiveLabel}>Navigation Archive</span>
                    <nav className={styles.pageLinks} aria-label="Page navigation">
                        {OVERLAY_PAGE_LINKS.map((link) => (
                            <a
                                key={link.label}
                                href={link.href}
                                className={styles.pageLink}
                                onClick={onClose}
                            >
                                {link.label}
                            </a>
                        ))}
                    </nav>
                </div>
            </div>
        </div>
    </div>
);

export default MenuOverlay;
