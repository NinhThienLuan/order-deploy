import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from '../features/auth/AuthContext';
import { ACCESS } from './routes.config';

const STAFF_ROLES = ['FRANCHISE_ADMIN', 'STORE_MANAGER'];
const isStaff = (user) => user && STAFF_ROLES.includes(user.role);

/**
 * ProtectedRoute
 *
 * Wraps a route element and enforces access control:
 *  - PUBLIC           → always renders
 *  - GUEST_ONLY       → renders only when NOT logged in; logged-in users → /home (staff → /admin)
 *  - PROTECTED        → requires any logged-in *customer*; staff are redirected to /admin
 *  - FRANCHISE_ADMIN  → requires role === 'FRANCHISE_ADMIN'
 *  - STORE_MANAGER    → requires role === 'STORE_MANAGER'
 *  - array of roles   → requires role to match any entry
 *
 * On access denied:
 *  - Not logged in        → redirect to /login (with `from` state preserved)
 *  - Staff on customer route → redirect to /admin
 *  - Wrong role           → redirect to /home
 */
const ProtectedRoute = ({ element, access }) => {
    const { user } = useAuth();
    const location = useLocation();

    // Guest-only — redirect logged-in users away
    if (access === ACCESS.GUEST_ONLY) {
        if (!user) return element;
        return <Navigate to={isStaff(user) ? '/admin' : '/home'} replace />;
    }

    // Public — no restriction
    if (access === ACCESS.PUBLIC) return element;

    // Not logged in → redirect to /login
    if (!user) {
        return <Navigate to="/login" state={{ from: location }} replace />;
    }

    const allowedRoles = Array.isArray(access) ? access : [access];

    // PROTECTED = customer-only routes; staff should not be here → send to /admin
    if (allowedRoles.includes(ACCESS.PROTECTED)) {
        return isStaff(user)
            ? <Navigate to="/admin" replace />
            : element;
    }

    // Role-based check
    if (allowedRoles.includes(user.role)) return element;

    // Logged in but wrong role → staff go to /admin, others to /home
    return <Navigate to={isStaff(user) ? '/admin' : '/home'} replace />;
};

export default ProtectedRoute;
