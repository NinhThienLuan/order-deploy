import { createContext, useContext, useState, useCallback } from 'react';
import apiClient from '@/services/apiClient';

const AuthContext = createContext(null);

const USER_KEY = 'auth_user';

const loadUser = () => {
    try {
        const stored = localStorage.getItem(USER_KEY);
        return stored ? JSON.parse(stored) : null;
    } catch {
        return null;
    }
};

export const AuthProvider = ({ children }) => {
    const [user, setUser] = useState(loadUser);

    // Re-read user from localStorage (call this after the login page sets it)
    const refreshUser = useCallback(() => {
        setUser(loadUser());
    }, []);

    const logout = useCallback(async () => {
        try {
            await apiClient.post('/api/v1/auth/logout');
        } catch {
            // Proceed even if request fails
        } finally {
            localStorage.removeItem(USER_KEY);
            setUser(null);
        }
    }, []);

    return (
        <AuthContext.Provider value={{ user, logout, refreshUser }}>
            {children}
        </AuthContext.Provider>
    );
};

// eslint-disable-next-line react-refresh/only-export-components
export const useAuth = () => {
    const ctx = useContext(AuthContext);
    if (!ctx) throw new Error('useAuth must be used inside <AuthProvider>');
    return ctx;
};

