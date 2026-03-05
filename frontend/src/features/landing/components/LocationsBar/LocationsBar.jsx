import styles from './LocationsBar.module.css';
import { LOCATIONS } from '../../landing.constants';

const LocationsBar = () => (
    <section className={styles.bar} aria-label="Our locations">
        <div className={styles.inner}>
            {LOCATIONS.map((loc) => (
                <a key={loc.href} href={loc.href} className={styles.link}>
                    {loc.label}
                </a>
            ))}
        </div>
    </section>
);

export default LocationsBar;
