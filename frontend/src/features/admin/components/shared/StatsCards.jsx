import styles from './StatsCards.module.css';

export default function StatsCards({ stats }) {
  return (
    <div className={styles.statsGrid}>
      {stats.map((stat, i) => (
        <div key={i} className={styles.statCard}>
          <div className={styles.statLabel}>{stat.label}</div>
          <div className={`${styles.statValue} ${stat.highlight ? styles.highlight : ''}`}>
            {stat.value}
          </div>
        </div>
      ))}
    </div>
  );
}
