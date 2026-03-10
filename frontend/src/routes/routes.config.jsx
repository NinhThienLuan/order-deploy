/**
 * ROUTE MAP
 * ─────────────────────────────────────────────────────────────────
 * PUBLIC         accessible by everyone (menu, login…)
 * GUEST_ONLY     accessible only when NOT logged in (landing); logged-in → /home
 * PROTECTED      must be logged in (any role)
 * ADMIN          role === 'ADMIN' only
 * STORE_MANAGER  role === 'STORE_MANAGER' only
 * ─────────────────────────────────────────────────────────────────
 *
 * Route tree:
 *
 *  /                      → LandingPage
 *  /home                  → HomePage
 *  /menu                  → ProductListPage
 *  /menu/:id              → ProductDetailPage
 *  /login                 → LoginPage
 *  /forgot-password       → ForgotPasswordPage
 *
 *  [auth required]
 *  /cart                  → CartPage
 *  /checkout/:id          → CheckoutPage
 *  /orders                → OrderListPage
 *  /orders/:id            → OrderDetailPage (customer receipt)
 *  /transactions          → TransactionHistoryPage
 *
 *  [ADMIN only]
 *  /admin/orders          → OrderManagementPage
 *
 *  [ADMIN + STORE_MANAGER — role-based view]
 *  /admin                 → AdminDashboardPage (ADMIN sees franchise view, STORE_MANAGER sees store view)
 *  /admin/stores/orders   → StoreOrderManagementPage
 */

import LandingPage from '@/features/storefront/pages/LandingPage';
import HomePage from '@/features/storefront/pages/HomePage';
import ProductListPage from '@/features/products/pages/ProductListPage';
import ProductDetailPage from '@/features/products/pages/ProductDetailPage';
import LoginPage from '@/features/authentication/pages/LoginPage';
// RegisterPage removed - not implemented
import ForgotPasswordPage from '@/features/authentication/pages/ForgotPasswordPage';
import CartPage from '@/features/orders/pages/CartPage';
import CheckoutPage from '@/features/orders/pages/CheckoutPage';
import OrderListPage from '@/features/orders/pages/OrderListPage';
import OrderDetailPage from '@/features/orders/pages/OrderDetailPage';
import TransactionHistoryPage from '@/features/admin/pages/transactions/TransactionHistoryPage';
import OrderManagementPage from '@/features/admin/pages/orders/OrderManagementPage';
import AdminOrderDetailPage from '@/features/admin/pages/orders/AdminOrderDetailPage';
import AdminDashboardPage from '@/features/admin/pages/shared/AdminDashboardPage';
import StoreOrderManagementPage from '@/features/admin/pages/orders/StoreOrderManagementPage';
import POSPage from '@/features/admin/pages/orders/POSPage';
import AdminProductCrudPage from '@/features/admin/pages/products/AdminProductCrudPage';
import AdminCategoryPage from '@/features/admin/pages/categories/AdminCategoryPage';
import AdminProductDetailPage from '@/features/admin/pages/products/AdminProductDetailPage';
import PaymentResultPage from '@/features/payments/pages/PaymentResultPage';
import PaymentSuccessPage from '@/features/payments/pages/PaymentSuccessPage';
import PaymentFailedPage from '@/features/payments/pages/PaymentFailedPage';
import PaymentLoadingPage from '@/features/payments/pages/PaymentLoadingPage';

// ── Access levels ────────────────────────────────────────────────
export const ACCESS = {
    PUBLIC: 'PUBLIC',
    GUEST_ONLY: 'GUEST_ONLY',      // unauthenticated only — logged-in → /home
    PROTECTED: 'PROTECTED',        // any logged-in user
    FRANCHISE_ADMIN: 'FRANCHISE_ADMIN',
    ADMIN: 'FRANCHISE_ADMIN',      // alias for convenience
    STORE_MANAGER: 'STORE_MANAGER',
};

// ── Route definitions ────────────────────────────────────────────
export const ROUTES = [
    // Public
    { path: '/', element: <LandingPage />, access: ACCESS.GUEST_ONLY },
    { path: '/home', element: <HomePage />, access: ACCESS.PROTECTED },
    { path: '/menu', element: <ProductListPage />, access: ACCESS.PUBLIC },
    { path: '/menu/:id', element: <ProductDetailPage />, access: ACCESS.PUBLIC },
    { path: '/login', element: <LoginPage />, access: ACCESS.PUBLIC },
    { path: '/forgot-password', element: <ForgotPasswordPage />, access: ACCESS.PUBLIC },

    // Protected — any logged-in user
    { path: '/cart', element: <CartPage />, access: ACCESS.PUBLIC },
    { path: '/checkout', element: <CheckoutPage />, access: ACCESS.PROTECTED },
    { path: '/payment/result', element: <PaymentResultPage />, access: ACCESS.PUBLIC },
    { path: '/payment/success', element: <PaymentSuccessPage />, access: ACCESS.PUBLIC },
    { path: '/payment/failed', element: <PaymentFailedPage />, access: ACCESS.PUBLIC },
    { path: '/payment/loading', element: <PaymentLoadingPage />, access: ACCESS.PUBLIC },
    { path: '/orders', element: <OrderListPage />, access: ACCESS.PROTECTED },
    { path: '/orders/:id', element: <OrderDetailPage />, access: ACCESS.PROTECTED },
    { path: '/transactions', element: <TransactionHistoryPage />, access: ACCESS.PROTECTED },
];

// ── Admin / Store-Manager routes (rendered inside AdminLayout) ───
export const ADMIN_ROUTES = [
    // Admin + Store Manager — role-based view on same route
    { path: '/admin', element: <AdminDashboardPage />, access: [ACCESS.ADMIN, ACCESS.STORE_MANAGER] },
    { path: '/admin/orders', element: <OrderManagementPage />, access: ACCESS.ADMIN },
    { path: '/admin/orders/:id', element: <AdminOrderDetailPage />, access: [ACCESS.ADMIN, ACCESS.STORE_MANAGER] },

    // Product CRUD
    { path: '/admin/catalog', element: <AdminProductCrudPage />, access: ACCESS.PUBLIC },
    { path: '/admin/catalog/:productId', element: <AdminProductDetailPage />, access: ACCESS.PUBLIC },
    { path: '/admin/stores/products', element: <AdminProductCrudPage />, access: ACCESS.PUBLIC },

    // Category Management
    { path: '/admin/categories', element: <AdminCategoryPage />, access: ACCESS.PUBLIC },

    // Store Manager (also accessible by ADMIN)
    { path: '/admin/stores/orders', element: <StoreOrderManagementPage />, access: [ACCESS.ADMIN, ACCESS.STORE_MANAGER] },

    // POS Terminal
    { path: '/admin/pos', element: <POSPage />, access: [ACCESS.ADMIN, ACCESS.STORE_MANAGER] },
];
