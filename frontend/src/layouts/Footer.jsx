import styles from './Footer.module.css';
import { FOOTER_LINKS } from '@/features/storefront/constants/landing.constants';

const Footer = () => (
    <footer className={styles.footer} id="footer">
        <div className={styles.container}>
            <div className={styles.grid}>
                {/* Brand column */}
                <div>
                    <div className={styles.logo}>E-COFFEE</div>
                    <p className={styles.tagline}>
                        Crafted with precision. Every cup, a testament to our pursuit of richness.
                    </p>
                </div>

                {/* Link columns */}
                {Object.entries(FOOTER_LINKS).map(([heading, links]) => (
                    <div key={heading}>
                        <h4 className={styles.colHeading}>{heading}</h4>
                        <ul className={styles.linkList}>
                            {links.map((link) => (
                                <li key={link.label}>
                                    <a href={link.href}>{link.label}</a>
                                </li>
                            ))}
                        </ul>
                    </div>
                ))}
            </div>

            <div className={styles.bottom}>
                <span>© 2026 E-Coffee Architectural Suite V.4.0</span>
                <div className={styles.bottomRight}>
                    <span>Precision Guaranteed</span>
                    <span>Legal</span>
                </div>
            </div>
        </div>
    </footer>
);

export default Footer;
