import { useState, useEffect, useRef } from 'react';
import { Link } from 'react-router-dom';
import styles from './Navbar.module.css';
import MenuOverlay from './MenuOverlay';
import { useCart } from '@/features/orders/services/CartContext';
import { useAuth } from '@/features/authentication/services/AuthContext';
import { NAV_ITEMS } from '@/features/storefront/constants/nav.constants';
import { getCategories, getProducts } from '@/features/products/services/products.service';

const getInitials = (name = '') => {
    const trimmed = name.trim();
    if (!trimmed) return '?';
    return trimmed[0].toUpperCase();
};

const BellIcon = () => (
    <svg width="19" height="19" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
        <path d="M18 8A6 6 0 006 8c0 7-3 9-3 9h18s-3-2-3-9" />
        <path d="M13.73 21a2 2 0 01-3.46 0" />
    </svg>
);

const BagIcon = () => (
    <svg width="19" height="19" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
        <path d="M6 2L3 6v14a2 2 0 002 2h14a2 2 0 002-2V6l-3-4z" />
        <line x1="3" y1="6" x2="21" y2="6" />
        <path d="M16 10a4 4 0 01-8 0" />
    </svg>
);

const UserIcon = () => (
    <svg width="19" height="19" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
        <path d="M20 21v-2a4 4 0 00-4-4H8a4 4 0 00-4 4v2" /><circle cx="12" cy="7" r="4" />
    </svg>
);

const Navbar = () => {
    const [isScrolled, setIsScrolled] = useState(false);
    const [isMenuOpen, setIsMenuOpen] = useState(false);
    const [activeMenu, setActiveMenu] = useState(null);
    const [isUserDropOpen, setIsUserDropOpen] = useState(false);
    const [navCategories, setNavCategories] = useState([]);
    const [navFeatured, setNavFeatured] = useState([]);
    const { totalItems, openSidebar } = useCart();
    const { user, logout } = useAuth();
    const navRef = useRef(null);
    const userDropRef = useRef(null);
    const megaTimeoutRef = useRef(null);

    useEffect(() => {
        getCategories()
            .then((cats) => setNavCategories(cats))
            .catch(() => {});
        getProducts({ size: 4 })
            .then((data) => setNavFeatured(data.content))
            .catch(() => {});
    }, []);

    useEffect(() => {
        const handleScroll = () => setIsScrolled(window.scrollY > 20);
        window.addEventListener('scroll', handleScroll, { passive: true });
        return () => window.removeEventListener('scroll', handleScroll);
    }, []);

    useEffect(() => {
        if (isMenuOpen) {
            const w = window.innerWidth - document.documentElement.clientWidth;
            document.body.style.overflow = 'hidden';
            if (w > 0) {
                document.body.style.paddingRight = `${w}px`;
                if (navRef.current) navRef.current.style.paddingRight = `${w}px`;
            }
        } else {
            document.body.style.overflow = '';
            document.body.style.paddingRight = '';
            if (navRef.current) navRef.current.style.paddingRight = '';
        }
        return () => {
            document.body.style.overflow = '';
            document.body.style.paddingRight = '';
        };
    }, [isMenuOpen]);

    useEffect(() => {
        const onOutside = (e) => {
            if (userDropRef.current && !userDropRef.current.contains(e.target))
                setIsUserDropOpen(false);
        };
        if (isUserDropOpen) document.addEventListener('mousedown', onOutside);
        return () => document.removeEventListener('mousedown', onOutside);
    }, [isUserDropOpen]);

    const handleMenuEnter = (label) => {
        clearTimeout(megaTimeoutRef.current);
        setActiveMenu(label);
    };

    const handleMenuLeave = () => {
        megaTimeoutRef.current = setTimeout(() => setActiveMenu(null), 120);
    };

    const closeMega = () => setActiveMenu(null);

    const handleLogout = async () => {
        await logout();
        setIsUserDropOpen(false);
    };

    return (
        <>
            <nav
                ref={navRef}
                className={`${styles.nav} ${isScrolled ? styles.scrolled : ''} ${activeMenu ? styles.megaOpen : ''}`}
            >
                <div className={styles.container}>
                    {/* Logo */}
                    <Link to="/" className={styles.logo} aria-label="Home">&amp;</Link>

                    {/* Center nav items */}
                    <ul className={styles.navCenter} role="menubar">
                        {NAV_ITEMS.filter((item) => {
                            if (!item.adminOnly) return true;
                            return user && (user.role === 'ADMIN' || user.role === 'STORE_MANAGER');
                        }).map((item) => (
                            <li
                                key={item.label}
                                className={`${styles.navItem} ${activeMenu === item.label ? styles.navItemActive : ''}`}
                                onMouseEnter={() => item.mega && handleMenuEnter(item.label)}
                                onMouseLeave={handleMenuLeave}
                                role="none"
                            >
                                <Link
                                    to={item.href}
                                    className={styles.navLink}
                                    role="menuitem"
                                    aria-haspopup={!!item.mega}
                                    aria-expanded={activeMenu === item.label}
                                    onClick={closeMega}
                                >
                                    {item.label}
                                </Link>

                                {item.mega && activeMenu === item.label && (
                                    <div className={styles.megaMenu} role="menu">
                                        <div className={styles.megaInner}>
                                            <div className={styles.megaColumns}>
                                                {item.mega.columns.map((col) => (
                                                    <div key={col.heading} className={styles.megaCol}>
                                                        <span className={styles.megaHeading}>{col.heading}</span>
                                                        <ul className={styles.megaLinks}>
                                                            {(item.label === 'Menu' && col.heading === 'Browse by Category'
                                                                ? [
                                                                    { label: 'All Products', href: '/menu' },
                                                                    ...navCategories.map((c) => ({ label: c.name, href: `/menu?category=${c.id}` }))
                                                                  ]
                                                                : item.label === 'Menu' && col.heading === 'Featured'
                                                                ? navFeatured.map((p) => ({ label: p.productName, href: `/menu/${p.id}` }))
                                                                : col.links
                                                            ).map((link) => (
                                                                <li key={link.label}>
                                                                    <Link
                                                                        to={link.href}
                                                                        className={styles.megaLink}
                                                                        onClick={closeMega}
                                                                    >
                                                                        {link.label}
                                                                    </Link>
                                                                </li>
                                                            ))}
                                                        </ul>
                                                    </div>
                                                ))}
                                            </div>

                                            {item.mega.footer && (
                                                <div className={styles.megaFooter}>
                                                    {item.mega.footer.map((f) => (
                                                        <Link
                                                            key={f.label}
                                                            to={f.href}
                                                            className={styles.megaFooterLink}
                                                            onClick={closeMega}
                                                        >
                                                            {f.label}
                                                        </Link>
                                                    ))}
                                                </div>
                                            )}
                                        </div>
                                    </div>
                                )}
                            </li>
                        ))}
                    </ul>

                    {/* Right icons */}
                    <div className={styles.navRight}>
                        {/* Notifications */}
                        <button className={styles.iconBtn} aria-label="Notifications">
                            <BellIcon />
                        </button>

                        {/* Cart */}
                        <button className={styles.iconBtn} onClick={openSidebar} aria-label={`Cart (${totalItems} items)`}>
                            <BagIcon />
                            {totalItems > 0 && <span className={styles.cartBadge}>{totalItems}</span>}
                        </button>

                        {/* User */}
                        {user ? (
                            <div className={styles.userWrap} ref={userDropRef}>
                                <button
                                    className={styles.iconBtn}
                                    onClick={() => setIsUserDropOpen((p) => !p)}
                                    aria-expanded={isUserDropOpen}
                                    aria-label="Account"
                                >
                                    <span className={styles.userAvatar}>{getInitials(user.name || user.email)}</span>
                                </button>

                                {isUserDropOpen && (
                                    <div className={styles.userDrop}>
                                        <div className={styles.dropHeader}>
                                            <span className={styles.dropName}>{user.name || user.email}</span>
                                            <span className={styles.dropRole}>{(user.roles?.[0]?.code ?? user.roles?.[0] ?? '')}</span>
                                        </div>
                                        {user.role === 'ADMIN' && (
                                            <Link to="/admin" className={styles.dropItem} onClick={() => setIsUserDropOpen(false)}>
                                                Dashboard
                                            </Link>
                                        )}
                                        <Link to="/orders" className={styles.dropItem} onClick={() => setIsUserDropOpen(false)}>
                                            My Orders
                                        </Link>
                                        <button className={styles.dropLogout} onClick={handleLogout}>
                                            Sign Out
                                        </button>
                                    </div>
                                )}
                            </div>
                        ) : (
                            <Link to="/login" className={styles.iconBtn} aria-label="Sign in">
                                <UserIcon />
                            </Link>
                        )}

                        {/* Mobile menu toggle */}
                        <button
                            className={`${styles.menuToggle} ${isMenuOpen ? styles.active : ''}`}
                            onClick={() => setIsMenuOpen((p) => !p)}
                            aria-expanded={isMenuOpen}
                            aria-label={isMenuOpen ? 'Close menu' : 'Open menu'}
                        >
                            <span className={styles.menuIcon} aria-hidden="true">
                                <span /><span />
                            </span>
                        </button>
                    </div>
                </div>
            </nav>

            <MenuOverlay isOpen={isMenuOpen} onClose={() => setIsMenuOpen(false)} />
        </>
    );
};

export default Navbar;
