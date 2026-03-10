import { useState, useRef, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '@/features/authentication/services/AuthContext';
import styles from './AdminHeader.module.css';

const getInitials = (name = '') =>
    name.trim().split(/\s+/).filter(Boolean).map(w => w[0]).slice(0, 2).join('').toUpperCase() || '?';

const BellIcon = () => (
    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
        <path d="M18 8A6 6 0 006 8c0 7-3 9-3 9h18s-3-2-3-9" />
        <path d="M13.73 21a2 2 0 01-3.46 0" />
    </svg>
);

// Placeholder notifications — replace with real API data later
const MOCK_NOTIFICATIONS = [
    { id: 1, text: 'New order #ORD-1205 received', time: '2m ago', unread: true },
    { id: 2, text: 'Store HCM2 low stock alert', time: '15m ago', unread: true },
    { id: 3, text: 'Monthly report is ready', time: '1h ago', unread: false },
];

export default function AdminHeader({ breadcrumbs = [] }) {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [userOpen, setUserOpen] = useState(false);
  const [bellOpen, setBellOpen] = useState(false);
  const userRef = useRef(null);
  const bellRef = useRef(null);

  useEffect(() => {
    const onOutside = (e) => {
      if (userRef.current && !userRef.current.contains(e.target)) setUserOpen(false);
      if (bellRef.current && !bellRef.current.contains(e.target)) setBellOpen(false);
    };
    document.addEventListener('mousedown', onOutside);
    return () => document.removeEventListener('mousedown', onOutside);
  }, []);

  const handleLogout = async () => {
    await logout();
    navigate('/login');
  };

  const displayName = user?.name || user?.email || '';
  const roleLabel = user?.roles?.[0]?.name ?? user?.role ?? '';
  const unreadCount = MOCK_NOTIFICATIONS.filter(n => n.unread).length;

  return (
    <div className={styles.header}>
      <div className={styles.breadcrumb}>
        {breadcrumbs.map((crumb, i) => (
          <span key={i}>
            {i > 0 && ' / '}
            <span className={i === breadcrumbs.length - 1 ? styles.active : ''}>
              {crumb}
            </span>
          </span>
        ))}
      </div>

      <div className={styles.headerRight}>

        {/* Bell */}
        <div className={styles.iconWrap} ref={bellRef}>
          <button
            className={styles.iconBtn}
            onClick={() => { setBellOpen(p => !p); setUserOpen(false); }}
            aria-label="Notifications"
            aria-expanded={bellOpen}
          >
            <BellIcon />
            {unreadCount > 0 && <span className={styles.badge}>{unreadCount}</span>}
          </button>

          {bellOpen && (
            <div className={styles.notifDrop}>
              <div className={styles.notifHeader}>Notifications</div>
              {MOCK_NOTIFICATIONS.map(n => (
                <div key={n.id} className={`${styles.notifItem} ${n.unread ? styles.unread : ''}`}>
                  <span className={styles.notifText}>{n.text}</span>
                  <span className={styles.notifTime}>{n.time}</span>
                </div>
              ))}
            </div>
          )}
        </div>

        {/* User */}
        <div className={styles.userWrap} ref={userRef}>
          <button
            className={styles.avatarBtn}
            onClick={() => { setUserOpen(p => !p); setBellOpen(false); }}
            aria-expanded={userOpen}
            aria-label="Account menu"
          >
            <span className={styles.userAvatar}>{getInitials(displayName)}</span>
            <div className={styles.userInfo}>
              <span className={styles.userName}>{displayName.toUpperCase()}</span>
              <span className={styles.userRole}>{roleLabel}</span>
            </div>
          </button>

          {userOpen && (
            <div className={styles.userDrop}>
              <div className={styles.dropHeader}>
                <span className={styles.dropName}>{displayName}</span>
                <span className={styles.dropRole}>{roleLabel}</span>
              </div>
              <button className={styles.dropLogout} onClick={handleLogout}>
                Sign Out
              </button>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
