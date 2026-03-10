import styles from './Toggle.module.css';

export default function Toggle({ 
  checked, 
  onChange, 
  disabled,
  label 
}) {
  return (
    <div className={`${styles.toggleWrapper} ${disabled ? styles.disabled : ''}`}>
      {label && <span className={styles.topLabel}>{label}</span>}
      
      <label className={styles.switch}>
        <input 
          type="checkbox" 
          checked={checked} 
          onChange={e => onChange?.(e.target.checked)}
          disabled={disabled}
        />
        <div className={styles.slider}>
          <span className={styles.internalLabel}>
            {checked ? 'Active' : 'InActive'}
          </span>
          <div className={styles.handle} />
        </div>
      </label>
    </div>
  );
}
