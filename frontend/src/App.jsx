import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import './index.css';

import { CartProvider } from '@/features/orders/services/CartContext';
import { AuthProvider } from '@/features/authentication/services/AuthContext';
import CartSidebar from '@/features/orders/components/CartSidebar';

import { ROUTES, ADMIN_ROUTES, ACCESS } from './routes/routes.config';
import ProtectedRoute from './routes/ProtectedRoute';
import AdminLayout from './layouts/AdminLayout';

// Import trang OnlineOrder
import OnlineOrderPage from '@/features/onlineOrder/pages/OnlineOrderManagementPage';

const NotFound = () => (
  <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', minHeight: '100vh', fontFamily: 'Inter, sans-serif', color: '#231F1E' }}>
    404 — Page not found
  </div>
);

function App() {
  return (
    <AuthProvider>
      <CartProvider>
        <BrowserRouter>
          {/* Persistent UI — renders on every page */}
          <CartSidebar />

          <Routes>
            {/* Public / customer routes */}
            {ROUTES.map(({ path, element, access }) => (
              <Route
                key={path}
                path={path}
                element={<ProtectedRoute element={element} access={access} />}
              />
            ))}

            {/* Admin routes — AdminLayout renders once; sidebar state persists across navigations */}
            <Route
              element={
                <ProtectedRoute
                  element={<AdminLayout />}
                  access={[ACCESS.ADMIN, ACCESS.STORE_MANAGER]}
                />
              }
            >
              {ADMIN_ROUTES.map(({ path, element, access }) => (
                <Route
                  key={path}
                  path={path}
                  element={<ProtectedRoute element={element} access={access} />}
                />
              ))}
            </Route>

            <Route
              path="/online-order"
              element={
                <ProtectedRoute
                  element={<OnlineOrderPage />}
                  access={[ACCESS.ADMIN, ACCESS.STORE_MANAGER]}
                />
              }
            />

            <Route path="*" element={<NotFound />} />
          </Routes>
        </BrowserRouter>
      </CartProvider>
    </AuthProvider>
  );
}

export default App;