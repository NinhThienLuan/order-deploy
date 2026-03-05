import { useState, useEffect } from 'react';
import { useAuth } from '../../AuthContext';
import styles from './LoginModal.module.css';

const LoginModal = ({ isOpen, onClose }) => {
    const { login, loading, error, clearError } = useAuth();
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');

    // Reset form when modal closes
    useEffect(() => {
        if (!isOpen) {
            setUsername('');
            setPassword('');
            clearError();
        }
    }, [isOpen, clearError]);

    // Close on Escape key
    useEffect(() => {
        const onKey = (e) => { if (e.key === 'Escape') onClose(); };
        if (isOpen) window.addEventListener('keydown', onKey);
        return () => window.removeEventListener('keydown', onKey);
    }, [isOpen, onClose]);

    const handleSubmit = async (e) => {
        e.preventDefault();
        const ok = await login(username, password);
        if (ok) onClose();
    };

    if (!isOpen) return null;

    return (
        <div className={styles.backdrop} onClick={onClose}>
            <div className={styles.modal} onClick={(e) => e.stopPropagation()}>
                <button className={styles.closeBtn} onClick={onClose} aria-label="Close">
                    <span></span>
                    <span></span>
                </button>

                <div className={styles.header}>
                    <span className={styles.brand}>&</span>
                    <h2 className={styles.title}>Welcome Back</h2>
                    <p className={styles.subtitle}>Sign in to your account</p>
                </div>

                <form className={styles.form} onSubmit={handleSubmit}>
                    <div className={styles.field}>
                        <label className={styles.label} htmlFor="login-username">USERNAME</label>
                        <input
                            id="login-username"
                            className={styles.input}
                            type="text"
                            placeholder="your username"
                            value={username}
                            onChange={(e) => setUsername(e.target.value)}
                            required
                            autoComplete="username"
                        />
                    </div>

                    <div className={styles.field}>
                        <label className={styles.label} htmlFor="login-password">PASSWORD</label>
                        <input
                            id="login-password"
                            className={styles.input}
                            type="password"
                            placeholder="••••••••"
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                            required
                            autoComplete="current-password"
                        />
                    </div>

                    {error && <p className={styles.error}>{error}</p>}

                    <button className={styles.submitBtn} type="submit" disabled={loading}>
                        {loading ? 'SIGNING IN...' : 'SIGN IN'}
                    </button>
                </form>
            </div>
        </div>
    );
};

export default LoginModal;
