import axios from 'axios';

const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_URL || "http://localhost:8080",
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 10000,
  withCredentials: true, 
});

// Response Interceptor
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    const status = error.response?.status;
    if (status === 401 || status === 403) {
      console.warn(`[apiClient] ${status} - Session expired or unauthorized. Redirecting to login.`);
      
      // Clear all auth data immediately
      localStorage.removeItem('auth_user');
      
      // Force a full page reload to the login page to kill all stale state
      if (typeof window !== 'undefined' && !window.location.pathname.includes('/login')) {
        const from = window.location.pathname + window.location.search;
        window.location.href = `/login?from=${encodeURIComponent(from)}`;
      }
    }
    return Promise.reject(error);
  }
);

export default apiClient;
