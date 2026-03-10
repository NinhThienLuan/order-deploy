import { useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '@/features/authentication/services/AuthContext';
import apiClient from '@/services/apiClient';
import styles from './LoginPage.module.css';

const LoginPage = () => {
  const [formData, setFormData] = useState({
    email: '',
    password: ''
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  
  const navigate = useNavigate();
  const location = useLocation();
  const { refreshUser } = useAuth();

  const handleInputChange = (e) => {
    const { id, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [id]: value
    }));
    // Clear error when user starts typing
    if (error) setError('');
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      // Gọi API login
      await apiClient.post('/api/v1/auth/login', formData);

      // Lấy thông tin user
      const meResponse = await apiClient.get('/api/v1/auth/me');
      const userData = meResponse.data.result;

      // Lưu user info vào localStorage
      const firstName = userData.firstName || '';
      const lastName = userData.lastName || '';
      const fullName = [firstName, lastName].filter(Boolean).join(' ') || userData.email;
      const roles = userData.roles || [];
      // roles is an array of { id, code, name, description } objects
      const primaryRole = roles[0]?.code || null;
      localStorage.setItem('auth_user', JSON.stringify({
        id: userData.id,
        email: userData.email,
        name: fullName,
        role: primaryRole,
        roles,
        phone: userData.phone,
        address: userData.address
      }));

      // Redirect based on role
      refreshUser();
      if (primaryRole === 'FRANCHISE_ADMIN' || primaryRole === 'STORE_MANAGER') {
        navigate('/admin');
      } else {
        const from = location.state?.from?.pathname;
        if (from === '/cart') {
          navigate('/checkout');
        } else {
          navigate(from || '/home', { replace: true });
        }
      }

    } catch (err) {
      console.error('Login error:', err);

      // Xử lý lỗi
      if (err.response?.data?.message) {
        setError(err.response.data.message);
      } else if (err.response?.status === 401) {
        setError('Invalid email or password.');
      } else if (err.response?.status === 400) {
        setError('Please check your input and try again.');
      } else {
        setError('Login failed. Please try again later.');
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className={styles.page}>
      <div className={styles.card}>
        <div className={styles.cardHeader}>
          <h2 className={styles.title}>Sign In</h2>
          <p className={styles.subtitle}>
            Welcome back to the E-Coffee system.
          </p>
        </div>

        <form className={styles.form} onSubmit={handleSubmit}>
          {/* Email Field */}
          <div className={styles.field}>
            <label htmlFor="email" className={styles.label}>
              Email Address
            </label>
            <input
              id="email"
              type="email"
              placeholder="your@email.com"
              value={formData.email}
              onChange={handleInputChange}
              required
              disabled={loading}
              className={styles.input}
              autoComplete="email"
              autoFocus
            />
          </div>

          {/* Password Field */}
          <div className={styles.field}>
            <div className={styles.labelRow}>
              <label htmlFor="password" className={styles.label}>
                Password
              </label>
              <button
                type="button"
                className={styles.forgotBtn}
                onClick={() => navigate('/forgot-password')}
                disabled={loading}
              >
                Forgot password?
              </button>
            </div>
            <input
              id="password"
              type="password"
              placeholder="••••••••"
              value={formData.password}
              onChange={handleInputChange}
              required
              disabled={loading}
              className={styles.input}
              autoComplete="current-password"
            />
          </div>

          {/* Error Message */}
          {error && (
            <div className={styles.errorContainer} role="alert">
              <p className={styles.error}>{error}</p>
            </div>
          )}

          {/* Submit Button */}
          <button
            type="submit"
            disabled={loading}
            className={styles.submitBtn}
          >
            {loading ? 'SIGNING IN...' : 'SIGN IN'}
          </button>
        </form>
      </div>

      {/* Footer */}
      <p className={styles.pageFooter}>
        E-Coffee •{' '}
        <span className={styles.pageFooterAccent}>
          Franchise Management System
        </span>
      </p>
    </div>
  );
};

export default LoginPage;
