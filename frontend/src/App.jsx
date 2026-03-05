
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import './index.css';

import { CartProvider } from './features/cart/CartContext';
import { AuthProvider } from './features/auth/AuthContext';
import CartSidebar from './features/cart/components/CartSidebar/CartSidebar';

import { ROUTES, ADMIN_ROUTES, ACCESS } from './routes/routes.config';
import ProtectedRoute from './routes/ProtectedRoute';
import AdminLayout from './layouts/AdminLayout';

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

            {/* Catch-all */}
            <Route path="*" element={<NotFound />} />
          </Routes>
        </BrowserRouter>
      </CartProvider>
    </AuthProvider>
  );
}

export default App;
